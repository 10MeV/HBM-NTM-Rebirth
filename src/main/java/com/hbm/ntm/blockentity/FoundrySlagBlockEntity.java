package com.hbm.ntm.blockentity;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FoundrySlagBlockEntity extends BlockEntity {
    public static final int MAX_AMOUNT = MaterialShapes.BLOCK.q(16);
    private static final String TAG_MATERIAL = "mat";
    private static final String TAG_AMOUNT = "amount";

    private NTMMaterial material;
    private int amount;

    public FoundrySlagBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_SLAG.get(), pos, state);
    }

    public NTMMaterial getMaterialType() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public float getFillLevel() {
        return Math.max(0.0625F, Math.min(1.0F, amount / (float) MAX_AMOUNT));
    }

    public ItemStack asScrap() {
        return material == null || amount <= 0
                ? ItemStack.EMPTY
                : FoundryScrapsItem.create(new Mats.MaterialStack(material, amount));
    }

    public void addMaterial(NTMMaterial material, int amount) {
        if (material == null || amount <= 0) {
            return;
        }
        this.material = material;
        this.amount = Math.min(MAX_AMOUNT, this.amount + amount);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void consume(int amount) {
        if (amount <= 0) {
            return;
        }
        this.amount = Math.max(0, this.amount - amount);
        if (this.amount == 0) {
            this.material = null;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (material != null) {
            tag.putInt(TAG_MATERIAL, material.id);
        }
        tag.putInt(TAG_AMOUNT, amount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        material = Mats.matById.get(tag.getInt(TAG_MATERIAL));
        amount = tag.getInt(TAG_AMOUNT);
        if (material == null || amount <= 0) {
            material = null;
            amount = 0;
        }
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
