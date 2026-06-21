package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidPortSubscriptionTracker;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.menu.TurbineGasMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TurbineGasBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyButtonReceiver, HbmFluidCopiable,
        RORInteractive {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_IDENTIFIER = 1;
    public static final int SLOT_COUNT = 2;
    public static final int CONTROL_STATE = 0;
    public static final int CONTROL_AUTO = 1;
    public static final int CONTROL_SLIDER = 2;

    public static final long MAX_POWER = 1_000_000L;
    private static final int FUEL_CAPACITY = 100_000;
    private static final int LUBE_CAPACITY = 16_000;
    private static final int WATER_CAPACITY = 16_000;
    private static final int STEAM_CAPACITY = 160_000;
    private static final int RPM_IDLE = 10;
    private static final int TEMP_IDLE = 300;
    private static final Map<FluidType, Double> FUEL_MAX_CONSUMPTION = Map.of(
            HbmFluids.GAS, 50.0D,
            HbmFluids.SYNGAS, 10.0D,
            HbmFluids.OXYHYDROGEN, 100.0D,
            HbmFluids.REFORMGAS, 5.0D);
    private static final String TAG_ITEMS = "items";

    private final HbmFluidTank fuelTank;
    private final HbmFluidTank lubricantTank;
    private final HbmFluidTank waterTank;
    private final HbmFluidTank steamTank;
    private final HbmFluidPortSubscriptionTracker fuelLubePortSubscriptions =
            new HbmFluidPortSubscriptionTracker();
    private final HbmFluidPortSubscriptionTracker waterPortSubscriptions =
            new HbmFluidPortSubscriptionTracker();
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> true;
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem identifier
                        && isGasFuel(identifier.getIdentifiedFluid(level, worldPosition, stack));
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int rpm;
    private int temp = 20;
    private int sliderPos;
    private int throttle;
    private boolean autoMode;
    private int state;
    private int counter;
    private int instantPowerOutput;
    private double waterToBoil;
    private double fuelToConsume;
    private int rpmLast;
    private int tempLast;
    private Object audioLoop;

    public TurbineGasBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                new HbmFluidTank(HbmFluids.GAS, FUEL_CAPACITY),
                new HbmFluidTank(HbmFluids.LUBRICANT, LUBE_CAPACITY),
                new HbmFluidTank(HbmFluids.WATER, WATER_CAPACITY),
                new HbmFluidTank(HbmFluids.HOTSTEAM, STEAM_CAPACITY));
    }

    private TurbineGasBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank fuelTank,
            HbmFluidTank lubricantTank, HbmFluidTank waterTank, HbmFluidTank steamTank) {
        super(ModBlockEntities.TURBINE_GAS.get(), pos, state, energy,
                List.of(fuelTank, lubricantTank, waterTank, steamTank));
        this.fuelTank = fuelTank;
        this.lubricantTank = lubricantTank;
        this.waterTank = waterTank;
        this.steamTank = steamTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TurbineGasBlockEntity turbine) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, turbine);
        long oldPower = turbine.energy.getPower();
        int oldFuel = turbine.fuelTank.getFill();
        int oldLube = turbine.lubricantTank.getFill();
        int oldWater = turbine.waterTank.getFill();
        int oldSteam = turbine.steamTank.getFill();
        int oldRpm = turbine.rpm;
        int oldTemp = turbine.temp;
        int oldState = turbine.state;
        int oldSlider = turbine.sliderPos;
        boolean oldAuto = turbine.autoMode;

        turbine.waterToBoil = 0.0D;
        turbine.throttle = turbine.sliderPos * 100 / 60;
        turbine.applyIdentifierSlot();
        if (turbine.autoMode) {
            turbine.tickAutoSlider();
        }

        switch (turbine.state) {
            case 0 -> turbine.shutdown(level, pos);
            case -1 -> {
                turbine.stopIfNotReady();
                turbine.startup(level, pos);
            }
            case 1 -> {
                turbine.stopIfNotReady();
                turbine.runOnline(level, pos);
            }
            default -> {
            }
        }

        HbmEnergyUtil.chargeItemFromStorage(turbine.items.getStackInSlot(SLOT_BATTERY),
                turbine.energy, turbine.energy.getProviderSpeed());
        turbine.tryProvideEnergyToPorts();
        turbine.refreshLegacyPortSubscriptions(level, pos);
        turbine.tryProvideSteamToLegacyPorts(level, pos);
        turbine.energy.setPower(Math.min(MAX_POWER, turbine.energy.getPower()));

        boolean changed = oldPower != turbine.energy.getPower()
                || oldFuel != turbine.fuelTank.getFill()
                || oldLube != turbine.lubricantTank.getFill()
                || oldWater != turbine.waterTank.getFill()
                || oldSteam != turbine.steamTank.getFill()
                || oldRpm != turbine.rpm
                || oldTemp != turbine.temp
                || oldState != turbine.state
                || oldSlider != turbine.sliderPos
                || oldAuto != turbine.autoMode;
        turbine.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            turbine.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TurbineGasBlockEntity turbine) {
        if (!level.isClientSide) {
            return;
        }
        float pitch = (float) (0.55D + 0.1D * turbine.rpm / 10.0D);
        turbine.audioLoop = LegacyMachineAudioBridge.updateLoop(turbine.audioLoop, turbine,
                "hbm:block.turbinegasRunning", turbine.rpm >= 10 && turbine.state != -1, 1.0D, 20.0F,
                2.0F, pitch);
    }

    private void applyIdentifierSlot() {
        ItemStack stack = items.getStackInSlot(SLOT_IDENTIFIER);
        if (stack.getItem() instanceof IFluidIdentifierItem identifier) {
            FluidType type = identifier.getIdentifiedFluid(level, worldPosition, stack);
            if (isGasFuel(type)) {
                fuelTank.setTankType(type);
            }
        }
    }

    private void tickAutoSlider() {
        int target;
        int powerScaled = (int) (60L * energy.getPower() / MAX_POWER);
        if (fuelTank.getFill() * 10 > fuelTank.getMaxFill()) {
            target = 60 - powerScaled;
        } else {
            target = (int) (fuelTank.getFill() * 0.0001D * (60 - powerScaled));
        }
        if (target > sliderPos) {
            sliderPos++;
        } else if (target < sliderPos) {
            sliderPos--;
        }
        sliderPos = Mth.clamp(sliderPos, 0, 60);
    }

    private void stopIfNotReady() {
        if (fuelTank.getFill() == 0 || lubricantTank.getFill() == 0 || !hasAcceptableFuel()) {
            state = 0;
        }
    }

    private void startup(Level level, BlockPos pos) {
        counter++;
        if (counter <= 20) {
            rpm = 5 * counter;
        } else if (counter <= 40) {
            rpm = 100 - 5 * (counter - 20);
        } else if (counter > 50) {
            rpm = RPM_IDLE * (counter - 50) / 530;
            temp = TEMP_IDLE * (counter - 50) / 530;
        }

        if (counter == 50) {
            LegacySoundPlayer.playSoundEffect(level, pos.above(2), "hbm:block.turbinegasStartup",
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (counter == 580) {
            counter = 225;
            state = 1;
        }
    }

    private void shutdown(Level level, BlockPos pos) {
        autoMode = false;
        instantPowerOutput = 0;
        if (sliderPos > 0) {
            sliderPos--;
        }

        if (rpm <= 10 && counter > 0) {
            if (counter == 225) {
                LegacySoundPlayer.playSoundEffect(level, pos.above(2), "hbm:block.turbinegasShutdown",
                        SoundSource.BLOCKS, 1.0F, 1.0F);
                rpmLast = rpm;
                tempLast = temp;
            }
            counter--;
            rpm = rpmLast * counter / 225;
            temp = tempLast * counter / 225;
        } else if (rpm > 11) {
            counter = 42069;
            rpm--;
        } else if (rpm == 11) {
            counter = 225;
            rpm--;
        }
    }

    private void runOnline(Level level, BlockPos pos) {
        if ((int) (throttle * 0.9D) > rpm - RPM_IDLE) {
            if (level.getGameTime() % 5L == 0L) {
                rpm++;
            }
        } else if ((int) (throttle * 0.9D) < rpm - RPM_IDLE && level.getGameTime() % 2L == 0L) {
            rpm--;
        }

        int maxTemp = getFluidBurnTemp(fuelTank.getTankType());
        int targetTemp = throttle * 5 * (maxTemp - TEMP_IDLE) / 500;
        if (targetTemp > temp - TEMP_IDLE) {
            if (level.getGameTime() % 2L == 0L) {
                temp++;
            }
        } else if (targetTemp < temp - TEMP_IDLE && level.getGameTime() % 2L == 0L) {
            temp--;
        }

        double consumption = maxFuelConsumption(fuelTank.getTankType());
        if (level.getGameTime() % 20L == 0L && fuelTank.getTankType() != HbmFluids.OXYHYDROGEN) {
            PollutionManager.applyPollutionDelta(level, pos, PollutionType.SOOT,
                    com.hbm.handler.pollution.PollutionHandler.SOOT_PER_SECOND * 3.0F);
        }
        makePower(level, consumption, throttle);
    }

    private void makePower(Level level, double maxConsumption, int throttle) {
        double consumption = maxConsumption * 0.05D + maxConsumption * throttle / 100.0D;
        fuelToConsume += consumption;
        int fuelWhole = (int) Math.floor(fuelToConsume);
        fuelTank.drain(fuelWhole, false);
        fuelToConsume -= fuelWhole;

        if (level.getGameTime() % 10L == 0L) {
            lubricantTank.drain(1, false);
        }
        if (fuelTank.getFill() <= 0 || lubricantTank.getFill() <= 0) {
            state = 0;
        }

        long energyPerMb = 0L;
        CombustibleFluidTrait combustible = fuelTank.getTankType().getTrait(CombustibleFluidTrait.class);
        if (combustible != null) {
            energyPerMb = combustible.getCombustionEnergyPerBucket() / 1_000L;
        }

        int rpmEffective = rpm - RPM_IDLE;
        double targetOutput = maxConsumption * energyPerMb * rpmEffective / 90.0D;
        if (instantPowerOutput < targetOutput) {
            instantPowerOutput += level.random.nextDouble() * 0.005D * maxConsumption * energyPerMb;
            if (instantPowerOutput > targetOutput) {
                instantPowerOutput = (int) targetOutput;
            }
        } else if (instantPowerOutput > targetOutput) {
            instantPowerOutput -= level.random.nextDouble() * 0.011D * maxConsumption * energyPerMb;
            if (instantPowerOutput < targetOutput) {
                instantPowerOutput = (int) targetOutput;
            }
        }
        energy.setPower(energy.getPower() + instantPowerOutput);

        waterToBoil = maxConsumption * energyPerMb * (temp - TEMP_IDLE) / 220_000.0D;
        int heatCycles = (int) Math.floor(waterToBoil);
        int cycles = Math.min(heatCycles, Math.min(waterTank.getFill(), steamTank.getSpace() / 10));
        waterTank.drain(cycles, false);
        steamTank.setFill(steamTank.getFill() + cycles * 10);
    }

    private static int getFluidBurnTemp(FluidType type) {
        CombustibleFluidTrait combustible = type == null ? null : type.getTrait(CombustibleFluidTrait.class);
        double fuel = combustible == null ? 0.0D : combustible.getCombustionEnergyPerBucket();
        return (int) Math.floor(800.0D - Math.pow(Math.E, -fuel / 100_000.0D) * 300.0D);
    }

    public static boolean isGasFuel(FluidType type) {
        CombustibleFluidTrait combustible = type == null ? null : type.getTrait(CombustibleFluidTrait.class);
        return combustible != null && combustible.getGrade() == CombustibleFluidTrait.FuelGrade.GAS;
    }

    public static double maxFuelConsumption(FluidType type) {
        return FUEL_MAX_CONSUMPTION.getOrDefault(type, 5.0D);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private BlockPos relative(int forward, int side, int y) {
        return LegacyMultiblockOffsets.relative(facing(), forward, side, y);
    }

    private List<EnergyPort> energyPorts() {
        Direction facing = facing();
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        BlockPos offset = LegacyMultiblockOffsets.relative(facing, side, 0, -5, 1);
        return List.of(EnergyPort.of(offset.getX(), offset.getY(), offset.getZ(), side.getOpposite()));
    }

    private List<FluidPort> fluidPorts() {
        Direction facing = facing();
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        List<FluidPort> fuelLubePorts = fuelLubeFluidPorts(facing);
        List<FluidPort> waterPorts = waterFluidPorts(facing);
        List<FluidPort> steamPorts = steamFluidPorts(side);
        return List.of(
                fuelLubePorts.get(0),
                fuelLubePorts.get(1),
                waterPorts.get(0),
                waterPorts.get(1),
                steamPorts.get(0));
    }

    private List<FluidPort> fuelLubeFluidPorts(Direction facing) {
        return List.of(
                fluidPort(relative(-1, 1, 0), facing.getOpposite()),
                fluidPort(relative(1, 1, 0), facing));
    }

    private List<FluidPort> waterFluidPorts(Direction facing) {
        return List.of(
                fluidPort(relative(-1, -4, 0), facing.getOpposite()),
                fluidPort(relative(1, -4, 0), facing));
    }

    private List<FluidPort> steamFluidPorts(Direction side) {
        return List.of(fluidPort(relative(0, 4, 1), side.getOpposite()));
    }

    private static FluidPort fluidPort(BlockPos offset, Direction side) {
        return FluidPort.of(offset.getX(), offset.getY(), offset.getZ(), side);
    }

    private void refreshLegacyPortSubscriptions(Level level, BlockPos pos) {
        Direction facing = facing();
        fuelLubePortSubscriptions.refreshReceiverDetailed(level, pos, fuelLubeFluidPorts(facing),
                List.of(fuelTank, lubricantTank), this);
        waterPortSubscriptions.refreshReceiverDetailed(level, pos, waterFluidPorts(facing),
                List.of(waterTank), this);
    }

    private void tryProvideSteamToLegacyPorts(Level level, BlockPos pos) {
        if (steamTank.getTankType() == HbmFluids.NONE || steamTank.getFill() <= 0) {
            return;
        }
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing());
        HbmFluidUtil.tryProvideToPortsDetailedReport(level, pos, steamFluidPorts(side),
                steamTank.getTankType(), steamTank.getPressure(), this);
    }

    private void detachLegacyPortSubscriptions() {
        if (level == null || level.isClientSide) {
            return;
        }
        Direction facing = facing();
        fuelLubePortSubscriptions.detachAllDetailed(level, worldPosition, fuelLubeFluidPorts(facing), this, null);
        waterPortSubscriptions.detachAllDetailed(level, worldPosition, waterFluidPorts(facing), this, null);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    public HbmFluidTank getFuelTank() {
        return fuelTank;
    }

    public HbmFluidTank getLubricantTank() {
        return lubricantTank;
    }

    public HbmFluidTank getWaterTank() {
        return waterTank;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public int getRpm() {
        return rpm;
    }

    public int getTemperature() {
        return Math.max(20, temp);
    }

    public int getSliderPos() {
        return sliderPos;
    }

    public int getThrottle() {
        return throttle;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public int getState() {
        return state;
    }

    public int getCounter() {
        return counter;
    }

    public int getInstantPowerOutput() {
        return instantPowerOutput;
    }

    public double getWaterToBoil() {
        return waterToBoil;
    }

    public boolean hasAcceptableFuel() {
        return isGasFuel(fuelTank.getTankType());
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public void setRemoved() {
        detachLegacyPortSubscriptions();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        detachLegacyPortSubscriptions();
        super.onChunkUnloaded();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return energyPorts();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts();
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        Direction facing = facing();
        if (isGasFuel(type) || type == lubricantTank.getTankType()) {
            return fuelLubeFluidPorts(facing);
        }
        if (type == waterTank.getTankType()) {
            return waterFluidPorts(facing);
        }
        if (type == steamTank.getTankType()) {
            return steamFluidPorts(LegacyMultiblockOffsets.legacyUpSide(facing));
        }
        return List.of();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == fuelTank.getTankType()
                || type == lubricantTank.getTankType()
                || type == waterTank.getTankType();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == Direction.DOWN ? HbmFluidSideMode.NONE : HbmFluidSideMode.BOTH;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(fuelTank, lubricantTank, waterTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
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
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower()),
                LegacyLookOverlayLines.tank(true, fuelTank),
                LegacyLookOverlayLines.tank(true, lubricantTank),
                LegacyLookOverlayLines.tank(true, waterTank),
                LegacyLookOverlayLines.tank(false, steamTank)));
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.turbinegas", "Gas Turbine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new TurbineGasMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        if (player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) > 625.0D) {
            return false;
        }
        return switch (id) {
            case CONTROL_STATE -> value == -1 || value == 0;
            case CONTROL_AUTO -> state == 1 && (value == 0 || value == 1);
            case CONTROL_SLIDER -> state == 1 && value >= 0 && value <= 60;
            default -> false;
        };
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        switch (id) {
            case CONTROL_STATE -> {
                if (value == -1 && state == 0) {
                    state = -1;
                } else if (value == 0 && state == 1) {
                    state = 0;
                }
            }
            case CONTROL_AUTO -> autoMode = value != 0;
            case CONTROL_SLIDER -> {
                sliderPos = Mth.clamp(value, 0, 60);
                autoMode = false;
            }
            default -> {
            }
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, state == 1);
        data.putDouble(CompatEnergyControl.D_HEAT_C, getTemperature());
        data.putDouble(CompatEnergyControl.D_TURBINE_PERCENT, sliderPos * 100.0D / 60.0D);
        data.putInt(CompatEnergyControl.I_TURBINE_SPEED, rpm);
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, instantPowerOutput);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, waterToBoil);
        data.putDouble(CompatEnergyControl.D_OUTPUT_MB, waterToBoil * 10.0D);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_VALUE + "turbinepercent",
                PREFIX_VALUE + "turbinespeed",
                PREFIX_VALUE + "output",
                PREFIX_VALUE + "state",
                PREFIX_VALUE + "autoMode",
                PREFIX_VALUE + "temp",
                PREFIX_VALUE + "power",
                PREFIX_VALUE + "fuel",
                PREFIX_VALUE + "lubricant",
                PREFIX_VALUE + "water",
                PREFIX_VALUE + "steam",
                PREFIX_FUNCTION + "setAuto" + NAME_SEPARATOR + "auto",
                PREFIX_FUNCTION + "setThrottle" + NAME_SEPARATOR + "percent",
                PREFIX_FUNCTION + "setState" + NAME_SEPARATOR + "state"
        };
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((PREFIX_FUNCTION + "setAuto").equals(name) && params.length > 0) {
            autoMode = parseLooseInt(params[0]) == 1;
            setChanged();
            return null;
        }
        if ((PREFIX_FUNCTION + "setThrottle").equals(name) && params.length > 0) {
            sliderPos = Mth.clamp(parseLooseInt(params[0]), 0, 100) * 60 / 100;
            setChanged();
            return null;
        }
        if ((PREFIX_FUNCTION + "setState").equals(name) && params.length > 0) {
            int newState = parseLooseInt(params[0]);
            if (newState == 1 && state == 0) {
                state = -1;
            } else if (newState == 0 && state == 1) {
                state = 0;
            }
            setChanged();
            return null;
        }
        return null;
    }

    public String provideRORValue(String name) {
        if ((PREFIX_VALUE + "turbinepercent").equals(name)) return "" + (int) (sliderPos * 100.0D / 60.0D);
        if ((PREFIX_VALUE + "turbinespeed").equals(name)) return "" + rpm;
        if ((PREFIX_VALUE + "output").equals(name)) return "" + (instantPowerOutput * 20);
        if ((PREFIX_VALUE + "state").equals(name)) return "" + state;
        if ((PREFIX_VALUE + "autoMode").equals(name)) return "" + (autoMode ? 1 : 0);
        if ((PREFIX_VALUE + "temp").equals(name)) return "" + temp;
        if ((PREFIX_VALUE + "power").equals(name)) return "" + energy.getPower();
        if ((PREFIX_VALUE + "fuel").equals(name)) return "" + fuelTank.getFill();
        if ((PREFIX_VALUE + "lubricant").equals(name)) return "" + lubricantTank.getFill();
        if ((PREFIX_VALUE + "water").equals(name)) return "" + waterTank.getFill();
        if ((PREFIX_VALUE + "steam").equals(name)) return "" + steamTank.getFill();
        return null;
    }

    private static int parseLooseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        fuelTank.writeToNbt(tag, "gas");
        lubricantTank.writeToNbt(tag, "lube");
        waterTank.writeToNbt(tag, "water");
        steamTank.writeToNbt(tag, "densesteam");
        tag.putBoolean("automode", autoMode);
        tag.putLong("power", energy.getPower());
        if (state == 1) {
            tag.putInt("state", state);
            tag.putInt("rpm", rpm);
            tag.putInt("temperature", temp);
            tag.putInt("slidPos", sliderPos);
            tag.putInt("instPwr", instantPowerOutput);
            tag.putInt("counter", 225);
        } else {
            tag.putInt("state", 0);
            tag.putInt("rpm", 0);
            tag.putInt("temperature", 20);
            tag.putInt("slidPos", 0);
            tag.putInt("instpwr", 0);
            tag.putInt("counter", 0);
        }
        tag.putDouble("fuelToConsume", fuelToConsume);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        fuelTank.readFromNbt(tag, "gas");
        lubricantTank.readFromNbt(tag, "lube");
        waterTank.readFromNbt(tag, "water");
        steamTank.readFromNbt(tag, "densesteam");
        autoMode = tag.getBoolean("automode");
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        state = tag.getInt("state");
        rpm = tag.getInt("rpm");
        temp = tag.contains("temperature") ? tag.getInt("temperature") : 20;
        sliderPos = tag.getInt("slidPos");
        instantPowerOutput = tag.contains("instPwr") ? tag.getInt("instPwr") : tag.getInt("instpwr");
        counter = tag.getInt("counter");
        fuelToConsume = tag.getDouble("fuelToConsume");
        normalizeTankTypes();
    }

    private void normalizeTankTypes() {
        if (fuelTank.getTankType() == HbmFluids.NONE) {
            fuelTank.setTankType(HbmFluids.GAS);
        }
        if (lubricantTank.getTankType() == HbmFluids.NONE) {
            lubricantTank.setTankType(HbmFluids.LUBRICANT);
        }
        if (waterTank.getTankType() == HbmFluids.NONE) {
            waterTank.setTankType(HbmFluids.WATER);
        }
        if (steamTank.getTankType() == HbmFluids.NONE) {
            steamTank.setTankType(HbmFluids.HOTSTEAM);
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

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return switch (slot) {
                case SLOT_BATTERY, SLOT_IDENTIFIER -> items.insertItem(slot, stack, simulate);
                default -> stack;
            };
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT && items.isItemValid(slot, stack);
        }
    }
}
