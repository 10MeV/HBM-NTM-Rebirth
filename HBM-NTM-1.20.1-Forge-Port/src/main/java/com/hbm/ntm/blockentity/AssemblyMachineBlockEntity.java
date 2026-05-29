package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.menu.AssemblyMachineMenu;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AssemblyMachineBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver, HbmStandardFluidTransceiver, HbmTileSyncable {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_DID_PROCESS = "DidProcess";
    private static final String TAG_RING = "Ring";
    private static final String TAG_RING_TARGET = "RingTarget";
    private static final String TAG_RING_SPEED = "RingSpeed";
    private static final String TAG_RING_DELAY = "RingDelay";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_LEGACY_MAX_POWER = "maxPower";
    private static final String TAG_INPUT_TANK = "i";
    private static final String TAG_OUTPUT_TANK = "o";
    private static final String TAG_PROGRESS = "progress0";
    private static final String TAG_RECIPE = "recipe0";
    private static final String TAG_CONTROL_INDEX = "index";
    private static final String TAG_CONTROL_SELECTION = "selection";
    private static final long DEFAULT_MAX_POWER = 100_000L;
    private static final int TANK_CAPACITY = 4_000;
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 3;
    public static final int SLOT_INPUT_START = 4;
    public static final int SLOT_INPUT_END = 15;
    public static final int SLOT_OUTPUT = 16;
    public static final int[] INPUT_SLOTS = new int[] { 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
    public static final int[] OUTPUT_SLOTS = new int[] { SLOT_OUTPUT };
    private static final List<EnergyPort> ENERGY_PORTS = LegacyMultiblockPorts.xrFloorRingEnergyPorts(2);
    private static final List<FluidPort> FLUID_PORTS = LegacyMultiblockPorts.xrFloorRingFluidPorts(2);

    private final ItemStackHandler items = new ItemStackHandler(17) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != SLOT_OUTPUT;
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
    private final HbmFluidTank inputTank = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
    private final HbmFluidTank outputTank = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(AssemblyFluidHandler::new);
    private final AssemblerArm[] arms = new AssemblerArm[] { new AssemblerArm(1L), new AssemblerArm(2L) };

    private boolean didProcess;
    private double prevRing;
    private double ring;
    private double ringTarget;
    private double ringSpeed;
    private int ringDelay;
    private double progress;
    private String selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;

    public AssemblyMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_MACHINE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AssemblyMachineBlockEntity blockEntity) {
        long oldPower = blockEntity.energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(blockEntity.items.getStackInSlot(SLOT_BATTERY), blockEntity, blockEntity.getReceiverSpeed());
        blockEntity.subscribeEnergyReceiverToPorts();
        blockEntity.refreshFluidPortSubscriptions();
        boolean changed = blockEntity.tickRecipe(level);
        changed = changed || oldPower != blockEntity.energy.getPower();
        if (changed) {
            blockEntity.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AssemblyMachineBlockEntity blockEntity) {
        blockEntity.prevRing = blockEntity.ring;
        boolean animate = blockEntity.didProcess || blockEntity.previewAnimation();
        if (animate) {
            blockEntity.updateRing(level);
        }
        for (AssemblerArm arm : blockEntity.arms) {
            arm.updateInterp();
            if (animate) {
                arm.updateArm();
            } else {
                arm.returnToNullPos();
            }
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmEnergyStorage getEnergyStorage() {
        return energy;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(inputTank, outputTank);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank);
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
        this.selectedRecipe = selectedRecipe == null || selectedRecipe.isBlank()
                ? GenericMachineRecipeRuntime.NULL_RECIPE
                : selectedRecipe;
        this.progress = 0.0D;
        setChanged();
    }

    public boolean selectRecipe(String selectedRecipe) {
        if (level == null || GenericMachineRecipeRuntime.NULL_RECIPE.equals(selectedRecipe)) {
            setSelectedRecipe(GenericMachineRecipeRuntime.NULL_RECIPE);
            return true;
        }
        if (!GenericMachineRecipeRuntime.hasRecipe(level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, selectedRecipe)) {
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
        return GenericMachineRecipeRuntime.findByInternalName(level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, selectedRecipe);
    }

    public boolean canProcessSelectedRecipe() {
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        return recipe != null
                && energy.getPower() >= recipe.getPower()
                && GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                List.of(inputTank), List.of(outputTank));
    }

    public static CompoundTag recipeSelectionTag(String selection) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_CONTROL_INDEX, 0);
        tag.putString(TAG_CONTROL_SELECTION, selection == null ? GenericMachineRecipeRuntime.NULL_RECIPE : selection);
        return tag;
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

    public double getRing(float partialTick) {
        return Mth.lerp(partialTick, prevRing, ring);
    }

    public AssemblerArm getArm(int index) {
        return arms[index];
    }

    public boolean shouldRenderFrame() {
        return level != null && !level.getBlockState(worldPosition.above(3)).isAir();
    }

    private boolean previewAnimation() {
        return level != null && level.isClientSide && level.getGameTime() % 120L < 80L;
    }

    private void updateRing(Level level) {
        if (ring != ringTarget) {
            double ringDelta = Math.abs(ringTarget - ring);
            if (ringDelta <= ringSpeed) {
                ring = ringTarget;
            }
            if (ringTarget > ring) {
                ring += ringSpeed;
            }
            if (ringTarget < ring) {
                ring -= ringSpeed;
            }
            if (ringTarget == ring) {
                double sub = ringTarget >= 360.0D ? -360.0D : 360.0D;
                ringTarget += sub;
                ring += sub;
                prevRing += sub;
                ringDelay = 20 + level.random.nextInt(21);
            }
        } else {
            if (ringDelay > 0) {
                ringDelay--;
            }
            if (ringDelay <= 0) {
                ringTarget += (level.random.nextDouble() * 2.0D - 1.0D) * 135.0D;
                ringSpeed = 10.0D + level.random.nextDouble() * 5.0D;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        inputTank.writeToNbt(tag, TAG_INPUT_TANK);
        outputTank.writeToNbt(tag, TAG_OUTPUT_TANK);
        tag.putDouble(TAG_PROGRESS, progress);
        tag.putString(TAG_RECIPE, selectedRecipe);
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
        tag.putDouble(TAG_RING, ring);
        tag.putDouble(TAG_RING_TARGET, ringTarget);
        tag.putDouble(TAG_RING_SPEED, ringSpeed);
        tag.putInt(TAG_RING_DELAY, ringDelay);
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
        if (tag.contains(TAG_INPUT_TANK)) {
            inputTank.readFromNbt(tag, TAG_INPUT_TANK);
        }
        if (tag.contains(TAG_OUTPUT_TANK)) {
            outputTank.readFromNbt(tag, TAG_OUTPUT_TANK);
        }
        progress = tag.getDouble(TAG_PROGRESS);
        selectedRecipe = tag.getString(TAG_RECIPE);
        if (selectedRecipe.isBlank()) {
            selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
        }
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
        ring = tag.getDouble(TAG_RING);
        prevRing = ring;
        ringTarget = tag.getDouble(TAG_RING_TARGET);
        ringSpeed = tag.getDouble(TAG_RING_SPEED);
        ringDelay = tag.getInt(TAG_RING_DELAY);
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
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.machineAssemblyMachine");
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag.getInt(TAG_CONTROL_INDEX) == 0 && tag.contains(TAG_CONTROL_SELECTION);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (tag.getInt(TAG_CONTROL_INDEX) == 0) {
            selectRecipe(tag.getString(TAG_CONTROL_SELECTION));
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
        return new AssemblyMachineMenu(containerId, inventory, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
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
            return fluidHandler.cast();
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
                level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, selectedRecipe);
        if (recipe == null) {
            progress = 0.0D;
            updateDynamicCapacity(null);
            return wasProcessing || oldProgress != progress;
        }

        GenericMachineRecipe switchedRecipe = GenericMachineRecipeRuntime.findAutoSwitchRecipe(
                level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, recipe, items.getStackInSlot(INPUT_SLOTS[0]));
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
                List.of(inputTank), List.of(outputTank));
        if (!canProcess) {
            progress = 0.0D;
            return wasProcessing || oldProgress != progress;
        }

        energy.setPower(energy.getPower() - recipe.getPower());
        int duration = Math.max(recipe.getDuration(), 1);
        progress = Math.min(1.0D, progress + 1.0D / duration);
        didProcess = true;

        if (progress >= 1.0D) {
            GenericMachineRecipeRuntime.consumeInputs(recipe, items, INPUT_SLOTS, List.of(inputTank));
            GenericMachineRecipeRuntime.produceOutputs(recipe, items, OUTPUT_SLOTS, List.of(outputTank));
            if (GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                    List.of(inputTank), List.of(outputTank))
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
        conformTank(inputTank, recipe.getFluidInputs().isEmpty() ? null : recipe.getFluidInputs().get(0));
        conformTank(outputTank, recipe.getFluidOutputs().isEmpty() ? null : recipe.getFluidOutputs().get(0));
    }

    private void conformTank(HbmFluidTank tank, @Nullable HbmFluidStack stack) {
        if (stack == null) {
            tank.resetTank();
            tank.changeTankSize(TANK_CAPACITY);
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
                List.of(inputTank), List.of(outputTank), this);
    }

    private class AssemblyFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            HbmFluidTank hbmTank = getTank(tank);
            return hbmTank == null ? FluidStack.EMPTY : HbmFluidForgeMappings.toForge(hbmTank.getTankType(), hbmTank.getFill());
        }

        @Override
        public int getTankCapacity(int tank) {
            HbmFluidTank hbmTank = getTank(tank);
            return hbmTank == null ? 0 : hbmTank.getMaxFill();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            if (tank != 0 || stack == null || stack.isEmpty()) {
                return false;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(stack);
            return type != HbmFluids.NONE && inputTank.canAccept(type, 0);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return 0;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE) {
                return 0;
            }
            int filled = inputTank.fill(type, resource.getAmount(), 0, action.simulate());
            if (!action.simulate() && filled > 0) {
                onFluidContentsChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource == null || resource.isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidType type = HbmFluidForgeMappings.fromForge(resource);
            if (type == HbmFluids.NONE || outputTank.getTankType() != type) {
                return FluidStack.EMPTY;
            }
            int drained = outputTank.drain(resource.getAmount(), action.simulate());
            if (!action.simulate() && drained > 0) {
                onFluidContentsChanged();
            }
            return drained <= 0 ? FluidStack.EMPTY : new FluidStack(resource.getFluid(), drained);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || outputTank.isEmpty() || !HbmFluidForgeMappings.canExport(outputTank.getTankType())) {
                return FluidStack.EMPTY;
            }
            FluidType type = outputTank.getTankType();
            int drained = outputTank.drain(maxDrain, action.simulate());
            if (!action.simulate() && drained > 0) {
                onFluidContentsChanged();
            }
            return HbmFluidForgeMappings.toForge(type, drained);
        }

        @Nullable
        private HbmFluidTank getTank(int tank) {
            return tank == 0 ? inputTank : tank == 1 ? outputTank : null;
        }
    }

    public static class AssemblerArm {
        private static final double[][] POSITIONS = new double[][] {
                {45.0D, -15.0D, -5.0D},
                {15.0D, 15.0D, -15.0D},
                {25.0D, 10.0D, -15.0D},
                {30.0D, 0.0D, -10.0D},
                {70.0D, -10.0D, -25.0D}
        };

        private final java.util.Random random;
        private final double[] angles = new double[4];
        private final double[] prevAngles = new double[4];
        private final double[] targetAngles = new double[4];
        private final double[] speed = new double[4];
        private ArmActionState state = ArmActionState.ASSUME_POSITION;
        private int actionDelay;

        private AssemblerArm(long seed) {
            this.random = new java.util.Random(seed);
            resetSpeed();
        }

        private void updateInterp() {
            System.arraycopy(angles, 0, prevAngles, 0, angles.length);
        }

        private void returnToNullPos() {
            for (int i = 0; i < 4; i++) {
                targetAngles[i] = 0.0D;
            }
            for (int i = 0; i < 3; i++) {
                speed[i] = 3.0D;
            }
            speed[3] = 0.25D;
            state = ArmActionState.RETRACT_STRIKER;
            move();
        }

        private void updateArm() {
            resetSpeed();
            if (actionDelay > 0) {
                actionDelay--;
                return;
            }
            switch (state) {
                case ASSUME_POSITION -> {
                    if (move()) {
                        actionDelay = 2;
                        state = ArmActionState.EXTEND_STRIKER;
                        targetAngles[3] = -0.75D;
                    }
                }
                case EXTEND_STRIKER -> {
                    if (move()) {
                        state = ArmActionState.RETRACT_STRIKER;
                        targetAngles[3] = 0.0D;
                    }
                }
                case RETRACT_STRIKER -> {
                    if (move()) {
                        actionDelay = 2 + random.nextInt(5);
                        chooseNewArmPosition();
                        state = ArmActionState.ASSUME_POSITION;
                    }
                }
            }
        }

        private void resetSpeed() {
            speed[0] = 15.0D;
            speed[1] = 15.0D;
            speed[2] = 15.0D;
            speed[3] = 0.5D;
        }

        private void chooseNewArmPosition() {
            int chosen = random.nextInt(POSITIONS.length);
            targetAngles[0] = POSITIONS[chosen][0];
            targetAngles[1] = POSITIONS[chosen][1];
            targetAngles[2] = POSITIONS[chosen][2];
        }

        private boolean move() {
            boolean didMove = false;
            for (int i = 0; i < angles.length; i++) {
                if (angles[i] == targetAngles[i]) {
                    continue;
                }
                didMove = true;
                double delta = Math.abs(angles[i] - targetAngles[i]);
                if (delta <= speed[i]) {
                    angles[i] = targetAngles[i];
                } else if (angles[i] < targetAngles[i]) {
                    angles[i] += speed[i];
                } else {
                    angles[i] -= speed[i];
                }
            }
            return !didMove;
        }

        public double[] getPositions(float partialTick) {
            return new double[] {
                    Mth.lerp(partialTick, prevAngles[0], angles[0]),
                    Mth.lerp(partialTick, prevAngles[1], angles[1]),
                    Mth.lerp(partialTick, prevAngles[2], angles[2]),
                    Mth.lerp(partialTick, prevAngles[3], angles[3])
            };
        }

        private enum ArmActionState {
            ASSUME_POSITION,
            EXTEND_STRIKER,
            RETRACT_STRIKER
        }
    }
}
