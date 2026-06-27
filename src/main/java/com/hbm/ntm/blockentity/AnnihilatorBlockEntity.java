package com.hbm.ntm.blockentity;

import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidReleaseEffects;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.AnnihilatorMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.world.saveddata.AnnihilatorSavedData;
import java.math.BigInteger;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnnihilatorBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmLegacyControlReceiver {
    public static final int SLOT_TRASH = 0;
    public static final int SLOT_FLUID_IDENTIFIER = 1;
    public static final int SLOT_PAYOUT_START = 2;
    public static final int SLOT_PAYOUT_END = 7;
    public static final int SLOT_MONITOR = 8;
    public static final int SLOT_REQUEST = 9;
    public static final int SLOT_REQUEST_OUTPUT = 10;
    public static final int SLOT_COUNT = 11;

    private static final int TANK_CAPACITY = 2_500_000;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_POOL = "pool";
    private static final String TAG_MONITOR = "monitorBigInt";
    private static final String DEFAULT_POOL = "Recycling";

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_TRASH, SLOT_MONITOR, SLOT_REQUEST -> true;
                case SLOT_FLUID_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private String pool = DEFAULT_POOL;
    private BigInteger monitorBigInt = BigInteger.ZERO;

    public AnnihilatorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private AnnihilatorBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.ANNIHILATOR.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AnnihilatorBlockEntity annihilator) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, annihilator);

        int oldFill = annihilator.tank.getFill();
        FluidType oldType = annihilator.tank.getTankType();
        String oldPool = annihilator.pool;
        BigInteger oldMonitor = annihilator.monitorBigInt;
        boolean changed = annihilator.setFluidTankTypeFromIdentifierSlot(
                annihilator.items, SLOT_FLUID_IDENTIFIER, annihilator.tank);

        if (!annihilator.pool.isBlank() && level instanceof ServerLevel serverLevel) {
            if (annihilator.tank.getTankType() != HbmFluids.NONE) {
                annihilator.refreshTrackedReceiverFluidPortsReport(List.of(annihilator.tank), annihilator);
            }
            AnnihilatorSavedData data = AnnihilatorSavedData.getData(serverLevel);
            boolean didSomething = false;
            ItemStack trash = annihilator.items.getStackInSlot(SLOT_TRASH);
            if (!trash.isEmpty()) {
                ItemStack destroyed = trash.copy();
                annihilator.onDestroy(destroyed);
                annihilator.tryAddPayout(data.pushToPool(serverLevel, annihilator.pool, destroyed, false));
                annihilator.items.setStackInSlot(SLOT_TRASH, ItemStack.EMPTY);
                didSomething = true;
                changed = true;
            }
            if (annihilator.tank.getFill() > 0) {
                int amount = annihilator.tank.getFill();
                HbmFluidReleaseEffects.applyLegacyPollutingRelease(level, pos, annihilator.tank.getTankType(),
                        FluidReleaseType.BURN, safeDoubleMb(amount));
                annihilator.tryAddPayout(data.pushToPool(serverLevel, annihilator.pool,
                        annihilator.tank.getTankType(), amount, false));
                annihilator.tank.setFill(0);
                didSomething = true;
                changed = true;
            }
            if (didSomething) {
                annihilator.spawnBurnEffects(level);
            }
            annihilator.updateMonitor(data);
            if (annihilator.processRequest(serverLevel, data)) {
                changed = true;
            }
        } else {
            annihilator.monitorBigInt = BigInteger.ZERO;
        }

        changed = changed
                || oldFill != annihilator.tank.getFill()
                || oldType != annihilator.tank.getTankType()
                || !oldPool.equals(annihilator.pool)
                || !oldMonitor.equals(annihilator.monitorBigInt);
        annihilator.networkPackNT(25);
        if (changed) {
            annihilator.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private boolean processRequest(ServerLevel level, AnnihilatorSavedData data) {
        ItemStack request = items.getStackInSlot(SLOT_REQUEST);
        if (request.isEmpty()) {
            return false;
        }
        ItemStack single = request.copyWithCount(1);
        onDestroy(single);
        tryAddRequestPayout(data.pushToPool(level, pool, single, true));
        items.extractItem(SLOT_REQUEST, 1, false);
        return true;
    }

    private void updateMonitor(AnnihilatorSavedData data) {
        ItemStack monitor = items.getStackInSlot(SLOT_MONITOR);
        if (monitor.isEmpty()) {
            monitorBigInt = BigInteger.ZERO;
            return;
        }
        if (monitor.getItem() instanceof IFluidIdentifierItem identifier) {
            FluidType type = identifier.getIdentifiedFluid(level, worldPosition, monitor);
            monitorBigInt = data.getFluidAmount(pool, type);
        } else {
            monitorBigInt = data.getItemMetaAmount(pool, monitor, monitor.getDamageValue());
        }
    }

    private static int safeDoubleMb(int amount) {
        return amount > Integer.MAX_VALUE / 2 ? Integer.MAX_VALUE : amount * 2;
    }

    private void onDestroy(ItemStack stack) {
        float radiation = HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
        if (radiation <= 0.0F || level == null) {
            return;
        }
        Direction facing = facing();
        BlockPos radiationPos = worldPosition.offset(-facing.getStepX() * 3, 9, -facing.getStepZ() * 3);
        ChunkRadiationManager.incrementRadiation(level, radiationPos, Math.min(radiation * 5.0F, 1_000.0F));
    }

    private void spawnBurnEffects(Level level) {
        Vec3 pos = burnEffectPosition();
        ParticleUtil.spawnGasFlame(level, pos.x, pos.y, pos.z,
                level.random.nextGaussian() * 0.05D,
                0.1D,
                level.random.nextGaussian() * 0.05D);
        if (level.getGameTime() % 3L == 0L) {
            LegacySoundPlayer.playLegacyFlamethrowerShoot(level, pos.x, pos.y, pos.z,
                    SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 0.25F);
        }
    }

    private Vec3 burnEffectPosition() {
        Direction facing = facing();
        return new Vec3(
                worldPosition.getX() + 0.5D - facing.getStepX() * 3.0D,
                worldPosition.getY() + 8.75D,
                worldPosition.getZ() + 0.5D - facing.getStepZ() * 3.0D);
    }

    private void tryAddPayout(@Nullable ItemStack payout) {
        if (payout == null || payout.isEmpty()) {
            return;
        }
        for (int slot = SLOT_PAYOUT_START; slot <= SLOT_PAYOUT_END; slot++) {
            ItemStack existing = items.getStackInSlot(slot);
            if (canMerge(existing, payout)) {
                existing.grow(payout.getCount());
                items.setStackInSlot(slot, existing);
                return;
            }
        }
        for (int slot = SLOT_PAYOUT_START; slot <= SLOT_PAYOUT_END; slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                items.setStackInSlot(slot, payout.copy());
                return;
            }
        }
    }

    private void tryAddRequestPayout(@Nullable ItemStack payout) {
        if (payout == null || payout.isEmpty()) {
            return;
        }
        ItemStack existing = items.getStackInSlot(SLOT_REQUEST_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_REQUEST_OUTPUT, payout.copy());
        } else if (canMerge(existing, payout)) {
            existing.grow(payout.getCount());
            items.setStackInSlot(SLOT_REQUEST_OUTPUT, existing);
        }
    }

    private static boolean canMerge(ItemStack existing, ItemStack payout) {
        return !existing.isEmpty()
                && ItemStack.isSameItemSameTags(existing, payout)
                && existing.getCount() + payout.getCount() <= existing.getMaxStackSize();
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private List<FluidPort> annihilatorFluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX() * 5, 0, facing.getStepZ() * 5, facing),
                FluidPort.of(facing.getStepX() * 3 + side.getStepX() * 2, 0,
                        facing.getStepZ() * 3 + side.getStepZ() * 2, side),
                FluidPort.of(facing.getStepX() * 3 - side.getStepX() * 2, 0,
                        facing.getStepZ() * 3 - side.getStepZ() * 2, side.getOpposite()));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public String getPool() {
        return pool;
    }

    public BigInteger getMonitorBigInt() {
        return monitorBigInt;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 5.0D,
                worldPosition.getY(),
                worldPosition.getZ() - 5.0D,
                worldPosition.getX() + 6.0D,
                worldPosition.getY() + 9.0D,
                worldPosition.getZ() + 6.0D);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return annihilatorFluidPorts();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != null && type != HbmFluids.NONE && type == tank.getTankType();
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
    public HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return HbmEnergyReceiver.ConnectionPriority.LOW;
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmFluidSideMode.INPUT : HbmFluidSideMode.NONE;
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.annihilator", "Annihilator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AnnihilatorMenu(containerId, inventory, this);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains(TAG_POOL)) {
            String requested = data.getString(TAG_POOL);
            if (!requested.isBlank()) {
                pool = requested.length() > 20 ? requested.substring(0, 20) : requested;
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putString(TAG_POOL, pool);
        tag.putString(TAG_MONITOR, monitorBigInt.toString());
        tank.writeToNbt(tag, "t");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        pool = tag.contains(TAG_POOL) && !tag.getString(TAG_POOL).isBlank()
                ? tag.getString(TAG_POOL)
                : DEFAULT_POOL;
        if (pool.length() > 20) {
            pool = pool.substring(0, 20);
        }
        if (tag.contains("t") || tag.contains("t_type") || tag.contains("t_type_id")) {
            tank.readFromNbt(tag, "t");
        }
        monitorBigInt = parseBigInteger(tag.getString(TAG_MONITOR));
    }

    private static BigInteger parseBigInteger(String value) {
        if (value == null || value.isBlank()) {
            return BigInteger.ZERO;
        }
        try {
            return new BigInteger(value);
        } catch (NumberFormatException ignored) {
            return BigInteger.ZERO;
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        private static final int[] SLOTS = {
                SLOT_TRASH,
                SLOT_PAYOUT_START,
                SLOT_PAYOUT_START + 1,
                SLOT_PAYOUT_START + 2,
                SLOT_PAYOUT_START + 3,
                SLOT_PAYOUT_START + 4,
                SLOT_PAYOUT_END
        };

        @Override
        public int getSlots() {
            return SLOTS.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapSlot(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped == SLOT_TRASH ? items.insertItem(SLOT_TRASH, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped >= SLOT_PAYOUT_START && mapped <= SLOT_PAYOUT_END
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapSlot(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapSlot(slot);
            return mapped == SLOT_TRASH && items.isItemValid(SLOT_TRASH, stack);
        }

        private int mapSlot(int slot) {
            return slot < 0 || slot >= SLOTS.length ? -1 : SLOTS[slot];
        }
    }
}
