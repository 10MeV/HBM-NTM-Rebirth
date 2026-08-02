package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayPorts;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.LegacyFrameRenderState;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyPortInspectable;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidPortSubscriptionTracker;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.LegacyFluidTankPacket;
import com.hbm.ntm.item.ItemBlueprints;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ChemicalFactoryMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockPorts;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingFactors;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime.ProcessingResult;
import com.hbm.ntm.recipe.GenericMachineRecipeSelector;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.BufferUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

public class ChemicalFactoryBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyReceiver,
        HbmEnergyPortInspectable, HbmStandardFluidTransceiver, HbmLegacyLoadedTile, LegacyLookOverlayProvider,
        LegacyProxyDelegateProvider, RORValueProvider {
    private static final String TAG_INVENTORY = HbmInventoryMenuHelper.LEGACY_ITEMS_TAG;
    private static final String TAG_MODERN_INVENTORY = "Inventory";
    private static final String TAG_CUSTOM_NAME = "name";
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
    private static final long DEFAULT_MAX_POWER = 1_000_000L;
    private static final int RECIPE_TANK_CAPACITY = 24_000;
    private static final int COOLANT_TANK_CAPACITY = 4_000;
    private static final int MODULES = 4;
    private static final int TANKS_PER_MODULE = 3;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_END = 3;
    public static final int MODULE_BASE = 4;
    public static final int MODULE_STRIDE = 7;

    private final HbmStandardFluidTransceiver coolingFluidNetwork = new CoolingFluidNetwork();
    private final HbmFluidPortSubscriptionTracker recipeFluidPortSubscriptions = new HbmFluidPortSubscriptionTracker();
    private final HbmFluidPortSubscriptionTracker coolingFluidPortSubscriptions = new HbmFluidPortSubscriptionTracker();
    private final LegacyMachineUpgradeManager.SlotCache upgradeSlotCache =
            new LegacyMachineUpgradeManager.SlotCache(SLOT_UPGRADE_END - SLOT_UPGRADE_START + 1);
    private final ItemStackHandler items = new ItemStackHandler(32) {
        @Override
        protected void onContentsChanged(int slot) {
            if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
                invalidateUpgradeFactors();
            }
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
                        recipe, GenericMachineRecipe.Machine.CHEMICAL_PLANT, level, slot, stack, inputSlotsFor(module));
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
    private final int[][] moduleInputSlots = new int[MODULES][];
    private final int[][] moduleOutputSlots = new int[MODULES][];
    private final List<HbmFluidTank>[] moduleInputTankLists;
    private final List<HbmFluidTank>[] moduleOutputTankLists;
    private final List<HbmFluidTank> coolingReceivingTankList = List.of(water);
    private final List<HbmFluidTank> coolingSendingTankList = List.of(spentSteam);
    private final List<HbmFluidTank> coolingAllTankList = List.of(water, spentSteam);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new MappedItemHandler(allExternalSlots()));
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
    private final LazyOptional<IFluidHandler> fluidHandler;
    private final CapabilityDelegate coolingDelegate;
    private final CapabilityDelegate[] moduleDelegates = new CapabilityDelegate[MODULES];
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
    private Object audioLoop;
    @Nullable
    private String customName;
    @Nullable
    private ProcessingFactors cachedUpgradeFactors;
    private long appliedMaxPower = Long.MIN_VALUE;
    private final RORDispatcher ror = createRorDispatcher();

    @SuppressWarnings("unchecked")
    public ChemicalFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_FACTORY.get(), pos, state);
        moduleInputTankLists = (List<HbmFluidTank>[]) new List<?>[MODULES];
        moduleOutputTankLists = (List<HbmFluidTank>[]) new List<?>[MODULES];
        for (int i = 0; i < inputTanks.length; i++) {
            inputTanks[i] = new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY);
            outputTanks[i] = new HbmFluidTank(HbmFluids.NONE, RECIPE_TANK_CAPACITY);
        }
        for (int i = 0; i < MODULES; i++) {
            moduleInputSlots[i] = inputSlots(i);
            moduleOutputSlots[i] = outputSlots(i);
            moduleInputTankLists[i] = List.of(getInputTank(i, 0), getInputTank(i, 1), getInputTank(i, 2));
            moduleOutputTankLists[i] = List.of(getOutputTank(i, 0), getOutputTank(i, 1), getOutputTank(i, 2));
        }
        inputTankList = Arrays.asList(inputTanks);
        outputTankList = Arrays.asList(outputTanks);
        receivingTankList = inputTankList;
        sendingTankList = outputTankList;
        allTankList = join(join(inputTankList, outputTankList), coolingAllTankList);
        IFluidHandler recipeFluidHandler = ForgeRecipeFluidHandlerAdapter.create(receivingTankList, sendingTankList, 0,
                this::onFluidContentsChanged);
        fluidHandler = LazyOptional.of(() -> recipeFluidHandler);
        coolingDelegate = new CapabilityDelegate(null, new ForgeFluidHandlerAdapter(coolingReceivingTankList, coolingSendingTankList, 0,
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChemicalFactoryBlockEntity blockEntity) {
        if (level.getGameTime() % 20L == 0L) {
            state = LegacyFrameRenderState.syncFrameBlockState(level, pos, state, 3);
        }
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
        blockEntity.networkPackNT(100);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChemicalFactoryBlockEntity blockEntity) {
        blockEntity.prevAnim = blockEntity.anim;
        boolean active = false;
        for (boolean processing : blockEntity.didProcess) {
            active |= processing;
        }
        if (active && !LegacyClientAnimationLod.shouldSkipAnimationUpdate(level, pos)) {
            blockEntity.anim++;
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
        return LegacyFrameRenderState.isFrameVisible(getBlockState(), level, worldPosition, 3);
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
                && GenericMachineRecipeRuntime.canProcess(recipe, items, inputSlotsFor(module), outputSlotsFor(module),
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
        GenericMachineRecipeRuntime.setupTanks(recipe, moduleInputTanks(module), moduleOutputTanks(module),
                RECIPE_TANK_CAPACITY);
        updateDynamicCapacity();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                "VAL:progress1", "VAL:progress2", "VAL:progress3", "VAL:progress4",
                "VAL:recipe1", "VAL:recipe2", "VAL:recipe3", "VAL:recipe4",
                "VAL:anyactive", "VAL:active1", "VAL:active2", "VAL:active3", "VAL:active4"
        };
    }

    @Override
    public String provideRORValue(String name) { return ror.provideValue(name); }

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
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
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
        }
        water.writeToNbt(tag, TAG_WATER);
        spentSteam.writeToNbt(tag, TAG_SPENT_STEAM);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
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
        }
        if (tag.contains(TAG_WATER)) {
            water.readFromNbt(tag, TAG_WATER);
        }
        if (tag.contains(TAG_SPENT_STEAM)) {
            spentSteam.readFromNbt(tag, TAG_SPENT_STEAM);
        }
        water.setTankType(HbmFluids.WATER);
        spentSteam.setTankType(HbmFluids.SPENTSTEAM);
        invalidateUpgradeFactors();
        appliedMaxPower = Long.MIN_VALUE;
        updateDynamicCapacity();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
}

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
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
        // TileEntityMachineChemicalFactory#serialize, retaining the independent module ordering.
        writeLegacyLoadedTileBinary(data);
        for (HbmFluidTank tank : inputTanks) {
            LegacyFluidTankPacket.write(data, tank);
        }
        for (HbmFluidTank tank : outputTanks) {
            LegacyFluidTankPacket.write(data, tank);
        }
        LegacyFluidTankPacket.write(data, water);
        LegacyFluidTankPacket.write(data, spentSteam);
        data.writeLong(energy.getPower());
        data.writeLong(energy.getMaxPower());
        for (boolean processing : didProcess) {
            data.writeBoolean(processing);
        }
        for (int i = 0; i < MODULES; i++) {
            data.writeDouble(progress[i]);
            BufferUtil.writeString(data, selectedRecipes[i]);
        }
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        for (HbmFluidTank tank : inputTanks) {
            LegacyFluidTankPacket.read(data, tank);
        }
        for (HbmFluidTank tank : outputTanks) {
            LegacyFluidTankPacket.read(data, tank);
        }
        LegacyFluidTankPacket.read(data, water);
        LegacyFluidTankPacket.read(data, spentSteam);
        energy.setPower(data.readLong());
        energy.setMaxPower(Math.max(DEFAULT_MAX_POWER, data.readLong()));
        for (int i = 0; i < MODULES; i++) {
            didProcess[i] = data.readBoolean();
        }
        for (int i = 0; i < MODULES; i++) {
            progress[i] = data.readDouble();
            selectedRecipes[i] = GenericMachineRecipeSelector.normalize(BufferUtil.readString(data));
        }
    }

    private void writeClientSyncFields(CompoundTag tag) {
        for (int i = 0; i < MODULES; i++) {
            tag.putBoolean(TAG_DID_PROCESS + i, didProcess[i]);
        }
    }

    private void readClientSyncFields(CompoundTag tag) {
        for (int i = 0; i < MODULES; i++) {
            didProcess[i] = tag.getBoolean(TAG_DID_PROCESS + i);
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
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.machineChemicalFactory", "Chemical Factory");
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        int module = GenericMachineRecipeSelector.readIndex(tag);
        return HbmGuiControlSecurity.hasLegacyMachineUsePermission(player, this)
                && GenericMachineRecipeSelector.hasSelection(tag)
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
    public HbmEnergyUtil.PortSetSnapshot inspectEnergyPorts() {
        return level == null
                ? new HbmEnergyUtil.PortSetSnapshot(0, 0, 0, 0, 0, 0, 0L, 0L)
                : HbmEnergyUtil.inspectPorts(level, worldPosition, energyPorts());
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
    public void setRemoved() {
        detachFluidPortSubscriptions();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        detachFluidPortSubscriptions();
        super.onChunkUnloaded();
    }

    private void detachFluidPortSubscriptions() {
        recipeFluidPortSubscriptions.detachAllDetailed(level, worldPosition, recipeFluidPorts(), this, this);
        coolingFluidPortSubscriptions.detachAllDetailed(level, worldPosition, coolingFluidPorts(),
                coolingFluidNetwork, coolingFluidNetwork);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
        coolingDelegate.invalidate();
        for (CapabilityDelegate delegate : moduleDelegates) {
            delegate.invalidate();
        }
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
                    inputSlotsFor(i), outputSlotsFor(i), moduleInputTanks(i), moduleOutputTanks(i),
                    upgradeFactors(), canCool(), RECIPE_TANK_CAPACITY);
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
        if (cachedUpgradeFactors != null) {
            return cachedUpgradeFactors;
        }
        LegacyMachineUpgradeManager.Levels levels =
                upgradeSlotCache.get(items, SLOT_UPGRADE_START, SLOT_UPGRADE_END, VALID_UPGRADES);
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
        cachedUpgradeFactors = new ProcessingFactors(speed * 2.0D, pow * 2.0D);
        return cachedUpgradeFactors;
    }

    private void invalidateUpgradeFactors() {
        cachedUpgradeFactors = null;
        upgradeSlotCache.invalidate();
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
        applyDynamicCapacity(targetMax);
    }

    private void applyDynamicCapacity(long targetBase) {
        long targetMax = Math.max(Math.max(targetBase, DEFAULT_MAX_POWER), energy.getPower());
        if (appliedMaxPower == targetMax && energy.getMaxPower() == targetMax
                && energy.getReceiverSpeed() == targetMax && energy.getProviderSpeed() == 0L) {
            return;
        }
        energy.setMaxPower(targetMax);
        energy.setTransferRates(targetMax, 0L);
        appliedMaxPower = targetMax;
    }

    private int subscribeEnergyReceiverToPorts() {
        return level == null || level.isClientSide
                ? 0
                : HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, energyPorts(), this);
    }

    private void refreshFluidPortSubscriptions() {
        recipeFluidPortSubscriptions.refreshTransceiver(level, worldPosition, recipeFluidPorts(),
                receivingTankList, sendingTankList, this);
        coolingFluidPortSubscriptions.refreshTransceiver(level, worldPosition, coolingFluidPorts(),
                coolingReceivingTankList, coolingSendingTankList, coolingFluidNetwork);
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
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.chemicalPlant",
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
                GenericMachineRecipe.Machine.CHEMICAL_PLANT, level, items, slot, inputSlotsFor(module));
    }

    private int[] inputSlotsFor(int module) {
        return moduleInputSlots[module];
    }

    private int[] outputSlotsFor(int module) {
        return moduleOutputSlots[module];
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
        return moduleInputTankLists[module];
    }

    private List<HbmFluidTank> moduleOutputTanks(int module) {
        return moduleOutputTankLists[module];
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

    private RORDispatcher createRorDispatcher() {
        RORDispatcher.Builder builder = RORDispatcher.builder()
                .value("anyactive", () -> anyProcessing() ? "1" : "0");
        for (int module = 0; module < MODULES; module++) {
            final int index = module;
            builder.value("progress" + index, () -> Integer.toString((int) Math.round(progress[index] * 100.0D)))
                    .value("recipe" + index, () -> selectedRecipes[index])
                    .value("active" + index, () -> didProcess[index] ? "1" : "0");
        }
        return builder.build();
    }

    private boolean anyProcessing() {
        for (boolean processing : didProcess) {
            if (processing) return true;
        }
        return false;
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_INVENTORY)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        } else if (tag.contains(TAG_MODERN_INVENTORY)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_INVENTORY, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
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
            return coolingAllTankList;
        }

        @Override
        public List<HbmFluidTank> getReceivingTanks() {
            return coolingReceivingTankList;
        }

        @Override
        public List<HbmFluidTank> getSendingTanks() {
            return coolingSendingTankList;
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
            this.itemCapability = itemHandler == null ? LazyOptional.empty() : LazyOptional.of(() -> itemHandler);
            this.fluidCapability = LazyOptional.of(() -> fluidHandler);
        }

        private final LazyOptional<IItemHandler> itemCapability;
        private final LazyOptional<IFluidHandler> fluidCapability;

        private void invalidate() {
            itemCapability.invalidate();
            fluidCapability.invalidate();
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
            if (capability == ForgeCapabilities.ITEM_HANDLER && itemHandler != null) {
                return itemCapability.cast();
            }
            if (capability == ForgeCapabilities.ENERGY) {
                return energyHandler.cast();
            }
            if (capability == ForgeCapabilities.FLUID_HANDLER) {
                return fluidCapability.cast();
            }
            return LazyOptional.empty();
        }
    }
}
