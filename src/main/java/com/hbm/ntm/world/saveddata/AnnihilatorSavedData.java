package com.hbm.ntm.world.saveddata;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

    private final Map<String, AnnihilatorPool> pools = new HashMap<>();

    public AnnihilatorSavedData() {
        setDirty();
    }

    public static AnnihilatorSavedData load(CompoundTag tag) {
        AnnihilatorSavedData data = new AnnihilatorSavedData();
        data.pools.clear();
        ListTag pools = tag.getList(TAG_POOLS, Tag.TAG_COMPOUND);
        for (int i = 0; i < pools.size(); i++) {
            CompoundTag poolTag = pools.getCompound(i);
            AnnihilatorPool pool = new AnnihilatorPool();
            pool.deserialize(poolTag.getList(TAG_POOL, Tag.TAG_COMPOUND));
            data.pools.put(poolTag.getString(TAG_POOL_NAME), pool);
        }
        data.setDirty(false);
        return data;
    }

    public static AnnihilatorSavedData forLevel(ServerLevel level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, AnnihilatorSavedData::load, AnnihilatorSavedData::new);
    }

    public static Optional<AnnihilatorSavedData> forLevel(Level level) {
        return WorldSavedDataHelper.get(level, DATA_NAME, AnnihilatorSavedData::load, AnnihilatorSavedData::new);
    }

    public static AnnihilatorSavedData getData(ServerLevel level) {
        return forLevel(level);
    }

    public static Optional<AnnihilatorSavedData> getData(Level level) {
        return forLevel(level);
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

    public AnnihilatorPool grabPool(String pool) {
        return pools.computeIfAbsent(Objects.requireNonNull(pool, "pool"), ignored -> new AnnihilatorPool());
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

    public PoolPushResult pushToPool(String pool, FluidType type, long amount, boolean alwaysPayOut) {
        if (type == null || amount <= 0L) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.fluid(type), amount);
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
    }

    public PoolPushResult pushToPool(String pool, ItemStack stack, boolean alwaysPayOut) {
        if (stack == null || stack.isEmpty()) {
            return PoolPushResult.empty(alwaysPayOut);
        }
        IncrementResult result = increment(pool, PoolKey.item(stack.getItem()), stack.getCount());
        return new PoolPushResult(result, false, "payout_deferred", alwaysPayOut);
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
        IncrementResult result = increment(pool, PoolKey.itemMeta(BuiltInRegistries.ITEM.getKey(stack.getItem()),
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

    public List<Map.Entry<String, AnnihilatorPool>> poolsSnapshot() {
        return pools.entrySet().stream()
                .<Map.Entry<String, AnnihilatorPool>>map(
                        entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue().copy()))
                .toList();
    }

    public List<String> poolNamesSnapshot() {
        return pools.keySet().stream().sorted().toList();
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

    public void markDirty() {
        setDirty();
    }

    public static class AnnihilatorPool {
        private final Map<PoolKey, BigInteger> items = new HashMap<>();

        public IncrementResult increment(PoolKey key, long amount) {
            Objects.requireNonNull(key, "key");
            BigInteger previous = items.getOrDefault(key, BigInteger.ZERO);
            BigInteger current = previous.add(BigInteger.valueOf(amount));
            items.put(key, current);
            return new IncrementResult(previous, current);
        }

        public BigInteger getAmount(PoolKey key) {
            return items.getOrDefault(key, BigInteger.ZERO);
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
            return items.entrySet().stream()
                    .<Map.Entry<PoolKey, BigInteger>>map(
                            entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue()))
                    .toList();
        }

        private ListTag serialize() {
            ListTag list = new ListTag();
            for (Map.Entry<PoolKey, BigInteger> entry : items.entrySet()) {
                CompoundTag tag = new CompoundTag();
                entry.getKey().save(tag);
                tag.putByteArray(TAG_AMOUNT, entry.getValue().toByteArray());
                list.add(tag);
            }
            return list;
        }

        private void deserialize(ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                PoolKey.load(tag).ifPresent(key -> readAmount(tag).ifPresent(amount -> items.put(key, amount)));
            }
        }

        private AnnihilatorPool copy() {
            AnnihilatorPool copy = new AnnihilatorPool();
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
    }

    public record PoolKey(Kind kind, ResourceLocation item, int meta, String fluid, String oreDict) {
        public PoolKey {
            Objects.requireNonNull(kind, "kind");
        }

        public static PoolKey item(Item item) {
            return item(BuiltInRegistries.ITEM.getKey(item));
        }

        public static PoolKey item(ResourceLocation item) {
            return new PoolKey(Kind.ITEM, Objects.requireNonNull(item, "item"), 0, null, null);
        }

        public static PoolKey itemMeta(ResourceLocation item, int meta) {
            return new PoolKey(Kind.ITEM_META, Objects.requireNonNull(item, "item"), meta, null, null);
        }

        public static PoolKey itemMeta(ItemStack stack, int meta) {
            Objects.requireNonNull(stack, "stack");
            return itemMeta(BuiltInRegistries.ITEM.getKey(stack.getItem()), meta);
        }

        public static PoolKey fromItemStack(ItemStack stack) {
            Objects.requireNonNull(stack, "stack");
            return item(stack.getItem());
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

        private static Optional<PoolKey> load(CompoundTag tag) {
            Kind kind = Kind.byLegacyId(tag.getByte(TAG_KEY));
            return switch (kind) {
                case ITEM -> readItem(tag).map(PoolKey::item);
                case ITEM_META -> readItem(tag).map(item -> itemMeta(item, tag.getShort(TAG_META)));
                case FLUID -> Optional.of(fluid(tag.getString(TAG_FLUID)));
                case ORE_DICT -> Optional.of(oreDict(tag.getString(TAG_DICT)));
                case UNKNOWN -> Optional.empty();
            };
        }

        private void save(CompoundTag tag) {
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

        private static Kind byLegacyId(byte legacyId) {
            for (Kind kind : values()) {
                if (kind.legacyId == legacyId) {
                    return kind;
                }
            }
            return UNKNOWN;
        }
    }

    public record IncrementResult(BigInteger previous, BigInteger current) {
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
}
