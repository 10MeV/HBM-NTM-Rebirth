package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Source-compatible targetX/targetY/targetZ ownership record for a BlastDoor dummy. */
public class BlastDoorDummyBlockEntity extends BlockEntity {
    private BlockPos corePos = BlockPos.ZERO;

    public BlastDoorDummyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_DOOR_DUMMY.get(), pos, state);
    }

    public BlockPos corePos() {
        return corePos;
    }

    public void setCorePos(BlockPos corePos) {
        this.corePos = corePos.immutable();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("targetX", corePos.getX());
        tag.putInt("targetY", corePos.getY());
        tag.putInt("targetZ", corePos.getZ());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        corePos = new BlockPos(tag.getInt("targetX"), tag.getInt("targetY"), tag.getInt("targetZ"));
    }
}
