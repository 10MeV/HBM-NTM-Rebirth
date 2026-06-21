package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.item.LaserWavelength;
import com.hbm.ntm.menu.SilexMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.recipe.SilexRecipeRuntime;
import com.hbm.ntm.recipe.SilexRecipeRuntime.SilexRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class SilexBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmLegacyButtonReceiver {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_IDENTIFIER = 1;
    public static final int SLOT_CONTAINER_IN = 2;
    public static final int SLOT_CONTAINER_OUT = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_QUEUE_START = 5;
    public static final int SLOT_QUEUE_END = 10;
    public static final int SLOT_COUNT = 11;
    public static final int CONTROL_VOID = 0;
    public static final int MAX_FILL = 16_000;
    public static final int PROCESS_TIME = 100;
    private static final int PRIME = 137;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_FILL = "fill";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_RECIPE_INDEX = "recipeIndex";
    private static final String TAG_MODE = "mode";
    private static final String TAG_CURRENT_STACK = "currentStack";
    private static final String TAG_CURRENT_FLUID = "currentFluid";

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT -> SilexRecipeRuntime.isValidInput(stack);
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_CONTAINER_IN -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == SLOT_OUTPUT || slot >= SLOT_QUEUE_START) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int currentFill;
    private int progress;
    private int recipeIndex;
    private LaserWavelength mode = LaserWavelength.NULL;
    private ItemStack currentStack = ItemStack.EMPTY;
    private FluidType currentFluid = HbmFluids.NONE;
    private int loadDelay;

    public SilexBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SILEX.get(), pos, state, List.of(new HbmFluidTank(HbmFluids.PEROXIDE, MAX_FILL)));
        this.tank = getAllTanks().get(0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SilexBlockEntity silex) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, silex);
        int oldFill = silex.currentFill;
        int oldProgress = silex.progress;
        int oldTank = silex.tank.getFill();
        FluidType oldType = silex.tank.getTankType();
        silex.setFluidTankTypeFromIdentifierSlot(silex.items, SLOT_IDENTIFIER, silex.tank);
        HbmFluidItemTransfer.loadTankFromSlot(silex.items, SLOT_CONTAINER_IN, SLOT_CONTAINER_OUT, silex.tank);
        silex.loadFluidSource();
        if (!silex.process()) {
            silex.progress = 0;
        }
        silex.dequeue();
        if (silex.currentFill <= 0) {
            silex.clearCurrent();
        }
        silex.mode = LaserWavelength.NULL;
        if (oldFill != silex.currentFill || oldProgress != silex.progress || oldTank != silex.tank.getFill()
                || oldType != silex.tank.getTankType()) {
            silex.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public boolean acceptLaser(Direction felFacing, LaserWavelength wavelength) {
        Direction facing = getBlockState().getValue(HorizontalMachineBlock.FACING);
        if (facing != felFacing && facing != felFacing.getOpposite()) {
            return false;
        }
        if (mode != wavelength) {
            mode = wavelength == null ? LaserWavelength.NULL : wavelength;
            setChanged();
        }
        return true;
    }

    private void loadFluidSource() {
        Optional<SilexRecipe> fluidRecipe = SilexRecipeRuntime.findFluidSource(tank.getTankType());
        if (fluidRecipe.isPresent()) {
            if (currentFill == 0) {
                currentFluid = tank.getTankType();
                currentStack = ItemStack.EMPTY;
            }
            if (currentFluid == tank.getTankType()) {
                int moved = Math.min(50, Math.min(MAX_FILL - currentFill, tank.getFill()));
                if (moved > 0) {
                    currentFill += moved;
                    tank.drain(moved, false);
                }
            }
        }
        loadDelay = (loadDelay + 1) % 21;
        if (loadDelay != 0 || tank.getTankType() != HbmFluids.PEROXIDE) {
            return;
        }
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        Optional<SilexRecipe> recipe = SilexRecipeRuntime.find(input);
        if (recipe.isEmpty()) {
            return;
        }
        boolean sameSource = currentFill == 0 || (!currentStack.isEmpty() && ItemStack.isSameItemSameTags(currentStack, input));
        if (!sameSource) {
            return;
        }
        int load = recipe.get().fluidProduced();
        if (load <= MAX_FILL - currentFill && load <= tank.getFill()) {
            currentFill += load;
            currentStack = input.copyWithCount(1);
            currentFluid = HbmFluids.NONE;
            tank.drain(load, false);
            input.shrink(1);
            items.setStackInSlot(SLOT_INPUT, input);
        }
    }

    private boolean process() {
        Optional<SilexRecipe> recipe = currentRecipe();
        if (recipe.isEmpty() || !mode.canPower(recipe.get().laserStrength()) || currentFill < recipe.get().fluidConsumed()
                || !items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return false;
        }
        int speed = 1 << Math.max(0, mode.ordinal() - recipe.get().laserStrength().ordinal());
        progress += speed;
        if (progress >= PROCESS_TIME) {
            currentFill -= recipe.get().fluidConsumed();
            items.setStackInSlot(SLOT_OUTPUT, recipe.get().selectOutput(recipeIndex));
            recipeIndex += PRIME;
            progress = 0;
        }
        return true;
    }

    private Optional<SilexRecipe> currentRecipe() {
        if (currentFill <= 0) {
            return Optional.empty();
        }
        if (currentFluid != HbmFluids.NONE) {
            return SilexRecipeRuntime.findFluidSource(currentFluid);
        }
        return SilexRecipeRuntime.find(currentStack);
    }

    private void dequeue() {
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return;
        }
        for (int slot = SLOT_QUEUE_START; slot <= SLOT_QUEUE_END; slot++) {
            ItemStack queue = items.getStackInSlot(slot);
            if (!queue.isEmpty() && ItemStack.isSameItemSameTags(output, queue)
                    && queue.getCount() < queue.getMaxStackSize()) {
                queue.grow(1);
                output.shrink(1);
                items.setStackInSlot(SLOT_OUTPUT, output);
                return;
            }
        }
        for (int slot = SLOT_QUEUE_START; slot <= SLOT_QUEUE_END; slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                items.setStackInSlot(slot, output.copy());
                items.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
                return;
            }
        }
    }

    private void clearCurrent() {
        currentFill = 0;
        currentStack = ItemStack.EMPTY;
        currentFluid = HbmFluids.NONE;
    }

    public void voidContents() {
        clearCurrent();
        progress = 0;
        setChanged();
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_VOID) {
            voidContents();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public int getCurrentFill() {
        return currentFill;
    }

    public int getProgress() {
        return progress;
    }

    public LaserWavelength getMode() {
        return mode;
    }

    public FluidType getCurrentFluid() {
        return currentFluid;
    }

    public ItemStack getCurrentStack() {
        return currentStack;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.machineSILEX");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SilexMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(tank);
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver() {
        return tank.getTankType() != HbmFluids.NONE;
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = getBlockState().getValue(HorizontalMachineBlock.FACING);
        Direction side = facing.getClockWise();
        return List.of(
                new FluidPort(new BlockPos(side.getStepX() * 2, 1, side.getStepZ() * 2), side),
                new FluidPort(new BlockPos(-side.getStepX() * 2, 1, -side.getStepZ() * 2), side.getOpposite()));
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.allFluidUserTanks(this));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putInt(TAG_FILL, currentFill);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_RECIPE_INDEX, recipeIndex);
        tag.putString(TAG_MODE, mode.name());
        tag.putString(TAG_CURRENT_FLUID, currentFluid.getName());
        if (!currentStack.isEmpty()) {
            tag.put(TAG_CURRENT_STACK, currentStack.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        currentFill = tag.getInt(TAG_FILL);
        progress = tag.getInt(TAG_PROGRESS);
        recipeIndex = tag.getInt(TAG_RECIPE_INDEX);
        mode = tag.contains(TAG_MODE) ? LaserWavelength.valueOf(tag.getString(TAG_MODE)) : LaserWavelength.NULL;
        currentFluid = tag.contains(TAG_CURRENT_FLUID) ? HbmFluids.fromName(tag.getString(TAG_CURRENT_FLUID)) : HbmFluids.NONE;
        currentStack = tag.contains(TAG_CURRENT_STACK) ? ItemStack.of(tag.getCompound(TAG_CURRENT_STACK)) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
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
}
