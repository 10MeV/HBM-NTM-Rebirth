package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidPortLayouts.LegacyPort;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.TripleRecipe;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class CatalyticReformerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_CONTAINER = 1;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 2;
    public static final int SLOT_OUTPUT_1_CONTAINER = 3;
    public static final int SLOT_OUTPUT_1_CONTAINER_OUTPUT = 4;
    public static final int SLOT_OUTPUT_2_CONTAINER = 5;
    public static final int SLOT_OUTPUT_2_CONTAINER_OUTPUT = 6;
    public static final int SLOT_OUTPUT_3_CONTAINER = 7;
    public static final int SLOT_OUTPUT_3_CONTAINER_OUTPUT = 8;
    public static final int SLOT_IDENTIFIER = 9;
    public static final int SLOT_CATALYST = 10;
    public static final int ITEM_COUNT = 11;
    private static final long MAX_POWER = 1_000_000L;
    private static final long POWER_PER_OPERATION = 20_000L;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank reformateTank;
    private final HbmFluidTank petroleumTank;
    private final HbmFluidTank hydrogenTank;

    public CatalyticReformerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.NAPHTHA, 64_000),
                tank(HbmFluids.REFORMATE, 24_000),
                tank(HbmFluids.PETROLEUM, 24_000),
                tank(HbmFluids.HYDROGEN, 24_000));
    }

    private CatalyticReformerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank reformateTank, HbmFluidTank petroleumTank, HbmFluidTank hydrogenTank) {
        super(ModBlockEntities.CATALYTIC_REFORMER.get(), pos, state, MAX_POWER,
                List.of(inputTank, reformateTank, petroleumTank, hydrogenTank),
                List.of(inputTank),
                List.of(reformateTank, petroleumTank, hydrogenTank),
                true, ITEM_COUNT);
        this.inputTank = inputTank;
        this.reformateTank = reformateTank;
        this.petroleumTank = petroleumTank;
        this.hydrogenTank = hydrogenTank;
    }

    @Override
    public LegacyGuiProfile getLegacyGuiProfile() {
        return LegacyGuiProfile.CATALYTIC_REFORMER;
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        boolean changed = setInputTypeFromIdentifier();
        changed |= processFluidContainers();
        chargeFromSlot(SLOT_BATTERY);
        changed |= reform();
        return changed;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_BATTERY, SLOT_INPUT_CONTAINER, SLOT_OUTPUT_1_CONTAINER, SLOT_OUTPUT_2_CONTAINER,
                 SLOT_OUTPUT_3_CONTAINER, SLOT_IDENTIFIER, SLOT_CATALYST -> true;
            default -> false;
        };
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return HbmFluidPortLayouts.legacy(facing, rot,
                LegacyPort.of(2, 1, facing),
                LegacyPort.of(2, -1, facing),
                LegacyPort.of(-2, 1, facing.getOpposite()),
                LegacyPort.of(-2, -1, facing.getOpposite()),
                LegacyPort.of(0, 3, rot),
                LegacyPort.of(0, -3, rot.getOpposite()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                EnergyPort.of(facing.getStepX() * 2 + rot.getStepX(), 0,
                        facing.getStepZ() * 2 + rot.getStepZ(), facing),
                EnergyPort.of(facing.getStepX() * 2 - rot.getStepX(), 0,
                        facing.getStepZ() * 2 - rot.getStepZ(), facing),
                EnergyPort.of(-facing.getStepX() * 2 + rot.getStepX(), 0,
                        -facing.getStepZ() * 2 + rot.getStepZ(), facing.getOpposite()),
                EnergyPort.of(-facing.getStepX() * 2 - rot.getStepX(), 0,
                        -facing.getStepZ() * 2 - rot.getStepZ(), facing.getOpposite()),
                EnergyPort.of(rot.getStepX() * 3, 0, rot.getStepZ() * 3, rot),
                EnergyPort.of(-rot.getStepX() * 3, 0, -rot.getStepZ() * 3, rot.getOpposite()));
    }

    private boolean setInputTypeFromIdentifier() {
        ItemStackHandler items = getItems();
        return items != null && setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, inputTank);
    }

    private boolean processFluidContainers() {
        ItemStackHandler items = getItems();
        return items != null && processFluidItemTransfers(items, HbmFluidItemTransfer.combineTransfers(
                HbmFluidItemTransfer.loadTransfers(
                        SLOT_INPUT_CONTAINER, SLOT_INPUT_CONTAINER_OUTPUT, inputTank),
                HbmFluidItemTransfer.unloadTransfers(
                        SLOT_OUTPUT_1_CONTAINER, SLOT_OUTPUT_1_CONTAINER_OUTPUT, 2,
                        reformateTank, petroleumTank, hydrogenTank)));
    }

    private boolean reform() {
        TripleRecipe recipe = LegacyOilFluidRecipes.getReforming(inputTank.getTankType());
        boolean changed = setupRecipeTanks(recipe);
        if (recipe == null || !hasCatalyst()) {
            return changed;
        }
        if (energy.getPower() < POWER_PER_OPERATION || inputTank.getFill() < 100
                || !hasSpace(reformateTank, recipe.first().amount())
                || !hasSpace(petroleumTank, recipe.second().amount())
                || !hasSpace(hydrogenTank, recipe.third().amount())) {
            return changed;
        }
        inputTank.setFill(inputTank.getFill() - 100);
        addFluid(reformateTank, recipe.first().type(), recipe.first().amount());
        addFluid(petroleumTank, recipe.second().type(), recipe.second().amount());
        addFluid(hydrogenTank, recipe.third().type(), recipe.third().amount());
        consumePower(POWER_PER_OPERATION);
        onFluidContentsChanged();
        return true;
    }

    private boolean setupRecipeTanks(TripleRecipe recipe) {
        if (recipe == null) {
            boolean changed = reformateTank.getTankType() != HbmFluids.NONE
                    || petroleumTank.getTankType() != HbmFluids.NONE
                    || hydrogenTank.getTankType() != HbmFluids.NONE;
            configureTank(reformateTank, HbmFluids.NONE);
            configureTank(petroleumTank, HbmFluids.NONE);
            configureTank(hydrogenTank, HbmFluids.NONE);
            return changed;
        }
        boolean changed = reformateTank.getTankType() != recipe.first().type()
                || petroleumTank.getTankType() != recipe.second().type()
                || hydrogenTank.getTankType() != recipe.third().type();
        configureTank(reformateTank, recipe.first().type());
        configureTank(petroleumTank, recipe.second().type());
        configureTank(hydrogenTank, recipe.third().type());
        return changed;
    }

    private boolean hasCatalyst() {
        ItemStackHandler items = getItems();
        return items != null && items.getStackInSlot(SLOT_CATALYST).is(ModItems.CATALYTIC_CONVERTER.get());
    }
}
