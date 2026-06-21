package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKDebrisPlanner;
import com.hbm.ntm.neutron.RBMKDebrisPlanner.RBMKDebrisPlan;
import com.hbm.ntm.neutron.RBMKDebrisPlanner.RBMKDebrisType;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

public class RBMKDebrisEntity extends Entity {
    private static final String TAG_DEBRIS_TYPE = "debtype";
    private static final String TAG_ROTATION = "rot";
    private static final EntityDataAccessor<Integer> DATA_TYPE =
            SynchedEntityData.defineId(RBMKDebrisEntity.class, EntityDataSerializers.INT);

    public float debrisRotation;
    public float debrisRotationO;

    public RBMKDebrisEntity(EntityType<? extends RBMKDebrisEntity> type, Level level) {
        super(type, level);
        debrisRotation = debrisRotationO = random.nextFloat() * 360.0F;
    }

    public RBMKDebrisEntity(Level level) {
        this(ModEntityTypes.RBMK_DEBRIS.get(), level);
    }

    public RBMKDebrisEntity(Level level, double x, double y, double z, RBMKDebrisType type) {
        this(level);
        setPos(x, y, z);
        setDebrisType(type);
    }

    @Override
    public void tick() {
        super.tick();
        RBMKDebrisPlan plan = plan();
        if (!level().isClientSide()) {
            traceLidBlockBreak();
            applyRadiationAura(plan);
            if (RBMKDebrisPlanner.shouldDespawn(tickCount, getId(), plan.lifetimeTicks(),
                    NeutronHandler.rbmkRuntimeSettings(level()).permanentScrap())) {
                discard();
                return;
            }
        }
        tickLegacyMotion();
    }

    private void traceLidBlockBreak() {
        Vec3 motion = getDeltaMovement();
        if (!RBMKDebrisPlanner.shouldTraceBlockBreak(plan().breaksBlocksOnUpwardHit(), motion.y)) {
            return;
        }

        Vec3 start = position();
        Vec3 end = start.add(motion.scale(2.0D));
        BlockHitResult hit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos origin = hit.getBlockPos();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    int rn = Math.abs(x) + Math.abs(y) + Math.abs(z);
                    if (rn <= 1 || random.nextInt(rn) == 0) {
                        level().setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        discard();
    }

    private void applyRadiationAura(RBMKDebrisPlan plan) {
        Integer amplifier = plan.radiationAmplifier();
        if (amplifier == null) {
            return;
        }
        AABB box = getBoundingBox().inflate(RBMKDebrisPlanner.RADIATION_RADIUS);
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, box)) {
            entity.addEffect(new MobEffectInstance(ModEffects.RADIATION.get(),
                    RBMKDebrisPlanner.RADIATION_DURATION_TICKS, amplifier));
        }
    }

    private void tickLegacyMotion() {
        Vec3 motion = getDeltaMovement().add(0.0D, -RBMKDebrisPlanner.GRAVITY, 0.0D);
        Vec3 before = position();
        move(MoverType.SELF, motion);

        double nextX = motion.x;
        double nextY = motion.y;
        double nextZ = motion.z;
        Vec3 moved = position().subtract(before);
        if (Math.abs(moved.x - motion.x) > 1.0E-7D) {
            nextX *= -0.75D;
        }
        if (Math.abs(moved.y - motion.y) > 1.0E-7D) {
            nextY = 0.0D;
        }
        if (Math.abs(moved.z - motion.z) > 1.0E-7D) {
            nextZ *= -0.75D;
        }

        debrisRotationO = debrisRotation;
        if (onGround()) {
            nextX *= RBMKDebrisPlanner.GROUND_HORIZONTAL_DAMPING;
            nextZ *= RBMKDebrisPlanner.GROUND_HORIZONTAL_DAMPING;
            nextY *= RBMKDebrisPlanner.GROUND_VERTICAL_BOUNCE;
        } else {
            debrisRotation += RBMKDebrisPlanner.AIR_ROTATION_STEP;
            if (debrisRotation >= 360.0F) {
                debrisRotation -= 360.0F;
                debrisRotationO -= 360.0F;
            }
        }
        setDeltaMovement(nextX, nextY, nextZ);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide && !isRemoved()) {
            ItemStack stack = pickupStack();
            if (!stack.isEmpty() && player.getInventory().add(stack)) {
                player.containerMenu.broadcastChanges();
                discard();
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private ItemStack pickupStack() {
        RegistryObject<Item> item = ModItems.legacyItem(plan().pickupItemId());
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        RBMKDebrisPlan plan = plan();
        return EntityDimensions.scalable(plan.width(), plan.height());
    }

    public void setDebrisType(RBMKDebrisType type) {
        RBMKDebrisType safeType = type == null ? RBMKDebrisType.BLANK : type;
        entityData.set(DATA_TYPE, safeType.ordinal());
        refreshDimensions();
    }

    public RBMKDebrisType getDebrisType() {
        return RBMKDebrisPlanner.rbmkTypeFromLegacyOrdinal(entityData.get(DATA_TYPE)).type();
    }

    private RBMKDebrisPlan plan() {
        return RBMKDebrisPlanner.rbmk(getDebrisType());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TYPE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(DATA_TYPE, RBMKDebrisPlanner.rbmkTypeFromLegacyOrdinal(tag.getInt(TAG_DEBRIS_TYPE)).legacyIndex());
        debrisRotation = tag.getFloat(TAG_ROTATION);
        debrisRotationO = debrisRotation;
        refreshDimensions();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt(TAG_DEBRIS_TYPE, entityData.get(DATA_TYPE));
        tag.putFloat(TAG_ROTATION, debrisRotation);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0D * 128.0D;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
