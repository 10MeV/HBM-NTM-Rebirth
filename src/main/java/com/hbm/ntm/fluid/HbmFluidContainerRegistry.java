package com.hbm.ntm.fluid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class HbmFluidContainerRegistry {
    private static final Map<HbmFluidContainerRules.ContainerKind, RegistryObject<Item>> EMPTY_ITEMS =
            new IdentityHashMap<>();
    private static final Map<HbmFluidContainerRules.ContainerKind, RegistryObject<Item>> FULL_ITEMS =
            new IdentityHashMap<>();
    private static final List<ContainerEntry> DIRECT_ENTRIES = List.of(
            direct(new ItemStack(Items.BUCKET), new ItemStack(Items.WATER_BUCKET), HbmFluids.WATER, 1000),
            direct(new ItemStack(Items.BUCKET), new ItemStack(Items.LAVA_BUCKET), HbmFluids.LAVA, 1000),
            direct(new ItemStack(Items.GLASS_BOTTLE), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), HbmFluids.WATER, 250),
            direct(new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), HbmFluids.XPJUICE, 100));
    private static final List<ContainerEntry> EXTERNAL_ENTRIES = new CopyOnWriteArrayList<>();
    private static final List<HbmFluidContainerRegisterListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile int lastInvokedListeners;
    private static volatile int lastRegisteredContainers;
    private static volatile int lastSkippedContainers;

    static {
        register(HbmFluidContainerRules.ContainerKind.CANISTER, ModItems.CANISTER_EMPTY, ModItems.CANISTER_FULL);
        register(HbmFluidContainerRules.ContainerKind.GAS_TANK, ModItems.GAS_EMPTY, ModItems.GAS_FULL);
        register(HbmFluidContainerRules.ContainerKind.FLUID_TANK, ModItems.FLUID_TANK_EMPTY, ModItems.FLUID_TANK_FULL);
        register(HbmFluidContainerRules.ContainerKind.LEAD_FLUID_TANK, ModItems.FLUID_TANK_LEAD_EMPTY, ModItems.FLUID_TANK_LEAD_FULL);
        register(HbmFluidContainerRules.ContainerKind.FLUID_BARREL, ModItems.FLUID_BARREL_EMPTY, ModItems.FLUID_BARREL_FULL);
        register(HbmFluidContainerRules.ContainerKind.FLUID_PACK, ModItems.FLUID_PACK_EMPTY, ModItems.FLUID_PACK_FULL);
        register(HbmFluidContainerRules.ContainerKind.DISPERSER_CANISTER, ModItems.DISPERSER_CANISTER_EMPTY, ModItems.DISPERSER_CANISTER);
        register(HbmFluidContainerRules.ContainerKind.GLYPHID_GLAND, ModItems.GLYPHID_GLAND_EMPTY, ModItems.GLYPHID_GLAND);
    }

    public static List<ContainerEntry> getContainers(FluidType type) {
        if (type == null || type == HbmFluids.NONE) {
            return List.of();
        }
        List<ContainerEntry> result = new ArrayList<>();
        for (ContainerEntry entry : fixedEntries()) {
            if (entry.type() == type) {
                result.add(entry);
            }
        }
        for (HbmFluidContainerRules.ContainerKind kind : HbmFluidContainerRules.ContainerKind.values()) {
            if (HbmFluidContainerRules.accepts(kind, type)) {
                result.add(entry(kind, type));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public static List<ContainerEntry> getAllContainers() {
        List<ContainerEntry> result = new ArrayList<>();
        for (FluidType type : HbmFluids.all()) {
            result.addAll(getContainers(type));
        }
        return Collections.unmodifiableList(result);
    }

    public static @Nullable ContainerEntry getContainer(FluidType type, ItemStack stack) {
        if (stack.isEmpty() || type == null || type == HbmFluids.NONE) {
            return null;
        }
        for (ContainerEntry entry : getContainers(type)) {
            if (entry.matchesEmpty(stack)) {
                return entry;
            }
        }
        return null;
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        if (stack.isEmpty() || type == null || type == HbmFluids.NONE) {
            return 0;
        }
        if (stack.getItem() instanceof HbmFluidContainerItem container && !(container instanceof HbmInfiniteFluidItem)) {
            return container.getFirstFluidType(stack) == type ? container.getFill(stack) : 0;
        }
        for (ContainerEntry entry : fixedEntries()) {
            if (entry.type() == type && entry.matchesFull(stack)) {
                return entry.content();
            }
        }
        return 0;
    }

    public static FluidType getFluidType(ItemStack stack) {
        if (stack.isEmpty()) {
            return HbmFluids.NONE;
        }
        if (stack.getItem() instanceof HbmFluidContainerItem container && !(container instanceof HbmInfiniteFluidItem)) {
            FluidType type = container.getFirstFluidType(stack);
            return container.getFill(stack) > 0 ? type : HbmFluids.NONE;
        }
        for (ContainerEntry entry : fixedEntries()) {
            if (entry.matchesFull(stack)) {
                return entry.type();
            }
        }
        return HbmFluids.NONE;
    }

    public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
        ContainerEntry entry = getContainer(type, stack);
        return entry == null ? ItemStack.EMPTY : entry.copyFullContainer();
    }

    public static ItemStack getEmptyContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!(stack.getItem() instanceof HbmFluidContainerItem container) || container instanceof HbmInfiniteFluidItem) {
            for (ContainerEntry entry : fixedEntries()) {
                if (entry.matchesFull(stack)) {
                    return entry.copyEmptyContainer();
                }
            }
            return ItemStack.EMPTY;
        }
        if (container.getFill(stack) <= 0) {
            return ItemStack.EMPTY;
        }
        return emptyContainer(container.getContainerKind());
    }

    public static ItemStack emptyContainer(HbmFluidContainerRules.ContainerKind kind) {
        RegistryObject<Item> item = EMPTY_ITEMS.get(kind);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    public static ItemStack fullContainer(HbmFluidContainerRules.ContainerKind kind, FluidType type) {
        RegistryObject<Item> item = FULL_ITEMS.get(kind);
        if (item == null || !(item.get() instanceof HbmFluidContainerItem container)) {
            return ItemStack.EMPTY;
        }
        return container.createFilledStack(type, HbmFluidContainerRules.capacity(kind), 0);
    }

    public static void registerFluidContainerRegisterListener(HbmFluidContainerRegisterListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static boolean registerContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type, int content) {
        if (fullContainer == null || fullContainer.isEmpty()
                || type == null || type == HbmFluids.NONE
                || content <= 0) {
            lastSkippedContainers++;
            return false;
        }
        EXTERNAL_ENTRIES.add(direct(emptyContainer == null ? ItemStack.EMPTY : emptyContainer.copy(),
                fullContainer.copy(), type, content));
        lastRegisteredContainers++;
        return true;
    }

    static void reloadExternalContainers() {
        EXTERNAL_ENTRIES.clear();
        lastInvokedListeners = 0;
        lastRegisteredContainers = 0;
        lastSkippedContainers = 0;
        for (HbmFluidContainerRegisterListener listener : LISTENERS) {
            try {
                listener.onFluidContainersLoad();
                lastInvokedListeners++;
            } catch (RuntimeException ex) {
                lastSkippedContainers++;
                HbmNtm.LOGGER.warn("HBM fluid container register listener failed.", ex);
            }
        }
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LISTENERS.size(), lastInvokedListeners, EXTERNAL_ENTRIES.size(),
                lastRegisteredContainers, lastSkippedContainers);
    }

    private static ContainerEntry entry(HbmFluidContainerRules.ContainerKind kind, FluidType type) {
        return new ContainerEntry(kind, emptyContainer(kind), fullContainer(kind, type), type, HbmFluidContainerRules.capacity(kind));
    }

    private static ContainerEntry direct(ItemStack empty, ItemStack full, FluidType type, int content) {
        return new ContainerEntry(null, empty, full, type, content);
    }

    private static List<ContainerEntry> directEntries() {
        List<ContainerEntry> entries = new ArrayList<>(DIRECT_ENTRIES);
        addLegacyDirect(entries, "cell_empty", "cell_deuterium", HbmFluids.DEUTERIUM, 1000);
        addLegacyDirect(entries, "cell_empty", "cell_tritium", HbmFluids.TRITIUM, 1000);
        addLegacyDirect(entries, "cell_empty", "cell_uf6", HbmFluids.UF6, 1000);
        addLegacyDirect(entries, "cell_empty", "cell_puf6", HbmFluids.PUF6, 1000);
        addLegacyDirect(entries, "cell_empty", "cell_antimatter", HbmFluids.AMAT, 1000);
        addLegacyDirect(entries, "cell_empty", "cell_anti_schrabidium", HbmFluids.ASCHRAB, 1000);
        addLegacyDirect(entries, "cell_empty", "cell_sas3", HbmFluids.SAS3, 1000);
        addLegacyDirect(entries, "rod_zirnox_empty", "rod_zirnox_tritium", HbmFluids.TRITIUM, 2000);
        addLegacyDirect(entries, "particle_empty", "particle_hydrogen", HbmFluids.HYDROGEN, 1000);
        addLegacyDirect(entries, "particle_empty", "particle_amat", HbmFluids.AMAT, 1000);
        addLegacyDirect(entries, "particle_empty", "particle_aschrab", HbmFluids.ASCHRAB, 1000);
        addLegacyDirect(entries, "iv_empty", "iv_blood", HbmFluids.BLOOD, 100);
        addLegacyDirect(entries, "iv_xp_empty", "iv_xp", HbmFluids.XPJUICE, 100);
        addLegacyDirect(entries, "can_empty", "can_mug", HbmFluids.MUG, 100);
        addLegacyDirectBlock(entries, "tank_steel", "red_barrel", HbmFluids.DIESEL, 10000);
        addLegacyDirectBlock(entries, "tank_steel", "pink_barrel", HbmFluids.KEROSENE, 10000);
        addLegacyDirectBlock(entries, "tank_steel", "lox_barrel", HbmFluids.OXYGEN, 10000);
        addLegacyDirect(entries, Items.BUCKET, "bucket_mud", HbmFluids.WATZ, 1000);
        addLegacyDirect(entries, Items.BUCKET, "bucket_schrabidic_acid", HbmFluids.SCHRABIDIC, 1000);
        addLegacyDirect(entries, Items.BUCKET, "bucket_sulfuric_acid", HbmFluids.SULFURIC_ACID, 1000);
        addLegacyDirect(entries, Items.GLASS_BOTTLE, "bottle_mercury", HbmFluids.MERCURY, 1000);
        addLegacyConsumable(entries, "ingot_mercury", HbmFluids.MERCURY, 125);
        addLegacyConsumableBlock(entries, "ore_oil", HbmFluids.OIL, 250);
        addLegacyConsumableBlock(entries, "ore_gneiss_gas", HbmFluids.PETROLEUM, 250);
        return entries;
    }

    private static List<ContainerEntry> fixedEntries() {
        List<ContainerEntry> entries = directEntries();
        entries.addAll(EXTERNAL_ENTRIES);
        return entries;
    }

    private static void addLegacyDirect(List<ContainerEntry> entries, String emptyLegacyName, String fullLegacyName,
            FluidType type, int content) {
        RegistryObject<Item> empty = ModItems.legacyItem(emptyLegacyName);
        RegistryObject<Item> full = ModItems.legacyItem(fullLegacyName);
        if (empty == null || full == null || !empty.isPresent() || !full.isPresent()) {
            return;
        }
        entries.add(direct(new ItemStack(empty.get()), new ItemStack(full.get()), type, content));
    }

    private static void addLegacyDirect(List<ContainerEntry> entries, Item emptyItem, String fullLegacyName,
            FluidType type, int content) {
        RegistryObject<Item> full = ModItems.legacyItem(fullLegacyName);
        if (full == null || !full.isPresent()) {
            return;
        }
        entries.add(direct(new ItemStack(emptyItem), new ItemStack(full.get()), type, content));
    }

    private static void addLegacyDirectBlock(List<ContainerEntry> entries, String emptyLegacyName, String fullLegacyName,
            FluidType type, int content) {
        RegistryObject<Item> empty = ModItems.legacyItem(emptyLegacyName);
        RegistryObject<? extends Block> full = ModBlocks.legacyBlock(fullLegacyName);
        if (empty == null || full == null || !empty.isPresent() || !full.isPresent()
                || !(full.get().asItem() instanceof BlockItem item)) {
            return;
        }
        entries.add(direct(new ItemStack(empty.get()), new ItemStack(item), type, content));
    }

    private static void addLegacyConsumable(List<ContainerEntry> entries, String fullLegacyName, FluidType type, int content) {
        RegistryObject<Item> full = ModItems.legacyItem(fullLegacyName);
        if (full == null || !full.isPresent()) {
            return;
        }
        entries.add(direct(ItemStack.EMPTY, new ItemStack(full.get()), type, content));
    }

    private static void addLegacyConsumableBlock(List<ContainerEntry> entries, String fullLegacyName, FluidType type, int content) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(fullLegacyName);
        if (block == null || !block.isPresent() || !(block.get().asItem() instanceof BlockItem item)) {
            return;
        }
        entries.add(direct(ItemStack.EMPTY, new ItemStack(item), type, content));
    }

    private static void register(HbmFluidContainerRules.ContainerKind kind, RegistryObject<Item> empty, RegistryObject<Item> full) {
        EMPTY_ITEMS.put(kind, empty);
        FULL_ITEMS.put(kind, full);
    }

    public record ContainerEntry(
            @Nullable HbmFluidContainerRules.ContainerKind kind,
            ItemStack emptyContainer,
            ItemStack fullContainer,
            FluidType type,
            int content) {
        public boolean matchesEmpty(ItemStack stack) {
            return !emptyContainer.isEmpty() && ItemStack.isSameItem(emptyContainer, stack);
        }

        public boolean matchesFull(ItemStack stack) {
            if (fullContainer.isEmpty() || !ItemStack.isSameItem(fullContainer, stack)) {
                return false;
            }
            if (fullContainer.is(Items.POTION)) {
                return PotionUtils.getPotion(fullContainer) == PotionUtils.getPotion(stack);
            }
            return true;
        }

        public ItemStack copyEmptyContainer() {
            return emptyContainer.copy();
        }

        public ItemStack copyFullContainer() {
            return fullContainer.copy();
        }

        public boolean supportsItemTag() {
            return kind == null && !fullContainer.isEmpty() && !fullContainer.hasTag();
        }

        public String legacyOreDictionaryName() {
            return legacyOreDictionaryName("ntmcontainer");
        }

        public String legacyCompatOreDictionaryName() {
            return legacyOreDictionaryName("container");
        }

        private String legacyOreDictionaryName(String prefix) {
            return prefix + content + type.getName().replace("_", "").toLowerCase(java.util.Locale.US);
        }
    }

    public record Diagnostics(int listeners, int lastInvokedListeners, int externalContainers,
            int lastRegisteredContainers, int lastSkippedContainers) {
        public String summary() {
            return "fluid containers listeners=" + listeners + " lastInvoked=" + lastInvokedListeners
                    + " externalContainers=" + externalContainers + " lastRegistered=" + lastRegisteredContainers
                    + " lastSkipped=" + lastSkippedContainers;
        }
    }

    private HbmFluidContainerRegistry() {
    }
}
