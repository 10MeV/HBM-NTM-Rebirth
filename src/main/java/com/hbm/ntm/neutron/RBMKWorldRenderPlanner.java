package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKWorldRenderPlanner {
    public static final int DUMMY_CORE_METADATA_OFFSET = 10;
    public static final int FULL_BRIGHT = 240;

    public static final double BASE_X_OFFSET = 0.5D;
    public static final double BASE_Z_OFFSET = 0.5D;

    public static final String AUTOLOADER_MODEL = "rbmk_autoloader";
    public static final String AUTOLOADER_TEXTURE = "rbmk_autoloader_tex";
    public static final double AUTOLOADER_PISTON_Y = 4.0D;
    public static final double AUTOLOADER_PISTON_TRAVEL = -4.0D;
    public static final double AUTOLOADER_ITEM_INVENTORY_Y = -6.0D;
    public static final double AUTOLOADER_ITEM_INVENTORY_SCALE = 1.75D;
    public static final double AUTOLOADER_ITEM_YAW = 180.0D;

    public static final String CONSOLE_MODEL = "rbmk_console";
    public static final String CONSOLE_TEXTURE = "rbmk_console_tex";
    public static final double CONSOLE_LOCAL_X = 0.5D;
    public static final int CONSOLE_GRID_SIZE = 15;
    public static final double CONSOLE_COLUMN_X = -0.3725D;
    public static final double CONSOLE_COLUMN_Y_START = 3.625D;
    public static final double CONSOLE_COLUMN_Z_HALF = 0.125D * 7.0D;
    public static final double CONSOLE_COLUMN_STEP = -0.125D;
    public static final double CONSOLE_SCREEN_BASE_X = -0.42D;
    public static final double CONSOLE_SCREEN_BASE_Y = 3.5D;
    public static final double CONSOLE_SCREEN_BASE_Z = 1.75D;
    public static final double CONSOLE_SCREEN_ODD_Z_OFFSET = -3.5D;
    public static final double CONSOLE_SCREEN_ROW_Y_OFFSET = -0.75D;
    public static final float CONSOLE_SCREEN_MAX_TEXT_SCALE = 0.03F;
    public static final float CONSOLE_SCREEN_MAX_TEXT_WIDTH = 0.8F;
    public static final int CONSOLE_SCREEN_TEXT_COLOR = 0x00ff00;

    public static final String CONTROL_ROD_MODEL = "rbmk_rods_vbo";
    public static final String CONTROL_ROD_PART = "Lid";
    public static final String CONTROL_ROD_AUTO_TEXTURE = "textures/block/legacy_blocks/rbmk_control_auto.png";
    public static final String CONTROL_ROD_STANDARD_TEXTURE = "textures/block/legacy_blocks/rbmk_control.png";
    public static final String[] CONTROL_ROD_COLOR_TEXTURES = {
            "textures/block/legacy_blocks/rbmk_control_red.png",
            "textures/block/legacy_blocks/rbmk_control_yellow.png",
            "textures/block/legacy_blocks/rbmk_control_green.png",
            "textures/block/legacy_blocks/rbmk_control_blue.png",
            "textures/block/legacy_blocks/rbmk_control_purple.png"
    };

    public static final String FUEL_ROD_MODEL = "rbmk_element_rods_vbo";
    public static final String FUEL_ROD_PART = "Rods";
    public static final String FUEL_ROD_TEXTURE = "textures/block/legacy_blocks/rbmk_element_fuel.png";
    public static final double CHERENKOV_Y_OFFSET = 0.75D;
    public static final double CHERENKOV_STEP = 0.25D;
    public static final int CHERENKOV_FLUX_THRESHOLD = 5;
    public static final float CHERENKOV_RED = 0.4F;
    public static final float CHERENKOV_GREEN = 0.9F;
    public static final float CHERENKOV_BLUE = 1.0F;
    public static final float CHERENKOV_ALPHA = 0.1F;

    public static final double COLUMN_WIDTH = 0.0625D * 0.75D;
    public static final double DOT_WIDTH = 0.03125D;
    public static final double DOT_EDGE = 0.022097D;
    public static final int INDICATOR_COLOR = 0xffff00;

    private RBMKWorldRenderPlanner() {
    }

    public static BaseTransform baseTransform(int legacyMetadata) {
        return new BaseTransform(BASE_X_OFFSET, 0.0D, BASE_Z_OFFSET, yawForMetadata(legacyMetadata), true, true);
    }

    public static BaseTransform consoleBaseTransform(int legacyMetadataWithDummyOffset) {
        return new BaseTransform(BASE_X_OFFSET, 0.0D, BASE_Z_OFFSET,
                yawForMetadata(legacyMetadataWithDummyOffset - DUMMY_CORE_METADATA_OFFSET), true, true);
    }

    public static AutoloaderRenderPlan autoloaderRenderPlan(double lastPiston, double renderPiston,
            float partialTick) {
        double piston = lastPiston + (renderPiston - lastPiston) * partialTick;
        double pistonY = AUTOLOADER_PISTON_Y + piston * AUTOLOADER_PISTON_TRAVEL;
        return new AutoloaderRenderPlan(
                AUTOLOADER_MODEL,
                AUTOLOADER_TEXTURE,
                List.of("Base", "Piston"),
                piston,
                pistonY,
                true);
    }

    public static AutoloaderItemRenderPlan autoloaderItemRenderPlan() {
        return new AutoloaderItemRenderPlan(
                AUTOLOADER_ITEM_INVENTORY_Y,
                AUTOLOADER_ITEM_INVENTORY_SCALE,
                AUTOLOADER_ITEM_YAW,
                false,
                true);
    }

    public static int controlRodRenderOffset(boolean[] sameBlockAbove) {
        int offset = 1;
        for (int o = 1; o < 16; o++) {
            if (sameBlockAbove != null && o - 1 < sameBlockAbove.length && sameBlockAbove[o - 1]) {
                offset = o;
            } else {
                break;
            }
        }
        return offset;
    }

    public static ControlRodRenderPlan controlRodRenderPlan(boolean manual, RBMKControlRodPlanner.RBMKColor color,
            double lastLevel, double level, float partialTick, boolean[] sameBlockAbove) {
        int offset = controlRodRenderOffset(sameBlockAbove);
        double interpolatedLevel = lastLevel + (level - lastLevel) * partialTick;
        return new ControlRodRenderPlan(
                CONTROL_ROD_MODEL,
                CONTROL_ROD_PART,
                controlRodTexture(manual, color),
                offset,
                offset + interpolatedLevel,
                true,
                true);
    }

    public static int fuelChannelRenderOffset(boolean[] sameBlockAbove, int[] metadataAbove) {
        int offset = 1;
        for (int o = 1; o < 16; o++) {
            if (sameBlockAbove == null || o - 1 >= sameBlockAbove.length || !sameBlockAbove[o - 1]) {
                break;
            }
            offset = o;
            int meta = metadataAbove != null && o - 1 < metadataAbove.length ? metadataAbove[o - 1] : 0;
            if (meta > 5 && meta < 12) {
                break;
            }
        }
        return offset;
    }

    public static FuelChannelRenderPlan fuelChannelRenderPlan(boolean hasRod, int fluxQuantity, int rodColor,
            boolean[] sameBlockAbove, int[] metadataAbove) {
        int offset = fuelChannelRenderOffset(sameBlockAbove, metadataAbove);
        List<RodStackPart> rods = new ArrayList<>();
        if (hasRod) {
            for (int j = 0; j <= offset; j++) {
                rods.add(new RodStackPart(j, j));
            }
        }
        boolean cherenkov = fluxQuantity > CHERENKOV_FLUX_THRESHOLD;
        List<CherenkovQuad> quads = new ArrayList<>();
        if (cherenkov) {
            int index = 0;
            for (double y = 0.0D; y <= offset + 1.0E-9D; y += CHERENKOV_STEP) {
                quads.add(new CherenkovQuad(index++, y + CHERENKOV_Y_OFFSET,
                        CHERENKOV_RED, CHERENKOV_GREEN, CHERENKOV_BLUE, CHERENKOV_ALPHA));
            }
        }
        return new FuelChannelRenderPlan(
                FUEL_ROD_MODEL,
                FUEL_ROD_PART,
                FUEL_ROD_TEXTURE,
                offset,
                rgb(rodColor),
                List.copyOf(rods),
                cherenkov,
                List.copyOf(quads));
    }

    public static ConsoleRenderPlan consoleRenderPlan(List<ConsoleColumnInput> columns, List<String> screenDisplays,
            List<Integer> screenTextWidths) {
        return new ConsoleRenderPlan(
                CONSOLE_MODEL,
                CONSOLE_TEXTURE,
                CONSOLE_LOCAL_X,
                consoleColumnPlans(columns),
                consoleScreenPlans(screenDisplays, screenTextWidths));
    }

    public static List<ConsoleColumnPlan> consoleColumnPlans(List<ConsoleColumnInput> columns) {
        List<ConsoleColumnPlan> plans = new ArrayList<>();
        int count = columns == null ? 0 : Math.min(columns.size(), CONSOLE_GRID_SIZE * CONSOLE_GRID_SIZE);
        for (int i = 0; i < count; i++) {
            ConsoleColumnInput column = columns.get(i);
            if (column == null || column.type() == null) {
                continue;
            }
            double y = -(i / CONSOLE_GRID_SIZE) * 0.125D + CONSOLE_COLUMN_Y_START;
            double z = -(i % CONSOLE_GRID_SIZE) * 0.125D + CONSOLE_COLUMN_Z_HALF;
            int baseColor = column.indicator() > 0 ? INDICATOR_COLOR : columnBaseColor(i, column);
            DotPlan dot = columnDot(column);
            plans.add(new ConsoleColumnPlan(i, CONSOLE_COLUMN_X, y, z, baseColor, dot));
        }
        return List.copyOf(plans);
    }

    public static List<ConsoleScreenTextPlan> consoleScreenPlans(List<String> displays, List<Integer> textWidths) {
        List<ConsoleScreenTextPlan> plans = new ArrayList<>();
        int count = Math.max(RBMKConsolePlanner.CONSOLE_SCREEN_COUNT, displays == null ? 0 : displays.size());
        count = Math.min(count, RBMKConsolePlanner.CONSOLE_SCREEN_COUNT);
        for (int i = 0; i < count; i++) {
            String raw = displays != null && i < displays.size() ? safeString(displays.get(i)) : "";
            ResolvedDisplayText text = displayText(raw);
            int width = textWidths != null && i < textWidths.size() && textWidths.get(i) != null
                    ? Math.max(0, textWidths.get(i))
                    : -1;
            float scale = width < 0
                    ? CONSOLE_SCREEN_MAX_TEXT_SCALE
                    : Math.min(CONSOLE_SCREEN_MAX_TEXT_SCALE, CONSOLE_SCREEN_MAX_TEXT_WIDTH / Math.max(width, 1));
            double z = CONSOLE_SCREEN_BASE_Z + (i % 2 == 1 ? CONSOLE_SCREEN_ODD_Z_OFFSET : 0.0D);
            double y = CONSOLE_SCREEN_BASE_Y + CONSOLE_SCREEN_ROW_Y_OFFSET * (i / 2);
            plans.add(new ConsoleScreenTextPlan(i, text.text(), text.translationKey(), text.translationFallback(),
                    CONSOLE_SCREEN_BASE_X, y, z, width, scale, CONSOLE_SCREEN_TEXT_COLOR, !text.text().isEmpty()));
        }
        return List.copyOf(plans);
    }

    public static ResolvedDisplayText displayText(String raw) {
        String safe = safeString(raw);
        if (safe.isEmpty()) {
            return new ResolvedDisplayText("", "", "");
        }
        String[] parts = safe.split("=", 2);
        if (parts.length == 2) {
            return new ResolvedDisplayText(safe, parts[0], parts[1]);
        }
        return new ResolvedDisplayText(safe, "", "");
    }

    public static String controlRodTexture(boolean manual, RBMKControlRodPlanner.RBMKColor color) {
        if (!manual) {
            return CONTROL_ROD_AUTO_TEXTURE;
        }
        if (color == null) {
            return CONTROL_ROD_STANDARD_TEXTURE;
        }
        return CONTROL_ROD_COLOR_TEXTURES[color.ordinal()];
    }

    private static int columnBaseColor(int index, ConsoleColumnInput column) {
        if (column.color() >= 0) {
            return switch (column.color()) {
                case 0 -> 0xff0000;
                case 1 -> 0xffff00;
                case 2 -> 0x008000;
                case 3 -> 0x0000ff;
                case 4 -> 0x8000ff;
                default -> 0xffffff;
            };
        }
        double heat = column.maxHeat() == 0.0D ? 0.0D : column.heat() / column.maxHeat();
        double base = 0.65D + (index % 2) * 0.05D;
        int red = (int) Math.round((base + ((1.0D - base) * heat)) * 255.0D);
        int greenBlue = (int) Math.round(base * 255.0D);
        return clamp(red, 0, 255) << 16 | clamp(greenBlue, 0, 255) << 8 | clamp(greenBlue, 0, 255);
    }

    private static DotPlan columnDot(ConsoleColumnInput column) {
        return switch (column.type()) {
            case FUEL, FUEL_SIM -> new DotPlan(true, 0x00ff00,
                    0.0F, (float) (0.25D + column.enrichment() * 0.75D), 0.0F);
            case CONTROL -> new DotPlan(true, 0xffff00,
                    (float) column.level(), (float) column.level(), 0.0F);
            case CONTROL_AUTO -> new DotPlan(true, 0xff00ff,
                    (float) column.level(), 0.0F, (float) column.level());
            default -> DotPlan.none();
        };
    }

    private static int rgb(int color) {
        return color & 0xffffff;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double yawForMetadata(int metadata) {
        return switch (metadata) {
            case 2 -> 90.0D;
            case 4 -> 180.0D;
            case 3 -> 270.0D;
            case 5 -> 0.0D;
            default -> 0.0D;
        };
    }

    private static String safeString(String value) {
        return value == null ? "" : value;
    }

    public record BaseTransform(
            double translateX,
            double translateY,
            double translateZ,
            double yawDegrees,
            boolean enableCullFace,
            boolean enableLighting) {
    }

    public record AutoloaderRenderPlan(
            String model,
            String texture,
            List<String> parts,
            double interpolatedPiston,
            double pistonPartY,
            boolean smoothShade) {
    }

    public record AutoloaderItemRenderPlan(
            double inventoryTranslateY,
            double inventoryScale,
            double commonYawDegrees,
            boolean cullFaceDuringCommonRender,
            boolean smoothShade) {
    }

    public record ControlRodRenderPlan(
            String model,
            String part,
            String texture,
            int columnOffset,
            double lidWorldY,
            boolean useAboveColumnBrightness,
            boolean cullFace) {
    }

    public record FuelChannelRenderPlan(
            String model,
            String part,
            String texture,
            int columnOffset,
            int rodRgb,
            List<RodStackPart> rodParts,
            boolean cherenkov,
            List<CherenkovQuad> cherenkovQuads) {
    }

    public record RodStackPart(int partIndex, double yOffset) {
    }

    public record CherenkovQuad(int index, double y, float red, float green, float blue, float alpha) {
    }

    public record ConsoleColumnInput(
            RBMKConsolePlanner.ColumnType type,
            int color,
            int indicator,
            double heat,
            double maxHeat,
            double enrichment,
            double level) {
    }

    public record DotPlan(boolean present, int rgb, float red, float green, float blue) {
        public static DotPlan none() {
            return new DotPlan(false, 0xffffff, 1.0F, 1.0F, 1.0F);
        }
    }

    public record ConsoleColumnPlan(int index, double x, double y, double z, int baseColor, DotPlan dot) {
    }

    public record ResolvedDisplayText(String text, String translationKey, String translationFallback) {
    }

    public record ConsoleScreenTextPlan(
            int index,
            String text,
            String translationKey,
            String translationFallback,
            double x,
            double y,
            double z,
            int measuredWidth,
            float scale,
            int color,
            boolean visible) {
    }

    public record ConsoleRenderPlan(
            String model,
            String texture,
            double localX,
            List<ConsoleColumnPlan> columns,
            List<ConsoleScreenTextPlan> screens) {
    }
}
