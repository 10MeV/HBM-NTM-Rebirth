package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.trait.PheromoneFluidTrait;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_Pheromone.
 */
@Deprecated(forRemoval = false)
public class FT_Pheromone extends PheromoneFluidTrait {
    public int type;

    public FT_Pheromone() {
        this(0);
    }

    public FT_Pheromone(int type) {
        super(type);
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getLegacyName() {
        return "pheromone";
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal(type == 1 ? "[Glyphid Pheromones]" : "[Modified Pheromones]")
                .withStyle(type == 1 ? ChatFormatting.AQUA : ChatFormatting.BLUE));
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("type", type);
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("type").value(type);
    }

    public void deserializeJSON(JsonObject object) {
        if (object != null && object.has("type")) {
            this.type = object.get("type").getAsInt();
        }
    }
}
