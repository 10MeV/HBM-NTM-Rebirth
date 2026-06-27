package com.hbm.ntm.blockentity;

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
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.menu.DieselGeneratorMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DieselGeneratorBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmFluidCopiable, MenuProvider {
    public static final int SLOT_FLUID_INPUT = 0;
    public static final int SLOT_FLUID_OUTPUT = 1;
    public static final int SLOT_BATTERY = 2;
    public static final int SLOT_IDENTIFIER = 3;
    public static final int SLOT_IDENTIFIER_OUTPUT = 4;
    public static final int SLOT_COUNT = 5;
    public static final long MAX_POWER = 50_000L;
    public static final int TANK_CAPACITY = 16_000;
    private static final String TAG_LEGACY_ITEMS = "items";
    private static final String TAG_MODERN_INVENTORY = "Inventory";
    private static final Map<CombustibleFluidTrait.FuelGrade, Double> FUEL_EFFICIENCY =
            new EnumMap<>(CombustibleFluidTrait.FuelGrade.class);
    private static final List<EnergyPort> ALL_ENERGY_PORTS = Direction.stream()
            .map(direction -> EnergyPort.of(direction.getStepX(), direction.getStepY(), direction.getStepZ(), direction))
            .toList();
    private static final List<FluidPort> ALL_FLUID_PORTS = Direction.stream()
            .map(direction -> FluidPort.of(direction.getStepX(), direction.getStepY(), direction.getStepZ(), direction))
            .toList();

    static {
        FUEL_EFFICIENCY.put(CombustibleFluidTrait.FuelGrade.MEDIUM, 0.5D);
        FUEL_EFFICIENCY.put(CombustibleFluidTrait.FuelGrade.HIGH, 0.75D);
        FUEL_EFFICIENCY.put(CombustibleFluidTrait.FuelGrade.AERO, 0.1D);
    }

    private final HbmFluidTank tank;
    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(100);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FLUID_INPUT -> HbmFluidItemTransfer.getItemFluid(stack).amount() > 0;
                case SLOT_BATTERY -> stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
                case SLOT_IDENTIFIER -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> topItemHandler =
            LazyOptional.of(() -> new AccessibleItemHandler(items, new int[] {SLOT_FLUID_INPUT}, true));
    private final LazyOptional<IItemHandler> bottomItemHandler =
            LazyOptional.of(() -> new AccessibleItemHandler(items, new int[] {SLOT_FLUID_OUTPUT, SLOT_BATTERY}, false));
    private final LazyOptional<IItemHandler> sideItemHandler =
            LazyOptional.of(() -> new AccessibleItemHandler(items, new int[] {SLOT_BATTERY}, true));

    private boolean wasOn;
    private long powerCap = MAX_POWER;
    private long lastPowerProduced;
    private Object audioLoop;

    public DieselGeneratorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.DIESEL_GENERATOR.get(), pos, state);
    }

    private DieselGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                List.of(new HbmFluidTank(HbmFluids.DIESEL, TANK_CAPACITY)));
        this.tank = getAllTanks().get(0);
        this.tank.conform(new HbmFluidStack(HbmFluids.DIESEL, 0));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DieselGeneratorBlockEntity diesel) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, diesel);
        long oldPower = diesel.energy.getPower();
        int oldFill = diesel.tank.getFill();
        boolean oldWasOn = diesel.wasOn;
        FluidType oldType = diesel.tank.getTankType();

        diesel.wasOn = false;
        diesel.lastPowerProduced = 0L;

        if (HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(diesel.items,
                SLOT_IDENTIFIER, SLOT_IDENTIFIER_OUTPUT, diesel.tank, level, pos)) {
            oldType = HbmFluids.NONE;
        }
        diesel.processFluidItemTransfers(diesel.items,
                HbmFluidItemTransfer.loadTransfers(SLOT_FLUID_INPUT, SLOT_FLUID_OUTPUT, diesel.tank));
        if (oldType != diesel.tank.getTankType()) {
            diesel.refreshFluidNodeState();
        }

        diesel.powerCap = diesel.tank.getTankType() == HbmFluids.NITAN ? MAX_POWER * 10L : MAX_POWER;
        if (diesel.energy.getPower() > diesel.powerCap) {
            diesel.energy.setPower(diesel.powerCap);
        }
        HbmEnergyUtil.chargeItemFromStorage(diesel.items.getStackInSlot(SLOT_BATTERY),
                diesel.energy, diesel.energy.getProviderSpeed());
        diesel.generate(level, pos);
        diesel.tryProvideEnergyToPorts();
        diesel.sendSmokeToPorts(level, pos);
        if (diesel.tank.getTankType() != HbmFluids.NONE) {
            diesel.refreshTrackedReceiverFluidPortsReport(List.of(diesel.tank), diesel);
        }
        diesel.networkPackNT(50);

        if (oldPower != diesel.energy.getPower() || oldFill != diesel.tank.getFill()
                || oldWasOn != diesel.wasOn) {
            diesel.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DieselGeneratorBlockEntity diesel) {
        if (!level.isClientSide) {
            return;
        }
        diesel.audioLoop = LegacyMachineAudioBridge.updateLoop(diesel.audioLoop, diesel,
                "hbm:block.engine", diesel.wasOn, 10.0D, 10.0F);
    }

    private void generate(Level level, BlockPos pos) {
        if (level.hasNeighborSignal(pos) || !hasAcceptableFuel() || tank.getFill() <= 0) {
            return;
        }
        wasOn = true;
        tank.setFill(tank.getFill() - 1);
        if (level.getGameTime() % 5L == 0L) {
            pollution.polluteFluidRelease(level, pos, tank.getTankType(), FluidReleaseType.BURN, 5.0F);
        }
        long produced = getHEFromFuel();
        long before = energy.getPower();
        energy.setPower(Math.min(powerCap, before + produced));
        lastPowerProduced = Math.max(0L, energy.getPower() - before);
    }

    private void sendSmokeToPorts(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos connectorPos = pos.relative(direction);
            pollution.sendSmoke(level, connectorPos.getX(), connectorPos.getY(), connectorPos.getZ(), direction);
        }
    }

    public boolean hasAcceptableFuel() {
        return getHEFromFuel() > 0L;
    }

    public boolean isBurning() {
        return wasOn && tank.getFill() > 0 && hasAcceptableFuel();
    }

    public long getHEFromFuel() {
        return getHEFromFuel(tank.getTankType());
    }

    public static long getHEFromFuel(FluidType type) {
        CombustibleFluidTrait fuel = type == null ? null : type.getTrait(CombustibleFluidTrait.class);
        if (fuel == null || fuel.getGrade() == CombustibleFluidTrait.FuelGrade.LOW) {
            return 0L;
        }
        double efficiency = FUEL_EFFICIENCY.getOrDefault(fuel.getGrade(), 0.0D);
        return (long) (fuel.getCombustionEnergyPerBucket() / 1_000L * efficiency);
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    public long getMaxPower() {
        return powerCap;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    public int getPowerBarHeight(int height) {
        return powerCap <= 0L ? 0 : (int) (energy.getPower() * height / powerCap);
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
        return ALL_FLUID_PORTS;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return ALL_ENERGY_PORTS;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
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
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        if (tag == null || !tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            return false;
        }
        java.util.OptionalInt id = HbmFluidCopiable.copiedFluidIdAt(tag, index);
        if (id.isEmpty()) {
            return false;
        }
        tank.setTankType(HbmFluids.fromId(id.getAsInt()));
        onFluidContentsChanged();
        return true;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        boolean active = tank.getFill() > 0 && getHEFromFuel() > 0L;
        data.putBoolean(CompatEnergyControl.B_ACTIVE, active);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, active ? 1.0D : 0.0D);
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, getHEFromFuel());
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_DIESEL_FUEL, tank);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineDiesel", "Diesel Generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DieselGeneratorMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_LEGACY_ITEMS, items);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_MODERN_INVENTORY, items);
        tag.putLong("powerTime", energy.getPower());
        tag.putLong("powerCap", powerCap);
        tank.writeToNbt(tag, "fuel");
        pollution.writeLegacyNbt(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_LEGACY_ITEMS, Tag.TAG_LIST) || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_INVENTORY, items);
        }
        if (tag.contains("powerTime")) {
            energy.setPower(tag.getLong("powerTime"));
        }
        powerCap = tag.contains("powerCap") ? tag.getLong("powerCap") : MAX_POWER;
        tank.readFromNbt(tag, "fuel");
        pollution.readLegacyNbt(tag);
        readRuntimeSync(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putBoolean("wasOn", wasOn);
        tag.putLong("lastPowerProduced", lastPowerProduced);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
        readRuntimeSync(tag);
    }

    private void readRuntimeSync(CompoundTag tag) {
        if (tag.contains("wasOn")) {
            wasOn = tag.getBoolean("wasOn");
        }
        if (tag.contains("lastPowerProduced")) {
            lastPowerProduced = Math.max(0L, tag.getLong("lastPowerProduced"));
        }
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
        topItemHandler.invalidate();
        bottomItemHandler.invalidate();
        sideItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return getItemHandler(side).cast();
        }
        return super.getCapability(capability, side);
    }

    private LazyOptional<IItemHandler> getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return itemHandler;
        }
        return switch (side) {
            case UP -> topItemHandler;
            case DOWN -> bottomItemHandler;
            default -> sideItemHandler;
        };
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private static final class AccessibleItemHandler implements IItemHandler {
        private final ItemStackHandler items;
        private final int[] slots;
        private final boolean allowInsert;

        private AccessibleItemHandler(ItemStackHandler items, int[] slots, boolean allowInsert) {
            this.items = items;
            this.slots = slots;
            this.allowInsert = allowInsert;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slots[slot]);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return allowInsert ? items.insertItem(slots[slot], stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return items.extractItem(slots[slot], amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slots[slot]);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return allowInsert && items.isItemValid(slots[slot], stack);
        }
    }
}
