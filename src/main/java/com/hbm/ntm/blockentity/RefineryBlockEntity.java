package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmExtinguishType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidRepairMaterials;
import com.hbm.ntm.fluid.HbmFluidRepairMaterials.HbmRepairMaterial;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidOverpressurable;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluidRepairable;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.RefineryRecipe;
import com.hbm.ntm.menu.RefineryMenu;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class RefineryBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmPersistentBlockState, HbmStandardFluidTransceiver, HbmFluidOverpressurable,
        HbmFluidRepairable, LegacyLookOverlayProvider, MenuProvider {
    private static final String TAG_SULFUR = "sulfur";
    private static final String TAG_EXPLODED = "exploded";
    private static final String TAG_ON_FIRE = "onFire";
    private static final String TAG_INVENTORY = HbmInventoryMenuHelper.LEGACY_ITEMS_TAG;
    private static final String TAG_MODERN_INVENTORY = "Inventory";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_LEGACY_POWER = "power";
    private static final int MAX_SULFUR = 10;
    private static final long POWER_PER_OPERATION = 5L;
    private static final int INPUT_PER_OPERATION = 100;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_CONTAINER = 1;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 2;
    public static final int SLOT_HEAVY_CONTAINER = 3;
    public static final int SLOT_HEAVY_CONTAINER_OUTPUT = 4;
    public static final int SLOT_NAPHTHA_CONTAINER = 5;
    public static final int SLOT_NAPHTHA_CONTAINER_OUTPUT = 6;
    public static final int SLOT_LIGHT_CONTAINER = 7;
    public static final int SLOT_LIGHT_CONTAINER_OUTPUT = 8;
    public static final int SLOT_PETROLEUM_CONTAINER = 9;
    public static final int SLOT_PETROLEUM_CONTAINER_OUTPUT = 10;
    public static final int SLOT_SOLID_OUTPUT = 11;
    public static final int SLOT_IDENTIFIER = 12;
    public static final int ITEM_COUNT = 13;

    private boolean exploded;
    private boolean onFire;
    private boolean isOn;
    private int sulfur;
    private int audioTime;
    private Explosion lastExplosion;
    private Object audioLoop;
    @Nullable
    private String customName;
    private final ItemStackHandler items = new ItemStackHandler(ITEM_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_INPUT_CONTAINER, SLOT_HEAVY_CONTAINER, SLOT_NAPHTHA_CONTAINER, SLOT_LIGHT_CONTAINER,
                     SLOT_PETROLEUM_CONTAINER -> true;
                default -> slot == SLOT_BATTERY && stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalItemHandler = LazyOptional.of(() -> new RefineryExternalItemHandler(items));

    public RefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFINERY.get(), pos, state, new HbmEnergyStorage(1_000L, 1_000L, 0L),
                List.of(
                        new HbmFluidTank(HbmFluids.HOTOIL, 64_000),
                        new HbmFluidTank(HbmFluids.HEAVYOIL, 24_000),
                        new HbmFluidTank(HbmFluids.NAPHTHA, 24_000),
                        new HbmFluidTank(HbmFluids.LIGHTOIL, 24_000),
                        new HbmFluidTank(HbmFluids.PETROLEUM, 24_000)));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity refinery) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, refinery);
        boolean changed = false;
        if (refinery.exploded && refinery.onFire) {
            changed = refinery.burnResidualFluid();
        } else if (!refinery.exploded) {
            changed |= refinery.tickRefinery();
        }
        if (changed) {
            refinery.setChanged();
        }
        refinery.networkPackNT(150);
        if (changed) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, RefineryBlockEntity refinery) {
        refinery.updateAudioLoop();
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return exploded ? List.of() : List.of(getAllTanks().get(0));
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return exploded ? List.of() : getAllTanks().subList(1, 5);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (exploded || side == Direction.DOWN) {
            return HbmFluidSideMode.NONE;
        }
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return exploded ? List.of() : List.of(getAllTanks().get(0));
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return exploded ? List.of() : getAllTanks().subList(1, 5);
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return !exploded && type == getAllTanks().get(0).getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return !exploded && getSendingTanks().stream()
                .anyMatch(tank -> tank.getTankType() == type && tank.getFill() > 0);
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return HbmFluidPortLayouts.squareSidesWithoutCorners(2);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(
                EnergyPort.of(2, 0, 1, Direction.EAST),
                EnergyPort.of(2, 0, -1, Direction.EAST),
                EnergyPort.of(-2, 0, 1, Direction.WEST),
                EnergyPort.of(-2, 0, -1, Direction.WEST),
                EnergyPort.of(1, 0, 2, Direction.SOUTH),
                EnergyPort.of(-1, 0, 2, Direction.SOUTH),
                EnergyPort.of(1, 0, -2, Direction.NORTH),
                EnergyPort.of(-1, 0, -2, Direction.NORTH));
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
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return exploded ? HbmEnergySideMode.NONE : HbmEnergySideMode.INPUT;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return !exploded && side != null && side != Direction.DOWN
                && type != null && getFluidNodeTypes().contains(type);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public boolean isProcessing() {
        return isOn;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_LEGACY_POWER, getPower());
        tag.putInt(TAG_SULFUR, sulfur);
        tag.putBoolean(TAG_EXPLODED, exploded);
        tag.putBoolean(TAG_ON_FIRE, onFire);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
        getAllTanks().get(0).writeToNbt(tag, "input");
        getAllTanks().get(1).writeToNbt(tag, "heavy");
        getAllTanks().get(2).writeToNbt(tag, "naphtha");
        getAllTanks().get(3).writeToNbt(tag, "light");
        getAllTanks().get(4).writeToNbt(tag, "petroleum");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_LEGACY_POWER)) {
            setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        sulfur = tag.getInt(TAG_SULFUR);
        exploded = tag.getBoolean(TAG_EXPLODED);
        onFire = tag.getBoolean(TAG_ON_FIRE);
        loadInventory(tag);
        customName = tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING) ? tag.getString(TAG_CUSTOM_NAME) : null;
        getAllTanks().get(0).readFromNbt(tag, "input");
        getAllTanks().get(1).readFromNbt(tag, "heavy");
        getAllTanks().get(2).readFromNbt(tag, "naphtha");
        getAllTanks().get(3).readFromNbt(tag, "light");
        getAllTanks().get(4).readFromNbt(tag, "petroleum");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putBoolean(TAG_IS_ON, isOn);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        isOn = tag.getBoolean(TAG_IS_ON);
        if (isOn) {
            audioTime = 20;
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

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        if (getAllTanks().stream().allMatch(tank -> tank.getFill() == 0) && !exploded) {
            return;
        }
        HbmPersistentBlockState.writeIndexedTanks(persistent, getAllTanks());
        persistent.putBoolean("hasExploded", exploded);
        persistent.putBoolean(TAG_ON_FIRE, onFire);
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        HbmPersistentBlockState.readIndexedTanks(persistent, getAllTanks());
        exploded = persistent.getBoolean("hasExploded");
        onFire = persistent.getBoolean(TAG_ON_FIRE);
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    public ItemStack createPersistentBlockDrop(Item item) {
        ItemStack stack = new ItemStack(item);
        writePersistentStateToStack(stack);
        return stack;
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.machineRefinery", "Refinery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return exploded ? null : new RefineryMenu(containerId, inventory, this);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !exploded) {
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

    public boolean isExploded() {
        return exploded;
    }

    public boolean isOnFire() {
        return onFire;
    }

    public boolean hasStoredFluid() {
        return getAllTanks().stream().anyMatch(tank -> tank.getFill() > 0);
    }

    public void explode() {
        if (exploded) {
            return;
        }
        exploded = true;
        onFire = true;
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public boolean markExplosionHandled(Explosion explosion) {
        if (lastExplosion == explosion) {
            return false;
        }
        lastExplosion = explosion;
        return true;
    }

    public void tryExtinguish(HbmExtinguishType type) {
        if (!exploded || !onFire) {
            return;
        }
        if (type == HbmExtinguishType.FOAM || type == HbmExtinguishType.CO2) {
            onFire = false;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
            return;
        }
        if (type == HbmExtinguishType.WATER && level != null && hasStoredFluid()) {
            level.explode(null, worldPosition.getX() + 0.5D, worldPosition.getY() + 1.5D,
                    worldPosition.getZ() + 0.5D, 5.0F, Level.ExplosionInteraction.TNT);
        }
    }

    public void repair() {
        exploded = false;
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean isDamagedForFluidRepair() {
        return exploded;
    }

    @Override
    public List<HbmRepairMaterial> getFluidRepairMaterials() {
        return List.of(
                HbmFluidRepairMaterials.item(ModItems.STEEL_PLATE.get(), 8),
                HbmFluidRepairMaterials.optionalLegacyItem("ducttape", 4,
                        HbmFluidRepairMaterials.UNRESOLVED_DUCT_TAPE));
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos) {
        if (player == null || !player.getMainHandItem().is(ModItems.BLOWTORCH.get())
                || !isDamagedForFluidRepair()) {
            return null;
        }
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.repairMaterials(getFluidRepairMaterials()));
    }

    @Override
    public void repairFluidMachine() {
        repair();
    }

    @Override
    public void explodeFromFluidOverpressure(Level level, BlockPos pos) {
        explode();
    }

    private boolean burnResidualFluid() {
        boolean changed = false;
        for (HbmFluidTank tank : getAllTanks()) {
            int fill = tank.getFill();
            if (fill > 0) {
                tank.setFill(Math.max(fill - 10, 0));
                changed = true;
            }
        }
        if (changed && level != null) {
            burnResidualArea();
            if (level.getGameTime() % 20L == 0L) {
                PollutionManager.incrementPollution(level, worldPosition, PollutionType.SOOT,
                        PollutionManager.SOOT_PER_SECOND * 70.0F);
            }
        }
        return changed;
    }

    private void burnResidualArea() {
        AABB area = new AABB(worldPosition.getX() - 1.5D, worldPosition.getY(), worldPosition.getZ() - 1.5D,
                worldPosition.getX() + 2.5D, worldPosition.getY() + 8.0D, worldPosition.getZ() + 2.5D);
        for (Entity entity : level.getEntities(null, area)) {
            entity.setSecondsOnFire(5);
        }
        ParticleUtil.spawnGasFlame(level,
                worldPosition.getX() + level.random.nextDouble(),
                worldPosition.getY() + 1.5D + level.random.nextDouble() * 3.0D,
                worldPosition.getZ() + level.random.nextDouble(),
                level.random.nextGaussian() * 0.05D,
                0.1D,
                level.random.nextGaussian() * 0.05D);
    }

    private boolean tickRefinery() {
        boolean changed = false;
        boolean oldOn = isOn;
        isOn = false;
        changed |= setInputTypeFromIdentifier();
        changed |= processFluidItemTransfers(items, HbmFluidItemTransfer.combineTransfers(
                HbmFluidItemTransfer.loadTransfers(
                        SLOT_INPUT_CONTAINER, SLOT_INPUT_CONTAINER_OUTPUT, inputTank()),
                HbmFluidItemTransfer.unloadTransfers(
                        SLOT_HEAVY_CONTAINER, SLOT_HEAVY_CONTAINER_OUTPUT, 2,
                        outputTank(0), outputTank(1), outputTank(2), outputTank(3))));
        long oldPower = energy.getPower();
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, energy.getReceiverSpeed());
        changed |= oldPower != energy.getPower();
        changed |= refine();
        return changed || oldOn != isOn;
    }

    private void updateAudioLoop() {
        if (level == null || !level.isClientSide) {
            return;
        }
        if (audioTime > 0) {
            audioTime--;
        }
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.boiler",
                audioTime > 0, 30.0D, 15.0F, 0.25F, 1.0F);
    }

    private boolean setInputTypeFromIdentifier() {
        return setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, inputTank());
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_INVENTORY)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        } else if (tag.contains(TAG_MODERN_INVENTORY)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_MODERN_INVENTORY, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
    }

    private boolean refine() {
        RefineryRecipe recipe = LegacyOilFluidRecipes.getRefinery(level, inputTank().getTankType());
        boolean changed = setupRecipeTanks(recipe);
        if (recipe == null) {
            return changed;
        }
        HbmFluidStack[] outputs = recipe.outputs();
        if (energy.getPower() < POWER_PER_OPERATION || inputTank().getFill() < INPUT_PER_OPERATION) {
            return changed;
        }
        HbmFluidRecipeIO.RecipeFluidIoProcessReport report = HbmFluidRecipeIO.processLegacyFixedRecipeIoReport(
                List.of(HbmFluidRecipeIO.requirementFromTank(inputTank(), INPUT_PER_OPERATION)),
                List.of(outputs),
                List.of(inputTank()),
                getAllTanks().subList(1, 5),
                false);
        if (!report.complete()) {
            return changed;
        }
        energy.setPower(energy.getPower() - POWER_PER_OPERATION);
        sulfur++;
        if (sulfur >= MAX_SULFUR) {
            addSolid(recipe.solidStack());
            sulfur = 0;
        }
        isOn = true;
        if (level != null && level.getGameTime() % 20L == 0L) {
            PollutionManager.incrementPollution(level, worldPosition, PollutionType.SOOT,
                    PollutionManager.SOOT_PER_SECOND * 5.0F);
        }
        onFluidContentsChanged();
        return true;
    }

    private boolean setupRecipeTanks(@Nullable RefineryRecipe recipe) {
        return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                List.of(), recipe == null ? List.of() : List.of(recipe.outputs()),
                List.of(), getAllTanks().subList(1, 5)).changed();
    }

    private void addSolid(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_SOLID_OUTPUT, SLOT_SOLID_OUTPUT, stack);
    }

    private HbmFluidTank inputTank() {
        return getAllTanks().get(0);
    }

    private HbmFluidTank outputTank(int index) {
        return getAllTanks().get(index + 1);
    }

    private static final class RefineryExternalItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private RefineryExternalItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_SOLID_OUTPUT) : ItemStack.EMPTY;
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
            ItemStack existing = items.getStackInSlot(SLOT_SOLID_OUTPUT);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = HbmItemStackUtil.carefulCopyWithSize(existing,
                    Math.min(amount, existing.getCount()));
            if (!simulate) {
                ItemStack remaining = existing.copy();
                remaining.shrink(extracted.getCount());
                items.setStackInSlot(SLOT_SOLID_OUTPUT, remaining.isEmpty() ? ItemStack.EMPTY : remaining);
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
