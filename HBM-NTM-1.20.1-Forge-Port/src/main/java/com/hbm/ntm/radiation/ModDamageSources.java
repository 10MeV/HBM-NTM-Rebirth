package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "radiation"));
    public static final ResourceKey<DamageType> DIGAMMA = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "digamma"));

    public static DamageSource radiation(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RADIATION));
    }

    public static DamageSource digamma(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DIGAMMA));
    }

    private ModDamageSources() {
    }
}
