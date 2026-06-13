package com.hbm.inventory.fluid.trait;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait.PollutionKind;
import com.hbm.ntm.pollution.PollutionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Legacy package facade for 1.7.10's polluting fluid trait.
 */
@Deprecated(forRemoval = false)
public class FT_Polluting extends PollutingFluidTrait {
    public final HashMap<PollutionHandler.PollutionType, Float> releaseMap = new HashMap<>();
    public final HashMap<PollutionHandler.PollutionType, Float> burnMap = new HashMap<>();

    public FT_Polluting release(PollutionHandler.PollutionType type, float amount) {
        if (type != null) {
            releaseMap.put(type, amount);
            PollutionKind kind = PollutionKind.byPollutionType(type.modern());
            if (kind != null) {
                super.release(kind, amount);
            }
        }
        return this;
    }

    public FT_Polluting release(com.hbm.ntm.pollution.PollutionType type, float amount) {
        return release(PollutionHandler.fromModern(type), amount);
    }

    public FT_Polluting burn(PollutionHandler.PollutionType type, float amount) {
        if (type != null) {
            burnMap.put(type, amount);
            PollutionKind kind = PollutionKind.byPollutionType(type.modern());
            if (kind != null) {
                super.burn(kind, amount);
            }
        }
        return this;
    }

    public FT_Polluting burn(com.hbm.ntm.pollution.PollutionType type, float amount) {
        return burn(PollutionHandler.fromModern(type), amount);
    }

    public static void pollute(Level level, int x, int y, int z, FluidType type,
            FluidTrait.FluidReleaseType release, float amountMb) {
        pollute(level, new BlockPos(x, y, z), type, release, amountMb);
    }

    public static void pollute(Level level, BlockPos pos, FluidType type,
            FluidTrait.FluidReleaseType release, float amountMb) {
        com.hbm.ntm.fluid.FluidReleaseType modernRelease = release == null
                ? com.hbm.ntm.fluid.FluidReleaseType.SPILL
                : release.modern();
        pollute(level, pos, type, modernRelease, amountMb);
    }

    public static void pollute(Level level, int x, int y, int z, FluidType type,
            com.hbm.ntm.fluid.FluidReleaseType release, float amountMb) {
        pollute(level, new BlockPos(x, y, z), type, release, amountMb);
    }

    public static void pollute(Level level, BlockPos pos, FluidType type,
            com.hbm.ntm.fluid.FluidReleaseType release, float amountMb) {
        if (level == null || pos == null || type == null || type == HbmFluids.NONE
                || release == com.hbm.ntm.fluid.FluidReleaseType.VOID
                || !Float.isFinite(amountMb) || amountMb == 0.0F) {
            return;
        }

        PollutingFluidTrait trait = type.getTrait(PollutingFluidTrait.class);
        if (trait == null) {
            return;
        }

        Map<PollutionKind, Float> source = release == com.hbm.ntm.fluid.FluidReleaseType.BURN
                ? trait.getBurnPollution()
                : trait.getReleasePollution();
        for (Map.Entry<PollutionKind, Float> entry : source.entrySet()) {
            float amount = entry.getValue() * amountMb;
            if (Float.isFinite(amount) && amount != 0.0F) {
                PollutionManager.incrementPollution(level, pos, entry.getKey().pollutionType(), amount);
            }
        }
    }
}
