package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.fluid.trait.CombustibleFluidTrait;
import com.hbm.ntm.fluid.trait.FluidTooltipUtil;
import java.io.IOException;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Legacy package bridge for 1.7.10 FT_Combustible.
 */
@Deprecated(forRemoval = false)
public class FT_Combustible extends CombustibleFluidTrait {
    private FuelGrade fuelGrade;
    private long combustionEnergy;

    public FT_Combustible() {
        this(FuelGrade.LOW, 0L);
    }

    public FT_Combustible(FuelGrade grade, long energy) {
        super(grade == null ? CombustibleFluidTrait.FuelGrade.LOW : grade.modern(), energy);
        this.fuelGrade = grade == null ? FuelGrade.LOW : grade;
        this.combustionEnergy = energy;
    }

    public long getCombustionEnergy() {
        return combustionEnergy;
    }

    @Override
    public long getCombustionEnergyPerBucket() {
        return combustionEnergy;
    }

    public FuelGrade getGradeLegacy() {
        return fuelGrade;
    }

    @Override
    public CombustibleFluidTrait.FuelGrade getGrade() {
        return fuelGrade.modern();
    }

    @Override
    public String getLegacyName() {
        return "combustible";
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Combustible]").withStyle(ChatFormatting.GOLD));
        if (combustionEnergy > 0) {
            info.add(Component.literal("Provides ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(FluidTooltipUtil.shortNumber(combustionEnergy) + "HE ")
                            .withStyle(ChatFormatting.RED))
                    .append(Component.literal("per bucket").withStyle(ChatFormatting.GOLD)));
            info.add(Component.literal("Fuel grade: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(fuelGrade.getGrade()).withStyle(ChatFormatting.RED)));
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("energy", combustionEnergy);
        object.addProperty("grade", fuelGrade.name());
    }

    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("energy").value(combustionEnergy);
        writer.name("grade").value(fuelGrade.name());
    }

    public void deserializeJSON(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("energy")) {
            this.combustionEnergy = object.get("energy").getAsLong();
        }
        if (object.has("grade")) {
            this.fuelGrade = FuelGrade.valueOf(object.get("grade").getAsString());
        }
    }

    public enum FuelGrade {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        AERO("Aviation"),
        GAS("Gaseous");

        private final String grade;

        FuelGrade(String grade) {
            this.grade = grade;
        }

        public String getGrade() {
            return grade;
        }

        private CombustibleFluidTrait.FuelGrade modern() {
            return CombustibleFluidTrait.FuelGrade.valueOf(name());
        }
    }
}
