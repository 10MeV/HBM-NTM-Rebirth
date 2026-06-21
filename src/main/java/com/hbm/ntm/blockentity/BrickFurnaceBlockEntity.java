package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.BrickFurnaceBlock;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.menu.BrickFurnaceMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BrickFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_ITEMS = "items";
    private static final int ASH_THRESHOLD = 2_000;
    public static final int PROCESS_TIME = 200;

    private final LegacyBurnTimeModule burnModule = new LegacyBurnTimeModule();
    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> smeltingResult(stack).isPresent();
                case 1 -> burnModule.getBurnTime(stack) > 0;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int burnTime;
    private int maxBurnTime;
    private int progress;
    private int ashLevelWood;
    private int ashLevelCoal;
    private int ashLevelMisc;

    public BrickFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRICK_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrickFurnaceBlockEntity furnace) {
        boolean changed = furnace.tick(level);
        boolean lit = furnace.burnTime > 0;
        if (state.hasProperty(BrickFurnaceBlock.LIT) && state.getValue(BrickFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(BrickFurnaceBlock.LIT, lit), Block.UPDATE_CLIENTS);
            changed = true;
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_CLIENTS);
        }
    }

    private boolean tick(Level level) {
        int oldBurn = burnTime;
        int oldMax = maxBurnTime;
        int oldProgress = progress;

        if (burnTime > 0) {
            burnTime--;
        }
        if ((burnTime > 0 || !items.getStackInSlot(1).isEmpty()) && !items.getStackInSlot(0).isEmpty()) {
            if (burnTime == 0 && canSmelt()) {
                consumeFuel();
            }
            if (burnTime > 0 && canSmelt()) {
                progress += burnSpeed(items.getStackInSlot(0));
                if (progress >= PROCESS_TIME) {
                    progress = 0;
                    smelt();
                }
            } else {
                progress = 0;
            }
        }
        return oldBurn != burnTime || oldMax != maxBurnTime || oldProgress != progress;
    }

    private void consumeFuel() {
        ItemStack fuel = items.getStackInSlot(1);
        int burn = burnModule.getBurnTime(fuel);
        if (burn <= 0) {
            return;
        }
        maxBurnTime = burnTime = burn;
        addAsh(fuel, burn);
        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        items.setStackInSlot(1, fuel.isEmpty() ? remainder : fuel);
    }

    private void addAsh(ItemStack fuel, int amount) {
        switch (burnModule.getAshFromFuel(fuel)) {
            case WOOD -> ashLevelWood += amount;
            case COAL -> ashLevelCoal += amount;
            case MISC -> ashLevelMisc += amount;
        }
        while (ashLevelWood >= ASH_THRESHOLD && insertAsh("powder_ash_wood")) {
            ashLevelWood -= ASH_THRESHOLD;
        }
        while (ashLevelCoal >= ASH_THRESHOLD && insertAsh("powder_ash_coal")) {
            ashLevelCoal -= ASH_THRESHOLD;
        }
        while (ashLevelMisc >= ASH_THRESHOLD && insertAsh("powder_ash_misc")) {
            ashLevelMisc -= ASH_THRESHOLD;
        }
    }

    private boolean insertAsh(String name) {
        RegistryObject<net.minecraft.world.item.Item> item = ModItems.legacyItem(name);
        if (item == null) {
            return false;
        }
        ItemStack output = new ItemStack(item.get());
        return HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, 3, 3, output).isEmpty();
    }

    private boolean canSmelt() {
        return smeltingResult(items.getStackInSlot(0))
                .filter(result -> HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, 2, 2, result))
                .isPresent();
    }

    private void smelt() {
        smeltingResult(items.getStackInSlot(0)).ifPresent(result -> {
            HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, 2, 2, result);
            items.extractItem(0, 1, false);
        });
    }

    private Optional<ItemStack> smeltingResult(ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }
        Container container = new SimpleContainer(input);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()).copy());
    }

    private static int burnSpeed(ItemStack input) {
        if (input.is(Items.CLAY_BALL) || input.is(Blocks.NETHERRACK.asItem()) || isLegacyItem(input, "ball_fireclay")) {
            return 4;
        }
        if (input.is(Blocks.COBBLESTONE.asItem()) || input.is(Blocks.SAND.asItem())
                || input.is(Blocks.OAK_LOG.asItem()) || input.is(Blocks.SPRUCE_LOG.asItem())
                || input.is(Blocks.BIRCH_LOG.asItem()) || input.is(Blocks.JUNGLE_LOG.asItem())
                || input.is(Blocks.ACACIA_LOG.asItem()) || input.is(Blocks.DARK_OAK_LOG.asItem())) {
            return 2;
        }
        return 1;
    }

    private static boolean isLegacyItem(ItemStack stack, String name) {
        RegistryObject<net.minecraft.world.item.Item> item = ModItems.legacyItem(name);
        return item != null && stack.is(item.get());
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getProgress() {
        return progress;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.furnaceBrick", "Brick Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BrickFurnaceMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurn", maxBurnTime);
        tag.putInt("progress", progress);
        tag.putInt("ashWood", ashLevelWood);
        tag.putInt("ashCoal", ashLevelCoal);
        tag.putInt("ashMisc", ashLevelMisc);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurn");
        progress = tag.getInt("progress");
        ashLevelWood = tag.getInt("ashWood");
        ashLevelCoal = tag.getInt("ashCoal");
        ashLevelMisc = tag.getInt("ashMisc");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : LazyOptional.of(() -> new AccessibleItemHandler(side)).cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Nullable
        private final Direction side;

        private AccessibleItemHandler() {
            this(null);
        }

        private AccessibleItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return visibleSlots().length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int target = targetSlot(slot);
            return target >= 0 ? items.getStackInSlot(target) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int target = targetSlot(slot);
            return target == 0 || target == 1 ? items.insertItem(target, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int target = targetSlot(slot);
            return target >= 2 ? items.extractItem(target, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int target = targetSlot(slot);
            return target >= 0 ? items.getSlotLimit(target) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int target = targetSlot(slot);
            return target >= 0 && (target == 0 || target == 1) && items.isItemValid(target, stack);
        }

        private int targetSlot(int visibleSlot) {
            int[] slots = visibleSlots();
            return visibleSlot >= 0 && visibleSlot < slots.length ? slots[visibleSlot] : -1;
        }

        private int[] visibleSlots() {
            if (side == Direction.DOWN) {
                return new int[] {2, 1, 3};
            }
            if (side == Direction.UP) {
                return new int[] {0};
            }
            if (side != null) {
                return new int[] {1};
            }
            return new int[] {0, 1, 2, 3};
        }
    }
}
