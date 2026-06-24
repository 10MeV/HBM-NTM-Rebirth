package com.hbm.ntm.explosion;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class ExplosionBalefire {
    private static final RegistryObject<? extends Block> SCHRABIDIUM_CLUSTER = requireLegacyBlock("block_schrabidium_cluster");
    private static final RegistryObject<? extends Block> EUPHEMIUM_CLUSTER = requireLegacyBlock("block_euphemium_cluster");
    private static final double MODERN_BEDROCK_CORE_FRACTION = 0.75D;

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

    public ExplosionBalefire(int x, int y, int z, Level level, int radius) {
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

    public int currentWorldX() {
        return posX + lastposX;
    }

    public int currentWorldZ() {
        return posZ + lastposZ;
    }

    private void breakColumn(int x, int z) {
        int distance = (int) (radius - Math.sqrt(x * x + z * z));
        if (distance <= 0) {
            return;
        }

        int worldX = posX + x;
        int worldZ = posZ + z;
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
        int maxDepth = (int) (10 + radius * 0.25D);
        int depthOffset = (int) ((maxDepth * distance / (double) radius) + (Math.sin(distance * 0.15D + 2.0D) * 2.0D));
        int minBuildHeight = level.getMinBuildHeight();
        int depth = modernBottomRelativeDepth(y, depthOffset, distance);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        while (y > depth) {
            cursor.set(worldX, y, worldZ);
            if (level.getBlockState(cursor).is(SCHRABIDIUM_CLUSTER.get())) {
                transmuteSchrabidiumCluster(cursor);
                return;
            }

            clearBlock(cursor);
            y--;
        }

        if (level.random.nextInt(10) == 0) {
            placeBalefire(cursor.set(worldX, depth + 1, worldZ));
            if (level.getBlockState(cursor.set(worldX, y, worldZ)).is(SCHRABIDIUM_CLUSTER.get())) {
                level.setBlock(cursor, EUPHEMIUM_CLUSTER.get().defaultBlockState(), 3);
            }
        }

        for (int i = depth; i > depth - 5 && i >= minBuildHeight; i--) {
            cursor.set(worldX, i, worldZ);
            if (level.getBlockState(cursor).is(Blocks.STONE)) {
                level.setBlock(cursor, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), 3);
            }
        }
    }

    private int modernBottomRelativeDepth(int surfaceY, int depthOffset, int distance) {
        int legacyDepth = surfaceY - depthOffset;
        if (legacyDepth <= 0 || distance >= radius * MODERN_BEDROCK_CORE_FRACTION) {
            return level.getMinBuildHeight();
        }
        return Math.min(legacyDepth, level.getMaxBuildHeight() - 1);
    }

    private void clearBlock(BlockPos pos) {
        LegacyExplosionFluidCleanup.clearBlockOrLegacyLiquidNeighborhood(level, pos, 3);
    }

    private void transmuteSchrabidiumCluster(BlockPos pos) {
        if (level.random.nextInt(10) == 0) {
            placeBalefire(pos.above());
            level.setBlock(pos, EUPHEMIUM_CLUSTER.get().defaultBlockState(), 3);
        }
    }

    private void placeBalefire(BlockPos pos) {
        level.setBlock(pos, ModBlocks.BALEFIRE.get().defaultBlockState(), 3);
    }

    private static RegistryObject<? extends Block> requireLegacyBlock(String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return Objects.requireNonNull(block, "Missing legacy block hbm_ntm_rebirth:" + name);
    }
}
