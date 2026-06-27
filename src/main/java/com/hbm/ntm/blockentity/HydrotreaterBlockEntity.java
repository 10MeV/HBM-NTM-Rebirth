package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.TripleRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class HydrotreaterBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_CONTAINER = 1;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 2;
    public static final int SLOT_HYDROGEN_INPUT = 3;
    public static final int SLOT_HYDROGEN_OUTPUT = 4;
    public static final int SLOT_OUTPUT_LEFT_CONTAINER = 5;
    public static final int SLOT_OUTPUT_LEFT_CONTAINER_OUTPUT = 6;
    public static final int SLOT_OUTPUT_RIGHT_CONTAINER = 7;
    public static final int SLOT_OUTPUT_RIGHT_CONTAINER_OUTPUT = 8;
    public static final int SLOT_IDENTIFIER = 9;
    public static final int SLOT_CATALYST = 10;
    public static final int ITEM_COUNT = 11;
    private static final long MAX_POWER = 1_000_000L;
    private static final long POWER_PER_OPERATION = 20_000L;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank hydrogenTank;
    private final HbmFluidTank desulfurizedOilTank;
    private final HbmFluidTank sourGasTank;

    public HydrotreaterBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.OIL, 64_000),
                tank(HbmFluids.HYDROGEN, 64_000, 1),
                tank(HbmFluids.OIL_DS, 24_000),
                tank(HbmFluids.SOURGAS, 24_000));
    }

    private HydrotreaterBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank hydrogenTank, HbmFluidTank desulfurizedOilTank, HbmFluidTank sourGasTank) {
        super(ModBlockEntities.HYDROTREATER.get(), pos, state, MAX_POWER,
                List.of(inputTank, hydrogenTank, desulfurizedOilTank, sourGasTank),
                List.of(inputTank, hydrogenTank),
                List.of(desulfurizedOilTank, sourGasTank),
                true, ITEM_COUNT);
        this.inputTank = inputTank;
        this.hydrogenTank = hydrogenTank;
        this.desulfurizedOilTank = desulfurizedOilTank;
        this.sourGasTank = sourGasTank;
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 25;
    }

    @Override
    public LegacyGuiProfile getLegacyGuiProfile() {
        return LegacyGuiProfile.HYDROTREATER;
    }

    @Override
    protected String legacyContainerKey() {
        return "container.hydrotreater";
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        boolean changed = setInputTypeFromIdentifier();
        changed |= processFluidContainers();
        chargeFromSlot(SLOT_BATTERY);
        if (level.getGameTime() % 2L == 0L) {
            changed |= reform();
        } else {
            changed |= setupRecipeTanks(LegacyOilFluidRecipes.getHydrotreating(level, inputTank.getTankType()));
        }
        return changed;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_BATTERY, SLOT_INPUT_CONTAINER, SLOT_OUTPUT_LEFT_CONTAINER, SLOT_OUTPUT_RIGHT_CONTAINER,
                 SLOT_IDENTIFIER, SLOT_CATALYST -> true;
            default -> false;
        };
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return fixedSurroundingPorts();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return energyPortsFromOffsets(List.of(
                new BlockPos(2, 0, 1),
                new BlockPos(2, 0, -1),
                new BlockPos(-2, 0, 1),
                new BlockPos(-2, 0, -1),
                new BlockPos(1, 0, 2),
                new BlockPos(-1, 0, 2),
                new BlockPos(1, 0, -2),
                new BlockPos(-1, 0, -2)));
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
                HbmFluidItemTransfer.loadTransfers(
                        SLOT_HYDROGEN_INPUT, SLOT_HYDROGEN_OUTPUT, hydrogenTank),
                HbmFluidItemTransfer.unloadTransfers(
                        SLOT_OUTPUT_LEFT_CONTAINER, SLOT_OUTPUT_LEFT_CONTAINER_OUTPUT, 2,
                        desulfurizedOilTank, sourGasTank)));
    }

    private boolean reform() {
        TripleRecipe recipe = LegacyOilFluidRecipes.getHydrotreating(level, inputTank.getTankType());
        boolean changed = setupRecipeTanks(recipe);
        if (recipe == null || !hasCatalyst()) {
            return changed;
        }
        if (energy.getPower() < POWER_PER_OPERATION) {
            return changed;
        }
        HbmFluidRecipeIO.RecipeFluidIoProcessReport report = HbmFluidRecipeIO.processLegacyFixedRecipeIoReport(
                List.of(HbmFluidRecipeIO.requirementFromTank(inputTank, 100), recipe.first()),
                List.of(recipe.second(), recipe.third()),
                List.of(inputTank, hydrogenTank),
                List.of(desulfurizedOilTank, sourGasTank),
                false);
        if (!report.complete()) {
            return changed;
        }
        consumePower(POWER_PER_OPERATION);
        onFluidContentsChanged();
        return true;
    }

    private boolean setupRecipeTanks(TripleRecipe recipe) {
        if (recipe == null) {
            return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                    List.of(), List.of(), List.of(), List.of(desulfurizedOilTank, sourGasTank)).changed();
        }
        return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                List.of(recipe.first()),
                List.of(recipe.second(), recipe.third()),
                List.of(hydrogenTank),
                List.of(desulfurizedOilTank, sourGasTank)).changed();
    }

    private boolean hasCatalyst() {
        ItemStackHandler items = getItems();
        return items != null && items.getStackInSlot(SLOT_CATALYST).is(ModItems.CATALYTIC_CONVERTER.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", energy.getPower());
        inputTank.writeToNbt(tag, "t0");
        hydrogenTank.writeToNbt(tag, "t1");
        desulfurizedOilTank.writeToNbt(tag, "t2");
        sourGasTank.writeToNbt(tag, "t3");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        if (tag.contains("t0")) {
            inputTank.readFromNbt(tag, "t0");
        }
        if (tag.contains("t1")) {
            hydrogenTank.readFromNbt(tag, "t1");
        }
        if (tag.contains("t2")) {
            desulfurizedOilTank.readFromNbt(tag, "t2");
        }
        if (tag.contains("t3")) {
            sourGasTank.readFromNbt(tag, "t3");
        }
    }
}
