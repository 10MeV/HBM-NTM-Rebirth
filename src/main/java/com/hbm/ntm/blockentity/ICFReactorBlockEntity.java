package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingStep;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.menu.ICFReactorMenu;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ICFReactorBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, IInfoProviderEC {
    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_ACTIVE = 5;
    public static final int SLOT_OUTPUT_START = 6;
    public static final int SLOT_IDENTIFIER = 11;
    public static final int SLOT_COUNT = 12;
    public static final long MAX_HEAT = 1_000_000_000_000L;
    public static final int COOLANT_CAPACITY = 512_000;
    public static final int FLUX_CAPACITY = 24_000;
    private static final String TAG_LASER = "laser";
    private static final String TAG_MAX_LASER = "maxLaser";
    private static final int[] AUTOMATION_SLOTS = {
            SLOT_INPUT_START, SLOT_INPUT_START + 1, SLOT_INPUT_START + 2, SLOT_INPUT_START + 3,
            SLOT_INPUT_START + 4, SLOT_OUTPUT_START, SLOT_OUTPUT_START + 1, SLOT_OUTPUT_START + 2,
            SLOT_OUTPUT_START + 3, SLOT_OUTPUT_START + 4};

    private final HbmFluidTank coolantTank;
    private final HbmFluidTank hotCoolantTank;
    private final HbmFluidTank stellarFluxTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (slot == SLOT_IDENTIFIER) {
                identifierTankDirty = true;
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT_START, SLOT_INPUT_START + 1, SLOT_INPUT_START + 2, SLOT_INPUT_START + 3,
                        SLOT_INPUT_START + 4 -> stack.is(ModItems.ICF_PELLET.get());
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private long laser;
    private long maxLaser;
    private long heat;
    private long heatup;
    private int consumption;
    private int output;
    private boolean identifierTankDirty = true;
    private Direction cachedFluidPortFacing;
    private List<FluidPort> cachedFluidPorts = List.of();

    public ICFReactorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.SODIUM, COOLANT_CAPACITY),
                new HbmFluidTank(HbmFluids.SODIUM_HOT, COOLANT_CAPACITY),
                new HbmFluidTank(HbmFluids.STELLAR_FLUX, FLUX_CAPACITY));
    }

    private ICFReactorBlockEntity(BlockPos pos, BlockState state, HbmFluidTank coolantTank,
            HbmFluidTank hotCoolantTank, HbmFluidTank stellarFluxTank) {
        super(ModBlockEntities.ICF_REACTOR.get(), pos, state, List.of(coolantTank, hotCoolantTank, stellarFluxTank));
        this.coolantTank = coolantTank;
        this.hotCoolantTank = hotCoolantTank;
        this.stellarFluxTank = stellarFluxTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ICFReactorBlockEntity reactor) {
        boolean changed = reactor.shouldRefreshIdentifierTankType(level) && reactor.updateIdentifierTankType();
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, reactor);
        changed |= reactor.tickServer(level);
        reactor.networkPackNT(150);
        if (changed) {
            reactor.setChanged();
        }
        reactor.clearLaserPulse();
    }

    public void receiveLaser(long power, long maxPower) {
        laser += power;
        maxLaser += maxPower;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getCoolantTank() {
        return coolantTank;
    }

    public HbmFluidTank getHotCoolantTank() {
        return hotCoolantTank;
    }

    public HbmFluidTank getStellarFluxTank() {
        return stellarFluxTank;
    }

    public long getLaser() {
        return laser;
    }

    public long getMaxLaser() {
        return maxLaser;
    }

    public long getHeat() {
        return heat;
    }

    public long getHeatup() {
        return heatup;
    }

    public int getConsumption() {
        return consumption;
    }

    public int getOutput() {
        return output;
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putBoolean(CompatEnergyControl.B_ACTIVE, heatup > 0L);
        data.putLong(CompatEnergyControl.L_CAPACITY_TU, MAX_HEAT);
        data.putLong(CompatEnergyControl.L_ENERGY_TU, heat);
        data.putLong(CompatEnergyControl.L_ICF_HEATING_RATE_TU, heatup);
        data.putLong(CompatEnergyControl.L_ICF_LASER_TU, laser);
        data.putLong(CompatEnergyControl.L_ICF_MAX_LASER_TU, maxLaser);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, consumption);
        data.putDouble(CompatEnergyControl.D_OUTPUT_MB, output);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_ICF_COOLANT, coolantTank);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_ICF_HOT_COOLANT, hotCoolantTank);
        CompatEnergyControl.putTankAmountInfo(data, CompatEnergyControl.S_ICF_STELLAR_FLUX, stellarFluxTank);
        putPelletInfo(data, items.getStackInSlot(SLOT_ACTIVE));
    }

    private static void putPelletInfo(CompoundTag data, ItemStack stack) {
        if (stack.is(ModItems.ICF_PELLET.get())) {
            data.putLong(CompatEnergyControl.L_ICF_PELLET_DEPLETION, ICFPelletItem.getDepletion(stack));
            data.putLong(CompatEnergyControl.L_ICF_PELLET_MAX_DEPLETION, ICFPelletItem.getMaxDepletion(stack));
            data.putLong(CompatEnergyControl.L_ICF_PELLET_FUSING_DIFFICULTY, ICFPelletItem.getFusingDifficulty(stack));
            data.putString(CompatEnergyControl.S_ICF_PELLET_PRIMARY, ICFPelletItem.type(stack, true).name());
            data.putString(CompatEnergyControl.S_ICF_PELLET_SECONDARY, ICFPelletItem.type(stack, false).name());
        } else {
            data.putLong(CompatEnergyControl.L_ICF_PELLET_DEPLETION, 0L);
            data.putLong(CompatEnergyControl.L_ICF_PELLET_MAX_DEPLETION, 0L);
            data.putLong(CompatEnergyControl.L_ICF_PELLET_FUSING_DIFFICULTY, 0L);
            data.putString(CompatEnergyControl.S_ICF_PELLET_PRIMARY, "");
            data.putString(CompatEnergyControl.S_ICF_PELLET_SECONDARY, "");
        }
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineICF", "ICF Reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ICFReactorMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(coolantTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(coolantTank, hotCoolantTank, stellarFluxTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(hotCoolantTank, stellarFluxTank);
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
    public long getProviderSpeed(FluidType type, int pressure) {
        if (type == hotCoolantTank.getTankType()) {
            return Math.max(1L, hotCoolantTank.getFill());
        }
        if (type == stellarFluxTank.getTankType()) {
            return Math.max(1L, stellarFluxTank.getFill());
        }
        return 1L;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts();
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == coolantTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return (type == hotCoolantTank.getTankType() && hotCoolantTank.getFill() > 0)
                || (type == stellarFluxTank.getTankType() && stellarFluxTank.getFill() > 0);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(coolantTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(hotCoolantTank, stellarFluxTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() + 0.5D - 8.0D,
                worldPosition.getY(),
                worldPosition.getZ() + 0.5D - 8.0D,
                worldPosition.getX() + 0.5D + 9.0D,
                worldPosition.getY() + 5.5D,
                worldPosition.getZ() + 0.5D + 9.0D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        coolantTank.writeToNbt(tag, "t0");
        hotCoolantTank.writeToNbt(tag, "t1");
        stellarFluxTank.writeToNbt(tag, "t2");
        tag.putLong("heat", heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        if (hasTankTag(tag, "t0")) {
            coolantTank.readFromNbt(tag, "t0");
        }
        if (hasTankTag(tag, "t1")) {
            hotCoolantTank.readFromNbt(tag, "t1");
        }
        if (hasTankTag(tag, "t2")) {
            stellarFluxTank.readFromNbt(tag, "t2");
        }
        heat = tag.getLong("heat");
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_LASER, laser);
        tag.putLong(TAG_MAX_LASER, maxLaser);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_LASER)) {
            laser = tag.getLong(TAG_LASER);
        }
        if (tag.contains(TAG_MAX_LASER)) {
            maxLaser = tag.getLong(TAG_MAX_LASER);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeLong(laser);
        data.writeLong(maxLaser);
        data.writeLong(heat);
        writeTank(data, coolantTank);
        writeTank(data, hotCoolantTank);
        writeTank(data, stellarFluxTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        laser = data.readLong();
        maxLaser = data.readLong();
        heat = data.readLong();
        readTank(data, coolantTank);
        readTank(data, hotCoolantTank);
        readTank(data, stellarFluxTank);
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

    private boolean tickServer(Level level) {
        boolean changed = false;
        changed |= ejectDepletedPellet();
        changed |= insertFreshPellet();
        FluidType previousHotType = hotCoolantTank.getTankType();
        int previousHotFill = hotCoolantTank.getFill();
        int previousFluxFill = stellarFluxTank.getFill();
        heatup = 0L;
        ItemStack active = items.getStackInSlot(SLOT_ACTIVE);
        if (active.is(ModItems.ICF_PELLET.get()) && ICFPelletItem.getFusingDifficulty(active) <= laser) {
            heatup = ICFPelletItem.react(active, laser);
            heat += heatup;
            if (ICFPelletItem.getDepletion(active) >= ICFPelletItem.getMaxDepletion(active)) {
                items.setStackInSlot(SLOT_ACTIVE, new ItemStack(ModItems.ICF_PELLET_DEPLETED.get()));
                changed = true;
            }
            int fluxAdd = (int) Math.ceil(heat * 10.0D / MAX_HEAT);
            stellarFluxTank.setFill(stellarFluxTank.getFill() + fluxAdd);
            if (stellarFluxTank.getFill() > stellarFluxTank.getMaxFill()) {
                stellarFluxTank.setFill(stellarFluxTank.getMaxFill());
            }
            CompoundTag data = new CompoundTag();
            data.putString("type", ParticleUtil.TYPE_HADRON);
            ParticleUtil.spawnAux(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 3.5D,
                    worldPosition.getZ() + 0.5D, data, 25.0D);
        }
        if (heatup == 0L) {
            heat += (long) (laser * 0.25D);
        }
        coolWithLegacyFormula();
        if (hotCoolantTank.getFill() > 0) {
            tryProvideFluidToPorts(hotCoolantTank.getTankType(), hotCoolantTank.getPressure(), this);
        }
        if (stellarFluxTank.getFill() > 0) {
            tryProvideFluidToPorts(stellarFluxTank.getTankType(), stellarFluxTank.getPressure(), this);
        }
        if (previousHotType != hotCoolantTank.getTankType()
                || (previousHotFill <= 0) != (hotCoolantTank.getFill() <= 0)
                || (previousFluxFill <= 0) != (stellarFluxTank.getFill() <= 0)) {
            markTankTypesDirty();
        }
        heat = (long) (heat * 0.999D);
        if (heat > MAX_HEAT) {
            heat = MAX_HEAT;
        }
        return changed;
    }

    private boolean updateIdentifierTankType() {
        identifierTankDirty = false;
        boolean changed = setFluidTankTypeFromIdentifierSlotReport(items, SLOT_IDENTIFIER, SLOT_IDENTIFIER, coolantTank,
                0, false).changed();
        if (changed) {
            markTankTypesDirty();
        }
        return changed;
    }

    private boolean shouldRefreshIdentifierTankType(Level level) {
        return identifierTankDirty || level.getGameTime() % 20L == Math.floorMod(worldPosition.hashCode(), 20);
    }

    private void clearLaserPulse() {
        laser = 0L;
        maxLaser = 0L;
    }

    private boolean ejectDepletedPellet() {
        if (!items.getStackInSlot(SLOT_ACTIVE).is(ModItems.ICF_PELLET_DEPLETED.get())) {
            return false;
        }
        for (int slot = SLOT_OUTPUT_START; slot < SLOT_OUTPUT_START + 5; slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                items.setStackInSlot(slot, items.getStackInSlot(SLOT_ACTIVE).copy());
                items.setStackInSlot(SLOT_ACTIVE, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private boolean insertFreshPellet() {
        if (!items.getStackInSlot(SLOT_ACTIVE).isEmpty()) {
            return false;
        }
        for (int slot = SLOT_INPUT_START; slot < SLOT_INPUT_START + 5; slot++) {
            if (items.getStackInSlot(slot).is(ModItems.ICF_PELLET.get())) {
                items.setStackInSlot(SLOT_ACTIVE, items.getStackInSlot(slot).copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    private void coolWithLegacyFormula() {
        consumption = 0;
        output = 0;
        HeatableFluidTrait trait = coolantTank.getTankType().getTrait(HeatableFluidTrait.class);
        if (trait == null) {
            return;
        }
        HeatingStep step = trait.getFirstStep();
        hotCoolantTank.setTankType(step.producedType());
        int coolingCycles = coolantTank.getFill() / step.amountRequired();
        int heatingCycles = (hotCoolantTank.getMaxFill() - hotCoolantTank.getFill()) / step.amountProduced();
        int heatCycles = (int) Math.min(heat / 4.0D / step.heatRequired() * trait.getEfficiency(HeatingType.ICF),
                heat / (double) step.heatRequired());
        int cycles = Math.min(coolingCycles, Math.min(heatingCycles, heatCycles));
        coolantTank.setFill(coolantTank.getFill() - step.amountRequired() * cycles);
        hotCoolantTank.setFill(hotCoolantTank.getFill() + step.amountProduced() * cycles);
        heat -= step.heatRequired() * cycles;
        consumption = step.amountRequired() * cycles;
        output = step.amountProduced() * cycles;
    }

    private List<FluidPort> fluidPorts() {
        Direction facing = getBlockState().getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING);
        if (facing == cachedFluidPortFacing && !cachedFluidPorts.isEmpty()) {
            return cachedFluidPorts;
        }
        Direction rot = facing.getClockWise();
        cachedFluidPortFacing = facing;
        cachedFluidPorts = List.of(
                FluidPort.of(0, 6, 0, Direction.UP),
                FluidPort.of(0, -1, 0, Direction.DOWN),
                FluidPort.of(facing.getStepX() * 3 + rot.getStepX() * 6, 3,
                        facing.getStepZ() * 3 + rot.getStepZ() * 6, facing),
                FluidPort.of(facing.getStepX() * 3 - rot.getStepX() * 6, 3,
                        facing.getStepZ() * 3 - rot.getStepZ() * 6, facing),
                FluidPort.of(-facing.getStepX() * 3 + rot.getStepX() * 6, 3,
                        -facing.getStepZ() * 3 + rot.getStepZ() * 6, facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 3 - rot.getStepX() * 6, 3,
                        -facing.getStepZ() * 3 - rot.getStepZ() * 6, facing.getOpposite()));
        return cachedFluidPorts;
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.write(data, tank);
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.read(data, tank);
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override public int getSlots() { return AUTOMATION_SLOTS.length; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            return mapped >= SLOT_INPUT_START && mapped < SLOT_INPUT_START + 5 && stack.is(ModItems.ICF_PELLET.get())
                    ? items.insertItem(mapped, stack, simulate)
                    : stack;
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped >= SLOT_OUTPUT_START && mapped < SLOT_OUTPUT_START + 5
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped >= SLOT_INPUT_START && mapped < SLOT_INPUT_START + 5 && stack.is(ModItems.ICF_PELLET.get());
        }

        private int map(int slot) {
            return slot >= 0 && slot < AUTOMATION_SLOTS.length ? AUTOMATION_SLOTS[slot] : -1;
        }
    }
}
