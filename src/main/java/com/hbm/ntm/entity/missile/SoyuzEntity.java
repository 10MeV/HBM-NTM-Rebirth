package com.hbm.ntm.entity.missile;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    // EntitySoyuz owns a normal nullable array.  Cargo-mode deployment hands
    // this very array to EntitySoyuzCapsule; it is not a NonNullList-backed
    // inventory with an EMPTY sentinel contract.
    private ItemStack[] payload = new ItemStack[PAYLOAD_SLOTS];

    public SoyuzEntity(EntityType<? extends SoyuzEntity> type, Level level) {
        super(type, level);
        noCulling = true;
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
        // EntitySoyuz overrides onUpdate without calling Entity#onUpdate. In
        // particular, modern base ticking would apply fluid pushing and mutate
        // motionX/motionZ even though the legacy rocket only accelerates Y.
        Vec3 motion = getDeltaMovement();
        if (motion.y < 2.0D) {
            acceleration += 0.00025D;
            // EntitySoyuz only increments motionY.  Keep an externally supplied
            // horizontal motion vector intact while the rocket accelerates.
            motion = new Vec3(motion.x, motion.y + acceleration, motion.z);
            setDeltaMovement(motion);
        }

        // EntitySoyuz used setLocationAndAngles(..., 0, 0) for every flight
        // update, so its stored flight rotations are reset rather than retaining
        // an arbitrary spawn or reload orientation.
        moveTo(getX() + motion.x, getY() + motion.y, getZ() + motion.z,
                0.0F, 0.0F);

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
                LegacySoundPlayer.playLegacySoyuzedAlarm(this);
            }
            if (entity instanceof Player player) {
                AchievementHandler.award(player, AchievementHandler.SOYUZ);
            }
        }
    }

    private void spawnExhaust() {
        spawnExhaustAt(getX(), getY(), getZ());
        spawnExhaustAt(getX() + 2.75D, getY(), getZ());
        spawnExhaustAt(getX() - 2.75D, getY(), getZ());
        spawnExhaustAt(getX(), getY(), getZ() + 2.75D);
        spawnExhaustAt(getX(), getY(), getZ() - 2.75D);
    }

    private void spawnExhaustAt(double x, double y, double z) {
        ParticleUtil.spawnExhaustSoyuz(level(), x, y, z, 1, random.nextDouble() * 0.25D - 0.5D);
    }

    private void deployPayload() {
        if (level() instanceof ServerLevel serverLevel) {
            if (mode == MODE_SATELLITE) {
                ItemStack load = payload[0];
                if (load != null && load.is(ModItems.SAT_FOEQ.get())) {
                    // EntitySoyuz awarded FOEQ before entering the generic
                    // ISatChip orbit path.  Keep this distinct source-backed
                    // launch-side award even though SatelliteRelay's onOrbit
                    // award is idempotent for the same players.
                    for (Player player : serverLevel.players()) {
                        AchievementHandler.award(player, AchievementHandler.FOEQ);
                    }
                }
                if (load != null && load.getItem() instanceof ISatelliteChip) {
                    int frequency = ISatelliteChip.getFrequencyFromStack(load);
                    Satellite.orbit(serverLevel, Satellite.getIDFromItem(load.getItem()), frequency,
                            getX(), getY(), getZ());
                }
            } else if (mode == MODE_CAPSULE) {
                SoyuzCapsuleEntity capsule = new SoyuzCapsuleEntity(serverLevel);
                capsule.setSkin(skin());
                capsule.payload = payload;
                capsule.setPos(targetX + 0.5D, 600.0D, targetZ + 0.5D);
                serverLevel.getChunk(capsule.blockPosition());
                serverLevel.addFreshEntity(capsule);
            }
        }
        discard();
    }

    public void setSat(ItemStack stack) {
        // EntitySoyuz#setSat stores the launcher's stack reference verbatim.
        // In particular, it does not create a detached payload copy before the
        // launcher clears its own slot.
        payload[0] = stack;
    }

    /** Modern call-site name retained as a direct bridge to the legacy API. */
    public void setSatellitePayload(ItemStack stack) {
        setSat(stack);
    }

    public void setPayload(List<ItemStack> stacks) {
        // Keep EntitySoyuz#setPayload's direct indexed assignment.  Do not
        // truncate an oversized source list or copy its stacks: both would
        // hide the legacy method's observable array/reference boundary.
        for (int slot = 0; slot < stacks.size(); slot++) {
            payload[slot] = stacks.get(slot);
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
        // EntitySoyuz's watcher accepts the persisted value verbatim.  The item
        // layer validates its own skin NBT, but entity reload must not silently
        // rewrite the old entity field: the legacy renderer maps every value
        // other than 1/2 to the default texture set.
        entityData.set(SKIN, skin);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return com.hbm.ntm.util.HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setSkin(tag.getInt("skin"));
        targetX = tag.getInt("targetX");
        targetZ = tag.getInt("targetZ");
        mode = tag.getInt("mode");
        // EntitySoyuz only overwrote slots present in its legacy "items"
        // list.  A second entity-NBT read therefore retains cargo from slots
        // absent in the new list rather than clearing the whole array first.
        HbmItemStackUtil.loadSlottedItems(tag.getList("items", net.minecraft.nbt.Tag.TAG_COMPOUND),
                "slot", payload);
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
