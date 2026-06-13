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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class HbmFluidContainerRegistry {
    private static final Map<HbmFluidContainerRules.ContainerKind, RegistryObject<Item>> EMPTY_ITEMS =
            new IdentityHashMap<>();
    private static final Map<HbmFluidContainerRules.ContainerKind, RegistryObject<Item>> FULL_ITEMS =
            new IdentityHashMap<>();
    private static final List<ContainerEntry> DIRECT_ENTRIES = List.of(
            direct(ContainerSource.BUILTIN_FIXED, new ItemStack(Items.BUCKET), new ItemStack(Items.WATER_BUCKET), HbmFluids.WATER, 1000),
            direct(ContainerSource.BUILTIN_FIXED, new ItemStack(Items.BUCKET), new ItemStack(Items.LAVA_BUCKET), HbmFluids.LAVA, 1000),
            direct(ContainerSource.BUILTIN_FIXED, new ItemStack(Items.GLASS_BOTTLE), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), HbmFluids.WATER, 250),
            direct(ContainerSource.BUILTIN_FIXED, new ItemStack(Items.GLASS_BOTTLE), new ItemStack(Items.EXPERIENCE_BOTTLE), HbmFluids.XPJUICE, 100));
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

    public static List<ContainerEntry> getFixedContainersSnapshot() {
        return Collections.unmodifiableList(fixedEntries());
    }

    public static @Nullable ContainerEntry getContainer(FluidType type, ItemStack stack) {
        return inspectFullContainerFor(stack, type).matchedEntry();
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        return inspectFluidContent(stack, type).contentMb();
    }

    public static FluidType getFluidType(ItemStack stack) {
        return inspectFluidType(stack).matchedType();
    }

    public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
        return inspectFullContainerFor(stack, type).copyFullContainer();
    }

    public static ItemStack getEmptyContainer(ItemStack stack) {
        return inspectEmptyContainerFor(stack).copyEmptyContainer();
    }

    public static EmptyContainerLookupReport inspectFullContainerFor(ItemStack emptyStack, FluidType type) {
        ItemStack input = safeSingleCopy(emptyStack);
        FluidType requested = type == null ? HbmFluids.NONE : type;
        if (input.isEmpty() || requested == HbmFluids.NONE) {
            return new EmptyContainerLookupReport(requested, !input.isEmpty(), input, List.of(), -1, null);
        }
        List<ContainerEntry> candidates = getContainers(requested);
        for (int i = 0; i < candidates.size(); i++) {
            ContainerEntry entry = candidates.get(i);
            if (entry.matchesEmpty(input)) {
                return new EmptyContainerLookupReport(requested, true, input, candidates, i, entry);
            }
        }
        return new EmptyContainerLookupReport(requested, true, input, candidates, -1, null);
    }

    public static FluidContentLookupReport inspectFluidContent(ItemStack fullStack, FluidType type) {
        ItemStack input = safeSingleCopy(fullStack);
        FluidType requested = type == null ? HbmFluids.NONE : type;
        if (input.isEmpty() || requested == HbmFluids.NONE) {
            return new FluidContentLookupReport(requested, HbmFluids.NONE, !input.isEmpty(), input,
                    0, 0, null, null);
        }
        if (input.getItem() instanceof HbmFluidContainerItem container && !(container instanceof HbmInfiniteFluidItem)) {
            FluidType contained = container.getFirstFluidType(input);
            int fill = contained == requested ? container.getFill(input) : 0;
            return new FluidContentLookupReport(requested, contained, true, input, fill,
                    container.getPressure(input), ContainerSource.KIND_DYNAMIC, null);
        }
        for (ContainerEntry entry : fixedEntries()) {
            if (entry.type() == requested && entry.matchesFull(input)) {
                return new FluidContentLookupReport(requested, entry.type(), true, input,
                        entry.content(), 0, entry.source(), entry);
            }
        }
        return new FluidContentLookupReport(requested, HbmFluids.NONE, true, input, 0, 0, null, null);
    }

    public static FluidTypeLookupReport inspectFluidType(ItemStack fullStack) {
        ItemStack input = safeSingleCopy(fullStack);
        if (input.isEmpty()) {
            return new FluidTypeLookupReport(false, input, HbmFluids.NONE, 0, 0, null, null);
        }
        if (input.getItem() instanceof HbmFluidContainerItem container && !(container instanceof HbmInfiniteFluidItem)) {
            FluidType type = container.getFirstFluidType(input);
            int fill = container.getFill(input);
            return new FluidTypeLookupReport(true, input, fill > 0 ? type : HbmFluids.NONE,
                    fill, container.getPressure(input), ContainerSource.KIND_DYNAMIC, null);
        }
        for (ContainerEntry entry : fixedEntries()) {
            if (entry.matchesFull(input)) {
                return new FluidTypeLookupReport(true, input, entry.type(), entry.content(),
                        0, entry.source(), entry);
            }
        }
        return new FluidTypeLookupReport(true, input, HbmFluids.NONE, 0, 0, null, null);
    }

    public static FullContainerLookupReport inspectEmptyContainerFor(ItemStack fullStack) {
        ItemStack input = safeSingleCopy(fullStack);
        if (input.isEmpty()) {
            return new FullContainerLookupReport(false, input, HbmFluids.NONE, 0, 0, null, null, ItemStack.EMPTY);
        }
        if (input.getItem() instanceof HbmFluidContainerItem container && !(container instanceof HbmInfiniteFluidItem)) {
            FluidType type = container.getFirstFluidType(input);
            int fill = container.getFill(input);
            ItemStack empty = fill <= 0 ? ItemStack.EMPTY : emptyContainer(container.getContainerKind());
            return new FullContainerLookupReport(true, input, fill > 0 ? type : HbmFluids.NONE,
                    fill, container.getPressure(input), ContainerSource.KIND_DYNAMIC, null, empty);
        }
        for (ContainerEntry entry : fixedEntries()) {
            if (entry.matchesFull(input)) {
                return new FullContainerLookupReport(true, input, entry.type(), entry.content(),
                        0, entry.source(), entry, entry.copyEmptyContainer());
            }
        }
        return new FullContainerLookupReport(true, input, HbmFluids.NONE, 0, 0, null, null, ItemStack.EMPTY);
    }

    public static ItemStack getCraftingRemainder(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack fixedEmpty = getEmptyContainer(stack);
        if (!fixedEmpty.isEmpty()) {
            return fixedEmpty;
        }
        return drainForgeContainerCopy(stack);
    }

    public static List<ItemStack> getCraftingRemainders(ItemStack stack, int count) {
        if (stack.isEmpty() || count <= 0) {
            return List.of();
        }
        ItemStack empty = getCraftingRemainder(stack);
        if (empty.isEmpty()) {
            return List.of();
        }
        List<ItemStack> remainders = new ArrayList<>();
        int remaining = count;
        int maxStackSize = Math.max(1, empty.getMaxStackSize());
        while (remaining > 0) {
            ItemStack copy = empty.copy();
            copy.setCount(Math.min(remaining, maxStackSize));
            remainders.add(copy);
            remaining -= copy.getCount();
        }
        return Collections.unmodifiableList(remainders);
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
        return registerContainer(ContainerSource.EXTERNAL, fullContainer, emptyContainer, type, content);
    }

    static boolean registerConfiguredContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type, int content) {
        return registerContainer(ContainerSource.CONFIG, fullContainer, emptyContainer, type, content);
    }

    private static boolean registerContainer(ContainerSource source, ItemStack fullContainer, ItemStack emptyContainer,
            FluidType type, int content) {
        if (fullContainer == null || fullContainer.isEmpty()
                || type == null || type == HbmFluids.NONE
                || content <= 0) {
            lastSkippedContainers++;
            return false;
        }
        EXTERNAL_ENTRIES.add(direct(source, emptyContainer == null ? ItemStack.EMPTY : emptyContainer.copy(),
                fullContainer.copy(), type, content));
        lastRegisteredContainers++;
        return true;
    }

    public static void reloadExternalContainers(java.nio.file.Path configDir) {
        EXTERNAL_ENTRIES.clear();
        lastInvokedListeners = 0;
        lastRegisteredContainers = 0;
        lastSkippedContainers = 0;
        HbmFluidContainerConfig.initialize(configDir);
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

    public static void reloadExternalContainers() {
        reloadExternalContainers(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LISTENERS.size(), lastInvokedListeners, EXTERNAL_ENTRIES.size(),
                lastRegisteredContainers, lastSkippedContainers);
    }

    private static ContainerEntry entry(HbmFluidContainerRules.ContainerKind kind, FluidType type) {
        return new ContainerEntry(kind, ContainerSource.KIND_DYNAMIC, emptyContainer(kind), fullContainer(kind, type), type,
                HbmFluidContainerRules.capacity(kind));
    }

    private static ContainerEntry direct(ContainerSource source, ItemStack empty, ItemStack full, FluidType type, int content) {
        return new ContainerEntry(null, source, empty, full, type, content);
    }

    private static ItemStack drainForgeContainerCopy(ItemStack stack) {
        if (stack.getItem() instanceof HbmFluidContainerItem || stack.getCount() != 1) {
            return ItemStack.EMPTY;
        }
        ItemStack working = stack.copy();
        return working.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
                .map(handler -> drainForgeContainerCopy(working, handler))
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack drainForgeContainerCopy(ItemStack original, IFluidHandlerItem handler) {
        boolean drainedAny = false;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack contained = handler.getFluidInTank(tank);
            if (contained.isEmpty() || HbmFluidForgeMappings.fromForge(contained) == HbmFluids.NONE) {
                continue;
            }
            FluidStack drained = handler.drain(contained, IFluidHandler.FluidAction.EXECUTE);
            drainedAny |= !drained.isEmpty();
        }
        if (!drainedAny) {
            return ItemStack.EMPTY;
        }
        ItemStack container = handler.getContainer();
        return ItemStack.isSameItemSameTags(original, container) ? ItemStack.EMPTY : container.copy();
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
        entries.add(direct(ContainerSource.LEGACY_DIRECT, new ItemStack(empty.get()), new ItemStack(full.get()), type, content));
    }

    private static void addLegacyDirect(List<ContainerEntry> entries, Item emptyItem, String fullLegacyName,
            FluidType type, int content) {
        RegistryObject<Item> full = ModItems.legacyItem(fullLegacyName);
        if (full == null || !full.isPresent()) {
            return;
        }
        entries.add(direct(ContainerSource.LEGACY_DIRECT, new ItemStack(emptyItem), new ItemStack(full.get()), type, content));
    }

    private static void addLegacyDirectBlock(List<ContainerEntry> entries, String emptyLegacyName, String fullLegacyName,
            FluidType type, int content) {
        RegistryObject<Item> empty = ModItems.legacyItem(emptyLegacyName);
        RegistryObject<? extends Block> full = ModBlocks.legacyBlock(fullLegacyName);
        if (empty == null || full == null || !empty.isPresent() || !full.isPresent()
                || !(full.get().asItem() instanceof BlockItem item)) {
            return;
        }
        entries.add(direct(ContainerSource.LEGACY_DIRECT, new ItemStack(empty.get()), new ItemStack(item), type, content));
    }

    private static void addLegacyConsumable(List<ContainerEntry> entries, String fullLegacyName, FluidType type, int content) {
        RegistryObject<Item> full = ModItems.legacyItem(fullLegacyName);
        if (full == null || !full.isPresent()) {
            return;
        }
        entries.add(direct(ContainerSource.LEGACY_DIRECT, ItemStack.EMPTY, new ItemStack(full.get()), type, content));
    }

    private static void addLegacyConsumableBlock(List<ContainerEntry> entries, String fullLegacyName, FluidType type, int content) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(fullLegacyName);
        if (block == null || !block.isPresent() || !(block.get().asItem() instanceof BlockItem item)) {
            return;
        }
        entries.add(direct(ContainerSource.LEGACY_DIRECT, ItemStack.EMPTY, new ItemStack(item), type, content));
    }

    private static void register(HbmFluidContainerRules.ContainerKind kind, RegistryObject<Item> empty, RegistryObject<Item> full) {
        EMPTY_ITEMS.put(kind, empty);
        FULL_ITEMS.put(kind, full);
    }

    private static ItemStack safeSingleCopy(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    public record EmptyContainerLookupReport(
            FluidType requestedType,
            boolean stackPresent,
            ItemStack inputStack,
            List<ContainerEntry> candidates,
            int matchedIndex,
            @Nullable ContainerEntry matchedEntry) {
        public EmptyContainerLookupReport {
            requestedType = requestedType == null ? HbmFluids.NONE : requestedType;
            inputStack = inputStack == null ? ItemStack.EMPTY : inputStack.copy();
            candidates = candidates == null ? List.of() : List.copyOf(candidates);
        }

        public boolean matched() {
            return matchedEntry != null;
        }

        public ItemStack copyFullContainer() {
            return matchedEntry == null ? ItemStack.EMPTY : matchedEntry.copyFullContainer();
        }
    }

    public record FluidContentLookupReport(
            FluidType requestedType,
            FluidType matchedType,
            boolean stackPresent,
            ItemStack inputStack,
            int contentMb,
            int pressure,
            @Nullable ContainerSource source,
            @Nullable ContainerEntry matchedEntry) {
        public FluidContentLookupReport {
            requestedType = requestedType == null ? HbmFluids.NONE : requestedType;
            matchedType = matchedType == null ? HbmFluids.NONE : matchedType;
            inputStack = inputStack == null ? ItemStack.EMPTY : inputStack.copy();
            contentMb = Math.max(0, contentMb);
            pressure = HbmFluidTank.clampPressure(pressure);
        }

        public boolean matched() {
            return matchedType != HbmFluids.NONE && contentMb > 0;
        }
    }

    public record FluidTypeLookupReport(
            boolean stackPresent,
            ItemStack inputStack,
            FluidType matchedType,
            int contentMb,
            int pressure,
            @Nullable ContainerSource source,
            @Nullable ContainerEntry matchedEntry) {
        public FluidTypeLookupReport {
            inputStack = inputStack == null ? ItemStack.EMPTY : inputStack.copy();
            matchedType = matchedType == null ? HbmFluids.NONE : matchedType;
            contentMb = Math.max(0, contentMb);
            pressure = HbmFluidTank.clampPressure(pressure);
        }

        public boolean matched() {
            return matchedType != HbmFluids.NONE && contentMb > 0;
        }
    }

    public record FullContainerLookupReport(
            boolean stackPresent,
            ItemStack inputStack,
            FluidType matchedType,
            int contentMb,
            int pressure,
            @Nullable ContainerSource source,
            @Nullable ContainerEntry matchedEntry,
            ItemStack emptyContainer) {
        public FullContainerLookupReport {
            inputStack = inputStack == null ? ItemStack.EMPTY : inputStack.copy();
            matchedType = matchedType == null ? HbmFluids.NONE : matchedType;
            contentMb = Math.max(0, contentMb);
            pressure = HbmFluidTank.clampPressure(pressure);
            emptyContainer = emptyContainer == null ? ItemStack.EMPTY : emptyContainer.copy();
        }

        public boolean matched() {
            return matchedType != HbmFluids.NONE && contentMb > 0;
        }

        public ItemStack copyEmptyContainer() {
            return emptyContainer.copy();
        }
    }

    public record ContainerEntry(
            @Nullable HbmFluidContainerRules.ContainerKind kind,
            ContainerSource source,
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

    public enum ContainerSource {
        BUILTIN_FIXED,
        LEGACY_DIRECT,
        CONFIG,
        EXTERNAL,
        KIND_DYNAMIC
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
