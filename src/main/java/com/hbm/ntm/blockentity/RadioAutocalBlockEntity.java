package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RTTYAutocalState;
import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.menu.RadioAutocalMenu;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class RadioAutocalBlockEntity extends BlockEntity implements MenuProvider, HbmTileSyncable {
    private final RTTYAutocalState autocal = new RTTYAutocalState();
    private long ticksExisted;

    public RadioAutocalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADIO_AUTOCAL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadioAutocalBlockEntity blockEntity) {
        blockEntity.ticksExisted++;
        blockEntity.autocal.setMaxClockSpeed(ServerConfig.autocalMaxClockSpeed());
        blockEntity.autocal.tick(level);
        if (blockEntity.ticksExisted % 15L == 0L || blockEntity.autocal.isOn()) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public RTTYAutocalState autocalState() {
        return autocal;
    }

    public void sendControl(CompoundTag tag) {
        if (autocal.applyControl(tag)) {
            setChangedAndSync();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadioAutocalMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        autocal.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (level != null && level.isClientSide) {
            autocal.loadClient(tag);
        } else {
            autocal.load(tag);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        autocal.saveClient(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        autocal.loadClient(tag);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag != null && !tag.isEmpty();
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        sendControl(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D,
                pos.getZ() + 1.0D);
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
