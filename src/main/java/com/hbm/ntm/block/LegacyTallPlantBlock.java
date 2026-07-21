package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Modern double-block rewrite of legacy {@code plant_tall} metadata states. */
public final class LegacyTallPlantBlock extends TallFlowerBlock {
    private final Kind kind;

    public LegacyTallPlantBlock(Properties properties, Kind kind) {
        super(properties.randomTicks());
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public boolean shouldAutosawIgnore(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER && (kind == Kind.CD2 || kind == Kind.CD3);
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
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return;
        }
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (kind == Kind.WEED && (isLegacySoil(level.getBlockState(pos.below()), "dirt_dead")
                || isLegacySoil(level.getBlockState(pos.below()), "dirt_oily"))) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(pos, ModBlocks.PLANT_DEAD_BIGFLOWER.get().defaultBlockState(), Block.UPDATE_ALL);
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
        if (kind != Kind.CD2 && kind != Kind.CD3) {
            return false;
        }
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (!hasAdjacentWater(level, lower)) {
            return false;
        }
        return kind != Kind.CD3 || isLegacySoil(level.getBlockState(lower.below()), "dirt_dead")
                || isLegacySoil(level.getBlockState(lower.below()), "dirt_oily");
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return kind == Kind.CD3 || random.nextFloat() < 0.33F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (kind != Kind.CD2 && kind != Kind.CD3) {
            return;
        }
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        LegacyTallPlantBlock next = (LegacyTallPlantBlock) ModBlocks.PLANT_TALL_BLOCKS.get(kind.legacyMeta() + 1).get();
        level.setBlock(lower, next.defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER), Block.UPDATE_ALL);
        level.setBlock(lower.above(), next.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
        if (kind == Kind.CD3) {
            level.setBlock(lower.below(), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos upperPos = pos.above();
            BlockState upper = level.getBlockState(upperPos);
            if (upper.is(this) && upper.getValue(HALF) == DoubleBlockHalf.UPPER) {
                if (!player.isCreative()) {
                    Block.dropResources(upper, level, upperPos, null, player, player.getMainHandItem());
                }
                level.setBlock(upperPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private static boolean hasAdjacentWater(LevelReader level, BlockPos lower) {
        BlockPos soil = lower.below();
        return level.getFluidState(soil.east()).is(FluidTags.WATER)
                || level.getFluidState(soil.west()).is(FluidTags.WATER)
                || level.getFluidState(soil.north()).is(FluidTags.WATER)
                || level.getFluidState(soil.south()).is(FluidTags.WATER);
    }

    private static boolean isLegacySoil(BlockState state, String name) {
        return ModBlocks.legacyBlock(name) != null && state.is(ModBlocks.legacyBlock(name).get());
    }

    public enum Kind {
        WEED,
        CD2,
        CD3,
        CD4;

        public int legacyMeta() {
            return ordinal();
        }

        public String idSuffix() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String textureName() {
            return "plant_tall." + idSuffix();
        }
    }
}
