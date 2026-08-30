package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.SealControllerBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Persistent controller ownership for one runtime seal hatch cell. */
public class SealHatchBlockEntity extends BlockEntity {
    private BlockPos controllerPos = BlockPos.ZERO;

    public SealHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEAL_HATCH.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SealHatchBlockEntity hatch) {
        if (!level.getBlockState(hatch.controllerPos).is(ModBlocks.SEAL_CONTROLLER.get())
                || SealControllerBlock.getFrameSize(level, hatch.controllerPos) == 0) {
            level.removeBlock(pos, false);
        }
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos.immutable();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("x1", controllerPos.getX());
        tag.putInt("y1", controllerPos.getY());
        tag.putInt("z1", controllerPos.getZ());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerPos = new BlockPos(tag.getInt("x1"), tag.getInt("y1"), tag.getInt("z1"));
    }
}
