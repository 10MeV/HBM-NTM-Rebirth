package com.hbm.ntm.turret;

import com.hbm.ntm.api.entity.RadarCommandReceiver;
import com.hbm.ntm.api.redstoneoverradio.ROR;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class ArtilleryTargetQueue {
    private final List<Vec3> targets = new ArrayList<>();

    boolean enqueue(TurretBlockEntityBase turret, double x, double y, double z) {
        Vec3 target = new Vec3(x, y, z);
        if (target.distanceTo(turret.getTurretPos()) > turret.getDetectorRange()) {
            return false;
        }
        targets.add(target);
        turret.setChanged();
        turret.syncRuntimeToTracking();
        return true;
    }

    boolean sendCommandPosition(TurretBlockEntityBase turret, int x, int y, int z) {
        Vec3 target = RadarCommandReceiver.centeredPositionTarget(x, y, z);
        return enqueue(turret, target.x, target.y, target.z);
    }

    boolean sendCommandEntity(TurretBlockEntityBase turret, Entity target) {
        return target != null && enqueue(turret, target.getX(), target.getY(), target.getZ());
    }

    void applyManualTarget(TurretBlockEntityBase turret, boolean manualMode) {
        if (manualMode) {
            Vec3 target = peek();
            if (target != null) {
                turret.setManualTarget(target);
            }
        } else {
            clear();
        }
    }

    @Nullable
    Vec3 peek() {
        return targets.isEmpty() ? null : targets.get(0);
    }

    void removeFirst() {
        if (!targets.isEmpty()) {
            targets.remove(0);
        }
    }

    boolean isEmpty() {
        return targets.isEmpty();
    }

    void clear() {
        targets.clear();
    }

    String[] appendFunctionInfo(String[] baseInfo) {
        List<String> info = new ArrayList<>(Arrays.asList(baseInfo));
        info.add(ROR.functionInfo("enqueue", "x" + ArtilleryTargetReceiver.PARAM_SEPARATOR
                + "y" + ArtilleryTargetReceiver.PARAM_SEPARATOR + "z"));
        return info.toArray(String[]::new);
    }

    String runRORFunction(TurretBlockEntityBase turret, String name, String[] params) {
        if (ROR.function("enqueue").equals(name) && params != null && params.length > 2) {
            try {
                int x = Integer.parseInt(params[0]);
                int y = Integer.parseInt(params[1]);
                int z = Integer.parseInt(params[2]);
                sendCommandPosition(turret, x, y, z);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
