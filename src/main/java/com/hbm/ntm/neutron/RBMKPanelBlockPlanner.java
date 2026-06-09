package com.hbm.ntm.neutron;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public final class RBMKPanelBlockPlanner {
    public static final float LEGACY_HARDNESS = 3.0F;
    public static final float LEGACY_RESISTANCE = 30.0F;
    public static final int GUI_ID = 0;
    public static final double PANEL_THICKNESS_INSET = 0.25D;
    public static final double INVENTORY_PANEL_MIN_X = 0.25D;
    public static final double CONSOLE_GUIDE_BOOK_SIZE = 0.1875D;
    public static final String CONSOLE_GUIDE_BOOK_ID = "book_guide:RBMK";

    private RBMKPanelBlockPlanner() {
    }

    public static int miniPanelMetaFromYawQuadrant(int quadrant) {
        return switch (Math.floorMod(quadrant, 4)) {
            case 0 -> Direction.NORTH.ordinal();
            case 1 -> Direction.EAST.ordinal();
            case 2 -> Direction.SOUTH.ordinal();
            default -> Direction.WEST.ordinal();
        };
    }

    public static AABB miniPanelBounds(int meta) {
        if (meta == Direction.EAST.ordinal()) {
            return new AABB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D);
        }
        if (meta == Direction.SOUTH.ordinal()) {
            return new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D);
        }
        if (meta == Direction.WEST.ordinal()) {
            return new AABB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        }
        if (meta == Direction.NORTH.ordinal()) {
            return new AABB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D);
        }
        return new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    public static AABB miniPanelInventoryBounds() {
        return new AABB(INVENTORY_PANEL_MIN_X, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    public static PanelActivationPlan planConfigPanelScrewdriver(boolean screwdriver, boolean remoteLevel) {
        if (!screwdriver) {
            return PanelActivationPlan.reject();
        }
        return new PanelActivationPlan(true, remoteLevel, GUI_ID, PanelBlockAction.OPEN_CONFIG_GUI);
    }

    public static PanelActivationPlan planDisplayScrewdriver(boolean screwdriver, boolean remoteLevel) {
        if (!screwdriver) {
            return PanelActivationPlan.reject();
        }
        return new PanelActivationPlan(true, !remoteLevel, GUI_ID, PanelBlockAction.ROTATE_DISPLAY);
    }

    public static PanelActivationPlan planTerminalActivation(boolean sneaking, boolean remoteLevel) {
        if (sneaking) {
            return PanelActivationPlan.reject();
        }
        return new PanelActivationPlan(true, remoteLevel, GUI_ID, PanelBlockAction.OPEN_TERMINAL_GUI);
    }

    public static KeypadHitPlan planKeypadHit(int meta, int sideOrdinal, double hitX, double hitY, double hitZ,
            boolean sneaking) {
        if (sneaking || isEdgeOrVertical(sideOrdinal, hitX, hitZ)) {
            return new KeypadHitPlan(false, -1);
        }
        int index = horizontalIndex(meta, hitX, hitZ);
        if (hitY < 0.5D) {
            index += 2;
        }
        return new KeypadHitPlan(true, index);
    }

    public static LeverHitPlan planLeverHit(int meta, int sideOrdinal, double hitX, double hitZ, boolean sneaking) {
        if (sneaking || isEdgeOrVertical(sideOrdinal, hitX, hitZ)) {
            return new LeverHitPlan(false, -1);
        }
        return new LeverHitPlan(true, horizontalIndex(meta, hitX, hitZ));
    }

    public static ConsoleStructurePlan consoleStructure(BlockPos core) {
        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        return new ConsoleStructurePlan(
                safeCore,
                new RBMKBlockPlanner.LegacyDimensions(3, 0, 0, 0, 2, 2),
                new RBMKBlockPlanner.LegacyDimensions(0, 0, 0, 1, 2, 2),
                1);
    }

    public static ConsoleStructurePlan craneConsoleStructure(BlockPos core) {
        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        return new ConsoleStructurePlan(
                safeCore,
                new RBMKBlockPlanner.LegacyDimensions(1, 0, 0, 0, 1, 1),
                new RBMKBlockPlanner.LegacyDimensions(0, 0, 0, 1, 1, 1),
                1);
    }

    public static ConsoleGuideBookPlan planConsoleGuideBookClick(
            BlockPos core,
            BlockPos clicked,
            int coreMeta,
            int sideOrdinal,
            double hitX,
            double hitZ,
            boolean playerHasGuideBook) {
        if (sideOrdinal != Direction.UP.ordinal() || playerHasGuideBook) {
            return new ConsoleGuideBookPlan(false, CONSOLE_GUIDE_BOOK_ID, BlockPos.ZERO);
        }

        BlockPos safeCore = core == null ? BlockPos.ZERO : core;
        BlockPos safeClicked = clicked == null ? safeCore : clicked;
        ConsoleGuideOffset offset = consoleGuideOffset(coreMeta - RBMKBlockPlanner.CORE_METADATA_OFFSET);
        double hitWorldX = safeClicked.getX() + hitX;
        double hitWorldZ = safeClicked.getZ() + hitZ;
        double targetX = safeCore.getX() + 0.5D + offset.x();
        double targetZ = safeCore.getZ() + 0.5D + offset.z();
        boolean grant = Math.abs(hitWorldX - targetX) < CONSOLE_GUIDE_BOOK_SIZE
                && Math.abs(hitWorldZ - targetZ) < CONSOLE_GUIDE_BOOK_SIZE;
        return new ConsoleGuideBookPlan(grant, CONSOLE_GUIDE_BOOK_ID, safeCore);
    }

    public static PanelActivationPlan planConsoleScrewdriver(boolean screwdriver, boolean remoteLevel) {
        if (!screwdriver) {
            return PanelActivationPlan.reject();
        }
        return new PanelActivationPlan(true, !remoteLevel, GUI_ID, PanelBlockAction.ROTATE_CONSOLE);
    }

    public static PanelActivationPlan planCraneConsoleScrewdriver(boolean screwdriver, boolean remoteLevel) {
        if (!screwdriver) {
            return PanelActivationPlan.reject();
        }
        return new PanelActivationPlan(true, !remoteLevel, GUI_ID, PanelBlockAction.CYCLE_CRANE_ROTATION);
    }

    public static AABB craneConsoleBounds(int meta) {
        if (meta == Direction.UP.ordinal()) {
            return new AABB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
        }
        return new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    public static List<PanelBlockType> guiScrewdriverPanels() {
        return List.of(
                PanelBlockType.GAUGE,
                PanelBlockType.GRAPH,
                PanelBlockType.INDICATOR,
                PanelBlockType.KEYPAD,
                PanelBlockType.LEVER,
                PanelBlockType.NUMITRON);
    }

    private static boolean isEdgeOrVertical(int sideOrdinal, double hitX, double hitZ) {
        return hitX == 0.0D || hitX == 1.0D || hitZ == 0.0D || hitZ == 1.0D
                || sideOrdinal == Direction.DOWN.ordinal() || sideOrdinal == Direction.UP.ordinal();
    }

    private static int horizontalIndex(int meta, double hitX, double hitZ) {
        int index = 0;
        if (meta == Direction.NORTH.ordinal() && hitX < 0.5D) {
            index = 1;
        }
        if (meta == Direction.SOUTH.ordinal() && hitX > 0.5D) {
            index = 1;
        }
        if (meta == Direction.WEST.ordinal() && hitZ > 0.5D) {
            index = 1;
        }
        if (meta == Direction.EAST.ordinal() && hitZ < 0.5D) {
            index = 1;
        }
        return index;
    }

    private static ConsoleGuideOffset consoleGuideOffset(int legacyDirectionOrdinal) {
        double x = 1.375D;
        double z = 0.75D;
        return switch (legacyDirectionOrdinal) {
            case 2 -> new ConsoleGuideOffset(z, -x);
            case 3 -> new ConsoleGuideOffset(-z, x);
            case 4 -> new ConsoleGuideOffset(-x, -z);
            default -> new ConsoleGuideOffset(x, z);
        };
    }

    public enum PanelBlockType {
        DISPLAY,
        GAUGE,
        GRAPH,
        INDICATOR,
        KEYPAD,
        LEVER,
        NUMITRON,
        TERMINAL,
        CONSOLE,
        CRANE_CONSOLE
    }

    public enum PanelBlockAction {
        NONE,
        OPEN_CONFIG_GUI,
        OPEN_TERMINAL_GUI,
        ROTATE_DISPLAY,
        ROTATE_CONSOLE,
        CYCLE_CRANE_ROTATION
    }

    public record PanelActivationPlan(boolean handled, boolean performAction, int guiId, PanelBlockAction action) {
        private static PanelActivationPlan reject() {
            return new PanelActivationPlan(false, false, GUI_ID, PanelBlockAction.NONE);
        }
    }

    public record KeypadHitPlan(boolean hitButton, int keyIndex) {
    }

    public record LeverHitPlan(boolean hitLever, int leverIndex) {
    }

    public record ConsoleStructurePlan(
            BlockPos core,
            RBMKBlockPlanner.LegacyDimensions mainDimensions,
            RBMKBlockPlanner.LegacyDimensions footprintDimensions,
            int placementOffset) {
    }

    public record ConsoleGuideOffset(double x, double z) {
    }

    public record ConsoleGuideBookPlan(boolean grantGuideBook, String guideBookId, BlockPos core) {
    }
}
