package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingStep;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKAbsorberColumn;
import com.hbm.ntm.neutron.RBMKBoilerRuntime;
import com.hbm.ntm.neutron.RBMKBoilerState;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.neutron.RBMKControlColumn;
import com.hbm.ntm.neutron.RBMKControlRodPlanner;
import com.hbm.ntm.neutron.RBMKControlState;
import com.hbm.ntm.neutron.RBMKFuelColumnRuntime;
import com.hbm.ntm.neutron.RBMKFuelRodColumnPlanner;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.neutron.RBMKHeaterRuntime;
import com.hbm.ntm.neutron.RBMKHeaterState;
import com.hbm.ntm.neutron.RBMKNeutronColumn;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.neutron.RBMKOutgasserColumn;
import com.hbm.ntm.neutron.RBMKOutgasserState;
import com.hbm.ntm.neutron.RBMKReaSimRodColumn;
import com.hbm.ntm.neutron.RBMKRodColumn;
import com.hbm.ntm.neutron.RBMKRodFluxState;
import com.hbm.ntm.neutron.RBMKRuntimeSettings;
import com.hbm.ntm.neutron.RBMKThermalRuntime;
import com.hbm.ntm.neutron.RBMKThermalState;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.OutgasserRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver;
import com.hbm.tileentity.machine.rbmk.IRBMKLoadable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RBMKColumnBlockEntity extends HbmFluidNetworkBlockEntity
        implements RBMKNeutronColumn, RBMKAbsorberColumn, RBMKControlColumn, RBMKRodColumn,
        RBMKOutgasserColumn, IRBMKFluxReceiver, IRBMKLoadable, HbmStandardFluidReceiver,
        HbmStandardFluidSender, RORValueProvider, RORInteractive {
    public static final String TAG_HEAT = "heat";
    public static final String TAG_REASIM_WATER = "reasimWater";
    public static final String TAG_REASIM_STEAM = "reasimSteam";
    public static final String TAG_FUEL_ROD = "rod";
    public static final String TAG_STORAGE_ITEMS = "items";
    public static final String TAG_STORAGE_SLOT = "slot";
    public static final String TAG_OUTGASSER_ITEMS = "outgasserItems";
    public static final String TAG_STARTING_LEVEL = "startingLevel";
    public static final String TAG_CRANE_INDICATOR = "craneIndicator";
    public static final String TAG_COLOR = "color";
    public static final String TAG_LEVEL_LOWER = "levelLower";
    public static final String TAG_LEVEL_UPPER = "levelUpper";
    public static final String TAG_HEAT_LOWER = "heatLower";
    public static final String TAG_HEAT_UPPER = "heatUpper";
    public static final String TAG_FUNCTION = "function";
    public static final String TAG_OUTGASSER_PROGRESS = RBMKOutgasserState.TAG_PROGRESS;
    private static final String TAG_BOILER_FEED = "feed";
    private static final String TAG_BOILER_STEAM = "steam";
    private static final String TAG_HEATER_FEED = "heater_feed";
    private static final String TAG_HEATER_OUTPUT = "heater_output";
    private static final String TAG_COOLER_COLD = "t0";
    private static final String TAG_COOLER_WARM = "t1";
    private static final String TAG_OUTGASSER_GAS = "gas";
    private static final int BOILER_FEED_CAPACITY = 10_000;
    private static final int BOILER_STEAM_CAPACITY = 1_000_000;
    private static final int HEATER_TANK_CAPACITY = 16_000;
    private static final int COOLER_TANK_CAPACITY = 4_000;
    private static final int OUTGASSER_GAS_CAPACITY = 64_000;
    public static final int OUTGASSER_DURATION = 10_000;
    private static final int COOLER_TRANSFER_MB = 50;
    private static final double COOLER_HEAT_REMOVED = 200.0D;
    private static final String[] CONTROL_ROR_INFO = {
            RORInfo.PREFIX_VALUE + "extraction",
            RORInfo.PREFIX_FUNCTION + "setrods" + RORInteractive.NAME_SEPARATOR + "percent",
            RORInfo.PREFIX_FUNCTION + "extendrods" + RORInteractive.NAME_SEPARATOR + "percent"
    };
    private static final String[] CONTROL_AUTO_ROR_INFO = {
            RORInfo.PREFIX_VALUE + "extraction"
    };
    private static final String[] BOILER_ROR_INFO = {
            RORInfo.PREFIX_VALUE + "feed",
            RORInfo.PREFIX_VALUE + "steam",
            RORInfo.PREFIX_VALUE + "consumption"
    };
    private static final String[] HEATER_ROR_INFO = {
            RORInfo.PREFIX_VALUE + "in",
            RORInfo.PREFIX_VALUE + "out"
    };
    private static final String[] NO_ROR_INFO = {};

    private double heat = 20.0D;
    private int reasimWater;
    private int reasimSteam;
    private int craneIndicator;
    private int lastBoilerConsumption;
    private ItemStack fuelRod = ItemStack.EMPTY;
    private final RBMKRodFluxState rodFluxState = new RBMKRodFluxState();
    private final RBMKControlState controlState = new RBMKControlState();
    private final ItemStackHandler storageItems = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof RBMKFuelRodItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> storageItemHandler = LazyOptional.of(() -> storageItems);
    private final ItemStackHandler outgasserItems = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && outgasserRecipeFor(stack) != null;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 1 ? 64 : super.getSlotLimit(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> outgasserItemHandler = LazyOptional.of(() -> outgasserItems);
    private double startingLevel;
    @Nullable
    private RBMKControlRodPlanner.RBMKColor color;
    private double levelLower;
    private double levelUpper;
    private double heatLower;
    private double heatUpper;
    private RBMKControlRodPlanner.RBMKFunction function = RBMKControlRodPlanner.RBMKFunction.LINEAR;
    private final HbmFluidTank boilerFeedTank;
    private final HbmFluidTank boilerSteamTank;
    private final HbmFluidTank heaterFeedTank;
    private final HbmFluidTank heaterOutputTank;
    private final HbmFluidTank coolerColdTank;
    private final HbmFluidTank coolerWarmTank;
    private final HbmFluidTank outgasserGasTank;
    private final RBMKOutgasserState outgasserState = new RBMKOutgasserState();

    public RBMKColumnBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, TankBundle.create());
    }

    private RBMKColumnBlockEntity(BlockPos pos, BlockState state, TankBundle tanks) {
        super(ModBlockEntities.RBMK_COLUMN.get(), pos, state, tanks.allTanks());
        this.boilerFeedTank = tanks.boilerFeedTank();
        this.boilerSteamTank = tanks.boilerSteamTank();
        this.heaterFeedTank = tanks.heaterFeedTank();
        this.heaterOutputTank = tanks.heaterOutputTank();
        this.coolerColdTank = tanks.coolerColdTank();
        this.coolerWarmTank = tanks.coolerWarmTank();
        this.outgasserGasTank = tanks.outgasserGasTank();
    }

    public static RBMKColumnBlockEntity create(BlockPos pos, BlockState state) {
        return state.getBlock() instanceof RBMKColumnBlock column && column.kind().reasim()
                ? new ReaSim(pos, state)
                : new RBMKColumnBlockEntity(pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKColumnBlockEntity blockEntity) {
        if (blockEntity.heat < 20.0D) {
            blockEntity.heat = 20.0D;
            blockEntity.setChanged();
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
        if (blockEntity.craneIndicator > 0) {
            blockEntity.craneIndicator--;
            blockEntity.setChanged();
        }
        blockEntity.tickBaseThermal(level, pos);
        blockEntity.tickFluidColumn(level, pos);
        blockEntity.tickFuelRod();
        blockEntity.tickControl();
        blockEntity.tickStorage(level);
        blockEntity.networkPackNT(25);
    }

    @Override
    public RBMKNeutronHandler.RBMKType getRBMKType() {
        return kind().rbmkType();
    }

    @Override
    public boolean hasRBMKLid() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof RBMKColumnBlock && state.hasProperty(RBMKColumnBlock.LID)
                && state.getValue(RBMKColumnBlock.LID).hasLid();
    }

    @Override
    public boolean isRBMKModerated() {
        return kind().moderated();
    }

    @Override
    public void addAbsorberHeat(double heat) {
        if (getRBMKType() != RBMKNeutronHandler.RBMKType.ABSORBER) {
            return;
        }
        this.heat += heat;
        setChanged();
    }

    @Override
    public boolean hasFuelRod() {
        return kind().rod() && !fuelRod.isEmpty() && fuelRod.getItem() instanceof RBMKFuelRodItem;
    }

    @Override
    public double lastFluxQuantity() {
        return rodFluxState.lastFluxQuantity();
    }

    public int fuelRodRenderColor() {
        if (rodFluxState.rodColor() != 0) {
            return rodFluxState.rodColor();
        }
        return fuelRod.getItem() instanceof RBMKFuelRodItem item
                ? item.getSpec().colorTint()
                : 0x304825;
    }

    @Override
    public void receiveFlux(RBMKNeutronHandler.RBMKNeutronStream stream) {
        if (kind().rod()) {
            rodFluxState.receiveFlux(stream);
            setChanged();
            return;
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER) {
            RBMKOutgasserState.OutgasserFluxResult result =
                    outgasserState.receiveFlux(stream, runtimeSettings(), canProcessFlux());
            if (result.shouldProcessRecipe()) {
                processOutgasserRecipe();
            }
            if (result.addedProgress() > 0.0D || result.shouldProcessRecipe()) {
                setChanged();
            }
        }
    }

    @Override
    public boolean canProcessFlux() {
        return kind() == RBMKColumnBlock.Kind.OUTGASSER && canProcessOutgasserRecipe();
    }

    @Override
    public double controlLevel() {
        return kind().control() ? controlState.level() : 0.0D;
    }

    @Override
    public double controlMultiplier() {
        RBMKColumnBlock.Kind kind = kind();
        if (!kind.control()) {
            return 0.0D;
        }
        if (kind.automatic()) {
            return controlState.level();
        }
        return RBMKControlRodPlanner.manualMultiplier(controlState, startingLevel, 1.0D);
    }

    public double heat() {
        return heat;
    }

    public double maxHeat() {
        return 1500.0D;
    }

    public int reasimWater() {
        return reasimWater;
    }

    public int reasimSteam() {
        return reasimSteam;
    }

    public int craneIndicator() {
        return craneIndicator;
    }

    public void setCraneIndicator(int ticks) {
        int next = Math.max(0, ticks);
        if (craneIndicator != next) {
            craneIndicator = next;
            setChanged();
        }
    }

    public int receiveReaSimWater(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, RBMKThermalState.MAX_WATER - reasimWater);
        if (accepted <= 0) {
            return 0;
        }
        reasimWater += accepted;
        setChanged();
        return accepted;
    }

    public int extractReaSimSteam(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int extracted = Math.min(amount, reasimSteam);
        if (extracted <= 0) {
            return 0;
        }
        reasimSteam -= extracted;
        setChanged();
        return extracted;
    }

    public ItemStack fuelRod() {
        return fuelRod.copy();
    }

    public boolean isFuelRodColumn() {
        return kind().rod();
    }

    public RBMKFuelRodSpec autoloaderFuelSpec() {
        return fuelRod.getItem() instanceof RBMKFuelRodItem item ? item.getSpec() : null;
    }

    public RBMKFuelRodState autoloaderFuelState() {
        return fuelRod.getItem() instanceof RBMKFuelRodItem item ? item.getState(fuelRod) : null;
    }

    public boolean coldEnoughForAutoloader() {
        return kind().rod() && RBMKFuelColumnRuntime.coldEnoughForAutoloader(autoloaderFuelState());
    }

    public boolean shouldAutoloaderReplaceFuelRod(int cycle) {
        if (!kind().rod()) {
            return false;
        }
        if (!hasFuelRod()) {
            return true;
        }
        RBMKFuelRodSpec spec = autoloaderFuelSpec();
        RBMKFuelRodState state = autoloaderFuelState();
        return spec != null && state != null && state.enrichment(spec) * 100.0D < cycle;
    }

    public boolean autoloaderLoadFuelRod(ItemStack stack) {
        if (!kind().rod() || hasFuelRod() || !(stack.getItem() instanceof RBMKFuelRodItem item)) {
            return false;
        }
        RBMKFuelRodColumnPlanner.LoadPlan plan =
                RBMKFuelRodColumnPlanner.planAutoloaderLoad(item.getLegacyRodId(), true);
        if (!plan.accepted()) {
            return false;
        }
        fuelRod = stack.copy();
        fuelRod.setCount(1);
        rodFluxState.setHasRod(true);
        setChanged();
        return true;
    }

    public ItemStack autoloaderUnloadFuelRod() {
        if (!hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
            return ItemStack.EMPTY;
        }
        RBMKFuelRodState state = item.getState(fuelRod);
        RBMKFuelRodColumnPlanner.UnloadPlan plan =
                RBMKFuelRodColumnPlanner.planAutoloaderUnload(item.getLegacyRodId(), state);
        if (!plan.accepted()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = fuelRod.copy();
        fuelRod = ItemStack.EMPTY;
        rodFluxState.clearRodTick();
        setChanged();
        return stack;
    }

    public boolean loadFuelRod(ItemStack stack) {
        if (!kind().rod() || hasFuelRod() || !(stack.getItem() instanceof RBMKFuelRodItem)) {
            return false;
        }
        fuelRod = stack.copy();
        fuelRod.setCount(1);
        rodFluxState.setHasRod(true);
        setChanged();
        return true;
    }

    public ItemStack manualUnloadFuelRod() {
        if (!hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
            return ItemStack.EMPTY;
        }
        RBMKFuelRodState state = item.getState(fuelRod);
        RBMKFuelRodColumnPlanner.UnloadPlan plan =
                RBMKFuelRodColumnPlanner.planManualUnload(item.getLegacyRodId(), state);
        if (!plan.accepted()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = fuelRod.copy();
        fuelRod = ItemStack.EMPTY;
        rodFluxState.clearRodTick();
        setChanged();
        return stack;
    }

    public boolean isCraneLoadable() {
        return kind().rod() || kind().storage();
    }

    public boolean canCraneLoad(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof RBMKFuelRodItem)) {
            return false;
        }
        if (kind().rod()) {
            return !hasFuelRod();
        }
        return canLoadStorageRod();
    }

    public boolean canCraneUnload() {
        if (kind().rod()) {
            return canManualUnloadFuelRod();
        }
        return canUnloadStorageRod();
    }

    public boolean craneLoad(ItemStack stack) {
        if (!canCraneLoad(stack)) {
            return false;
        }
        return kind().rod() ? loadFuelRod(stack) : loadStorageRod(stack);
    }

    public ItemStack craneUnload() {
        if (kind().rod()) {
            return manualUnloadFuelRod();
        }
        if (!canUnloadStorageRod()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = provideNextStorageRod().copy();
        unloadStorageRod();
        return stack;
    }

    @Override
    public boolean canLoad(ItemStack toLoad) {
        return canCraneLoad(toLoad);
    }

    @Override
    public void load(ItemStack toLoad) {
        craneLoad(toLoad);
    }

    @Override
    public boolean canUnload() {
        return canCraneUnload();
    }

    @Override
    public ItemStack provideNext() {
        if (kind().rod()) {
            return fuelRod.copy();
        }
        return provideNextStorageRod().copy();
    }

    @Override
    public void unload() {
        if (kind().rod()) {
            if (canManualUnloadFuelRod()) {
                fuelRod = ItemStack.EMPTY;
                rodFluxState.clearRodTick();
                setChanged();
            }
            return;
        }
        unloadStorageRod();
    }

    private boolean canManualUnloadFuelRod() {
        if (!hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
            return false;
        }
        RBMKFuelRodState state = item.getState(fuelRod);
        return RBMKFuelRodColumnPlanner.planManualUnload(item.getLegacyRodId(), state).accepted();
    }

    public ItemStack removeFuelRodForDrop() {
        if (fuelRod.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = fuelRod.copy();
        fuelRod = ItemStack.EMPTY;
        rodFluxState.clearRodTick();
        setChanged();
        return stack;
    }

    public ItemStackHandler storageItems() {
        return storageItems;
    }

    public ItemStackHandler outgasserItems() {
        return outgasserItems;
    }

    public double outgasserProgress() {
        return outgasserState.progress();
    }

    public HbmFluidTank outgasserGasTank() {
        return outgasserGasTank;
    }

    public boolean canLoadStorageRod() {
        return kind().storage() && storageItems.getStackInSlot(11).isEmpty();
    }

    public boolean loadStorageRod(ItemStack stack) {
        if (!canLoadStorageRod() || stack.isEmpty()) {
            return false;
        }
        ItemStack copy = stack.copy();
        storageItems.setStackInSlot(11, copy);
        setChanged();
        return true;
    }

    public boolean canUnloadStorageRod() {
        return kind().storage() && !storageItems.getStackInSlot(0).isEmpty();
    }

    public ItemStack provideNextStorageRod() {
        return kind().storage() ? storageItems.getStackInSlot(0) : ItemStack.EMPTY;
    }

    public void unloadStorageRod() {
        if (!kind().storage()) {
            return;
        }
        storageItems.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
    }

    public List<ItemStack> removeStorageForDrop() {
        if (!kind().storage()) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < storageItems.getSlots(); slot++) {
            ItemStack stack = storageItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                storageItems.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        setChanged();
        return drops;
    }

    public List<ItemStack> removeOutgasserItemsForDrop() {
        if (kind() != RBMKColumnBlock.Kind.OUTGASSER) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < outgasserItems.getSlots(); slot++) {
            ItemStack stack = outgasserItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                outgasserItems.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        setChanged();
        return drops;
    }

    public RBMKControlState controlState() {
        return controlState;
    }

    public double startingLevel() {
        return startingLevel;
    }

    @Nullable
    public RBMKControlRodPlanner.RBMKColor color() {
        return color;
    }

    public RBMKConsolePlanner.ColumnType consoleType() {
        return switch (kind()) {
            case BLANK -> RBMKConsolePlanner.ColumnType.BLANK;
            case MODERATOR -> RBMKConsolePlanner.ColumnType.MODERATOR;
            case REFLECTOR -> RBMKConsolePlanner.ColumnType.REFLECTOR;
            case ABSORBER -> RBMKConsolePlanner.ColumnType.ABSORBER;
            case ROD, ROD_MOD -> RBMKConsolePlanner.ColumnType.FUEL;
            case ROD_REASIM, ROD_REASIM_MOD -> RBMKConsolePlanner.ColumnType.FUEL_SIM;
            case BOILER -> RBMKConsolePlanner.ColumnType.BOILER;
            case HEATER -> RBMKConsolePlanner.ColumnType.HEATEX;
            case COOLER -> RBMKConsolePlanner.ColumnType.COOLER;
            case OUTGASSER -> RBMKConsolePlanner.ColumnType.OUTGASSER;
            case STORAGE -> RBMKConsolePlanner.ColumnType.STORAGE;
            case CONTROL, CONTROL_MOD, CONTROL_REASIM -> RBMKConsolePlanner.ColumnType.CONTROL;
            case CONTROL_AUTO, CONTROL_REASIM_AUTO -> RBMKConsolePlanner.ColumnType.CONTROL_AUTO;
        };
    }

    public CompoundTag consoleData() {
        CompoundTag tag = new CompoundTag();
        if (kind().rod() && fuelRod.getItem() instanceof RBMKFuelRodItem item) {
            RBMKFuelRodSpec spec = item.getSpec();
            RBMKFuelRodState state = item.getState(fuelRod);
            tag.putDouble("enrichment", state.enrichment(spec));
            tag.putDouble("xenon", state.xenon());
            tag.putDouble("c_heat", state.hullHeat());
            tag.putDouble("c_coreHeat", state.coreHeat());
            tag.putDouble("c_maxHeat", spec.meltingPoint());
        }
        if (kind().control()) {
            tag.putDouble("level", controlState.level());
            tag.putDouble("targetLevel", controlState.targetLevel());
            if (!kind().automatic()) {
                tag.putShort("color", (short) (color == null ? -1 : color.ordinal()));
            }
        }
        if (kind() == RBMKColumnBlock.Kind.BOILER) {
            tag.putInt("water", boilerFeedTank.getFill());
            tag.putInt("maxWater", boilerFeedTank.getMaxFill());
            tag.putInt("steam", boilerSteamTank.getFill());
            tag.putInt("maxSteam", boilerSteamTank.getMaxFill());
            tag.putShort("type", (short) boilerSteamTank.getTankType().getId());
        }
        if (kind() == RBMKColumnBlock.Kind.HEATER) {
            tag.putInt("water", heaterFeedTank.getFill());
            tag.putInt("maxWater", heaterFeedTank.getMaxFill());
            tag.putInt("steam", heaterOutputTank.getFill());
            tag.putInt("maxSteam", heaterOutputTank.getMaxFill());
            tag.putShort("type", (short) heaterFeedTank.getTankType().getId());
            tag.putShort("hottype", (short) heaterOutputTank.getTankType().getId());
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER) {
            tag.putInt("gas", outgasserGasTank.getFill());
            tag.putInt("maxGas", outgasserGasTank.getMaxFill());
            tag.putShort("type", (short) outgasserGasTank.getTankType().getId());
            tag.putDouble("progress", outgasserState.progress());
        }
        return tag;
    }

    public RBMKConsolePlanner.ColumnSnapshot displaySnapshot() {
        return RBMKConsolePlanner.displayColumnSnapshot(consoleType(), consoleData(), heat, maxHeat(),
                craneIndicator, color);
    }

    public RBMKControlRodPlanner.AutoSettings autoSettings() {
        return new RBMKControlRodPlanner.AutoSettings(levelLower, levelUpper, heatLower, heatUpper, function);
    }

    public void setControlTarget(double targetLevel) {
        startingLevel = controlState.level();
        controlState.setTargetLevel(targetLevel);
        setChanged();
    }

    public void setManualControlColor(@Nullable RBMKControlRodPlanner.RBMKColor color) {
        if (!kind().control() || kind().automatic()) {
            return;
        }
        this.color = color;
        setChanged();
    }

    public void cycleBoilerCompressor() {
        if (kind() != RBMKColumnBlock.Kind.BOILER) {
            return;
        }
        RBMKBoilerState state = new RBMKBoilerState();
        state.setFeedMax(boilerFeedTank.getMaxFill());
        state.setFeedFill(boilerFeedTank.getFill());
        state.setSteamMax(boilerSteamTank.getMaxFill());
        state.setSteamFill(boilerSteamTank.getFill());
        state.setSteamGrade(steamGradeFor(boilerSteamTank.getTankType()));
        RBMKBoilerRuntime.cycleCompressor(state);
        boilerSteamTank.setTankType(fluidForSteamGrade(state.steamGrade()));
        boilerSteamTank.setFill(state.steamFill());
        onFluidContentsChanged();
        setChanged();
    }

    private void tickControl() {
        RBMKColumnBlock.Kind kind = kind();
        if (!kind.control()) {
            return;
        }
        if (kind.automatic()) {
            RBMKControlRodPlanner.AutoTargetPlan target =
                    RBMKControlRodPlanner.planAutoTarget(heat, autoSettings());
            controlState.setTargetLevel(target.targetLevel());
        }
        double before = controlState.level();
        controlState.tick(kind.powered(), runtimeSettings());
        if (controlState.level() != before) {
            setChanged();
        }
    }

    private void tickBaseThermal(Level level, BlockPos pos) {
        RBMKRuntimeSettings settings = runtimeSettings();
        RBMKThermalState primary = thermalState();
        List<NeighborThermalState> neighbors = horizontalNeighborThermalStates(level, pos);
        boolean equalized = RBMKThermalRuntime.equalizeWithNeighbors(
                primary,
                neighbors.stream().map(NeighborThermalState::thermalState).toList(),
                settings);
        if (settings.reasimBoilers()) {
            RBMKThermalRuntime.boilWater(primary, settings);
        }
        RBMKThermalRuntime.coolPassively(primary, settings, neighbors.size());
        applyThermalState(primary);
        if (equalized) {
            for (NeighborThermalState neighbor : neighbors) {
                neighbor.column().applyThermalState(neighbor.thermalState());
                neighbor.column().setChanged();
            }
        }
        setChanged();
    }

    private void tickFluidColumn(Level level, BlockPos pos) {
        returnFixedTankTypes();
        RBMKColumnBlock.Kind kind = kind();
        if (kind == RBMKColumnBlock.Kind.BOILER) {
            tickBoilerColumn();
            provideFluidIfPresent(boilerSteamTank);
            return;
        }
        if (kind == RBMKColumnBlock.Kind.HEATER) {
            tickHeaterColumn();
            provideFluidIfPresent(heaterOutputTank);
            return;
        }
        if (kind == RBMKColumnBlock.Kind.COOLER) {
            tickCoolerColumn(level, pos);
            provideFluidIfPresent(coolerWarmTank);
            return;
        }
        if (kind == RBMKColumnBlock.Kind.OUTGASSER) {
            outgasserState.tick(canProcessFlux());
            provideFluidIfPresent(outgasserGasTank);
        }
    }

    private void tickBoilerColumn() {
        RBMKBoilerState state = new RBMKBoilerState();
        state.setFeedMax(boilerFeedTank.getMaxFill());
        state.setFeedFill(boilerFeedTank.getFill());
        state.setSteamMax(boilerSteamTank.getMaxFill());
        state.setSteamFill(boilerSteamTank.getFill());
        state.setSteamGrade(steamGradeFor(boilerSteamTank.getTankType()));
        RBMKThermalState thermalState = thermalState();
        RBMKBoilerRuntime.BoilerTickResult result =
                RBMKBoilerRuntime.tickBoiler(runtimeSettings(), thermalState, state);
        applyThermalState(thermalState);
        lastBoilerConsumption = result.waterUsed();
        boilerFeedTank.setFill(state.feedFill());
        boilerSteamTank.setTankType(fluidForSteamGrade(state.steamGrade()));
        boilerSteamTank.setFill(state.steamFill());
        if (result.waterUsed() > 0 || result.steamProduced() > 0 || result.vented()) {
            onFluidContentsChanged();
        }
    }

    private void tickHeaterColumn() {
        HeatableFluidTrait trait = heaterFeedTank.getTankType().getTrait(HeatableFluidTrait.class);
        HeatingStep step = trait == null ? null : trait.getFirstStep();
        double efficiency = trait == null ? 0.0D : trait.getEfficiency(HeatingType.HEATEXCHANGER);
        RBMKHeaterRuntime.HeatingStepSpec spec = step == null
                ? null
                : new RBMKHeaterRuntime.HeatingStepSpec(
                        step.amountRequired(),
                        step.amountProduced(),
                        step.heatRequired(),
                        step.producedType().getTemperature(),
                        efficiency);
        if (step != null && heaterOutputTank.isEmpty()) {
            heaterOutputTank.setTankType(step.producedType());
        }

        RBMKHeaterState state = new RBMKHeaterState();
        state.setFeedMax(heaterFeedTank.getMaxFill());
        state.setFeedFill(heaterFeedTank.getFill());
        state.setOutputMax(heaterOutputTank.getMaxFill());
        state.setOutputFill(heaterOutputTank.getFill());
        RBMKThermalState thermalState = thermalState();
        RBMKHeaterRuntime.HeaterTickResult result =
                RBMKHeaterRuntime.tickHeater(thermalState, state, spec);
        applyThermalState(thermalState);
        heaterFeedTank.setFill(state.feedFill());
        if (step != null) {
            heaterOutputTank.setTankType(step.producedType());
        }
        heaterOutputTank.setFill(state.outputFill());
        if (result.inputUsed() > 0 || result.outputProduced() > 0) {
            onFluidContentsChanged();
        }
    }

    private void tickCoolerColumn(Level level, BlockPos pos) {
        if (coolerColdTank.getFill() < COOLER_TRANSFER_MB || coolerWarmTank.getSpaceFor(HbmFluids.PERFLUOROMETHYL) < COOLER_TRANSFER_MB) {
            return;
        }
        coolerColdTank.drain(COOLER_TRANSFER_MB, false);
        coolerWarmTank.fill(HbmFluids.PERFLUOROMETHYL, COOLER_TRANSFER_MB, coolerWarmTank.getPressure(), false);
        for (RBMKColumnBlockEntity column : nearbyCoolerColumns(level, pos)) {
            column.reduceHeat(COOLER_HEAT_REMOVED);
        }
        onFluidContentsChanged();
    }

    private void processOutgasserRecipe() {
        OutgasserRecipe recipe = outgasserRecipeFor(outgasserItems.getStackInSlot(0));
        if (recipe == null || !canProcessOutgasserRecipe(recipe)) {
            return;
        }

        outgasserItems.extractItem(0, 1, false);
        recipe.fluidOutput().ifPresent(fluid -> {
            outgasserGasTank.setTankType(fluid.type());
            outgasserGasTank.fill(fluid.type(), fluid.amount(), fluid.pressure(), false);
        });
        recipe.solidOutput().ifPresent(output -> {
            ItemStack existing = outgasserItems.getStackInSlot(1);
            if (existing.isEmpty()) {
                outgasserItems.setStackInSlot(1, output.copy());
            } else {
                ItemStack merged = existing.copy();
                merged.grow(output.getCount());
                outgasserItems.setStackInSlot(1, merged);
            }
        });
        onFluidContentsChanged();
        setChanged();
    }

    private boolean canProcessOutgasserRecipe() {
        OutgasserRecipe recipe = outgasserRecipeFor(outgasserItems.getStackInSlot(0));
        return recipe != null && canProcessOutgasserRecipe(recipe);
    }

    private boolean canProcessOutgasserRecipe(OutgasserRecipe recipe) {
        if (recipe == null || recipe.fusionOnly()) {
            return false;
        }
        ItemStack input = outgasserItems.getStackInSlot(0);
        if (input.isEmpty() || !recipe.matches(input)) {
            return false;
        }
        if (recipe.fluidOutput().isPresent()) {
            HbmFluidStack fluid = recipe.fluidOutput().get();
            if (outgasserGasTank.getTankType() != fluid.type() && outgasserGasTank.getFill() > 0) {
                return false;
            }
            outgasserGasTank.setTankType(fluid.type());
            if (outgasserGasTank.getFill() + fluid.amount() > outgasserGasTank.getMaxFill()) {
                return false;
            }
        }
        if (recipe.solidOutput().isEmpty()) {
            return true;
        }
        ItemStack output = recipe.solidOutput().get();
        ItemStack existing = outgasserItems.getStackInSlot(1);
        return existing.isEmpty()
                || ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    @Nullable
    private OutgasserRecipe outgasserRecipeFor(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.OUTGASSER.type().get())
                .stream()
                .filter(recipe -> recipe.matchesIgnoringCount(stack))
                .sorted((left, right) -> Boolean.compare(
                        left.input().legacyOreName() != null,
                        right.input().legacyOreName() != null))
                .findFirst()
                .orElse(null);
    }

    private void provideFluidIfPresent(HbmFluidTank tank) {
        if (tank.getTankType() != HbmFluids.NONE && tank.getFill() > 0) {
            tryProvideFluidToPorts(tank.getTankType(), tank.getPressure(), this);
        }
    }

    private void returnFixedTankTypes() {
        if (boilerFeedTank.isEmpty()) {
            boilerFeedTank.setTankType(HbmFluids.WATER);
        }
        if (boilerSteamTank.getTankType() == HbmFluids.NONE) {
            boilerSteamTank.setTankType(HbmFluids.STEAM);
        }
        if (heaterFeedTank.getTankType() == HbmFluids.NONE) {
            heaterFeedTank.setTankType(HbmFluids.COOLANT);
        }
        if (heaterOutputTank.getTankType() == HbmFluids.NONE) {
            heaterOutputTank.setTankType(HbmFluids.COOLANT_HOT);
        }
        if (coolerColdTank.isEmpty()) {
            coolerColdTank.setTankType(HbmFluids.PERFLUOROMETHYL_COLD);
        }
        if (coolerWarmTank.isEmpty()) {
            coolerWarmTank.setTankType(HbmFluids.PERFLUOROMETHYL);
        }
        if (outgasserGasTank.isEmpty()) {
            outgasserGasTank.setTankType(HbmFluids.TRITIUM);
        }
    }

    private RBMKThermalState thermalState() {
        RBMKThermalState thermalState = new RBMKThermalState();
        thermalState.setHeat(heat);
        thermalState.setReasimWater(reasimWater);
        thermalState.setReasimSteam(reasimSteam);
        thermalState.setCraneIndicator(craneIndicator);
        return thermalState;
    }

    private void applyThermalState(RBMKThermalState thermalState) {
        heat = thermalState.heat();
        reasimWater = thermalState.reasimWater();
        reasimSteam = thermalState.reasimSteam();
        craneIndicator = thermalState.craneIndicator();
    }

    @Override
    public String[] getFunctionInfo() {
        RBMKColumnBlock.Kind kind = kind();
        if (kind.rod()) {
            return RBMKFuelRodColumnPlanner.redstoneRadioFunctionInfo();
        }
        if (kind.control()) {
            return kind.automatic() ? CONTROL_AUTO_ROR_INFO : CONTROL_ROR_INFO;
        }
        if (kind == RBMKColumnBlock.Kind.BOILER) {
            return BOILER_ROR_INFO;
        }
        if (kind == RBMKColumnBlock.Kind.HEATER) {
            return HEATER_ROR_INFO;
        }
        return NO_ROR_INFO;
    }

    @Override
    public String provideRORValue(String name) {
        RBMKColumnBlock.Kind kind = kind();
        if (kind.rod()) {
            return provideRodRORValue(name);
        }
        if (kind.control() && (RORInfo.PREFIX_VALUE + "extraction").equals(name)) {
            return Integer.toString((int) (controlState.level() * 100.0D));
        }
        if (kind == RBMKColumnBlock.Kind.BOILER) {
            if ((RORInfo.PREFIX_VALUE + "feed").equals(name)) {
                return Integer.toString(boilerFeedTank.getFill());
            }
            if ((RORInfo.PREFIX_VALUE + "steam").equals(name)) {
                return Integer.toString(boilerSteamTank.getFill());
            }
            if ((RORInfo.PREFIX_VALUE + "consumption").equals(name)) {
                return Integer.toString(lastBoilerConsumption);
            }
        }
        if (kind == RBMKColumnBlock.Kind.HEATER) {
            if ((RORInfo.PREFIX_VALUE + "in").equals(name)) {
                return Integer.toString(heaterFeedTank.getFill());
            }
            if ((RORInfo.PREFIX_VALUE + "out").equals(name)) {
                return Integer.toString(heaterOutputTank.getFill());
            }
        }
        return null;
    }

    private String provideRodRORValue(String name) {
        RBMKFuelRodState state = null;
        RBMKFuelRodSpec spec = null;
        if (fuelRod.getItem() instanceof RBMKFuelRodItem item) {
            state = item.getState(fuelRod);
            spec = item.getSpec();
        }
        RBMKFuelRodColumnPlanner.RedstoneRadioValuePlan plan =
                RBMKFuelRodColumnPlanner.redstoneRadioValue(name, thermalState(), rodFluxState, spec, state);
        return plan.present() ? plan.value() : null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        RBMKColumnBlock.Kind kind = kind();
        if (!kind.control() || kind.automatic()) {
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setrods").equals(name) && params.length > 0) {
            int percent = RORInteractive.parseInt(params[0], 0, 100);
            setControlTarget(percent / 100.0D);
            setChanged();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "extendrods").equals(name) && params.length > 0) {
            int percent = RORInteractive.parseInt(params[0], -100, 100);
            setControlTarget(Math.max(0.0D, Math.min(1.0D, controlState.targetLevel() + percent / 100.0D)));
            setChanged();
            return null;
        }
        return null;
    }

    private void reduceHeat(double amount) {
        heat = Math.max(RBMKThermalState.MIN_PASSIVE_HEAT, heat - Math.max(0.0D, amount));
        setChanged();
    }

    private void tickFuelRod() {
        if (!kind().rod()) {
            return;
        }
        if (!(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
            if (!fuelRod.isEmpty()) {
                fuelRod = ItemStack.EMPTY;
                setChanged();
            }
            RBMKFuelRodColumnPlanner.planEmptyTick(rodFluxState);
            return;
        }

        RBMKFuelRodState state = item.getState(fuelRod);
        RBMKThermalState thermalState = new RBMKThermalState();
        thermalState.setHeat(heat);
        thermalState.setReasimWater(reasimWater);
        thermalState.setReasimSteam(reasimSteam);
        RBMKFuelRodColumnPlanner.ColumnTickPlan plan = RBMKFuelRodColumnPlanner.planFuelRodTick(
                runtimeSettings(),
                thermalState,
                rodFluxState,
                item.getLegacyRodId(),
                state,
                hasRBMKLid(),
                RBMKFuelColumnRuntime.DEFAULT_COLUMN_MAX_HEAT);

        heat = thermalState.heat();
        reasimWater = thermalState.reasimWater();
        reasimSteam = thermalState.reasimSteam();
        item.setState(fuelRod, state);
        if (level != null && plan.leakRadiation() > 0.0D) {
            RBMKNeutronHandler.settings().leakHandler().leak(level, worldPosition, (float) plan.leakRadiation());
        }
        if (plan.spreadFlux()) {
            if (kind().reasim()) {
                RBMKNeutronHandler.spreadReaSimFlux(this, plan.outgoingFluxQuantity(), plan.outgoingFluxRatio());
            } else {
                RBMKNeutronHandler.spreadCardinalFlux(this, plan.outgoingFluxQuantity(), plan.outgoingFluxRatio());
            }
        }
        setChanged();
    }

    private void tickStorage(Level level) {
        if (!kind().storage() || level.getGameTime() % 10L != 0L) {
            return;
        }
        boolean changed = false;
        for (int slot = 0; slot < storageItems.getSlots() - 1; slot++) {
            if (storageItems.getStackInSlot(slot).isEmpty()
                    && !storageItems.getStackInSlot(slot + 1).isEmpty()) {
                storageItems.setStackInSlot(slot, storageItems.getStackInSlot(slot + 1));
                storageItems.setStackInSlot(slot + 1, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    public RBMKColumnBlock.Kind kind() {
        return getBlockState().getBlock() instanceof RBMKColumnBlock column
                ? column.kind()
                : RBMKColumnBlock.Kind.BLANK;
    }

    private RBMKRuntimeSettings runtimeSettings() {
        return level == null ? RBMKRuntimeSettings.legacyDefaults() : NeutronHandler.rbmkRuntimeSettings(level);
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            NeutronNodeWorld.removeNode(level, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null) {
            NeutronNodeWorld.removeNode(level, worldPosition);
        }
        super.onChunkUnloaded();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble(TAG_HEAT, heat);
        tag.putInt(TAG_REASIM_WATER, reasimWater);
        tag.putInt(TAG_REASIM_STEAM, reasimSteam);
        tag.putInt(TAG_CRANE_INDICATOR, craneIndicator);
        if (kind().rod()) {
            rodFluxState.save(tag);
            if (!fuelRod.isEmpty()) {
                tag.put(TAG_FUEL_ROD, fuelRod.save(new CompoundTag()));
            }
        }
        if (kind().storage()) {
            tag.put(TAG_STORAGE_ITEMS, HbmItemStackUtil.saveSlottedItems(storageItems, TAG_STORAGE_SLOT));
        }
        if (kind() == RBMKColumnBlock.Kind.BOILER) {
            boilerFeedTank.writeToNbt(tag, TAG_BOILER_FEED);
            boilerSteamTank.writeToNbt(tag, TAG_BOILER_STEAM);
        }
        if (kind() == RBMKColumnBlock.Kind.HEATER) {
            heaterFeedTank.writeToNbt(tag, TAG_HEATER_FEED);
            heaterOutputTank.writeToNbt(tag, TAG_HEATER_OUTPUT);
        }
        if (kind() == RBMKColumnBlock.Kind.COOLER) {
            coolerColdTank.writeToNbt(tag, TAG_COOLER_COLD);
            coolerWarmTank.writeToNbt(tag, TAG_COOLER_WARM);
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER) {
            tag.put(TAG_OUTGASSER_ITEMS, HbmItemStackUtil.saveSlottedItems(outgasserItems, TAG_STORAGE_SLOT));
            outgasserState.save(tag);
            outgasserGasTank.writeToNbt(tag, TAG_OUTGASSER_GAS);
        }
        if (kind().control()) {
            controlState.save(tag);
            tag.putDouble(TAG_STARTING_LEVEL, startingLevel);
            tag.putDouble("mult", controlMultiplier());
            if (color != null) {
                tag.putInt(TAG_COLOR, color.ordinal());
            }
            if (kind().automatic()) {
                tag.putDouble(TAG_LEVEL_LOWER, levelLower);
                tag.putDouble(TAG_LEVEL_UPPER, levelUpper);
                tag.putDouble(TAG_HEAT_LOWER, heatLower);
                tag.putDouble(TAG_HEAT_UPPER, heatUpper);
                tag.putInt(TAG_FUNCTION, function.ordinal());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.contains(TAG_HEAT) ? tag.getDouble(TAG_HEAT) : 20.0D;
        reasimWater = tag.getInt(TAG_REASIM_WATER);
        reasimSteam = tag.getInt(TAG_REASIM_STEAM);
        craneIndicator = Math.max(0, tag.getInt(TAG_CRANE_INDICATOR));
        rodFluxState.load(tag);
        fuelRod = tag.contains(TAG_FUEL_ROD) ? ItemStack.of(tag.getCompound(TAG_FUEL_ROD)) : ItemStack.EMPTY;
        loadStorageItems(tag);
        loadOutgasserItems(tag);
        boilerFeedTank.readFromNbt(tag, TAG_BOILER_FEED);
        boilerSteamTank.readFromNbt(tag, TAG_BOILER_STEAM);
        if (tag.contains(TAG_HEATER_FEED)) {
            heaterFeedTank.readFromNbt(tag, TAG_HEATER_FEED);
        } else {
            heaterFeedTank.readFromNbt(tag, "feed");
        }
        if (tag.contains(TAG_HEATER_OUTPUT)) {
            heaterOutputTank.readFromNbt(tag, TAG_HEATER_OUTPUT);
        } else {
            heaterOutputTank.readFromNbt(tag, "steam");
        }
        coolerColdTank.readFromNbt(tag, TAG_COOLER_COLD);
        coolerWarmTank.readFromNbt(tag, TAG_COOLER_WARM);
        outgasserState.load(tag);
        outgasserGasTank.readFromNbt(tag, TAG_OUTGASSER_GAS);
        returnFixedTankTypes();
        controlState.load(tag);
        startingLevel = tag.contains(TAG_STARTING_LEVEL) ? tag.getDouble(TAG_STARTING_LEVEL) : 0.0D;
        if (tag.contains(TAG_COLOR)) {
            int index = tag.getInt(TAG_COLOR);
            color = index >= 0 && index < RBMKControlRodPlanner.RBMKColor.values().length
                    ? RBMKControlRodPlanner.RBMKColor.values()[index]
                    : null;
        } else {
            color = null;
        }
        levelLower = tag.getDouble(TAG_LEVEL_LOWER);
        levelUpper = tag.getDouble(TAG_LEVEL_UPPER);
        heatLower = tag.getDouble(TAG_HEAT_LOWER);
        heatUpper = tag.getDouble(TAG_HEAT_UPPER);
        if (tag.contains(TAG_FUNCTION)) {
            int index = tag.getInt(TAG_FUNCTION);
            function = index >= 0 && index < RBMKControlRodPlanner.RBMKFunction.values().length
                    ? RBMKControlRodPlanner.RBMKFunction.values()[index]
                    : RBMKControlRodPlanner.RBMKFunction.LINEAR;
        } else {
            function = RBMKControlRodPlanner.RBMKFunction.LINEAR;
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

    private void loadStorageItems(CompoundTag tag) {
        for (int slot = 0; slot < storageItems.getSlots(); slot++) {
            storageItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        if (tag.contains(TAG_STORAGE_ITEMS)) {
            HbmItemStackUtil.loadSlottedItems(tag, TAG_STORAGE_ITEMS, TAG_STORAGE_SLOT, storageItems);
            return;
        }
        if (tag.contains(HbmItemStackUtil.LEGACY_ITEMS_TAG)) {
            NonNullList<ItemStack> legacyItems = HbmItemStackUtil.loadLegacyOrForgeItems(tag, storageItems.getSlots());
            for (int slot = 0; slot < Math.min(storageItems.getSlots(), legacyItems.size()); slot++) {
                storageItems.setStackInSlot(slot, legacyItems.get(slot));
            }
        }
    }

    private void loadOutgasserItems(CompoundTag tag) {
        for (int slot = 0; slot < outgasserItems.getSlots(); slot++) {
            outgasserItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        if (tag.contains(TAG_OUTGASSER_ITEMS)) {
            HbmItemStackUtil.loadSlottedItems(tag, TAG_OUTGASSER_ITEMS, TAG_STORAGE_SLOT, outgasserItems);
            return;
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER && tag.contains(HbmItemStackUtil.LEGACY_ITEMS_TAG)) {
            NonNullList<ItemStack> legacyItems = HbmItemStackUtil.loadLegacyOrForgeItems(tag, outgasserItems.getSlots());
            for (int slot = 0; slot < Math.min(outgasserItems.getSlots(), legacyItems.size()); slot++) {
                outgasserItems.setStackInSlot(slot, legacyItems.get(slot));
            }
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return switch (kind()) {
            case BOILER -> List.of(boilerFeedTank);
            case HEATER -> List.of(heaterFeedTank);
            case COOLER -> List.of(coolerColdTank);
            default -> List.of();
        };
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return switch (kind()) {
            case BOILER -> List.of(boilerSteamTank);
            case HEATER -> List.of(heaterOutputTank);
            case COOLER -> List.of(coolerWarmTank);
            case OUTGASSER -> List.of(outgasserGasTank);
            default -> List.of();
        };
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        prepareInputTankFor(type);
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return getReceivingTanks().stream().anyMatch(tank -> tank.getTankType() == type);
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return getSendingTanks().stream().anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (getReceivingTanks().stream().anyMatch(tank -> tank.getTankType() == type)) {
            return inputFluidPorts();
        }
        if (getSendingTanks().stream().anyMatch(tank -> tank.getTankType() == type)) {
            return outputFluidPorts();
        }
        return List.of();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return outputFluidPorts();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
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
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (getReceivingTanks().isEmpty() && getSendingTanks().isEmpty()) {
            return HbmFluidSideMode.NONE;
        }
        if (getReceivingTanks().isEmpty()) {
            return HbmFluidSideMode.OUTPUT;
        }
        if (getSendingTanks().isEmpty()) {
            return HbmFluidSideMode.INPUT;
        }
        return HbmFluidSideMode.BOTH;
    }

    private void prepareInputTankFor(FluidType type) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        if (kind() == RBMKColumnBlock.Kind.HEATER
                && heaterFeedTank.getFill() == 0
                && type.hasTrait(HeatableFluidTrait.class)) {
            heaterFeedTank.setTankType(type);
            HeatingStep step = type.getTrait(HeatableFluidTrait.class).getFirstStep();
            if (step != null && heaterOutputTank.getFill() == 0) {
                heaterOutputTank.setTankType(step.producedType());
            }
        }
    }

    private List<FluidPort> inputFluidPorts() {
        return switch (kind()) {
            case BOILER, HEATER, COOLER -> List.of(FluidPort.of(0, -1, 0, Direction.DOWN));
            default -> List.of();
        };
    }

    private List<FluidPort> outputFluidPorts() {
        int height = RBMKNeutronHandler.settings().columnHeight();
        BlockPos loaderBelow = worldPosition.below();
        BlockPos loaderTwoBelow = worldPosition.below(2);
        if (level != null && level.getBlockState(loaderBelow).is(ModBlocks.RBMK_LOADER.get())) {
            return loaderOutputPorts(-1, height);
        }
        if (level != null && level.getBlockState(loaderTwoBelow).is(ModBlocks.RBMK_LOADER.get())) {
            return loaderOutputPorts(-2, height);
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER) {
            return List.of(
                    FluidPort.of(0, height + 1, 0, Direction.UP),
                    FluidPort.of(0, -1, 0, Direction.DOWN));
        }
        if (!getSendingTanks().isEmpty()) {
            return List.of(FluidPort.of(0, height + 1, 0, Direction.UP));
        }
        return List.of();
    }

    private static List<FluidPort> loaderOutputPorts(int loaderYOffset, int columnHeight) {
        return List.of(
                FluidPort.of(0, columnHeight + 1, 0, Direction.UP),
                FluidPort.of(1, loaderYOffset, 0, Direction.EAST),
                FluidPort.of(-1, loaderYOffset, 0, Direction.WEST),
                FluidPort.of(0, loaderYOffset, 1, Direction.SOUTH),
                FluidPort.of(0, loaderYOffset, -1, Direction.NORTH),
                FluidPort.of(0, loaderYOffset - 1, 0, Direction.DOWN));
    }

    private List<NeighborThermalState> horizontalNeighborThermalStates(Level level, BlockPos pos) {
        List<NeighborThermalState> neighbors = new ArrayList<>();
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos.relative(direction));
            if (core == null || core.pos().equals(pos)) {
                continue;
            }
            if (level.getBlockEntity(core.pos()) instanceof RBMKColumnBlockEntity column) {
                neighbors.add(new NeighborThermalState(column, column.thermalState()));
            }
        }
        return neighbors;
    }

    private List<RBMKColumnBlockEntity> nearbyCoolerColumns(Level level, BlockPos pos) {
        List<RBMKColumnBlockEntity> columns = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos.offset(dx, 0, dz));
                if (core == null) {
                    continue;
                }
                if (level.getBlockEntity(core.pos()) instanceof RBMKColumnBlockEntity column
                        && !columns.contains(column)) {
                    columns.add(column);
                }
            }
        }
        return columns;
    }

    private static RBMKBoilerRuntime.SteamGrade steamGradeFor(FluidType type) {
        if (type == HbmFluids.HOTSTEAM) {
            return RBMKBoilerRuntime.SteamGrade.HOTSTEAM;
        }
        if (type == HbmFluids.SUPERHOTSTEAM) {
            return RBMKBoilerRuntime.SteamGrade.SUPERHOTSTEAM;
        }
        if (type == HbmFluids.ULTRAHOTSTEAM) {
            return RBMKBoilerRuntime.SteamGrade.ULTRAHOTSTEAM;
        }
        return RBMKBoilerRuntime.SteamGrade.STEAM;
    }

    private static FluidType fluidForSteamGrade(RBMKBoilerRuntime.SteamGrade grade) {
        return switch (grade == null ? RBMKBoilerRuntime.SteamGrade.STEAM : grade) {
            case STEAM -> HbmFluids.STEAM;
            case HOTSTEAM -> HbmFluids.HOTSTEAM;
            case SUPERHOTSTEAM -> HbmFluids.SUPERHOTSTEAM;
            case ULTRAHOTSTEAM -> HbmFluids.ULTRAHOTSTEAM;
        };
    }

    private record TankBundle(
            List<HbmFluidTank> allTanks,
            HbmFluidTank boilerFeedTank,
            HbmFluidTank boilerSteamTank,
            HbmFluidTank heaterFeedTank,
            HbmFluidTank heaterOutputTank,
            HbmFluidTank coolerColdTank,
            HbmFluidTank coolerWarmTank,
            HbmFluidTank outgasserGasTank) {
        private static TankBundle create() {
            HbmFluidTank boilerFeed = new HbmFluidTank(HbmFluids.WATER, BOILER_FEED_CAPACITY);
            HbmFluidTank boilerSteam = new HbmFluidTank(HbmFluids.STEAM, BOILER_STEAM_CAPACITY);
            HbmFluidTank heaterFeed = new HbmFluidTank(HbmFluids.COOLANT, HEATER_TANK_CAPACITY);
            HbmFluidTank heaterOutput = new HbmFluidTank(HbmFluids.COOLANT_HOT, HEATER_TANK_CAPACITY);
            HbmFluidTank coolerCold = new HbmFluidTank(HbmFluids.PERFLUOROMETHYL_COLD, COOLER_TANK_CAPACITY);
            HbmFluidTank coolerWarm = new HbmFluidTank(HbmFluids.PERFLUOROMETHYL, COOLER_TANK_CAPACITY);
            HbmFluidTank outgasserGas = new HbmFluidTank(HbmFluids.TRITIUM, OUTGASSER_GAS_CAPACITY);
            return new TankBundle(
                    List.of(boilerFeed, boilerSteam, heaterFeed, heaterOutput, coolerCold, coolerWarm, outgasserGas),
                    boilerFeed,
                    boilerSteam,
                    heaterFeed,
                    heaterOutput,
                    coolerCold,
                    coolerWarm,
                    outgasserGas);
        }
    }

    private record NeighborThermalState(RBMKColumnBlockEntity column, RBMKThermalState thermalState) {
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        storageItemHandler.invalidate();
        outgasserItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (kind().storage() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return storageItemHandler.cast();
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER && capability == ForgeCapabilities.ITEM_HANDLER) {
            return outgasserItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public static class ReaSim extends RBMKColumnBlockEntity implements RBMKReaSimRodColumn {
        public ReaSim(BlockPos pos, BlockState state) {
            super(pos, state);
        }
    }
}
