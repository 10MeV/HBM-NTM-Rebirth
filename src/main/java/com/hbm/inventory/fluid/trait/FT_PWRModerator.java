package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.trait.PwrModeratorFluidTrait;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_PWRModerator.
 */
@Deprecated(forRemoval = false)
public class FT_PWRModerator extends PwrModeratorFluidTrait {
    private double multiplier;

    public FT_PWRModerator() {
        this(1.0D);
    }

    public FT_PWRModerator(double multiplier) {
        super(multiplier);
        this.multiplier = multiplier;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String getLegacyName() {
        return "pwrmoderator";
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

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("multiplier").value(multiplier);
    }

    public void deserializeJSON(JsonObject object) {
        if (object != null && object.has("multiplier")) {
            this.multiplier = object.get("multiplier").getAsDouble();
        }
    }
}
