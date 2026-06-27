package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.menu.CombinationOvenMenu;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.CombinationOvenRecipe;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombinationOvenBlockEntity extends HbmFluidBlockEntity
        implements MenuProvider, HbmStandardFluidSender {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_TANK_INPUT = 2;
    public static final int SLOT_TANK_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int PROCESS_TIME = 20_000;
    public static final int MAX_HEAT = 100_000;
    public static final double DIFFUSION = 0.25D;
    public static final int TANK_CAPACITY = 24_000;

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "prog";
    private static final String TAG_HEAT = "heat";
    private static final String TAG_WAS_ON = "wasOn";

    private final HbmFluidTank tank;
    private final MachinePollutionBuffers pollution = new MachinePollutionBuffers(50);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT -> level != null && findRecipe(level, stack) != null;
                case SLOT_TANK_INPUT -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler =
            LazyOptional.of(() -> new CombinationOvenExternalItemHandler(items));

    private boolean wasOn;
    private int progress;
    private int heat;

    public CombinationOvenBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private CombinationOvenBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.COMBINATION_OVEN.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CombinationOvenBlockEntity oven) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = oven.tickServer(level, pos, state);
        oven.networkPackNT(50);
        if (changed) {
            oven.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, CombinationOvenBlockEntity oven) {
        if (!level.isClientSide || !oven.wasOn || level.random.nextInt(15) != 0) {
            return;
        }
        level.addParticle(ParticleTypes.LAVA,
                pos.getX() + 0.5D + level.random.nextGaussian() * 0.5D,
                pos.getY() + 2.0D,
                pos.getZ() + 0.5D + level.random.nextGaussian() * 0.5D,
                0.0D, 0.0D, 0.0D);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public int getProgress() {
        return progress;
    }

    public int getHeat() {
        return heat;
    }

    public int getProgressPixels(int width) {
        return Math.max(0, Math.min(width, progress * width / PROCESS_TIME));
    }

    public int getHeatPixels(int width) {
        return Math.max(0, Math.min(width, heat * width / MAX_HEAT));
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(tank, pollution.soot(), pollution.heavyMetal(), pollution.poison());
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.OUTPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts(getBlockState());
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        onFluidContentsChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.furnaceCombination", "Combination Oven");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CombinationOvenMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        pollution.writeLegacyNbt(tag);
        tank.writeToNbt(tag, "tank");
        tag.putBoolean(TAG_WAS_ON, wasOn);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putInt(TAG_HEAT, heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        pollution.readLegacyNbt(tag);
        if (tag.contains("tank")) {
            tank.readFromNbt(tag, "tank");
        }
        wasOn = tag.getBoolean(TAG_WAS_ON);
        progress = tag.getInt(TAG_PROGRESS);
        heat = tag.getInt(TAG_HEAT);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : externalItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        externalItemHandler.invalidate();
    }

    private boolean tickServer(Level level, BlockPos pos, BlockState state) {
        int oldProgress = progress;
        int oldHeat = heat;
        int oldFill = tank.getFill();
        boolean oldWasOn = wasOn;

        tryPullHeat(level, pos);
        if (level.getGameTime() % 20L == 0L) {
            sendOutputsToPorts(level, pos, state);
        }
        boolean changed = processFluidItemTransfers(items,
                List.of(com.hbm.ntm.fluid.HbmFluidItemTransfer.TankSlotTransfer.unload(
                        SLOT_TANK_INPUT, SLOT_TANK_OUTPUT, tank)));

        wasOn = false;
        CombinationOvenRecipe recipe = findRecipe(level, items.getStackInSlot(SLOT_INPUT));
        if (canSmelt(recipe)) {
            int burn = heat / 100;
            if (burn > 0) {
                wasOn = true;
                progress += burn;
                heat -= burn;
                if (progress >= PROCESS_TIME) {
                    progress -= PROCESS_TIME;
                    finishRecipe(recipe);
                }
                igniteTopEntities(level, pos);
                if (level.getGameTime() % 10L == 0L) {
                    LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY() + 1.0D, pos.getZ(),
                            "hbm:weapon.flamethrowerShoot", 0.25F, 0.5F);
                }
                if (level.getGameTime() % 20L == 0L) {
                    pollution.pollute(level, pos, PollutionType.SOOT,
                            com.hbm.handler.pollution.PollutionHandler.SOOT_PER_SECOND * 3.0F);
                }
            }
        } else if (progress != 0) {
            progress = 0;
        }

        return changed
                || oldProgress != progress
                || oldHeat != heat
                || oldFill != tank.getFill()
                || oldWasOn != wasOn;
    }

    private void tryPullHeat(Level level, BlockPos pos) {
        if (heat >= MAX_HEAT) {
            return;
        }
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below instanceof HeatSource source) {
            int diff = source.getHeatStored() - heat;
            if (diff > 0) {
                diff = (int) Math.ceil(diff * DIFFUSION);
                source.useUpHeat(diff);
                heat = Math.min(MAX_HEAT, heat + diff);
                return;
            }
            if (diff == 0) {
                return;
            }
        }
        heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
    }

    private void sendOutputsToPorts(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Direction rot = dir.getClockWise();
            for (int y = 0; y <= 1; y++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos port = pos.offset(dir.getStepX() * 2 + rot.getStepX() * j, y,
                            dir.getStepZ() * 2 + rot.getStepZ() * j);
                    sendAtPort(level, port, dir);
                }
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                sendAtPort(level, pos.offset(x, 2, z), Direction.UP);
            }
        }
        if (tank.getFill() > 0) {
            refreshTrackedProviderFluidPortsReport(List.of(tank), this);
            tryProvideFluidToPorts(tank.getTankType(), tank.getPressure(), this);
        }
    }

    private void sendAtPort(Level level, BlockPos port, Direction direction) {
        if (tank.getFill() > 0) {
            tryProvideFluidToForgeHandlerReport(level.getBlockEntity(port), direction.getOpposite(),
                    tank.getTankType(), tank.getPressure(), this);
        }
        pollution.sendSmoke(level, port.getX(), port.getY(), port.getZ(), direction);
    }

    @Nullable
    private CombinationOvenRecipe findRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        SimpleContainer container = new SimpleContainer(input);
        for (CombinationOvenRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.COMBINATION_OVEN.type().get())) {
            if (recipe.matches(container, level)) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canSmelt(@Nullable CombinationOvenRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (!recipe.input().test(input)) {
            return false;
        }
        if (recipe.outputItem().isPresent()) {
            ItemStack output = recipe.outputItem().get().representativeStack();
            ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
            if (!existing.isEmpty()) {
                if (!ItemStack.isSameItemSameTags(output, existing)) {
                    return false;
                }
                if (existing.getCount() + output.getCount() > existing.getMaxStackSize()) {
                    return false;
                }
            }
        }
        if (recipe.outputFluid().isPresent()) {
            HbmFluidStack output = recipe.outputFluid().get();
            if (tank.getTankType() != output.type() && tank.getFill() > 0) {
                return false;
            }
            if (tank.getTankType() == output.type() && tank.getFill() + output.amount() > tank.getMaxFill()) {
                return false;
            }
        }
        return true;
    }

    private void finishRecipe(CombinationOvenRecipe recipe) {
        recipe.outputItem().map(HbmItemOutput::representativeStack).ifPresent(this::addOutput);
        recipe.outputFluid().ifPresent(this::addOutputFluid);
        items.extractItem(SLOT_INPUT, recipe.input().count(), false);
        setChanged();
    }

    private void addOutput(ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, output.copy());
    }

    private void addOutputFluid(HbmFluidStack output) {
        if (output.isEmpty()) {
            return;
        }
        if (tank.getTankType() != output.type()) {
            tank.conform(new HbmFluidStack(output.type(), 0, output.pressure()));
        }
        tank.setFill(tank.getFill() + output.amount());
        onFluidContentsChanged();
    }

    private void igniteTopEntities(Level level, BlockPos pos) {
        AABB box = new AABB(pos.getX() - 0.5D, pos.getY() + 2.0D, pos.getZ() - 0.5D,
                pos.getX() + 1.5D, pos.getY() + 4.0D, pos.getZ() + 1.5D);
        for (Entity entity : level.getEntities(null, box)) {
            entity.setSecondsOnFire(5);
        }
    }

    private static List<FluidPort> fluidPorts(BlockState state) {
        List<FluidPort> ports = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Direction rot = dir.getClockWise();
            for (int y = 0; y <= 1; y++) {
                for (int j = -1; j <= 1; j++) {
                    ports.add(FluidPort.of(dir.getStepX() * 2 + rot.getStepX() * j, y,
                            dir.getStepZ() * 2 + rot.getStepZ() * j, dir));
                }
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ports.add(FluidPort.of(x, 2, z, Direction.UP));
            }
        }
        return ports;
    }

    private static Direction legacyFacing(BlockState state) {
        return state.hasProperty(HorizontalMachineBlock.FACING) ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static final class CombinationOvenExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private CombinationOvenExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> items.getStackInSlot(SLOT_INPUT);
                case 1 -> items.getStackInSlot(SLOT_OUTPUT);
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !items.isItemValid(SLOT_INPUT, stack)) {
                return stack;
            }
            return items.insertItem(SLOT_INPUT, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 1 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(SLOT_OUTPUT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case 0 -> items.getSlotLimit(SLOT_INPUT);
                case 1 -> items.getSlotLimit(SLOT_OUTPUT);
                default -> 0;
            };
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && items.isItemValid(SLOT_INPUT, stack);
        }
    }
}
