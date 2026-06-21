package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.WasteDrumMenu;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKFuelRodRuntime;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.recipe.FuelPoolRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WasteDrumBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 12;

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
            return FuelPoolRecipes.isInput(stack) || stack.getItem() instanceof RBMKFuelRodItem;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    public WasteDrumBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WASTE_DRUM.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WasteDrumBlockEntity drum) {
        int water = drum.adjacentWater(level, pos);
        if (water <= 0) {
            return;
        }
        int chance = 60 * 60 * 20 / water;
        boolean changed = false;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = drum.items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (drum.coolRbmkRod(level, stack)) {
                changed = true;
                continue;
            }
            if (level.random.nextInt(chance) != 0) {
                continue;
            }
            ItemStack output = FuelPoolRecipes.cool(stack);
            if (!output.isEmpty()) {
                drum.items.setStackInSlot(i, output);
                changed = true;
            }
        }
        if (changed) {
            drum.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.wasteDrum", "Spent Fuel Pool Drum");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WasteDrumMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private int adjacentWater(Level level, BlockPos pos) {
        int water = 0;
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                water++;
            }
        }
        return water;
    }

    private boolean coolRbmkRod(Level level, ItemStack stack) {
        if (!(stack.getItem() instanceof RBMKFuelRodItem rod)) {
            return false;
        }
        RBMKFuelRodState state = rod.getState(stack);
        double beforeCore = state.coreHeat();
        double beforeHull = state.hullHeat();
        RBMKFuelRodRuntime.updateHeat(NeutronHandler.rbmkRuntimeSettings(level), rod.getSpec(), state, 0.025D);
        RBMKFuelRodRuntime.provideHeat(NeutronHandler.rbmkRuntimeSettings(level), rod.getSpec(), state, 20.0D, 0.025D);
        rod.setState(stack, state);
        return state.coreHeat() != beforeCore || state.hullHeat() != beforeHull;
    }

    private static boolean canExtractFromPool(ItemStack stack) {
        if (stack.getItem() instanceof RBMKFuelRodItem rod) {
            RBMKFuelRodState state = rod.getState(stack);
            return state.coreHeat() < 50.0D && state.hullHeat() < 50.0D;
        }
        return FuelPoolRecipes.canExtract(stack);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return items.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return items.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return canExtractFromPool(items.getStackInSlot(slot))
                    ? items.extractItem(slot, amount, simulate)
                    : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return items.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return items.isItemValid(slot, stack); }
    }
}
