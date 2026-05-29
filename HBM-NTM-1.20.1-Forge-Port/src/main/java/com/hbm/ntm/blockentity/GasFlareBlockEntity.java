package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
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

public class GasFlareBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmLegacyButtonReceiver {
    public static final int SLOT_ENERGY_OUTPUT = 0;
    public static final int SLOT_FLUID_INPUT = 1;
    public static final int SLOT_FLUID_OUTPUT = 2;
    public static final int SLOT_IDENTIFIER = 3;
    public static final int SLOT_UPGRADE_SPEED = 4;
    public static final int SLOT_UPGRADE_EFFECT = 5;
    public static final int CONTROL_VALVE = 0;
    public static final int CONTROL_BURN = 1;

    private static final long MAX_POWER = 100_000L;
    private static final int TANK_CAPACITY = 64_000;
    private static final int BASE_MAX_VENT = 50;
    private static final int BASE_MAX_BURN = 10;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(2, 0, 0, Direction.EAST),
            FluidPort.of(-2, 0, 0, Direction.WEST),
            FluidPort.of(0, 0, 2, Direction.SOUTH),
            FluidPort.of(0, 0, -2, Direction.NORTH));
    private static final List<EnergyPort> ENERGY_PORTS = List.of(
            EnergyPort.of(2, 0, 0, Direction.EAST),
            EnergyPort.of(-2, 0, 0, Direction.WEST),
            EnergyPort.of(0, 0, 2, Direction.SOUTH),
            EnergyPort.of(0, 0, -2, Direction.NORTH));

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_ENERGY_OUTPUT -> stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
                case SLOT_FLUID_INPUT -> true;
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_UPGRADE_SPEED, SLOT_UPGRADE_EFFECT -> false;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private boolean on;
    private boolean burn;
    private int fluidUsed;
    private int lastOutput;

    public GasFlareBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER), new HbmFluidTank(HbmFluids.GAS, TANK_CAPACITY));
    }

    private GasFlareBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank tank) {
        super(ModBlockEntities.GAS_FLARE.get(), pos, state, energy, List.of(tank));
        this.tank = tank;
        this.tank.conform(new HbmFluidStack(HbmFluids.GAS, 0));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasFlareBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, blockEntity);
        boolean changed = false;
        int oldFill = blockEntity.tank.getFill();
        long oldPower = blockEntity.energy.getPower();
        boolean oldOn = blockEntity.on;
        boolean oldBurn = blockEntity.burn;

        blockEntity.fluidUsed = 0;
        blockEntity.lastOutput = 0;
        changed |= blockEntity.setTankTypeFromIdentifierSlot();
        changed |= HbmFluidItemTransfer.loadTankFromSlot(blockEntity.items,
                SLOT_FLUID_INPUT, SLOT_FLUID_OUTPUT, blockEntity.tank);
        changed |= blockEntity.consumeFluid(level, pos);
        HbmEnergyUtil.chargeItemFromStorage(blockEntity.items.getStackInSlot(SLOT_ENERGY_OUTPUT),
                blockEntity.energy, blockEntity.energy.getProviderSpeed());
        blockEntity.tryProvideEnergyToPorts();

        changed |= oldFill != blockEntity.tank.getFill()
                || oldPower != blockEntity.energy.getPower()
                || oldOn != blockEntity.on
                || oldBurn != blockEntity.burn;
        if (changed) {
            blockEntity.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
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

    public boolean doesBurn() {
        return burn;
    }

    public int getFluidUsed() {
        return fluidUsed;
    }

    public int getLastOutput() {
        return lastOutput;
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    public void toggleOn() {
        on = !on;
        onFluidContentsChanged();
    }

    public void toggleBurn() {
        burn = !burn;
        onFluidContentsChanged();
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
    public com.hbm.ntm.energy.HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return com.hbm.ntm.energy.HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return ENERGY_PORTS;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    public Component getDisplayName() {
        return Component.translatable("container.gasFlare");
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_VALVE || id == CONTROL_BURN;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_VALVE) {
            toggleOn();
        } else if (id == CONTROL_BURN) {
            toggleBurn();
        }
    }

    private boolean consumeFluid(Level level, BlockPos pos) {
        if (!on || tank.isEmpty()) {
            return false;
        }
        FluidType type = tank.getTankType();
        FlammableFluidTrait flammable = type.getTrait(FlammableFluidTrait.class);
        if (!burn || flammable == null) {
            if (!type.hasTrait(SimpleFluidTraits.Gaseous.class)
                    && !type.hasTrait(SimpleFluidTraits.GaseousAtRoomTemperature.class)) {
                return false;
            }
            int eject = Math.min(BASE_MAX_VENT, tank.getFill());
            if (eject <= 0) {
                return false;
            }
            tank.release(level, pos, eject, FluidReleaseType.SPILL, false);
            fluidUsed = eject;
            return true;
        }

        int eject = Math.min(BASE_MAX_BURN, tank.getFill());
        if (eject <= 0) {
            return false;
        }
        tank.release(level, pos, eject, FluidReleaseType.BURN, false);
        long powerProduced = flammable.getHeatEnergyPerBucket() * eject / 1_000L;
        int penalty = type.hasTrait(SimpleFluidTraits.Gaseous.class)
                || type.hasTrait(SimpleFluidTraits.GaseousAtRoomTemperature.class) ? 5 : 10;
        powerProduced /= penalty;
        if (powerProduced > 0L) {
            long before = energy.getPower();
            energy.setPower(Math.min(MAX_POWER, before + powerProduced));
            lastOutput = (int) Math.min(Integer.MAX_VALUE, energy.getPower() - before);
        }
        fluidUsed = eject;
        return true;
    }

    private boolean setTankTypeFromIdentifierSlot() {
        ItemStack input = items.getStackInSlot(SLOT_IDENTIFIER);
        if (input.isEmpty() || !(input.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        FluidType newType = identifier.getIdentifiedFluid(level, worldPosition, input);
        if (newType == null || newType == tank.getTankType()) {
            return false;
        }
        tank.setTankType(newType);
        onFluidContentsChanged();
        return true;
    }

    @Override
    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
        tag.putBoolean("isOn", on);
        tag.putBoolean("doesBurn", burn);
        tag.putInt("fluidUsed", fluidUsed);
        tag.putInt("output", lastOutput);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
        on = tag.getBoolean("isOn");
        burn = tag.getBoolean("doesBurn");
        fluidUsed = tag.getInt("fluidUsed");
        lastOutput = tag.getInt("output");
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

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new java.util.ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }
}
