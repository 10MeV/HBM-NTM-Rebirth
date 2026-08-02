package com.hbm.ntm.blockentity;

import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyPedestalBlockEntity extends BlockEntity {
    private static final String TAG_ITEM = "item";

    private ItemStack item = ItemStack.EMPTY;

    public LegacyPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL.get(), pos, state);
    }

    public boolean hasItem() {
        return !item.isEmpty();
    }

    public ItemStack getItem() {
        return item.copy();
    }

    public void setItem(ItemStack stack) {
        item = stack == null ? ItemStack.EMPTY : stack.copy();
        setChangedAndSync();
    }

    public void clearItem() {
        item = ItemStack.EMPTY;
        setChangedAndSync();
    }

    /** Source {@code TileEntityPedestal#updateEntity}: passive gold-defuser scan every 60 ticks. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyPedestalBlockEntity pedestal) {
        if (level.getGameTime() % 60 != 0 || !pedestal.item.is(ModItems.DEFUSER_GOLD.get())) {
            return;
        }
        for (Creeper creeper : level.getEntitiesOfClass(Creeper.class,
                new net.minecraft.world.phys.AABB(pos).inflate(25.0D))) {
            ArmorModItems.Defuser.defuse(creeper, null, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!item.isEmpty()) {
            tag.put(TAG_ITEM, item.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        item = tag.contains(TAG_ITEM, Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound(TAG_ITEM)) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return new CompoundTag();
}

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
