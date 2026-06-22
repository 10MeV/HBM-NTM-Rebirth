package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidPortLayouts.LegacyPort;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes;
import com.hbm.ntm.fluid.LegacyOilFluidRecipes.PairRecipe;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CatalyticCrackerBlockEntity extends LegacyRemoteFluidMachineBlockEntity {
    private static final int INPUT_CAPACITY = 4_000;
    private static final int STEAM_CAPACITY = 8_000;
    private static final int OUTPUT_CAPACITY = 4_000;
    private static final int SPENT_STEAM_CAPACITY = 800;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank steamTank;
    private final HbmFluidTank leftOutputTank;
    private final HbmFluidTank rightOutputTank;
    private final HbmFluidTank spentSteamTank;

    public CatalyticCrackerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state,
                tank(HbmFluids.BITUMEN, INPUT_CAPACITY),
                tank(HbmFluids.STEAM, STEAM_CAPACITY),
                tank(HbmFluids.OIL, OUTPUT_CAPACITY),
                tank(HbmFluids.PETROLEUM, OUTPUT_CAPACITY),
                tank(HbmFluids.SPENTSTEAM, SPENT_STEAM_CAPACITY));
    }

    private CatalyticCrackerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank steamTank, HbmFluidTank leftOutputTank, HbmFluidTank rightOutputTank,
            HbmFluidTank spentSteamTank) {
        super(ModBlockEntities.CATALYTIC_CRACKER.get(), pos, state, 0L,
                List.of(inputTank, steamTank, leftOutputTank, rightOutputTank, spentSteamTank),
                List.of(inputTank, steamTank),
                List.of(leftOutputTank, rightOutputTank, spentSteamTank),
                false);
        this.inputTank = inputTank;
        this.steamTank = steamTank;
        this.leftOutputTank = leftOutputTank;
        this.rightOutputTank = rightOutputTank;
        this.spentSteamTank = spentSteamTank;
    }

    @Override
    protected int legacyNetworkPackRange() {
        return 25;
    }

    @Override
    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        PairRecipe recipe = LegacyOilFluidRecipes.getCracking(inputTank.getTankType());
        boolean changed = setupTanks(recipe);
        if (recipe != null && level.getGameTime() % 5L == 0L) {
            changed |= crack(recipe);
        }
        return changed;
    }

    @Override
    protected void refreshFluidPorts() {
        HbmFluidPortMachine.refreshReceiverPorts(level, worldPosition, getFluidPorts(),
                List.of(inputTank, steamTank), this);
        if (level != null && level.getGameTime() % 10L == 0L) {
            HbmFluidPortMachine.refreshProviderPorts(level, worldPosition, getFluidPorts(),
                    List.of(leftOutputTank, rightOutputTank, spentSteamTank), this);
        }
    }

    @Override
    public boolean canSetInputTypeWithIdentifier() {
        return true;
    }

    @Override
    public boolean setInputTypeFromIdentifier(FluidType type) {
        if (type == null || type == HbmFluids.NONE || inputTank.getTankType() == type) {
            return false;
        }
        inputTank.conform(new HbmFluidStack(type, 0));
        onFluidContentsChanged();
        return true;
    }

    private boolean setupTanks(PairRecipe recipe) {
        if (recipe == null) {
            return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                    List.of(), List.of(), List.of(), List.of(leftOutputTank, rightOutputTank, spentSteamTank))
                    .changed();
        }
        return HbmFluidRecipeIO.setupLegacyFixedRecipeTanks(
                List.of(new HbmFluidStack(HbmFluids.STEAM, 200, steamTank.getPressure())),
                List.of(recipe.left(), recipe.right(), new HbmFluidStack(HbmFluids.SPENTSTEAM, 2)),
                List.of(steamTank),
                List.of(leftOutputTank, rightOutputTank, spentSteamTank)).changed();
    }

    private boolean crack(PairRecipe recipe) {
        int ops = 0;
        for (int i = 0; i < 2; i++) {
            HbmFluidRecipeIO.RecipeFluidIoProcessReport report = HbmFluidRecipeIO.processLegacyFixedRecipeIoReport(
                    List.of(HbmFluidRecipeIO.requirementFromTank(inputTank, 100),
                            HbmFluidRecipeIO.requirementFromTank(steamTank, 200)),
                    List.of(recipe.left(), recipe.right(), new HbmFluidStack(HbmFluids.SPENTSTEAM, 2)),
                    List.of(inputTank, steamTank),
                    List.of(leftOutputTank, rightOutputTank, spentSteamTank),
                    false);
            if (!report.complete()) {
                break;
            }
            ops++;
        }
        if (ops > 0) {
            onFluidContentsChanged();
        }
        return ops > 0;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.allCompactFluidUserTanks(this));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return HbmFluidPortLayouts.legacy(facing, rot,
                LegacyPort.of(4, 1, facing),
                LegacyPort.of(4, -2, facing),
                LegacyPort.of(-4, 1, facing.getOpposite()),
                LegacyPort.of(-4, -2, facing.getOpposite()),
                LegacyPort.of(2, 3, rot),
                LegacyPort.of(2, -4, rot),
                LegacyPort.of(-2, 3, rot.getOpposite()),
                LegacyPort.of(-2, -4, rot.getOpposite()));
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
            if (tag.contains(key)) {
                tanks.get(i).readFromNbt(tag, key);
            }
        }
    }
}
