package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CraneSplitterBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CraneSplitterBlockEntity extends BlockEntity {
    private static final String TAG_POSITION = "pos";
    private static final String TAG_REMAINING = "count";
    private static final String TAG_LEFT = "left";
    private static final String TAG_RIGHT = "right";

    private boolean position;
    private byte remaining;
    private byte leftRatio = 1;
    private byte rightRatio = 1;

    public CraneSplitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRANE_SPLITTER.get(), pos, state);
    }

    public ItemStack[] splitStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new ItemStack[] { ItemStack.EMPTY, ItemStack.EMPTY };
        }

        int left = 0;
        int right = 0;
        int count = stack.getCount();

        if (remaining <= 0) {
            remaining = position ? rightRatio : leftRatio;
        }

        while (count > 0) {
            int toExtract = Math.min(remaining, count);
            remaining -= toExtract;
            count -= toExtract;
            if (position) {
                right += toExtract;
            } else {
                left += toExtract;
            }

            if (remaining <= 0) {
                position = !position;
                remaining = position ? rightRatio : leftRatio;
            }
        }

        ItemStack leftStack = stack.copy();
        ItemStack rightStack = stack.copy();
        leftStack.setCount(left);
        rightStack.setCount(right);
        markChangedAndSync();
        return new ItemStack[] { leftStack, rightStack };
    }

    public int getLeftRatio() {
        return leftRatio;
    }

    public int getRightRatio() {
        return rightRatio;
    }

    public void adjustLeftRatio(int adjustment) {
        byte next = clampRatio(leftRatio + adjustment);
        if (next != leftRatio) {
            leftRatio = next;
            markChangedAndSync();
        }
    }

    public void adjustRightRatio(int adjustment) {
        byte next = clampRatio(rightRatio + adjustment);
        if (next != rightRatio) {
            rightRatio = next;
            markChangedAndSync();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        position = tag.getBoolean(TAG_POSITION);
        remaining = tag.getByte(TAG_REMAINING);
        leftRatio = clampRatio(tag.getByte(TAG_LEFT));
        rightRatio = clampRatio(tag.getByte(TAG_RIGHT));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_POSITION, position);
        tag.putByte(TAG_REMAINING, remaining);
        tag.putByte(TAG_LEFT, leftRatio);
        tag.putByte(TAG_RIGHT, rightRatio);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos side = worldPosition.relative(CraneSplitterBlock.sideOffset(getBlockState()));
        return new AABB(worldPosition).minmax(new AABB(side)).inflate(0.0625D);
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static byte clampRatio(int value) {
        return (byte) Mth.clamp(value, 1, 16);
    }
}
