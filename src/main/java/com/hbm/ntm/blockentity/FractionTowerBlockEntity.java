package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.PairRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
        PairRecipe recipe = LegacyOilFluidRecipes.getFractioning(level, inputTank.getTankType());
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
            return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                    List.of(), List.of(), List.of(inputTank), List.of(leftOutputTank, rightOutputTank)).changed();
        }
        return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                List.of(), List.of(recipe.left(), recipe.right()),
                List.of(), List.of(leftOutputTank, rightOutputTank)).changed();
    }

    private boolean fractionate(PairRecipe recipe) {
        HbmFluidRecipeIO.RecipeFluidIoProcessReport report = HbmFluidRecipeIO.processLegacyFixedRecipeIoReport(
                List.of(HbmFluidRecipeIO.requirementFromTank(inputTank, 100)),
                List.of(recipe.left(), recipe.right()),
                List.of(inputTank),
                List.of(leftOutputTank, rightOutputTank),
                false);
        if (!report.complete()) {
            return false;
        }
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

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.allCompactFluidUserTanks(this));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).writeToNbt(tag, "tank" + i);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        List<HbmFluidTank> tanks = getAllTanks();
        for (int i = 0; i < tanks.size(); i++) {
            String key = "tank" + i;
            if (hasTankTag(tag, key)) {
                tanks.get(i).readFromNbt(tag, key);
            }
        }
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }
}
