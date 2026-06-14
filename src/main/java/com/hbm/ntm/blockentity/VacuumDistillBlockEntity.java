package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.VacuumRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

public class VacuumDistillBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    private static final String TAG_IS_ON = "isOn";
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT_CONTAINER = 1;
    public static final int SLOT_INPUT_CONTAINER_OUTPUT = 2;
    public static final int SLOT_OUTPUT_HEAVY_CONTAINER = 3;
    public static final int SLOT_OUTPUT_HEAVY_CONTAINER_OUTPUT = 4;
    public static final int SLOT_OUTPUT_REFORMATE_CONTAINER = 5;
    public static final int SLOT_OUTPUT_REFORMATE_CONTAINER_OUTPUT = 6;
    public static final int SLOT_OUTPUT_LIGHT_CONTAINER = 7;
    public static final int SLOT_OUTPUT_LIGHT_CONTAINER_OUTPUT = 8;
    public static final int SLOT_OUTPUT_GAS_CONTAINER = 9;
    public static final int SLOT_OUTPUT_GAS_CONTAINER_OUTPUT = 10;
    public static final int SLOT_IDENTIFIER = 11;
    public static final int ITEM_COUNT = 12;
    private static final long MAX_POWER = 1_000_000L;
    private static final long POWER_PER_OPERATION = 10_000L;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank heavyOilTank;
    private final HbmFluidTank reformateTank;
    private final HbmFluidTank lightOilTank;
    private final HbmFluidTank sourGasTank;
    private boolean isOn;
    private Object audioLoop;

    public VacuumDistillBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.OIL, 64_000, 2),
                tank(HbmFluids.HEAVYOIL_VACUUM, 24_000),
                tank(HbmFluids.REFORMATE, 24_000),
                tank(HbmFluids.LIGHTOIL_VACUUM, 24_000),
                tank(HbmFluids.SOURGAS, 24_000));
    }

    private VacuumDistillBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank heavyOilTank, HbmFluidTank reformateTank, HbmFluidTank lightOilTank,
            HbmFluidTank sourGasTank) {
        super(ModBlockEntities.VACUUM_DISTILL.get(), pos, state, MAX_POWER,
                List.of(inputTank, heavyOilTank, reformateTank, lightOilTank, sourGasTank),
                List.of(inputTank),
                List.of(heavyOilTank, reformateTank, lightOilTank, sourGasTank),
                true, ITEM_COUNT);
        this.inputTank = inputTank;
        this.heavyOilTank = heavyOilTank;
        this.reformateTank = reformateTank;
        this.lightOilTank = lightOilTank;
        this.sourGasTank = sourGasTank;
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 150;
    }

    @Override
    public LegacyGuiProfile getLegacyGuiProfile() {
        return LegacyGuiProfile.VACUUM_DISTILL;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, VacuumDistillBlockEntity blockEntity) {
        blockEntity.updateAudioLoop();
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        boolean wasOn = isOn;
        isOn = false;
        boolean changed = setInputTypeFromIdentifier();
        changed |= processFluidContainers();
        chargeFromSlot(SLOT_BATTERY);
        changed |= refine();
        return changed || wasOn != isOn;
    }

    @Override
    protected boolean isItemValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_BATTERY, SLOT_OUTPUT_HEAVY_CONTAINER, SLOT_OUTPUT_REFORMATE_CONTAINER,
                 SLOT_OUTPUT_LIGHT_CONTAINER, SLOT_OUTPUT_GAS_CONTAINER, SLOT_IDENTIFIER -> true;
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
        return items != null && setFluidTankTypeFromIdentifierSlot(items, SLOT_IDENTIFIER, inputTank, 2, true);
    }

    private boolean processFluidContainers() {
        ItemStackHandler items = getItems();
        return items != null && processFluidItemTransfers(items, HbmFluidItemTransfer.combineTransfers(
                HbmFluidItemTransfer.loadTransfers(
                        SLOT_INPUT_CONTAINER, SLOT_INPUT_CONTAINER_OUTPUT, inputTank),
                HbmFluidItemTransfer.unloadTransfers(
                        SLOT_OUTPUT_HEAVY_CONTAINER, SLOT_OUTPUT_HEAVY_CONTAINER_OUTPUT, 2,
                        heavyOilTank, reformateTank, lightOilTank, sourGasTank)));
    }

    private boolean refine() {
        VacuumRecipe recipe = LegacyOilFluidRecipes.getVacuum(inputTank.getTankType());
        boolean changed = setupRecipeTanks(recipe);
        if (recipe == null) {
            return changed;
        }
        if (energy.getPower() < POWER_PER_OPERATION) {
            return changed;
        }
        HbmFluidRecipeIO.RecipeFluidIoProcessReport report = HbmFluidRecipeIO.processLegacyFixedRecipeIoReport(
                List.of(HbmFluidRecipeIO.requirementFromTank(inputTank, 100)),
                List.of(recipe.outputs()),
                List.of(inputTank),
                List.of(heavyOilTank, reformateTank, lightOilTank, sourGasTank),
                false);
        if (!report.complete()) {
            return changed;
        }
        consumePower(POWER_PER_OPERATION);
        isOn = true;
        onFluidContentsChanged();
        return true;
    }

    private boolean setupRecipeTanks(VacuumRecipe recipe) {
        List<HbmFluidStack> outputs = recipe == null ? List.of() : List.of(recipe.outputs());
        return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                List.of(), outputs, List.of(), List.of(heavyOilTank, reformateTank, lightOilTank, sourGasTank))
                .changed();
    }

    private void updateAudioLoop() {
        if (level == null || !level.isClientSide) {
            return;
        }
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.boiler",
                isOn, 30.0D, 15.0F, 0.25F, 1.0F);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean(TAG_IS_ON, isOn);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        isOn = tag.getBoolean(TAG_IS_ON);
    }
}
