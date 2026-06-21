package com.hbm.inventory;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy package facade backed by the modern HBM fluid container registry.
 */
@Deprecated(forRemoval = false)
public final class FluidContainerRegistry {
    public static final List<FluidContainer> allContainers = new ArrayList<>();
    private static final Map<FluidType, List<FluidContainer>> containerMap = new IdentityHashMap<>();

    static {
        refreshSnapshot();
    }

    public static void clearRegistry() {
        allContainers.clear();
        containerMap.clear();
    }

    public static void register() {
        refreshSnapshot();
    }

    public static void registerContainer(FluidContainer container) {
        if (container == null || container.fullContainer == null || container.fullContainer.isEmpty()
                || container.type == null || container.type == HbmFluids.NONE || container.content <= 0) {
            return;
        }
        ItemStack empty = container.emptyContainer == null || container.emptyContainer.isEmpty()
                ? ItemStack.EMPTY
                : container.emptyContainer.copy();
        HbmFluidContainerRegistry.registerContainer(container.fullContainer.copy(), empty, container.type,
                container.content);
        refreshSnapshot();
    }

    public static List<FluidContainer> getContainers(FluidType type) {
        refreshSnapshotIfEmpty();
        List<FluidContainer> containers = containerMap.get(type);
        return containers == null || containers.isEmpty() ? null : Collections.unmodifiableList(containers);
    }

    public static FluidContainer getContainer(FluidType type, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        HbmFluidContainerRegistry.ContainerEntry entry = HbmFluidContainerRegistry.getContainer(type, single(stack));
        return entry == null ? null : fromEntry(entry);
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        return HbmFluidContainerRegistry.getFluidContent(single(stack), type);
    }

    public static FluidType getFluidType(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return HbmFluids.NONE;
        }
        FluidType type = HbmFluidContainerRegistry.getFluidType(single(stack));
        return type == null ? HbmFluids.NONE : type;
    }

    public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ItemStack result = HbmFluidContainerRegistry.getFullContainer(single(stack), type);
        return result.isEmpty() ? null : result;
    }

    public static ItemStack getEmptyContainer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ItemStack result = HbmFluidContainerRegistry.getEmptyContainer(single(stack));
        return result.isEmpty() ? null : result;
    }

    private static void refreshSnapshotIfEmpty() {
        if (allContainers.isEmpty()) {
            refreshSnapshot();
        }
    }

    private static void refreshSnapshot() {
        allContainers.clear();
        containerMap.clear();
        for (HbmFluidContainerRegistry.ContainerEntry entry : HbmFluidContainerRegistry.getAllContainers()) {
            FluidContainer container = fromEntry(entry);
            allContainers.add(container);
            containerMap.computeIfAbsent(container.type, ignored -> new ArrayList<>()).add(container);
        }
    }

    private static FluidContainer fromEntry(HbmFluidContainerRegistry.ContainerEntry entry) {
        return new FluidContainer(entry.copyFullContainer(), entry.copyEmptyContainer(), entry.type(),
                entry.content());
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private FluidContainerRegistry() {
    }
}
