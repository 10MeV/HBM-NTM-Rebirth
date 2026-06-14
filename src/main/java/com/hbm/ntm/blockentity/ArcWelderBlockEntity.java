package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ArcWelderMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingFactors;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingResult;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import java.util.List;
import java.util.Map;

public class ArcWelderBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmStandardFluidReceiver, HbmLegacyLoadedTile {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_LEGACY_MAX_POWER = "maxPower";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_PROCESS_TIME = "processTime";
    private static final String TAG_TANK = "t";
    private static final String TAG_DID_PROCESS = "DidProcess";
    private static final long DEFAULT_MAX_POWER = 2_000L;
    private static final int TANK_CAPACITY = 24_000;

    public static final int SLOT_INPUT_0 = 0;
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_FLUID_IDENTIFIER = 5;
    public static final int SLOT_UPGRADE_0 = 6;
    public static final int SLOT_UPGRADE_1 = 7;
    public static final int[] INPUT_SLOTS = new int[] { SLOT_INPUT_0, SLOT_INPUT_1, SLOT_INPUT_2 };
    public static final int[] OUTPUT_SLOTS = new int[] { SLOT_OUTPUT };

    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(8) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT_0, SLOT_INPUT_1, SLOT_INPUT_2 -> true;
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_FLUID_IDENTIFIER -> true;
                case SLOT_UPGRADE_0, SLOT_UPGRADE_1 -> stack.getItem() instanceof ItemMachineUpgrade;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final HbmEnergyStorage energy = new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L);
    private final HbmFluidTank inputTank = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new ArcWelderAccessibleItemHandler());
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() ->
            ForgeRecipeFluidHandlerAdapter.create(List.of(inputTank), List.of(), 0, this::onFluidContentsChanged));

    private int progress;
    private int processTime = 1;
    private long consumption = 100L;
    private boolean didProcess;
    private String selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;

    public ArcWelderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARC_WELDER.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcWelderBlockEntity arcWelder) {
        long oldPower = arcWelder.energy.getPower();
        int oldProgress = arcWelder.progress;
        int oldProcessTime = arcWelder.processTime;
        long oldConsumption = arcWelder.consumption;
        boolean oldDidProcess = arcWelder.didProcess;
        FluidType oldType = arcWelder.inputTank.getTankType();
        int oldFill = arcWelder.inputTank.getFill();

        HbmEnergyUtil.chargeStorageFromItem(arcWelder.items.getStackInSlot(SLOT_BATTERY),
                arcWelder.energy, arcWelder.energy.getReceiverSpeed());
        HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(arcWelder.items, SLOT_FLUID_IDENTIFIER,
                arcWelder.inputTank, level, pos);
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyUtil.subscribeReceiverToPorts(level, pos, arcWelder.connectionEnergyPorts(state), arcWelder);
            HbmFluidPortMachine.refreshReceiverPorts(level, pos, arcWelder.connectionFluidPorts(state),
                    List.of(arcWelder.inputTank), arcWelder);
        }

        boolean changed = arcWelder.tickRecipe(level);
        if (arcWelder.didProcess && level.getGameTime() % 2L == 0L) {
            arcWelder.spawnWeldingParticles(level, state);
        }
        changed |= oldPower != arcWelder.energy.getPower()
                || oldProgress != arcWelder.progress
                || oldProcessTime != arcWelder.processTime
                || oldConsumption != arcWelder.consumption
                || oldDidProcess != arcWelder.didProcess
                || oldType != arcWelder.inputTank.getTankType()
                || oldFill != arcWelder.inputTank.getFill();

        if (changed) {
            arcWelder.setChanged();
        }
        arcWelder.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ArcWelderBlockEntity arcWelder) {
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

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    public long getConsumption() {
        return consumption;
    }

    public boolean isProcessing() {
        return didProcess;
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition() {
        if (level == null) {
            return null;
        }
        return GenericMachineRecipeRuntime.findByInternalName(level, GenericMachineRecipe.Machine.ARC_WELDER, selectedRecipe);
    }

    public ItemStack getDisplayOutput() {
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        if (recipe == null || recipe.getItemOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        return recipe.getItemOutputs().get(0);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private boolean tickRecipe(Level level) {
        didProcess = false;
        GenericMachineRecipe recipe = selectCurrentRecipe(level);
        if (recipe == null) {
            selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
            progress = 0;
            processTime = 1;
            consumption = 100L;
            updateDynamicCapacity(null);
            return false;
        }

        selectedRecipe = recipe.getInternalName();
        UpgradeFactors factors = upgradeFactors(recipe);
        processTime = factors.processTime();
        consumption = factors.consumption();
        updateDynamicCapacity(recipe);

        ProcessingResult result = GenericMachineRecipeRuntime.update(level, GenericMachineRecipe.Machine.ARC_WELDER,
                selectedRecipe, progress / (double) Math.max(processTime, 1), ItemStack.EMPTY, energy, items,
                INPUT_SLOTS, OUTPUT_SLOTS, inputTanksFor(recipe), List.of(),
                new ProcessingFactors(factors.progressPerTick(), factors.powerMultiplier()), true, TANK_CAPACITY,
                worldPosition);
        selectedRecipe = result.selectedRecipe();
        didProcess = result.didProcess();
        progress = (int) Math.round(result.progress() * Math.max(processTime, 1));
        if (result.recipe() == null) {
            progress = 0;
        }
        return result.changed();
    }

    private void spawnWeldingParticles(Level level, BlockState state) {
        Direction dir = state.getValue(HorizontalMachineBlock.FACING);
        double x = worldPosition.getX() + 0.5D - dir.getStepX() * 0.5D;
        double y = worldPosition.getY() + 1.25D;
        double z = worldPosition.getZ() + 0.5D - dir.getStepZ() * 0.5D;
        if (level.getGameTime() % 20L == 0L) {
            ParticleUtil.spawnTau(level, x, y, z, 5, false);
        } else {
            ParticleUtil.spawnHadron(level, x, y, z);
        }
    }

    @Nullable
    private GenericMachineRecipe selectCurrentRecipe(Level level) {
        for (GenericMachineRecipe recipe : GenericMachineRecipeRuntime.recipes(level, GenericMachineRecipe.Machine.ARC_WELDER)) {
            if (GenericMachineRecipeRuntime.canProcess(recipe, items, INPUT_SLOTS, OUTPUT_SLOTS,
                    inputTanksFor(recipe), List.of())) {
                return recipe;
            }
        }
        return null;
    }

    private List<HbmFluidTank> inputTanksFor(GenericMachineRecipe recipe) {
        return recipe.getFluidInputs().isEmpty() ? List.of() : List.of(inputTank);
    }

    private UpgradeFactors upgradeFactors(GenericMachineRecipe recipe) {
        LegacyMachineUpgradeManager.Levels levels = LegacyMachineUpgradeManager.checkSlots(
                items, SLOT_UPGRADE_0, SLOT_UPGRADE_1, VALID_UPGRADES);
        int redLevel = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        int blueLevel = Math.min(levels.getLevel(UpgradeType.POWER), 3);
        int blackLevel = Math.min(levels.getLevel(UpgradeType.OVERDRIVE), 3);

        int legacyProcessTime = recipe.getDuration()
                - (recipe.getDuration() * redLevel / 6)
                + (recipe.getDuration() * blueLevel / 3);
        legacyProcessTime = Math.max(1, legacyProcessTime);
        long legacyConsumption = recipe.getPower()
                + (recipe.getPower() * redLevel)
                - (recipe.getPower() * blueLevel / 6);
        legacyConsumption = Math.max(0L, legacyConsumption);
        legacyConsumption = legacyConsumption << blackLevel;
        double speed = (1.0D + blackLevel) * recipe.getDuration() / (double) legacyProcessTime;
        double power = recipe.getPower() <= 0L ? 0.0D : legacyConsumption / (double) recipe.getPower();
        return new UpgradeFactors(legacyProcessTime, legacyConsumption, speed, power);
    }

    private void updateDynamicCapacity(@Nullable GenericMachineRecipe recipe) {
        long targetMax = recipe == null ? DEFAULT_MAX_POWER : Math.max(DEFAULT_MAX_POWER, consumption * 20L);
        targetMax = Math.max(targetMax, energy.getPower());
        energy.setMaxPower(targetMax);
        energy.setTransferRates(targetMax, 0L);
    }

    private List<EnergyPort> connectionEnergyPorts(BlockState state) {
        Direction dir = state.getValue(HorizontalMachineBlock.FACING);
        Direction rot = dir.getClockWise();
        List<EnergyPort> ports = new ArrayList<>();
        for (PortOffset offset : legacyConnectionOffsets(dir, rot)) {
            ports.add(EnergyPort.of(offset.x(), 0, offset.z(), offset.direction()));
        }
        return ports;
    }

    private List<FluidPort> connectionFluidPorts(BlockState state) {
        Direction dir = state.getValue(HorizontalMachineBlock.FACING);
        Direction rot = dir.getClockWise();
        List<FluidPort> ports = new ArrayList<>();
        for (PortOffset offset : legacyConnectionOffsets(dir, rot)) {
            ports.add(FluidPort.of(offset.x(), 0, offset.z(), offset.direction()));
        }
        return ports;
    }

    private static List<PortOffset> legacyConnectionOffsets(Direction dir, Direction rot) {
        return List.of(
                port(dir, 1, rot, 0, dir),
                port(dir, 1, rot, 1, dir),
                port(dir, 1, rot, -1, dir),
                port(dir, -2, rot, 0, dir.getOpposite()),
                port(dir, -2, rot, 1, dir.getOpposite()),
                port(dir, -2, rot, -1, dir.getOpposite()),
                port(dir, 0, rot, 2, rot),
                port(dir, -1, rot, 2, rot),
                port(dir, 0, rot, -2, rot.getOpposite()),
                port(dir, -1, rot, -2, rot.getOpposite()));
    }

    private static PortOffset port(Direction dir, int forward, Direction rot, int side, Direction face) {
        return new PortOffset(dir.getStepX() * forward + rot.getStepX() * side,
                dir.getStepZ() * forward + rot.getStepZ() * side, face);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_PROCESS_TIME, processTime);
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
        inputTank.writeToNbt(tag, TAG_TANK);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        if (tag.contains(TAG_LEGACY_MAX_POWER)) {
            energy.setMaxPower(Math.max(DEFAULT_MAX_POWER, tag.getLong(TAG_LEGACY_MAX_POWER)));
        }
        progress = tag.getInt(TAG_PROGRESS);
        processTime = Math.max(1, tag.getInt(TAG_PROCESS_TIME));
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
        if (tag.contains(TAG_TANK)) {
            inputTank.readFromNbt(tag, TAG_TANK);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(saveWithoutMetadata());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            load(tag);
        }
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
        return Component.translatableWithFallback("container.machineArcWelder", "Arc Welder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ArcWelderMenu(containerId, inventory, this);
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

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(inputTank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    private class ArcWelderAccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 4;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            if (mapped < SLOT_INPUT_0 || mapped > SLOT_INPUT_2) {
                return stack;
            }
            return items.insertItem(mapped, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            return mapped == SLOT_OUTPUT ? items.extractItem(mapped, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapExternalSlot(slot);
            return mapped >= SLOT_INPUT_0 && mapped <= SLOT_INPUT_2 && items.isItemValid(mapped, stack);
        }

        private int mapExternalSlot(int slot) {
            return switch (slot) {
                case 0 -> SLOT_INPUT_0;
                case 1 -> SLOT_INPUT_1;
                case 2 -> SLOT_INPUT_2;
                case 3 -> SLOT_OUTPUT;
                default -> -1;
            };
        }
    }

    private record UpgradeFactors(int processTime, long consumption, double progressPerTick, double powerMultiplier) {
    }

    private record PortOffset(int x, int z, Direction direction) {
    }
}
