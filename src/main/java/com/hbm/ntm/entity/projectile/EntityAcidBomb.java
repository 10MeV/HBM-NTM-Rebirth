package com.hbm.ntm.entity.projectile;

import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Source-backed 1.7.10 EntityAcidBomb used by Glyphid Bombardiers. */
public class EntityAcidBomb extends LegacyThrowableEntity implements ItemSupplier {
    private float damage = 1.5F;

    public EntityAcidBomb(EntityType<? extends EntityAcidBomb> type, Level level) {
        super(type, level);
    }

    public EntityAcidBomb(Level level) {
        this(ModEntityTypes.ACID_BOMB.get(), level);
    }

    public EntityAcidBomb(Level level, double x, double y, double z) {
        this(level);
        setPos(x, y, z);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void onImpact(HitResult hit) {
        if (level().isClientSide()) {
            return;
        }
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (!(target instanceof EntityGlyphid)) {
                EntityDamageUtil.attackEntityFromNt(target,
                        ModDamageSources.indirect(level(), ModDamageSources.ACID, this, getOwner()), damage);
                discard();
            }
            return;
        }
        if (hit.getType() == HitResult.Type.BLOCK) {
            discard();
        }
    }

    @Override
    protected float getAirDrag() {
        return 1.0F;
    }

    @Override
    protected double getGravityVelocity() {
        return 0.04D;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.SLIME_BALL);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("damage");
    }
}
