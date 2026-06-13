package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.neutron.RBMKAbsorberColumn;
import com.hbm.ntm.neutron.RBMKControlColumn;
import com.hbm.ntm.neutron.RBMKControlRodPlanner;
import com.hbm.ntm.neutron.RBMKControlState;
import com.hbm.ntm.neutron.RBMKFuelColumnRuntime;
import com.hbm.ntm.neutron.RBMKFuelRodColumnPlanner;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.neutron.RBMKNeutronColumn;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.neutron.RBMKReaSimRodColumn;
import com.hbm.ntm.neutron.RBMKRodColumn;
import com.hbm.ntm.neutron.RBMKRodFluxState;
import com.hbm.ntm.neutron.RBMKRuntimeSettings;
import com.hbm.ntm.neutron.RBMKThermalState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RBMKColumnBlockEntity extends BlockEntity
        implements RBMKNeutronColumn, RBMKAbsorberColumn, RBMKControlColumn, RBMKRodColumn {
    public static final String TAG_HEAT = "heat";
    public static final String TAG_REASIM_WATER = "reasimWater";
    public static final String TAG_REASIM_STEAM = "reasimSteam";
    public static final String TAG_FUEL_ROD = "rod";
    public static final String TAG_STORAGE_ITEMS = "items";
    public static final String TAG_STORAGE_SLOT = "slot";
    public static final String TAG_STARTING_LEVEL = "startingLevel";
    public static final String TAG_COLOR = "color";
    public static final String TAG_LEVEL_LOWER = "levelLower";
    public static final String TAG_LEVEL_UPPER = "levelUpper";
    public static final String TAG_HEAT_LOWER = "heatLower";
    public static final String TAG_HEAT_UPPER = "heatUpper";
    public static final String TAG_FUNCTION = "function";

    private double heat = 20.0D;
    private int reasimWater;
    private int reasimSteam;
    private ItemStack fuelRod = ItemStack.EMPTY;
    private final RBMKRodFluxState rodFluxState = new RBMKRodFluxState();
    private final RBMKControlState controlState = new RBMKControlState();
    private final ItemStackHandler storageItems = new ItemStackHandler(12) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof RBMKFuelRodItem;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> storageItemHandler = LazyOptional.of(() -> storageItems);
    private double startingLevel;
    @Nullable
    private RBMKControlRodPlanner.RBMKColor color;
    private double levelLower;
    private double levelUpper;
    private double heatLower;
    private double heatUpper;
    private RBMKControlRodPlanner.RBMKFunction function = RBMKControlRodPlanner.RBMKFunction.LINEAR;

    public RBMKColumnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RBMK_COLUMN.get(), pos, state);
    }

    public static RBMKColumnBlockEntity create(BlockPos pos, BlockState state) {
        return state.getBlock() instanceof RBMKColumnBlock column && column.kind().reasim()
                ? new ReaSim(pos, state)
                : new RBMKColumnBlockEntity(pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKColumnBlockEntity blockEntity) {
        if (blockEntity.heat < 20.0D) {
            blockEntity.heat = 20.0D;
            blockEntity.setChanged();
        }
        blockEntity.tickFuelRod();
        blockEntity.tickControl();
        blockEntity.tickStorage(level);
    }

    @Override
    public RBMKNeutronHandler.RBMKType getRBMKType() {
        return kind().rbmkType();
    }

    @Override
    public boolean hasRBMKLid() {
        BlockState state = getBlockState();
        return state.getBlock() instanceof RBMKColumnBlock && state.hasProperty(RBMKColumnBlock.LID)
                && state.getValue(RBMKColumnBlock.LID).hasLid();
    }

    @Override
    public boolean isRBMKModerated() {
        return kind().moderated();
    }

    @Override
    public void addAbsorberHeat(double heat) {
        if (getRBMKType() != RBMKNeutronHandler.RBMKType.ABSORBER) {
            return;
        }
        this.heat += heat;
        setChanged();
    }

    @Override
    public boolean hasFuelRod() {
        return kind().rod() && !fuelRod.isEmpty() && fuelRod.getItem() instanceof RBMKFuelRodItem;
    }

    @Override
    public double lastFluxQuantity() {
        return rodFluxState.lastFluxQuantity();
    }

    @Override
    public void receiveFlux(RBMKNeutronHandler.RBMKNeutronStream stream) {
        if (kind().rod()) {
            rodFluxState.receiveFlux(stream);
            setChanged();
        }
    }

    @Override
    public double controlLevel() {
        return kind().control() ? controlState.level() : 0.0D;
    }

    @Override
    public double controlMultiplier() {
        RBMKColumnBlock.Kind kind = kind();
        if (!kind.control()) {
            return 0.0D;
        }
        if (kind.automatic()) {
            return controlState.level();
        }
        return RBMKControlRodPlanner.manualMultiplier(controlState, startingLevel, 1.0D);
    }

    public double heat() {
        return heat;
    }

    public int reasimWater() {
        return reasimWater;
    }

    public int reasimSteam() {
        return reasimSteam;
    }

    public ItemStack fuelRod() {
        return fuelRod.copy();
    }

    public boolean loadFuelRod(ItemStack stack) {
        if (!kind().rod() || hasFuelRod() || !(stack.getItem() instanceof RBMKFuelRodItem)) {
            return false;
        }
        fuelRod = stack.copy();
        fuelRod.setCount(1);
        rodFluxState.setHasRod(true);
        setChanged();
        return true;
    }

    public ItemStack manualUnloadFuelRod() {
        if (!hasFuelRod() || !(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
            return ItemStack.EMPTY;
        }
        RBMKFuelRodState state = item.getState(fuelRod);
        RBMKFuelRodColumnPlanner.UnloadPlan plan =
                RBMKFuelRodColumnPlanner.planManualUnload(item.getLegacyRodId(), state);
        if (!plan.accepted()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = fuelRod.copy();
        fuelRod = ItemStack.EMPTY;
        rodFluxState.clearRodTick();
        setChanged();
        return stack;
    }

    public ItemStack removeFuelRodForDrop() {
        if (fuelRod.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = fuelRod.copy();
        fuelRod = ItemStack.EMPTY;
        rodFluxState.clearRodTick();
        setChanged();
        return stack;
    }

    public ItemStackHandler storageItems() {
        return storageItems;
    }

    public boolean canLoadStorageRod() {
        return kind().storage() && storageItems.getStackInSlot(11).isEmpty();
    }

    public boolean loadStorageRod(ItemStack stack) {
        if (!canLoadStorageRod() || stack.isEmpty()) {
            return false;
        }
        ItemStack copy = stack.copy();
        storageItems.setStackInSlot(11, copy);
        setChanged();
        return true;
    }

    public boolean canUnloadStorageRod() {
        return kind().storage() && !storageItems.getStackInSlot(0).isEmpty();
    }

    public ItemStack provideNextStorageRod() {
        return kind().storage() ? storageItems.getStackInSlot(0) : ItemStack.EMPTY;
    }

    public void unloadStorageRod() {
        if (!kind().storage()) {
            return;
        }
        storageItems.setStackInSlot(0, ItemStack.EMPTY);
        setChanged();
    }

    public List<ItemStack> removeStorageForDrop() {
        if (!kind().storage()) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < storageItems.getSlots(); slot++) {
            ItemStack stack = storageItems.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                storageItems.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        setChanged();
        return drops;
    }

    public RBMKControlState controlState() {
        return controlState;
    }

    public double startingLevel() {
        return startingLevel;
    }

    @Nullable
    public RBMKControlRodPlanner.RBMKColor color() {
        return color;
    }

    public RBMKControlRodPlanner.AutoSettings autoSettings() {
        return new RBMKControlRodPlanner.AutoSettings(levelLower, levelUpper, heatLower, heatUpper, function);
    }

    public void setControlTarget(double targetLevel) {
        startingLevel = controlState.level();
        controlState.setTargetLevel(targetLevel);
        setChanged();
    }

    private void tickControl() {
        RBMKColumnBlock.Kind kind = kind();
        if (!kind.control()) {
            return;
        }
        if (kind.automatic()) {
            RBMKControlRodPlanner.AutoTargetPlan target =
                    RBMKControlRodPlanner.planAutoTarget(heat, autoSettings());
            controlState.setTargetLevel(target.targetLevel());
        }
        double before = controlState.level();
        controlState.tick(kind.powered(), RBMKRuntimeSettings.legacyDefaults());
        if (controlState.level() != before) {
            setChanged();
        }
    }

    private void tickFuelRod() {
        if (!kind().rod()) {
            return;
        }
        if (!(fuelRod.getItem() instanceof RBMKFuelRodItem item)) {
            if (!fuelRod.isEmpty()) {
                fuelRod = ItemStack.EMPTY;
                setChanged();
            }
            RBMKFuelRodColumnPlanner.planEmptyTick(rodFluxState);
            return;
        }

        RBMKFuelRodState state = item.getState(fuelRod);
        RBMKThermalState thermalState = new RBMKThermalState();
        thermalState.setHeat(heat);
        thermalState.setReasimWater(reasimWater);
        thermalState.setReasimSteam(reasimSteam);
        RBMKFuelRodColumnPlanner.ColumnTickPlan plan = RBMKFuelRodColumnPlanner.planFuelRodTick(
                RBMKRuntimeSettings.legacyDefaults(),
                thermalState,
                rodFluxState,
                item.getLegacyRodId(),
                state,
                hasRBMKLid(),
                RBMKFuelColumnRuntime.DEFAULT_COLUMN_MAX_HEAT);

        heat = thermalState.heat();
        reasimWater = thermalState.reasimWater();
        reasimSteam = thermalState.reasimSteam();
        item.setState(fuelRod, state);
        if (level != null && plan.leakRadiation() > 0.0D) {
            RBMKNeutronHandler.settings().leakHandler().leak(level, worldPosition, (float) plan.leakRadiation());
        }
        if (plan.spreadFlux()) {
            if (kind().reasim()) {
                RBMKNeutronHandler.spreadReaSimFlux(this, plan.outgoingFluxQuantity(), plan.outgoingFluxRatio());
            } else {
                RBMKNeutronHandler.spreadCardinalFlux(this, plan.outgoingFluxQuantity(), plan.outgoingFluxRatio());
            }
        }
        setChanged();
    }

    private void tickStorage(Level level) {
        if (!kind().storage() || level.getGameTime() % 10L != 0L) {
            return;
        }
        boolean changed = false;
        for (int slot = 0; slot < storageItems.getSlots() - 1; slot++) {
            if (storageItems.getStackInSlot(slot).isEmpty()
                    && !storageItems.getStackInSlot(slot + 1).isEmpty()) {
                storageItems.setStackInSlot(slot, storageItems.getStackInSlot(slot + 1));
                storageItems.setStackInSlot(slot + 1, ItemStack.EMPTY);
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    public RBMKColumnBlock.Kind kind() {
        return getBlockState().getBlock() instanceof RBMKColumnBlock column
                ? column.kind()
                : RBMKColumnBlock.Kind.BLANK;
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            NeutronNodeWorld.removeNode(level, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null) {
            NeutronNodeWorld.removeNode(level, worldPosition);
        }
        super.onChunkUnloaded();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble(TAG_HEAT, heat);
        tag.putInt(TAG_REASIM_WATER, reasimWater);
        tag.putInt(TAG_REASIM_STEAM, reasimSteam);
        if (kind().rod()) {
            rodFluxState.save(tag);
            if (!fuelRod.isEmpty()) {
                tag.put(TAG_FUEL_ROD, fuelRod.save(new CompoundTag()));
            }
        }
        if (kind().storage()) {
            tag.put(TAG_STORAGE_ITEMS, HbmItemStackUtil.saveSlottedItems(storageItems, TAG_STORAGE_SLOT));
        }
        if (kind().control()) {
            controlState.save(tag);
            tag.putDouble(TAG_STARTING_LEVEL, startingLevel);
            tag.putDouble("mult", controlMultiplier());
            if (color != null) {
                tag.putInt(TAG_COLOR, color.ordinal());
            }
            if (kind().automatic()) {
                tag.putDouble(TAG_LEVEL_LOWER, levelLower);
                tag.putDouble(TAG_LEVEL_UPPER, levelUpper);
                tag.putDouble(TAG_HEAT_LOWER, heatLower);
                tag.putDouble(TAG_HEAT_UPPER, heatUpper);
                tag.putInt(TAG_FUNCTION, function.ordinal());
            }
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = tag.contains(TAG_HEAT) ? tag.getDouble(TAG_HEAT) : 20.0D;
        reasimWater = tag.getInt(TAG_REASIM_WATER);
        reasimSteam = tag.getInt(TAG_REASIM_STEAM);
        rodFluxState.load(tag);
        fuelRod = tag.contains(TAG_FUEL_ROD) ? ItemStack.of(tag.getCompound(TAG_FUEL_ROD)) : ItemStack.EMPTY;
        loadStorageItems(tag);
        controlState.load(tag);
        startingLevel = tag.contains(TAG_STARTING_LEVEL) ? tag.getDouble(TAG_STARTING_LEVEL) : 0.0D;
        if (tag.contains(TAG_COLOR)) {
            int index = tag.getInt(TAG_COLOR);
            color = index >= 0 && index < RBMKControlRodPlanner.RBMKColor.values().length
                    ? RBMKControlRodPlanner.RBMKColor.values()[index]
                    : null;
        } else {
            color = null;
        }
        levelLower = tag.getDouble(TAG_LEVEL_LOWER);
        levelUpper = tag.getDouble(TAG_LEVEL_UPPER);
        heatLower = tag.getDouble(TAG_HEAT_LOWER);
        heatUpper = tag.getDouble(TAG_HEAT_UPPER);
        if (tag.contains(TAG_FUNCTION)) {
            int index = tag.getInt(TAG_FUNCTION);
            function = index >= 0 && index < RBMKControlRodPlanner.RBMKFunction.values().length
                    ? RBMKControlRodPlanner.RBMKFunction.values()[index]
                    : RBMKControlRodPlanner.RBMKFunction.LINEAR;
        } else {
            function = RBMKControlRodPlanner.RBMKFunction.LINEAR;
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

    private void loadStorageItems(CompoundTag tag) {
        for (int slot = 0; slot < storageItems.getSlots(); slot++) {
            storageItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        if (tag.contains(TAG_STORAGE_ITEMS)) {
            HbmItemStackUtil.loadSlottedItems(tag, TAG_STORAGE_ITEMS, TAG_STORAGE_SLOT, storageItems);
            return;
        }
        if (tag.contains(HbmItemStackUtil.LEGACY_ITEMS_TAG)) {
            NonNullList<ItemStack> legacyItems = HbmItemStackUtil.loadLegacyOrForgeItems(tag, storageItems.getSlots());
            for (int slot = 0; slot < Math.min(storageItems.getSlots(), legacyItems.size()); slot++) {
                storageItems.setStackInSlot(slot, legacyItems.get(slot));
            }
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        storageItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (kind().storage() && capability == ForgeCapabilities.ITEM_HANDLER) {
            return storageItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public static class ReaSim extends RBMKColumnBlockEntity implements RBMKReaSimRodColumn {
        public ReaSim(BlockPos pos, BlockState state) {
            super(pos, state);
        }
    }
}
