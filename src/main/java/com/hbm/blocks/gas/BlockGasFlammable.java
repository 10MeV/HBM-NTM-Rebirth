package com.hbm.blocks.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

/**
 * Source-backed 1.7.10 flammable gas behavior.
 */
@SuppressWarnings("deprecation")
public class BlockGasFlammable extends BlockGasBase {
    private static final Set<Block> FIRE_SOURCES = Set.of(
            Blocks.FIRE,
            Blocks.LAVA,
            Blocks.TORCH,
            Blocks.JACK_O_LANTERN);

    public BlockGasFlammable() {
        super(0.8F, 0.8F, 0.2F);
    }

    @Override
    protected Direction firstDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return random.nextInt(3) == 0 ? Direction.from3DDataValue(random.nextInt(2)) : randomHorizontal(random);
    }

    @Override
    protected Direction secondDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return randomHorizontal(random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            if (isFireSource(level.getBlockState(pos.relative(direction)).getBlock())) {
                combust(level, pos);
                return;
            }
        }

        if (random.nextInt(20) == 0 && level.getBlockState(pos.below()).isAir()) {
            level.removeBlock(pos, false);
            return;
        }

        super.tick(state, level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity.isOnFire()) {
            combust(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (isFireSource(level.getBlockState(pos.relative(direction)).getBlock())) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    protected boolean schedulesOnNeighborUpdates() {
        return false;
    }

    protected void combust(Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
    }

    public boolean isFireSource(Block block) {
        return FIRE_SOURCES.contains(block);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    protected int getDelay(ServerLevel level) {
        return level.random.nextInt(5) + 16;
    }
}
