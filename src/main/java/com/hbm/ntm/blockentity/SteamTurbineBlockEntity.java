package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.config.SteamTurbineConfig;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.HbmTurbineConversion;
import com.hbm.ntm.fluid.HbmTurbineConversion.TurbineResult;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.menu.SteamTurbineMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SteamTurbineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmStandardFluidSender {
    public static final int INPUT_TANK = 0;
    public static final int OUTPUT_TANK = 1;
    public static final int SLOT_IDENTIFIER = 0;
    public static final int SLOT_IDENTIFIER_OUTPUT = 1;
    public static final int SLOT_INPUT_CONTAINER = 2;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_OUTPUT_CONTAINER = 5;
    public static final int SLOT_OUTPUT_CONTAINER_OUTPUT = 6;
    public static final int SLOT_COUNT = 7;
    private static final String TAG_ITEMS = "items";
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.allAdjacent();

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
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
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
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
    private int lastInputUsed;
    private int lastOutputProduced;
    private long lastPowerProduced;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(SteamTurbineConfig.steamTurbineMaxPower(), 0L,
                        SteamTurbineConfig.steamTurbineMaxPower()),
                new HbmFluidTank(HbmFluids.STEAM, SteamTurbineConfig.steamTurbineInputTankSize()),
                new HbmFluidTank(HbmFluids.SPENTSTEAM, SteamTurbineConfig.steamTurbineOutputTankSize()));
    }

    private SteamTurbineBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank inputTank,
            HbmFluidTank outputTank) {
        super(ModBlockEntities.STEAM_TURBINE.get(), pos, state, energy, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity turbine) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, turbine);
        turbine.normalizeConfigState();
        turbine.age = (turbine.age + 1) % 2;
        turbine.handleInventoryFluidTransfer();
        HbmEnergyUtil.chargeItemFromStorage(turbine.items.getStackInSlot(SLOT_BATTERY),
                turbine.energy, turbine.energy.getProviderSpeed());
        turbine.energy.setPower((long) (turbine.energy.getPower() * 0.95D));
        HbmTurbineConversion.prepareOutputTank(turbine.inputTank, turbine.outputTank);

        TurbineResult result = turbine.runTurbine();
        if (result.powerProduced() > 0L) {
            turbine.energy.setPower(Math.min(turbine.energy.getMaxPower(), turbine.energy.getPower() + result.powerProduced()));
        }
        turbine.lastInputUsed = result.inputUsed();
        turbine.lastOutputProduced = result.outputProduced();
        turbine.lastPowerProduced = result.powerProduced();

        HbmEnergyUtil.tryProvideToAllNeighbors(level, pos, turbine.energy);
        if (turbine.outputTank.getTankType() != HbmFluids.NONE && turbine.outputTank.getFill() > 0) {
            turbine.tryProvideFluidToPorts(turbine.outputTank.getTankType(), turbine.outputTank.getPressure(), turbine);
        }
        HbmFluidItemTransfer.unloadTankToSlot(turbine.items, SLOT_OUTPUT_CONTAINER,
                SLOT_OUTPUT_CONTAINER_OUTPUT, turbine.outputTank);
        turbine.networkPackNT(25);
        if (result.converted() || level.getGameTime() % 20L == 0L) {
            turbine.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private TurbineResult runTurbine() {
        return HbmTurbineConversion.run(inputTank, outputTank, SteamTurbineConfig.steamTurbineEfficiency(),
                SteamTurbineConfig.steamTurbineMaxSteamPerTick(), false);
    }

    private void normalizeConfigState() {
        long maxPower = SteamTurbineConfig.steamTurbineMaxPower();
        energy.setMaxPower(maxPower);
        energy.setTransferRates(0L, maxPower);
        inputTank.changeTankSize(SteamTurbineConfig.steamTurbineInputTankSize());
        outputTank.changeTankSize(SteamTurbineConfig.steamTurbineOutputTankSize());
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

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public int getLastInputUsed() {
        return lastInputUsed;
    }

    public int getLastOutputProduced() {
        return lastOutputProduced;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    @Nullable
    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return null;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank);
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
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == inputTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE && outputTank.getFill() > 0 && type == outputTank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(inputTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(outputTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
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
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineTurbine", "Steam Turbine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SteamTurbineMenu(containerId, inventory, this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("age", age);
        tag.putInt("lastInputUsed", lastInputUsed);
        tag.putInt("lastOutputProduced", lastOutputProduced);
        tag.putLong("lastPowerProduced", lastPowerProduced);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        readRuntimeSync(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
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

    public static boolean isValidTurbineInput(FluidType type) {
        CoolableFluidTrait trait = type == null ? null : type.getTrait(CoolableFluidTrait.class);
        return trait != null && trait.getEfficiency(CoolableFluidTrait.CoolingType.TURBINE) > 0.0D;
    }

    private void readRuntimeSync(CompoundTag tag) {
        if (tag.contains("age")) {
            age = Math.floorMod(tag.getInt("age"), 2);
        }
        if (tag.contains("lastInputUsed")) {
            lastInputUsed = Math.max(0, tag.getInt("lastInputUsed"));
        }
        if (tag.contains("lastOutputProduced")) {
            lastOutputProduced = Math.max(0, tag.getInt("lastOutputProduced"));
        }
        if (tag.contains("lastPowerProduced")) {
            lastPowerProduced = Math.max(0L, tag.getLong("lastPowerProduced"));
        }
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
