package com.hbm.ntm.blockentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.item.FoundryScrapsItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class FoundryBaseBlockEntity extends BlockEntity implements ICrucibleAcceptor {
    protected static final String TAG_TYPE = "type";
    protected static final String TAG_AMOUNT = "amount";

    protected NTMMaterial type;
    protected int amount;

    protected FoundryBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public NTMMaterial getMaterialType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getMoltenColor() {
        return type == null ? 0xFFFFFF : type.moltenColor;
    }

    public abstract int getCapacity();

    public boolean isEmpty() {
        return type == null || amount <= 0;
    }

    public ItemStack drainAsScrap() {
        if (type == null || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack scrap = FoundryScrapsItem.create(new MaterialStack(type, amount));
        type = null;
        amount = 0;
        setChangedAndUpdate();
        return scrap;
    }

    protected boolean standardCheck(MaterialStack stack) {
        if (stack == null || stack.material == null || stack.amount <= 0) {
            return false;
        }
        if (type != null && type != stack.material && amount > 0) {
            return false;
        }
        return amount < getCapacity();
    }

    protected MaterialStack standardAdd(MaterialStack stack) {
        type = stack.material;
        int accepted = Math.min(stack.amount, getCapacity() - amount);
        amount += accepted;
        if (amount <= 0) {
            type = null;
        }
        setChangedAndUpdate();
        return accepted >= stack.amount ? null : new MaterialStack(stack.material, stack.amount - accepted);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return standardCheck(stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return standardCheck(stack) ? standardAdd(stack) : stack;
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return side == Direction.UP && standardCheck(stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        return canAcceptPartialPour(level, pos, hit, side, stack) ? standardAdd(stack) : stack;
    }

    protected void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    protected void cleanupMaterial() {
        if (amount <= 0 || type == null) {
            amount = 0;
            type = null;
        } else if (amount > getCapacity()) {
            amount = getCapacity();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_TYPE, type == null ? -1 : type.id);
        tag.putInt(TAG_AMOUNT, amount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        type = Mats.matById.get(tag.getInt(TAG_TYPE));
        amount = tag.getInt(TAG_AMOUNT);
        cleanupMaterial();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }
    }
}
