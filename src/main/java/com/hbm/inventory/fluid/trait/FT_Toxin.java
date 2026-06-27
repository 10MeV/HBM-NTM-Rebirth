package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.trait.ToxinFluidTrait;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ArmorUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Legacy package bridge for 1.7.10 FT_Toxin.
 */
@Deprecated(forRemoval = false)
public class FT_Toxin extends ToxinFluidTrait {
    public final List<ToxinEntry> entries = new ArrayList<>();

    public FT_Toxin addEntry(ToxinEntry entry) {
        if (entry != null) {
            entries.add(entry);
            super.addEntry(entry);
        }
        return this;
    }

    @Override
    public FT_Toxin addEntry(ToxinFluidTrait.ToxinEntry entry) {
        if (entry instanceof ToxinEntry legacyEntry) {
            return addEntry(legacyEntry);
        }
        if (entry != null) {
            super.addEntry(entry);
        }
        return this;
    }

    @Override
    public String getLegacyName() {
        return "toxin";
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        info.add(Component.literal("[Toxin]").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (ToxinEntry entry : entries) {
            entry.addInfo(info);
        }
    }

    public void addInfoHidden(List<String> info) {
        if (info == null) {
            return;
        }
        info.add(ChatFormatting.LIGHT_PURPLE + "[Toxin]");
        List<Component> entryInfo = new ArrayList<>();
        for (ToxinEntry entry : entries) {
            entry.addInfo(entryInfo);
        }
        for (Component line : entryInfo) {
            info.add(line.getString());
        }
    }

    public void affect(LivingEntity entity, double intensity) {
        if (entity == null || intensity <= 0.0D) {
            return;
        }
        for (ToxinEntry entry : entries) {
            entry.poison(entity, intensity);
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        JsonArray entriesJson = new JsonArray();
        for (ToxinEntry entry : entries) {
            JsonObject entryJson = new JsonObject();
            if (entry instanceof ToxinDirectDamage damage) {
                entryJson.addProperty("type", "directdamage");
                entryJson.addProperty("amount", damage.amount);
                entryJson.addProperty("source", legacyDamageName(damage.getDamageType()));
                entryJson.addProperty("delay", damage.delay);
                writeProtectionJson(entryJson, damage);
            } else if (entry instanceof ToxinEffects effects) {
                entryJson.addProperty("type", "effects");
                JsonArray effectsJson = new JsonArray();
                for (EffectSpec effect : effects.getEffects()) {
                    JsonArray effectJson = new JsonArray();
                    effectJson.add(effect.effect().toString());
                    effectJson.add(effect.durationTicks());
                    effectJson.add(effect.amplifier());
                    effectJson.add(effect.ambient());
                    effectsJson.add(effectJson);
                }
                entryJson.add("effects", effectsJson);
                writeProtectionJson(entryJson, effects);
            }
            entriesJson.add(entryJson);
        }
        object.add("entries", entriesJson);
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("entries").beginArray();
        for (ToxinEntry entry : entries) {
            writer.beginObject();
            if (entry instanceof ToxinDirectDamage damage) {
                writer.name("type").value("directdamage");
                writer.name("amount").value(damage.amount);
                writer.name("source").value(legacyDamageName(damage.getDamageType()));
                writer.name("delay").value(damage.delay);
                writer.name("hazmat").value(damage.fullBody);
                writer.name("masktype").value(maskName(damage.clazz));
            } else if (entry instanceof ToxinEffects effects) {
                writer.name("type").value("effects");
                writer.name("effects").beginArray();
                writer.setIndent("");
                for (EffectSpec effect : effects.getEffects()) {
                    writer.beginArray();
                    writer.value(effect.effect().toString());
                    writer.value(effect.durationTicks());
                    writer.value(effect.amplifier());
                    writer.value(effect.ambient());
                    writer.endArray();
                }
                writer.endArray();
                writer.setIndent("  ");
                writer.name("hazmat").value(effects.fullBody);
                writer.name("masktype").value(maskName(effects.clazz));
            }
            writer.endObject();
        }
        writer.endArray();
    }

    public void deserializeJSON(JsonObject object) {
        if (object == null || !object.has("entries") || !object.get("entries").isJsonArray()) {
            return;
        }
        JsonArray array = object.getAsJsonArray("entries");
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String type = stringValue(entry, "type", "").toLowerCase(Locale.US);
            HazardClass hazardClass = hazardClass(stringValue(entry, "masktype", "NONE"));
            boolean fullBody = booleanValue(entry, "hazmat", false);
            if ("directdamage".equals(type)) {
                addEntry(new ToxinDirectDamage(
                        damageId(stringValue(entry, "source", "cloud")),
                        floatValue(entry, "amount", 0.0F),
                        intValue(entry, "delay", 20),
                        hazardClass,
                        fullBody));
            } else if ("effects".equals(type)) {
                ToxinEffects effects = new ToxinEffects(hazardClass, fullBody);
                if (entry.has("effects") && entry.get("effects").isJsonArray()) {
                    for (JsonElement effectElement : entry.getAsJsonArray("effects")) {
                        readEffect(effectElement, effects);
                    }
                }
                addEntry(effects);
            }
        }
    }

    public static abstract class ToxinEntry extends ToxinFluidTrait.ToxinEntry {
        public final HazardClass clazz;
        public final boolean fullBody;

        protected ToxinEntry(HazardClass clazz, boolean fullBody) {
            super(modern(clazz), fullBody);
            this.clazz = clazz;
            this.fullBody = fullBody;
        }

        public boolean isProtected(LivingEntity entity) {
            boolean hasMask = clazz == null || ArmorUtil.hasToxinProtection(entity, clazz, fullBody, true);
            boolean hasSuit = !fullBody || ArmorUtil.checkForHazmat(entity);
            return hasMask && hasSuit;
        }

        public abstract void poison(LivingEntity entity, double intensity);
    }

    public static class ToxinDirectDamage extends ToxinEntry {
        public final float amount;
        public final int delay;

        public ToxinDirectDamage(ResourceKey<DamageType> damage, float amount, int delay, HazardClass clazz,
                boolean fullBody) {
            this(damage == null ? hbm("cloud") : damage.location(), amount, delay, clazz, fullBody);
        }

        public ToxinDirectDamage(ResourceLocation damage, float amount, int delay, HazardClass clazz,
                boolean fullBody) {
            super(clazz, fullBody);
            this.amount = amount;
            this.delay = delay;
            this.damageType = damage == null ? hbm("cloud") : damage;
        }

        public ToxinDirectDamage(String damage, float amount, int delay, HazardClass clazz, boolean fullBody) {
            this(damageId(damage), amount, delay, clazz, fullBody);
        }

        private final ResourceLocation damageType;

        public ResourceLocation getDamageType() {
            return damageType;
        }

        public float getAmount() {
            return amount;
        }

        public int getDelayTicks() {
            return delay;
        }

        @Override
        public void poison(LivingEntity entity, double intensity) {
            if (isProtected(entity)) {
                return;
            }
            if (delay == 0 || entity.level().getGameTime() % delay == 0) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(entity,
                        ModDamageSources.source(entity.level(), legacyDamageName(damageType)),
                        (float) (amount * intensity));
            }
        }

        @Override
        public void addInfo(List<Component> info) {
            float dps = delay <= 0 ? amount * 20.0F : amount * 20.0F / delay;
            info.add(Component.literal("- ").withStyle(ChatFormatting.YELLOW)
                    .append(protectionLabel())
                    .append(Component.literal(": " + String.format(Locale.US, "%,.1f", dps) + " DPS")
                            .withStyle(ChatFormatting.YELLOW)));
        }
    }

    public static class ToxinEffects extends ToxinEntry {
        public ToxinEffects(HazardClass clazz, boolean fullBody) {
            super(clazz, fullBody);
        }

        public ToxinEffects add(MobEffectInstance... effects) {
            if (effects != null) {
                for (MobEffectInstance effect : effects) {
                    if (effect != null) {
                        ResourceLocation id = HbmRegistryUtil.mobEffectKey(effect.getEffect());
                        if (id != null) {
                            addEffect(id, effect.getDuration(), effect.getAmplifier(), effect.isAmbient());
                        }
                    }
                }
            }
            return this;
        }

        public ToxinEffects add(MobEffect effect, int durationTicks, int amplifier) {
            return add(effect, durationTicks, amplifier, false);
        }

        public ToxinEffects add(MobEffect effect, int durationTicks, int amplifier, boolean ambient) {
            ResourceLocation id = HbmRegistryUtil.mobEffectKey(effect);
            if (id != null) {
                addEffect(id, durationTicks, amplifier, ambient);
            }
            return this;
        }

        private final List<EffectSpec> effects = new ArrayList<>();

        public ToxinEffects addEffect(ResourceLocation effect, int durationTicks, int amplifier, boolean ambient) {
            effects.add(new EffectSpec(effect, durationTicks, amplifier, ambient));
            return this;
        }

        public List<EffectSpec> getEffects() {
            return List.copyOf(effects);
        }

        @Override
        public void poison(LivingEntity entity, double intensity) {
            if (isProtected(entity)) {
                return;
            }
            for (EffectSpec effect : getEffects()) {
                Optional<MobEffect> resolved = HbmRegistryUtil.mobEffect(effect.effect());
                if (resolved.isEmpty()) {
                    continue;
                }
                int duration = Math.max(1, (int) (effect.durationTicks() * intensity));
                entity.addEffect(new MobEffectInstance(resolved.get(), duration, effect.amplifier(),
                        effect.ambient(), true));
            }
        }

        @Override
        public void addInfo(List<Component> info) {
            info.add(Component.literal("- ").withStyle(ChatFormatting.YELLOW)
                    .append(protectionLabel())
                    .append(Component.literal(":").withStyle(ChatFormatting.YELLOW)));
            for (EffectSpec effect : getEffects()) {
                info.add(Component.literal("   - ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatableWithFallback(effect.effect().toLanguageKey("effect"),
                                effect.effect().toString()))
                        .append(Component.literal(" " + formatDuration(effect.durationTicks()))
                                .withStyle(ChatFormatting.YELLOW)));
            }
        }
    }

    private static void readEffect(JsonElement element, ToxinEffects effects) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() >= 4) {
                effects.addEffect(effectId(array.get(0)), LegacyFluidTraitJson.intValue(array.get(1)),
                        LegacyFluidTraitJson.intValue(array.get(2)),
                        LegacyFluidTraitJson.booleanValue(array.get(3)));
            }
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            effects.addEffect(resource(stringValue(object, "effect", "minecraft:poison")),
                    intValue(object, "duration", intValue(object, "durationTicks", 0)),
                    intValue(object, "amplifier", 0),
                    booleanValue(object, "ambient", false));
        }
    }

    private static void writeProtectionJson(JsonObject object, ToxinEntry entry) {
        object.addProperty("hazmat", entry.fullBody);
        object.addProperty("masktype", maskName(entry.clazz));
    }

    private static ResourceLocation effectId(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            return legacyPotionId(element.getAsInt());
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String value = element.getAsString();
            try {
                return legacyPotionId(Integer.decode(value));
            } catch (NumberFormatException ignored) {
                return resource(value);
            }
        }
        return resource(element.getAsString());
    }

    private static ResourceLocation legacyPotionId(int id) {
        return switch (id) {
            case 2 -> mc("slowness");
            case 4 -> mc("mining_fatigue");
            case 7 -> mc("instant_damage");
            case 9 -> mc("nausea");
            case 15 -> mc("blindness");
            case 17 -> mc("hunger");
            case 18 -> mc("weakness");
            case 19 -> mc("poison");
            case 20 -> mc("wither");
            case 72 -> registryEffect(ModEffects.POTION_SICKNESS.get());
            case 73 -> registryEffect(ModEffects.DEATH.get());
            default -> mc("poison");
        };
    }

    private static ResourceLocation registryEffect(MobEffect effect) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        return id == null ? mc("poison") : id;
    }

    private static ResourceLocation damageId(String value) {
        return ModDamageSources.legacyKey(value)
                .map(ResourceKey::location)
                .orElseGet(() -> resource(value == null ? "cloud" : value));
    }

    private static ResourceLocation resource(String value) {
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        if (parsed != null) {
            return parsed;
        }
        return hbm(value == null || value.isBlank() ? "cloud" : value);
    }

    private static String legacyDamageName(ResourceLocation id) {
        if (id == null) {
            return "cloud";
        }
        Optional<ModDamageSources.LegacyDamageType> legacy = ModDamageSources.legacyDamageType(id.toString());
        if (legacy.isPresent()) {
            return legacy.get().expectedMessageId();
        }
        return id.getNamespace().equals(HbmNtm.MOD_ID) ? id.getPath() : id.toString();
    }

    private static HazardClass hazardClass(String value) {
        if (value == null || value.isBlank() || "NONE".equalsIgnoreCase(value)) {
            return null;
        }
        return HazardClass.valueOf(value.toUpperCase(Locale.US));
    }

    private static com.hbm.ntm.api.item.HazardClass modern(HazardClass hazard) {
        return hazard == null ? null : com.hbm.ntm.api.item.HazardClass.valueOf(hazard.name());
    }

    private static String maskName(HazardClass hazard) {
        return hazard == null ? "NONE" : hazard.name();
    }

    private static String stringValue(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element == null ? fallback : element.getAsString();
    }

    private static int intValue(JsonObject object, String key, int fallback) {
        return LegacyFluidTraitJson.intValue(object, key, fallback);
    }

    private static float floatValue(JsonObject object, String key, float fallback) {
        return LegacyFluidTraitJson.floatValue(object, key, fallback);
    }

    private static boolean booleanValue(JsonObject object, String key, boolean fallback) {
        return LegacyFluidTraitJson.booleanValue(object, key, fallback);
    }

    private static String formatDuration(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        return seconds / 60 + ":" + String.format(Locale.US, "%02d", seconds % 60);
    }

    private static ResourceLocation hbm(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static ResourceLocation mc(String path) {
        return new ResourceLocation("minecraft", path);
    }
}
