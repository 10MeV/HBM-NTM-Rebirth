package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.config.SteamTurbineConfig;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmTurbineConversion;
import com.hbm.ntm.fluid.HbmTurbineConversion.TurbineResult;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.menu.LegacyLargeTurbineMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

public class LegacyLargeTurbineBlockEntity extends LegacySteamTurbineBlockEntity implements MenuProvider {
    public static final int SLOT_IDENTIFIER = 0;
    public static final int SLOT_IDENTIFIER_OUTPUT = 1;
    public static final int SLOT_INPUT_CONTAINER = 2;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_OUTPUT_CONTAINER = 5;
    public static final int SLOT_OUTPUT_CONTAINER_OUTPUT = 6;
    public static final int SLOT_COUNT = 7;
    private static final String TAG_ITEMS = "items";
    private static final double CONSUMPTION_PERCENT = 0.2D;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem identifier
                        && isValidTurbineInput(identifier.getIdentifiedFluid(level, worldPosition, stack));
                case SLOT_INPUT_CONTAINER, SLOT_OUTPUT_CONTAINER -> true;
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                case SLOT_IDENTIFIER_OUTPUT, SLOT_INPUT_CONTAINER_OUTPUT, SLOT_OUTPUT_CONTAINER_OUTPUT -> false;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private int age;
    private float rotor;
    private float lastRotor;
    private float fanAcceleration;
    private final float audioDesync;
    private Object audioLoop;

    public LegacyLargeTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_LARGE_TURBINE.get(), pos, state,
                SteamTurbineConfig.largeTurbineMaxPower(),
                SteamTurbineConfig.largeTurbineInputTankSize(),
                SteamTurbineConfig.largeTurbineOutputTankSize());
        audioDesync = new Random(pos.asLong()).nextFloat() * 0.05F;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyLargeTurbineBlockEntity turbine) {
        tickTurbine(level, pos, state, turbine);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LegacyLargeTurbineBlockEntity turbine) {
        turbine.lastRotor = turbine.rotor;
        turbine.rotor += turbine.fanAcceleration;
        if (turbine.rotor >= 360.0F) {
            turbine.rotor -= 360.0F;
            turbine.lastRotor -= 360.0F;
        }
        if (turbine.isOperational()) {
            turbine.fanAcceleration = Math.max(0.0F, Math.min(15.0F,
                    turbine.fanAcceleration + 0.075F + turbine.audioDesync));
        } else {
            turbine.fanAcceleration = Math.max(0.0F, Math.min(15.0F, turbine.fanAcceleration - 0.1F));
        }
        turbine.updateAudioLoop();
    }

    @Override
    protected double getEfficiency() {
        return SteamTurbineConfig.largeTurbineEfficiency();
    }

    @Override
    protected double getConsumptionPercent() {
        return CONSUMPTION_PERCENT;
    }

    @Override
    protected TurbineResult runConversion() {
        CoolableFluidTrait trait = inputTank.getTankType().getTrait(CoolableFluidTrait.class);
        int maxInputMb = 0;
        if (trait != null && trait.getAmountRequired() > 0) {
            int inputOps = inputTank.getFill() / trait.getAmountRequired();
            int cappedOps = (int) Math.ceil(inputOps * CONSUMPTION_PERCENT);
            maxInputMb = cappedOps * trait.getAmountRequired();
        }
        return HbmTurbineConversion.run(inputTank, outputTank, getEfficiency(), maxInputMb, false);
    }

    @Override
    protected void beforeTurbineTick() {
        normalizeConfigState();
        age = (age + 1) % 2;
        handleInventoryFluidTransfer();
        HbmEnergyUtil.chargeItemFromStorage(items.getStackInSlot(SLOT_BATTERY),
                energy, energy.getProviderSpeed());
        energy.setPower((long) (energy.getPower() * 0.95D));
    }

    @Override
    protected void afterTurbineTick() {
        HbmFluidItemTransfer.unloadTankToSlot(items, SLOT_OUTPUT_CONTAINER,
                SLOT_OUTPUT_CONTAINER_OUTPUT, outputTank);
    }

    @Override
    protected void normalizeConfigState() {
        long maxPower = SteamTurbineConfig.largeTurbineMaxPower();
        energy.setMaxPower(maxPower);
        energy.setTransferRates(0L, maxPower);
        normalizeTankCapacity(SteamTurbineConfig.largeTurbineInputTankSize(),
                SteamTurbineConfig.largeTurbineOutputTankSize());
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = rotatedFacing();
        return List.of(
                fluidPort(rot, rot.getStepX() * 2, 0, rot.getStepZ() * 2),
                fluidPort(rot.getOpposite(), -rot.getStepX() * 2, 0, -rot.getStepZ() * 2),
                fluidPort(facing, facing.getStepX() * 2, 0, facing.getStepZ() * 2));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        return List.of(energyPort(facing.getOpposite(), -facing.getStepX() * 4, 0, -facing.getStepZ() * 4));
    }

    private void handleInventoryFluidTransfer() {
        if (level == null) {
            return;
        }
        HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, SLOT_IDENTIFIER_OUTPUT,
                inputTank, level, worldPosition);
        HbmFluidItemTransfer.loadTankFromSlot(items, SLOT_INPUT_CONTAINER, SLOT_INPUT_CONTAINER_OUTPUT, inputTank);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public float getRotor() {
        return rotor;
    }

    public float getLastRotor() {
        return lastRotor;
    }

    public float getFanAcceleration() {
        return fanAcceleration;
    }

    private void updateAudioLoop() {
        float turbineSpeed = fanAcceleration / 15.0F;
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.largeTurbineRunning",
                fanAcceleration > 0.0F, 10.0D, 20.0F,
                0.4F * turbineSpeed, 0.25F + 0.75F * turbineSpeed);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, getLastOutputProduced() > 0);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, getLastInputUsed());
        data.putDouble(CompatEnergyControl.D_OUTPUT_MB, getLastOutputProduced());
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, getLastPowerProduced());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        inputTank.writeToNbt(tag, "water");
        outputTank.writeToNbt(tag, "steam");
        tag.putLong("power", energy.getPower());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        normalizeConfigState();
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains("water")) {
            inputTank.readFromNbt(tag, "water");
        }
        if (tag.contains("steam")) {
            outputTank.readFromNbt(tag, "steam");
        }
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        readRuntimeSync(tag);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineLargeTurbine", "Industrial Steam Turbine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LegacyLargeTurbineMenu(containerId, inventory, this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("age", age);
        tag.putFloat("rotor", rotor);
        tag.putFloat("lastRotor", lastRotor);
        tag.putFloat("fanAcceleration", fanAcceleration);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        readRuntimeSync(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    private void readRuntimeSync(CompoundTag tag) {
        if (tag.contains("age")) {
            age = Math.floorMod(tag.getInt("age"), 2);
        }
        if (tag.contains("rotor")) {
            rotor = tag.getFloat("rotor");
        }
        if (tag.contains("lastRotor")) {
            lastRotor = tag.getFloat("lastRotor");
        }
        if (tag.contains("fanAcceleration")) {
            fanAcceleration = Math.max(0.0F, Math.min(15.0F, tag.getFloat("fanAcceleration")));
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
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side == null) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public static boolean isValidTurbineInput(FluidType type) {
        CoolableFluidTrait trait = type == null ? null : type.getTrait(CoolableFluidTrait.class);
        return trait != null && trait.getEfficiency(CoolableFluidTrait.CoolingType.TURBINE) > 0.0D;
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
            return slot == SLOT_BATTERY ? items.insertItem(slot, stack, simulate) : stack;
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
            return slot == SLOT_BATTERY && items.isItemValid(slot, stack);
        }
    }
}
