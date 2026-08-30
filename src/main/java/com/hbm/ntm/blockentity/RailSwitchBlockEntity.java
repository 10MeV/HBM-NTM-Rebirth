package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/** Persistent/synchronised carrier for legacy {@code TileEntityRailSwitch}. */
public final class RailSwitchBlockEntity extends BlockEntity {
    private static final String TAG_SWITCHED = "isSwitched";
    private boolean switched;

    public RailSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RAIL_SWITCH.get(), pos, state);
    }

    public boolean isSwitched() {
        return switched;
    }

    public void toggle() {
        setSwitched(!switched);
    }

    public void setSwitched(boolean switched) {
        if (this.switched == switched) {
            return;
        }
        this.switched = switched;
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_SWITCHED, switched);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        switched = tag.getBoolean(TAG_SWITCHED);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return clientStateTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            readClientStateTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        readClientStateTag(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-16, 0, -16), worldPosition.offset(17, 3, 17));
    }

    private CompoundTag clientStateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_SWITCHED, switched);
        return tag;
    }

    private void readClientStateTag(CompoundTag tag) {
        switched = tag.getBoolean(TAG_SWITCHED);
    }
}
