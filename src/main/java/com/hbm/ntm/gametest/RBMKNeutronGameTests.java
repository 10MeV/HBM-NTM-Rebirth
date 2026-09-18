package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.neutron.NeutronNodeWorld;
import com.hbm.ntm.neutron.RBMKFluxReceiver;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.hbm.ntm.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegistryObject;

/** Regression coverage for source-backed RBMK neutron-stream interactions. */
@PrefixGameTestTemplate(false)
public final class RBMKNeutronGameTests {
    private static final double TEST_FLUX = 100.0D;
    private static final double TEST_CONTROL_LEVEL = 0.25D;

    private RBMKNeutronGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(RBMKNeutronGameTests.class);
    }

    /**
     * Legacy TileEntityRBMKControl did not implement IRBMKFluxReceiver. The
     * shared modern column BE does, so node type must win over interface
     * identity when selecting the old stream interaction branch.
     */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "rbmkNeutron")
    public static void everyControlColumnAttenuatesFluxDespiteSharedReceiverInterface(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        NeutronNodeWorld.unloadLevel(level);
        List<RegistryObject<Block>> controlBlocks = List.of(
                ModBlocks.RBMK_CONTROL,
                ModBlocks.RBMK_CONTROL_MOD,
                ModBlocks.RBMK_CONTROL_AUTO,
                ModBlocks.RBMK_CONTROL_REASIM,
                ModBlocks.RBMK_CONTROL_REASIM_AUTO);

        for (int index = 0; index < controlBlocks.size(); index++) {
            BlockPos originPos = helper.absolutePos(new BlockPos(2, 2, 2 + index * 2));
            BlockPos controlPos = originPos.east();
            clearStreamCorridor(level, originPos);
            RBMKColumnBlockEntity origin = placeColumn(level, originPos, ModBlocks.RBMK_BLANK);
            RBMKColumnBlockEntity control = placeColumn(level, controlPos, controlBlocks.get(index));
            control.controlState().setLevel(TEST_CONTROL_LEVEL);

            assertTrue(control instanceof RBMKFluxReceiver,
                    "fixture must retain the shared modern flux-receiver interface overlap");
            assertTrue(control.controlLevel() == TEST_CONTROL_LEVEL,
                    "control layout and level must be operational before the stream test");

            NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(level);
            RBMKNeutronHandler.RBMKNeutronNode originNode = RBMKNeutronHandler.makeNode(streamWorld, origin);
            streamWorld.addNode(originNode);
            RBMKNeutronHandler.RBMKNeutronStream stream = new RBMKNeutronHandler.RBMKNeutronStream(
                    originNode, new Vec3(1.0D, 0.0D, 0.0D), TEST_FLUX, 1.0D);

            stream.runStreamInteraction(level, streamWorld);
            assertEquals(TEST_FLUX * TEST_CONTROL_LEVEL, stream.getFluxQuantity(),
                    "control column " + control.kind() + " must apply its legacy extraction multiplier");
        }

        NeutronNodeWorld.unloadLevel(level);
        helper.succeed();
    }

    private static RBMKColumnBlockEntity placeColumn(ServerLevel level, BlockPos pos,
            RegistryObject<Block> blockObject) {
        Block block = blockObject.get();
        assertTrue(block instanceof RBMKColumnBlock, "RBMK column registry object must use RBMKColumnBlock");
        RBMKColumnBlock columnBlock = (RBMKColumnBlock) block;
        // The empty GameTest template sits in generated terrain. Give the
        // source-backed vertical column the same replaceable space as an
        // ordinary player placement before asking setPlacedBy to fill it.
        for (int height = 0; height <= RBMKStructureDimensions.columnHeightAboveCore(); height++) {
            level.setBlock(pos.above(height), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        BlockState state = block.defaultBlockState();
        level.setBlock(pos, state, Block.UPDATE_ALL);
        columnBlock.setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
        assertTrue(level.getBlockEntity(pos) instanceof RBMKColumnBlockEntity,
                "RBMK column placement must create its shared block entity at " + pos + ", got "
                        + level.getBlockState(pos));
        return (RBMKColumnBlockEntity) level.getBlockEntity(pos);
    }

    private static void clearStreamCorridor(ServerLevel level, BlockPos originPos) {
        for (int distance = 0; distance <= RBMKNeutronHandler.settings().fluxRange(); distance++) {
            BlockPos base = originPos.east(distance);
            for (int height = 0; height < RBMKStructureDimensions.neutronScanSegments(); height++) {
                level.setBlock(base.above(height), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Double.compare(expected, actual) != 0) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
