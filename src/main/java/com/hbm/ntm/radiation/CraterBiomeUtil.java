package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import com.hbm.ntm.world.WorldUtil;

import java.util.Optional;

public final class CraterBiomeUtil {
    public static final ResourceKey<Biome> CRATER = biomeKey("crater");
    public static final ResourceKey<Biome> CRATER_INNER = biomeKey("crater_inner");
    public static final ResourceKey<Biome> CRATER_OUTER = biomeKey("crater_outer");

    public static boolean setCraterBiome(ServerLevel level, int x, int z, CraterRadiationData.CraterZone zone) {
        Holder<Biome> biome = craterBiomeHolder(level, zone).orElse(null);
        if (biome == null) {
            return false;
        }

        return WorldUtil.setBiome(level, x, z, biome);
    }

    public static void resendCraterBiomes(ServerLevel level, ChunkPos chunkPos) {
        WorldUtil.syncBiomeChange(level, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
    }

    private static Optional<Holder.Reference<Biome>> craterBiomeHolder(ServerLevel level, CraterRadiationData.CraterZone zone) {
        ResourceKey<Biome> key = switch (zone) {
            case OUTER -> CRATER_OUTER;
            case CRATER -> CRATER;
            case INNER -> CRATER_INNER;
            case NONE -> null;
        };
        if (key == null) {
            return Optional.empty();
        }
        net.minecraft.core.Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        return registry.getHolder(key);
    }

    private static ResourceKey<Biome> biomeKey(String name) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(HbmNtm.MOD_ID, name));
    }

    private CraterBiomeUtil() {
    }
}
