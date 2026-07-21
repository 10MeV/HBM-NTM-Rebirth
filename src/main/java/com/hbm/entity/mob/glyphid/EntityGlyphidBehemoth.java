package com.hbm.entity.mob.glyphid;

import com.hbm.entity.effect.EntityMist;
import com.hbm.entity.projectile.EntityChemical;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Source-backed 1.7.10 Glyphid Behemoth sulfuric breath carrier. */
public class EntityGlyphidBehemoth extends EntityGlyphid {
    private int timer = 120;
    private int breathTime;

    public EntityGlyphidBehemoth(EntityType<? extends EntityGlyphidBehemoth> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidBehemoth(Level level) {
        this(ModEntityTypes.GLYPHID_BEHEMOTH.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 125.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.8D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_behemoth.png");
    }

    @Override
    public float getScale() {
        return 1.5F;
    }

    @Override
    public void tick() {
        super.tick();
        Entity target = getTarget();
        if (target == null) {
            timer = 120;
            breathTime = 0;
            return;
        }
        if (breathTime > 0) {
            if (!isGlyphidSwingInProgress()) {
                swingGlyphid();
            }
            acidAttack(target);
            setYRot(yRotO);
            breathTime--;
        } else if (--timer <= 0) {
            breathTime = 120;
            timer = 120;
        }
    }

    private void acidAttack(Entity target) {
        if (level().isClientSide() || !(target instanceof LivingEntity) || distanceTo(target) >= 20.0F) {
            return;
        }
        addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 6));
        EntityChemical chemical = new EntityChemical(level(), this, 0.0D, 0.0D, 0.0D);
        chemical.setFluid(HbmFluids.SULFURIC_ACID);
        level().addFreshEntity(chemical);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level().isClientSide()) {
            return;
        }
        EntityMist mist = new EntityMist(level());
        mist.setType(HbmFluids.SULFURIC_ACID);
        mist.setPos(getX(), getY(), getZ());
        mist.setArea(10.0F, 4.0F);
        mist.setDuration(120);
        level().addFreshEntity(mist);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (ModItems.GLYPHID_GLAND.get() instanceof HbmFluidContainerItem gland) {
            spawnAtLocation(gland.createFilledStack(HbmFluids.SULFURIC_ACID));
        } else {
            spawnAtLocation(new ItemStack(ModItems.GLYPHID_GLAND.get()));
        }
        super.dropCustomDeathLoot(source, looting, recentlyHit);
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.15D, 2.0D), 100.0D);
    }

    @Override
    protected int getGlyphidSwingDuration() {
        return 100;
    }

    @Override
    protected float getArmorThresholdMultiplier() {
        return 5.0F;
    }

    @Override
    protected float getArmorResistanceMultiplier() {
        return 0.35F;
    }
}
