package com.hbm.ntm.fluid.trait;

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

    public enum FuelGrade {
        LOW,
        MEDIUM,
        HIGH,
        AERO,
        GAS
    }
}
