package com.hbm.ntm.neutron;

import java.util.List;

import net.minecraft.core.Direction;

public final class RBMKFluidBlockPlanner {
    public static final String PERFLUOROMETHYL_ID = "perfluoromethyl";
    public static final String WATER_ID = "water";
    public static final String SUPERHOT_STEAM_ID = "superhot_steam";

    private RBMKFluidBlockPlanner() {
    }

    public static LoaderConnectionPlan planLoaderConnection(Direction side, FluidTraits fluidTraits) {
        Direction safeSide = side == null ? Direction.NORTH : side;
        FluidTraits safeTraits = fluidTraits == null ? FluidTraits.none() : fluidTraits;
        boolean connect = safeSide == Direction.UP
                ? safeTraits.heatable()
                : safeTraits.coolable() || PERFLUOROMETHYL_ID.equals(safeTraits.legacyFluidId());
        return new LoaderConnectionPlan(safeSide, safeTraits.legacyFluidId(), connect);
    }

    public static FluidBlockContract blockContract(FluidBlockType type) {
        FluidBlockType safeType = type == null ? FluidBlockType.LOADER : type;
        return switch (safeType) {
            case LOADER -> new FluidBlockContract(
                    safeType,
                    "",
                    false,
                    true,
                    List.of(FluidTrait.HEATABLE, FluidTrait.COOLABLE),
                    List.of(PERFLUOROMETHYL_ID));
            case INLET -> new FluidBlockContract(
                    safeType,
                    "TileEntityRBMKInlet",
                    true,
                    true,
                    List.of(FluidTrait.COOLABLE),
                    List.of(WATER_ID));
            case OUTLET -> new FluidBlockContract(
                    safeType,
                    "TileEntityRBMKOutlet",
                    true,
                    true,
                    List.of(FluidTrait.HEATABLE),
                    List.of(SUPERHOT_STEAM_ID));
        };
    }

    public enum FluidBlockType {
        LOADER,
        INLET,
        OUTLET
    }

    public enum FluidTrait {
        HEATABLE,
        COOLABLE
    }

    public record FluidTraits(String legacyFluidId, boolean heatable, boolean coolable) {
        public FluidTraits {
            legacyFluidId = legacyFluidId == null ? "" : legacyFluidId;
        }

        public static FluidTraits none() {
            return new FluidTraits("", false, false);
        }
    }

    public record LoaderConnectionPlan(Direction side, String legacyFluidId, boolean canConnect) {
    }

    public record FluidBlockContract(
            FluidBlockType type,
            String legacyTileEntity,
            boolean createsBlockEntity,
            boolean addsStandardTooltip,
            List<FluidTrait> expectedTraits,
            List<String> explicitFluidIds) {
    }
}
