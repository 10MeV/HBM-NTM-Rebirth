package com.hbm.ntm.blockentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.menu.StrandCasterMenu;
import com.hbm.ntm.multiblock.LegacyProxyDelegateProvider;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StrandCasterBlockEntity extends HbmFluidBlockEntity
        implements MenuProvider, ICrucibleAcceptor, HbmStandardFluidTransceiver, LegacyProxyDelegateProvider {
    public static final int SLOT_MOLD = 0;
    public static final int SLOT_OUTPUT_START = 1;
    public static final int SLOT_OUTPUT_END = 6;
    public static final int SLOT_COUNT = 7;

    private static final String TAG_ITEMS = "Items";
    private static final String TAG_TYPE = "type";
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_LAST_PROGRESS = "t";
    private static final int WATER_CAPACITY = 64_000;
    private static final int STEAM_CAPACITY = 64_000;

    private final HbmFluidTank waterTank;
    private final HbmFluidTank steamTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_MOLD && FoundryMoldItem.isMold(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new ExternalItemHandler(items));
    private final IFluidHandler waterHandler;
    private final IFluidHandler steamHandler;
    private final ICapabilityProvider waterDelegate;
    private final ICapabilityProvider steamDelegate;
    private NTMMaterial type;
    private int amount;
    private long lastProgressTick;

    public StrandCasterBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WATER, WATER_CAPACITY),
                new HbmFluidTank(HbmFluids.SPENTSTEAM, STEAM_CAPACITY));
    }

    private StrandCasterBlockEntity(BlockPos pos, BlockState state, HbmFluidTank waterTank,
            HbmFluidTank steamTank) {
        super(ModBlockEntities.STRAND_CASTER.get(), pos, state, List.of(waterTank, steamTank));
        this.waterTank = waterTank;
        this.steamTank = steamTank;
        this.waterTank.setTankType(HbmFluids.WATER);
        this.steamTank.setTankType(HbmFluids.SPENTSTEAM);
        waterHandler = new ForgeFluidHandlerAdapter(List.of(waterTank), List.of(), 0, true, false,
                this::onFluidContentsChanged);
        steamHandler = new ForgeFluidHandlerAdapter(List.of(), List.of(steamTank), 0, false, true,
                this::onFluidContentsChanged);
        waterDelegate = new FluidDelegate(() -> waterHandler);
        steamDelegate = new FluidDelegate(() -> steamHandler);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StrandCasterBlockEntity caster) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = caster.tickServer(level);
        caster.networkPackNT(150);
        if (changed) {
            caster.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getWaterTank() {
        return waterTank;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public ItemStack getMold() {
        return items.getStackInSlot(SLOT_MOLD);
    }

    public void setMold(ItemStack mold) {
        items.setStackInSlot(SLOT_MOLD, mold);
        setChanged();
    }

    public ItemStack removeMold() {
        ItemStack mold = items.getStackInSlot(SLOT_MOLD);
        if (mold.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.setStackInSlot(SLOT_MOLD, ItemStack.EMPTY);
        setChanged();
        return mold.copy();
    }

    public int getMoltenAmount() {
        return amount;
    }

    public int getMoltenColor() {
        return type == null ? 0xFFFFFF : type.moltenColor;
    }

    public int getMoltenMaterialId() {
        return type == null ? -1 : type.id;
    }

    public String getMoltenMaterialName() {
        return type == null ? "" : type.names[0];
    }

    public int getCapacity() {
        FoundryMoldItem.Mold mold = getInstalledMold();
        return mold == null ? 50_000 : mold.cost() * 10;
    }

    public int getWaterRequired() {
        FoundryMoldItem.Mold mold = getInstalledMold();
        return mold == null ? 50 : 5 * mold.cost();
    }

    public ItemStack drainMoltenAsScrap() {
        if (amount <= 0 || type == null) {
            return ItemStack.EMPTY;
        }
        ItemStack scrap = FoundryScrapsItem.create(new MaterialStack(type, amount));
        amount = 0;
        type = null;
        setChanged();
        return scrap;
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>(HbmInventoryMenuHelper.clearToDrops(items));
        ItemStack scrap = drainMoltenAsScrap();
        if (!scrap.isEmpty()) {
            drops.add(scrap);
        }
        return drops;
    }

    @Nullable
    public FoundryMoldItem.Mold getInstalledMold() {
        return FoundryMoldItem.getMold(items.getStackInSlot(SLOT_MOLD));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineStrandCaster", "Strand Caster");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new StrandCasterMenu(containerId, inventory, this);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        FoundryMoldItem.Mold mold = getInstalledMold();
        return LegacyLookOverlay.forBlock(this, List.of(
                mold == null
                        ? Component.translatable("foundry.noCast").withStyle(net.minecraft.ChatFormatting.RED)
                        : mold.title().copy().withStyle(net.minecraft.ChatFormatting.BLUE)));
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(waterTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(steamTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts(getBlockState());
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
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    public long getProviderSpeed(FluidType type, int pressure) {
        return Math.max(1L, steamTank.getFill());
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        if (side != Direction.UP || stack == null || stack.material == null) {
            return false;
        }
        if (!metalPourPositions().contains(pos)) {
            return false;
        }
        FoundryMoldItem.Mold mold = getInstalledMold();
        if (mold == null || mold.getOutput(stack.material).isEmpty()) {
            return false;
        }
        if (type != null && type != stack.material) {
            return false;
        }
        return amount < mold.cost() * 9;
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        if (!canAcceptPartialPour(level, pos, hit, side, stack)) {
            return stack;
        }
        FoundryMoldItem.Mold mold = getInstalledMold();
        int limit = mold.cost() * 9;
        type = stack.material;
        int accepted = Math.min(stack.amount, limit - amount);
        amount += accepted;
        lastProgressTick = level.getGameTime();
        setChanged();
        return accepted >= stack.amount ? null : new MaterialStack(stack.material, stack.amount - accepted);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return false;
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return null;
    }

    @Nullable
    @Override
    public ICapabilityProvider getLegacyProxyDelegate(BlockPos proxyPos) {
        if (waterPortPositions().contains(proxyPos)) {
            return waterDelegate;
        }
        return steamPortPositions().contains(proxyPos) ? steamDelegate : null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        waterTank.writeToNbt(tag, "w");
        steamTank.writeToNbt(tag, "s");
        if (type != null) {
            tag.putInt(TAG_TYPE, type.id);
        }
        tag.putInt(TAG_AMOUNT, amount);
        tag.putLong(TAG_LAST_PROGRESS, lastProgressTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains("w") || tag.contains("w_type")) {
            waterTank.readFromNbt(tag, "w");
        }
        if (tag.contains("s") || tag.contains("s_type")) {
            steamTank.readFromNbt(tag, "s");
        }
        type = tag.contains(TAG_TYPE) ? Mats.matById.get(tag.getInt(TAG_TYPE)) : null;
        amount = tag.getInt(TAG_AMOUNT);
        lastProgressTick = tag.getLong(TAG_LAST_PROGRESS);
        if (amount <= 0 || type == null) {
            amount = 0;
            type = null;
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-7, 0, -7), worldPosition.offset(7, 3, 7));
    }

    private boolean tickServer(Level level) {
        int oldWater = waterTank.getFill();
        int oldSteam = steamTank.getFill();
        int oldAmount = amount;
        NTMMaterial oldType = type;

        refreshTrackedTransceiverFluidPortsReport(getReceivingTanks(), getSendingTanks(), this);
        if (steamTank.getFill() > 0) {
            tryProvideFluidToPorts(steamTank.getTankType(), steamTank.getPressure(), this);
        }
        if (amount > getCapacity()) {
            ItemStack scrap = FoundryScrapsItem.create(new MaterialStack(type, amount - getCapacity()));
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 2.0D,
                    worldPosition.getZ() + 0.5D, scrap));
            amount = getCapacity();
        }
        if (amount == 0) {
            type = null;
        }
        int processable = maxProcessable();
        if (processable > 0 && (processable >= 9 || level.getGameTime() >= lastProgressTick + 200L)) {
            cast(processable);
            lastProgressTick = level.getGameTime();
        }
        return oldWater != waterTank.getFill()
                || oldSteam != steamTank.getFill()
                || oldAmount != amount
                || oldType != type;
    }

    private int maxProcessable() {
        FoundryMoldItem.Mold mold = getInstalledMold();
        if (type == null || mold == null) {
            return 0;
        }
        ItemStack output = mold.getOutput(type);
        if (output.isEmpty()) {
            return 0;
        }
        int freeSlots = 0;
        int stackLimit = output.getMaxStackSize();
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            ItemStack current = items.getStackInSlot(slot);
            if (current.isEmpty()) {
                freeSlots += stackLimit;
            } else if (ItemStack.isSameItemSameTags(current, output)) {
                freeSlots += stackLimit - current.getCount();
            }
        }
        int count = amount / mold.cost();
        count = Math.min(count, freeSlots / output.getCount());
        count = Math.min(count, waterTank.getFill() / getWaterRequired());
        count = Math.min(count, steamTank.getSpace() / getWaterRequired());
        return count;
    }

    private void cast(int count) {
        FoundryMoldItem.Mold mold = getInstalledMold();
        if (mold == null || type == null || count <= 0) {
            return;
        }
        ItemStack output = mold.getOutput(type);
        if (output.isEmpty()) {
            return;
        }
        amount -= count * mold.cost();
        int remaining = output.getCount() * count;
        int maxStackSize = output.getMaxStackSize();
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && remaining > 0; slot++) {
            ItemStack current = items.getStackInSlot(slot);
            if (current.isEmpty()) {
                current = output.copy();
                current.setCount(0);
                items.setStackInSlot(slot, current);
            }
            if (ItemStack.isSameItemSameTags(current, output)) {
                int moved = Math.min(remaining, maxStackSize - current.getCount());
                current.grow(moved);
                remaining -= moved;
            }
        }
        waterTank.setFill(waterTank.getFill() - getWaterRequired() * count);
        steamTank.setFill(steamTank.getFill() + getWaterRequired() * count);
        if (amount <= 0) {
            amount = 0;
            type = null;
        }
    }

    private List<BlockPos> fluidPortPositions() {
        List<BlockPos> ports = new ArrayList<>(waterPortPositions());
        ports.addAll(steamPortPositions());
        return ports;
    }

    private List<BlockPos> waterPortPositions() {
        Direction facing = facing(getBlockState());
        Direction rot = facing.getClockWise();
        return List.of(
                worldPosition.offset(relative(facing, rot, -1, 2, 0)),
                worldPosition.offset(relative(facing, rot, -5, 2, 0)));
    }

    private List<BlockPos> steamPortPositions() {
        Direction facing = facing(getBlockState());
        Direction rot = facing.getClockWise();
        return List.of(
                worldPosition.offset(relative(facing, rot, -1, -1, 0)),
                worldPosition.offset(relative(facing, rot, -5, -1, 0)));
    }

    private List<BlockPos> metalPourPositions() {
        Direction facing = facing(getBlockState());
        Direction rot = facing.getClockWise();
        return List.of(
                worldPosition.offset(relative(facing, rot, -1, 1, 2)),
                worldPosition.offset(relative(facing, rot, -1, 0, 2)),
                worldPosition.offset(relative(facing, rot, 0, 1, 2)),
                worldPosition.offset(relative(facing, rot, 0, 0, 2)));
    }

    private static List<FluidPort> fluidPorts(BlockState state) {
        Direction facing = facing(state);
        Direction rot = facing.getClockWise();
        return List.of(
                fluidPort(relative(facing, rot, -1, 2, 0), rot),
                fluidPort(relative(facing, rot, -1, -1, 0), rot.getOpposite()),
                fluidPort(relative(facing, rot, -5, 2, 0), rot),
                fluidPort(relative(facing, rot, -5, -1, 0), rot.getOpposite()));
    }

    private static Direction facing(BlockState state) {
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    private static BlockPos relative(Direction facing, Direction rot, int forward, int side, int y) {
        return new BlockPos(
                facing.getStepX() * forward + rot.getStepX() * side,
                y,
                facing.getStepZ() * forward + rot.getStepZ() * side);
    }

    private static FluidPort fluidPort(BlockPos offset, Direction side) {
        return FluidPort.of(offset.getX(), offset.getY(), offset.getZ(), side);
    }

    private record FluidDelegate(java.util.function.Supplier<IFluidHandler> handler) implements ICapabilityProvider {
        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
            return capability == ForgeCapabilities.FLUID_HANDLER
                    ? LazyOptional.of(handler::get).cast()
                    : LazyOptional.empty();
        }
    }

    private static final class ExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private ExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots()
                    ? items.getStackInSlot(SLOT_OUTPUT_START + slot)
                    : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= 0 && slot < getSlots()
                    ? items.extractItem(SLOT_OUTPUT_START + slot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < getSlots() ? items.getSlotLimit(SLOT_OUTPUT_START + slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
