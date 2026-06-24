package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.SpotlightBeamBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LegacySpotlightBlock extends LegacyDirectionalShapeBlock {
    private final int beamLength;
    private final boolean active;
    private final String onBlockName;
    private final String offBlockName;

    public LegacySpotlightBlock(BlockBehaviour.Properties properties, Kind kind, int beamLength, boolean active,
            String onBlockName, String offBlockName) {
        super(properties, kind, false);
        this.beamLength = beamLength;
        this.active = active;
        this.onBlockName = onBlockName;
        this.offBlockName = offBlockName;
    }

    public boolean isActive() {
        return active;
    }

    public static Block incandescent(BlockBehaviour.Properties properties, boolean active) {
        return new LegacySpotlightBlock(properties, Kind.SPOTLIGHT_INCANDESCENT, 2, active,
                "spotlight_incandescent", "spotlight_incandescent_off");
    }

    public static Block fluoro(BlockBehaviour.Properties properties, boolean active) {
        return new LegacySpotlightBlock(properties, Kind.SPOTLIGHT_FLUORO, 8, active,
                "spotlight_fluoro", "spotlight_fluoro_off");
    }

    public static Block halogen(BlockBehaviour.Properties properties, boolean active) {
        return new LegacySpotlightBlock(properties, Kind.SPOTLIGHT_HALOGEN, 32, active,
                "spotlight_halogen", "spotlight_halogen_off");
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            if (!updatePower(level, pos, state)) {
                updateBeam(level, pos, state);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (level.isClientSide || neighborBlock instanceof SpotlightBeamBlock) {
            return;
        }
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (!updatePower(level, pos, state)) {
            updateBeam(level, pos, state);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (active && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, copyDirectionalState(state, offBlock().defaultBlockState()), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            unpropagateBeam(level, pos, state.getValue(FACE));
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        Direction face = state.getValue(FACE);
        BlockPos attached = pos.relative(face.getOpposite());
        return level.getBlockState(attached).isFaceSturdy(level, attached, face);
    }

    private boolean updatePower(Level level, BlockPos pos, BlockState state) {
        boolean powered = level.hasNeighborSignal(pos);
        if (active && powered) {
            level.scheduleTick(pos, this, 4);
            return true;
        }
        if (!active && !powered) {
            level.setBlock(pos, copyDirectionalState(state, onBlock().defaultBlockState()), Block.UPDATE_ALL);
            return true;
        }
        return false;
    }

    private void updateBeam(Level level, BlockPos pos, BlockState state) {
        if (active) {
            propagateBeam(level, pos, state.getValue(FACE), beamLength);
        }
    }

    private Block onBlock() {
        return ModBlocks.legacyBlock(onBlockName).get();
    }

    private Block offBlock() {
        return ModBlocks.legacyBlock(offBlockName).get();
    }

    private static BlockState copyDirectionalState(BlockState source, BlockState target) {
        return target.setValue(FACE, source.getValue(FACE))
                .setValue(TOP_BOTTOM_ROTATED, source.getValue(TOP_BOTTOM_ROTATED));
    }

    public static void propagateBeam(Level level, BlockPos source, Direction direction, int distance) {
        if (--distance <= 0) {
            return;
        }
        BlockPos pos = source.relative(direction);
        BlockState state = level.getBlockState(pos);
        if (!state.isAir() && !state.is(ModBlocks.SPOTLIGHT_BEAM.get())) {
            return;
        }
        if (!state.is(ModBlocks.SPOTLIGHT_BEAM.get())) {
            level.setBlock(pos, ModBlocks.SPOTLIGHT_BEAM.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (!(level.getBlockEntity(pos) instanceof SpotlightBeamBlockEntity beam)
                || beam.setDirection(direction, true) == 0) {
            return;
        }
        propagateBeam(level, pos, direction, distance);
    }

    public static void unpropagateBeam(LevelAccessor level, BlockPos source, Direction direction) {
        BlockPos pos = source.relative(direction);
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.SPOTLIGHT_BEAM.get())) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof SpotlightBeamBlockEntity beam
                && beam.setDirection(direction, false) == 0) {
            level.removeBlock(pos, false);
        }
        unpropagateBeam(level, pos, direction);
    }

    public static void backPropagate(Level level, BlockPos beamPos, Direction direction) {
        BlockPos pos = beamPos.relative(direction.getOpposite());
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LegacySpotlightBlock spotlight) {
            propagateBeam(level, pos, direction, spotlight.beamLength);
        } else if (state.is(ModBlocks.SPOTLIGHT_BEAM.get())) {
            backPropagate(level, pos, direction);
        }
    }
}
