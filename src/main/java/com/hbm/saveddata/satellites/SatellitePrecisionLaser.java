package com.hbm.saveddata.satellites;

import com.hbm.ntm.entity.logic.OrbitalLaserEntity;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Locale;

/** Source-backed 1.7.10 XSatelliteRegistry Precision Laser. */
public final class SatellitePrecisionLaser extends SatelliteBase {
    public static final String CMD_FIRE = "fire";
    public static final String CMD_CANFIRE = "canfire";
    public static final String CMD_SETENTITYTARGET = "setentitytarget";
    public static final int MAX_TARGET_RANGE = 1_000;
    public static final int CHARGE_TIME = 5 * 20;

    public long lastShot;
    public int targetedEntity = -1;

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.PRECISION_LASER;
    }

    @Override
    public String getType() {
        return "ORBITAL_TATOO_REMOVER";
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putLong("lastShot", lastShot);
        tag.putInt("targetedEntity", targetedEntity);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastShot = tag.getLong("lastShot");
        targetedEntity = tag.getInt("targetedEntity");
    }

    @Override
    public void onCommandImpl(ServerLevel level, String... command) {
        if (command == null || command.length == 0) {
            return;
        }
        if (CMD_FIRE.equals(command[0])) {
            fire(level);
            return;
        }
        if (CMD_CANFIRE.equals(command[0])) {
            tx = Boolean.toString(lastShot + CHARGE_TIME < level.getGameTime()).toUpperCase(Locale.US);
            return;
        }
        if (CMD_SETENTITYTARGET.equals(command[0]) && command.length >= 2) {
            targetedEntity = RORInteractive.parseInt(command[1], Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }

    @Override
    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        setTarget(x, z);
        deathBlast(level, targetX, targetZ);
    }

    private void fire(ServerLevel level) {
        if (targetedEntity != -1) {
            Entity entity = level.getEntity(targetedEntity);
            targetedEntity = -1;
            if (entity == null || !entity.isAlive()) {
                return;
            }
            int x = (int) Math.floor(entity.getX());
            int z = (int) Math.floor(entity.getZ());
            double deltaX = x - targetX;
            double deltaZ = z - targetZ;
            if (deltaX * deltaX + deltaZ * deltaZ <= MAX_TARGET_RANGE * MAX_TARGET_RANGE) {
                deathBlast(level, entity.getX(), entity.getY(), entity.getZ());
                return;
            }
        }
        deathBlast(level, targetX, targetZ);
    }

    public void deathBlast(ServerLevel level, int x, int z) {
        deathBlast(level, x + 0.5D, WorldUtil.legacyGetHeightValue(level, x, z), z + 0.5D);
    }

    public void deathBlast(ServerLevel level, double x, double y, double z) {
        if (lastShot + CHARGE_TIME < level.getGameTime()) {
            lastShot = level.getGameTime();
            OrbitalLaserEntity blast = new OrbitalLaserEntity(level);
            blast.setPos(x, y, z);
            blast.explode();
            level.addFreshEntity(blast);
        }
    }
}
