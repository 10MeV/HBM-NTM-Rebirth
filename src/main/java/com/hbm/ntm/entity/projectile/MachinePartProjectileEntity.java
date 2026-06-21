package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class MachinePartProjectileEntity extends LegacyThrowableEntity {
    private static final EntityDataAccessor<Integer> ORIENTATION =
            SynchedEntityData.defineId(MachinePartProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> META =
            SynchedEntityData.defineId(MachinePartProjectileEntity.class, EntityDataSerializers.INT);

    protected MachinePartProjectileEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    protected MachinePartProjectileEntity(EntityType<?> type, Level level, double x, double y, double z) {
        this(type, level);
        setPos(x, y, z);
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            int orientation = getOrientation();
            if (orientation >= 6 && !inGround) {
                setOrientation(orientation - 6);
            }
        }
        super.tick();
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity().isAlive()) {
            Entity target = entityHit.getEntity();
            EntityDamageUtil.attackEntityFromNt(target,
                    ModDamageSources.indirect(level(), ModDamageSources.RUBBLE, this, getOwner()), 1_000.0F);
            if (!target.isAlive() && target instanceof LivingEntity) {
                ParticleUtil.spawnGiblets(target, 0, 5);
                LegacySoundPlayer.playSoundAtEntity(target, "mob.zombie.woodbreak", SoundSource.NEUTRAL,
                        2.0F, 0.95F + level().random.nextFloat() * 0.2F);
            }
        }

        if (tickCount <= 1 || !(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        int orientation = getOrientation();
        if (orientation < 6) {
            if (getDeltaMovement().length() < 0.75D) {
                orientation += 6;
                setOrientation(orientation);
            } else {
                Direction side = blockHit.getDirection();
                Vec3 motion = getDeltaMovement();
                setDeltaMovement(
                        motion.x * (1 - Math.abs(side.getStepX()) * 2),
                        motion.y * (1 - Math.abs(side.getStepY()) * 2),
                        motion.z * (1 - Math.abs(side.getStepZ()) * 2));
                level().explode(this, getX(), getY(), getZ(), 3.0F, false, Level.ExplosionInteraction.NONE);
                destroyWeakImpactBlock(blockHit.getBlockPos());
            }
        }
        if (orientation >= 6) {
            setDeltaMovement(Vec3.ZERO);
            inGround = true;
        }
    }

    private void destroyWeakImpactBlock(BlockPos pos) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState state = serverLevel.getBlockState(pos);
        float resistance = state.getBlock().getExplosionResistance(state, serverLevel, pos,
                (Explosion) null);
        if (resistance < 50.0F) {
            serverLevel.destroyBlock(pos, false, this);
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!level().isClientSide) {
            ItemStack stack = getPickupStack();
            if (!stack.isEmpty() && player.getInventory().add(stack)) {
                discard();
                player.inventoryMenu.broadcastChanges();
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.25F;
    }

    @Override
    protected double getGravityVelocity() {
        return inGround ? 0.0D : 0.03D;
    }

    @Override
    protected int groundDespawn() {
        return 0;
    }

    public int getOrientation() {
        return entityData.get(ORIENTATION);
    }

    public MachinePartProjectileEntity setOrientation(int orientation) {
        entityData.set(ORIENTATION, orientation);
        return this;
    }

    public int getMeta() {
        return entityData.get(META);
    }

    public MachinePartProjectileEntity setMeta(int meta) {
        entityData.set(META, meta);
        return this;
    }

    protected abstract ItemStack getPickupStack();

    @Override
    protected void defineSynchedData() {
        entityData.define(ORIENTATION, 0);
        entityData.define(META, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOrientation(tag.getInt("rot"));
        setMeta(tag.getInt("meta"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("rot", getOrientation());
        tag.putInt("meta", getMeta());
    }

    public static int legacyOrientation(Direction direction) {
        return switch (direction) {
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 3;
        };
    }

    public static Direction legacyDownRotation(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }
}
