package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_Corrosive.
 */
@Deprecated(forRemoval = false)
public class FT_Corrosive extends CorrosiveFluidTrait {
    private int rating;

    public FT_Corrosive() {
        this(0);
    }

    public FT_Corrosive(int rating) {
        super(rating);
        this.rating = rating;
    }

    @Override
    public int getRating() {
        return rating;
    }

    @Override
    public boolean isHighlyCorrosive() {
        return rating > 50;
    }

    @Override
    public String getLegacyName() {
        return "corrosive";
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal(isHighlyCorrosive() ? "[Strongly Corrosive]" : "[Corrosive]")
                .withStyle(isHighlyCorrosive() ? ChatFormatting.GOLD : ChatFormatting.YELLOW));
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("rating", rating);
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("rating").value(rating);
    }

    public void deserializeJSON(JsonObject object) {
        if (object != null && object.has("rating")) {
            this.rating = object.get("rating").getAsInt();
        }
    }
}
