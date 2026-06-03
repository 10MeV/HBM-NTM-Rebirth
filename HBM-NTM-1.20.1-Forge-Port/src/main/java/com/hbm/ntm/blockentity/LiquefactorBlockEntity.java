package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.menu.LiquefactorMenu;
import com.hbm.ntm.recipe.LiquefactionRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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

import java.util.ArrayList;
import java.util.List;

public class LiquefactorBlockEntity extends HbmEnergyAndFluidBlockEntity implements MenuProvider, HbmStandardFluidSender {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_USAGE = "usage";
    private static final String TAG_PROCESS_TIME = "processTime";
    private static final long MAX_POWER = 100_000L;
    private static final int USAGE_BASE = 250;
    private static final int PROCESS_TIME_BASE = 100;
    private static final int TANK_CAPACITY = 24_000;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(0, 4, 0, Direction.UP),
            FluidPort.of(0, -1, 0, Direction.DOWN),
            FluidPort.of(2, 1, 0, Direction.EAST),
            FluidPort.of(-2, 1, 0, Direction.WEST),
            FluidPort.of(0, 1, 2, Direction.SOUTH),
            FluidPort.of(0, 1, -2, Direction.NORTH));

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int SLOT_UPGRADE_SPEED = 2;
    public static final int SLOT_UPGRADE_POWER = 3;

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_INPUT) {
                return level == null || findRecipe(level, stack) != null;
            }
            if (slot == SLOT_BATTERY) {
                return HbmBatteryTransfer.isHbmBattery(stack)
                        || stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    private int progress;
    private int usage = USAGE_BASE;
    private int processTime = PROCESS_TIME_BASE;

    public LiquefactorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L), new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private LiquefactorBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank tank) {
        super(ModBlockEntities.LIQUEFACTOR.get(), pos, state, energy, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LiquefactorBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, blockEntity);
        HbmEnergyUtil.chargeStorageFromItem(blockEntity.items.getStackInSlot(SLOT_BATTERY), blockEntity.energy, blockEntity.energy.getReceiverSpeed());

        boolean changed = false;
        if (blockEntity.canProcess(level)) {
            blockEntity.energy.usePower(blockEntity.usage);
            blockEntity.progress++;
            changed = true;
            if (blockEntity.progress >= blockEntity.processTime) {
                blockEntity.finishProcess(level);
            }
        } else if (blockEntity.progress != 0) {
            blockEntity.progress = 0;
            changed = true;
        }
        if (blockEntity.tank.getTankType() != HbmFluids.NONE && blockEntity.tank.getFill() > 0) {
            blockEntity.tryProvideFluidToPorts(blockEntity.tank.getTankType(), blockEntity.tank.getPressure(), blockEntity);
        }

        if (changed) {
            blockEntity.setChanged();
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getUsage() {
        return usage;
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public int getPowerBarHeight(int maxHeight) {
        return getMaxPower() <= 0L ? 0 : (int) (getPower() * maxHeight / getMaxPower());
    }

    public boolean canProcess(Level level) {
        if (energy.getPower() < usage) {
            return false;
        }
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        LiquefactionRecipe recipe = findRecipe(level, input);
        if (recipe == null) {
            return false;
        }
        HbmFluidStack output = recipe.getOutputFluid();
        return tank.fill(output.type(), output.amount(), output.pressure(), true) == output.amount();
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(tank);
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE && tank.getTankType() == type && tank.getFill() > 0;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.OUTPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(tank);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, items.serializeNBT());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_USAGE, usage);
        tag.putInt(TAG_PROCESS_TIME, processTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        progress = tag.getInt(TAG_PROGRESS);
        usage = tag.contains(TAG_USAGE) ? tag.getInt(TAG_USAGE) : USAGE_BASE;
        processTime = tag.contains(TAG_PROCESS_TIME) ? tag.getInt(TAG_PROCESS_TIME) : PROCESS_TIME_BASE;
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
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineLiquefactor", "Liquefactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new LiquefactorMenu(containerId, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    private void finishProcess(Level level) {
        LiquefactionRecipe recipe = findRecipe(level, items.getStackInSlot(SLOT_INPUT));
        if (recipe == null) {
            progress = 0;
            return;
        }
        HbmFluidStack output = recipe.getOutputFluid();
        int filled = tank.fill(output.type(), output.amount(), output.pressure(), false);
        if (filled == output.amount()) {
            items.extractItem(SLOT_INPUT, 1, false);
        }
        progress = 0;
    }

    @Nullable
    private static LiquefactionRecipe findRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        SimpleContainer container = new SimpleContainer(stack);
        return level.getRecipeManager().getRecipeFor(ModRecipes.LIQUEFACTION.type().get(), container, level).orElse(null);
    }
}
