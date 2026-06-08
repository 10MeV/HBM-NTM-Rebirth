package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait.PollutionKind;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

final class SmokeExhaustPollution {
    static final FluidType[] SMOKES = {
            HbmFluids.SMOKE,
            HbmFluids.SMOKE_LEADED,
            HbmFluids.SMOKE_POISON
    };

    static boolean isSmoke(FluidType type) {
        return type == HbmFluids.SMOKE || type == HbmFluids.SMOKE_LEADED || type == HbmFluids.SMOKE_POISON;
    }

    static void pollute(Level level, BlockPos pos, FluidType type, long amountMb) {
        PollutionType pollutionType = toPollutionType(type);
        if (pollutionType == null || level == null || pos == null || amountMb <= 0L) {
            return;
        }
        PollutionManager.incrementPollution(level, pos, pollutionType, amountMb / 100.0F);
    }

    static boolean polluteBuffered(Level level, BlockPos pos, HbmFluidTank tank, PollutionType type, float amount) {
        FluidType smokeType = toSmokeType(type);
        if (smokeType == null || tank == null || amount <= 0.0F || !Float.isFinite(amount)) {
            return false;
        }
        int fluidAmount = (int) Math.ceil(amount * 100.0F);
        int accepted = tank.fill(smokeType, fluidAmount, 0, false);
        int overflow = fluidAmount - accepted;
        if (overflow > 0) {
            if (level != null && pos != null) {
                PollutionManager.incrementPollution(level, pos, type, overflow / 100.0F);
            }
            return true;
        }
        return false;
    }

    static boolean polluteBuffered(Level level, BlockPos pos, HbmFluidTank sootTank, HbmFluidTank heavyMetalTank,
            HbmFluidTank poisonTank, FluidType fluid, FluidReleaseType release, float ignoredAmountMb) {
        FluidType type = fluid == null ? HbmFluids.NONE : fluid;
        if (type == HbmFluids.NONE || release == FluidReleaseType.VOID) {
            return false;
        }
        PollutingFluidTrait trait = type.getTrait(PollutingFluidTrait.class);
        if (trait == null) {
            return false;
        }

        Map<PollutionKind, Float> source = release == FluidReleaseType.BURN
                ? trait.getBurnPollution()
                : trait.getReleasePollution();
        boolean overflowed = false;
        for (Map.Entry<PollutionKind, Float> entry : source.entrySet()) {
            PollutionType pollutionType = toPollutionType(entry.getKey());
            HbmFluidTank tank = tankFor(pollutionType, sootTank, heavyMetalTank, poisonTank);
            overflowed |= polluteBuffered(level, pos, tank, pollutionType, entry.getValue());
        }
        return overflowed;
    }

    private static PollutionType toPollutionType(FluidType type) {
        if (type == HbmFluids.SMOKE) {
            return PollutionType.SOOT;
        }
        if (type == HbmFluids.SMOKE_LEADED) {
            return PollutionType.HEAVYMETAL;
        }
        if (type == HbmFluids.SMOKE_POISON) {
            return PollutionType.POISON;
        }
        return null;
    }

    private static PollutionType toPollutionType(PollutionKind kind) {
        return switch (kind) {
            case SOOT -> PollutionType.SOOT;
            case HEAVY_METAL -> PollutionType.HEAVYMETAL;
            case POISON -> PollutionType.POISON;
            case FALLOUT -> PollutionType.FALLOUT;
        };
    }

    private static FluidType toSmokeType(PollutionType type) {
        if (type == null) {
            return null;
        }
        if (type == PollutionType.SOOT) {
            return HbmFluids.SMOKE;
        }
        if (type == PollutionType.HEAVYMETAL) {
            return HbmFluids.SMOKE_LEADED;
        }
        return HbmFluids.SMOKE_POISON;
    }

    private static HbmFluidTank tankFor(PollutionType type, HbmFluidTank sootTank, HbmFluidTank heavyMetalTank,
            HbmFluidTank poisonTank) {
        if (type == PollutionType.SOOT) {
            return sootTank;
        }
        if (type == PollutionType.HEAVYMETAL) {
            return heavyMetalTank;
        }
        return poisonTank;
    }

    private SmokeExhaustPollution() {
    }
}
