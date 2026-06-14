package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ShrapnelEntity extends LegacyThrowableEntity {
    private static final EntityDataAccessor<Byte> MODE =
            SynchedEntityData.defineId(ShrapnelEntity.class, EntityDataSerializers.BYTE);
    private static final byte MODE_NORMAL = 0;
    private static final byte MODE_TRAIL = 1;
    private static final byte MODE_VOLCANO = 2;
    private static final byte MODE_WATZ = 3;
    private static final byte MODE_RAD_VOLCANO = 4;

    public ShrapnelEntity(EntityType<? extends ShrapnelEntity> type, Level level) {
        super(type, level);
    }

    public ShrapnelEntity(Level level) {
        this(ModEntityTypes.SHRAPNEL.get(), level);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && mode() == MODE_TRAIL) {
            ParticleUtil.spawnLegacyShrapnelTrailFlame(level(), getX(), getY(), getZ());
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) {
            EntityDamageUtil.attackEntityFromNt(entityHit.getEntity(), ModDamageSources.indirect(level(), ModDamageSources.SHRAPNEL, this, getOwner()), 15.0F);
        }
        if (tickCount <= 5) {
            return;
        }
        if (level() instanceof ServerLevel serverLevel) {
            byte currentMode = mode();
            if (currentMode == MODE_VOLCANO || currentMode == MODE_RAD_VOLCANO) {
                placeVolcanicImpact(serverLevel, hit, currentMode == MODE_RAD_VOLCANO);
            } else if (currentMode == MODE_WATZ) {
                placeWatzImpact(serverLevel, hit);
            } else {
                spawnLavaSplash(serverLevel, hit);
            }
            LegacySoundPlayer.playSoundEffect(level(), getX(), getY(), getZ(),
                    "random.fizz", SoundSource.BLOCKS, 1.0F, 1.0F);
            discard();
        }
    }

    private void placeVolcanicImpact(ServerLevel level, HitResult hit, boolean radioactive) {
        BlockPos impact = new BlockPos(hitBlockX(hit), hitBlockY(hit), hitBlockZ(hit));
        BlockPos above = impact.above();
        if (getDeltaMovement().y < -0.2D) {
            BlockState lava = (radioactive ? ModBlocks.RAD_LAVA_BLOCK : ModBlocks.VOLCANIC_LAVA_BLOCK).get().defaultBlockState();
            if (canReplace(level, above)) {
                level.setBlock(above, lava, 3);
            }
            for (int xx = -1; xx <= 1; xx++) {
                for (int yy = 0; yy <= 2; yy++) {
                    for (int zz = -1; zz <= 1; zz++) {
                        BlockPos gasPos = impact.offset(xx, yy, zz);
                        if (level.getBlockState(gasPos).isAir()) {
                            level.setBlock(gasPos, ModBlocks.GAS_MONOXIDE.get().defaultBlockState(), 3);
                        }
                    }
                }
            }
        } else if (getDeltaMovement().y > 0.0D) {
            ExplosionNT.ExAttrib lavaAttrib = radioactive ? ExplosionNT.ExAttrib.LAVA_R : ExplosionNT.ExAttrib.LAVA_V;
            new ExplosionNT(level, null, impact.getX() + 0.5D, impact.getY() + 0.5D, impact.getZ() + 0.5D, 7.0F)
                    .addAttrib(ExplosionNT.ExAttrib.NODROP)
                    .addAttrib(lavaAttrib)
                    .addAttrib(ExplosionNT.ExAttrib.NOSOUND)
                    .addAttrib(ExplosionNT.ExAttrib.ALLMOD)
                    .addAttrib(ExplosionNT.ExAttrib.NOHURT)
                    .explode();
        }
    }

    private void placeWatzImpact(ServerLevel level, HitResult hit) {
        BlockPos above = new BlockPos(hitBlockX(hit), hitBlockY(hit) + 1, hitBlockZ(hit));
        if (canReplace(level, above)) {
            level.setBlock(above, ModBlocks.MUD_BLOCK.get().defaultBlockState(), 3);
        }
    }

    private void spawnLavaSplash(ServerLevel level, HitResult hit) {
        ParticleUtil.spawnLegacyShrapnelLavaSplash(level, hit.getLocation());
    }

    private static boolean canReplace(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }

    public void setTrail() {
        setMode(MODE_TRAIL);
    }

    public void setVolcano() {
        setMode(MODE_VOLCANO);
    }

    public void setWatz() {
        setMode(MODE_WATZ);
    }

    public void setRadVolcano() {
        setMode(MODE_RAD_VOLCANO);
    }

    public boolean isLargeRenderMode() {
        return mode() >= MODE_VOLCANO;
    }

    public byte mode() {
        return entityData.get(MODE);
    }

    private void setMode(byte mode) {
        entityData.set(MODE, mode);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, MODE_NORMAL);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setMode(tag.getByte("mode"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("mode", mode());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
