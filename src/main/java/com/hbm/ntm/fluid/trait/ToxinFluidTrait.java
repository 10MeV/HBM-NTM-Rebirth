package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.api.item.HazardClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToxinFluidTrait extends FluidTrait {
    private final List<ToxinEntry> entries = new ArrayList<>();

    public ToxinFluidTrait addEntry(ToxinEntry entry) {
        entries.add(entry);
        return this;
    }

    public List<ToxinEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        info.add(Component.literal("[Toxin]").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (ToxinEntry entry : entries) {
            entry.addInfo(info);
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        JsonArray entriesJson = new JsonArray();
        for (ToxinEntry entry : entries) {
            JsonObject entryJson = new JsonObject();
            if (entry instanceof DirectDamage damage) {
                entryJson.addProperty("type", "directdamage");
                entryJson.addProperty("amount", damage.getAmount());
                entryJson.addProperty("source", damage.getDamageType().toString());
                entryJson.addProperty("delay", damage.getDelayTicks());
                writeProtectionJson(entryJson, damage);
            } else if (entry instanceof EffectApplication effectApplication) {
                entryJson.addProperty("type", "effects");
                JsonArray effectsJson = new JsonArray();
                for (EffectSpec effect : effectApplication.getEffects()) {
                    JsonArray effectJson = new JsonArray();
                    effectJson.add(effect.effect().toString());
                    effectJson.add(effect.durationTicks());
                    effectJson.add(effect.amplifier());
                    effectJson.add(effect.ambient());
                    effectsJson.add(effectJson);
                }
                entryJson.add("effects", effectsJson);
                writeProtectionJson(entryJson, effectApplication);
            }
            entriesJson.add(entryJson);
        }
        object.add("entries", entriesJson);
    }

    public abstract static class ToxinEntry {
        private final HazardClass hazardClass;
        private final boolean fullBodyProtection;

        protected ToxinEntry(HazardClass hazardClass, boolean fullBodyProtection) {
            this.hazardClass = hazardClass;
            this.fullBodyProtection = fullBodyProtection;
        }

        public HazardClass getHazardClass() {
            return hazardClass;
        }

        public boolean requiresFullBodyProtection() {
            return fullBodyProtection;
        }

        protected Component protectionLabel() {
            Component label = hazardClass == null ? Component.literal("Unprotected")
                    : Component.translatableWithFallback(hazardClass.translationKey(), prettyHazardName(hazardClass));
            if (fullBodyProtection) {
                return label.copy().append(Component.literal(" (requires hazmat suit)").withStyle(ChatFormatting.RED));
            }
            return label;
        }

        public abstract void addInfo(List<Component> info);
    }

    public static class DirectDamage extends ToxinEntry {
        private final ResourceLocation damageType;
        private final float amount;
        private final int delayTicks;

        public DirectDamage(ResourceLocation damageType, float amount, int delayTicks, HazardClass hazardClass, boolean fullBodyProtection) {
            super(hazardClass, fullBodyProtection);
            this.damageType = damageType;
            this.amount = amount;
            this.delayTicks = delayTicks;
        }

        public ResourceLocation getDamageType() {
            return damageType;
        }

        public float getAmount() {
            return amount;
        }

        public int getDelayTicks() {
            return delayTicks;
        }

        @Override
        public void addInfo(List<Component> info) {
            float dps = delayTicks <= 0 ? amount * 20.0F : amount * 20.0F / delayTicks;
            info.add(Component.literal("- ").withStyle(ChatFormatting.YELLOW)
                    .append(protectionLabel())
                    .append(Component.literal(": " + String.format(Locale.US, "%,.1f", dps) + " DPS")
                            .withStyle(ChatFormatting.YELLOW)));
        }
    }

    public static class EffectApplication extends ToxinEntry {
        private final List<EffectSpec> effects = new ArrayList<>();

        public EffectApplication(HazardClass hazardClass, boolean fullBodyProtection) {
            super(hazardClass, fullBodyProtection);
        }

        public EffectApplication addEffect(ResourceLocation effect, int durationTicks, int amplifier, boolean ambient) {
            effects.add(new EffectSpec(effect, durationTicks, amplifier, ambient));
            return this;
        }

        public List<EffectSpec> getEffects() {
            return Collections.unmodifiableList(effects);
        }

        @Override
        public void addInfo(List<Component> info) {
            info.add(Component.literal("- ").withStyle(ChatFormatting.YELLOW)
                    .append(protectionLabel())
                    .append(Component.literal(":").withStyle(ChatFormatting.YELLOW)));
            for (EffectSpec effect : effects) {
                info.add(Component.literal("   - ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatableWithFallback(effect.effect().toLanguageKey("effect"), effect.effect().toString()))
                        .append(Component.literal(" " + formatDuration(effect.durationTicks())).withStyle(ChatFormatting.YELLOW)));
            }
        }
    }

    public record EffectSpec(ResourceLocation effect, int durationTicks, int amplifier, boolean ambient) {
    }

    private static void writeProtectionJson(JsonObject object, ToxinEntry entry) {
        object.addProperty("hazmat", entry.requiresFullBodyProtection());
        object.addProperty("masktype", entry.getHazardClass() == null ? "NONE" : entry.getHazardClass().name());
    }

    private static String formatDuration(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        return seconds / 60 + ":" + String.format(Locale.US, "%02d", seconds % 60);
    }

    private static String prettyHazardName(HazardClass hazardClass) {
        StringBuilder builder = new StringBuilder();
        for (String part : hazardClass.name().toLowerCase(Locale.US).split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
