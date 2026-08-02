package com.hbm.ntm.satellite;

import net.minecraft.nbt.CompoundTag;
import com.hbm.util.fauxpointtwelve.NBTTagCompound;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

    private final List<InterfaceActions> legacyInterfaceActions = new ArrayList<>();
    private final List<CoordActions> legacyCoordActions = new ArrayList<>();
    protected final EnumSet<InterfaceAction> interfaceActions = EnumSet.noneOf(InterfaceAction.class);
    protected final EnumSet<CoordAction> coordActions = EnumSet.noneOf(CoordAction.class);
    protected SatelliteInterface satelliteInterface = SatelliteInterface.NONE;
    public List<InterfaceActions> ifaceAcs = new LegacyInterfaceActionList();
    public List<CoordActions> coordAcs = new LegacyCoordActionList();
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
        return com.hbm.saveddata.satellites.Satellite.create(legacyId);
    }

    public static Satellite create(LegacySatelliteType type) {
        return type == null ? null : create(type.legacyId());
    }

    public static Satellite create(Class<? extends Satellite> satelliteClass) {
        int legacyId = getLegacyIdFromClass(satelliteClass);
        return legacyId >= 0 ? create(legacyId) : null;
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
        SatelliteSavedData data = SatelliteSavedData.get(level);
        data.putSatelliteForOrbit(frequency, satellite);
        satellite.onOrbit(level, x, y, z);
        data.markDirty();
        return true;
    }

    public static boolean orbit(Level level, int legacyId, int frequency, double x, double y, double z) {
        return level instanceof ServerLevel serverLevel && orbit(serverLevel, legacyId, frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, LegacySatelliteType type, int frequency, double x, double y, double z) {
        return type != null && orbit(level, type.legacyId(), frequency, x, y, z);
    }

    public static boolean orbit(Level level, LegacySatelliteType type, int frequency, double x, double y, double z) {
        return type != null && orbit(level, type.legacyId(), frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, Class<? extends Satellite> satelliteClass, int frequency,
                                double x, double y, double z) {
        int legacyId = getLegacyIdFromClass(satelliteClass);
        return legacyId >= 0 && orbit(level, legacyId, frequency, x, y, z);
    }

    public static boolean orbit(Level level, Class<? extends Satellite> satelliteClass, int frequency,
                                double x, double y, double z) {
        int legacyId = getLegacyIdFromClass(satelliteClass);
        return legacyId >= 0 && orbit(level, legacyId, frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, Item item, int frequency, double x, double y, double z) {
        return orbit(level, getLegacyIdFromItem(item), frequency, x, y, z);
    }

    public static boolean orbit(Level level, Item item, int frequency, double x, double y, double z) {
        return orbit(level, getLegacyIdFromItem(item), frequency, x, y, z);
    }

    public static boolean orbit(ServerLevel level, ItemStack stack, int frequency, double x, double y, double z) {
        return stack != null && !stack.isEmpty() && orbit(level, getLegacyIdFromStack(stack), frequency, x, y, z);
    }

    public static boolean orbit(Level level, ItemStack stack, int frequency, double x, double y, double z) {
        return stack != null && !stack.isEmpty() && orbit(level, getLegacyIdFromStack(stack), frequency, x, y, z);
    }

    public static void registerSatelliteItem(Item item, LegacySatelliteType type) {
        if (item != null && type != null && !ITEM_TYPES.containsKey(item) && !ITEM_TYPES.containsValue(type)) {
            ITEM_TYPES.put(item, type);
        }
    }

    public static void registerSatelliteClass(Class<? extends Satellite> satelliteClass, LegacySatelliteType type) {
        if (satelliteClass != null && type != null && !CLASS_TYPES.containsKey(satelliteClass)) {
            CLASS_TYPES.put(satelliteClass, type);
            TYPE_CLASSES.putIfAbsent(type, satelliteClass);
        }
    }

    public static void registerSatellite(LegacySatelliteType type, Item item) {
        com.hbm.saveddata.satellites.Satellite.registerSatellite(type, item);
    }

    public static void registerSatellite(int legacyId, Item item) {
        registerSatellite(LegacySatelliteType.byLegacyId(legacyId), item);
    }

    public static void registerSatellite(Class<? extends Satellite> satelliteClass, Item item) {
        if (satelliteClass == null || com.hbm.saveddata.satellites.Satellite.class.isAssignableFrom(satelliteClass)) {
            com.hbm.saveddata.satellites.Satellite.registerSatellite(satelliteClass, item);
            return;
        }
        getTypeFromClass(satelliteClass).ifPresent(type -> registerSatelliteItem(item, type));
    }

    public static Optional<LegacySatelliteType> getTypeFromItem(Item item) {
        int legacyId = getLegacyIdFromLegacyItemMap(item);
        if (legacyId >= 0) {
            return Optional.ofNullable(LegacySatelliteType.byLegacyId(legacyId));
        }
        if (legacyItemClassMap().containsKey(item)) {
            return Optional.empty();
        }
        if (item instanceof SatelliteChipItem chipItem && chipItem.satelliteType() != null) {
            return Optional.of(chipItem.satelliteType());
        }
        return Optional.ofNullable(ITEM_TYPES.get(item));
    }

    public static Optional<LegacySatelliteType> getTypeFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        LegacySatelliteType xSatelliteType = com.hbm.saveddata.satellites.XSatelliteRegistry.typeFromItemStack(stack);
        return xSatelliteType != null ? Optional.of(xSatelliteType) : getTypeFromItem(stack.getItem());
    }

    public static Optional<LegacySatelliteType> getTypeFromClass(Class<? extends Satellite> satelliteClass) {
        if (isLegacySatelliteClass(satelliteClass)) {
            int legacyId = getLegacyIdFromLegacyClassList(satelliteClass);
            return legacyId >= 0 ? Optional.ofNullable(LegacySatelliteType.byLegacyId(legacyId)) : Optional.empty();
        }
        int legacyId = getLegacyIdFromLegacyClassList(satelliteClass);
        if (legacyId >= 0) {
            return Optional.ofNullable(LegacySatelliteType.byLegacyId(legacyId));
        }
        return Optional.ofNullable(satelliteClass == null ? null : CLASS_TYPES.get(satelliteClass));
    }

    public static Optional<LegacySatelliteType> getTypeFromSatellite(Satellite satellite) {
        if (satellite == null) {
            return Optional.empty();
        }
        Optional<LegacySatelliteType> listType = getTypeFromClass(satellite.getClass());
        if (listType.isEmpty() && isLegacySatelliteClass(satellite.getClass())) {
            return Optional.empty();
        }
        return listType.isPresent() ? listType : Optional.ofNullable(satellite.type());
    }

    public static Optional<Class<? extends Satellite>> getClassFromType(LegacySatelliteType type) {
        return Optional.ofNullable(type == null ? null : TYPE_CLASSES.get(type));
    }

    public static Optional<Class<? extends Satellite>> getClassFromLegacyId(int legacyId) {
        return getLegacyClassAt(legacyId);
    }

    public static Optional<Class<? extends Satellite>> getClassFromItem(Item item) {
        Optional<Class<? extends Satellite>> legacyClass = getLegacyClassFromItemMap(item);
        if (legacyClass.isPresent() || legacyItemClassMap().containsKey(item)) {
            return legacyClass;
        }
        return getTypeFromItem(item).flatMap(Satellite::getClassFromType);
    }

    public static Optional<Class<? extends Satellite>> getClassFromStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? Optional.empty() : getTypeFromStack(stack).flatMap(Satellite::getClassFromType);
    }

    public static Optional<Class<? extends Satellite>> getClassFromSatellite(Satellite satellite) {
        if (satellite == null) {
            return Optional.empty();
        }
        if (isLegacySatelliteClass(satellite.getClass())) {
            return getLegacyClassAt(getLegacyIdFromSatellite(satellite));
        }
        Optional<Class<? extends Satellite>> legacyClass = getLegacyClassAt(getLegacyIdFromSatellite(satellite));
        return legacyClass.isPresent() ? legacyClass : getTypeFromSatellite(satellite).flatMap(Satellite::getClassFromType);
    }

    public static int getLegacyIdFromItem(Item item) {
        int legacyId = getLegacyIdFromLegacyItemMap(item);
        if (legacyId >= 0) {
            return legacyId;
        }
        if (legacyItemClassMap().containsKey(item)) {
            return -1;
        }
        return getTypeFromItem(item)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getLegacyIdFromStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? -1 : getTypeFromStack(stack)
                .map(LegacySatelliteType::legacyId)
                .orElseGet(() -> getLegacyIdFromItem(stack.getItem()));
    }

    public static int getLegacyIdFromClass(Class<? extends Satellite> satelliteClass) {
        if (isLegacySatelliteClass(satelliteClass)) {
            return getLegacyIdFromLegacyClassList(satelliteClass);
        }
        int legacyId = getLegacyIdFromLegacyClassList(satelliteClass);
        if (legacyId >= 0) {
            return legacyId;
        }
        return getTypeFromClass(satelliteClass)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getLegacyIdFromSatellite(Satellite satellite) {
        if (satellite == null) {
            return -1;
        }
        if (isLegacySatelliteClass(satellite.getClass())) {
            return getLegacyIdFromLegacyClassList(satellite.getClass());
        }
        int legacyId = getLegacyIdFromLegacyClassList(satellite.getClass());
        if (legacyId >= 0) {
            return legacyId;
        }
        return getTypeFromSatellite(satellite)
                .map(LegacySatelliteType::legacyId)
                .orElse(-1);
    }

    public static int getIDFromItem(Item item) {
        return getLegacyIdFromItem(item);
    }

    public static int getIDFromStack(ItemStack stack) {
        return getLegacyIdFromStack(stack);
    }

    public static int getIDFromClass(Class<? extends Satellite> satelliteClass) {
        return getLegacyIdFromClass(satelliteClass);
    }

    public static int getIDFromSatellite(Satellite satellite) {
        return getLegacyIdFromSatellite(satellite);
    }

    public static boolean matchesClass(@Nullable Satellite satellite,
                                       @Nullable Class<? extends Satellite> satelliteClass) {
        int legacyId = getLegacyIdFromClass(satelliteClass);
        return satellite != null && legacyId >= 0 && getLegacyIdFromSatellite(satellite) == legacyId;
    }

    public static Optional<String> getCargoPoolFromItem(Item item) {
        boolean legacyItemEntry = legacyItemClassMap().containsKey(item);
        String legacyCargo = com.hbm.saveddata.satellites.SatelliteMiner.getCargoForItem(item);
        if (legacyCargo != null) {
            return Optional.of(legacyCargo);
        }
        if (legacyItemEntry) {
            return Optional.empty();
        }
        return getTypeFromItem(item).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? Optional.empty()
                : getTypeFromStack(stack).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromClass(Class<? extends Satellite> satelliteClass) {
        String legacyCargo = com.hbm.saveddata.satellites.SatelliteMiner.getCargoForClass(satelliteClass);
        if (legacyCargo != null) {
            return Optional.of(legacyCargo);
        }
        if (isLegacySatelliteClass(satelliteClass)) {
            return Optional.empty();
        }
        return getTypeFromClass(satelliteClass).flatMap(Satellite::cargoPoolForType);
    }

    public static Optional<String> getCargoPoolFromSatellite(Satellite satellite) {
        return satellite == null ? Optional.empty() : satellite.cargoPool();
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
        if (type == null) {
            return;
        }
        CARGO_POOLS.put(type, cargoPool);
    }

    public static void registerCargo(int legacyId, String cargoPool) {
        registerCargo(LegacySatelliteType.byLegacyId(legacyId), cargoPool);
    }

    public static void registerCargoForClass(Class<? extends Satellite> satelliteClass, String cargoPool) {
        getTypeFromClass(satelliteClass).ifPresent(type -> registerCargo(type, cargoPool));
    }

    public static Optional<String> cargoPoolForType(LegacySatelliteType type) {
        return Optional.ofNullable(type == null ? null : CARGO_POOLS.get(type));
    }

    public static boolean hasCargoPool(LegacySatelliteType type) {
        return cargoPoolForType(type).isPresent();
    }

    public static boolean hasCargoPool(Class<? extends Satellite> satelliteClass) {
        return getCargoPoolFromClass(satelliteClass).isPresent();
    }

    public static boolean hasCargoPool(Satellite satellite) {
        return satellite != null && satellite.cargoPool().isPresent();
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
        return Collections.unmodifiableList(new ArrayList<>(satellites));
    }

    public static Map<Item, LegacySatelliteType> itemTypesSnapshot() {
        Map<Item, LegacySatelliteType> result = new IdentityHashMap<>(ITEM_TYPES);
        for (Map.Entry<Item, Class<? extends com.hbm.saveddata.satellites.Satellite>> entry :
                legacyItemClassMap().entrySet()) {
            LegacySatelliteType type = LegacySatelliteType.byLegacyId(
                    getLegacyIdFromLegacyClassList(entry.getValue()));
            if (type != null) {
                result.put(entry.getKey(), type);
            } else {
                result.remove(entry.getKey());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public static Map<Item, Class<? extends Satellite>> itemClassesSnapshot() {
        return Collections.unmodifiableMap(new HashMap<>(itemToClass));
    }

    public static Map<LegacySatelliteType, String> cargoPoolsSnapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(CARGO_POOLS));
    }

    private static final class LegacySatelliteClassList extends AbstractList<Class<? extends Satellite>> {
        private final List<Class<? extends com.hbm.saveddata.satellites.Satellite>> backing;

        private LegacySatelliteClassList() {
            this(null);
        }

        private LegacySatelliteClassList(
                @Nullable List<Class<? extends com.hbm.saveddata.satellites.Satellite>> backing) {
            this.backing = backing;
        }

        private List<Class<? extends com.hbm.saveddata.satellites.Satellite>> backing() {
            return backing == null ? legacySatelliteClasses() : backing;
        }

        @Override
        public Class<? extends Satellite> get(int index) {
            return widenLegacyClassOrNull(backing().get(index));
        }

        @Override
        public int size() {
            return backing().size();
        }

        @Override
        public void add(int index, Class<? extends Satellite> element) {
            backing().add(index, narrowLegacyClassOrNull(element));
        }

        @Override
        public Class<? extends Satellite> set(int index, Class<? extends Satellite> element) {
            return widenLegacyClassOrNull(backing().set(index, narrowLegacyClassOrNull(element)));
        }

        @Override
        public Class<? extends Satellite> remove(int index) {
            return widenLegacyClassOrNull(backing().remove(index));
        }

        @Override
        public void clear() {
            backing().clear();
        }

        @Override
        public ListIterator<Class<? extends Satellite>> listIterator(int index) {
            ListIterator<Class<? extends com.hbm.saveddata.satellites.Satellite>> iterator =
                    backing().listIterator(index);
            return new ListIterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Class<? extends Satellite> next() {
                    return widenLegacyClassOrNull(iterator.next());
                }

                @Override
                public boolean hasPrevious() {
                    return iterator.hasPrevious();
                }

                @Override
                public Class<? extends Satellite> previous() {
                    return widenLegacyClassOrNull(iterator.previous());
                }

                @Override
                public int nextIndex() {
                    return iterator.nextIndex();
                }

                @Override
                public int previousIndex() {
                    return iterator.previousIndex();
                }

                @Override
                public void remove() {
                    iterator.remove();
                }

                @Override
                public void set(Class<? extends Satellite> element) {
                    iterator.set(narrowLegacyClassOrNull(element));
                }

                @Override
                public void add(Class<? extends Satellite> element) {
                    iterator.add(narrowLegacyClassOrNull(element));
                }
            };
        }

        @Override
        public List<Class<? extends Satellite>> subList(int fromIndex, int toIndex) {
            return new LegacySatelliteClassList(backing().subList(fromIndex, toIndex));
        }
    }

    private static final class LegacySatelliteItemClassMap extends AbstractMap<Item, Class<? extends Satellite>> {
        @Override
        public Class<? extends Satellite> get(Object key) {
            Class<? extends com.hbm.saveddata.satellites.Satellite> legacyClass =
                    legacyItemClassMap().get(key);
            if (legacyItemClassMap().containsKey(key)) {
                return widenLegacyClassOrNull(legacyClass);
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            return legacyItemClassMap().containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            if (value == null) {
                return legacyItemClassMap().containsValue(null);
            }
            if (!(value instanceof Class<?> satelliteClass) || !Satellite.class.isAssignableFrom(satelliteClass)) {
                return false;
            }
            if (com.hbm.saveddata.satellites.Satellite.class.isAssignableFrom(satelliteClass)) {
                return legacyItemClassMap().containsValue(
                        satelliteClass.asSubclass(com.hbm.saveddata.satellites.Satellite.class));
            }
            return false;
        }

        @Override
        public Class<? extends Satellite> put(Item key, Class<? extends Satellite> value) {
            return widenLegacyClassOrNull(legacyItemClassMap().put(key, narrowLegacyClassOrNull(value)));
        }

        @Override
        public Class<? extends Satellite> remove(Object key) {
            return widenLegacyClassOrNull(legacyItemClassMap().remove(key));
        }

        @Override
        public void clear() {
            legacyItemClassMap().clear();
        }

        @Override
        public Set<Entry<Item, Class<? extends Satellite>>> entrySet() {
            return new LegacySatelliteItemClassEntrySet();
        }
    }

    private static final class LegacySatelliteItemClassEntrySet
            extends AbstractSet<Map.Entry<Item, Class<? extends Satellite>>> {
        @Override
        public Iterator<Map.Entry<Item, Class<? extends Satellite>>> iterator() {
            Iterator<Map.Entry<Item, Class<? extends com.hbm.saveddata.satellites.Satellite>>> iterator =
                    legacyItemClassMap().entrySet().iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Map.Entry<Item, Class<? extends Satellite>> next() {
                    return new LegacySatelliteItemClassEntry(iterator.next());
                }

                @Override
                public void remove() {
                    iterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return legacyItemClassMap().size();
        }

        @Override
        public void clear() {
            legacyItemClassMap().clear();
        }

        @Override
        public boolean contains(Object object) {
            if (!(object instanceof Map.Entry<?, ?> entry) || !legacyItemClassMap().containsKey(entry.getKey())) {
                return false;
            }
            Class<? extends com.hbm.saveddata.satellites.Satellite> value =
                    legacyItemClassMap().get(entry.getKey());
            return java.util.Objects.equals(widenLegacyClassOrNull(value), entry.getValue());
        }

        @Override
        public boolean remove(Object object) {
            if (!contains(object)) {
                return false;
            }
            legacyItemClassMap().remove(((Map.Entry<?, ?>) object).getKey());
            return true;
        }
    }

    private static final class LegacySatelliteItemClassEntry
            implements Map.Entry<Item, Class<? extends Satellite>> {
        private final Map.Entry<Item, Class<? extends com.hbm.saveddata.satellites.Satellite>> backing;

        private LegacySatelliteItemClassEntry(
                Map.Entry<Item, Class<? extends com.hbm.saveddata.satellites.Satellite>> backing) {
            this.backing = backing;
        }

        @Override
        public Item getKey() {
            return backing.getKey();
        }

        @Override
        public Class<? extends Satellite> getValue() {
            return widenLegacyClassOrNull(backing.getValue());
        }

        @Override
        public Class<? extends Satellite> setValue(Class<? extends Satellite> value) {
            return widenLegacyClassOrNull(backing.setValue(narrowLegacyClassOrNull(value)));
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof Map.Entry<?, ?> entry)) {
                return false;
            }
            return java.util.Objects.equals(getKey(), entry.getKey())
                    && java.util.Objects.equals(getValue(), entry.getValue());
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hashCode(getKey()) ^ java.util.Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private static List<Class<? extends com.hbm.saveddata.satellites.Satellite>> legacySatelliteClasses() {
        return com.hbm.saveddata.satellites.Satellite.satellites;
    }

    private static Map<Item, Class<? extends com.hbm.saveddata.satellites.Satellite>> legacyItemClassMap() {
        return com.hbm.saveddata.satellites.Satellite.itemToClass;
    }

    private static boolean isLegacySatelliteClass(@Nullable Class<?> satelliteClass) {
        return satelliteClass != null
                && com.hbm.saveddata.satellites.Satellite.class.isAssignableFrom(satelliteClass);
    }

    private static Optional<Class<? extends Satellite>> getLegacyClassAt(int legacyId) {
        if (legacyId < 0 || legacyId >= legacySatelliteClasses().size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(widenLegacyClassOrNull(legacySatelliteClasses().get(legacyId)));
    }

    private static Optional<Class<? extends Satellite>> getLegacyClassFromItemMap(Item item) {
        Class<? extends com.hbm.saveddata.satellites.Satellite> satelliteClass =
                legacyItemClassMap().get(item);
        return satelliteClass == null ? Optional.empty() : Optional.of(widenLegacyClass(satelliteClass));
    }

    private static int getLegacyIdFromLegacyItemMap(Item item) {
        Class<? extends com.hbm.saveddata.satellites.Satellite> satelliteClass =
                legacyItemClassMap().get(item);
        return legacySatelliteClasses().indexOf(satelliteClass);
    }

    private static int getLegacyIdFromLegacyClassList(Class<? extends Satellite> satelliteClass) {
        if (satelliteClass == null
                || !com.hbm.saveddata.satellites.Satellite.class.isAssignableFrom(satelliteClass)) {
            return -1;
        }
        return legacySatelliteClasses().indexOf(
                satelliteClass.asSubclass(com.hbm.saveddata.satellites.Satellite.class));
    }

    private static Class<? extends Satellite> widenLegacyClass(
            Class<? extends com.hbm.saveddata.satellites.Satellite> satelliteClass) {
        return satelliteClass.asSubclass(Satellite.class);
    }

    @Nullable
    private static Class<? extends Satellite> widenLegacyClassOrNull(
            @Nullable Class<? extends com.hbm.saveddata.satellites.Satellite> satelliteClass) {
        return satelliteClass == null ? null : widenLegacyClass(satelliteClass);
    }

    private static Class<? extends com.hbm.saveddata.satellites.Satellite> narrowLegacyClass(
            Class<? extends Satellite> satelliteClass) {
        if (satelliteClass == null) {
            throw new NullPointerException("satelliteClass");
        }
        return satelliteClass.asSubclass(com.hbm.saveddata.satellites.Satellite.class);
    }

    @Nullable
    private static Class<? extends com.hbm.saveddata.satellites.Satellite> narrowLegacyClassOrNull(
            @Nullable Class<? extends Satellite> satelliteClass) {
        return satelliteClass == null ? null : narrowLegacyClass(satelliteClass);
    }

    /**
     * The 1.7.10 base class did not require a predefined enum identity: callers
     * could register another {@code Satellite} subclass in the public class/item
     * tables.  Built-in satellites override this modern lookup aid; an external
     * legacy-style subclass deliberately remains untyped rather than being
     * forced into one of the nine built-in IDs.
     */
    @Nullable
    public LegacySatelliteType type() {
        return null;
    }

    public int legacyId() {
        return getLegacyIdFromSatellite(this);
    }

    public int getID() {
        return legacyId();
    }

    public String legacyName() {
        LegacySatelliteType type = type();
        return type == null ? getClass().getSimpleName() : type.legacyName();
    }

    public String getName() {
        return legacyName();
    }

    public SatelliteInterface satelliteInterface() {
        return satIface == null ? SatelliteInterface.NONE : satIface.modern();
    }

    public Set<InterfaceAction> interfaceActions() {
        return Set.copyOf(currentInterfaceActionSet());
    }

    public Set<CoordAction> coordActions() {
        return Set.copyOf(currentCoordActionSet());
    }

    public boolean hasInterfaceAction(InterfaceAction action) {
        return action != null && currentInterfaceActionSet().contains(action);
    }

    public boolean hasInterfaceAction(InterfaceActions action) {
        return action != null && hasInterfaceAction(action.modern());
    }

    public boolean hasCoordAction(CoordAction action) {
        return action != null && currentCoordActionSet().contains(action);
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
        addInterfaceAction(InterfaceActions.fromModern(action));
    }

    protected void addInterfaceAction(InterfaceActions action) {
        if (action != null) {
            ifaceAcs.add(action);
        }
    }

    protected void addCoordAction(CoordAction action) {
        addCoordAction(CoordActions.fromModern(action));
    }

    protected void addCoordAction(CoordActions action) {
        if (action != null) {
            coordAcs.add(action);
        }
    }

    public Interfaces legacySatelliteInterface() {
        return Interfaces.fromModern(satelliteInterface());
    }

    private void rebuildInterfaceActionSet() {
        interfaceActions.clear();
        for (InterfaceActions action : legacyInterfaceActions) {
            if (action != null) {
                interfaceActions.add(action.modern());
            }
        }
    }

    private void rebuildCoordActionSet() {
        coordActions.clear();
        for (CoordActions action : legacyCoordActions) {
            if (action != null) {
                coordActions.add(action.modern());
            }
        }
    }

    private EnumSet<InterfaceAction> currentInterfaceActionSet() {
        EnumSet<InterfaceAction> actions = EnumSet.noneOf(InterfaceAction.class);
        for (InterfaceActions action : ifaceAcs) {
            if (action != null) {
                actions.add(action.modern());
            }
        }
        return actions;
    }

    private EnumSet<CoordAction> currentCoordActionSet() {
        EnumSet<CoordAction> actions = EnumSet.noneOf(CoordAction.class);
        for (CoordActions action : coordAcs) {
            if (action != null) {
                actions.add(action.modern());
            }
        }
        return actions;
    }

    private final class LegacyInterfaceActionList extends AbstractList<InterfaceActions> {
        @Override
        public InterfaceActions get(int index) {
            return legacyInterfaceActions.get(index);
        }

        @Override
        public int size() {
            return legacyInterfaceActions.size();
        }

        @Override
        public void add(int index, InterfaceActions element) {
            legacyInterfaceActions.add(index, element);
            rebuildInterfaceActionSet();
        }

        @Override
        public InterfaceActions set(int index, InterfaceActions element) {
            InterfaceActions old = legacyInterfaceActions.set(index, element);
            rebuildInterfaceActionSet();
            return old;
        }

        @Override
        public InterfaceActions remove(int index) {
            InterfaceActions old = legacyInterfaceActions.remove(index);
            rebuildInterfaceActionSet();
            return old;
        }

        @Override
        public boolean remove(Object object) {
            boolean removed = legacyInterfaceActions.remove(object);
            if (removed) {
                rebuildInterfaceActionSet();
            }
            return removed;
        }

        @Override
        public void clear() {
            legacyInterfaceActions.clear();
            interfaceActions.clear();
        }
    }

    private final class LegacyCoordActionList extends AbstractList<CoordActions> {
        @Override
        public CoordActions get(int index) {
            return legacyCoordActions.get(index);
        }

        @Override
        public int size() {
            return legacyCoordActions.size();
        }

        @Override
        public void add(int index, CoordActions element) {
            legacyCoordActions.add(index, element);
            rebuildCoordActionSet();
        }

        @Override
        public CoordActions set(int index, CoordActions element) {
            CoordActions old = legacyCoordActions.set(index, element);
            rebuildCoordActionSet();
            return old;
        }

        @Override
        public CoordActions remove(int index) {
            CoordActions old = legacyCoordActions.remove(index);
            rebuildCoordActionSet();
            return old;
        }

        @Override
        public boolean remove(Object object) {
            boolean removed = legacyCoordActions.remove(object);
            if (removed) {
                rebuildCoordActionSet();
            }
            return removed;
        }

        @Override
        public void clear() {
            legacyCoordActions.clear();
            coordActions.clear();
        }
    }

    public CompoundTag saveData() {
        CompoundTag tag = new CompoundTag();
        writeToNBT(tag);
        return tag;
    }

    public NBTTagCompound saveLegacyData() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return tag;
    }

    public void save(CompoundTag tag) {
    }

    public void save(NBTTagCompound tag) {
        save((CompoundTag) tag);
    }

    public void load(CompoundTag tag) {
    }

    public void load(NBTTagCompound tag) {
        load((CompoundTag) tag);
    }

    public void writeToNBT(CompoundTag tag) {
        save(tag);
    }

    public void writeToNBT(NBTTagCompound tag) {
        save(tag);
    }

    public void readFromNBT(CompoundTag tag) {
        load(tag);
    }

    public void readFromNBT(NBTTagCompound tag) {
        load(tag);
    }

    public void onOrbit(ServerLevel level, double x, double y, double z) {
        onOrbit((Level) level, x, y, z);
    }

    public void onOrbit(Level level, double x, double y, double z) {
        // Achievements and gameplay side effects are restored with the concrete satellite systems.
    }

    public void onClick(ServerLevel level, int x, int z) {
    }

    public void onClick(Level level, int x, int z) {
        if (level instanceof ServerLevel serverLevel) {
            onClick(serverLevel, x, z);
        }
    }

    public boolean tryClick(ServerLevel level, int x, int z) {
        return false;
    }

    public boolean tryClick(Level level, int x, int z) {
        return level instanceof ServerLevel serverLevel && tryClick(serverLevel, x, z);
    }

    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
    }

    public void onCoordAction(Level level, Player player, int x, int y, int z) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            onCoordAction(serverLevel, serverPlayer, x, y, z);
        }
    }

    public boolean tryCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        return false;
    }

    public boolean tryCoordAction(Level level, Player player, int x, int y, int z) {
        return level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer
                && tryCoordAction(serverLevel, serverPlayer, x, y, z);
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

        private static InterfaceActions fromModern(InterfaceAction action) {
            return action == null ? null : InterfaceActions.valueOf(action.name());
        }
    }

    public enum CoordActions {
        HAS_Y;

        private CoordAction modern() {
            return CoordAction.valueOf(name());
        }

        private static CoordActions fromModern(CoordAction action) {
            return action == null ? null : CoordActions.valueOf(action.name());
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
