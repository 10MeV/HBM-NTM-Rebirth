package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.InfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.menu.RadiolysisMenu;
import com.hbm.ntm.recipe.RadiolysisRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.RtgPelletRuntime;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadiolysisBlockEntity extends HbmFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmEnergyProvider, InfoProviderEC {
    public static final int SLOT_RTG_START = 0;
    public static final int SLOT_RTG_END = 9;
    public static final int SLOT_FLUID_ID_INPUT = 10;
    public static final int SLOT_FLUID_ID_OUTPUT = 11;
    public static final int SLOT_STERILIZE_INPUT = 12;
    public static final int SLOT_STERILIZE_OUTPUT = 13;
    public static final int SLOT_BATTERY = 14;
    public static final int SLOT_COUNT = 15;

    public static final long MAX_POWER = 1_000_000L;
    private static final int TANK_CAPACITY = 2_000;
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_POWER = "power";
    private static final String TAG_HEAT = "heat";
    private static final String CONTAGION_TAG = "ntmContagion";
    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank1;
    private final HbmFluidTank outputTank2;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isLegacyManualInputSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler(items));
    private final LazyOptional<IEnergyStorage> energyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(this, false, true));

    private long power;
    private int heat;

    public RadiolysisBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private RadiolysisBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank outputTank1, HbmFluidTank outputTank2) {
        super(ModBlockEntities.RADIOLYSIS.get(), pos, state, List.of(inputTank, outputTank1, outputTank2));
        this.inputTank = inputTank;
        this.outputTank1 = outputTank1;
        this.outputTank2 = outputTank2;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadiolysisBlockEntity radiolysis) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = radiolysis.tickServer(level, pos, state);
        radiolysis.networkPackNT(50);
        if (changed) {
            radiolysis.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RadiolysisBlockEntity radiolysis) {
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank1() {
        return outputTank1;
    }

    public HbmFluidTank getOutputTank2() {
        return outputTank2;
    }

    public int getHeat() {
        return heat;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.radiolysis", "Radiolysis Chamber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadiolysisMenu(containerId, inventory, this);
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = Math.max(0L, Math.min(MAX_POWER, power));
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public long getProviderSpeed() {
        return Math.max(0L, heat * 10L);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, heat * 10L);
        data.putInt("heat", heat);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_RADIOLYSIS_INPUT, inputTank);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_RADIOLYSIS_OUTPUT_1, outputTank1);
        CompatEnergyControl.putTypedTankInfo(data, CompatEnergyControl.S_RADIOLYSIS_OUTPUT_2, outputTank2);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank1, outputTank2);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return getReceivingTanks();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == Direction.DOWN ? HbmFluidSideMode.NONE : HbmFluidSideMode.BOTH;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts();
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return side == Direction.DOWN ? LazyOptional.empty() : energyHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putLong(TAG_POWER, power);
        tag.putInt(TAG_HEAT, heat);
        inputTank.writeToNbt(tag, "input");
        outputTank1.writeToNbt(tag, "output1");
        outputTank2.writeToNbt(tag, "output2");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        setPower(tag.getLong(TAG_POWER));
        heat = tag.getInt(TAG_HEAT);
        if (tag.contains("input") || tag.contains("input_type")) {
            inputTank.readFromNbt(tag, "input");
        }
        if (tag.contains("output1") || tag.contains("output1_type")) {
            outputTank1.readFromNbt(tag, "output1");
        }
        if (tag.contains("output2") || tag.contains("output2_type")) {
            outputTank2.readFromNbt(tag, "output2");
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 3, 2));
    }

    private boolean tickServer(Level level, BlockPos pos, BlockState state) {
        long oldPower = power;
        int oldHeat = heat;
        int oldInputFill = inputTank.getFill();
        int oldOutput1Fill = outputTank1.getFill();
        int oldOutput2Fill = outputTank2.getFill();
        FluidType oldInputType = inputTank.getTankType();
        FluidType oldOutput1Type = outputTank1.getTankType();
        FluidType oldOutput2Type = outputTank2.getTankType();

        HbmEnergyUtil.chargeItemFromStorage(items.getStackInSlot(SLOT_BATTERY), this, getMaxPower());
        heat = calculateHeat();
        setPower(power + heat * 10L);

        setFluidTankTypeFromIdentifierSlot(items, SLOT_FLUID_ID_INPUT, SLOT_FLUID_ID_OUTPUT, inputTank);
        setupTanks(level);

        if (heat > 100) {
            int crackTime = (int) Math.max(-0.1D * (heat - 100) + 30.0D, 5.0D);
            if (level.getGameTime() % crackTime == 0L) {
                crack(level);
            }
            if (heat >= 200 && level.getGameTime() % 100L == 0L) {
                sterilize();
            }
        }

        refreshTrackedTransceiverFluidPortsReport(getReceivingTanks(), getSendingTanks(), this);
        if (outputTank1.getFill() > 0) {
            tryProvideFluidToPorts(outputTank1.getTankType(), outputTank1.getPressure(), this);
        }
        if (outputTank2.getFill() > 0) {
            tryProvideFluidToPorts(outputTank2.getTankType(), outputTank2.getPressure(), this);
        }
        if (power > 0L) {
            HbmEnergyUtil.tryProvideToPorts(level, pos, energyPorts(), this);
        }

        return oldPower != power
                || oldHeat != heat
                || oldInputFill != inputTank.getFill()
                || oldOutput1Fill != outputTank1.getFill()
                || oldOutput2Fill != outputTank2.getFill()
                || oldInputType != inputTank.getTankType()
                || oldOutput1Type != outputTank1.getTankType()
                || oldOutput2Type != outputTank2.getTankType();
    }

    private int calculateHeat() {
        return RtgPelletRuntime.updateHeat(items, SLOT_RTG_START, SLOT_RTG_END);
    }

    private void setupTanks(Level level) {
        RadiolysisRecipes.Result result = RadiolysisRecipes.getRadiolysis(level.getRecipeManager(),
                inputTank.getTankType());
        if (result == null) {
            inputTank.setTankType(HbmFluids.NONE);
            outputTank1.setTankType(HbmFluids.NONE);
            outputTank2.setTankType(HbmFluids.NONE);
            return;
        }
        outputTank1.setTankType(result.left().type());
        outputTank2.setTankType(result.right().type());
    }

    private void crack(Level level) {
        RadiolysisRecipes.Result result = RadiolysisRecipes.getRadiolysis(level.getRecipeManager(),
                inputTank.getTankType());
        if (result == null || inputTank.getFill() < 100 || !hasSpace(result.left(), result.right())) {
            return;
        }
        inputTank.drain(100, false);
        outputTank1.setFill(outputTank1.getFill() + result.left().amount());
        outputTank2.setFill(outputTank2.getFill() + result.right().amount());
        onFluidContentsChanged();
    }

    private boolean hasSpace(HbmFluidStack left, HbmFluidStack right) {
        return outputTank1.getTankType() == left.type()
                && outputTank2.getTankType() == right.type()
                && outputTank1.getFill() + left.amount() <= outputTank1.getMaxFill()
                && outputTank2.getFill() + right.amount() <= outputTank2.getMaxFill();
    }

    private void sterilize() {
        ItemStack input = items.getStackInSlot(SLOT_STERILIZE_INPUT);
        if (input.isEmpty()) {
            return;
        }
        if (input.getFoodProperties(null) != null && !isPancake(input)) {
            items.extractItem(SLOT_STERILIZE_INPUT, 1, false);
            return;
        }
        if (!hasContagion(input)) {
            return;
        }
        ItemStack output = cleanContagion(input.copyWithCount(1));
        ItemStack existing = items.getStackInSlot(SLOT_STERILIZE_OUTPUT);
        if (existing.isEmpty()) {
            items.extractItem(SLOT_STERILIZE_INPUT, 1, false);
            items.setStackInSlot(SLOT_STERILIZE_OUTPUT, output);
        } else if (HbmItemStackUtil.doesStackDataMatch(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize()) {
            items.extractItem(SLOT_STERILIZE_INPUT, 1, false);
            existing.grow(1);
            items.setStackInSlot(SLOT_STERILIZE_OUTPUT, existing);
        }
    }

    private static boolean hasContagion(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(CONTAGION_TAG);
    }

    private static ItemStack cleanContagion(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag().copy();
            tag.remove(CONTAGION_TAG);
            stack.setTag(tag.isEmpty() ? null : tag);
        }
        return stack;
    }

    private static boolean isPancake(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "pancake".equals(id.getPath());
    }

    private static int rtgHeat(ItemStack stack) {
        return RtgPelletRuntime.heat(stack);
    }

    private static boolean isDepletedRtg(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getPath().startsWith("pellet_rtg_depleted");
    }

    private static List<FluidPort> fluidPorts() {
        return List.of(
                FluidPort.of(2, 0, 0, Direction.EAST),
                FluidPort.of(-2, 0, 0, Direction.WEST),
                FluidPort.of(0, 0, 2, Direction.SOUTH),
                FluidPort.of(0, 0, -2, Direction.NORTH));
    }

    private static List<EnergyPort> energyPorts() {
        return List.of(
                EnergyPort.of(2, 0, 0, Direction.EAST),
                EnergyPort.of(-2, 0, 0, Direction.WEST),
                EnergyPort.of(0, 0, 2, Direction.SOUTH),
                EnergyPort.of(0, 0, -2, Direction.NORTH));
    }

    private static final class AccessibleItemHandler implements IItemHandler {
        private static final int[] SLOT_IO = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 13};
        private final IItemHandlerModifiable items;

        private AccessibleItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return SLOT_IO.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            if ((mapped >= SLOT_RTG_START && mapped <= SLOT_RTG_END && rtgHeat(stack) > 0)
                    || mapped == SLOT_STERILIZE_INPUT) {
                return items.insertItem(mapped, stack, simulate);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            if (mapped == SLOT_STERILIZE_OUTPUT
                    || (mapped >= SLOT_RTG_START && mapped <= SLOT_RTG_END
                    && isDepletedRtg(items.getStackInSlot(mapped)))) {
                return items.extractItem(mapped, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return (mapped >= SLOT_RTG_START && mapped <= SLOT_RTG_END && rtgHeat(stack) > 0)
                    || mapped == SLOT_STERILIZE_INPUT;
        }

        private int map(int slot) {
            return slot >= 0 && slot < SLOT_IO.length ? SLOT_IO[slot] : -1;
        }
    }

    private static boolean isLegacyManualInputSlot(int slot) {
        return (slot >= SLOT_RTG_START && slot <= SLOT_RTG_END)
                || slot == SLOT_FLUID_ID_INPUT
                || slot == SLOT_STERILIZE_INPUT
                || slot == SLOT_BATTERY;
    }
}
