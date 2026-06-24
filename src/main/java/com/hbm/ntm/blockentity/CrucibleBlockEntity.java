package com.hbm.ntm.blockentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.menu.CrucibleMenu;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.util.CrucibleUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrucibleBlockEntity extends BlockEntity
        implements MenuProvider, ICrucibleAcceptor, LegacyLookOverlayProvider, HbmTileSyncable {
    public static final int SLOT_INPUT_START = 1;
    public static final int SLOT_INPUT_END = 10;
    public static final int SLOT_COUNT = 10;
    public static final int RECIPE_CAPACITY = MaterialShapes.BLOCK.q(16);
    public static final int WASTE_CAPACITY = MaterialShapes.BLOCK.q(16);
    public static final int PROCESS_TIME = 20_000;
    public static final double DIFFUSION = 0.25D;
    public static final int MAX_HEAT = 100_000;

    private static final String TAG_ITEMS = HbmInventoryMenuHelper.LEGACY_ITEMS_TAG;
    private static final String TAG_MODERN_ITEMS = "Items";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_REC = "rec";
    private static final String TAG_WAS = "was";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_HEAT = "heat";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= SLOT_INPUT_START && slot < SLOT_INPUT_END && isItemSmeltable(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final List<MaterialStack> recipeStack = new ArrayList<>();
    private final List<MaterialStack> wasteStack = new ArrayList<>();

    private String recipe = "null";
    private int progress;
    private int heat;
    @Nullable
    private String customName;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = crucible.tickServer(level, pos, state);
        if (changed || level.getGameTime() % 20L == 0L) {
            crucible.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        if (!level.isClientSide || crucible.getTotalMaterialAmount() <= 0 || level.getGameTime() % 10L != 0L) {
            return;
        }
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                (level.random.nextDouble() - 0.5D) * 0.02D, 0.08D, (level.random.nextDouble() - 0.5D) * 0.02D);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getHeat() {
        return heat;
    }

    public int getRecipeAmount() {
        return totalAmount(recipeStack);
    }

    public List<MaterialStack> getRecipeStacks() {
        return List.copyOf(recipeStack);
    }

    public int getWasteAmount() {
        return totalAmount(wasteStack);
    }

    public List<MaterialStack> getWasteStacks() {
        return List.copyOf(wasteStack);
    }

    public String getSelectedRecipeName() {
        return recipe;
    }

    @Nullable
    public CrucibleRecipeRuntime.Recipe getSelectedRecipeDefinition() {
        return CrucibleRecipeRuntime.find(recipe);
    }

    public boolean selectRecipe(String selectedRecipe) {
        if (!CrucibleRecipeRuntime.canSelect(selectedRecipe)) {
            return false;
        }
        recipe = CrucibleRecipeRuntime.normalize(selectedRecipe);
        progress = 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    public int getRecipeColor() {
        return firstColor(recipeStack);
    }

    public int getWasteColor() {
        return firstColor(wasteStack);
    }

    public int getTotalMaterialAmount() {
        return getRecipeAmount() + getWasteAmount();
    }

    public float getMoltenLevel() {
        return Math.min(0.875F, getTotalMaterialAmount() * 0.875F / (RECIPE_CAPACITY + WASTE_CAPACITY));
    }

    public List<Component> recipeTooltip(boolean showMb) {
        return materialTooltip(recipeStack, showMb);
    }

    public List<Component> wasteTooltip(boolean showMb) {
        return materialTooltip(wasteStack, showMb);
    }

    public List<ItemStack> drainAllAsScraps() {
        return drainMaterialStacks();
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>(HbmInventoryMenuHelper.clearToDrops(items));
        drops.addAll(drainMaterialStacks());
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.machineCrucible", "Crucible");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrucibleMenu(containerId, inventory, this);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal("Heat: " + heat + " / " + MAX_HEAT + "TU"),
                Component.literal("Selected: " + recipe),
                Component.literal("Recipe: " + Mats.formatAmount(getRecipeAmount(), false)),
                Component.literal("Waste: " + Mats.formatAmount(getWasteAmount(), false))));
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        if (stack == null || stack.material == null) {
            return false;
        }
        CrucibleRecipeRuntime.Recipe loadedRecipe = getSelectedRecipeDefinition();
        if (loadedRecipe != null) {
            int required = CrucibleRecipeRuntime.recipeInputAmount(loadedRecipe, stack.material);
            if (required <= 0) {
                return false;
            }
            int recipeContent = CrucibleRecipeRuntime.recipeInputAmount(loadedRecipe);
            int materialMaximum = required * RECIPE_CAPACITY / Math.max(1, recipeContent);
            return amountOf(recipeStack, stack.material) < materialMaximum
                    && getRecipeAmount() < RECIPE_CAPACITY;
        }
        return getWasteAmount() < WASTE_CAPACITY;
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        if (!canAcceptPartialPour(level, pos, hit, side, stack)) {
            return stack;
        }
        CrucibleRecipeRuntime.Recipe loadedRecipe = getSelectedRecipeDefinition();
        List<MaterialStack> target = loadedRecipe == null ? wasteStack : recipeStack;
        int capacity = loadedRecipe == null ? WASTE_CAPACITY : RECIPE_CAPACITY;
        int free = loadedRecipe == null
                ? Math.max(0, capacity - totalAmount(target))
                : recipeFreeForInput(loadedRecipe, stack.material);
        int accepted = Math.min(free, stack.amount);
        if (accepted <= 0) {
            return stack;
        }
        addToStack(target, new MaterialStack(stack.material, accepted));
        setChanged();
        return accepted >= stack.amount ? null : new MaterialStack(stack.material, stack.amount - accepted);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return false;
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        tag.putString(TAG_RECIPE, recipe);
        tag.put(TAG_REC, Mats.writeList(recipeStack));
        tag.put(TAG_WAS, Mats.writeList(wasteStack));
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_HEAT, heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        recipe = CrucibleRecipeRuntime.normalize(tag.contains(TAG_RECIPE) ? tag.getString(TAG_RECIPE) : "null");
        if (!CrucibleRecipeRuntime.canSelect(recipe)) {
            recipe = CrucibleRecipeRuntime.NULL_RECIPE;
        }
        recipeStack.clear();
        recipeStack.addAll(Mats.readList(tag.getList(TAG_REC, net.minecraft.nbt.Tag.TAG_COMPOUND)));
        wasteStack.clear();
        wasteStack.addAll(Mats.readList(tag.getList(TAG_WAS, net.minecraft.nbt.Tag.TAG_COMPOUND)));
        progress = tag.getInt(TAG_PROGRESS);
        heat = tag.getInt(TAG_HEAT);
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        if (tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
            return;
        }
        if (tag.contains(TAG_MODERN_ITEMS, Tag.TAG_COMPOUND)) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
            ItemStackHandler oldModernItems = new ItemStackHandler(SLOT_INPUT_END - SLOT_INPUT_START);
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_ITEMS, oldModernItems);
            for (int slot = 0; slot < oldModernItems.getSlots(); slot++) {
                items.setStackInSlot(SLOT_INPUT_START + slot, oldModernItems.getStackInSlot(slot));
            }
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 3, 3));
    }

    private boolean tickServer(Level level, BlockPos pos, BlockState state) {
        int oldHeat = heat;
        int oldProgress = progress;
        int oldRecipe = getRecipeAmount();
        int oldWaste = getWasteAmount();
        pullHeat(level, pos);
        collectItems(level, pos);
        burnLivingEntities(level, pos);
        if (!trySmelt()) {
            progress = 0;
        }
        tryRecipe(level);
        pourStacks(level, pos, state);
        cleanup(recipeStack);
        cleanup(wasteStack);
        return oldHeat != heat
                || oldProgress != progress
                || oldRecipe != getRecipeAmount()
                || oldWaste != getWasteAmount();
    }

    private void pullHeat(Level level, BlockPos pos) {
        if (heat >= MAX_HEAT) {
            return;
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below instanceof HeatSource source) {
            int diff = source.getHeatStored() - heat;
            diff = Math.min(diff, MAX_HEAT - heat);
            if (diff > 0) {
                int pulled = (int) Math.ceil(diff * DIFFUSION);
                source.useUpHeat(pulled);
                heat = Math.min(MAX_HEAT, heat + pulled);
                return;
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    private void collectItems(Level level, BlockPos pos) {
        if (level.getGameTime() % 5L != 0L) {
            return;
        }
        AABB area = new AABB(pos.getX() - 0.5D, pos.getY() + 0.5D, pos.getZ() - 0.5D,
                pos.getX() + 1.5D, pos.getY() + 1.0D, pos.getZ() + 1.5D);
        for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, area)) {
            if (!entity.isAlive()) {
                continue;
            }
            ItemStack stack = entity.getItem();
            if (!isItemSmeltable(stack)) {
                continue;
            }
            for (int slot = SLOT_INPUT_START; slot < SLOT_INPUT_END; slot++) {
                if (items.getStackInSlot(slot).isEmpty()) {
                    ItemStack one = stack.copyWithCount(1);
                    items.setStackInSlot(slot, one);
                    stack.shrink(1);
                    entity.setPickUpDelay(60);
                    if (stack.isEmpty()) {
                        entity.discard();
                    }
                    break;
                }
            }
        }
    }

    private void burnLivingEntities(Level level, BlockPos pos) {
        double molten = getMoltenLevel();
        if (molten <= 0.0D) {
            return;
        }
        AABB area = new AABB(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                pos.getX() + 0.5D, pos.getY() + 0.5D + molten, pos.getZ() + 0.5D).inflate(1.0D, 0.0D, 1.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            entity.hurt(level.damageSources().lava(), 5.0F);
            entity.setSecondsOnFire(5);
        }
    }

    private boolean trySmelt() {
        if (heat < MAX_HEAT / 2) {
            return false;
        }
        int slot = firstSmeltableSlot();
        if (slot < 0) {
            return false;
        }
        int delta = (int) ((heat - (MAX_HEAT / 2)) * 0.05D);
        progress += delta;
        heat -= delta;
        if (progress >= PROCESS_TIME) {
            progress = 0;
            for (MaterialStack material : Mats.getSmeltingMaterialsFromItem(items.getStackInSlot(slot))) {
                addMaterial(material);
            }
            items.extractItem(slot, 1, false);
        }
        return true;
    }

    private void tryRecipe(Level level) {
        CrucibleRecipeRuntime.Recipe loadedRecipe = getSelectedRecipeDefinition();
        if (loadedRecipe == null || level.getGameTime() % loadedRecipe.frequency() > 0) {
            return;
        }
        if (CrucibleRecipeRuntime.process(loadedRecipe, recipeStack, RECIPE_CAPACITY)) {
            setChanged();
        }
    }

    private void pourStacks(Level level, BlockPos pos, BlockState state) {
        Direction facing = facing(state);
        if (!wasteStack.isEmpty()) {
            pourStackList(level, pos, facing.getOpposite(), wasteStack);
        }
        if (!recipeStack.isEmpty()) {
            CrucibleRecipeRuntime.Recipe loadedRecipe = getSelectedRecipeDefinition();
            if (loadedRecipe == null) {
                pourStackList(level, pos, facing, recipeStack);
            } else {
                List<MaterialStack> outputStacks = recipeOutputStacks(loadedRecipe);
                if (!outputStacks.isEmpty()) {
                    pourStackList(level, pos, facing, outputStacks);
                }
            }
        }
    }

    private void pourStackList(Level level, BlockPos pos, Direction direction, List<MaterialStack> stacks) {
        CrucibleUtil.ImpactHolder impact = new CrucibleUtil.ImpactHolder();
        CrucibleUtil.PourResult result = CrucibleUtil.pourFullStackDetailed(level,
                pos.getX() + 0.5D + direction.getStepX() * 1.875D,
                pos.getY() + 0.25D,
                pos.getZ() + 0.5D + direction.getStepZ() * 1.875D,
                6.0D, true, stacks, MaterialShapes.NUGGET.q(3), impact);
        if (result.moved() != null && result.impact() != null) {
            com.hbm.ntm.particle.ParticleUtil.spawnFoundryPour(level, result.impact(),
                    result.moved().material.moltenColor, direction,
                    (float) result.impact().distanceTo(new Vec3(
                            pos.getX() + 0.5D + direction.getStepX() * 1.875D,
                            pos.getY() + 0.25D,
                            pos.getZ() + 0.5D + direction.getStepZ() * 1.875D)));
        }
    }

    private int firstSmeltableSlot() {
        for (int slot = SLOT_INPUT_START; slot < SLOT_INPUT_END; slot++) {
            if (isItemSmeltable(items.getStackInSlot(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private boolean isItemSmeltable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(stack);
        if (materials.isEmpty()) {
            return false;
        }
        CrucibleRecipeRuntime.Recipe loadedRecipe = getSelectedRecipeDefinition();
        if (loadedRecipe == null) {
            return getWasteAmount() + totalAmount(materials) <= WASTE_CAPACITY;
        }
        boolean matchesRecipe = false;
        int recipeAmount = getRecipeAmount();
        int wasteAmount = getWasteAmount();
        for (MaterialStack material : materials) {
            if (CrucibleRecipeRuntime.canFitRecipeMaterial(loadedRecipe, recipeStack, material, RECIPE_CAPACITY)) {
                matchesRecipe = true;
                recipeAmount += material.amount;
            } else {
                wasteAmount += material.amount;
            }
        }
        return recipeAmount <= RECIPE_CAPACITY && wasteAmount <= WASTE_CAPACITY && matchesRecipe;
    }

    private List<ItemStack> drainMaterialStacks() {
        List<ItemStack> drops = new ArrayList<>();
        for (MaterialStack stack : recipeStack) {
            ItemStack scrap = FoundryScrapsItem.create(stack.copy());
            if (!scrap.isEmpty()) {
                drops.add(scrap);
            }
        }
        for (MaterialStack stack : wasteStack) {
            ItemStack scrap = FoundryScrapsItem.create(stack.copy());
            if (!scrap.isEmpty()) {
                drops.add(scrap);
            }
        }
        recipeStack.clear();
        wasteStack.clear();
        setChanged();
        return drops;
    }

    private static List<Component> materialTooltip(List<MaterialStack> stacks, boolean showMb) {
        if (stacks.isEmpty()) {
            return List.of(Component.literal("Empty"));
        }
        List<Component> lines = new ArrayList<>();
        for (MaterialStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                lines.add(Component.translatable(stack.material.getUnlocalizedName())
                        .append(": " + Mats.formatAmount(stack.amount, showMb)));
            }
        }
        return lines;
    }

    private static void addToStack(List<MaterialStack> stacks, MaterialStack material) {
        if (material == null || material.isEmpty()) {
            return;
        }
        for (MaterialStack stack : stacks) {
            if (stack.material == material.material) {
                stack.amount += material.amount;
                return;
            }
        }
        stacks.add(material.copy());
    }

    private void addMaterial(MaterialStack material) {
        CrucibleRecipeRuntime.Recipe loadedRecipe = getSelectedRecipeDefinition();
        if (loadedRecipe != null
                && CrucibleRecipeRuntime.isRecipeMaterial(loadedRecipe, material.material)
                && CrucibleRecipeRuntime.canFitRecipeMaterial(loadedRecipe, recipeStack, material, RECIPE_CAPACITY)) {
            addToStack(recipeStack, material);
            return;
        }
        addToStack(wasteStack, material);
    }

    private int recipeFreeForInput(CrucibleRecipeRuntime.Recipe loadedRecipe, NTMMaterial material) {
        int required = CrucibleRecipeRuntime.recipeInputAmount(loadedRecipe, material);
        if (required <= 0) {
            return 0;
        }
        int recipeContent = CrucibleRecipeRuntime.recipeInputAmount(loadedRecipe);
        int materialMaximum = required * RECIPE_CAPACITY / Math.max(1, recipeContent);
        return Math.max(0, Math.min(materialMaximum - amountOf(recipeStack, material),
                RECIPE_CAPACITY - getRecipeAmount()));
    }

    private List<MaterialStack> recipeOutputStacks(CrucibleRecipeRuntime.Recipe loadedRecipe) {
        List<MaterialStack> outputs = new ArrayList<>();
        for (MaterialStack stack : recipeStack) {
            if (CrucibleRecipeRuntime.isRecipeOutput(loadedRecipe, stack.material)) {
                outputs.add(stack);
            }
        }
        return outputs;
    }

    private static int totalAmount(List<MaterialStack> stacks) {
        int total = 0;
        for (MaterialStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                total += stack.amount;
            }
        }
        return total;
    }

    private static int amountOf(List<MaterialStack> stacks, NTMMaterial material) {
        int total = 0;
        for (MaterialStack stack : stacks) {
            if (stack != null && stack.material == material) {
                total += stack.amount;
            }
        }
        return total;
    }

    private static int firstColor(List<MaterialStack> stacks) {
        for (MaterialStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                return stack.material.moltenColor;
            }
        }
        return 0xFFFFFF;
    }

    private static void cleanup(List<MaterialStack> stacks) {
        stacks.removeIf(stack -> stack == null || stack.isEmpty());
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    public static CompoundTag recipeSelectionTag(String selection) {
        return GenericMachineRecipeSelector.selectionTag(selection);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, this, 128.0D)
                && GenericMachineRecipeSelector.isSelectionTag(tag)
                && CrucibleRecipeRuntime.canSelect(GenericMachineRecipeSelector.readSelection(tag));
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (GenericMachineRecipeSelector.isSelectionTag(tag)) {
            selectRecipe(GenericMachineRecipeSelector.readSelection(tag));
        }
    }
}
