package com.hbm.ntm.blockentity;

import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.menu.RBMKAutoloaderMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.neutron.RBMKAutoloaderPlanner;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RBMKAutoloaderBlockEntity extends BlockEntity implements MenuProvider, HbmTileSyncable {
    public static final String TAG_ITEMS = "items";
    public static final String TAG_SLOT = "slot";
    public static final String TAG_PISTON = "piston";
    public static final String TAG_RETRACTING = "ret";
    public static final String TAG_DELAY = "delay";
    public static final String TAG_CYCLE = "cycle";
    public static final String CONTROL_MINUS = "minus";
    public static final String CONTROL_PLUS = "plus";
    private static final int LAYOUT_REPAIR_INTERVAL = 20;

    private final ItemStackHandler items = new ItemStackHandler(RBMKAutoloaderPlanner.SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (!(stack.getItem() instanceof RBMKFuelRodItem item)) {
                return false;
            }
            return RBMKAutoloaderPlanner.acceptsInput(item.getSpec(), item.getState(stack), slot, cycle);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final IItemHandler menuItems = new LayoutGuardedMenuItems();
    private final IItemHandler automationItems = new AutomationItems();
    private final LazyOptional<IItemHandler> automationItemHandler = LazyOptional.of(() -> automationItems);
    private double piston;
    private double renderPiston;
    private double lastPiston;
    private double syncPiston;
    private int turnProgress;
    private boolean clientRenderInitialized;
    private boolean clientWasMoving;
    private Object audioLift;
    private boolean isRetracting = true;
    private int delay;
    private int cycle = RBMKAutoloaderPlanner.DEFAULT_CYCLE;

    public RBMKAutoloaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RBMK_AUTOLOADER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKAutoloaderBlockEntity blockEntity) {
        if (level.isClientSide) {
            blockEntity.tickClient();
            return;
        }
        if (!blockEntity.ensureAutoloaderLayout(level, pos)) {
            return;
        }
        blockEntity.tickServer(level);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            ensureAutoloaderLayoutNow(level, worldPosition);
        } else if (level != null) {
            initializeClientRenderPiston(piston);
        }
    }

    private boolean ensureAutoloaderLayout(Level level, BlockPos pos) {
        if ((level.getGameTime() + pos.asLong()) % LAYOUT_REPAIR_INTERVAL != 0) {
            return MultiblockHelper.isOperationalCoreLayoutComplete(level, pos);
        }
        return ensureAutoloaderLayoutNow(level, pos);
    }

    private static boolean ensureAutoloaderLayoutNow(Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCoreAt(level, pos);
        if (level.isClientSide || core == null) {
            return false;
        }
        return MultiblockHelper.ensureOperationalCoreLayoutComplete(level, core.pos());
    }

    public IItemHandler items() {
        return menuItems;
    }

    public int cycle() {
        return hasCompleteLayout() ? cycle : RBMKAutoloaderPlanner.DEFAULT_CYCLE;
    }

    public double piston() {
        return hasCompleteLayout() ? piston : 0.0D;
    }

    public double renderPiston() {
        return hasCompleteLayout() ? renderPiston : 0.0D;
    }

    public double lastPiston() {
        return hasCompleteLayout() ? lastPiston : 0.0D;
    }

    public List<ItemStack> removeItemsForDrop() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        setChanged();
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.rbmkAutoloader");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (!hasCompleteLayout()) {
            return null;
        }
        return new RBMKAutoloaderMenu(containerId, inventory, this);
    }

    private void tickServer(Level level) {
        cycle = clampCycle(cycle);
        maybeStartCycle(level);

        RBMKAutoloaderPlanner.MotionPlan motion =
                RBMKAutoloaderPlanner.tickMotion(piston, isRetracting, delay);
        boolean changed = motion.piston() != piston || motion.delay() != delay || motion.isRetracting() != isRetracting;
        piston = motion.piston();
        delay = motion.delay();
        isRetracting = motion.isRetracting();

        if (!isRetracting && piston >= 1.0D) {
            exchangeWithTarget(level);
            changed = true;
        }

        if (changed) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void tickClient() {
        lastPiston = renderPiston;
        if (turnProgress > 0) {
            renderPiston += (syncPiston - renderPiston) / turnProgress;
            turnProgress--;
        } else {
            renderPiston = syncPiston;
        }
        boolean moving = renderPiston > 0.01D && renderPiston < 0.99D;
        audioLift = LegacyMachineAudioBridge.updateLoop(audioLift, this, "hbm:door.wgh_start",
                moving, 25.0D, 25.0F, 0.75F, 1.0F);
        if (clientWasMoving && !moving) {
            LegacyMachineAudioBridge.playLocal(this, "hbm:door.wgh_stop", 2.0F, 1.0F, 25.0D);
        }
        clientWasMoving = moving;
        if (level != null && renderPiston > 0.99D) {
            for (int i = 0; i < 3; i++) {
                ParticleUtil.spawnCoolingTower(level,
                        worldPosition.getX() + 0.5D + level.random.nextGaussian() * 0.125D,
                        worldPosition.getY() + 0.25D,
                        worldPosition.getZ() + 0.5D + level.random.nextGaussian() * 0.125D,
                        0.0F, 0.25F, 1.5F, 70 + level.random.nextInt(30),
                        true, 0.05F, 2.0F, -1);
            }
        }
    }

    private void maybeStartCycle(Level level) {
        if (!isRetracting) {
            return;
        }
        RBMKColumnBlockEntity target = targetFuelColumn(level);
        RBMKAutoloaderPlanner.StartCyclePlan plan = RBMKAutoloaderPlanner.planStartCycle(
                isRetracting,
                level.getGameTime(),
                rodSlots(),
                target != null && target.isFuelRodColumn(),
                target != null && target.coldEnoughForAutoloader(),
                target == null || !target.hasFuelRod(),
                target == null ? null : target.autoloaderFuelSpec(),
                target == null ? null : target.autoloaderFuelState(),
                cycle);
        if (plan.startExtending()) {
            isRetracting = false;
            setChanged();
        }
    }

    private void exchangeWithTarget(Level level) {
        RBMKColumnBlockEntity target = targetFuelColumn(level);
        if (target == null || !target.isFuelRodColumn() || !target.coldEnoughForAutoloader()) {
            isRetracting = true;
            delay = RBMKAutoloaderPlanner.ACTION_DELAY;
            return;
        }

        RBMKAutoloaderPlanner.ExchangePlan plan = RBMKAutoloaderPlanner.planExtendedExchange(
                rodSlots(),
                target.hasFuelRod(),
                target.autoloaderFuelSpec(),
                target.autoloaderFuelState(),
                cycle);
        if (plan.removedOldRod()) {
            ItemStack removed = target.autoloaderUnloadFuelRod();
            if (!removed.isEmpty() && items.getStackInSlot(plan.removedToSlot()).isEmpty()) {
                items.setStackInSlot(plan.removedToSlot(), removed);
            }
        }
        if (plan.insertedNewRod()) {
            ItemStack input = items.getStackInSlot(plan.insertedFromSlot());
            if (!input.isEmpty() && target.autoloaderLoadFuelRod(input)) {
                items.setStackInSlot(plan.insertedFromSlot(), ItemStack.EMPTY);
            }
        }

        isRetracting = plan.retractAfterExchange();
        delay = plan.delayAfterExchange();
    }

    @Nullable
    private RBMKColumnBlockEntity targetFuelColumn(Level level) {
        RBMKColumnBlockEntity column = RBMKColumnBlockEntity.resolveOperationalColumn(level, worldPosition.below());
        return column != null && column.isFuelRodColumn() ? column : null;
    }

    private List<RBMKAutoloaderPlanner.RodSlot> rodSlots() {
        List<RBMKAutoloaderPlanner.RodSlot> slots = new ArrayList<>(RBMKAutoloaderPlanner.SLOT_COUNT);
        for (int slot = 0; slot < RBMKAutoloaderPlanner.SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.getItem() instanceof RBMKFuelRodItem item) {
                slots.add(new RBMKAutoloaderPlanner.RodSlot(true, item.getSpec(), item.getState(stack)));
            } else {
                slots.add(RBMKAutoloaderPlanner.RodSlot.empty());
            }
        }
        return slots;
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return hasCompleteLayout()
                && player.containerMenu instanceof RBMKAutoloaderMenu menu
                && menu.getBlockEntity() == this;
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        RBMKAutoloaderPlanner.CyclePlan plan = RBMKAutoloaderPlanner.planCycleChange(
                cycle,
                tag.getBoolean(CONTROL_MINUS),
                tag.getBoolean(CONTROL_PLUS));
        if (plan.cycle() != cycle) {
            cycle = plan.cycle();
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ITEMS, HbmItemStackUtil.saveSlottedItems(items, TAG_SLOT));
        tag.putDouble(TAG_PISTON, piston);
        tag.putBoolean(TAG_RETRACTING, isRetracting);
        tag.putInt(TAG_DELAY, delay);
        tag.putInt(TAG_CYCLE, cycle);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadItems(tag);
        piston = tag.getDouble(TAG_PISTON);
        if (level != null && level.isClientSide) {
            syncPiston = piston;
            if (!clientRenderInitialized) {
                initializeClientRenderPiston(syncPiston);
            } else {
                turnProgress = 2;
            }
        }
        isRetracting = !tag.contains(TAG_RETRACTING) || tag.getBoolean(TAG_RETRACTING);
        delay = tag.getInt(TAG_DELAY);
        cycle = tag.contains(TAG_CYCLE) ? clampCycle(tag.getInt(TAG_CYCLE)) : RBMKAutoloaderPlanner.DEFAULT_CYCLE;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 9, 1)).inflate(1.0D);
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
        automationItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !hasCompleteLayout()) {
            return LazyOptional.empty();
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return automationItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public boolean hasCompleteLayout() {
        return level != null && MultiblockHelper.isOperationalCoreLayoutComplete(level, worldPosition);
    }

    private void loadItems(CompoundTag tag) {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        if (tag.contains(TAG_ITEMS)) {
            HbmItemStackUtil.loadSlottedItems(tag, TAG_ITEMS, TAG_SLOT, items);
            return;
        }
        NonNullList<ItemStack> legacyItems =
                HbmItemStackUtil.loadLegacyOrForgeItems(tag, RBMKAutoloaderPlanner.SLOT_COUNT);
        for (int slot = 0; slot < Math.min(items.getSlots(), legacyItems.size()); slot++) {
            items.setStackInSlot(slot, legacyItems.get(slot));
        }
    }

    private void initializeClientRenderPiston(double value) {
        if (clientRenderInitialized) {
            return;
        }
        syncPiston = value;
        renderPiston = value;
        lastPiston = value;
        turnProgress = 0;
        clientRenderInitialized = true;
    }

    private static int clampCycle(int value) {
        return Math.max(RBMKAutoloaderPlanner.MIN_CYCLE, Math.min(RBMKAutoloaderPlanner.MAX_CYCLE, value));
    }

    private class LayoutGuardedMenuItems implements IItemHandler {
        @Override
        public int getSlots() {
            return hasCompleteLayout() ? items.getSlots() : 0;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (!hasCompleteLayout()) {
                return ItemStack.EMPTY;
            }
            return items.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!hasCompleteLayout()) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!hasCompleteLayout()) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return hasCompleteLayout() ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasCompleteLayout() && items.isItemValid(slot, stack);
        }
    }

    private class AutomationItems implements IItemHandler {
        @Override
        public int getSlots() {
            return hasCompleteLayout() ? items.getSlots() : 0;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (!hasCompleteLayout()) {
                return ItemStack.EMPTY;
            }
            return items.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!hasCompleteLayout() || piston > 0.0D || slot < RBMKAutoloaderPlanner.INPUT_SLOT_START
                    || slot > RBMKAutoloaderPlanner.INPUT_SLOT_END) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!hasCompleteLayout() || piston > 0.0D || !RBMKAutoloaderPlanner.canExtract(slot)) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return hasCompleteLayout() ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return hasCompleteLayout() && piston <= 0.0D && items.isItemValid(slot, stack);
        }
    }
}
