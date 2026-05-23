package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "radiation"));
    public static final ResourceKey<DamageType> DIGAMMA = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "digamma"));
    public static final ResourceKey<DamageType> MKU = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "mku"));
    public static final ResourceKey<DamageType> ASBESTOS = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "asbestos"));
    public static final ResourceKey<DamageType> BLACK_LUNG = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "blacklung"));
    public static final ResourceKey<DamageType> EXPLOSION = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "explosion"));
    public static final ResourceKey<DamageType> MONOXIDE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "monoxide"));
    public static final ResourceKey<DamageType> PC = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "pc"));
    public static final ResourceKey<DamageType> CLOUD = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "cloud"));
    public static final ResourceKey<DamageType> ELECTRIC = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "electric"));
    public static final ResourceKey<DamageType> SHRAPNEL = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "shrapnel"));
    public static final ResourceKey<DamageType> RUBBLE = ResourceKey.create(
            Registries.DAMAGE_TYPE, new ResourceLocation(HbmNtm.MOD_ID, "rubble"));

    public static DamageSource radiation(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RADIATION));
    }

    public static DamageSource digamma(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DIGAMMA));
    }

    public static DamageSource mku(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MKU));
    }

    public static DamageSource asbestos(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ASBESTOS));
    }

    public static DamageSource blackLung(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(BLACK_LUNG));
    }

    public static DamageSource monoxide(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MONOXIDE));
    }

    public static DamageSource pc(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(PC));
    }

    public static DamageSource cloud(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CLOUD));
    }

    public static DamageSource electric(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ELECTRIC));
    }

    public static DamageSource shrapnel(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(SHRAPNEL));
    }

    public static DamageSource rubble(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(RUBBLE));
    }

    public static DamageSource explosion(Level level, @Nullable Entity source) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(EXPLOSION), source);
    }

    private ModDamageSources() {
    }
}
