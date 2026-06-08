package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.RadioTorchBlock;
import com.hbm.ntm.menu.RadioTorchMenu;
import com.hbm.ntm.network.HbmTileSyncable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
        implements LegacyLookOverlayProvider, MenuProvider, HbmTileSyncable {
    protected RadioTorchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(RadioTorchBlock.FACING) ? state.getValue(RadioTorchBlock.FACING) : Direction.UP;
    }

    public BlockPos attachedPos() {
        return worldPosition.relative(facing().getOpposite());
    }

    protected int attachedRedstoneInput(Level level) {
        BlockPos attached = attachedPos();
        BlockState attachedState = level.getBlockState(attached);
        if (attachedState.hasAnalogOutputSignal()) {
            return attachedState.getAnalogOutputSignal(level, attached);
        }
        return level.getSignal(attached, facing());
    }

    public boolean applyRadioConfiguration(CompoundTag tag) {
        return false;
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
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag != null && !tag.isEmpty();
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        applyRadioConfiguration(tag);
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
}
