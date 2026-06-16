package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.MixerMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.recipe.MixerRecipe;
import com.hbm.ntm.recipe.MixerRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MixerBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyButtonReceiver, HbmFluidCopiable {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_SOLID_INPUT = 1;
    public static final int SLOT_FLUID_ID = 2;
    public static final int SLOT_UPGRADE_1 = 3;
    public static final int SLOT_UPGRADE_2 = 4;
    public static final int SLOT_COUNT = 5;
    public static final int CONTROL_NEXT_RECIPE = 0;

    private static final String TAG_INVENTORY = "items";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_PROCESS_TIME = "processTime";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_WAS_ON = "wasOn";
    private static final String TAG_ROTATION = "rotation";
    private static final long MAX_POWER = 10_000L;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 6);

    private final HbmFluidTank inputTank1;
    private final HbmFluidTank inputTank2;
    private final HbmFluidTank outputTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_SOLID_INPUT -> isValidSolidInput(stack);
                case SLOT_FLUID_ID -> true;
                case SLOT_UPGRADE_1, SLOT_UPGRADE_2 -> stack.getItem() instanceof ItemMachineUpgrade;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private final LazyOptional<IEnergyStorage> energyInput =
            LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));

    private int progress;
    private int processTime;
    private int recipeIndex;
    private boolean wasOn;
    private int consumption = 50;
    private float rotation;
    private float prevRotation;

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, 16_000),
                new HbmFluidTank(HbmFluids.NONE, 16_000),
                new HbmFluidTank(HbmFluids.NONE, 24_000));
    }

    private MixerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank1,
            HbmFluidTank inputTank2, HbmFluidTank outputTank) {
        super(ModBlockEntities.MIXER.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(inputTank1, inputTank2, outputTank));
        this.inputTank1 = inputTank1;
        this.inputTank2 = inputTank2;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixerBlockEntity mixer) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, mixer);
        long oldPower = mixer.energy.getPower();
        int oldProgress = mixer.progress;
        int oldProcessTime = mixer.processTime;
        int oldRecipe = mixer.recipeIndex;
        int oldConsumption = mixer.consumption;
        boolean oldOn = mixer.wasOn;
        HbmFluidTank.TankState oldInput1 = mixer.inputTank1.snapshot();
        HbmFluidTank.TankState oldInput2 = mixer.inputTank2.snapshot();
        HbmFluidTank.TankState oldOutput = mixer.outputTank.snapshot();

        HbmEnergyUtil.chargeStorageFromItem(mixer.items.getStackInSlot(SLOT_BATTERY),
                mixer.energy, mixer.energy.getReceiverSpeed());
        mixer.setFluidTankTypeFromIdentifierSlot(mixer.items, SLOT_FLUID_ID, mixer.outputTank);
        mixer.refreshTrackedTransceiverFluidPortsReport(mixer.getReceivingTanks(), mixer.getSendingTanks(), mixer);

        boolean changed = mixer.tickRecipe(level);
        changed |= oldPower != mixer.energy.getPower()
                || oldProgress != mixer.progress
                || oldProcessTime != mixer.processTime
                || oldRecipe != mixer.recipeIndex
                || oldConsumption != mixer.consumption
                || oldOn != mixer.wasOn
                || !oldInput1.equals(mixer.inputTank1.snapshot())
                || !oldInput2.equals(mixer.inputTank2.snapshot())
                || !oldOutput.equals(mixer.outputTank.snapshot());

        mixer.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            mixer.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MixerBlockEntity mixer) {
        mixer.prevRotation = mixer.rotation;
        if (mixer.wasOn) {
            mixer.rotation += 20.0F;
        }
        if (mixer.rotation >= 360.0F) {
            mixer.rotation -= 360.0F;
            mixer.prevRotation -= 360.0F;
        }
    }

    private boolean tickRecipe(Level level) {
        MixerRecipe recipe = getSelectedRecipe(level);
        setupInputTankTypes(recipe);
        LegacyMachineUpgradeManager.Levels upgrades =
                LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_1, SLOT_UPGRADE_2, VALID_UPGRADES);
        consumption = calculateConsumption(upgrades);
        wasOn = canProcess(recipe);
        if (!wasOn) {
            progress = 0;
            return false;
        }

        progress++;
        energy.setPower(energy.getPower() - consumption);
        processTime = calculateProcessTime(recipe, upgrades);
        if (progress >= processTime) {
            process(recipe);
            progress = 0;
        }
        return true;
    }

    @Nullable
    public MixerRecipe getSelectedRecipe(Level level) {
        if (outputTank.getTankType() == HbmFluids.NONE) {
            recipeIndex = 0;
            return null;
        }
        List<MixerRecipe> recipes = MixerRecipeRuntime.recipesForOutput(level, outputTank.getTankType());
        if (recipes.isEmpty()) {
            recipeIndex = 0;
            return null;
        }
        recipeIndex = Math.floorMod(recipeIndex, recipes.size());
        return recipes.get(recipeIndex);
    }

    public List<MixerRecipe> getRecipesForOutput() {
        return level == null ? List.of() : MixerRecipeRuntime.recipesForOutput(level, outputTank.getTankType());
    }

    private void setupInputTankTypes(@Nullable MixerRecipe recipe) {
        inputTank1.setTankType(recipe == null ? HbmFluids.NONE
                : recipe.input1().map(input -> input.type()).orElse(HbmFluids.NONE));
        inputTank2.setTankType(recipe == null ? HbmFluids.NONE
                : recipe.input2().map(input -> input.type()).orElse(HbmFluids.NONE));
    }

    private boolean canProcess(@Nullable MixerRecipe recipe) {
        if (recipe == null || energy.getPower() < consumption) {
            return false;
        }
        if (outputTank.getTankType() != recipe.output().type()
                || outputTank.getFill() + recipe.output().amount() > outputTank.getMaxFill()) {
            return false;
        }
        if (recipe.input1().isPresent() && !hasFluid(inputTank1, recipe.input1().get())) {
            return false;
        }
        if (recipe.input2().isPresent() && !hasFluid(inputTank2, recipe.input2().get())) {
            return false;
        }
        return recipe.solidInput().map(input -> input.test(items.getStackInSlot(SLOT_SOLID_INPUT))).orElse(true);
    }

    private static boolean hasFluid(HbmFluidTank tank, com.hbm.ntm.fluid.HbmFluidStack stack) {
        return tank.getTankType() == stack.type()
                && tank.getPressure() == stack.pressure()
                && tank.getFill() >= stack.amount();
    }

    private void process(MixerRecipe recipe) {
        recipe.input1().ifPresent(input -> inputTank1.drain(input.amount(), false));
        recipe.input2().ifPresent(input -> inputTank2.drain(input.amount(), false));
        recipe.solidInput().ifPresent(input -> items.extractItem(SLOT_SOLID_INPUT, input.count(), false));
        outputTank.fill(recipe.output().type(), recipe.output().amount(), recipe.output().pressure(), false);
        onFluidContentsChanged();
    }

    private static int calculateConsumption(LegacyMachineUpgradeManager.Levels upgrades) {
        int speed = Math.min(upgrades.getLevel(UpgradeType.SPEED), 3);
        int power = Math.min(upgrades.getLevel(UpgradeType.POWER), 3);
        int overdrive = Math.min(upgrades.getLevel(UpgradeType.OVERDRIVE), 6);
        int result = 50 + speed * 150;
        result -= (int) (result * power * 0.25D);
        result *= overdrive * 3 + 1;
        return Math.max(1, result);
    }

    private static int calculateProcessTime(MixerRecipe recipe, LegacyMachineUpgradeManager.Levels upgrades) {
        int speed = Math.min(upgrades.getLevel(UpgradeType.SPEED), 3);
        int overdrive = Math.min(upgrades.getLevel(UpgradeType.OVERDRIVE), 6);
        int result = recipe.duration();
        result -= result * speed / 4;
        result /= overdrive + 1;
        return Math.max(1, result);
    }

    private boolean isValidSolidInput(ItemStack stack) {
        if (level == null) {
            return true;
        }
        MixerRecipe recipe = getSelectedRecipe(level);
        return recipe != null && recipe.solidInput().map(input -> input.test(stack, true)).orElse(false);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank1() {
        return inputTank1;
    }

    public HbmFluidTank getInputTank2() {
        return inputTank2;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return Math.max(1, processTime);
    }

    public int getRecipeIndex() {
        return recipeIndex;
    }

    public int getConsumption() {
        return consumption;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public float getRotation(float partialTick) {
        return prevRotation + (rotation - prevRotation) * partialTick;
    }

    public int getTotalFluidFill() {
        int total = 0;
        for (HbmFluidTank tank : getAllTanks()) {
            if (tank.getTankType() != HbmFluids.NONE) {
                total += tank.getFill();
            }
        }
        return total;
    }

    public int getTotalFluidCapacity() {
        int total = 0;
        for (HbmFluidTank tank : getAllTanks()) {
            if (tank.getTankType() != HbmFluids.NONE) {
                total += tank.getMaxFill();
            }
        }
        return total;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(
                EnergyPort.of(0, -1, 0, Direction.DOWN),
                EnergyPort.of(1, 0, 0, Direction.EAST),
                EnergyPort.of(-1, 0, 0, Direction.WEST),
                EnergyPort.of(0, 0, 1, Direction.SOUTH),
                EnergyPort.of(0, 0, -1, Direction.NORTH));
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return List.of(
                FluidPort.of(0, -1, 0, Direction.DOWN),
                FluidPort.of(1, 0, 0, Direction.EAST),
                FluidPort.of(-1, 0, 0, Direction.WEST),
                FluidPort.of(0, 0, 1, Direction.SOUTH),
                FluidPort.of(0, 0, -1, Direction.NORTH));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return getReceivingTanks();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank1, inputTank2);
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
        long before = outputTank.getFill();
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (before != outputTank.getFill()) {
            onFluidContentsChanged();
        }
    }

    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return outputTank;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineMixer", "Industrial Mixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MixerMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_NEXT_RECIPE
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_NEXT_RECIPE) {
            recipeIndex++;
            progress = 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_PROCESS_TIME, processTime);
        tag.putInt(TAG_RECIPE, recipeIndex);
        tag.putBoolean(TAG_WAS_ON, wasOn);
        tag.putFloat(TAG_ROTATION, rotation);
        inputTank1.writeToNbt(tag, "0");
        inputTank2.writeToNbt(tag, "1");
        outputTank.writeToNbt(tag, "2");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        progress = tag.getInt(TAG_PROGRESS);
        processTime = tag.getInt(TAG_PROCESS_TIME);
        recipeIndex = tag.getInt(TAG_RECIPE);
        wasOn = tag.getBoolean(TAG_WAS_ON);
        rotation = tag.getFloat(TAG_ROTATION);
        prevRotation = rotation;
        inputTank1.readFromNbt(tag, "0");
        inputTank2.readFromNbt(tag, "1");
        outputTank.readFromNbt(tag, "2");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyInput.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyInput.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_SOLID_INPUT) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 ? items.insertItem(SLOT_SOLID_INPUT, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(SLOT_SOLID_INPUT) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && items.isItemValid(SLOT_SOLID_INPUT, stack);
        }
    }
}
