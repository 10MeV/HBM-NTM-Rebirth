package com.hbm.ntm.fluid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.ContainerFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.FluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.PheromoneFluidTrait;
import com.hbm.ntm.fluid.trait.PoisonFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PwrModeratorFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.fluid.trait.ToxinFluidTrait;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEffects;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class HbmFluidTraitConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "hbmFluidTraits.json";
    private static final String TEMPLATE_FILE = "_hbmFluidTraits.json";
    private static JsonObject builtInTraits;
    private static volatile LoadReport report = new LoadReport(false, 0, 0, 0, List.of());

    public static LoadReport initialize(Path configDir) {
        captureBuiltIns();
        Path hbmDir = configDir.resolve("hbm");
        Path config = hbmDir.resolve(CONFIG_FILE);
        Path template = hbmDir.resolve(TEMPLATE_FILE);
        try {
            Files.createDirectories(hbmDir);
            if (Files.notExists(config)) {
                writeTemplate(template);
                resetToBuiltIns();
                return remember(new LoadReport(false, HbmFluids.all().size(), 0, 0, List.of()));
            }
            try (Reader reader = Files.newBufferedReader(config)) {
                resetToBuiltIns();
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                return remember(readTraits(json));
            }
        } catch (IOException | RuntimeException ex) {
            HbmNtm.LOGGER.warn("Failed to load HBM fluid trait config, using built-in traits.", ex);
            resetToBuiltIns();
            return remember(new LoadReport(false, HbmFluids.all().size(), 0, 0, List.of("failed to read " + CONFIG_FILE)));
        }
    }

    public static LoadReport loadReport() {
        return report;
    }

    public static void resetToCapturedBuiltIns() {
        resetToBuiltIns();
    }

    public static void recaptureBuiltIns() {
        builtInTraits = HbmFluids.fluidTraitsJson().deepCopy();
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static void writeTemplate(Path template) throws IOException {
        try (Writer writer = Files.newBufferedWriter(template)) {
            GSON.toJson(builtInTraits, writer);
        }
    }

    private static void captureBuiltIns() {
        if (builtInTraits == null) {
            builtInTraits = HbmFluids.fluidTraitsJson().deepCopy();
        }
    }

    private static void resetToBuiltIns() {
        if (builtInTraits != null) {
            readTraits(builtInTraits.deepCopy());
        }
    }

    private static LoadReport readTraits(JsonObject root) {
        List<String> warnings = new ArrayList<>();
        int fluids = 0;
        int traits = 0;
        int skipped = 0;

        if (root == null) {
            warnings.add("empty root");
            return new LoadReport(true, 0, 0, 1, warnings);
        }

        for (Map.Entry<String, JsonElement> fluidEntry : root.entrySet()) {
            FluidType type = HbmFluidJsonUtil.readFluidReference(fluidEntry.getKey());
            if (type == HbmFluids.NONE && !HbmFluidJsonUtil.isExplicitNoneReference(fluidEntry.getKey())) {
                throw new IllegalArgumentException("unknown fluid " + fluidEntry.getKey());
            }
            if (!fluidEntry.getValue().isJsonObject()) {
                skipped++;
                warnings.add("non-object trait block for " + fluidEntry.getKey());
                continue;
            }

            JsonObject traitRoot = fluidEntry.getValue().getAsJsonObject();
            TraitParseResult parsed = readTraitBlock(type.getName(), traitRoot);
            warnings.addAll(parsed.warnings());
            skipped += parsed.skipped();
            traits += parsed.traitCount();
            type.setTraits(parsed.traits());
            fluids++;
        }

        return new LoadReport(true, fluids, traits, skipped, warnings);
    }

    static TraitParseResult readTraitBlock(String fluidName, JsonObject traitRoot) {
        List<String> warnings = new ArrayList<>();
        List<FluidTrait> parsedTraits = new ArrayList<>();
        int traits = 0;
        int skipped = 0;

        if (traitRoot == null) {
            return new TraitParseResult(List.of(), 0, 0, List.of("empty trait block for " + fluidName));
        }

        for (Map.Entry<String, JsonElement> traitEntry : traitRoot.entrySet()) {
            if (!traitEntry.getValue().isJsonObject()) {
                skipped++;
                warnings.add(fluidName + "." + traitEntry.getKey() + " is not an object");
                continue;
            }
            try {
                FluidTrait trait = readTrait(traitEntry.getKey(), traitEntry.getValue().getAsJsonObject());
                if (trait == null) {
                    skipped++;
                    warnings.add(fluidName + "." + traitEntry.getKey() + " is unsupported");
                    continue;
                }
                parsedTraits.add(trait);
                traits++;
            } catch (RuntimeException ex) {
                if (ex instanceof HbmFluidJsonUtil.UnknownFluidReferenceException) {
                    throw ex;
                }
                skipped++;
                warnings.add(fluidName + "." + traitEntry.getKey() + " failed: " + ex.getMessage());
            }
        }

        return new TraitParseResult(List.copyOf(parsedTraits), traits, skipped, List.copyOf(warnings));
    }

    @Nullable
    private static FluidTrait readTrait(String name, JsonObject object) {
        return switch (normalize(name)) {
            case "corrosive" -> new CorrosiveFluidTrait(intValue(object, "rating", 0));
            case "flammable" -> new FlammableFluidTrait(longValue(object, "energy", 0L));
            case "combustible" -> new CombustibleFluidTrait(enumValue(
                    CombustibleFluidTrait.FuelGrade.class, stringValue(object, "grade", "LOW"),
                    CombustibleFluidTrait.FuelGrade.LOW), longValue(object, "energy", 0L));
            case "polluting" -> readPolluting(object);
            case "heatable" -> readHeatable(object);
            case "coolable" -> readCoolable(object);
            case "pwrmoderator" -> new PwrModeratorFluidTrait(doubleValue(object, "multiplier", 1.0D));
            case "poison" -> new PoisonFluidTrait(booleanValue(object, "withering", false), intValue(object, "level", 0));
            case "toxin" -> readToxin(object);
            case "ventradiation" -> new VentRadiationFluidTrait(floatValue(object, "radiation", 0.0F));
            case "pheromone" -> new PheromoneFluidTrait(intValue(object, "type", 0));
            case "container" -> readContainer(object);
            case "gaseous" -> SimpleFluidTraits.GASEOUS;
            case "gaseous_art" -> SimpleFluidTraits.GASEOUS_AT_ROOM_TEMPERATURE;
            case "liquid" -> SimpleFluidTraits.LIQUID;
            case "viscous" -> SimpleFluidTraits.VISCOUS;
            case "plasma" -> SimpleFluidTraits.PLASMA;
            case "amat" -> SimpleFluidTraits.ANTIMATTER;
            case "leadcontainer" -> SimpleFluidTraits.LEAD_CONTAINER;
            case "delicious" -> SimpleFluidTraits.DELICIOUS;
            case "noid" -> SimpleFluidTraits.NO_ID;
            case "nocontainer" -> SimpleFluidTraits.NO_CONTAINER;
            case "unsiphonable" -> SimpleFluidTraits.UNSIPHONABLE;
            default -> readLegacyTrait(name, object);
        };
    }

    @Nullable
    private static FluidTrait readLegacyTrait(String name, JsonObject object) {
        Class<? extends FluidTrait> traitClass = com.hbm.inventory.fluid.trait.FluidTrait.traitNameMap.get(normalize(name));
        if (traitClass == null) {
            return null;
        }
        try {
            FluidTrait trait = traitClass.getDeclaredConstructor().newInstance();
            trait.deserializeJSON(object);
            return trait;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("legacy trait " + name + " could not be constructed", ex);
        }
    }

    private static PollutingFluidTrait readPolluting(JsonObject object) {
        PollutingFluidTrait trait = new PollutingFluidTrait();
        JsonObject release = object(object, "release");
        if (release != null) {
            for (PollutingFluidTrait.PollutionKind kind : PollutingFluidTrait.PollutionKind.orderedValues()) {
                Float value = floatValue(release, kind.legacyName());
                if (value == null) {
                    value = floatValue(release, kind.name());
                }
                if (value != null) {
                    trait.release(kind, value);
                }
            }
        }
        JsonObject burn = object(object, "burn");
        if (burn != null) {
            for (PollutingFluidTrait.PollutionKind kind : PollutingFluidTrait.PollutionKind.orderedValues()) {
                Float value = floatValue(burn, kind.legacyName());
                if (value == null) {
                    value = floatValue(burn, kind.name());
                }
                if (value != null) {
                    trait.burn(kind, value);
                }
            }
        }
        return trait;
    }

    private static HeatableFluidTrait readHeatable(JsonObject object) {
        HeatableFluidTrait trait = new HeatableFluidTrait();
        JsonArray steps = array(object, "steps");
        if (steps != null) {
            for (JsonElement element : steps) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject step = element.getAsJsonObject();
                trait.addStep(
                        intValue(step, "heatReq", 0),
                        intValue(step, "amountReq", 0),
                        fluidValue(step, "typeProduced", "NONE"),
                        intValue(step, "amountProd", 0));
            }
        }
        for (HeatableFluidTrait.HeatingType type : HeatableFluidTrait.HeatingType.values()) {
            Double efficiency = doubleValue(object, type.name());
            if (efficiency != null) {
                trait.setEfficiency(type, efficiency);
            }
        }
        return trait;
    }

    private static CoolableFluidTrait readCoolable(JsonObject object) {
        CoolableFluidTrait trait = new CoolableFluidTrait(
                fluidValue(object, "coolsTo", "NONE"),
                intValue(object, "amountReq", 0),
                intValue(object, "amountProd", 0),
                intValue(object, "heatEnergy", 0));
        for (CoolableFluidTrait.CoolingType type : CoolableFluidTrait.CoolingType.values()) {
            Double efficiency = doubleValue(object, type.name());
            if (efficiency != null) {
                trait.setEfficiency(type, efficiency);
            }
        }
        return trait;
    }

    private static ToxinFluidTrait readToxin(JsonObject object) {
        ToxinFluidTrait trait = new ToxinFluidTrait();
        JsonArray entries = array(object, "entries");
        if (entries == null) {
            return trait;
        }
        for (JsonElement element : entries) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String type = normalize(stringValue(entry, "type", ""));
            HazardClass hazardClass = hazardClass(stringValue(entry, "masktype", "NONE"));
            boolean fullBody = booleanValue(entry, "hazmat", false);
            if ("directdamage".equals(type)) {
                trait.addEntry(new ToxinFluidTrait.DirectDamage(
                        damageId(stringValue(entry, "source", "cloud")),
                        floatValue(entry, "amount", 0.0F),
                        intValue(entry, "delay", 20),
                        hazardClass,
                        fullBody));
            } else if ("effects".equals(type)) {
                ToxinFluidTrait.EffectApplication application = new ToxinFluidTrait.EffectApplication(hazardClass, fullBody);
                JsonArray effects = array(entry, "effects");
                if (effects != null) {
                    for (JsonElement effectElement : effects) {
                        readEffect(effectElement, application);
                    }
                }
                trait.addEntry(application);
            }
        }
        return trait;
    }

    private static void readEffect(JsonElement element, ToxinFluidTrait.EffectApplication application) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() >= 4) {
                application.addEffect(effectId(array.get(0)), array.get(1).getAsInt(), array.get(2).getAsInt(), array.get(3).getAsBoolean());
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            application.addEffect(resource(stringValue(object, "effect", "minecraft:poison")),
                    intValue(object, "duration", intValue(object, "durationTicks", 0)),
                    intValue(object, "amplifier", 0),
                    booleanValue(object, "ambient", false));
        }
    }

    private static ContainerFluidTrait readContainer(JsonObject object) {
        ContainerFluidTrait trait = new ContainerFluidTrait();
        Integer canisterColor = intValueOrNull(object, "canisterColor");
        if (canisterColor != null) {
            trait.withCanister(canisterColor);
        }
        Integer bottleColor = intValueOrNull(object, "gasTankBottleColor");
        Integer labelColor = intValueOrNull(object, "gasTankLabelColor");
        if (bottleColor != null && labelColor != null) {
            trait.withGasTank(bottleColor, labelColor);
        }
        return trait;
    }

    private static HazardClass hazardClass(String value) {
        if (value == null || value.isBlank() || "NONE".equalsIgnoreCase(value)) {
            return null;
        }
        return enumValue(HazardClass.class, value, null);
    }

    private static ResourceLocation effectId(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return legacyPotionId(element.getAsInt());
        }
        return resource(element.getAsString());
    }

    private static ResourceLocation legacyPotionId(int id) {
        return switch (id) {
            case 2 -> new ResourceLocation("minecraft", "slowness");
            case 4 -> new ResourceLocation("minecraft", "mining_fatigue");
            case 7 -> new ResourceLocation("minecraft", "instant_damage");
            case 9 -> new ResourceLocation("minecraft", "nausea");
            case 15 -> new ResourceLocation("minecraft", "blindness");
            case 17 -> new ResourceLocation("minecraft", "hunger");
            case 18 -> new ResourceLocation("minecraft", "weakness");
            case 19 -> new ResourceLocation("minecraft", "poison");
            case 20 -> new ResourceLocation("minecraft", "wither");
            case 72 -> ModEffects.POTION_SICKNESS.getId();
            case 73 -> ModEffects.DEATH.getId();
            default -> new ResourceLocation("minecraft", "poison");
        };
    }

    private static ResourceLocation damageId(String value) {
        return ModDamageSources.legacyKey(value)
                .map(key -> key.location())
                .orElseGet(() -> resource(value == null || value.isBlank() ? HbmNtm.MOD_ID + ":cloud" : value));
    }

    private static ResourceLocation resource(String value) {
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        return parsed == null ? new ResourceLocation(HbmNtm.MOD_ID, value) : parsed;
    }

    private static JsonObject object(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonArray array(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static String stringValue(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsString();
    }

    private static int intValue(JsonObject object, String key, int fallback) {
        Integer value = intValueOrNull(object, key);
        return value == null ? fallback : value;
    }

    private static Integer intValueOrNull(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null ? null : intValue(element);
    }

    private static int intValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Integer.decode(element.getAsString());
        }
        return element.getAsInt();
    }

    private static long longValue(JsonObject object, String key, long fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsLong();
    }

    @Nullable
    private static Float floatValue(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null ? null : element.getAsFloat();
    }

    private static float floatValue(JsonObject object, String key, float fallback) {
        Float value = floatValue(object, key);
        return value == null ? fallback : value;
    }

    @Nullable
    private static Double doubleValue(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null ? null : element.getAsDouble();
    }

    private static double doubleValue(JsonObject object, String key, double fallback) {
        Double value = doubleValue(object, key);
        return value == null ? fallback : value;
    }

    private static boolean booleanValue(JsonObject object, String key, boolean fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsBoolean();
    }

    private static FluidType fluidValue(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        if (element == null) {
            return HbmFluidJsonUtil.readFluidReference(fallback);
        }
        FluidType type = HbmFluidJsonUtil.readFluidReference(element);
        if (type == HbmFluids.NONE && !HbmFluidJsonUtil.isExplicitNoneReference(element)) {
            throw HbmFluidJsonUtil.unknownFluidReference(key, element);
        }
        return type;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.US));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.US);
    }

    public record LoadReport(boolean loadedConfig, int fluids, int traits, int skipped, List<String> warnings) {
        public String summary() {
            return "fluid traits loadedConfig=" + loadedConfig + " fluids=" + fluids + " traits=" + traits + " skipped=" + skipped;
        }
    }

    public record TraitParseResult(List<FluidTrait> traits, int traitCount, int skipped, List<String> warnings) {
    }

    private HbmFluidTraitConfig() {
    }
}
