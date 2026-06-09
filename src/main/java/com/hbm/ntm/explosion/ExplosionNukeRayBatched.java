package com.hbm.ntm.explosion;

import com.hbm.ntm.util.HbmBlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class ExplosionNukeRayBatched implements ExplosionRay {
    public final Map<ChunkPos, List<FloatTriplet>> perChunk = new HashMap<>();
    public final List<ChunkPos> orderedChunks = new ArrayList<>();
    private final CoordComparator comparator = new CoordComparator();
    protected final int posX;
    protected final int posY;
    protected final int posZ;
    protected final Level level;
    protected final int strength;
    protected final int speed;
    protected final int length;
    private final int gspNumMax;
    private int gspNum;
    private double gspX;
    private double gspY;
    private final BiConsumer<Integer, Integer> chunkLoader;
    public boolean isAusf3Complete;

    public ExplosionNukeRayBatched(Level level, int x, int y, int z, int strength, int speed, int length) {
        this(level, x, y, z, strength, speed, length, (chunkX, chunkZ) -> {
        });
    }

    public ExplosionNukeRayBatched(Level level, int x, int y, int z, int strength, int speed, int length,
            BiConsumer<Integer, Integer> chunkLoader) {
        this.level = level;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.strength = strength;
        this.speed = speed;
        this.length = length;
        this.gspNumMax = (int) (2.5D * Math.PI * Math.pow(this.strength, 2.0D));
        this.gspNum = 1;
        this.gspX = Math.PI;
        this.gspY = 0.0D;
        this.chunkLoader = chunkLoader;
    }

    public void saveToNbt(CompoundTag tag, String name) {
        tag.putInt(name + "gspNum", gspNum);
        tag.putDouble(name + "gspX", gspX);
        tag.putDouble(name + "gspY", gspY);
        tag.putBoolean(name + "complete", isAusf3Complete);

        ListTag chunkList = new ListTag();
        for (Map.Entry<ChunkPos, List<FloatTriplet>> entry : perChunk.entrySet()) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", entry.getKey().x);
            chunkTag.putInt("z", entry.getKey().z);
            List<FloatTriplet> tips = entry.getValue();
            int[] tipBits = new int[tips.size() * 3];
            for (int i = 0; i < tips.size(); i++) {
                FloatTriplet tip = tips.get(i);
                int offset = i * 3;
                tipBits[offset] = Float.floatToIntBits(tip.xCoord);
                tipBits[offset + 1] = Float.floatToIntBits(tip.yCoord);
                tipBits[offset + 2] = Float.floatToIntBits(tip.zCoord);
            }
            chunkTag.putIntArray("tips", tipBits);
            chunkList.add(chunkTag);
        }
        tag.put(name + "chunks", chunkList);

        long[] order = new long[orderedChunks.size()];
        for (int i = 0; i < orderedChunks.size(); i++) {
            order[i] = orderedChunks.get(i).toLong();
        }
        tag.putLongArray(name + "order", order);
    }

    public void readFromNbt(CompoundTag tag, String name) {
        if (tag.contains(name + "gspNum")) {
            gspNum = Math.max(1, tag.getInt(name + "gspNum"));
            gspX = tag.getDouble(name + "gspX");
            gspY = tag.getDouble(name + "gspY");
        }
        isAusf3Complete = tag.getBoolean(name + "complete");
        perChunk.clear();
        orderedChunks.clear();

        ListTag chunkList = tag.getList(name + "chunks", 10);
        for (int i = 0; i < chunkList.size(); i++) {
            CompoundTag chunkTag = chunkList.getCompound(i);
            ChunkPos chunkPos = new ChunkPos(chunkTag.getInt("x"), chunkTag.getInt("z"));
            int[] tipBits = chunkTag.getIntArray("tips");
            List<FloatTriplet> tips = new ArrayList<>(tipBits.length / 3);
            for (int j = 0; j + 2 < tipBits.length; j += 3) {
                tips.add(new FloatTriplet(Float.intBitsToFloat(tipBits[j]),
                        Float.intBitsToFloat(tipBits[j + 1]),
                        Float.intBitsToFloat(tipBits[j + 2])));
            }
            if (!tips.isEmpty()) {
                perChunk.put(chunkPos, tips);
            }
        }

        for (long packed : tag.getLongArray(name + "order")) {
            ChunkPos chunkPos = new ChunkPos(packed);
            if (perChunk.containsKey(chunkPos)) {
                orderedChunks.add(chunkPos);
            }
        }
        if (isAusf3Complete && orderedChunks.isEmpty() && !perChunk.isEmpty()) {
            orderedChunks.addAll(perChunk.keySet());
            orderedChunks.sort(comparator);
        }
    }

    public void collectTip(int count) {
        if (level == null || level.isClientSide()) {
            isAusf3Complete = true;
            return;
        }

        int processed = 0;
        while (gspNumMax >= gspNum) {
            Vec3 vector = getSphericalCartesian();
            int rayLength = (int) Math.ceil(strength);
            float remaining = strength;
            FloatTriplet lastPos = null;
            Set<ChunkPos> chunkCoords = new HashSet<>();

            for (int i = 0; i < rayLength; i++) {
                if (i > length) {
                    break;
                }

                float x0 = (float) (posX + vector.x * i);
                float y0 = (float) (posY + vector.y * i);
                float z0 = (float) (posZ + vector.z * i);
                int blockX = (int) Math.floor(x0);
                int blockY = (int) Math.floor(y0);
                int blockZ = (int) Math.floor(z0);
                BlockPos blockPos = new BlockPos(blockX, blockY, blockZ);

                double factor = 100.0D - (double) i / (double) rayLength * 100.0D;
                factor *= 0.07D;

                if (!level.isOutsideBuildHeight(blockY)) {
                    BlockState state = level.getBlockState(blockPos);
                    if (state.getFluidState().isEmpty()) {
                        remaining -= Math.pow(masqueradeResistance(state), 7.5D - factor);
                    }

                    if (remaining > 0.0F && !isLegacyEmpty(state)) {
                        lastPos = new FloatTriplet(x0, y0, z0);
                        chunkCoords.add(new ChunkPos(blockX >> 4, blockZ >> 4));
                    }
                }

                if (remaining <= 0.0F || i + 1 >= length || i == rayLength - 1) {
                    break;
                }
            }

            if (lastPos != null) {
                for (ChunkPos chunkPos : chunkCoords) {
                    perChunk.computeIfAbsent(chunkPos, ignored -> new ArrayList<>()).add(lastPos);
                }
            }

            generateGspUp();
            processed++;
            if (processed >= count) {
                return;
            }
        }

        orderedChunks.addAll(perChunk.keySet());
        orderedChunks.sort(comparator);
        isAusf3Complete = true;
    }

    public static float masqueradeResistance(BlockState state) {
        if (state.is(Blocks.SANDSTONE)) {
            return HbmBlockStateUtil.explosionResistance(Blocks.STONE.defaultBlockState());
        }
        if (state.is(Blocks.OBSIDIAN)) {
            return HbmBlockStateUtil.explosionResistance(Blocks.STONE.defaultBlockState()) * 3.0F;
        }
        return HbmBlockStateUtil.explosionResistance(state);
    }

    public void processChunk() {
        if (level == null || level.isClientSide() || perChunk.isEmpty() || orderedChunks.isEmpty()) {
            return;
        }

        ChunkPos coord = orderedChunks.get(0);
        List<FloatTriplet> list = perChunk.get(coord);
        Set<BlockPos> toRemove = new HashSet<>();
        Set<BlockPos> toRemoveTips = new HashSet<>();
        int chunkX = coord.x;
        int chunkZ = coord.z;
        Set<Long> loadedFluidChunks = new HashSet<>();
        loadWorkChunk(chunkX, chunkZ);
        loadedFluidChunks.add(coord.toLong());

        int enter = Math.min(Math.abs(posX - (chunkX << 4)), Math.abs(posZ - (chunkZ << 4))) - 16;
        enter = Math.max(enter, 0);

        for (FloatTriplet triplet : list) {
            Vec3 vector = new Vec3(triplet.xCoord - posX, triplet.yCoord - posY, triplet.zCoord - posZ);
            double lengthVector = vector.length();
            if (lengthVector <= 0.0D) {
                continue;
            }

            double pX = vector.x / lengthVector;
            double pY = vector.y / lengthVector;
            double pZ = vector.z / lengthVector;
            int tipX = (int) Math.floor(triplet.xCoord);
            int tipY = (int) Math.floor(triplet.yCoord);
            int tipZ = (int) Math.floor(triplet.zCoord);
            boolean inChunk = false;

            for (int i = enter; i < lengthVector; i++) {
                int x0 = (int) Math.floor(posX + pX * i);
                int y0 = (int) Math.floor(posY + pY * i);
                int z0 = (int) Math.floor(posZ + pZ * i);

                if ((x0 >> 4) != chunkX || (z0 >> 4) != chunkZ) {
                    if (inChunk) {
                        break;
                    }
                    continue;
                }

                inChunk = true;
                if (level.isOutsideBuildHeight(y0)) {
                    continue;
                }

                BlockPos blockPos = new BlockPos(x0, y0, z0);
                BlockState state = level.getBlockState(blockPos);
                if (!isLegacyEmpty(state)) {
                    if (x0 == tipX && y0 == tipY && z0 == tipZ) {
                        toRemoveTips.add(blockPos);
                    }
                    toRemove.add(blockPos);
                    if (!state.getFluidState().isEmpty()) {
                        addFluidCleanup(blockPos, toRemove, loadedFluidChunks);
                    }
                }
            }
        }

        for (BlockPos blockPos : toRemove) {
            if (toRemoveTips.contains(blockPos)) {
                handleTip(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            } else {
                clearLegacyExplosionBlock(blockPos, 2);
            }
        }

        perChunk.remove(coord);
        orderedChunks.remove(0);
    }

    protected void handleTip(int x, int y, int z) {
        clearLegacyExplosionBlock(new BlockPos(x, y, z), 3);
    }

    private void clearLegacyExplosionBlock(BlockPos blockPos, int flags) {
        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), flags);
    }

    private void addFluidCleanup(BlockPos center, Set<BlockPos> toRemove, Set<Long> loadedFluidChunks) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos fluidPos = center.offset(x, y, z);
                    if (level.isOutsideBuildHeight(fluidPos)) {
                        continue;
                    }
                    loadFluidCleanupChunk(fluidPos.getX() >> 4, fluidPos.getZ() >> 4, loadedFluidChunks);
                    BlockState state = level.getBlockState(fluidPos);
                    if (!state.getFluidState().isEmpty()) {
                        toRemove.add(fluidPos);
                    }
                }
            }
        }
    }

    private void loadWorkChunk(int chunkX, int chunkZ) {
        chunkLoader.accept(chunkX, chunkZ);
        level.getChunk(chunkX, chunkZ);
    }

    private void loadFluidCleanupChunk(int chunkX, int chunkZ, Set<Long> loadedFluidChunks) {
        if (loadedFluidChunks.add(ChunkPos.asLong(chunkX, chunkZ))) {
            level.getChunk(chunkX, chunkZ);
        }
    }

    private static boolean isLegacyEmpty(BlockState state) {
        return state.isAir() && state.getFluidState().isEmpty();
    }

    @Override
    public boolean isComplete() {
        return isAusf3Complete && perChunk.isEmpty();
    }

    @Override
    public void cacheChunksTick(int processTimeMs) {
        if (!isAusf3Complete) {
            collectTip(speed * 10);
        }
    }

    @Override
    public void destructionTick(int processTimeMs) {
        if (!isAusf3Complete) {
            return;
        }
        long start = System.currentTimeMillis();
        while (!perChunk.isEmpty() && System.currentTimeMillis() < start + processTimeMs) {
            processChunk();
        }
    }

    @Override
    public void cancel() {
        isAusf3Complete = true;
        perChunk.clear();
        orderedChunks.clear();
    }

    private void generateGspUp() {
        if (gspNum < gspNumMax) {
            int k = gspNum + 1;
            double hk = -1.0D + 2.0D * (k - 1.0D) / (gspNumMax - 1.0D);
            gspX = Math.acos(hk);
            double lon = gspY + 3.6D / Math.sqrt(gspNumMax) / Math.sqrt(1.0D - hk * hk);
            gspY = lon % (Math.PI * 2.0D);
        } else {
            gspX = 0.0D;
            gspY = 0.0D;
        }
        gspNum++;
    }

    private Vec3 getSphericalCartesian() {
        double dx = Math.sin(gspX) * Math.cos(gspY);
        double dz = Math.sin(gspX) * Math.sin(gspY);
        double dy = Math.cos(gspX);
        return new Vec3(dx, dy, dz);
    }

    public class CoordComparator implements Comparator<ChunkPos> {
        @Override
        public int compare(ChunkPos first, ChunkPos second) {
            int chunkX = ExplosionNukeRayBatched.this.posX >> 4;
            int chunkZ = ExplosionNukeRayBatched.this.posZ >> 4;
            int diff1 = Math.abs(chunkX - first.x) + Math.abs(chunkZ - first.z);
            int diff2 = Math.abs(chunkX - second.x) + Math.abs(chunkZ - second.z);
            return diff1 - diff2;
        }
    }

    public static class FloatTriplet {
        public final float xCoord;
        public final float yCoord;
        public final float zCoord;

        public FloatTriplet(float x, float y, float z) {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
        }
    }
}
