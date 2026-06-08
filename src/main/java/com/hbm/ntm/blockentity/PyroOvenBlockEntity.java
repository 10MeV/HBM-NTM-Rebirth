package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
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
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.PyroOvenMenu;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.PyroOvenRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PyroOvenBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_IDENTIFIER = 3;
    public static final int SLOT_UPGRADE_1 = 4;
    public static final int SLOT_UPGRADE_2 = 5;
    public static final int ITEM_COUNT = 6;

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "prog";
    private static final String TAG_PROGRESSING = "isProgressing";
    private static final String TAG_VENTING = "isVenting";
    private static final String TAG_SMOKE = "smoke";
    private static final long MAX_POWER = 10_000_000L;
    private static final int BASE_CONSUMPTION = 10_000;
    private static final int TANK_CAPACITY = 24_000;
    private static final int SMOKE_CAPACITY = 50;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private final HbmFluidTank smokeTank;
    private final ItemStackHandler items = new ItemStackHandler(ITEM_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY, SLOT_INPUT, SLOT_IDENTIFIER, SLOT_UPGRADE_1, SLOT_UPGRADE_2 -> true;
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
            LazyOptional.of(() -> new PyroOvenExternalItemHandler(items));

    private float progress;
    private boolean progressing;
    private boolean venting;
    private int usage = BASE_CONSUMPTION;
    private int speedLevel;
    private int powerLevel;
    private int overdriveLevel;
    private ResourceLocation lastRecipeId;
    private int prevAnim;
    private int anim;
    private Object audioLoop;

    public PyroOvenBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.SMOKE, SMOKE_CAPACITY));
    }

    private PyroOvenBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy,
            HbmFluidTank inputTank, HbmFluidTank outputTank, HbmFluidTank smokeTank) {
        super(ModBlockEntities.PYRO_OVEN.get(), pos, state, energy, List.of(inputTank, outputTank, smokeTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
        this.smokeTank = smokeTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PyroOvenBlockEntity oven) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, oven);
        boolean changed = oven.tickServer(level);
        if (changed) {
            oven.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PyroOvenBlockEntity oven) {
        if (!level.isClientSide) {
            return;
        }
        oven.tickClient(level, pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public HbmFluidTank getSmokeTank() {
        return smokeTank;
    }

    public float getProgressFraction() {
        return progress;
    }

    public int getProgressPixels(int width) {
        return Math.max(0, Math.min(width, (int) (progress * width)));
    }

    public boolean isProgressing() {
        return progressing;
    }

    public boolean isVenting() {
        return venting;
    }

    public int getUsage() {
        return usage;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public int getOverdriveLevel() {
        return overdriveLevel;
    }

    public int getPrevAnim() {
        return prevAnim;
    }

    public int getAnim() {
        return anim;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank, smokeTank);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(inputTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(outputTank, smokeTank);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == inputTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE
                && ((type == outputTank.getTankType() && outputTank.getFill() > 0)
                || (type == smokeTank.getTankType() && smokeTank.getFill() > 0));
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
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
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts(getBlockState());
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return energyPorts(getBlockState());
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (type == HbmFluids.SMOKE) {
            return smokePorts(getBlockState());
        }
        return fluidPorts(getBlockState());
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
        onFluidContentsChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, HbmInventoryMenuHelper.saveLegacyItems(items));
        tag.putFloat(TAG_PROGRESS, progress);
        tag.putBoolean(TAG_PROGRESSING, progressing);
        tag.putBoolean(TAG_VENTING, venting);
        smokeTank.writeToNbt(tag, TAG_SMOKE);
        tag.putInt("usage", usage);
        tag.putInt("speedLevel", speedLevel);
        tag.putInt("powerLevel", powerLevel);
        tag.putInt("overdriveLevel", overdriveLevel);
        if (lastRecipeId != null) {
            tag.putString("lastRecipe", lastRecipeId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyItems(tag.getCompound(TAG_INVENTORY), items);
        progress = tag.getFloat(TAG_PROGRESS);
        progressing = tag.getBoolean(TAG_PROGRESSING);
        venting = tag.getBoolean(TAG_VENTING);
        if (tag.contains(TAG_SMOKE)) {
            smokeTank.readFromNbt(tag, TAG_SMOKE);
        }
        usage = tag.contains("usage") ? tag.getInt("usage") : BASE_CONSUMPTION;
        speedLevel = tag.getInt("speedLevel");
        powerLevel = tag.getInt("powerLevel");
        overdriveLevel = tag.getInt("overdriveLevel");
        lastRecipeId = tag.contains("lastRecipe") ? ResourceLocation.tryParse(tag.getString("lastRecipe")) : null;
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
        return Component.translatableWithFallback("container.machinePyroOven", "Pyrolysis Oven");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PyroOvenMenu(containerId, inventory, this);
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

    private boolean tickServer(Level level) {
        boolean changed = false;
        long oldPower = energy.getPower();
        int oldInputFill = inputTank.getFill();
        int oldOutputFill = outputTank.getFill();
        int oldSmokeFill = smokeTank.getFill();
        boolean oldProgressing = progressing;
        boolean oldVenting = venting;

        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        changed |= oldPower != energy.getPower();
        changed |= setInputTankTypeFromIdentifier();
        changed |= updateUpgrades();
        if (smokeTank.getFill() > 0) {
            tryProvideFluidToPorts(smokeTank.getTankType(), smokeTank.getPressure(), this);
        }

        progressing = false;
        venting = false;
        PyroOvenRecipe recipe = getMatchingRecipe(level);
        if (canProcess(recipe)) {
            progressing = true;
            energy.setPower(energy.getPower() - usage);
            int effectiveDuration = effectiveDuration(recipe);
            progress += 1.0F / effectiveDuration;
            if (progress >= 1.0F) {
                finishRecipe(recipe);
                progress = 0.0F;
            }
            polluteSoot(level);
            changed = true;
        } else if (progress != 0.0F) {
            progress = 0.0F;
            changed = true;
        }

        if (outputTank.getTankType() != HbmFluids.NONE && outputTank.getFill() > 0) {
            tryProvideFluidToPorts(outputTank.getTankType(), outputTank.getPressure(), this);
        }
        changed |= oldInputFill != inputTank.getFill()
                || oldOutputFill != outputTank.getFill()
                || oldSmokeFill != smokeTank.getFill()
                || oldProgressing != progressing
                || oldVenting != venting
                || oldPower != energy.getPower();
        return changed;
    }

    private void tickClient(Level level, BlockPos pos, BlockState state) {
        prevAnim = anim;
        Direction dir = legacyFacing(state);
        Direction rot = dir.getClockWise();
        if (progressing) {
            anim++;
            audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, ModSounds.BLOCK_PYRO_OPERATE.getId(),
                    true, 50.0D, 15.0F, 1.0F, 1.0F);
            spawnOperatingClouds(level, pos, dir, rot);
        } else {
            audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, ModSounds.BLOCK_PYRO_OPERATE.getId(),
                    false, 50.0D, 15.0F, 1.0F, 1.0F);
        }
        if (venting && level.getGameTime() % 2L == 0L) {
            ParticleUtil.spawnCoolingTower(level, pos.getX() + 0.5D - rot.getStepX(), pos.getY() + 3.0D,
                    pos.getZ() + 0.5D - rot.getStepZ(), 10.0F, 0.25F, 2.5F,
                    100 + level.random.nextInt(20), false, 0.075F, 0.25F, 0x202020);
        }
    }

    private boolean setInputTankTypeFromIdentifier() {
        ItemStack stack = items.getStackInSlot(SLOT_IDENTIFIER);
        if (!(stack.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        FluidType selected = identifier.getIdentifiedFluid(level, worldPosition, stack);
        if (selected == null || selected == inputTank.getTankType()) {
            return false;
        }
        inputTank.setTankType(selected);
        onFluidContentsChanged();
        return true;
    }

    private boolean updateUpgrades() {
        int oldUsage = usage;
        int oldSpeed = speedLevel;
        int oldPower = powerLevel;
        int oldOverdrive = overdriveLevel;
        LegacyMachineUpgradeManager.Levels levels =
                LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_1, SLOT_UPGRADE_2, VALID_UPGRADES);
        speedLevel = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        powerLevel = Math.min(levels.getLevel(UpgradeType.POWER), 3);
        overdriveLevel = Math.min(levels.getLevel(UpgradeType.OVERDRIVE), 3);
        usage = getConsumption(speedLevel + overdriveLevel * 2, powerLevel);
        return oldUsage != usage || oldSpeed != speedLevel || oldPower != powerLevel || oldOverdrive != overdriveLevel;
    }

    private void polluteSoot(Level level) {
        venting = SmokeExhaustPollution.polluteBuffered(level, worldPosition, smokeTank,
                PollutionType.SOOT, PollutionManager.SOOT_PER_SECOND);
    }

    private static int getConsumption(int speed, int powerSaving) {
        return (int) (BASE_CONSUMPTION * Math.pow(speed + 1, 2)) / (powerSaving + 1);
    }

    @Nullable
    private PyroOvenRecipe getMatchingRecipe(Level level) {
        if (lastRecipeId != null) {
            PyroOvenRecipe last = level.getRecipeManager().byKey(lastRecipeId)
                    .filter(PyroOvenRecipe.class::isInstance)
                    .map(PyroOvenRecipe.class::cast)
                    .orElse(null);
            if (last != null && doesRecipeMatch(last)) {
                return last;
            }
        }
        SimpleContainer container = new SimpleContainer(items.getStackInSlot(SLOT_INPUT));
        for (PyroOvenRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.PYRO_OVEN.type().get())) {
            if (recipe.matches(container, level) && doesRecipeMatch(recipe)) {
                lastRecipeId = recipe.getId();
                return recipe;
            }
        }
        lastRecipeId = null;
        return null;
    }

    private boolean doesRecipeMatch(PyroOvenRecipe recipe) {
        ItemStack item = items.getStackInSlot(SLOT_INPUT);
        if (recipe.inputItem().isPresent()) {
            if (item.isEmpty() || !recipe.inputItem().get().test(item)) {
                return false;
            }
        } else if (!item.isEmpty()) {
            return false;
        }
        return recipe.inputFluid().map(fluid -> inputTank.getTankType() == fluid.type()).orElse(true);
    }

    private boolean canProcess(@Nullable PyroOvenRecipe recipe) {
        if (recipe == null || energy.getPower() < usage) {
            return false;
        }
        if (recipe.inputFluid().isPresent()) {
            HbmFluidStack input = recipe.inputFluid().get();
            if (inputTank.getTankType() != input.type()
                    || inputTank.getPressure() != input.pressure()
                    || inputTank.getFill() < input.amount()) {
                return false;
            }
        }
        if (recipe.inputItem().isPresent()) {
            HbmIngredient input = recipe.inputItem().get();
            if (!input.test(items.getStackInSlot(SLOT_INPUT))) {
                return false;
            }
        }
        if (recipe.outputFluid().isPresent()) {
            HbmFluidStack output = recipe.outputFluid().get();
            if (!canAcceptOutputFluid(output)) {
                return false;
            }
        }
        return recipe.outputItem().isEmpty() || canFitOutput(recipe.outputItem().get().representativeStack());
    }

    private boolean canAcceptOutputFluid(HbmFluidStack output) {
        if (output.isEmpty()) {
            return true;
        }
        if (outputTank.getTankType() != output.type() || outputTank.getPressure() != output.pressure()) {
            return true;
        }
        return outputTank.getFill() + output.amount() <= outputTank.getMaxFill();
    }

    private int effectiveDuration(PyroOvenRecipe recipe) {
        int speedDuration = recipe.duration() - speedLevel * (recipe.duration() / 4);
        return Math.max(speedDuration / (overdriveLevel * 2 + 1), 1);
    }

    private void finishRecipe(PyroOvenRecipe recipe) {
        recipe.outputItem().map(output -> output.collapse(level.random)).ifPresent(this::addOutput);
        recipe.outputFluid().ifPresent(this::addOutputFluid);
        recipe.inputItem().ifPresent(input -> items.extractItem(SLOT_INPUT, input.count(), false));
        recipe.inputFluid().ifPresent(input -> inputTank.drain(input.amount(), false));
        onFluidContentsChanged();
    }

    private void addOutputFluid(HbmFluidStack output) {
        if (output.isEmpty()) {
            return;
        }
        if (outputTank.getTankType() != output.type() || outputTank.getPressure() != output.pressure()) {
            outputTank.conform(new HbmFluidStack(output.type(), 0, output.pressure()));
        }
        outputTank.setFill(outputTank.getFill() + output.amount());
    }

    private boolean canFitOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        return existing.isEmpty()
                || ItemHandlerHelper.canItemStacksStack(existing, stack)
                && existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), items.getSlotLimit(SLOT_OUTPUT));
    }

    private void addOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, stack.copy());
            return;
        }
        ItemStack merged = existing.copy();
        merged.grow(stack.getCount());
        items.setStackInSlot(SLOT_OUTPUT, merged);
    }

    private static List<FluidPort> fluidPorts(BlockState state) {
        Direction dir = legacyFacing(state);
        Direction rot = dir.getClockWise();
        List<FluidPort> ports = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            ports.add(FluidPort.of(dir.getStepX() * i + rot.getStepX() * 3, 0,
                    dir.getStepZ() * i + rot.getStepZ() * 3, rot));
        }
        return ports;
    }

    private static List<EnergyPort> energyPorts(BlockState state) {
        Direction dir = legacyFacing(state);
        Direction rot = dir.getClockWise();
        List<EnergyPort> ports = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            ports.add(EnergyPort.of(dir.getStepX() * i + rot.getStepX() * 3, 0,
                    dir.getStepZ() * i + rot.getStepZ() * 3, rot));
        }
        return ports;
    }

    private static List<FluidPort> smokePorts(BlockState state) {
        Direction rot = legacyFacing(state).getClockWise();
        return List.of(FluidPort.of(-rot.getStepX(), 3, -rot.getStepZ(), Direction.UP));
    }

    private static Direction legacyFacing(BlockState state) {
        return state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static void spawnOperatingClouds(Level level, BlockPos pos, Direction dir, Direction rot) {
        spawnCloud(level, pos.getX() + 0.5D - rot.getStepX() - dir.getStepX() * 0.875D,
                pos.getY() + 3.0D, pos.getZ() + 0.5D - rot.getStepZ() - dir.getStepZ() * 0.875D);
        spawnCloud(level, pos.getX() + 0.5D - rot.getStepX() - dir.getStepX() * 2.375D,
                pos.getY() + 3.0D, pos.getZ() + 0.5D - rot.getStepZ() - dir.getStepZ() * 2.375D);
        spawnCloud(level, pos.getX() + 0.5D - rot.getStepX() + dir.getStepX() * 0.875D,
                pos.getY() + 3.0D, pos.getZ() + 0.5D - rot.getStepZ() + dir.getStepZ() * 0.875D);
        spawnCloud(level, pos.getX() + 0.5D - rot.getStepX() + dir.getStepX() * 2.375D,
                pos.getY() + 3.0D, pos.getZ() + 0.5D - rot.getStepZ() + dir.getStepZ() * 2.375D);
    }

    private static void spawnCloud(Level level, double x, double y, double z) {
        if (level.random.nextInt(20) == 0) {
            level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.05D, 0.0D);
        }
    }

    private static final class PyroOvenExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private PyroOvenExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_INPUT)
                    : slot == 1 ? items.getStackInSlot(SLOT_OUTPUT)
                    : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !items.isItemValid(SLOT_INPUT, stack)) {
                return stack;
            }
            return items.insertItem(SLOT_INPUT, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(SLOT_OUTPUT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(SLOT_INPUT)
                    : slot == 1 ? items.getSlotLimit(SLOT_OUTPUT)
                    : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && items.isItemValid(SLOT_INPUT, stack);
        }
    }
}
