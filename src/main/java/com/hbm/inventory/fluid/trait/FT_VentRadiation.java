package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.FluidReleaseType;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidReleaseEffects;
import com.hbm.ntm.fluid.trait.VentRadiationFluidTrait;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Legacy package bridge for 1.7.10 FT_VentRadiation.
 */
@Deprecated(forRemoval = false)
public class FT_VentRadiation extends VentRadiationFluidTrait {
    private float radPerMB;

    public FT_VentRadiation() {
        this(0.0F);
    }

    public FT_VentRadiation(float rad) {
        super(rad);
        this.radPerMB = rad;
    }

    public float getRadPerMB() {
        return radPerMB;
    }

    @Override
    public float getRadiationPerMb() {
        return radPerMB;
    }

    @Override
    public String getLegacyName() {
        return "ventradiation";
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Radioactive]").withStyle(ChatFormatting.YELLOW));
    }

    public void onFluidRelease(Level level, int x, int y, int z, com.hbm.inventory.fluid.tank.FluidTank tank,
            int overflowAmount, FluidTrait.FluidReleaseType release) {
        FluidType type = tank == null ? null : tank.getTankType();
        if (type != null) {
            FluidReleaseType modernRelease = release == null ? FluidReleaseType.SPILL : release.modern();
            HbmFluidReleaseEffects.applyRelease(level, new BlockPos(x, y, z), type, overflowAmount, modernRelease);
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("radiation", radPerMB);
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("radiation").value(radPerMB);
    }

    public void deserializeJSON(JsonObject object) {
        if (object != null && object.has("radiation")) {
            this.radPerMB = object.get("radiation").getAsFloat();
        }
    }
}
