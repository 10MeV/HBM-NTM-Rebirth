package com.hbm.ntm.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/**
 * Legacy-name sub-chunk snapshot facade.
 */
@Deprecated(forRemoval = false)
public final class SubChunkSnapshot {
    public static final SubChunkSnapshot EMPTY = new SubChunkSnapshot(com.hbm.ntm.world.SubChunkSnapshot.EMPTY);

    private final com.hbm.ntm.world.SubChunkSnapshot delegate;

    private SubChunkSnapshot(com.hbm.ntm.world.SubChunkSnapshot delegate) {
        this.delegate = delegate;
    }

    public static SubChunkSnapshot getSnapshot(Level level, SubChunkKey key, boolean allowGeneration) {
        com.hbm.ntm.world.SubChunkSnapshot snapshot =
                com.hbm.ntm.world.SubChunkSnapshot.getSnapshot(level, key == null ? null : key.modern(),
                        allowGeneration);
        return snapshot == com.hbm.ntm.world.SubChunkSnapshot.EMPTY ? EMPTY : new SubChunkSnapshot(snapshot);
    }

    public com.hbm.ntm.world.SubChunkSnapshot modern() {
        return delegate;
    }

    public Block getBlock(int x, int y, int z) {
        return delegate.getBlock(x, y, z);
    }

    public BlockState getBlockState(int x, int y, int z) {
        return delegate.getBlockState(x, y, z);
    }

    public Block getBlock(BlockPos worldPos) {
        return delegate.getBlock(worldPos);
    }

    public BlockState getBlockState(BlockPos worldPos) {
        return delegate.getBlockState(worldPos);
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public int paletteSize() {
        return delegate.paletteSize();
    }

    public List<BlockState> paletteSnapshot() {
        return delegate.paletteSnapshot();
    }

    public short[] dataCopy() {
        return delegate.dataCopy();
    }

    public int nonAirBlockCount() {
        return delegate.nonAirBlockCount();
    }

    public boolean containsNonAir() {
        return delegate.containsNonAir();
    }

    public boolean containsBlockState(BlockState state) {
        return delegate.containsBlockState(state);
    }

    public boolean containsBlock(Block block) {
        return delegate.containsBlock(block);
    }

    public int blockCount(Block block) {
        return delegate.blockCount(block);
    }

    public int blockStateCount(BlockState state) {
        return delegate.blockStateCount(state);
    }

    public Map<BlockState, Integer> blockStateCounts() {
        return delegate.blockStateCounts();
    }

    public Map<Block, Integer> blockCounts() {
        return delegate.blockCounts();
    }
}
