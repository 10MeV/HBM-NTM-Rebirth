package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidCompressorRecipes;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.menu.CompressorMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompressorBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyButtonReceiver {
    public static final int SLOT_IDENTIFIER = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_UPGRADE_SPEED = 2;
    public static final int SLOT_UPGRADE_POWER = 3;
    public static final int ITEM_COUNT = 4;
    public static final int CONTROL_INPUT_PRESSURE = 0;

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_INPUT_PRESSURE = "inputPressure";
    private static final String TAG_LEGACY_COMPRESSION = "compression";
    private static final long MAX_POWER = 100_000L;
    private static final int TANK_CAPACITY = 16_000;
    private static final int BASE_POWER_REQUIREMENT = 2_500;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private final ItemStackHandler items = new ItemStackHandler(ITEM_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_BATTERY -> stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
                case SLOT_UPGRADE_SPEED, SLOT_UPGRADE_POWER -> false;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int progress;
    private int processTime = 100;
    private int powerRequirement = BASE_POWER_REQUIREMENT;
    private boolean on;
    private float fanSpin;
    private float prevFanSpin;
    private float piston;
    private float prevPiston;
    private boolean pistonDir;
    private float pistonReturnSpeed = 0.1F;

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY).withPressure(1));
    }

    private CompressorBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(ModBlockEntities.COMPRESSOR.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CompressorBlockEntity compressor) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, compressor);
        boolean changed = false;
        int oldInputFill = compressor.inputTank.getFill();
        int oldOutputFill = compressor.outputTank.getFill();
        FluidType oldInputType = compressor.inputTank.getTankType();
        FluidType oldOutputType = compressor.outputTank.getTankType();
        int oldInputPressure = compressor.inputTank.getPressure();
        int oldOutputPressure = compressor.outputTank.getPressure();
        long oldPower = compressor.energy.getPower();
        int oldProgress = compressor.progress;
        boolean oldOn = compressor.on;

        changed |= compressor.setInputTypeFromIdentifierSlot();
        compressor.setupOutputTank();
        HbmEnergyUtil.chargeStorageFromItem(compressor.items.getStackInSlot(SLOT_BATTERY),
                compressor.energy, compressor.energy.getReceiverSpeed());
        compressor.refreshFluidPorts();
        changed |= compressor.processRecipe();

        changed |= oldInputFill != compressor.inputTank.getFill()
                || oldOutputFill != compressor.outputTank.getFill()
                || oldInputType != compressor.inputTank.getTankType()
                || oldOutputType != compressor.outputTank.getTankType()
                || oldInputPressure != compressor.inputTank.getPressure()
                || oldOutputPressure != compressor.outputTank.getPressure()
                || oldPower != compressor.energy.getPower()
                || oldProgress != compressor.progress
                || oldOn != compressor.on;
        if (changed) {
            compressor.setChanged();
        }
        compressor.networkPackNT(100);
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CompressorBlockEntity compressor) {
        if (!level.isClientSide) {
            return;
        }
        compressor.prevFanSpin = compressor.fanSpin;
        compressor.prevPiston = compressor.piston;
        if (!compressor.on) {
            return;
        }
        if (compressor.isCompact()) {
            compressor.advanceFan(45.0F);
            return;
        }
        compressor.advanceFan(15.0F);
        if (compressor.pistonDir) {
            compressor.piston -= compressor.pistonReturnSpeed;
            if (compressor.piston <= 0.0F) {
                LegacySoundPlayer.playSoundClient(pos, "hbm:item.boltgun", compressor.getVolume(0.5F), 0.75F);
                compressor.pistonDir = false;
            }
        } else {
            compressor.piston += 0.05F;
            if (compressor.piston >= 1.0F) {
                compressor.pistonReturnSpeed = 0.085F + level.random.nextFloat() * 0.03F;
                compressor.pistonDir = true;
            }
        }
        compressor.piston = Mth.clamp(compressor.piston, 0.0F, 1.0F);
    }

    private void advanceFan(float step) {
        fanSpin += step;
        if (fanSpin >= 360.0F) {
            prevFanSpin -= 360.0F;
            fanSpin -= 360.0F;
        }
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getPowerRequirement() {
        return powerRequirement;
    }

    public boolean isOn() {
        return on;
    }

    public float getFanSpin(float partialTick) {
        return prevFanSpin + (fanSpin - prevFanSpin) * partialTick;
    }

    public float getPiston(float partialTick) {
        return prevPiston + (piston - prevPiston) * partialTick;
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        int[] ids = getFluidIdsToCopy();
        if (ids.length > 0) {
            tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, ids);
        }
        tag.putInt(TAG_LEGACY_COMPRESSION, inputTank.getPressure());
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        boolean changed = false;
        if (tag != null && tag.contains(TAG_LEGACY_COMPRESSION)) {
            int before = inputTank.getPressure();
            setInputPressure(tag.getInt(TAG_LEGACY_COMPRESSION));
            changed |= inputTank.getPressure() != before;
        }
        HbmFluidTank pasteTarget = getTankToPasteFluidSettings();
        if (pasteTarget != null && tag != null && tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            int[] ids = tag.getIntArray(HbmFluidCopiable.TAG_FLUID_IDS);
            if (ids.length > 0) {
                int safeIndex = index >= 0 && index < ids.length ? index : 0;
                FluidType before = pasteTarget.getTankType();
                pasteTarget.setTankType(HbmFluids.fromId(ids[safeIndex]));
                changed |= pasteTarget.getTankType() != before;
            }
        }
        setupOutputTank();
        if (changed) {
            onFluidContentsChanged();
        }
        return changed;
    }

    public long getPower() {
        return energy.getPower();
    }

    public long getMaxPower() {
        return energy.getMaxPower();
    }

    private boolean setInputTypeFromIdentifierSlot() {
        boolean changed = setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, inputTank);
        if (changed) {
            setupOutputTank();
        }
        return changed;
    }

    private boolean processRecipe() {
        processTime = HbmFluidCompressorRecipes.durationFor(inputTank.getTankType(), inputTank.getPressure());
        powerRequirement = BASE_POWER_REQUIREMENT;
        if (!canProcess()) {
            progress = 0;
            on = false;
            return false;
        }
        progress++;
        on = true;
        energy.usePower(powerRequirement);
        if (progress < processTime) {
            return true;
        }
        progress = 0;
        int inputAmount = HbmFluidCompressorRecipes.inputAmountFor(inputTank.getTankType(), inputTank.getPressure());
        HbmFluidStack output = HbmFluidCompressorRecipes.outputFor(inputTank.getTankType(), inputTank.getPressure());
        inputTank.drain(inputAmount, false);
        outputTank.fill(output.type(), output.amount(), output.pressure(), false);
        return true;
    }

    private boolean canProcess() {
        if (energy.getPower() <= powerRequirement || inputTank.getTankType() == HbmFluids.NONE) {
            return false;
        }
        int inputAmount = HbmFluidCompressorRecipes.inputAmountFor(inputTank.getTankType(), inputTank.getPressure());
        HbmFluidStack output = HbmFluidCompressorRecipes.outputFor(inputTank.getTankType(), inputTank.getPressure());
        return inputTank.getFill() >= inputAmount
                && outputTank.canAccept(output.type(), output.pressure())
                && outputTank.getFill() + output.amount() <= outputTank.getMaxFill();
    }

    private void setupOutputTank() {
        HbmFluidStack output = HbmFluidCompressorRecipes.outputFor(inputTank.getTankType(), inputTank.getPressure());
        outputTank.conform(new HbmFluidStack(output.type(), 0, output.pressure()));
    }

    private void setInputPressure(int pressure) {
        int clamped = HbmFluidTank.clampPressure(pressure);
        if (clamped != inputTank.getPressure()) {
            inputTank.withPressure(clamped);
            setupOutputTank();
            progress = 0;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void refreshFluidPorts() {
        HbmFluidPortMachine.refreshTransceiverPorts(level, worldPosition, getFluidPorts(),
                List.of(inputTank), List.of(outputTank), this);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return compressorFluidPortsForState();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return compressorEnergyPortsForState();
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
    protected java.util.List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(inputTank);
    }

    @Override
    protected java.util.List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(outputTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null ? HbmFluidSideMode.BOTH : HbmFluidSideMode.BOTH;
    }

    @Override
    protected int getInputPressure(@Nullable Direction side) {
        return inputTank.getPressure();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return type != null && type != HbmFluids.NONE && side != null;
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

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CompressorMenu(containerId, playerInventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_INPUT_PRESSURE
                && value >= 0
                && value <= 4
                && player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_INPUT_PRESSURE) {
            setInputPressure(value);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_INPUT_PRESSURE, inputTank.getPressure());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        progress = tag.getInt(TAG_PROGRESS);
        if (tag.contains(TAG_INPUT_PRESSURE)) {
            inputTank.withPressure(tag.getInt(TAG_INPUT_PRESSURE));
        }
        setupOutputTank();
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

    private List<FluidPort> compressorFluidPortsForState() {
        if (isCompact()) {
            return compressorCompactFluidPortsForState();
        }
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, 0, 2, 0), rot),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, 0, -2, 0), rot.getOpposite()),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, -2, 0, 0), facing.getOpposite()));
    }

    private List<EnergyPort> compressorEnergyPortsForState() {
        if (isCompact()) {
            return compressorCompactEnergyPortsForState();
        }
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, 0, 2, 0), rot),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, 0, -2, 0), rot.getOpposite()),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, -2, 0, 0), facing.getOpposite()));
    }

    private List<FluidPort> compressorCompactFluidPortsForState() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, 0, 4, 1), rot),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, 0, -4, 1), rot.getOpposite()),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, 2, -1, 1), facing),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, 2, 1, 1), facing),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, -2, -1, 1), facing.getOpposite()),
                new FluidPort(LegacyMultiblockOffsets.relative(facing, rot, -2, 1, 1), facing.getOpposite()));
    }

    private List<EnergyPort> compressorCompactEnergyPortsForState() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, 0, 4, 1), rot),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, 0, -4, 1), rot.getOpposite()),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, 2, -1, 1), facing),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, 2, 1, 1), facing),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, -2, -1, 1), facing.getOpposite()),
                new EnergyPort(LegacyMultiblockOffsets.relative(facing, rot, -2, 1, 1), facing.getOpposite()));
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private boolean isCompact() {
        return getBlockState().is(ModBlocks.MACHINE_COMPRESSOR_COMPACT.get());
    }

    @Override
    public float getVolume(float baseVolume) {
        return baseVolume;
    }

}
