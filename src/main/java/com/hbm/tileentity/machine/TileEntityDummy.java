package com.hbm.tileentity.machine;

import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Source-compatibility anchor for the legacy 1.7.10 dummy tile entity.
 */
@Deprecated(forRemoval = false)
public class TileEntityDummy extends MultiblockDummyBlockEntity {
    private static final String TAG_LEGACY_TARGET_X = "tx";
    private static final String TAG_LEGACY_TARGET_Y = "ty";
    private static final String TAG_LEGACY_TARGET_Z = "tz";

    public int targetX;
    public int targetY;
    public int targetZ;

    public TileEntityDummy(BlockPos pos, BlockState state) {
        super(pos, state);
        syncLegacyFields(pos);
    }

    @Override
    public void setCorePos(BlockPos corePos) {
        super.setCorePos(corePos);
        syncLegacyFields(corePos);
    }

    @Override
    public void configure(BlockPos corePos, com.hbm.ntm.multiblock.LegacyProxyMode proxyMode, boolean legacyExtra) {
        super.configure(corePos, proxyMode, legacyExtra);
        syncLegacyFields(corePos);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        syncLegacyFields(getCorePos());
        super.saveAdditional(tag);
        tag.putInt(TAG_LEGACY_TARGET_X, targetX);
        tag.putInt(TAG_LEGACY_TARGET_Y, targetY);
        tag.putInt(TAG_LEGACY_TARGET_Z, targetZ);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        BlockPos corePos = getCorePos();
        if (corePos != null) {
            syncLegacyFields(corePos);
        } else {
            targetX = tag.getInt(TAG_LEGACY_TARGET_X);
            targetY = tag.getInt(TAG_LEGACY_TARGET_Y);
            targetZ = tag.getInt(TAG_LEGACY_TARGET_Z);
        }
    }

    private void syncLegacyFields(BlockPos corePos) {
        if (corePos == null) {
            return;
        }
        targetX = corePos.getX();
        targetY = corePos.getY();
        targetZ = corePos.getZ();
    }
}
