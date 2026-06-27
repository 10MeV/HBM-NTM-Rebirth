package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait;
import com.hbm.ntm.fluid.trait.PollutingFluidTrait.PollutionKind;
import com.hbm.ntm.pollution.PollutionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    public void addInfoHidden(List<String> info) {
        if (info == null) {
            return;
        }
        if (!releaseMap.isEmpty()) {
            info.add(ChatFormatting.GREEN + "When spilled:");
            for (Map.Entry<PollutionHandler.PollutionType, Float> entry : releaseMap.entrySet()) {
                info.add(ChatFormatting.GREEN + " - " + entry.getValue() + " " + entry.getKey() + " per mB");
            }
        }
        if (!burnMap.isEmpty()) {
            info.add(ChatFormatting.RED + "When burned:");
            for (Map.Entry<PollutionHandler.PollutionType, Float> entry : burnMap.entrySet()) {
                info.add(ChatFormatting.RED + " - " + entry.getValue() + " " + entry.getKey() + " per mB");
            }
        }
    }

    public void onFluidRelease(Level level, int x, int y, int z, com.hbm.inventory.fluid.tank.FluidTank tank,
            int overflowAmount, FluidTrait.FluidReleaseType release) {
        onFluidRelease(level, new BlockPos(x, y, z), release);
    }

    public void onFluidRelease(Level level, BlockPos pos, FluidTrait.FluidReleaseType release) {
        if (release == FluidTrait.FluidReleaseType.VOID) {
            return;
        }
        Map<PollutionHandler.PollutionType, Float> source = release == FluidTrait.FluidReleaseType.BURN
                ? burnMap
                : releaseMap;
        for (Map.Entry<PollutionHandler.PollutionType, Float> entry : source.entrySet()) {
            PollutionHandler.incrementPollution(level, pos, entry.getKey(), entry.getValue());
        }
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("release").beginObject();
        for (Map.Entry<PollutionHandler.PollutionType, Float> entry : releaseMap.entrySet()) {
            writer.name(entry.getKey().name()).value(entry.getValue());
        }
        writer.endObject();
        writer.name("burn").beginObject();
        for (Map.Entry<PollutionHandler.PollutionType, Float> entry : burnMap.entrySet()) {
            writer.name(entry.getKey().name()).value(entry.getValue());
        }
        writer.endObject();
    }

    public void deserializeJSON(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("release")) {
            JsonObject release = object.getAsJsonObject("release");
            for (PollutionHandler.PollutionType type : PollutionHandler.PollutionType.values()) {
                if (release.has(type.name())) {
                    release(type, LegacyFluidTraitJson.floatValue(release.get(type.name())));
                }
            }
        }
        if (object.has("burn")) {
            JsonObject burn = object.getAsJsonObject("burn");
            for (PollutionHandler.PollutionType type : PollutionHandler.PollutionType.values()) {
                if (burn.has(type.name())) {
                    burn(type, LegacyFluidTraitJson.floatValue(burn.get(type.name())));
                }
            }
        }
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
