package com.hbm.ntm.explosion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class ExplosionNukeAdvanced {
    public static final int TYPE_DESTRUCTION = 0;
    public static final int TYPE_VAPOR = 1;
    public static final int TYPE_WASTE = 2;

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
    public int type = TYPE_DESTRUCTION;

    public ExplosionNukeAdvanced(int x, int y, int z, Level level, int radius, float coefficient, int type) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.level = level;
        this.radius = radius;
        this.radius2 = radius * radius;
        float safeCoefficient = Math.max(0.001F, coefficient);
        this.explosionCoefficient = Math.min(Math.max((radius + safeCoefficient * (y - 60.0F)) / (safeCoefficient * radius),
                1.0F / safeCoefficient), 1.0F);
        this.type = type;
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
        tag.putInt(name + "type", type);
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
        type = tag.getInt(name + "type");
    }

    public boolean update() {
        if (level == null || level.isClientSide()) {
            return true;
        }

        switch (type) {
            case TYPE_VAPOR -> vapor(lastposX, lastposZ);
            case TYPE_WASTE -> waste(lastposX, lastposZ);
            default -> breakColumn(lastposX, lastposZ);
        }

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
        int distance = radius2 - (x * x + z * z);
        if (distance <= 0) {
            return;
        }

        distance = (int) Math.sqrt(distance);
        for (int y = distance; y > -distance * explosionCoefficient; y--) {
            int protection = ExplosionNukeGeneric.destruction(level, posX + x, posY + y, posZ + z);
            if (y < 8) {
                y -= protection;
            }
        }
    }

    private void vapor(int x, int z) {
        int distance = radius2 - (x * x + z * z);
        if (distance <= 0) {
            return;
        }

        distance = (int) Math.sqrt(distance);
        for (int y = distance; y > -distance * explosionCoefficient; y--) {
            y -= ExplosionNukeGeneric.vaporDest(level, posX + x, posY + y, posZ + z);
        }
    }

    private void waste(int x, int z) {
        int distance = radius2 - (x * x + z * z);
        if (distance <= 0) {
            return;
        }

        distance = (int) Math.sqrt(distance);
        for (int y = distance; y > -distance * explosionCoefficient; y--) {
            if (radius >= 95) {
                ExplosionNukeGeneric.wasteDest(level, posX + x, posY + y, posZ + z);
            } else {
                ExplosionNukeGeneric.wasteDestNoSchrab(level, posX + x, posY + y, posZ + z);
            }
        }
    }
}
