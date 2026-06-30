package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.ContainerFluidTrait;
import com.hbm.ntm.item.PistonSetItem;
import com.hbm.ntm.menu.CombustionEngineMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombustionEngineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmLegacyButtonReceiver, MenuProvider, RORValueProvider, RORInteractive {
    public static final int SLOT_FLUID_INPUT = 0;
    public static final int SLOT_FLUID_OUTPUT = 1;
    public static final int SLOT_PISTON = 2;
    public static final int SLOT_ENERGY_OUTPUT = 3;
    public static final int SLOT_IDENTIFIER = 4;
    public static final int CONTROL_TOGGLE = 0;
    public static final int CONTROL_THROTTLE = 1;
    private static final String TAG_MODERN_INVENTORY = "Inventory";
    private static final String TAG_LEGACY_TANK = "tank";
    private static final String TAG_LEGACY_POWER = "power";

    private static final long MAX_POWER = 2_500_000L;
    private static final int TANK_CAPACITY = 24_000;
    private final HbmFluidTank tank;
    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(50);
    private final RORDispatcher ror;
    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FLUID_INPUT, SLOT_PISTON, SLOT_ENERGY_OUTPUT, SLOT_IDENTIFIER -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler = LazyOptional.of(() -> EmptyExternalItemHandler.INSTANCE);

    private boolean on;
    private boolean wasOn;
    private int throttle;
    private int tenth;
    private int playersUsing;
    private float doorAngle;
    private float prevDoorAngle;
    private int lastFuelUsedTenths;
    private long lastPowerProduced;
    private Object audioLoop;

    public CombustionEngineBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                new HbmFluidTank(HbmFluids.DIESEL, TANK_CAPACITY));
    }

    private CombustionEngineBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank tank) {
        super(ModBlockEntities.COMBUSTION_ENGINE.get(), pos, state, energy, List.of(tank));
        this.tank = tank;
        this.tank.conform(new HbmFluidStack(HbmFluids.DIESEL, 0));
        this.ror = createRorDispatcher();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CombustionEngineBlockEntity engine) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, engine);
        boolean changed = false;
        long oldPower = engine.energy.getPower();
        int oldFill = engine.tank.getFill();
        boolean oldWasOn = engine.wasOn;

        changed |= engine.processFluidItemTransfers(engine.items,
                HbmFluidItemTransfer.loadTransfers(SLOT_FLUID_INPUT, SLOT_FLUID_OUTPUT, engine.tank));
        if (engine.setFluidTankTypeFromIdentifierSlot(engine.items, SLOT_IDENTIFIER, engine.tank)) {
            engine.tenth = 0;
            changed = true;
        }

        engine.wasOn = false;
        engine.lastFuelUsedTenths = 0;
        engine.lastPowerProduced = 0L;
        changed |= engine.burnFuel(level, pos);
        HbmEnergyUtil.chargeItemFromStorage(engine.items.getStackInSlot(SLOT_ENERGY_OUTPUT),
                engine.energy, engine.energy.getProviderSpeed());
        engine.tryProvideEnergyToPorts();
        engine.sendSmokeToPorts(level, pos);
        if (engine.energy.getPower() > MAX_POWER) {
            engine.energy.setPower(MAX_POWER);
        }
        if (engine.tank.getTankType() != HbmFluids.NONE) {
            engine.refreshTrackedReceiverFluidPortsReport(List.of(engine.tank), engine);
        }

        changed |= oldPower != engine.energy.getPower()
                || oldFill != engine.tank.getFill()
                || oldWasOn != engine.wasOn;
        engine.networkPackNT(50);
        if (changed) {
            engine.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CombustionEngineBlockEntity engine) {
        if (!level.isClientSide) {
            return;
        }
        engine.prevDoorAngle = engine.doorAngle;
        float swingSpeed = engine.doorAngle / 10.0F + 3.0F;
        engine.doorAngle += engine.playersUsing > 0 ? swingSpeed : -swingSpeed;
        engine.doorAngle = Mth.clamp(engine.doorAngle, 0.0F, 135.0F);
        engine.audioLoop = LegacyMachineAudioBridge.updateLoop(engine.audioLoop, engine,
                "hbm:block.igeneratorOperate", engine.wasOn, 10.0D, 20.0F);
    }

    private boolean burnFuel(Level level, BlockPos pos) {
        if (!on || throttle <= 0 || tank.isEmpty()) {
            return false;
        }
        ItemStack pistonStack = items.getStackInSlot(SLOT_PISTON);
        if (!(pistonStack.getItem() instanceof PistonSetItem pistonSet)) {
            return false;
        }
        CombustibleFluidTrait combustible = tank.getTankType().getTrait(CombustibleFluidTrait.class);
        if (combustible == null) {
            return false;
        }
        double efficiency = pistonSet.type().efficiency(gradeFor(combustible.getGrade()));
        if (efficiency <= 0.0D) {
            return false;
        }

        int fillTenths = tank.getFill() * 10 + tenth;
        int toBurnTenths = Math.min(fillTenths, throttle * 2);
        if (toBurnTenths <= 0) {
            return false;
        }
        long produced = Math.round(toBurnTenths * (combustible.getCombustionEnergyPerBucket() / 10_000.0D) * efficiency);
        long oldPower = energy.getPower();
        energy.setPower(Math.min(MAX_POWER, oldPower + produced));
        lastPowerProduced = Math.max(0L, energy.getPower() - oldPower);
        fillTenths -= toBurnTenths;
        tank.setFill(fillTenths / 10);
        tenth = fillTenths % 10;
        lastFuelUsedTenths = toBurnTenths;
        wasOn = true;
        if (level.getGameTime() % 5L == 0L) {
            pollution.polluteFluidRelease(level, pos, tank.getTankType(), FluidReleaseType.BURN,
                    toBurnTenths * 0.5F);
        }
        return true;
    }

    private void sendSmokeToPorts(Level level, BlockPos pos) {
        for (FluidPort port : combustionFluidPorts()) {
            BlockPos connectorPos = port.connectorPos(pos);
            pollution.sendSmoke(level, connectorPos.getX(), connectorPos.getY(), connectorPos.getZ(),
                    port.direction());
        }
    }

    private static PistonSetItem.FuelGrade gradeFor(CombustibleFluidTrait.FuelGrade grade) {
        return switch (grade) {
            case LOW -> PistonSetItem.FuelGrade.LOW;
            case MEDIUM -> PistonSetItem.FuelGrade.MEDIUM;
            case HIGH -> PistonSetItem.FuelGrade.HIGH;
            case AERO -> PistonSetItem.FuelGrade.AERO;
            case GAS -> PistonSetItem.FuelGrade.GAS;
        };
    }

    private RORDispatcher createRorDispatcher() {
        return RORDispatcher.builder()
                .value("state", () -> Integer.toString(on ? 1 : 0))
                .value("throttle", () -> Integer.toString(throttle))
                .value("power", () -> Long.toString(energy.getPower()))
                .value("fuel", () -> Integer.toString(tank.getFill()))
                .value("efficiency", () -> Integer.toString((int) Math.round(currentEfficiency() * 100.0D)))
                .function("setState", params -> {
                    if (params.length > 0) {
                        setStateFromRor(params[0]);
                    }
                    return null;
                }, "state")
                .function("setThrottle", params -> {
                    if (params.length > 0) {
                        setThrottleFromRor(params[0]);
                    }
                    return null;
                }, "throttle")
                .build();
    }

    private double currentEfficiency() {
        ItemStack pistonStack = items.getStackInSlot(SLOT_PISTON);
        CombustibleFluidTrait combustible = tank.getTankType().getTrait(CombustibleFluidTrait.class);
        if (!(pistonStack.getItem() instanceof PistonSetItem pistonSet) || combustible == null) {
            return 0.0D;
        }
        return pistonSet.type().efficiency(gradeFor(combustible.getGrade()));
    }

    private void setStateFromRor(String value) {
        try {
            on = Integer.parseInt(value) == 1;
            setChanged();
        } catch (NumberFormatException ignored) {
        }
    }

    private void setThrottleFromRor(String value) {
        try {
            setThrottle(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
        }
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isOn() {
        return on;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle) {
        this.throttle = Mth.clamp(throttle, 0, 30);
        setChanged();
    }

    public int getFuelUsedTenths() {
        return lastFuelUsedTenths;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    public float getDoorAngle(float partialTick) {
        return Mth.lerp(partialTick, prevDoorAngle, doorAngle);
    }

    public int getCanisterColor() {
        ContainerFluidTrait container = tank.getTankType().getTrait(ContainerFluidTrait.class);
        return container != null && container.hasCanister() ? container.getCanisterColor() : 0xFFFFFF;
    }

    public void toggleOn() {
        on = !on;
        setChanged();
    }

    @Override
    public String[] getFunctionInfo() {
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        return ror.runFunction(name, params);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, wasOn);
        data.putInt(CompatEnergyControl.I_THROTTLE, throttle);
        data.putInt(CompatEnergyControl.I_STATE, on ? 1 : 0);
        data.putDouble(CompatEnergyControl.D_EFFICIENCY, currentEfficiency());
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, lastFuelUsedTenths / 10.0D);
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, lastPowerProduced);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_COMBUSTION_FUEL, tank);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return tank.getSpace();
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return combustionFluidPorts();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                EnergyPort.of(facing.getStepX() + side.getStepX(), 0,
                        facing.getStepZ() + side.getStepZ(), facing),
                EnergyPort.of(facing.getStepX() - side.getStepX(), 0,
                        facing.getStepZ() - side.getStepZ(), facing),
                EnergyPort.of(-facing.getStepX() * 2 + side.getStepX(), 0,
                        -facing.getStepZ() * 2 + side.getStepZ(), facing.getOpposite()),
                EnergyPort.of(-facing.getStepX() * 2 - side.getStepX(), 0,
                        -facing.getStepZ() * 2 - side.getStepZ(), facing.getOpposite()));
    }

    private List<FluidPort> combustionFluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX() + side.getStepX(), 0,
                        facing.getStepZ() + side.getStepZ(), facing),
                FluidPort.of(facing.getStepX() - side.getStepX(), 0,
                        facing.getStepZ() - side.getStepZ(), facing),
                FluidPort.of(-facing.getStepX() * 2 + side.getStepX(), 0,
                        -facing.getStepZ() * 2 + side.getStepZ(), facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 2 - side.getStepX(), 0,
                        -facing.getStepZ() * 2 - side.getStepZ(), facing.getOpposite()));
    }

    private Direction facing() {
        return getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == Direction.DOWN ? HbmFluidSideMode.NONE : HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == Direction.DOWN ? HbmEnergySideMode.NONE : HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return false;
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, getFluidIdsToCopy());
        tag.putBoolean("isOn", on);
        tag.putInt("burnRate", throttle);
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        if (tag == null) {
            return false;
        }
        boolean changed = false;
        java.util.OptionalInt id = HbmFluidCopiable.copiedFluidIdAt(tag, index);
        if (id.isPresent()) {
            tank.setTankType(HbmFluids.fromId(id.getAsInt()));
            tenth = 0;
            changed = true;
        }
        if (tag.contains("isOn")) {
            on = tag.getBoolean("isOn");
            changed = true;
        }
        if (tag.contains("burnRate")) {
            setThrottle(tag.getInt("burnRate"));
            changed = true;
        }
        if (changed) {
            onFluidContentsChanged();
        }
        return changed;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.combustionEngine", "Industrial Combustion Engine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        playersUsing++;
        setChanged();
        syncToClient();
        return new CombustionEngineMenu(containerId, inventory, this);
    }

    public void closeMenu(Player player) {
        playersUsing = Math.max(0, playersUsing - 1);
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_TOGGLE || id == CONTROL_THROTTLE;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_TOGGLE) {
            toggleOn();
        } else if (id == CONTROL_THROTTLE) {
            setThrottle(value);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_MODERN_INVENTORY, items);
        tank.writeToNbt(tag, TAG_LEGACY_TANK);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putBoolean("isOn", on);
        tag.putBoolean("wasOn", wasOn);
        tag.putInt("setting", throttle);
        tag.putInt("tenth", tenth);
        tag.putInt("playersUsing", playersUsing);
        tag.putFloat("doorAngle", doorAngle);
        tag.putFloat("prevDoorAngle", prevDoorAngle);
        tag.putInt("fuelUsedTenths", lastFuelUsedTenths);
        tag.putLong("lastPowerProduced", lastPowerProduced);
        pollution.writeLegacyNbt(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItems(tag);
        if (hasLegacyTankTag(tag, TAG_LEGACY_TANK)) {
            tank.readFromNbt(tag, TAG_LEGACY_TANK);
        }
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        on = tag.getBoolean("isOn");
        wasOn = tag.getBoolean("wasOn");
        throttle = Mth.clamp(tag.getInt("setting"), 0, 30);
        tenth = Mth.clamp(tag.getInt("tenth"), 0, 9);
        playersUsing = Math.max(0, tag.getInt("playersUsing"));
        doorAngle = tag.getFloat("doorAngle");
        prevDoorAngle = tag.getFloat("prevDoorAngle");
        lastFuelUsedTenths = tag.getInt("fuelUsedTenths");
        lastPowerProduced = tag.getLong("lastPowerProduced");
        pollution.readLegacyNbt(tag);
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
        externalItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private void loadItems(CompoundTag tag) {
        if (tag.contains(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, Tag.TAG_LIST)
                || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_INVENTORY, items);
    }

    private static boolean hasLegacyTankTag(CompoundTag tag, String key) {
        return tag.contains(key)
                || tag.contains(key + "_type")
                || tag.contains(key + "_type_id");
    }

    private enum EmptyExternalItemHandler implements IItemHandler {
        INSTANCE;

        @Override
        public int getSlots() {
            return 0;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
