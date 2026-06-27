package com.hbm.ntm.blockentity;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.menu.DiFurnaceMenu;
import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiFurnaceBlockEntity extends HbmFluidBlockEntity
        implements MenuProvider, HbmStandardFluidSender {
    public static final int SLOT_UPPER = 0;
    public static final int SLOT_LOWER = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int MAX_FUEL = 12_800;
    public static final int PROCESSING_SPEED = 400;

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_FUEL = "powerTime";
    private static final String TAG_PROGRESS = "cookTime";
    private static final String TAG_MODES = "modes";

    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(50);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_UPPER, SLOT_LOWER -> true;
                case SLOT_FUEL -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int progress;
    private int fuel;
    private byte sideFuel = 1;
    private byte sideUpper = 1;
    private byte sideLower = 1;
    private boolean processing;

    public DiFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIFURNACE.get(), pos, state, List.of());
        pollution.normalizeTypes();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DiFurnaceBlockEntity furnace) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = furnace.tickServer(level, pos, state);
        furnace.networkPackNT(15);
        if (changed) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DiFurnaceBlockEntity furnace) {
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getFuel() {
        return fuel;
    }

    public byte getSideFuel() {
        return sideFuel;
    }

    public byte getSideUpper() {
        return sideUpper;
    }

    public byte getSideLower() {
        return sideLower;
    }

    public boolean hasPower() {
        return fuel > 0;
    }

    public boolean isProcessing() {
        return processing;
    }

    public boolean hasExtension() {
        if (level == null) {
            return false;
        }
        RegistryObject<? extends Block> extension = ModBlocks.legacyBlock("machine_difurnace_extension");
        return extension != null && level.getBlockState(worldPosition.above()).is(extension.get());
    }

    public int getProgressPixels(int width) {
        return Math.max(0, Math.min(width, progress * width / PROCESSING_SPEED));
    }

    public int getFuelPixels(int height) {
        return Math.max(0, Math.min(height, fuel * height / MAX_FUEL));
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public void cycleSideMode(int slot) {
        switch (slot) {
            case SLOT_UPPER -> sideUpper = (byte) ((sideUpper + 1) % 6);
            case SLOT_LOWER -> sideLower = (byte) ((sideLower + 1) % 6);
            case SLOT_FUEL -> sideFuel = (byte) ((sideFuel + 1) % 6);
            default -> {
                return;
            }
        }
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.diFurnace", "Blast Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DiFurnaceMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return pollution.tanks();
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.OUTPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        pollution.writeLegacyNbt(tag);
        tag.putInt(TAG_FUEL, fuel);
        tag.putShort(TAG_PROGRESS, (short) progress);
        tag.putByteArray(TAG_MODES, new byte[] {sideFuel, sideUpper, sideLower});
        tag.putBoolean("processing", processing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        pollution.readLegacyNbt(tag);
        fuel = tag.getInt(TAG_FUEL);
        progress = tag.getShort(TAG_PROGRESS);
        byte[] modes = tag.getByteArray(TAG_MODES);
        if (modes.length >= 3) {
            sideFuel = modes[0];
            sideUpper = modes[1];
            sideLower = modes[2];
        }
        processing = tag.getBoolean("processing");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : LazyOptional.of(() -> new AccessibleItemHandler(items, side)).cast();
        }
        return super.getCapability(capability, side);
    }

    private boolean tickServer(Level level, BlockPos pos, BlockState state) {
        int oldFuel = fuel;
        int oldProgress = progress;
        boolean oldProcessing = processing;
        int oldSoot = pollution.soot().getFill();

        boolean extension = hasExtension();
        sendSmoke(level, pos, extension);
        loadFuel();
        BlastFurnaceRecipe recipe = findRecipe(level);
        if (canProcess(recipe)) {
            fuel = Math.max(0, fuel - 1);
            progress += extension ? 3 : 1;
            processing = true;
            if (progress >= PROCESSING_SPEED) {
                progress -= PROCESSING_SPEED;
                process(recipe);
            }
            if (level.getGameTime() % 20L == 0L) {
                pollution.pollute(level, pos, PollutionHandler.PollutionType.SOOT,
                        PollutionHandler.SOOT_PER_SECOND * (extension ? 3.0F : 1.0F));
            }
        } else {
            progress = 0;
            processing = false;
        }

        return oldFuel != fuel || oldProgress != progress || oldProcessing != processing
                || oldSoot != pollution.soot().getFill();
    }

    private void sendSmoke(Level level, BlockPos pos, boolean extension) {
        for (Direction direction : Direction.values()) {
            BlockPos connector = pos.relative(direction);
            pollution.sendSmoke(level, connector.getX(), connector.getY(), connector.getZ(), direction);
        }
        if (extension) {
            BlockPos connector = pos.above(2);
            pollution.sendSmoke(level, connector.getX(), connector.getY(), connector.getZ(), Direction.UP);
        }
    }

    private void loadFuel() {
        ItemStack stack = items.getStackInSlot(SLOT_FUEL);
        int burn = getItemPower(stack);
        if (burn <= 0 || fuel > MAX_FUEL - burn) {
            return;
        }
        fuel += burn;
        ItemStack remainder = stack.getCraftingRemainingItem();
        items.extractItem(SLOT_FUEL, 1, false);
        if (items.getStackInSlot(SLOT_FUEL).isEmpty() && !remainder.isEmpty()) {
            items.setStackInSlot(SLOT_FUEL, remainder.copy());
        }
    }

    @Nullable
    private BlastFurnaceRecipe findRecipe(Level level) {
        ItemStack upper = items.getStackInSlot(SLOT_UPPER);
        ItemStack lower = items.getStackInSlot(SLOT_LOWER);
        if (upper.isEmpty() || lower.isEmpty()) {
            return null;
        }
        for (BlastFurnaceRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.BLAST_FURNACE.type().get())) {
            if (recipe.inputs().size() == 2 && recipe.outputs().size() == 1 && recipe.matches(upper, lower)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canProcess(@Nullable BlastFurnaceRecipe recipe) {
        if (recipe == null || !hasPower()) {
            return false;
        }
        int consumeUpper = recipe.consumedCountForSlot(0,
                items.getStackInSlot(SLOT_UPPER), items.getStackInSlot(SLOT_LOWER));
        int consumeLower = recipe.consumedCountForSlot(1,
                items.getStackInSlot(SLOT_UPPER), items.getStackInSlot(SLOT_LOWER));
        if (consumeUpper <= 0 || consumeLower <= 0) {
            return false;
        }
        ItemStack output = recipe.outputs().get(0).representativeStack();
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, output);
    }

    private void process(BlastFurnaceRecipe recipe) {
        HbmItemOutput output = recipe.outputs().get(0);
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT,
                output.representativeStack());
        ItemStack upper = items.getStackInSlot(SLOT_UPPER);
        ItemStack lower = items.getStackInSlot(SLOT_LOWER);
        int consumeUpper = recipe.consumedCountForSlot(0, upper, lower);
        int consumeLower = recipe.consumedCountForSlot(1, upper, lower);
        items.extractItem(SLOT_UPPER, consumeUpper, false);
        items.extractItem(SLOT_LOWER, consumeLower, false);
    }

    private static int getItemPower(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
            return 200;
        }
        if (stack.is(Blocks.COAL_BLOCK.asItem())) {
            return 2_000;
        }
        RegistryObject<? extends Block> cokeBlock = ModBlocks.legacyBlock("block_coke");
        if (cokeBlock != null && stack.is(cokeBlock.get().asItem())) {
            return 4_000;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return 12_800;
        }
        if (stack.is(Items.BLAZE_ROD)) {
            return 1_000;
        }
        if (stack.is(Items.BLAZE_POWDER)) {
            return 300;
        }
        if (isLegacyItem(stack, "lignite") || isLegacyItem(stack, "powder_lignite")) {
            return 150;
        }
        if (isLegacyItem(stack, "powder_coal")) {
            return 200;
        }
        if (isLegacyItem(stack, "briquette_coal")) {
            return 200;
        }
        if (isLegacyItem(stack, "coke_coal")) {
            return 400;
        }
        if (isLegacyItem(stack, "solid_fuel")) {
            return 400;
        }
        return 0;
    }

    private static boolean isLegacyItem(ItemStack stack, String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item != null && stack.is(item.get());
    }

    private final class AccessibleItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;
        private final Direction side;

        private AccessibleItemHandler(IItemHandlerModifiable items, Direction side) {
            this.items = items;
            this.side = side;
        }

        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return valid(slot) ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!valid(slot) || slot == SLOT_OUTPUT || !canInsertFromSide(slot)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_OUTPUT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return valid(slot) ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return valid(slot) && canInsertFromSide(slot) && items.isItemValid(slot, stack);
        }

        private boolean canInsertFromSide(int slot) {
            int ordinal = side.ordinal();
            return switch (slot) {
                case SLOT_UPPER -> sideUpper == ordinal;
                case SLOT_LOWER -> sideLower == ordinal;
                case SLOT_FUEL -> sideFuel == ordinal;
                default -> false;
            };
        }

        private boolean valid(int slot) {
            return slot >= 0 && slot < SLOT_COUNT;
        }
    }
}
