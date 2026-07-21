package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Direct 1.7.10 EntityBoxcar fall, impact, shock, damage and placement contract. */
public class BoxcarEntity extends Entity {
    private static final double PARTICLE_RANGE = 150.0D;

    public BoxcarEntity(EntityType<? extends BoxcarEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public BoxcarEntity(Level level) {
        this(ModEntityTypes.BOXCAR.get(), level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            return;
        }
        if (tickCount == 1) {
            spawnArrivalParticles();
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        setDeltaMovement(motion.x, Math.max(-1.5D, motion.y - 0.03D), motion.z);

        BlockPos impact = BlockPos.containing(getX(), getY(), getZ());
        if (!level().getBlockState(impact).isAir()) {
            impact(impact);
        }
    }

    private void spawnArrivalParticles() {
        for (int i = 0; i < 50; i++) {
            CompoundTag data = new CompoundTag();
            data.putString("type", ParticleUtil.TYPE_BALEFIRE_CLOUD);
            ParticleUtil.spawnAux(level(),
                    getX() + (random.nextDouble() - 0.5D) * 3.0D,
                    getY() + (random.nextDouble() - 0.5D) * 15.0D,
                    getZ() + (random.nextDouble() - 0.5D) * 3.0D,
                    data, PARTICLE_RANGE);
        }
    }

    private void impact(BlockPos impact) {
        LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(), "hbm:weapon.trainImpact",
                SoundSource.BLOCKS, 100.0F, 1.0F);
        discard();
        ExplosionLarge.spawnShock(level(), getX(), getY() + 1.0D, getZ(), 24, 3.0D);
        ExplosionLarge.spawnShock(level(), getX(), getY() + 1.0D, getZ(), 24, 2.5D);
        ExplosionLarge.spawnShock(level(), getX(), getY() + 1.0D, getZ(), 24, 2.0D);

        AABB damageArea = new AABB(getX() - 2.0D, getY() - 2.0D, getZ() - 2.0D,
                getX() + 2.0D, getY() + 2.0D, getZ() + 2.0D);
        for (Entity entity : level().getEntities(this, damageArea)) {
            entity.hurt(ModDamageSources.source(level(), ModDamageSources.BOXCAR), 1000.0F);
        }
        BlockPos placement = BlockPos.containing(getX(), getY() + 0.5D, getZ());
        level().setBlock(placement, ModBlocks.BOXCAR.get().defaultBlockState(), 3);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
