package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.entity.RadarControl;
import com.hbm.ntm.api.entity.RadarControlState;
import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarEnergyConnectionProfile;
import com.hbm.ntm.api.entity.RadarLaunchCommand;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarHostSyncState;
import com.hbm.ntm.api.entity.RadarHostTickProfile;
import com.hbm.ntm.api.entity.RadarLinkedReceiverCommand;
import com.hbm.ntm.api.entity.RadarInventoryProfile;
import com.hbm.ntm.api.entity.RadarMap;
import com.hbm.ntm.api.entity.RadarMapScanOperation;
import com.hbm.ntm.api.entity.RadarMapScanSlice;
import com.hbm.ntm.api.entity.RadarMapUpdate;
import com.hbm.ntm.api.entity.RadarScanProvider;
import com.hbm.ntm.api.entity.RadarScanResult;
import com.hbm.ntm.api.entity.RadarScanner;
import com.hbm.ntm.api.entity.RadarRedstoneMode;
import com.hbm.ntm.api.entity.RadarSatelliteCommand;
import com.hbm.ntm.api.entity.RadarScreenLinkUpdate;
import com.hbm.ntm.api.entity.RadarStatusSnapshot;
import com.hbm.ntm.api.entity.RadarSyncSnapshot;
import com.hbm.ntm.config.RadarConfig;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.item.ItemCoordinateBase;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class RadarBlockEntity extends HbmEnergyBlockEntity
        implements LegacyLookOverlayProvider, MenuProvider, RadarRedstoneSource, RadarScanProvider {
    public static final long MAX_POWER = RadarConfig.POWER_CAP_DEFAULT;
    public static final long CONSUMPTION = RadarConfig.CONSUMPTION_DEFAULT;
    public static final int SLOT_COUNT = RadarInventoryProfile.SLOT_COUNT;
    public static final int SLOT_LINKER = RadarInventoryProfile.SLOT_LINKER;
    public static final int SLOT_BATTERY = RadarInventoryProfile.SLOT_BATTERY;

    public static final int CONTROL_SCAN_MISSILES = RadarControl.SCAN_MISSILES.id();
    public static final int CONTROL_SCAN_SHELLS = RadarControl.SCAN_SHELLS.id();
    public static final int CONTROL_SCAN_PLAYERS = RadarControl.SCAN_PLAYERS.id();
    public static final int CONTROL_SMART_MODE = RadarControl.SMART_MODE.id();
    public static final int CONTROL_RED_MODE = RadarControl.REDSTONE_MODE.id();
    public static final int CONTROL_SHOW_MAP = RadarControl.SHOW_MAP.id();
    public static final int CONTROL_CLEAR_MAP = RadarControl.CLEAR_MAP.id();

    public static final int MAP_WIDTH = RadarMap.WIDTH;
    public static final int MAP_SIZE = RadarMap.SIZE;
    public static final int MAP_SLICE_COUNT = RadarMap.SLICE_COUNT;
    public static final int MAP_SLICE_SIZE = RadarMap.SLICE_SIZE;
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_LEGACY_POWER = "power";

    private boolean scanMissiles = true;
    private boolean scanShells = true;
    private boolean scanPlayers = true;
    private boolean smartMode = true;
    private boolean redMode = true;
    private boolean showMap;
    private boolean jammed;
    private byte[] map = new byte[MAP_SIZE];
    private boolean clearFlag;
    private boolean mapClearDirty;
    private int lastMapSlice = -1;
    private int lastRedPower;
    private int pingTimer;
    private float previousRotation;
    private float rotation;
    private final List<RadarEntry> entries = new ArrayList<>();
    private final int range;
    private final int energyPortDistance;
    private final AABB renderBoundsOffset;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return RadarInventoryProfile.isValidStack(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };

    public RadarBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MACHINE_RADAR.get(), pos, state, RadarConfig.radarRange(), 1,
                new AABB(-1.0D, 0.0D, -1.0D, 2.0D, 3.0D, 2.0D));
    }

    protected RadarBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState state, int range, int energyPortDistance, AABB renderBoundsOffset) {
        super(type, pos, state, new HbmEnergyStorage(RadarConfig.powerCap(), RadarConfig.powerCap(), 0L));
        this.range = range;
        this.energyPortDistance = energyPortDistance;
        this.renderBoundsOffset = renderBoundsOffset;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarBlockEntity radar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        radar.applyConfiguredEnergyLimits();
        RadarHostSyncState previousSyncState = radar.syncState();

        if (RadarHostTickProfile.shouldRefreshEnergyConnections(level.getGameTime())) {
            radar.refreshEnergyConnections();
        }

        HbmEnergyUtil.chargeStorageFromItem(radar.items.getStackInSlot(SLOT_BATTERY), radar.energy,
                radar.energy.getReceiverSpeed());
        radar.allocateTargets(serverLevel);
        radar.updateSonarPing(serverLevel);
        boolean mapChanged = radar.updateMapScan(serverLevel);
        radar.lastRedPower = radar.getRedPower();
        radar.updateLinkedScreen();

        RadarHostSyncState.SyncPlan syncPlan = previousSyncState.planAfter(radar.syncState(), mapChanged,
                radar.mapClearDirty, level.getGameTime());

        if (syncPlan.redstoneChanged()) {
            level.updateNeighborsAt(pos, state.getBlock());
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (syncPlan.changed()) {
            radar.setChanged();
            if (syncPlan.mapSync()) {
                ModMessages.syncTileToTracking(radar, radar);
                radar.mapClearDirty = false;
            }
            if (syncPlan.blockUpdate()) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }
        radar.networkPackNT(50);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RadarBlockEntity radar) {
        RadarHostTickProfile.Rotation rotation = RadarHostTickProfile.advanceRotation(
                radar.previousRotation, radar.rotation, radar.getPower() > 0L);
        radar.previousRotation = rotation.previous();
        radar.rotation = rotation.current();
    }

    public void refreshEnergyConnections() {
        for (Direction side : RadarEnergyConnectionProfile.directConnectionSides(energyPortDistance)) {
            subscribeEnergyReceiverToSide(side);
        }
        if (energyPortDistance > 1) {
            subscribeEnergyReceiverToPorts();
        }
    }

    private void allocateTargets(ServerLevel serverLevel) {
        entries.clear();
        jammed = false;

        if (worldPosition.getY() < RadarConfig.radarAltitude()) {
            return;
        }
        long consumption = RadarConfig.consumption();
        if (energy.getPower() < consumption) {
            energy.setPower(0L);
            return;
        }

        energy.setPower(energy.getPower() - consumption);
        RadarScanResult result = RadarScanner.scan(RadarContext.legacy(serverLevel, worldPosition, getRange(),
                RadarConfig.radarBuffer(), RadarConfig.radarAltitude(), scanParams()));
        jammed = result.jammed();
        entries.addAll(result.entries());
    }

    private void applyConfiguredEnergyLimits() {
        long powerCap = RadarConfig.powerCap();
        energy.setMaxPower(powerCap);
        energy.setTransferRates(powerCap, 0L);
    }

    private void updateSonarPing(ServerLevel serverLevel) {
        RadarHostTickProfile.SonarPing ping = RadarHostTickProfile.sonarPing(pingTimer, energy.getPower());
        pingTimer = ping.timer();
        if (ping.playSound()) {
            serverLevel.playSound(null, worldPosition, ModSounds.BLOCK_SONAR_PING.get(),
                    SoundSource.BLOCKS, 5.0F, 1.0F);
        }
    }

    private RadarDetectable.RadarScanParams scanParams() {
        return new RadarDetectable.RadarScanParams(scanMissiles, scanShells, scanPlayers, smartMode);
    }

    private boolean updateMapScan(ServerLevel serverLevel) {
        normalizeMap();

        if (clearFlag) {
            Arrays.fill(map, (byte) 0);
            clearFlag = false;
            mapClearDirty = true;
            lastMapSlice = -1;
            return true;
        }

        if (!showMap) {
            lastMapSlice = -1;
            return false;
        }

        RadarMapScanSlice scanSlice = RadarMapScanSlice.forGameTime(worldPosition, getRange(),
                serverLevel.getGameTime());
        RadarMapScanOperation.execute(serverLevel, map, scanSlice, RadarConfig.mapChunkLoadCap(),
                RadarConfig.mapGenerateChunks());
        lastMapSlice = scanSlice.slice();
        return true;
    }

    private void normalizeMap() {
        map = RadarMap.normalize(map);
    }

    private RadarHostSyncState syncState() {
        return new RadarHostSyncState(energy.getPower(), jammed, lastRedPower, entries.size());
    }

    public int getRange() {
        return range;
    }

    public int getRedPower() {
        return getRedstoneMode().power(entries, worldPosition, getRange());
    }

    @Override
    public int redstoneOutput() {
        return getRedPower();
    }

    public List<RadarEntry> getEntries() {
        return List.copyOf(entries);
    }

    public RadarScanResult getScanResultSnapshot() {
        return new RadarScanResult(entries, jammed);
    }

    public RadarStatusSnapshot getStatusSnapshot() {
        return new RadarStatusSnapshot(worldPosition, getRange(), getPower(), getMaxPower(), jammed,
                entries.size(), redstoneOutput(), scanParams(), redMode, showMap);
    }

    public RadarDetectable.RadarScanParams getScanSettings() {
        return scanParams();
    }

    public void setScanSettings(RadarDetectable.RadarScanParams params) {
        RadarDetectable.RadarScanParams settings = params != null
                ? params
                : RadarDetectable.RadarScanParams.DEFAULT;
        scanMissiles = settings.scanMissiles();
        scanShells = settings.scanShells();
        scanPlayers = settings.scanPlayers();
        smartMode = settings.smartMode();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public boolean isJammed() {
        return jammed;
    }

    public boolean isShowMap() {
        return showMap;
    }

    public byte[] getMap() {
        normalizeMap();
        return map;
    }

    public boolean isScanMissiles() {
        return scanMissiles;
    }

    public boolean isScanShells() {
        return scanShells;
    }

    public boolean isScanPlayers() {
        return scanPlayers;
    }

    public boolean isSmartMode() {
        return smartMode;
    }

    public boolean isRedMode() {
        return redMode;
    }

    public RadarRedstoneMode getRedstoneMode() {
        return RadarRedstoneMode.fromLegacyFlag(redMode);
    }

    public int getLastRedPower() {
        return lastRedPower;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        getStatusSnapshot().writeTo(data);
    }

    public float getPreviousRotation() {
        return previousRotation;
    }

    public float getRotation() {
        return rotation;
    }

    public static CompoundTag controlTag(int control) {
        return RadarControl.byId(control)
                .map(RadarControl::toTag)
                .orElseGet(CompoundTag::new);
    }

    public static CompoundTag controlTag(RadarControl control) {
        return control != null ? control.toTag() : new CompoundTag();
    }

    public static CompoundTag launchPositionTag(int linkSlot, int x, int z) {
        return RadarLaunchCommand.position(linkSlot, x, z).toTag();
    }

    public static CompoundTag launchEntityTag(int linkSlot, int entityId) {
        return RadarLaunchCommand.entity(linkSlot, entityId).toTag();
    }

    @Override
    protected com.hbm.ntm.energy.HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return RadarEnergyConnectionProfile.sideMode(side);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return RadarEnergyConnectionProfile.energyPorts(energyPortDistance);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return renderBoundsOffset.move(worldPosition);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.energyStorage(getPower(), getMaxPower()));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadarMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        if (RadarControl.isControlTag(tag)) {
            return true;
        }
        return RadarLaunchCommand.isValidTag(tag, SLOT_LINKER);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        boolean changed = applyControlState(tag);
        if (RadarLaunchCommand.fromTag(tag).isPresent()) {
            handleLaunchControl(player, tag);
        }
        if (!changed) {
            return;
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean applyControlState(CompoundTag tag) {
        List<RadarControl> controls = RadarControl.controlsFromTag(tag);
        if (controls.isEmpty()) {
            return false;
        }
        RadarControlState.Application application =
                RadarControlState.of(scanParams(), redMode, showMap).apply(controls);
        setControlState(application.state());
        if (application.clearMap()) {
            clearFlag = true;
        }
        return true;
    }

    private void setControlState(RadarControlState state) {
        scanMissiles = state.scanSettings().scanMissiles();
        scanShells = state.scanSettings().scanShells();
        scanPlayers = state.scanSettings().scanPlayers();
        smartMode = state.scanSettings().smartMode();
        redMode = state.redstoneProximityMode();
        showMap = state.showMap();
    }

    private void handleLaunchControl(ServerPlayer player, CompoundTag tag) {
        RadarLaunchCommand command = RadarLaunchCommand.fromTag(tag).orElse(null);
        if (command == null || !command.isValidLinkSlot(SLOT_LINKER)) {
            return;
        }
        int linkSlot = command.linkSlot();
        ItemStack link = items.getStackInSlot(linkSlot);
        if (link.isEmpty()) {
            return;
        }
        if (link.is(ModItems.SAT_RELAY.get())) {
            handleSatelliteRelayControl(player, link, command);
            return;
        }
        if (link.is(ModItems.RADAR_LINKER.get())) {
            handleRadarLinkerControl(player, link, command);
        }
    }

    private void handleSatelliteRelayControl(ServerPlayer player, ItemStack link, RadarLaunchCommand command) {
        if (!(level instanceof ServerLevel serverLevel) || command.target().isEntity()) {
            return;
        }
        Satellite satellite = SatelliteSavedData.get(serverLevel).getSatFromFreq(ISatelliteChip.getFrequencyFromStack(link));
        if (satellite == null) {
            return;
        }
        if (RadarSatelliteCommand.dispatch(serverLevel, player, satellite, command).successful()) {
            playTechBleep(player);
        }
    }

    private void handleRadarLinkerControl(ServerPlayer player, ItemStack link, RadarLaunchCommand command) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos receiverPos = ItemCoordinateBase.getPosition(link);
        if (receiverPos == null) {
            return;
        }
        if (RadarLinkedReceiverCommand.dispatch(serverLevel, worldPosition, receiverPos, command).successful()) {
            playTechBleep(player);
        }
    }

    private void playTechBleep(ServerPlayer player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BLEEP.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private void updateLinkedScreen() {
        ItemStack linker = items.getStackInSlot(SLOT_LINKER);
        RadarScreenLinkUpdate.deliver(level, linker,
                RadarScreenLinkUpdate.snapshot(worldPosition, getRange(), entries));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        syncSnapshot(RadarMapUpdate.NONE).writeTo(tag, false);
        normalizeMap();
        RadarMap.writeTo(tag, map);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_LEGACY_POWER, Tag.TAG_LONG)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        applySyncSnapshot(RadarSyncSnapshot.fromTag(tag), tag.contains(TAG_LEGACY_POWER, Tag.TAG_LONG), false);
        map = RadarMap.readFrom(tag, map);
        map = RadarMapUpdate.fromTag(tag).applyTo(map);
        if (tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        }
        if (tag.contains(TAG_ENTRIES, Tag.TAG_LIST)) {
            entries.clear();
            RadarEntry.readListInto(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND), entries);
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        syncSnapshot(mapUpdateSnapshot()).writeTo(tag, true);
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        applySyncSnapshot(RadarSyncSnapshot.fromTag(tag), tag.contains(TAG_LEGACY_POWER, Tag.TAG_LONG),
                tag.contains(TAG_ENTRIES, Tag.TAG_LIST));
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

    private RadarSyncSnapshot syncSnapshot(RadarMapUpdate mapUpdate) {
        return RadarSyncSnapshot.of(getPower(), scanParams(), redMode, showMap, jammed, lastRedPower, entries,
                mapUpdate);
    }

    private void applySyncSnapshot(RadarSyncSnapshot snapshot, boolean applyPower, boolean includeEntries) {
        if (applyPower) {
            energy.setPower(snapshot.power());
        }
        scanMissiles = snapshot.scanSettings().scanMissiles();
        scanShells = snapshot.scanSettings().scanShells();
        scanPlayers = snapshot.scanSettings().scanPlayers();
        smartMode = snapshot.scanSettings().smartMode();
        redMode = snapshot.redstoneProximityMode();
        showMap = snapshot.showMap();
        jammed = snapshot.jammed();
        lastRedPower = snapshot.redstonePower();
        if (includeEntries) {
            entries.clear();
            entries.addAll(snapshot.entries());
        }
        map = snapshot.mapUpdate().applyTo(map);
    }

    private RadarMapUpdate mapUpdateSnapshot() {
        if (mapClearDirty) {
            return RadarMapUpdate.CLEAR;
        }
        if (!showMap || lastMapSlice < 0) {
            return RadarMapUpdate.NONE;
        }
        normalizeMap();
        return RadarMapUpdate.sliceFromMap(lastMapSlice, map);
    }

}
