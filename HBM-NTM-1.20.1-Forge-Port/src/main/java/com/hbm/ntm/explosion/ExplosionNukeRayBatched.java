package com.hbm.ntm.explosion;

import net.minecraft.core.BlockPos;
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
    public boolean isAusf3Complete;

    public ExplosionNukeRayBatched(Level level, int x, int y, int z, int strength, int speed, int length) {
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

                    if (remaining > 0.0F && !state.isAir()) {
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
            return Blocks.STONE.getExplosionResistance();
        }
        if (state.is(Blocks.OBSIDIAN)) {
            return Blocks.STONE.getExplosionResistance() * 3.0F;
        }
        return state.getBlock().getExplosionResistance();
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
                if (!level.getBlockState(blockPos).isAir()) {
                    if (x0 == tipX && y0 == tipY && z0 == tipZ) {
                        toRemoveTips.add(blockPos);
                    }
                    toRemove.add(blockPos);
                }
            }
        }

        for (BlockPos blockPos : toRemove) {
            if (toRemoveTips.contains(blockPos)) {
                handleTip(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            } else {
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        perChunk.remove(coord);
        orderedChunks.remove(0);
    }

    protected void handleTip(int x, int y, int z) {
        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
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
