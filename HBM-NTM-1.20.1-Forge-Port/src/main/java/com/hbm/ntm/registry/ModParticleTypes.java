package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, HbmNtm.MOD_ID);

    public static final RegistryObject<SimpleParticleType> RADIATION_FOG =
            PARTICLE_TYPES.register("radiation_fog", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> TOWN_AURA =
            PARTICLE_TYPES.register("town_aura", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SCHRAB_FOG =
            PARTICLE_TYPES.register("schrab_fog", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ROCKET_FLAME =
            PARTICLE_TYPES.register("rocket_flame", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CONTRAIL =
            PARTICLE_TYPES.register("contrail", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LAUNCH_SMOKE =
            PARTICLE_TYPES.register("launch_smoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EX_SMOKE =
            PARTICLE_TYPES.register("ex_smoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FOAM =
            PARTICLE_TYPES.register("foam", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER =
            PARTICLE_TYPES.register("flamethrower", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLACK_POWDER_SPARK =
            PARTICLE_TYPES.register("black_powder_spark", () -> new SimpleParticleType(false));

    private ModParticleTypes() {
    }
}
