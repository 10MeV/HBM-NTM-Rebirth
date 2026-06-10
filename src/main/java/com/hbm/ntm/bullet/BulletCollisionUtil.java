package com.hbm.ntm.bullet;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BulletCollisionUtil {
    public static final double ENTITY_SEARCH_INFLATE = 1.0D;
    public static final double ENTITY_HITBOX_INFLATE = 0.3D;

    public static CollisionScan scan(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, Vec3 position, Vec3 motion, int ticksInAir) {
        return scan(config, level, projectile, shooter, position, motion, ticksInAir, 0.0F);
    }

    public static CollisionScan scan(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, Vec3 position, Vec3 motion, int ticksInAir, float acceleration) {
        AABB bounds = projectile == null ? defaultBounds(position) : projectile.getBoundingBox();
        return scan(config, level, projectile, shooter, bounds, position, motion, ticksInAir, acceleration);
    }

    public static CollisionScan scan(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, AABB projectileBounds, Vec3 position, Vec3 motion, int ticksInAir) {
        return scan(config, level, projectile, shooter, projectileBounds, position, motion, ticksInAir, 0.0F);
    }

    public static CollisionScan scan(BulletConfig config, Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, AABB projectileBounds, Vec3 position, Vec3 motion, int ticksInAir,
            float acceleration) {
        if (config == null || level == null || position == null || motion == null) {
            return CollisionScan.NONE;
        }

        AABB bounds = projectileBounds == null ? defaultBounds(position) : projectileBounds;
        Vec3 movement = BulletKinematicsUtil.movementDelta(config, motion, acceleration);
        Vec3 end = position.add(movement);
        BlockCollision blockHit = config.spectral() ? null : findBlockHit(level, projectile, position, end);
        Vec3 clippedEnd = blockHit == null ? end : blockHit.location();
        List<EntityCollision> entityHits = level.isClientSide()
                ? Collections.emptyList()
                : findEntityHits(level, projectile, shooter, bounds, position, clippedEnd, movement, ticksInAir,
                        config.selfDamageDelay());
        EntityCollision nearest = config.penetrates() ? null : nearest(entityHits);
        PrimaryHit primary = nearest != null ? PrimaryHit.ENTITY : blockHit != null ? PrimaryHit.BLOCK : PrimaryHit.MISS;
        Vec3 primaryLocation = nearest != null ? nearest.location() : blockHit != null ? blockHit.location() : null;
        return new CollisionScan(position, end, clippedEnd, movement, blockHit,
                Collections.unmodifiableList(entityHits), nearest, primary, primaryLocation);
    }

    @Nullable
    public static BlockCollision findBlockHit(Level level, @Nullable Entity projectile, Vec3 start, Vec3 end) {
        if (level == null || start == null || end == null) {
            return null;
        }

        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, projectile));
        if (hit.getType() == HitResult.Type.MISS) {
            return null;
        }

        boolean portal = level.getBlockState(hit.getBlockPos()).is(Blocks.NETHER_PORTAL);
        return new BlockCollision(hit, hit.getLocation(), hit.getBlockPos(), hit.getDirection(), portal);
    }

    public static List<EntityCollision> findEntityHits(Level level, @Nullable Entity projectile,
            @Nullable Entity shooter, AABB projectileBounds, Vec3 start, Vec3 end, Vec3 movement, int ticksInAir,
            int selfDamageDelay) {
        if (level == null || projectileBounds == null || start == null || end == null || movement == null) {
            return Collections.emptyList();
        }

        AABB search = projectileBounds.expandTowards(movement).inflate(ENTITY_SEARCH_INFLATE);
        List<Entity> candidates = projectile == null
                ? level.getEntitiesOfClass(Entity.class, search, entity -> canHit(entity, null, shooter, ticksInAir,
                        selfDamageDelay))
                : level.getEntities(projectile, search, entity -> canHit(entity, projectile, shooter, ticksInAir,
                        selfDamageDelay));
        List<EntityCollision> hits = new ArrayList<>();
        for (Entity entity : candidates) {
            Vec3 hit = entity.getBoundingBox().inflate(ENTITY_HITBOX_INFLATE).clip(start, end).orElse(null);
            if (hit != null) {
                hits.add(new EntityCollision(entity, hit, start.distanceToSqr(hit)));
            }
        }
        return hits.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(hits);
    }

    private static boolean canHit(Entity entity, @Nullable Entity projectile, @Nullable Entity shooter,
            int ticksInAir, int selfDamageDelay) {
        return entity != null
                && entity != projectile
                && entity.isAlive()
                && entity.isPickable()
                && (entity != shooter || ticksInAir >= selfDamageDelay);
    }

    @Nullable
    private static EntityCollision nearest(List<EntityCollision> hits) {
        EntityCollision nearest = null;
        for (EntityCollision hit : hits) {
            if (nearest == null || hit.distanceSqr() < nearest.distanceSqr()) {
                nearest = hit;
            }
        }
        return nearest;
    }

    private static AABB defaultBounds(Vec3 position) {
        double half = BulletKinematicsUtil.ENTITY_SIZE * 0.5D;
        return new AABB(position.x - half, position.y - half, position.z - half,
                position.x + half, position.y + half, position.z + half);
    }

    public enum PrimaryHit {
        MISS,
        BLOCK,
        ENTITY
    }

    public record CollisionScan(Vec3 start, Vec3 end, Vec3 clippedEnd, Vec3 movement, @Nullable BlockCollision blockHit,
            List<EntityCollision> entityHits, @Nullable EntityCollision nearestEntityHit, PrimaryHit primaryHit,
            @Nullable Vec3 primaryLocation) {
        public static final CollisionScan NONE = new CollisionScan(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO,
                null, Collections.emptyList(), null, PrimaryHit.MISS, null);
    }

    public record BlockCollision(BlockHitResult hit, Vec3 location, net.minecraft.core.BlockPos blockPos,
            net.minecraft.core.Direction side, boolean portal) {
    }

    public record EntityCollision(Entity entity, Vec3 location, double distanceSqr) {
    }

    private BulletCollisionUtil() {
    }
}
