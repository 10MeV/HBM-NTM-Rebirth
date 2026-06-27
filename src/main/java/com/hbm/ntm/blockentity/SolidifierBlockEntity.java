package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.SolidificationRecipe;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.SolidifierMenu;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SolidifierBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver {
    private static final String TAG_INVENTORY = HbmInventoryMenuHelper.LEGACY_ITEMS_TAG;
    private static final String TAG_MODERN_INVENTORY = "Inventory";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_USAGE = "usage";
    private static final String TAG_PROCESS_TIME = "processTime";
    private static final String TAG_LEGACY_TANK = "tank";
    private static final long MAX_POWER = 100_000L;
    private static final int USAGE_BASE = 250;
    private static final int PROCESS_TIME_BASE = 100;
    private static final int TANK_CAPACITY = 24_000;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3);
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(0, 4, 0, Direction.UP),
            FluidPort.of(0, -1, 0, Direction.DOWN),
            FluidPort.of(2, 1, 0, Direction.EAST),
            FluidPort.of(-2, 1, 0, Direction.WEST),
            FluidPort.of(0, 1, 2, Direction.SOUTH),
            FluidPort.of(0, 1, -2, Direction.NORTH));

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_UPGRADE_SPEED = 2;
    public static final int SLOT_UPGRADE_POWER = 3;
    public static final int SLOT_IDENTIFIER = 4;
    public static final int ITEM_COUNT = 5;

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(ITEM_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_OUTPUT, SLOT_BATTERY, SLOT_IDENTIFIER -> true;
                case SLOT_UPGRADE_SPEED, SLOT_UPGRADE_POWER -> stack.getItem() instanceof ItemMachineUpgrade;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new SolidifierExternalItemHandler(items));

    private int progress;
    private int usage = USAGE_BASE;
    private int processTime = PROCESS_TIME_BASE;
    private String customName;

    public SolidifierBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private SolidifierBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank tank) {
        super(ModBlockEntities.SOLIDIFIER.get(), pos, state, energy, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolidifierBlockEntity solidifier) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, solidifier);
        boolean changed = solidifier.tickSolidifier();
        if (changed) {
            solidifier.setChanged();
        }
        solidifier.networkPackNT(50);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return tank;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getUsage() {
        return usage;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, progress > 0);
        data.putInt(CompatEnergyControl.I_PROGRESS, progress);
        data.putDouble(CompatEnergyControl.D_PROCESS_TIME, processTime);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_HE, usage);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_SOLIDIFIER_INPUT, tank);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(tank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(
                EnergyPort.of(0, 4, 0, Direction.UP),
                EnergyPort.of(0, -1, 0, Direction.DOWN),
                EnergyPort.of(2, 1, 0, Direction.EAST),
                EnergyPort.of(-2, 1, 0, Direction.WEST),
                EnergyPort.of(0, 1, 2, Direction.SOUTH),
                EnergyPort.of(0, 1, -2, Direction.NORTH));
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_USAGE, usage);
        tag.putInt(TAG_PROCESS_TIME, processTime);
        tank.writeToNbt(tag, TAG_LEGACY_TANK);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        progress = tag.getInt(TAG_PROGRESS);
        usage = tag.contains(TAG_USAGE) ? tag.getInt(TAG_USAGE) : USAGE_BASE;
        processTime = tag.contains(TAG_PROCESS_TIME) ? tag.getInt(TAG_PROCESS_TIME) : PROCESS_TIME_BASE;
        if (hasTankTag(tag, TAG_LEGACY_TANK)) {
            tank.readFromNbt(tag, TAG_LEGACY_TANK);
        }
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
    public Component getDisplayName() {
        if (customName != null && !customName.isBlank()) {
            return Component.literal(customName);
        }
        return Component.translatableWithFallback("container.machineSolidifier", "Solidifier");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolidifierMenu(containerId, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        externalItemHandler.invalidate();
    }

    private boolean tickSolidifier() {
        boolean changed = false;
        long oldPower = energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        changed |= oldPower != energy.getPower();
        changed |= setTankTypeFromIdentifier();
        changed |= updateUpgrades();
        if (canProcess()) {
            energy.setPower(energy.getPower() - usage);
            progress++;
            changed = true;
            if (progress >= processTime) {
                finishProcess();
            }
        } else if (progress != 0) {
            progress = 0;
            changed = true;
        }
        return changed;
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_INVENTORY, Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        } else if (tag.contains(TAG_MODERN_INVENTORY, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_INVENTORY, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private boolean setTankTypeFromIdentifier() {
        ItemStack stack = items.getStackInSlot(SLOT_IDENTIFIER);
        if (!(stack.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        FluidType selected = identifier.getPrimaryType(stack);
        if (selected == null || selected == HbmFluids.NONE || tank.getTankType() == selected) {
            return false;
        }
        tank.conform(new HbmFluidStack(selected, 0));
        return true;
    }

    private boolean updateUpgrades() {
        int oldUsage = usage;
        int oldProcessTime = processTime;
        LegacyMachineUpgradeManager.Levels levels =
                LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_SPEED, SLOT_UPGRADE_POWER, VALID_UPGRADES);
        int speed = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        int power = Math.min(levels.getLevel(UpgradeType.POWER), 3);
        processTime = PROCESS_TIME_BASE - (PROCESS_TIME_BASE / 4) * speed;
        usage = (USAGE_BASE + USAGE_BASE * speed) / (power + 1);
        return oldUsage != usage || oldProcessTime != processTime;
    }

    private boolean canProcess() {
        if (energy.getPower() < usage) {
            return false;
        }
        SolidificationRecipe recipe = LegacyOilFluidRecipes.getSolidification(level, tank.getTankType());
        if (recipe == null || recipe.inputAmount() > tank.getFill()) {
            return false;
        }
        return canFitOutput(recipe.outputStack());
    }

    private void finishProcess() {
        SolidificationRecipe recipe = LegacyOilFluidRecipes.getSolidification(level, tank.getTankType());
        if (recipe == null || recipe.inputAmount() > tank.getFill()) {
            progress = 0;
            return;
        }
        ItemStack output = recipe.outputStack();
        if (!canFitOutput(output)) {
            progress = 0;
            return;
        }
        tank.setFill(tank.getFill() - recipe.inputAmount());
        addOutput(output);
        progress = 0;
        onFluidContentsChanged();
    }

    private boolean canFitOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, stack);
    }

    private void addOutput(ItemStack stack) {
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, stack);
    }

    private static final class SolidifierExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private SolidifierExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_OUTPUT) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = HbmItemStackUtil.carefulCopyWithSize(existing,
                    Math.min(amount, existing.getCount()));
            if (!simulate) {
                ItemStack remaining = existing.copy();
                remaining.shrink(extracted.getCount());
                items.setStackInSlot(SLOT_OUTPUT, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}
