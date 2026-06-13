package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.PairRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FractionTowerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    private static final int TANK_CAPACITY = 4_000;
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.cardinal(2);

    private final HbmFluidTank inputTank;
    private final HbmFluidTank leftOutputTank;
    private final HbmFluidTank rightOutputTank;

    public FractionTowerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.HEAVYOIL, TANK_CAPACITY),
                tank(HbmFluids.BITUMEN, TANK_CAPACITY),
                tank(HbmFluids.SMEAR, TANK_CAPACITY));
    }

    private FractionTowerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank leftOutputTank, HbmFluidTank rightOutputTank) {
        super(ModBlockEntities.FRACTION_TOWER.get(), pos, state, 0L,
                List.of(inputTank, leftOutputTank, rightOutputTank),
                List.of(inputTank),
                List.of(leftOutputTank, rightOutputTank),
                false);
        this.inputTank = inputTank;
        this.leftOutputTank = leftOutputTank;
        this.rightOutputTank = rightOutputTank;
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 50;
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        boolean changed = transferStackedTowerFluids(level, pos);
        PairRecipe recipe = LegacyOilFluidRecipes.getFractioning(inputTank.getTankType());
        changed |= setupTanks(recipe);
        if (recipe != null && level.getGameTime() % 10L == 0L) {
            changed |= fractionate(recipe);
        }
        return changed;
    }

    @Override
    public boolean canSetInputTypeWithIdentifier() {
        return true;
    }

    public boolean isBottomSegment() {
        return level == null || !(level.getBlockEntity(worldPosition.below(3)) instanceof FractionTowerBlockEntity);
    }

    @Override
    public boolean setInputTypeFromIdentifier(FluidType type) {
        if (!isBottomSegment() || type == null || type == HbmFluids.NONE || inputTank.getTankType() == type) {
            return false;
        }
        inputTank.conform(new HbmFluidStack(type, 0));
        onFluidContentsChanged();
        return true;
    }

    private boolean setupTanks(PairRecipe recipe) {
        if (recipe == null) {
            boolean changed = inputTank.getTankType() != HbmFluids.NONE
                    || leftOutputTank.getTankType() != HbmFluids.NONE
                    || rightOutputTank.getTankType() != HbmFluids.NONE;
            configureTank(inputTank, HbmFluids.NONE);
            configureTank(leftOutputTank, HbmFluids.NONE);
            configureTank(rightOutputTank, HbmFluids.NONE);
            return changed;
        }
        boolean changed = leftOutputTank.getTankType() != recipe.left().type()
                || rightOutputTank.getTankType() != recipe.right().type();
        configureTank(leftOutputTank, recipe.left().type());
        configureTank(rightOutputTank, recipe.right().type());
        return changed;
    }

    private boolean fractionate(PairRecipe recipe) {
        if (inputTank.getFill() < 100
                || !hasSpace(leftOutputTank, recipe.left().amount())
                || !hasSpace(rightOutputTank, recipe.right().amount())) {
            return false;
        }
        inputTank.setFill(inputTank.getFill() - 100);
        addOutput(leftOutputTank, recipe.left());
        addOutput(rightOutputTank, recipe.right());
        onFluidContentsChanged();
        return true;
    }

    private boolean transferStackedTowerFluids(Level level, BlockPos pos) {
        BlockEntity above = level.getBlockEntity(pos.above(3));
        if (!(above instanceof FractionTowerBlockEntity tower)) {
            return false;
        }

        boolean typeChanged = tower.inputTank.getTankType() != inputTank.getTankType()
                || tower.leftOutputTank.getTankType() != leftOutputTank.getTankType()
                || tower.rightOutputTank.getTankType() != rightOutputTank.getTankType();
        tower.inputTank.setTankType(inputTank.getTankType());
        tower.leftOutputTank.setTankType(leftOutputTank.getTankType());
        tower.rightOutputTank.setTankType(rightOutputTank.getTankType());

        int inputUp = Math.min(inputTank.getFill(), tower.inputTank.getMaxFill() - tower.inputTank.getFill());
        int leftDown = Math.min(tower.leftOutputTank.getFill(), leftOutputTank.getMaxFill() - leftOutputTank.getFill());
        int rightDown = Math.min(tower.rightOutputTank.getFill(), rightOutputTank.getMaxFill() - rightOutputTank.getFill());
        if (inputUp <= 0 && leftDown <= 0 && rightDown <= 0) {
            if (typeChanged) {
                tower.onFluidContentsChanged();
            }
            return typeChanged;
        }

        inputTank.setFill(inputTank.getFill() - inputUp);
        tower.inputTank.setFill(tower.inputTank.getFill() + inputUp);
        leftOutputTank.setFill(leftOutputTank.getFill() + leftDown);
        tower.leftOutputTank.setFill(tower.leftOutputTank.getFill() - leftDown);
        rightOutputTank.setFill(rightOutputTank.getFill() + rightDown);
        tower.rightOutputTank.setFill(tower.rightOutputTank.getFill() - rightDown);
        tower.onFluidContentsChanged();
        onFluidContentsChanged();
        return true;
    }

    private static void addOutput(HbmFluidTank tank, HbmFluidStack stack) {
        addFluid(tank, stack.type(), stack.amount());
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.allCompactFluidUserTanks(this));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }
}
