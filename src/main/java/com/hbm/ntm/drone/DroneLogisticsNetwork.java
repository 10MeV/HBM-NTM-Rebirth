package com.hbm.ntm.drone;

import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Server-only lease registry for request-network nodes.  This replaces the legacy static
 * World map and wall-clock expiry while retaining its 20-tick announcements, 24 block LOS,
 * 5-chunk neighborhood and waypoint-only endpoint rule.
 */
public final class DroneLogisticsNetwork {
    public static final int MAX_RANGE = 24;
    /** Legacy RequestNetwork.maxAge was 2,000 ms; use the equivalent two game seconds, not 2,000 ticks. */
    public static final int LEASE_TICKS = 40;
    private static final Map<ServerLevel, DroneLogisticsNetwork> NETWORKS = new WeakHashMap<>();

    private final Map<Long, Node> nodes = new HashMap<>();

    public static DroneLogisticsNetwork forLevel(ServerLevel level) {
        return NETWORKS.computeIfAbsent(level, ignored -> new DroneLogisticsNetwork());
    }

    /**
     * Modern server-tick carrier for RequestNetwork#updateEntries.  Lease expiry must not
     * wait for another node announcement: after a request-network chunk unloads, an active
     * dock may otherwise keep routing through its obsolete nodes until it happens to publish.
     */
    public static void tickLeaseExpiry(ServerLevel level) {
        DroneLogisticsNetwork network = NETWORKS.get(level);
        if (network != null) {
            network.cleanup(level.getGameTime());
        }
    }

    public Node publish(ServerLevel level, BlockPos pos, NodeKind kind, boolean active,
            List<ItemStack> offer, List<DroneFilter> request) {
        long now = level.getGameTime();
        cleanup(now);
        Node node = new Node(pos.immutable(), kind, active, now + LEASE_TICKS, offer, request, nodes.get(pos.asLong()));
        nodes.put(pos.asLong(), node);
        refreshConnections(level, node);
        return node;
    }

    public Node node(BlockPos pos) {
        return nodes.get(pos.asLong());
    }

    public Collection<Node> localNodes(BlockPos center, int chunkRadius) {
        int centerChunkX = center.getX() >> 4;
        int centerChunkZ = center.getZ() >> 4;
        List<Node> local = new ArrayList<>();
        for (Node node : nodes.values()) {
            // TileEntityRequestNetwork#getAllLocalNodes iterated every full chunk in an
            // inclusive (2r+1) × (2r+1) square.  Block-distance clipping loses part of
            // edge chunks when the source node is near a chunk boundary.
            if (Math.abs((node.pos.getX() >> 4) - centerChunkX) <= chunkRadius
                    && Math.abs((node.pos.getZ() >> 4) - centerChunkZ) <= chunkRadius) {
                local.add(node);
            }
        }
        local.sort(Comparator.comparingLong(node -> node.pos.asLong()));
        return local;
    }

    /** Legacy depth-10 breadth-first route, returned without the start and including the end. */
    public List<BlockPos> findPath(Node start, Node end, Set<Long> localNodePositions) {
        if (start == null || end == null) return null;
        // TileEntityDroneDock passed one dock-centered 5-chunk local-node snapshot into
        // every generatePath call.  In particular, offer -> request and request -> dock
        // must not rebuild a larger/different neighbourhood around their intermediate node.
        List<List<BlockPos>> paths = new ArrayList<>();
        paths.add(List.of(start.pos));
        depthLoop: for (int depth = 0; depth < 10; depth++) {
            List<List<BlockPos>> nextPaths = new ArrayList<>();
            int brake = 1_000;
            for (List<BlockPos> path : paths) {
                Node current = nodes.get(path.get(path.size() - 1).asLong());
                if (current == null) continue;
                for (BlockPos reachable : current.reachable) {
                    // TileEntityDroneDock#generatePath intentionally permits repeated
                    // nodes in a path.  Its only guard is the 1,000-edge brake per depth;
                    // pruning cycles here changes which route can be found within depth 10.
                    if (localNodePositions.contains(reachable.asLong())) {
                        List<BlockPos> next = new ArrayList<>(path);
                        if (reachable.equals(end.pos)) {
                            next.remove(0);
                            return next;
                        }
                        next.add(reachable);
                        nextPaths.add(next);
                    }
                    // The legacy labelled continue discards the remainder of this breadth
                    // layer once its emergency budget is exhausted, then advances depth.
                    if (--brake <= 0) continue depthLoop;
                }
            }
            paths = nextPaths;
        }
        return null;
    }

    private void refreshConnections(ServerLevel level, Node node) {
        Map<Long, Node> local = new HashMap<>();
        for (Node candidate : localNodes(node.pos, 2)) local.put(candidate.pos.asLong(), candidate);

        // TileEntityRequestNetwork retained known nodes, pruned departed ones, rescanned all
        // retained LOS edges, then admitted at most five *new* neighbours each second.
        node.known.removeIf(known -> {
            Node candidate = local.get(known.asLong());
            // Legacy PathNode equality was position-only: a node type replacement at an
            // existing coordinate stayed known until its lease disappeared.  Connectability
            // constrained discovery only, not this retained-edge rescan.
            return candidate == null || candidate.pos.equals(node.pos);
        });
        node.reachable.retainAll(node.known);
        for (BlockPos known : node.known) {
            if (hasLineOfSight(level, node.pos, known)) node.reachable.add(known);
            else node.reachable.remove(known);
        }

        int newNodeLimit = 5;
        for (Node candidate : local.values()) {
            if (candidate.pos.equals(node.pos) || !connectable(node, candidate) || node.known.contains(candidate.pos)) continue;
            node.known.add(candidate.pos.immutable());
            if (hasLineOfSight(level, node.pos, candidate.pos)) node.reachable.add(candidate.pos.immutable());
            if (--newNodeLimit <= 0) break;
        }

        // TileEntityRequestNetwork only emitted a drone debug line when the edge was
        // reachable. Its nested colour ternary was therefore always green in practice;
        // do not add modern-only red lines for known-but-blocked candidates.
        for (BlockPos known : node.known) {
            boolean reachable = node.reachable.contains(known);
            if (!reachable) {
                continue;
            }
            ParticleUtil.spawnDroneLine(level, node.pos.getX() + 0.5D, node.pos.getY() + 0.5D,
                    node.pos.getZ() + 0.5D, (known.getX() - node.pos.getX()) / 2.0D,
                    (known.getY() - node.pos.getY()) / 2.0D,
                    (known.getZ() - node.pos.getZ()) / 2.0D, 0x00FF00);
        }
    }

    private void cleanup(long now) {
        nodes.values().removeIf(node -> node.leaseUntil < now);
    }

    private static boolean connectable(Node first, Node second) {
        return first.kind.waypoint || second.kind.waypoint;
    }

    public static boolean hasLineOfSight(ServerLevel level, BlockPos first, BlockPos second) {
        Vec3 start = Vec3.atCenterOf(first);
        Vec3 end = Vec3.atCenterOf(second);
        if (start.distanceTo(end) > MAX_RANGE) {
            return false;
        }
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult forward = level.clip(context);
        BlockHitResult backward = level.clip(new ClipContext(end, start, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, null));
        return forward.getType() == net.minecraft.world.phys.HitResult.Type.MISS
                && backward.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }

    public enum NodeKind {
        WAYPOINT(true), DOCK(false), PROVIDER(false), REQUESTER(false);
        private final boolean waypoint;
        NodeKind(boolean waypoint) { this.waypoint = waypoint; }
    }

    public static final class Node {
        private final BlockPos pos;
        private final NodeKind kind;
        private final boolean active;
        private final long leaseUntil;
        private final List<ItemStack> offer;
        private final List<DroneFilter> request;
        private final Set<BlockPos> known = new HashSet<>();
        private final Set<BlockPos> reachable = new HashSet<>();

        private Node(BlockPos pos, NodeKind kind, boolean active, long leaseUntil,
                List<ItemStack> offer, List<DroneFilter> request, Node previous) {
            this.pos = pos;
            this.kind = kind;
            this.active = active;
            this.leaseUntil = leaseUntil;
            this.offer = offer.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
            this.request = List.copyOf(request);
            if (previous != null) {
                known.addAll(previous.known);
                reachable.addAll(previous.reachable);
            }
        }

        public BlockPos pos() { return pos; }
        public NodeKind kind() { return kind; }
        public boolean active() { return active; }
        public List<ItemStack> offer() { return offer; }
        public List<DroneFilter> request() { return request; }
        public Set<BlockPos> reachable() { return Set.copyOf(reachable); }
    }
}
