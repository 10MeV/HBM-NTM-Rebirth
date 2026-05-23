package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class PneumaticSlotMonitor {
    public final int index;
    public final PneumaticSlotMonitorProvider parent;
    final LinkedHashSet<PneumaticStackCache.CacheSlot> viewedBy = new LinkedHashSet<>();

    @Nullable private Item item;
    private long stackSize;
    private int damage;
    @Nullable private CompoundTag tag;
    private boolean availabilityChanged = true;

    public PneumaticSlotMonitor(int index, PneumaticSlotMonitorProvider parent) {
        this.index = index;
        this.parent = parent;
    }

    public long getStackSize() {
        return stackSize;
    }

    public @Nullable ItemStack toZeroStack() {
        if (item == null) {
            return null;
        }
        ItemStack stack = new ItemStack(item, 0);
        stack.setDamageValue(damage);
        if (tag != null) {
            stack.setTag(tag.copy());
        }
        return stack;
    }

    public void availabilityHasChanged() {
        availabilityChanged = true;
    }

    public void checkUpdate() {
        PneumaticNetwork pneumaticNetwork = parent.getRelevantNetwork();
        if (availabilityChanged) {
            if (pneumaticNetwork != null) {
                for (PneumaticStackCache cache : pneumaticNetwork.getAccessors()) {
                    if (!cache.hasExpired() && parent.isAvailableToCache(cache)) {
                        cache.addToCache(this);
                    }
                }
            }

            Iterator<PneumaticStackCache.CacheSlot> iterator = viewedBy.iterator();
            while (iterator.hasNext()) {
                PneumaticStackCache.CacheSlot slot = iterator.next();
                PneumaticStackCache cache = slot.getStackCache();
                if (cache.hasExpired() || !parent.isAvailableToCache(cache)) {
                    slot.removeMonitor(this);
                    iterator.remove();
                }
            }

            availabilityChanged = false;
        }

        ItemStack stack = parent.getSlotAt(index);
        long amount = parent.getAmountAt(index);
        if (hasTypeChanged(stack)) {
            clearViewedBy();
            if (stack.isEmpty()) {
                item = null;
                stackSize = 0L;
                damage = 0;
                tag = null;
            } else {
                item = stack.getItem();
                stackSize = amount;
                damage = stack.getDamageValue();
                tag = stack.hasTag() ? stack.getTag().copy() : null;
            }

            if (pneumaticNetwork != null) {
                for (PneumaticStackCache cache : pneumaticNetwork.getAccessors()) {
                    if (!cache.hasExpired() && parent.isAvailableToCache(cache)) {
                        cache.addToCache(this);
                    }
                }
            }
            return;
        }

        if (stackSize != amount) {
            long delta = amount - stackSize;
            for (PneumaticStackCache.CacheSlot slot : viewedBy) {
                slot.changeAmount(delta);
            }
            stackSize = amount;
        }
    }

    private boolean hasTypeChanged(ItemStack stack) {
        if (stack.isEmpty() || item == null) {
            return stack.isEmpty() != (item == null);
        }
        if (item != stack.getItem() || damage != stack.getDamageValue()) {
            return true;
        }
        if (tag == null && stack.hasTag()) {
            return true;
        }
        if (tag != null && !stack.hasTag()) {
            return true;
        }
        return tag != null && !tag.equals(stack.getTag());
    }

    private void clearViewedBy() {
        Iterator<PneumaticStackCache.CacheSlot> iterator = viewedBy.iterator();
        while (iterator.hasNext()) {
            PneumaticStackCache.CacheSlot slot = iterator.next();
            slot.removeMonitor(this);
            iterator.remove();
        }
    }
}
