package com.hbm.ntm.worldgen;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.world.feature.GlyphidHive;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Ordinary overworld Glyphid hive pass from legacy {@code HbmWorldGen}.
 * Reward-cache cells are intentionally handled by {@link GlyphidHive}'s exclusion-aware carrier.
 */
public final class GlyphidHiveFeature extends Feature<NoneFeatureConfiguration> {
    public GlyphidHiveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!RadiationConfig.glyphidHivesEnabled()) {
            return false;
        }

        RandomSource random = context.random();
        if (random.nextInt(RadiationConfig.glyphidHiveSpawnChunks()) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos chunkOrigin = context.origin();
        int x = chunkOrigin.getX() + random.nextInt(16) + 8;
        int z = chunkOrigin.getZ() + random.nextInt(16) + 8;
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);

        for (int offset = 3; offset >= -1; offset--) {
            BlockPos support = new BlockPos(x, surfaceY - 1 + offset, z);
            if (level.isOutsideBuildHeight(support)) {
                continue;
            }
            BlockState supportState = level.getBlockState(support);
            if (supportState.isCollisionShapeFullBlock(level, support)) {
                GlyphidHive.generateSmall(level, new BlockPos(x, surfaceY + offset, z), random,
                        random.nextInt(10) == 0, true);
                return true;
            }
        }
        return false;
    }
}
