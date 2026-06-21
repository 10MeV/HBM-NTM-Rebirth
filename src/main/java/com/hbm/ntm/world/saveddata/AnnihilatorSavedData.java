package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.AnnihilatorRecipeRuntime;
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
import java.util.function.BiFunction;
import java.util.function.Function;

public class AnnihilatorSavedData extends SavedData {
    public static final String DATA_NAME = "annihilator";
    public static final String KEY = DATA_NAME;
    public static final String TAG_POOLS = "pools";
    public static final String TAG_POOL_NAME = "poolname";
    public static final String TAG_POOL = "pool";
    public static final String TAG_KEY = "key";
    public static final String TAG_ITEM = "item";
    public static final String TAG_META = "meta";
    public static final String TAG_FLUID = "fluid";
    public static final String TAG_DICT = "dict";
    public static final String TAG_AMOUNT = "amount";
    public static final byte KEY_ITEM = 0;
    public static final byte KEY_ITEM_META = 1;
    public static final byte KEY_FLUID = 2;
    public static final byte KEY_ORE_DICT = 3;

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

    public int readLegacyPools(CompoundTag tag) {
        return readLegacyPools(tag, true);
    }

    public int readLegacyPools(CompoundTag tag, boolean clearExisting) {
        AnnihilatorSavedData loaded = load(tag == null ? new CompoundTag() : tag);
        if (clearExisting) {
            pools.clear();
        }
        loaded.pools.forEach((name, pool) -> pools.put(name, pool.copy(this::setDirty)));
        loadDiagnostics = loaded.loadDiagnostics;
        poolLoadDiagnostics = loaded.poolLoadDiagnostics;
        if (loaded.poolCount() > 0) {
            setDirty();
        }
        return loaded.poolCount();
    }

    public int readLegacyPools(LegacyPools legacyPools) {
        return readLegacyPools(legacyPools, true);
    }

    public int readLegacyPools(LegacyPools legacyPools, boolean clearExisting) {
        if (legacyPools == null) {
            return 0;
        }
        if (clearExisting) {
            pools.clear();
        }
        for (LegacyPool pool : legacyPools.pools()) {
            pools.put(pool.name(), pool.toPool(this::setDirty));
        }
        if (!legacyPools.isEmpty()) {
            setDirty();
        }
        return legacyPools.poolCount();
    }

    public int readLegacyPools(ListTag pools) {
        return readLegacyPools(pools, true);
    }

    public int readLegacyPools(ListTag pools, boolean clearExisting) {
        if (pools == null) {
            return 0;
        }
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_POOLS, pools);
        return readLegacyPools(tag, clearExisting);
    }

    public void writeLegacyPools(CompoundTag tag) {
        if (tag != null) {
            tag.put(TAG_POOLS, writeLegacyPoolsList());
        }
    }

    public CompoundTag writeLegacyPoolsTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyPools(tag);
        return tag;
    }

    public ListTag writeLegacyPoolsList() {
        ListTag poolsTag = new ListTag();
        for (Map.Entry<String, AnnihilatorPool> entry : pools.entrySet()) {
            poolsTag.add(writeLegacyPoolTag(entry.getKey(), entry.getValue()));
        }
        return poolsTag;
    }

    public LegacyPools legacyPoolsSnapshot() {
        return new LegacyPools(pools.entrySet().stream()
                .map(entry -> new LegacyPool(entry.getKey(), entry.getValue().entriesSnapshot().stream()
                        .map(poolEntry -> new AnnihilatorPool.PoolEntry(poolEntry.getKey(),
                                poolEntry.getKey().toLegacyObject(), poolEntry.getValue()))
                        .toList()))
                .toList());
    }

    public boolean readLegacyPool(CompoundTag tag) {
        Optional<LegacyPool> legacyPool = readLegacyPoolTag(tag);
        legacyPool.ifPresent(pool -> pools.put(pool.name(), pool.toPool(this::setDirty)));
        return legacyPool.isPresent();
    }

    public boolean readLegacyPool(String poolName, ListTag entries) {
        if (poolName == null || poolName.isBlank() || entries == null) {
            return false;
        }
        AnnihilatorPool pool = createPool();
        pool.deserialize(entries);
        pools.put(poolName, pool);
        setDirty();
        return true;
    }

    public boolean readLegacyEntry(String poolName, CompoundTag tag) {
        if (poolName == null || poolName.isBlank() || tag == null) {
            return false;
        }
        boolean loaded = grabPool(poolName).deserializeEntry(tag);
        if (loaded) {
            setDirty();
        }
        return loaded;
    }

    public static void writeLegacyPool(CompoundTag tag, String poolName, AnnihilatorPool pool) {
        if (tag == null || poolName == null || poolName.isBlank() || pool == null) {
            return;
        }
        tag.putString(TAG_POOL_NAME, poolName);
        tag.put(TAG_POOL, pool.serialize());
    }

    public static CompoundTag writeLegacyPoolTag(String poolName, AnnihilatorPool pool) {
        CompoundTag tag = new CompoundTag();
        writeLegacyPool(tag, poolName, pool);
        return tag;
    }

    public static LegacyPools readLegacyPoolsTag(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_POOLS, Tag.TAG_LIST)) {
            return LegacyPools.EMPTY;
        }
        return new LegacyPools(readLegacyPoolsList(tag.getList(TAG_POOLS, Tag.TAG_COMPOUND)));
    }

    public static List<LegacyPool> readLegacyPoolsList(ListTag pools) {
        if (pools == null) {
            return List.of();
        }
        List<LegacyPool> result = new ArrayList<>();
        for (int i = 0; i < pools.size(); i++) {
            readLegacyPoolTag(pools.getCompound(i)).ifPresent(result::add);
        }
        return List.copyOf(result);
    }

    public static void writeLegacyPools(CompoundTag tag, Collection<LegacyPool> pools) {
        if (tag != null) {
            tag.put(TAG_POOLS, writeLegacyPoolsList(pools));
        }
    }

    public static CompoundTag writeLegacyPoolsTag(Collection<LegacyPool> pools) {
        CompoundTag tag = new CompoundTag();
        writeLegacyPools(tag, pools);
        return tag;
    }

    public static ListTag writeLegacyPoolsList(Collection<LegacyPool> pools) {
        ListTag poolsTag = new ListTag();
        if (pools != null) {
            for (LegacyPool pool : pools) {
                if (pool != null) {
                    poolsTag.add(pool.writeTag());
                }
            }
        }
        return poolsTag;
    }

    public static Optional<LegacyPool> readLegacyPoolTag(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_POOL_NAME, Tag.TAG_STRING)
                || !tag.contains(TAG_POOL, Tag.TAG_LIST)) {
            return Optional.empty();
        }
        String poolName = tag.getString(TAG_POOL_NAME);
        if (poolName.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new LegacyPool(poolName,
                AnnihilatorPool.readLegacyEntries(tag.getList(TAG_POOL, Tag.TAG_COMPOUND))));
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

    public ItemStack pushToPool(ServerLevel level, String pool, FluidType type, long amount, boolean alwaysPayOut) {
        if (type == null || amount <= 0L) {
            return ItemStack.EMPTY;
        }
        PoolKey key = PoolKey.fluid(type);
        IncrementResult result = increment(pool, key, amount);
        return AnnihilatorRecipeRuntime.findPayout(level, key, result, alwaysPayOut);
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

    public ItemStack pushToPool(ServerLevel level, String pool, ItemStack stack, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        PoolKey itemKey = PoolKey.item(stack.getItem());
        IncrementResult itemResult = increment(pool, itemKey, stack.getCount());
        PoolKey metaKey = PoolKey.itemMeta(stack, stack.getDamageValue());
        IncrementResult metaResult = increment(pool, metaKey, stack.getCount());
        ItemStack metaPayout = AnnihilatorRecipeRuntime.findPayout(level, metaKey, metaResult, alwaysPayOut);
        if (!metaPayout.isEmpty()) {
            return metaPayout;
        }
        return AnnihilatorRecipeRuntime.findPayout(level, itemKey, itemResult, alwaysPayOut);
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

    public ItemStack pushLegacyItemToPool(ServerLevel level, String pool, ItemStack stack, int legacyMeta,
            Collection<String> legacyOreDictNames, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PoolKey itemKey = PoolKey.item(stack.getItem());
        IncrementResult itemResult = increment(pool, itemKey, stack.getCount());
        PoolKey metaKey = legacyMeta < 0 ? null : PoolKey.itemMeta(stack, legacyMeta);
        IncrementResult metaResult = metaKey == null ? null : increment(pool, metaKey, stack.getCount());

        ItemStack dictPayout = ItemStack.EMPTY;
        Collection<String> oreDictNames = legacyOreDictNames == null ? List.of() : legacyOreDictNames;
        for (String name : oreDictNames) {
            if (name != null && !name.isBlank()) {
                PoolKey dictKey = PoolKey.oreDict(name);
                IncrementResult dictResult = increment(pool, dictKey, stack.getCount());
                ItemStack payout = AnnihilatorRecipeRuntime.findPayout(level, dictKey, dictResult, alwaysPayOut);
                if (!payout.isEmpty()) {
                    dictPayout = payout;
                }
            }
        }
        if (!dictPayout.isEmpty()) {
            return dictPayout;
        }
        if (metaKey != null && metaResult != null) {
            ItemStack metaPayout = AnnihilatorRecipeRuntime.findPayout(level, metaKey, metaResult, alwaysPayOut);
            if (!metaPayout.isEmpty()) {
                return metaPayout;
            }
        }
        return AnnihilatorRecipeRuntime.findPayout(level, itemKey, itemResult, alwaysPayOut);
    }

    public PoolPushResult pushItemMetaToPool(String pool, ItemStack stack, int legacyMeta, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.itemMeta(HbmRegistryUtil.itemKey(stack.getItem()),
                legacyMeta), stack.getCount());
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
    }

    public ItemStack pushItemMetaToPool(ServerLevel level, String pool, ItemStack stack, int legacyMeta,
            boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        PoolKey key = PoolKey.itemMeta(HbmRegistryUtil.itemKey(stack.getItem()), legacyMeta);
        IncrementResult result = increment(pool, key, stack.getCount());
        return AnnihilatorRecipeRuntime.findPayout(level, key, result, alwaysPayOut);
    }

    public PoolPushResult pushOreDictToPool(String pool, String oreDict, long amount, boolean alwaysPayOut) {
        if (oreDict == null || oreDict.isBlank() || amount <= 0L) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.oreDict(oreDict), amount);
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
    }

    public ItemStack pushOreDictToPool(ServerLevel level, String pool, String oreDict, long amount,
            boolean alwaysPayOut) {
        if (oreDict == null || oreDict.isBlank() || amount <= 0L) {
            return ItemStack.EMPTY;
        }
        PoolKey key = PoolKey.oreDict(oreDict);
        IncrementResult result = increment(pool, key, amount);
        return AnnihilatorRecipeRuntime.findPayout(level, key, result, alwaysPayOut);
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
        public AnnihilatorPool putIfAbsent(String key, AnnihilatorPool value) {
            AnnihilatorPool previous = super.putIfAbsent(key, bindPool(value));
            if (previous == null) {
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
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed) {
                setDirty();
            }
            return removed;
        }

        @Override
        public AnnihilatorPool replace(String key, AnnihilatorPool value) {
            AnnihilatorPool previous = super.replace(key, bindPool(value));
            if (previous != null && previous != value) {
                setDirty();
            }
            return previous;
        }

        @Override
        public boolean replace(String key, AnnihilatorPool oldValue, AnnihilatorPool newValue) {
            boolean replaced = super.replace(key, oldValue, bindPool(newValue));
            if (replaced && oldValue != newValue) {
                setDirty();
            }
            return replaced;
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
        public AnnihilatorPool computeIfAbsent(String key,
                                               Function<? super String, ? extends AnnihilatorPool> mappingFunction) {
            boolean hadKey = containsKey(key);
            AnnihilatorPool result = super.computeIfAbsent(key, poolName -> bindPool(mappingFunction.apply(poolName)));
            if (!hadKey && result != null) {
                setDirty();
            }
            return result;
        }

        @Override
        public AnnihilatorPool compute(String key,
                                       BiFunction<? super String, ? super AnnihilatorPool,
                                               ? extends AnnihilatorPool> remappingFunction) {
            boolean hadKey = containsKey(key);
            AnnihilatorPool previous = super.get(key);
            AnnihilatorPool result = super.compute(key,
                    (poolName, pool) -> bindPool(remappingFunction.apply(poolName, pool)));
            if (hadKey != containsKey(key) || previous != result) {
                setDirty();
            }
            return result;
        }

        @Override
        public AnnihilatorPool computeIfPresent(String key,
                                                BiFunction<? super String, ? super AnnihilatorPool,
                                                        ? extends AnnihilatorPool> remappingFunction) {
            boolean hadKey = containsKey(key);
            AnnihilatorPool previous = super.get(key);
            AnnihilatorPool result = super.computeIfPresent(key,
                    (poolName, pool) -> bindPool(remappingFunction.apply(poolName, pool)));
            if (hadKey && (previous != result || !containsKey(key))) {
                setDirty();
            }
            return result;
        }

        @Override
        public AnnihilatorPool merge(String key, AnnihilatorPool value,
                                     BiFunction<? super AnnihilatorPool, ? super AnnihilatorPool,
                                             ? extends AnnihilatorPool> remappingFunction) {
            boolean hadKey = containsKey(key);
            AnnihilatorPool previous = super.get(key);
            AnnihilatorPool result = super.merge(key, bindPool(value),
                    (oldPool, newPool) -> bindPool(remappingFunction.apply(oldPool, newPool)));
            if (!hadKey || previous != result) {
                setDirty();
            }
            return result;
        }

        @Override
        public void replaceAll(BiFunction<? super String, ? super AnnihilatorPool,
                ? extends AnnihilatorPool> function) {
            if (!isEmpty()) {
                super.replaceAll((name, pool) -> bindPool(function.apply(name, pool)));
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
                        .map(PoolKey::toLegacyObject)
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
                list.add(serializeEntryTag(key.get(), entry.getValue()));
            }
        }

        public static void serializeEntry(CompoundTag tag, Object legacyKey, BigInteger amount) {
            if (tag == null || amount == null) {
                return;
            }
            PoolKey.fromLegacyObject(legacyKey).ifPresent(key -> {
                key.save(tag);
                writeLegacyAmount(tag, amount);
            });
        }

        public static CompoundTag serializeEntryTag(Object legacyKey, BigInteger amount) {
            CompoundTag tag = new CompoundTag();
            serializeEntry(tag, legacyKey, amount);
            return tag;
        }

        public void deserialize(ListTag list) {
            deserializeWithDiagnostics(list);
        }

        public static List<PoolEntry> readLegacyEntries(ListTag list) {
            if (list == null) {
                return List.of();
            }
            List<PoolEntry> entries = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                readLegacyEntryTag(list.getCompound(i)).ifPresent(entries::add);
            }
            return List.copyOf(entries);
        }

        public static ListTag writeLegacyEntriesList(Iterable<PoolEntry> entries) {
            ListTag list = new ListTag();
            if (entries != null) {
                for (PoolEntry entry : entries) {
                    if (entry != null) {
                        list.add(entry.writeTag());
                    }
                }
            }
            return list;
        }

        public PoolEntryLoadDiagnostics deserializeWithDiagnostics(ListTag list) {
            Objects.requireNonNull(list, "list");
            int entriesLoaded = 0;
            int invalidKeys = 0;
            int invalidAmounts = 0;
            int duplicateKeys = 0;
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                Optional<PoolEntry> entry = readLegacyEntryTag(tag);
                if (entry.isEmpty()) {
                    if (deserializePoolKey(tag).isEmpty()) {
                        invalidKeys++;
                    } else {
                        invalidAmounts++;
                    }
                    continue;
                }
                PoolEntry value = entry.get();
                if (items.containsKey(value.key())) {
                    duplicateKeys++;
                }
                items.put(value.key(), value.amount());
                entriesLoaded++;
            }
            return new PoolEntryLoadDiagnostics(list.size(), entriesLoaded, invalidKeys, invalidAmounts,
                    duplicateKeys);
        }

        public boolean deserializeEntry(CompoundTag tag) {
            Optional<PoolEntry> entry = readLegacyEntryTag(tag);
            entry.ifPresent(value -> items.put(value.key(), value.amount()));
            return entry.isPresent();
        }

        public Optional<Map.Entry<PoolKey, BigInteger>> readEntry(CompoundTag tag) {
            return readLegacyEntryTag(tag).map(PoolEntry::modernEntry);
        }

        public Optional<PoolEntry> readLegacyEntry(CompoundTag tag) {
            return readLegacyEntryTag(tag);
        }

        public static Optional<PoolEntry> readLegacyEntryTag(CompoundTag tag) {
            if (tag == null) {
                return Optional.empty();
            }
            Optional<PoolKey> key = PoolKey.load(tag);
            if (key.isEmpty()) {
                return Optional.empty();
            }
            Optional<BigInteger> amount = readAmount(tag);
            if (amount.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new PoolEntry(key.get(), key.get().toLegacyObject(), amount.get()));
        }

        public static Optional<BigInteger> readLegacyAmount(CompoundTag tag) {
            return tag == null ? Optional.empty() : readAmount(tag);
        }

        public static void writeLegacyAmount(CompoundTag tag, BigInteger amount) {
            if (tag != null && amount != null) {
                tag.putByteArray(TAG_AMOUNT, amount.toByteArray());
            }
        }

        public record PoolEntry(PoolKey key, Object legacyKey, BigInteger amount) {
            public PoolEntry {
                Objects.requireNonNull(key, "key");
                legacyKey = legacyKey == null ? key.toLegacyObject() : legacyKey;
                Objects.requireNonNull(amount, "amount");
            }

            public Map.Entry<PoolKey, BigInteger> modernEntry() {
                return new AbstractMap.SimpleImmutableEntry<>(key, amount);
            }

            public Optional<Map.Entry<Object, BigInteger>> legacyEntry() {
                return legacyKey == null ? Optional.empty()
                        : Optional.of(new AbstractMap.SimpleImmutableEntry<>(legacyKey, amount));
            }

            public void write(CompoundTag tag) {
                if (tag != null) {
                    key.save(tag);
                    writeLegacyAmount(tag, amount);
                }
            }

            public CompoundTag writeTag() {
                CompoundTag tag = new CompoundTag();
                write(tag);
                return tag;
            }
        }

        public void serializeKey(CompoundTag tag, Object legacyKey) {
            Objects.requireNonNull(tag, "tag");
            PoolKey.fromLegacyObject(legacyKey).ifPresent(key -> key.save(tag));
        }

        public Object deserializeKey(CompoundTag tag) {
            return deserializeLegacyKey(tag);
        }

        public Optional<PoolKey> deserializePoolKey(CompoundTag tag) {
            return tag == null ? Optional.empty() : PoolKey.load(tag);
        }

        public Object deserializeLegacyKey(CompoundTag tag) {
            return deserializePoolKey(tag)
                    .map(PoolKey::toLegacyObject)
                    .orElse(null);
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
            public BigInteger putIfAbsent(Object key, BigInteger value) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                BigInteger previous = super.putIfAbsent(poolKey.get(), value);
                if (previous == null) {
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
            public boolean remove(Object key, Object value) {
                boolean removed = PoolKey.fromLegacyObject(key)
                        .map(poolKey -> super.remove(poolKey, value))
                        .orElse(false);
                if (removed) {
                    markDirty();
                }
                return removed;
            }

            @Override
            public BigInteger replace(Object key, BigInteger value) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                BigInteger previous = super.replace(poolKey.get(), value);
                if (previous != null && !Objects.equals(previous, value)) {
                    markDirty();
                }
                return previous;
            }

            @Override
            public boolean replace(Object key, BigInteger oldValue, BigInteger newValue) {
                boolean replaced = PoolKey.fromLegacyObject(key)
                        .map(poolKey -> super.replace(poolKey, oldValue, newValue))
                        .orElse(false);
                if (replaced && !Objects.equals(oldValue, newValue)) {
                    markDirty();
                }
                return replaced;
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
            public BigInteger computeIfAbsent(Object key,
                                              Function<? super Object, ? extends BigInteger> mappingFunction) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                boolean hadKey = super.containsKey(poolKey.get());
                BigInteger result = super.computeIfAbsent(poolKey.get(), ignored -> mappingFunction.apply(key));
                if (!hadKey && result != null) {
                    markDirty();
                }
                return result;
            }

            @Override
            public BigInteger compute(Object key,
                                      BiFunction<? super Object, ? super BigInteger,
                                              ? extends BigInteger> remappingFunction) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                boolean hadKey = super.containsKey(poolKey.get());
                BigInteger previous = super.get(poolKey.get());
                BigInteger result = super.compute(poolKey.get(), (ignored, amount) -> remappingFunction.apply(key, amount));
                if (hadKey != super.containsKey(poolKey.get()) || !Objects.equals(previous, result)) {
                    markDirty();
                }
                return result;
            }

            @Override
            public BigInteger computeIfPresent(Object key,
                                               BiFunction<? super Object, ? super BigInteger,
                                                       ? extends BigInteger> remappingFunction) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                BigInteger previous = super.get(poolKey.get());
                BigInteger result = super.computeIfPresent(poolKey.get(),
                        (ignored, amount) -> remappingFunction.apply(key, amount));
                if (!Objects.equals(previous, result)) {
                    markDirty();
                }
                return result;
            }

            @Override
            public BigInteger merge(Object key, BigInteger value,
                                    BiFunction<? super BigInteger, ? super BigInteger,
                                            ? extends BigInteger> remappingFunction) {
                Optional<PoolKey> poolKey = PoolKey.fromLegacyObject(key);
                if (poolKey.isEmpty()) {
                    return null;
                }
                boolean hadKey = super.containsKey(poolKey.get());
                BigInteger previous = super.get(poolKey.get());
                BigInteger result = super.merge(poolKey.get(), value, remappingFunction);
                if (!hadKey || !Objects.equals(previous, result)) {
                    markDirty();
                }
                return result;
            }

            @Override
            public void replaceAll(BiFunction<? super Object, ? super BigInteger,
                    ? extends BigInteger> function) {
                if (!isEmpty()) {
                    super.replaceAll((key, amount) -> function.apply(key, amount));
                    markDirty();
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

        public Object toLegacyObject() {
            return switch (kind) {
                case ITEM -> HbmRegistryUtil.item(item).map(Object.class::cast).orElse(item);
                case ITEM_META -> new LegacyItemKey(item, meta);
                case FLUID -> HbmFluids.fromName(fluid);
                case ORE_DICT -> oreDict;
                case UNKNOWN -> null;
            };
        }

        private static Optional<ResourceLocation> readItem(CompoundTag tag) {
            return Optional.ofNullable(ResourceLocation.tryParse(tag.getString(TAG_ITEM)));
        }
    }

    public enum Kind {
        ITEM(KEY_ITEM),
        ITEM_META(KEY_ITEM_META),
        FLUID(KEY_FLUID),
        ORE_DICT(KEY_ORE_DICT),
        UNKNOWN(-1);

        private final byte legacyId;

        Kind(int legacyId) {
            this.legacyId = (byte) legacyId;
        }

        public String commandName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public byte legacyId() {
            return legacyId;
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

        public static Kind byLegacyId(byte legacyId) {
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

    public record LegacyPool(String name, List<AnnihilatorPool.PoolEntry> entries) {
        public LegacyPool {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name");
            }
            entries = entries == null ? List.of() : List.copyOf(entries);
        }

        public AnnihilatorPool toPool() {
            return toPool(null);
        }

        private AnnihilatorPool toPool(Runnable dirtyCallback) {
            AnnihilatorPool pool = new AnnihilatorPool(dirtyCallback);
            for (AnnihilatorPool.PoolEntry entry : entries) {
                pool.items.put(entry.key(), entry.amount());
            }
            return pool;
        }

        public void write(CompoundTag tag) {
            if (tag != null) {
                tag.putString(TAG_POOL_NAME, name);
                tag.put(TAG_POOL, AnnihilatorPool.writeLegacyEntriesList(entries));
            }
        }

        public CompoundTag writeTag() {
            CompoundTag tag = new CompoundTag();
            write(tag);
            return tag;
        }
    }

    public record LegacyPools(List<LegacyPool> pools) {
        public static final LegacyPools EMPTY = new LegacyPools(List.of());

        public LegacyPools {
            pools = pools == null ? List.of() : pools.stream()
                    .filter(Objects::nonNull)
                    .toList();
        }

        public boolean isEmpty() {
            return pools.isEmpty();
        }

        public int poolCount() {
            return pools.size();
        }

        public int entryCount() {
            return pools.stream().mapToInt(pool -> pool.entries().size()).sum();
        }

        public List<String> poolNames() {
            return pools.stream().map(LegacyPool::name).toList();
        }

        public AnnihilatorSavedData toData() {
            AnnihilatorSavedData data = new AnnihilatorSavedData();
            data.pools.clear();
            for (LegacyPool pool : pools) {
                data.pools.put(pool.name(), pool.toPool(data::setDirty));
            }
            data.setDirty(false);
            return data;
        }

        public void write(CompoundTag tag) {
            writeLegacyPools(tag, pools);
        }

        public CompoundTag writeTag() {
            return writeLegacyPoolsTag(pools);
        }

        public ListTag writeList() {
            return writeLegacyPoolsList(pools);
        }
    }
}
