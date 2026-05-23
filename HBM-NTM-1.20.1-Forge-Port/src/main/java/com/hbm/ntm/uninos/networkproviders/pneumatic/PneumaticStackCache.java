package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

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

    public void addToCache(PneumaticSlotMonitor monitor) {
        StackIdentity identity = StackIdentity.of(monitor.toZeroStack());
        CacheSlot cache = cacheSlots.computeIfAbsent(identity, ignored -> new CacheSlot(monitor.toZeroStack()));
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

        private CacheSlot(@Nullable ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                this.displayStack = ItemStack.EMPTY;
                this.stackSize = 0L;
            } else {
                this.displayStack = stack.copy();
                this.displayStack.setCount(1);
                this.stackSize = stack.getCount();
            }
        }

        public @Nullable ItemStack getDisplayStack() {
            return displayStack;
        }

        public long getStackSize() {
            return stackSize;
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
    }

    public record StackIdentity(Item item, int damage, @Nullable CompoundTag tag) {
        private static StackIdentity of(@Nullable ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return new StackIdentity(ItemStack.EMPTY.getItem(), 0, null);
            }
            return new StackIdentity(stack.getItem(), stack.getDamageValue(), stack.hasTag() ? stack.getTag().copy() : null);
        }
    }
}
