package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidItemTransfer.TankSlotTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import com.hbm.ntm.blockentity.RefineryBlockEntity.ExtinguishType;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.menu.FluidTankMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidTankBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmStandardFluidSender, HbmLegacyButtonReceiver,
        HbmPersistentBlockState {
    public static final int SLOT_TYPE_INPUT = 0;
    public static final int SLOT_TYPE_OUTPUT = 1;
    public static final int SLOT_LOAD_INPUT = 2;
    public static final int SLOT_LOAD_OUTPUT = 3;
    public static final int SLOT_UNLOAD_INPUT = 4;
    public static final int SLOT_UNLOAD_OUTPUT = 5;
    public static final int CONTROL_MODE = 0;

    public static final int MODE_INPUT = 0;
    public static final int MODE_BUFFER = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_NONE = 3;

    protected static final int DEFAULT_TANK_CAPACITY = 256_000;
    private static final long DEFAULT_TRANSFER_SPEED_FLOOR = 500L;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(2, 0, -1, Direction.EAST),
            FluidPort.of(2, 0, 1, Direction.EAST),
            FluidPort.of(-2, 0, -1, Direction.WEST),
            FluidPort.of(-2, 0, 1, Direction.WEST),
            FluidPort.of(-1, 0, 2, Direction.SOUTH),
            FluidPort.of(1, 0, 2, Direction.SOUTH),
            FluidPort.of(-1, 0, -2, Direction.NORTH),
            FluidPort.of(1, 0, -2, Direction.NORTH));

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(this::getExternalItemHandler);

    private int mode = MODE_INPUT;
    private boolean exploded;
    private boolean onFire;
    private int age;
    private int lastComparatorPower;
    private Explosion lastExplosion;

    public FluidTankBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, DEFAULT_TANK_CAPACITY));
    }

    protected FluidTankBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        this(pos, state, ModBlockEntities.FLUID_TANK.get(), tank);
    }

    protected FluidTankBlockEntity(BlockPos pos, BlockState state, BlockEntityType<?> type, HbmFluidTank tank) {
        super(type, pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidTankBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
        boolean changed = false;

        if (!blockEntity.exploded) {
            blockEntity.age = (blockEntity.age + 1) % 20;
            changed |= blockEntity.handleItemTransfer();
            if (blockEntity.mode == MODE_OUTPUT && !blockEntity.tank.isEmpty()) {
                blockEntity.tryProvideFluidToPorts(blockEntity.tank.getTankType(), blockEntity.tank.getPressure(), blockEntity);
            }
            changed |= blockEntity.checkHazards();
        } else {
            changed |= blockEntity.leakDamagedTank(level, pos);
        }

        int comparator = blockEntity.getComparatorPower();
        if (comparator != blockEntity.lastComparatorPower) {
            blockEntity.lastComparatorPower = comparator;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
            changed = true;
        }

        if (changed || blockEntity.age == 0) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    protected boolean handleItemTransfer() {
        boolean changed = false;
        changed |= setTankTypeFromIdentifierSlot();
        changed |= HbmFluidItemTransfer.processTransfers(items, List.of(
                TankSlotTransfer.load(SLOT_LOAD_INPUT, SLOT_LOAD_OUTPUT, tank),
                TankSlotTransfer.unload(SLOT_UNLOAD_INPUT, SLOT_UNLOAD_OUTPUT, tank)));
        return changed;
    }

    protected boolean setTankTypeFromIdentifierSlot() {
        ItemStack input = items.getStackInSlot(SLOT_TYPE_INPUT);
        if (input.isEmpty() || !(input.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        ItemStack output = items.getStackInSlot(SLOT_TYPE_OUTPUT);
        if (!output.isEmpty()) {
            return false;
        }
        FluidType newType = identifier.getIdentifiedFluid(level, worldPosition, input);
        if (newType == null || newType == tank.getTankType()) {
            return false;
        }
        tank.setTankType(newType);
        items.setStackInSlot(SLOT_TYPE_OUTPUT, input.copy());
        items.setStackInSlot(SLOT_TYPE_INPUT, ItemStack.EMPTY);
        onFluidContentsChanged();
        return true;
    }

    protected boolean checkHazards() {
        if (tank.isEmpty()) {
            return false;
        }
        FluidType type = tank.getTankType();
        if (type.isAntimatter()) {
            explodeAntimatterContents();
            explodeTank();
            tank.setFill(0);
            return true;
        }
        CorrosiveFluidTrait corrosive = type.getTrait(CorrosiveFluidTrait.class);
        if (corrosive != null && corrosive.isHighlyCorrosive()) {
            explodeTank();
            return true;
        }
        return false;
    }

    protected boolean leakDamagedTank(Level level, BlockPos pos) {
        if (tank.isEmpty()) {
            return false;
        }
        FluidType type = tank.getTankType();
        int leaking;
        if (type.isAntimatter()) {
            leaking = tank.getFill();
        } else if (type.hasTrait(SimpleFluidTraits.Gaseous.class)
                || type.hasTrait(SimpleFluidTraits.GaseousAtRoomTemperature.class)) {
            leaking = Math.min(tank.getFill(), tank.getMaxFill() / 100);
        } else {
            leaking = Math.min(tank.getFill(), tank.getMaxFill() / 10_000);
        }
        if (leaking <= 0) {
            return false;
        }
        tank.release(level, pos, leaking, onFire ? FluidReleaseType.BURN : FluidReleaseType.SPILL, false);
        return true;
    }

    public void explodeTank() {
        if (exploded) {
            return;
        }
        exploded = true;
        onFire = tank.getTankType().hasTrait(FlammableFluidTrait.class);
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    protected void explodeAntimatterContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        new ExplosionVnt(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D,
                worldPosition.getZ() + 0.5D, 5.0F)
                .makeAmat()
                .setBlockAllocator(null)
                .setBlockProcessor(null)
                .explode();
    }

    public void cycleMode() {
        setMode((mode + 1) % 4);
    }

    public void setMode(int mode) {
        int clamped = Math.max(MODE_INPUT, Math.min(MODE_NONE, mode));
        if (this.mode != clamped) {
            this.mode = clamped;
            invalidateFluidHandlers();
            onFluidContentsChanged();
            if (level != null) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
    }

    public boolean setIdentifiedType(FluidType newType) {
        if (newType == null || newType == tank.getTankType()) {
            return false;
        }
        tank.setTankType(newType);
        onFluidContentsChanged();
        return true;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    protected IItemHandler getExternalItemHandler() {
        return EmptyItemHandler.INSTANCE;
    }

    protected IItemHandler getTankContainerAutomationItemHandler() {
        return new TankContainerAutomationItemHandler();
    }

    public int getMode() {
        return mode;
    }

    @Override
    public int[] getFluidIdsToCopy() {
        FluidType type = tank.getTankType();
        return type == null || type.hasNoId() ? new int[0] : new int[] {type.getId()};
    }

    @Nullable
    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return tank;
    }

    public boolean isExploded() {
        return exploded;
    }

    public boolean isOnFire() {
        return onFire;
    }

    protected boolean hasDamageState() {
        return true;
    }

    public void repairTank() {
        if (!exploded) {
            return;
        }
        exploded = false;
        onFire = false;
        invalidateFluidHandlers();
        onFluidContentsChanged();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public boolean markExplosionHandled(Explosion explosion) {
        if (lastExplosion == explosion) {
            return false;
        }
        lastExplosion = explosion;
        return true;
    }

    public boolean usesExternalExplosionDamageChain() {
        return hasDamageState();
    }

    public void tryExtinguish(ExtinguishType type) {
        if (!exploded || !onFire) {
            return;
        }
        if (type == ExtinguishType.WATER) {
            if (tank.getTankType().hasTrait(SimpleFluidTraits.Liquid.class) && level != null) {
                level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D,
                        worldPosition.getZ() + 0.5D, 5.0F, Level.ExplosionInteraction.TNT);
            } else {
                onFire = false;
                onFluidContentsChanged();
            }
            return;
        }
        if (type == ExtinguishType.FOAM || type == ExtinguishType.CO2) {
            onFire = false;
            onFluidContentsChanged();
        }
    }

    public int getComparatorPower() {
        if (tank.getFill() == 0 || tank.getMaxFill() <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(15, (int) ((double) tank.getFill() / (double) tank.getMaxFill() * 15.0D) + 1));
    }

    public int getTankFillHeight(int maxHeight) {
        return tank.getMaxFill() <= 0 ? 0 : tank.getFill() * maxHeight / tank.getMaxFill();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return !exploded && (mode == MODE_INPUT || mode == MODE_BUFFER) ? List.of(tank) : List.of();
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return !exploded && (mode == MODE_BUFFER || mode == MODE_OUTPUT) ? List.of(tank) : List.of();
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (exploded || (mode != MODE_INPUT && mode != MODE_BUFFER) || amount <= 0L || !tank.canAccept(type, pressure)) {
            return amount;
        }
        int accepted = tank.fill(type, (int) Math.min(Integer.MAX_VALUE, amount), pressure, false);
        if (accepted > 0) {
            onFluidContentsChanged();
        }
        return amount - accepted;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if (exploded || (mode != MODE_INPUT && mode != MODE_BUFFER) || !tank.canAccept(type, pressure)) {
            return 0L;
        }
        return tank.getSpace();
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return Math.max(getTransferSpeedFloor(), tank.getSpace() / 100L);
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    public long getProviderSpeed(FluidType type, int pressure) {
        return Math.max(getTransferSpeedFloor(), tank.getFill() / 100L);
    }

    @Override
    public HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return mode == MODE_BUFFER
                ? HbmEnergyReceiver.ConnectionPriority.LOW
                : HbmEnergyReceiver.ConnectionPriority.NORMAL;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return !exploded && mode == MODE_BUFFER && !tank.isEmpty();
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return !exploded && mode == MODE_BUFFER && type == tank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return !exploded && (mode == MODE_BUFFER || mode == MODE_OUTPUT) && tank.getTankType() == type && tank.getFill() > 0;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return !exploded && (mode == MODE_INPUT || mode == MODE_BUFFER) && tank.getTankType() == type;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    protected long getTransferSpeedFloor() {
        return DEFAULT_TRANSFER_SPEED_FLOOR;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (exploded) {
            return HbmFluidSideMode.NONE;
        }
        return switch (mode) {
            case MODE_INPUT -> HbmFluidSideMode.INPUT;
            case MODE_BUFFER -> HbmFluidSideMode.BOTH;
            case MODE_OUTPUT -> HbmFluidSideMode.OUTPUT;
            default -> HbmFluidSideMode.NONE;
        };
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return !exploded && side != null && type != null && type != HbmFluids.NONE
                && (mode == MODE_INPUT || mode == MODE_BUFFER || mode == MODE_OUTPUT);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.fluidtank");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FluidTankMenu(id, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_MODE;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_MODE) {
            cycleMode();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", items.serializeNBT());
        tag.putInt("mode", mode);
        tag.putBoolean("exploded", exploded);
        tag.putBoolean("onFire", onFire);
        tag.putInt("age", age);
        tag.putInt("lastComparatorPower", lastComparatorPower);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.deserializeNBT(tag.getCompound("Inventory"));
        mode = Math.max(MODE_INPUT, Math.min(MODE_NONE, tag.getInt("mode")));
        exploded = tag.getBoolean("exploded");
        onFire = tag.getBoolean("onFire");
        age = Math.floorMod(tag.getInt("age"), 20);
        lastComparatorPower = Math.max(0, Math.min(15, tag.getInt("lastComparatorPower")));
        invalidateFluidHandlers();
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
    public void writePersistentState(CompoundTag persistent) {
        if (tank.getFill() == 0 && (!hasDamageState() || !exploded)) {
            return;
        }
        tank.writeToNbt(persistent, "tank");
        persistent.putShort("mode", (short) mode);
        if (hasDamageState()) {
            persistent.putBoolean("hasExploded", exploded);
            persistent.putBoolean("onFire", onFire);
        }
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        tank.readFromNbt(persistent, "tank");
        mode = Math.max(MODE_INPUT, Math.min(MODE_NONE, persistent.getShort("mode")));
        exploded = hasDamageState() && persistent.getBoolean("hasExploded");
        onFire = hasDamageState() && persistent.getBoolean("onFire");
        invalidateFluidHandlers();
        refreshFluidNodeState();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public ItemStack createPersistentBlockDrop(Item item) {
        ItemStack stack = new ItemStack(item);
        writePersistentStateToStack(stack);
        return stack;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new java.util.ArrayList<>();
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        return drops;
    }

    protected void copyInventoryFrom(FluidTankBlockEntity other) {
        for (int slot = 0; slot < items.getSlots() && slot < other.items.getSlots(); slot++) {
            items.setStackInSlot(slot, other.items.getStackInSlot(slot).copy());
            other.items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private class TankContainerAutomationItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return items.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return isAutomationSlot(slot) ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != SLOT_LOAD_OUTPUT && slot != SLOT_UNLOAD_OUTPUT) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return isAutomationSlot(slot) ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_LOAD_INPUT) {
                return wouldLoadFluid(stack);
            }
            if (slot == SLOT_UNLOAD_INPUT) {
                return wouldUnloadFluid(stack);
            }
            return false;
        }

        private boolean isAutomationSlot(int slot) {
            return slot == SLOT_LOAD_INPUT || slot == SLOT_LOAD_OUTPUT
                    || slot == SLOT_UNLOAD_INPUT || slot == SLOT_UNLOAD_OUTPUT;
        }

        private boolean wouldLoadFluid(ItemStack stack) {
            if (stack.isEmpty() || stack.getCount() <= 0) {
                return false;
            }
            ItemStackHandlerPreview preview = new ItemStackHandlerPreview(SLOT_LOAD_INPUT, stack);
            return HbmFluidItemTransfer.loadTankFromSlot(preview, SLOT_LOAD_INPUT, SLOT_LOAD_OUTPUT,
                    tank, Integer.MAX_VALUE, true);
        }

        private boolean wouldUnloadFluid(ItemStack stack) {
            if (stack.isEmpty() || stack.getCount() <= 0) {
                return false;
            }
            ItemStackHandlerPreview preview = new ItemStackHandlerPreview(SLOT_UNLOAD_INPUT, stack);
            return HbmFluidItemTransfer.unloadTankToSlot(preview, SLOT_UNLOAD_INPUT, SLOT_UNLOAD_OUTPUT,
                    tank, Integer.MAX_VALUE, true);
        }
    }

    private static class ItemStackHandlerPreview extends ItemStackHandler {
        ItemStackHandlerPreview(int slot, ItemStack stack) {
            super(6);
            setStackInSlot(slot, stack.copy());
        }
    }

    private enum EmptyItemHandler implements IItemHandler {
        INSTANCE;

        @Override
        public int getSlots() {
            return 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    }
}
