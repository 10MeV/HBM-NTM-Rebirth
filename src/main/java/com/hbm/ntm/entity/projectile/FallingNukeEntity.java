package com.hbm.ntm.entity.projectile;

import com.hbm.ntm.explosion.CustomNukeExplosion;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FallingNukeEntity extends Entity {
    private float tnt;
    private float nuke;
    private float hydro;
    private float amat;
    private float dirty;
    private float schrab;
    private float euph;
    private byte legacyFacingMeta;

    public FallingNukeEntity(EntityType<? extends FallingNukeEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public FallingNukeEntity(Level level, float tnt, float nuke, float hydro, float amat, float dirty, float schrab, float euph) {
        this(ModEntityTypes.FALLING_NUKE.get(), level);
        this.tnt = tnt;
        this.nuke = nuke;
        this.hydro = hydro;
        this.amat = amat;
        this.dirty = dirty;
        this.schrab = schrab;
        this.euph = euph;
        setXRot(90.0F);
        xRotO = 90.0F;
    }

    public static FallingNukeEntity create(Level level, double x, double y, double z, float tnt, float nuke, float hydro,
            float amat, float dirty, float schrab, float euph, byte legacyFacingMeta) {
        FallingNukeEntity entity = new FallingNukeEntity(level, tnt, nuke, hydro, amat, dirty, schrab, euph);
        entity.setPos(x, y, z);
        entity.legacyFacingMeta = legacyFacingMeta;
        return entity;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);

        setDeltaMovement(motion.x * 0.99D, Math.max(motion.y - 0.05D, -1.0D), motion.z * 0.99D);

        xRotO = getXRot();
        if (getXRot() > -75.0F) {
            setXRot(getXRot() - 2.0F);
        }

        BlockPos pos = BlockPos.containing(Math.floor(getX()), Math.floor(getY()), Math.floor(getZ()));
        if (!level().getBlockState(pos).isAir()) {
            if (!level().isClientSide()) {
                CustomNukeExplosion.explode(level(), getX(), getY(), getZ(), tnt, nuke, hydro, amat, dirty, schrab, euph);
                discard();
            }
        }
    }

    public byte legacyFacingMeta() {
        return legacyFacingMeta;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tnt = tag.getFloat("tnt");
        nuke = tag.getFloat("nuke");
        hydro = tag.getFloat("hydro");
        amat = tag.getFloat("amat");
        dirty = tag.getFloat("dirty");
        schrab = tag.getFloat("schrab");
        euph = tag.getFloat("euph");
        legacyFacingMeta = tag.getByte("legacyFacingMeta");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("tnt", tnt);
        tag.putFloat("nuke", nuke);
        tag.putFloat("hydro", hydro);
        tag.putFloat("amat", amat);
        tag.putFloat("dirty", dirty);
        tag.putFloat("schrab", schrab);
        tag.putFloat("euph", euph);
        tag.putByte("legacyFacingMeta", legacyFacingMeta);
    }
}
