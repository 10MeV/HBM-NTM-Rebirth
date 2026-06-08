package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.entity.RadarContext;
import com.hbm.ntm.api.entity.RadarDetectable;
import com.hbm.ntm.api.entity.RadarEntry;
import com.hbm.ntm.api.entity.RadarScanResult;
import com.hbm.ntm.api.entity.RadarScanner;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
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

public class RadarBlockEntity extends HbmEnergyBlockEntity implements LegacyLookOverlayProvider, MenuProvider {
    public static final long MAX_POWER = 100_000L;
    public static final long CONSUMPTION = 500L;
    public static final int SLOT_COUNT = 10;
    public static final int SLOT_LINKER = 8;
    public static final int SLOT_BATTERY = 9;

    public static final int CONTROL_SCAN_MISSILES = 0;
    public static final int CONTROL_SCAN_SHELLS = 1;
    public static final int CONTROL_SCAN_PLAYERS = 2;
    public static final int CONTROL_SMART_MODE = 3;
    public static final int CONTROL_RED_MODE = 4;
    public static final int CONTROL_SHOW_MAP = 5;
    public static final int CONTROL_CLEAR_MAP = 6;

    public static final int MAP_WIDTH = 200;
    public static final int MAP_SIZE = MAP_WIDTH * MAP_WIDTH;
    public static final int MAP_SLICE_COUNT = 400;
    public static final int MAP_SLICE_SIZE = 100;
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
    private static final String TAG_CONTROL = "control";
    private static final String TAG_LINK = "link";
    private static final String TAG_LAUNCH_ENTITY = "launchEntity";
    private static final String TAG_LAUNCH_POS_X = "launchPosX";
    private static final String TAG_LAUNCH_POS_Z = "launchPosZ";
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
        this(ModBlockEntities.MACHINE_RADAR.get(), pos, state, RadarContext.LEGACY_RANGE, 1,
                new AABB(-1.0D, 0.0D, -1.0D, 2.0D, 3.0D, 2.0D));
    }

    protected RadarBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState state, int range, int energyPortDistance, AABB renderBoundsOffset) {
        super(type, pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
        this.range = range;
        this.energyPortDistance = energyPortDistance;
        this.renderBoundsOffset = renderBoundsOffset;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadarBlockEntity radar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

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

        if (worldPosition.getY() < RadarContext.LEGACY_MINIMUM_ALTITUDE) {
            return;
        }
        if (energy.getPower() < CONSUMPTION) {
            energy.setPower(0L);
            return;
        }

        energy.setPower(energy.getPower() - CONSUMPTION);
        RadarScanResult result = RadarScanner.scan(RadarContext.legacy(serverLevel, worldPosition, getRange(),
                scanParams()));
        jammed = result.jammed();
        entries.addAll(result.entries());
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

        int slice = (int) (serverLevel.getGameTime() % MAP_SLICE_COUNT);
        int start = slice * MAP_SLICE_SIZE;
        for (int offset = 0; offset < MAP_SLICE_SIZE; offset++) {
            int index = start + offset;
            int gridX = index % MAP_WIDTH;
            int gridZ = index / MAP_WIDTH;
            int sampleX = worldPosition.getX() - getRange() + gridX * getRange() * 2 / MAP_WIDTH;
            int sampleZ = worldPosition.getZ() - getRange() + gridZ * getRange() * 2 / MAP_WIDTH;

            if (serverLevel.hasChunk(sampleX >> 4, sampleZ >> 4)) {
                int height = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, sampleX, sampleZ);
                map[index] = (byte) Mth.clamp(height, 50, 128);
            }
        }
        lastMapSlice = slice;
        return true;
    }

    private void normalizeMap() {
        if (map == null || map.length != MAP_SIZE) {
            map = new byte[MAP_SIZE];
        }
    }

    public int getRange() {
        return range;
    }

    public int getRedPower() {
        return RadarScanner.redstonePower(entries, worldPosition, getRange(), redMode);
    }

    public List<RadarEntry> getEntries() {
        return List.copyOf(entries);
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

    public float getPreviousRotation() {
        return previousRotation;
    }

    public float getRotation() {
        return rotation;
    }

    public static CompoundTag controlTag(int control) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_CONTROL, control);
        return tag;
    }

    public static CompoundTag launchPositionTag(int linkSlot, int x, int z) {
        CompoundTag tag = launchTag(linkSlot);
        tag.putInt(TAG_LAUNCH_POS_X, x);
        tag.putInt(TAG_LAUNCH_POS_Z, z);
        return tag;
    }

    public static CompoundTag launchEntityTag(int linkSlot, int entityId) {
        CompoundTag tag = launchTag(linkSlot);
        tag.putInt(TAG_LAUNCH_ENTITY, entityId);
        return tag;
    }

    private static CompoundTag launchTag(int linkSlot) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_LINK, linkSlot);
        return tag;
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
        if (tag.contains(TAG_CONTROL, Tag.TAG_INT)) {
            return true;
        }
        if (!tag.contains(TAG_LINK, Tag.TAG_INT)) {
            return false;
        }
        int linkSlot = tag.getInt(TAG_LINK);
        return linkSlot >= 0 && linkSlot < SLOT_LINKER
                && (tag.contains(TAG_LAUNCH_ENTITY, Tag.TAG_INT)
                || tag.contains(TAG_LAUNCH_POS_X, Tag.TAG_INT) && tag.contains(TAG_LAUNCH_POS_Z, Tag.TAG_INT));
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (!tag.contains(TAG_CONTROL, Tag.TAG_INT)) {
            handleLaunchControl(player, tag);
            return;
        }
        switch (tag.getInt(TAG_CONTROL)) {
            case CONTROL_SCAN_MISSILES -> scanMissiles = !scanMissiles;
            case CONTROL_SCAN_SHELLS -> scanShells = !scanShells;
            case CONTROL_SCAN_PLAYERS -> scanPlayers = !scanPlayers;
            case CONTROL_SMART_MODE -> smartMode = !smartMode;
            case CONTROL_RED_MODE -> redMode = !redMode;
            case CONTROL_SHOW_MAP -> showMap = !showMap;
            case CONTROL_CLEAR_MAP -> clearFlag = true;
            default -> {
                return;
            }
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void handleLaunchControl(ServerPlayer player, CompoundTag tag) {
        int linkSlot = tag.getInt(TAG_LINK);
        ItemStack link = items.getStackInSlot(linkSlot);
        if (link.isEmpty()) {
            return;
        }
        if (link.is(ModItems.SAT_RELAY.get())) {
            handleSatelliteRelayControl(player, link, tag);
            return;
        }
        if (link.is(ModItems.RADAR_LINKER.get())) {
            handleRadarLinkerControl(player, link, tag);
        }
    }

    private void handleSatelliteRelayControl(ServerPlayer player, ItemStack link, CompoundTag tag) {
        if (!(level instanceof ServerLevel serverLevel) || !tag.contains(TAG_LAUNCH_POS_X, Tag.TAG_INT)
                || !tag.contains(TAG_LAUNCH_POS_Z, Tag.TAG_INT)) {
            return;
        }
        Satellite satellite = SatelliteSavedData.get(serverLevel).getSatFromFreq(ISatelliteChip.getFrequencyFromStack(link));
        if (satellite == null) {
            return;
        }

        int x = tag.getInt(TAG_LAUNCH_POS_X);
        int z = tag.getInt(TAG_LAUNCH_POS_Z);
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

    private void handleRadarLinkerControl(ServerPlayer player, ItemStack link, CompoundTag tag) {
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
        if (tag.contains(TAG_LAUNCH_ENTITY, Tag.TAG_INT)) {
            Entity entity = serverLevel.getEntity(tag.getInt(TAG_LAUNCH_ENTITY));
            if (entity != null && receiver.sendCommandEntity(entity)) {
                playTechBleep(player);
            }
            return;
        }
        if (tag.contains(TAG_LAUNCH_POS_X, Tag.TAG_INT) && tag.contains(TAG_LAUNCH_POS_Z, Tag.TAG_INT)
                && receiver.sendCommandPosition(tag.getInt(TAG_LAUNCH_POS_X), worldPosition.getY(),
                tag.getInt(TAG_LAUNCH_POS_Z))) {
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
            HbmInventoryMenuHelper.loadLegacyItemsCompound(tag, TAG_ITEMS, items);
        }
        if (tag.contains(TAG_ENTRIES, Tag.TAG_LIST)) {
            entries.clear();
            readEntries(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND), entries);
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
        tag.put(TAG_ENTRIES, writeEntries(entries));
        if (mapClearDirty) {
            tag.putBoolean(TAG_MAP_CLEAR, true);
        }
        if (showMap && lastMapSlice >= 0) {
            normalizeMap();
            int start = lastMapSlice * MAP_SLICE_SIZE;
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
        readEntries(tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND), entries);
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
            if (slice >= 0 && slice < MAP_SLICE_COUNT && data.length == MAP_SLICE_SIZE) {
                System.arraycopy(data, 0, map, slice * MAP_SLICE_SIZE, MAP_SLICE_SIZE);
            }
        }
    }

    private static boolean readBooleanOrDefault(CompoundTag tag, String key, boolean fallback) {
        return tag.contains(key, Tag.TAG_BYTE) ? tag.getBoolean(key) : fallback;
    }

    private static ListTag writeEntries(List<RadarEntry> entries) {
        ListTag list = new ListTag();
        for (RadarEntry entry : entries) {
            CompoundTag tag = new CompoundTag();
            tag.putString("name", entry.name());
            tag.putInt("blipLevel", entry.blipLevel());
            tag.putInt("x", entry.pos().getX());
            tag.putInt("y", entry.pos().getY());
            tag.putInt("z", entry.pos().getZ());
            tag.putString("dimension", entry.dimension().toString());
            tag.putInt("entityId", entry.entityId());
            tag.putBoolean("redstone", entry.redstone());
            list.add(tag);
        }
        return list;
    }

    private static void readEntries(ListTag list, List<RadarEntry> target) {
        for (Tag rawTag : list) {
            if (rawTag instanceof CompoundTag tag) {
                target.add(new RadarEntry(
                        tag.getString("name"),
                        tag.getInt("blipLevel"),
                        new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
                        new ResourceLocation(tag.getString("dimension")),
                        tag.getInt("entityId"),
                        tag.getBoolean("redstone")));
            }
        }
    }
}
