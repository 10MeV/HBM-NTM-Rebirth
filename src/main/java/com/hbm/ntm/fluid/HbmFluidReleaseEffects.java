package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait.PollutionKind;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class HbmFluidReleaseEffects {

    public static ReleaseReport previewRelease(FluidType fluid, int amountMb, FluidReleaseType releaseType) {
        return collectEffects(fluid, amountMb, releaseType, false, null, null);
    }

    public static ReleaseReport applyRelease(Level level, BlockPos pos, FluidType fluid, int amountMb, FluidReleaseType releaseType) {
        return collectEffects(fluid, amountMb, releaseType, true, level, pos);
    }

    public static ReleaseReport applyRelease(Level level, BlockPos pos, HbmFluidTank tank, int amountMb, FluidReleaseType releaseType) {
        FluidType fluid = tank == null ? HbmFluids.NONE : tank.getTankType();
        return applyRelease(level, pos, fluid, amountMb, releaseType);
    }

    private static ReleaseReport collectEffects(FluidType fluid, int amountMb, FluidReleaseType releaseType, boolean apply, Level level, BlockPos pos) {
        FluidType type = fluid == null ? HbmFluids.NONE : fluid;
        int amount = Math.max(0, amountMb);
        FluidReleaseType release = releaseType == null ? FluidReleaseType.SPILL : releaseType;
        EnumMap<PollutionKind, Float> pollution = new EnumMap<>(PollutionKind.class);
        float radiation = 0.0F;

        if (type != HbmFluids.NONE && amount > 0 && release != FluidReleaseType.VOID) {
            VentRadiationFluidTrait radiationTrait = type.getTrait(VentRadiationFluidTrait.class);
            if (radiationTrait != null) {
                radiation = radiationTrait.getRadiationPerMb() * amount;
                if (apply && radiation > 0.0F && level != null && pos != null) {
                    ChunkRadiationManager.incrementRadiation(level, pos, radiation);
                }
            }

            PollutingFluidTrait pollutingTrait = type.getTrait(PollutingFluidTrait.class);
            if (pollutingTrait != null) {
                Map<PollutionKind, Float> source = release == FluidReleaseType.BURN
                        ? pollutingTrait.getBurnPollution()
                        : pollutingTrait.getReleasePollution();
                for (Map.Entry<PollutionKind, Float> entry : source.entrySet()) {
                    float pollutionAmount = entry.getValue() * amount;
                    pollution.merge(entry.getKey(), pollutionAmount, Float::sum);
                    if (apply && pollutionAmount > 0.0F && level != null && pos != null) {
                        PollutionManager.incrementPollution(level, pos, toPollutionType(entry.getKey()), pollutionAmount);
                    }
                }
            }
        }

        return new ReleaseReport(type, amount, release, radiation, pollution);
    }

    private static PollutionType toPollutionType(PollutionKind kind) {
        return switch (kind) {
            case SOOT -> PollutionType.SOOT;
            case POISON -> PollutionType.POISON;
            case HEAVY_METAL -> PollutionType.HEAVYMETAL;
            case FALLOUT -> PollutionType.FALLOUT;
        };
    }

    private HbmFluidReleaseEffects() {
    }

    public static final class ReleaseReport {
        private final FluidType fluid;
        private final int amountMb;
        private final FluidReleaseType releaseType;
        private final float radiation;
        private final EnumMap<PollutionKind, Float> pollution;

        private ReleaseReport(FluidType fluid, int amountMb, FluidReleaseType releaseType, float radiation, EnumMap<PollutionKind, Float> pollution) {
            this.fluid = fluid;
            this.amountMb = amountMb;
            this.releaseType = releaseType;
            this.radiation = radiation;
            this.pollution = pollution;
        }

        public FluidType getFluid() {
            return fluid;
        }

        public int getAmountMb() {
            return amountMb;
        }

        public FluidReleaseType getReleaseType() {
            return releaseType;
        }

        public float getRadiation() {
            return radiation;
        }

        public Map<PollutionKind, Float> getPollution() {
            return Collections.unmodifiableMap(pollution);
        }

        public boolean hasWorldEffects() {
            return radiation > 0.0F || !pollution.isEmpty();
        }
    }
}
