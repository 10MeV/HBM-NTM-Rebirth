package com.hbm.entity.mob.glyphid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Deprecated(forRemoval = false)
public class EntityGlyphidScout extends EntityGlyphid {
    public EntityGlyphidScout(EntityType<? extends EntityGlyphidScout> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidScout(Level level) {
        this(ModEntityTypes.GLYPHID_SCOUT.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.5D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_scout.png");
    }

    @Override
    public float getScale() {
        return 0.75F;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target) && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 3), this);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount, 2.0D), 100.0D);
    }
}
