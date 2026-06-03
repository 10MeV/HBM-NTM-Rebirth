package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayPorts;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidItemTransfer.TankSlotTransfer;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.menu.ChemicalPlantMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChemicalPlantBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmTileSyncable, HbmStandardFluidTransceiver, LegacyLookOverlayProvider {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_LEGACY_MAX_POWER = "maxPower";
    private static final String TAG_INPUT_TANK = "i";
    private static final String TAG_OUTPUT_TANK = "o";
    private static final String TAG_PROGRESS = "progress0";
    private static final String TAG_RECIPE = "recipe0";
    private static final String TAG_DID_PROCESS = "DidProcess";
    private static final String TAG_ANIM = "Anim";
    private static final String TAG_FRAME = "Frame";
    private static final long DEFAULT_MAX_POWER = 100_000L;
    private static final int TANK_CAPACITY = 24_000;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 3;
    public static final int SLOT_ITEM_INPUT_START = 4;
    public static final int SLOT_ITEM_INPUT_END = 6;
    public static final int SLOT_ITEM_OUTPUT_START = 7;
    public static final int SLOT_ITEM_OUTPUT_END = 9;
    public static final int SLOT_FLUID_INPUT_START = 10;
    public static final int SLOT_FLUID_INPUT_END = 12;
    public static final int SLOT_FLUID_INPUT_RETURN_START = 13;
    public static final int SLOT_FLUID_INPUT_RETURN_END = 15;
    public static final int SLOT_FLUID_OUTPUT_START = 16;
    public static final int SLOT_FLUID_OUTPUT_END = 18;
    public static final int SLOT_FLUID_OUTPUT_RETURN_START = 19;
    public static final int SLOT_FLUID_OUTPUT_RETURN_END = 21;
    public static final int ITEM_COUNT = 22;
    public static final int[] INPUT_SLOTS = new int[] { 4, 5, 6 };
    public static final int[] OUTPUT_SLOTS = new int[] { 7, 8, 9 };
    private static final List<EnergyPort> ENERGY_PORTS = LegacyMultiblockPorts.xrFloorRingEnergyPorts(2);
    private static final List<FluidPort> FLUID_PORTS = LegacyMultiblockPorts.xrFloorRingFluidPorts(2);

    private final ItemStackHandler items = new ItemStackHandler(ITEM_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !isOutputOnlySlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final HbmEnergyStorage energy = new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L);
    private final HbmFluidTank[] inputTanks = new HbmFluidTank[] {
            new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
            new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
            new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY)
    };
    private final HbmFluidTank[] outputTanks = new HbmFluidTank[] {
            new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
            new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
            new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY)
    };
    private final List<HbmFluidTank> inputTankList = Arrays.asList(inputTanks);
    private final List<HbmFluidTank> outputTankList = Arrays.asList(outputTanks);
    private final List<HbmFluidTank> allTankList = List.of(
            inputTanks[0], inputTanks[1], inputTanks[2],
            outputTanks[0], outputTanks[1], outputTanks[2]);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() ->
            new ForgeFluidHandlerAdapter(inputTankList, 0, true, false, this::onFluidContentsChanged));
    private final LazyOptional<IFluidHandler> outputFluidHandler = LazyOptional.of(() ->
            new ForgeFluidHandlerAdapter(outputTankList, 0, false, true, this::onFluidContentsChanged));
    private final LazyOptional<IFluidHandler> allFluidHandler = LazyOptional.of(() ->
            new ChemicalPlantFluidHandler(this::onFluidContentsChanged));

    private boolean didProcess;
    private int prevAnim;
    private int anim;
    private boolean frame;
    private double progress;
    private String selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;

    public ChemicalPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_PLANT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChemicalPlantBlockEntity blockEntity) {
        long oldPower = blockEntity.energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(blockEntity.items.getStackInSlot(SLOT_BATTERY), blockEntity,
                blockEntity.getReceiverSpeed());
        blockEntity.subscribeEnergyReceiverToPorts();

        boolean changed = blockEntity.processFluidContainers();
        blockEntity.refreshFluidPortSubscriptions();
        changed |= blockEntity.tickRecipe(level);
        changed |= oldPower != blockEntity.energy.getPower();
        if (changed) {
            blockEntity.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChemicalPlantBlockEntity blockEntity) {
        blockEntity.prevAnim = blockEntity.anim;
        if (blockEntity.didProcess) {
            blockEntity.anim++;
        }
        if (level.getGameTime() % 20L == 0L) {
            blockEntity.frame = !level.getBlockState(pos.above(3)).isAir();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    public HbmFluidTank getInputTank(int index) {
        return inputTanks[index];
    }

    public HbmFluidTank getOutputTank(int index) {
        return outputTanks[index];
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlayPorts.factoryMachinePort(this, viewedPos);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return allTankList;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return inputTankList;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return outputTankList;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    public double getProgress() {
        return progress;
    }

    public String getSelectedRecipeName() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(String selectedRecipe) {
        this.selectedRecipe = GenericMachineRecipeSelector.normalize(selectedRecipe);
        this.progress = 0.0D;
        setChanged();
    }

    public boolean selectRecipe(String selectedRecipe) {
        if (level == null || GenericMachineRecipeSelector.isNullSelection(selectedRecipe)) {
            setSelectedRecipe(GenericMachineRecipeRuntime.NULL_RECIPE);
            return true;
        }
        if (!GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.CHEMICAL_PLANT, selectedRecipe)) {
            return false;
        }
        setSelectedRecipe(selectedRecipe);
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        setupTanks(recipe);
        updateDynamicCapacity(recipe);
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition() {
        if (level == null) {
            return null;
        }
        return GenericMachineRecipeRuntime.findByInternalName(level, GenericMachineRecipe.Machine.CHEMICAL_PLANT, selectedRecipe);
    }

    public boolean canProcessSelectedRecipe() {
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        return recipe != null
                && energy.getPower() >= recipe.getPower()
                && GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                inputTankList, outputTankList);
    }

    public static CompoundTag recipeSelectionTag(String selection) {
        return GenericMachineRecipeSelector.selectionTag(selection);
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    public int getAnim() {
        return anim;
    }

    public int getPrevAnim() {
        return prevAnim;
    }

    public boolean shouldRenderFrame() {
        return frame || (level != null && !level.getBlockState(worldPosition.above(3)).isAir());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        for (int i = 0; i < 3; i++) {
            inputTanks[i].writeToNbt(tag, TAG_INPUT_TANK + i);
            outputTanks[i].writeToNbt(tag, TAG_OUTPUT_TANK + i);
        }
        tag.putDouble(TAG_PROGRESS, progress);
        tag.putString(TAG_RECIPE, selectedRecipe);
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
        tag.putInt(TAG_ANIM, anim);
        tag.putBoolean(TAG_FRAME, frame);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        for (int i = 0; i < 3; i++) {
            if (tag.contains(TAG_INPUT_TANK + i)) {
                inputTanks[i].readFromNbt(tag, TAG_INPUT_TANK + i);
            }
            if (tag.contains(TAG_OUTPUT_TANK + i)) {
                outputTanks[i].readFromNbt(tag, TAG_OUTPUT_TANK + i);
            }
        }
        progress = tag.getDouble(TAG_PROGRESS);
        selectedRecipe = tag.getString(TAG_RECIPE);
        if (selectedRecipe.isBlank()) {
            selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
        }
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
        anim = tag.getInt(TAG_ANIM);
        prevAnim = anim;
        frame = tag.getBoolean(TAG_FRAME);
        updateDynamicCapacity(getSelectedRecipeDefinition());
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
    public AABB getRenderBoundingBox() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            return block.definition().renderBoundingBox(state, worldPosition);
        }
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineChemicalPlant", "Chemical Plant");
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return player.containerMenu instanceof ChemicalPlantMenu
                && GenericMachineRecipeSelector.isSelectionTag(tag)
                && GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                GenericMachineRecipeSelector.readSelection(tag));
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (GenericMachineRecipeSelector.isSelectionTag(tag)) {
            selectRecipe(GenericMachineRecipeSelector.readSelection(tag));
        }
    }

    @Override
    public long getPower() {
        return energy.getPower();
    }

    @Override
    public void setPower(long power) {
        energy.setPower(power);
    }

    @Override
    public long getMaxPower() {
        return energy.getMaxPower();
    }

    @Override
    public long getReceiverSpeed() {
        return energy.getReceiverSpeed();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ChemicalPlantMenu(containerId, inventory, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
        outputFluidHandler.invalidate();
        allFluidHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null) {
                return allFluidHandler.cast();
            }
            return side == Direction.DOWN ? outputFluidHandler.cast() : fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean tickRecipe(Level level) {
        boolean wasProcessing = didProcess;
        double oldProgress = progress;
        long oldPower = energy.getPower();
        didProcess = false;

        GenericMachineRecipe recipe = GenericMachineRecipeRuntime.findByInternalName(
                level, GenericMachineRecipe.Machine.CHEMICAL_PLANT, selectedRecipe);
        if (recipe == null) {
            progress = 0.0D;
            updateDynamicCapacity(null);
            return wasProcessing || oldProgress != progress;
        }

        GenericMachineRecipe switchedRecipe = GenericMachineRecipeRuntime.findAutoSwitchRecipe(
                level, GenericMachineRecipe.Machine.CHEMICAL_PLANT, recipe, items.getStackInSlot(INPUT_SLOTS[0]));
        if (switchedRecipe != null) {
            selectedRecipe = switchedRecipe.getInternalName();
            progress = 0.0D;
            setupTanks(switchedRecipe);
            updateDynamicCapacity(switchedRecipe);
            return true;
        }

        setupTanks(recipe);
        updateDynamicCapacity(recipe);
        boolean canProcess = energy.getPower() >= recipe.getPower()
                && GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                inputTankList, outputTankList);
        if (!canProcess) {
            progress = 0.0D;
            return wasProcessing || oldProgress != progress;
        }

        energy.setPower(energy.getPower() - recipe.getPower());
        int duration = Math.max(recipe.getDuration(), 1);
        progress = Math.min(1.0D, progress + 1.0D / duration);
        didProcess = true;

        if (progress >= 1.0D) {
            GenericMachineRecipeRuntime.consumeInputs(recipe, items, INPUT_SLOTS, inputTankList);
            GenericMachineRecipeRuntime.produceOutputs(recipe, items, OUTPUT_SLOTS, outputTankList);
            if (GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                    inputTankList, outputTankList)
                    && energy.getPower() >= recipe.getPower()) {
                progress -= 1.0D;
            } else {
                progress = 0.0D;
            }
        }

        return wasProcessing != didProcess || oldProgress != progress || oldPower != energy.getPower();
    }

    private void setupTanks(@Nullable GenericMachineRecipe recipe) {
        if (recipe == null) {
            return;
        }
        List<HbmFluidStack> fluidInputs = recipe.getFluidInputs();
        List<HbmFluidStack> fluidOutputs = recipe.getFluidOutputs();
        for (int i = 0; i < 3; i++) {
            conformTank(inputTanks[i], i < fluidInputs.size() ? fluidInputs.get(i) : null);
            conformTank(outputTanks[i], i < fluidOutputs.size() ? fluidOutputs.get(i) : null);
        }
    }

    private void conformTank(HbmFluidTank tank, @Nullable HbmFluidStack stack) {
        if (stack == null) {
            if (tank.isEmpty()) {
                tank.resetTank();
            }
            tank.changeTankSize(Math.max(tank.getFill(), TANK_CAPACITY));
            return;
        }
        tank.conform(stack);
        tank.changeTankSize(Math.max(Math.max(tank.getFill(), stack.amount() * 2), TANK_CAPACITY));
    }

    private void updateDynamicCapacity(@Nullable GenericMachineRecipe recipe) {
        long targetMax = recipe == null ? DEFAULT_MAX_POWER : Math.max(DEFAULT_MAX_POWER, recipe.getPower() * 100L);
        targetMax = Math.max(targetMax, energy.getPower());
        energy.setMaxPower(targetMax);
        energy.setTransferRates(targetMax, 0L);
    }

    private int subscribeEnergyReceiverToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, ENERGY_PORTS, this);
    }

    private void refreshFluidPortSubscriptions() {
        HbmFluidPortMachine.refreshTransceiverPorts(level, worldPosition, FLUID_PORTS,
                inputTankList, outputTankList, this);
    }

    private boolean processFluidContainers() {
        List<TankSlotTransfer> transfers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            transfers.add(TankSlotTransfer.load(
                    SLOT_FLUID_INPUT_START + i,
                    SLOT_FLUID_INPUT_RETURN_START + i,
                    inputTanks[i]));
            transfers.add(TankSlotTransfer.unload(
                    SLOT_FLUID_OUTPUT_START + i,
                    SLOT_FLUID_OUTPUT_RETURN_START + i,
                    outputTanks[i]));
        }
        return HbmFluidItemTransfer.processTransfers(items, transfers);
    }

    private static boolean isOutputOnlySlot(int slot) {
        return (slot >= SLOT_ITEM_OUTPUT_START && slot <= SLOT_ITEM_OUTPUT_END)
                || (slot >= SLOT_FLUID_INPUT_RETURN_START && slot <= SLOT_FLUID_INPUT_RETURN_END)
                || (slot >= SLOT_FLUID_OUTPUT_RETURN_START && slot <= SLOT_FLUID_OUTPUT_RETURN_END);
    }

    private class ChemicalPlantFluidHandler extends ForgeFluidHandlerAdapter {
        private ChemicalPlantFluidHandler(Runnable onChanged) {
            super(inputTankList, outputTankList, 0, true, true, onChanged);
        }

        @Override
        public boolean isFluidValid(int tank, net.minecraftforge.fluids.FluidStack stack) {
            return tank >= 0 && tank < 3 && super.isFluidValid(tank, stack);
        }

        @Override
        public int fill(net.minecraftforge.fluids.FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            FluidType type = com.hbm.ntm.fluid.HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE) {
                return 0;
            }
            int remaining = resource.getAmount();
            int filled = 0;
            boolean simulate = action.simulate();
            for (HbmFluidTank tank : inputTanks) {
                if (remaining <= 0) {
                    break;
                }
                int accepted = tank.fill(type, remaining, 0, simulate);
                filled += accepted;
                remaining -= accepted;
            }
            if (!simulate && filled > 0) {
                onFluidContentsChanged();
            }
            return filled;
        }
    }
}
