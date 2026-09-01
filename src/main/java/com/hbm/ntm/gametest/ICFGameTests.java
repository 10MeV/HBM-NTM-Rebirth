package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.ICFControllerBlock;
import com.hbm.ntm.blockentity.ICFControllerBlockEntity;
import com.hbm.ntm.blockentity.ICFReactorBlockEntity;
import com.hbm.ntm.blockentity.ICFStructCoreBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyNodeBlockEntity;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.item.ICFPelletItem.FuelType;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.registry.ModBlocks;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Source-backed end-to-end contracts for the 1.7.10 ICF laser/reactor chain. */
@PrefixGameTestTemplate(false)
public final class ICFGameTests {
    private ICFGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(ICFGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "icfLinkage", timeoutTicks = 100)
    public static void assembledLaserIgnitesCompleteReactor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos reactorPos = helper.absolutePos(new BlockPos(32, 12, 32));
        BlockPos controllerPos = reactorPos.north(10).above(3);
        forceLoaded(level, controllerPos.offset(-12, -8, -12), reactorPos.offset(12, 10, 12));
        clear(level, controllerPos.offset(-10, -6, -8), reactorPos.offset(10, 9, 10));

        BlockState controllerState = ModBlocks.ICF_CONTROLLER.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.setBlock(controllerPos, controllerState, Block.UPDATE_ALL);
        assertTrue(level.getBlockEntity(controllerPos) instanceof ICFControllerBlockEntity,
                "ICF controller fixture creates its block entity");
        ICFControllerBlockEntity controller = (ICFControllerBlockEntity) level.getBlockEntity(controllerPos);

        BlockPos cell = controllerPos.north();
        BlockPos emitter = cell.north();
        BlockPos capacitor = emitter.north();
        BlockPos turbo = capacitor.north();
        Set<BlockPos> coreParts = Set.of(cell, emitter, capacitor, turbo);
        level.setBlock(cell, ModBlocks.ICF_LASER_CELL.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(emitter, ModBlocks.ICF_LASER_EMITTER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(capacitor, ModBlocks.ICF_LASER_CAPACITOR.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(turbo, ModBlocks.ICF_LASER_TURBO.get().defaultBlockState(), Block.UPDATE_ALL);

        Set<BlockPos> shell = new LinkedHashSet<>();
        for (BlockPos corePart : coreParts) {
            for (Direction side : Direction.values()) {
                BlockPos shellPos = corePart.relative(side);
                if (!shellPos.equals(controllerPos) && !coreParts.contains(shellPos)) {
                    shell.add(shellPos);
                }
            }
        }
        BlockPos port = cell.east();
        for (BlockPos shellPos : shell) {
            level.setBlock(shellPos, shellPos.equals(port)
                    ? ModBlocks.ICF_LASER_PORT.get().defaultBlockState()
                    : ModBlocks.ICF_LASER_CASING.get().defaultBlockState(), Block.UPDATE_ALL);
        }

        FakePlayer player = FakePlayerFactory.getMinecraft(level);
        ((ICFControllerBlock) ModBlocks.ICF_CONTROLLER.get()).use(controllerState, level, controllerPos, player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(controllerPos), Direction.UP, controllerPos, false));
        assertTrue(controller.isAssembled(), "source-backed laser component shell assembles");
        assertEquals(7_500_000L, controller.getMaxPower(),
                "one valid capacitor and turbo keep the legacy sqrt power formula");

        BlockState reactorState = ModBlocks.ICF.get().defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        level.setBlock(reactorPos, reactorState, Block.UPDATE_ALL);
        LegacyMultiblockLayout reactorLayout = ICFStructCoreBlockEntity.icfLayout(Direction.SOUTH);
        boolean reactorFilled = MultiblockHelper.fillLayout(level, reactorPos, reactorLayout);
        assertTrue(reactorFilled, "complete ICF reactor multiblock fixture fills its legacy layout; problems="
                + MultiblockHelper.findLayoutProblems(level, reactorPos, reactorLayout).stream().limit(12).toList());
        assertTrue(level.getBlockEntity(reactorPos) instanceof ICFReactorBlockEntity,
                "ICF reactor fixture creates its block entity");
        ICFReactorBlockEntity reactor = (ICFReactorBlockEntity) level.getBlockEntity(reactorPos);
        ItemStack pellet = ICFPelletItem.setup(FuelType.DEUTERIUM, FuelType.TRITIUM, true);
        reactor.getItems().setStackInSlot(ICFReactorBlockEntity.SLOT_INPUT_START, pellet);

        IronGolem beamTarget = EntityType.IRON_GOLEM.create(level);
        assertTrue(beamTarget != null, "ICF laser damage fixture creates its living target");
        BlockPos targetPos = controllerPos.south(4);
        beamTarget.setPos(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
        assertTrue(level.addFreshEntity(beamTarget), "ICF laser damage fixture enters the test world");
        float targetHealthBefore = beamTarget.getHealth();

        long pulse = controller.getMaxPower();
        controller.setPower(pulse);
        ICFControllerBlockEntity.serverTick(level, controllerPos, controllerState, controller);
        assertEquals(8L, controller.getLaserLength(), "laser stops at the source-backed reactor entrance dummy");
        assertEquals(0L, controller.getPower(), "laser consumes all stored controller power after firing");
        assertEquals(pulse, reactor.getLaser(), "reactor receives the controller laser pulse");
        assertEquals(pulse, reactor.getMaxLaser(), "reactor receives the controller maximum-laser denominator");
        assertFloatEquals(targetHealthBefore - 50.0F, beamTarget.getHealth(),
                "source-backed ICF beam deals 50 in-fire damage to entities in its path");
        assertTrue(beamTarget.getRemainingFireTicks() >= 5 * 20,
                "source-backed ICF beam ignites entities in its path for five seconds");
        beamTarget.discard();

        ICFReactorBlockEntity.serverTick(level, reactorPos, reactorState, reactor);
        assertTrue(reactor.getHeat() > 0L, "received laser pulse heats the ICF reactor");
        assertTrue(ICFPelletItem.getDepletion(reactor.getItems().getStackInSlot(ICFReactorBlockEntity.SLOT_ACTIVE)) > 0L,
                "received laser pulse fuses and depletes the active pellet");
        assertEquals(0L, reactor.getLaser(), "reactor clears the transient laser pulse after its tick");
        assertEquals(0L, reactor.getMaxLaser(), "reactor clears the transient maximum-laser pulse after its tick");

        long depletionBeforeNetworkPulse = ICFPelletItem.getDepletion(
                reactor.getItems().getStackInSlot(ICFReactorBlockEntity.SLOT_ACTIVE));
        BlockPos firstCable = port.east();
        BlockPos batteryPos = port.east(5);
        for (BlockPos cablePos : BlockPos.betweenClosed(firstCable, batteryPos.west())) {
            level.setBlock(cablePos, ModBlocks.RED_CABLE.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(cablePos) instanceof HbmEnergyNodeBlockEntity cable) {
                cable.refreshEnergyNode();
            }
        }
        level.setBlock(batteryPos, ModBlocks.MACHINE_SCHRABIDIUM_BATTERY.get().defaultBlockState(), Block.UPDATE_ALL);
        assertTrue(level.getBlockEntity(batteryPos) instanceof MachineBatteryBlockEntity,
                "source-backed high-discharge battery fixture creates its block entity");
        MachineBatteryBlockEntity battery = (MachineBatteryBlockEntity) level.getBlockEntity(batteryPos);
        while (battery.getRedLow() != MachineBatteryBlockEntity.MODE_OUTPUT) {
            battery.cycleRedLowMode();
        }
        battery.setPower(pulse);

        HbmEnergyNodespace.tick(level);
        ICFControllerBlockEntity.serverTick(level, controllerPos, controllerState, controller);
        MachineBatteryBlockEntity.serverTick(level, batteryPos, level.getBlockState(batteryPos), battery);
        HbmPowerNet powerNet = HbmEnergyUtil.getPowerNet(level, firstCable);
        assertTrue(powerNet != null && powerNet.isValid(),
                "assembled ICF port is attached to the real red-cable power network");
        HbmPowerNet.DebugSnapshot beforeTransfer = powerNet.createDebugSnapshot();
        long transferred = powerNet.update();
        assertTrue(transferred > 0L, "real Energy MK2 network transfers an ICF laser pulse; snapshot="
                + beforeTransfer + ", battery=" + battery.getPower() + ", controller=" + controller.getPower());
        assertEquals(pulse, controller.getPower(), "assembled ICF port receives the full network pulse");
        assertTrue(battery.getPower() < pulse, "high-discharge battery spends HE into the assembled ICF port");

        ICFControllerBlockEntity.serverTick(level, controllerPos, controllerState, controller);
        assertEquals(8L, controller.getLaserLength(), "network-powered laser reaches the reactor entrance dummy");
        assertEquals(pulse, reactor.getLaser(), "network-powered controller forwards its pulse to the reactor");
        ICFReactorBlockEntity.serverTick(level, reactorPos, reactorState, reactor);
        assertTrue(ICFPelletItem.getDepletion(reactor.getItems().getStackInSlot(ICFReactorBlockEntity.SLOT_ACTIVE))
                        > depletionBeforeNetworkPulse,
                "network-powered laser pulse continues the active ICF reaction");
        helper.succeed();
    }

    private static void forceLoaded(ServerLevel level, BlockPos min, BlockPos max) {
        for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++) {
            for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    private static void clear(ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
        }
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFloatEquals(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > 0.001F) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
