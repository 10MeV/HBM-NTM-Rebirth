package com.hbm.ntm.entity.effect;

import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.ContaminationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class DigammaSpearEntity extends Entity {
    private static final EntityDataAccessor<Integer> TICKS_IN_GROUND =
            SynchedEntityData.defineId(DigammaSpearEntity.class, EntityDataSerializers.INT);

    public int ticksInGround;

    public DigammaSpearEntity(EntityType<? extends DigammaSpearEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        blocksBuilding = false;
    }

    public DigammaSpearEntity(Level level) {
        this(ModEntityTypes.DIGAMMA_SPEAR.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TICKS_IN_GROUND, 0);
    }

    @Override
    public void tick() {
        xo = getX();
        yo = getY();
        zo = getZ();
        setDeltaMovement(0.0D, -0.2D, 0.0D);

        BlockPos pos = blockPosition();
        if (level().getBlockState(pos.below()).isAir()) {
            setPos(getX(), getY() - 0.2D, getZ());
            if (!level().isClientSide) {
                spawnFallingDigammaBlast((ServerLevel) level());
            } else {
                double y = level().getHeight(Heightmap.Types.WORLD_SURFACE,
                        Mth.floor(getX()), Mth.floor(getZ())) + 2.0D;
                ParticleUtil.spawnSmokeRadialDigamma(level(), getX(), y, getZ(), 5);
            }

            if (level().getBlockState(pos.below(3)).isAir()) {
                setTicksInGround(0);
            }
        } else {
            setTicksInGround(ticksInGround + 1);
            if (!level().isClientSide && ticksInGround > 100) {
                finishInGround((ServerLevel) level());
            }
        }
    }

    private void spawnFallingDigammaBlast(ServerLevel level) {
        double x = getX() + random.nextGaussian() * 25.0D;
        double z = getZ() + random.nextGaussian() * 25.0D;
        double y = level.getHeight(Heightmap.Types.WORLD_SURFACE, Mth.floor(x), Mth.floor(z)) + 2.0D;
        double horizontalDistance = new Vec3(x - getX(), 0.0D, z - getZ()).length();
        ExplosionNT.ExAttrib digamma = horizontalDistance < 20.0D
                ? ExplosionNT.ExAttrib.DIGAMMA_CIRCUIT
                : ExplosionNT.ExAttrib.DIGAMMA;

        new ExplosionNT(level, this, x, y, z, 7.5F)
                .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                .addAttrib(ExplosionNT.ExAttrib.NOPARTICLE)
                .addAttrib(ExplosionNT.ExAttrib.NODROP)
                .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                .addAttrib(digamma)
                .explode();

        for (ServerPlayer player : level.players()) {
            ContaminationUtil.contaminate(player,
                    ContaminationUtil.HazardType.DIGAMMA,
                    ContaminationUtil.ContaminationType.DIGAMMA,
                    0.05F);
            AchievementHandler.award(player, AchievementHandler.DIGAMMA_KAUAI_MOHO);
        }
    }

    private void finishInGround(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof LivingEntity living) {
                ContaminationUtil.contaminate(living,
                        ContaminationUtil.HazardType.DIGAMMA,
                        ContaminationUtil.ContaminationType.DIGAMMA2,
                        10.0F);
            }
        }
        discard();
        LegacySoundPlayer.playSoundEffect(level, getX(), getY(), getZ(), "hbm:weapon.dFlash", 25000.0F, 1.0F);
        ParticleUtil.spawnSmokeRadialDigamma(level, getX(), getY() + 7.0D, getZ(), 100);
    }

    private void setTicksInGround(int ticksInGround) {
        this.ticksInGround = ticksInGround;
        entityData.set(TICKS_IN_GROUND, ticksInGround);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (TICKS_IN_GROUND.equals(key)) {
            ticksInGround = entityData.get(TICKS_IN_GROUND);
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 25000.0D;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
