package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class VentRadiationFluidTrait extends FluidTrait {
    private final float radiationPerMb;

    public VentRadiationFluidTrait(float radiationPerMb) {
        this.radiationPerMb = radiationPerMb;
    }

    public float getRadiationPerMb() {
        return radiationPerMb;
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Radioactive]").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("radiation", radiationPerMb);
    }
}
