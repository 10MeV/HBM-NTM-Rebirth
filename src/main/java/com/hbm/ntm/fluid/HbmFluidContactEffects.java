package com.hbm.ntm.fluid;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.PheromoneFluidTrait;
import com.hbm.ntm.fluid.trait.PoisonFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.fluid.trait.ToxinFluidTrait;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class HbmFluidContactEffects {
    private static final ContactContext MIST_CONTEXT =
            new ContactContext(ContactProfile.MIST, ChemicalContactStyle.NULL, null, null);

    public static ContactReport previewContact(FluidType fluid, Entity entity, float intensity) {
        return collectContact(fluid, entity, Mth.clamp(intensity, 0.0F, 1.0F), false, MIST_CONTEXT);
    }

    public static ContactReport affectEntity(FluidType fluid, Entity entity, float intensity) {
        return collectContact(fluid, entity, Mth.clamp(intensity, 0.0F, 1.0F), true, MIST_CONTEXT);
    }

    public static ContactReport affectChemicalProjectile(FluidType fluid, Entity entity, float intensity,
            ChemicalContactStyle style, Entity direct, Entity owner) {
        ContactContext context = new ContactContext(ContactProfile.CHEMICAL_PROJECTILE,
                style == null ? ChemicalContactStyle.NULL : style, direct, owner);
        return collectContact(fluid, entity, Mth.clamp(intensity, 0.0F, 1.0F), true, context);
    }

    private static ContactReport collectContact(FluidType fluid, Entity entity, float intensity, boolean apply,
            ContactContext context) {
        FluidType type = fluid == null ? HbmFluids.NONE : fluid;
        ContactReport report = new ContactReport(type, intensity);
        if (type == HbmFluids.NONE || entity == null || intensity <= 0.0F) {
            return report;
        }

        LivingEntity living = entity instanceof LivingEntity livingEntity ? livingEntity : null;
        applyTemperature(type, entity, living, intensity, apply, report, context);
        applyDelicious(type, living, intensity, apply, report, context);
        applyFlammableOil(type, living, apply, report, context);
        applyChemicalFlame(type, entity, intensity, apply, report, context);
        applyCorrosive(type, living, apply, report, context);
        applyVentRadiation(type, entity, living, apply, report, context);
        applyPoison(type, living, intensity, apply, report);
        applyToxin(type, living, intensity, apply, report);
        applyPheromone(type, living, apply, report, context);

        return report;
    }

    private static void applyTemperature(FluidType type, Entity entity, LivingEntity living, float intensity,
            boolean apply, ContactReport report, ContactContext context) {
        int temperature = type.getTemperature();
        if (temperature >= 100) {
            float damage = context.profile == ContactProfile.CHEMICAL_PROJECTILE
                    ? Math.min(0.25F + (temperature - 100) * 0.001F, 15.0F)
                    : 0.2F + (temperature - 100) * 0.02F;
            report.addDirectDamage("temperature_hot", damage);
            if (apply) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(entity,
                        damageSource(entity, ModDamageSources.BOIL, context), damage);
                if (temperature >= 500) {
                    entity.setSecondsOnFire(10);
                }
            }
        } else if (temperature < -20 && living != null && coldApplies(context)) {
            float damage = context.profile == ContactProfile.CHEMICAL_PROJECTILE
                    ? Math.min(0.25F + (-temperature) * 0.01F, 2.0F)
                    : 0.2F + (temperature + 20) * -0.05F;
            report.addDirectDamage("temperature_cold", damage);
            if (context.profile == ContactProfile.MIST) {
                report.addEffect(MobEffects.MOVEMENT_SLOWDOWN, 100, 2);
                report.addEffect(MobEffects.DIG_SLOWDOWN, 100, 4);
            }
            if (apply) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(living,
                        damageSource(living, context.profile == ContactProfile.CHEMICAL_PROJECTILE
                                ? ModDamageSources.BOIL : ModDamageSources.ICE, context), damage);
                if (context.profile == ContactProfile.MIST) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                    living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 4));
                }
            }
        }
    }

    private static boolean coldApplies(ContactContext context) {
        return context.profile == ContactProfile.MIST
                || context.style == ChemicalContactStyle.LIQUID
                || context.style == ChemicalContactStyle.GAS;
    }

    private static void applyDelicious(FluidType type, LivingEntity living, float intensity, boolean apply,
            ContactReport report, ContactContext context) {
        if (living == null || !living.isAlive() || !type.hasTrait(SimpleFluidTraits.Delicious.class)) {
            return;
        }
        if (context.profile == ContactProfile.CHEMICAL_PROJECTILE
                && context.style != ChemicalContactStyle.LIQUID
                && context.style != ChemicalContactStyle.GAS) {
            return;
        }
        int applications = context.profile == ContactProfile.CHEMICAL_PROJECTILE
                && context.style == ChemicalContactStyle.LIQUID ? 2 : 1;
        float amount = 2.0F * intensity * applications;
        report.healing += amount;
        if (apply) {
            living.heal(amount);
        }
    }

    private static void applyFlammableOil(FluidType type, LivingEntity living, boolean apply, ContactReport report,
            ContactContext context) {
        if (living == null || !type.hasTrait(FlammableFluidTrait.class) || !type.hasTrait(SimpleFluidTraits.Liquid.class)) {
            return;
        }
        if (context.profile == ContactProfile.CHEMICAL_PROJECTILE && context.style != ChemicalContactStyle.LIQUID) {
            return;
        }
        int oilTicks = context.profile == ContactProfile.CHEMICAL_PROJECTILE ? 300 : 200;
        report.oilTicks = Math.max(report.oilTicks, oilTicks);
        if (apply) {
            HbmLivingProperties.ensureOil(living, oilTicks);
        }
    }

    private static void applyChemicalFlame(FluidType type, Entity entity, float intensity, boolean apply,
            ContactReport report, ContactContext context) {
        if (context.profile != ContactProfile.CHEMICAL_PROJECTILE) {
            return;
        }
        if (context.style == ChemicalContactStyle.BURNING) {
            CombustibleFluidTrait trait = type.getTrait(CombustibleFluidTrait.class);
            float heat = trait == null ? 0.0F
                    : Math.min(trait.getCombustionEnergyPerBucket() / 100_000.0F, 15.0F);
            float damage = 0.2F + heat;
            report.addDirectDamage("flamethrower", damage);
            report.fireTicks = Math.max(report.fireTicks, 5 * 20);
            if (apply) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(entity,
                        damageSource(entity, ModDamageSources.FLAMETHROWER, context), damage);
                entity.setSecondsOnFire(5);
            }
        } else if (context.style == ChemicalContactStyle.GASFLAME) {
            FlammableFluidTrait flammable = type.getTrait(FlammableFluidTrait.class);
            CombustibleFluidTrait combustible = type.getTrait(CombustibleFluidTrait.class);
            float heat = Math.max(flammable == null ? 0.0F : flammable.getHeatEnergyPerBucket() / 50_000.0F,
                    combustible == null ? 0.0F
                            : Math.min(combustible.getCombustionEnergyPerBucket() / 100_000.0F, 15.0F));
            heat *= intensity;
            float damage = (0.2F + heat) * intensity;
            int seconds = Math.max(1, Mth.ceil(5.0F * intensity));
            report.addDirectDamage("flamethrower", damage);
            report.fireTicks = Math.max(report.fireTicks, seconds * 20);
            if (apply) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(entity,
                        damageSource(entity, ModDamageSources.FLAMETHROWER, context), damage);
                entity.setSecondsOnFire(seconds);
            }
        }
    }

    private static void applyCorrosive(FluidType type, LivingEntity living, boolean apply, ContactReport report,
            ContactContext context) {
        if (living == null) {
            return;
        }
        CorrosiveFluidTrait trait = type.getTrait(CorrosiveFluidTrait.class);
        if (trait == null) {
            return;
        }
        float damage = trait.getRating() / (context.profile == ContactProfile.CHEMICAL_PROJECTILE ? 50.0F : 60.0F);
        int suitDamage = trait.getRating() / (context.profile == ContactProfile.CHEMICAL_PROJECTILE ? 40 : 50);
        report.addDirectDamage("acid", damage);
        report.suitDamage += suitDamage * 4;
        if (apply) {
            EntityDamageUtil.attackEntityFromIgnoreIFrame(living,
                    damageSource(living, context.profile == ContactProfile.CHEMICAL_PROJECTILE
                            ? ModDamageSources.ACID_PLAYER : ModDamageSources.ACID, context), damage);
            for (int slot = 0; slot < 4; slot++) {
                ArmorUtil.damageSuit(living, slot, suitDamage);
            }
        }
    }

    private static void applyVentRadiation(FluidType type, Entity entity, LivingEntity living, boolean apply,
            ContactReport report, ContactContext context) {
        VentRadiationFluidTrait trait = type.getTrait(VentRadiationFluidTrait.class);
        if (trait == null) {
            return;
        }
        float radiation = trait.getRadiationPerMb() * 5.0F;
        if (living != null) {
            report.radiation += radiation;
        }
        if (context.profile == ContactProfile.CHEMICAL_PROJECTILE) {
            report.chunkRadiation += radiation;
        }
        if (apply) {
            if (living != null) {
                RadiationUtil.contaminate(living, radiation, true);
            }
            if (context.profile == ContactProfile.CHEMICAL_PROJECTILE) {
                ChunkRadiationManager.incrementRadiation(entity.level(), entity.blockPosition(), radiation);
            }
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
        int amplifier = 0;
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
            if (isProtected(living, entry, apply)) {
                report.protectedToxins++;
                continue;
            }
            if (entry instanceof ToxinFluidTrait.DirectDamage damage) {
                int delay = Math.max(0, damage.getDelayTicks());
                if (delay == 0 || living.level().getGameTime() % delay == 0) {
                    float amount = damage.getAmount() * intensity;
                    report.addDirectDamage(damage.getDamageType().toString(), amount);
                    if (apply) {
                        EntityDamageUtil.attackEntityFromIgnoreIFrame(living, resolveDamage(living.level(), damage.getDamageType()), amount);
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
            } else if (entry instanceof com.hbm.inventory.fluid.trait.FT_Toxin.ToxinDirectDamage damage) {
                int delay = Math.max(0, damage.getDelayTicks());
                if (delay == 0 || living.level().getGameTime() % delay == 0) {
                    float amount = damage.getAmount() * intensity;
                    report.addDirectDamage(damage.getDamageType().toString(), amount);
                    if (apply) {
                        EntityDamageUtil.attackEntityFromIgnoreIFrame(living,
                                resolveDamage(living.level(), damage.getDamageType()), amount);
                    }
                }
            } else if (entry instanceof com.hbm.inventory.fluid.trait.FT_Toxin.ToxinEffects effects) {
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

    private static void applyPheromone(FluidType type, LivingEntity living, boolean apply, ContactReport report,
            ContactContext context) {
        PheromoneFluidTrait trait = type.getTrait(PheromoneFluidTrait.class);
        if (living == null || trait == null) {
            return;
        }
        if (context.profile == ContactProfile.CHEMICAL_PROJECTILE) {
            addEffect(living, MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 2, apply, report);
            addEffect(living, MobEffects.MOVEMENT_SPEED, 5 * 60 * 20, 1, apply, report);
            addEffect(living, MobEffects.DIG_SPEED, 2 * 60 * 20, 4, apply, report);
            if (living instanceof Player && trait.getType() == 2) {
                addEffect(living, MobEffects.DAMAGE_BOOST, 2 * 60 * 20, 2, apply, report);
            }
            return;
        }

        if (living instanceof Player && trait.getType() == 2) {
            int mult = trait.getType();
            addEffect(living, MobEffects.MOVEMENT_SPEED, mult * 60 * 20, 1, apply, report);
            addEffect(living, MobEffects.DIG_SPEED, mult * 60 * 20, 1, apply, report);
            addEffect(living, MobEffects.REGENERATION, mult * 2 * 20, 0, apply, report);
            addEffect(living, MobEffects.DAMAGE_RESISTANCE, mult * 60 * 20, 0, apply, report);
            addEffect(living, MobEffects.DAMAGE_BOOST, mult * 60 * 20, 1, apply, report);
            addEffect(living, MobEffects.FIRE_RESISTANCE, mult * 60 * 20, 0, apply, report);
        }
    }

    private static void addEffect(LivingEntity living, MobEffect effect, int duration, int amplifier, boolean apply,
            ContactReport report) {
        report.addEffect(effect, duration, amplifier);
        if (apply) {
            living.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private static DamageSource damageSource(Entity entity, ResourceKey<DamageType> key, ContactContext context) {
        if (context.profile == ContactProfile.CHEMICAL_PROJECTILE && context.direct != null) {
            return ModDamageSources.indirect(entity.level(), key, context.direct, context.owner);
        }
        return ModDamageSources.source(entity.level(), key);
    }

    private static boolean isProtected(LivingEntity entity, ToxinFluidTrait.ToxinEntry entry, boolean apply) {
        return ArmorUtil.hasToxinProtection(entity, entry.getHazardClass(), entry.requiresFullBodyProtection(),
                apply);
    }

    private static DamageSource resolveDamage(Level level, ResourceLocation damageType) {
        try {
            return ModDamageSources.source(level, damageType.toString());
        } catch (IllegalArgumentException ignored) {
        }
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
        return HbmRegistryUtil.mobEffect(effectId);
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
        private float chunkRadiation;
        private float healing;
        private int suitDamage;
        private int oilTicks;
        private int fireTicks;
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
            effects.add(new EffectApplication(HbmRegistryUtil.mobEffectKey(effect), durationTicks, amplifier));
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

        public float getChunkRadiation() {
            return chunkRadiation;
        }

        public float getHealing() {
            return healing;
        }

        public int getFireTicks() {
            return fireTicks;
        }

        public int getOilTicks() {
            return oilTicks;
        }

        public int getSuitDamage() {
            return suitDamage;
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
            return radiation > 0.0F || chunkRadiation > 0.0F || healing > 0.0F || suitDamage > 0 || oilTicks > 0
                    || fireTicks > 0
                    || !directDamage.isEmpty() || !effects.isEmpty();
        }
    }

    private enum ContactProfile {
        MIST,
        CHEMICAL_PROJECTILE
    }

    private record ContactContext(ContactProfile profile, ChemicalContactStyle style, Entity direct, Entity owner) {
    }

    public enum ChemicalContactStyle {
        AMAT,
        LIGHTNING,
        LIQUID,
        GAS,
        GASFLAME,
        BURNING,
        NULL
    }

    public record DamageApplication(String source, float amount) {
    }

    public record EffectApplication(ResourceLocation effect, int durationTicks, int amplifier) {
    }
}
