package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_Heatable.
 */
@Deprecated(forRemoval = false)
public class FT_Heatable extends HeatableFluidTrait {
    public FT_Heatable addStep(int heat, int req, FluidType type, int prod) {
        super.addStep(heat, req, type, prod);
        return this;
    }

    public FT_Heatable setEff(HeatingType type, double eff) {
        if (type != null) {
            super.setEfficiency(type.modern(), eff);
        }
        return this;
    }

    public double getEfficiency(HeatingType type) {
        return type == null ? 0.0D : super.getEfficiency(type.modern());
    }

    @Override
    public String getLegacyName() {
        return "heatable";
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        HeatableFluidTrait.HeatingStep first = getFirstStep();
        if (first != null) {
            info.add(Component.literal("Thermal capacity: " + first.heatRequired() + " TU per "
                    + first.amountRequired() + "mB").withStyle(ChatFormatting.RED));
        }
        for (HeatingType type : HeatingType.values()) {
            double eff = getEfficiency(type);
            if (eff > 0.0D) {
                info.add(Component.literal("[" + type.displayName + "] ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("Efficiency: " + (int) (eff * 100.0D) + "%")
                                .withStyle(ChatFormatting.AQUA)));
            }
        }
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("steps").beginArray();
        for (HeatableFluidTrait.HeatingStep step : getSteps()) {
            writer.beginObject();
            writer.name("typeProduced").value(step.producedType().getName());
            writer.name("amountReq").value(step.amountRequired());
            writer.name("amountProd").value(step.amountProduced());
            writer.name("heatReq").value(step.heatRequired());
            writer.endObject();
        }
        writer.endArray();
        for (HeatingType type : HeatingType.values()) {
            double eff = getEfficiency(type);
            if (eff > 0.0D) {
                writer.name(type.name()).value(eff);
            }
        }
    }

    public void deserializeJSON(JsonObject object) {
        if (object == null) {
            return;
        }
        JsonArray steps = object.has("steps") ? object.getAsJsonArray("steps") : new JsonArray();
        for (int i = 0; i < steps.size(); i++) {
            JsonObject step = steps.get(i).getAsJsonObject();
            addStep(step.get("heatReq").getAsInt(), step.get("amountReq").getAsInt(),
                    Fluids.fromName(step.get("typeProduced").getAsString()),
                    step.get("amountProd").getAsInt());
        }
        for (HeatingType type : HeatingType.values()) {
            if (object.has(type.name())) {
                setEff(type, object.get(type.name()).getAsDouble());
            }
        }
    }

    public enum HeatingType {
        BOILER("Boilable"),
        HEATEXCHANGER("Heatable"),
        PWR("PWR Coolant"),
        ICF("ICF Coolant"),
        PA("Particle Accelerator Coolant");

        public final String displayName;

        HeatingType(String displayName) {
            this.displayName = displayName;
        }

        private HeatableFluidTrait.HeatingType modern() {
            return HeatableFluidTrait.HeatingType.valueOf(name());
        }
    }
}
