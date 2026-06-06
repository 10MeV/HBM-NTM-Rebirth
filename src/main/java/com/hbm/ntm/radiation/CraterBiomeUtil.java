package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.List;
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

        LevelChunk chunk = level.getChunk(x >> 4, z >> 4);
        int quartX = QuartPos.fromBlock(x);
        int quartZ = QuartPos.fromBlock(z);
        int baseQuartX = QuartPos.fromBlock(chunk.getPos().getMinBlockX());
        int baseQuartZ = QuartPos.fromBlock(chunk.getPos().getMinBlockZ());
        boolean changed = false;

        for (int sectionIndex = 0; sectionIndex < chunk.getSections().length; sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            boolean sectionChanged = false;
            for (int quartY = 0; quartY < 4; quartY++) {
                Holder<Biome> current = section.getNoiseBiome(quartX & 3, quartY, quartZ & 3);
                if (!current.equals(biome)) {
                    sectionChanged = true;
                    break;
                }
            }
            if (!sectionChanged) {
                continue;
            }

            int baseQuartY = QuartPos.fromSection(chunk.getSectionYFromSectionIndex(sectionIndex));
            section.fillBiomesFromNoise((noiseX, noiseY, noiseZ, sampler) -> {
                if (noiseX == quartX && noiseZ == quartZ) {
                    return biome;
                }
                return section.getNoiseBiome(noiseX & 3, noiseY & 3, noiseZ & 3);
            }, null, baseQuartX, baseQuartY, baseQuartZ);
            changed = true;
        }

        if (changed) {
            chunk.setUnsaved(true);
        }
        return changed;
    }

    public static void resendCraterBiomes(ServerLevel level, ChunkPos chunkPos) {
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(level.getChunk(chunkPos.x, chunkPos.z)));
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
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
        return registry.getHolder(key);
    }

    private static ResourceKey<Biome> biomeKey(String name) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(HbmNtm.MOD_ID, name));
    }

    private CraterBiomeUtil() {
    }
}
