package com.hbm.ntm.satellite;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class Satellite {
    private static final Map<Item, LegacySatelliteType> ITEM_TYPES = new IdentityHashMap<>();
    private static final Map<Class<? extends Satellite>, LegacySatelliteType> CLASS_TYPES = new IdentityHashMap<>();
    private static final EnumMap<LegacySatelliteType, Class<? extends Satellite>> TYPE_CLASSES =
            new EnumMap<>(LegacySatelliteType.class);
    private static final EnumMap<LegacySatelliteType, String> CARGO_POOLS = new EnumMap<>(LegacySatelliteType.class);
    public static final List<Class<? extends Satellite>> satellites = new LegacySatelliteClassList();
    public static final Map<Item, Class<? extends Satellite>> itemToClass = new LegacySatelliteItemClassMap();

    protected final EnumSet<InterfaceAction> interfaceActions = EnumSet.noneOf(InterfaceAction.class);
    protected final EnumSet<CoordAction> coordActions = EnumSet.noneOf(CoordAction.class);
    protected SatelliteInterface satelliteInterface = SatelliteInterface.NONE;
    public final List<InterfaceActions> ifaceAcs = new LegacyInterfaceActionList();
    public final List<CoordActions> coordAcs = new LegacyCoordActionList();
    public Interfaces satIface = Interfaces.NONE;

    static {
        registerSatelliteClass(SatelliteMapper.class, LegacySatelliteType.MAPPER);
        registerSatelliteClass(SatelliteScanner.class, LegacySatelliteType.SCANNER);
        registerSatelliteClass(SatelliteRadar.class, LegacySatelliteType.RADAR);
        registerSatelliteClass(SatelliteLaser.class, LegacySatelliteType.LASER);
        registerSatelliteClass(SatelliteResonator.class, LegacySatelliteType.RESONATOR);
        registerSatelliteClass(SatelliteRelay.class, LegacySatelliteType.RELAY);
        registerSatelliteClass(SatelliteMiner.class, LegacySatelliteType.MINER);
        registerSatelliteClass(SatelliteLunarMiner.class, LegacySatelliteType.LUNAR_MINER);
        registerSatelliteClass(SatelliteHorizons.class, LegacySatelliteType.HORIZONS);
        for (LegacySatelliteType type : LegacySatelliteType.values()) {
            type.defaultCargoPool().ifPresent(cargo -> CARGO_POOLS.put(type, cargo));
        }
    }

    public static Satellite create(int legacyId) {
        LegacySatelliteType type = LegacySatelliteType.byLegacyId(legacyId);
        if (type == null) {
            return null;
        }
        return create(type);
    }

    public static Satellite create(LegacySatelliteType type) {
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

    public static Satellite create(Class<? extends Satellite> satelliteClass) {
        return getTypeFromClass(satelliteClass)
                .map(Satellite::create)
                .orElse(null);
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

    public static boolean orbit(ServerLevel level, Class<? extends Satellite> satelliteClass, int frequency,
                                double x, double y, double z) {
        return getTypeFromClass(satelliteClass)
                .map(type -> orbit(level, type, frequency, x, y, z))
                .orElse(false);
    }

    public static boolean orbit(ServerLevel level, Item item, int frequency, double x, double y, double z) {
        return orbit(level, getLegacyIdFromItem(item), frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, ItemStack stack, int frequency, double x, double y, double z) {
        return stack != null && !stack.isEmpty() && orbit(level, stack.getItem(), frequency, x, y, z);
    }

    public static void registerSatelliteItem(Item item, LegacySatelliteType type) {
        if (item != null && type != null && !ITEM_TYPES.containsKey(item) && !ITEM_TYPES.containsValue(type)) {
            ITEM_TYPES.put(item, type);
        }
    }

    public static void registerSatelliteClass(Class<? extends Satellite> satelliteClass, LegacySatelliteType type) {
        if (satelliteClass != null && type != null
                && !CLASS_TYPES.containsKey(satelliteClass)
                && !TYPE_CLASSES.containsKey(type)) {
            CLASS_TYPES.put(satelliteClass, type);
            TYPE_CLASSES.put(type, satelliteClass);
        }
    }

    public static void registerSatellite(LegacySatelliteType type, Item item) {
        registerSatelliteItem(item, type);
    }

    public static void registerSatellite(int legacyId, Item item) {
        registerSatelliteItem(item, LegacySatelliteType.byLegacyId(legacyId));
    }

    public static void registerSatellite(Class<? extends Satellite> satelliteClass, Item item) {
        getTypeFromClass(satelliteClass).ifPresent(type -> registerSatellite(type, item));
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

    public static Optional<LegacySatelliteType> getTypeFromClass(Class<? extends Satellite> satelliteClass) {
        return Optional.ofNullable(satelliteClass == null ? null : CLASS_TYPES.get(satelliteClass));
    }

    public static Optional<LegacySatelliteType> getTypeFromSatellite(Satellite satellite) {
        return Optional.ofNullable(satellite == null ? null : satellite.type());
    }

    public static Optional<Class<? extends Satellite>> getClassFromType(LegacySatelliteType type) {
        return Optional.ofNullable(type == null ? null : TYPE_CLASSES.get(type));
    }

    public static Optional<Class<? extends Satellite>> getClassFromLegacyId(int legacyId) {
        return getClassFromType(LegacySatelliteType.byLegacyId(legacyId));
    }

    public static Optional<Class<? extends Satellite>> getClassFromItem(Item item) {
        return getTypeFromItem(item).flatMap(Satellite::getClassFromType);
    }

    public static Optional<Class<? extends Satellite>> getClassFromStack(ItemStack stack) {
        return getTypeFromStack(stack).flatMap(Satellite::getClassFromType);
    }

    public static Optional<Class<? extends Satellite>> getClassFromSatellite(Satellite satellite) {
        return getTypeFromSatellite(satellite).flatMap(Satellite::getClassFromType);
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

    public static int getLegacyIdFromClass(Class<? extends Satellite> satelliteClass) {
        return getTypeFromClass(satelliteClass)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getLegacyIdFromSatellite(Satellite satellite) {
        return getTypeFromSatellite(satellite)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getIDFromItem(Item item) {
        return getLegacyIdFromItem(item);
    }

    public static int getIDFromClass(Class<? extends Satellite> satelliteClass) {
        return getLegacyIdFromClass(satelliteClass);
    }

    public static int getIDFromSatellite(Satellite satellite) {
        return getLegacyIdFromSatellite(satellite);
    }

    public static Optional<String> getCargoPoolFromItem(Item item) {
        return getTypeFromItem(item).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromStack(ItemStack stack) {
        return getTypeFromStack(stack).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromClass(Class<? extends Satellite> satelliteClass) {
        return getTypeFromClass(satelliteClass).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromSatellite(Satellite satellite) {
        return getTypeFromSatellite(satellite).flatMap(Satellite::cargoPoolForType);
    }

    @Nullable
    public static String getCargoForItem(Item item) {
        return getCargoPoolFromItem(item).orElse(null);
    }

    @Nullable
    public static String getCargoForStack(ItemStack stack) {
        return getCargoPoolFromStack(stack).orElse(null);
    }

    @Nullable
    public static String getCargoForClass(Class<? extends Satellite> satelliteClass) {
        return getCargoPoolFromClass(satelliteClass).orElse(null);
    }

    @Nullable
    public static String getCargoForSatellite(Satellite satellite) {
        return getCargoPoolFromSatellite(satellite).orElse(null);
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

    public static void registerCargo(Class<? extends Satellite> satelliteClass, String cargoPool) {
        getTypeFromClass(satelliteClass).ifPresent(type -> registerCargo(type, cargoPool));
    }

    public static Optional<String> cargoPoolForType(LegacySatelliteType type) {
        return Optional.ofNullable(type == null ? null : CARGO_POOLS.get(type));
    }

    public static boolean hasCargoPool(LegacySatelliteType type) {
        return cargoPoolForType(type).isPresent();
    }

    public static boolean hasCargoPool(Class<? extends Satellite> satelliteClass) {
        return getTypeFromClass(satelliteClass)
                .filter(Satellite::hasCargoPool)
                .isPresent();
    }

    public static boolean hasCargoPool(Satellite satellite) {
        return getTypeFromSatellite(satellite)
                .filter(Satellite::hasCargoPool)
                .isPresent();
    }

    @Nullable
    public static String getCargoForType(LegacySatelliteType type) {
        return cargoPoolForType(type).orElse(null);
    }

    public static List<LegacySatelliteType> satelliteTypesSnapshot() {
        return List.of(LegacySatelliteType.values());
    }

    public static Map<Class<? extends Satellite>, LegacySatelliteType> classTypesSnapshot() {
        return Map.copyOf(CLASS_TYPES);
    }

    public static Map<LegacySatelliteType, Class<? extends Satellite>> typeClassesSnapshot() {
        return Map.copyOf(TYPE_CLASSES);
    }

    public static List<Class<? extends Satellite>> satelliteClassesSnapshot() {
        return satelliteTypesSnapshot().stream()
                .map(TYPE_CLASSES::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public static Map<Item, LegacySatelliteType> itemTypesSnapshot() {
        return Map.copyOf(ITEM_TYPES);
    }

    public static Map<Item, Class<? extends Satellite>> itemClassesSnapshot() {
        return Map.copyOf(itemToClass);
    }

    public static Map<LegacySatelliteType, String> cargoPoolsSnapshot() {
        return Map.copyOf(CARGO_POOLS);
    }

    private static final class LegacySatelliteClassList extends AbstractList<Class<? extends Satellite>> {
        @Override
        public Class<? extends Satellite> get(int index) {
            LegacySatelliteType type = LegacySatelliteType.byLegacyId(index);
            if (type == null) {
                throw new IndexOutOfBoundsException(index);
            }
            Class<? extends Satellite> satelliteClass = TYPE_CLASSES.get(type);
            if (satelliteClass == null) {
                throw new IndexOutOfBoundsException(index);
            }
            return satelliteClass;
        }

        @Override
        public int size() {
            return (int) satelliteTypesSnapshot().stream()
                    .map(TYPE_CLASSES::get)
                    .filter(java.util.Objects::nonNull)
                    .count();
        }
    }

    private static final class LegacySatelliteItemClassMap extends AbstractMap<Item, Class<? extends Satellite>> {
        @Override
        public Class<? extends Satellite> get(Object key) {
            if (!(key instanceof Item item)) {
                return null;
            }
            return getClassFromItem(item).orElse(null);
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key) != null;
        }

        @Override
        public boolean containsValue(Object value) {
            return value instanceof Class<?> satelliteClass
                    && Satellite.class.isAssignableFrom(satelliteClass)
                    && CLASS_TYPES.containsKey(satelliteClass);
        }

        @Override
        public Set<Entry<Item, Class<? extends Satellite>>> entrySet() {
            return ITEM_TYPES.entrySet().stream()
                    .map(entry -> getClassFromType(entry.getValue())
                            .map(satelliteClass -> new AbstractMap.SimpleImmutableEntry<Item,
                                    Class<? extends Satellite>>(entry.getKey(), satelliteClass)))
                    .flatMap(Optional::stream)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
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
        return satIface == null ? SatelliteInterface.NONE : satIface.modern();
    }

    public Set<InterfaceAction> interfaceActions() {
        return Set.copyOf(interfaceActions);
    }

    public Set<CoordAction> coordActions() {
        return Set.copyOf(coordActions);
    }

    public boolean hasInterfaceAction(InterfaceAction action) {
        return action != null && interfaceActions.contains(action);
    }

    public boolean hasInterfaceAction(InterfaceActions action) {
        return action != null && hasInterfaceAction(action.modern());
    }

    public boolean hasCoordAction(CoordAction action) {
        return action != null && coordActions.contains(action);
    }

    public boolean hasCoordAction(CoordActions action) {
        return action != null && hasCoordAction(action.modern());
    }

    protected void setSatelliteInterface(SatelliteInterface satelliteInterface) {
        this.satelliteInterface = satelliteInterface == null ? SatelliteInterface.NONE : satelliteInterface;
        this.satIface = Interfaces.fromModern(this.satelliteInterface);
    }

    protected void setSatelliteInterface(Interfaces satelliteInterface) {
        this.satIface = satelliteInterface == null ? Interfaces.NONE : satelliteInterface;
        this.satelliteInterface = this.satIface.modern();
    }

    protected void addInterfaceAction(InterfaceAction action) {
        if (action != null) {
            interfaceActions.add(action);
        }
    }

    protected void addInterfaceAction(InterfaceActions action) {
        if (action != null) {
            addInterfaceAction(action.modern());
        }
    }

    protected void addCoordAction(CoordAction action) {
        if (action != null) {
            coordActions.add(action);
        }
    }

    protected void addCoordAction(CoordActions action) {
        if (action != null) {
            addCoordAction(action.modern());
        }
    }

    public Interfaces legacySatelliteInterface() {
        return Interfaces.fromModern(satelliteInterface());
    }

    private final class LegacyInterfaceActionList extends AbstractList<InterfaceActions> {
        @Override
        public InterfaceActions get(int index) {
            int current = 0;
            for (InterfaceActions action : InterfaceActions.values()) {
                if (interfaceActions.contains(action.modern())) {
                    if (current == index) {
                        return action;
                    }
                    current++;
                }
            }
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int size() {
            return interfaceActions.size();
        }

        @Override
        public void add(int index, InterfaceActions element) {
            addInterfaceAction(element);
        }

        @Override
        public InterfaceActions set(int index, InterfaceActions element) {
            InterfaceActions old = get(index);
            remove(index);
            addInterfaceAction(element);
            return old;
        }

        @Override
        public InterfaceActions remove(int index) {
            InterfaceActions old = get(index);
            interfaceActions.remove(old.modern());
            return old;
        }

        @Override
        public boolean remove(Object object) {
            return object instanceof InterfaceActions action && interfaceActions.remove(action.modern());
        }

        @Override
        public void clear() {
            interfaceActions.clear();
        }
    }

    private final class LegacyCoordActionList extends AbstractList<CoordActions> {
        @Override
        public CoordActions get(int index) {
            int current = 0;
            for (CoordActions action : CoordActions.values()) {
                if (coordActions.contains(action.modern())) {
                    if (current == index) {
                        return action;
                    }
                    current++;
                }
            }
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int size() {
            return coordActions.size();
        }

        @Override
        public void add(int index, CoordActions element) {
            addCoordAction(element);
        }

        @Override
        public CoordActions set(int index, CoordActions element) {
            CoordActions old = get(index);
            remove(index);
            addCoordAction(element);
            return old;
        }

        @Override
        public CoordActions remove(int index) {
            CoordActions old = get(index);
            coordActions.remove(old.modern());
            return old;
        }

        @Override
        public boolean remove(Object object) {
            return object instanceof CoordActions action && coordActions.remove(action.modern());
        }

        @Override
        public void clear() {
            coordActions.clear();
        }
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

    public enum InterfaceActions {
        HAS_MAP,
        CAN_CLICK,
        SHOW_COORDS,
        HAS_RADAR,
        HAS_ORES;

        private InterfaceAction modern() {
            return InterfaceAction.valueOf(name());
        }
    }

    public enum CoordActions {
        HAS_Y;

        private CoordAction modern() {
            return CoordAction.valueOf(name());
        }
    }

    public enum Interfaces {
        NONE,
        SAT_PANEL,
        SAT_COORD;

        private SatelliteInterface modern() {
            return SatelliteInterface.valueOf(name());
        }

        private static Interfaces fromModern(SatelliteInterface satelliteInterface) {
            return satelliteInterface == null ? NONE : Interfaces.valueOf(satelliteInterface.name());
        }
    }
}
