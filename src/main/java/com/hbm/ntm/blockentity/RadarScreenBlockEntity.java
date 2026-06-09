package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarScanResult;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class RadarScreenBlockEntity extends BlockEntity {
    private static final String TAG_LINKED = "linked";
    private static final String TAG_REF = "ref";
    private static final String TAG_RANGE = "range";
    private static final String TAG_ENTRIES = "Entries";

    private final List<RadarEntry> entries = new ArrayList<>();
    private BlockPos refPos = BlockPos.ZERO;
    private int range;
    private boolean linked;

    public RadarScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_RADAR_SCREEN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarScreenBlockEntity screen) {
        if (level.isClientSide) {
            return;
        }
        if (screen.linked || !screen.entries.isEmpty()) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        screen.entries.clear();
        screen.linked = false;
        screen.setChanged();
    }

    public void receiveRadarUpdate(RadarBlockEntity radar) {
        entries.clear();
        entries.addAll(radar.getEntries());
        refPos = radar.getBlockPos();
        range = radar.getRange();
        linked = true;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public List<RadarEntry> getEntries() {
        return List.copyOf(entries);
    }

    public RadarScanResult getScanResultSnapshot() {
        return new RadarScanResult(entries, false);
    }

    public int getEntryAmount() {
        return entries.size();
    }

    public Optional<RadarEntry> getEntryAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().entryAtLegacyIndex(legacyIndex);
    }

    public Optional<Boolean> isEntryPlayerAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().isPlayerAtLegacyIndex(legacyIndex);
    }

    public OptionalInt getEntryTypeAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().typeAtLegacyIndex(legacyIndex);
    }

    public Optional<RadarEntry.LegacyEntityInfo> getEntryInfoAtLegacyIndex(int legacyIndex) {
        return getScanResultSnapshot().entityInfoAtLegacyIndex(legacyIndex);
    }

    public BlockPos getRefPos() {
        return refPos;
    }

    public int getRange() {
        return range;
    }

    public boolean isLinked() {
        return linked;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 1.0D,
                worldPosition.getY() - 1.0D,
                worldPosition.getZ() - 1.0D,
                worldPosition.getX() + 2.0D,
                worldPosition.getY() + 2.0D,
                worldPosition.getZ() + 2.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_LINKED, linked);
        tag.putLong(TAG_REF, refPos.asLong());
        tag.putInt(TAG_RANGE, range);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        linked = tag.getBoolean(TAG_LINKED);
        refPos = BlockPos.of(tag.getLong(TAG_REF));
        range = tag.getInt(TAG_RANGE);
        if (tag.contains(TAG_ENTRIES, Tag.TAG_LIST)) {
            entries.clear();
            RadarEntry.readListInto(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND), entries);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.put(TAG_ENTRIES, RadarEntry.writeList(entries));
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
