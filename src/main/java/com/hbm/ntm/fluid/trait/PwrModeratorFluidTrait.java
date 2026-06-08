package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PwrModeratorFluidTrait extends FluidTrait {
    private final double multiplier;

    public PwrModeratorFluidTrait(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[PWR Flux Multiplier]").withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        int percent = (int) (multiplier * 100.0D - 100.0D);
        info.add(Component.literal("Core flux " + (percent >= 0 ? "+" : "") + percent + "%")
                .withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("multiplier", multiplier);
    }
}
