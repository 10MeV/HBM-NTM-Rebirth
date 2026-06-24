package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.api.tile.ControlReceiver;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidThermalExchange;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait.CoolingType;
import com.hbm.ntm.menu.HeaterHeatexMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
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

public class HeaterHeatexBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmStandardFluidSender, HeatSource,
        ControlReceiver, RORValueProvider {
    public static final int SLOT_IDENTIFIER = 0;
    public static final int SLOT_COUNT = 1;
    public static final int TANK_CAPACITY = 24_000;

    private static final String TAG_HEAT = "heatEnergy";
    private static final String TAG_TO_COOL = "toCool";
    private static final String TAG_DELAY = "delay";
    private static final String TAG_INVENTORY = "Inventory";

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int amountToCool = TANK_CAPACITY;
    private int tickDelay = 1;
    private int heatEnergy;
    private int lastInputUsed;
    private int lastOutputProduced;

    public HeaterHeatexBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.COOLANT_HOT, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.COOLANT, TANK_CAPACITY));
    }

    private HeaterHeatexBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank outputTank) {
        super(ModBlockEntities.HEATER_HEATEX.get(), pos, state, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeaterHeatexBlockEntity heatex) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, heatex);

        int oldHeat = heatex.heatEnergy;
        int oldInputFill = heatex.inputTank.getFill();
        int oldOutputFill = heatex.outputTank.getFill();
        FluidType oldInputType = heatex.inputTank.getTankType();
        FluidType oldOutputType = heatex.outputTank.getTankType();
        int oldToCool = heatex.amountToCool;
        int oldDelay = heatex.tickDelay;

        boolean changed = heatex.setFluidTankTypeFromIdentifierSlot(heatex.items, SLOT_IDENTIFIER, heatex.inputTank);
        changed |= heatex.setupTanks();
        heatex.heatEnergy = Math.max(0, (int) (heatex.heatEnergy * 0.999D));
        changed |= heatex.tryConvert(level);

        if (heatex.outputTank.getFill() > 0) {
            heatex.tryProvideFluidToPorts(heatex.outputTank.getTankType(), heatex.outputTank.getPressure(), heatex);
        }

        changed |= oldHeat != heatex.heatEnergy
                || oldInputFill != heatex.inputTank.getFill()
                || oldOutputFill != heatex.outputTank.getFill()
                || oldInputType != heatex.inputTank.getTankType()
                || oldOutputType != heatex.outputTank.getTankType()
                || oldToCool != heatex.amountToCool
                || oldDelay != heatex.tickDelay;
        if (changed || level.getGameTime() % 20L == 0L) {
            heatex.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        heatex.networkPackNT(25);
    }

    private boolean setupTanks() {
        FluidType inputType = inputTank.getTankType();
        CoolableFluidTrait trait = inputType.getTrait(CoolableFluidTrait.class);
        FluidType targetInput = inputType;
        FluidType targetOutput = HbmFluids.NONE;
        if (trait != null && trait.getEfficiency(CoolingType.HEATEXCHANGER) > 0.0D) {
            targetOutput = trait.getCoolsTo();
        } else {
            targetInput = HbmFluids.NONE;
        }
        boolean changed = inputTank.getTankType() != targetInput || outputTank.getTankType() != targetOutput;
        inputTank.setTankType(targetInput);
        outputTank.setTankType(targetOutput);
        if (changed) {
            onFluidContentsChanged();
        }
        return changed;
    }

    private boolean tryConvert(Level level) {
        tickDelay = Math.max(tickDelay, 1);
        if (amountToCool < 1 || level.getGameTime() % tickDelay != 0L) {
            return false;
        }
        CoolableFluidTrait trait = inputTank.getTankType().getTrait(CoolableFluidTrait.class);
        if (trait == null || trait.getAmountRequired() <= 0) {
            return false;
        }
        int maxInputMb = Math.max(1, amountToCool) * trait.getAmountRequired();
        HbmFluidThermalExchange.ThermalResult result = HbmFluidThermalExchange.cool(
                inputTank, outputTank, CoolingType.HEATEXCHANGER, 1.0D, maxInputMb, false);
        lastInputUsed = result.inputUsed();
        lastOutputProduced = result.outputProduced();
        if (!result.converted()) {
            return false;
        }
        heatEnergy += (int) result.heatProduced();
        onFluidContentsChanged();
        return true;
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

    public int getAmountToCool() {
        return amountToCool;
    }

    public int getTickDelay() {
        return tickDelay;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int getLastInputUsed() {
        return lastInputUsed;
    }

    public int getLastOutputProduced() {
        return lastOutputProduced;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public void onIdentifierFluidChanged() {
        setupTanks();
        onFluidContentsChanged();
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        heatEnergy = Math.max(0, heatEnergy - Math.max(0, heat));
        setChanged();
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
        return type != HbmFluids.NONE && type == outputTank.getTankType() && outputTank.getFill() > 0;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        return HbmFluidPortLayouts.legacy(facing,
                HbmFluidPortLayouts.LegacyPort.of(2, 1, facing),
                HbmFluidPortLayouts.LegacyPort.of(2, -1, facing),
                HbmFluidPortLayouts.LegacyPort.of(-2, 1, facing.getOpposite()),
                HbmFluidPortLayouts.LegacyPort.of(-2, -1, facing.getOpposite()));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return canConnectFluidSide(side) ? List.of(inputTank) : List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return canConnectFluidSide(side) ? List.of(outputTank) : List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return canConnectFluidSide(side) ? HbmFluidSideMode.BOTH : HbmFluidSideMode.NONE;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(LegacyLookOverlayLines.heatTu(heatEnergy)));
    }

    @Override
    public CompoundTag getFluidSettings() {
        CompoundTag tag = new CompoundTag();
        int[] ids = getFluidIdsToCopy();
        if (ids.length > 0) {
            tag.putIntArray(HbmFluidCopiable.TAG_FLUID_IDS, ids);
        }
        tag.putInt(TAG_TO_COOL, amountToCool);
        return tag;
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        if (tag == null) {
            return false;
        }
        boolean changed = false;
        if (tag.contains(HbmFluidCopiable.TAG_FLUID_IDS)) {
            int[] ids = tag.getIntArray(HbmFluidCopiable.TAG_FLUID_IDS);
            if (ids.length > 0) {
                int safeIndex = index >= 0 && index < ids.length ? index : 0;
                inputTank.setTankType(HbmFluids.fromId(ids[safeIndex]));
                changed = true;
            }
        }
        if (tag.contains(TAG_TO_COOL)) {
            amountToCool = Mth.clamp(tag.getInt(TAG_TO_COOL), 1, inputTank.getMaxFill());
            changed = true;
        }
        if (changed) {
            setupTanks();
            onFluidSettingsPasted();
        }
        return changed;
    }

    @Override
    public boolean hasPermission(Player player) {
        return player != null && !isRemoved()
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) < 256.0D;
    }

    @Override
    public void receiveControl(CompoundTag data) {
        boolean changed = false;
        if (data.contains(TAG_TO_COOL)) {
            amountToCool = Mth.clamp(data.getInt(TAG_TO_COOL), 1, inputTank.getMaxFill());
            changed = true;
        }
        if (data.contains(TAG_DELAY)) {
            tickDelay = Math.max(data.getInt(TAG_DELAY), 1);
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    @Override
    public void receiveControl(Player player, CompoundTag data) {
        if (hasPermission(player)) {
            receiveControl(data);
        }
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return hasPermission(player);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        receiveControl(player, tag);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                RORInfo.PREFIX_VALUE + "hotfluid",
                RORInfo.PREFIX_VALUE + "coldfluid",
                RORInfo.PREFIX_VALUE + "heat"
        };
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "hotfluid").equals(name)) {
            return Integer.toString(inputTank.getFill());
        }
        if ((RORInfo.PREFIX_VALUE + "coldfluid").equals(name)) {
            return Integer.toString(outputTank.getFill());
        }
        if ((RORInfo.PREFIX_VALUE + "heat").equals(name)) {
            return Integer.toString(heatEnergy);
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.heaterHeatex", "Heat Exchanging Heater");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new HeaterHeatexMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        inputTank.writeToNbt(tag, "0");
        outputTank.writeToNbt(tag, "1");
        tag.putInt(TAG_HEAT, heatEnergy);
        tag.putInt(TAG_TO_COOL, amountToCool);
        tag.putInt(TAG_DELAY, tickDelay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItems(tag);
        if (tag.contains("0_type") || tag.contains("0_type_id") || tag.contains("0")) {
            inputTank.readFromNbt(tag, "0");
        }
        if (tag.contains("1_type") || tag.contains("1_type_id") || tag.contains("1")) {
            outputTank.readFromNbt(tag, "1");
        }
        heatEnergy = Math.max(0, tag.getInt(TAG_HEAT));
        amountToCool = Mth.clamp(tag.contains(TAG_TO_COOL) ? tag.getInt(TAG_TO_COOL) : TANK_CAPACITY,
                1, inputTank.getMaxFill());
        tickDelay = Math.max(tag.contains(TAG_DELAY) ? tag.getInt(TAG_DELAY) : 1, 1);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("lastInputUsed", lastInputUsed);
        tag.putInt("lastOutputProduced", lastOutputProduced);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        lastInputUsed = Math.max(0, tag.getInt("lastInputUsed"));
        lastOutputProduced = Math.max(0, tag.getInt("lastOutputProduced"));
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

    private boolean canConnectFluidSide(@Nullable Direction side) {
        if (side == null) {
            return true;
        }
        Direction facing = facing();
        return side == facing || side == facing.getOpposite();
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private void loadItems(CompoundTag tag) {
        if (tag.contains(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, Tag.TAG_LIST)
                || tag.contains("Items", Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
    }
}
