package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.entity.RadarScanProvider;
import com.hbm.ntm.api.entity.RadarScanResult;
import com.hbm.ntm.api.entity.RadarScreenDisplayProfile;
import com.hbm.ntm.api.entity.RadarScreenSnapshot;
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

public class RadarScreenBlockEntity extends BlockEntity implements RadarScanProvider {
    private RadarScreenSnapshot snapshot = RadarScreenSnapshot.UNLINKED;

    public RadarScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_RADAR_SCREEN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarScreenBlockEntity screen) {
        if (level.isClientSide) {
            return;
        }
        RadarScreenDisplayProfile.ServerTickPlan tickPlan =
                RadarScreenDisplayProfile.serverTick(screen.snapshot);
        if (tickPlan.syncBeforeReset()) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        screen.snapshot = tickPlan.nextSnapshot();
        screen.setChanged();
    }

    public void receiveRadarUpdate(RadarBlockEntity radar) {
        receiveRadarSnapshot(RadarScreenSnapshot.linked(radar.getBlockPos(), radar.getRange(), radar.getEntries()));
    }

    public void receiveRadarSnapshot(RadarScreenSnapshot snapshot) {
        this.snapshot = snapshot != null ? snapshot : RadarScreenSnapshot.UNLINKED;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public RadarScanResult getScanResultSnapshot() {
        return snapshot.scanResult();
    }

    public BlockPos getRefPos() {
        return snapshot.refPos();
    }

    public int getRange() {
        return snapshot.range();
    }

    public boolean isLinked() {
        return snapshot.linked();
    }

    public RadarScreenSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return RadarScreenDisplayProfile.renderBoundingBox(worldPosition);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(snapshot.toTag(false));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        snapshot = RadarScreenSnapshot.fromTag(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.merge(snapshot.toTag(true));
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
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}
