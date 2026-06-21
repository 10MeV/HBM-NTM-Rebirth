package com.hbm.ntm.blockentity;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ElectrolyserMenu;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime.FluidRecipe;
import com.hbm.ntm.recipe.ElectrolyserRecipeRuntime.MetalRecipe;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.util.CrucibleUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectrolyserBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyButtonReceiver, HbmFluidCopiable,
        LegacyUpgradeInfoProvider {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_UPGRADE_1 = 1;
    public static final int SLOT_UPGRADE_2 = 2;
    public static final int SLOT_FLUID_ID_INPUT = 3;
    public static final int SLOT_FLUID_ID_OUTPUT = 4;
    public static final int SLOT_INPUT_CONTAINER = 5;
    public static final int SLOT_INPUT_CONTAINER_OUT = 6;
    public static final int SLOT_OUTPUT1_CONTAINER = 7;
    public static final int SLOT_OUTPUT1_CONTAINER_OUT = 8;
    public static final int SLOT_OUTPUT2_CONTAINER = 9;
    public static final int SLOT_OUTPUT2_CONTAINER_OUT = 10;
    public static final int SLOT_FLUID_BYPRODUCT_START = 11;
    public static final int SLOT_FLUID_BYPRODUCT_END = 13;
    public static final int SLOT_METAL_INPUT = 14;
    public static final int SLOT_METAL_OUTPUT_START = 15;
    public static final int SLOT_METAL_OUTPUT_END = 20;
    public static final int SLOT_COUNT = 21;
    public static final int MODE_FLUID = 0;
    public static final int MODE_METAL = 1;
    public static final int CONTROL_FLUID_MODE = 0;
    public static final int CONTROL_METAL_MODE = 1;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_PROGRESS_FLUID = "progressFluid";
    private static final String TAG_PROCESS_FLUID_TIME = "processFluidTime";
    private static final String TAG_PROGRESS_ORE = "progressOre";
    private static final String TAG_PROCESS_ORE_TIME = "processOreTime";
    private static final String TAG_LAST_SELECTED_GUI = "lastSelectedGUI";
    private static final String TAG_LEFT_TYPE = "leftType";
    private static final String TAG_LEFT_AMOUNT = "leftAmount";
    private static final String TAG_RIGHT_TYPE = "rightType";
    private static final String TAG_RIGHT_AMOUNT = "rightAmount";
    private static final long MAX_POWER = 20_000_000L;
    private static final int TANK_CAPACITY = 16_000;
    private static final int MAX_MATERIAL = MaterialShapes.BLOCK.q(16);
    private static final int USAGE_FLUID_BASE = 10_000;
    private static final int USAGE_ORE_BASE = 10_000;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3,
            UpgradeType.OVERDRIVE, 3);

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank1;
    private final HbmFluidTank outputTank2;
    private final HbmFluidTank nitricTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_UPGRADE_1, SLOT_UPGRADE_2 -> stack.getItem() instanceof ItemMachineUpgrade;
                case SLOT_FLUID_ID_INPUT -> stack.getItem() instanceof IFluidIdentifierItem;
                case SLOT_INPUT_CONTAINER, SLOT_OUTPUT1_CONTAINER, SLOT_OUTPUT2_CONTAINER -> true;
                case SLOT_METAL_INPUT -> ElectrolyserRecipeRuntime.metalForInput(stack) != null;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private int progressFluid;
    private int processFluidTime = 100;
    private int progressOre;
    private int processOreTime = 600;
    private int usageFluid;
    private int usageOre;
    private int lastSelectedGui;
    private MaterialStack leftStack;
    private MaterialStack rightStack;

    public ElectrolyserBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                new HbmFluidTank(HbmFluids.WATER, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.HYDROGEN, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.OXYGEN, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.NITRIC_ACID, TANK_CAPACITY));
    }

    private ElectrolyserBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank outputTank1, HbmFluidTank outputTank2, HbmFluidTank nitricTank) {
        super(ModBlockEntities.ELECTROLYSER.get(), pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                List.of(inputTank, outputTank1, outputTank2, nitricTank));
        this.inputTank = inputTank;
        this.outputTank1 = outputTank1;
        this.outputTank2 = outputTank2;
        this.nitricTank = nitricTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectrolyserBlockEntity electrolyser) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, electrolyser);

        long oldPower = electrolyser.energy.getPower();
        int oldProgressFluid = electrolyser.progressFluid;
        int oldProgressOre = electrolyser.progressOre;
        int oldProcessFluidTime = electrolyser.processFluidTime;
        int oldProcessOreTime = electrolyser.processOreTime;
        int oldUsageFluid = electrolyser.usageFluid;
        int oldUsageOre = electrolyser.usageOre;
        int oldLeftAmount = electrolyser.getLeftAmount();
        int oldRightAmount = electrolyser.getRightAmount();
        HbmFluidTank.TankState oldInput = electrolyser.inputTank.snapshot();
        HbmFluidTank.TankState oldOutput1 = electrolyser.outputTank1.snapshot();
        HbmFluidTank.TankState oldOutput2 = electrolyser.outputTank2.snapshot();
        HbmFluidTank.TankState oldNitric = electrolyser.nitricTank.snapshot();

        HbmEnergyUtil.chargeStorageFromItem(electrolyser.items.getStackInSlot(SLOT_BATTERY),
                electrolyser.energy, electrolyser.energy.getReceiverSpeed());
        boolean changed = electrolyser.setFluidTankTypeFromIdentifierSlot(electrolyser.items,
                SLOT_FLUID_ID_INPUT, SLOT_FLUID_ID_OUTPUT, electrolyser.inputTank);
        changed |= electrolyser.processFluidItemTransfers(electrolyser.items, List.of(
                HbmFluidItemTransfer.TankSlotTransfer.load(SLOT_INPUT_CONTAINER, SLOT_INPUT_CONTAINER_OUT,
                        electrolyser.inputTank),
                HbmFluidItemTransfer.TankSlotTransfer.unload(SLOT_OUTPUT1_CONTAINER, SLOT_OUTPUT1_CONTAINER_OUT,
                        electrolyser.outputTank1),
                HbmFluidItemTransfer.TankSlotTransfer.unload(SLOT_OUTPUT2_CONTAINER, SLOT_OUTPUT2_CONTAINER_OUT,
                        electrolyser.outputTank2)));
        electrolyser.refreshTrackedTransceiverFluidPortsReport(electrolyser.getReceivingTanks(),
                electrolyser.getSendingTanks(), electrolyser);

        LegacyMachineUpgradeManager.Levels upgrades = LegacyMachineUpgradeManager.checkSlots(electrolyser.items,
                SLOT_UPGRADE_1, SLOT_UPGRADE_2, VALID_UPGRADES);
        electrolyser.usageFluid = usageFor(USAGE_FLUID_BASE, upgrades);
        electrolyser.usageOre = usageFor(USAGE_ORE_BASE, upgrades);
        electrolyser.processFluidTime = electrolyser.getDurationFluid(upgrades);
        electrolyser.processOreTime = electrolyser.getDurationMetal(upgrades);

        for (int i = 0; i < electrolyser.getCycleCount(upgrades); i++) {
            if (electrolyser.canProcessFluid()) {
                electrolyser.progressFluid++;
                electrolyser.energy.setPower(electrolyser.energy.getPower() - electrolyser.usageFluid);
                if (electrolyser.progressFluid >= electrolyser.processFluidTime) {
                    electrolyser.processFluids();
                    electrolyser.progressFluid = 0;
                    changed = true;
                }
            } else if (electrolyser.progressFluid != 0) {
                electrolyser.progressFluid = 0;
                changed = true;
            }
            if (electrolyser.canProcessMetal()) {
                electrolyser.progressOre++;
                electrolyser.energy.setPower(electrolyser.energy.getPower() - electrolyser.usageOre);
                if (electrolyser.progressOre >= electrolyser.processOreTime) {
                    electrolyser.processMetal();
                    electrolyser.progressOre = 0;
                    changed = true;
                }
            } else if (electrolyser.progressOre != 0) {
                electrolyser.progressOre = 0;
                changed = true;
            }
        }
        changed |= electrolyser.pourMetalStacks(level, pos, state, upgrades);

        electrolyser.networkPackNT(50);
        changed |= oldPower != electrolyser.energy.getPower()
                || oldProgressFluid != electrolyser.progressFluid
                || oldProgressOre != electrolyser.progressOre
                || oldProcessFluidTime != electrolyser.processFluidTime
                || oldProcessOreTime != electrolyser.processOreTime
                || oldUsageFluid != electrolyser.usageFluid
                || oldUsageOre != electrolyser.usageOre
                || oldLeftAmount != electrolyser.getLeftAmount()
                || oldRightAmount != electrolyser.getRightAmount()
                || !oldInput.equals(electrolyser.inputTank.snapshot())
                || !oldOutput1.equals(electrolyser.outputTank1.snapshot())
                || !oldOutput2.equals(electrolyser.outputTank2.snapshot())
                || !oldNitric.equals(electrolyser.nitricTank.snapshot());
        if (changed || level.getGameTime() % 20L == 0L) {
            electrolyser.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static int usageFor(int base, LegacyMachineUpgradeManager.Levels upgrades) {
        int speed = upgrades.getLevel(UpgradeType.SPEED);
        int power = upgrades.getLevel(UpgradeType.POWER);
        return Math.max(1, base - base * power / 4 + base * speed);
    }

    private boolean canProcessFluid() {
        FluidRecipe recipe = ElectrolyserRecipeRuntime.fluidForInput(inputTank.getTankType());
        if (recipe == null || energy.getPower() < usageFluid || inputTank.getFill() < recipe.amount()) {
            return false;
        }
        if (!canFitFluid(outputTank1, recipe.output1Type(), recipe.output1Amount())) {
            return false;
        }
        if (!canFitFluid(outputTank2, recipe.output2Type(), recipe.output2Amount())) {
            return false;
        }
        return canFitByproducts(recipe.byproducts(), SLOT_FLUID_BYPRODUCT_START, SLOT_FLUID_BYPRODUCT_END);
    }

    private static boolean canFitFluid(HbmFluidTank tank, FluidType type, int amount) {
        if (type == HbmFluids.NONE || amount <= 0) {
            return true;
        }
        return (tank.getTankType() == HbmFluids.NONE || tank.getTankType() == type)
                && tank.getFill() + amount <= tank.getMaxFill();
    }

    private boolean canFitByproducts(List<ItemStack> byproducts, int startSlot, int endSlot) {
        ItemStack[] simulated = new ItemStack[endSlot - startSlot + 1];
        for (int i = 0; i < simulated.length; i++) {
            simulated[i] = items.getStackInSlot(startSlot + i).copy();
        }
        for (ItemStack byproduct : byproducts) {
            if (byproduct.isEmpty()) {
                continue;
            }
            ItemStack remaining = byproduct.copy();
            for (int i = 0; i < simulated.length && !remaining.isEmpty(); i++) {
                ItemStack existing = simulated[i];
                if (existing.isEmpty()) {
                    simulated[i] = remaining.copy();
                    remaining = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameTags(existing, remaining)) {
                    int move = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    if (move > 0) {
                        existing.grow(move);
                        remaining.shrink(move);
                    }
                }
            }
            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void processFluids() {
        FluidRecipe recipe = ElectrolyserRecipeRuntime.fluidForInput(inputTank.getTankType());
        if (recipe == null) {
            return;
        }
        inputTank.drain(recipe.amount(), false);
        fillOutput(outputTank1, recipe.output1Type(), recipe.output1Amount());
        fillOutput(outputTank2, recipe.output2Type(), recipe.output2Amount());
        mergeByproducts(recipe.byproducts(), SLOT_FLUID_BYPRODUCT_START, SLOT_FLUID_BYPRODUCT_END);
        onFluidContentsChanged();
    }

    private boolean canProcessMetal() {
        MetalRecipe recipe = ElectrolyserRecipeRuntime.metalForInput(items.getStackInSlot(SLOT_METAL_INPUT));
        if (recipe == null || energy.getPower() < usageOre || nitricTank.getFill() < 100) {
            return false;
        }
        if (!canFitMaterial(leftStack, recipe.output1())) {
            return false;
        }
        if (!canFitMaterial(rightStack, recipe.output2())) {
            return false;
        }
        return canFitByproducts(recipe.byproducts(), SLOT_METAL_OUTPUT_START, SLOT_METAL_OUTPUT_END);
    }

    private static boolean canFitMaterial(@Nullable MaterialStack current, @Nullable MaterialStack incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return true;
        }
        if (current == null || current.isEmpty()) {
            return incoming.amount <= MAX_MATERIAL;
        }
        return current.material == incoming.material && current.amount + incoming.amount <= MAX_MATERIAL;
    }

    private void processMetal() {
        MetalRecipe recipe = ElectrolyserRecipeRuntime.metalForInput(items.getStackInSlot(SLOT_METAL_INPUT));
        if (recipe == null) {
            return;
        }
        leftStack = addMaterial(leftStack, recipe.output1());
        rightStack = addMaterial(rightStack, recipe.output2());
        mergeByproducts(recipe.byproducts(), SLOT_METAL_OUTPUT_START, SLOT_METAL_OUTPUT_END);
        nitricTank.drain(100, false);
        items.extractItem(SLOT_METAL_INPUT, 1, false);
        onFluidContentsChanged();
    }

    @Nullable
    private static MaterialStack addMaterial(@Nullable MaterialStack current, @Nullable MaterialStack incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return current;
        }
        if (current == null || current.isEmpty()) {
            return incoming.copy();
        }
        current.amount += incoming.amount;
        return current;
    }

    private static void fillOutput(HbmFluidTank tank, FluidType type, int amount) {
        if (type != HbmFluids.NONE && amount > 0) {
            tank.fill(type, amount, 0, false);
        }
    }

    private void mergeByproducts(List<ItemStack> byproducts, int startSlot, int endSlot) {
        for (ItemStack byproduct : byproducts) {
            ItemStack remaining = byproduct.copy();
            for (int slot = startSlot; slot <= endSlot && !remaining.isEmpty(); slot++) {
                ItemStack existing = items.getStackInSlot(slot);
                if (existing.isEmpty()) {
                    items.setStackInSlot(slot, remaining.copy());
                    remaining = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameTags(existing, remaining)) {
                    int move = Math.min(remaining.getCount(), existing.getMaxStackSize() - existing.getCount());
                    if (move > 0) {
                        existing.grow(move);
                        items.setStackInSlot(slot, existing);
                        remaining.shrink(move);
                    }
                }
            }
        }
    }

    private int getDurationFluid(LegacyMachineUpgradeManager.Levels upgrades) {
        FluidRecipe recipe = ElectrolyserRecipeRuntime.fluidForInput(inputTank.getTankType());
        int base = recipe == null ? 100 : recipe.duration();
        int speed = upgrades.getLevel(UpgradeType.SPEED) - Math.min(upgrades.getLevel(UpgradeType.POWER), 1);
        return Math.max(1, (int) Math.ceil(base * Math.max(1.0F - 0.25F * speed, 0.2F)));
    }

    private int getDurationMetal(LegacyMachineUpgradeManager.Levels upgrades) {
        MetalRecipe recipe = ElectrolyserRecipeRuntime.metalForInput(items.getStackInSlot(SLOT_METAL_INPUT));
        int base = recipe == null ? 600 : recipe.duration();
        int speed = upgrades.getLevel(UpgradeType.SPEED) - Math.min(upgrades.getLevel(UpgradeType.POWER), 1);
        return Math.max(1, (int) Math.ceil(base * Math.max(1.0F - 0.25F * speed, 0.2F)));
    }

    private int getCycleCount(LegacyMachineUpgradeManager.Levels upgrades) {
        return Math.min(1 + upgrades.getLevel(UpgradeType.OVERDRIVE) * 2, 7);
    }

    private boolean pourMetalStacks(Level level, BlockPos pos, BlockState state,
            LegacyMachineUpgradeManager.Levels upgrades) {
        boolean changed = false;
        int quanta = MaterialShapes.NUGGET.q(3)
                * Math.max(getCycleCount(upgrades) * upgrades.getLevel(UpgradeType.SPEED), 1);
        Direction facing = facing(state);
        changed |= pourMetalStack(level, pos, facing.getOpposite(), true, quanta);
        changed |= pourMetalStack(level, pos, facing, false, quanta);
        return changed;
    }

    private boolean pourMetalStack(Level level, BlockPos pos, Direction direction, boolean left, int quanta) {
        MaterialStack stack = left ? leftStack : rightStack;
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        List<MaterialStack> toPour = new ArrayList<>();
        toPour.add(stack);
        CrucibleUtil.ImpactHolder impact = new CrucibleUtil.ImpactHolder();
        CrucibleUtil.PourResult result = CrucibleUtil.pourFullStackDetailed(level,
                pos.getX() + 0.5D + direction.getStepX() * 5.875D,
                pos.getY() + 2.0D,
                pos.getZ() + 0.5D + direction.getStepZ() * 5.875D,
                6.0D, true, toPour, quanta, impact);
        if (result.moved() == null || result.moved().isEmpty()) {
            return false;
        }
        if (left) {
            leftStack = toPour.isEmpty() ? null : toPour.get(0);
        } else {
            rightStack = toPour.isEmpty() ? null : toPour.get(0);
        }
        Vec3 hit = impact.value();
        float length = Math.max(1.0F, pos.getY() - (float) (Math.ceil(hit.y) - 0.875D) + 2.0F);
        ParticleUtil.spawnFoundryPour(level,
                new Vec3(pos.getX() + 0.5D + direction.getStepX() * 5.875D,
                        pos.getY() + 2.0D,
                        pos.getZ() + 0.5D + direction.getStepZ() * 5.875D),
                result.moved().material.moltenColor, direction, length);
        return true;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank1() {
        return outputTank1;
    }

    public HbmFluidTank getOutputTank2() {
        return outputTank2;
    }

    public HbmFluidTank getNitricTank() {
        return nitricTank;
    }

    public int getProgressFluid() {
        return progressFluid;
    }

    public int getProcessFluidTime() {
        return Math.max(1, processFluidTime);
    }

    public int getProgressOre() {
        return progressOre;
    }

    public int getProcessOreTime() {
        return Math.max(1, processOreTime);
    }

    public int getUsageFluid() {
        return usageFluid;
    }

    public int getUsageOre() {
        return usageOre;
    }

    public int getLastSelectedGui() {
        return lastSelectedGui;
    }

    public int getLeftMaterialId() {
        return leftStack == null || leftStack.material == null ? -1 : leftStack.material.id;
    }

    public int getLeftAmount() {
        return leftStack == null || leftStack.isEmpty() ? 0 : leftStack.amount;
    }

    public int getLeftColor() {
        return leftStack == null || leftStack.material == null ? 0xFFFFFF : leftStack.material.moltenColor;
    }

    public int getRightMaterialId() {
        return rightStack == null || rightStack.material == null ? -1 : rightStack.material.id;
    }

    public int getRightAmount() {
        return rightStack == null || rightStack.isEmpty() ? 0 : rightStack.amount;
    }

    public int getRightColor() {
        return rightStack == null || rightStack.material == null ? 0xFFFFFF : rightStack.material.moltenColor;
    }

    public int getMaxMaterial() {
        return MAX_MATERIAL;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return fluidPorts().stream()
                .map(port -> EnergyPort.of(port.offset().getX(), port.offset().getY(), port.offset().getZ(),
                        port.direction()))
                .toList();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fluidPorts();
    }

    private List<FluidPort> fluidPorts() {
        Direction facing = facing();
        Direction side = LegacyMultiblockOffsets.legacyUpSide(facing);
        List<FluidPort> ports = new ArrayList<>();
        for (int sideOffset : List.of(0, 1, -1)) {
            BlockPos back = LegacyMultiblockOffsets.relative(facing, side, -6, sideOffset, 0);
            ports.add(FluidPort.of(back.getX(), back.getY(), back.getZ(), facing.getOpposite()));
            BlockPos front = LegacyMultiblockOffsets.relative(facing, side, 6, sideOffset, 0);
            ports.add(FluidPort.of(front.getX(), front.getY(), front.getZ(), facing));
        }
        return ports;
    }

    private Direction facing() {
        return facing(getBlockState());
    }

    private Direction facing(BlockState state) {
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return getReceivingTanks();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank, nitricTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank1, outputTank2);
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
        long before1 = outputTank1.getFill();
        long before2 = outputTank2.getFill();
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (before1 != outputTank1.getFill() || before2 != outputTank2.getFill()) {
            onFluidContentsChanged();
        }
    }

    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return inputTank;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineElectrolyser", "Electrolysis Machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectrolyserMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveLegacyButton(ServerPlayer player, int value, int id) {
        return (id == CONTROL_FLUID_MODE || id == CONTROL_METAL_MODE)
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256.0D;
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_FLUID_MODE) {
            lastSelectedGui = MODE_FLUID;
        } else if (id == CONTROL_METAL_MODE) {
            lastSelectedGui = MODE_METAL;
        }
        player.closeContainer();
        net.minecraftforge.network.NetworkHooks.openScreen(player, this, worldPosition);
        setChanged();
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS_FLUID, progressFluid);
        tag.putInt(TAG_PROCESS_FLUID_TIME, processFluidTime);
        tag.putInt(TAG_PROGRESS_ORE, progressOre);
        tag.putInt(TAG_PROCESS_ORE_TIME, processOreTime);
        tag.putInt(TAG_LAST_SELECTED_GUI, lastSelectedGui);
        if (leftStack != null && !leftStack.isEmpty()) {
            tag.putInt(TAG_LEFT_TYPE, leftStack.material.id);
            tag.putInt(TAG_LEFT_AMOUNT, leftStack.amount);
        }
        if (rightStack != null && !rightStack.isEmpty()) {
            tag.putInt(TAG_RIGHT_TYPE, rightStack.material.id);
            tag.putInt(TAG_RIGHT_AMOUNT, rightStack.amount);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        progressFluid = tag.getInt(TAG_PROGRESS_FLUID);
        processFluidTime = tag.getInt(TAG_PROCESS_FLUID_TIME);
        progressOre = tag.getInt(TAG_PROGRESS_ORE);
        processOreTime = tag.getInt(TAG_PROCESS_ORE_TIME);
        lastSelectedGui = tag.getInt(TAG_LAST_SELECTED_GUI);
        leftStack = readMaterialStack(tag, TAG_LEFT_TYPE, TAG_LEFT_AMOUNT);
        rightStack = readMaterialStack(tag, TAG_RIGHT_TYPE, TAG_RIGHT_AMOUNT);
    }

    @Nullable
    private static MaterialStack readMaterialStack(CompoundTag tag, String typeKey, String amountKey) {
        if (!tag.contains(typeKey)) {
            return null;
        }
        return new MaterialStack(Mats.matById.get(tag.getInt(typeKey)), tag.getInt(amountKey));
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
            return SLOT_METAL_OUTPUT_END - SLOT_FLUID_BYPRODUCT_START + 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = SLOT_FLUID_BYPRODUCT_START + slot;
            return mapped >= SLOT_FLUID_BYPRODUCT_START && mapped <= SLOT_METAL_OUTPUT_END
                    ? items.getStackInSlot(mapped)
                    : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = SLOT_FLUID_BYPRODUCT_START + slot;
            return mapped >= SLOT_FLUID_BYPRODUCT_START && mapped <= SLOT_METAL_OUTPUT_END
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = SLOT_FLUID_BYPRODUCT_START + slot;
            return mapped >= SLOT_FLUID_BYPRODUCT_START && mapped <= SLOT_METAL_OUTPUT_END
                    ? items.getSlotLimit(mapped)
                    : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }

}
