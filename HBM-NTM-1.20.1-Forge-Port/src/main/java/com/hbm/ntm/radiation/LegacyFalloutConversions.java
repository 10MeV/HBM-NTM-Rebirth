package com.hbm.ntm.radiation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.block.LegacySellafieldBlock;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

public final class LegacyFalloutConversions {
    private static final String CONFIG_FOLDER = "hbmConfig";
    private static final String CONFIG_FILE = "hbmFallout.json";
    private static final String TEMPLATE_FILE = "_hbmFallout.json";
    private static final double WOOD_EFFECT_RANGE = 65.0D;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Entry> DEFAULT_ENTRIES = defaultEntries();

    private static volatile List<Entry> entries = DEFAULT_ENTRIES;
    private static volatile LoadReport report = LoadReport.defaults(null, null, false, List.of());

    public static LoadReport initialize(Path configDirectory) {
        Path folder = configDirectory.resolve(CONFIG_FOLDER);
        Path config = folder.resolve(CONFIG_FILE);
        Path template = folder.resolve(TEMPLATE_FILE);
        List<String> warnings = new ArrayList<>();
        boolean wroteTemplate = false;

        try {
            Files.createDirectories(folder);
            if (Files.notExists(config)) {
                writeTemplate(template);
                wroteTemplate = true;
            }
        } catch (Exception exception) {
            warnings.add("Could not write fallout template: " + exception.getMessage());
        }

        if (Files.notExists(config)) {
            entries = DEFAULT_ENTRIES;
            return remember(LoadReport.defaults(config, template, wroteTemplate, warnings));
        }

        List<Entry> loaded = readConfig(config, warnings);
        if (loaded == null || loaded.isEmpty()) {
            entries = DEFAULT_ENTRIES;
            warnings.add("No usable custom entries found; using built-in fallout table.");
            return remember(LoadReport.defaults(config, template, wroteTemplate, warnings));
        }

        entries = List.copyOf(loaded);
        return remember(new LoadReport(true, false, entries.size(), config, template, wroteTemplate, List.copyOf(warnings)));
    }

    public static Result apply(Level level, BlockPos pos, BlockState state, double distancePercent) {
        Context context = new Context(level, pos, state, distancePercent);
        for (Entry entry : entries) {
            Result result = entry.apply(context);
            if (result.matched()) {
                return result;
            }
        }
        return Result.noMatch();
    }

    public static LoadReport loadReport() {
        return report;
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static List<Entry> defaultEntries() {
        List<Entry> defaults = new ArrayList<>();

        defaults.add(entry(matchesBlock(Blocks.MUSHROOM_STEM), WOOD_EFFECT_RANGE, false, preserveAxisLegacy("waste_log", 1)));
        defaults.add(entry(matchesTag(BlockTags.LOGS), WOOD_EFFECT_RANGE, false, preserveAxisLegacy("waste_log", 1)));
        defaults.add(entry(matchesBlock(Blocks.RED_MUSHROOM_BLOCK), WOOD_EFFECT_RANGE, false, block(Blocks.AIR, 1)));
        defaults.add(entry(matchesBlock(Blocks.BROWN_MUSHROOM_BLOCK), WOOD_EFFECT_RANGE, false, block(Blocks.AIR, 1)));
        defaults.add(entry(matchesBlock(Blocks.SNOW), WOOD_EFFECT_RANGE, false, block(Blocks.AIR, 1)));
        defaults.add(entry(matchesTag(BlockTags.PLANKS), WOOD_EFFECT_RANGE, false, legacy("waste_planks", 1)));
        defaults.add(entry(LegacyFalloutConversions::isLegacyWoodenMaterial, WOOD_EFFECT_RANGE, false, block(Blocks.AIR, 1)));
        defaults.add(entry(LegacyFalloutConversions::isLegacyLeafOrPlant, WOOD_EFFECT_RANGE, false, block(Blocks.AIR, 1)));
        defaults.add(entry(matchesTag(BlockTags.LEAVES), 60.0D, 100.0D, false, legacy("waste_leaves", 1)));

        defaults.add(entry(matchesBlock(Blocks.MOSSY_COBBLESTONE), 100.0D, false, block(Blocks.COAL_ORE, 1)));
        defaults.add(entry(matchesLegacy("ore_nether_uranium"), 100.0D, false,
                legacy("ore_nether_schrabidium", 1),
                legacy("ore_nether_uranium_scorched", 99)));

        for (int distanceBand = 1; distanceBand <= 10; distanceBand++) {
            double maxDistance = distanceBand * 5.0D;
            int level = 10 - distanceBand;
            defaults.add(entry(matchesBlock(Blocks.COAL_ORE), 0.0D, maxDistance, 0.5D, true,
                    legacyLevel("ore_sellafield_diamond", level, 3),
                    legacyLevel("ore_sellafield_emerald", level, 2)));
            defaults.add(entry(matchesBlock(Blocks.DEEPSLATE_COAL_ORE), 0.0D, maxDistance, 0.5D, true,
                    legacyLevel("ore_sellafield_diamond", level, 3),
                    legacyLevel("ore_sellafield_emerald", level, 2)));
            defaults.add(entry(matchesLegacy("ore_lignite"), 0.0D, maxDistance, 0.2D, true,
                    legacyLevel("ore_sellafield_diamond", level, 1)));
            defaults.add(entry(matchesLegacy("ore_beryllium"), 0.0D, maxDistance, true,
                    legacyLevel("ore_sellafield_emerald", level, 1)));
            if (level > 4) {
                defaults.add(entry(matchesLegacy("ore_uranium"), 0.0D, maxDistance, true,
                        legacyLevel("ore_sellafield_schrabidium", level, 1),
                        legacyLevel("ore_sellafield_uranium_scorched", level, 9)));
                defaults.add(entry(matchesLegacy("ore_gneiss_uranium"), 0.0D, maxDistance, true,
                        legacyLevel("ore_sellafield_schrabidium", level, 1),
                        legacyLevel("ore_sellafield_uranium_scorched", level, 9)));
            }
            defaults.add(entry(matchesBlock(Blocks.DIAMOND_ORE), 0.0D, maxDistance, true, legacyLevel("ore_sellafield_radgem", level, 1)));
            defaults.add(entry(matchesBlock(Blocks.DEEPSLATE_DIAMOND_ORE), 0.0D, maxDistance, true, legacyLevel("ore_sellafield_radgem", level, 1)));
            defaults.add(entry(matchesBlock(Blocks.BEDROCK), 0.0D, maxDistance, true, bedrockSellafield(level, 1)));
            defaults.add(entry(matchesLegacy("ore_bedrock"), 0.0D, maxDistance, true, bedrockSellafield(level, 1)));
            defaults.add(entry(matchesLegacy("ore_bedrock_oil"), 0.0D, maxDistance, true, bedrockSellafield(level, 1)));
            defaults.add(entry(matchesLegacy("sellafield_bedrock"), 0.0D, maxDistance, true, bedrockSellafield(level, 1)));
            defaults.add(entry(LegacyFalloutConversions::isLegacyIronMaterial, 0.0D, maxDistance, true, slaked(level, 1)));
            defaults.add(entry(LegacyFalloutConversions::isLegacyRockMaterial, 0.0D, maxDistance, true, slaked(level, 1)));
            defaults.add(entry(LegacyFalloutConversions::isLegacySandMaterial, 0.0D, maxDistance, true, slaked(level, 1)));
            defaults.add(entry(LegacyFalloutConversions::isLegacyGroundMaterial, 0.0D, maxDistance, true, slaked(level, 1)));
            if (distanceBand <= 9) {
                defaults.add(entry(LegacyFalloutConversions::isLegacyGrassMaterial, 0.0D, maxDistance, true, slaked(level, 1)));
            }
        }
        defaults.add(entry(matchesBlock(Blocks.MYCELIUM), 100.0D, false, legacy("waste_mycelium", 1)));
        defaults.add(entry(matchesBlock(Blocks.SAND), 0.0D, 100.0D, 0.05D, false, legacy("waste_trinitite", 1)));
        defaults.add(entry(matchesBlock(Blocks.RED_SAND), 0.0D, 100.0D, 0.05D, false, legacy("waste_trinitite_red", 1)));
        defaults.add(entry(matchesBlock(Blocks.CLAY), 100.0D, false, block(Blocks.TERRACOTTA, 1)));

        return List.copyOf(defaults);
    }

    private static List<Entry> readConfig(Path config, List<String> warnings) {
        try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has("entries") || !root.get("entries").isJsonArray()) {
                warnings.add("Fallout config has no entries array: " + config);
                return null;
            }

            List<Entry> loaded = new ArrayList<>();
            JsonArray definitions = root.getAsJsonArray("entries");
            for (int index = 0; index < definitions.size(); index++) {
                Entry entry = readEntry(definitions.get(index), warnings, index);
                if (entry != null) {
                    loaded.add(entry);
                }
            }
            return loaded;
        } catch (Exception exception) {
            warnings.add("Could not read fallout config: " + exception.getMessage());
            return null;
        }
    }

    private static Entry readEntry(JsonElement element, List<String> warnings, int index) {
        if (!element.isJsonObject()) {
            warnings.add("Entry #" + index + " is not an object.");
            return null;
        }

        JsonObject json = element.getAsJsonObject();
        List<Predicate<Context>> matchers = new ArrayList<>();
        Integer matchesMeta = optionalInt(json, "matchesMeta");

        if (json.has("matchesBlock")) {
            Predicate<Context> matcher = configuredBlockMatcher(optionalString(json, "matchesBlock"), matchesMeta, warnings, index);
            if (matcher == null) {
                return null;
            }
            matchers.add(matcher);
        }
        if (json.has("matchesTag")) {
            String tagName = optionalString(json, "matchesTag");
            ResourceLocation id = parseId(tagName, "minecraft");
            if (id == null) {
                warnings.add("Entry #" + index + " has invalid matchesTag '" + tagName + "'.");
                return null;
            }
            matchers.add(matchesTag(TagKey.create(Registries.BLOCK, id)));
        }
        if (json.has("matchesMaterial")) {
            Predicate<Context> matcher = configuredMaterialMatcher(optionalString(json, "matchesMaterial"), warnings, index);
            if (matcher == null) {
                return null;
            }
            matchers.add(matcher);
        }
        if (optionalBoolean(json, "mustBeOpaque", false)) {
            matchers.add(context -> context.state().isSolidRender(context.level(), context.pos()));
        }
        if (matchers.isEmpty()) {
            warnings.add("Entry #" + index + " has no supported matcher.");
            return null;
        }

        List<WeightedOutcome> primary = readOutcomes(json.get("primarySubstitution"), warnings, index);
        List<WeightedOutcome> secondary = readOutcomes(json.get("secondarySubstitutions"), warnings, index);
        if (primary.isEmpty() && secondary.isEmpty()) {
            warnings.add("Entry #" + index + " has no usable substitution.");
            return null;
        }

        double chance = clamp(optionalDouble(json, "chance", 1.0D), 0.0D, 1.0D);
        double min = optionalDouble(json, "minimumDistancePercent", 0.0D);
        double max = optionalDouble(json, "maximumDistancePercent", 100.0D);
        double falloff = optionalDouble(json, "falloffStartFactor", 0.9D);
        boolean restrictDepth = optionalBoolean(json, "restrictDepth", false);
        Predicate<Context> matcher = context -> matchers.stream().allMatch(rule -> rule.test(context));
        return new Entry(matcher, primary, secondary, chance, min, max, falloff, restrictDepth);
    }

    private static Predicate<Context> configuredBlockMatcher(String name, Integer metadata, List<String> warnings, int index) {
        String normalized = normalizeName(name);
        if (metadata != null && metadata == 10
                && ("minecraft:red_mushroom_block".equals(normalized) || "minecraft:brown_mushroom_block".equals(normalized))) {
            return matchesBlock(Blocks.MUSHROOM_STEM);
        }
        if ("minecraft:log".equals(normalized) || "minecraft:log2".equals(normalized)) {
            return matchesTag(BlockTags.LOGS);
        }
        if ("minecraft:leaves".equals(normalized) || "minecraft:leaves2".equals(normalized)) {
            return matchesTag(BlockTags.LEAVES);
        }
        if ("minecraft:snow_layer".equals(normalized)) {
            return matchesBlock(Blocks.SNOW);
        }
        if ("minecraft:grass".equals(normalized)) {
            return matchesBlock(Blocks.GRASS_BLOCK);
        }
        if ("minecraft:sand".equals(normalized) && metadata != null && metadata == 1) {
            return matchesBlock(Blocks.RED_SAND);
        }

        Block block = resolveBlock(normalized);
        if (block == null) {
            warnings.add("Entry #" + index + " has unavailable matchesBlock '" + name + "'.");
            return null;
        }
        if (metadata != null && block.defaultBlockState().hasProperty(LegacySellafieldBlock.LEVEL)) {
            int level = clamp(metadata, 0, 5);
            return context -> context.state().is(block)
                    && context.state().getValue(LegacySellafieldBlock.LEVEL) == level;
        }
        if (metadata != null && block.defaultBlockState().hasProperty(LegacySellafieldSlakedBlock.LEVEL)) {
            int level = clamp(metadata, 0, 15);
            return context -> context.state().is(block)
                    && context.state().getValue(LegacySellafieldSlakedBlock.LEVEL) == level;
        }
        return matchesBlock(block);
    }

    private static Predicate<Context> configuredMaterialMatcher(String name, List<String> warnings, int index) {
        if (name == null) {
            warnings.add("Entry #" + index + " has an empty matchesMaterial.");
            return null;
        }

        return switch (name.toLowerCase(Locale.ROOT)) {
            case "wood" -> context -> context.state().is(BlockTags.LOGS)
                    || context.state().is(BlockTags.PLANKS)
                    || isLegacyWoodenMaterial(context);
            case "leaves" -> context -> context.state().is(BlockTags.LEAVES)
                    || context.state().is(ModBlocks.WASTE_LEAVES.get());
            case "plants" -> LegacyFalloutConversions::isLegacyPlantMaterial;
            case "vine" -> LegacyFalloutConversions::isLegacyVineMaterial;
            case "grass" -> LegacyFalloutConversions::isLegacyGrassMaterial;
            case "ground" -> LegacyFalloutConversions::isLegacyGroundMaterial;
            case "rock" -> LegacyFalloutConversions::isLegacyRockMaterial;
            case "sand" -> LegacyFalloutConversions::isLegacySandMaterial;
            case "iron", "anvil" -> LegacyFalloutConversions::isLegacyIronMaterial;
            case "clay" -> matchesBlock(Blocks.CLAY);
            case "snow" -> matchesBlock(Blocks.SNOW);
            default -> {
                warnings.add("Entry #" + index + " uses unported material matcher '" + name + "'.");
                yield null;
            }
        };
    }

    private static List<WeightedOutcome> readOutcomes(JsonElement element, List<String> warnings, int entryIndex) {
        if (element == null || !element.isJsonArray()) {
            return List.of();
        }

        List<WeightedOutcome> outcomes = new ArrayList<>();
        JsonArray json = element.getAsJsonArray();
        for (int outcomeIndex = 0; outcomeIndex < json.size(); outcomeIndex++) {
            JsonElement substitution = json.get(outcomeIndex);
            if (!substitution.isJsonArray() || substitution.getAsJsonArray().size() < 3) {
                warnings.add("Entry #" + entryIndex + " has a malformed substitution at index " + outcomeIndex + ".");
                continue;
            }

            JsonArray values = substitution.getAsJsonArray();
            String name = values.get(0).getAsString();
            int metadata = values.get(1).getAsInt();
            int weight = values.get(2).getAsInt();
            Block block = resolveBlock(normalizeName(name));
            if (block == null) {
                warnings.add("Entry #" + entryIndex + " substitutes unavailable block '" + name + "'.");
                continue;
            }
            if (metadata != 0
                    && !block.defaultBlockState().hasProperty(LegacySellafieldBlock.LEVEL)
                    && !block.defaultBlockState().hasProperty(LegacySellafieldSlakedBlock.LEVEL)) {
                warnings.add("Entry #" + entryIndex + " metadata for '" + name + "' has no modern state mapping and was skipped.");
                continue;
            }
            outcomes.add(new WeightedOutcome(weight, context -> configuredState(block, metadata, context)));
        }
        return outcomes;
    }

    private static BlockState configuredState(Block block, int metadata, Context context) {
        BlockState replacement = block.defaultBlockState();
        if (replacement.hasProperty(LegacySellafieldBlock.LEVEL)) {
            replacement = replacement.setValue(LegacySellafieldBlock.LEVEL, clamp(metadata, 0, 5));
        }
        if (replacement.hasProperty(LegacySellafieldSlakedBlock.LEVEL)) {
            replacement = replacement.setValue(LegacySellafieldSlakedBlock.LEVEL, clamp(metadata, 0, 15));
        }
        if (replacement.hasProperty(RotatedPillarBlock.AXIS) && context.state().hasProperty(RotatedPillarBlock.AXIS)) {
            replacement = replacement.setValue(RotatedPillarBlock.AXIS, context.state().getValue(RotatedPillarBlock.AXIS));
        }
        return replacement;
    }

    private static Block resolveBlock(String normalizedName) {
        String normalized = normalizeName(normalizedName);
        Block alias = switch (normalized) {
            case "minecraft:snow_layer" -> Blocks.SNOW;
            case "minecraft:hardened_clay" -> Blocks.TERRACOTTA;
            case "minecraft:grass" -> Blocks.GRASS_BLOCK;
            default -> null;
        };
        if (alias != null) {
            return alias;
        }

        ResourceLocation id = parseId(normalized, "minecraft");
        if (id != null && ForgeRegistries.BLOCKS.containsKey(id)) {
            return ForgeRegistries.BLOCKS.getValue(id);
        }

        String path = normalized.contains(":") ? normalized.substring(normalized.indexOf(':') + 1) : normalized;
        RegistryObject<? extends Block> legacy = ModBlocks.legacyBlock(path);
        if (legacy != null && legacy.isPresent()) {
            return legacy.get();
        }
        return null;
    }

    private static ResourceLocation parseId(String value, String defaultNamespace) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalizeName(value);
        return ResourceLocation.tryParse(normalized.contains(":") ? normalized : defaultNamespace + ":" + normalized);
    }

    private static String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("tile.") ? normalized.substring(5) : normalized;
    }

    private static void writeTemplate(Path template) throws Exception {
        JsonObject root = new JsonObject();
        JsonArray templateEntries = new JsonArray();
        templateEntries.add(templateEntry("matchesBlock", "minecraft:mushroom_stem", 0.0D, WOOD_EFFECT_RANGE, false, outcome("hbm:waste_log", 0, 1)));
        templateEntries.add(templateEntry("matchesTag", BlockTags.LOGS.location().toString(), 0.0D, WOOD_EFFECT_RANGE, false, outcome("hbm:waste_log", 0, 1)));
        templateEntries.add(templateEntry("matchesBlock", "minecraft:red_mushroom_block", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesBlock", "minecraft:brown_mushroom_block", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesBlock", "minecraft:snow", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesTag", BlockTags.PLANKS.location().toString(), 0.0D, WOOD_EFFECT_RANGE, false, outcome("hbm:waste_planks", 0, 1)));
        templateEntries.add(templateEntry("matchesMaterial", "wood", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesMaterial", "leaves", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesMaterial", "plants", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesMaterial", "vine", 0.0D, WOOD_EFFECT_RANGE, false, outcome("minecraft:air", 0, 1)));
        templateEntries.add(templateEntry("matchesTag", BlockTags.LEAVES.location().toString(), 60.0D, 100.0D, false, outcome("hbm:waste_leaves", 0, 1)));
        templateEntries.add(templateEntry("matchesBlock", "minecraft:mossy_cobblestone", 0.0D, 100.0D, false, outcome("minecraft:coal_ore", 0, 1)));
        templateEntries.add(templateEntry("matchesBlock", "hbm:ore_nether_uranium", 0.0D, 100.0D, false,
                outcome("hbm:ore_nether_schrabidium", 0, 1), outcome("hbm:ore_nether_uranium_scorched", 0, 99)));
        for (int distanceBand = 1; distanceBand <= 10; distanceBand++) {
            int level = 10 - distanceBand;
            double maxDistance = distanceBand * 5.0D;
            templateEntries.add(templateEntryWithChance("matchesBlock", "minecraft:coal_ore", 0.0D, maxDistance, 0.5D, true,
                    outcome("hbm:ore_sellafield_diamond", level, 3), outcome("hbm:ore_sellafield_emerald", level, 2)));
            templateEntries.add(templateEntryWithChance("matchesBlock", "minecraft:deepslate_coal_ore", 0.0D, maxDistance, 0.5D, true,
                    outcome("hbm:ore_sellafield_diamond", level, 3), outcome("hbm:ore_sellafield_emerald", level, 2)));
            templateEntries.add(templateEntryWithChance("matchesBlock", "hbm:ore_lignite", 0.0D, maxDistance, 0.2D, true,
                    outcome("hbm:ore_sellafield_diamond", level, 1)));
            templateEntries.add(templateEntry("matchesBlock", "hbm:ore_beryllium", 0.0D, maxDistance, true,
                    outcome("hbm:ore_sellafield_emerald", level, 1)));
            if (level > 4) {
                templateEntries.add(templateEntry("matchesBlock", "hbm:ore_uranium", 0.0D, maxDistance, true,
                        outcome("hbm:ore_sellafield_schrabidium", level, 1), outcome("hbm:ore_sellafield_uranium_scorched", level, 9)));
                templateEntries.add(templateEntry("matchesBlock", "hbm:ore_gneiss_uranium", 0.0D, maxDistance, true,
                        outcome("hbm:ore_sellafield_schrabidium", level, 1), outcome("hbm:ore_sellafield_uranium_scorched", level, 9)));
            }
            templateEntries.add(templateEntry("matchesBlock", "minecraft:diamond_ore", 0.0D, maxDistance, true,
                    outcome("hbm:ore_sellafield_radgem", level, 1)));
            templateEntries.add(templateEntry("matchesBlock", "minecraft:deepslate_diamond_ore", 0.0D, maxDistance, true,
                    outcome("hbm:ore_sellafield_radgem", level, 1)));
            templateEntries.add(templateEntry("matchesBlock", "minecraft:bedrock", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_bedrock", level, 1)));
            templateEntries.add(templateEntry("matchesBlock", "hbm:ore_bedrock", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_bedrock", level, 1)));
            templateEntries.add(templateEntry("matchesBlock", "hbm:ore_bedrock_oil", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_bedrock", level, 1)));
            templateEntries.add(templateEntry("matchesBlock", "hbm:sellafield_bedrock", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_bedrock", level, 1)));
            templateEntries.add(templateEntry("matchesMaterial", "iron", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_slaked", level, 1)));
            templateEntries.add(templateEntry("matchesMaterial", "ground", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_slaked", level, 1)));
            templateEntries.add(templateEntry("matchesMaterial", "rock", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_slaked", level, 1)));
            templateEntries.add(templateEntry("matchesMaterial", "sand", 0.0D, maxDistance, true,
                    outcome("hbm:sellafield_slaked", level, 1)));
            if (distanceBand <= 9) {
                templateEntries.add(templateEntry("matchesMaterial", "grass", 0.0D, maxDistance, true,
                        outcome("hbm:sellafield_slaked", level, 1)));
            }
        }
        templateEntries.add(templateEntry("matchesBlock", "minecraft:mycelium", 0.0D, 100.0D, false, outcome("hbm:waste_mycelium", 0, 1)));
        templateEntries.add(templateEntryWithChance("matchesBlock", "minecraft:sand", 0.0D, 100.0D, 0.05D, false,
                outcome("hbm:waste_trinitite", 0, 1)));
        templateEntries.add(templateEntryWithChance("matchesBlock", "minecraft:red_sand", 0.0D, 100.0D, 0.05D, false,
                outcome("hbm:waste_trinitite_red", 0, 1)));
        templateEntries.add(templateEntry("matchesBlock", "minecraft:clay", 0.0D, 100.0D, false, outcome("minecraft:terracotta", 0, 1)));
        root.add("entries", templateEntries);
        try (Writer writer = Files.newBufferedWriter(template, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
    }

    private static JsonObject templateEntry(String matcher, String value, double min, double max, boolean restrictDepth,
                                            JsonArray... outcomes) {
        JsonObject entry = new JsonObject();
        entry.addProperty(matcher, value);
        if (min != 0.0D) {
            entry.addProperty("minimumDistancePercent", min);
        }
        if (max != 100.0D) {
            entry.addProperty("maximumDistancePercent", max);
        }
        if (restrictDepth) {
            entry.addProperty("restrictDepth", true);
        }
        JsonArray choices = new JsonArray();
        for (JsonArray outcome : outcomes) {
            choices.add(outcome);
        }
        entry.add("primarySubstitution", choices);
        return entry;
    }

    private static JsonObject templateEntryWithChance(String matcher, String value, double min, double max, double chance,
                                                      boolean restrictDepth, JsonArray... outcomes) {
        JsonObject entry = templateEntry(matcher, value, min, max, restrictDepth, outcomes);
        entry.addProperty("chance", chance);
        return entry;
    }

    private static JsonArray outcome(String block, int metadata, int weight) {
        JsonArray outcome = new JsonArray();
        outcome.add(block);
        outcome.add(metadata);
        outcome.add(weight);
        return outcome;
    }

    private static Entry entry(Predicate<Context> matcher, double maxDistance, boolean restrictDepth, WeightedOutcome... primary) {
        return entry(matcher, 0.0D, maxDistance, restrictDepth, primary);
    }

    private static Entry entry(Predicate<Context> matcher, double minDistance, double maxDistance, boolean restrictDepth, WeightedOutcome... primary) {
        return new Entry(matcher, List.of(primary), List.of(), 1.0D, minDistance, maxDistance, 0.9D, restrictDepth);
    }

    private static Entry entry(Predicate<Context> matcher, double minDistance, double maxDistance, double chance,
                               boolean restrictDepth, WeightedOutcome... primary) {
        return new Entry(matcher, List.of(primary), List.of(), chance, minDistance, maxDistance, 0.9D, restrictDepth);
    }

    private static Predicate<Context> matchesBlock(Block block) {
        return context -> context.state().is(block);
    }

    private static Predicate<Context> matchesTag(TagKey<Block> tag) {
        return context -> context.state().is(tag);
    }

    private static Predicate<Context> matchesLegacy(String legacyName) {
        return context -> {
            RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
            return block != null && block.isPresent() && context.state().is(block.get());
        };
    }

    private static WeightedOutcome block(Block block, int weight) {
        return new WeightedOutcome(weight, context -> block.defaultBlockState());
    }

    private static WeightedOutcome legacy(String legacyName, int weight) {
        return new WeightedOutcome(weight, context -> legacyState(legacyName));
    }

    private static WeightedOutcome slaked(int level, int weight) {
        return legacyLevel("sellafield_slaked", level, weight);
    }

    private static WeightedOutcome bedrockSellafield(int level, int weight) {
        return legacyLevel("sellafield_bedrock", level, weight);
    }

    private static WeightedOutcome legacyLevel(String legacyName, int level, int weight) {
        return new WeightedOutcome(weight, context -> withSlakedLevel(legacyState(legacyName), level));
    }

    private static WeightedOutcome preserveAxisLegacy(String legacyName, int weight) {
        return new WeightedOutcome(weight, context -> {
            BlockState replacement = legacyState(legacyName);
            if (replacement == null) {
                return null;
            }
            if (context.state().hasProperty(RotatedPillarBlock.AXIS) && replacement.hasProperty(RotatedPillarBlock.AXIS)) {
                return replacement.setValue(RotatedPillarBlock.AXIS, context.state().getValue(RotatedPillarBlock.AXIS));
            }
            return replacement;
        });
    }

    private static BlockState legacyState(String legacyName) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(legacyName);
        return block == null || !block.isPresent() ? null : block.get().defaultBlockState();
    }

    private static BlockState withSlakedLevel(BlockState replacement, int level) {
        if (replacement == null || !replacement.hasProperty(LegacySellafieldSlakedBlock.LEVEL)) {
            return replacement;
        }
        return replacement.setValue(LegacySellafieldSlakedBlock.LEVEL, clamp(level, 0, 15));
    }

    private static boolean isLegacyLeafOrPlant(Context context) {
        return context.state().is(ModBlocks.WASTE_LEAVES.get())
                || context.state().is(BlockTags.LEAVES)
                || isLegacyPlantMaterial(context)
                || isLegacyVineMaterial(context);
    }

    private static boolean isLegacyPlantMaterial(Context context) {
        BlockState state = context.state();
        return state.is(Blocks.GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.DEAD_BUSH)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(BlockTags.CROPS);
    }

    private static boolean isLegacyVineMaterial(Context context) {
        BlockState state = context.state();
        return state.is(Blocks.VINE)
                || state.is(Blocks.TWISTING_VINES)
                || state.is(Blocks.TWISTING_VINES_PLANT)
                || state.is(Blocks.WEEPING_VINES)
                || state.is(Blocks.WEEPING_VINES_PLANT);
    }

    private static boolean isLegacyWoodenMaterial(Context context) {
        BlockState state = context.state();
        return state.is(BlockTags.WOODEN_BUTTONS)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.SIGNS)
                || state.is(BlockTags.ALL_HANGING_SIGNS);
    }

    private static boolean isLegacyGroundMaterial(Context context) {
        BlockState state = context.state();
        return state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.MUD);
    }

    private static boolean isLegacyGrassMaterial(Context context) {
        return context.state().is(Blocks.GRASS_BLOCK) || context.state().is(Blocks.MYCELIUM);
    }

    private static boolean isLegacyRockMaterial(Context context) {
        BlockState state = context.state();
        return state.is(ModBlocks.SELLAFIELD_SLAKED.get())
                || state.is(ModBlocks.SELLAFIELD_BEDROCK.get())
                || state.is(Blocks.STONE)
                || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE);
    }

    private static boolean isLegacySandMaterial(Context context) {
        BlockState state = context.state();
        return state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL);
    }

    private static boolean isLegacyIronMaterial(Context context) {
        BlockState state = context.state();
        return state.is(Blocks.IRON_BLOCK)
                || state.is(Blocks.IRON_BARS)
                || state.is(Blocks.IRON_DOOR)
                || state.is(Blocks.IRON_TRAPDOOR)
                || state.is(Blocks.CHAIN)
                || state.is(Blocks.ANVIL)
                || state.is(Blocks.CHIPPED_ANVIL)
                || state.is(Blocks.DAMAGED_ANVIL);
    }

    private static boolean blocksLegacyReplacement(Context context, BlockState replacement) {
        boolean replacementIsBedrockSellafield = replacement.is(ModBlocks.SELLAFIELD_BEDROCK.get());
        if (context.pos().getY() <= context.level().getMinBuildHeight() && !replacementIsBedrockSellafield) {
            return true;
        }
        BlockState original = context.state();
        if (original.is(ModBlocks.SELLAFIELD_BEDROCK.get()) && !replacementIsBedrockSellafield) {
            return true;
        }
        if (original.is(ModBlocks.SELLAFIELD_SLAKED.get()) && replacement.is(ModBlocks.SELLAFIELD_SLAKED.get())) {
            return replacement.getValue(LegacySellafieldSlakedBlock.LEVEL)
                    <= original.getValue(LegacySellafieldSlakedBlock.LEVEL);
        }
        if (original.is(ModBlocks.SELLAFIELD_BEDROCK.get()) && replacementIsBedrockSellafield) {
            return replacement.getValue(LegacySellafieldSlakedBlock.LEVEL)
                    <= original.getValue(LegacySellafieldSlakedBlock.LEVEL);
        }
        return false;
    }

    private static String optionalString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static Integer optionalInt(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
    }

    private static double optionalDouble(JsonObject json, String key, double fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsDouble() : fallback;
    }

    private static boolean optionalBoolean(JsonObject json, String key, boolean fallback) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : fallback;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record Entry(Predicate<Context> matcher, List<WeightedOutcome> primary, List<WeightedOutcome> secondary,
                         double primaryChance, double minDistance, double maxDistance, double falloffStart,
                         boolean restrictDepth) {
        private Result apply(Context context) {
            if (context.distancePercent() > maxDistance || context.distancePercent() < minDistance || !matcher.test(context)) {
                return Result.noMatch();
            }
            if (context.distancePercent() > maxDistance * falloffStart && shouldSkipFalloff(context)) {
                return Result.noMatch();
            }

            List<WeightedOutcome> choices = primaryChance == 1.0D || context.level().random.nextDouble() < primaryChance
                    ? primary
                    : secondary;
            BlockState replacement = choose(context, choices);
            if (replacement == null || replacement.equals(context.state()) || blocksLegacyReplacement(context, replacement)) {
                return Result.noMatch();
            }

            context.level().setBlock(context.pos(), replacement, 3);
            return new Result(true, restrictDepth);
        }

        private boolean shouldSkipFalloff(Context context) {
            double denominator = maxDistance - maxDistance * falloffStart;
            if (denominator <= 0.0D) {
                return false;
            }
            double distance = (context.distancePercent() - maxDistance * falloffStart) / denominator;
            return Math.abs(context.level().random.nextGaussian()) < Math.pow(distance, 2.0D) * 3.0D;
        }

        private static BlockState choose(Context context, List<WeightedOutcome> outcomes) {
            int totalWeight = outcomes.stream().mapToInt(outcome -> Math.max(0, outcome.weight())).sum();
            if (totalWeight <= 0) {
                return null;
            }
            int randomWeight = context.level().random.nextInt(totalWeight);
            for (WeightedOutcome outcome : outcomes) {
                randomWeight -= Math.max(0, outcome.weight());
                if (randomWeight <= 0) {
                    return outcome.state().apply(context);
                }
            }
            return outcomes.get(0).state().apply(context);
        }
    }

    private record WeightedOutcome(int weight, Function<Context, BlockState> state) {
    }

    private record Context(Level level, BlockPos pos, BlockState state, double distancePercent) {
    }

    public record LoadReport(boolean customConfigLoaded, boolean usingDefaults, int entryCount, Path configFile,
                             Path templateFile, boolean templateWritten, List<String> warnings) {
        private static LoadReport defaults(Path config, Path template, boolean wroteTemplate, List<String> warnings) {
            return new LoadReport(false, true, DEFAULT_ENTRIES.size(), config, template, wroteTemplate, List.copyOf(warnings));
        }

        public String summary() {
            return (customConfigLoaded ? "custom" : "built-in") + " fallout table, entries=" + entryCount
                    + ", warnings=" + warnings.size();
        }
    }

    public record Result(boolean matched, boolean restrictDepth) {
        private static Result noMatch() {
            return new Result(false, false);
        }
    }

    private LegacyFalloutConversions() {
    }
}
