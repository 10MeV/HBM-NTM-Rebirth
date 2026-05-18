package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.menu.BasicMachineMenu;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.PressRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicMachineBlockEntity extends BlockEntity implements MenuProvider {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "Progress";
    private static final String TAG_MAX_PROGRESS = "MaxProgress";
    private static final String TAG_TICKS_EXISTED = "TicksExisted";
    private static final String TAG_BURN_TIME = "BurnTime";

    public static final int SLOT_FUEL = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;

    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<ItemStackHandler> itemHandler = LazyOptional.of(() -> items);

    private int progress;
    private int maxProgress = 200;
    private int burnTime;
    private long ticksExisted;

    public BasicMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BASIC_MACHINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BasicMachineBlockEntity blockEntity) {
        blockEntity.ticksExisted++;
        PressRecipe recipe = blockEntity.findMatchingPressRecipe(level);
        if (recipe != null && blockEntity.ensureFuel()) {
            blockEntity.progress++;
            blockEntity.burnTime--;
            if (blockEntity.progress >= blockEntity.maxProgress) {
                blockEntity.finishPressRecipe(recipe);
                blockEntity.progress = 0;
            }
            blockEntity.setChanged();
        } else if (blockEntity.progress != 0) {
            blockEntity.progress = 0;
            blockEntity.setChanged();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public long getTicksExisted() {
        return ticksExisted;
    }

    public int getBurnTime() {
        return burnTime;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_MAX_PROGRESS, maxProgress);
        tag.putLong(TAG_TICKS_EXISTED, ticksExisted);
        tag.putInt(TAG_BURN_TIME, burnTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        progress = tag.getInt(TAG_PROGRESS);
        maxProgress = tag.contains(TAG_MAX_PROGRESS) ? tag.getInt(TAG_MAX_PROGRESS) : 200;
        ticksExisted = tag.getLong(TAG_TICKS_EXISTED);
        burnTime = tag.getInt(TAG_BURN_TIME);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hbm.machine_press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BasicMachineMenu(containerId, inventory, this);
    }

    private PressRecipe findMatchingPressRecipe(Level level) {
        SimpleContainer container = new SimpleContainer(items.getStackInSlot(SLOT_INPUT), items.getStackInSlot(SLOT_STAMP));
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.PRESS.type().get()).stream()
                .filter(recipe -> recipe.matches(container, level))
                .filter(this::canFitOutput)
                .findFirst()
                .orElse(null);
    }

    private boolean ensureFuel() {
        if (burnTime > 0) {
            return true;
        }

        ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
        int fuelTime = ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
        if (fuelTime <= 0) {
            return false;
        }

        burnTime = fuelTime;
        ItemStack remainder = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        if (fuel.isEmpty() && !remainder.isEmpty()) {
            items.setStackInSlot(SLOT_FUEL, remainder);
        }
        return true;
    }

    private boolean canFitOutput(PressRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(output, result)) {
            return false;
        }
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void finishPressRecipe(PressRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        items.extractItem(SLOT_INPUT, 1, false);
        ItemStack stamp = items.getStackInSlot(SLOT_STAMP);
        if (stamp.isDamageableItem()) {
            stamp.hurt(1, level.random, null);
            if (stamp.getDamageValue() >= stamp.getMaxDamage()) {
                items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
            }
        }
    }
}
