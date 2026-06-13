package com.hbm.ntm.blockentity;

import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LegacyLanternBlockEntity extends BlockEntity implements HbmLegacyLoadedTile {
    private final HbmLegacyLoadedTileState legacyLoadedState = new HbmLegacyLoadedTileState();
    private boolean broken;
    private int comTimer = -1;

    public LegacyLanternBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_LANTERN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyLanternBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        if (blockEntity.comTimer >= 0) {
            blockEntity.comTimer--;
            blockEntity.setChanged();
        }
        blockEntity.networkPackNT(250);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedState;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        if (this.broken == broken) {
            return;
        }
        this.broken = broken;
        markChangedAndUpdate();
    }

    public int getComTimer() {
        return comTimer;
    }

    public void setComTimer(int comTimer) {
        if (this.comTimer == comTimer) {
            return;
        }
        this.comTimer = comTimer;
        markChangedAndUpdate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putBoolean("isBroken", broken);
        tag.putInt("comTimer", comTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        broken = tag.getBoolean("isBroken");
        comTimer = tag.getInt("comTimer");
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeBoolean(broken);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        broken = data.readBoolean();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 6, 1));
    }
}
