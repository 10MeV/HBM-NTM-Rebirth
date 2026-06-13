package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AnnihilatorSavedData extends SavedData {
    public static final String DATA_NAME = "annihilator";
    public static final String KEY = DATA_NAME;
    private static final String TAG_POOLS = "pools";
    private static final String TAG_POOL_NAME = "poolname";
    private static final String TAG_POOL = "pool";
    private static final String TAG_KEY = "key";
    private static final String TAG_ITEM = "item";
    private static final String TAG_META = "meta";
    private static final String TAG_FLUID = "fluid";
    private static final String TAG_DICT = "dict";
    private static final String TAG_AMOUNT = "amount";

    public final HashMap<String, AnnihilatorPool> pools = new DirtyTrackingPoolMap();
    private LoadDiagnostics loadDiagnostics = LoadDiagnostics.empty();
    private List<PoolLoadDiagnostics> poolLoadDiagnostics = List.of();

    public AnnihilatorSavedData() {
        setDirty();
    }

    public AnnihilatorSavedData(String name) {
        this();
    }

    public static AnnihilatorSavedData load(CompoundTag tag) {
        AnnihilatorSavedData data = new AnnihilatorSavedData();
        data.pools.clear();
        boolean hasPoolsTag = tag.contains(TAG_POOLS, Tag.TAG_LIST);
        ListTag pools = tag.getList(TAG_POOLS, Tag.TAG_COMPOUND);
        int poolsLoaded = 0;
        int missingPoolNames = 0;
        int missingPoolTags = 0;
        int duplicatePoolNames = 0;
        int entriesRead = 0;
        int entriesLoaded = 0;
        int invalidKeys = 0;
        int invalidAmounts = 0;
        int duplicateKeys = 0;
        Set<String> seenPoolNames = new HashSet<>();
        List<PoolLoadDiagnostics> poolDiagnostics = new ArrayList<>();
        for (int i = 0; i < pools.size(); i++) {
            CompoundTag poolTag = pools.getCompound(i);
            boolean hasPoolName = poolTag.contains(TAG_POOL_NAME, Tag.TAG_STRING)
                    && !poolTag.getString(TAG_POOL_NAME).isBlank();
            boolean hasPoolList = poolTag.contains(TAG_POOL, Tag.TAG_LIST);
            if (!hasPoolName) {
                missingPoolNames++;
            }
            if (!hasPoolList) {
                missingPoolTags++;
            }
            String poolName = poolTag.getString(TAG_POOL_NAME);
            boolean duplicatePoolName = !seenPoolNames.add(poolName);
            if (duplicatePoolName) {
                duplicatePoolNames++;
            }
            AnnihilatorPool pool = data.createPool();
            PoolEntryLoadDiagnostics entryDiagnostics = pool.deserializeWithDiagnostics(
                    poolTag.getList(TAG_POOL, Tag.TAG_COMPOUND));
            entriesRead += entryDiagnostics.entriesRead();
            entriesLoaded += entryDiagnostics.entriesLoaded();
            invalidKeys += entryDiagnostics.invalidKeys();
            invalidAmounts += entryDiagnostics.invalidAmounts();
            duplicateKeys += entryDiagnostics.duplicateKeys();
            poolDiagnostics.add(new PoolLoadDiagnostics(i, poolName, hasPoolName, hasPoolList, duplicatePoolName,
                    entryDiagnostics.entriesRead(), entryDiagnostics.entriesLoaded(),
                    entryDiagnostics.invalidKeys(), entryDiagnostics.invalidAmounts(),
                    entryDiagnostics.duplicateKeys()));
            data.pools.put(poolName, pool);
            poolsLoaded++;
        }
        data.loadDiagnostics = new LoadDiagnostics(hasPoolsTag, pools.size(), poolsLoaded, missingPoolNames,
                missingPoolTags, duplicatePoolNames, entriesRead, entriesLoaded, invalidKeys, invalidAmounts,
                duplicateKeys);
        data.poolLoadDiagnostics = List.copyOf(poolDiagnostics);
        data.setDirty(false);
        return data;
    }

    public static AnnihilatorSavedData forLevel(ServerLevel level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, AnnihilatorSavedData::load, AnnihilatorSavedData::new);
    }

    public static Optional<AnnihilatorSavedData> forLevel(Level level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, AnnihilatorSavedData::load, AnnihilatorSavedData::new);
    }

    public static AnnihilatorSavedData forWorld(ServerLevel level) {
        return forLevel(level);
    }

    public static Optional<AnnihilatorSavedData> forWorld(Level level) {
        return forLevel(level);
    }

    public static AnnihilatorSavedData forWorld(MinecraftServer server) {
        return getData(server);
    }

    public static Optional<AnnihilatorSavedData> forWorld(MinecraftServer server, ResourceKey<Level> dimension) {
        return getData(server, dimension);
    }

    public static Optional<AnnihilatorSavedData> getExisting(ServerLevel level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, AnnihilatorSavedData::load);
    }

    public static Optional<AnnihilatorSavedData> getExisting(MinecraftServer server) {
        return WorldSavedDataHelper.getExisting(server, DATA_NAME, AnnihilatorSavedData::load);
    }

    public static Optional<AnnihilatorSavedData> getExisting(MinecraftServer server, ResourceKey<Level> dimension) {
        return WorldSavedDataHelper.getExisting(server, dimension, DATA_NAME, AnnihilatorSavedData::load);
    }

    public static Optional<AnnihilatorSavedData> getExisting(Level level) {
        return WorldSavedDataHelper.getExisting(level, DATA_NAME, AnnihilatorSavedData::load);
    }

    public static AnnihilatorSavedData getData(ServerLevel level) {
        return forLevel(level);
    }

    public static Optional<AnnihilatorSavedData> getData(Level level) {
        return forLevel(level);
    }

    public static AnnihilatorSavedData getData(MinecraftServer server) {
        return WorldSavedDataHelper.get(server, DATA_NAME, AnnihilatorSavedData::load, AnnihilatorSavedData::new);
    }

    public static Optional<AnnihilatorSavedData> getData(MinecraftServer server, ResourceKey<Level> dimension) {
        return WorldSavedDataHelper.get(server, dimension, DATA_NAME, AnnihilatorSavedData::load,
                AnnihilatorSavedData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag poolsTag = new ListTag();
        for (Map.Entry<String, AnnihilatorPool> entry : pools.entrySet()) {
            CompoundTag poolTag = new CompoundTag();
            poolTag.putString(TAG_POOL_NAME, entry.getKey());
            poolTag.put(TAG_POOL, entry.getValue().serialize());
            poolsTag.add(poolTag);
        }
        tag.put(TAG_POOLS, poolsTag);
        return tag;
    }

    public void readFromNBT(CompoundTag tag) {
        AnnihilatorSavedData loaded = load(tag == null ? new CompoundTag() : tag);
        pools.clear();
        loaded.pools.forEach((name, pool) -> pools.put(name, pool.copy(this::setDirty)));
        loadDiagnostics = loaded.loadDiagnostics;
        poolLoadDiagnostics = loaded.poolLoadDiagnostics;
        setDirty(false);
    }

    public void writeToNBT(CompoundTag tag) {
        save(tag);
    }

    public AnnihilatorPool grabPool(String pool) {
        return pools.computeIfAbsent(Objects.requireNonNull(pool, "pool"), ignored -> createPool());
    }

    public Optional<AnnihilatorPool> getPool(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? Optional.empty() : Optional.of(poolInstance.copy());
    }

    public IncrementResult increment(String pool, PoolKey key, long amount) {
        IncrementResult result = grabPool(pool).increment(key, amount);
        setDirty();
        return result;
    }

    public Optional<IncrementResult> incrementLegacy(String pool, Object legacyKey, long amount) {
        Optional<PoolKey> key = PoolKey.fromLegacyObject(legacyKey);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(increment(pool, key.get(), amount));
    }

    public IncrementResult incrementLegacyItem(String pool, Item item, int legacyMeta, long amount) {
        return increment(pool, PoolKey.legacyItem(item, legacyMeta), amount);
    }

    public IncrementResult incrementLegacyItem(String pool, ResourceLocation item, int legacyMeta, long amount) {
        return increment(pool, PoolKey.legacyItem(item, legacyMeta), amount);
    }

    public ItemStack pushToPool(String pool, FluidType type, long amount, boolean alwaysPayOut) {
        pushToPoolResult(pool, type, amount, alwaysPayOut);
        return null;
    }

    public PoolPushResult pushToPoolResult(String pool, FluidType type, long amount, boolean alwaysPayOut) {
        if (type == null || amount <= 0L) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.fluid(type), amount);
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
    }

    public ItemStack pushToPool(String pool, ItemStack stack, boolean alwaysPayOut) {
        pushToPoolResult(pool, stack, alwaysPayOut);
        return null;
    }

    public PoolPushResult pushToPoolResult(String pool, ItemStack stack, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.item(stack.getItem()), stack.getCount());
        IncrementResult metaResult = increment(pool, PoolKey.itemMeta(stack, stack.getDamageValue()), stack.getCount());
        return new PoolPushResult(metaResult, false, "payout_deferred_ore_dict_deferred", alwaysPayOut);
    }

    public ItemPoolPushResult pushLegacyItemToPool(String pool, ItemStack stack, int legacyMeta,
                                                   Collection<String> legacyOreDictNames, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return ItemPoolPushResult.empty(alwaysPayOut);
        }

        List<IncrementResult> oreDictResults = new ArrayList<>();
        IncrementResult itemResult = increment(pool, PoolKey.item(stack.getItem()), stack.getCount());
        IncrementResult metaResult = legacyMeta < 0 ? null
                : increment(pool, PoolKey.itemMeta(stack, legacyMeta), stack.getCount());

        IncrementResult lastOreDictResult = null;
        Collection<String> oreDictNames = legacyOreDictNames == null ? List.of() : legacyOreDictNames;
        for (String name : oreDictNames) {
            if (name != null && !name.isBlank()) {
                lastOreDictResult = increment(pool, PoolKey.oreDict(name), stack.getCount());
                oreDictResults.add(lastOreDictResult);
            }
        }

        IncrementResult preferred = lastOreDictResult != null ? lastOreDictResult
                : metaResult != null ? metaResult : itemResult;
        return new ItemPoolPushResult(preferred, itemResult, Optional.ofNullable(metaResult), List.copyOf(oreDictResults),
                false, "payout_deferred", alwaysPayOut);
    }

    public PoolPushResult pushItemMetaToPool(String pool, ItemStack stack, int legacyMeta, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.itemMeta(HbmRegistryUtil.itemKey(stack.getItem()),
                legacyMeta), stack.getCount());
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
    }

    public PoolPushResult pushOreDictToPool(String pool, String oreDict, long amount, boolean alwaysPayOut) {
        if (oreDict == null || oreDict.isBlank() || amount <= 0L) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.oreDict(oreDict), amount);
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
    }

    public BigInteger getAmount(String pool, PoolKey key) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? BigInteger.ZERO : poolInstance.getAmount(key);
    }

    public BigInteger getLegacyAmount(String pool, Object legacyKey) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? BigInteger.ZERO : poolInstance.getLegacyAmount(legacyKey);
    }

    public BigInteger getLegacyItemAmount(String pool, Item item, int legacyMeta) {
        return item == null ? BigInteger.ZERO : getAmount(pool, PoolKey.legacyItem(item, legacyMeta));
    }

    public BigInteger getLegacyItemAmount(String pool, ResourceLocation item, int legacyMeta) {
        return item == null ? BigInteger.ZERO : getAmount(pool, PoolKey.legacyItem(item, legacyMeta));
    }

    public BigInteger getItemAmount(String pool, Item item) {
        return item == null ? BigInteger.ZERO : getAmount(pool, PoolKey.item(item));
    }

    public BigInteger getItemAmount(String pool, ItemStack stack) {
        return stack == null || stack.isEmpty() ? BigInteger.ZERO : getItemAmount(pool, stack.getItem());
    }

    public BigInteger getItemMetaAmount(String pool, ItemStack stack, int legacyMeta) {
        return stack == null || stack.isEmpty() ? BigInteger.ZERO
                : getAmount(pool, PoolKey.itemMeta(stack, legacyMeta));
    }

    public BigInteger getFluidAmount(String pool, FluidType type) {
        return type == null ? BigInteger.ZERO : getAmount(pool, PoolKey.fluid(type));
    }

    public BigInteger getOreDictAmount(String pool, String oreDict) {
        return oreDict == null || oreDict.isBlank() ? BigInteger.ZERO : getAmount(pool, PoolKey.oreDict(oreDict));
    }

    public BigInteger setAmount(String pool, PoolKey key, BigInteger amount) {
        return key == null ? BigInteger.ZERO : grabPool(pool).putAmount(key, amount);
    }

    public BigInteger setAmount(String pool, PoolKey key, long amount) {
        return setAmount(pool, key, BigInteger.valueOf(amount));
    }

    public Optional<BigInteger> setLegacyAmount(String pool, Object legacyKey, BigInteger amount) {
        return PoolKey.fromLegacyObject(legacyKey)
                .map(key -> setAmount(pool, key, amount));
    }

    public Optional<BigInteger> setLegacyAmount(String pool, Object legacyKey, long amount) {
        return setLegacyAmount(pool, legacyKey, BigInteger.valueOf(amount));
    }

    public BigInteger removeAmount(String pool, PoolKey key) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null || key == null ? BigInteger.ZERO : poolInstance.removeAmount(key);
    }

    public BigInteger removeLegacyAmount(String pool, Object legacyKey) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? BigInteger.ZERO : poolInstance.removeLegacyAmount(legacyKey);
    }

    public boolean containsAmount(String pool, PoolKey key) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance != null && poolInstance.containsAmount(key);
    }

    public boolean containsLegacyAmount(String pool, Object legacyKey) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance != null && poolInstance.containsLegacyAmount(legacyKey);
    }

    public boolean hasPool(String pool) {
        return pools.containsKey(pool);
    }

    public int poolEntryCount(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? 0 : poolInstance.size();
    }

    public BigInteger totalAmount(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? BigInteger.ZERO : poolInstance.totalAmount();
    }

    public Map<Kind, Integer> keyKindCounts(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? Map.of() : poolInstance.keyKindCounts();
    }

    public Map<Kind, BigInteger> keyKindTotals(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? Map.of() : poolInstance.keyKindTotals();
    }

    public Map<Kind, Integer> keyKindCounts() {
        EnumMap<Kind, Integer> counts = new EnumMap<>(Kind.class);
        for (AnnihilatorPool pool : pools.values()) {
            for (Map.Entry<Kind, Integer> entry : pool.keyKindCounts().entrySet()) {
                counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    public Map<Kind, BigInteger> keyKindTotals() {
        EnumMap<Kind, BigInteger> totals = new EnumMap<>(Kind.class);
        for (AnnihilatorPool pool : pools.values()) {
            for (Map.Entry<Kind, BigInteger> entry : pool.keyKindTotals().entrySet()) {
                totals.merge(entry.getKey(), entry.getValue(), BigInteger::add);
            }
        }
        return Map.copyOf(totals);
    }

    public List<Map.Entry<PoolKey, BigInteger>> poolEntriesSnapshot(String pool) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? List.of() : poolInstance.entriesSnapshot();
    }

    public List<Map.Entry<PoolKey, BigInteger>> topEntriesSnapshot(String pool, int limit) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? List.of() : poolInstance.topEntriesSnapshot(limit);
    }

    public List<Map.Entry<PoolKey, BigInteger>> entriesByKindSnapshot(String pool, Kind kind, int limit) {
        AnnihilatorPool poolInstance = pools.get(pool);
        return poolInstance == null ? List.of() : poolInstance.entriesByKindSnapshot(kind, limit);
    }

    public List<Map.Entry<String, AnnihilatorPool>> poolsSnapshot() {
        return pools.entrySet().stream()
                .<Map.Entry<String, AnnihilatorPool>>map(
                        entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue().copy()))
                .toList();
    }

    public List<String> poolNamesSnapshot() {
        return pools.keySet().stream().sorted().toList();
    }

    public List<PoolSummary> poolSummariesSnapshot() {
        return pools.entrySet().stream()
                .map(entry -> PoolSummary.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PoolSummary::name))
                .toList();
    }

    public List<PoolSummary> topPoolSummariesSnapshot(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return poolSummariesSnapshot().stream()
                .sorted(Comparator.comparing(PoolSummary::totalAmount).reversed()
                        .thenComparing(PoolSummary::name))
                .limit(limit)
                .toList();
    }

    public boolean removePool(String pool) {
        if (pools.remove(pool) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public int poolCount() {
        return pools.size();
    }

    public int poolEntryCount() {
        return pools.values().stream().mapToInt(AnnihilatorPool::size).sum();
    }

    public BigInteger totalAmount() {
        return pools.values().stream()
                .map(AnnihilatorPool::totalAmount)
                .reduce(BigInteger.ZERO, BigInteger::add);
    }

    public boolean isEmpty() {
        return pools.isEmpty();
    }

    public LoadDiagnostics loadDiagnostics() {
        return loadDiagnostics;
    }

    public List<PoolLoadDiagnostics> poolLoadDiagnosticsSnapshot() {
        return poolLoadDiagnostics;
    }

    public List<PoolLoadDiagnostics> problemPoolLoadDiagnosticsSnapshot() {
        return poolLoadDiagnostics.stream()
                .filter(diagnostics -> !diagnostics.clean())
                .toList();
    }

    public void markDirty() {
        setDirty();
    }

    private AnnihilatorPool createPool() {
        return new AnnihilatorPool(this::setDirty);
    }

    private AnnihilatorPool bindPool(AnnihilatorPool pool) {
        if (pool != null) {
            pool.bindDirtyCallback(this::setDirty);
        }
        return pool;
    }

    private final class DirtyTrackingPoolMap extends HashMap<String, AnnihilatorPool> {
        @Override
        public AnnihilatorPool put(String key, AnnihilatorPool value) {
            AnnihilatorPool previous = super.put(key, bindPool(value));
            if (previous != value) {
                setDirty();
            }
            return previous;
        }

        @Override
        public AnnihilatorPool remove(Object key) {
            AnnihilatorPool previous = super.remove(key);
            if (previous != null) {
                setDirty();
            }
            return previous;
        }

        @Override
        public void putAll(Map<? extends String, ? extends AnnihilatorPool> map) {
            if (!map.isEmpty()) {
                for (Map.Entry<? extends String, ? extends AnnihilatorPool> entry : map.entrySet()) {
                    super.put(entry.getKey(), bindPool(entry.getValue()));
                }
                setDirty();
            }
        }

        @Override
        public void clear() {
            if (!isEmpty()) {
                super.clear();
                setDirty();
            }
        }
    }

    public static class AnnihilatorPool {
        public final HashMap<Object, BigInteger> items;

        public AnnihilatorPool() {
            this(null);
        }

        private AnnihilatorPool(Runnable dirtyCallback) {
            this.items = new LegacyKeyPoolMap(dirtyCallback);
        }

        private void bindDirtyCallback(Runnable dirtyCallback) {
            if (items instanceof LegacyKeyPoolMap map) {
                map.dirtyCallback = dirtyCallback;
            }
        }

        public IncrementResult increment(PoolKey key, long amount) {
            Objects.requireNonNull(key, "key");
            BigInteger previous = items.getOrDefault(key, BigInteger.ZERO);
            BigInteger current = previous.add(BigInteger.valueOf(amount));
            items.put(key, current);
            return new IncrementResult(previous, current);
        }

        public Optional<IncrementResult> increment(Object legacyKey, long amount) {
            return PoolKey.fromLegacyObject(legacyKey)
                    .map(key -> increment(key, amount));
        }

        public ItemStack increment(Object legacyKey, long amount, boolean alwaysPayOut) {
            incrementResult(legacyKey, amount, alwaysPayOut);
            return null;
        }

        public Optional<IncrementResult> incrementResult(Object legacyKey, long amount, boolean alwaysPayOut) {
            return increment(legacyKey, amount);
        }

        public BigInteger getAmount(PoolKey key) {
            return items.getOrDefault(key, BigInteger.ZERO);
        }

        public BigInteger getLegacyAmount(Object legacyKey) {
            return PoolKey.fromLegacyObject(legacyKey)
                    .map(this::getAmount)
                    .orElse(BigInteger.ZERO);
        }

        public BigInteger putAmount(PoolKey key, BigInteger amount) {
            if (key == null || amount == null) {
                return BigInteger.ZERO;
            }
            BigInteger previous = items.put(key, amount);
            return previous == null ? BigInteger.ZERO : previous;
        }

        public Optional<BigInteger> putLegacyAmount(Object legacyKey, BigInteger amount) {
            if (amount == null) {
                return Optional.empty();
            }
            return PoolKey.fromLegacyObject(legacyKey)
                    .map(key -> putAmount(key, amount));
        }

        public BigInteger removeAmount(PoolKey key) {
            if (key == null) {
                return BigInteger.ZERO;
            }
            BigInteger previous = items.remove(key);
            return previous == null ? BigInteger.ZERO : previous;
        }

        public BigInteger removeLegacyAmount(Object legacyKey) {
            return PoolKey.fromLegacyObject(legacyKey)
                    .map(this::removeAmount)
                    .orElse(BigInteger.ZERO);
        }

        public boolean containsAmount(PoolKey key) {
            return key != null && items.containsKey(key);
        }

        public boolean containsLegacyAmount(Object legacyKey) {
            return PoolKey.fromLegacyObject(legacyKey)
                    .map(this::containsAmount)
                    .orElse(false);
        }

        public int size() {
            return items.size();
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        public BigInteger totalAmount() {
            return items.values().stream().reduce(BigInteger.ZERO, BigInteger::add);
        }

        public List<Map.Entry<PoolKey, BigInteger>> entriesSnapshot() {
            List<Map.Entry<PoolKey, BigInteger>> entries = new ArrayList<>();
            for (Map.Entry<Object, BigInteger> entry : items.entrySet()) {
                PoolKey.fromLegacyObject(entry.getKey())
                        .ifPresent(key -> entries.add(new AbstractMap.SimpleImmutableEntry<>(key, entry.getValue())));
            }
            return List.copyOf(entries);
        }

        public List<Map.Entry<Object, BigInteger>> legacyEntriesSnapshot() {
            List<Map.Entry<Object, BigInteger>> entries = new ArrayList<>();
            for (Map.Entry<Object, BigInteger> entry : items.entrySet()) {
                PoolKey.fromLegacyObject(entry.getKey())
                        .ifPresent(key -> entries.add(new AbstractMap.SimpleImmutableEntry<>(key, entry.getValue())));
            }
            return List.copyOf(entries);
        }

        public List<Map.Entry<PoolKey, BigInteger>> topEntriesSnapshot(int limit) {
            if (limit <= 0) {
                return List.of();
            }
            return entriesSnapshot().stream()
                    .sorted(Map.Entry.<PoolKey, BigInteger>comparingByValue(Comparator.reverseOrder()))
                    .limit(limit)
                    .toList();
        }

        public List<Map.Entry<PoolKey, BigInteger>> entriesByKindSnapshot(Kind kind, int limit) {
            if (kind == null || kind == Kind.UNKNOWN || limit <= 0) {
                return List.of();
            }
            return entriesSnapshot().stream()
                    .filter(entry -> entry.getKey().kind() == kind)
                    .sorted(Map.Entry.<PoolKey, BigInteger>comparingByValue(Comparator.reverseOrder()))
                    .limit(limit)
                    .toList();
        }

        public Map<Kind, Integer> keyKindCounts() {
            EnumMap<Kind, Integer> counts = new EnumMap<>(Kind.class);
            for (Object key : items.keySet()) {
                PoolKey.fromLegacyObject(key).ifPresent(poolKey -> counts.merge(poolKey.kind(), 1, Integer::sum));
            }
            return Map.copyOf(counts);
        }

        public Map<Kind, BigInteger> keyKindTotals() {
            EnumMap<Kind, BigInteger> totals = new EnumMap<>(Kind.class);
            for (Map.Entry<Object, BigInteger> entry : items.entrySet()) {
                PoolKey.fromLegacyObject(entry.getKey())
                        .ifPresent(key -> totals.merge(key.kind(), entry.getValue(), BigInteger::add));
            }
            return Map.copyOf(totals);
        }

        public ListTag serialize() {
            ListTag list = new ListTag();
            serialize(list);
            return list;
        }

        public void serialize(ListTag list) {
            Objects.requireNonNull(list, "list");
            for (Map.Entry<Object, BigInteger> entry : items.entrySet()) {
                Optional<PoolKey> key = PoolKey.fromLegacyObject(entry.getKey());
                if (key.isEmpty()) {
                    continue;
                }
                CompoundTag tag = new CompoundTag();
                serializeKey(tag, key.get());
                tag.putByteArray(TAG_AMOUNT, entry.getValue().toByteArray());
                list.add(tag);
            }
        }

        public void deserialize(ListTag list) {
            deserializeWithDiagnostics(list);
        }

        public PoolEntryLoadDiagnostics deserializeWithDiagnostics(ListTag list) {
            Objects.requireNonNull(list, "list");
            int entriesLoaded = 0;
            int invalidKeys = 0;
            int invalidAmounts = 0;
            int duplicateKeys = 0;
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                Optional<PoolKey> key = deserializePoolKey(tag);
                if (key.isEmpty()) {
                    invalidKeys++;
                    continue;
                }
                Optional<BigInteger> amount = readAmount(tag);
                if (amount.isEmpty()) {
                    invalidAmounts++;
                    continue;
                }
                if (items.containsKey(key.get())) {
                    duplicateKeys++;
                }
                items.put(key.get(), amount.get());
                entriesLoaded++;
            }
            return new PoolEntryLoadDiagnostics(list.size(), entriesLoaded, invalidKeys, invalidAmounts,
                    duplicateKeys);
        }

        public void serializeKey(CompoundTag tag, Object legacyKey) {
            Objects.requireNonNull(tag, "tag");
            PoolKey.fromLegacyObject(legacyKey).ifPresent(key -> key.save(tag));
        }

        public Object deserializeKey(CompoundTag tag) {
            return deserializePoolKey(tag).orElse(null);
        }

        public Optional<PoolKey> deserializePoolKey(CompoundTag tag) {
            return tag == null ? Optional.empty() : PoolKey.load(tag);
        }

        private AnnihilatorPool copy() {
            return copy(null);
        }

        private AnnihilatorPool copy(Runnable dirtyCallback) {
            AnnihilatorPool copy = new AnnihilatorPool(dirtyCallback);
            copy.items.putAll(items);
            return copy;
        }

        private static Optional<BigInteger> readAmount(CompoundTag tag) {
            try {
                byte[] amount = tag.getByteArray(TAG_AMOUNT);
                return amount.length == 0 ? Optional.empty() : Optional.of(new BigInteger(amount));
            } catch (RuntimeException ignored) {
                return Optional.empty();
            }
        }

        private static final class LegacyKeyPoolMap extends HashMap<Object, BigInteger> {
            private Runnable dirtyCallback;

            private LegacyKeyPoolMap(Runnable dirtyCallback) {
                this.dirtyCallback = dirtyCallback;
            }

            @Override
            public BigInteger get(Object key) {
                return PoolKey.fromLegacyObject(key)
                        .map(poolKey -> super.get(poolKey))
                        .orElse(null);
            }

            @Override
            public boolean containsKey(Object key) {
                return PoolKey.fromLegacyObject(key)
                        .map(poolKey -> super.containsKey(poolKey))
                        .orElse(false);
            }

            @Override
            public BigInteger getOrDefault(Object key, BigInteger defaultValue) {
                return PoolKey.fromLegacyObject(key)
                        .map(poolKey -> super.getOrDefault(poolKey, defaultValue))
                        .orElse(defaultValue);
            }

            @Override
            public BigInteger put(Object key, BigInteger value) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                BigInteger previous = super.put(poolKey.get(), value);
                if (!Objects.equals(previous, value)) {
                    markDirty();
                }
                return previous;
            }

            @Override
            public BigInteger remove(Object key) {
                BigInteger previous = PoolKey.fromLegacyObject(key)
                        .map(poolKey -> super.remove(poolKey))
                        .orElse(null);
                if (previous != null) {
                    markDirty();
                }
                return previous;
            }

            @Override
            public void putAll(Map<?, ? extends BigInteger> map) {
                if (!map.isEmpty()) {
                    for (Map.Entry<?, ? extends BigInteger> entry : map.entrySet()) {
                        put(entry.getKey(), entry.getValue());
                    }
                }
            }

            @Override
            public void clear() {
                if (!isEmpty()) {
                    super.clear();
                    markDirty();
                }
            }

            private void markDirty() {
                if (dirtyCallback != null) {
                    dirtyCallback.run();
                }
            }
        }
    }

    public record PoolKey(Kind kind, ResourceLocation item, int meta, String fluid, String oreDict) {
        public PoolKey {
            Objects.requireNonNull(kind, "kind");
        }

        public static PoolKey item(Item item) {
            return item(HbmRegistryUtil.itemKey(item));
        }

        public static PoolKey item(ResourceLocation item) {
            return new PoolKey(Kind.ITEM, Objects.requireNonNull(item, "item"), 0, null, null);
        }

        public static PoolKey itemMeta(ResourceLocation item, int meta) {
            return new PoolKey(Kind.ITEM_META, Objects.requireNonNull(item, "item"), meta, null, null);
        }

        public static PoolKey itemMeta(ItemStack stack, int meta) {
            Objects.requireNonNull(stack, "stack");
            return itemMeta(HbmRegistryUtil.itemKey(stack.getItem()), meta);
        }

        public static PoolKey legacyItem(Item item, int legacyMeta) {
            return itemMeta(HbmRegistryUtil.itemKey(Objects.requireNonNull(item, "item")), legacyMeta);
        }

        public static PoolKey legacyItem(ResourceLocation item, int legacyMeta) {
            return itemMeta(item, legacyMeta);
        }

        public static PoolKey legacyItem(LegacyItemKey key) {
            Objects.requireNonNull(key, "key");
            return itemMeta(key.item(), key.meta());
        }

        public static PoolKey fromItemStack(ItemStack stack) {
            Objects.requireNonNull(stack, "stack");
            return item(stack.getItem());
        }

        public static Optional<PoolKey> fromLegacyObject(Object legacyKey) {
            if (legacyKey == null) {
                return Optional.empty();
            }
            if (legacyKey instanceof PoolKey poolKey) {
                return Optional.of(poolKey);
            }
            if (legacyKey instanceof LegacyItemKey key) {
                return Optional.of(legacyItem(key));
            }
            if (legacyKey instanceof Item item) {
                return Optional.of(item(item));
            }
            if (legacyKey instanceof ItemStack stack && !stack.isEmpty()) {
                return Optional.of(itemMeta(stack, stack.getDamageValue()));
            }
            if (legacyKey instanceof FluidType fluid) {
                return Optional.of(fluid(fluid));
            }
            if (legacyKey instanceof String oreDict && !oreDict.isBlank()) {
                return Optional.of(oreDict(oreDict));
            }
            if (legacyKey instanceof ResourceLocation item) {
                return Optional.of(item(item));
            }
            return Optional.empty();
        }

        public static PoolKey fluid(FluidType fluid) {
            Objects.requireNonNull(fluid, "fluid");
            return fluid(fluid.getName());
        }

        public static PoolKey fluid(String fluid) {
            return new PoolKey(Kind.FLUID, null, 0, Objects.requireNonNull(fluid, "fluid"), null);
        }

        public static PoolKey oreDict(String oreDict) {
            return new PoolKey(Kind.ORE_DICT, null, 0, null, Objects.requireNonNull(oreDict, "oreDict"));
        }

        public static Optional<PoolKey> load(CompoundTag tag) {
            Kind kind = Kind.byLegacyId(tag.getByte(TAG_KEY));
            return switch (kind) {
                case ITEM -> readItem(tag).map(PoolKey::item);
                case ITEM_META -> readItem(tag).map(item -> itemMeta(item, tag.getShort(TAG_META)));
                case FLUID -> Optional.of(fluid(tag.getString(TAG_FLUID)));
                case ORE_DICT -> Optional.of(oreDict(tag.getString(TAG_DICT)));
                case UNKNOWN -> Optional.empty();
            };
        }

        public void save(CompoundTag tag) {
            tag.putByte(TAG_KEY, kind.legacyId);
            switch (kind) {
                case ITEM -> tag.putString(TAG_ITEM, item.toString());
                case ITEM_META -> {
                    tag.putString(TAG_ITEM, item.toString());
                    tag.putShort(TAG_META, (short) meta);
                }
                case FLUID -> tag.putString(TAG_FLUID, fluid);
                case ORE_DICT -> tag.putString(TAG_DICT, oreDict);
                case UNKNOWN -> {
                }
            }
        }

        public FluidType fluidType() {
            return kind == Kind.FLUID ? HbmFluids.fromName(fluid) : HbmFluids.NONE;
        }

        private static Optional<ResourceLocation> readItem(CompoundTag tag) {
            return Optional.ofNullable(ResourceLocation.tryParse(tag.getString(TAG_ITEM)));
        }
    }

    public enum Kind {
        ITEM(0),
        ITEM_META(1),
        FLUID(2),
        ORE_DICT(3),
        UNKNOWN(-1);

        private final byte legacyId;

        Kind(int legacyId) {
            this.legacyId = (byte) legacyId;
        }

        public String commandName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Optional<Kind> byCommandName(String name) {
            if (name == null || name.isBlank()) {
                return Optional.empty();
            }
            String normalized = name.trim().toUpperCase(java.util.Locale.ROOT);
            for (Kind kind : values()) {
                if (kind != UNKNOWN && kind.name().equals(normalized)) {
                    return Optional.of(kind);
                }
            }
            return Optional.empty();
        }

        private static Kind byLegacyId(byte legacyId) {
            for (Kind kind : values()) {
                if (kind.legacyId == legacyId) {
                    return kind;
                }
            }
            return UNKNOWN;
        }
    }

    public record LegacyItemKey(ResourceLocation item, int meta) {
        public LegacyItemKey {
            Objects.requireNonNull(item, "item");
        }

        public static LegacyItemKey of(Item item, int meta) {
            return new LegacyItemKey(HbmRegistryUtil.itemKey(Objects.requireNonNull(item, "item")), meta);
        }

        public static LegacyItemKey of(ItemStack stack) {
            Objects.requireNonNull(stack, "stack");
            return of(stack.getItem(), stack.getDamageValue());
        }
    }

    public record IncrementResult(BigInteger previous, BigInteger current) {
    }

    public record LoadDiagnostics(boolean hasPoolsTag, int poolsRead, int poolsLoaded,
                                  int missingPoolNames, int missingPoolTags, int duplicatePoolNames,
                                  int entriesRead, int entriesLoaded,
                                  int invalidKeys, int invalidAmounts, int duplicateKeys) {
        public static LoadDiagnostics empty() {
            return new LoadDiagnostics(false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public boolean clean() {
            return hasPoolsTag
                    && missingPoolNames == 0
                    && missingPoolTags == 0
                    && duplicatePoolNames == 0
                    && invalidKeys == 0
                    && invalidAmounts == 0
                    && duplicateKeys == 0;
        }

        public int problemCount() {
            return (hasPoolsTag ? 0 : 1)
                    + missingPoolNames
                    + missingPoolTags
                    + duplicatePoolNames
                    + invalidKeys
                    + invalidAmounts
                    + duplicateKeys;
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasPoolsTag) {
                issues.add("missing_pools");
            }
            if (missingPoolNames > 0) {
                issues.add("missing_pool_names=" + missingPoolNames);
            }
            if (missingPoolTags > 0) {
                issues.add("missing_pool_tags=" + missingPoolTags);
            }
            if (duplicatePoolNames > 0) {
                issues.add("duplicate_pool_names=" + duplicatePoolNames);
            }
            if (invalidKeys > 0) {
                issues.add("invalid_keys=" + invalidKeys);
            }
            if (invalidAmounts > 0) {
                issues.add("invalid_amounts=" + invalidAmounts);
            }
            if (duplicateKeys > 0) {
                issues.add("duplicate_keys=" + duplicateKeys);
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "hasPools=" + hasPoolsTag
                    + " poolsRead=" + poolsRead
                    + " poolsLoaded=" + poolsLoaded
                    + " missingPoolNames=" + missingPoolNames
                    + " missingPoolTags=" + missingPoolTags
                    + " duplicatePoolNames=" + duplicatePoolNames
                    + " entriesRead=" + entriesRead
                    + " entriesLoaded=" + entriesLoaded
                    + " invalidKeys=" + invalidKeys
                    + " invalidAmounts=" + invalidAmounts
                    + " duplicateKeys=" + duplicateKeys
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }

    public record PoolEntryLoadDiagnostics(int entriesRead, int entriesLoaded,
                                           int invalidKeys, int invalidAmounts, int duplicateKeys) {
    }

    public record PoolLoadDiagnostics(int poolIndex, String poolName, boolean hasPoolName, boolean hasPoolTag,
                                      boolean duplicatePoolName, int entriesRead, int entriesLoaded,
                                      int invalidKeys, int invalidAmounts, int duplicateKeys) {
        public PoolLoadDiagnostics {
            poolName = poolName == null ? "" : poolName;
        }

        public boolean clean() {
            return hasPoolName
                    && hasPoolTag
                    && !duplicatePoolName
                    && invalidKeys == 0
                    && invalidAmounts == 0
                    && duplicateKeys == 0;
        }

        public int problemCount() {
            return (hasPoolName ? 0 : 1)
                    + (hasPoolTag ? 0 : 1)
                    + (duplicatePoolName ? 1 : 0)
                    + invalidKeys
                    + invalidAmounts
                    + duplicateKeys;
        }

        public List<String> issues() {
            List<String> issues = new ArrayList<>();
            if (!hasPoolName) {
                issues.add("missing_pool_name");
            }
            if (!hasPoolTag) {
                issues.add("missing_pool");
            }
            if (duplicatePoolName) {
                issues.add("duplicate_pool_name");
            }
            if (invalidKeys > 0) {
                issues.add("invalid_keys=" + invalidKeys);
            }
            if (invalidAmounts > 0) {
                issues.add("invalid_amounts=" + invalidAmounts);
            }
            if (duplicateKeys > 0) {
                issues.add("duplicate_keys=" + duplicateKeys);
            }
            return List.copyOf(issues);
        }

        public String summary() {
            return "poolIndex=" + poolIndex
                    + " poolName=" + poolName
                    + " hasPoolName=" + hasPoolName
                    + " hasPoolTag=" + hasPoolTag
                    + " duplicatePoolName=" + duplicatePoolName
                    + " entriesRead=" + entriesRead
                    + " entriesLoaded=" + entriesLoaded
                    + " invalidKeys=" + invalidKeys
                    + " invalidAmounts=" + invalidAmounts
                    + " duplicateKeys=" + duplicateKeys
                    + " problems=" + problemCount()
                    + " issues=" + issues()
                    + " clean=" + clean();
        }
    }

    public record PoolPushResult(IncrementResult increment, boolean paidOut, String payoutStatus,
                                 boolean alwaysPayOut) {
        private static PoolPushResult empty(boolean alwaysPayOut) {
            return new PoolPushResult(new IncrementResult(BigInteger.ZERO, BigInteger.ZERO), false, "empty",
                    alwaysPayOut);
        }
    }

    public record ItemPoolPushResult(IncrementResult preferredIncrement, IncrementResult itemIncrement,
                                     Optional<IncrementResult> metaIncrement,
                                     List<IncrementResult> oreDictIncrements, boolean paidOut,
                                     String payoutStatus, boolean alwaysPayOut) {
        private static ItemPoolPushResult empty(boolean alwaysPayOut) {
            IncrementResult empty = new IncrementResult(BigInteger.ZERO, BigInteger.ZERO);
            return new ItemPoolPushResult(empty, empty, Optional.empty(), List.of(), false, "empty", alwaysPayOut);
        }
    }

    public record PoolSummary(String name, int entries, BigInteger totalAmount,
                              Map<Kind, Integer> keyKindCounts,
                              Map<Kind, BigInteger> keyKindTotals) {
        public PoolSummary {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(totalAmount, "totalAmount");
            keyKindCounts = keyKindCounts == null ? Map.of() : Map.copyOf(keyKindCounts);
            keyKindTotals = keyKindTotals == null ? Map.of() : Map.copyOf(keyKindTotals);
        }

        private static PoolSummary of(String name, AnnihilatorPool pool) {
            return new PoolSummary(name, pool.size(), pool.totalAmount(),
                    pool.keyKindCounts(), pool.keyKindTotals());
        }
    }
}
