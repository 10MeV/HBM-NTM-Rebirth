package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait.PollutionKind;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
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

    public static ReleaseBatchReport previewTanks(Iterable<HbmFluidTank> tanks, int amountPerTank, FluidReleaseType releaseType) {
        return releaseTanks(null, null, tanks, amountPerTank, releaseType, true);
    }

    public static ReleaseBatchReport previewAllTanks(Iterable<HbmFluidTank> tanks, FluidReleaseType releaseType) {
        return releaseAllTanks(null, null, tanks, releaseType, true);
    }

    public static ReleaseBatchReport releaseTanks(
            Level level, BlockPos pos, Iterable<HbmFluidTank> tanks, int amountPerTank, FluidReleaseType releaseType, boolean simulate) {
        List<ReleaseReport> reports = new ArrayList<>();
        if (tanks != null) {
            int amount = Math.max(0, amountPerTank);
            for (HbmFluidTank tank : tanks) {
                if (tank != null) {
                    reports.add(tank.release(level, pos, amount, releaseType, simulate));
                }
            }
        }
        return ReleaseBatchReport.of(reports);
    }

    public static ReleaseBatchReport releaseAllTanks(
            Level level, BlockPos pos, Iterable<HbmFluidTank> tanks, FluidReleaseType releaseType, boolean simulate) {
        List<ReleaseReport> reports = new ArrayList<>();
        if (tanks != null) {
            for (HbmFluidTank tank : tanks) {
                if (tank != null) {
                    reports.add(tank.releaseAll(level, pos, releaseType, simulate));
                }
            }
        }
        return ReleaseBatchReport.of(reports);
    }

    private static ReleaseReport collectEffects(FluidType fluid, int amountMb, FluidReleaseType releaseType, boolean apply, Level level, BlockPos pos) {
        FluidType type = fluid == null ? HbmFluids.NONE : fluid;
        int amount = Math.max(0, amountMb);
        FluidReleaseType release = releaseType == null ? FluidReleaseType.SPILL : releaseType;
        EnumMap<PollutionKind, Float> pollution = new EnumMap<>(PollutionKind.class);
        PollutionSavedData.PollutionSample worldPollution = new PollutionSavedData.PollutionSample();
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
                    if (pollutionAmount > 0.0F) {
                        worldPollution.add(entry.getKey().pollutionType(), pollutionAmount);
                    }
                }
            }
        }

        if (apply && level != null && pos != null && worldPollution.hasAnyPollution()) {
            PollutionManager.incrementPollution(level, pos, worldPollution);
        }

        return new ReleaseReport(type, amount, release, radiation, pollution);
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

        public float getPollution(PollutionType type) {
            PollutionKind kind = PollutionKind.byPollutionType(type);
            return kind == null ? 0.0F : pollution.getOrDefault(kind, 0.0F);
        }

        public PollutionSavedData.PollutionSample getPollutionSample() {
            PollutionSavedData.PollutionSample sample = new PollutionSavedData.PollutionSample();
            for (Map.Entry<PollutionKind, Float> entry : pollution.entrySet()) {
                sample.add(entry.getKey().pollutionType(), entry.getValue());
            }
            return sample;
        }

        public String formatPollution() {
            return getPollutionSample().formatValues();
        }

        public boolean hasWorldEffects() {
            return radiation > 0.0F || !pollution.isEmpty();
        }
    }

    public static final class ReleaseBatchReport {
        private final List<ReleaseReport> reports;
        private final int amountMb;
        private final float radiation;
        private final EnumMap<PollutionKind, Float> pollution;

        private ReleaseBatchReport(List<ReleaseReport> reports, int amountMb, float radiation, EnumMap<PollutionKind, Float> pollution) {
            this.reports = reports;
            this.amountMb = amountMb;
            this.radiation = radiation;
            this.pollution = pollution;
        }

        private static ReleaseBatchReport of(List<ReleaseReport> reports) {
            List<ReleaseReport> entries = reports == null ? List.of() : List.copyOf(reports);
            int amount = 0;
            float radiation = 0.0F;
            EnumMap<PollutionKind, Float> pollution = new EnumMap<>(PollutionKind.class);
            for (ReleaseReport report : entries) {
                if (report == null) {
                    continue;
                }
                amount += report.getAmountMb();
                radiation += report.getRadiation();
                for (Map.Entry<PollutionKind, Float> entry : report.getPollution().entrySet()) {
                    pollution.merge(entry.getKey(), entry.getValue(), Float::sum);
                }
            }
            return new ReleaseBatchReport(entries, amount, radiation, pollution);
        }

        public List<ReleaseReport> getReports() {
            return reports;
        }

        public int getTankReports() {
            return reports.size();
        }

        public int getAmountMb() {
            return amountMb;
        }

        public float getRadiation() {
            return radiation;
        }

        public Map<PollutionKind, Float> getPollution() {
            return Collections.unmodifiableMap(pollution);
        }

        public float getPollution(PollutionType type) {
            PollutionKind kind = PollutionKind.byPollutionType(type);
            return kind == null ? 0.0F : pollution.getOrDefault(kind, 0.0F);
        }

        public PollutionSavedData.PollutionSample getPollutionSample() {
            PollutionSavedData.PollutionSample sample = new PollutionSavedData.PollutionSample();
            for (Map.Entry<PollutionKind, Float> entry : pollution.entrySet()) {
                sample.add(entry.getKey().pollutionType(), entry.getValue());
            }
            return sample;
        }

        public String formatPollution() {
            return getPollutionSample().formatValues();
        }

        public boolean hasWorldEffects() {
            return radiation > 0.0F || !pollution.isEmpty();
        }
    }
}
