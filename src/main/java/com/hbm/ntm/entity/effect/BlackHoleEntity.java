package com.hbm.ntm.entity.effect;

import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.explosion.LegacyExplosionFluidCleanup;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class BlackHoleEntity extends Entity {
    private static final double LEGACY_PULL_SPEED = 0.1D;
    private static final double LEGACY_ENTITY_SWIRL_RADIANS = Math.PI / 12.0D;
    private static final double LEGACY_ENTITY_SWIRL_COS = Math.cos(LEGACY_ENTITY_SWIRL_RADIANS);
    private static final double LEGACY_ENTITY_SWIRL_SIN = Math.sin(LEGACY_ENTITY_SWIRL_RADIANS);
    private static final double MIN_PULL_DISTANCE_SQR = 1.0E-8D;
    private static final double MOTION_DAMPING = 0.99D;

    protected static final EntityDataAccessor<Float> SIZE =
            SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Boolean> BREAKS_BLOCKS =
            SynchedEntityData.defineId(BlackHoleEntity.class, EntityDataSerializers.BOOLEAN);

    public BlackHoleEntity(EntityType<? extends BlackHoleEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        blocksBuilding = false;
    }

    public BlackHoleEntity(Level level) {
        this(ModEntityTypes.BLACK_HOLE.get(), level);
    }

    public BlackHoleEntity(Level level, float size) {
        this(level);
        setSize(size);
    }

    public BlackHoleEntity noBreak() {
        entityData.set(BREAKS_BLOCKS, false);
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        float size = getSize();
        if (size <= 0.0F) {
            discard();
            return;
        }

        if (!level().isClientSide) {
            if (breaksBlocks()) {
                breakBlocks(size);
            }
            pullEntities(size);
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        setDeltaMovement(motion.x * MOTION_DAMPING, motion.y * MOTION_DAMPING, motion.z * MOTION_DAMPING);
    }

    protected void breakBlocks(float size) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int rays = Math.max(1, Mth.ceil(size * 2.0F));
        int length = Math.max(1, Mth.ceil(size * 15.0F));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        double originX = getX();
        double originY = getY();
        double originZ = getZ();
        for (int ray = 0; ray < rays; ray++) {
            double phi = random.nextDouble() * Math.PI * 2.0D;
            double cosTheta = random.nextDouble() * 2.0D - 1.0D;
            double theta = Math.acos(cosTheta);
            double directionX = Math.sin(theta) * Math.cos(phi);
            double directionY = Math.sin(theta) * Math.sin(phi);
            double directionZ = Math.cos(theta);
            for (int i = 0; i < length; i++) {
                cursor.set(Mth.floor(originX + directionX * i),
                        Mth.floor(originY + directionY * i),
                        Mth.floor(originZ + directionZ * i));
                if (serverLevel.isOutsideBuildHeight(cursor)) {
                    continue;
                }
                BlockState state = serverLevel.getBlockState(cursor);
                if (LegacyExplosionFluidCleanup.isLegacyLiquidBlock(state)) {
                    LegacyExplosionFluidCleanup.clearLegacyLiquidNeighborhood(serverLevel, cursor, 3);
                    continue;
                }
                if (state.isAir() || state.getDestroySpeed(serverLevel, cursor) < 0.0F) {
                    continue;
                }

                RubbleEntity rubble = new RubbleEntity(serverLevel);
                rubble.setPos(cursor.getX() + 0.5D, cursor.getY(), cursor.getZ() + 0.5D);
                rubble.setBlockState(state);
                serverLevel.addFreshEntity(rubble);
                serverLevel.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
                break;
            }
        }
    }

    protected void pullEntities(float size) {
        double range = size * 15.0D;
        double rangeSqr = range * range;
        double damageRange = size * 1.5D;
        double centerX = getX();
        double centerY = getY();
        double centerZ = getZ();
        AABB bounds = getBoundingBox().inflate(range);
        List<Entity> entities = level().getEntities(this, bounds, entity -> entity.isAlive() && !entity.isSpectator());
        for (Entity entity : entities) {
            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }
            if (entity instanceof FallingBlockEntity fallingBlock && !level().isClientSide && fallingBlock.tickCount > 1) {
                convertFallingBlockToRubble(fallingBlock);
            }

            double pullX = centerX - entity.getX();
            double pullY = centerY - entity.getY();
            double pullZ = centerZ - entity.getZ();
            double distanceSqr = pullX * pullX + pullY * pullY + pullZ * pullZ;
            if (distanceSqr <= MIN_PULL_DISTANCE_SQR || distanceSqr > rangeSqr) {
                continue;
            }
            double distance = Math.sqrt(distanceSqr);
            double directionX = pullX / distance;
            double directionY = pullY / distance;
            double directionZ = pullZ / distance;
            if (!(entity instanceof ItemEntity)) {
                double rotatedX = directionX * LEGACY_ENTITY_SWIRL_COS + directionZ * LEGACY_ENTITY_SWIRL_SIN;
                double rotatedZ = directionZ * LEGACY_ENTITY_SWIRL_COS - directionX * LEGACY_ENTITY_SWIRL_SIN;
                directionX = rotatedX;
                directionZ = rotatedZ;
            }

            Vec3 entityMotion = entity.getDeltaMovement();
            entity.setDeltaMovement(entityMotion.x + directionX * LEGACY_PULL_SPEED,
                    entityMotion.y + directionY * LEGACY_PULL_SPEED * 2.0D,
                    entityMotion.z + directionZ * LEGACY_PULL_SPEED);
            entity.hurtMarked = true;

            if (entity instanceof BlackHoleEntity) {
                continue;
            }
            if (distance < damageRange) {
                EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.blackhole(level(), this), 1000.0F);
                if (!(entity instanceof LivingEntity)) {
                    entity.discard();
                }
                if (entity instanceof ItemEntity item && annihilatesBlackHole(item.getItem())) {
                    discard();
                    level().explode(null, getX(), getY(), getZ(), 5.0F, Level.ExplosionInteraction.BLOCK);
                    return;
                }
            }
        }
    }

    private void convertFallingBlockToRubble(FallingBlockEntity fallingBlock) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState state = fallingBlock.getBlockState();
        Vec3 motion = fallingBlock.getDeltaMovement();
        RubbleEntity rubble = new RubbleEntity(serverLevel);
        rubble.setBlockState(state);
        rubble.moveTo(fallingBlock.getX(), fallingBlock.getY(), fallingBlock.getZ(), 0.0F, 0.0F);
        rubble.setDeltaMovement(motion);
        fallingBlock.discard();
        serverLevel.addFreshEntity(rubble);
    }

    private boolean annihilatesBlackHole(ItemStack stack) {
        return isLegacyItem(stack, "pellet_antimatter");
    }

    private static boolean isLegacyItem(ItemStack stack, String name) {
        Supplier<? extends net.minecraft.world.item.Item> item = ModItems.legacyItem(name);
        return item != null && stack.is(item.get());
    }

    public float getSize() {
        return entityData.get(SIZE);
    }

    protected void setSize(float size) {
        entityData.set(SIZE, size);
    }

    public boolean breaksBlocks() {
        return entityData.get(BREAKS_BLOCKS);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SIZE, 0.5F);
        entityData.define(BREAKS_BLOCKS, true);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSize(tag.getFloat("size"));
        entityData.set(BREAKS_BLOCKS, !tag.contains("breaksBlocks") || tag.getBoolean("breaksBlocks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("size", getSize());
        tag.putBoolean("breaksBlocks", breaksBlocks());
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}
