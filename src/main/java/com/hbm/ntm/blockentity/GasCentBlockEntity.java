package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.GasCentMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import net.minecraft.world.item.Item;
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
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GasCentBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver {
    public static final int SLOT_OUTPUT_0 = 0;
    public static final int SLOT_OUTPUT_1 = 1;
    public static final int SLOT_OUTPUT_2 = 2;
    public static final int SLOT_OUTPUT_3 = 3;
    public static final int SLOT_BATTERY = 4;
    public static final int SLOT_IDENTIFIER = 5;
    public static final int SLOT_UPGRADE = 6;
    public static final int SLOT_COUNT = 7;

    private static final long MAX_POWER = 100_000L;
    private static final int REAL_TANK_CAPACITY = 2_000;
    private static final int PSEUDO_TANK_CAPACITY = 8_000;
    private static final int NORMAL_PROCESSING_SPEED = 150;
    private static final int SPEED_UPGRADE_PROCESSING_SPEED = 80;
    private static final long NORMAL_CONSUMPTION = 200L;
    private static final long SPEED_UPGRADE_CONSUMPTION = 300L;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_CUSTOM_NAME = "name";

    private static final List<FluidPort> PORTS = List.of(
            FluidPort.of(0, -1, 0, Direction.DOWN),
            FluidPort.of(1, 0, 0, Direction.EAST),
            FluidPort.of(-1, 0, 0, Direction.WEST),
            FluidPort.of(0, 0, 1, Direction.SOUTH),
            FluidPort.of(0, 0, -1, Direction.NORTH));

    private final HbmFluidTank tank;
    private final PseudoFluidTank inputTank = new PseudoFluidTank(PseudoFluidType.NUF6, PSEUDO_TANK_CAPACITY);
    private final PseudoFluidTank outputTank = new PseudoFluidTank(PseudoFluidType.LEUF6, PSEUDO_TANK_CAPACITY);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isLegacyBatteryItem(stack);
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_UPGRADE -> isSpeedUpgrade(stack);
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private int progress;
    private boolean isProgressing;
    private int audioDuration;
    private Object audioLoop;
    @Nullable
    private String customName;

    public GasCentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_CENT.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(new HbmFluidTank(HbmFluids.UF6, REAL_TANK_CAPACITY)));
        this.tank = getAllTanks().get(0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasCentBlockEntity gasCent) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, gasCent);

        long oldPower = gasCent.energy.getPower();
        int oldProgress = gasCent.progress;
        boolean oldProgressing = gasCent.isProgressing;
        int oldTankFill = gasCent.tank.getFill();
        FluidType oldTankType = gasCent.tank.getTankType();
        PseudoFluidType oldInputType = gasCent.inputTank.getTankType();
        PseudoFluidType oldOutputType = gasCent.outputTank.getTankType();
        int oldInputFill = gasCent.inputTank.getFill();
        int oldOutputFill = gasCent.outputTank.getFill();

        HbmEnergyUtil.chargeStorageFromItem(gasCent.items.getStackInSlot(SLOT_BATTERY),
                gasCent.energy, gasCent.energy.getReceiverSpeed());
        gasCent.updateRealTankTypeFromIdentifier();
        if (PseudoFluidType.fromRealFluid(gasCent.tank.getTankType()) == gasCent.inputTank.getTankType()) {
            gasCent.attemptConversion();
        }

        if (gasCent.canEnrich()) {
            gasCent.isProgressing = true;
            gasCent.progress++;
            gasCent.energy.setPower(gasCent.energy.getPower() - gasCent.getConsumption());
            if (gasCent.energy.getPower() < 0L) {
                gasCent.energy.setPower(0L);
                gasCent.progress = 0;
            }
            if (gasCent.progress >= gasCent.getProcessingSpeed()) {
                gasCent.enrich();
            }
        } else {
            gasCent.isProgressing = false;
            gasCent.progress = 0;
        }

        if (level.getGameTime() % 10L == 0L) {
            BlockEntity behind = level.getBlockEntity(pos.relative(gasCent.facing().getOpposite()));
            if (!gasCent.attemptTransfer(behind) && gasCent.inputTank.getTankType() == PseudoFluidType.LEUF6) {
                gasCent.convertLeuf6Remainder();
            }
        }

        boolean changed = oldPower != gasCent.energy.getPower()
                || oldProgress != gasCent.progress
                || oldProgressing != gasCent.isProgressing
                || oldTankFill != gasCent.tank.getFill()
                || oldTankType != gasCent.tank.getTankType()
                || oldInputType != gasCent.inputTank.getTankType()
                || oldOutputType != gasCent.outputTank.getTankType()
                || oldInputFill != gasCent.inputTank.getFill()
                || oldOutputFill != gasCent.outputTank.getFill();
        gasCent.networkPackNT(50);
        if (changed) {
            gasCent.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GasCentBlockEntity gasCent) {
        if (gasCent.isProgressing) {
            gasCent.audioDuration += 2;
        } else {
            gasCent.audioDuration -= 3;
        }
        gasCent.audioDuration = Math.max(0, Math.min(60, gasCent.audioDuration));
        float pitch = (gasCent.audioDuration - 10) / 100.0F + 0.5F;
        gasCent.audioLoop = LegacyMachineAudioBridge.updateLoop(gasCent.audioLoop, gasCent,
                "hbm:block.centrifugeOperate", gasCent.audioDuration > 10, 25.0D, 20.0F, 1.0F, pitch);
    }

    private void updateRealTankTypeFromIdentifier() {
        ItemStack stack = items.getStackInSlot(SLOT_IDENTIFIER);
        if (!(stack.getItem() instanceof IFluidIdentifierItem identifier)) {
            return;
        }
        FluidType newType = identifier.getIdentifiedFluid(level, worldPosition, stack);
        PseudoFluidType pseudo = PseudoFluidType.fromRealFluid(newType);
        if (pseudo == null || tank.getTankType() == newType) {
            return;
        }
        inputTank.setTankType(pseudo);
        outputTank.setTankType(pseudo.getOutputType());
        tank.setTankType(newType);
        onFluidContentsChanged();
    }

    private boolean canEnrich() {
        PseudoFluidType type = inputTank.getTankType();
        if (energy.getPower() <= 0L || inputTank.getFill() < type.getFluidConsumed()
                || outputTank.getFill() + type.getFluidProduced() > outputTank.getMaxFill()) {
            return false;
        }
        if (type.requiresHighSpeed() && !hasSpeedUpgrade()) {
            return false;
        }
        List<ItemStack> outputs = type.createOutputs();
        return !outputs.isEmpty() && canFitOutputs(outputs);
    }

    private void enrich() {
        PseudoFluidType type = inputTank.getTankType();
        List<ItemStack> outputs = type.createOutputs();
        progress = 0;
        inputTank.setFill(inputTank.getFill() - type.getFluidConsumed());
        outputTank.setFill(outputTank.getFill() + type.getFluidProduced());
        mergeOutputs(outputs);
    }

    private void attemptConversion() {
        if (inputTank.getFill() >= inputTank.getMaxFill() || tank.getFill() <= 0) {
            return;
        }
        int fill = Math.min(inputTank.getMaxFill() - inputTank.getFill(), tank.getFill());
        tank.setFill(tank.getFill() - fill);
        inputTank.setFill(inputTank.getFill() + fill);
        onFluidContentsChanged();
    }

    private boolean attemptTransfer(BlockEntity blockEntity) {
        if (!(blockEntity instanceof GasCentBlockEntity other) || other.tank.getTankType() != tank.getTankType()) {
            return false;
        }
        if (other.inputTank.getTankType() != outputTank.getTankType()
                && outputTank.getTankType() != PseudoFluidType.NONE) {
            other.inputTank.setTankType(outputTank.getTankType());
            other.outputTank.setTankType(outputTank.getTankType().getOutputType());
        }
        if (other.inputTank.getFill() < other.inputTank.getMaxFill() && outputTank.getFill() > 0) {
            int fill = Math.min(other.inputTank.getMaxFill() - other.inputTank.getFill(), outputTank.getFill());
            outputTank.setFill(outputTank.getFill() - fill);
            other.inputTank.setFill(other.inputTank.getFill() + fill);
            other.setChanged();
            if (other.level != null && !other.level.isClientSide) {
                other.level.sendBlockUpdated(other.worldPosition, other.getBlockState(), other.getBlockState(),
                        Block.UPDATE_CLIENTS);
            }
        }
        return true;
    }

    private void convertLeuf6Remainder() {
        if (outputTank.getFill() < 600) {
            return;
        }
        List<ItemStack> converted = List.of(
                legacyStack("nugget_uranium_fuel", 6),
                legacyStack("fluorite", 1));
        if (!canFitOutputs(converted)) {
            return;
        }
        outputTank.setFill(outputTank.getFill() - 600);
        mergeOutputs(converted);
    }

    private boolean canFitOutputs(List<ItemStack> outputs) {
        ItemStack[] simulated = new ItemStack[] {
                items.getStackInSlot(SLOT_OUTPUT_0).copy(),
                items.getStackInSlot(SLOT_OUTPUT_1).copy(),
                items.getStackInSlot(SLOT_OUTPUT_2).copy(),
                items.getStackInSlot(SLOT_OUTPUT_3).copy()
        };
        for (ItemStack output : outputs) {
            if (output.isEmpty()) {
                continue;
            }
            ItemStack remaining = output.copy();
            for (int i = 0; i < simulated.length && !remaining.isEmpty(); i++) {
                ItemStack existing = simulated[i];
                if (existing.isEmpty()) {
                    simulated[i] = remaining.copy();
                    remaining = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameTags(existing, remaining)) {
                    int move = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    if (move > 0) {
                        existing.grow(move);
                        remaining.shrink(move);
                    }
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void mergeOutputs(List<ItemStack> outputs) {
        for (ItemStack output : outputs) {
            ItemStack remaining = output.copy();
            for (int slot = SLOT_OUTPUT_0; slot <= SLOT_OUTPUT_3 && !remaining.isEmpty(); slot++) {
                ItemStack existing = items.getStackInSlot(slot);
                if (existing.isEmpty()) {
                    items.setStackInSlot(slot, remaining.copy());
                    remaining = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameTags(existing, remaining)) {
                    int move = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    if (move > 0) {
                        existing.grow(move);
                        items.setStackInSlot(slot, existing);
                        remaining.shrink(move);
                    }
                }
            }
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public PseudoFluidTank getInputPseudoTank() {
        return inputTank;
    }

    public PseudoFluidTank getOutputPseudoTank() {
        return outputTank;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessingSpeed() {
        return hasSpeedUpgrade() ? SPEED_UPGRADE_PROCESSING_SPEED : NORMAL_PROCESSING_SPEED;
    }

    public long getConsumption() {
        return hasSpeedUpgrade() ? SPEED_UPGRADE_CONSUMPTION : NORMAL_CONSUMPTION;
    }

    public boolean isProgressing() {
        return isProgressing;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private boolean hasSpeedUpgrade() {
        return isSpeedUpgrade(items.getStackInSlot(SLOT_UPGRADE));
    }

    private static boolean isSpeedUpgrade(ItemStack stack) {
        return stack.getItem() instanceof ItemMachineUpgrade upgrade
                && upgrade.getUpgradeType() == UpgradeType.SPEED;
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
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
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == tank.getTankType() && PseudoFluidType.fromRealFluid(type) == inputTank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return PORTS;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return PORTS.stream()
                .map(port -> EnergyPort.of(port.offset().getX(), port.offset().getY(), port.offset().getZ(),
                        port.direction()))
                .toList();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return tank.getTankType() != HbmFluids.NONE;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(tank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null || side != Direction.UP ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == null || side != Direction.UP ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.gasCentrifuge", "Gas Centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GasCentMenu(containerId, inventory, this);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, progress > 0);
        data.putInt(CompatEnergyControl.I_PROGRESS, progress);
        data.putDouble(CompatEnergyControl.D_PROCESS_TIME, getProcessingSpeed());
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_GAS_CENT_FEED, tank);
        data.putString("gasCentInputType", inputTank.getTankType().legacyName());
        data.putInt("gasCentInput", inputTank.getFill());
        data.putInt("gasCentInputMax", inputTank.getMaxFill());
        data.putString("gasCentOutputType", outputTank.getTankType().legacyName());
        data.putInt("gasCentOutput", outputTank.getFill());
        data.putInt("gasCentOutputMax", outputTank.getMaxFill());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        tag.putLong("power", energy.getPower());
        tag.putShort("progress", (short) progress);
        tank.writeToNbt(tag, "tank");
        inputTank.writeToNbt(tag, "inputTank");
        outputTank.writeToNbt(tag, "outputTank");
        tag.putBoolean("isProgressing", isProgressing);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        progress = tag.getShort("progress");
        if (tag.contains("tank") || tag.contains("tank_type") || tag.contains("tank_type_id")) {
            tank.readFromNbt(tag, "tank");
        }
        inputTank.readFromNbt(tag, "inputTank");
        outputTank.readFromNbt(tag, "outputTank");
        isProgressing = tag.getBoolean("isProgressing");
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        } else if (tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
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

    public enum PseudoFluidType {
        NONE("NONE", 0, 0, null, false),
        HEUF6("HEUF6", 300, 0, NONE, true, output("nugget_u238", 2), output("nugget_u235", 1),
                output("fluorite", 1)),
        MEUF6("MEUF6", 200, 100, HEUF6, false, output("nugget_u238", 1)),
        LEUF6("LEUF6", 300, 200, MEUF6, false, output("nugget_u238", 1), output("fluorite", 1)),
        NUF6("NUF6", 400, 300, LEUF6, false, output("nugget_u238", 1)),
        PF6("PF6", 300, 0, NONE, false, output("nugget_pu238", 1), output("nugget_pu_mix", 2),
                output("fluorite", 1)),
        MUD_HEAVY("MUD_HEAVY", 500, 0, NONE, false, output("powder_iron", 1), output("dust", 1),
                output("nuclear_waste_tiny", 1)),
        MUD("MUD", 1000, 500, MUD_HEAVY, false, output("powder_lead", 1), output("dust", 1));

        private final String legacyName;
        private final int fluidConsumed;
        private final int fluidProduced;
        private final PseudoFluidType outputType;
        private final boolean highSpeed;
        private final List<OutputEntry> outputs;

        PseudoFluidType(String legacyName, int fluidConsumed, int fluidProduced, @Nullable PseudoFluidType outputType,
                boolean highSpeed, OutputEntry... outputs) {
            this.legacyName = legacyName;
            this.fluidConsumed = fluidConsumed;
            this.fluidProduced = fluidProduced;
            this.outputType = outputType;
            this.highSpeed = highSpeed;
            this.outputs = outputs == null ? List.of() : List.of(outputs);
        }

        public int getFluidConsumed() {
            return fluidConsumed;
        }

        public int getFluidProduced() {
            return fluidProduced;
        }

        public PseudoFluidType getOutputType() {
            return outputType == null ? NONE : outputType;
        }

        public boolean requiresHighSpeed() {
            return highSpeed;
        }

        public String legacyName() {
            return legacyName;
        }

        public String translationKey() {
            return "hbmpseudofluid." + legacyName.toLowerCase(Locale.US);
        }

        public String displayName() {
            return Component.translatableWithFallback(translationKey(), legacyName).getString();
        }

        public List<ItemStack> createOutputs() {
            List<ItemStack> stacks = new ArrayList<>();
            for (OutputEntry output : outputs) {
                ItemStack stack = output.createStack();
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
            return stacks;
        }

        @Nullable
        public static PseudoFluidType fromRealFluid(FluidType type) {
            if (type == HbmFluids.UF6) {
                return NUF6;
            }
            if (type == HbmFluids.PUF6) {
                return PF6;
            }
            if (type == HbmFluids.WATZ) {
                return MUD;
            }
            return null;
        }

        public static PseudoFluidType fromLegacyName(String name) {
            if (name == null || name.isBlank()) {
                return NONE;
            }
            for (PseudoFluidType type : values()) {
                if (type.legacyName.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return NONE;
        }
    }

    public static final class PseudoFluidTank {
        private PseudoFluidType type;
        private int fill;
        private int maxFill;

        private PseudoFluidTank(PseudoFluidType type, int maxFill) {
            this.type = type == null ? PseudoFluidType.NONE : type;
            this.maxFill = Math.max(0, maxFill);
        }

        public PseudoFluidType getTankType() {
            return type;
        }

        public void setTankType(PseudoFluidType type) {
            PseudoFluidType newType = type == null ? PseudoFluidType.NONE : type;
            if (this.type == newType) {
                return;
            }
            this.type = newType;
            setFill(0);
        }

        public int getFill() {
            return fill;
        }

        public void setFill(int fill) {
            this.fill = Math.max(0, Math.min(fill, maxFill));
        }

        public int getMaxFill() {
            return maxFill;
        }

        private void writeToNbt(CompoundTag tag, String key) {
            tag.putInt(key, fill);
            tag.putInt(key + "_max", maxFill);
            tag.putString(key + "_type", type.legacyName());
        }

        private void readFromNbt(CompoundTag tag, String key) {
            if (!tag.contains(key) && !tag.contains(key + "_type")) {
                return;
            }
            fill = Math.max(0, tag.getInt(key));
            int savedMax = tag.getInt(key + "_max");
            if (savedMax > 0) {
                maxFill = savedMax;
            }
            fill = Math.min(fill, maxFill);
            type = PseudoFluidType.fromLegacyName(tag.getString(key + "_type"));
        }
    }

    private record OutputEntry(String legacyItemName, int count) {
        private ItemStack createStack() {
            return legacyStack(legacyItemName, count);
        }
    }

    private static OutputEntry output(String legacyName, int count) {
        return new OutputEntry(legacyName, count);
    }

    private static ItemStack legacyStack(String legacyName, int count) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 4;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= SLOT_OUTPUT_0 && slot <= SLOT_OUTPUT_3 ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= SLOT_OUTPUT_0 && slot <= SLOT_OUTPUT_3
                    ? items.extractItem(slot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= SLOT_OUTPUT_0 && slot <= SLOT_OUTPUT_3 ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
