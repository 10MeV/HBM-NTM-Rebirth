package com.hbm.ntm.entity.item;

import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class RubberBoatEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_TIME_SINCE_HIT =
            SynchedEntityData.defineId(RubberBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FORWARD_DIRECTION =
            SynchedEntityData.defineId(RubberBoatEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE_TAKEN =
            SynchedEntityData.defineId(RubberBoatEntity.class, EntityDataSerializers.FLOAT);

    private boolean boatEmpty = true;
    private double speedMultiplier = 0.07D;
    private int lerpSteps;
    private double lerpX;
    private double lerpY;
    private double lerpZ;
    private double lerpYaw;
    private double lerpPitch;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    public float prevRenderYaw;

    public RubberBoatEntity(EntityType<? extends RubberBoatEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
    }

    public RubberBoatEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.RUBBER_BOAT.get(), level);
        setPos(x, y + getBbHeight() / 2.0F, z);
        setDeltaMovement(Vec3.ZERO);
        xo = x;
        yo = y;
        zo = z;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_TIME_SINCE_HIT, 0);
        entityData.define(DATA_FORWARD_DIRECTION, 1);
        entityData.define(DATA_DAMAGE_TAKEN, 0.0F);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return entity.canBeCollidedWith();
    }

    @Override
    public double getPassengersRidingOffset() {
        return -0.3D;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        }
        if (!level().isClientSide && !isRemoved()) {
            setForwardDirection(-getForwardDirection());
            setTimeSinceHit(10);
            setDamageTaken(getDamageTaken() + amount * 10.0F);
            markHurt();
            boolean creative = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
            if (creative || getDamageTaken() > 40.0F) {
                ejectPassengers();
                if (!creative) {
                    dropBoat();
                }
                discard();
            }
        }
        return true;
    }

    @Override
    public void animateHurt(float yaw) {
        setForwardDirection(-getForwardDirection());
        setTimeSinceHit(10);
        setDamageTaken(getDamageTaken() * 11.0F);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        if (boatEmpty) {
            lerpSteps = steps;
        } else {
            double dx = x - getX();
            double dy = y - getY();
            double dz = z - getZ();
            if (dx * dx + dy * dy + dz * dz <= 1.0D) {
                return;
            }
            lerpSteps = 3;
        }
        lerpX = x;
        lerpY = y;
        lerpZ = z;
        lerpYaw = yaw;
        lerpPitch = pitch;
        setDeltaMovement(velocityX, velocityY, velocityZ);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
        setDeltaMovement(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        if (getTimeSinceHit() > 0) {
            setTimeSinceHit(getTimeSinceHit() - 1);
        }
        if (getDamageTaken() > 0.0F) {
            setDamageTaken(getDamageTaken() - 1.0F);
        }

        xo = getX();
        yo = getY();
        zo = getZ();

        double water = waterCoverage();
        if (level().isClientSide && boatEmpty) {
            clientEmptyTick();
        } else {
            legacyPhysicsTick(water);
        }

        spawnLegacySplash();
    }

    private void clientEmptyTick() {
        Vec3 motion = getDeltaMovement();
        if (lerpSteps > 0) {
            double x = getX() + (lerpX - getX()) / lerpSteps;
            double y = getY() + (lerpY - getY()) / lerpSteps;
            double z = getZ() + (lerpZ - getZ()) / lerpSteps;
            double yaw = Mth.wrapDegrees(lerpYaw - getYRot());
            setYRot((float) (getYRot() + yaw / lerpSteps));
            setXRot((float) (getXRot() + (lerpPitch - getXRot()) / lerpSteps));
            --lerpSteps;
            setPos(x, y, z);
        } else {
            setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
            if (onGround()) {
                motion = motion.scale(0.5D);
            }
            setDeltaMovement(passiveDecelerate(motion));
        }
    }

    private void legacyPhysicsTick(double water) {
        Vec3 motion = getDeltaMovement();
        if (water < 1.0D) {
            motion = motion.add(0.0D, 0.04D * (water * 2.0D - 1.0D), 0.0D);
        } else {
            if (motion.y < 0.0D) {
                motion = new Vec3(motion.x, motion.y / 2.0D, motion.z);
            }
            motion = motion.add(0.0D, 0.007000000216066837D, 0.0D);
        }

        double previousSpeed = horizontalSpeed(motion);
        setDeltaMovement(motion);
        hasImpulse = false;

        Entity controlling = getControllingPassenger();
        if (controlling instanceof LivingEntity living) {
            motion = applyRiderInput(motion, living);
        } else {
            motion = motion.scale(0.95D);
        }

        double speed = horizontalSpeed(motion);
        if (speed > 0.5D) {
            double clamp = 0.5D / speed;
            motion = new Vec3(motion.x * clamp, motion.y, motion.z * clamp);
            speed = 0.5D;
        }

        if (speed > previousSpeed && speedMultiplier < 0.5D) {
            speedMultiplier += (0.5D - speedMultiplier) / 50.0D;
            if (speedMultiplier > 0.5D) {
                speedMultiplier = 0.5D;
            }
        } else {
            speedMultiplier -= (speedMultiplier - 0.07D) / 35.0D;
            if (speedMultiplier < 0.07D) {
                speedMultiplier = 0.07D;
            }
        }

        clearLegacySurfaceBlocks();

        if (onGround()) {
            motion = motion.scale(0.5D);
        }

        setDeltaMovement(motion);
        move(MoverType.SELF, motion);
        motion = getDeltaMovement();

        if (horizontalCollision && previousSpeed > 0.2D) {
            motion = motion.scale(0.25D);
        } else {
            motion = passiveDecelerate(motion);
        }
        setDeltaMovement(motion);

        setXRot(0.0F);
        if (!(getControllingPassenger() instanceof LivingEntity)) {
            alignYawToMotion();
        }
        setRot(getYRot(), getXRot());

        if (!level().isClientSide) {
            collideWithBoats();
            if (getControllingPassenger() != null && !getControllingPassenger().isAlive()) {
                ejectPassengers();
            }
        }
    }

    private Vec3 applyRiderInput(Vec3 motion, LivingEntity rider) {
        float forward = rider.zza;
        float strafing = rider.xxa;
        if (forward != 0.0F || strafing != 0.0F) {
            double radians = -((getYRot() + 90.0D) * Math.PI / 180.0D);
            Vec3 direction = new Vec3(-Math.sin(radians), 0.0D, Math.cos(radians));
            motion = motion.add(direction.x * speedMultiplier * forward * 0.05D,
                    0.0D, direction.z * speedMultiplier * forward * 0.05D);
            float previousYaw = getYRot();
            setYRot(getYRot() - strafing * 3.0F);
            float deltaYaw = getYRot() - previousYaw;
            double rotate = -deltaYaw * Math.PI / 180.0D;
            double cos = Math.cos(rotate);
            double sin = Math.sin(rotate);
            motion = new Vec3(motion.x * cos + motion.z * sin, motion.y,
                    motion.z * cos - motion.x * sin);
            hasImpulse = true;
        }
        return motion;
    }

    private double waterCoverage() {
        int slices = 5;
        double coverage = 0.0D;
        AABB box = getBoundingBox();
        for (int index = 0; index < slices; ++index) {
            double minY = box.minY + (box.maxY - box.minY) * index / slices - 0.125D;
            double maxY = box.minY + (box.maxY - box.minY) * (index + 1) / slices - 0.125D;
            AABB slice = new AABB(box.minX, minY, box.minZ, box.maxX, maxY, box.maxZ);
            if (isAabbInWater(slice)) {
                coverage += 1.0D / slices;
            }
        }
        return coverage;
    }

    private boolean isAabbInWater(AABB box) {
        int minX = Mth.floor(box.minX);
        int maxX = Mth.ceil(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.ceil(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.ceil(box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    pos.set(x, y, z);
                    if (level().getFluidState(pos).is(Fluids.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void clearLegacySurfaceBlocks() {
        for (int index = 0; index < 4; ++index) {
            int x = Mth.floor(getX() + ((index % 2) - 0.5D) * 0.8D);
            int z = Mth.floor(getZ() + ((index / 2) - 0.5D) * 0.8D);
            for (int yOff = 0; yOff < 2; ++yOff) {
                BlockPos pos = new BlockPos(x, Mth.floor(getY()) + yOff, z);
                if (level().getBlockState(pos).is(Blocks.SNOW)) {
                    level().removeBlock(pos, false);
                    horizontalCollision = false;
                } else if (level().getBlockState(pos).is(Blocks.LILY_PAD)) {
                    level().destroyBlock(pos, true);
                    horizontalCollision = false;
                }
            }
        }
    }

    private void alignYawToMotion() {
        double yaw = getYRot();
        double deltaX = xo - getX();
        double deltaZ = zo - getZ();
        if (deltaX * deltaX + deltaZ * deltaZ > 0.001D) {
            yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI);
        }
        double rotation = Mth.wrapDegrees(yaw - getYRot());
        if (rotation > 20.0D) {
            rotation = 20.0D;
        }
        if (rotation < -20.0D) {
            rotation = -20.0D;
        }
        setYRot((float) (getYRot() + rotation));
    }

    private void collideWithBoats() {
        List<Entity> entities = level().getEntities(this, getBoundingBox().inflate(0.2D, 0.0D, 0.2D));
        for (Entity entity : entities) {
            if (entity != getControllingPassenger() && entity.isPushable()
                    && (entity instanceof RubberBoatEntity || entity instanceof Boat)) {
                entity.push(this);
            }
        }
    }

    private void spawnLegacySplash() {
        double deltaX = xo - getX();
        double deltaZ = zo - getZ();
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (speed > 0.2625D) {
            double cosYaw = Math.cos(getYRot() * Math.PI / 180.0D);
            double sinYaw = Math.sin(getYRot() * Math.PI / 180.0D);
            for (double j = 0; j < 1.0D + speed * 60.0D; ++j) {
                double offset = random.nextFloat() * 2.0F - 1.0F;
                double side = (random.nextInt(2) * 2 - 1) * 0.7D;
                double x;
                double z;
                if (random.nextBoolean()) {
                    x = getX() - cosYaw * offset * 0.8D + sinYaw * side;
                    z = getZ() - sinYaw * offset * 0.8D - cosYaw * side;
                } else {
                    x = getX() + cosYaw + sinYaw * offset * 0.7D;
                    z = getZ() + sinYaw - cosYaw * offset * 0.7D;
                }
                level().addParticle(net.minecraft.core.particles.ParticleTypes.SPLASH,
                        x, getY() - 0.125D, z, deltaX, 0.1D, deltaZ);
            }
        }
    }

    private static Vec3 passiveDecelerate(Vec3 motion) {
        return new Vec3(motion.x * 0.99D, motion.y * 0.95D, motion.z * 0.99D);
    }

    private static double horizontalSpeed(Vec3 motion) {
        return Math.sqrt(motion.x * motion.x + motion.z * motion.z);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (hasPassenger(passenger)) {
            double offsetX = Math.cos(getYRot() * Math.PI / 180.0D) * 0.4D;
            double offsetZ = Math.sin(getYRot() * Math.PI / 180.0D) * 0.4D;
            callback.accept(passenger, getX() + offsetX,
                    getY() + getPassengersRidingOffset() + passenger.getMyRidingOffset(),
                    getZ() + offsetZ);
            if (passenger instanceof Player player) {
                player.yBodyRot = Mth.wrapDegrees(getYRot() + 90.0F);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (getControllingPassenger() instanceof Player passenger && passenger != player) {
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (!level().isClientSide) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state,
            BlockPos pos) {
        if (onGround) {
            if (fallDistance > 5.0F) {
                causeFallDamage(fallDistance, 1.0F, damageSources().fall());
                if (!level().isClientSide && !isRemoved()) {
                    discard();
                    dropBoat();
                }
                fallDistance = 0.0F;
            }
        } else if (!level().getFluidState(blockPosition().below()).is(Fluids.WATER) && y < 0.0D) {
            fallDistance = (float) (fallDistance - y);
        }
    }

    public void dropBoat() {
        spawnAtLocation(new ItemStack(ModItems.BOAT_RUBBER.get()), 0.0F);
    }

    public void setDamageTaken(float amount) {
        entityData.set(DATA_DAMAGE_TAKEN, amount);
    }

    public float getDamageTaken() {
        return entityData.get(DATA_DAMAGE_TAKEN);
    }

    public void setTimeSinceHit(int time) {
        entityData.set(DATA_TIME_SINCE_HIT, time);
    }

    public int getTimeSinceHit() {
        return entityData.get(DATA_TIME_SINCE_HIT);
    }

    public void setForwardDirection(int direction) {
        entityData.set(DATA_FORWARD_DIRECTION, direction);
    }

    public int getForwardDirection() {
        return entityData.get(DATA_FORWARD_DIRECTION);
    }

    public void setBoatEmpty(boolean empty) {
        boatEmpty = empty;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
