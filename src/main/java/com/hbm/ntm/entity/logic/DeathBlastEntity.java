package com.hbm.ntm.entity.logic;

import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacyBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class DeathBlastEntity extends Entity {
    public static final int MAX_AGE = 60;
    private static final int NUKE_RADIUS = 40;
    private static final int MASKMAN_BOLT_COUNT = 100;

    public DeathBlastEntity(EntityType<? extends DeathBlastEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public DeathBlastEntity(Level level) {
        this(ModEntityTypes.DEATH_BLAST.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount >= MAX_AGE) {
            discard();
            level().addFreshEntity(NukeExplosionMk5Entity.statFacNoRad(level(), NUKE_RADIUS, getX(), getY(), getZ()));
            spawnMaskmanBoltRing();
            ParticleUtil.spawnNuclearBurstVisual(level(), getX(), getY() + 0.5D, getZ(), ParticleUtil.TYPE_MUKE, false);
            level().playSound(null, getX(), getY(), getZ(), ModSounds.WEAPON_MUKE_EXPLOSION.get(),
                    SoundSource.BLOCKS, 25.0F, 0.9F);
        }
    }

    private void spawnMaskmanBoltRing() {
        Vec3 origin = new Vec3(getX(), getY() + 2.0D, getZ());
        for (int i = 0; i < MASKMAN_BOLT_COUNT; i++) {
            double angle = 2.0D * Math.PI * i / MASKMAN_BOLT_COUNT;
            Vec3 motion = legacyRotateY(0.2D, -0.01D, 0.0D, angle);
            BulletLaunchUtil.LaunchPlan base = BulletLaunchUtil.directedLaunchPlan(
                    LegacyBulletConfigs.MASKMAN_BOLT, origin, motion, 1.0F, 0.0F, random);
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.withMotion(base, motion);
            level().addFreshEntity(BulletProjectileEntity.fromLaunchPlan(level(), plan, null));
        }
    }

    private static Vec3 legacyRotateY(double x, double y, double z, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(x * cos + z * sin, y, z * cos - x * sin);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 25000.0D;
    }
}
