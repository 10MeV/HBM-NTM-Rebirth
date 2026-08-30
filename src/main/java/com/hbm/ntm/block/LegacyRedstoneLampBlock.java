package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;

/**
 * The 1.7.10 reinforced and tritium lamps used separate hidden ON block IDs,
 * rather than a shared powered block state.  Keeping that replacement model
 * preserves legacy registry IDs, pick/drop targets, and the four-tick
 * unpower delay.
 */
public final class LegacyRedstoneLampBlock extends Block {
    public enum Kind {
        REINFORCED(null),
        TRITIUM_GREEN("lamp_tritium_green"),
        TRITIUM_BLUE("lamp_tritium_blue");

        private final String legacyName;

        Kind(String legacyName) {
            this.legacyName = legacyName;
        }

        private boolean hasBeam() {
            return legacyName != null;
        }
    }

    private final Kind kind;
    private final boolean on;
    private final String onBlockName;
    private final String offBlockName;

    public LegacyRedstoneLampBlock(BlockBehaviour.Properties properties, Kind kind, boolean on,
            String onBlockName, String offBlockName) {
        super(properties);
        this.kind = kind;
        this.on = on;
        this.onBlockName = onBlockName;
        this.offBlockName = offBlockName;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            updatePowerAndBeam(level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, moving);
        if (!level.isClientSide) {
            updatePowerAndBeam(level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (on && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, offBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!level.isClientSide && !state.is(newState.getBlock()) && kind.hasBeam()) {
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                LegacySpotlightBlock.unpropagateBeam(level, pos, direction);
            }
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(offBlock());
    }

    private void updatePowerAndBeam(Level level, BlockPos pos) {
        boolean powered = level.hasNeighborSignal(pos);
        if (on && !powered) {
            level.scheduleTick(pos, this, 4);
        } else if (!on && powered) {
            level.setBlock(pos, onBlock().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (on && kind.hasBeam()) {
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                LegacySpotlightBlock.propagateBeam(level, pos, direction, 8);
            }
        }
    }

    private Block onBlock() {
        return ModBlocks.legacyBlock(onBlockName).get();
    }

    private Block offBlock() {
        return ModBlocks.legacyBlock(offBlockName).get();
    }
}
