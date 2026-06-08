package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class CombustibleFluidTrait extends FluidTrait {
    private final FuelGrade grade;
    private final long combustionEnergyPerBucket;

    public CombustibleFluidTrait(FuelGrade grade, long combustionEnergyPerBucket) {
        this.grade = grade;
        this.combustionEnergyPerBucket = combustionEnergyPerBucket;
    }

    public FuelGrade getGrade() {
        return grade;
    }

    public long getCombustionEnergyPerBucket() {
        return combustionEnergyPerBucket;
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Combustible]").withStyle(ChatFormatting.GOLD));
        if (combustionEnergyPerBucket > 0) {
            info.add(Component.literal("Provides ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(FluidTooltipUtil.shortNumber(combustionEnergyPerBucket) + "HE ").withStyle(ChatFormatting.RED))
                    .append(Component.literal("per bucket").withStyle(ChatFormatting.GOLD)));
            info.add(Component.literal("Fuel grade: ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(grade.displayName()).withStyle(ChatFormatting.RED)));
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        object.addProperty("energy", combustionEnergyPerBucket);
        object.addProperty("grade", grade.name());
    }

    public enum FuelGrade {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        AERO("Aviation"),
        GAS("Gaseous");

        private final String displayName;

        FuelGrade(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }
}
