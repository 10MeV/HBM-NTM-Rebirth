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
    public static final RegistryObject<SimpleParticleType> GAS_FLAME =
            PARTICLE_TYPES.register("gas_flame", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CONTRAIL =
            PARTICLE_TYPES.register("contrail", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SMOKE_PLUME =
            PARTICLE_TYPES.register("smoke_plume", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LAUNCH_SMOKE =
            PARTICLE_TYPES.register("launch_smoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EX_SMOKE =
            PARTICLE_TYPES.register("ex_smoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FOAM =
            PARTICLE_TYPES.register("foam", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER =
            PARTICLE_TYPES.register("flamethrower", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER_BALEFIRE =
            PARTICLE_TYPES.register("flamethrower_balefire", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER_DIGAMMA =
            PARTICLE_TYPES.register("flamethrower_digamma", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER_OXY =
            PARTICLE_TYPES.register("flamethrower_oxy", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FLAMETHROWER_BLACK =
            PARTICLE_TYPES.register("flamethrower_black", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLACK_POWDER_SPARK =
            PARTICLE_TYPES.register("black_powder_spark", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> EXPLOSION_SMALL =
            PARTICLE_TYPES.register("explosion_small", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BLACK_POWDER_SMOKE =
            PARTICLE_TYPES.register("black_powder_smoke", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ASHES =
            PARTICLE_TYPES.register("ashes", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MUKE_WAVE =
            PARTICLE_TYPES.register("muke_wave", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> DEAD_LEAF =
            PARTICLE_TYPES.register("dead_leaf", () -> new SimpleParticleType(false));

    private ModParticleTypes() {
    }
}
