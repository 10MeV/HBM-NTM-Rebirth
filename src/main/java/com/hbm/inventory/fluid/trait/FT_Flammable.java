package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.fluid.trait.FluidTooltipUtil;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_Flammable.
 */
@Deprecated(forRemoval = false)
public class FT_Flammable extends FlammableFluidTrait {
    private long energy;

    public FT_Flammable() {
        this(0L);
    }

    public FT_Flammable(long energy) {
        super(energy);
        this.energy = energy;
    }

    public long getHeatEnergy() {
        return energy;
    }

    @Override
    public long getHeatEnergyPerBucket() {
        return energy;
    }

    @Override
    public String getLegacyName() {
        return "flammable";
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Flammable]").withStyle(ChatFormatting.YELLOW));
        if (energy > 0) {
            info.add(Component.literal("Provides ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(FluidTooltipUtil.shortNumber(energy) + "TU ")
                            .withStyle(ChatFormatting.RED))
                    .append(Component.literal("per bucket").withStyle(ChatFormatting.YELLOW)));
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("energy", energy);
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("energy").value(energy);
    }

    public void deserializeJSON(JsonObject object) {
        if (object != null && object.has("energy")) {
            this.energy = object.get("energy").getAsLong();
        }
    }
}
