package com.hbm.ntm.neutron;

import java.util.ArrayList;
import java.util.List;

public final class RBMKMenuScreenPlanner {
    public static final int STANDARD_WIDTH = 176;
    public static final int STANDARD_HEIGHT = 186;
    public static final int AUTOLOADER_HEIGHT = 182;
    public static final int LEGACY_PLAYER_INV_X = 8;
    public static final int LEGACY_PLAYER_INV_Y = 104;
    public static final int LEGACY_HOTBAR_Y = 162;
    public static final int AUTOLOADER_PLAYER_INV_Y = 100;
    public static final int AUTOLOADER_HOTBAR_Y = 158;

    private RBMKMenuScreenPlanner() {
    }

    public static ScreenContract screenContract(ScreenKind kind) {
        ScreenKind safeKind = kind == null ? ScreenKind.GENERIC : kind;
        return switch (safeKind) {
            case ROD -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_element.png", 0x404040);
            case STORAGE -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_storage.png", 0x404040);
            case HEATER -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_heater.png", 0x404040);
            case OUTGASSER -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_outgasser.png", 0x404040);
            case AUTOLOADER -> new ScreenContract(
                    safeKind,
                    "hbm:textures/gui/machine/gui_autoloader.png",
                    STANDARD_WIDTH,
                    AUTOLOADER_HEIGHT,
                    0xFFFFFF,
                    0x404040);
            case CONTROL_MANUAL -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_control.png", 0x404040);
            case CONTROL_AUTO -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_control_auto.png", 0x404040);
            case BOILER -> standard(safeKind, "hbm:textures/gui/reactors/gui_rbmk_boiler.png", 0x404040);
            case GENERIC -> new ScreenContract(safeKind, "", STANDARD_WIDTH, STANDARD_HEIGHT, 0x404040, 0x404040);
        };
    }

    public static MenuLayout menuLayout(ScreenKind kind) {
        ScreenKind safeKind = kind == null ? ScreenKind.GENERIC : kind;
        List<SlotPlan> slots = new ArrayList<>();
        TransferContract transfer = new TransferContract(TileTransferRule.NO_SHIFT_TRANSFER, -1, -1, 0, 35,
                "legacy container returns null for shift transfer");

        switch (safeKind) {
            case ROD -> {
                slots.add(new SlotPlan(SlotOwner.TILE, SlotRole.NORMAL, 0, 80, 45));
                addLegacyPlayerInventory(slots, LEGACY_PLAYER_INV_Y, LEGACY_HOTBAR_Y);
                transfer = new TransferContract(
                        TileTransferRule.TILE_TO_PLAYER_MANUAL_COLD_OR_CREATIVE,
                        0,
                        0,
                        1,
                        36,
                        "slotClick on tile slot 0 returns a copy without taking when hot and not creative");
            }
            case STORAGE -> {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 4; j++) {
                        slots.add(new SlotPlan(SlotOwner.TILE, SlotRole.NORMAL, i + j * 3, 32 + 32 * j,
                                29 + 16 * i));
                    }
                }
                addLegacyPlayerInventory(slots, LEGACY_PLAYER_INV_Y, LEGACY_HOTBAR_Y);
                transfer = new TransferContract(TileTransferRule.TILE_TO_PLAYER_AND_PLAYER_TO_TILE, 0, 11, 12, 47,
                        "player stacks merge into storage slots 0..11");
            }
            case HEATER -> {
                slots.add(new SlotPlan(SlotOwner.TILE, SlotRole.NORMAL, 0, 41, 45));
                addLegacyPlayerInventory(slots, LEGACY_PLAYER_INV_Y, LEGACY_HOTBAR_Y);
                transfer = new TransferContract(TileTransferRule.TILE_TO_PLAYER_AND_PLAYER_TO_TILE, 0, 0, 1, 36,
                        "generic single-slot heater transfer");
            }
            case OUTGASSER -> {
                slots.add(new SlotPlan(SlotOwner.TILE, SlotRole.NORMAL, 0, 48, 45));
                slots.add(new SlotPlan(SlotOwner.TILE, SlotRole.CRAFTING_OUTPUT, 1, 112, 69));
                addLegacyPlayerInventory(slots, LEGACY_PLAYER_INV_Y, LEGACY_HOTBAR_Y);
                transfer = new TransferContract(TileTransferRule.OUTGASSER_LEGACY, 0, 1, 2, 37,
                        "legacy shift-click from output/player attempts merge into input range 0..1");
            }
            case AUTOLOADER -> {
                addGrid(slots, SlotOwner.TILE, SlotRole.NORMAL, 0, 17, 18, 3, 3, 18);
                addGrid(slots, SlotOwner.TILE, SlotRole.TAKE_ONLY, 9, 107, 18, 3, 3, 18);
                addPlayerInventory(slots, LEGACY_PLAYER_INV_X, AUTOLOADER_PLAYER_INV_Y, AUTOLOADER_HOTBAR_Y);
                transfer = new TransferContract(TileTransferRule.AUTOLOADER, 0, 17, 18, 53,
                        "tile slots shift to player; player slots shift into input slots 0..8 only");
            }
            case CONTROL_MANUAL, CONTROL_AUTO, BOILER, GENERIC -> {
                addLegacyPlayerInventory(slots, LEGACY_PLAYER_INV_Y, LEGACY_HOTBAR_Y);
                transfer = new TransferContract(TileTransferRule.NO_SHIFT_TRANSFER, -1, -1, 0, 35,
                        "legacy container returns null for shift transfer");
            }
        }

        return new MenuLayout(safeKind, List.copyOf(slots), transfer);
    }

    public static RodScreenPlan rodScreenPlan(boolean hasRod, double depletion, double xenonPoisonLevel,
            boolean coldEnoughForAutoloader, boolean coldEnoughForManual) {
        List<TextureRect> rects = new ArrayList<>();
        if (hasRod) {
            rects.add(new TextureRect("depletion_background", 34, 21, 176, 0, 18, 67));
            int depletionHeight = clamp((int) (Math.max(0.0D, depletion) * 67.0D), 0, 67);
            rects.add(new TextureRect("depletion_fill", 34, 21, 194, 0, 18, depletionHeight));

            int xenonHeight = clamp((int) (Math.max(0.0D, xenonPoisonLevel) * 58.0D), 0, 58);
            rects.add(new TextureRect("xenon_fill", 126, 82 - xenonHeight, 212, 58 - xenonHeight, 14, xenonHeight));
        }

        List<WarningPanel> warnings = new ArrayList<>();
        if (!coldEnoughForAutoloader) {
            warnings.add(new WarningPanel("autoloader_hot", -16, 20, 16, 16, 6));
        }
        if (!coldEnoughForManual) {
            warnings.add(new WarningPanel("manual_hot", -16, 36, 16, 16, 7));
        }
        return new RodScreenPlan(List.copyOf(rects), List.copyOf(warnings));
    }

    public static OutgasserScreenPlan outgasserScreenPlan(int progress, int duration, int gasFill, int gasMaxFill) {
        int safeDuration = Math.max(1, duration);
        int safeGasMax = Math.max(1, gasMaxFill);
        int progressWidth = clamp(progress * 13 / safeDuration, 0, 13);
        int gasHeight = clamp(gasFill * 42 / safeGasMax, 0, 42);
        return new OutgasserScreenPlan(
                new TextureRect("progress", 82, 50, 176, 0, progressWidth, 6),
                new TextureRect("gas", 115, 66 - gasHeight, 188, 42 - gasHeight, 10, gasHeight),
                new Hitbox("gas_tooltip", 112, 21, 16, 48));
    }

    public static HeaterScreenPlan heaterScreenPlan() {
        return new HeaterScreenPlan(
                new Hitbox("feed_tank_tooltip", 68, 24, 16, 58),
                new Hitbox("steam_tank_tooltip", 126, 24, 16, 58),
                List.of(
                        new TextureRect("feed_overlay", 72, 72, 176, 0, 10, 10),
                        new TextureRect("steam_overlay", 130, 72, 186, 0, 10, 10)));
    }

    public static BoilerScreenPlan boilerScreenPlan(int feedFill, int feedMaxFill, int steamFill, int steamMaxFill,
            SteamType steamType) {
        int safeFeedMax = Math.max(1, feedMaxFill);
        int safeSteamMax = Math.max(1, steamMaxFill);
        int feedHeight = clamp(feedFill * 58 / safeFeedMax, 0, 58);
        int steamHeight = Math.max(0, steamFill) * 22 / safeSteamMax;
        if (steamHeight > 0) {
            steamHeight++;
        }
        if (steamHeight > 22) {
            steamHeight++;
        }
        steamHeight = clamp(steamHeight, 0, 24);

        SteamType safeType = steamType == null ? SteamType.NONE : steamType;
        TextureRect typeRect = safeType == SteamType.NONE
                ? TextureRect.empty("steam_type")
                : new TextureRect("steam_type", 36, 24, safeType.textureU(), 0, 14, 58);

        return new BoilerScreenPlan(
                new TextureRect("feed", 126, 82 - feedHeight, 176, 58 - feedHeight, 14, feedHeight),
                new TextureRect("steam", 91, 65 - steamHeight, 190, 24 - steamHeight, 4, steamHeight),
                typeRect,
                new Hitbox("feed_tank_tooltip", 126, 24, 16, 56),
                new Hitbox("steam_tank_tooltip", 89, 39, 8, 28),
                new ControlButton("compression", 33, 21, 20, 64, "compression", "true"));
    }

    public static ControlManualScreenPlan controlManualScreenPlan(double level, RBMKControlRodPlanner.RBMKColor color,
            boolean powered, boolean hasPower) {
        int levelHeight = controlLevelHeight(level);
        List<ControlButton> buttons = new ArrayList<>();
        for (int k = 0; k < 5; k++) {
            buttons.add(new ControlButton("level_" + k, 118, 26 + k * 11, 30, 10, "level",
                    Double.toString(1.0D - k * 0.25D)));
            buttons.add(new ControlButton("color_" + k, 28, 26 + k * 11, 12, 10, "color", Integer.toString(k)));
        }

        TextureRect colorRect = color == null
                ? TextureRect.empty("selected_color")
                : new TextureRect("selected_color", 28, 26 + color.ordinal() * 11, 184, color.ordinal() * 10, 12, 10);
        TextureRect powerRect = powered
                ? new TextureRect("power", 87, 21, 196, hasPower ? 16 : 0, 16, 16)
                : TextureRect.empty("power");

        return new ControlManualScreenPlan(
                new TextureRect("level", 75, 29, 176, 56 - levelHeight, 8, levelHeight),
                colorRect,
                powerRect,
                List.copyOf(buttons),
                new Hitbox("level_tooltip", 71, 29, 16, 56),
                new Hitbox("power_tooltip", 87, 21, 16, 16));
    }

    public static ControlAutoScreenPlan controlAutoScreenPlan(double level, RBMKControlRodPlanner.RBMKFunction function,
            boolean powered, boolean hasPower) {
        int levelHeight = controlLevelHeight(level);
        RBMKControlRodPlanner.RBMKFunction safeFunction =
                function == null ? RBMKControlRodPlanner.RBMKFunction.LINEAR : function;

        List<TextFieldContract> fields = new ArrayList<>();
        fields.add(new TextFieldContract("levelUpper", 30, 27, 26, 6, 3, 100));
        fields.add(new TextFieldContract("levelLower", 30, 38, 26, 6, 3, 100));
        fields.add(new TextFieldContract("heatUpper", 30, 49, 26, 6, 4, 9999));
        fields.add(new TextFieldContract("heatLower", 30, 60, 26, 6, 4, 9999));

        List<ControlButton> buttons = new ArrayList<>();
        buttons.add(new ControlButton("save", 28, 70, 30, 10, "saveParameters", "true"));
        for (int k = 0; k < 3; k++) {
            buttons.add(new ControlButton("function_" + k, 61, 48 + k * 11, 22, 10, "function",
                    Integer.toString(k)));
        }

        TextureRect powerRect = powered
                ? new TextureRect("power", 136, 21, 210, hasPower ? 16 : 0, 16, 16)
                : TextureRect.empty("power");

        return new ControlAutoScreenPlan(
                new TextureRect("level", 124, 29, 176, 56 - levelHeight, 8, levelHeight),
                new TextureRect("function", 59, 27, 184, safeFunction.ordinal() * 19, 26, 19),
                powerRect,
                List.copyOf(fields),
                List.copyOf(buttons),
                new Hitbox("level_tooltip", 124, 29, 16, 56),
                new Hitbox("power_tooltip", 136, 21, 16, 16));
    }

    public static AutoloaderScreenPlan autoloaderScreenPlan(int cycle) {
        return new AutoloaderScreenPlan(
                Integer.toString(clamp(cycle, RBMKAutoloaderPlanner.MIN_CYCLE, RBMKAutoloaderPlanner.MAX_CYCLE)) + "%",
                23,
                0x00FF00,
                List.of(
                        new ControlButton("minus", 74, 36, 12, 12, "minus", "true"),
                        new ControlButton("plus", 90, 36, 12, 12, "plus", "true")));
    }

    private static ScreenContract standard(ScreenKind kind, String texture, int titleColor) {
        return new ScreenContract(kind, texture, STANDARD_WIDTH, STANDARD_HEIGHT, titleColor, 0x404040);
    }

    private static void addLegacyPlayerInventory(List<SlotPlan> slots, int inventoryY, int hotbarY) {
        addPlayerInventory(slots, LEGACY_PLAYER_INV_X, inventoryY, hotbarY);
    }

    private static void addPlayerInventory(List<SlotPlan> slots, int x, int inventoryY, int hotbarY) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                slots.add(new SlotPlan(SlotOwner.PLAYER_MAIN, SlotRole.NORMAL, j + i * 9 + 9, x + j * 18,
                        inventoryY + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            slots.add(new SlotPlan(SlotOwner.PLAYER_HOTBAR, SlotRole.NORMAL, i, x + i * 18, hotbarY));
        }
    }

    private static void addGrid(List<SlotPlan> slots, SlotOwner owner, SlotRole role, int from, int x, int y,
            int rows, int cols, int slotSize) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                slots.add(new SlotPlan(owner, role, col + row * cols + from, x + col * slotSize,
                        y + row * slotSize));
            }
        }
    }

    private static int controlLevelHeight(double level) {
        return clamp((int) (56.0D * (1.0D - level)), 0, 56);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public enum ScreenKind {
        ROD,
        STORAGE,
        HEATER,
        OUTGASSER,
        AUTOLOADER,
        CONTROL_MANUAL,
        CONTROL_AUTO,
        BOILER,
        GENERIC
    }

    public enum SlotOwner {
        TILE,
        PLAYER_MAIN,
        PLAYER_HOTBAR
    }

    public enum SlotRole {
        NORMAL,
        TAKE_ONLY,
        CRAFTING_OUTPUT
    }

    public enum TileTransferRule {
        TILE_TO_PLAYER_AND_PLAYER_TO_TILE,
        TILE_TO_PLAYER_MANUAL_COLD_OR_CREATIVE,
        OUTGASSER_LEGACY,
        AUTOLOADER,
        NO_SHIFT_TRANSFER
    }

    public enum SteamType {
        NONE(0),
        STEAM(194),
        HOTSTEAM(208),
        SUPERHOTSTEAM(222),
        ULTRAHOTSTEAM(236);

        private final int textureU;

        SteamType(int textureU) {
            this.textureU = textureU;
        }

        public int textureU() {
            return textureU;
        }
    }

    public record ScreenContract(
            ScreenKind kind,
            String texture,
            int width,
            int height,
            int titleColor,
            int inventoryLabelColor) {
    }

    public record MenuLayout(ScreenKind kind, List<SlotPlan> slots, TransferContract transfer) {
    }

    public record SlotPlan(SlotOwner owner, SlotRole role, int slot, int x, int y) {
    }

    public record TransferContract(
            TileTransferRule rule,
            int tileSlotStart,
            int tileSlotEnd,
            int playerSlotStartInContainer,
            int playerSlotEndInContainer,
            String note) {
    }

    public record TextureRect(String name, int x, int y, int u, int v, int width, int height) {
        public static TextureRect empty(String name) {
            return new TextureRect(name, 0, 0, 0, 0, 0, 0);
        }
    }

    public record Hitbox(String name, int x, int y, int width, int height) {
    }

    public record ControlButton(String name, int x, int y, int width, int height, String packetKey, String packetValue) {
    }

    public record WarningPanel(String name, int x, int y, int width, int height, int infoPanelIndex) {
    }

    public record TextFieldContract(
            String packetKey,
            int x,
            int y,
            int width,
            int height,
            int maxStringLength,
            int clampMax) {
    }

    public record RodScreenPlan(List<TextureRect> rects, List<WarningPanel> warnings) {
    }

    public record OutgasserScreenPlan(TextureRect progress, TextureRect gas, Hitbox gasTooltip) {
    }

    public record HeaterScreenPlan(Hitbox feedTankTooltip, Hitbox steamTankTooltip, List<TextureRect> overlays) {
    }

    public record BoilerScreenPlan(
            TextureRect feed,
            TextureRect steam,
            TextureRect steamType,
            Hitbox feedTankTooltip,
            Hitbox steamTankTooltip,
            ControlButton compressionButton) {
    }

    public record ControlManualScreenPlan(
            TextureRect level,
            TextureRect selectedColor,
            TextureRect powerIcon,
            List<ControlButton> buttons,
            Hitbox levelTooltip,
            Hitbox powerTooltip) {
    }

    public record ControlAutoScreenPlan(
            TextureRect level,
            TextureRect functionSelection,
            TextureRect powerIcon,
            List<TextFieldContract> fields,
            List<ControlButton> buttons,
            Hitbox levelTooltip,
            Hitbox powerTooltip) {
    }

    public record AutoloaderScreenPlan(String cycleText, int cycleTextY, int cycleTextColor,
            List<ControlButton> buttons) {
    }
}
