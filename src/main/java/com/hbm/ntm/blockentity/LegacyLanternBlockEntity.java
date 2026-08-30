package com.hbm.ntm.blockentity;

import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class LegacyLanternBlockEntity extends BlockEntity implements HbmLegacyLoadedTile {
    private final HbmLegacyLoadedTileState legacyLoadedState = new HbmLegacyLoadedTileState();
    private boolean broken;
    private int comTimer = -1;

    public LegacyLanternBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LEGACY_LANTERN.get(), pos, state);
    }

    protected LegacyLanternBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyLanternBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        if (blockEntity.comTimer >= 0) {
            blockEntity.comTimer--;
            blockEntity.setChanged();
        }
        // TileEntityLantern#updateEntity: the lamp's actual light sits five blocks above its base.
        if (level.getGameTime() % 20L == 0L) {
            AABB area = new AABB(pos.getX() + 0.5D, pos.getY() + 5.5D, pos.getZ() + 0.5D,
                    pos.getX() + 0.5D, pos.getY() + 5.5D, pos.getZ() + 0.5D).inflate(7.5D);
            for (EntityGlyphid glyphid : level.getEntitiesOfClass(EntityGlyphid.class, area)) {
                glyphid.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            }
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
        return getClientSyncTag();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        handleClientSyncTag(packet.getTag());
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        // The normal lantern has no runtime packet in 1.7.10, while the
        // Behemoth packet contains isBroken.  A modern chunk snapshot must
        // also retain the loaded-tile state rather than calling load() with a
        // partial tag and resetting it client-side.
        writeLegacyLoadedTileClientTag(tag);
        tag.putBoolean("isBroken", broken);
        tag.putInt("comTimer", comTimer);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        if (tag == null) {
            return;
        }
        readLegacyLoadedTileClientTag(tag);
        broken = tag.getBoolean("isBroken");
        comTimer = tag.getInt("comTimer");
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
