package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.CokerRecipe;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CokerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    private static final String TAG_WAS_ON = "wasOn";
    private static final String TAG_PROGRESS = "prog";
    private static final String TAG_HEAT = "heat";
    private static final int PROCESS_TIME = 20_000;
    private static final int MAX_HEAT = 100_000;
    private static final double DIFFUSION = 0.25D;

    public static final int SLOT_IDENTIFIER = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int ITEM_COUNT = 2;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private final LazyOptional<IItemHandler> internalItemHandler = LazyOptional.of(this::getItems);
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new CokerExternalItemHandler(getItems()));
    private boolean wasOn;
    private int progress;
    private int heat;

    public CokerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.HEAVYOIL, 16_000),
                tank(HbmFluids.OIL_COKER, 8_000));
    }

    private CokerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(ModBlockEntities.COKER.get(), pos, state, 0L,
                List.of(inputTank, outputTank),
                List.of(inputTank),
                List.of(outputTank),
                false, ITEM_COUNT);
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    @Override
    public LegacyGuiProfile getLegacyGuiProfile() {
        return LegacyGuiProfile.COKER;
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        boolean changed = tryPullHeat(level, pos);
        changed |= setInputTypeFromIdentifier();

        boolean oldWasOn = wasOn;
        wasOn = false;
        CokerRecipe recipe = LegacyOilFluidRecipes.getCoking(inputTank.getTankType());
        if (canProcess(recipe)) {
            int burn = heat / 100;
            if (burn > 0) {
                wasOn = true;
                progress += burn;
                heat -= burn;
                changed = true;
                if (progress >= PROCESS_TIME) {
                    progress -= PROCESS_TIME;
                    finishProcess(recipe);
                }
            }
        }

        if (outputTank.getFill() > 0) {
            tryProvideFluidToPorts(outputTank.getTankType(), outputTank.getPressure(), this);
        }
        if (wasOn && level.getGameTime() % 2L == 0L) {
            ParticleUtil.spawnCoolingTower(level, pos.getX() + 0.5D, pos.getY() + 22.0D, pos.getZ() + 0.5D,
                    10.0F, 0.75F, 3.0F, 200 + level.random.nextInt(50), false, 0.075F, 0.25F, 0x404040);
        }
        return changed || oldWasOn != wasOn;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return slot == SLOT_IDENTIFIER && stack.getItem() instanceof IFluidIdentifierItem;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fixedSurroundingPorts();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == outputTank.getTankType() && outputTank.getFill() > 0;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.heatTu(heat),
                LegacyLookOverlayLines.compactTank(true, inputTank),
                LegacyLookOverlayLines.compactTank(false, outputTank)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_WAS_ON, wasOn);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_HEAT, heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        wasOn = tag.getBoolean(TAG_WAS_ON);
        progress = tag.getInt(TAG_PROGRESS);
        heat = tag.getInt(TAG_HEAT);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? internalItemHandler.cast() : externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        internalItemHandler.invalidate();
        externalItemHandler.invalidate();
    }

    public boolean isWasOn() {
        return wasOn;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return PROCESS_TIME;
    }

    public int getHeat() {
        return heat;
    }

    public int getMaxHeat() {
        return MAX_HEAT;
    }

    public int getProgressBarWidth(int width) {
        return progress * width / PROCESS_TIME;
    }

    public int getHeatBarWidth(int width) {
        return heat * width / MAX_HEAT;
    }

    private boolean setInputTypeFromIdentifier() {
        ItemStackHandler items = getItems();
        if (items == null) {
            return false;
        }
        ItemStack stack = items.getStackInSlot(SLOT_IDENTIFIER);
        if (!(stack.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        FluidType selected = identifier.getPrimaryType(stack);
        if (selected == null || selected == HbmFluids.NONE || inputTank.getTankType() == selected) {
            return false;
        }
        inputTank.conform(new HbmFluidStack(selected, 0));
        return true;
    }

    private boolean tryPullHeat(Level level, BlockPos pos) {
        int oldHeat = heat;
        if (heat >= MAX_HEAT) {
            return false;
        }
        if (heat < MAX_HEAT) {
            BlockEntity sourceEntity = level.getBlockEntity(pos.below());
            if (sourceEntity instanceof HeatSource source) {
                int diff = source.getHeatStored() - heat;
                if (diff > 0) {
                    int pulled = (int) Math.ceil(diff * DIFFUSION);
                    source.useUpHeat(pulled);
                    heat = Math.min(heat + pulled, MAX_HEAT);
                    return heat != oldHeat;
                }
                if (diff == 0) {
                    return false;
                }
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
        return heat != oldHeat;
    }

    private boolean canProcess(@Nullable CokerRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        HbmFluidStack byproduct = recipe.byproduct();
        if (byproduct != null) {
            configureTank(outputTank, byproduct.type());
        }
        if (inputTank.getFill() < recipe.inputAmount()) {
            return false;
        }
        if (byproduct != null && (outputTank.getTankType() != byproduct.type()
                || outputTank.getFill() + byproduct.amount() > outputTank.getMaxFill())) {
            return false;
        }
        return canFitOutput(recipe.outputStack());
    }

    private void finishProcess(CokerRecipe recipe) {
        ItemStack output = recipe.outputStack();
        if (!output.isEmpty()) {
            addOutput(output);
        }
        HbmFluidStack byproduct = recipe.byproduct();
        if (byproduct != null) {
            outputTank.setFill(outputTank.getFill() + byproduct.amount());
        }
        inputTank.setFill(inputTank.getFill() - recipe.inputAmount());
        onFluidContentsChanged();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean canFitOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStackHandler items = getItems();
        if (items == null) {
            return false;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        return existing.isEmpty()
                || ItemHandlerHelper.canItemStacksStack(existing, stack)
                && existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), items.getSlotLimit(SLOT_OUTPUT));
    }

    private void addOutput(ItemStack stack) {
        ItemStackHandler items = getItems();
        if (items == null || stack.isEmpty()) {
            return;
        }
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, stack.copy());
            return;
        }
        ItemStack merged = existing.copy();
        merged.grow(stack.getCount());
        items.setStackInSlot(SLOT_OUTPUT, merged);
    }

    private static final class CokerExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private CokerExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_OUTPUT) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = existing.copyWithCount(Math.min(amount, existing.getCount()));
            if (!simulate) {
                ItemStack remaining = existing.copy();
                remaining.shrink(extracted.getCount());
                items.setStackInSlot(SLOT_OUTPUT, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}
