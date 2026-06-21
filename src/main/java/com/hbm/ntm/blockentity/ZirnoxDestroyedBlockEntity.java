package com.hbm.ntm.blockentity;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmBlockStateUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ZirnoxDestroyedBlockEntity extends BlockEntity {
    private static final double RADIATION_RANGE = 100.0D;
    private static final float BURNING_RADIATION = 500_000.0F;
    private static final float COOLED_RADIATION = 75_000.0F;
    private static final double FIRE_DAMAGE_RANGE = 5.0D;

    private boolean onFire = true;

    public ZirnoxDestroyedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZIRNOX_DESTROYED.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ZirnoxDestroyedBlockEntity core) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        core.radiate(serverLevel, pos);

        RandomSource random = serverLevel.random;
        if (core.onFire && random.nextInt(5000) == 0) {
            core.onFire = false;
            core.setChanged();
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        if (core.onFire && serverLevel.getGameTime() % 50L == 0L) {
            spawnFlame(serverLevel, pos, random);
        }
    }

    public static void spawnFlame(ServerLevel level, BlockPos pos, RandomSource random) {
        ParticleUtil.spawnRbmkFlame(level,
                pos.getX() + 0.25D + random.nextDouble() * 0.5D,
                pos.getY() + 1.75D,
                pos.getZ() + 0.25D + random.nextDouble() * 0.5D,
                90);
        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F);
    }

    public boolean isOnFire() {
        return onFire;
    }

    public void setOnFire(boolean onFire) {
        if (this.onFire != onFire) {
            this.onFire = onFire;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("onFire", onFire);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("fire")) {
            onFire = tag.getBoolean("fire");
        } else if (tag.contains("onFire")) {
            onFire = tag.getBoolean("onFire");
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-3, 0, -3), worldPosition.offset(4, 3, 4));
    }

    private void radiate(Level level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB area = new AABB(center, center).inflate(RADIATION_RANGE);
        float baseRads = onFire ? BURNING_RADIATION : COOLED_RADIATION;

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            Vec3 delta = entity.getEyePosition().subtract(center);
            double length = delta.length();
            if (length <= 0.0D) {
                continue;
            }

            Vec3 normal = delta.normalize();
            float resistance = 0.0F;
            for (int i = 1; i < length; i++) {
                BlockPos sample = BlockPos.containing(center.add(normal.scale(i)));
                resistance += HbmBlockStateUtil.explosionResistance(level.getBlockState(sample), level, sample);
            }
            if (resistance < 1.0F) {
                resistance = 1.0F;
            }

            float exposure = baseRads / resistance / (float) (length * length);
            RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, exposure);
            if (onFire && length < FIRE_DAMAGE_RANGE) {
                EntityDamageUtil.attackEntityFromNt(entity, level.damageSources().inFire(), 2.0F);
            }
        }
    }
}
