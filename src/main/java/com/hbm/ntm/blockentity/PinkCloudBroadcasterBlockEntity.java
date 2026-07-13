package com.hbm.ntm.blockentity;

import com.hbm.lib.ModDamageSource;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.AudioWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class PinkCloudBroadcasterBlockEntity extends BlockEntity {
    private AudioWrapper audio;
    public PinkCloudBroadcasterBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.BROADCASTER_PC.get(), pos, state); }
    public static void tick(Level level, BlockPos pos, BlockState state, PinkCloudBroadcasterBlockEntity broadcaster) {
        if (!level.isClientSide) { broadcaster.applyEffects(); } else { broadcaster.keepAudioAlive(); }
    }
    private void applyEffects() {
        AABB range = new AABB(worldPosition).inflate(25.0D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, range)) {
            double distance = living.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D);
            distance = Math.sqrt(distance);
            if (distance <= 25.0D && (living.getEffect(MobEffects.CONFUSION) == null || living.getEffect(MobEffects.CONFUSION).getDuration() < 100)) {
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
            }
            if (distance <= 15.0D) living.hurt(ModDamageSource.source(level, ModDamageSource.broadcast), (float) ((15.0D - distance) / 15.0D * 10.0D));
        }
    }
    private void keepAudioAlive() {
        if (audio == null) {
            int variant = new java.util.Random(worldPosition.getX() + worldPosition.getY() + worldPosition.getZ()).nextInt(3) + 1;
            audio = AudioWrapper.getLoopedSound(level, new ResourceLocation(HbmNtm.MOD_ID, "block.broadcast" + variant),
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 25.0F, 25.0F, 1.0F, 20);
            audio.startSound();
        } else if (!audio.isPlaying()) audio.startSound();
        audio.keepAlive();
    }
    @Override public void setRemoved() { super.setRemoved(); if (audio != null) { audio.stopSound(); audio = null; } }
    @Override public void onChunkUnloaded() { super.onChunkUnloaded(); if (audio != null) { audio.stopSound(); audio = null; } }
}
