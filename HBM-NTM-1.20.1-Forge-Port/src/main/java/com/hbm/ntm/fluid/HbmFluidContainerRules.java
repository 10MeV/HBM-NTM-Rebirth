package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.ContainerFluidTrait;

public final class HbmFluidContainerRules {
    public static final int BUCKET_CAPACITY = 1_000;
    public static final int BOTTLE_CAPACITY = 250;
    public static final int SMALL_CONTAINER_CAPACITY = 1_000;
    public static final int BARREL_CAPACITY = 16_000;
    public static final int FLUID_PACK_CAPACITY = 32_000;
    public static final int DISPERSER_CAPACITY = 2_000;
    public static final int GLYPHID_GLAND_CAPACITY = 4_000;

    public static boolean accepts(ContainerKind kind, FluidType type) {
        if (type == null || type == HbmFluids.NONE) {
            return false;
        }
        return switch (kind) {
            case CANISTER -> canFillCanister(type);
            case GAS_TANK -> canFillGasTank(type);
            case FLUID_TANK -> canFillFluidTank(type);
            case LEAD_FLUID_TANK -> canFillLeadFluidTank(type);
            case FLUID_BARREL -> canFillFluidBarrel(type);
            case FLUID_PACK -> canFillFluidPack(type);
            case DISPERSER_CANISTER -> canFillDisperserCanister(type);
            case GLYPHID_GLAND -> canFillGlyphidGland(type);
        };
    }

    public static int capacity(ContainerKind kind) {
        return switch (kind) {
            case FLUID_BARREL -> BARREL_CAPACITY;
            case FLUID_PACK -> FLUID_PACK_CAPACITY;
            case DISPERSER_CANISTER -> DISPERSER_CAPACITY;
            case GLYPHID_GLAND -> GLYPHID_GLAND_CAPACITY;
            default -> SMALL_CONTAINER_CAPACITY;
        };
    }

    public static boolean canFillCanister(FluidType type) {
        ContainerFluidTrait trait = type.getTrait(ContainerFluidTrait.class);
        return trait != null && trait.hasCanister();
    }

    public static boolean canFillGasTank(FluidType type) {
        ContainerFluidTrait trait = type.getTrait(ContainerFluidTrait.class);
        return trait != null && trait.hasGasTank();
    }

    public static boolean canFillFluidTank(FluidType type) {
        return canUseGeneralContainer(type) && !type.needsLeadContainer();
    }

    public static boolean canFillLeadFluidTank(FluidType type) {
        return canUseGeneralContainer(type);
    }

    public static boolean canFillFluidBarrel(FluidType type) {
        return canUseGeneralContainer(type) && !type.needsLeadContainer();
    }

    public static boolean canFillFluidPack(FluidType type) {
        return canUseGeneralContainer(type) && !type.needsLeadContainer();
    }

    public static boolean canFillDisperserCanister(FluidType type) {
        return type != null && type.isDispersible();
    }

    public static boolean canFillGlyphidGland(FluidType type) {
        return type == HbmFluids.PHEROMONE || type == HbmFluids.SULFURIC_ACID;
    }

    public static boolean canUseGeneralContainer(FluidType type) {
        return type != null && type != HbmFluids.NONE && !type.hasNoContainer();
    }

    public enum ContainerKind {
        CANISTER("canister_empty", "canister_full"),
        GAS_TANK("gas_empty", "gas_full"),
        FLUID_TANK("fluid_tank_empty", "fluid_tank_full"),
        LEAD_FLUID_TANK("fluid_tank_lead_empty", "fluid_tank_lead_full"),
        FLUID_BARREL("fluid_barrel_empty", "fluid_barrel_full"),
        FLUID_PACK("fluid_pack_empty", "fluid_pack_full"),
        DISPERSER_CANISTER("disperser_canister_empty", "disperser_canister"),
        GLYPHID_GLAND("glyphid_gland_empty", "glyphid_gland");

        private final String emptyLegacyName;
        private final String fullLegacyName;

        ContainerKind(String emptyLegacyName, String fullLegacyName) {
            this.emptyLegacyName = emptyLegacyName;
            this.fullLegacyName = fullLegacyName;
        }

        public String getEmptyLegacyName() {
            return emptyLegacyName;
        }

        public String getFullLegacyName() {
            return fullLegacyName;
        }
    }

    private HbmFluidContainerRules() {
    }
}
