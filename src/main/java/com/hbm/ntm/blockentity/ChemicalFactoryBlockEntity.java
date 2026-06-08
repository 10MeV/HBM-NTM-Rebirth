package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayPorts;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ChemicalFactoryMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingFactors;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingResult;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
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
import java.util.Map;

public class ChemicalFactoryBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmStandardFluidTransceiver, HbmTileSyncable, LegacyLookOverlayProvider, LegacyProxyDelegateProvider {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_LEGACY_MAX_POWER = "maxPower";
    private static final String TAG_INPUT_TANK = "i";
    private static final String TAG_OUTPUT_TANK = "o";
    private static final String TAG_WATER = "w";
    private static final String TAG_SPENT_STEAM = "s";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_DID_PROCESS = "DidProcess";
    private static final String TAG_ANIM = "Anim";
    private static final String TAG_FRAME = "Frame";
    private static final long DEFAULT_MAX_POWER = 1_000_000L;
    private static final int RECIPE_TANK_CAPACITY = 24_000;
    private static final int COOLANT_TANK_CAPACITY = 4_000;
    private static final int MODULES = 4;
    private static final int TANKS_PER_MODULE = 3;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_END = 3;
    public static final int MODULE_BASE = 4;
    public static final int MODULE_STRIDE = 7;

    private final HbmStandardFluidTransceiver coolingFluidNetwork = new CoolingFluidNetwork();
    private final ItemStackHandler items = new ItemStackHandler(32) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_BATTERY) {
                return true;
            }
            if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
            int module = moduleForSlot(slot);
            if (module >= 0 && slot == blueprintSlot(module)) {
                return stack.getItem() instanceof ItemBlueprints;
            }
            if (module >= 0 && slot >= inputStart(module) && slot <= inputEnd(module)) {
                GenericMachineRecipe recipe = getSelectedRecipeDefinition(module);
                return level != null && GenericMachineRecipeRuntime.isItemValidForCurrentRecipe(
                        recipe, GenericMachineRecipe.Machine.CHEMICAL_PLANT, level, slot, stack, inputSlots(module));
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final HbmEnergyStorage energy = new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L);
    private final HbmFluidTank[] inputTanks = new HbmFluidTank[MODULES * TANKS_PER_MODULE];
    private final HbmFluidTank[] outputTanks = new HbmFluidTank[MODULES * TANKS_PER_MODULE];
    private final HbmFluidTank water = new HbmFluidTank(HbmFluids.WATER, COOLANT_TANK_CAPACITY);
    private final HbmFluidTank spentSteam = new HbmFluidTank(HbmFluids.SPENTSTEAM, COOLANT_TANK_CAPACITY);
    private final List<HbmFluidTank> inputTankList;
    private final List<HbmFluidTank> outputTankList;
    private final List<HbmFluidTank> receivingTankList;
    private final List<HbmFluidTank> sendingTankList;
    private final List<HbmFluidTank> allTankList;
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new MappedItemHandler(allExternalSlots()));
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler;
    private final ICapabilityProvider coolingDelegate;
    private final ICapabilityProvider[] moduleDelegates = new ICapabilityProvider[MODULES];
    private final double[] progress = new double[MODULES];
    private final String[] selectedRecipes = new String[] {
            GenericMachineRecipeRuntime.NULL_RECIPE,
            GenericMachineRecipeRuntime.NULL_RECIPE,
            GenericMachineRecipeRuntime.NULL_RECIPE,
            GenericMachineRecipeRuntime.NULL_RECIPE
    };
    private final boolean[] didProcess = new boolean[MODULES];
    private int prevAnim;
    private int anim;
    private boolean frame;
    private Object audioLoop;

    public ChemicalFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_FACTORY.get(), pos, state);
        for (int i = 0; i < inputTanks.length; i++) {
            inputTanks[i] = new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY);
            outputTanks[i] = new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY);
        }
        inputTankList = Arrays.asList(inputTanks);
        outputTankList = Arrays.asList(outputTanks);
        receivingTankList = inputTankList;
        sendingTankList = outputTankList;
        allTankList = join(join(inputTankList, outputTankList), List.of(water, spentSteam));
        IFluidHandler recipeFluidHandler = new ForgeRecipeFluidHandlerAdapter(receivingTankList, sendingTankList, 0,
                this::onFluidContentsChanged);
        fluidHandler = LazyOptional.of(() -> recipeFluidHandler);
        coolingDelegate = new CapabilityDelegate(null, new ForgeFluidHandlerAdapter(List.of(water), List.of(spentSteam), 0,
                true, true, this::onFluidContentsChanged));
        for (int i = 0; i < MODULES; i++) {
            moduleDelegates[i] = new CapabilityDelegate(new MappedItemHandler(moduleExternalSlots(i)),
                    recipeFluidHandler);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChemicalFactoryBlockEntity blockEntity) {
        long oldPower = blockEntity.energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(blockEntity.items.getStackInSlot(SLOT_BATTERY), blockEntity,
                blockEntity.getReceiverSpeed());
        blockEntity.subscribeEnergyReceiverToPorts();
        boolean changed = blockEntity.shareInternalFluids();
        blockEntity.refreshFluidPortSubscriptions();
        changed |= blockEntity.tickRecipes(level);
        changed |= oldPower != blockEntity.energy.getPower();
        if (changed) {
            blockEntity.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChemicalFactoryBlockEntity blockEntity) {
        blockEntity.prevAnim = blockEntity.anim;
        boolean active = false;
        for (boolean processing : blockEntity.didProcess) {
            active |= processing;
        }
        if (active) {
            blockEntity.anim++;
        }
        if (level.getGameTime() % 20L == 0L) {
            blockEntity.frame = !level.getBlockState(pos.above(3)).isAir();
        }
        blockEntity.updateAudioLoop();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank(int module, int tank) {
        return inputTanks[module * TANKS_PER_MODULE + tank];
    }

    public HbmFluidTank getOutputTank(int module, int tank) {
        return outputTanks[module * TANKS_PER_MODULE + tank];
    }

    public HbmFluidTank getWaterTank() {
        return water;
    }

    public HbmFluidTank getSpentSteamTank() {
        return spentSteam;
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

    public double getProgress(int module) {
        return progress[module];
    }

    public boolean isProcessing(int module) {
        return didProcess[module];
    }

    public String getSelectedRecipeName(int module) {
        return selectedRecipes[module];
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition(int module) {
        return level == null ? null : GenericMachineRecipeRuntime.findByInternalName(
                level, GenericMachineRecipe.Machine.CHEMICAL_PLANT, selectedRecipes[module]);
    }

    public boolean canProcessSelectedRecipe(int module) {
        GenericMachineRecipe recipe = getSelectedRecipeDefinition(module);
        return recipe != null && canCool()
                && energy.getPower() >= recipe.getPower()
                && GenericMachineRecipeRuntime.canProcess(recipe, items, inputSlots(module), outputSlots(module),
                moduleInputTanks(module), moduleOutputTanks(module));
    }

    public boolean selectRecipe(int module, String selectedRecipe) {
        if (module < 0 || module >= MODULES) {
            return false;
        }
        if (level == null || GenericMachineRecipeSelector.isNullSelection(selectedRecipe)) {
            setSelectedRecipe(module, GenericMachineRecipeRuntime.NULL_RECIPE);
            return true;
        }
        if (!GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                selectedRecipe, items.getStackInSlot(blueprintSlot(module)))) {
            return false;
        }
        setSelectedRecipe(module, selectedRecipe);
        GenericMachineRecipe recipe = getSelectedRecipeDefinition(module);
        GenericMachineRecipeRuntime.setupTanks(recipe, moduleInputTanks(module), moduleOutputTanks(module), RECIPE_TANK_CAPACITY);
        updateDynamicCapacity();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    public static CompoundTag recipeSelectionTag(int module, String selection) {
        return GenericMachineRecipeSelector.selectionTag(module, selection);
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
        return receivingTankList;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return sendingTankList;
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

    @Nullable
    @Override
    public ICapabilityProvider getLegacyProxyDelegate(BlockPos proxyPos) {
        if (LegacyLookOverlayPorts.isFactoryCoolPort(this, proxyPos)) {
            return coolingDelegate;
        }
        int port = LegacyLookOverlayPorts.factoryRecipePort(this, proxyPos);
        return port >= 1 && port <= MODULES ? moduleDelegates[port - 1] : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        for (int i = 0; i < inputTanks.length; i++) {
            inputTanks[i].writeToNbt(tag, TAG_INPUT_TANK + i);
            outputTanks[i].writeToNbt(tag, TAG_OUTPUT_TANK + i);
        }
        for (int i = 0; i < MODULES; i++) {
            tag.putDouble(TAG_PROGRESS + i, progress[i]);
            tag.putString(TAG_RECIPE + i, selectedRecipes[i]);
            tag.putBoolean(TAG_DID_PROCESS + i, didProcess[i]);
        }
        water.writeToNbt(tag, TAG_WATER);
        spentSteam.writeToNbt(tag, TAG_SPENT_STEAM);
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
        for (int i = 0; i < inputTanks.length; i++) {
            if (tag.contains(TAG_INPUT_TANK + i)) {
                inputTanks[i].readFromNbt(tag, TAG_INPUT_TANK + i);
            }
            if (tag.contains(TAG_OUTPUT_TANK + i)) {
                outputTanks[i].readFromNbt(tag, TAG_OUTPUT_TANK + i);
            }
        }
        for (int i = 0; i < MODULES; i++) {
            progress[i] = tag.getDouble(TAG_PROGRESS + i);
            selectedRecipes[i] = GenericMachineRecipeSelector.normalize(tag.getString(TAG_RECIPE + i));
            didProcess[i] = tag.getBoolean(TAG_DID_PROCESS + i);
        }
        if (tag.contains(TAG_WATER)) {
            water.readFromNbt(tag, TAG_WATER);
        }
        if (tag.contains(TAG_SPENT_STEAM)) {
            spentSteam.readFromNbt(tag, TAG_SPENT_STEAM);
        }
        water.setTankType(HbmFluids.WATER);
        spentSteam.setTankType(HbmFluids.SPENTSTEAM);
        anim = tag.getInt(TAG_ANIM);
        prevAnim = anim;
        frame = tag.getBoolean(TAG_FRAME);
        updateDynamicCapacity();
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
        return LegacyMachineRenderBounds.visibleMultiblockOr(this,
                new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 3, 3)));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineChemicalFactory", "Chemical Factory");
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        int module = GenericMachineRecipeSelector.readIndex(tag);
        return GenericMachineRecipeSelector.hasSelection(tag)
                && module >= 0 && module < MODULES
                && GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                GenericMachineRecipeSelector.readSelection(tag), items.getStackInSlot(blueprintSlot(module)));
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        if (GenericMachineRecipeSelector.hasSelection(tag)) {
            selectRecipe(GenericMachineRecipeSelector.readIndex(tag), GenericMachineRecipeSelector.readSelection(tag));
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
        return new ChemicalFactoryMenu(containerId, inventory, this);
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

    private boolean tickRecipes(Level level) {
        boolean changed = false;
        long oldPower = energy.getPower();
        for (int i = 0; i < MODULES; i++) {
            boolean wasProcessing = didProcess[i];
            double oldProgress = progress[i];
            didProcess[i] = false;
            ProcessingResult result = GenericMachineRecipeRuntime.update(level, GenericMachineRecipe.Machine.CHEMICAL_PLANT,
                    selectedRecipes[i], progress[i], items.getStackInSlot(blueprintSlot(i)), energy, items,
                    inputSlots(i), outputSlots(i), moduleInputTanks(i), moduleOutputTanks(i),
                    upgradeFactors(), canCool(), RECIPE_TANK_CAPACITY, worldPosition);
            selectedRecipes[i] = result.selectedRecipe();
            progress[i] = result.progress();
            didProcess[i] = result.didProcess();
            if (didProcess[i]) {
                water.drain(100, false);
                spentSteam.fill(HbmFluids.SPENTSTEAM, 100, 0, false);
            }
            changed |= result.changed() || wasProcessing != didProcess[i] || oldProgress != progress[i];
        }
        updateDynamicCapacity();
        return changed || oldPower != energy.getPower();
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
        return new ProcessingFactors(speed * 2.0D, pow * 2.0D);
    }

    private boolean canCool() {
        return water.getFill() >= 100 && spentSteam.getFill() <= spentSteam.getMaxFill() - 100;
    }

    private boolean shareInternalFluids() {
        boolean changed = false;
        for (HbmFluidTank input : inputTanks) {
            for (HbmFluidTank output : outputTanks) {
                if (output.isEmpty()
                        || input.getTankType() != output.getTankType()
                        || input.getPressure() != output.getPressure()) {
                    continue;
                }
                int move = Math.min(50, Math.min(input.getSpace(), output.getFill()));
                if (move > 0) {
                    output.drain(move, false);
                    input.fill(output.getTankType(), move, output.getPressure(), false);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void updateDynamicCapacity() {
        long targetMax = 0L;
        if (level != null) {
            for (int i = 0; i < MODULES; i++) {
                GenericMachineRecipe recipe = getSelectedRecipeDefinition(i);
                if (recipe != null) {
                    targetMax += recipe.getPower() * 100L;
                }
            }
        }
        targetMax = Math.max(Math.max(targetMax, DEFAULT_MAX_POWER), energy.getPower());
        energy.setMaxPower(targetMax);
        energy.setTransferRates(targetMax, 0L);
    }

    private int subscribeEnergyReceiverToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, energyPorts(), this);
    }

    private void refreshFluidPortSubscriptions() {
        HbmFluidPortMachine.refreshTransceiverPorts(level, worldPosition, recipeFluidPorts(),
                receivingTankList, sendingTankList, this);
        HbmFluidPortMachine.refreshTransceiverPorts(level, worldPosition, coolingFluidPorts(),
                List.of(water), List.of(spentSteam), coolingFluidNetwork);
    }

    private void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void updateAudioLoop() {
        if (level == null || !level.isClientSide) {
            return;
        }
        boolean active = false;
        for (boolean processing : didProcess) {
            active |= processing;
        }
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, ModSounds.BLOCK_CHEMPLANT_OPERATE.getId(),
                active, 50.0D, 15.0F, 1.0F, 1.0F);
    }

    private boolean canExtractExternalSlot(int slot) {
        for (int i = 0; i < MODULES; i++) {
            if ((slot >= outputStart(i) && slot <= outputEnd(i)) || isCloggedInputSlot(i, slot)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCloggedInputSlot(int module, int slot) {
        if (level == null || slot < inputStart(module) || slot > inputEnd(module)) {
            return false;
        }
        return GenericMachineRecipeRuntime.isSlotClogged(getSelectedRecipeDefinition(module),
                GenericMachineRecipe.Machine.CHEMICAL_PLANT, level, items, slot, inputSlots(module));
    }

    public static int blueprintSlot(int module) {
        return MODULE_BASE + module * MODULE_STRIDE;
    }

    public static int inputStart(int module) {
        return blueprintSlot(module) + 1;
    }

    public static int inputEnd(int module) {
        return blueprintSlot(module) + 3;
    }

    public static int outputStart(int module) {
        return blueprintSlot(module) + 4;
    }

    public static int outputEnd(int module) {
        return blueprintSlot(module) + 6;
    }

    public static int[] inputSlots(int module) {
        return new int[] {inputStart(module), inputStart(module) + 1, inputStart(module) + 2};
    }

    public static int[] outputSlots(int module) {
        return new int[] {outputStart(module), outputStart(module) + 1, outputStart(module) + 2};
    }

    private static int moduleForSlot(int slot) {
        if (slot < MODULE_BASE) {
            return -1;
        }
        int relative = slot - MODULE_BASE;
        int module = relative / MODULE_STRIDE;
        return module >= 0 && module < MODULES ? module : -1;
    }

    private static int[] allExternalSlots() {
        int[] slots = new int[MODULES * 6];
        int index = 0;
        for (int module = 0; module < MODULES; module++) {
            for (int slot = inputStart(module); slot <= outputEnd(module); slot++) {
                slots[index++] = slot;
            }
        }
        return slots;
    }

    private static int[] moduleExternalSlots(int module) {
        int[] slots = new int[15];
        int index = 0;
        for (int slot = inputStart(module); slot <= inputEnd(module); slot++) {
            slots[index++] = slot;
        }
        for (int i = 0; i < MODULES; i++) {
            for (int slot = outputStart(i); slot <= outputEnd(i); slot++) {
                slots[index++] = slot;
            }
        }
        return slots;
    }

    private List<HbmFluidTank> moduleInputTanks(int module) {
        return List.of(getInputTank(module, 0), getInputTank(module, 1), getInputTank(module, 2));
    }

    private List<HbmFluidTank> moduleOutputTanks(int module) {
        return List.of(getOutputTank(module, 0), getOutputTank(module, 1), getOutputTank(module, 2));
    }

    private static List<HbmFluidTank> join(List<HbmFluidTank> first, List<HbmFluidTank> second) {
        List<HbmFluidTank> joined = new ArrayList<>(first);
        joined.addAll(second);
        return List.copyOf(joined);
    }

    private Direction facing() {
        return getBlockState().hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private List<EnergyPort> energyPorts() {
        Direction facing = facing();
        return LegacyMultiblockPorts.combineEnergyPorts(
                LegacyMultiblockPorts.factoryRecipeEnergyPorts(facing, true),
                LegacyMultiblockPorts.factoryCoolingEnergyPorts(facing));
    }

    private List<FluidPort> recipeFluidPorts() {
        return LegacyMultiblockPorts.factoryRecipeFluidPorts(facing(), true);
    }

    private List<FluidPort> coolingFluidPorts() {
        return LegacyMultiblockPorts.factoryCoolingFluidPorts(facing());
    }

    private void setSelectedRecipe(int module, String recipe) {
        selectedRecipes[module] = GenericMachineRecipeSelector.normalize(recipe);
        setChanged();
    }

    private class MappedItemHandler implements IItemHandler {
        private final int[] slots;

        private MappedItemHandler(int[] slots) {
            this.slots = slots;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            return mapped < 0 ? stack : items.insertItem(mapped, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped < 0 || !canExtractExternalSlot(mapped) ? ItemStack.EMPTY : items.extractItem(mapped, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped >= 0 && items.isItemValid(mapped, stack);
        }

        private int map(int slot) {
            return slot >= 0 && slot < slots.length ? slots[slot] : -1;
        }
    }

    private class CoolingFluidNetwork implements HbmStandardFluidTransceiver {
        @Override
        public List<HbmFluidTank> getAllTanks() {
            return List.of(water, spentSteam);
        }

        @Override
        public List<HbmFluidTank> getReceivingTanks() {
            return List.of(water);
        }

        @Override
        public List<HbmFluidTank> getSendingTanks() {
            return List.of(spentSteam);
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
    }

    private class CapabilityDelegate implements ICapabilityProvider {
        @Nullable
        private final IItemHandler itemHandler;
        private final IFluidHandler fluidHandler;

        private CapabilityDelegate(@Nullable IItemHandler itemHandler, IFluidHandler fluidHandler) {
            this.itemHandler = itemHandler;
            this.fluidHandler = fluidHandler;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            if (capability == ForgeCapabilities.ITEM_HANDLER && itemHandler != null) {
                return LazyOptional.of(() -> itemHandler).cast();
            }
            if (capability == ForgeCapabilities.ENERGY) {
                return energyHandler.cast();
            }
            if (capability == ForgeCapabilities.FLUID_HANDLER) {
                return LazyOptional.of(() -> fluidHandler).cast();
            }
            return LazyOptional.empty();
        }
    }
}
