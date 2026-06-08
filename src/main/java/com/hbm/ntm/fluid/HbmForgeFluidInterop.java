package com.hbm.ntm.fluid;

/**
 * Forge's standard fluid capability has no pressure channel. Only 0 PU HBM
 * fluid may cross that boundary; pressurized fluid must stay on the MK2 path.
 */
public final class HbmForgeFluidInterop {
    public static final int STANDARD_PRESSURE = 0;

    public static boolean isStandardPressure(int pressure) {
        return HbmFluidTank.clampPressure(pressure) == STANDARD_PRESSURE;
    }

    public static boolean canFillFromForge(HbmFluidTank tank, int pressure) {
        return tank != null && isStandardPressure(pressure) && isStandardPressure(tank.getPressure());
    }

    public static boolean canExposeToForge(HbmFluidTank tank) {
        return tank != null
                && isStandardPressure(tank.getPressure())
                && tank.getFill() > 0
                && HbmFluidForgeMappings.canExport(tank.getTankType());
    }

    public static boolean canExposeItemToForge(HbmFluidContainerItemAccess item, net.minecraft.world.item.ItemStack stack) {
        return item != null && stack != null && !stack.isEmpty() && isStandardPressure(item.getPressure(stack));
    }

    public interface HbmFluidContainerItemAccess {
        int getPressure(net.minecraft.world.item.ItemStack stack);
    }

    private HbmForgeFluidInterop() {
    }
}
