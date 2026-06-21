package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.RadioTorchBlock;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.menu.RadioTorchMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public abstract class RadioTorchBlockEntity extends BlockEntity
        implements LegacyLookOverlayProvider, MenuProvider, HbmLegacyLoadedTile {
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();

    protected RadioTorchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(RadioTorchBlock.FACING) ? state.getValue(RadioTorchBlock.FACING) : Direction.UP;
    }

    public BlockPos attachedPos() {
        return worldPosition.relative(facing().getOpposite());
    }

    protected BlockEntity attachedBlockEntity(Level level) {
        return MultiblockHelper.resolveOperationalCoreBlockEntity(level, attachedPos());
    }

    protected int attachedRedstoneInput(Level level) {
        BlockPos attached = attachedPos();
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, attached);
        if (core != null) {
            if (!MultiblockHelper.ensureOperationalCoreLayoutComplete(level, core.pos())) {
                return 0;
            }
            attached = core.pos();
        }
        BlockState attachedState = level.getBlockState(attached);
        if (attachedState.hasAnalogOutputSignal()) {
            return attachedState.getAnalogOutputSignal(level, attached);
        }
        return level.getSignal(attached, facing());
    }

    public boolean applyRadioConfiguration(CompoundTag tag) {
        return false;
    }

    protected int legacyNetworkPackRange() {
        return 50;
    }

    protected void networkPackLegacyRadioTorch() {
        int range = legacyNetworkPackRange();
        if (range > 0) {
            networkPackNT(range);
        }
    }

    public List<Component> describeRadioConfiguration() {
        return List.of(Component.literal("No RTTY configuration"));
    }

    protected boolean finishRadioConfiguration(boolean changed, boolean updateNeighbors) {
        if (changed) {
            setChangedAndSync(updateNeighbors);
        }
        return changed;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadioTorchMenu(containerId, inventory, this);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag != null && !tag.isEmpty();
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        applyRadioConfiguration(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    protected void setChangedAndSync(boolean updateNeighbors) {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
            if (updateNeighbors) {
                level.updateNeighborsAt(worldPosition, state.getBlock());
                level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
            }
        }
    }

    protected void selfDestruct() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos pos = worldPosition;
        level.destroyBlock(pos, false);
        WeaponExplosionUtil.smooth(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                5.0F, null, 50.0F, 1.0D, false, 5.0F, 0.5F).explode();
    }
}
