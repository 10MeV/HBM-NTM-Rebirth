package com.hbm.ntm.worldgen;

import com.hbm.ntm.registry.ModBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** Exact {@code DungeonToolbox#generateFlowers(..., ModBlocks.reeds, 0)} placement contract. */
public final class LegacyReedPatchFeature extends Feature<LegacyReedPatchFeature.Configuration> {
    private static final int SET_BLOCK_FLAGS = Block.UPDATE_CLIENTS;
    private static final int LEGACY_ATTEMPTS = 64;

    public LegacyReedPatchFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        RandomSource random = context.random();
        if (random.nextInt(context.config().chance()) != 0) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int sourceX = origin.getX() + random.nextInt(16) + 8;
        int sourceZ = origin.getZ() + random.nextInt(16) + 8;
        int sourceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sourceX, sourceZ);
        BlockState reeds = ModBlocks.PLANT_REEDS.get().defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        for (int attempt = 0; attempt < LEGACY_ATTEMPTS; attempt++) {
            int x = sourceX + random.nextInt(8) - random.nextInt(8);
            int y = sourceY + random.nextInt(4) - random.nextInt(4);
            int z = sourceZ + random.nextInt(8) - random.nextInt(8);
            cursor.set(x, y, z);
            if (level.isOutsideBuildHeight(cursor) || !level.getBlockState(cursor).isAir()) {
                continue;
            }
            if (reeds.canSurvive(level, cursor)) {
                placedAny |= level.setBlock(cursor, reeds, SET_BLOCK_FLAGS);
            }
        }
        return placedAny;
    }

    public record Configuration(int chance) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("chance").forGetter(Configuration::chance)
        ).apply(instance, Configuration::new));
    }
}
