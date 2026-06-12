package com.hbm.ntm.satellite;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class Satellite {
    private static final Map<Item, LegacySatelliteType> ITEM_TYPES = new IdentityHashMap<>();
    private static final EnumMap<LegacySatelliteType, String> CARGO_POOLS = new EnumMap<>(LegacySatelliteType.class);

    protected final EnumSet<InterfaceAction> interfaceActions = EnumSet.noneOf(InterfaceAction.class);
    protected final EnumSet<CoordAction> coordActions = EnumSet.noneOf(CoordAction.class);
    protected SatelliteInterface satelliteInterface = SatelliteInterface.NONE;

    static {
        for (LegacySatelliteType type : LegacySatelliteType.values()) {
            type.defaultCargoPool().ifPresent(cargo -> CARGO_POOLS.put(type, cargo));
        }
    }

    public static Satellite create(int legacyId) {
        LegacySatelliteType type = LegacySatelliteType.byLegacyId(legacyId);
        if (type == null) {
            return null;
        }
        return switch (type) {
            case MAPPER -> new SatelliteMapper();
            case SCANNER -> new SatelliteScanner();
            case RADAR -> new SatelliteRadar();
            case LASER -> new SatelliteLaser();
            case RESONATOR -> new SatelliteResonator();
            case RELAY -> new SatelliteRelay();
            case MINER -> new SatelliteMiner();
            case LUNAR_MINER -> new SatelliteLunarMiner();
            case HORIZONS -> new SatelliteHorizons();
        };
    }

    public static Satellite load(int legacyId, CompoundTag data) {
        Satellite satellite = create(legacyId);
        if (satellite != null) {
            satellite.readFromNBT(data);
        }
        return satellite;
    }

    public static boolean orbit(ServerLevel level, int legacyId, int frequency, double x, double y, double z) {
        Satellite satellite = create(legacyId);
        if (satellite == null) {
            return false;
        }
        satellite.onOrbit(level, x, y, z);
        SatelliteSavedData.get(level).putSatellite(frequency, satellite);
        return true;
    }

    public static boolean orbit(ServerLevel level, LegacySatelliteType type, int frequency, double x, double y, double z) {
        return type != null && orbit(level, type.legacyId(), frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, Item item, int frequency, double x, double y, double z) {
        return orbit(level, getLegacyIdFromItem(item), frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, ItemStack stack, int frequency, double x, double y, double z) {
        return stack != null && !stack.isEmpty() && orbit(level, stack.getItem(), frequency, x, y, z);
    }

    public static void registerSatelliteItem(Item item, LegacySatelliteType type) {
        if (item != null && type != null) {
            ITEM_TYPES.put(item, type);
        }
    }

    public static void registerSatellite(LegacySatelliteType type, Item item) {
        registerSatelliteItem(item, type);
    }

    public static void registerSatellite(int legacyId, Item item) {
        registerSatelliteItem(item, LegacySatelliteType.byLegacyId(legacyId));
    }

    public static Optional<LegacySatelliteType> getTypeFromItem(Item item) {
        if (item instanceof SatelliteChipItem chipItem && chipItem.satelliteType() != null) {
            return Optional.of(chipItem.satelliteType());
        }
        return Optional.ofNullable(ITEM_TYPES.get(item));
    }

    public static Optional<LegacySatelliteType> getTypeFromStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? Optional.empty() : getTypeFromItem(stack.getItem());
    }

    public static int getLegacyIdFromItem(Item item) {
        return getTypeFromItem(item)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getLegacyIdFromStack(ItemStack stack) {
        return getTypeFromStack(stack)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getIDFromItem(Item item) {
        return getLegacyIdFromItem(item);
    }

    public static Optional<String> getCargoPoolFromItem(Item item) {
        return getTypeFromItem(item).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromStack(ItemStack stack) {
        return getTypeFromStack(stack).flatMap(Satellite::cargoPoolForType);
    }

    @Nullable
    public static String getCargoForItem(Item item) {
        return getCargoPoolFromItem(item).orElse(null);
    }

    @Nullable
    public static String getCargoForStack(ItemStack stack) {
        return getCargoPoolFromStack(stack).orElse(null);
    }

    public static void registerCargo(LegacySatelliteType type, String cargoPool) {
        if (type == null || cargoPool == null || cargoPool.isBlank()) {
            return;
        }
        CARGO_POOLS.put(type, cargoPool);
    }

    public static void registerCargo(int legacyId, String cargoPool) {
        registerCargo(LegacySatelliteType.byLegacyId(legacyId), cargoPool);
    }

    public static Optional<String> cargoPoolForType(LegacySatelliteType type) {
        return Optional.ofNullable(type == null ? null : CARGO_POOLS.get(type));
    }

    @Nullable
    public static String getCargoForType(LegacySatelliteType type) {
        return cargoPoolForType(type).orElse(null);
    }

    public static List<LegacySatelliteType> satelliteTypesSnapshot() {
        return List.of(LegacySatelliteType.values());
    }

    public static Map<Item, LegacySatelliteType> itemTypesSnapshot() {
        return Map.copyOf(ITEM_TYPES);
    }

    public static Map<LegacySatelliteType, String> cargoPoolsSnapshot() {
        return Map.copyOf(CARGO_POOLS);
    }

    public abstract LegacySatelliteType type();

    public int legacyId() {
        return type().legacyId();
    }

    public int getID() {
        return legacyId();
    }

    public String legacyName() {
        return type().legacyName();
    }

    public String getName() {
        return legacyName();
    }

    public SatelliteInterface satelliteInterface() {
        return satelliteInterface;
    }

    public Set<InterfaceAction> interfaceActions() {
        return Set.copyOf(interfaceActions);
    }

    public Set<CoordAction> coordActions() {
        return Set.copyOf(coordActions);
    }

    public CompoundTag saveData() {
        CompoundTag tag = new CompoundTag();
        writeToNBT(tag);
        return tag;
    }

    public void save(CompoundTag tag) {
    }

    public void load(CompoundTag tag) {
    }

    public void writeToNBT(CompoundTag tag) {
        save(tag);
    }

    public void readFromNBT(CompoundTag tag) {
        load(tag);
    }

    public void onOrbit(ServerLevel level, double x, double y, double z) {
        // Achievements and gameplay side effects are restored with the concrete satellite systems.
    }

    public void onClick(ServerLevel level, int x, int z) {
    }

    public boolean tryClick(ServerLevel level, int x, int z) {
        return false;
    }

    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
    }

    public boolean tryCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        return false;
    }

    public Optional<String> cargoPool() {
        return cargoPoolForType(type());
    }

    public long lastOperationMillis() {
        return 0L;
    }

    public void setLastOperationMillis(long timeMillis) {
    }

    protected void playTeleportSound(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public enum InterfaceAction {
        HAS_MAP,
        CAN_CLICK,
        SHOW_COORDS,
        HAS_RADAR,
        HAS_ORES
    }

    public enum CoordAction {
        HAS_Y
    }

    public enum SatelliteInterface {
        NONE,
        SAT_PANEL,
        SAT_COORD
    }
}
