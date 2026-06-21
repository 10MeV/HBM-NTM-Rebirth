package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fuel.LegacyBurnTimeModule;
import com.hbm.ntm.menu.WoodBurnerMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.recipe.WoodBurnerRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WoodBurnerBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, HbmLegacyButtonReceiver, HbmFluidCopiable {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_ASH = 1;
    public static final int SLOT_IDENTIFIER = 2;
    public static final int SLOT_FLUID_INPUT = 3;
    public static final int SLOT_FLUID_OUTPUT = 4;
    public static final int SLOT_BATTERY = 5;
    public static final int SLOT_COUNT = 6;
    public static final int CONTROL_TOGGLE = 0;
    public static final int CONTROL_SWITCH = 1;

    private static final String TAG_INVENTORY = "items";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_BURN_TIME = "burnTime";
    private static final String TAG_MAX_BURN_TIME = "maxBurnTime";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_LIQUID_BURN = "liquidBurn";
    private static final String TAG_ASH_WOOD = "ashLevelWood";
    private static final String TAG_ASH_COAL = "ashLevelCoal";
    private static final String TAG_ASH_MISC = "ashLevelMisc";
    private static final String TAG_POWER_GEN = "powerGen";
    private static final int TANK_CAPACITY = 16_000;
    private static final int ASH_THRESHOLD = 2_000;
    private static final long MAX_POWER = 100_000L;
    private static final LegacyBurnTimeModule BURN_MODULE = WoodBurnerRecipeRuntime.burnModule();

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
                case SLOT_FUEL -> BURN_MODULE.getBurnTime(stack) > 0;
                case SLOT_IDENTIFIER, SLOT_FLUID_INPUT, SLOT_BATTERY -> true;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int burnTime;
    private int maxBurnTime;
    private boolean on;
    private boolean liquidBurn;
    private int powerGen;
    private int ashLevelWood;
    private int ashLevelCoal;
    private int ashLevelMisc;

    public WoodBurnerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                new HbmFluidTank(HbmFluids.WOODOIL, TANK_CAPACITY));
    }

    private WoodBurnerBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank tank) {
        super(ModBlockEntities.WOOD_BURNER.get(), pos, state, energy, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WoodBurnerBlockEntity burner) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, burner);
        long oldPower = burner.energy.getPower();
        int oldFill = burner.tank.getFill();
        int oldBurn = burner.burnTime;
        int oldPowerGen = burner.powerGen;
        boolean oldOn = burner.on;
        boolean oldLiquid = burner.liquidBurn;

        burner.powerGen = 0;
        boolean changed = burner.setFluidTankTypeFromIdentifierSlot(burner.items, SLOT_IDENTIFIER, burner.tank);
        changed |= burner.processFluidItemTransfers(burner.items,
                HbmFluidItemTransfer.loadTransfers(SLOT_FLUID_INPUT, SLOT_FLUID_OUTPUT, burner.tank));
        HbmEnergyUtil.chargeItemFromStorage(burner.items.getStackInSlot(SLOT_BATTERY),
                burner.energy, burner.energy.getProviderSpeed());

        if (burner.tank.getTankType() != HbmFluids.NONE) {
            burner.refreshTrackedReceiverFluidPortsReport(List.of(burner.tank), burner);
        }
        burner.sendSmokeToPorts(level, pos);
        burner.tryProvideEnergyToPorts();

        changed |= burner.liquidBurn ? burner.tickLiquidBurn(level, pos) : burner.tickSolidBurn(level, pos);
        burner.energy.setPower(Math.min(MAX_POWER, burner.energy.getPower() + burner.powerGen));

        changed |= oldPower != burner.energy.getPower()
                || oldFill != burner.tank.getFill()
                || oldBurn != burner.burnTime
                || oldPowerGen != burner.powerGen
                || oldOn != burner.on
                || oldLiquid != burner.liquidBurn;
        burner.networkPackNT(25);
        if (changed || level.getGameTime() % 20L == 0L) {
            burner.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, WoodBurnerBlockEntity burner) {
        if (!level.isClientSide || burner.powerGen <= 0) {
            return;
        }
        Direction facing = burner.facing();
        Direction side = facing.getClockWise();
        level.addParticle(ParticleTypes.SMOKE,
                pos.getX() + 0.5D - facing.getStepX() + side.getStepX(),
                pos.getY() + 4.0D,
                pos.getZ() + 0.5D - facing.getStepZ() + side.getStepZ(),
                0.0D, 0.05D, 0.0D);
    }

    private boolean tickSolidBurn(Level level, BlockPos pos) {
        boolean changed = false;
        if (burnTime <= 0) {
            changed |= tryStartSolidFuel();
        } else if (energy.getPower() < MAX_POWER && on) {
            burnTime--;
            powerGen += 100;
            changed = true;
            if (level.getGameTime() % 20L == 0L) {
                pollution.pollute(level, pos, com.hbm.ntm.pollution.PollutionType.SOOT,
                        com.hbm.handler.pollution.PollutionHandler.SOOT_PER_SECOND);
            }
        }
        return changed;
    }

    private boolean tryStartSolidFuel() {
        ItemStack fuel = items.getStackInSlot(SLOT_FUEL);
        int burn = BURN_MODULE.getBurnTime(fuel);
        if (burn <= 0) {
            return false;
        }
        switch (BURN_MODULE.getAshFromFuel(fuel)) {
            case WOOD -> ashLevelWood += burn;
            case COAL -> ashLevelCoal += burn;
            case MISC -> ashLevelMisc += burn;
        }
        processAshLevels();
        maxBurnTime = burnTime = burn;
        ItemStack container = fuel.getCraftingRemainingItem();
        items.extractItem(SLOT_FUEL, 1, false);
        if (items.getStackInSlot(SLOT_FUEL).isEmpty() && !container.isEmpty()) {
            items.setStackInSlot(SLOT_FUEL, container.copy());
        }
        return true;
    }

    private boolean tickLiquidBurn(Level level, BlockPos pos) {
        if (energy.getPower() >= MAX_POWER || tank.getFill() <= 0 || !on) {
            return false;
        }
        FlammableFluidTrait trait = tank.getTankType().getTrait(FlammableFluidTrait.class);
        if (trait == null) {
            return false;
        }
        int toBurn = Math.min(tank.getFill(), 2);
        if (toBurn <= 0) {
            return false;
        }
        powerGen += (int) (trait.getHeatEnergyPerBucket() * toBurn / 2_000L);
        tank.drain(toBurn, false);
        if (level.getGameTime() % 20L == 0L) {
            pollution.polluteFluidRelease(level, pos, tank.getTankType(), FluidReleaseType.BURN, toBurn / 2.0F);
        }
        return true;
    }

    private void processAshLevels() {
        while (processAsh(AshKind.WOOD)) {
            ashLevelWood -= ASH_THRESHOLD;
        }
        while (processAsh(AshKind.COAL)) {
            ashLevelCoal -= ASH_THRESHOLD;
        }
        while (processAsh(AshKind.MISC)) {
            ashLevelMisc -= ASH_THRESHOLD;
        }
    }

    private boolean processAsh(AshKind kind) {
        int level = switch (kind) {
            case WOOD -> ashLevelWood;
            case COAL -> ashLevelCoal;
            case MISC -> ashLevelMisc;
        };
        if (level < ASH_THRESHOLD) {
            return false;
        }
        ItemStack output = new ItemStack(ModItems.legacyItem(kind.itemName).get());
        ItemStack existing = items.getStackInSlot(SLOT_ASH);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_ASH, output);
            return true;
        }
        if (ItemStack.isSameItemSameTags(existing, output) && existing.getCount() < existing.getMaxStackSize()) {
            existing.grow(1);
            items.setStackInSlot(SLOT_ASH, existing);
            return true;
        }
        return false;
    }

    private void sendSmokeToPorts(Level level, BlockPos pos) {
        for (FluidPort port : getWoodBurnerFluidPorts()) {
            BlockPos connector = pos.offset(port.offset());
            pollution.sendSmoke(level, connector.getX(), connector.getY(), connector.getZ(), port.direction());
        }
    }

    private List<EnergyPort> getWoodBurnerEnergyPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                EnergyPort.of(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2, facing.getOpposite()),
                EnergyPort.of(-facing.getStepX() * 2 + side.getStepX(), 0,
                        -facing.getStepZ() * 2 + side.getStepZ(), facing.getOpposite()));
    }

    private List<FluidPort> getWoodBurnerFluidPorts() {
        Direction facing = facing();
        Direction side = facing.getClockWise();
        return List.of(
                FluidPort.of(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2, facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 2 + side.getStepX(), 0,
                        -facing.getStepZ() * 2 + side.getStepZ(), facing.getOpposite()));
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public boolean isOn() {
        return on;
    }

    public boolean isLiquidBurn() {
        return liquidBurn;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getPowerGen() {
        return powerGen;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return getWoodBurnerEnergyPorts();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return getWoodBurnerFluidPorts();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == tank.getTankType();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
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
    public long getReceiverSpeed(FluidType type, int pressure) {
        return tank.getSpace();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineWoodBurner", "Wood Burner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WoodBurnerMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return (id == CONTROL_TOGGLE || id == CONTROL_SWITCH)
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_TOGGLE) {
            on = !on;
        } else if (id == CONTROL_SWITCH) {
            liquidBurn = !liquidBurn;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        pollution.writeLegacyNbt(tag);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putInt(TAG_BURN_TIME, burnTime);
        tag.putInt(TAG_MAX_BURN_TIME, maxBurnTime);
        tag.putBoolean(TAG_IS_ON, on);
        tag.putBoolean(TAG_LIQUID_BURN, liquidBurn);
        tag.putInt(TAG_ASH_WOOD, ashLevelWood);
        tag.putInt(TAG_ASH_COAL, ashLevelCoal);
        tag.putInt(TAG_ASH_MISC, ashLevelMisc);
        tag.putInt(TAG_POWER_GEN, powerGen);
        tank.writeToNbt(tag, "t");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        pollution.readLegacyNbt(tag);
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        burnTime = tag.getInt(TAG_BURN_TIME);
        maxBurnTime = tag.getInt(TAG_MAX_BURN_TIME);
        on = tag.getBoolean(TAG_IS_ON);
        liquidBurn = tag.getBoolean(TAG_LIQUID_BURN);
        ashLevelWood = tag.getInt(TAG_ASH_WOOD);
        ashLevelCoal = tag.getInt(TAG_ASH_COAL);
        ashLevelMisc = tag.getInt(TAG_ASH_MISC);
        powerGen = tag.getInt(TAG_POWER_GEN);
        if (tag.contains("t_type") || tag.contains("t")) {
            tank.readFromNbt(tag, "t");
        }
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
            return 2;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return switch (slot) {
                case 0 -> items.getStackInSlot(SLOT_FUEL);
                case 1 -> items.getStackInSlot(SLOT_ASH);
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == 0 ? items.insertItem(SLOT_FUEL, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 1 ? items.extractItem(SLOT_ASH, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return switch (slot) {
                case 0 -> items.getSlotLimit(SLOT_FUEL);
                case 1 -> items.getSlotLimit(SLOT_ASH);
                default -> 0;
            };
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && items.isItemValid(SLOT_FUEL, stack);
        }
    }

    private enum AshKind {
        WOOD("powder_ash_wood"),
        COAL("powder_ash_coal"),
        MISC("powder_ash_misc");

        private final String itemName;

        AshKind(String itemName) {
            this.itemName = itemName;
        }
    }
}
