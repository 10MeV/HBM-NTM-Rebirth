package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fusion.FusionPowerReceiver;
import com.hbm.ntm.menu.FusionBreederMenu;
import com.hbm.ntm.recipe.FusionFluidBreederRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.OutgasserRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.ntm.uninos.networkproviders.PlasmaNode;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FusionBreederBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, FusionPowerReceiver {
    public static final int SLOT_FLUID_ID = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int TANK_CAPACITY = 16_000;
    public static final double CAPACITY = 10_000.0D;
    private static final String TAG_NEUTRON_ENERGY_SYNC = "neutronEnergySync";
    private static final String TAG_PROGRESS = "progress";

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_FLUID_ID ? 1 : 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FLUID_ID -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_INPUT -> level == null || isSpecialMeteoriteSwordInput(stack) || hasFusionSolidRecipe(stack);
                default -> false;
            };
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private PlasmaNode plasmaNode;
    private double neutronEnergy;
    private double neutronEnergySync;
    private double progress;

    public FusionBreederBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY));
    }

    private FusionBreederBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank outputTank) {
        super(ModBlockEntities.FUSION_BREEDER.get(), pos, state, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionBreederBlockEntity breeder) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, breeder);
        boolean changed = breeder.tickServer(level);
        breeder.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            breeder.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() { return items; }
    public HbmFluidTank getInputTank() { return inputTank; }
    public HbmFluidTank getOutputTank() { return outputTank; }
    public double getNeutronEnergySync() { return neutronEnergySync; }
    public double getProgress() { return progress; }
    public List<ItemStack> getDrops() { return HbmInventoryMenuHelper.clearToDrops(items); }

    @Override
    public boolean receivesFusionPower() {
        return false;
    }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        neutronEnergy = neutronPower;
        doProgress();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.fusionBreeder", "Fusion Reactor Breeder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FusionBreederMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank);
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
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX() * 3, 2, facing.getStepZ() * 3, facing),
                FluidPort.of(rot.getStepX() * 2, 0, rot.getStepZ() * 2, rot),
                FluidPort.of(-rot.getStepX() * 2, 0, -rot.getStepZ() * 2, rot.getOpposite()),
                FluidPort.of(facing.getStepX() + rot.getStepX() * 2, 0,
                        facing.getStepZ() + rot.getStepZ() * 2, rot),
                FluidPort.of(facing.getStepX() - rot.getStepX() * 2, 0,
                        facing.getStepZ() - rot.getStepZ() * 2, rot.getOpposite()));
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == inputTank.getTankType() && type != HbmFluids.NONE;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == outputTank.getTankType() && outputTank.getFill() > 0;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-2, 0, -2), worldPosition.offset(3, 4, 3));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        tag.putDouble(TAG_PROGRESS, progress);
        inputTank.writeToNbt(tag, "t0");
        outputTank.writeToNbt(tag, "t1");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        progress = tag.getDouble(TAG_PROGRESS);
        inputTank.readFromNbt(tag, "t0");
        outputTank.readFromNbt(tag, "t1");
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putDouble(TAG_NEUTRON_ENERGY_SYNC, neutronEnergySync);
        tag.putDouble(TAG_PROGRESS, progress);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_NEUTRON_ENERGY_SYNC)) {
            neutronEnergySync = tag.getDouble(TAG_NEUTRON_ENERGY_SYNC);
            neutronEnergy = neutronEnergySync;
        }
        if (tag.contains(TAG_PROGRESS)) {
            progress = tag.getDouble(TAG_PROGRESS);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && plasmaNode != null) {
            PlasmaNodespace.destroyNode(level, plasmaNode.getPos());
        }
        super.setRemoved();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private boolean tickServer(Level level) {
        boolean changed = setFluidTankTypeFromIdentifierSlotReport(items, SLOT_FLUID_ID, SLOT_FLUID_ID, inputTank,
                0, false).changed();
        neutronEnergySync = neutronEnergy;
        ensureNode(level);
        if (inputTank.getTankType() != HbmFluids.NONE) {
            refreshTrackedReceiverFluidPortsReport(List.of(inputTank), this);
        }
        if (outputTank.getFill() > 0) {
            tryProvideFluidToPorts(outputTank.getTankType(), outputTank.getPressure(), this);
        }
        if (!canProcessSolid() && !canProcessLiquid()) {
            progress = 0.0D;
        }
        neutronEnergy = 0.0D;
        return changed;
    }

    private void ensureNode(Level level) {
        Direction direction = facing().getOpposite();
        BlockPos nodePos = worldPosition.relative(direction, 2).above(2);
        if (plasmaNode == null || plasmaNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, nodePos);
            plasmaNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(nodePos, Set.of(direction)))
                    : existing;
        }
        PlasmaNetwork net = plasmaNode.getPlasmaNet();
        if (net != null) {
            net.addReceiver(this);
        }
    }

    private void doProgress() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (canProcessSolid() || canProcessLiquid()) {
            progress += neutronEnergy;
            if (progress > CAPACITY) {
                if (!processSolid()) {
                    processLiquid();
                }
                progress = 0.0D;
                setChanged();
            }
        } else {
            progress = 0.0D;
        }
    }

    private boolean canProcessSolid() {
        return canProcessSpecialMeteoriteSword() || findSolidRecipe() != null;
    }

    private boolean processSolid() {
        if (canProcessSpecialMeteoriteSword()) {
            return processSpecialMeteoriteSword();
        }
        OutgasserRecipe recipe = findSolidRecipe();
        if (recipe == null) {
            return false;
        }
        items.extractItem(SLOT_INPUT, 1, false);
        recipe.fluidOutput().ifPresent(output -> outputTank.fill(output.type(), output.amount(), output.pressure(), false));
        recipe.solidOutput().ifPresent(this::mergeOutput);
        return true;
    }

    @Nullable
    private OutgasserRecipe findSolidRecipe() {
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (input.isEmpty() || level == null) {
            return null;
        }
        for (OutgasserRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.OUTGASSER.type().get())) {
            if (!recipe.matchesFusion(input)) {
                continue;
            }
            if (recipe.fluidOutput().isPresent() && !canFitOutputFluid(recipe.fluidOutput().get())) {
                continue;
            }
            if (recipe.solidOutput().isPresent() && !canFitOutputItem(recipe.solidOutput().get())) {
                continue;
            }
            return recipe;
        }
        return null;
    }

    private boolean hasFusionSolidRecipe(ItemStack input) {
        if (input.isEmpty() || level == null) {
            return false;
        }
        for (OutgasserRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.OUTGASSER.type().get())) {
            if (recipe.matchesFusion(input)) {
                return true;
            }
        }
        return false;
    }

    private boolean canProcessSpecialMeteoriteSword() {
        return isSpecialMeteoriteSwordInput(items.getStackInSlot(SLOT_INPUT))
                && items.getStackInSlot(SLOT_OUTPUT).isEmpty()
                && ModItems.legacyItem("meteorite_sword_fused") != null;
    }

    private static boolean isSpecialMeteoriteSwordInput(ItemStack stack) {
        return java.util.Optional.ofNullable(ModItems.legacyItem("meteorite_sword_irradiated"))
                .map(item -> stack.is(item.get()))
                .orElse(false);
    }

    private boolean processSpecialMeteoriteSword() {
        return java.util.Optional.ofNullable(ModItems.legacyItem("meteorite_sword_fused"))
                .map(item -> {
                    items.extractItem(SLOT_INPUT, 1, false);
                    items.setStackInSlot(SLOT_OUTPUT, new ItemStack(item.get()));
                    return true;
                })
                .orElse(false);
    }

    private boolean canProcessLiquid() {
        return findLiquidRecipe() != null;
    }

    private boolean processLiquid() {
        FusionFluidBreederRecipe recipe = findLiquidRecipe();
        if (recipe == null) {
            return false;
        }
        inputTank.drain(recipe.input().amount(), false);
        outputTank.fill(recipe.output().type(), recipe.output().amount(), recipe.output().pressure(), false);
        return true;
    }

    @Nullable
    private FusionFluidBreederRecipe findLiquidRecipe() {
        if (level == null || inputTank.isEmpty()) {
            return null;
        }
        for (FusionFluidBreederRecipe recipe : level.getRecipeManager().getAllRecipesFor(
                ModRecipes.FUSION_FLUID_BREEDER.type().get())) {
            if (recipe.matches(inputTank.getTankType(), inputTank.getFill()) && canFitOutputFluid(recipe.output())) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canFitOutputFluid(HbmFluidStack output) {
        return outputTank.getSpaceFor(output.type()) >= output.amount();
    }

    private boolean canFitOutputItem(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        return existing.isEmpty() || (ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize());
    }

    private void mergeOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, output.copy());
        } else if (ItemStack.isSameItemSameTags(existing, output)) {
            existing.grow(output.getCount());
            items.setStackInSlot(SLOT_OUTPUT, existing);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return items.getStackInSlot(slot); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == SLOT_OUTPUT ? stack : items.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_OUTPUT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) { return items.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return items.isItemValid(slot, stack); }
    }
}
