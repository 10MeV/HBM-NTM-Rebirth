package com.hbm.ntm.fluid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.PheromoneFluidTrait;
import com.hbm.ntm.fluid.trait.PoisonFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.fluid.trait.ToxinFluidTrait;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class HbmFluidContactEffects {
    private static final ResourceLocation HBM_DEATH_EFFECT = new ResourceLocation(HbmNtm.MOD_ID, "death");

    public static ContactReport previewContact(FluidType fluid, Entity entity, float intensity) {
        return collectContact(fluid, entity, Mth.clamp(intensity, 0.0F, 1.0F), false);
    }

    public static ContactReport affectEntity(FluidType fluid, Entity entity, float intensity) {
        return collectContact(fluid, entity, Mth.clamp(intensity, 0.0F, 1.0F), true);
    }

    private static ContactReport collectContact(FluidType fluid, Entity entity, float intensity, boolean apply) {
        FluidType type = fluid == null ? HbmFluids.NONE : fluid;
        ContactReport report = new ContactReport(type, intensity);
        if (type == HbmFluids.NONE || entity == null || intensity <= 0.0F) {
            return report;
        }

        LivingEntity living = entity instanceof LivingEntity livingEntity ? livingEntity : null;
        applyTemperature(type, entity, living, intensity, apply, report);
        applyFlammableOil(type, living, apply, report);
        applyVentRadiation(type, living, intensity, apply, report);
        applyPoison(type, living, intensity, apply, report);
        applyToxin(type, living, intensity, apply, report);
        applyPheromone(type, living, apply, report);

        return report;
    }

    private static void applyTemperature(FluidType type, Entity entity, LivingEntity living, float intensity, boolean apply, ContactReport report) {
        int temperature = type.getTemperature();
        if (temperature >= 100) {
            float damage = (0.2F + (temperature - 100) * 0.02F) * intensity;
            report.addDirectDamage("temperature_hot", damage);
            if (apply) {
                entity.hurt(entity.damageSources().onFire(), damage);
                if (temperature >= 500) {
                    entity.setSecondsOnFire(10);
                }
            }
        } else if (temperature < -20 && living != null) {
            float damage = (0.2F + (temperature + 20) * -0.05F) * intensity;
            report.addDirectDamage("temperature_cold", damage);
            report.addEffect(MobEffects.MOVEMENT_SLOWDOWN, scaleDuration(100, intensity), 2);
            report.addEffect(MobEffects.DIG_SLOWDOWN, scaleDuration(100, intensity), 4);
            if (apply) {
                living.hurt(living.damageSources().freeze(), damage);
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, scaleDuration(100, intensity), 2));
                living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, scaleDuration(100, intensity), 4));
            }
        }
    }

    private static void applyFlammableOil(FluidType type, LivingEntity living, boolean apply, ContactReport report) {
        if (living == null || !type.hasTrait(FlammableFluidTrait.class) || !type.hasTrait(SimpleFluidTraits.Liquid.class)) {
            return;
        }
        report.oilTicks = Math.max(report.oilTicks, 200);
        if (apply) {
            RadiationData.setOil(living, Math.max(RadiationData.getOil(living), 200));
        }
    }

    private static void applyVentRadiation(FluidType type, LivingEntity living, float intensity, boolean apply, ContactReport report) {
        if (living == null) {
            return;
        }
        VentRadiationFluidTrait trait = type.getTrait(VentRadiationFluidTrait.class);
        if (trait == null) {
            return;
        }
        float radiation = trait.getRadiationPerMb() * 5.0F * intensity;
        report.radiation += radiation;
        if (apply) {
            RadiationUtil.contaminate(living, radiation, true);
        }
    }

    private static void applyPoison(FluidType type, LivingEntity living, float intensity, boolean apply, ContactReport report) {
        if (living == null) {
            return;
        }
        PoisonFluidTrait trait = type.getTrait(PoisonFluidTrait.class);
        if (trait == null) {
            return;
        }
        MobEffect effect = trait.isWithering() ? MobEffects.WITHER : MobEffects.POISON;
        int duration = scaleDuration(5 * 20, intensity);
        int amplifier = Math.max(0, trait.getLevel());
        report.addEffect(effect, duration, amplifier);
        if (apply) {
            living.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private static void applyToxin(FluidType type, LivingEntity living, float intensity, boolean apply, ContactReport report) {
        if (living == null) {
            return;
        }
        ToxinFluidTrait trait = type.getTrait(ToxinFluidTrait.class);
        if (trait == null) {
            return;
        }

        for (ToxinFluidTrait.ToxinEntry entry : trait.getEntries()) {
            if (isProtected(living, entry)) {
                report.protectedToxins++;
                continue;
            }
            if (entry instanceof ToxinFluidTrait.DirectDamage damage) {
                int delay = Math.max(0, damage.getDelayTicks());
                if (delay == 0 || living.level().getGameTime() % delay == 0) {
                    float amount = damage.getAmount() * intensity;
                    report.addDirectDamage(damage.getDamageType().toString(), amount);
                    if (apply) {
                        living.hurt(resolveDamage(living.level(), damage.getDamageType()), amount);
                    }
                }
            } else if (entry instanceof ToxinFluidTrait.EffectApplication effects) {
                for (ToxinFluidTrait.EffectSpec spec : effects.getEffects()) {
                    Optional<MobEffect> effect = resolveEffect(spec.effect());
                    if (effect.isEmpty()) {
                        report.unresolvedEffects++;
                        continue;
                    }
                    int duration = scaleDuration(spec.durationTicks(), intensity);
                    report.addEffect(effect.get(), duration, spec.amplifier());
                    if (apply) {
                        living.addEffect(new MobEffectInstance(effect.get(), duration, spec.amplifier(), spec.ambient(), true));
                    }
                }
            }
        }
    }

    private static void applyPheromone(FluidType type, LivingEntity living, boolean apply, ContactReport report) {
        if (living == null || type.getTrait(PheromoneFluidTrait.class) == null) {
            return;
        }
        report.addEffect(MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 2);
        report.addEffect(MobEffects.MOVEMENT_SPEED, 5 * 60 * 20, 1);
        report.addEffect(MobEffects.DIG_SPEED, 2 * 60 * 20, 4);
        if (apply) {
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 2));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 5 * 60 * 20, 1));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 2 * 60 * 20, 4));
        }
    }

    private static boolean isProtected(LivingEntity entity, ToxinFluidTrait.ToxinEntry entry) {
        boolean hazardProtected = switch (entry.getHazardClass()) {
            case GAS_LUNG, GAS_BLISTERING -> ArmorUtil.hasLungGasProtection(entity);
            case GAS_MONOXIDE, GAS_INERT -> ArmorUtil.hasMonoxideGasProtection(entity);
            case PARTICLE_FINE -> ArmorUtil.hasFineParticleProtection(entity);
            case PARTICLE_COARSE, SAND -> ArmorUtil.hasCoarseParticleProtection(entity);
            case BACTERIA -> ArmorUtil.hasBacteriaProtection(entity);
            case LIGHT -> false;
        };
        return hazardProtected && (!entry.requiresFullBodyProtection() || ArmorUtil.checkForHazmat(entity));
    }

    private static DamageSource resolveDamage(Level level, ResourceLocation damageType) {
        if (damageType.getNamespace().equals(HbmNtm.MOD_ID) && damageType.getPath().equals("cloud")) {
            return ModDamageSources.cloud(level);
        }
        if (damageType.getNamespace().equals(HbmNtm.MOD_ID) && damageType.getPath().equals("monoxide")) {
            return ModDamageSources.monoxide(level);
        }
        if (damageType.getNamespace().equals(HbmNtm.MOD_ID) && damageType.getPath().equals("pc")) {
            return ModDamageSources.pc(level);
        }
        return ModDamageSources.cloud(level);
    }

    private static Optional<MobEffect> resolveEffect(ResourceLocation effectId) {
        if (effectId.equals(HBM_DEATH_EFFECT)) {
            return Optional.empty();
        }
        return Optional.ofNullable(BuiltInRegistries.MOB_EFFECT.get(effectId));
    }

    private static int scaleDuration(int durationTicks, float intensity) {
        return Math.max(1, Mth.ceil(durationTicks * intensity));
    }

    private HbmFluidContactEffects() {
    }

    public static final class ContactReport {
        private final FluidType fluid;
        private final float intensity;
        private final List<DamageApplication> directDamage = new ArrayList<>();
        private final List<EffectApplication> effects = new ArrayList<>();
        private float radiation;
        private int oilTicks;
        private int protectedToxins;
        private int unresolvedEffects;

        private ContactReport(FluidType fluid, float intensity) {
            this.fluid = fluid;
            this.intensity = intensity;
        }

        private void addDirectDamage(String source, float amount) {
            if (amount > 0.0F) {
                directDamage.add(new DamageApplication(source, amount));
            }
        }

        private void addEffect(MobEffect effect, int durationTicks, int amplifier) {
            effects.add(new EffectApplication(BuiltInRegistries.MOB_EFFECT.getKey(effect), durationTicks, amplifier));
        }

        public FluidType getFluid() {
            return fluid;
        }

        public float getIntensity() {
            return intensity;
        }

        public float getRadiation() {
            return radiation;
        }

        public int getOilTicks() {
            return oilTicks;
        }

        public int getProtectedToxins() {
            return protectedToxins;
        }

        public int getUnresolvedEffects() {
            return unresolvedEffects;
        }

        public List<DamageApplication> getDirectDamage() {
            return List.copyOf(directDamage);
        }

        public List<EffectApplication> getEffects() {
            return List.copyOf(effects);
        }

        public boolean hasEffects() {
            return radiation > 0.0F || oilTicks > 0 || !directDamage.isEmpty() || !effects.isEmpty();
        }
    }

    public record DamageApplication(String source, float amount) {
    }

    public record EffectApplication(ResourceLocation effect, int durationTicks, int amplifier) {
    }
}
