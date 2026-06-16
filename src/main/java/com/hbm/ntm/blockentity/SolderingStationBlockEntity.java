package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.SolderingStationMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.recipe.SolderingStationRecipe;
import com.hbm.ntm.recipe.SolderingStationRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SolderingStationBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmLegacyButtonReceiver {
    public static final int SLOT_TOPPING_0 = 0;
    public static final int SLOT_TOPPING_1 = 1;
    public static final int SLOT_TOPPING_2 = 2;
    public static final int SLOT_PCB_0 = 3;
    public static final int SLOT_PCB_1 = 4;
    public static final int SLOT_SOLDER = 5;
    public static final int SLOT_OUTPUT = 6;
    public static final int SLOT_BATTERY = 7;
    public static final int SLOT_IDENTIFIER = 8;
    public static final int SLOT_UPGRADE_0 = 9;
    public static final int SLOT_UPGRADE_1 = 10;
    public static final int SLOT_COUNT = 11;

    public static final int CONTROL_COLLISION_PREVENTION = 0;
    private static final long DEFAULT_MAX_POWER = 2_000L;
    private static final long DEFAULT_CONSUMPTION = 100L;
    private static final int TANK_CAPACITY = 8_000;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_DISPLAY = "display";
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    private final HbmFluidTank tank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_TOPPING_0, SLOT_TOPPING_1, SLOT_TOPPING_2 -> isUniqueInput(slot, stack, SLOT_TOPPING_0,
                        SLOT_TOPPING_2) && (level == null || SolderingStationRecipeRuntime.isTopping(level, stack));
                case SLOT_PCB_0, SLOT_PCB_1 -> isUniqueInput(slot, stack, SLOT_PCB_0, SLOT_PCB_1)
                        && (level == null || SolderingStationRecipeRuntime.isPcb(level, stack));
                case SLOT_SOLDER -> level == null || SolderingStationRecipeRuntime.isSolder(level, stack);
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_UPGRADE_0, SLOT_UPGRADE_1 -> stack.getItem() instanceof ItemMachineUpgrade upgrade
                        && VALID_UPGRADES.containsKey(upgrade.getUpgradeType());
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private long consumption = DEFAULT_CONSUMPTION;
    private int progress;
    private int processTime = 1;
    private boolean collisionPrevention;
    private boolean isOn;
    private ItemStack displayStack = ItemStack.EMPTY;

    public SolderingStationBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLDERING_STATION.get(), pos, state,
                new HbmEnergyStorage(DEFAULT_MAX_POWER, DEFAULT_MAX_POWER, 0L),
                List.of(new HbmFluidTank(HbmFluids.NONE, TANK_CAPACITY)));
        this.tank = getAllTanks().get(0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolderingStationBlockEntity station) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, station);

        long oldPower = station.energy.getPower();
        long oldMaxPower = station.energy.getMaxPower();
        long oldConsumption = station.consumption;
        int oldProgress = station.progress;
        int oldProcessTime = station.processTime;
        int oldFill = station.tank.getFill();
        FluidType oldType = station.tank.getTankType();
        boolean oldCollision = station.collisionPrevention;
        boolean oldOn = station.isOn;
        ItemStack oldDisplay = station.displayStack.copy();

        HbmEnergyUtil.chargeStorageFromItem(station.items.getStackInSlot(SLOT_BATTERY),
                station.energy, station.energy.getReceiverSpeed());
        station.setFluidTankTypeFromIdentifierSlot(station.items, SLOT_IDENTIFIER, station.tank);

        SolderingStationRecipe recipe = station.findRecipe(level);
        station.displayStack = recipe == null ? ItemStack.EMPTY : recipe.output();
        station.updateRecipeState(recipe);
        boolean processing = recipe != null && station.canProcess(recipe);
        station.isOn = processing;
        if (processing) {
            int overdrive = station.upgradeLevels().getLevel(UpgradeType.OVERDRIVE);
            station.progress += 1 + overdrive;
            station.energy.setPower(station.energy.getPower() - station.consumption);
            if (station.progress >= station.processTime) {
                station.progress = 0;
                station.consume(recipe);
                station.mergeOutput(recipe.output());
            }
            if (level.getGameTime() % 20L == 0L) {
                Vec3 particlePos = station.solderParticlePos();
                ParticleUtil.spawnTau(level, particlePos.x, particlePos.y, particlePos.z, 3, false);
            }
        } else {
            station.progress = 0;
        }

        if (station.tank.getTankType() != HbmFluids.NONE) {
            station.refreshTrackedReceiverFluidPortsReport(List.of(station.tank), station);
        }

        boolean changed = oldPower != station.energy.getPower()
                || oldMaxPower != station.energy.getMaxPower()
                || oldConsumption != station.consumption
                || oldProgress != station.progress
                || oldProcessTime != station.processTime
                || oldFill != station.tank.getFill()
                || oldType != station.tank.getTankType()
                || oldCollision != station.collisionPrevention
                || oldOn != station.isOn
                || !ItemStack.isSameItemSameTags(oldDisplay, station.displayStack)
                || oldDisplay.getCount() != station.displayStack.getCount();
        station.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            station.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private SolderingStationRecipe findRecipe(Level level) {
        return SolderingStationRecipeRuntime.find(level,
                new ItemStack[] {
                        items.getStackInSlot(SLOT_TOPPING_0),
                        items.getStackInSlot(SLOT_TOPPING_1),
                        items.getStackInSlot(SLOT_TOPPING_2)
                },
                new ItemStack[] {
                        items.getStackInSlot(SLOT_PCB_0),
                        items.getStackInSlot(SLOT_PCB_1)
                },
                items.getStackInSlot(SLOT_SOLDER));
    }

    private void updateRecipeState(@Nullable SolderingStationRecipe recipe) {
        if (recipe == null) {
            consumption = DEFAULT_CONSUMPTION;
            processTime = 1;
            setDynamicMaxPower(DEFAULT_MAX_POWER);
            return;
        }
        LegacyMachineUpgradeManager.Levels levels = upgradeLevels();
        int speed = levels.getLevel(UpgradeType.SPEED);
        int power = levels.getLevel(UpgradeType.POWER);
        int overdrive = levels.getLevel(UpgradeType.OVERDRIVE);
        processTime = recipe.duration() - recipe.duration() * speed / 6 + recipe.duration() * power / 3;
        consumption = recipe.consumption() + recipe.consumption() * speed - recipe.consumption() * power / 6;
        consumption = Math.max(1L, consumption << overdrive);
        processTime = Math.max(1, processTime);
        setDynamicMaxPower(Math.max(consumption * 20L, energy.getPower()));
    }

    private void setDynamicMaxPower(long maxPower) {
        energy.setMaxPower(Math.max(DEFAULT_MAX_POWER, maxPower));
        energy.setTransferRates(energy.getMaxPower(), 0L);
    }

    private LegacyMachineUpgradeManager.Levels upgradeLevels() {
        return LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE_0, SLOT_UPGRADE_1, VALID_UPGRADES);
    }

    private boolean canProcess(SolderingStationRecipe recipe) {
        if (energy.getPower() < consumption) {
            return false;
        }
        if (recipe.fluid().isPresent()) {
            HbmFluidStack required = recipe.fluid().get();
            if (tank.getTankType() != required.type() || tank.getFill() < required.amount()
                    || tank.getPressure() != required.pressure()) {
                return false;
            }
        } else if (collisionPrevention && tank.getFill() > 0) {
            return false;
        }
        return canOutput(recipe.output());
    }

    private void consume(SolderingStationRecipe recipe) {
        consumeGroup(recipe.toppings(), SLOT_TOPPING_0, SLOT_TOPPING_2);
        consumeGroup(recipe.pcb(), SLOT_PCB_0, SLOT_PCB_1);
        consumeGroup(recipe.solder(), SLOT_SOLDER, SLOT_SOLDER);
        recipe.fluid().ifPresent(required -> tank.drain(required.amount(), false));
    }

    private void consumeGroup(List<HbmIngredient> ingredients, int startSlot, int endSlot) {
        for (HbmIngredient ingredient : ingredients) {
            for (int slot = startSlot; slot <= endSlot; slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (ingredient.test(stack)) {
                    items.extractItem(slot, ingredient.count(), false);
                    break;
                }
            }
        }
    }

    private boolean canOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        return existing.isEmpty()
                || ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    private void mergeOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, output.copy());
        } else {
            existing.grow(output.getCount());
            items.setStackInSlot(SLOT_OUTPUT, existing);
        }
    }

    private boolean isUniqueInput(int slot, ItemStack stack, int startSlot, int endSlot) {
        for (int i = startSlot; i <= endSlot; i++) {
            if (i != slot && ItemStack.isSameItemSameTags(items.getStackInSlot(i), stack)) {
                return false;
            }
        }
        return true;
    }

    private net.minecraft.world.phys.Vec3 solderParticlePos() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return net.minecraft.world.phys.Vec3.atLowerCornerOf(worldPosition)
                .add(0.5D - facing.getStepX() * 0.5D + side.getStepX() * 0.5D,
                        1.125D,
                        0.5D - facing.getStepZ() * 0.5D + side.getStepZ() * 0.5D);
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private List<FluidPort> solderingFluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX(), 0, facing.getStepZ(), facing),
                FluidPort.of(facing.getStepX() + side.getStepX(), 0, facing.getStepZ() + side.getStepZ(), facing),
                FluidPort.of(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2, facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 2 + side.getStepX(), 0,
                        -facing.getStepZ() * 2 + side.getStepZ(), facing.getOpposite()),
                FluidPort.of(-side.getStepX(), 0, -side.getStepZ(), side.getOpposite()),
                FluidPort.of(-facing.getStepX() - side.getStepX(), 0,
                        -facing.getStepZ() - side.getStepZ(), side.getOpposite()),
                FluidPort.of(side.getStepX() * 2, 0, side.getStepZ() * 2, side),
                FluidPort.of(-facing.getStepX() + side.getStepX() * 2, 0,
                        -facing.getStepZ() + side.getStepZ() * 2, side));
    }

    private List<EnergyPort> solderingEnergyPorts() {
        return solderingFluidPorts().stream()
                .map(port -> EnergyPort.of(port.offset().getX(), port.offset().getY(), port.offset().getZ(),
                        port.direction()))
                .toList();
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    @Override
    public long getPower() {
        return energy.getPower();
    }

    @Override
    public long getMaxPower() {
        return energy.getMaxPower();
    }

    public long getConsumption() {
        return consumption;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return Math.max(1, processTime);
    }

    public boolean isCollisionPrevention() {
        return collisionPrevention;
    }

    public boolean isOn() {
        return isOn;
    }

    public ItemStack getDisplayStack() {
        return displayStack.copy();
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return solderingEnergyPorts();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return solderingFluidPorts();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return tank.getTankType() != HbmFluids.NONE;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == tank.getTankType();
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
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected boolean showsLegacyFluidLookOverlay() {
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower()),
                LegacyLookOverlayLines.tank(true, tank),
                Component.literal("Consumption: " + consumption + " HE/t")));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineSolderingStation", "Soldering Station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SolderingStationMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return id == CONTROL_COLLISION_PREVENTION
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_COLLISION_PREVENTION) {
            collisionPrevention = !collisionPrevention;
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putLong("power", energy.getPower());
        tag.putLong("maxPower", energy.getMaxPower());
        tag.putLong("consumption", consumption);
        tag.putInt("progress", progress);
        tag.putInt("processTime", processTime);
        tag.putBoolean("collisionPrevention", collisionPrevention);
        tag.putBoolean("isOn", isOn);
        tank.writeToNbt(tag, "t");
        if (!displayStack.isEmpty()) {
            tag.put(TAG_DISPLAY, displayStack.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        if (tag.contains("maxPower")) {
            setDynamicMaxPower(tag.getLong("maxPower"));
        }
        consumption = tag.contains("consumption") ? Math.max(1L, tag.getLong("consumption")) : DEFAULT_CONSUMPTION;
        progress = tag.getInt("progress");
        processTime = tag.contains("processTime") ? Math.max(1, tag.getInt("processTime")) : 1;
        collisionPrevention = tag.getBoolean("collisionPrevention");
        isOn = tag.getBoolean("isOn");
        if (tag.contains("t") || tag.contains("t_type")) {
            tank.readFromNbt(tag, "t");
        }
        displayStack = tag.contains(TAG_DISPLAY) ? ItemStack.of(tag.getCompound(TAG_DISPLAY)) : ItemStack.EMPTY;
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return 7;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot <= SLOT_OUTPUT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot >= SLOT_TOPPING_0 && slot <= SLOT_SOLDER ? items.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_OUTPUT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot <= SLOT_OUTPUT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= SLOT_TOPPING_0 && slot <= SLOT_SOLDER && items.isItemValid(slot, stack);
        }
    }
}
