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
import com.hbm.ntm.blockentity.AutocrafterBlockEntity;
import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.blockentity.MassStorageBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.entity.item.MovingPackageEntity;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.BedrockOreItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySirenTrack;
import com.hbm.ntm.util.LegacyPatternMatcher;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Source-backed contracts from the 1.7.10 conveyor blocks, moving conveyor
 * entities, and conveyor wand.  These tests deliberately cover the shared
 * library rather than only a downstream machine consumer.
 */
@PrefixGameTestTemplate(false)
public final class ConveyorGameTests {
    private ConveyorGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(ConveyorGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
    public static void patternMatcherPreservesLegacyMetadataAndDurabilityContracts(GameTestHelper helper) {
        LegacyStateBlockItem absorber = (LegacyStateBlockItem) ModBlocks.RAD_ABSORBER.get().asItem();
        ItemStack tierOne = LegacyStateBlockItem.createStack(absorber, 1);
        ItemStack tierTwo = LegacyStateBlockItem.createStack(absorber, 2);
        LegacyPatternMatcher matcher = new LegacyPatternMatcher(7);

        matcher.initPatternStandard(tierOne, 0);
        assertEquals(LegacyPatternMatcher.MODE_EXACT, matcher.getMode(0),
                "legacy state-backed metadata defaults to exact mode");
        assertTrue(matcher.isValidForFilter(tierOne, 0, tierOne.copy()),
                "exact metadata filter accepts its own variant");
        assertTrue(!matcher.isValidForFilter(tierOne, 0, tierTwo),
                "exact metadata filter rejects another state-backed variant");

        ItemStack wornSword = new ItemStack(Items.DIAMOND_SWORD);
        wornSword.setDamageValue(12);
        matcher.initPatternStandard(wornSword, 1);
        assertEquals(LegacyPatternMatcher.MODE_WILDCARD, matcher.getMode(1),
                "durability is not legacy metadata");
        assertTrue(matcher.isValidForFilter(wornSword, 1, new ItemStack(Items.DIAMOND_SWORD)),
                "wildcard mode accepts the same durable item at another wear value");

        ItemStack waterIcon = FluidIconItem.make(HbmFluids.WATER, 100);
        ItemStack gasolineIcon = FluidIconItem.make(HbmFluids.GASOLINE, 100);
        matcher.initPatternStandard(waterIcon, 2);
        assertEquals(LegacyPatternMatcher.MODE_EXACT, matcher.getMode(2),
                "legacy fluid-icon metadata defaults to exact mode");
        assertTrue(!matcher.isValidForFilter(waterIcon, 2, gasolineIcon),
                "exact fluid-icon filter ignores amount NBT but preserves the old fluid-id metadata");

        HbmFluidContainerItem canister = (HbmFluidContainerItem) ModItems.CANISTER_FULL.get();
        ItemStack waterCanister = canister.createFilledStack(HbmFluids.WATER);
        ItemStack gasolineCanister = canister.createFilledStack(HbmFluids.GASOLINE);
        matcher.initPatternStandard(waterCanister, 3);
        assertEquals(LegacyPatternMatcher.MODE_EXACT, matcher.getMode(3),
                "legacy fluid-container metadata defaults to exact mode");
        assertTrue(!matcher.isValidForFilter(waterCanister, 3, gasolineCanister),
                "exact fluid-container filter preserves the old fluid-id metadata");

        ItemStack nuggetMold = FoundryMoldItem.stackForId(0);
        ItemStack billetMold = FoundryMoldItem.stackForId(1);
        matcher.initPatternStandard(nuggetMold, 4);
        assertTrue(!matcher.isValidForFilter(nuggetMold, 4, billetMold),
                "exact mold filter preserves its old mold metadata");

        ItemStack hatchCassette = SirenCassetteItem.stackForTrack(ModItems.SIREN_TRACK.get(), LegacySirenTrack.HATCH);
        ItemStack autopilotCassette = SirenCassetteItem.stackForTrack(ModItems.SIREN_TRACK.get(), LegacySirenTrack.ATUOPILOT);
        matcher.initPatternStandard(hatchCassette, 5);
        assertTrue(!matcher.isValidForFilter(hatchCassette, 5, autopilotCassette),
                "exact cassette filter preserves the old track metadata");

        ItemStack lightBedrock = BedrockOreItem.make(BedrockOreItem.BedrockOreGrade.BASE,
                BedrockOreItem.BedrockOreType.LIGHT_METAL);
        ItemStack heavyBedrock = BedrockOreItem.make(BedrockOreItem.BedrockOreGrade.BASE,
                BedrockOreItem.BedrockOreType.HEAVY_METAL);
        matcher.initPatternStandard(lightBedrock, 6);
        assertEquals(LegacyPatternMatcher.MODE_BEDROCK, matcher.getMode(6),
                "bedrock ore retains its source-backed grade mode");
        assertTrue(matcher.isValidForFilter(lightBedrock, 6, heavyBedrock),
                "bedrock mode accepts another ore type with the same grade");
        matcher.setMode(6, LegacyPatternMatcher.MODE_EXACT);
        assertTrue(!matcher.isValidForFilter(lightBedrock, 6, heavyBedrock),
                "bedrock exact mode preserves the full old metadata value, including ore type");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "energy_workspace", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
    public static void movingItemsCramAndOffBeltDropsFollowLegacyLifecycle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
        ConveyorBlock belt = (ConveyorBlock) ModBlocks.CONVEYOR.get();
        level.setBlock(pos, belt.stateFromLegacyMetadata(Direction.NORTH.get3DDataValue()), Block.UPDATE_ALL);

        MovingItemEntity anchor = null;
        for (int index = 0; index < 25; index++) {
            MovingItemEntity moving = new MovingItemEntity(level, new ItemStack(Items.IRON_INGOT));
            moving.setPos(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
            level.addFreshEntity(moving);
            if (anchor == null) {
                anchor = moving;
            }
        }
        int checkTick = 400 - Math.floorMod(anchor.getId(), 400);
        if (checkTick <= 5) {
            checkTick += 400;
        }
        anchor.tickCount = checkTick - 1;
        anchor.tick();
        assertSame(Blocks.AIR, level.getBlockState(pos).getBlock(), "cram check destroys the occupied conveyor");
        assertEquals(0, level.getEntitiesOfClass(MovingItemEntity.class, new AABB(pos).inflate(1.0D)).size(),
                "cram check discards every overlapping moving conveyor object");

        MovingItemEntity leaving = new MovingItemEntity(level, new ItemStack(Items.GOLD_INGOT));
        leaving.setPos(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
        leaving.setDeltaMovement(0.1D, 0.0D, 0.0D);
        leaving.tickCount = 6;
        level.addFreshEntity(leaving);
        leaving.tick();
        List<ItemEntity> droppedItems = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(1.0D));
        assertEquals(1, droppedItems.size(), "leaving moving item becomes one vanilla item entity");
        assertEquals(60 * 20, droppedItems.get(0).lifespan, "off-belt drop keeps the legacy one-minute lifespan");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
    public static void settingsToolCopiesLegacyCraneFiltersOrientationsAndRouterPatterns(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);

        BlockPos extractorSourcePos = helper.absolutePos(new BlockPos(1, 2, 1));
        BlockPos extractorTargetPos = helper.absolutePos(new BlockPos(3, 2, 1));
        CraneLogisticsBlock extractorBlock = (CraneLogisticsBlock) ModBlocks.CRANE_EXTRACTOR.get();
        level.setBlock(extractorSourcePos,
                extractorBlock.defaultBlockState().setValue(CraneLogisticsBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        level.setBlock(extractorTargetPos,
                extractorBlock.defaultBlockState().setValue(CraneLogisticsBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        CraneLogisticsBlockEntity extractorSource = (CraneLogisticsBlockEntity) level.getBlockEntity(extractorSourcePos);
        CraneLogisticsBlockEntity extractorTarget = (CraneLogisticsBlockEntity) level.getBlockEntity(extractorTargetPos);
        extractorSource.setInput(Direction.EAST);
        extractorSource.setOutputOverride(Direction.UP);
        extractorSource.setPatternStack(3, new ItemStack(Items.EMERALD));
        CompoundTag extractorSettings = extractorSource.getSettings(level, extractorSourcePos);
        extractorTarget.pasteSettings(extractorSettings, 0, level, player, extractorTargetPos);
        assertSame(Items.EMERALD, extractorTarget.getItems().getStackInSlot(3).getItem(),
                "normal crane index zero copies its sparse filter slot");
        extractorTarget.pasteSettings(extractorSettings, 1, level, player, extractorTargetPos);
        assertEquals(extractorSource.getInputSide(), extractorTarget.getInputSide(),
                "normal crane index one copies input orientation");
        assertEquals(extractorSource.getOutputSide(), extractorTarget.getOutputSide(),
                "normal crane index one copies output orientation");

        BlockPos routerSourcePos = helper.absolutePos(new BlockPos(6, 2, 1));
        BlockPos routerTargetPos = helper.absolutePos(new BlockPos(8, 2, 1));
        CraneLogisticsBlock routerBlock = (CraneLogisticsBlock) ModBlocks.CRANE_ROUTER.get();
        level.setBlock(routerSourcePos, routerBlock.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(routerTargetPos, routerBlock.defaultBlockState(), Block.UPDATE_ALL);
        CraneLogisticsBlockEntity routerSource = (CraneLogisticsBlockEntity) level.getBlockEntity(routerSourcePos);
        CraneLogisticsBlockEntity routerTarget = (CraneLogisticsBlockEntity) level.getBlockEntity(routerTargetPos);
        routerSource.setPatternStack(5, new ItemStack(Items.IRON_INGOT));
        routerSource.setPatternStack(6, new ItemStack(Items.DIAMOND));
        CompoundTag modeToggle = new CompoundTag();
        modeToggle.putInt("toggle", Direction.NORTH.get3DDataValue());
        routerSource.receiveControl(player, modeToggle);
        routerTarget.setPatternStack(5, new ItemStack(Items.GOLD_INGOT));
        routerTarget.setPatternStack(6, new ItemStack(Items.REDSTONE));
        routerTarget.pasteSettings(routerSource.getSettings(level, routerSourcePos), 1, level, player, routerTargetPos);
        assertSame(Items.GOLD_INGOT, routerTarget.getItems().getStackInSlot(5).getItem(),
                "legacy router pattern copy leaves the first selected slot untouched");
        assertSame(Items.DIAMOND, routerTarget.getItems().getStackInSlot(6).getItem(),
                "legacy router pattern copy updates the remaining selected slots");
        assertEquals(routerSource.getRouterMode(Direction.NORTH.get3DDataValue()),
                routerTarget.getRouterMode(Direction.NORTH.get3DDataValue()),
                "router copy synchronizes the complete legacy route-mode array");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
    public static void settingsToolCopiesLegacyAutocrafterTemplateFilters(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        BlockPos sourcePos = helper.absolutePos(new BlockPos(11, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(13, 2, 1));
        level.setBlock(sourcePos, ModBlocks.MACHINE_AUTOCRAFTER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(targetPos, ModBlocks.MACHINE_AUTOCRAFTER.get().defaultBlockState(), Block.UPDATE_ALL);
        AutocrafterBlockEntity source = (AutocrafterBlockEntity) level.getBlockEntity(sourcePos);
        AutocrafterBlockEntity target = (AutocrafterBlockEntity) level.getBlockEntity(targetPos);
        source.getItems().setStackInSlot(2, new ItemStack(Items.IRON_INGOT));
        source.updatePatternSlot(2, source.getItems().getStackInSlot(2));
        target.getItems().setStackInSlot(4, new ItemStack(Items.GOLD_INGOT));
        target.updatePatternSlot(4, target.getItems().getStackInSlot(4));

        target.pasteSettings(source.getSettings(level, sourcePos), 0, level, player, targetPos);
        assertSame(Items.IRON_INGOT, target.getItems().getStackInSlot(2).getItem(),
                "autocrafter settings copy restores the relative template filter slot");
        assertSame(Items.GOLD_INGOT, target.getItems().getStackInSlot(4).getItem(),
                "autocrafter settings copy preserves sparse target template slots");
        assertEquals(0, target.getModeIndex(2),
                "legacy filter paste invokes nextMode from the empty target matcher state");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
    public static void settingsToolCopiesLegacyMassStorageFilterWithoutControlPacketGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        BlockPos sourcePos = helper.absolutePos(new BlockPos(15, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(17, 2, 1));
        level.setBlock(sourcePos, ModBlocks.MASS_STORAGE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(targetPos, ModBlocks.MASS_STORAGE.get().defaultBlockState(), Block.UPDATE_ALL);
        MassStorageBlockEntity source = (MassStorageBlockEntity) level.getBlockEntity(sourcePos);
        MassStorageBlockEntity target = (MassStorageBlockEntity) level.getBlockEntity(targetPos);
        source.setFilter(new ItemStack(Items.IRON_INGOT));
        target.setFilter(new ItemStack(Items.GOLD_INGOT));
        assertEquals(0, target.increaseTotalStockpile(7, true), "mass storage fixture fills the existing filter type");

        target.pasteSettings(source.getSettings(level, sourcePos), 0, level, player, targetPos);
        assertSame(Items.IRON_INGOT, target.type().getItem(),
                "legacy settings paste writes the mass-storage filter despite non-empty stockpile");
        assertEquals(7, target.stockpile(), "settings paste does not alter the stored count");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
    public static void settingsToolCopiesLegacyRadioTorchCounterFilters(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        BlockPos sourcePos = helper.absolutePos(new BlockPos(19, 2, 1));
        BlockPos targetPos = helper.absolutePos(new BlockPos(21, 2, 1));
        level.setBlock(sourcePos, ModBlocks.RADIO_TORCH_COUNTER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(targetPos, ModBlocks.RADIO_TORCH_COUNTER.get().defaultBlockState(), Block.UPDATE_ALL);
        RadioTorchCounterBlockEntity source = (RadioTorchCounterBlockEntity) level.getBlockEntity(sourcePos);
        RadioTorchCounterBlockEntity target = (RadioTorchCounterBlockEntity) level.getBlockEntity(targetPos);
        source.getFilterItems().setStackInSlot(2, new ItemStack(Items.IRON_INGOT));

        target.pasteSettings(source.getSettings(level, sourcePos), 0, level, player, targetPos);
        assertSame(Items.IRON_INGOT, target.getFilterItems().getStackInSlot(2).getItem(),
                "radio counter settings copy restores the relative third filter slot");
        assertEquals("Item and meta match", target.filterModeLabel(2),
                "radio counter paste advances a blank target matcher to the legacy exact mode");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "conveyorLibrary")
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
