package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidPortSubscriptionTracker;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.PrecassMenu;
import com.hbm.ntm.menu.PurexMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingFactors;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingResult;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LegacyGenericSelectorMachineBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmStandardFluidTransceiver, HbmLegacyLoadedTile, LegacyLookOverlayProvider {
    private static final String TAG_KIND = "kind";
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_LEGACY_MAX_POWER = "maxPower";
    private static final String TAG_PROGRESS = "progress0";
    private static final String TAG_RECIPE = "recipe0";
    private static final String TAG_DID_PROCESS = "DidProcess";
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_BLUEPRINT = 1;
    public static final int SLOT_UPGRADE_START = 2;
    public static final int SLOT_UPGRADE_END = 3;

    private final Kind kind;
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final HbmFluidPortSubscriptionTracker fluidPortSubscriptions = new HbmFluidPortSubscriptionTracker();
    private final ItemStackHandler items;
    private final HbmEnergyStorage energy;
    private final HbmFluidTank[] inputTanks;
    private final HbmFluidTank[] outputTanks;
    private final List<HbmFluidTank> inputTankList;
    private final List<HbmFluidTank> outputTankList;
    private final List<HbmFluidTank> allTankList;
    private final LazyOptional<IItemHandler> itemHandler;
    private final LazyOptional<IEnergyStorage> energyHandler;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private final LazyOptional<IFluidHandler> allFluidHandler;

    private double progress;
    private String selectedRecipe = GenericMachineRecipeRuntime.NULL_RECIPE;
    private boolean didProcess;
    private boolean frame;
    private Object audioLoop;
    private boolean wasClientProcessing;
    private int prevAnim;
    private int anim;
    private double prevRing;
    private double ring;
    private double ringSpeed;
    private double ringTarget;
    private int ringDelay;
    private final double[] armAngles = new double[] { 45.0D, -15.0D, -5.0D };
    private final double[] prevArmAngles = new double[] { 45.0D, -15.0D, -5.0D };
    private final double[] strikers = new double[4];
    private final double[] prevStrikers = new double[4];
    private final boolean[] strikerDir = new boolean[4];
    private int strikerIndex;
    private int strikerDelay;

    public LegacyGenericSelectorMachineBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, inferKind(state));
    }

    public LegacyGenericSelectorMachineBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super(ModBlockEntities.LEGACY_GENERIC_SELECTOR_MACHINE.get(), pos, state);
        this.kind = kind;
        this.items = createItemHandler(kind);
        this.energy = new HbmEnergyStorage(kind.defaultMaxPower, kind.defaultMaxPower, 0L);
        this.inputTanks = createTanks(kind.inputTankCount, kind.tankCapacity);
        this.outputTanks = createTanks(kind.outputTankCount, kind.tankCapacity);
        this.inputTankList = Arrays.asList(inputTanks);
        this.outputTankList = Arrays.asList(outputTanks);
        this.allTankList = java.util.stream.Stream.concat(inputTankList.stream(), outputTankList.stream()).toList();
        this.itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
        this.energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
        this.fluidHandler = LazyOptional.of(() -> ForgeRecipeFluidHandlerAdapter.create(inputTankList,
                outputTankList, 0, this::onFluidContentsChanged));
        this.allFluidHandler = LazyOptional.of(() -> ForgeRecipeFluidHandlerAdapter.create(inputTankList,
                outputTankList, 0, this::onFluidContentsChanged));
    }

    private static Kind inferKind(BlockState state) {
        if (state.is(ModBlocks.MACHINE_PUREX.get())) {
            return Kind.PUREX;
        }
        return Kind.PRECASS;
    }

    private static HbmFluidTank[] createTanks(int count, int capacity) {
        HbmFluidTank[] tanks = new HbmFluidTank[count];
        for (int i = 0; i < count; i++) {
            tanks[i] = new HbmFluidTank(HbmFluids.NONE, capacity);
        }
        return tanks;
    }

    private ItemStackHandler createItemHandler(Kind kind) {
        return new ItemStackHandler(kind.slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_BATTERY) {
                    return true;
                }
                if (slot == SLOT_BLUEPRINT) {
                    return stack.getItem() instanceof ItemBlueprints;
                }
                if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
                    return stack.getItem() instanceof ItemMachineUpgrade;
                }
                if (kind.isInputSlot(slot)) {
                    GenericMachineRecipe recipe = getSelectedRecipeDefinition();
                    return level != null && GenericMachineRecipeRuntime.isItemValidForCurrentRecipe(
                            recipe, kind.recipeMachine, level, slot, stack, kind.inputSlots);
                }
                return false;
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            LegacyGenericSelectorMachineBlockEntity blockEntity) {
        long oldPower = blockEntity.energy.getPower();
        double oldProgress = blockEntity.progress;
        boolean oldProcessing = blockEntity.didProcess;

        HbmEnergyUtil.chargeStorageFromItem(blockEntity.items.getStackInSlot(SLOT_BATTERY),
                blockEntity, blockEntity.getReceiverSpeed());
        blockEntity.subscribeEnergyReceiverToPorts();
        blockEntity.refreshFluidPortSubscriptions();
        boolean changed = blockEntity.tickRecipe(level);
        changed |= oldPower != blockEntity.energy.getPower()
                || oldProgress != blockEntity.progress
                || oldProcessing != blockEntity.didProcess;
        if (changed) {
            blockEntity.setChanged();
        }
        blockEntity.networkPackNT(100);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state,
            LegacyGenericSelectorMachineBlockEntity blockEntity) {
        if (blockEntity.kind == Kind.PUREX) {
            blockEntity.prevAnim = blockEntity.anim;
            if (blockEntity.didProcess) {
                blockEntity.anim++;
            }
            if (level.getGameTime() % 20L == 0L) {
                blockEntity.frame = !level.getBlockState(pos.above(5)).isAir();
            }
            return;
        }

        if (level.getGameTime() % 20L == 0L) {
            blockEntity.frame = !level.getBlockState(pos.above(3)).isAir();
        }
        blockEntity.updatePrecassAudio();
        for (int i = 0; i < 3; i++) {
            blockEntity.prevArmAngles[i] = blockEntity.armAngles[i];
        }
        for (int i = 0; i < 4; i++) {
            blockEntity.prevStrikers[i] = blockEntity.strikers[i];
        }
        blockEntity.prevRing = blockEntity.ring;
        blockEntity.updatePrecassStrikers();
        blockEntity.updatePrecassRing(level);
        blockEntity.updatePrecassArms(level);
    }

    private void updatePrecassAudio() {
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.motor",
                didProcess, 50.0D, 15.0F, 0.5F, 0.75F);
        if (wasClientProcessing && !didProcess) {
            LegacyMachineAudioBridge.playLocal(this, "hbm:block.assemblerStop", 0.25F, 1.25F, 50.0D);
        }
        wasClientProcessing = didProcess;
    }

    private void updatePrecassStrikers() {
        for (int i = 0; i < 4; i++) {
            if (strikerDir[i]) {
                strikers[i] = -0.75D;
                strikerDir[i] = false;
                LegacyMachineAudioBridge.playLocal(this, "hbm:block.assemblerStrike", 0.5F, 1.25F, 50.0D);
            } else {
                strikers[i] = Mth.clamp(strikers[i] + 0.5D, -0.75D, 0.0D);
            }
        }
    }

    private void updatePrecassRing(Level level) {
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
                ringDelay = 100 + level.random.nextInt(21);
            }
        }
        if (didProcess && ring == ringTarget) {
            if (ringDelay > 0) {
                ringDelay--;
            }
            if (ringDelay <= 0) {
                ringTarget += 45.0D * (level.random.nextBoolean() ? -1.0D : 1.0D);
                ringSpeed = 10.0D + level.random.nextDouble() * 5.0D;
                LegacyMachineAudioBridge.playLocal(this, "hbm:block.assemblerStart",
                        0.25F, 1.25F + level.random.nextFloat() * 0.25F, 50.0D);
            }
        }
    }

    private void updatePrecassArms(Level level) {
        if (didProcess) {
            if (!isInWorkingPosition(armAngles) && canArmsMove()) {
                moveArm(WORKING_POSITION);
            }
            if (isInWorkingPosition(armAngles)) {
                strikerDelay--;
                if (strikerDelay <= 0) {
                    strikerDir[strikerIndex] = true;
                    strikerIndex = (strikerIndex + 1) % strikers.length;
                    strikerDelay = strikerIndex == 3 ? 10 + level.random.nextInt(3) : 2;
                }
            }
        } else {
            Arrays.fill(strikerDir, false);
            if (canArmsMove()) {
                moveArm(NULL_POSITION);
            }
        }
    }

    private static final double[] NULL_POSITION = new double[] { 45.0D, -30.0D, 45.0D };
    private static final double[] WORKING_POSITION = new double[] { 45.0D, -15.0D, -5.0D };

    private boolean canArmsMove() {
        for (double striker : strikers) {
            if (striker != 0.0D) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInWorkingPosition(double[] arms) {
        for (int i = 0; i < 3; i++) {
            if (arms[i] != WORKING_POSITION[i]) {
                return false;
            }
        }
        return true;
    }

    private void moveArm(double[] targetAngles) {
        for (int i = 0; i < armAngles.length; i++) {
            if (armAngles[i] == targetAngles[i]) {
                continue;
            }
            double delta = Math.abs(armAngles[i] - targetAngles[i]);
            if (delta <= 15.0D) {
                armAngles[i] = targetAngles[i];
            } else if (armAngles[i] < targetAngles[i]) {
                armAngles[i] += 15.0D;
            } else {
                armAngles[i] -= 15.0D;
            }
        }
    }

    private boolean tickRecipe(Level level) {
        boolean wasProcessing = didProcess;
        double oldProgress = progress;
        long oldPower = energy.getPower();
        didProcess = false;

        GenericMachineRecipe recipe = GenericMachineRecipeRuntime.findByInternalName(
                level, kind.recipeMachine, selectedRecipe);
        updateDynamicCapacity(recipe);

        ProcessingResult result = GenericMachineRecipeRuntime.update(level, kind.recipeMachine, selectedRecipe,
                progress, items.getStackInSlot(SLOT_BLUEPRINT), energy, items, kind.inputSlots, kind.outputSlots,
                inputTankList, outputTankList, upgradeFactors(), true, kind.tankCapacity, worldPosition);
        selectedRecipe = result.selectedRecipe();
        progress = result.progress();
        didProcess = result.didProcess();
        updateDynamicCapacity(result.recipe());

        return result.changed() || wasProcessing != didProcess || oldProgress != progress
                || oldPower != energy.getPower();
    }

    private ProcessingFactors upgradeFactors() {
        LegacyMachineUpgradeManager.Levels levels = LegacyMachineUpgradeManager.checkSlots(
                items, SLOT_UPGRADE_START, SLOT_UPGRADE_END, VALID_UPGRADES);
        double speed = 1.0D;
        double pow = 1.0D;
        int speedLevel = Math.min(levels.getLevel(UpgradeType.SPEED), 3);
        int powerLevel = Math.min(levels.getLevel(UpgradeType.POWER), 3);
        int overdriveLevel = Math.min(levels.getLevel(UpgradeType.OVERDRIVE), 3);
        speed += speedLevel / 3.0D;
        speed += overdriveLevel;
        pow -= powerLevel * 0.25D;
        pow += speedLevel;
        pow += overdriveLevel * 10.0D / 3.0D;
        return new ProcessingFactors(speed, pow);
    }

    private void updateDynamicCapacity(@Nullable GenericMachineRecipe recipe) {
        long targetMax = recipe == null ? kind.defaultMaxPower : Math.max(kind.defaultMaxPower, recipe.getPower() * 100L);
        targetMax = Math.max(targetMax, energy.getPower());
        energy.setMaxPower(targetMax);
        energy.setTransferRates(targetMax, 0L);
    }

    private void subscribeEnergyReceiverToPorts() {
        if (level != null && !level.isClientSide
                && Math.floorMod(level.getGameTime() + worldPosition.hashCode(), 20) == 0L) {
            HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, kind.energyPorts, this);
        }
    }

    private void refreshFluidPortSubscriptions() {
        if (level != null && !level.isClientSide) {
            fluidPortSubscriptions.refreshTransceiver(level, worldPosition, kind.fluidPorts,
                    inputTankList, outputTankList, this);
        }
    }

    private void detachFluidPortSubscriptions() {
        if (level != null && !level.isClientSide) {
            fluidPortSubscriptions.detachAll(level, worldPosition, kind.fluidPorts, this, this);
        }
    }

    @Override
    public void setRemoved() {
        detachFluidPortSubscriptions();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        detachFluidPortSubscriptions();
        super.onChunkUnloaded();
    }

    private boolean canExtractExternalSlot(int slot) {
        return kind.isOutputSlot(slot) || isCloggedInputSlot(slot);
    }

    private boolean isCloggedInputSlot(int slot) {
        if (level == null) {
            return false;
        }
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        return GenericMachineRecipeRuntime.isSlotClogged(recipe, kind.recipeMachine, level, items, slot,
                kind.inputSlots);
    }

    public Kind kind() {
        return kind;
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

    public String getSelectedRecipeName() {
        return selectedRecipe;
    }

    public void setSelectedRecipe(String selectedRecipe) {
        this.selectedRecipe = GenericMachineRecipeSelector.normalize(selectedRecipe);
        setChanged();
    }

    public boolean selectRecipe(String selectedRecipe) {
        if (level == null || GenericMachineRecipeSelector.isNullSelection(selectedRecipe)) {
            setSelectedRecipe(GenericMachineRecipeRuntime.NULL_RECIPE);
            return true;
        }
        if (!GenericMachineRecipeSelector.canSelect(level, kind.recipeMachine, selectedRecipe,
                items.getStackInSlot(SLOT_BLUEPRINT))) {
            return false;
        }
        setSelectedRecipe(selectedRecipe);
        GenericMachineRecipe recipe = getSelectedRecipeDefinition();
        GenericMachineRecipeRuntime.setupTanks(recipe, inputTankList, outputTankList, kind.tankCapacity);
        updateDynamicCapacity(recipe);
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition() {
        return level == null ? null : GenericMachineRecipeRuntime.findByInternalName(level, kind.recipeMachine,
                selectedRecipe);
    }

    public boolean isProcessing() {
        return didProcess;
    }

    public double getProgress() {
        return progress;
    }

    public boolean shouldRenderFrame() {
        return frame || (level != null && !level.getBlockState(worldPosition.above(kind.frameYOffset)).isAir());
    }

    public float getPurexAnim(float partialTick) {
        return Mth.lerp(partialTick, prevAnim, anim);
    }

    public double getPrecassRing(float partialTick) {
        return Mth.lerp(partialTick, prevRing, ring);
    }

    public double[] getPrecassArm(float partialTick) {
        return new double[] {
                Mth.lerp(partialTick, prevArmAngles[0], armAngles[0]),
                Mth.lerp(partialTick, prevArmAngles[1], armAngles[1]),
                Mth.lerp(partialTick, prevArmAngles[2], armAngles[2])
        };
    }

    public double getPrecassStriker(int index, float partialTick) {
        return Mth.lerp(partialTick, prevStrikers[index], strikers[index]);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public static CompoundTag recipeSelectionTag(String selection) {
        return GenericMachineRecipeSelector.selectionTag(selection);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return null;
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
    public Component getDisplayName() {
        return Component.translatableWithFallback(kind.containerKey, kind.fallbackTitle);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return kind == Kind.PUREX
                ? new PurexMenu(containerId, inventory, this)
                : new PrecassMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return GenericMachineRecipeSelector.isSelectionTag(tag)
                && GenericMachineRecipeSelector.canSelect(level, kind.recipeMachine,
                GenericMachineRecipeSelector.readSelection(tag), items.getStackInSlot(SLOT_BLUEPRINT));
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (GenericMachineRecipeSelector.isSelectionTag(tag)) {
            selectRecipe(GenericMachineRecipeSelector.readSelection(tag));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        tag.putString(TAG_KIND, kind.name());
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        writeTanks(tag);
        tag.putDouble(TAG_PROGRESS, progress);
        tag.putString(TAG_RECIPE, selectedRecipe);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        loadItems(tag);
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        readTanks(tag);
        progress = tag.getDouble(TAG_PROGRESS);
        selectedRecipe = GenericMachineRecipeSelector.normalize(tag.getString(TAG_RECIPE));
        updateDynamicCapacity(getSelectedRecipeDefinition());
    }

    private void writeTanks(CompoundTag tag) {
        if (kind == Kind.PRECASS) {
            inputTanks[0].writeToNbt(tag, "i");
            outputTanks[0].writeToNbt(tag, "o");
            return;
        }
        for (int i = 0; i < inputTanks.length; i++) {
            inputTanks[i].writeToNbt(tag, "i" + i);
        }
        for (int i = 0; i < outputTanks.length; i++) {
            outputTanks[i].writeToNbt(tag, "o" + i);
        }
    }

    private void readTanks(CompoundTag tag) {
        if (kind == Kind.PRECASS) {
            if (tag.contains("i")) {
                inputTanks[0].readFromNbt(tag, "i");
            }
            if (tag.contains("o")) {
                outputTanks[0].readFromNbt(tag, "o");
            }
            return;
        }
        for (int i = 0; i < inputTanks.length; i++) {
            if (tag.contains("i" + i)) {
                inputTanks[i].readFromNbt(tag, "i" + i);
            }
        }
        for (int i = 0; i < outputTanks.length; i++) {
            if (tag.contains("o" + i)) {
                outputTanks[i].readFromNbt(tag, "o" + i);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = saveWithoutMetadata();
        writeClientSyncFields(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
        readClientSyncFields(tag);
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

    private void writeClientSyncFields(CompoundTag tag) {
        tag.putBoolean(TAG_DID_PROCESS, didProcess);
    }

    private void readClientSyncFields(CompoundTag tag) {
        didProcess = tag.getBoolean(TAG_DID_PROCESS);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return kind == Kind.PUREX
                ? new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 5, 3))
                : new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
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
            return side == null ? allFluidHandler.cast() : fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void loadItems(CompoundTag tag) {
        if (tag.contains(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, Tag.TAG_LIST)
                || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return kind.externalSlots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 && kind.isInputSlot(mapped) ? items.insertItem(mapped, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 && canExtractExternalSlot(mapped)
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapExternalSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapExternalSlot(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int mapExternalSlot(int slot) {
            return slot >= 0 && slot < kind.externalSlots.length ? kind.externalSlots[slot] : -1;
        }
    }

    public enum Kind {
        PRECASS(
                GenericMachineRecipe.Machine.PRECASS,
                22,
                new int[] { 4, 5, 6, 7, 8, 9, 10, 11, 12 },
                new int[] { 13, 14, 15, 16, 17, 18, 19, 20, 21 },
                1,
                1,
                4_000,
                100_000L,
                2,
                3,
                "container.machinePrecAss",
                "Precision Assembly Machine"),
        PUREX(
                GenericMachineRecipe.Machine.PUREX,
                13,
                new int[] { 4, 5, 6 },
                new int[] { 7, 8, 9, 10, 11, 12 },
                3,
                1,
                24_000,
                1_000_000L,
                3,
                5,
                "container.machinePUREX",
                "PUREX");

        private final GenericMachineRecipe.Machine recipeMachine;
        private final int slotCount;
        private final int[] inputSlots;
        private final int[] outputSlots;
        private final int[] externalSlots;
        private final int inputTankCount;
        private final int outputTankCount;
        private final int tankCapacity;
        private final long defaultMaxPower;
        private final List<EnergyPort> energyPorts;
        private final List<FluidPort> fluidPorts;
        private final int frameYOffset;
        private final String containerKey;
        private final String fallbackTitle;

        Kind(GenericMachineRecipe.Machine recipeMachine, int slotCount, int[] inputSlots, int[] outputSlots,
                int inputTankCount, int outputTankCount, int tankCapacity, long defaultMaxPower, int portRadius,
                int frameYOffset, String containerKey, String fallbackTitle) {
            this.recipeMachine = recipeMachine;
            this.slotCount = slotCount;
            this.inputSlots = inputSlots;
            this.outputSlots = outputSlots;
            this.externalSlots = concat(inputSlots, outputSlots);
            this.inputTankCount = inputTankCount;
            this.outputTankCount = outputTankCount;
            this.tankCapacity = tankCapacity;
            this.defaultMaxPower = defaultMaxPower;
            this.energyPorts = LegacyMultiblockPorts.xrFloorRingEnergyPorts(portRadius);
            this.fluidPorts = LegacyMultiblockPorts.xrFloorRingFluidPorts(portRadius);
            this.frameYOffset = frameYOffset;
            this.containerKey = containerKey;
            this.fallbackTitle = fallbackTitle;
        }

        public GenericMachineRecipe.Machine recipeMachine() {
            return recipeMachine;
        }

        public int[] inputSlots() {
            return inputSlots;
        }

        public int[] outputSlots() {
            return outputSlots;
        }

        public boolean isInputSlot(int slot) {
            return contains(inputSlots, slot);
        }

        public boolean isOutputSlot(int slot) {
            return contains(outputSlots, slot);
        }

        private static int[] concat(int[] first, int[] second) {
            int[] result = Arrays.copyOf(first, first.length + second.length);
            System.arraycopy(second, 0, result, first.length, second.length);
            return result;
        }

        private static boolean contains(int[] slots, int slot) {
            for (int candidate : slots) {
                if (candidate == slot) {
                    return true;
                }
            }
            return false;
        }
    }
}
