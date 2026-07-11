package com.hbm.ntm.entity.missile;

import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class BobmazonDeliveryEntity extends Entity {
    private static final EntityDataAccessor<Integer> STATUS =
            SynchedEntityData.defineId(BobmazonDeliveryEntity.class, EntityDataSerializers.INT);

    private ItemStack payload = ItemStack.EMPTY;

    public BobmazonDeliveryEntity(EntityType<? extends BobmazonDeliveryEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noCulling = true;
    }

    public BobmazonDeliveryEntity(Level level) {
        this(ModEntityTypes.BOBMAZON_DELIVERY.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(STATUS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, -0.5D, 0.0D);

        for (int i = 0; i < 4; i++) {
            BlockPos sample = BlockPos.containing(getX() - 0.5D, getY() + 1.0D, getZ() - 0.5D);
            if (!level().getBlockState(sample).isAir() && !level().isClientSide && entityData.get(STATUS) != 1) {
                impact();
                break;
            }
            setPos(getX(), getY() - 0.5D, getZ());
        }

        if (level().isClientSide) {
            ParticleUtil.spawnExhaust(level(), getX(), getY() + 1.0D, getZ(), "meteor", 1, 0.0D);
        }
    }

    public void setPayload(ItemStack payload) {
        this.payload = payload == null ? ItemStack.EMPTY : payload.copy();
    }

    public ItemStack payload() {
        return payload.copy();
    }

    private void impact() {
        ExplosionLarge.spawnParticles(level(), getX(), getY() + 1.0D, getZ(), 50);
        level().playSound(null, getX(), getY(), getZ(), ModSounds.ENTITY_OLD_EXPLOSION.get(),
                SoundSource.BLOCKS, 10.0F, 0.5F + random.nextFloat() * 0.1F);
        if (!payload.isEmpty()) {
            ItemEntity pack = new ItemEntity(level(), getX(), getY() + 2.0D, getZ(), payload.copy());
            pack.setDeltaMovement(0.0D, pack.getDeltaMovement().y, 0.0D);
            level().addFreshEntity(pack);
        }
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        payload = tag.contains("payload", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("payload"))
                : ItemStack.EMPTY;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (!payload.isEmpty()) {
            tag.put("payload", payload.save(new CompoundTag()));
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 500000.0D;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
