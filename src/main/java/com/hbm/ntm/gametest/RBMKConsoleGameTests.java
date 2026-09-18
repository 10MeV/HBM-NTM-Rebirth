package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RTTYControllerState;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.block.RBMKConsoleBlock;
import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.neutron.RBMKConsolePlanner;
import com.hbm.ntm.neutron.RBMKWorldRenderPlanner;
import com.hbm.ntm.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Source-contract checks for the 1.7.10 RBMK console world overlay plan. */
@PrefixGameTestTemplate(false)
public final class RBMKConsoleGameTests {
    private RBMKConsoleGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(RBMKConsoleGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "rbmkConsole")
    public static void consoleOverlayKeepsLegacyGridDotsAndScreenLayout(GameTestHelper helper) {
        List<RBMKWorldRenderPlanner.ConsoleColumnPlan> columns = RBMKWorldRenderPlanner.consoleColumnPlans(List.of(
                new RBMKWorldRenderPlanner.ConsoleColumnInput(RBMKConsolePlanner.ColumnType.FUEL, -1, 0,
                        100.0D, 200.0D, 0.5D, 0.0D),
                new RBMKWorldRenderPlanner.ConsoleColumnInput(RBMKConsolePlanner.ColumnType.CONTROL_AUTO, 2, 1,
                        0.0D, 1.0D, 0.0D, 0.75D)));
        require(columns.size() == 2, "console must plan one overlay cell per non-null legacy column");
        RBMKWorldRenderPlanner.ConsoleColumnPlan first = columns.get(0);
        requireClose(-0.3725D, first.x(), "first console column X");
        requireClose(3.625D, first.y(), "first console column Y");
        requireClose(0.875D, first.z(), "first console column Z");
        require(first.dot().present() && first.dot().rgb() == 0x00FF00,
                "fuel columns must retain the legacy green enrichment dot");
        RBMKWorldRenderPlanner.ConsoleColumnPlan second = columns.get(1);
        require(second.baseColor() == 0xFFFF00, "indicator must override manual color with legacy yellow");
        require(second.dot().present() && second.dot().rgb() == 0xFF00FF,
                "automatic control columns must retain the legacy magenta level dot");

        List<RBMKWorldRenderPlanner.ConsoleScreenTextPlan> screens = RBMKWorldRenderPlanner.consoleScreenPlans(
                List.of("rbmk.screen.temp=12.3°C", "literal=with=equals", ""), List.of(40, 40, 0));
        require(screens.size() == RBMKConsolePlanner.CONSOLE_SCREEN_COUNT,
                "console must retain all six source screen slots");
        RBMKWorldRenderPlanner.ConsoleScreenTextPlan firstScreen = screens.get(0);
        require("rbmk.screen.temp".equals(firstScreen.translationKey())
                        && "12.3°C".equals(firstScreen.translationFallback()),
                "two-part key=value display must retain source translation arguments");
        RBMKWorldRenderPlanner.ConsoleScreenTextPlan secondScreen = screens.get(1);
        require(secondScreen.translationKey().isEmpty() && "literal=with=equals".equals(secondScreen.text()),
                "multiple equals signs must remain literal as in RenderRBMKConsole");
        requireClose(3.5D, firstScreen.y(), "first screen Y");
        requireClose(1.75D, firstScreen.z(), "first screen Z");
        requireClose(3.5D, secondScreen.y(), "second screen Y");
        requireClose(-1.75D, secondScreen.z(), "odd screen legacy Z offset");
        requireClose(2.75D, screens.get(2).y(), "next screen-pair legacy Y offset");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "rbmkConsole")
    public static void consoleLayoutUsesTheLegacyCoreOriginForBothXrBoxes(GameTestHelper helper) {
        RBMKConsoleBlock block = (RBMKConsoleBlock) ModBlocks.RBMK_CONSOLE.get();
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            LegacyMultiblockLayout layout = block.getMultiblockLayout(
                    block.defaultBlockState().setValue(RBMKConsoleBlock.FACING, facing),
                    helper.getLevel(), BlockPos.ZERO);

            require(layout.offsets().size() == 25,
                    "RBMK console must retain the source 5x2 footprint and four-block-high rear row for " + facing);
            require(layout.checkOffsets().size() == 25,
                    "RBMK console preflight and filled layout must use the same source footprint for " + facing);

            Direction lateral = facing.getClockWise();
            for (int side = -2; side <= 2; side++) {
                for (int y = 0; y <= 3; y++) {
                    require(layout.containsOffset(consoleOffset(facing, lateral, 0, side, y)),
                            "missing source rear-row cell for " + facing + " side=" + side + " y=" + y);
                }
                BlockPos frontCell = consoleOffset(facing, lateral, 1, side, 0);
                require(layout.containsOffset(frontCell),
                        "missing source front-row cell for " + facing + " side=" + side);
                require(!layout.proxyMode(frontCell).isProxy(),
                        "ordinary RBMK console front-row dummies must not become legacy proxy ports");
                require(!layout.isLegacyExtraOffset(frontCell),
                        "the second fillSpace box is not a legacy makeExtra region");
                require(!layout.containsOffset(consoleOffset(facing, lateral, 2, side, 0)),
                        "RBMK console must not gain a spurious third footprint row for " + facing);
            }
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "rbmkConsole")
    public static void terminalSupportsRorWriteSubmitAndPolling(GameTestHelper helper) {
        RBMKPanelBlockEntity terminal = new RBMKPanelBlockEntity(BlockPos.ZERO,
                ModBlocks.RBMK_TERMINAL.get().defaultBlockState());
        List<String> functions = List.of(terminal.getFunctionInfo());
        require(functions.contains(RORInfo.PREFIX_FUNCTION + "write!text"),
                "RBMK terminal must advertise the legacy RoR write function");
        require(functions.contains(RORInfo.PREFIX_FUNCTION + "submit!command"),
                "RBMK terminal must advertise the legacy RoR submit function");

        terminal.runRORFunction(RORInfo.PREFIX_FUNCTION + "set5", new String[] {"fixed", "line"});
        require("fixed line".equals(terminal.terminal().history()[4]),
                "FUN:set<line#> must write the selected terminal screen line");

        RTTYControllerState controller = new RTTYControllerState();
        controller.setChannel("rbmk-terminal-ror");
        controller.setPolling(true);
        RTTYSystem.broadcast(helper.getLevel(), controller.channel(), "write!controller:line");
        RTTYSystem.updateBroadcastQueue(helper.getLevel().getServer());
        RTTYSystem.RTTYChannel channel = RTTYSystem.listen(helper.getLevel(), controller.channel());
        require(controller.runFromChannel(terminal, channel, helper.getLevel().getGameTime()).ran(),
                "RoR controller must execute terminal write commands");
        require("controller line".equals(terminal.terminal().history()[0]),
                "controller write command must reach the terminal screen");
        require(controller.runFromChannel(terminal, channel, helper.getLevel().getGameTime()).ran(),
                "polling RoR controller must independently repeat the current terminal command");
        require("controller line".equals(terminal.terminal().history()[1]),
                "independent polling must execute the terminal command again");

        RTTYSystem.broadcast(helper.getLevel(), controller.channel(), "submit!chan:reactor-control");
        RTTYSystem.updateBroadcastQueue(helper.getLevel().getServer());
        require(controller.runFromChannel(terminal, RTTYSystem.listen(helper.getLevel(), controller.channel()),
                helper.getLevel().getGameTime()).ran(), "RoR controller must execute terminal submit commands");
        require("reactor-control".equals(terminal.terminal().channel()),
                "FUN:submit must evaluate the submitted legacy terminal command");
        RTTYSystem.clear(helper.getLevel());
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireClose(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 1.0E-6D) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }

    private static BlockPos consoleOffset(Direction facing, Direction lateral, int forward, int side, int y) {
        return new BlockPos(
                facing.getStepX() * forward + lateral.getStepX() * side,
                y,
                facing.getStepZ() * forward + lateral.getStepZ() * side);
    }
}
