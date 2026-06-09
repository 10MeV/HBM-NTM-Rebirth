package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.entity.RadarControl;
import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarLaunchCommand;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarMap;
import com.hbm.ntm.api.entity.RadarScanProvider;
import com.hbm.ntm.api.entity.RadarScanResult;
import com.hbm.ntm.api.entity.RadarScanner;
import com.hbm.ntm.api.entity.RadarStatusSnapshot;
import com.hbm.ntm.config.RadarConfig;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.item.ItemCoordinateBase;
import com.hbm.ntm.menu.RadarMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RadarBlockEntity extends HbmEnergyBlockEntity
        implements LegacyLookOverlayProvider, MenuProvider, RadarRedstoneSource, RadarScanProvider {
    public static final long MAX_POWER = RadarConfig.POWER_CAP_DEFAULT;
    public static final long CONSUMPTION = RadarConfig.CONSUMPTION_DEFAULT;
    public static final int SLOT_COUNT = 10;
    public static final int SLOT_LINKER = 8;
    public static final int SLOT_BATTERY = 9;

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
    private static final int PING_INTERVAL = 80;
    private static final float ROTATION_STEP = 5.0F;

    private static final String TAG_SCAN_MISSILES = "scanMissiles";
    private static final String TAG_SCAN_SHELLS = "scanShells";
    private static final String TAG_SCAN_PLAYERS = "scanPlayers";
    private static final String TAG_SMART_MODE = "smartMode";
    private static final String TAG_RED_MODE = "redMode";
    private static final String TAG_SHOW_MAP = "showMap";
    private static final String TAG_JAMMED = "jammed";
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_LAST_RED_POWER = "lastPower";
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_MAP = "map";
    private static final String TAG_MAP_CLEAR = "mapClear";
    private static final String TAG_MAP_SLICE = "mapSlice";
    private static final String TAG_MAP_SLICE_INDEX = "mapSliceIndex";

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
            return (slot >= 0 && slot < SLOT_LINKER && isCommandLinkItem(stack))
                    || (slot == SLOT_LINKER && stack.is(ModItems.RADAR_LINKER.get()))
                    || (slot == SLOT_BATTERY && HbmInventoryMenuHelper.isBatteryLike(stack));
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
        long previousPower = radar.energy.getPower();
        boolean previousJammed = radar.jammed;
        int previousRedPower = radar.lastRedPower;
        int previousEntryCount = radar.entries.size();

        if (level.getGameTime() % 20L == 0L) {
            radar.refreshEnergyConnections();
        }

        HbmEnergyUtil.chargeStorageFromItem(radar.items.getStackInSlot(SLOT_BATTERY), radar.energy,
                radar.energy.getReceiverSpeed());
        radar.allocateTargets(serverLevel);
        radar.updateSonarPing(serverLevel);
        boolean mapChanged = radar.updateMapScan(serverLevel);
        radar.lastRedPower = radar.getRedPower();
        radar.updateLinkedScreen();

        boolean changed = previousPower != radar.energy.getPower()
                || previousJammed != radar.jammed
                || previousRedPower != radar.lastRedPower
                || previousEntryCount != radar.entries.size()
                || mapChanged
                || radar.mapClearDirty;

        if (previousRedPower != radar.lastRedPower) {
            level.updateNeighborsAt(pos, state.getBlock());
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (changed) {
            radar.setChanged();
            if (mapChanged || radar.mapClearDirty) {
                ModMessages.syncTileToTracking(radar, radar);
                radar.mapClearDirty = false;
            }
            if (level.getGameTime() % 10L == 0L || previousRedPower != radar.lastRedPower
                    || previousJammed != radar.jammed) {
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RadarBlockEntity radar) {
        radar.previousRotation = radar.rotation;
        if (radar.getPower() > 0L) {
            radar.rotation += ROTATION_STEP;
            if (radar.rotation >= 360.0F) {
                radar.rotation -= 360.0F;
                radar.previousRotation -= 360.0F;
            }
        }
    }

    public void refreshEnergyConnections() {
        if (energyPortDistance <= 1) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                subscribeEnergyReceiverToSide(side);
            }
            return;
        }
        subscribeEnergyReceiverToPorts();
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
        pingTimer++;
        if (energy.getPower() > 0L && pingTimer >= PING_INTERVAL) {
            serverLevel.playSound(null, worldPosition, ModSounds.BLOCK_SONAR_PING.get(),
                    SoundSource.BLOCKS, 5.0F, 1.0F);
            pingTimer = 0;
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

        int slice = RadarMap.sliceForGameTime(serverLevel.getGameTime());
        int start = RadarMap.sliceStart(slice);
        int chunkLoads = 0;
        int chunkLoadCap = RadarConfig.mapChunkLoadCap();
        boolean generateChunks = RadarConfig.mapGenerateChunks();
        for (int offset = 0; offset < MAP_SLICE_SIZE; offset++) {
            int index = start + offset;
            int sampleX = RadarMap.sampleX(worldPosition, getRange(), index);
            int sampleZ = RadarMap.sampleZ(worldPosition, getRange(), index);
            int chunkX = WorldUtil.blockToChunkCoord(sampleX);
            int chunkZ = WorldUtil.blockToChunkCoord(sampleZ);
            boolean chunkLoaded = serverLevel.hasChunk(chunkX, chunkZ);

            if (!chunkLoaded && map[index] == 0 && chunkLoads < chunkLoadCap
                    && tryLoadMapChunk(serverLevel, chunkX, chunkZ, generateChunks)) {
                chunkLoads++;
                chunkLoaded = serverLevel.hasChunk(chunkX, chunkZ);
            }
            if (chunkLoaded) {
                writeMapHeight(serverLevel, index, sampleX, sampleZ);
            }
        }
        lastMapSlice = slice;
        return true;
    }

    private boolean tryLoadMapChunk(ServerLevel serverLevel, int chunkX, int chunkZ, boolean generateChunks) {
        try {
            if (generateChunks) {
                serverLevel.getChunk(chunkX, chunkZ);
                return true;
            }
            return WorldUtil.provideChunk(serverLevel, chunkX, chunkZ).isPresent();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void writeMapHeight(ServerLevel serverLevel, int index, int sampleX, int sampleZ) {
        map[index] = RadarMap.sampleHeight(serverLevel, sampleX, sampleZ);
    }

    private void normalizeMap() {
        map = RadarMap.normalize(map);
    }

    public int getRange() {
        return range;
    }

    public int getRedPower() {
        return RadarScanner.redstonePower(entries, worldPosition, getRange(), redMode);
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
        return RadarLaunchCommand.positionTag(linkSlot, x, z);
    }

    public static CompoundTag launchEntityTag(int linkSlot, int entityId) {
        return RadarLaunchCommand.entityTag(linkSlot, entityId);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        if (energyPortDistance <= 1) {
            return List.of();
        }
        return List.of(
                port(Direction.EAST),
                port(Direction.WEST),
                port(Direction.SOUTH),
                port(Direction.NORTH));
    }

    private EnergyPort port(Direction direction) {
        return new EnergyPort(new BlockPos(
                direction.getStepX() * energyPortDistance,
                0,
                direction.getStepZ() * energyPortDistance), direction);
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
        Optional<RadarControl> control = RadarControl.fromTag(tag);
        if (control.isEmpty()) {
            handleLaunchControl(player, tag);
            return;
        }
        switch (control.get()) {
            case SCAN_MISSILES -> scanMissiles = !scanMissiles;
            case SCAN_SHELLS -> scanShells = !scanShells;
            case SCAN_PLAYERS -> scanPlayers = !scanPlayers;
            case SMART_MODE -> smartMode = !smartMode;
            case REDSTONE_MODE -> redMode = !redMode;
            case SHOW_MAP -> showMap = !showMap;
            case CLEAR_MAP -> clearFlag = true;
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
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

        int x = command.target().x();
        int z = command.target().z();
        if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_PANEL
                && satellite.interfaceActions().contains(Satellite.InterfaceAction.CAN_CLICK)) {
            satellite.onClick(serverLevel, x, z);
            SatelliteSavedData.get(serverLevel).setDirty();
            playTechBleep(player);
        } else if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_COORD) {
            int y = satellite.coordActions().contains(Satellite.CoordAction.HAS_Y)
                    ? serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)
                    : 60;
            satellite.onCoordAction(serverLevel, player, x, y, z);
            SatelliteSavedData.get(serverLevel).setDirty();
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
        net.minecraft.world.level.block.entity.BlockEntity target =
                MultiblockHelper.resolveCoreBlockEntity(serverLevel, receiverPos);
        if (!(target instanceof com.hbm.ntm.api.entity.RadarCommandReceiver receiver)) {
            return;
        }
        if (command.dispatch(serverLevel, worldPosition, receiver).successful()) {
            playTechBleep(player);
        }
    }

    private void playTechBleep(ServerPlayer player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BLEEP.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    private static boolean isCommandLinkItem(ItemStack stack) {
        return stack.is(ModItems.SAT_RELAY.get()) || stack.is(ModItems.RADAR_LINKER.get());
    }

    private void updateLinkedScreen() {
        if (level == null || level.isClientSide) {
            return;
        }
        ItemStack linker = items.getStackInSlot(SLOT_LINKER);
        if (!linker.is(ModItems.RADAR_LINKER.get())) {
            return;
        }
        BlockPos screenPos = ItemCoordinateBase.getPosition(linker);
        if (screenPos == null || !(level.getBlockEntity(screenPos) instanceof RadarScreenBlockEntity screen)) {
            return;
        }
        screen.receiveRadarUpdate(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_SCAN_MISSILES, scanMissiles);
        tag.putBoolean(TAG_SCAN_SHELLS, scanShells);
        tag.putBoolean(TAG_SCAN_PLAYERS, scanPlayers);
        tag.putBoolean(TAG_SMART_MODE, smartMode);
        tag.putBoolean(TAG_RED_MODE, redMode);
        tag.putBoolean(TAG_SHOW_MAP, showMap);
        tag.putBoolean(TAG_JAMMED, jammed);
        tag.putInt(TAG_LAST_RED_POWER, lastRedPower);
        normalizeMap();
        tag.putByteArray(TAG_MAP, map);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_LEGACY_POWER, Tag.TAG_LONG)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        scanMissiles = readBooleanOrDefault(tag, TAG_SCAN_MISSILES, true);
        scanShells = readBooleanOrDefault(tag, TAG_SCAN_SHELLS, true);
        scanPlayers = readBooleanOrDefault(tag, TAG_SCAN_PLAYERS, true);
        smartMode = readBooleanOrDefault(tag, TAG_SMART_MODE, true);
        redMode = readBooleanOrDefault(tag, TAG_RED_MODE, true);
        showMap = tag.getBoolean(TAG_SHOW_MAP);
        jammed = tag.getBoolean(TAG_JAMMED);
        lastRedPower = tag.getInt(TAG_LAST_RED_POWER);
        if (tag.contains(TAG_MAP, Tag.TAG_BYTE_ARRAY)) {
            map = tag.getByteArray(TAG_MAP);
            normalizeMap();
        }
        readMapSync(tag);
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
        tag.putBoolean(TAG_SCAN_MISSILES, scanMissiles);
        tag.putBoolean(TAG_SCAN_SHELLS, scanShells);
        tag.putBoolean(TAG_SCAN_PLAYERS, scanPlayers);
        tag.putBoolean(TAG_SMART_MODE, smartMode);
        tag.putBoolean(TAG_RED_MODE, redMode);
        tag.putBoolean(TAG_SHOW_MAP, showMap);
        tag.putBoolean(TAG_JAMMED, jammed);
        tag.putInt(TAG_LAST_RED_POWER, lastRedPower);
        tag.put(TAG_ENTRIES, RadarEntry.writeList(entries));
        if (mapClearDirty) {
            tag.putBoolean(TAG_MAP_CLEAR, true);
        }
        if (showMap && lastMapSlice >= 0) {
            normalizeMap();
            int start = RadarMap.sliceStart(lastMapSlice);
            tag.putShort(TAG_MAP_SLICE_INDEX, (short) lastMapSlice);
            tag.putByteArray(TAG_MAP_SLICE, Arrays.copyOfRange(map, start, start + MAP_SLICE_SIZE));
        }
        return tag;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        scanMissiles = readBooleanOrDefault(tag, TAG_SCAN_MISSILES, true);
        scanShells = readBooleanOrDefault(tag, TAG_SCAN_SHELLS, true);
        scanPlayers = readBooleanOrDefault(tag, TAG_SCAN_PLAYERS, true);
        smartMode = readBooleanOrDefault(tag, TAG_SMART_MODE, true);
        redMode = readBooleanOrDefault(tag, TAG_RED_MODE, true);
        showMap = tag.getBoolean(TAG_SHOW_MAP);
        jammed = tag.getBoolean(TAG_JAMMED);
        lastRedPower = tag.getInt(TAG_LAST_RED_POWER);
        entries.clear();
        RadarEntry.readListInto(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND), entries);
        readMapSync(tag);
    }

    private void readMapSync(CompoundTag tag) {
        normalizeMap();
        if (tag.getBoolean(TAG_MAP_CLEAR)) {
            Arrays.fill(map, (byte) 0);
        }
        if (tag.contains(TAG_MAP_SLICE_INDEX, Tag.TAG_SHORT) && tag.contains(TAG_MAP_SLICE, Tag.TAG_BYTE_ARRAY)) {
            int slice = tag.getShort(TAG_MAP_SLICE_INDEX) & 0xFFFF;
            byte[] data = tag.getByteArray(TAG_MAP_SLICE);
            if (RadarMap.validSliceData(slice, data)) {
                System.arraycopy(data, 0, map, RadarMap.sliceStart(slice), MAP_SLICE_SIZE);
            }
        }
    }

    private static boolean readBooleanOrDefault(CompoundTag tag, String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_BYTE) ? tag.getBoolean(key) : fallback;
    }

}
