package com.hbm.ntm.explosion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ExplosionSolinium {
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
    public float explosionCoefficient = 1.0F;
    public float explosionCoefficient2 = 1.0F;

    public ExplosionSolinium(int x, int y, int z, Level level, int radius, float coefficient, float coefficient2) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.level = level;
        this.radius = radius;
        this.radius2 = radius * radius;
        this.explosionCoefficient = coefficient;
        this.explosionCoefficient2 = coefficient2;
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
        tag.putFloat(name + "explosionCoefficient", explosionCoefficient);
        tag.putFloat(name + "explosionCoefficient2", explosionCoefficient2);
    }

    public void readFromNbt(CompoundTag tag, String name) {
        posX = tag.getInt(name + "posX");
        posY = tag.getInt(name + "posY");
        posZ = tag.getInt(name + "posZ");
        lastposX = tag.getInt(name + "lastposX");
        lastposZ = tag.getInt(name + "lastposZ");
        radius = tag.getInt(name + "radius");
        radius2 = tag.getInt(name + "radius2");
        n = tag.getInt(name + "n");
        nlimit = tag.getInt(name + "nlimit");
        shell = tag.getInt(name + "shell");
        leg = tag.getInt(name + "leg");
        element = tag.getInt(name + "element");
        explosionCoefficient = tag.getFloat(name + "explosionCoefficient");
        explosionCoefficient2 = tag.getFloat(name + "explosionCoefficient2");
    }

    public boolean update() {
        if (level == null || level.isClientSide()) {
            return true;
        }

        breakColumn(lastposX, lastposZ);
        shell = (int) Math.floor((Math.sqrt(n) + 1.0D) / 2.0D);
        int shell2 = Math.max(shell * 2, 1);
        leg = (int) Math.floor((double) (n - (shell2 - 1) * (shell2 - 1)) / shell2);
        element = (n - (shell2 - 1) * (shell2 - 1)) - shell2 * leg - shell + 1;
        lastposX = leg == 0 ? shell : leg == 1 ? -element : leg == 2 ? -shell : element;
        lastposZ = leg == 0 ? element : leg == 1 ? shell : leg == 2 ? -element : -shell;
        n++;
        return n > nlimit;
    }

    private void breakColumn(int x, int z) {
        int distance = radius2 - (x * x + z * z);
        if (distance <= 0) {
            return;
        }

        distance = (int) Math.sqrt(distance);
        int maxY = (int) (distance / Math.max(0.001F, explosionCoefficient2));
        int minY = (int) (-distance / Math.max(0.001F, explosionCoefficient));
        for (int y = maxY; y > minY; y--) {
            int worldY = posY + y;
            if (level.isOutsideBuildHeight(worldY)) {
                if (worldY < level.getMinBuildHeight()) {
                    break;
                }
                continue;
            }
            ExplosionNukeGeneric.solinium(level, posX + x, worldY, posZ + z);
        }
    }
}
