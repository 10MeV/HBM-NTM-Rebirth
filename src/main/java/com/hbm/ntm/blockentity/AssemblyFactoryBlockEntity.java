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
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.AssemblyFactoryMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingFactors;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingResult;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import java.util.Random;

public class AssemblyFactoryBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmStandardFluidTransceiver, HbmLegacyLoadedTile, LegacyLookOverlayProvider, LegacyProxyDelegateProvider {
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
    private static final String TAG_FRAME = "Frame";
    private static final long DEFAULT_MAX_POWER = 1_000_000L;
    private static final int TANK_CAPACITY = 4_000;
    private static final int MODULES = 4;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_END = 3;
    public static final int MODULE_STRIDE = 14;
    public static final int MODULE_BASE = 4;

    private final HbmStandardFluidTransceiver coolingFluidNetwork = new CoolingFluidNetwork();
    private final ItemStackHandler items = new ItemStackHandler(60) {
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
                        recipe, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, level, slot, stack, inputSlots(module));
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final HbmEnergyStorage energy = new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L);
    private final HbmFluidTank[] inputTanks = new HbmFluidTank[MODULES];
    private final HbmFluidTank[] outputTanks = new HbmFluidTank[MODULES];
    private final HbmFluidTank water = new HbmFluidTank(HbmFluids.WATER, TANK_CAPACITY);
    private final HbmFluidTank spentSteam = new HbmFluidTank(HbmFluids.SPENTSTEAM, TANK_CAPACITY);
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
    private final TragicYuri[] animations = new TragicYuri[] {
            new TragicYuri(0),
            new TragicYuri(1)
    };
    private boolean frame;
    private Object audioLoop;

    public AssemblyFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLY_FACTORY.get(), pos, state);
        for (int i = 0; i < MODULES; i++) {
            inputTanks[i] = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
            outputTanks[i] = new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY);
        }
        inputTankList = Arrays.asList(inputTanks);
        outputTankList = Arrays.asList(outputTanks);
        receivingTankList = inputTankList;
        sendingTankList = outputTankList;
        allTankList = join(join(inputTankList, outputTankList), List.of(water, spentSteam));
        IFluidHandler recipeFluidHandler = ForgeRecipeFluidHandlerAdapter.create(receivingTankList, sendingTankList, 0,
                this::onFluidContentsChanged);
        fluidHandler = LazyOptional.of(() -> recipeFluidHandler);
        coolingDelegate = new CapabilityDelegate(null, new ForgeFluidHandlerAdapter(List.of(water), List.of(spentSteam), 0,
                true, true, this::onFluidContentsChanged));
        for (int i = 0; i < MODULES; i++) {
            moduleDelegates[i] = new CapabilityDelegate(new MappedItemHandler(moduleExternalSlots(i)),
                    recipeFluidHandler);
        }
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AssemblyFactoryBlockEntity blockEntity) {
        long oldPower = blockEntity.energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(blockEntity.items.getStackInSlot(SLOT_BATTERY), blockEntity,
                blockEntity.getReceiverSpeed());
        blockEntity.subscribeEnergyReceiverToPorts();
        blockEntity.refreshFluidPortSubscriptions();
        boolean changed = blockEntity.tickRecipes(level);
        changed |= oldPower != blockEntity.energy.getPower();
        if (changed) {
            blockEntity.setChanged();
        }
        blockEntity.networkPackNT(100);
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AssemblyFactoryBlockEntity blockEntity) {
        boolean working = false;
        for (boolean processing : blockEntity.didProcess) {
            working |= processing;
        }
        for (TragicYuri animation : blockEntity.animations) {
            animation.update(blockEntity, working);
        }
        if (level.getGameTime() % 20L == 0L) {
            blockEntity.frame = !level.getBlockState(pos.above(3)).isAir();
        }
        blockEntity.updateAudioLoop();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank(int module) {
        return inputTanks[module];
    }

    public HbmFluidTank getOutputTank(int module) {
        return outputTanks[module];
    }

    public HbmFluidTank getWaterTank() {
        return water;
    }

    public HbmFluidTank getSpentSteamTank() {
        return spentSteam;
    }

    public double getProgress(int module) {
        return progress[module];
    }

    public boolean isProcessing(int module) {
        return didProcess[module];
    }

    public TragicYuri getAnimation(int group) {
        return animations[group];
    }

    public boolean shouldRenderFrame() {
        return frame || (level != null && !level.getBlockState(worldPosition.above(3)).isAir());
    }

    public String getSelectedRecipeName(int module) {
        return selectedRecipes[module];
    }

    @Nullable
    public GenericMachineRecipe getSelectedRecipeDefinition(int module) {
        return level == null ? null : GenericMachineRecipeRuntime.findByInternalName(
                level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, selectedRecipes[module]);
    }

    public boolean canProcessSelectedRecipe(int module) {
        GenericMachineRecipe recipe = getSelectedRecipeDefinition(module);
        return recipe != null && canCool()
                && energy.getPower() >= recipe.getPower()
                && GenericMachineRecipeRuntime.canProcess(recipe, items, inputSlots(module), new int[] {outputSlot(module)},
                List.of(inputTanks[module]), List.of(outputTanks[module]));
    }

    public boolean selectRecipe(int module, String selectedRecipe) {
        if (module < 0 || module >= MODULES) {
            return false;
        }
        if (level == null || GenericMachineRecipeSelector.isNullSelection(selectedRecipe)) {
            setSelectedRecipe(module, GenericMachineRecipeRuntime.NULL_RECIPE);
            return true;
        }
        if (!GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
                selectedRecipe, items.getStackInSlot(blueprintSlot(module)))) {
            return false;
        }
        setSelectedRecipe(module, selectedRecipe);
        GenericMachineRecipe recipe = getSelectedRecipeDefinition(module);
        GenericMachineRecipeRuntime.setupTanksReport(recipe, List.of(inputTanks[module]), List.of(outputTanks[module]),
                TANK_CAPACITY);
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
        return HbmInventoryMenuHelper.clearToDrops(items);
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
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putLong(TAG_LEGACY_MAX_POWER, energy.getMaxPower());
        for (int i = 0; i < MODULES; i++) {
            inputTanks[i].writeToNbt(tag, TAG_INPUT_TANK + i);
            outputTanks[i].writeToNbt(tag, TAG_OUTPUT_TANK + i);
            tag.putDouble(TAG_PROGRESS + i, progress[i]);
            tag.putString(TAG_RECIPE + i, selectedRecipes[i]);
            tag.putBoolean(TAG_DID_PROCESS + i, didProcess[i]);
        }
        water.writeToNbt(tag, TAG_WATER);
        spentSteam.writeToNbt(tag, TAG_SPENT_STEAM);
        tag.putBoolean(TAG_FRAME, frame);
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
        for (int i = 0; i < MODULES; i++) {
            if (tag.contains(TAG_INPUT_TANK + i)) {
                inputTanks[i].readFromNbt(tag, TAG_INPUT_TANK + i);
            }
            if (tag.contains(TAG_OUTPUT_TANK + i)) {
                outputTanks[i].readFromNbt(tag, TAG_OUTPUT_TANK + i);
            }
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
        frame = tag.getBoolean(TAG_FRAME);
        water.setTankType(HbmFluids.WATER);
        spentSteam.setTankType(HbmFluids.SPENTSTEAM);
        updateDynamicCapacity();
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
        return LegacyMachineRenderBounds.visibleMultiblockOr(this,
                new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 3, 3)));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineAssemblyFactory", "Assembly Factory");
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        int module = GenericMachineRecipeSelector.readIndex(tag);
        return GenericMachineRecipeSelector.hasSelection(tag)
                && module >= 0 && module < MODULES
                && GenericMachineRecipeSelector.canSelect(level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
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
        return new AssemblyFactoryMenu(containerId, inventory, this);
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
            ProcessingResult result = GenericMachineRecipeRuntime.update(level, GenericMachineRecipe.Machine.ASSEMBLY_MACHINE,
                    selectedRecipes[i], progress[i], items.getStackInSlot(blueprintSlot(i)), energy, items,
                    inputSlots(i), new int[] {outputSlot(i)}, List.of(inputTanks[i]), List.of(outputTanks[i]),
                    upgradeFactors(), canCool(), TANK_CAPACITY, worldPosition);
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
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.assemblerOperate",
                active, 50.0D, 15.0F, 0.5F, 0.75F);
    }

    private boolean canExtractExternalSlot(int slot) {
        for (int i = 0; i < MODULES; i++) {
            if (slot == outputSlot(i) || isCloggedInputSlot(i, slot)) {
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
                GenericMachineRecipe.Machine.ASSEMBLY_MACHINE, level, items, slot, inputSlots(module));
    }

    public static int blueprintSlot(int module) {
        return MODULE_BASE + module * MODULE_STRIDE;
    }

    public static int inputStart(int module) {
        return blueprintSlot(module) + 1;
    }

    public static int inputEnd(int module) {
        return blueprintSlot(module) + 12;
    }

    public static int outputSlot(int module) {
        return blueprintSlot(module) + 13;
    }

    public static int[] inputSlots(int module) {
        int[] slots = new int[12];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = inputStart(module) + i;
        }
        return slots;
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
        int[] slots = new int[MODULES * 13];
        int index = 0;
        for (int module = 0; module < MODULES; module++) {
            for (int slot = inputStart(module); slot <= outputSlot(module); slot++) {
                slots[index++] = slot;
            }
        }
        return slots;
    }

    private static int[] moduleExternalSlots(int module) {
        int[] slots = new int[16];
        int index = 0;
        for (int slot = inputStart(module); slot <= inputEnd(module); slot++) {
            slots[index++] = slot;
        }
        for (int i = 0; i < MODULES; i++) {
            slots[index++] = outputSlot(i);
        }
        return slots;
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
                LegacyMultiblockPorts.factoryRecipeEnergyPorts(facing, false),
                LegacyMultiblockPorts.factoryCoolingEnergyPorts(facing));
    }

    private List<FluidPort> recipeFluidPorts() {
        return LegacyMultiblockPorts.factoryRecipeFluidPorts(facing(), false);
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

    public static final class TragicYuri {
        public final AssemblerArm striker;
        public final AssemblerArm saw;

        private final Random random = new Random();
        private YuriState state = YuriState.WORKING;
        private double slider;
        private double prevSlider;
        private boolean direction;
        private int timeUntilReposition;

        private TragicYuri(int group) {
            striker = new AssemblerArm(group == 0 ? 0 : 3);
            saw = new AssemblerArm(group == 0 ? 1 : 2, true);
            timeUntilReposition = nextRepositionDelay();
        }

        private void update(AssemblyFactoryBlockEntity factory, boolean working) {
            prevSlider = slider;
            boolean strikerWorking = factory.didProcess[striker.recipeIndex];
            boolean sawWorking = factory.didProcess[saw.recipeIndex];
            if (strikerWorking || sawWorking) {
                switch (state) {
                    case WORKING -> {
                        timeUntilReposition--;
                        if (timeUntilReposition <= 0) {
                            state = YuriState.RETIRING;
                        }
                    }
                    case RETIRING -> {
                        if (striker.state == ArmState.WAIT && saw.state == ArmState.WAIT) {
                            state = YuriState.SLIDING;
                            direction = !direction;
                            LegacyMachineAudioBridge.playLocal(factory, "hbm:block.assemblerStart",
                                    0.25F, 1.25F + factory.level.random.nextFloat() * 0.25F, 50.0D);
                        }
                    }
                    case SLIDING -> {
                        double sliderSpeed = 1.0D / 10.0D;
                        if (direction) {
                            slider += sliderSpeed;
                            if (slider >= 1.0D) {
                                slider = 1.0D;
                                state = YuriState.WORKING;
                            }
                        } else {
                            slider -= sliderSpeed;
                            if (slider <= 0.0D) {
                                slider = 0.0D;
                                state = YuriState.WORKING;
                            }
                        }
                        if (state == YuriState.WORKING) {
                            timeUntilReposition = nextRepositionDelay();
                        }
                    }
                }
            }
            striker.updateArm(factory);
            saw.updateArm(factory);
        }

        public double getSlider(float partialTick) {
            return prevSlider + (slider - prevSlider) * partialTick;
        }

        private int nextRepositionDelay() {
            return 140 + random.nextInt(161);
        }

        public final class AssemblerArm {
            private final double[] angles = new double[4];
            private final double[] prevAngles = new double[4];
            private final double[] targetAngles = new double[4];
            private final double[] speed = new double[4];
            private final Random armRandom = new Random();
            private double sawAngle;
            private double prevSawAngle;
            private final int recipeIndex;
            private final boolean saw;
            private ArmState state = ArmState.REPOSITION;
            private int actionDelay;

            private AssemblerArm(int recipeIndex) {
                this(recipeIndex, false);
            }

            private AssemblerArm(int recipeIndex, boolean saw) {
                this.recipeIndex = recipeIndex;
                this.saw = saw;
                resetSpeed();
                chooseNewArmPosition();
            }

            private void resetSpeed() {
                speed[0] = 15.0D;
                speed[1] = 15.0D;
                speed[2] = 15.0D;
                speed[3] = saw ? 0.125D : 0.5D;
            }

            private void updateArm(AssemblyFactoryBlockEntity factory) {
                resetSpeed();
                System.arraycopy(angles, 0, prevAngles, 0, angles.length);
                prevSawAngle = sawAngle;

                int serviceIndex = recipeIndex;
                if (slider > 0.5D) {
                    serviceIndex += serviceIndex % 2 == 0 ? 1 : -1;
                }
                if (!factory.didProcess[serviceIndex]) {
                    state = ArmState.RETIRE;
                }

                if (state == ArmState.CUT || state == ArmState.EXTEND) {
                    sawAngle += 45.0D;
                }

                if (actionDelay > 0) {
                    actionDelay--;
                    return;
                }

                switch (state) {
                    case REPOSITION -> {
                        if (move()) {
                            actionDelay = 2;
                            state = ArmState.EXTEND;
                            targetAngles[3] = saw ? -0.375D : -0.75D;
                        }
                    }
                    case EXTEND -> {
                        if (move()) {
                            if (saw) {
                                state = ArmState.CUT;
                                targetAngles[2] = -targetAngles[2];
                                LegacyMachineAudioBridge.playLocal(factory, "hbm:block.assemblerCut",
                                        0.5F, 1.0F + factory.level.random.nextFloat() * 0.25F, 50.0D);
                            } else {
                                state = ArmState.RETRACT;
                                targetAngles[3] = 0.0D;
                                LegacyMachineAudioBridge.playLocal(factory, "hbm:block.assemblerStrike",
                                        0.5F, 1.0F, 50.0D);
                            }
                        }
                    }
                    case CUT -> {
                        speed[2] = Math.abs(targetAngles[2] / 20.0D);
                        if (move()) {
                            state = ArmState.RETRACT;
                            targetAngles[3] = 0.0D;
                        }
                    }
                    case RETRACT -> {
                        if (move()) {
                            actionDelay = 2 + armRandom.nextInt(5);
                            chooseNewArmPosition();
                            state = TragicYuri.this.state == YuriState.RETIRING ? ArmState.RETIRE : ArmState.REPOSITION;
                        }
                    }
                    case RETIRE -> {
                        Arrays.fill(targetAngles, 0.0D);
                        if (move()) {
                            actionDelay = 2 + armRandom.nextInt(5);
                            chooseNewArmPosition();
                            state = ArmState.WAIT;
                        }
                    }
                    case WAIT -> {
                        if (TragicYuri.this.state == YuriState.WORKING) {
                            state = ArmState.REPOSITION;
                        }
                    }
                }
            }

            private void chooseNewArmPosition() {
                double[][] positions = !saw ? new double[][] {
                        {10.0D, 10.0D, -10.0D},
                        {15.0D, 15.0D, -15.0D},
                        {25.0D, 10.0D, -15.0D},
                        {30.0D, 0.0D, -10.0D},
                        {-10.0D, 10.0D, 0.0D},
                        {-20.0D, 30.0D, -15.0D}
                } : new double[][] {
                        {-15.0D, 15.0D, -10.0D},
                        {-15.0D, 15.0D, -15.0D},
                        {-15.0D, 15.0D, 10.0D},
                        {-15.0D, 15.0D, 15.0D},
                        {-15.0D, 15.0D, 2.0D},
                        {-15.0D, 15.0D, -2.0D}
                };
                double[] chosen = positions[armRandom.nextInt(positions.length)];
                targetAngles[0] = chosen[0];
                targetAngles[1] = chosen[1];
                targetAngles[2] = chosen[2];
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
                        lerp(prevAngles[0], angles[0], partialTick),
                        lerp(prevAngles[1], angles[1], partialTick),
                        lerp(prevAngles[2], angles[2], partialTick),
                        lerp(prevAngles[3], angles[3], partialTick),
                        lerp(prevSawAngle, sawAngle, partialTick)
                };
            }

            private static double lerp(double previous, double current, float partialTick) {
                return previous + (current - previous) * partialTick;
            }
        }
    }

    public enum YuriState {
        WORKING,
        RETIRING,
        SLIDING
    }

    public enum ArmState {
        REPOSITION,
        EXTEND,
        CUT,
        RETRACT,
        RETIRE,
        WAIT
    }
}
