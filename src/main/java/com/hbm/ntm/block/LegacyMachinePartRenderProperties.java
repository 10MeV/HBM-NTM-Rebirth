package com.hbm.ntm.block;

public record LegacyMachinePartRenderProperties(
        LegacyMachinePartRenderMode mode,
        int color,
        int alpha,
        boolean hasColor,
        boolean fullBright) {

    public static LegacyMachinePartRenderProperties mode(LegacyMachinePartRenderMode mode) {
        return new LegacyMachinePartRenderProperties(mode, 0xFFFFFF, 255, false, false);
    }

    public static LegacyMachinePartRenderProperties color(int color, int alpha,
            LegacyMachinePartRenderMode mode) {
        return new LegacyMachinePartRenderProperties(mode, color & 0xFFFFFF, clamp(alpha), true, false);
    }

    public LegacyMachinePartRenderProperties asFullBright() {
        return new LegacyMachinePartRenderProperties(mode, color, alpha, hasColor, true);
    }

    public boolean translucent() {
        return mode != LegacyMachinePartRenderMode.CUTOUT_NO_CULL
                && mode != LegacyMachinePartRenderMode.CUTOUT_CULL;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
