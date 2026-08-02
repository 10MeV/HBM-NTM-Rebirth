package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.DroneCrateBlockEntity;
import com.hbm.ntm.blockentity.DroneLogisticsBlockEntity;
import com.hbm.ntm.drone.DroneFilter;
import com.hbm.ntm.entity.item.DeliveryDroneEntity;
import com.hbm.ntm.entity.item.RequestDroneEntity;
import com.hbm.ntm.item.DroneItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * Server-side regression coverage for the source-backed drone logistics contracts.  These
 * tests exercise actual block-entity ticks and entities; they are deliberately not a
 * replacement for the client OBJ/particle/GUI acceptance pass.
 */
@PrefixGameTestTemplate(false)
public final class DroneLogisticsGameTests {
    // Keep long-lived host fixtures above the empty-template top barrier and
    // other batches' ground-level machinery.
    private static final int DISPATCH_FIXTURE_Y = 124;
    private DroneLogisticsGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(DroneLogisticsGameTests.class);
    }

    /** TileEntityDroneCrate loads every cargo slot, flies to its linked point, then unloads. */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "drone_logistics", batch = "dronePatrol")
    public static void patrolDroneTransfersCargoBetweenLinkedCrates(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos destinationPos = helper.absolutePos(new BlockPos(8, 2, 2));
        level.setBlock(sourcePos, ModBlocks.DRONE_CRATE.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(destinationPos, ModBlocks.DRONE_CRATE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(sourcePos) instanceof DroneCrateBlockEntity source)
                || !(level.getBlockEntity(destinationPos) instanceof DroneCrateBlockEntity destination)) {
            throw new AssertionError("Drone crate fixture did not create both block entities");
        }

        source.items().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));
        source.handleLegacyButton(null, 0, DroneCrateBlockEntity.CONTROL_TOGGLE_MODE);
        source.setNextDroneTarget(destination.dronePoint());
        // The receiving crate requires a linked target before its legacy per-tick dock AABB
        // can unload the arriving drone.  Its own empty cargo means it cannot reload it.
        destination.setNextDroneTarget(source.dronePoint());

        DeliveryDroneEntity drone = new DeliveryDroneEntity(ModEntityTypes.DELIVERY_DRONE.get(), level);
        drone.moveTo(source.dronePoint().getX() + 0.5D, source.dronePoint().getY(),
                source.dronePoint().getZ() + 0.5D, 0.0F, 0.0F);
        level.addFreshEntity(drone);

        helper.startSequence()
                .thenIdle(50)
                .thenExecute(() -> {
                    assertEmpty(source.items().getStackInSlot(0), "source crate cargo was loaded into patrol drone");
                    ItemStack received = destination.items().getStackInSlot(0);
                    assertTrue(received.is(Items.IRON_INGOT) && received.getCount() == 12,
                            "destination crate received the complete patrol cargo");
                    assertTrue(drone.appearance() == 0, "unloaded patrol drone returned to empty appearance");
                })
                .thenSucceed();
    }

    /** EntityRequestDrone executes provider pickup, requester unload and dock return in order. */
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "drone_logistics", batch = "droneDirectRequest", timeoutTicks = 120)
    public static void requestDronePicksUnloadsAndReturnsToDock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos providerPos = helper.absolutePos(new BlockPos(2, 2, 8));
        BlockPos requesterPos = helper.absolutePos(new BlockPos(7, 2, 8));
        BlockPos dockPos = helper.absolutePos(new BlockPos(12, 2, 8));
        level.setBlock(providerPos, ModBlocks.DRONE_CRATE_PROVIDER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(requesterPos, ModBlocks.DRONE_CRATE_REQUESTER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(dockPos, ModBlocks.DRONE_DOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(providerPos) instanceof DroneLogisticsBlockEntity provider)
                || !(level.getBlockEntity(requesterPos) instanceof DroneLogisticsBlockEntity requester)
                || !(level.getBlockEntity(dockPos) instanceof DroneLogisticsBlockEntity dock)) {
            throw new AssertionError("Request-drone fixture did not create provider/requester/dock block entities");
        }

        provider.items().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));
        requester.setFilter(0, new ItemStack(Items.IRON_INGOT));
        RequestDroneEntity drone = new RequestDroneEntity(ModEntityTypes.REQUEST_DRONE.get(), level);
        BlockPos providerPoint = providerPos.above();
        BlockPos requesterPoint = requesterPos.above();
        BlockPos dockPoint = dockPos.above();
        drone.moveTo(providerPoint.getX() + 0.5D, providerPoint.getY() + 1.0D,
                providerPoint.getZ() + 0.5D, 0.0F, 0.0F);
        DroneFilter requestedIron = new DroneFilter(new ItemStack(Items.IRON_INGOT), null);
        drone.addPickup(requestedIron);
        drone.addPosition(requesterPoint);
        drone.addUnload();
        drone.addPosition(dockPoint);
        drone.addDock();
        level.addFreshEntity(drone);

        helper.startSequence()
                // EntityRequestDrone keeps the legacy fixed five-tick pickup and unload
                // pauses, plus a zero-motion tick before every following program action.
                // Allow its complete two-station return to finish without making timing a
                // modern gameplay constraint of the test itself.
                .thenIdle(90)
                .thenExecute(() -> {
                    require(helper, provider.items().getStackInSlot(0).isEmpty(),
                            "provider cargo was picked up by request drone");
                    ItemStack delivered = requester.items().getStackInSlot(9);
                    require(helper, delivered.is(Items.IRON_INGOT) && delivered.getCount() == 12,
                            "requester received the complete requested stack");
                    ItemStack returnedDrone = dock.items().getStackInSlot(0);
                    require(helper, returnedDrone.is(ModItems.DRONE.get())
                                    && DroneItem.typeOf(returnedDrone) == DroneItem.DroneType.REQUEST,
                            "request drone was returned to the dock inventory");
                    require(helper, drone.isRemoved(), "returning request drone was discarded only after dock handoff");
                })
                .thenSucceed();
    }

    /**
     * TileEntityDroneDock's own 20-tick announcement/dispatch path must consume a request
     * drone only after it has discovered the full waypoint-mediated route. The direct
     * entity-program test above owns the subsequent three-leg pickup/unload/return contract;
     * keeping that execution check separate keeps the announcement assertion compact and deterministic.
     */
    // GameTest packs templates into 15x15 cells; the request-network fixture itself
    // uses an isolated forced lane so its legacy discovery budget is deterministic.
    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "drone_logistics", batch = "droneDockDispatch", timeoutTicks = 120)
    public static void dockDispatchesAcrossRequestWaypointAndRecoversDrone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // RequestNetwork discovers only five new local nodes per announcement.  Its
        // source contract must not be changed to compensate for unrelated tests
        // publishing nodes in the packed GameTest cells, so use an isolated chunk lane.
        BlockPos fixtureBase = helper.absolutePos(new BlockPos(0, DISPATCH_FIXTURE_Y, 280_000));
        // The route remains the same three stations linked only through the waypoint.
        BlockPos dockPos = fixtureBase.offset(2, 0, 2);
        BlockPos providerPos = fixtureBase.offset(7, 0, 2);
        BlockPos requesterPos = fixtureBase.offset(12, 0, 2);
        BlockPos waypointPos = fixtureBase.offset(7, 0, 6);
        setFixtureChunkForced(level, dockPos, true);
        setFixtureChunkForced(level, providerPos, true);
        setFixtureChunkForced(level, requesterPos, true);
        setFixtureChunkForced(level, waypointPos, true);
        clearDispatchFixtureAir(level, fixtureBase);
        level.setBlock(dockPos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(providerPos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(requesterPos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(dockPos, ModBlocks.DRONE_DOCK.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(providerPos, ModBlocks.DRONE_CRATE_PROVIDER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(requesterPos, ModBlocks.DRONE_CRATE_REQUESTER.get().defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(waypointPos.below(), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(waypointPos, ModBlocks.DRONE_WAYPOINT_REQUEST.get().defaultBlockState(), Block.UPDATE_ALL);
        // Placement through the actual block state and BE path matches the legacy
        // support/survival contract without relying on a synthetic structure BE tag.
        if (!(level.getBlockEntity(dockPos) instanceof DroneLogisticsBlockEntity dock)
                || !(level.getBlockEntity(providerPos) instanceof DroneLogisticsBlockEntity provider)
                || !(level.getBlockEntity(requesterPos) instanceof DroneLogisticsBlockEntity requester)) {
            throw new AssertionError("Dock dispatch fixture did not create all logistics block entities");
        }

        dock.items().setStackInSlot(0, DroneItem.withType(new ItemStack(ModItems.DRONE.get()),
                DroneItem.DroneType.REQUEST));
        provider.items().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 12));
        requester.setFilter(0, new ItemStack(Items.IRON_INGOT));

        helper.startSequence()
                // A node's legacy PathNode copied the owner tile's prior reachable set.
                // With four hosts, the first announcement only publishes nodes, the
                // second can give endpoint nodes their waypoint edge. GameTest runs its
                // sequence before the BlockEntity phase of the matching tick, so retain
                // one more source-shaped 20-tick announcement for the dock to observe
                // that converged three-leg snapshot and embark the drone.
                // GameTest sequences execute before that tick's block-entity phase.
                // Wait through the following 20-tick dock cadence as well, so this
                // assertion observes the source-backed dispatch rather than the
                // already-converged route one tick before the dock consumes a drone.
                .thenIdle(99)
                .thenExecute(() -> {
                    require(helper, hasFixtureHosts(level, dockPos, providerPos, requesterPos, waypointPos),
                            "request-network fixture host was cleared before its third announcement; "
                                    + dispatchDiagnostics(level, dockPos, providerPos, requesterPos, waypointPos));
                    require(helper, hasFixtureNodes(level, dockPos, providerPos, requesterPos, waypointPos),
                            "request-network fixture hosts propagated all three legacy 20-tick cadences; "
                                    + dispatchDiagnostics(level, dockPos, providerPos, requesterPos, waypointPos));
                })
                // This third dock announcement is the dispatch boundary. Every candidate
                // list has one source-backed entry, and the direct entity test owns its
                // later pickup/unload/return timeline. Do not keep this compact template
                // alive beyond this immediate verification.
                .thenExecute(() -> {
                    require(helper, hasFixtureHosts(level, dockPos, providerPos, requesterPos, waypointPos),
                            "request-network fixture host was cleared before dock dispatch; "
                                    + dispatchDiagnostics(level, dockPos, providerPos, requesterPos, waypointPos));
                    // The hosts intentionally sit above the template's ground-level
                    // barrier, so query the actual fixture volume instead of the
                    // template's y=0..16 cell.
                    net.minecraft.world.phys.AABB fixtureBounds = new net.minecraft.world.phys.AABB(
                            dockPos.offset(-2, -2, -2), requesterPos.offset(2, 8, 8));
                    require(helper, dock.items().getStackInSlot(0).isEmpty(),
                            "dock consumed its request drone only after discovering the route; "
                                    + dispatchDiagnostics(level, dockPos, providerPos, requesterPos, waypointPos));
                    require(helper, level.getEntitiesOfClass(RequestDroneEntity.class, fixtureBounds).stream()
                                    .anyMatch(entity -> !entity.isRemoved()),
                            "dock spawned a request drone for the discovered waypoint route; "
                                    + dispatchDiagnostics(level, dockPos, providerPos, requesterPos, waypointPos));
                })
                .thenSucceed();
    }

    private static void setFixtureChunkForced(ServerLevel level, BlockPos pos, boolean forced) {
        level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, forced);
    }

    /** Generated terrain must not become an unintentional obstruction for legacy bidirectional LOS. */
    private static void clearDispatchFixtureAir(ServerLevel level, BlockPos fixtureBase) {
        for (BlockPos pos : BlockPos.betweenClosed(fixtureBase.offset(-2, -2, -2), fixtureBase.offset(16, 12, 10))) {
            level.removeBlock(pos, false);
        }
    }

    /** Keeps failures inside Forge's GameTest reporting path rather than crashing the server tick loop. */
    private static void require(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }

    private static void assertEmpty(ItemStack stack, String message) {
        assertTrue(stack.isEmpty(), message);
    }

    /** Failure-only snapshot for the source-backed dock announcement and three-leg BFS state. */
    private static String dispatchDiagnostics(ServerLevel level, BlockPos dockPos, BlockPos providerPos,
            BlockPos requesterPos, BlockPos waypointPos) {
        var network = com.hbm.ntm.drone.DroneLogisticsNetwork.forLevel(level);
        var dock = network.node(dockPos.above());
        var provider = network.node(providerPos.above());
        var requester = network.node(requesterPos.above());
        java.util.Set<Long> snapshot = new java.util.HashSet<>();
        for (var node : network.localNodes(dockPos, 5)) snapshot.add(node.pos().asLong());
        String paths = dock == null || provider == null || requester == null ? "unavailable"
                : "dockToOffer=" + network.findPath(dock, provider, snapshot)
                + ", offerToRequest=" + network.findPath(provider, requester, snapshot)
                + ", requestToDock=" + network.findPath(requester, dock, snapshot);
        return "gameTime=" + level.getGameTime() + " nodes={dock=" + describeNode(network.node(dockPos.above()))
                + ", provider=" + describeNode(network.node(providerPos.above()))
                + ", requester=" + describeNode(network.node(requesterPos.above()))
                + ", waypoint=" + describeNode(network.node(waypointPos.above(5))) + "} hosts={dock="
                + describeHost(level, dockPos) + ", provider=" + describeHost(level, providerPos)
                + ", requester=" + describeHost(level, requesterPos) + ", waypoint="
                + describeHost(level, waypointPos) + "} dockStack=" + dockStack(dockPos, level) + " paths={" + paths + "}";
    }

    private static String dockStack(BlockPos dockPos, ServerLevel level) {
        if (level.getBlockEntity(dockPos) instanceof DroneLogisticsBlockEntity dock) {
            ItemStack stack = dock.items().getStackInSlot(0);
            return stack + "/" + DroneItem.typeOf(stack);
        }
        return "missing";
    }

    private static boolean hasFixtureNodes(ServerLevel level, BlockPos dockPos, BlockPos providerPos,
            BlockPos requesterPos, BlockPos waypointPos) {
        var network = com.hbm.ntm.drone.DroneLogisticsNetwork.forLevel(level);
        return network.node(dockPos.above()) != null
                && network.node(providerPos.above()) != null
                && network.node(requesterPos.above()) != null
                && network.node(waypointPos.above(5)) != null;
    }

    private static boolean hasFixtureHosts(ServerLevel level, BlockPos dockPos, BlockPos providerPos,
            BlockPos requesterPos, BlockPos waypointPos) {
        return level.getBlockEntity(dockPos) instanceof DroneLogisticsBlockEntity dock
                && dock.kind() == DroneLogisticsBlockEntity.Kind.DOCK
                && level.getBlockEntity(providerPos) instanceof DroneLogisticsBlockEntity provider
                && provider.kind() == DroneLogisticsBlockEntity.Kind.PROVIDER
                && level.getBlockEntity(requesterPos) instanceof DroneLogisticsBlockEntity requester
                && requester.kind() == DroneLogisticsBlockEntity.Kind.REQUESTER
                && level.getBlockEntity(waypointPos) instanceof com.hbm.ntm.blockentity.DroneRequestWaypointBlockEntity;
    }

    private static String describeNode(com.hbm.ntm.drone.DroneLogisticsNetwork.Node node) {
        return node == null ? "missing" : node.kind() + " active=" + node.active()
                + " offers=" + node.offer().size() + " requests=" + node.request().size()
                + " reachable=" + node.reachable();
    }

    private static String describeHost(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        var blockEntity = level.getBlockEntity(pos);
        return state.getBlock() + "/" + (blockEntity == null ? "missing" : blockEntity.getClass().getSimpleName())
                + " loaded=" + level.hasChunkAt(pos);
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
