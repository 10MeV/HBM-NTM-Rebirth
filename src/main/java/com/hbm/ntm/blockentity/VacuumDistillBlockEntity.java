package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.VacuumRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
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
            case SLOT_BATTERY -> stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent();
            case SLOT_IDENTIFIER -> stack.getItem() instanceof IFluidIdentifierItem;
            case SLOT_OUTPUT_HEAVY_CONTAINER, SLOT_OUTPUT_REFORMATE_CONTAINER, SLOT_OUTPUT_LIGHT_CONTAINER,
                 SLOT_OUTPUT_GAS_CONTAINER -> true;
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
        if (items == null) {
            return false;
        }
        ItemStack stack = items.getStackInSlot(SLOT_IDENTIFIER);
        if (!(stack.getItem() instanceof IFluidIdentifierItem identifier)) {
            return false;
        }
        FluidType selected = identifier.getPrimaryType(stack);
        if (selected == null || selected == HbmFluids.NONE || inputTank.getTankType() == selected) {
            return false;
        }
        inputTank.conform(new com.hbm.ntm.fluid.HbmFluidStack(selected, 0, 2));
        inputTank.withPressure(2);
        return true;
    }

    private boolean processFluidContainers() {
        ItemStackHandler items = getItems();
        return items != null && HbmFluidItemTransfer.processTransfers(items, List.of(
                HbmFluidItemTransfer.TankSlotTransfer.load(SLOT_INPUT_CONTAINER,
                        SLOT_INPUT_CONTAINER_OUTPUT, inputTank),
                HbmFluidItemTransfer.TankSlotTransfer.unload(SLOT_OUTPUT_HEAVY_CONTAINER,
                        SLOT_OUTPUT_HEAVY_CONTAINER_OUTPUT, heavyOilTank),
                HbmFluidItemTransfer.TankSlotTransfer.unload(SLOT_OUTPUT_REFORMATE_CONTAINER,
                        SLOT_OUTPUT_REFORMATE_CONTAINER_OUTPUT, reformateTank),
                HbmFluidItemTransfer.TankSlotTransfer.unload(SLOT_OUTPUT_LIGHT_CONTAINER,
                        SLOT_OUTPUT_LIGHT_CONTAINER_OUTPUT, lightOilTank),
                HbmFluidItemTransfer.TankSlotTransfer.unload(SLOT_OUTPUT_GAS_CONTAINER,
                        SLOT_OUTPUT_GAS_CONTAINER_OUTPUT, sourGasTank)));
    }

    private boolean refine() {
        VacuumRecipe recipe = LegacyOilFluidRecipes.getVacuum(inputTank.getTankType());
        boolean changed = setupRecipeTanks(recipe);
        if (recipe == null) {
            return changed;
        }
        if (energy.getPower() < POWER_PER_OPERATION || inputTank.getFill() < 100
                || !hasSpace(heavyOilTank, recipe.heavyOil().amount())
                || !hasSpace(reformateTank, recipe.reformate().amount())
                || !hasSpace(lightOilTank, recipe.lightOil().amount())
                || !hasSpace(sourGasTank, recipe.gas().amount())) {
            return changed;
        }
        inputTank.setFill(inputTank.getFill() - 100);
        addFluid(heavyOilTank, recipe.heavyOil().type(), recipe.heavyOil().amount());
        addFluid(reformateTank, recipe.reformate().type(), recipe.reformate().amount());
        addFluid(lightOilTank, recipe.lightOil().type(), recipe.lightOil().amount());
        addFluid(sourGasTank, recipe.gas().type(), recipe.gas().amount());
        consumePower(POWER_PER_OPERATION);
        isOn = true;
        onFluidContentsChanged();
        return true;
    }

    private boolean setupRecipeTanks(VacuumRecipe recipe) {
        if (recipe == null) {
            boolean changed = heavyOilTank.getTankType() != HbmFluids.NONE
                    || reformateTank.getTankType() != HbmFluids.NONE
                    || lightOilTank.getTankType() != HbmFluids.NONE
                    || sourGasTank.getTankType() != HbmFluids.NONE;
            configureTank(heavyOilTank, HbmFluids.NONE);
            configureTank(reformateTank, HbmFluids.NONE);
            configureTank(lightOilTank, HbmFluids.NONE);
            configureTank(sourGasTank, HbmFluids.NONE);
            return changed;
        }
        boolean changed = heavyOilTank.getTankType() != recipe.heavyOil().type()
                || reformateTank.getTankType() != recipe.reformate().type()
                || lightOilTank.getTankType() != recipe.lightOil().type()
                || sourGasTank.getTankType() != recipe.gas().type();
        configureTank(heavyOilTank, recipe.heavyOil().type());
        configureTank(reformateTank, recipe.reformate().type());
        configureTank(lightOilTank, recipe.lightOil().type());
        configureTank(sourGasTank, recipe.gas().type());
        return changed;
    }

    private void updateAudioLoop() {
        if (level == null || !level.isClientSide) {
            return;
        }
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, ModSounds.BLOCK_BOILER.getId(),
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
