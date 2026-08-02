package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.redstoneoverradio.ROR;
import com.hbm.ntm.block.Mk2PileDeviceBlock;
import com.hbm.ntm.block.Mk2PileStructureBlock;
import com.hbm.ntm.blockentity.Mk2PileCoreBlockEntity;
import com.hbm.ntm.blockentity.Mk2PileDeviceBlockEntity;
import com.hbm.ntm.blockentity.Mk2PileMemberBlockEntity;
import com.hbm.ntm.item.Mk2PileRodItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/** Source-backed contracts from the 1.7.10 dynamic MK2 Pile channel API. */
@PrefixGameTestTemplate(false)
public final class Mk2PileGameTests {
    private Mk2PileGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(Mk2PileGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "mk2Pile")
    public static void coreChannelsKeepLegacyRolesAndDeviceFacingState(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(new BlockPos(6, 5, 6));
        setRole(level, corePos, Mk2PileStructureBlock.Role.CORE);
        assertTrue(level.getBlockEntity(corePos) instanceof Mk2PileCoreBlockEntity,
                "MK2 pile core state creates the dedicated core block entity");
        Mk2PileCoreBlockEntity core = (Mk2PileCoreBlockEntity) level.getBlockEntity(corePos);
        core.configure(5, 5, 5, 2, 2, Direction.SOUTH);

        BlockPos fuelEntry = corePos.east(3);
        fillDummyLine(level, fuelEntry, Direction.SOUTH, 5);
        assertTrue(core.drillChannel(fuelEntry, Direction.SOUTH), "fuel channel drills through a five-block depth");
        assertRole(level, fuelEntry, Mk2PileStructureBlock.Role.FUEL_IN, "fuel input role");
        assertRole(level, fuelEntry.south(4), Mk2PileStructureBlock.Role.FUEL_OUT, "fuel output role");
        assertRole(level, fuelEntry.south(2), Mk2PileStructureBlock.Role.CHANNEL, "fuel interior role");

        ItemStack source = new ItemStack(ModItems.PILE_ROD.get());
        source.setDamageValue(Mk2PileRodItem.RodType.RA226BE.ordinal());
        for (int i = 0; i < 5; i++) {
            assertTrue(core.loadFuelRod(fuelEntry, source), "fuel channel accepts MK2 pile rod " + i);
        }
        assertTrue(!core.lastFuelRod(fuelEntry).isEmpty(), "loader-facing channel endpoint exposes the final rod");

        BlockPos ventEntry = corePos.west(3);
        fillDummyLine(level, ventEntry, Direction.EAST, 5);
        assertTrue(core.drillChannel(ventEntry, Direction.EAST), "vent channel drills perpendicular to pile facing");
        assertRole(level, ventEntry, Mk2PileStructureBlock.Role.AIR_IN, "vent input role");
        assertRole(level, ventEntry.east(4), Mk2PileStructureBlock.Role.AIR_OUT, "vent output role");
        assertEquals(1_000, core.fillVentilation(ventEntry, 1_250), "ventilation channel caps air at the old 1000 mB maximum");

        BlockPos controlEntry = corePos.north(3);
        fillDummyLine(level, controlEntry, Direction.UP, 5);
        assertTrue(core.drillChannel(controlEntry, Direction.UP), "vertical channel is a control channel");
        assertRole(level, controlEntry, Mk2PileStructureBlock.Role.CONTROL, "control input role");
        assertRole(level, controlEntry.above(4), Mk2PileStructureBlock.Role.CONTROL, "control endpoint remains control role");
        assertTrue(core.setControlLevel(controlEntry, 1.5D), "control channel is addressable by its entry");

        assertTrue(core.drillChannel(fuelEntry, Direction.SOUTH), "drilling an existing fuel channel removes it");
        for (int i = 0; i < 5; i++) {
            assertRole(level, fuelEntry.south(i), Mk2PileStructureBlock.Role.DUMMY,
                    "removed fuel channel restores dummy role " + i);
        }
        helper.succeed();
    }

    /** Source: BlockPile#printHook and BlockPileDevice#printHook. */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "mk2Pile")
    public static void pileLookOverlaysKeepLegacyRoleAndDeviceFields(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(new BlockPos(2, 5, 2));
        setRole(level, corePos, Mk2PileStructureBlock.Role.CORE);
        Mk2PileCoreBlockEntity core = (Mk2PileCoreBlockEntity) level.getBlockEntity(corePos);
        assertTrue(core != null, "MK2 pile overlay fixture creates a core block entity");
        LegacyLookOverlay coreOverlay = core.getLookOverlay(level, corePos);
        assertEquals("Max Temp: 0 / 800°C", coreOverlay.lines().get(0).getString(),
                "core overlay keeps legacy maximum-temperature text");

        BlockPos fuelPortPos = corePos.east();
        setRole(level, fuelPortPos, Mk2PileStructureBlock.Role.FUEL_IN);
        Mk2PileMemberBlockEntity fuelPort = (Mk2PileMemberBlockEntity) level.getBlockEntity(fuelPortPos);
        assertTrue(fuelPort != null, "MK2 pile fuel input uses a member block entity");
        LegacyLookOverlay fuelPortOverlay = fuelPort.getLookOverlay(level, fuelPortPos);
        assertEquals("Fuel Loading Port", fuelPortOverlay.lines().get(0).getString(),
                "fuel input keeps its role-local legacy overlay");
        assertTrue(fuelPort.getLookOverlay(level, fuelPortPos.above()) == null,
                "pile member overlay does not leak to a different viewed block");

        BlockPos loaderPos = corePos.south();
        level.setBlock(loaderPos, ModBlocks.PILE_DEVICE.get().defaultBlockState()
                .setValue(Mk2PileDeviceBlock.KIND, Mk2PileDeviceBlock.Kind.LOADER)
                .setValue(Mk2PileDeviceBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        Mk2PileDeviceBlockEntity loader = (Mk2PileDeviceBlockEntity) level.getBlockEntity(loaderPos);
        assertTrue(loader != null, "MK2 pile loader creates its device block entity");
        LegacyLookOverlay loaderOverlay = loader.getLookOverlay(level, loaderPos);
        assertEquals("Temp: 0 / 800°C", loaderOverlay.lines().get(0).getString(),
                "loader overlay keeps legacy channel-temperature field");

        BlockPos controlPos = corePos.north();
        level.setBlock(controlPos, ModBlocks.PILE_DEVICE.get().defaultBlockState()
                .setValue(Mk2PileDeviceBlock.KIND, Mk2PileDeviceBlock.Kind.CONTROL)
                .setValue(Mk2PileDeviceBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        Mk2PileDeviceBlockEntity control = (Mk2PileDeviceBlockEntity) level.getBlockEntity(controlPos);
        assertTrue(control != null, "MK2 pile control creates its device block entity");
        assertEquals("Extraction level: 0%", control.getLookOverlay(level, controlPos).lines().get(0).getString(),
                "control overlay keeps legacy extraction-level field");

        BlockPos ventPos = corePos.west();
        level.setBlock(ventPos, ModBlocks.PILE_DEVICE.get().defaultBlockState()
                .setValue(Mk2PileDeviceBlock.KIND, Mk2PileDeviceBlock.Kind.VENT)
                .setValue(Mk2PileDeviceBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        Mk2PileDeviceBlockEntity vent = (Mk2PileDeviceBlockEntity) level.getBlockEntity(ventPos);
        assertTrue(vent != null && vent.getLookOverlay(level, ventPos) == null,
                "vent keeps the old no-constant-overlay boundary");
        helper.succeed();
    }

    /** Source: TileEntityPileLoader ISidedInventory/IRORValueProvider and TileEntityPileControl IRORInteractive. */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "mk2Pile")
    public static void pileDevicesExposeLegacyHopperAndRorContracts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos loaderPos = helper.absolutePos(new BlockPos(2, 5, 2));
        level.setBlock(loaderPos, ModBlocks.PILE_DEVICE.get().defaultBlockState()
                .setValue(Mk2PileDeviceBlock.KIND, Mk2PileDeviceBlock.Kind.LOADER)
                .setValue(Mk2PileDeviceBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        Mk2PileDeviceBlockEntity loader = (Mk2PileDeviceBlockEntity) level.getBlockEntity(loaderPos);
        assertTrue(loader != null, "MK2 pile loader creates its device block entity");

        IItemHandler loaderHandler = loader.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP)
                .orElseThrow(() -> new AssertionError("loader exposes its one-slot Forge item handler"));
        assertTrue(!loaderHandler.insertItem(0, new ItemStack(Items.STONE), false).isEmpty(),
                "hopper handler rejects non-rod items");
        ItemStack rod = new ItemStack(ModItems.PILE_ROD.get());
        rod.setDamageValue(Mk2PileRodItem.RodType.NU.ordinal());
        assertTrue(loaderHandler.insertItem(0, rod, false).isEmpty(), "hopper handler accepts one MK2 pile rod");
        assertEquals(Mk2PileRodItem.RodType.NU.ordinal(), loader.getLoaderStack().getDamageValue(),
                "hopper insertion reaches the loader's legacy stack");
        assertTrue(loaderHandler.extractItem(0, 1, false).isEmpty(), "hopper handler keeps the legacy no-extraction rule");

        assertContains(loader.getFunctionInfo(), "VAL:meta", "loader publishes legacy rod metadata value");
        assertContains(loader.getFunctionInfo(), "VAL:temp", "loader publishes legacy channel temperature value");
        assertEquals("-1", loader.provideRORValue("VAL:meta"), "empty channel exposes legacy metadata sentinel");
        assertEquals("0", loader.provideRORValue("VAL:temp"), "empty channel exposes zero temperature");

        BlockPos controlPos = helper.absolutePos(new BlockPos(5, 5, 2));
        level.setBlock(controlPos, ModBlocks.PILE_DEVICE.get().defaultBlockState()
                .setValue(Mk2PileDeviceBlock.KIND, Mk2PileDeviceBlock.Kind.CONTROL)
                .setValue(Mk2PileDeviceBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        Mk2PileDeviceBlockEntity control = (Mk2PileDeviceBlockEntity) level.getBlockEntity(controlPos);
        assertTrue(control != null, "MK2 pile control creates its device block entity");
        assertTrue(!control.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(),
                "only the loader exposes an item capability");
        assertContains(control.getFunctionInfo(), "FUN:setrods!percent", "control publishes setrods RoR command");
        assertContains(control.getFunctionInfo(), "FUN:extendrods!percent", "control publishes extendrods RoR command");
        ROR.run(control, "setrods!50");
        assertEquals(0.5D, control.getTargetLevel(), "setrods sets an exact target percentage");
        ROR.run(control, "extendrods!25");
        assertEquals(0.75D, control.getTargetLevel(), "extendrods applies a relative target percentage");
        ROR.run(control, "extendrods!-100");
        assertEquals(0.0D, control.getTargetLevel(), "extendrods clamps the target to fully retracted");
        helper.succeed();
    }

    private static void fillDummyLine(ServerLevel level, BlockPos start, Direction direction, int length) {
        for (int index = 0; index < length; index++) {
            setRole(level, start.relative(direction, index), Mk2PileStructureBlock.Role.DUMMY);
        }
    }

    private static void setRole(ServerLevel level, BlockPos pos, Mk2PileStructureBlock.Role role) {
        BlockState state = ModBlocks.PILE_BLOCK.get().defaultBlockState().setValue(Mk2PileStructureBlock.ROLE, role);
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private static void assertRole(ServerLevel level, BlockPos pos, Mk2PileStructureBlock.Role expected, String message) {
        BlockState state = level.getBlockState(pos);
        assertTrue(state.is(ModBlocks.PILE_BLOCK.get()) && state.getValue(Mk2PileStructureBlock.ROLE) == expected, message);
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected '" + expected + "', got '" + actual + "'");
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertContains(String[] values, String expected, String message) {
        for (String value : values) {
            if (expected.equals(value)) {
                return;
            }
        }
        throw new AssertionError(message + ": missing " + expected);
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
