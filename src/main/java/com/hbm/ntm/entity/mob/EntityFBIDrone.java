package com.hbm.ntm.entity.mob;

import com.hbm.ntm.entity.projectile.DynamiteStickEntity;
import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.UniversalGrenadeItem.Filling;
import com.hbm.ntm.item.UniversalGrenadeItem.Fuze;
import com.hbm.ntm.item.UniversalGrenadeItem.Shell;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Source-backed {@code EntityUFOBase}/{@code EntityFBIDrone} flight contract.
 * The legacy type had no spawn-table registration; this class therefore owns
 * only its runtime/summon behavior, not a new natural-spawn rule.
 */
public class EntityFBIDrone extends Monster {
    private static final EntityDataAccessor<Integer> WAYPOINT_X =
            SynchedEntityData.defineId(EntityFBIDrone.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAYPOINT_Y =
            SynchedEntityData.defineId(EntityFBIDrone.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WAYPOINT_Z =
            SynchedEntityData.defineId(EntityFBIDrone.class, EntityDataSerializers.INT);

    private int scanCooldown;
    private int courseChangeCooldown;
    private int attackCooldown;
    private Player legacyTarget;

    public EntityFBIDrone(EntityType<? extends EntityFBIDrone> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 35.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(WAYPOINT_X, 0);
        entityData.define(WAYPOINT_Y, 0);
        entityData.define(WAYPOINT_Z, 0);
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            legacyServerTick();
        }
        setNoGravity(true);
        super.tick();
    }

    private void legacyServerTick() {
        if (level().getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }

        // EntityUFOBase clears every prior motion before it chooses a course.
        setDeltaMovement(Vec3.ZERO);
        if (legacyTarget != null && !legacyTarget.isAlive()) {
            legacyTarget = null;
        }

        if (scanCooldown <= 0) {
            scanForLegacyTarget();
            scanCooldown = 100;
        }
        if (courseChangeCooldown <= 0) {
            setLegacyCourse();
        }

        if (courseChangeCooldown > 0) {
            courseChangeCooldown--;
        }
        if (scanCooldown > 0) {
            scanCooldown--;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        attackWithLegacyGrenade();

        if (courseChangeCooldown > 0) {
            approachLegacyWaypoint(legacyTarget == null ? 0.25D : 0.5D);
        }
    }

    private void scanForLegacyTarget() {
        AABB range = getBoundingBox().inflate(100.0D, 50.0D, 100.0D);
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player player : level().getEntitiesOfClass(Player.class, range)) {
            if (!player.isAlive() || player.getAbilities().instabuild || player.isInvisible()) {
                continue;
            }
            double distance = distanceToSqr(player);
            if (distance < closestDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        legacyTarget = closest;
    }

    private void setLegacyCourse() {
        if (legacyTarget != null) {
            setLegacyTargetCourse();
            courseChangeCooldown = 20 + random.nextInt(20);
            return;
        }

        int x = Mth.floor(getX() + random.nextGaussian() * 5.0D);
        int z = Mth.floor(getZ() + random.nextGaussian() * 5.0D);
        setLegacyWaypoint(x, surfaceY(x, z) + 7 + random.nextInt(4), z);
        courseChangeCooldown = 60 + random.nextInt(20);
    }

    private void setLegacyTargetCourse() {
        double vectorX = getX() - legacyTarget.getX();
        double vectorZ = getZ() - legacyTarget.getZ();
        float rotation = (float) (Math.PI * 2.0D * random.nextFloat());
        double cosine = Mth.cos(rotation);
        double sine = Mth.sin(rotation);
        double rotatedX = vectorX * cosine + vectorZ * sine;
        double rotatedZ = vectorZ * cosine - vectorX * sine;
        double length = Math.sqrt(rotatedX * rotatedX + rotatedZ * rotatedZ);
        if (length <= 0.0D) {
            setLegacyWaypoint(Mth.floor(getX()), Mth.floor(legacyTarget.getY()) + 7 + random.nextInt(4),
                    Mth.floor(getZ()));
            return;
        }

        double overshoot = 10.0D + random.nextDouble() * 10.0D;
        int x = Mth.floor(legacyTarget.getX() - rotatedX / length * overshoot);
        int z = Mth.floor(legacyTarget.getZ() - rotatedZ / length * overshoot);
        setLegacyWaypoint(x, Math.max(surfaceY(x, z), Mth.floor(legacyTarget.getY())) + 7 + random.nextInt(4), z);
    }

    private void approachLegacyWaypoint(double speed) {
        double deltaX = waypointX() - getX();
        double deltaY = waypointY() - getY();
        double deltaZ = waypointZ() - getZ();
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (length <= 5.0D) {
            return;
        }
        if (isLegacyCourseTraversable(deltaX, deltaY, deltaZ, length)) {
            setDeltaMovement(deltaX * speed / length, deltaY * speed / length, deltaZ * speed / length);
        } else {
            courseChangeCooldown = 0;
        }
    }

    private boolean isLegacyCourseTraversable(double deltaX, double deltaY, double deltaZ, double length) {
        double stepX = deltaX / length;
        double stepY = deltaY / length;
        double stepZ = deltaZ / length;
        AABB collisionBox = getBoundingBox();
        for (int step = 1; step < length; step++) {
            collisionBox = collisionBox.move(stepX, stepY, stepZ);
            if (!level().noCollision(this, collisionBox)) {
                return false;
            }
        }
        return true;
    }

    private void attackWithLegacyGrenade() {
        if (legacyTarget == null || attackCooldown > 0) {
            return;
        }
        double deltaX = getX() - legacyTarget.getX();
        double deltaY = getY() - legacyTarget.getY();
        double deltaZ = getZ() - legacyTarget.getZ();
        if (Math.abs(deltaX) >= 5.0D || Math.abs(deltaZ) >= 5.0D || deltaY <= 3.0D) {
            return;
        }

        attackCooldown = 60;
        DynamiteStickEntity grenade = new DynamiteStickEntity(level(), this);
        grenade.setItem(UniversalGrenadeItem.make(Shell.FRAG, Filling.HE, Fuze.S7));
        grenade.setPos(getX(), getY(), getZ());
        level().addFreshEntity(grenade);
    }

    private int surfaceY(int x, int z) {
        return level().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    private void setLegacyWaypoint(int x, int y, int z) {
        entityData.set(WAYPOINT_X, x);
        entityData.set(WAYPOINT_Y, y);
        entityData.set(WAYPOINT_Z, z);
    }

    private int waypointX() { return entityData.get(WAYPOINT_X); }
    private int waypointY() { return entityData.get(WAYPOINT_Y); }
    private int waypointZ() { return entityData.get(WAYPOINT_Z); }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }
}
