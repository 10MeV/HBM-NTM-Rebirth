package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyRenderColor;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;

public final class LegacyRbmkDisplayRenderer {
    public static final int GRID_COLUMNS = 7;
    public static final double DISPLAY_SCALE = 8.0D / 7.0D;
    public static final double COLUMN_X = 0.28125D;
    public static final double COLUMN_Y_START = 0.875D;
    public static final double COLUMN_Z_CENTER = 0.125D * 3.0D;
    public static final double COLUMN_STEP = 0.125D;
    public static final double COLUMN_WIDTH = 0.0625D * 0.75D;
    public static final double DOT_X_OFFSET = 0.01D;
    public static final double DOT_WIDTH = 0.03125D;
    public static final double DOT_EDGE = 0.022097D;

    public static void renderDisplay(ObjRenderContext context, RBMKConsolePlanner.ColumnSnapshot[] columns) {
        if (columns == null || columns.length == 0) {
            return;
        }
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.scale(1.0F, (float) DISPLAY_SCALE, (float) DISPLAY_SCALE);
        poseStack.translate(0.0D, -0.5D, 0.0D);

        ObjRenderContext displayContext = context.fullBright().withoutTranslucency();
        for (int i = 0; i < columns.length; i++) {
            RBMKConsolePlanner.ColumnSnapshot column = columns[i];
            if (column == null) {
                continue;
            }
            renderColumn(displayContext, i, column);
        }
        poseStack.popPose();
    }

    public static void renderColumn(ObjRenderContext context, int index, RBMKConsolePlanner.ColumnSnapshot column) {
        if (column == null) {
            return;
        }
        double x = columnX();
        double y = columnY(index);
        double z = columnZ(index);
        CompoundTag data = column.data() == null ? new CompoundTag() : column.data();
        int baseColor = baseColor(index, data);
        LegacyUntexturedQuadRenderer.xPlaneCenteredRect(context, x, y, z, COLUMN_WIDTH, COLUMN_WIDTH, baseColor, 255);

        RBMKConsolePlanner.ColumnType type = column.type() == null ? RBMKConsolePlanner.ColumnType.BLANK : column.type();
        switch (type) {
            case FUEL, FUEL_SIM -> renderFuelDot(context, x + DOT_X_OFFSET, y, z, data.getDouble("enrichment"));
            case CONTROL -> renderControlDot(context, x + DOT_X_OFFSET, y, z, data.getDouble("level"));
            case CONTROL_AUTO -> renderControlAutoDot(context, x + DOT_X_OFFSET, y, z, data.getDouble("level"));
            default -> {
            }
        }
    }

    public static int baseColor(int index, CompoundTag data) {
        if (data != null && data.getByte("indicator") > 0) {
            return 0xFFFF00;
        }
        if (data != null && data.contains("color") && data.getByte("color") >= 0) {
            return manualColor(data.getByte("color"));
        }
        double heat = data == null ? 0.0D : data.getDouble("heat");
        double maxHeat = data == null ? 0.0D : data.getDouble("maxHeat");
        double ratio = maxHeat <= 0.0D ? 0.0D : heat / maxHeat;
        ratio = Math.max(0.0D, Math.min(1.0D, ratio));
        double base = 0.65D + (index % 2) * 0.05D;
        return LegacyRenderColor.color((float) (base + ((1.0D - base) * ratio)), (float) base, (float) base);
    }

    public static int manualColor(int color) {
        return switch (color) {
            case 0 -> 0xFF0000;
            case 1 -> 0xFFFF00;
            case 2 -> 0x008000;
            case 3 -> 0x0000FF;
            case 4 -> 0x8000FF;
            default -> 0xFFFFFF;
        };
    }

    public static void renderFuelDot(ObjRenderContext context, double x, double y, double z, double enrichment) {
        renderDot(context, x, y, z, LegacyRenderColor.color(0.0F, 0.25F + (float) (enrichment * 0.75D), 0.0F));
    }

    public static void renderControlDot(ObjRenderContext context, double x, double y, double z, double level) {
        renderDot(context, x, y, z, LegacyRenderColor.color((float) level, (float) level, 0.0F));
    }

    public static void renderControlAutoDot(ObjRenderContext context, double x, double y, double z, double level) {
        renderDot(context, x, y, z, LegacyRenderColor.color((float) level, 0.0F, (float) level));
    }

    public static void renderDot(ObjRenderContext context, double x, double y, double z, int color) {
        LegacyUntexturedQuadRenderer.xPlaneDot(context, x, y, z, DOT_WIDTH, DOT_EDGE, color, 255);
    }

    public static double columnX() {
        return COLUMN_X;
    }

    public static double columnY(int index) {
        return -(index / GRID_COLUMNS) * COLUMN_STEP + COLUMN_Y_START;
    }

    public static double columnZ(int index) {
        return -(index % GRID_COLUMNS) * COLUMN_STEP + COLUMN_Z_CENTER;
    }

    private LegacyRbmkDisplayRenderer() {
    }
}
