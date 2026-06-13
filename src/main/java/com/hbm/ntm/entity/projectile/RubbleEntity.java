package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ParticleBurstPacket;
import com.hbm.ntm.radiation.ModDamageSources;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RubbleEntity extends LegacyThrowableEntity {
    private static final EntityDataAccessor<Integer> BLOCK_STATE_ID =
            SynchedEntityData.defineId(RubbleEntity.class, EntityDataSerializers.INT);

    public RubbleEntity(EntityType<? extends RubbleEntity> type, Level level) {
        super(type, level);
    }

    public RubbleEntity(Level level) {
        this(ModEntityTypes.RUBBLE.get(), level);
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) {
            EntityDamageUtil.attackEntityFromNt(entityHit.getEntity(), ModDamageSources.indirect(level(), ModDamageSources.RUBBLE, this, getOwner()), 15.0F);
        }
        if (tickCount <= 2) {
            return;
        }
        if (level() instanceof ServerLevel serverLevel) {
            BlockPos pos = BlockPos.containing(hit.getLocation());
            LegacySoundPlayer.playSoundAtEntity(this, "hbm:block.debris", SoundSource.BLOCKS, 1.5F, 1.0F);
            ModMessages.sendToAllAround(new ParticleBurstPacket(pos, blockState()), serverLevel, getX(), getY(), getZ(), 50.0D);
            discard();
        }
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    public BlockState blockState() {
        BlockState state = Block.stateById(entityData.get(BLOCK_STATE_ID));
        return state == null ? Blocks.STONE.defaultBlockState() : state;
    }

    public void setBlockState(BlockState state) {
        entityData.set(BLOCK_STATE_ID, Block.getId(state));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BLOCK_STATE_ID, Block.getId(Blocks.STONE.defaultBlockState()));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBlockState(Block.stateById(tag.getInt("blockState")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("blockState", entityData.get(BLOCK_STATE_ID));
    }
}
