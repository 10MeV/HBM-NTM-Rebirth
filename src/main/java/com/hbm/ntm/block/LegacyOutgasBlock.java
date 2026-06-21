package com.hbm.ntm.block;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public class LegacyOutgasBlock extends RadiatingHazardBlock {
    private final Supplier<? extends Block> gas;
    private final boolean onBreak;
    private final boolean onNeighbour;
    private final boolean walkingRelease;
    private final boolean volumeReleaseOnBreak;

    public LegacyOutgasBlock(String legacyName, Properties properties, Supplier<? extends Block> gas, boolean onBreak, boolean onNeighbour) {
        this(legacyName, properties, gas, onBreak, onNeighbour, false);
    }

    public LegacyOutgasBlock(String legacyName, Properties properties, Supplier<? extends Block> gas, boolean onBreak, boolean onNeighbour, boolean walkingRelease) {
        this(legacyName, properties, gas, onBreak, onNeighbour, walkingRelease, false);
    }

    public LegacyOutgasBlock(String legacyName, Properties properties, Supplier<? extends Block> gas, boolean onBreak, boolean onNeighbour,
            boolean walkingRelease, boolean volumeReleaseOnBreak) {
        super(legacyName, properties);
        this.gas = gas;
        this.onBreak = onBreak;
        this.onNeighbour = onNeighbour;
        this.walkingRelease = walkingRelease;
        this.volumeReleaseOnBreak = volumeReleaseOnBreak;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = Direction.values()[random.nextInt(6)];
        placeGas(level, pos.relative(direction));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || !onNeighbour || level.random.nextInt(3) != 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            placeGas(level, pos.relative(direction));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (level.isClientSide || !onBreak || state.getBlock() == newState.getBlock() || !newState.isAir()) {
            return;
        }

        level.setBlock(pos, gas.get().defaultBlockState(), Block.UPDATE_ALL);
        if (volumeReleaseOnBreak) {
            releaseGasVolume(level, pos);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        BlockPos above = pos.above();
        if (!level.isClientSide && walkingRelease && level.isEmptyBlock(above) && level.random.nextInt(10) == 0) {
            level.setBlock(above, gas.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (level.isClientSide && walkingRelease && gas.get() == ModBlocks.GAS_ASBESTOS.get()) {
            ParticleUtil.spawnOutgasTownAuraBurst(level, pos, level.random, 5);
        }
    }

    private void placeGas(Level level, BlockPos pos) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, gas.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void releaseGasVolume(Level level, BlockPos pos) {
        for (int ix = -2; ix <= 2; ix++) {
            for (int iy = -2; iy <= 2; iy++) {
                for (int iz = -2; iz <= 2; iz++) {
                    int legacySum = Math.abs(ix + iy + iz);
                    if (legacySum > 0 && legacySum < 5) {
                        placeGas(level, pos.offset(ix, iy, iz));
                    }
                }
            }
        }
    }
}

