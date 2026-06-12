package com.hbm.ntm.explosion;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ExplosionTom {
    public int posX;
    public int posY;
    public int posZ;
    public int lastposX = 0;
    public int lastposZ = 0;
    public int radius;
    public int radius2;
    public Level level;
    private int n = 1;
    private int nlimit;
    private int shell;
    private int leg;
    private int element;

    public ExplosionTom(int x, int y, int z, Level level, int radius) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.level = level;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.nlimit = this.radius2 * 4;
    }

    public void saveToNbt(CompoundTag tag, String name) {
        tag.putInt(name + "posX", posX);
        tag.putInt(name + "posY", posY);
        tag.putInt(name + "posZ", posZ);
        tag.putInt(name + "lastposX", lastposX);
        tag.putInt(name + "lastposZ", lastposZ);
        tag.putInt(name + "radius", radius);
        tag.putInt(name + "radius2", radius2);
        tag.putInt(name + "n", n);
        tag.putInt(name + "nlimit", nlimit);
        tag.putInt(name + "shell", shell);
        tag.putInt(name + "leg", leg);
        tag.putInt(name + "element", element);
    }

    public void readFromNbt(CompoundTag tag, String name) {
        posX = tag.getInt(name + "posX");
        posY = tag.getInt(name + "posY");
        posZ = tag.getInt(name + "posZ");
        lastposX = tag.getInt(name + "lastposX");
        lastposZ = tag.getInt(name + "lastposZ");
        radius = tag.getInt(name + "radius");
        radius2 = tag.getInt(name + "radius2");
        n = Math.max(tag.getInt(name + "n"), 1);
        nlimit = tag.getInt(name + "nlimit");
        shell = tag.getInt(name + "shell");
        leg = tag.getInt(name + "leg");
        element = tag.getInt(name + "element");
    }

    public boolean update() {
        if (level == null || level.isClientSide() || n == 0) {
            return true;
        }

        breakColumn(lastposX, lastposZ);
        shell = (int) Math.floor((Math.sqrt(n) + 1.0D) / 2.0D);
        int shell2 = shell * 2;
        if (shell2 == 0) {
            return true;
        }

        leg = (int) Math.floor((double) (n - (shell2 - 1) * (shell2 - 1)) / shell2);
        element = (n - (shell2 - 1) * (shell2 - 1)) - shell2 * leg - shell + 1;
        lastposX = leg == 0 ? shell : leg == 1 ? -element : leg == 2 ? -shell : element;
        lastposZ = leg == 0 ? element : leg == 1 ? shell : leg == 2 ? -element : -shell;
        n++;
        return n > nlimit;
    }

    private void breakColumn(int x, int z) {
        int distanceRemaining = radius2 - (x * x + z * z);
        if (distanceRemaining <= 0) {
            return;
        }

        int worldX = posX + x;
        int worldZ = posZ + z;
        double distance = Math.sqrt(Math.pow(posX - worldX, 2.0D) + Math.pow(posZ - worldZ, 2.0D));
        int terrain = 63;
        double radialDistance = Math.sqrt(x * x + z * z);
        double craterBase = terrain - Math.pow(Math.E, -Math.pow(radialDistance, 2.0D) / 40000.0D) * 13.0D + level.random.nextInt(2);
        double craterRing = craterBase + Math.pow(Math.E, -Math.pow(radialDistance - 200.0D, 2.0D) / 400.0D) * 13.0D;
        int craterFloor = (int) (craterRing + Math.pow(Math.E, -Math.pow(radialDistance - 500.0D, 2.0D) / 2000.0D) * 37.0D);

        int sampleTop = Math.min(256, level.getMaxBuildHeight() - 1);
        int y = sampleTop;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int sampleY = sampleTop; sampleY > 0; sampleY--) {
            cursor.set(worldX, sampleY, worldZ);
            if (sampleY == craterFloor || !level.getBlockState(cursor).isAir()) {
                y = sampleY;
                break;
            }
        }

        int height = terrain - 14;
        int offset = 20;
        int threshold = (int) (radialDistance * (height + offset) / Math.max(1.0D, radius) + level.random.nextInt(2) - offset);
        while (y > threshold && y > 0) {
            cursor.set(worldX, y, worldZ);
            if (y <= craterFloor) {
                level.setBlock(cursor, level.random.nextInt(200) == 0 ? tektiteOreState() : tektiteState(), 3);
            } else if (y > terrain + 1) {
                if (distance < 500.0D) {
                    clearVolatileNeighborhood(cursor, true);
                    level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                }
            } else {
                floodLavaNeighborhood(cursor);
                level.setBlock(cursor, Blocks.LAVA.defaultBlockState(), 2);
            }
            y--;
        }
    }

    private void clearVolatileNeighborhood(BlockPos center, boolean clearCenter) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx < 3; dx++) {
            for (int dy = -2; dy < 3; dy++) {
                for (int dz = -2; dz < 3; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (isTomVolatile(level.getBlockState(cursor))) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 2);
                        if (clearCenter) {
                            level.setBlock(center, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
        }
    }

    private void floodLavaNeighborhood(BlockPos center) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx < 3; dx++) {
            for (int dy = -2; dy < 3; dy++) {
                for (int dz = -2; dz < 3; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.getFluidState().isEmpty() || state.is(Blocks.ICE) || state.isAir()) {
                        level.setBlock(cursor, Blocks.LAVA.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private boolean isTomVolatile(BlockState state) {
        return !state.getFluidState().isEmpty()
                || state.is(Blocks.ICE)
                || state.is(BlockTags.SNOW)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.PLANKS)
                || state.ignitedByLava();
    }

    private static BlockState tektiteState() {
        return ModBlocks.TEKTITE.get().defaultBlockState();
    }

    private static BlockState tektiteOreState() {
        return ModBlocks.ORE_TEKTITE_OSMIRIDIUM.get().defaultBlockState();
    }
}
