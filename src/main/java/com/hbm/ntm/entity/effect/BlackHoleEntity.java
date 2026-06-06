package com.hbm.ntm.entity.effect;

import com.hbm.ntm.entity.projectile.RubbleEntity;
import com.hbm.ntm.damage.EntityDamageUtil;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class BlackHoleEntity extends Entity {
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

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);
        setDeltaMovement(getDeltaMovement().scale(0.99D));
    }

    protected void breakBlocks(float size) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int rays = Math.max(1, Mth.ceil(size * 2.0F));
        int length = Math.max(1, Mth.ceil(size * 15.0F));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int ray = 0; ray < rays; ray++) {
            Vec3 direction = randomSphereDirection();
            for (int i = 0; i < length; i++) {
                cursor.set(Mth.floor(getX() + direction.x * i),
                        Mth.floor(getY() + direction.y * i),
                        Mth.floor(getZ() + direction.z * i));
                BlockState state = serverLevel.getBlockState(cursor);
                if (state.getFluidState().getType() != Fluids.EMPTY) {
                    serverLevel.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
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

    private Vec3 randomSphereDirection() {
        double phi = random.nextDouble() * Math.PI * 2.0D;
        double cosTheta = random.nextDouble() * 2.0D - 1.0D;
        double theta = Math.acos(cosTheta);
        double x = Math.sin(theta) * Math.cos(phi);
        double y = Math.sin(theta) * Math.sin(phi);
        double z = Math.cos(theta);
        return new Vec3(x, y, z);
    }

    protected void pullEntities(float size) {
        double range = size * 15.0D;
        AABB bounds = getBoundingBox().inflate(range);
        List<Entity> entities = level().getEntities(this, bounds, entity -> entity.isAlive() && !entity.isSpectator());
        for (Entity entity : entities) {
            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }
            if (entity instanceof FallingBlockEntity fallingBlock && !level().isClientSide && fallingBlock.tickCount > 1) {
                convertFallingBlockToRubble(fallingBlock);
            }

            Vec3 pull = position().subtract(entity.position());
            double distance = pull.length();
            if (distance <= 0.0001D || distance > range) {
                continue;
            }
            Vec3 direction = pull.normalize();
            if (!(entity instanceof ItemEntity)) {
                direction = rotateAroundY(direction, Math.toRadians(15.0D));
            }

            entity.setDeltaMovement(entity.getDeltaMovement().add(direction.x * 0.1D, direction.y * 0.2D, direction.z * 0.1D));
            entity.hurtMarked = true;

            if (entity instanceof BlackHoleEntity) {
                continue;
            }
            if (distance < size * 1.5D) {
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

    private static Vec3 rotateAroundY(Vec3 vec, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private boolean annihilatesBlackHole(ItemStack stack) {
        return isLegacyItem(stack, "pellet_antimatter") || isLegacyItem(stack, "flame_pony");
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
        return distance < 25000.0D;
    }
}
