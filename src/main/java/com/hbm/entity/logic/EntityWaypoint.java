package com.hbm.entity.logic;

import com.hbm.config.MobConfig;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.entity.mob.glyphid.EntityGlyphidScout;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Source-backed 1.7.10 Glyphid task waypoint. */
public class EntityWaypoint extends Entity {
    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(EntityWaypoint.class, EntityDataSerializers.INT);

    public int maxAge = 2400;
    public int radius = 3;
    public boolean highPriority;
    private EntityWaypoint additional;
    private boolean hasSpawned;

    public EntityWaypoint(EntityType<? extends EntityWaypoint> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public EntityWaypoint(Level level) {
        this(ModEntityTypes.GLYPHID_WAYPOINT.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TYPE, EntityGlyphid.TASK_IDLE);
    }

    public int getWaypointType() {
        return entityData.get(DATA_TYPE);
    }

    public void setWaypointType(int waypointType) {
        entityData.set(DATA_TYPE, waypointType);
    }

    public void setHighPriority() {
        highPriority = true;
    }

    public void setAdditionalWaypoint(EntityWaypoint waypoint) {
        additional = waypoint;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= maxAge) {
            discard();
            return;
        }
        if (level().isClientSide()) {
            spawnLegacyDebugEffect();
            return;
        }
        if (tickCount % 40 != 0) {
            return;
        }

        AABB box = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(radius);
        for (Entity entity : level().getEntities(this, box)) {
            if (!(entity instanceof EntityGlyphid glyphid)) {
                continue;
            }
            if (additional != null && !hasSpawned) {
                level().addFreshEntity(additional);
                hasSpawned = true;
            }
            boolean exception = glyphid.getWaypoint() != this
                    || glyphid instanceof EntityGlyphidScout
                    || glyphid.isNuclearGlyphid();
            if (!exception) {
                glyphid.setCurrentTask(getWaypointType(), additional);
            }
            if (getWaypointType() == EntityGlyphid.TASK_BUILD_HIVE) {
                if (glyphid instanceof EntityGlyphidScout) {
                    discard();
                }
            } else {
                discard();
            }
        }
    }

    private void spawnLegacyDebugEffect() {
        if (!MobConfig.waypointDebug()) {
            return;
        }
        AABB box = new AABB(getX(), getY(), getZ(), getX(), getY(), getZ()).inflate(radius);
        double x = box.minX + (random.nextDouble() - 0.5D) * (box.maxX - box.minX);
        double y = box.minY + random.nextDouble() * (box.maxY - box.minY);
        double z = box.minZ + (random.nextDouble() - 0.5D) * (box.maxZ - box.minZ);
        ParticleUtil.spawnCoolingTower(level(), x, y, z, 0.5F, 0.75F, 2.0F, 50 + random.nextInt(10),
                false, 0.075F, 0.25F, getColor());
    }

    private int getColor() {
        return switch (getWaypointType()) {
            case EntityGlyphid.TASK_RETREAT_FOR_REINFORCEMENTS -> 0x5FA6E8;
            case EntityGlyphid.TASK_BUILD_HIVE, EntityGlyphid.TASK_INITIATE_RETREAT -> 0x127766;
            default -> 0x566573;
        };
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setWaypointType(tag.getInt("type"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("type", getWaypointType());
    }
}
