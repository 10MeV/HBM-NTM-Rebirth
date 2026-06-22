package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.block.LegacyCoriumFiniteBlock;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidOverpressure;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
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
import com.hbm.ntm.entity.effect.DigammaSpearEntity;
import com.hbm.ntm.entity.projectile.RBMKDebrisEntity;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKAbsorberColumn;
import com.hbm.ntm.neutron.RBMKBaseRuntimePlanner;
import com.hbm.ntm.neutron.RBMKBoilerRuntime;
import com.hbm.ntm.neutron.RBMKBoilerState;
import com.hbm.ntm.neutron.RBMKColumnLifecyclePlanner;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.neutron.RBMKControlColumn;
import com.hbm.ntm.neutron.RBMKControlRodPlanner;
import com.hbm.ntm.neutron.RBMKControlState;
import com.hbm.ntm.neutron.RBMKCoolerRuntime;
import com.hbm.ntm.neutron.RBMKCoolerState;
import com.hbm.ntm.neutron.RBMKDebrisPlanner.RBMKDebrisType;
import com.hbm.ntm.neutron.RBMKFuelColumnRuntime;
import com.hbm.ntm.neutron.RBMKFuelRodColumnPlanner;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.neutron.RBMKHeaterRuntime;
import com.hbm.ntm.neutron.RBMKHeaterState;
import com.hbm.ntm.neutron.RBMKMeltdownSequencePlanner;
import com.hbm.ntm.neutron.RBMKMeltPlanner;
import com.hbm.ntm.neutron.RBMKNeutronColumn;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.neutron.RBMKOutgasserColumn;
import com.hbm.ntm.neutron.RBMKOutgasserState;
import com.hbm.ntm.neutron.RBMKReaSimRodColumn;
import com.hbm.ntm.neutron.RBMKRodColumn;
import com.hbm.ntm.neutron.RBMKRodFluxState;
import com.hbm.ntm.neutron.RBMKRuntimeSettings;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.hbm.ntm.neutron.RBMKThermalRuntime;
import com.hbm.ntm.neutron.RBMKThermalState;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.OutgasserRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.tileentity.machine.rbmk.IRBMKFluxReceiver;
import com.hbm.tileentity.machine.rbmk.IRBMKLoadable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RBMKColumnBlockEntity extends HbmFluidNetworkBlockEntity
        implements RBMKNeutronColumn, RBMKAbsorberColumn, RBMKControlColumn, RBMKRodColumn,
        RBMKOutgasserColumn, IRBMKFluxReceiver, IRBMKLoadable, HbmStandardFluidReceiver,
        HbmStandardFluidSender, HbmEnergyReceiver, RORValueProvider, RORInteractive {
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
    public static final String TAG_LEVEL = "level";
    public static final String TAG_LEVEL_LOWER = "levelLower";
    public static final String TAG_LEVEL_UPPER = "levelUpper";
    public static final String TAG_HEAT_LOWER = "heatLower";
    public static final String TAG_HEAT_UPPER = "heatUpper";
    public static final String TAG_FUNCTION = "function";
    public static final String TAG_OUTGASSER_PROGRESS = RBMKOutgasserState.TAG_PROGRESS;
    private static final String TAG_BOILER_FEED = "feed";
    private static final String TAG_BOILER_STEAM = "steam";
    private static final String TAG_HEATER_ITEMS = "heaterItems";
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
    private static final int LAYOUT_REPAIR_INTERVAL = 20;
    private static final int LEGACY_MAX_COLUMN_HEIGHT_ABOVE_CORE = 15;
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
    private static final String[] COOLER_ROR_INFO = {
            RORInfo.PREFIX_VALUE + "heat",
            RORInfo.PREFIX_VALUE + "in",
            RORInfo.PREFIX_VALUE + "out"
    };
    private static final String[] OUTGASSER_ROR_INFO = {
            RORInfo.PREFIX_VALUE + "gas",
            RORInfo.PREFIX_VALUE + "gasmax",
            RORInfo.PREFIX_VALUE + "progress",
            RORInfo.PREFIX_VALUE + "type"
    };
    private static final String[] NO_ROR_INFO = {};

    private double heat = 20.0D;
    private int reasimWater;
    private int reasimSteam;
    private int craneIndicator;
    private int boilerVentDelay;
    private int lastBoilerConsumption;
    private final List<BlockPos> thermalNeighborCache = new ArrayList<>(4);
    private ItemStack fuelRod = ItemStack.EMPTY;
    private final RBMKRodFluxState rodFluxState = new RBMKRodFluxState();
    private final RBMKControlState controlState = new RBMKControlState();
    private final IItemHandlerModifiable rodItems = new IItemHandlerModifiable() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return hasOperationalLayout() && slot == 0 ? fuelRod : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !(stack.getItem() instanceof RBMKFuelRodItem)
                    || !hasOperationalLayout() || !kind().rod() || hasFuelRod()) {
                return stack;
            }
            ItemStack inserted = stack.copy();
            inserted.setCount(1);
            if (!simulate) {
                fuelRod = inserted;
                rodFluxState.setHasRod(true);
                setChanged();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(1);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!hasOperationalLayout() || slot != 0 || amount <= 0 || fuelRod.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = fuelRod.copy();
            extracted.setCount(1);
            if (!simulate) {
                fuelRod = ItemStack.EMPTY;
                rodFluxState.clearRodTick();
                setChanged();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasOperationalLayout() && slot == 0 && stack.getItem() instanceof RBMKFuelRodItem;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (!hasOperationalLayout() || slot != 0) {
                return;
            }
            if (stack.isEmpty()) {
                fuelRod = ItemStack.EMPTY;
                rodFluxState.clearRodTick();
            } else if (stack.getItem() instanceof RBMKFuelRodItem) {
                fuelRod = stack.copy();
                fuelRod.setCount(1);
                rodFluxState.setHasRod(true);
            }
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> rodItemHandler = LazyOptional.of(() -> rodItems);
    private final LazyOptional<IEnergyStorage> controlEnergyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(this, true, false));
    private final IItemHandlerModifiable rodMenuItems = new RodMenuItemHandler();
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
    private final LazyOptional<IItemHandler> storageItemHandler = LazyOptional.of(() -> new StorageAutomationItemHandler());
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
    private final ItemStackHandler heaterItems = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> heaterItemHandler = LazyOptional.of(() -> heaterItems);
    private final IItemHandlerModifiable storageMenuItems = new LayoutGuardedMenuItemHandler(storageItems);
    private final IItemHandlerModifiable outgasserMenuItems = new LayoutGuardedMenuItemHandler(outgasserItems);
    private final IItemHandlerModifiable outgasserAutomationItems = new OutgasserAutomationItemHandler();
    private final LazyOptional<IItemHandler> outgasserAutomationItemHandler =
            LazyOptional.of(() -> outgasserAutomationItems);
    private final IItemHandlerModifiable heaterMenuItems = new LayoutGuardedItemHandler(heaterItems);
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
    private final RBMKCoolerState coolerState = new RBMKCoolerState();
    private final List<BlockPos> coolerNeighborCache = new ArrayList<>(25);

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
        if (!blockEntity.ensureColumnLayout(level, pos)) {
            blockEntity.removeNeutronNodesForColumn(level);
            blockEntity.removeFluidNode();
            return;
        }
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
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            if (!ensureColumnLayoutNow(level, worldPosition)) {
                removeFluidNode();
                removeNeutronNodesForColumn(level);
            }
        }
    }

    private boolean ensureColumnLayout(Level level, BlockPos pos) {
        if (level.isClientSide || (level.getGameTime() + pos.asLong()) % LAYOUT_REPAIR_INTERVAL != 0) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos);
        }
        return ensureColumnLayoutNow(level, pos);
    }

    private boolean ensureColumnLayoutNow(Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCoreAt(level, pos);
        if (core == null || !(core.state().getBlock() instanceof RBMKColumnBlock)) {
            return false;
        }
        return MultiblockHelper.ensureOperationalCoreLayoutComplete(level, core.pos());
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
        if (!hasOperationalLayout() || getRBMKType() != RBMKNeutronHandler.RBMKType.ABSORBER) {
            return;
        }
        this.heat += heat;
        setChanged();
    }

    @Override
    public boolean hasFuelRod() {
        return hasOperationalLayout() && kind().rod() && !fuelRod.isEmpty()
                && fuelRod.getItem() instanceof RBMKFuelRodItem;
    }

    @Override
    public double lastFluxQuantity() {
        return hasOperationalLayout() ? rodFluxState.lastFluxQuantity() : 0.0D;
    }

    public int fuelRodRenderColor() {
        if (!hasOperationalLayout()) {
            return 0x304825;
        }
        if (rodFluxState.rodColor() != 0) {
            return rodFluxState.rodColor();
        }
        return fuelRod.getItem() instanceof RBMKFuelRodItem item
                ? item.getSpec().colorTint()
                : 0x304825;
    }

    @Override
    public void receiveFlux(RBMKNeutronHandler.RBMKNeutronStream stream) {
        if (!hasOperationalLayout()) {
            return;
        }
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
        return hasOperationalLayout() && kind() == RBMKColumnBlock.Kind.OUTGASSER && canProcessOutgasserRecipe();
    }

    @Override
    public double controlLevel() {
        if (!hasOperationalLayout()) {
            return 0.0D;
        }
        return kind().control() ? controlState.level() : 0.0D;
    }

    @Override
    public double controlMultiplier() {
        if (!hasOperationalLayout()) {
            return 0.0D;
        }
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
        if (!hasOperationalLayout() || amount <= 0) {
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
        if (!hasOperationalLayout() || amount <= 0) {
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
        return hasOperationalLayout() ? fuelRod.copy() : ItemStack.EMPTY;
    }

    public boolean isFuelRodColumn() {
        return kind().rod();
    }

    public RBMKFuelRodSpec autoloaderFuelSpec() {
        return hasOperationalLayout() && fuelRod.getItem() instanceof RBMKFuelRodItem item ? item.getSpec() : null;
    }

    public RBMKFuelRodState autoloaderFuelState() {
        return hasOperationalLayout() && fuelRod.getItem() instanceof RBMKFuelRodItem item ? item.getState(fuelRod) : null;
    }

    public boolean coldEnoughForAutoloader() {
        return hasOperationalLayout() && kind().rod()
                && RBMKFuelColumnRuntime.coldEnoughForAutoloader(autoloaderFuelState());
    }

    public boolean shouldAutoloaderReplaceFuelRod(int cycle) {
        if (!hasOperationalLayout() || !kind().rod()) {
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
        if (!hasOperationalLayout() || !kind().rod() || hasFuelRod()
                || !(stack.getItem() instanceof RBMKFuelRodItem item)) {
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
        if (!hasOperationalLayout() || !hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
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
        if (!hasOperationalLayout() || !kind().rod() || hasFuelRod()
                || !(stack.getItem() instanceof RBMKFuelRodItem)) {
            return false;
        }
        fuelRod = stack.copy();
        fuelRod.setCount(1);
        rodFluxState.setHasRod(true);
        setChanged();
        return true;
    }

    public ItemStack manualUnloadFuelRod() {
        if (!hasOperationalLayout() || !hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
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
        return hasOperationalLayout() && (kind().rod() || kind().storage());
    }

    public boolean canCraneLoad(ItemStack stack) {
        if (!hasOperationalLayout() || stack.isEmpty() || !(stack.getItem() instanceof RBMKFuelRodItem)) {
            return false;
        }
        if (kind().rod()) {
            return !hasFuelRod();
        }
        return canLoadStorageRod();
    }

    public boolean canCraneUnload() {
        if (!hasOperationalLayout()) {
            return false;
        }
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
        if (!hasOperationalLayout()) {
            return ItemStack.EMPTY;
        }
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
        if (!hasOperationalLayout()) {
            return ItemStack.EMPTY;
        }
        if (kind().rod()) {
            return fuelRod.copy();
        }
        return provideNextStorageRod().copy();
    }

    @Override
    public void unload() {
        if (!hasOperationalLayout()) {
            return;
        }
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

    public boolean canManualUnloadFuelRod() {
        if (!hasOperationalLayout() || !hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
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

    public IItemHandlerModifiable storageMenuItems() {
        return storageMenuItems;
    }

    public IItemHandlerModifiable rodItems() {
        return rodItems;
    }

    public IItemHandlerModifiable rodMenuItems() {
        return rodMenuItems;
    }

    public ItemStackHandler outgasserItems() {
        return outgasserItems;
    }

    public IItemHandlerModifiable outgasserMenuItems() {
        return outgasserMenuItems;
    }

    public ItemStackHandler heaterItems() {
        return heaterItems;
    }

    public IItemHandlerModifiable heaterMenuItems() {
        return heaterMenuItems;
    }

    public double outgasserProgress() {
        return hasOperationalLayout() ? outgasserState.progress() : 0.0D;
    }

    public HbmFluidTank boilerFeedTank() {
        return boilerFeedTank;
    }

    public HbmFluidTank boilerSteamTank() {
        return boilerSteamTank;
    }

    public HbmFluidTank heaterFeedTank() {
        return heaterFeedTank;
    }

    public HbmFluidTank heaterOutputTank() {
        return heaterOutputTank;
    }

    public HbmFluidTank outgasserGasTank() {
        return outgasserGasTank;
    }

    public boolean canLoadStorageRod() {
        return hasOperationalLayout() && kind().storage() && storageItems.getStackInSlot(11).isEmpty();
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
        return hasOperationalLayout() && kind().storage() && !storageItems.getStackInSlot(0).isEmpty();
    }

    public ItemStack provideNextStorageRod() {
        return hasOperationalLayout() && kind().storage() ? storageItems.getStackInSlot(0) : ItemStack.EMPTY;
    }

    public void unloadStorageRod() {
        if (!hasOperationalLayout() || !kind().storage()) {
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

    public List<ItemStack> removeHeaterItemsForDrop() {
        if (kind() != RBMKColumnBlock.Kind.HEATER) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < heaterItems.getSlots(); slot++) {
            ItemStack stack = heaterItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                heaterItems.setStackInSlot(slot, ItemStack.EMPTY);
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
        if (!hasOperationalLayout()) {
            return tag;
        }
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

    public CompoundTag diagnosticData() {
        CompoundTag tag = consoleData();
        if (!hasOperationalLayout()) {
            return tag;
        }
        tag.putDouble("heat", heat);
        tag.putDouble("maxHeat", maxHeat());
        tag.putString("rbmkType", getRBMKType().name());
        tag.putBoolean("hasLid", hasRBMKLid());

        if (kind().rod()) {
            RBMKFuelRodColumnPlanner.FuelDiagnostics diagnostics = RBMKFuelRodColumnPlanner.FuelDiagnostics.EMPTY;
            if (fuelRod.getItem() instanceof RBMKFuelRodItem item) {
                RBMKFuelRodSpec spec = item.getSpec();
                RBMKFuelRodState state = item.getState(fuelRod);
                diagnostics = RBMKFuelRodColumnPlanner.fuelDiagnostics(spec, state);
            }
            RBMKFuelRodColumnPlanner.DiagnosticNbtPlan plan =
                    RBMKFuelRodColumnPlanner.diagnosticNbtPlan(rodFluxState, diagnostics);
            tag.putDouble("fluxSlow", plan.fluxSlow());
            tag.putDouble("fluxFast", plan.fluxFast());
            tag.putBoolean("hasRod", plan.hasRod());
            if (plan.writeFuelText()) {
                tag.putString(plan.fuelYieldKey(), plan.diagnostics().fuelYield());
                tag.putString(plan.fuelXenonKey(), plan.diagnostics().fuelXenon());
                tag.putString(plan.fuelHeatKey(), plan.diagnostics().fuelHeat());
            }
        }
        return tag;
    }

    public RBMKConsolePlanner.ColumnSnapshot displaySnapshot() {
        if (!hasOperationalLayout()) {
            return null;
        }
        return RBMKConsolePlanner.displayColumnSnapshot(consoleType(), consoleData(), heat, maxHeat(),
                craneIndicator, color);
    }

    public RBMKControlRodPlanner.AutoSettings autoSettings() {
        if (!hasOperationalLayout()) {
            return new RBMKControlRodPlanner.AutoSettings(0.0D, 0.0D, 0.0D, 0.0D,
                    RBMKControlRodPlanner.RBMKFunction.LINEAR);
        }
        return new RBMKControlRodPlanner.AutoSettings(levelLower, levelUpper, heatLower, heatUpper, function);
    }

    public boolean isPoweredControlRod() {
        return kind().control() && kind().powered();
    }

    public boolean controlHasPower() {
        return kind().control() && controlState.hasPower();
    }

    @Override
    public long getPower() {
        return kind().control() ? controlState.power() : 0L;
    }

    @Override
    public void setPower(long power) {
        if (!kind().control()) {
            return;
        }
        controlState.setPower(power);
        setChanged();
    }

    @Override
    public long getMaxPower() {
        return isPoweredControlRod() ? RBMKControlState.MAX_POWER : 0L;
    }

    @Override
    public long getReceiverSpeed() {
        return isPoweredControlRod() ? getMaxPower() : 0L;
    }

    @Override
    public ConnectionPriority getPriority() {
        return ConnectionPriority.LOW;
    }

    public void setControlTarget(double targetLevel) {
        if (!hasOperationalLayout()) {
            return;
        }
        startingLevel = controlState.level();
        controlState.setTargetLevel(targetLevel);
        setChanged();
    }

    public void setManualControlColor(@Nullable RBMKControlRodPlanner.RBMKColor color) {
        if (!hasOperationalLayout() || !kind().control() || kind().automatic()) {
            return;
        }
        this.color = color;
        setChanged();
    }

    public void cycleBoilerCompressor() {
        if (!hasOperationalLayout() || kind() != RBMKColumnBlock.Kind.BOILER) {
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

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        if (!hasOperationalLayout() || !hasLegacyControlPermission(player) || tag == null || tag.isEmpty()) {
            return false;
        }
        RBMKColumnBlock.Kind kind = kind();
        return (kind.control() && (tag.contains(TAG_LEVEL) || tag.contains(TAG_COLOR)
                || tag.contains(TAG_LEVEL_LOWER) || tag.contains(TAG_LEVEL_UPPER)
                || tag.contains(TAG_HEAT_LOWER) || tag.contains(TAG_HEAT_UPPER)
                || tag.contains(TAG_FUNCTION)))
                || (kind == RBMKColumnBlock.Kind.BOILER && tag.getBoolean("compression"));
    }

    private boolean hasLegacyControlPermission(ServerPlayer player) {
        return player != null && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(),
                worldPosition.getZ()) < 400.0D;
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        boolean changed = false;
        RBMKColumnBlock.Kind kind = kind();
        if (kind.control() && tag.contains(TAG_LEVEL)) {
            setControlTarget(Mth.clamp(tag.getDouble(TAG_LEVEL), 0.0D, 1.0D));
            changed = true;
        }
        if (kind.control() && !kind.automatic() && tag.contains(TAG_COLOR)) {
            RBMKControlRodPlanner.ColorTogglePlan plan =
                    RBMKControlRodPlanner.planColorToggle(color, tag.getInt(TAG_COLOR));
            setManualControlColor(plan.color());
            changed = true;
        }
        if (kind.control() && kind.automatic()) {
            if (tag.contains(TAG_LEVEL_LOWER)) {
                levelLower = Mth.clamp(tag.getDouble(TAG_LEVEL_LOWER), 0.0D, 100.0D);
                changed = true;
            }
            if (tag.contains(TAG_LEVEL_UPPER)) {
                levelUpper = Mth.clamp(tag.getDouble(TAG_LEVEL_UPPER), 0.0D, 100.0D);
                changed = true;
            }
            if (tag.contains(TAG_HEAT_LOWER)) {
                heatLower = Mth.clamp(tag.getDouble(TAG_HEAT_LOWER), 0.0D, 9999.0D);
                changed = true;
            }
            if (tag.contains(TAG_HEAT_UPPER)) {
                heatUpper = Mth.clamp(tag.getDouble(TAG_HEAT_UPPER), 0.0D, 9999.0D);
                changed = true;
            }
            if (tag.contains(TAG_FUNCTION)) {
                RBMKControlRodPlanner.AutoFunctionPlan plan =
                        RBMKControlRodPlanner.planAutoFunctionPacket(tag.getInt(TAG_FUNCTION));
                if (plan.accepted()) {
                    function = plan.function();
                    changed = true;
                }
            }
        }
        if (kind == RBMKColumnBlock.Kind.BOILER && tag.getBoolean("compression")) {
            cycleBoilerCompressor();
            changed = true;
        }
        if (changed) {
            setChanged();
        }
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
        List<NeighborThermalState> neighbors = cachedHorizontalNeighborThermalStates(level, pos);
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
            if (HbmFluidItemTransfer.loadTankFromSlot(heaterItems, 0, 0, heaterFeedTank)) {
                onFluidContentsChanged();
                setChanged();
            }
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
        state.setVentDelay(boilerVentDelay);
        RBMKThermalState thermalState = thermalState();
        RBMKBoilerRuntime.BoilerTickResult result =
                RBMKBoilerRuntime.tickBoiler(runtimeSettings(), thermalState, state,
                        () -> 20 + level.random.nextInt(10));
        applyThermalState(thermalState);
        lastBoilerConsumption = result.waterUsed();
        boilerVentDelay = state.ventDelay();
        boilerFeedTank.setFill(state.feedFill());
        boilerSteamTank.setTankType(fluidForSteamGrade(state.steamGrade()));
        boilerSteamTank.setFill(state.steamFill());
        if (result.vented()) {
            spawnBoilerVentEffect(level);
        }
        if (result.waterUsed() > 0 || result.steamProduced() > 0 || result.vented()) {
            onFluidContentsChanged();
        }
    }

    private void spawnBoilerVentEffect(Level level) {
        int ventY = worldPosition.getY() + RBMKStructureDimensions.columnHeightAboveCore();
        ParticleUtil.spawnRbmkSteam(level,
                worldPosition.getX() + 0.25D + level.random.nextInt(2) * 0.5D,
                ventY,
                worldPosition.getZ() + 0.25D + level.random.nextInt(2) * 0.5D,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.0D,
                worldPosition.getZ() + 0.5D);
        LegacySoundPlayer.playSoundEffect(level,
                worldPosition.getX(),
                ventY,
                worldPosition.getZ(),
                "hbm:block.steamEngineOperate",
                SoundSource.BLOCKS,
                2.0F,
                1.0F + level.random.nextFloat() * 0.25F);
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
        if (!result.validHeatableFluid()) {
            boolean changed = heaterFeedTank.getTankType() != HbmFluids.NONE
                    || heaterFeedTank.getFill() > 0
                    || heaterOutputTank.getTankType() != HbmFluids.NONE
                    || heaterOutputTank.getFill() > 0;
            heaterFeedTank.setTankType(HbmFluids.NONE);
            heaterOutputTank.setTankType(HbmFluids.NONE);
            if (changed) {
                onFluidContentsChanged();
            }
            return;
        }
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
        coolerState.setColdMax(coolerColdTank.getMaxFill());
        coolerState.setColdFill(coolerColdTank.getFill());
        coolerState.setHotMax(coolerWarmTank.getMaxFill());
        coolerState.setHotFill(coolerWarmTank.getFill());
        if (coolerState.timer() <= 0) {
            refreshCoolerNeighborCache(level, pos);
        }
        List<RBMKColumnBlockEntity> cachedColumns = resolveCachedCoolerColumns(level);
        List<RBMKThermalState> cachedStates = new ArrayList<>(cachedColumns.size());
        for (RBMKColumnBlockEntity column : cachedColumns) {
            cachedStates.add(column == null ? null : column.thermalState());
        }
        RBMKCoolerRuntime.CoolerTickResult result = RBMKCoolerRuntime.tickCooler(coolerState, cachedStates);
        if (!result.convertedCoolant()) {
            return;
        }
        coolerColdTank.setFill(coolerState.coldFill());
        coolerWarmTank.setTankType(HbmFluids.PERFLUOROMETHYL);
        coolerWarmTank.setFill(coolerState.hotFill());
        for (int index : result.cooledColumnIndexes()) {
            if (index >= 0 && index < cachedColumns.size()) {
                RBMKColumnBlockEntity column = cachedColumns.get(index);
                if (column != null) {
                    column.applyThermalState(cachedStates.get(index));
                    column.setChanged();
                }
            }
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
        if (!hasOperationalLayout()) {
            return NO_ROR_INFO;
        }
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
        if (kind == RBMKColumnBlock.Kind.COOLER) {
            return COOLER_ROR_INFO;
        }
        if (kind == RBMKColumnBlock.Kind.OUTGASSER) {
            return OUTGASSER_ROR_INFO;
        }
        return NO_ROR_INFO;
    }

    @Override
    public String provideRORValue(String name) {
        if (!hasOperationalLayout()) {
            return null;
        }
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
        if (kind == RBMKColumnBlock.Kind.COOLER) {
            if ((RORInfo.PREFIX_VALUE + "heat").equals(name)) {
                return Integer.toString((int) heat);
            }
            if ((RORInfo.PREFIX_VALUE + "in").equals(name)) {
                return Integer.toString(coolerColdTank.getFill());
            }
            if ((RORInfo.PREFIX_VALUE + "out").equals(name)) {
                return Integer.toString(coolerWarmTank.getFill());
            }
        }
        if (kind == RBMKColumnBlock.Kind.OUTGASSER) {
            if ((RORInfo.PREFIX_VALUE + "gas").equals(name)) {
                return Integer.toString(outgasserGasTank.getFill());
            }
            if ((RORInfo.PREFIX_VALUE + "gasmax").equals(name)) {
                return Integer.toString(outgasserGasTank.getMaxFill());
            }
            if ((RORInfo.PREFIX_VALUE + "progress").equals(name)) {
                return Integer.toString((int) outgasserState.progress());
            }
            if ((RORInfo.PREFIX_VALUE + "type").equals(name)) {
                return outgasserGasTank.getTankType().getName();
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
        if (!hasOperationalLayout()) {
            return null;
        }
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
        if (level instanceof ServerLevel serverLevel && plan.meltdown()) {
            executeMeltdownSequence(serverLevel);
            return;
        }
        if (level != null && plan.spawnSuppressedMeltdownFlame()) {
            spawnSuppressedMeltdownFlame(level);
            setChanged();
            return;
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

    private void spawnSuppressedMeltdownFlame(Level level) {
        int columnHeight = legacyColumnHeight();
        ParticleUtil.spawnGasFlame(level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + columnHeight + 0.5D,
                worldPosition.getZ() + 0.5D,
                0.0D,
                0.2D,
                0.0D);
    }

    private void executeMeltdownSequence(ServerLevel level) {
        if (!hasOperationalLayout()) {
            removeNeutronNodesForColumn(level);
            removeFluidNode();
            return;
        }

        List<RBMKColumnBlockEntity> columns = collectConnectedOperationalColumns(level);
        if (columns.isEmpty()) {
            removeNeutronNodesForColumn(level);
            removeFluidNode();
            return;
        }

        boolean digamma = columns.stream().anyMatch(RBMKColumnBlockEntity::hasDigammaFuelRod);
        MeltdownFluidNetworks fluidNetworks = collectMeltdownFluidNetworks(columns);
        List<RBMKMeltdownSequencePlanner.ColumnRef> refs = columns.stream()
                .map(RBMKColumnBlockEntity::meltdownColumnRef)
                .toList();
        RBMKMeltdownSequencePlanner.MeltdownSequencePlan sequence =
                RBMKMeltdownSequencePlanner.planSequence(
                        refs,
                        digamma,
                        runtimeSettings().meltdownOverpressure(),
                        fluidNetworks.pipeNodes().size(),
                        fluidNetworks.receivers().size());

        for (RBMKMeltdownSequencePlanner.ColumnReducePlan reduction : sequence.columnReductions()) {
            RBMKColumnBlockEntity column = columnAt(columns, reduction.column().origin());
            if (column != null) {
                column.executeColumnMeltdown(level, reduction.reduce());
            }
        }

        executePostCoriumConversions(level, sequence.pribrisConversions());
        executeMeltdownOverpressure(level, sequence.overpressure(), fluidNetworks);
        playMeltdownEffects(level, sequence);
        awardMeltdownAchievement(level, sequence);
        spawnDigammaSpear(level, sequence);
    }

    private List<RBMKColumnBlockEntity> collectConnectedOperationalColumns(Level level) {
        List<RBMKColumnBlockEntity> columns = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        open.add(worldPosition.immutable());

        while (!open.isEmpty()) {
            BlockPos cursor = open.removeFirst();
            RBMKColumnBlockEntity column = resolveOperationalColumn(level, cursor);
            if (column == null) {
                continue;
            }
            BlockPos core = column.getBlockPos().immutable();
            if (!visited.add(core)) {
                continue;
            }
            columns.add(column);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                open.add(core.relative(direction));
            }
        }

        return columns;
    }

    private MeltdownFluidNetworks collectMeltdownFluidNetworks(List<RBMKColumnBlockEntity> columns) {
        Set<HbmFluidNode> pipeNodes = new LinkedHashSet<>();
        Set<BlockEntity> receivers = new LinkedHashSet<>();
        for (RBMKColumnBlockEntity column : columns) {
            if (column == null) {
                continue;
            }
            for (FluidType type : column.getFluidNodeTypes()) {
                HbmFluidNet net = column.getFluidNet(type);
                if (net == null || !net.isValid()) {
                    continue;
                }
                pipeNodes.addAll(net.getLinks());
                for (HbmFluidReceiver receiver : net.getSubscribedReceivers()) {
                    if (receiver instanceof BlockEntity receiverBlockEntity) {
                        receivers.add(receiverBlockEntity);
                    }
                }
            }
        }
        return new MeltdownFluidNetworks(pipeNodes, receivers);
    }

    @Nullable
    private static RBMKColumnBlockEntity columnAt(List<RBMKColumnBlockEntity> columns, BlockPos pos) {
        for (RBMKColumnBlockEntity column : columns) {
            if (column.getBlockPos().equals(pos)) {
                return column;
            }
        }
        return null;
    }

    private RBMKMeltdownSequencePlanner.ColumnRef meltdownColumnRef() {
        return new RBMKMeltdownSequencePlanner.ColumnRef(
                worldPosition.immutable(),
                lifecycleColumnKind(kind()),
                hasCoriumFuelRod());
    }

    private void executeColumnMeltdown(ServerLevel level, int reduce) {
        RBMKColumnBlock.Kind columnKind = kind();
        boolean randomExtraReduce = level.getRandom().nextInt(3) == 0;
        removeNeutronNodesForColumn(level);
        removeFluidNode();

        if (columnKind.rod()) {
            String legacyRodId = fuelRod.getItem() instanceof RBMKFuelRodItem item ? item.getLegacyRodId() : "";
            RBMKFuelRodColumnPlanner.RodMeltdownPlan plan = RBMKFuelRodColumnPlanner.planRodMeltdown(
                    worldPosition,
                    legacyRodId,
                    legacyColumnHeight(),
                    reduce,
                    randomExtraReduce,
                    columnKind.moderated(),
                    lidType() == RBMKColumnBlock.LidType.STANDARD);
            if (plan.clearFuelSlot()) {
                fuelRod = ItemStack.EMPTY;
                rodFluxState.clearRodTick();
            }
            spawnDebris(level, plan.debris());
            if (plan.hadFuelRod()) {
                applyCoriumLayers(level, plan.coriumLayers());
            } else if (plan.standardMelt() != null) {
                applyStandardMelt(level, plan.standardMelt());
            }
            setChanged();
            return;
        }

        RBMKColumnLifecyclePlanner.ColumnMeltPlan plan = RBMKColumnLifecyclePlanner.planColumnMelt(
                lifecycleColumnKind(columnKind),
                worldPosition,
                legacyColumnHeight(),
                reduce,
                randomExtraReduce,
                lifecycleLidType(),
                columnKind.moderated());
        spawnLifecycleDebris(level, plan.debris());
        applyStandardMelt(level, plan.standardMelt());
        setChanged();
    }

    private void spawnDebris(ServerLevel level, List<RBMKFuelRodColumnPlanner.DebrisRange> ranges) {
        for (RBMKFuelRodColumnPlanner.DebrisRange range : ranges) {
            spawnDebrisRange(level, RBMKDebrisType.valueOf(range.type().name()), range.minCount(), range.maxCount());
        }
    }

    private void spawnLifecycleDebris(ServerLevel level, List<RBMKColumnLifecyclePlanner.DebrisRange> ranges) {
        for (RBMKColumnLifecyclePlanner.DebrisRange range : ranges) {
            spawnDebrisRange(level, RBMKDebrisType.valueOf(range.type().name()), range.minCount(), range.maxCount());
        }
    }

    private void spawnDebrisRange(ServerLevel level, RBMKDebrisType type, int minCount, int maxCount) {
        int min = Math.max(0, minCount);
        int max = Math.max(min, maxCount);
        int count = min == max ? min : min + level.getRandom().nextInt(max - min + 1);
        for (int i = 0; i < count; i++) {
            spawnDebris(level, type);
        }
    }

    private void spawnDebris(ServerLevel level, RBMKDebrisType type) {
        RBMKDebrisEntity debris = new RBMKDebrisEntity(level,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 4.0D,
                worldPosition.getZ() + 0.5D,
                type);
        double motionX = level.getRandom().nextGaussian() * 0.25D;
        double motionY = 0.25D + level.getRandom().nextDouble() * 1.25D;
        double motionZ = level.getRandom().nextGaussian() * 0.25D;
        if (type == RBMKDebrisType.LID) {
            motionX *= 0.5D;
            motionY += 0.5D;
            motionZ *= 0.5D;
        }
        debris.setDeltaMovement(motionX, motionY, motionZ);
        level.addFreshEntity(debris);
    }

    private void applyCoriumLayers(ServerLevel level, List<RBMKFuelRodColumnPlanner.CoriumLayerMutation> layers) {
        LegacyCoriumFiniteBlock corium = (LegacyCoriumFiniteBlock) ModBlocks.CORIUM_BLOCK.get();
        for (RBMKFuelRodColumnPlanner.CoriumLayerMutation layer : layers) {
            level.setBlock(layer.pos(), corium.legacyState(layer.legacyMeta()), Block.UPDATE_ALL);
        }
    }

    private void applyStandardMelt(ServerLevel level, RBMKMeltPlanner.StandardMeltPlan plan) {
        for (RBMKMeltPlanner.LayerMutation layer : plan.layers()) {
            level.setBlock(layer.pos(), meltLayerState(layer.state()), Block.UPDATE_ALL);
        }
    }

    private static BlockState meltLayerState(RBMKMeltPlanner.MeltLayerState state) {
        return switch (state) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case PRIBRIS -> ModBlocks.PRIBRIS.get().defaultBlockState();
            case PRIBRIS_BURNING -> ModBlocks.PRIBRIS_BURNING.get().defaultBlockState();
        };
    }

    private void executePostCoriumConversions(ServerLevel level,
            List<RBMKMeltdownSequencePlanner.PribrisConversionCandidate> candidates) {
        for (RBMKMeltdownSequencePlanner.PribrisConversionCandidate candidate : candidates) {
            if (candidate.randomDenominator() <= 0
                    || level.getRandom().nextInt(candidate.randomDenominator()) != 0) {
                continue;
            }
            BlockState state = level.getBlockState(candidate.pos());
            if (!state.is(ModBlocks.PRIBRIS.get()) && !state.is(ModBlocks.PRIBRIS_BURNING.get())) {
                continue;
            }
            BlockState target = candidate.target() == RBMKMeltdownSequencePlanner.PribrisConversionTarget.DIGAMMA
                    ? ModBlocks.PRIBRIS_DIGAMMA.get().defaultBlockState()
                    : ModBlocks.PRIBRIS_RADIATING.get().defaultBlockState();
            level.setBlock(candidate.pos(), target, Block.UPDATE_ALL);
        }
    }

    private void executeMeltdownOverpressure(ServerLevel level,
            RBMKMeltdownSequencePlanner.OverpressurePlan plan,
            MeltdownFluidNetworks networks) {
        if (plan == null || !plan.enabled() || networks == null) {
            return;
        }
        HbmFluidOverpressure.damagePipeNodes(level, networks.pipeNodes(), plan.pipeBlocksToBreak());
        HbmFluidOverpressure.damageReceivers(networks.receivers());
    }

    private void playMeltdownEffects(ServerLevel level, RBMKMeltdownSequencePlanner.MeltdownSequencePlan sequence) {
        RBMKMeltdownSequencePlanner.EffectPlan effect = sequence.mushroomEffect();
        BlockPos effectOrigin = effect.origin();
        CompoundTag data = new CompoundTag();
        data.putString("type", effect.particleType());
        data.putFloat("scale", effect.scale());
        ParticleUtil.spawnAuxThreaded(level,
                effectOrigin.getX() + 0.5D,
                effectOrigin.getY(),
                effectOrigin.getZ() + 0.5D,
                data,
                effect.range());

        RBMKMeltdownSequencePlanner.SoundPlan sound = sequence.explosionSound();
        BlockPos soundOrigin = sound.origin();
        LegacySoundPlayer.playSoundEffect(level,
                soundOrigin.getX() + 0.5D,
                soundOrigin.getY(),
                soundOrigin.getZ() + 0.5D,
                sound.sound(),
                50.0F,
                1.0F);
    }

    private void awardMeltdownAchievement(ServerLevel level,
            RBMKMeltdownSequencePlanner.MeltdownSequencePlan sequence) {
        int radius = Math.max(0, sequence.achievementRadius());
        AABB area = new AABB(
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D).inflate(radius);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            AchievementHandler.award(player, AchievementHandler.RBMK_BOOM);
        }
    }

    private void spawnDigammaSpear(ServerLevel level,
            RBMKMeltdownSequencePlanner.MeltdownSequencePlan sequence) {
        if (!sequence.spawnDigammaSpear() || sequence.bounds() == RBMKMeltPlanner.MeltdownBounds.EMPTY) {
            return;
        }
        DigammaSpearEntity spear = new DigammaSpearEntity(level);
        spear.setPos(sequence.bounds().centerX() + 0.5D,
                worldPosition.getY() + 100.0D,
                sequence.bounds().centerZ() + 0.5D);
        level.addFreshEntity(spear);
    }

    private boolean hasCoriumFuelRod() {
        return kind().rod() && fuelRod.getItem() instanceof RBMKFuelRodItem;
    }

    private boolean hasDigammaFuelRod() {
        return fuelRod.getItem() instanceof RBMKFuelRodItem item
                && "rbmk_fuel_drx".equals(item.getLegacyRodId());
    }

    private RBMKColumnBlock.LidType lidType() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof RBMKColumnBlock && state.hasProperty(RBMKColumnBlock.LID)
                ? state.getValue(RBMKColumnBlock.LID)
                : RBMKColumnBlock.LidType.NONE;
    }

    private RBMKColumnLifecyclePlanner.LidType lifecycleLidType() {
        return switch (lidType()) {
            case STANDARD -> RBMKColumnLifecyclePlanner.LidType.STANDARD;
            case GLASS -> RBMKColumnLifecyclePlanner.LidType.GLASS;
            case NONE -> RBMKColumnLifecyclePlanner.LidType.NONE;
        };
    }

    private static RBMKColumnLifecyclePlanner.ColumnKind lifecycleColumnKind(RBMKColumnBlock.Kind kind) {
        return switch (kind) {
            case BLANK -> RBMKColumnLifecyclePlanner.ColumnKind.BLANK;
            case MODERATOR -> RBMKColumnLifecyclePlanner.ColumnKind.MODERATOR;
            case REFLECTOR -> RBMKColumnLifecyclePlanner.ColumnKind.REFLECTOR;
            case ABSORBER -> RBMKColumnLifecyclePlanner.ColumnKind.ABSORBER;
            case ROD, ROD_MOD -> RBMKColumnLifecyclePlanner.ColumnKind.FUEL;
            case ROD_REASIM, ROD_REASIM_MOD -> RBMKColumnLifecyclePlanner.ColumnKind.FUEL_SIM;
            case BOILER -> RBMKColumnLifecyclePlanner.ColumnKind.BOILER;
            case HEATER -> RBMKColumnLifecyclePlanner.ColumnKind.HEATEX;
            case COOLER -> RBMKColumnLifecyclePlanner.ColumnKind.COOLER;
            case OUTGASSER -> RBMKColumnLifecyclePlanner.ColumnKind.OUTGASSER;
            case STORAGE -> RBMKColumnLifecyclePlanner.ColumnKind.STORAGE;
            case CONTROL, CONTROL_MOD, CONTROL_REASIM -> RBMKColumnLifecyclePlanner.ColumnKind.CONTROL;
            case CONTROL_AUTO, CONTROL_REASIM_AUTO -> RBMKColumnLifecyclePlanner.ColumnKind.CONTROL_AUTO;
        };
    }

    private static int legacyColumnHeight() {
        return RBMKStructureDimensions.columnHeightAboveCore() + 1;
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
            removeNeutronNodesForColumn(level);
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null) {
            removeNeutronNodesForColumn(level);
        }
        super.onChunkUnloaded();
    }

    private void removeNeutronNodesForColumn(Level level) {
        int heightAbove = Math.max(RBMKStructureDimensions.columnHeightAboveCore(),
                LEGACY_MAX_COLUMN_HEIGHT_ABOVE_CORE);
        for (int y = 0; y <= heightAbove; y++) {
            NeutronNodeWorld.removeNode(level, worldPosition.above(y));
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (level != null && getBlockState().getBlock() instanceof RBMKColumnBlock column) {
            LegacyMultiblockLayout layout = column.getMultiblockLayout(getBlockState(), level, worldPosition);
            if (layout != null) {
                return layout.renderBoundingBox(worldPosition, 0.25D);
            }
        }
        int heightAbove = RBMKStructureDimensions.columnHeightAboveCore();
        return new AABB(worldPosition, worldPosition.offset(1, heightAbove + 1, 1)).inflate(0.25D);
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
            tag.put(TAG_HEATER_ITEMS, HbmItemStackUtil.saveSlottedItems(heaterItems, TAG_STORAGE_SLOT));
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
        loadHeaterItems(tag);
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

    private void loadHeaterItems(CompoundTag tag) {
        for (int slot = 0; slot < heaterItems.getSlots(); slot++) {
            heaterItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        if (tag.contains(TAG_HEATER_ITEMS)) {
            HbmItemStackUtil.loadSlottedItems(tag, TAG_HEATER_ITEMS, TAG_STORAGE_SLOT, heaterItems);
            return;
        }
        if (kind() == RBMKColumnBlock.Kind.HEATER && tag.contains(HbmItemStackUtil.LEGACY_ITEMS_TAG)) {
            NonNullList<ItemStack> legacyItems = HbmItemStackUtil.loadLegacyOrForgeItems(tag, heaterItems.getSlots());
            for (int slot = 0; slot < Math.min(heaterItems.getSlots(), legacyItems.size()); slot++) {
                heaterItems.setStackInSlot(slot, legacyItems.get(slot));
            }
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        if (!hasOperationalLayout()) {
            return List.of();
        }
        return switch (kind()) {
            case BOILER -> List.of(boilerFeedTank);
            case HEATER -> List.of(heaterFeedTank);
            case COOLER -> List.of(coolerColdTank);
            default -> List.of();
        };
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        if (!hasOperationalLayout()) {
            return List.of();
        }
        return switch (kind()) {
            case BOILER -> List.of(boilerSteamTank);
            case HEATER -> List.of(heaterOutputTank);
            case COOLER -> List.of(coolerWarmTank);
            case OUTGASSER -> List.of(outgasserGasTank);
            default -> List.of();
        };
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return hasCompleteLayout() && super.canConnectFluid(type, side);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (!hasCompleteLayout()) {
            return amount;
        }
        prepareInputTankFor(type);
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        if (!hasCompleteLayout()) {
            return;
        }
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
        int topPortOffset = RBMKStructureDimensions.neutronScanSegments();
        BlockPos loaderBelow = worldPosition.below();
        BlockPos loaderTwoBelow = worldPosition.below(2);
        if (level != null && level.getBlockState(loaderBelow).is(ModBlocks.RBMK_LOADER.get())) {
            return loaderOutputPorts(-1, topPortOffset);
        }
        if (level != null && level.getBlockState(loaderTwoBelow).is(ModBlocks.RBMK_LOADER.get())) {
            return loaderOutputPorts(-2, topPortOffset);
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER) {
            return List.of(
                    FluidPort.of(0, topPortOffset, 0, Direction.UP),
                    FluidPort.of(0, -1, 0, Direction.DOWN));
        }
        if (!getSendingTanks().isEmpty()) {
            return List.of(FluidPort.of(0, topPortOffset, 0, Direction.UP));
        }
        return List.of();
    }

    private static List<FluidPort> loaderOutputPorts(int loaderYOffset, int topPortOffset) {
        return List.of(
                FluidPort.of(0, topPortOffset, 0, Direction.UP),
                FluidPort.of(1, loaderYOffset, 0, Direction.EAST),
                FluidPort.of(-1, loaderYOffset, 0, Direction.WEST),
                FluidPort.of(0, loaderYOffset, 1, Direction.SOUTH),
                FluidPort.of(0, loaderYOffset, -1, Direction.NORTH),
                FluidPort.of(0, loaderYOffset - 1, 0, Direction.DOWN));
    }

    private List<NeighborThermalState> horizontalNeighborThermalStates(Level level, BlockPos pos) {
        List<NeighborThermalState> neighbors = new ArrayList<>();
        for (Direction direction : RBMKBaseRuntimePlanner.neighborDirections()) {
            RBMKColumnBlockEntity column = resolveOperationalColumn(level, pos.relative(direction));
            if (column == null || column.getBlockPos().equals(pos)) {
                continue;
            }
            neighbors.add(new NeighborThermalState(column, column.thermalState()));
        }
        return neighbors;
    }

    private List<NeighborThermalState> cachedHorizontalNeighborThermalStates(Level level, BlockPos pos) {
        ensureThermalNeighborCacheSize();
        List<NeighborThermalState> neighbors = new ArrayList<>();
        List<Direction> directions = RBMKBaseRuntimePlanner.neighborDirections();
        for (int i = 0; i < directions.size(); i++) {
            BlockPos cachedPos = thermalNeighborCache.get(i);
            RBMKColumnBlockEntity column = cachedPos == null ? null : resolveCachedThermalNeighbor(level, cachedPos);
            if (column == null) {
                column = resolveOperationalColumn(level, pos.relative(directions.get(i)));
                thermalNeighborCache.set(i, column == null ? null : column.getBlockPos());
            }
            if (column != null && !column.getBlockPos().equals(pos)) {
                neighbors.add(new NeighborThermalState(column, column.thermalState()));
            }
        }
        return neighbors;
    }

    @Nullable
    private static RBMKColumnBlockEntity resolveCachedThermalNeighbor(Level level, BlockPos cachedPos) {
        if (!(level.getBlockEntity(cachedPos) instanceof RBMKColumnBlockEntity column) || column.isRemoved()) {
            return null;
        }
        return column.hasOperationalLayout() ? column : null;
    }

    private void ensureThermalNeighborCacheSize() {
        int targetSize = RBMKBaseRuntimePlanner.neighborDirections().size();
        while (thermalNeighborCache.size() < targetSize) {
            thermalNeighborCache.add(null);
        }
        while (thermalNeighborCache.size() > targetSize) {
            thermalNeighborCache.remove(thermalNeighborCache.size() - 1);
        }
    }

    private List<RBMKColumnBlockEntity> nearbyCoolerColumns(Level level, BlockPos pos) {
        List<RBMKColumnBlockEntity> columns = new ArrayList<>();
        for (BlockPos scanPos : RBMKCoolerRuntime.scanPositions(pos)) {
            RBMKColumnBlockEntity column = resolveOperationalColumn(level, scanPos);
            if (column != null && !columns.contains(column)) {
                columns.add(column);
            }
        }
        return columns;
    }

    private void refreshCoolerNeighborCache(Level level, BlockPos pos) {
        coolerNeighborCache.clear();
        for (RBMKColumnBlockEntity column : nearbyCoolerColumns(level, pos)) {
            coolerNeighborCache.add(column.getBlockPos());
        }
    }

    private List<RBMKColumnBlockEntity> resolveCachedCoolerColumns(Level level) {
        List<RBMKColumnBlockEntity> columns = new ArrayList<>(coolerNeighborCache.size());
        for (BlockPos cachedPos : coolerNeighborCache) {
            columns.add(resolveOperationalColumn(level, cachedPos));
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

    private final class LayoutGuardedItemHandler implements IItemHandlerModifiable {
        private final IItemHandlerModifiable delegate;

        private LayoutGuardedItemHandler(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return hasOperationalLayout() ? delegate.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return hasOperationalLayout() ? delegate.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return hasOperationalLayout() ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return hasOperationalLayout() ? delegate.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasOperationalLayout() && delegate.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (hasOperationalLayout()) {
                delegate.setStackInSlot(slot, stack);
            }
        }
    }

    private final class LayoutGuardedMenuItemHandler implements IItemHandlerModifiable {
        private final IItemHandlerModifiable delegate;

        private LayoutGuardedMenuItemHandler(IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return hasOperationalLayout() ? delegate.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!hasOperationalLayout() || stack.isEmpty() || slot < 0 || slot >= delegate.getSlots()) {
                return stack;
            }
            ItemStack existing = delegate.getStackInSlot(slot);
            int limit = Math.min(delegate.getSlotLimit(slot), stack.getMaxStackSize());
            if (existing.isEmpty()) {
                int inserted = Math.min(stack.getCount(), limit);
                if (!simulate) {
                    ItemStack copy = stack.copy();
                    copy.setCount(inserted);
                    delegate.setStackInSlot(slot, copy);
                }
                ItemStack remainder = stack.copy();
                remainder.shrink(inserted);
                return remainder;
            }
            if (!ItemStack.isSameItemSameTags(existing, stack) || existing.getCount() >= limit) {
                return stack;
            }
            int inserted = Math.min(stack.getCount(), limit - existing.getCount());
            if (!simulate) {
                ItemStack copy = existing.copy();
                copy.grow(inserted);
                delegate.setStackInSlot(slot, copy);
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return hasOperationalLayout() ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return hasOperationalLayout() ? delegate.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasOperationalLayout() && slot >= 0 && slot < delegate.getSlots();
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (hasOperationalLayout() && slot >= 0 && slot < delegate.getSlots()) {
                delegate.setStackInSlot(slot, stack);
            }
        }
    }

    private final class RodMenuItemHandler implements IItemHandlerModifiable {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return hasOperationalLayout() && slot == 0 ? fuelRod : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!hasOperationalLayout() || slot != 0 || stack.isEmpty()) {
                return stack;
            }
            int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
            if (!fuelRod.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(fuelRod, stack) || fuelRod.getCount() >= limit) {
                    return stack;
                }
                int inserted = Math.min(stack.getCount(), limit - fuelRod.getCount());
                if (!simulate) {
                    fuelRod.grow(inserted);
                    setChanged();
                }
                ItemStack remainder = stack.copy();
                remainder.shrink(inserted);
                return remainder;
            }
            int inserted = Math.min(stack.getCount(), limit);
            if (!simulate) {
                fuelRod = stack.copy();
                fuelRod.setCount(inserted);
                if (fuelRod.getItem() instanceof RBMKFuelRodItem) {
                    rodFluxState.setHasRod(true);
                } else {
                    rodFluxState.clearRodTick();
                }
                setChanged();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!hasOperationalLayout() || slot != 0 || amount <= 0 || fuelRod.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int extracted = Math.min(amount, fuelRod.getCount());
            ItemStack result = fuelRod.copy();
            result.setCount(extracted);
            if (!simulate) {
                fuelRod.shrink(extracted);
                if (fuelRod.isEmpty()) {
                    fuelRod = ItemStack.EMPTY;
                    rodFluxState.clearRodTick();
                }
                setChanged();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? 64 : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasOperationalLayout() && slot == 0;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (!hasOperationalLayout() || slot != 0) {
                return;
            }
            fuelRod = stack.copy();
            if (fuelRod.isEmpty()) {
                rodFluxState.clearRodTick();
            } else if (fuelRod.getItem() instanceof RBMKFuelRodItem) {
                rodFluxState.setHasRod(true);
            } else {
                rodFluxState.clearRodTick();
            }
            setChanged();
        }
    }

    private final class StorageAutomationItemHandler implements IItemHandlerModifiable {
        @Override
        public int getSlots() {
            return storageItems.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return hasOperationalLayout() ? storageItems.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return hasOperationalLayout() ? storageItems.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return hasOperationalLayout() ? storageItems.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return hasOperationalLayout() ? storageItems.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasOperationalLayout() && storageItems.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (hasOperationalLayout()) {
                storageItems.setStackInSlot(slot, stack);
            }
        }
    }

    private final class OutgasserAutomationItemHandler implements IItemHandlerModifiable {
        @Override
        public int getSlots() {
            return outgasserItems.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return hasOperationalLayout() ? outgasserItems.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!hasOperationalLayout() || slot != 0) {
                return stack;
            }
            return outgasserItems.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!hasOperationalLayout() || slot != 1) {
                return ItemStack.EMPTY;
            }
            return outgasserItems.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return hasOperationalLayout() ? outgasserItems.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasOperationalLayout() && slot == 0 && outgasserItems.isItemValid(slot, stack);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (hasOperationalLayout()) {
                outgasserItems.setStackInSlot(slot, stack);
            }
        }
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

    private record MeltdownFluidNetworks(Set<HbmFluidNode> pipeNodes, Set<BlockEntity> receivers) {
        private MeltdownFluidNetworks {
            pipeNodes = pipeNodes == null ? Set.of() : Set.copyOf(pipeNodes);
            receivers = receivers == null ? Set.of() : Set.copyOf(receivers);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        rodItemHandler.invalidate();
        controlEnergyHandler.invalidate();
        storageItemHandler.invalidate();
        outgasserAutomationItemHandler.invalidate();
        heaterItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if ((capability == ForgeCapabilities.ITEM_HANDLER || capability == ForgeCapabilities.FLUID_HANDLER)
                && !hasCompleteLayout()) {
            return LazyOptional.empty();
        }
        if (capability == ForgeCapabilities.ENERGY && isPoweredControlRod()) {
            return side == Direction.DOWN ? controlEnergyHandler.cast() : LazyOptional.empty();
        }
        if (kind().storage() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return storageItemHandler.cast();
        }
        if (kind().rod() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return rodItemHandler.cast();
        }
        if (kind() == RBMKColumnBlock.Kind.HEATER && capability == ForgeCapabilities.ITEM_HANDLER) {
            return heaterItemHandler.cast();
        }
        if (kind() == RBMKColumnBlock.Kind.OUTGASSER && capability == ForgeCapabilities.ITEM_HANDLER) {
            return outgasserAutomationItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private boolean hasCompleteLayout() {
        return level != null && MultiblockHelper.isOperationalCoreLayoutComplete(level, worldPosition);
    }

    public boolean hasOperationalLayout() {
        return hasCompleteLayout();
    }

    @Nullable
    public static RBMKColumnBlockEntity resolveOperationalColumn(Level level, @Nullable BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null || !MultiblockHelper.ensureOperationalCoreLayoutComplete(level, core.pos())) {
            return null;
        }
        return level.getBlockEntity(core.pos()) instanceof RBMKColumnBlockEntity column ? column : null;
    }

    public static class ReaSim extends RBMKColumnBlockEntity implements RBMKReaSimRodColumn {
        public ReaSim(BlockPos pos, BlockState state) {
            super(pos, state);
        }
    }
}
