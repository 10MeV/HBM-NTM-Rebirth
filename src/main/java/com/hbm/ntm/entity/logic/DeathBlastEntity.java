package com.hbm.ntm.entity.logic;

import com.hbm.ntm.bullet.LegacyBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class DeathBlastEntity extends Entity {
    public static final int MAX_AGE = 60;
    private static final int NUCLEAR_RADIUS = 40;
    private static final int MASKMAN_BOLT_COUNT = 100;
    private static final double MASKMAN_BOLT_HORIZONTAL_SPEED = 0.2D;
    private static final double MASKMAN_BOLT_VERTICAL_SPEED = -0.01D;

    public DeathBlastEntity(EntityType<? extends DeathBlastEntity> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public DeathBlastEntity(Level level) {
        this(ModEntityTypes.DEATH_BLAST.get(), level);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        // EntityDeathBlast#onUpdate deliberately omitted super.onUpdate().
        // Its ticksExisted therefore never advances on its own, including for
        // the SatelliteLaser-created runtime entity.
        if (!level().isClientSide && tickCount >= MAX_AGE) {
            discard();
            detonateLegacyPayload();
            ParticleUtil.spawnNuclearBurstVisual(level(), getX(), getY() + 0.5D, getZ(), ParticleUtil.TYPE_MUKE, false);
            LegacySoundPlayer.playLegacyMukeExplosion(level(), getX(), getY(), getZ(), 25.0F, 0.9F);
        }
    }

    /**
     * {@code RenderDeathBlast#renderOrb} reads the legacy entity age directly.
     * In particular, it does not interpolate that value with a render partial
     * tick.
     */
    public int legacyRenderAge() {
        return tickCount;
    }

    private void detonateLegacyPayload() {
        level().addFreshEntity(NukeExplosionMk5Entity.statFacNoRad(level(), NUCLEAR_RADIUS, getX(), getY(), getZ()));
        for (int i = 0; i < MASKMAN_BOLT_COUNT; i++) {
            double angle = 2.0D * Math.PI * i / MASKMAN_BOLT_COUNT;
            // 1.7.10 Vec3(0.2, 0, 0).rotateAroundY(angle): z is negative sine.
            BulletProjectileEntity bolt = new BulletProjectileEntity(level());
            bolt.setConfig(LegacyBulletConfigs.MASKMAN_BOLT);
            bolt.setPos(getX(), getY() + 2.0D, getZ());
            bolt.setDeltaMovement(MASKMAN_BOLT_HORIZONTAL_SPEED * Math.cos(angle), MASKMAN_BOLT_VERTICAL_SPEED,
                    -MASKMAN_BOLT_HORIZONTAL_SPEED * Math.sin(angle));
            level().addFreshEntity(bolt);
        }
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
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}
