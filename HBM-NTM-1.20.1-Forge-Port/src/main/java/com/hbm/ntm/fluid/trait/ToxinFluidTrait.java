package com.hbm.ntm.fluid.trait;

import com.hbm.ntm.api.item.HazardClass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    }

    public static final class DirectDamage extends ToxinEntry {
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
    }

    public static final class EffectApplication extends ToxinEntry {
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
    }

    public record EffectSpec(ResourceLocation effect, int durationTicks, int amplifier, boolean ambient) {
    }
}
