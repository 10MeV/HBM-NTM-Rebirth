package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.trait.PoisonFluidTrait;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for deprecated 1.7.10 FT_Poison.
 */
@Deprecated(forRemoval = false)
public class FT_Poison extends PoisonFluidTrait {
    private boolean withering;
    private int level;

    public FT_Poison() {
        this(false, 0);
    }

    public FT_Poison(boolean withering, int level) {
        super(withering, level);
        this.withering = withering;
        this.level = level;
    }

    @Override
    public boolean isWithering() {
        return withering;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getLegacyName() {
        return "poison";
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        info.add(Component.literal("[Toxic Fumes]").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("level", level);
        object.addProperty("withering", withering);
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("level").value(level);
        writer.name("withering").value(withering);
    }

    public void deserializeJSON(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("level")) {
            this.level = object.get("level").getAsInt();
        }
        if (object.has("withering")) {
            this.withering = object.get("withering").getAsBoolean();
        }
    }
}
