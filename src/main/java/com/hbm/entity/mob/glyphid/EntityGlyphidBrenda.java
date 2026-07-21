package com.hbm.entity.mob.glyphid;

import com.hbm.entity.effect.EntityMist;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Source-backed 1.7.10 Glyphid Brenda pheromone carrier. */
public class EntityGlyphidBrenda extends EntityGlyphid {
    public EntityGlyphidBrenda(EntityType<? extends EntityGlyphidBrenda> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidBrenda(Level level) {
        this(ModEntityTypes.GLYPHID_BRENDA.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 250.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.2D)
                .add(Attributes.ATTACK_DAMAGE, 50.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_brenda.png");
    }

    @Override
    public float getScale() {
        return 2.0F;
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.12D, 2.0D), 100.0D);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level().isClientSide() || getHealth() > 0.0F) {
            return;
        }

        EntityMist mist = new EntityMist(level());
        mist.setType(HbmFluids.PHEROMONE);
        mist.setPos(getX(), getY(), getZ());
        mist.setArea(14.0F, 6.0F);
        mist.setDuration(80);
        level().addFreshEntity(mist);

        for (int index = 0; index < 12; index++) {
            EntityGlyphid glyphid = new EntityGlyphid(level());
            glyphid.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
            glyphid.setDeltaMovement(random.nextGaussian(), 0.0D, random.nextGaussian());
            level().addFreshEntity(glyphid);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (random.nextInt(3) != 0) {
            return;
        }
        if (ModItems.GLYPHID_GLAND.get() instanceof HbmFluidContainerItem gland) {
            spawnAtLocation(gland.createFilledStack(HbmFluids.PHEROMONE));
        } else {
            spawnAtLocation(new ItemStack(ModItems.GLYPHID_GLAND.get()));
        }
    }

    @Override
    protected float getArmorThresholdMultiplier() {
        return 10.0F;
    }

    @Override
    protected float getArmorResistanceMultiplier() {
        return 0.5F;
    }
}
