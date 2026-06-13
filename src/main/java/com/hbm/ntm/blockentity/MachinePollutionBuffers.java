package com.hbm.ntm.blockentity;

import api.hbm.fluid.IFluidStandardSender;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 TileEntityMachinePolluting-compatible smoke buffers.
 */
public final class MachinePollutionBuffers implements IFluidStandardSender {
    private static final String TAG_SOOT = "smoke0";
    private static final String TAG_HEAVY_METAL = "smoke1";
    private static final String TAG_POISON = "smoke2";

    public final FluidTank smoke;
    public final FluidTank smoke_leaded;
    public final FluidTank smoke_poison;
    private final List<HbmFluidTank> tanks;

    public MachinePollutionBuffers(int capacity) {
        this(new FluidTank(HbmFluids.SMOKE, capacity),
                new FluidTank(HbmFluids.SMOKE_LEADED, capacity),
                new FluidTank(HbmFluids.SMOKE_POISON, capacity));
    }

    public MachinePollutionBuffers(FluidTank soot, FluidTank heavyMetal, FluidTank poison) {
        this.smoke = soot;
        this.smoke_leaded = heavyMetal;
        this.smoke_poison = poison;
        this.tanks = List.of(soot, heavyMetal, poison);
    }

    public List<HbmFluidTank> tanks() {
        return tanks;
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return tanks;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return tanks;
    }

    public FluidTank soot() {
        return smoke;
    }

    public FluidTank heavyMetal() {
        return smoke_leaded;
    }

    public FluidTank poison() {
        return smoke_poison;
    }

    public boolean pollute(Level level, BlockPos pos, PollutionType type, float amount) {
        return SmokeExhaustPollution.polluteBuffered(level, pos, tankFor(type), type, amount);
    }

    public boolean pollute(Level level, BlockPos pos, com.hbm.handler.pollution.PollutionHandler.PollutionType type,
            float amount) {
        return pollute(level, pos, type == null ? null : type.modern(), amount);
    }

    public boolean polluteFluidRelease(Level level, BlockPos pos, FluidType fluid, FluidReleaseType release,
            float legacyIgnoredAmountMb) {
        return SmokeExhaustPollution.polluteBuffered(level, pos, smoke, smoke_leaded, smoke_poison,
                fluid, release, legacyIgnoredAmountMb);
    }

    public boolean polluteFluidRelease(Level level, BlockPos pos, FluidType fluid,
            com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType release, float legacyIgnoredAmountMb) {
        FluidReleaseType modernRelease = release == null ? FluidReleaseType.SPILL : release.modern();
        return polluteFluidRelease(level, pos, fluid, modernRelease, legacyIgnoredAmountMb);
    }

    public boolean sendSmoke(Level level, int x, int y, int z, Direction directionFromSender) {
        if (level == null || directionFromSender == null) {
            return false;
        }
        boolean sent = false;
        BlockPos connectorPos = new BlockPos(x, y, z);
        if (smoke.getFill() > 0) {
            sent |= sendFluid(smoke, level, connectorPos, directionFromSender);
        }
        if (smoke_leaded.getFill() > 0) {
            sent |= sendFluid(smoke_leaded, level, connectorPos, directionFromSender);
        }
        if (smoke_poison.getFill() > 0) {
            sent |= sendFluid(smoke_poison, level, connectorPos, directionFromSender);
        }
        return sent;
    }

    public FluidTank[] getSmokeTanks() {
        return new FluidTank[] {smoke, smoke_leaded, smoke_poison};
    }

    public GenericMachineRecipeRuntime.PollutionSink customMachineRecipeSink() {
        return this::applyCustomMachineRecipePollution;
    }

    private boolean applyCustomMachineRecipePollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (amount > 0.0F) {
            pollute(level, pos, type, amount);
            return true;
        }
        return PollutionManager.applyPollutionDelta(level, pos, type, amount);
    }

    public void writeLegacyNbt(CompoundTag tag) {
        smoke.writeToNBT(tag, TAG_SOOT);
        smoke_leaded.writeToNBT(tag, TAG_HEAVY_METAL);
        smoke_poison.writeToNBT(tag, TAG_POISON);
    }

    public void readLegacyNbt(CompoundTag tag) {
        smoke.readFromNBT(tag, TAG_SOOT);
        smoke_leaded.readFromNBT(tag, TAG_HEAVY_METAL);
        smoke_poison.readFromNBT(tag, TAG_POISON);
        normalizeTypes();
    }

    public void normalizeTypes() {
        normalizeType(smoke, HbmFluids.SMOKE);
        normalizeType(smoke_leaded, HbmFluids.SMOKE_LEADED);
        normalizeType(smoke_poison, HbmFluids.SMOKE_POISON);
    }

    @Nullable
    public FluidTank tankFor(PollutionType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SOOT -> smoke;
            case HEAVYMETAL -> smoke_leaded;
            case POISON -> smoke_poison;
            case FALLOUT -> null;
        };
    }

    private static void normalizeType(HbmFluidTank tank, FluidType type) {
        if (tank.getTankType() == HbmFluids.NONE) {
            int fill = tank.getFill();
            tank.setTankType(type);
            tank.setFill(fill);
        }
    }
}
