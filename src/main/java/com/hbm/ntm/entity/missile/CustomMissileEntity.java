package com.hbm.ntm.entity.missile;

import com.hbm.ntm.api.entity.LegacyMissileRadarProfile;
import com.hbm.ntm.explosion.CustomMissileExplosion;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.item.missile.CustomMissilePartProfile;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CustomMissileEntity extends MissileEntity {
    private static final EntityDataAccessor<String> WARHEAD =
            SynchedEntityData.defineId(CustomMissileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FUSELAGE =
            SynchedEntityData.defineId(CustomMissileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> FINS =
            SynchedEntityData.defineId(CustomMissileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> THRUSTER =
            SynchedEntityData.defineId(CustomMissileEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> FUEL =
            SynchedEntityData.defineId(CustomMissileEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CONSUMPTION =
            SynchedEntityData.defineId(CustomMissileEntity.class, EntityDataSerializers.FLOAT);

    private static final String TAG_WARHEAD = "warhead";
    private static final String TAG_FUSELAGE = "fuselage";
    private static final String TAG_FINS = "fins";
    private static final String TAG_THRUSTER = "thruster";
    private static final String TAG_FUEL = "fuel";
    private static final String TAG_CONSUMPTION = "consumption";

    public CustomMissileEntity(EntityType<? extends CustomMissileEntity> type, Level level) {
        super(type, level, Variant.GENERIC);
        setHealth(50.0F);
    }

    public void configureParts(CustomMissilePartProfile.Assembly assembly) {
        setPartValue(WARHEAD, assembly.warhead());
        setPartValue(FUSELAGE, assembly.fuselage());
        setPartValue(FINS, assembly.fins());
        setPartValue(THRUSTER, assembly.thruster());
        setFuel(assembly.fuselage().profile().fuel());
        setConsumption(assembly.thruster().profile().consumption());
    }

    @Override
    public void tick() {
        if (!level().isClientSide && hasPropulsion()) {
            setFuel(fuel() - consumption());
        }
        super.tick();
    }

    @Override
    protected boolean hasPropulsion() {
        return fuel() > 0.0F;
    }

    @Override
    protected void onMissileImpact(HitResult hit) {
        CustomMissilePartProfile warhead = warheadProfile();
        if (warhead == null || warhead.warheadType() == null) {
            return;
        }
        CustomMissileExplosion.explode(level(), getX(), getY(), getZ(), getDeltaMovement(),
                warhead.strength(), warhead.warheadType(), this);
    }

    @Override
    public void killMissile() {
        // EntityMissileCustom#killMissile marks itself dead before running its
        // destruction blast.  discard is deferred in 1.20.1, so use the base
        // immediate legacy-isDead bridge rather than isRemoved as the re-entry
        // guard while this explosion is still enumerating entities.
        if (beginDestruction()) {
            ExplosionLarge.explode(level(), getX(), getY(), getZ(), 5.0F, true, false, true, this);
            ExplosionLarge.spawnShrapnelShower(level(), getX(), getY(), getZ(),
                    getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z, 15, 0.075D, this);
        }
    }

    @Override
    protected void spawnContrail() {
        String type = contrailType();
        if (type == null) {
            return;
        }
        Vec3 motion = getDeltaMovement();
        Vec3 direction = motion.lengthSqr() > 1.0E-7D ? motion.normalize() : Vec3.ZERO;
        // EntityMissileCustom emits one particle per integer below its unscaled
        // EntityThrowableNT motion multiplier.  Using travelled distance changes
        // the plume whenever the base missile's velocity multiplier changes.
        for (int i = 0; i < flightVelocity(); i++) {
            CompoundTag data = new CompoundTag();
            data.putString("type", type);
            ParticleUtil.spawnAux(level(),
                    getX() - direction.x * i,
                    getY() - direction.y * i,
                    getZ() - direction.z * i,
                    data, 150.0D);
        }
    }

    @Override
    public LegacyMissileRadarProfile radarProfile() {
        CustomMissilePartProfile fuselage = fuselageProfile();
        if (fuselage == null) {
            return LegacyMissileRadarProfile.UNKNOWN;
        }
        CustomMissilePartProfile.PartSize top = fuselage.top();
        CustomMissilePartProfile.PartSize bottom = fuselage.bottom();
        if (top == CustomMissilePartProfile.PartSize.SIZE_10
                && bottom == CustomMissilePartProfile.PartSize.SIZE_10) {
            return LegacyMissileRadarProfile.CUSTOM_10;
        }
        if (top == CustomMissilePartProfile.PartSize.SIZE_10
                && bottom == CustomMissilePartProfile.PartSize.SIZE_15) {
            return LegacyMissileRadarProfile.CUSTOM_10_15;
        }
        if (top == CustomMissilePartProfile.PartSize.SIZE_15
                && bottom == CustomMissilePartProfile.PartSize.SIZE_15) {
            return LegacyMissileRadarProfile.CUSTOM_15;
        }
        if (top == CustomMissilePartProfile.PartSize.SIZE_15
                && bottom == CustomMissilePartProfile.PartSize.SIZE_20) {
            return LegacyMissileRadarProfile.CUSTOM_15_20;
        }
        if (top == CustomMissilePartProfile.PartSize.SIZE_20
                && bottom == CustomMissilePartProfile.PartSize.SIZE_20) {
            return LegacyMissileRadarProfile.CUSTOM_20;
        }
        return LegacyMissileRadarProfile.TIER1;
    }

    public String warheadLegacyName() {
        return entityData.get(WARHEAD);
    }

    public String fuselageLegacyName() {
        return entityData.get(FUSELAGE);
    }

    public String finsLegacyName() {
        return entityData.get(FINS);
    }

    public String thrusterLegacyName() {
        return entityData.get(THRUSTER);
    }

    public float fuel() {
        return entityData.get(FUEL);
    }

    public void setFuel(float fuel) {
        entityData.set(FUEL, fuel);
    }

    public float consumption() {
        return entityData.get(CONSUMPTION);
    }

    public void setConsumption(float consumption) {
        entityData.set(CONSUMPTION, consumption);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(WARHEAD, "");
        entityData.define(FUSELAGE, "");
        entityData.define(FINS, "");
        entityData.define(THRUSTER, "");
        entityData.define(FUEL, 0.0F);
        entityData.define(CONSUMPTION, 0.0F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(WARHEAD, tag.getString(TAG_WARHEAD));
        entityData.set(FUSELAGE, tag.getString(TAG_FUSELAGE));
        entityData.set(FINS, tag.getString(TAG_FINS));
        entityData.set(THRUSTER, tag.getString(TAG_THRUSTER));
        setFuel(tag.getFloat(TAG_FUEL));
        setConsumption(tag.getFloat(TAG_CONSUMPTION));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(TAG_WARHEAD, warheadLegacyName());
        tag.putString(TAG_FUSELAGE, fuselageLegacyName());
        tag.putString(TAG_FINS, finsLegacyName());
        tag.putString(TAG_THRUSTER, thrusterLegacyName());
        tag.putFloat(TAG_FUEL, fuel());
        tag.putFloat(TAG_CONSUMPTION, consumption());
    }

    @Nullable
    private CustomMissilePartProfile warheadProfile() {
        return CustomMissilePartProfile.byLegacyName(warheadLegacyName());
    }

    @Nullable
    private CustomMissilePartProfile fuselageProfile() {
        return CustomMissilePartProfile.byLegacyName(fuselageLegacyName());
    }

    @Nullable
    private String contrailType() {
        CustomMissilePartProfile fuselage = fuselageProfile();
        if (fuselage == null || fuselage.fuelType() == null) {
            return null;
        }
        return switch (fuselage.fuelType()) {
            case BALEFIRE -> ParticleUtil.TYPE_EX_BALEFIRE;
            case HYDROGEN -> ParticleUtil.TYPE_EX_HYDROGEN;
            case KEROSENE -> ParticleUtil.TYPE_EX_KEROSENE;
            case SOLID -> ParticleUtil.TYPE_EX_SOLID;
            case XENON -> null;
        };
    }

    private void setPartValue(EntityDataAccessor<String> key,
            @Nullable CustomMissilePartProfile.ResolvedPart part) {
        entityData.set(key, part == null ? "" : part.legacyName());
    }
}
