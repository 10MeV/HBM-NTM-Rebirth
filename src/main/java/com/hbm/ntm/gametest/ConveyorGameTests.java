package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorPathType;
import com.hbm.ntm.api.conveyor.ConveyorRoutePlanner;
import com.hbm.ntm.api.conveyor.ConveyorRoutePlanner.ConveyorBlockKind;
import com.hbm.ntm.api.conveyor.ConveyorRoutePlanner.ConveyorWandType;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.block.CraneLogisticsBlock;
import com.hbm.ntm.block.conveyor.ChuteConveyorBlock;
import com.hbm.ntm.block.conveyor.ConveyorBlock;
import com.hbm.ntm.block.conveyor.LiftConveyorBlock;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Source-backed contracts from the 1.7.10 conveyor blocks, moving conveyor
 * entities, and conveyor wand.  These tests deliberately cover the shared
 * library rather than only a downstream machine consumer.
 */
@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConveyorGameTests {
    private ConveyorGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(ConveyorGameTests.class);
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void legacyMetadataAndLaneContracts(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 0, 0);
        for (int metadata = 2; metadata <= 13; metadata++) {
            ConveyorPathType path = ConveyorPathType.fromLegacyMetadata(metadata);
            int base = ConveyorMath.baseLegacyMetadata(metadata);
            assertEquals(Direction.from3DDataValue(base), ConveyorMath.inputDirection(metadata),
                    "input direction metadata " + metadata);
            Direction straightOutput = ConveyorMath.inputDirection(metadata).getOpposite();
            Direction expectedOutput = path == ConveyorPathType.RIGHT ? straightOutput.getClockWise()
                    : path == ConveyorPathType.LEFT ? straightOutput.getCounterClockWise() : straightOutput;
            assertEquals(expectedOutput, ConveyorMath.outputDirection(metadata),
                    "output direction metadata " + metadata);
        }

        Vec3 center = new Vec3(0.5D, 0.25D, 0.5D);
        int north = Direction.NORTH.get3DDataValue();
        assertVec3(0.5D, 0.25D, 0.5625D,
                ConveyorMath.travelLocation(north, pos, center, ConveyorMath.baseSpeed()), "regular speed");
        assertVec3(0.5D, 0.25D, 0.6875D,
                ConveyorMath.expressTravelLocation(north, pos, center, ConveyorMath.baseSpeed()), "express speed");
        assertVec3(0.75D, 0.25D, 0.5D,
                ConveyorMath.closestDoubleLaneSnappingPosition(north, pos, new Vec3(0.8D, 0.25D, 0.5D)),
                "double outer lane");
        assertVec3(0.8125D, 0.25D, 0.5D,
                ConveyorMath.closestTripleLaneSnappingPosition(north, pos, new Vec3(0.9D, 0.25D, 0.5D)),
                "triple outer lane");
        assertVec3(0.5D, 0.25D, 0.5D,
                ConveyorMath.closestTripleLaneSnappingPosition(north, pos, new Vec3(0.6D, 0.25D, 0.5D)),
                "triple center lane");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void liftAndChuteSegmentContracts(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos liftPos = helper.absolutePos(new BlockPos(1, 2, 1));
        ConveyorBlock lift = (ConveyorBlock) ModBlocks.CONVEYOR_LIFT.get();
        ConveyorBlock chute = (ConveyorBlock) ModBlocks.CONVEYOR_CHUTE.get();

        BlockState bottom = LiftConveyorBlock.withSegmentState(lift.defaultBlockState(), false, true, false);
        assertTrue(bottom.getValue(LiftConveyorBlock.BOTTOM) && !bottom.getValue(LiftConveyorBlock.TOP),
                "lift bottom state");
        BlockState middle = LiftConveyorBlock.withSegmentState(lift.defaultBlockState(), true, true, false);
        assertTrue(!middle.getValue(LiftConveyorBlock.BOTTOM) && !middle.getValue(LiftConveyorBlock.TOP),
                "lift middle state");
        BlockState top = LiftConveyorBlock.withSegmentState(lift.defaultBlockState(), true, false, false);
        assertTrue(!top.getValue(LiftConveyorBlock.BOTTOM) && top.getValue(LiftConveyorBlock.TOP), "lift top state");

        BlockState freeChute = ChuteConveyorBlock.withVisualState(chute.defaultBlockState(), true, false,
                true, false, false, true);
        assertTrue(freeChute.getValue(ChuteConveyorBlock.FREE_BOTTOM), "chute free-bottom state");
        assertTrue(freeChute.getValue(ChuteConveyorBlock.WEST_BELT)
                && freeChute.getValue(ChuteConveyorBlock.SOUTH_BELT), "chute side-belt states");

        level.setBlock(liftPos, lift.stateFromLegacyMetadata(Direction.NORTH.get3DDataValue()), Block.UPDATE_ALL);
        level.setBlock(liftPos.above(), lift.stateFromLegacyMetadata(Direction.NORTH.get3DDataValue()), Block.UPDATE_ALL);
        assertEquals(Direction.DOWN, ConveyorMath.liftTravelDirection(level, liftPos,
                Direction.NORTH.get3DDataValue()), "lift non-top moves vertically");
        // The upper segment has a conveyor below and no conveyor/entry above, so it is the legacy top exit.
        assertEquals(Direction.NORTH, ConveyorMath.liftTravelDirection(level, liftPos.above(),
                Direction.NORTH.get3DDataValue()), "lift top exits through its horizontal input");

        BlockPos chutePos = liftPos.east();
        level.setBlock(chutePos.below(), ModBlocks.CONVEYOR.get().defaultBlockState(), Block.UPDATE_ALL);
        assertEquals(Direction.UP, ConveyorMath.chuteTravelDirection(level, chutePos,
                Direction.NORTH.get3DDataValue(), new Vec3(chutePos.getX() + 0.5D, chutePos.getY() + 0.25D,
                        chutePos.getZ() + 0.5D)), "chute above a belt falls vertically");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void wandRoutePlannerReturnsLegacyOutcomes(GameTestHelper helper) {
        ConveyorRoutePlanner.RouteContext successContext = routeContext(4, pos -> true);
        ConveyorRoutePlanner.RouteResult success = ConveyorRoutePlanner.plan(successContext);
        assertEquals(ConveyorRoutePlanner.Status.SUCCESS, success.status(), "wand successful route status");
        assertEquals(4, success.placements().size(), "wand successful route count");
        assertEquals(ConveyorBlockKind.REGULAR, success.placements().get(0).kind(), "wand regular block kind");

        ConveyorRoutePlanner.RouteResult obstructed = ConveyorRoutePlanner.plan(routeContext(4,
                pos -> !pos.equals(new BlockPos(1, 0, 0))));
        assertEquals(ConveyorRoutePlanner.Status.OBSTRUCTED, obstructed.status(), "wand obstruction status");

        ConveyorRoutePlanner.RouteResult insufficient = ConveyorRoutePlanner.plan(routeContext(3, pos -> true));
        assertEquals(ConveyorRoutePlanner.Status.NOT_ENOUGH_CONVEYORS, insufficient.status(),
                "wand insufficient conveyor status");
        assertEquals(3, insufficient.placements().size(), "wand retains planned partial route");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void itemEntityConvertsAndMovesOnConveyor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        ConveyorBlock belt = (ConveyorBlock) ModBlocks.CONVEYOR.get();
        BlockState state = belt.stateFromLegacyMetadata(Direction.NORTH.get3DDataValue());
        level.setBlock(pos, state, Block.UPDATE_ALL);

        ItemEntity dropped = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D,
                new ItemStack(Items.IRON_INGOT));
        dropped.tickCount = 11;
        level.addFreshEntity(dropped);
        belt.entityInside(state, level, pos, dropped);
        assertTrue(dropped.isRemoved(), "vanilla item is replaced by a moving conveyor item");

        List<MovingItemEntity> movingItems = level.getEntitiesOfClass(MovingItemEntity.class,
                new AABB(pos).inflate(1.0D));
        assertEquals(1, movingItems.size(), "exactly one moving item is created");
        MovingItemEntity moving = movingItems.get(0);
        moving.tickCount = 6;
        double beforeZ = moving.getZ();
        moving.tick();
        assertTrue(moving.getZ() > beforeZ, "moving item follows the belt output direction");
        assertSame(Items.IRON_INGOT, moving.getItemStack().getItem(), "moving item keeps its stack");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "energy_workspace", batch = "conveyorLibrary")
    public static void wandTwoPointUsePlacesAndConsumesConveyors(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos start = helper.absolutePos(new BlockPos(1, 2, 8));
        BlockPos end = start.east(3);
        var player = FakePlayerFactory.getMinecraft(level);
        player.getInventory().clearContent();
        player.setYRot(0.0F);

        ItemStack wandStack = ConveyorWandItem.createStack(ModItems.CONVEYOR_WAND.get(),
                ConveyorWandType.REGULAR, 8);
        player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);
        ConveyorWandItem wand = (ConveyorWandItem) wandStack.getItem();
        assertTrue(wand.useOn(useOn(player, start, Direction.EAST)).consumesAction(), "wand selects start point");
        assertTrue(ConveyorWandItem.hasStart(wandStack), "wand stores first route point in NBT");
        assertTrue(wand.useOn(useOn(player, end, Direction.EAST)).consumesAction(), "wand builds selected route");
        assertTrue(!ConveyorWandItem.hasStart(wandStack), "wand clears route point after second use");
        for (int x = 1; x <= 4; x++) {
            assertSame(ModBlocks.CONVEYOR.get(), level.getBlockState(start.east(x)).getBlock(),
                    "wand places regular conveyor " + x);
        }
        assertEquals(4, wandStack.getCount(), "wand consumes exactly the four placed conveyors");
        player.getInventory().clearContent();
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void movingItemEntersCraneInserterFromLegacyInputSide(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos cranePos = helper.absolutePos(new BlockPos(8, 2, 3));
        CraneLogisticsBlock craneBlock = (CraneLogisticsBlock) ModBlocks.CRANE_INSERTER.get();
        BlockState state = craneBlock.defaultBlockState().setValue(CraneLogisticsBlock.FACING, Direction.SOUTH);
        level.setBlock(cranePos, state, Block.UPDATE_ALL);
        assertTrue(level.getBlockEntity(cranePos) instanceof CraneLogisticsBlockEntity,
                "crane inserter creates its logistics block entity");
        CraneLogisticsBlockEntity crane = (CraneLogisticsBlockEntity) level.getBlockEntity(cranePos);
        IEnterableBlock enterable = craneBlock;
        assertTrue(enterable.canItemEnter(level, cranePos, Direction.SOUTH,
                new MovingItemEntity(level, new ItemStack(Items.GOLD_INGOT))), "inserter accepts its input side");
        assertTrue(!enterable.canItemEnter(level, cranePos, Direction.NORTH,
                new MovingItemEntity(level, new ItemStack(Items.GOLD_INGOT))), "inserter rejects its output side");

        MovingItemEntity incoming = new MovingItemEntity(level, new ItemStack(Items.GOLD_INGOT, 3));
        incoming.enterBlock(enterable, cranePos, Direction.SOUTH);
        assertTrue(incoming.isRemoved(), "accepted moving item is consumed by the crane");
        assertSame(Items.GOLD_INGOT, crane.getItems().getStackInSlot(0).getItem(),
                "crane inserter receives the moving item stack");
        assertEquals(3, crane.getItems().getStackInSlot(0).getCount(), "crane inserter preserves item count");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void movingPackagesHonorLegacyCraneEntrySides(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos boxerPos = helper.absolutePos(new BlockPos(1, 2, 12));
        BlockPos unboxerPos = helper.absolutePos(new BlockPos(4, 2, 12));
        BlockPos partitionerPos = helper.absolutePos(new BlockPos(7, 2, 12));

        CraneLogisticsBlock boxerBlock = (CraneLogisticsBlock) ModBlocks.CRANE_BOXER.get();
        level.setBlock(boxerPos, boxerBlock.defaultBlockState().setValue(CraneLogisticsBlock.FACING, Direction.SOUTH),
                Block.UPDATE_ALL);
        CraneLogisticsBlockEntity boxer = (CraneLogisticsBlockEntity) level.getBlockEntity(boxerPos);
        MovingPackageEntity boxerPackage = new MovingPackageEntity(level,
                new ItemStack[] {new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.GOLD_INGOT, 3)});
        assertTrue(boxerBlock.canPackageEnter(level, boxerPos, Direction.UP, boxerPackage),
                "legacy boxer accepts packages from every side");
        boxerPackage.enterBlock(boxerBlock, boxerPos, Direction.UP);
        assertTrue(boxerPackage.isRemoved(), "accepted boxer package is consumed");
        assertSame(Items.IRON_INGOT, boxer.getItems().getStackInSlot(0).getItem(),
                "boxer stores the first package stack");
        assertSame(Items.GOLD_INGOT, boxer.getItems().getStackInSlot(1).getItem(),
                "boxer stores the second package stack");

        CraneLogisticsBlock unboxerBlock = (CraneLogisticsBlock) ModBlocks.CRANE_UNBOXER.get();
        level.setBlock(unboxerPos,
                unboxerBlock.defaultBlockState().setValue(CraneLogisticsBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        CraneLogisticsBlockEntity unboxer = (CraneLogisticsBlockEntity) level.getBlockEntity(unboxerPos);
        MovingPackageEntity unboxerPackage = new MovingPackageEntity(level,
                new ItemStack[] {new ItemStack(Items.COPPER_INGOT, 4)});
        Direction unboxerOutput = unboxer.getOutputSide();
        assertTrue(unboxerBlock.canPackageEnter(level, unboxerPos, unboxerOutput, unboxerPackage),
                "legacy unboxer accepts packages from its output side");
        assertTrue(!unboxerBlock.canPackageEnter(level, unboxerPos, unboxerOutput.getOpposite(), unboxerPackage),
                "legacy unboxer rejects packages from non-output sides");
        unboxerPackage.enterBlock(unboxerBlock, unboxerPos, unboxerOutput);
        assertTrue(unboxerPackage.isRemoved(), "accepted unboxer package is consumed");
        assertSame(Items.COPPER_INGOT, unboxer.getItems().getStackInSlot(0).getItem(),
                "unboxer stores the incoming package stack");

        CraneLogisticsBlock partitionerBlock = (CraneLogisticsBlock) ModBlocks.CRANE_PARTITIONER.get();
        level.setBlock(partitionerPos,
                partitionerBlock.defaultBlockState().setValue(CraneLogisticsBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        CraneLogisticsBlockEntity partitioner = (CraneLogisticsBlockEntity) level.getBlockEntity(partitionerPos);
        MovingPackageEntity rejectedPackage = new MovingPackageEntity(level,
                new ItemStack[] {new ItemStack(Items.IRON_INGOT)});
        assertTrue(!partitionerBlock.canPackageEnter(level, partitionerPos, Direction.WEST, rejectedPackage),
                "legacy partitioner rejects packages");
        assertTrue(partitionerBlock.canItemEnter(level, partitionerPos, partitioner.getInputSide(),
                new MovingItemEntity(level, new ItemStack(Items.IRON_INGOT))),
                "legacy partitioner accepts items from its belt travel side");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "empty", batch = "conveyorLibrary")
    public static void wandSneakVerticalPlacementPreservesLegacyConversions(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos liftBase = helper.absolutePos(new BlockPos(12, 2, 12));
        BlockPos chuteBase = liftBase.east(3);
        ConveyorBlock regular = (ConveyorBlock) ModBlocks.CONVEYOR.get();
        // ItemConveyorWand only places into a replaceable target. Keep this fixture-local
        // extension space clear instead of relying on the shared empty-template terrain.
        level.setBlock(liftBase.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(chuteBase.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(liftBase, regular.stateFromLegacyMetadata(Direction.NORTH.get3DDataValue()), Block.UPDATE_ALL);
        level.setBlock(chuteBase, regular.stateFromLegacyMetadata(Direction.SOUTH.get3DDataValue()), Block.UPDATE_ALL);

        var player = FakePlayerFactory.getMinecraft(level);
        player.getInventory().clearContent();
        player.setShiftKeyDown(true);
        ItemStack wandStack = ConveyorWandItem.createStack(ModItems.CONVEYOR_WAND.get(),
                ConveyorWandType.REGULAR, 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, wandStack);
        ConveyorWandItem wand = (ConveyorWandItem) wandStack.getItem();

        assertTrue(wand.useOn(useOn(player, liftBase, Direction.UP)).consumesAction(),
                "sneak up use handles a regular conveyor");
        assertSame(ModBlocks.CONVEYOR_LIFT.get(), level.getBlockState(liftBase).getBlock(),
                "sneak up use converts the clicked regular conveyor to a lift");
        assertSame(ModBlocks.CONVEYOR_LIFT.get(), level.getBlockState(liftBase.above()).getBlock(),
                "sneak up use extends the lift vertically");
        assertEquals(2, wandStack.getCount(), "lift conversion and extension consume one conveyor item");

        assertTrue(wand.useOn(useOn(player, chuteBase, Direction.DOWN)).consumesAction(),
                "sneak down use handles a regular conveyor");
        assertSame(ModBlocks.CONVEYOR_CHUTE.get(), level.getBlockState(chuteBase).getBlock(),
                "sneak down use converts the clicked regular conveyor to a chute");
        assertSame(ModBlocks.CONVEYOR_CHUTE.get(), level.getBlockState(chuteBase.below()).getBlock(),
                "sneak down use extends the chute vertically");
        assertEquals(1, wandStack.getCount(), "chute conversion and extension consume one conveyor item");
        player.setShiftKeyDown(false);
        player.getInventory().clearContent();
        helper.succeed();
    }

    private static ConveyorRoutePlanner.RouteContext routeContext(int maxConveyors,
            ConveyorRoutePlanner.Replaceability replaceability) {
        return new ConveyorRoutePlanner.RouteContext(ConveyorWandType.REGULAR, BlockPos.ZERO, Direction.EAST,
                new BlockPos(3, 0, 0), Direction.EAST, maxConveyors, 0.0F, replaceability,
                pos -> ConveyorBlockKind.OTHER);
    }

    private static UseOnContext useOn(net.minecraft.world.entity.player.Player player, BlockPos pos, Direction side) {
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(pos.getCenter(), side, pos, false));
    }

    private static void assertVec3(double x, double y, double z, Vec3 actual, String label) {
        if (Math.abs(x - actual.x) > 1.0E-9D || Math.abs(y - actual.y) > 1.0E-9D
                || Math.abs(z - actual.z) > 1.0E-9D) {
            throw new AssertionError(label + ": expected " + new Vec3(x, y, z) + " but got " + actual);
        }
    }

    private static void assertSame(Object expected, Object actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
