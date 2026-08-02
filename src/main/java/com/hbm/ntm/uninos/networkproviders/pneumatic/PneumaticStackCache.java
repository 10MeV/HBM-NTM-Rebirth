package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PneumaticStackCache {
    private final BlockPos pos;
    private boolean expired;
    private final LinkedHashMap<StackIdentity, CacheSlot> cacheSlots = new LinkedHashMap<>();

    public PneumaticStackCache(BlockPos pos) {
        this.pos = pos.immutable();
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean hasExpired() {
        return expired;
    }

    public Map<StackIdentity, CacheSlot> getCacheSlots() {
        return Map.copyOf(cacheSlots);
    }

    /** Source-shaped aggregate removal through the live storage monitors. */
    public long consumeItemsAndReturnQuantity(ItemStack stack, long amount) {
        if (stack == null || stack.isEmpty() || amount <= 0L) {
            return 0L;
        }
        CacheSlot cache = cacheSlots.get(StackIdentity.of(stack));
        if (cache == null) {
            return 0L;
        }
        long originalAmount = amount;
        for (PneumaticSlotMonitor monitor : cache.monitors) {
            ItemStack original = monitor.parent.getSlotAt(monitor.index);
            if (!sameIdentity(original, stack)) {
                continue;
            }
            amount = monitor.parent.useUpItem(monitor.index, amount);
            if (amount <= 0L) {
                break;
            }
        }
        return originalAmount - amount;
    }

    /** Source-shaped aggregate insertion through the live storage monitors. */
    public long addItemsAndReturnQuantity(ItemStack stack, long amount) {
        if (stack == null || stack.isEmpty() || amount <= 0L) {
            return amount;
        }
        CacheSlot cache = cacheSlots.get(StackIdentity.of(stack));
        if (cache != null) {
            for (PneumaticSlotMonitor monitor : cache.monitors) {
                ItemStack original = monitor.parent.getSlotAt(monitor.index);
                if (!sameIdentity(original, stack)) {
                    continue;
                }
                amount = monitor.parent.addItem(monitor.index, amount);
                if (amount <= 0L) {
                    return 0L;
                }
            }
        }
        if (amount > 0L) {
            CacheSlot emptyCache = cacheSlots.get(StackIdentity.of(ItemStack.EMPTY));
            if (emptyCache != null) {
                for (PneumaticSlotMonitor monitor : emptyCache.monitors) {
                    if (!monitor.parent.allowTypeSetting() || !isEmpty(monitor.parent.getSlotAt(monitor.index))) {
                        continue;
                    }
                    amount = monitor.parent.setupType(monitor.index, stack, amount);
                    if (amount <= 0L) {
                        break;
                    }
                }
            }
        }
        return amount;
    }

    public void addToCache(PneumaticSlotMonitor monitor) {
        StackIdentity identity = StackIdentity.of(monitor);
        CacheSlot cache = cacheSlots.computeIfAbsent(identity, ignored -> new CacheSlot(monitor));
        cache.addMonitor(monitor);
    }

    public void dissolveCache() {
        for (CacheSlot cacheSlot : cacheSlots.values()) {
            cacheSlot.destroy();
        }
        cacheSlots.clear();
        expired = true;
    }

    public final class CacheSlot {
        @Nullable private final ItemStack displayStack;
        private long stackSize;
        private final LinkedHashSet<PneumaticSlotMonitor> monitors = new LinkedHashSet<>();

        private CacheSlot(PneumaticSlotMonitor monitor) {
            this.displayStack = monitor.toDisplayStack();
            this.stackSize = 0L;
        }

        public @Nullable ItemStack getDisplayStack() {
            return displayStack;
        }

        public long getStackSize() {
            return stackSize;
        }

        public Set<PneumaticSlotMonitor> getMonitors() {
            return Set.copyOf(monitors);
        }

        public void addMonitor(PneumaticSlotMonitor monitor) {
            if (monitors.add(monitor)) {
                monitor.viewedBy.add(this);
                changeAmount(monitor.getStackSize());
            }
        }

        public void removeMonitor(PneumaticSlotMonitor monitor) {
            if (monitors.remove(monitor)) {
                changeAmount(-monitor.getStackSize());
            }
        }

        public void destroy() {
            for (PneumaticSlotMonitor monitor : monitors) {
                monitor.viewedBy.remove(this);
            }
            stackSize = 0L;
        }

        public void changeAmount(long delta) {
            stackSize += delta;
        }

        public PneumaticStackCache getStackCache() {
            return PneumaticStackCache.this;
        }

        public void recount() {
            stackSize = 0L;
            for (PneumaticSlotMonitor monitor : monitors) {
                stackSize += monitor.getStackSize();
            }
        }
    }

    public record StackIdentity(Item item, int damage, @Nullable CompoundTag tag) {
        public static StackIdentity of(PneumaticSlotMonitor monitor) {
            if (monitor == null || monitor.getItem() == null) {
                return new StackIdentity(ItemStack.EMPTY.getItem(), 0, null);
            }
            return new StackIdentity(monitor.getItem(), monitor.getDamage(), monitor.getTag());
        }

        public static StackIdentity of(@Nullable ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return new StackIdentity(ItemStack.EMPTY.getItem(), 0, null);
            }
            return new StackIdentity(stack.getItem(), stack.getDamageValue(), stack.hasTag() ? stack.getTag().copy() : null);
        }
    }

    private static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.isEmpty();
    }

    private static boolean sameIdentity(@Nullable ItemStack first, ItemStack second) {
        return !isEmpty(first) && ItemStack.isSameItemSameTags(first, second);
    }
}
