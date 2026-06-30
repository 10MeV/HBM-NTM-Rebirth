package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LegacyNtmFlowerBlock extends BushBlock implements BonemealableBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    private final Kind kind;

    public LegacyNtmFlowerBlock(Properties properties, Kind kind) {
        super(properties.randomTicks());
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.FARMLAND)
                || isLegacySoil(state, "dirt_dead")
                || isLegacySoil(state, "dirt_oily");
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (isValidBonemealTarget(level, pos, state, false)
                && isBonemealSuccess(level, random, pos, state)
                && random.nextInt(3) == 0) {
            performBonemeal(level, random, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean clientSide) {
        return kind == Kind.CD0 && hasCadmiumWillowWater(level, pos);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.33F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (kind == Kind.CD0) {
            level.setBlock(pos, ModBlocks.PLANT_FLOWER_CD1.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static boolean hasCadmiumWillowWater(LevelReader level, BlockPos pos) {
        BlockPos base = pos.below();
        return level.getFluidState(base.east()).is(FluidTags.WATER)
                || level.getFluidState(base.west()).is(FluidTags.WATER)
                || level.getFluidState(base.north()).is(FluidTags.WATER)
                || level.getFluidState(base.south()).is(FluidTags.WATER);
    }

    private static boolean isLegacySoil(BlockState state, String name) {
        return ModBlocks.legacyBlock(name) != null && state.is(ModBlocks.legacyBlock(name).get());
    }

    public enum Kind {
        FOXGLOVE,
        TOBACCO,
        NIGHTSHADE,
        WEED,
        CD0,
        CD1;

        public int legacyMeta() {
            return ordinal();
        }

        public String idSuffix() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String textureName() {
            return "plant_flower." + idSuffix();
        }

        public static Kind byLegacyMeta(int meta) {
            Kind[] values = values();
            if (meta < 0 || meta >= values.length) {
                return FOXGLOVE;
            }
            return values[meta];
        }
    }
}
