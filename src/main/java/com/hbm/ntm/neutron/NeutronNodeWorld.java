package com.hbm.ntm.neutron;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class NeutronNodeWorld {
    private static final Map<ResourceKey<Level>, StreamWorld> STREAM_WORLDS = new HashMap<>();

    private NeutronNodeWorld() {
    }

    public static NeutronNode getNode(Level level, BlockPos pos) {
        StreamWorld streamWorld = STREAM_WORLDS.get(level.dimension());
        return streamWorld == null ? null : streamWorld.getNode(pos);
    }

    public static void removeNode(Level level, BlockPos pos) {
        StreamWorld streamWorld = STREAM_WORLDS.get(level.dimension());
        if (streamWorld != null) {
            streamWorld.removeNode(pos);
        }
    }

    public static StreamWorld getOrAddWorld(Level level) {
        return STREAM_WORLDS.computeIfAbsent(level.dimension(), ignored -> new StreamWorld());
    }

    public static void removeAllWorlds() {
        STREAM_WORLDS.clear();
    }

    public static void removeEmptyWorlds() {
        STREAM_WORLDS.values().removeIf(StreamWorld::isStreamListEmpty);
    }

    public static void unloadLevel(Level level) {
        STREAM_WORLDS.remove(level.dimension());
    }

    public static void unloadChunk(Level level, ChunkPos chunkPos) {
        StreamWorld streamWorld = STREAM_WORLDS.get(level.dimension());
        if (streamWorld != null) {
            streamWorld.unloadChunk(chunkPos);
        }
    }

    public static Diagnostics getDiagnostics(Level level) {
        StreamWorld streamWorld = STREAM_WORLDS.get(level.dimension());
        return streamWorld == null ? Diagnostics.empty() : streamWorld.getDiagnostics();
    }

    static StreamWorld getWorld(Level level) {
        return STREAM_WORLDS.get(level.dimension());
    }

    public static final class StreamWorld {
        private final List<NeutronStream> streams = new ArrayList<>();
        private final Map<BlockPos, NeutronNode> nodeCache = new HashMap<>();

        public void runStreamInteractions(Level level) {
            for (NeutronStream stream : List.copyOf(streams)) {
                stream.runStreamInteraction(level, this);
            }
        }

        public void addStream(NeutronStream stream) {
            streams.add(stream);
        }

        public void removeAllStreams() {
            streams.clear();
        }

        public void cleanNodes() {
            List<BlockPos> toRemove = new ArrayList<>();
            nodeCache.values().removeIf(node -> node == null || !node.isValid());
            for (NeutronNode node : nodeCache.values()) {
                toRemove.addAll(node.collectStaleNodePositions(this));
            }
            for (BlockPos pos : toRemove) {
                nodeCache.remove(pos);
            }
        }

        public NeutronNode getNode(BlockPos pos) {
            return nodeCache.get(pos);
        }

        public void addNode(NeutronNode node) {
            nodeCache.put(node.getPos(), node);
        }

        public void removeNode(BlockPos pos) {
            nodeCache.remove(pos);
        }

        public void removeAllStreamsOfType(NeutronType type) {
            for (Iterator<NeutronStream> iterator = streams.iterator(); iterator.hasNext();) {
                if (iterator.next().getType() == type) {
                    iterator.remove();
                }
            }
        }

        public void unloadChunk(ChunkPos chunkPos) {
            nodeCache.entrySet().removeIf(entry -> new ChunkPos(entry.getKey()).equals(chunkPos));
            streams.removeIf(stream -> new ChunkPos(stream.getOrigin().getPos()).equals(chunkPos));
        }

        public boolean isStreamListEmpty() {
            return streams.isEmpty();
        }

        public Diagnostics getDiagnostics() {
            int rbmkNodes = 0;
            int pileNodes = 0;
            int rbmkStreams = 0;
            int pileStreams = 0;
            for (NeutronNode node : nodeCache.values()) {
                if (node.getType() == NeutronType.RBMK) {
                    rbmkNodes++;
                } else if (node.getType() == NeutronType.PILE) {
                    pileNodes++;
                }
            }
            for (NeutronStream stream : streams) {
                if (stream.getType() == NeutronType.RBMK) {
                    rbmkStreams++;
                } else if (stream.getType() == NeutronType.PILE) {
                    pileStreams++;
                }
            }
            return new Diagnostics(streams.size(), nodeCache.size(), rbmkStreams, pileStreams, rbmkNodes, pileNodes);
        }
    }

    public record Diagnostics(int streams, int nodes, int rbmkStreams, int pileStreams, int rbmkNodes, int pileNodes) {
        private static Diagnostics empty() {
            return new Diagnostics(0, 0, 0, 0, 0, 0);
        }
    }
}
