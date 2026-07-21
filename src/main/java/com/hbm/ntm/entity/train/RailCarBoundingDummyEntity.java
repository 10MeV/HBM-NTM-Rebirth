package com.hbm.ntm.entity.train;

import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Invisible, dynamic collision member of a custom rail car.  The old train
 * did not use its render-sized entity box for collision; it spawned these
 * independently moving boxes instead.
 */
public final class RailCarBoundingDummyEntity extends Entity {
    private static final EntityDataAccessor<Integer> TRAIN_ID = SynchedEntityData.defineId(
            RailCarBoundingDummyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> BOX_WIDTH = SynchedEntityData.defineId(
            RailCarBoundingDummyEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BOX_HEIGHT = SynchedEntityData.defineId(
            RailCarBoundingDummyEntity.class, EntityDataSerializers.FLOAT);

    private LegacyRailCarEntity train;

    public RailCarBoundingDummyEntity(EntityType<? extends RailCarBoundingDummyEntity> type, Level level) {
        super(type, level);
        noPhysics = false;
    }

    public RailCarBoundingDummyEntity(Level level, LegacyRailCarEntity train, float width, float height) {
        this(ModEntityTypes.RAIL_CAR_BOUNDING_DUMMY.get(), level);
        this.train = train;
        entityData.set(TRAIN_ID, train.getId());
        setBoxSize(width, height);
    }

    public void setBoxSize(float width, float height) {
        entityData.set(BOX_WIDTH, width);
        entityData.set(BOX_HEIGHT, height);
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(entityData.get(BOX_WIDTH), entityData.get(BOX_HEIGHT));
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(TRAIN_ID, 0);
        entityData.define(BOX_WIDTH, 1.0F);
        entityData.define(BOX_HEIGHT, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (train == null) {
            Entity owner = level().getEntity(entityData.get(TRAIN_ID));
            if (owner instanceof LegacyRailCarEntity railCar) {
                train = railCar;
            }
        }
        if (train == null || train.isRemoved()) {
            discard();
            return;
        }
        setPos(train.getBoundingDummyPosition(this));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return train == null ? InteractionResult.PASS : train.interact(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return train != null && train.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // Legacy writeToNBTOptional returned false: these entities are rebuilt by their train.
    }
}
