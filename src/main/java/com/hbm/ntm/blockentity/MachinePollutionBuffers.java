package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * 1.7.10 TileEntityMachinePolluting-compatible smoke buffers.
 */
final class MachinePollutionBuffers implements HbmStandardFluidSender {
    private static final String TAG_SOOT = "smoke0";
    private static final String TAG_HEAVY_METAL = "smoke1";
    private static final String TAG_POISON = "smoke2";

    private final HbmFluidTank soot;
    private final HbmFluidTank heavyMetal;
    private final HbmFluidTank poison;
    private final List<HbmFluidTank> tanks;

    MachinePollutionBuffers(int capacity) {
        this(new HbmFluidTank(HbmFluids.SMOKE, capacity),
                new HbmFluidTank(HbmFluids.SMOKE_LEADED, capacity),
                new HbmFluidTank(HbmFluids.SMOKE_POISON, capacity));
    }

    MachinePollutionBuffers(HbmFluidTank soot, HbmFluidTank heavyMetal, HbmFluidTank poison) {
        this.soot = soot;
        this.heavyMetal = heavyMetal;
        this.poison = poison;
        this.tanks = List.of(soot, heavyMetal, poison);
    }

    List<HbmFluidTank> tanks() {
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

    HbmFluidTank soot() {
        return soot;
    }

    HbmFluidTank heavyMetal() {
        return heavyMetal;
    }

    HbmFluidTank poison() {
        return poison;
    }

    boolean pollute(Level level, BlockPos pos, PollutionType type, float amount) {
        return SmokeExhaustPollution.polluteBuffered(level, pos, tankFor(type), type, amount);
    }

    boolean polluteFluidRelease(Level level, BlockPos pos, FluidType fluid, FluidReleaseType release,
            float legacyIgnoredAmountMb) {
        return SmokeExhaustPollution.polluteBuffered(level, pos, soot, heavyMetal, poison,
                fluid, release, legacyIgnoredAmountMb);
    }

    GenericMachineRecipeRuntime.PollutionSink customMachineRecipeSink() {
        return this::applyCustomMachineRecipePollution;
    }

    private boolean applyCustomMachineRecipePollution(Level level, BlockPos pos, PollutionType type, float amount) {
        if (amount > 0.0F) {
            pollute(level, pos, type, amount);
            return true;
        }
        return PollutionManager.applyPollutionDelta(level, pos, type, amount);
    }

    void writeLegacyNbt(CompoundTag tag) {
        soot.writeToNbt(tag, TAG_SOOT);
        heavyMetal.writeToNbt(tag, TAG_HEAVY_METAL);
        poison.writeToNbt(tag, TAG_POISON);
    }

    void readLegacyNbt(CompoundTag tag) {
        soot.readFromNbt(tag, TAG_SOOT);
        heavyMetal.readFromNbt(tag, TAG_HEAVY_METAL);
        poison.readFromNbt(tag, TAG_POISON);
        normalizeTypes();
    }

    void normalizeTypes() {
        normalizeType(soot, HbmFluids.SMOKE);
        normalizeType(heavyMetal, HbmFluids.SMOKE_LEADED);
        normalizeType(poison, HbmFluids.SMOKE_POISON);
    }

    @Nullable
    HbmFluidTank tankFor(PollutionType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SOOT -> soot;
            case HEAVYMETAL -> heavyMetal;
            case POISON -> poison;
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
