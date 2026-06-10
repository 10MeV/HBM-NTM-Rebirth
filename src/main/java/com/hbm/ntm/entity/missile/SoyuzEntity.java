package com.hbm.ntm.entity.missile;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteChipItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class SoyuzEntity extends Entity {
    public static final int MODE_SATELLITE = 0;
    public static final int MODE_CAPSULE = 1;
    private static final int PAYLOAD_SLOTS = 18;

    private static final EntityDataAccessor<Integer> SKIN =
            SynchedEntityData.defineId(SoyuzEntity.class, EntityDataSerializers.INT);

    private double acceleration;
    private int mode;
    private int targetX;
    private int targetZ;
    private boolean exhaustSoundPlayed;
    private final NonNullList<ItemStack> payload = NonNullList.withSize(PAYLOAD_SLOTS, ItemStack.EMPTY);

    public SoyuzEntity(EntityType<? extends SoyuzEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noCulling = true;
        noPhysics = true;
    }

    public SoyuzEntity(Level level) {
        this(ModEntityTypes.SOYUZ.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SKIN, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && tickCount == 1) {
            level().playSound(null, getX(), getY(), getZ(), ModSounds.ENTITY_SOYUZ_TAKEOFF.get(),
                    SoundSource.PLAYERS, 100.0F, 1.1F);
        }

        if (getDeltaMovement().y < 2.0D) {
            acceleration += 0.00025D;
            setDeltaMovement(0.0D, getDeltaMovement().y + acceleration, 0.0D);
        }

        setPos(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y, getZ() + getDeltaMovement().z);

        if (!level().isClientSide) {
            burnExhaustTargets();
        } else {
            spawnExhaust();
        }

        if (getY() > 600.0D) {
            deployPayload();
        }
    }

    private void burnExhaustTargets() {
        List<Entity> entities = level().getEntities(this,
                new AABB(getX() - 5.0D, getY() - 15.0D, getZ() - 5.0D,
                        getX() + 5.0D, getY(), getZ() + 5.0D));
        for (Entity entity : entities) {
            entity.setSecondsOnFire(15);
            EntityDamageUtil.attackEntityFromNt(entity,
                    ModDamageSources.source(level(), ModDamageSources.EXHAUST, this), 100.0F, true, true,
                    0.0D, 0.0F, 0.0F);
            if (entity instanceof Player && !exhaustSoundPlayed) {
                exhaustSoundPlayed = true;
                level().playSound(null, getX(), getY(), getZ(), ModSounds.ALARM_SOYUZED.get(),
                        SoundSource.RECORDS, 100.0F, 1.0F);
            }
        }
    }

    private void spawnExhaust() {
        double width = random.nextDouble() * 0.25D - 0.5D;
        ParticleUtil.spawnExhaustSoyuz(level(), getX(), getY(), getZ(), 1, width);
        ParticleUtil.spawnExhaustSoyuz(level(), getX() + 2.75D, getY(), getZ(), 1, width);
        ParticleUtil.spawnExhaustSoyuz(level(), getX() - 2.75D, getY(), getZ(), 1, width);
        ParticleUtil.spawnExhaustSoyuz(level(), getX(), getY(), getZ() + 2.75D, 1, width);
        ParticleUtil.spawnExhaustSoyuz(level(), getX(), getY(), getZ() - 2.75D, 1, width);
    }

    private void deployPayload() {
        if (level() instanceof ServerLevel serverLevel) {
            if (mode == MODE_SATELLITE) {
                ItemStack load = payload.get(0);
                if (!load.isEmpty() && load.getItem() instanceof SatelliteChipItem satelliteItem
                        && satelliteItem.isLaunchableSatellite()) {
                    int frequency = ISatelliteChip.getFrequencyFromStack(load);
                    Satellite.orbit(serverLevel, satelliteItem.satelliteType().legacyId(), frequency, getX(), getY(), getZ());
                }
            } else if (mode == MODE_CAPSULE) {
                SoyuzCapsuleEntity capsule = new SoyuzCapsuleEntity(serverLevel);
                capsule.setSkin(skin());
                capsule.setPayload(payload);
                capsule.setPos(targetX + 0.5D, 600.0D, targetZ + 0.5D);
                serverLevel.getChunk(capsule.blockPosition());
                serverLevel.addFreshEntity(capsule);
            }
        }
        discard();
    }

    public void setSatellitePayload(ItemStack stack) {
        payload.set(0, stack.copy());
    }

    public void setPayload(List<ItemStack> stacks) {
        for (int slot = 0; slot < Math.min(PAYLOAD_SLOTS, stacks.size()); slot++) {
            payload.set(slot, stacks.get(slot).copy());
        }
    }

    public int mode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int targetX() {
        return targetX;
    }

    public int targetZ() {
        return targetZ;
    }

    public void setTarget(int x, int z) {
        targetX = x;
        targetZ = z;
    }

    public int skin() {
        return entityData.get(SKIN);
    }

    public void setSkin(int skin) {
        entityData.set(SKIN, Math.max(0, Math.min(SoyuzRocketItem.SKIN_COUNT - 1, skin)));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSkin(tag.getInt("skin"));
        targetX = tag.getInt("targetX");
        targetZ = tag.getInt("targetZ");
        mode = tag.getInt("mode");
        HbmItemStackUtil.loadLegacyOrForgeItems(tag, payload);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("skin", skin());
        tag.putInt("targetX", targetX);
        tag.putInt("targetZ", targetZ);
        tag.putInt("mode", mode);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, payload);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
