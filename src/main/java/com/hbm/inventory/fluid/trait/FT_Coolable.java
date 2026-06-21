package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_Coolable.
 */
@Deprecated(forRemoval = false)
public class FT_Coolable extends CoolableFluidTrait {
    public FluidType coolsTo;
    public int amountReq;
    public int amountProduced;
    public int heatEnergy;
    private final Map<CoolingType, Double> efficiency = new EnumMap<>(CoolingType.class);

    public FT_Coolable() {
        this(HbmFluids.NONE, 0, 0, 0);
    }

    public FT_Coolable(FluidType type, int req, int prod, int heat) {
        super(type == null ? HbmFluids.NONE : type, req, prod, heat);
        this.coolsTo = type == null ? HbmFluids.NONE : type;
        this.amountReq = req;
        this.amountProduced = prod;
        this.heatEnergy = heat;
    }

    public FT_Coolable setEff(CoolingType type, double eff) {
        if (type != null) {
            efficiency.put(type, eff);
        }
        return this;
    }

    public double getEfficiency(CoolingType type) {
        return efficiency.getOrDefault(type, 0.0D);
    }

    @Override
    public double getEfficiency(CoolableFluidTrait.CoolingType type) {
        if (type == null) {
            return 0.0D;
        }
        return getEfficiency(CoolingType.valueOf(type.name()));
    }

    @Override
    public FluidType getCoolsTo() {
        return coolsTo;
    }

    @Override
    public int getAmountRequired() {
        return amountReq;
    }

    @Override
    public int getAmountProduced() {
        return amountProduced;
    }

    @Override
    public int getHeatEnergy() {
        return heatEnergy;
    }

    @Override
    public String getLegacyName() {
        return "coolable";
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        info.add(Component.literal("Thermal capacity: " + heatEnergy + " TU per " + amountReq + "mB")
                .withStyle(ChatFormatting.RED));
        for (CoolingType type : CoolingType.values()) {
            double eff = getEfficiency(type);
            if (eff > 0.0D) {
                info.add(Component.literal("[" + type.displayName + "] ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("Efficiency: " + (int) (eff * 100.0D) + "%")
                                .withStyle(ChatFormatting.AQUA)));
            }
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("coolsTo", coolsTo.getName());
        object.addProperty("amountReq", amountReq);
        object.addProperty("amountProd", amountProduced);
        object.addProperty("heatEnergy", heatEnergy);
        for (CoolingType type : CoolingType.values()) {
            double eff = getEfficiency(type);
            if (eff > 0.0D) {
                object.addProperty(type.name(), eff);
            }
        }
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("coolsTo").value(coolsTo.getName());
        writer.name("amountReq").value(amountReq);
        writer.name("amountProd").value(amountProduced);
        writer.name("heatEnergy").value(heatEnergy);
        for (CoolingType type : CoolingType.values()) {
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
        this.coolsTo = object.has("coolsTo") ? Fluids.fromName(object.get("coolsTo").getAsString()) : HbmFluids.NONE;
        this.amountReq = object.has("amountReq") ? object.get("amountReq").getAsInt() : 0;
        this.amountProduced = object.has("amountProd") ? object.get("amountProd").getAsInt() : 0;
        this.heatEnergy = object.has("heatEnergy") ? object.get("heatEnergy").getAsInt() : 0;
        for (CoolingType type : CoolingType.values()) {
            if (object.has(type.name())) {
                setEff(type, object.get(type.name()).getAsDouble());
            }
        }
    }

    public enum CoolingType {
        TURBINE("Turbine Steam"),
        HEATEXCHANGER("Coolable");

        public final String displayName;

        CoolingType(String displayName) {
            this.displayName = displayName;
        }
    }
}
