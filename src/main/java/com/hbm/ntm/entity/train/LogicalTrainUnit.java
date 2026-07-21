package com.hbm.ntm.entity.train;

import com.hbm.ntm.rail.HbmRail;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Source-faithful 1.7.10 logical-train-unit ordering, approach movement and collision push contract. */
public final class LogicalTrainUnit {
    private double pushForce;
    private LegacyRailCarEntity[] trains = new LegacyRailCarEntity[0];

    public static LogicalTrainUnit generateTrain(LegacyRailCarEntity train) {
        List<LegacyRailCarEntity> links = new ArrayList<>();
        Set<LegacyRailCarEntity> visited = new HashSet<>();
        LogicalTrainUnit unit = new LogicalTrainUnit();
        if (train.getCoupledTo(TrainCoupling.FRONT) == null && train.getCoupledTo(TrainCoupling.BACK) == null) {
            unit.trains = new LegacyRailCarEntity[] {train};
            train.setLogicalTrainUnit(unit, 0);
            return unit;
        }
        LegacyRailCarEntity current = train;
        while (current != null) {
            LegacyRailCarEntity next = null;
            LegacyRailCarEntity front = current.getCoupledTo(TrainCoupling.FRONT);
            LegacyRailCarEntity back = current.getCoupledTo(TrainCoupling.BACK);
            if (front != null && !visited.contains(front)) {
                next = front;
            }
            if (back != null && !visited.contains(back)) {
                next = back;
            }
            links.add(current);
            visited.add(current);
            current = next;
        }
        unit.trains = links.toArray(LegacyRailCarEntity[]::new);
        for (int index = 0; index < unit.trains.length; index++) {
            unit.trains[index].setLogicalTrainUnit(unit, index);
        }
        return unit;
    }

    public void dissolveTrain() {
        for (LegacyRailCarEntity train : trains) {
            train.setLogicalTrainUnit(null, 0);
        }
    }

    void updateMotion() {
        if (trains.length == 0 || trains[0].isRemoved()) {
            dissolveTrain();
            return;
        }
        double speed = getTotalSpeed() + pushForce;
        if (Math.abs(speed) < 0.001D) {
            speed = 0.0D;
        }
        for (LegacyRailCarEntity train : trains) {
            train.setCachedSpeed(speed);
        }
        if (trains.length == 1) {
            moveSingle(trains[0], speed);
        } else if (speed == 0.0D) {
            combineWagons();
        } else {
            moveTrainByApproach(speed);
        }
        pushForce = 0.0D;
        collideTrain(speed);
    }

    private void moveSingle(LegacyRailCarEntity train, double speed) {
        BlockPos anchor = train.getCurrentAnchorPos();
        Vec3 newPosition = train.getRelPosAlongRail(anchor, speed, new HbmRail.MoveContext(HbmRail.RailCheckType.CORE, 0.0D));
        if (newPosition == null) {
            train.derail();
            dissolveTrain();
            return;
        }
        train.setPos(newPosition);
        anchor = train.getCurrentAnchorPos();
        Vec3 front = train.getRelPosAlongRail(anchor, train.getLengthSpan(), new HbmRail.MoveContext(
                HbmRail.RailCheckType.FRONT, train.getCollisionSpan() - train.getLengthSpan()));
        Vec3 back = train.getRelPosAlongRail(anchor, -train.getLengthSpan(), new HbmRail.MoveContext(
                HbmRail.RailCheckType.BACK, train.getCollisionSpan() - train.getLengthSpan()));
        if (front == null || back == null) {
            train.derail();
            dissolveTrain();
            return;
        }
        LegacyRailCarEntity.setRailPose(train, front, back);
    }

    private double getTotalSpeed() {
        LegacyRailCarEntity first = trains[0];
        if (trains.length == 1) {
            return first.getCurrentSpeed();
        }
        boolean reverseTheReverse = first.getCouplingFrom(null) == TrainCoupling.BACK;
        double total = 0.0D;
        double maximum = Double.POSITIVE_INFINITY;
        for (LegacyRailCarEntity train : trains) {
            boolean reverse = false;
            LegacyRailCarEntity front = train.getCoupledTo(TrainCoupling.FRONT);
            LegacyRailCarEntity back = train.getCoupledTo(TrainCoupling.BACK);
            if (front != null && front.logicalTrainIndex() > train.logicalTrainIndex()) {
                reverse = true;
            }
            if (back != null && back.logicalTrainIndex() < train.logicalTrainIndex()) {
                reverse = true;
            }
            reverse ^= reverseTheReverse;
            double speed = train.getCurrentSpeed();
            total += reverse ? -speed : speed;
            maximum = Math.min(maximum, train.getMaxRailSpeed());
        }
        return Math.abs(total) > maximum ? maximum * Math.signum(total) : total;
    }

    private void combineWagons() {
        if (trains.length <= 1) {
            return;
        }
        int centerIndex = trains.length % 2 == 1 ? trains.length / 2 : trains.length / 2 - 1;
        LegacyRailCarEntity center = trains[centerIndex];
        LegacyRailCarEntity previous = center;
        for (int index = centerIndex - 1; index >= 0; index--) {
            LegacyRailCarEntity next = trains[index];
            moveWagonTo(previous, next);
            previous = next;
        }
        previous = center;
        for (int index = centerIndex + 1; index < trains.length; index++) {
            LegacyRailCarEntity next = trains[index];
            moveWagonTo(previous, next);
            previous = next;
        }
    }

    private void moveWagonTo(LegacyRailCarEntity moveTo, LegacyRailCarEntity moving) {
        TrainCoupling previousCoupling = moveTo.getCouplingFrom(moving);
        TrainCoupling nextCoupling = moving.getCouplingFrom(moveTo);
        Vec3 previousLocation = moveTo.getCouplingPos(previousCoupling);
        Vec3 nextLocation = moving.getCouplingPos(nextCoupling);
        if (previousLocation == null || nextLocation == null) {
            dissolveTrain();
            return;
        }
        Vec3 delta = new Vec3(previousLocation.x - nextLocation.x, 0.0D, previousLocation.z - nextLocation.z);
        double length = delta.length();
        length = length / (0.5D / (length * length) + 1.0D);
        BlockPos anchor = BlockPos.containing(moving.getX(), moving.getY(), moving.getZ());
        Vec3 position = moving.position();
        float yaw = LegacyRailCarEntity.generateYaw(previousLocation, nextLocation);
        Vec3 newPosition = LegacyRailCarEntity.getRelPosAlongRail(moving.level(), anchor, length, moving.getGauge(),
                position, yaw, new HbmRail.MoveContext(HbmRail.RailCheckType.CORE, 0.0D));
        if (newPosition == null) {
            moving.derail();
            dissolveTrain();
            return;
        }
        moving.setPos(newPosition);
        anchor = moving.getCurrentAnchorPos();
        Vec3 front = moving.getRelPosAlongRail(anchor, moving.getLengthSpan(), new HbmRail.MoveContext(
                HbmRail.RailCheckType.FRONT, moving.getCollisionSpan() - moving.getLengthSpan()));
        Vec3 back = moving.getRelPosAlongRail(anchor, -moving.getLengthSpan(), new HbmRail.MoveContext(
                HbmRail.RailCheckType.BACK, moving.getCollisionSpan() - moving.getLengthSpan()));
        if (front == null || back == null) {
            moving.derail();
            dissolveTrain();
            return;
        }
        LegacyRailCarEntity.setRailPose(moving, front, back);
    }

    private void moveTrainByApproach(double speed) {
        LegacyRailCarEntity previous = null;
        LegacyRailCarEntity first = trains[0];
        boolean order = (speed > 0.0D) ^ first.getCouplingFrom(null) == TrainCoupling.BACK;
        for (int index = order ? 0 : trains.length - 1; order ? index < trains.length : index >= 0; index += order ? 1 : -1) {
            LegacyRailCarEntity current = trains[index];
            if (previous == null) {
                double adjustedSpeed = first == current ? -speed : speed;
                boolean inReverse = first.getCouplingFrom(null) == current.getCouplingFrom(null);
                int sign = inReverse ? 1 : -1;
                BlockPos anchor = current.getCurrentAnchorPos();
                Vec3 front = current.getRelPosAlongRail(anchor, (adjustedSpeed + current.getLengthSpan()) * -sign,
                        new HbmRail.MoveContext(HbmRail.RailCheckType.FRONT,
                                current.getCollisionSpan() - current.getLengthSpan()));
                if (front == null) {
                    current.derail();
                    dissolveTrain();
                    return;
                }
                Vec3 core = current.getRelPosAlongRail(anchor, adjustedSpeed * -sign,
                        new HbmRail.MoveContext(HbmRail.RailCheckType.CORE, 0.0D));
                if (core == null) {
                    current.derail();
                    dissolveTrain();
                    return;
                }
                current.setPos(core);
                Vec3 back = current.getRelPosAlongRail(anchor, (adjustedSpeed - current.getLengthSpan()) * -sign,
                        new HbmRail.MoveContext(HbmRail.RailCheckType.BACK,
                                current.getCollisionSpan() - current.getLengthSpan()));
                if (back == null) {
                    current.derail();
                    dissolveTrain();
                    return;
                }
                LegacyRailCarEntity.setRailPose(current, inReverse ? back : front, inReverse ? front : back);
            } else {
                moveWagonTo(previous, current);
            }
            previous = current;
        }
    }

    private void collideTrain(double speed) {
        LegacyRailCarEntity colliding = speed > 0.0D ? trains[0] : trains[trains.length - 1];
        List<LegacyRailCarEntity> intersecting = colliding.level().getEntitiesOfClass(LegacyRailCarEntity.class,
                colliding.getBoundingBox().inflate(1.0D));
        LegacyRailCarEntity other = null;
        for (LegacyRailCarEntity train : intersecting) {
            if (train.logicalTrainUnit() != null && train.logicalTrainUnit() != this) {
                other = train;
                break;
            }
        }
        if (other == null) {
            return;
        }
        Vec3 delta = new Vec3(colliding.getX() - other.getX(), 0.0D, colliding.getZ() - other.getZ());
        double totalSpan = colliding.getCollisionSpan() + other.getCollisionSpan();
        double difference = delta.length();
        if (difference > totalSpan) {
            return;
        }
        double push = totalSpan - difference;
        applyCollisionPush(colliding, other, push);
        applyCollisionPush(other, colliding, push);
    }

    private static void applyCollisionPush(LegacyRailCarEntity from, LegacyRailCarEntity to, double push) {
        LogicalTrainUnit unit = from.logicalTrainUnit();
        if (unit == null) {
            return;
        }
        if (unit.trains.length == 1) {
            // Source applies pitch before yaw to the collision-span vector.
            Vec3 offset = LegacyRailCarEntity.rotateY(
                    LegacyRailCarEntity.rotateX(new Vec3(0.0D, 0.0D, from.getCollisionSpan()), from.getXRot()),
                    from.getYRot());
            Vec3 forward = new Vec3(to.getX() - (from.getX() + offset.x), 0.0D, to.getZ() - (from.getZ() + offset.z));
            Vec3 backward = new Vec3(to.getX() - (from.getX() - offset.x), 0.0D, to.getZ() - (from.getZ() - offset.z));
            unit.pushForce += forward.length() > backward.length() ? push : -push;
        } else if (from.logicalTrainIndex() < unit.trains.length / 2) {
            unit.pushForce -= push;
        } else {
            unit.pushForce += push;
        }
    }
}
