package com.hbm.ntm.entity.train;

import com.hbm.ntm.rail.HbmRail;
import com.hbm.ntm.rail.HbmRailTraversal;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Modern port of 1.7.10 {@code EntityRailCarBase}.  This is deliberately an
 * ordinary entity rather than a Minecart: HBM standard/narrow rails, bogies,
 * links and collision boxes have no vanilla-minecart equivalent.
 */
public abstract class LegacyRailCarEntity extends Entity {
    @Nullable private LogicalTrainUnit logicalTrainUnit;
    private int logicalTrainIndex;
    private boolean onRail = true;
    private double renderX;
    private double renderY;
    private double renderZ;
    private double lastRenderX;
    private double lastRenderY;
    private double lastRenderZ;
    private double cachedSpeed;
    @Nullable private LegacyRailCarEntity coupledFront;
    @Nullable private LegacyRailCarEntity coupledBack;
    private final List<RailCarBoundingDummyEntity> boundingDummies = new ArrayList<>();

    protected LegacyRailCarEntity(EntityType<? extends LegacyRailCarEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // The legacy base intentionally did not persist links or rail state.
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // The legacy base intentionally did not persist links or rail state.
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            updateRenderPosition();
            return;
        }

        if (!onRail) {
            unlinkBothSides();
        }
        if (coupledFront != null && coupledFront.isRemoved()) {
            coupledFront = null;
            dissolveLogicalTrain();
        }
        if (coupledBack != null && coupledBack.isRemoved()) {
            coupledBack = null;
            dissolveLogicalTrain();
        }
        if (logicalTrainUnit == null && (coupledFront == null || coupledBack == null) && onRail) {
            LogicalTrainUnit.generateTrain(this);
        }
        if (!onRail) {
            Vec3 motion = rotateY(new Vec3(0.0D, 0.0D, cachedSpeed), getYRot());
            setPos(getX() + motion.x, getY() + motion.y - 0.04D, getZ() + motion.z);
            setDeltaMovement(motion.x, motion.y - 0.04D, motion.z);
            renderX = getX();
            renderY = getY();
            renderZ = getZ();
            cachedSpeed *= 0.95D;
        }
        ensureBoundingDummies();
        updateBoundingDummies();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && player.getMainHandItem().is(ModItems.COUPLING_TOOL.get())) {
            if (tryCouple(player)) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    /** Exact source rule: same-gauge neighbour within the expanded 2-block AABB, nearest free endpoints under one block. */
    private boolean tryCouple(Player player) {
        AABB searchBox = getBoundingBox().inflate(2.0D, 0.0D, 2.0D);
        for (LegacyRailCarEntity neighbor : level().getEntitiesOfClass(LegacyRailCarEntity.class, searchBox,
                candidate -> candidate != this && candidate.getGauge() == getGauge())) {
            TrainCoupling own = null;
            TrainCoupling other = null;
            double closest = Double.POSITIVE_INFINITY;
            for (TrainCoupling ownCandidate : TrainCoupling.values()) {
                for (TrainCoupling neighborCandidate : TrainCoupling.values()) {
                    Vec3 ownPosition = getCouplingPos(ownCandidate);
                    Vec3 neighborPosition = neighbor.getCouplingPos(neighborCandidate);
                    if (ownPosition == null || neighborPosition == null) {
                        continue;
                    }
                    double distance = ownPosition.distanceTo(neighborPosition);
                    if (distance < 1.0D && distance < closest) {
                        closest = distance;
                        own = ownCandidate;
                        other = neighborCandidate;
                    }
                }
            }
            if (own == null || other == null || getCoupledTo(own) != null || neighbor.getCoupledTo(other) != null) {
                continue;
            }
            couple(own, neighbor);
            neighbor.couple(other, this);
            dissolveLogicalTrain();
            neighbor.dissolveLogicalTrain();
            player.swing(InteractionHand.MAIN_HAND, true);
            player.sendSystemMessage(Component.literal("Coupled " + hashCode() + " (" + own + ") to "
                    + neighbor.hashCode() + " (" + other + ")"));
            return true;
        }
        return false;
    }

    private void unlinkBothSides() {
        if (coupledFront != null) {
            coupledFront.couple(coupledFront.getCouplingFrom(this), null);
        }
        if (coupledBack != null) {
            coupledBack.couple(coupledBack.getCouplingFrom(this), null);
        }
        coupledFront = null;
        coupledBack = null;
    }

    private void ensureBoundingDummies() {
        DummyConfig[] definitions = getDummies();
        if (!boundingDummies.isEmpty()) {
            return;
        }
        for (DummyConfig definition : definitions) {
            RailCarBoundingDummyEntity dummy = new RailCarBoundingDummyEntity(level(), this,
                    definition.width(), definition.height());
            dummy.setPos(transformRenderOffset(definition.offset()));
            level().addFreshEntity(dummy);
            boundingDummies.add(dummy);
        }
    }

    private void updateBoundingDummies() {
        DummyConfig[] definitions = getDummies();
        if (boundingDummies.size() != definitions.length) {
            boundingDummies.removeIf(Entity::isRemoved);
            return;
        }
        for (int index = 0; index < definitions.length; index++) {
            RailCarBoundingDummyEntity dummy = boundingDummies.get(index);
            if (!dummy.isRemoved()) {
                dummy.setPos(transformRenderOffset(definitions[index].offset()));
            }
        }
    }

    Vec3 getBoundingDummyPosition(RailCarBoundingDummyEntity dummy) {
        int index = boundingDummies.indexOf(dummy);
        DummyConfig[] definitions = getDummies();
        return index >= 0 && index < definitions.length ? transformRenderOffset(definitions[index].offset()) : position();
    }

    private Vec3 transformRenderOffset(Vec3 offset) {
        Vec3 pitched = rotateX(offset, getXRot());
        Vec3 rotated = rotateY(pitched, getYRot());
        return new Vec3(renderX + rotated.x, renderY + rotated.y, renderZ + rotated.z);
    }

    private void updateRenderPosition() {
        lastRenderX = renderX;
        lastRenderY = renderY;
        lastRenderZ = renderZ;
        BlockPos anchor = getCurrentAnchorPos();
        Vec3 front = getRelPosAlongRail(anchor, getLengthSpan(),
                new HbmRail.MoveContext(HbmRail.RailCheckType.FRONT, getCollisionSpan() - getLengthSpan()));
        Vec3 back = getRelPosAlongRail(anchor, -getLengthSpan(),
                new HbmRail.MoveContext(HbmRail.RailCheckType.BACK, getCollisionSpan() - getLengthSpan()));
        if (front != null && back != null) {
            renderX = (front.x + back.x) / 2.0D;
            renderY = (front.y + back.y) / 2.0D;
            renderZ = (front.z + back.z) / 2.0D;
        } else {
            renderX = getX();
            renderY = getY();
            renderZ = getZ();
        }
    }

    @Nullable
    public Vec3 getRelPosAlongRail(BlockPos anchor, double distance, HbmRail.MoveContext context) {
        return HbmRailTraversal.travel(level(), anchor, distance, getGauge(), position(), getYRot(), context);
    }

    @Nullable
    static Vec3 getRelPosAlongRail(Level level, BlockPos anchor, double distance, HbmRail.TrackGauge gauge,
            Vec3 position, float yaw, HbmRail.MoveContext context) {
        return HbmRailTraversal.travel(level, anchor, distance, gauge, position, yaw, context);
    }

    public BlockPos getCurrentAnchorPos() {
        return BlockPos.containing(getX(), getY() + 0.25D, getZ());
    }

    public void derail() {
        onRail = false;
    }

    public boolean isOnRail() {
        return onRail;
    }

    public double getRenderX() { return renderX; }
    public double getRenderY() { return renderY; }
    public double getRenderZ() { return renderZ; }
    public double getLastRenderX() { return lastRenderX; }
    public double getLastRenderY() { return lastRenderY; }
    public double getLastRenderZ() { return lastRenderZ; }
    public double getCachedSpeed() { return cachedSpeed; }
    void setCachedSpeed(double speed) { cachedSpeed = speed; }

    public double getCouplingDist(TrainCoupling coupling) {
        return 0.0D;
    }

    @Nullable
    public Vec3 getCouplingPos(TrainCoupling coupling) {
        double distance = getCouplingDist(coupling);
        if (distance <= 0.0D) {
            return null;
        }
        if (coupling == TrainCoupling.BACK) {
            distance *= -1.0D;
        }
        Vec3 rotated = rotateY(new Vec3(0.0D, 0.0D, distance), getYRot());
        return new Vec3(renderX + rotated.x, renderY + rotated.y, renderZ + rotated.z);
    }

    @Nullable
    public LegacyRailCarEntity getCoupledTo(@Nullable TrainCoupling coupling) {
        return coupling == TrainCoupling.FRONT ? coupledFront : coupling == TrainCoupling.BACK ? coupledBack : null;
    }

    @Nullable
    public TrainCoupling getCouplingFrom(@Nullable LegacyRailCarEntity other) {
        return other == coupledFront ? TrainCoupling.FRONT : other == coupledBack ? TrainCoupling.BACK : null;
    }

    public void couple(@Nullable TrainCoupling coupling, @Nullable LegacyRailCarEntity target) {
        if (coupling == TrainCoupling.FRONT) {
            coupledFront = target;
        } else if (coupling == TrainCoupling.BACK) {
            coupledBack = target;
        }
    }

    @Nullable LogicalTrainUnit logicalTrainUnit() { return logicalTrainUnit; }
    void setLogicalTrainUnit(@Nullable LogicalTrainUnit unit, int index) { logicalTrainUnit = unit; logicalTrainIndex = index; }
    int logicalTrainIndex() { return logicalTrainIndex; }
    void dissolveLogicalTrain() { if (logicalTrainUnit != null) logicalTrainUnit.dissolveTrain(); }

    public abstract double getCurrentSpeed();
    public abstract double getMaxRailSpeed();
    public abstract HbmRail.TrackGauge getGauge();
    public abstract double getLengthSpan();
    public abstract double getCollisionSpan();

    public DummyConfig[] getDummies() {
        return new DummyConfig[0];
    }

    public record DummyConfig(float width, float height, Vec3 offset) {
    }

    public static float generateYaw(Vec3 front, Vec3 back) {
        return HbmRailTraversal.yawBetween(front, back);
    }

    static void setRailPose(LegacyRailCarEntity train, Vec3 front, Vec3 back) {
        train.renderX = (front.x + back.x) / 2.0D;
        train.renderY = (front.y + back.y) / 2.0D;
        train.renderZ = (front.z + back.z) / 2.0D;
        float yaw = generateYaw(front, back);
        Vec3 delta = front.subtract(back);
        float pitch = (float) (Math.asin(delta.y / delta.length()) * 180.0D / Math.PI);
        train.yRotO = train.getYRot();
        train.setYRot(yaw);
        train.setXRot(pitch);
        train.setDeltaMovement(yaw / 360.0D, pitch / 360.0D, 0.0D);
        train.hasImpulse = true;
    }

    /** Placement-facing access to the exact old bogie-derived pose setup. */
    public final void snapRailPose(Vec3 front, Vec3 back) {
        setRailPose(this, front, back);
    }

    static Vec3 rotateY(Vec3 vector, float yaw) {
        double radians = -yaw * Math.PI / 180.0D;
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        return new Vec3(vector.x * cosine + vector.z * sine, vector.y, vector.z * cosine - vector.x * sine);
    }

    static Vec3 rotateX(Vec3 vector, float pitch) {
        double radians = pitch * Math.PI / 180.0D;
        double cosine = Math.cos(radians);
        double sine = Math.sin(radians);
        return new Vec3(vector.x, vector.y * cosine + vector.z * sine, vector.z * cosine - vector.y * sine);
    }

    /** Server-level one-shot movement pass, called after individual entity ticks. */
    public static void updateMotion(ServerLevel level) {
        Set<LogicalTrainUnit> units = new HashSet<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LegacyRailCarEntity railCar && railCar.logicalTrainUnit != null) {
                units.add(railCar.logicalTrainUnit);
            }
        }
        for (LogicalTrainUnit unit : units) {
            unit.updateMotion();
        }
    }
}
