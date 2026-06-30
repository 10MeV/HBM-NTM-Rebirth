package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.menu.MicrowaveMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MicrowaveBlockEntity extends HbmEnergyBlockEntity implements MenuProvider, HbmLegacyButtonReceiver {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_COUNT = 3;
    public static final int CONTROL_SPEED_UP = 0;
    public static final int CONTROL_SPEED_DOWN = 1;
    public static final long MAX_POWER = 50_000L;
    public static final int CONSUMPTION = 50;
    public static final int MAX_TIME = 300;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT -> hasSmeltingOutput(stack);
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> menuItemHandler = LazyOptional.of(MenuItemHandler::new);
    private final LazyOptional<IItemHandler> automationInputHandler = LazyOptional.of(() -> new SidedItemHandler(false));
    private final LazyOptional<IItemHandler> automationOutputHandler = LazyOptional.of(() -> new SidedItemHandler(true));
    private int time;
    private int speed;

    public MicrowaveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MICROWAVE.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MicrowaveBlockEntity microwave) {
        if (level.isClientSide) {
            return;
        }
        int oldTime = microwave.time;
        int oldSpeed = microwave.speed;
        long oldPower = microwave.energy.getPower();
        microwave.subscribeEnergyReceiverToAllSides();
        HbmEnergyUtil.chargeStorageFromItem(microwave.items.getStackInSlot(SLOT_BATTERY), microwave.energy,
                microwave.energy.getReceiverSpeed());

        ItemStack output = microwave.getFoodSmeltingOutput(microwave.items.getStackInSlot(SLOT_INPUT));
        boolean canProcess = !output.isEmpty()
                && HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(microwave.items, SLOT_OUTPUT, SLOT_OUTPUT, output);
        if (canProcess && microwave.speed > 0 && microwave.energy.getPower() >= CONSUMPTION) {
            if (microwave.speed >= 5) {
                microwave.items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
                microwave.energy.setPower(0L);
                level.removeBlock(pos, false);
                level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F,
                        Level.ExplosionInteraction.BLOCK);
                return;
            }
            if (microwave.time >= MAX_TIME) {
                HbmInventoryUtil.tryAddItemToHandlerUnchecked(microwave.items, SLOT_OUTPUT, SLOT_OUTPUT, output);
                microwave.items.extractItem(SLOT_INPUT, 1, false);
                microwave.time = 0;
                output = microwave.getFoodSmeltingOutput(microwave.items.getStackInSlot(SLOT_INPUT));
                canProcess = !output.isEmpty()
                        && HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(microwave.items, SLOT_OUTPUT, SLOT_OUTPUT,
                        output);
            }
            if (!canProcess) {
                microwave.networkPackNT(20);
                microwave.setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                return;
            }
            microwave.energy.setPower(microwave.energy.getPower() - CONSUMPTION);
            microwave.time += microwave.speed * 2;
        }

        microwave.networkPackNT(20);
        if (oldTime != microwave.time || oldSpeed != microwave.speed || oldPower != microwave.energy.getPower()) {
            microwave.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MicrowaveBlockEntity microwave) {
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public IItemHandler getMenuItems() {
        return menuItemHandler.orElse(items);
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    public int getTime() {
        return time;
    }

    public int getSpeed() {
        return speed;
    }

    public int getTimeScaled(int width) {
        return time * width / MAX_TIME;
    }

    public int getPowerScaled(int height) {
        return energy.getMaxPower() <= 0L ? 0 : (int) (energy.getPower() * height / energy.getMaxPower());
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private boolean hasSmeltingOutput(ItemStack input) {
        if (level == null || input.isEmpty()) {
            return false;
        }
        Container container = new SimpleContainer(input.copyWithCount(1));
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()))
                .map(stack -> stack.copy())
                .filter(stack -> !stack.isEmpty())
                .isPresent();
    }

    private ItemStack getFoodSmeltingOutput(ItemStack input) {
        if (level == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Container container = new SimpleContainer(input.copyWithCount(1));
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()))
                .map(stack -> stack.copy())
                .filter(output -> !output.isEmpty() && (input.getItem().isEdible() || output.getItem().isEdible()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable net.minecraft.core.Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.microwave", "Microwave");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MicrowaveMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_SPEED_DOWN || id == CONTROL_SPEED_UP;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_SPEED_UP) {
            speed = Math.min(5, speed + 1);
        } else if (id == CONTROL_SPEED_DOWN) {
            speed = Math.max(0, speed - 1);
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "Inventory", items);
        tag.putLong("power", energy.getPower());
        tag.putInt("speed", speed);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        } else if (!tag.contains("Energy") && tag.contains("powerTime")) {
            energy.setPower(tag.getLong("powerTime"));
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "Inventory", items);
        time = tag.getInt("cookTime");
        speed = Math.max(0, Math.min(5, tag.getInt("speed")));
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = saveWithoutMetadata();
        tag.putInt("cookTime", time);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        handleClientSyncTag(packet.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        menuItemHandler.invalidate();
        automationInputHandler.invalidate();
        automationOutputHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return menuItemHandler.cast();
            }
            return side == Direction.DOWN ? automationOutputHandler.cast() : automationInputHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class SidedItemHandler implements IItemHandler {
        private final boolean output;

        private SidedItemHandler(boolean output) {
            this.output = output;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(output ? SLOT_OUTPUT : SLOT_INPUT) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 && !output ? items.insertItem(SLOT_INPUT, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 && output ? items.extractItem(SLOT_OUTPUT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(output ? SLOT_OUTPUT : SLOT_INPUT) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && !output && items.isItemValid(SLOT_INPUT, stack);
        }
    }

    private final class MenuItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != SLOT_INPUT && slot != SLOT_BATTERY) {
                return stack;
            }
            ItemStack existing = items.getStackInSlot(slot);
            int limit = Math.min(items.getSlotLimit(slot), stack.getMaxStackSize());
            if (!existing.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                    return stack;
                }
                limit -= existing.getCount();
            }
            if (limit <= 0) {
                return stack;
            }
            int transferred = Math.min(limit, stack.getCount());
            if (!simulate) {
                if (existing.isEmpty()) {
                    items.setStackInSlot(slot, ItemHandlerHelper.copyStackWithSize(stack, transferred));
                } else {
                    ItemStack merged = existing.copy();
                    merged.grow(transferred);
                    items.setStackInSlot(slot, merged);
                }
            }
            return ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - transferred);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= 0 && slot < SLOT_COUNT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_INPUT || slot == SLOT_BATTERY;
        }
    }
}
