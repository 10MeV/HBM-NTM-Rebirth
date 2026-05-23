package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.RegistryObject;

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
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) {
            entityHit.getEntity().hurt(ModDamageSources.shrapnel(level()), 15.0F);
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
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
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
        RegistryObject<? extends Block> mud = ModBlocks.legacyBlock("mud_block");
        if (mud == null) {
            return;
        }
        BlockPos above = new BlockPos(hitBlockX(hit), hitBlockY(hit) + 1, hitBlockZ(hit));
        if (canReplace(level, above)) {
            level.setBlock(above, mud.get().defaultBlockState(), 3);
        }
    }

    private void spawnLavaSplash(ServerLevel level, HitResult hit) {
        level.sendParticles(ParticleTypes.LAVA, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                5, 0.15D, 0.15D, 0.15D, 0.0D);
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

    private byte mode() {
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
}
