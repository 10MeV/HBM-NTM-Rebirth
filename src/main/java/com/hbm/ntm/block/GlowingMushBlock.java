package com.hbm.ntm.block;

import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.registry.ModBlocks;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowingMushBlock extends BushBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = box(4.8D, 0.0D, 4.8D, 11.2D, 6.4D, 11.2D);

    public GlowingMushBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return super.mayPlaceOn(state, level, pos) || canMushGrowOn(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        if (RadiationConfig.myceliumSpreadEnabled()
                && level.getBlockState(pos.below()).is(ModBlocks.WASTE_EARTH.get())
                && random.nextInt(5) == 0) {
            level.setBlock(pos.below(), ModBlocks.WASTE_MYCELIUM.get().defaultBlockState(), 2);
        }

        if (random.nextInt(25) != 0 || nearbyMushrooms(level, pos) >= 3) {
            return;
        }

        BlockPos candidate = pos.offset(random.nextInt(5) - 2, random.nextInt(2) - random.nextInt(2),
                random.nextInt(5) - 2);
        for (int i = 0; i < 4; i++) {
            if (canSpreadTo(level, candidate)) {
                pos = candidate;
            }
            candidate = pos.offset(random.nextInt(5) - 2, random.nextInt(2) - random.nextInt(2),
                    random.nextInt(5) - 2);
        }

        if (canSpreadTo(level, candidate)) {
            level.setBlock(candidate, defaultBlockState(), 2);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean clientSide) {
        return state.canSurvive(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(net.minecraft.world.level.Level level, RandomSource random, BlockPos pos,
            BlockState state) {
        return random.nextFloat() < 0.4F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.removeBlock(pos, false);
        generateHugeMush(level, pos);
    }

    private static void generateHugeMush(ServerLevel level, BlockPos pos) {
        BlockState cap = ModBlocks.MUSH_BLOCK.get().defaultBlockState();
        BlockState stem = ModBlocks.MUSH_BLOCK_STEM.get().defaultBlockState();
        placeSquare(level, pos, 0, 1, cap);
        placeSquare(level, pos, 3, 1, cap);
        placeSquare(level, pos, 5, 2, cap);
        for (int y = 6; y < 9; y++) {
            placeSquare(level, pos, y, 4, cap);
        }
        placeSquare(level, pos, 9, 3, cap);
        placeSquare(level, pos, 10, 1, cap);
        for (int y = 0; y < 8; y++) {
            level.setBlock(pos.above(y), stem, 2);
        }
    }

    private static void placeSquare(ServerLevel level, BlockPos origin, int yOffset, int radius, BlockState state) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(origin.offset(x, yOffset, z), state, 2);
            }
        }
    }

    private static int nearbyMushrooms(BlockGetter level, BlockPos pos) {
        int count = 0;
        for (BlockPos scan : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(scan).is(ModBlocks.MUSH.get()) && ++count >= 3) {
                return count;
            }
        }
        return count;
    }

    private static boolean canSpreadTo(ServerLevel level, BlockPos pos) {
        return level.isInWorldBounds(pos)
                && level.isEmptyBlock(pos)
                && canMushGrowOn(level.getBlockState(pos.below()));
    }

    private static boolean canMushGrowOn(BlockState state) {
        Set<net.minecraft.world.level.block.Block> blocks = Set.of(
                ModBlocks.WASTE_EARTH.get(),
                ModBlocks.WASTE_MYCELIUM.get(),
                ModBlocks.WASTE_TRINITITE.get(),
                ModBlocks.WASTE_TRINITITE_RED.get());
        return blocks.contains(state.getBlock())
                || ModBlocks.legacyBlock("block_waste") != null && state.is(ModBlocks.legacyBlock("block_waste").get())
                || ModBlocks.legacyBlock("block_waste_painted") != null
                        && state.is(ModBlocks.legacyBlock("block_waste_painted").get())
                || ModBlocks.legacyBlock("block_waste_vitrified") != null
                        && state.is(ModBlocks.legacyBlock("block_waste_vitrified").get());
    }
}
