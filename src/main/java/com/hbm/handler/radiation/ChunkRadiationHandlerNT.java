package com.hbm.handler.radiation;

import com.hbm.blocks.IRadResistantBlock;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.fauxpointtwelve.ChunkCoordIntPair;
import com.hbm.util.fauxpointtwelve.ForgeDirection;
import com.hbm.util.fauxpointtwelve.NBTTagCompound;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Legacy NT radiation pocket handler. It preserves the 1.7.10 pocket graph and
 * chunk NBT shape as an optional old-package handler; it is not selected by the
 * modern default Simple runtime.
 */
@Deprecated(forRemoval = false)
public class ChunkRadiationHandlerNT extends ChunkRadiationHandler {
    private static final String NBT_KEY_ROOT = "hbmRadDataNT";
    private static final String NBT_KEY_CHUNK_DATA = "chunkRadData";
    private static final String NBT_KEY_SECTION_COUNT = "sectionCount";

    private static Map<Level, WorldRadiationData> worldMap = new HashMap<>();
    private static RadPocket[] pocketsByBlock = null;
    private static Queue<BlockPos> stack = new ArrayDeque<>(1024);

    @Override
    public void clearSystem(Level level) {
        WorldRadiationData radWorld = worldMap.get(level);

        if (radWorld != null) {
            radWorld.data.clear();
            radWorld.activePockets.clear();
            radWorld.dirtyChunks.clear();
            radWorld.dirtyChunks2.clear();
        }
    }

    @Override
    public void incrementRad(Level level, int x, int y, int z, float rad) {
        if (!blockExists(level, x, y, z)) {
            return;
        }

        RadPocket p = getPocket(level, x, y, z);
        p.radiation += rad;

        if (rad > 0) {
            WorldRadiationData data = getWorldRadData(level);
            data.activePockets.add(p);
        }
    }

    @Override
    public void decrementRad(Level level, int x, int y, int z, float rad) {
        if (!isInBuildHeight(level, y) || !isSubChunkLoaded(level, x, y, z)) {
            return;
        }

        RadPocket p = getPocket(level, x, y, z);
        p.radiation -= Math.max(rad, 0);
        if (p.radiation < 0) {
            p.radiation = 0;
        }
    }

    @Override
    public void setRadiation(Level level, int x, int y, int z, float rad) {
        if (!isInBuildHeight(level, y)) {
            return;
        }
        RadPocket p = getPocket(level, x, y, z);
        p.radiation = Math.max(rad, 0);

        if (rad > 0) {
            WorldRadiationData data = getWorldRadData(level);
            data.activePockets.add(p);
        }
    }

    @Override
    public float getRadiation(Level level, int x, int y, int z) {
        if (!isSubChunkLoaded(level, x, y, z)) {
            return 0;
        }
        return getPocket(level, x, y, z).radiation;
    }

    public static void jettisonData(Level level) {
        WorldRadiationData data = getWorldRadData(level);
        data.data.clear();
        data.activePockets.clear();
    }

    public static RadPocket getPocket(Level level, int x, int y, int z) {
        return getSubChunkStorage(level, x, y, z).getPocket(x, y, z);
    }

    public static Collection<RadPocket> getActiveCollection(Level level) {
        return getWorldRadData(level).activePockets;
    }

    public static boolean isSubChunkLoaded(Level level, int x, int y, int z) {
        if (!isInBuildHeight(level, y)) {
            return false;
        }

        WorldRadiationData worldRadData = worldMap.get(level);
        if (worldRadData == null) {
            return false;
        }
        ChunkRadiationStorage st = worldRadData.data.get(new ChunkCoordIntPair(x >> 4, z >> 4));
        if (st == null) {
            return false;
        }
        SubChunkRadiationStorage sc = st.getForYLevel(y);
        if (sc == null) {
            return false;
        }
        return true;
    }

    public static SubChunkRadiationStorage getSubChunkStorage(Level level, int x, int y, int z) {
        ChunkRadiationStorage st = getChunkStorage(level, x, y, z);
        SubChunkRadiationStorage sc = st.getForYLevel(y);
        if (sc == null) {
            rebuildChunkPockets(level.getChunk(x >> 4, z >> 4), sectionY(y));
        }
        sc = st.getForYLevel(y);
        return sc;
    }

    public static ChunkRadiationStorage getChunkStorage(Level level, int x, int y, int z) {
        WorldRadiationData worldRadData = getWorldRadData(level);
        ChunkRadiationStorage st = worldRadData.data.get(new ChunkCoordIntPair(x >> 4, z >> 4));
        if (st == null) {
            st = new ChunkRadiationStorage(worldRadData, level.getChunk(x >> 4, z >> 4));
            worldRadData.data.put(new ChunkCoordIntPair(x >> 4, z >> 4), st);
        }
        return st;
    }

    private static WorldRadiationData getWorldRadData(Level level) {
        WorldRadiationData worldRadData = worldMap.get(level);
        if (worldRadData == null) {
            worldRadData = new WorldRadiationData(level);
            worldMap.put(level, worldRadData);
        }
        return worldRadData;
    }

    @Override
    public void updateSystem() {
        updateRadiation();
    }

    @Override
    public void receiveWorldTick(TickEvent.ServerTickEvent event) {
        rebuildDirty();
    }

    @Override
    public void receiveChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            WorldRadiationData data = getWorldRadData(level);
            ChunkCoordIntPair pos = legacyPos(event.getChunk().getPos());
            if (data.data.containsKey(pos)) {
                data.data.get(pos).unload();
                data.data.remove(pos);
            }
        }
    }

    @Override
    public void receiveChunkLoad(ChunkDataEvent.Load event) {
        if (event.getChunk().getWorldForge() instanceof Level level && !level.isClientSide()) {
            NBTTagCompound dataTag = NBTTagCompound.copyOf(event.getData());
            if (dataTag.hasKey(NBT_KEY_ROOT)) {
                WorldRadiationData data = getWorldRadData(level);
                ChunkRadiationStorage cData = new ChunkRadiationStorage(data, levelChunk(level, event.getChunk()));
                cData.readFromNBT(dataTag.getCompoundTag(NBT_KEY_ROOT));
                data.data.put(legacyPos(event.getChunk().getPos()), cData);
            }
        }
    }

    @Override
    public void receiveChunkSave(ChunkDataEvent.Save event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            WorldRadiationData data = getWorldRadData(level);
            ChunkCoordIntPair pos = legacyPos(event.getChunk().getPos());
            if (data.data.containsKey(pos)) {
                NBTTagCompound tag = new NBTTagCompound();
                data.data.get(pos).writeToNBT(tag);
                event.getData().put(NBT_KEY_ROOT, tag);
            }
        }
    }

    @Override
    public void receiveWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            worldMap.put(level, new WorldRadiationData(level));
        }
    }

    @Override
    public void receiveWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            worldMap.remove(level);
        }
    }

    public static void updateRadiation() {
        long time = System.currentTimeMillis();
        for (WorldRadiationData w : worldMap.values()) {
            List<RadPocket> itrActive = new ArrayList<>(w.activePockets);
            Iterator<RadPocket> itr = itrActive.iterator();
            while (itr.hasNext()) {
                RadPocket p = itr.next();
                BlockPos pos = p.parent.parent.getWorldPos(p.parent.yLevel);

                p.radiation *= 0.999F;
                p.radiation -= 0.05F;
                p.parent.parent.chunk.setUnsaved(true);
                if (p.radiation <= 0) {
                    p.radiation = 0;
                    p.accumulatedRads = 0;
                    itr.remove();
                    p.parent.parent.chunk.setUnsaved(true);
                    continue;
                }

                float count = 0;
                for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                    count += p.connectionIndices[e.ordinal()].size();
                }
                float amountPer = 0.7F / count;
                if (count == 0 || p.radiation < 1) {
                    amountPer = 0;
                }
                if (p.radiation > 0 && amountPer > 0) {
                    for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                        BlockPos nPos = pos.offset(e, 16);
                        if (!blockExists(p.parent.parent.chunk.getLevel(), nPos.getX(), nPos.getY(), nPos.getZ())
                                || !isInBuildHeight(p.parent.parent.chunk.getLevel(), nPos.getY())) {
                            continue;
                        }
                        if (p.connectionIndices[e.ordinal()].size() == 1
                                && p.connectionIndices[e.ordinal()].get(0) == -1) {
                            rebuildChunkPockets(p.parent.parent.chunk.getLevel().getChunk(nPos.getX() >> 4,
                                    nPos.getZ() >> 4), nPos.getY() >> 4);
                        } else {
                            SubChunkRadiationStorage sc2 = getSubChunkStorage(p.parent.parent.chunk.getLevel(),
                                    nPos.getX(), nPos.getY(), nPos.getZ());
                            for (int idx : p.connectionIndices[e.ordinal()]) {
                                sc2.pockets[idx].accumulatedRads += p.radiation * amountPer;
                                w.activePockets.add(sc2.pockets[idx]);
                            }
                        }
                    }
                }
                if (amountPer != 0) {
                    p.accumulatedRads += p.radiation * 0.3F;
                }
                if (System.currentTimeMillis() - time > 20) {
                    break;
                }
            }
            itr = w.activePockets.iterator();
            while (itr.hasNext()) {
                RadPocket p = itr.next();
                p.radiation = p.accumulatedRads;
                p.accumulatedRads = 0;
                if (p.radiation <= 0) {
                    itr.remove();
                }
            }
        }
        if (System.currentTimeMillis() - time > 50) {
            System.out.println("Rads took too long: " + (System.currentTimeMillis() - time));
        }
    }

    public static void markChunkForRebuild(Level level, int x, int y, int z) {
        if (!isInBuildHeight(level, y)) {
            return;
        }
        BlockPos chunkPos = new BlockPos(x >> 4, sectionY(y), z >> 4);
        WorldRadiationData r = getWorldRadData(level);

        if (r.iteratingDirty) {
            r.dirtyChunks2.add(chunkPos);
        } else {
            r.dirtyChunks.add(chunkPos);
        }
    }

    private static void rebuildDirty() {
        for (WorldRadiationData r : worldMap.values()) {
            r.iteratingDirty = true;
            for (BlockPos b : r.dirtyChunks) {
                rebuildChunkPockets(r.world.getChunk(b.getX(), b.getZ()), b.getY());
            }
            r.iteratingDirty = false;
            r.dirtyChunks.clear();
            r.dirtyChunks.addAll(r.dirtyChunks2);
            r.dirtyChunks2.clear();
        }
    }

    private static void rebuildChunkPockets(LevelChunk chunk, int sectionY) {
        if (!isSectionInBuildHeight(chunk.getLevel(), sectionY)) {
            return;
        }
        BlockPos subChunkPos = new BlockPos(chunk.getPos().x << 4, sectionY << 4, chunk.getPos().z << 4);
        List<RadPocket> pockets = new ArrayList<>();
        LevelChunkSection blocks = legacySection(chunk, sectionY);
        if (pocketsByBlock == null) {
            pocketsByBlock = new RadPocket[16 * 16 * 16];
        } else {
            Arrays.fill(pocketsByBlock, null);
        }
        ChunkRadiationStorage st = getChunkStorage(chunk.getLevel(), subChunkPos.getX(), subChunkPos.getY(),
                subChunkPos.getZ());
        SubChunkRadiationStorage subChunk = new SubChunkRadiationStorage(st, subChunkPos.getY(), null, null);

        if (blocks != null) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (pocketsByBlock[x * 16 * 16 + y * 16 + z] != null) {
                            continue;
                        }
                        Block block = blocks.getBlockState(x, y, z).getBlock();
                        if (!isRadResistant(block)) {
                            pockets.add(buildPocket(subChunk, chunk.getLevel(), new BlockPos(x, y, z),
                                    subChunkPos, blocks, pocketsByBlock, pockets.size()));
                        }
                    }
                }
            }
        } else {
            RadPocket pocket = new RadPocket(subChunk, 0);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    doEmptyChunk(chunk, subChunkPos, new BlockPos(x, 0, y), pocket, ForgeDirection.DOWN);
                    doEmptyChunk(chunk, subChunkPos, new BlockPos(x, 15, y), pocket, ForgeDirection.UP);
                    doEmptyChunk(chunk, subChunkPos, new BlockPos(x, y, 0), pocket, ForgeDirection.NORTH);
                    doEmptyChunk(chunk, subChunkPos, new BlockPos(x, y, 15), pocket, ForgeDirection.SOUTH);
                    doEmptyChunk(chunk, subChunkPos, new BlockPos(0, y, x), pocket, ForgeDirection.WEST);
                    doEmptyChunk(chunk, subChunkPos, new BlockPos(15, y, x), pocket, ForgeDirection.EAST);
                }
            }
            pockets.add(pocket);
        }
        subChunk.pocketsByBlock = pockets.size() == 1 ? null : pocketsByBlock;
        if (subChunk.pocketsByBlock != null) {
            pocketsByBlock = null;
        }
        subChunk.pockets = pockets.toArray(new RadPocket[pockets.size()]);
        st.setForYLevel(sectionY << 4, subChunk);
    }

    private static void doEmptyChunk(LevelChunk chunk, BlockPos subChunkPos, BlockPos pos, RadPocket pocket,
                                    ForgeDirection facing) {
        BlockPos newPos = pos.offset(facing);
        BlockPos outPos = newPos.add(subChunkPos);
        Block block = blockAt(chunk.getLevel(), outPos.getX(), outPos.getY(), outPos.getZ());
        if (!isRadResistant(block)) {
            if (!isSubChunkLoaded(chunk.getLevel(), outPos.getX(), outPos.getY(), outPos.getZ())) {
                if (!pocket.connectionIndices[facing.ordinal()].contains(-1)) {
                    pocket.connectionIndices[facing.ordinal()].add(-1);
                }
            } else {
                RadPocket outPocket = getPocket(chunk.getLevel(), outPos.getX(), outPos.getY(), outPos.getZ());
                if (!pocket.connectionIndices[facing.ordinal()].contains(Integer.valueOf(outPocket.index))) {
                    pocket.connectionIndices[facing.ordinal()].add(outPocket.index);
                }
            }
        }
    }

    private static RadPocket buildPocket(SubChunkRadiationStorage subChunk, Level level, BlockPos start,
                                         BlockPos subChunkWorldPos, LevelChunkSection chunk,
                                         RadPocket[] pocketsByBlock, int index) {
        RadPocket pocket = new RadPocket(subChunk, index);
        stack.clear();
        stack.add(start);
        while (!stack.isEmpty()) {
            BlockPos pos = stack.poll();
            Block block = chunk.getBlockState(pos.getX(), pos.getY(), pos.getZ()).getBlock();
            if (pocketsByBlock[pos.getX() * 16 * 16 + pos.getY() * 16 + pos.getZ()] != null
                    || isRadResistant(block)) {
                continue;
            }
            pocketsByBlock[pos.getX() * 16 * 16 + pos.getY() * 16 + pos.getZ()] = pocket;
            for (ForgeDirection facing : ForgeDirection.VALID_DIRECTIONS) {
                BlockPos newPos = pos.offset(facing);
                if (Math.max(Math.max(newPos.getX(), newPos.getY()), newPos.getZ()) > 15
                        || Math.min(Math.min(newPos.getX(), newPos.getY()), newPos.getZ()) < 0) {
                    BlockPos outPos = newPos.add(subChunkWorldPos);
                    if (!isInBuildHeight(level, outPos.getY())) {
                        continue;
                    }
                    block = blockAt(level, outPos.getX(), outPos.getY(), outPos.getZ());
                    if (!isRadResistant(block)) {
                        if (!isSubChunkLoaded(level, outPos.getX(), outPos.getY(), outPos.getZ())) {
                            if (!pocket.connectionIndices[facing.ordinal()].contains(-1)) {
                                pocket.connectionIndices[facing.ordinal()].add(-1);
                            }
                        } else {
                            RadPocket outPocket = getPocket(level, outPos.getX(), outPos.getY(), outPos.getZ());
                            if (!pocket.connectionIndices[facing.ordinal()].contains(Integer.valueOf(outPocket.index))) {
                                pocket.connectionIndices[facing.ordinal()].add(outPocket.index);
                            }
                        }
                    }
                    continue;
                }
                stack.add(newPos);
            }
        }
        return pocket;
    }

    private static boolean blockExists(Level level, int x, int y, int z) {
        return isInBuildHeight(level, y) && level.hasChunk(x >> 4, z >> 4);
    }

    private static boolean isRadResistant(Block block) {
        return block instanceof IRadResistantBlock resistant && resistant.getResistance() == 1;
    }

    private static Block blockAt(Level level, int x, int y, int z) {
        if (x < -30000000 || z < -30000000 || x >= 30000000 || z >= 30000000
                || !isInBuildHeight(level, y)) {
            return Blocks.AIR;
        }
        return level.getBlockState(new net.minecraft.core.BlockPos(x, y, z)).getBlock();
    }

    private static LevelChunkSection legacySection(LevelChunk chunk, int sectionY) {
        if (!isSectionInBuildHeight(chunk.getLevel(), sectionY)) {
            throw new ArrayIndexOutOfBoundsException(sectionY);
        }
        int rawSection = chunk.getLevel().getSectionIndexFromSectionY(sectionY);
        LevelChunkSection section = chunk.getSection(rawSection);
        return section.hasOnlyAir() ? null : section;
    }

    private static LevelChunk levelChunk(Level level, ChunkAccess chunk) {
        if (chunk instanceof LevelChunk levelChunk) {
            return levelChunk;
        }
        ChunkPos pos = chunk.getPos();
        return level.getChunk(pos.x, pos.z);
    }

    private static ChunkCoordIntPair legacyPos(ChunkPos pos) {
        return new ChunkCoordIntPair(pos.x, pos.z);
    }

    private static boolean isInBuildHeight(Level level, int y) {
        return y >= level.getMinBuildHeight() && y < level.getMaxBuildHeight();
    }

    private static int sectionY(int blockY) {
        return blockY >> 4;
    }

    private static boolean isSectionInBuildHeight(Level level, int sectionY) {
        int index = WorldUtil.sectionIndex(level, sectionY);
        return index >= 0 && index < level.getSectionsCount();
    }

    public static class RadPocket {
        public SubChunkRadiationStorage parent;
        public int index;
        public float radiation;
        private float accumulatedRads = 0;
        @SuppressWarnings("unchecked")
        public List<Integer>[] connectionIndices = new List[ForgeDirection.VALID_DIRECTIONS.length];

        public RadPocket(SubChunkRadiationStorage parent, int index) {
            this.parent = parent;
            this.index = index;
            for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
                connectionIndices[i] = new ArrayList<>(1);
            }
        }

        protected void remove(Level level, BlockPos pos) {
            for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                connectionIndices[e.ordinal()].clear();
            }
            parent.parent.parent.activePockets.remove(this);
        }

        public BlockPos getSubChunkPos() {
            return parent.parent.getWorldPos(parent.yLevel);
        }
    }

    public static class SubChunkRadiationStorage {
        public ChunkRadiationStorage parent;
        public int yLevel;
        public RadPocket[] pocketsByBlock;
        public RadPocket[] pockets;

        public SubChunkRadiationStorage(ChunkRadiationStorage parent, int yLevel, RadPocket[] pocketsByBlock,
                                        RadPocket[] pockets) {
            this.parent = parent;
            this.yLevel = yLevel;
            this.pocketsByBlock = pocketsByBlock;
            this.pockets = pockets;
        }

        public RadPocket getPocket(int x, int y, int z) {
            if (pocketsByBlock == null) {
                return pockets[0];
            } else {
                x &= 15;
                y &= 15;
                z &= 15;

                RadPocket p = pocketsByBlock[x * 16 * 16 + y * 16 + z];
                return p == null ? pockets[0] : p;
            }
        }

        public void setRad(SubChunkRadiationStorage other) {
            float total = 0;
            for (RadPocket p : other.pockets) {
                total += p.radiation;
            }
            float radPer = total / pockets.length;
            for (RadPocket p : pockets) {
                p.radiation = radPer;
                if (radPer > 0) {
                    p.parent.parent.parent.activePockets.add(p);
                }
            }
        }

        public void remove(Level level, BlockPos pos) {
            for (RadPocket p : pockets) {
                p.remove(level, pos);
            }
            for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                level.getChunk((pos.getX() + 16) >> 4, (pos.getZ() + 16) >> 4);

                BlockPos offPos = pos.offset(e, 16);
                if (isSubChunkLoaded(level, offPos.getX(), offPos.getY(), offPos.getZ())) {
                    SubChunkRadiationStorage sc = getSubChunkStorage(level, offPos.getX(), offPos.getY(),
                            offPos.getZ());
                    for (RadPocket p : sc.pockets) {
                        p.connectionIndices[e.getOpposite().ordinal()].clear();
                    }
                }
            }
        }

        public void add(Level level, BlockPos pos) {
            for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                level.getChunk((pos.getX() + 16) >> 4, (pos.getZ() + 16) >> 4);

                BlockPos offPos = pos.offset(e, 16);
                if (isSubChunkLoaded(level, offPos.getX(), offPos.getY(), offPos.getZ())) {
                    SubChunkRadiationStorage sc = getSubChunkStorage(level, offPos.getX(), offPos.getY(),
                            offPos.getZ());
                    for (RadPocket p : sc.pockets) {
                        p.connectionIndices[e.getOpposite().ordinal()].clear();
                    }
                    for (RadPocket p : pockets) {
                        List<Integer> indc = p.connectionIndices[e.ordinal()];
                        for (int idx : indc) {
                            sc.pockets[idx].connectionIndices[e.getOpposite().ordinal()].add(p.index);
                        }
                    }
                }
            }
        }
    }

    public static class ChunkRadiationStorage {
        private static ByteBuffer buf = ByteBuffer.allocate(8 * 1024 * 1024);

        public WorldRadiationData parent;
        private LevelChunk chunk;
        private SubChunkRadiationStorage[] chunks;

        public ChunkRadiationStorage(WorldRadiationData parent, LevelChunk chunk) {
            this.parent = parent;
            this.chunk = chunk;
            this.chunks = new SubChunkRadiationStorage[chunk.getLevel().getSectionsCount()];
        }

        public SubChunkRadiationStorage getForYLevel(int y) {
            int idx = WorldUtil.sectionIndex(chunk.getLevel(), sectionY(y));
            if (idx < 0 || idx >= chunks.length) {
                return null;
            }
            return chunks[idx];
        }

        public BlockPos getWorldPos(int y) {
            return new BlockPos(chunk.getPos().x << 4, y, chunk.getPos().z << 4);
        }

        public void setForYLevel(int y, SubChunkRadiationStorage sc) {
            int idx = WorldUtil.sectionIndex(chunk.getLevel(), sectionY(y));
            if (idx < 0 || idx >= chunks.length) {
                return;
            }
            if (chunks[idx] != null) {
                chunks[idx].remove(chunk.getLevel(), getWorldPos(y));
                if (sc != null) {
                    sc.setRad(chunks[idx]);
                }
            }
            if (sc != null) {
                sc.add(chunk.getLevel(), getWorldPos(y));
            }
            chunks[idx] = sc;
        }

        public void unload() {
            for (int y = 0; y < chunks.length; y++) {
                if (chunks[y] == null) {
                    continue;
                }
                for (RadPocket p : chunks[y].pockets) {
                    parent.activePockets.remove(p);
                }
                chunks[y] = null;
            }
        }

        public NBTTagCompound writeToNBT(NBTTagCompound tag) {
            tag.setInteger(NBT_KEY_SECTION_COUNT, chunks.length);
            for (SubChunkRadiationStorage st : chunks) {
                if (st == null) {
                    buf.put((byte) 0);
                } else {
                    buf.put((byte) 1);
                    buf.putShort((short) st.yLevel);
                    buf.putShort((short) st.pockets.length);
                    for (RadPocket p : st.pockets) {
                        writePocket(buf, p);
                    }
                    if (st.pocketsByBlock == null) {
                        buf.put((byte) 0);
                    } else {
                        buf.put((byte) 1);
                        for (RadPocket p : st.pocketsByBlock) {
                            buf.putShort(arrayIndex(p, st.pockets));
                        }
                    }
                }
            }
            buf.flip();
            byte[] data = new byte[buf.limit()];
            buf.get(data);
            tag.setByteArray(NBT_KEY_CHUNK_DATA, data);
            buf.clear();
            return tag;
        }

        public short arrayIndex(RadPocket p, RadPocket[] pockets) {
            for (short i = 0; i < pockets.length; i++) {
                if (p == pockets[i]) {
                    return i;
                }
            }
            return -1;
        }

        public void writePocket(ByteBuffer buf, RadPocket p) {
            buf.putInt(p.index);
            buf.putFloat(p.radiation);
            for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                List<Integer> indc = p.connectionIndices[e.ordinal()];
                buf.putShort((short) indc.size());
                for (int idx : indc) {
                    buf.putShort((short) idx);
                }
            }
        }

        public void readFromNBT(NBTTagCompound tag) {
            ByteBuffer data = ByteBuffer.wrap(tag.getByteArray(NBT_KEY_CHUNK_DATA));
            int storedSections = tag.hasKey(NBT_KEY_SECTION_COUNT)
                    ? Math.max(0, tag.getInteger(NBT_KEY_SECTION_COUNT)) : 16;
            for (int i = 0; i < storedSections && data.hasRemaining(); i++) {
                boolean subChunkExists = data.get() == 1 ? true : false;
                if (subChunkExists) {
                    int yLevel = data.getShort();
                    SubChunkRadiationStorage st = new SubChunkRadiationStorage(this, yLevel, null, null);
                    int pocketsLength = data.getShort();
                    st.pockets = new RadPocket[pocketsLength];
                    for (int j = 0; j < pocketsLength; j++) {
                        st.pockets[j] = readPocket(data, st);
                    }
                    boolean perBlockDataExists = data.get() == 1 ? true : false;
                    if (perBlockDataExists) {
                        st.pocketsByBlock = new RadPocket[16 * 16 * 16];
                        for (int j = 0; j < 16 * 16 * 16; j++) {
                            int idx = data.getShort();
                            if (idx >= 0) {
                                st.pocketsByBlock[j] = st.pockets[idx];
                            }
                        }
                    }
                    int sectionIndex = WorldUtil.sectionIndex(chunk.getLevel(), sectionY(yLevel));
                    if (sectionIndex >= 0 && sectionIndex < chunks.length) {
                        chunks[sectionIndex] = st;
                        for (RadPocket pocket : st.pockets) {
                            if (pocket.radiation > 0) {
                                parent.activePockets.add(pocket);
                            }
                        }
                    }
                }
            }
        }

        public RadPocket readPocket(ByteBuffer buf, SubChunkRadiationStorage parent) {
            int index = buf.getInt();
            RadPocket p = new RadPocket(parent, index);
            p.radiation = buf.getFloat();
            for (ForgeDirection e : ForgeDirection.VALID_DIRECTIONS) {
                List<Integer> indc = p.connectionIndices[e.ordinal()];
                int size = buf.getShort();
                for (int i = 0; i < size; i++) {
                    indc.add((int) buf.getShort());
                }
            }
            return p;
        }
    }

    public static class WorldRadiationData {
        public Level world;
        private Set<BlockPos> dirtyChunks = new HashSet<>();
        private Set<BlockPos> dirtyChunks2 = new HashSet<>();
        private boolean iteratingDirty = false;

        public Set<RadPocket> activePockets = new HashSet<>();
        public Map<ChunkCoordIntPair, ChunkRadiationStorage> data = new HashMap<>();

        public WorldRadiationData(Level world) {
            this.world = world;
        }
    }
}
