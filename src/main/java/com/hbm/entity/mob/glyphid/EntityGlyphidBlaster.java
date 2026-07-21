package com.hbm.entity.mob.glyphid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** Source-backed 1.7.10 Blaster parameter specialization of the Bombardier volley. */
public class EntityGlyphidBlaster extends EntityGlyphidBombardier {
    public EntityGlyphidBlaster(EntityType<? extends EntityGlyphidBlaster> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidBlaster(Level level) {
        this(ModEntityTypes.GLYPHID_BLASTER.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_blaster.png");
    }

    @Override
    public float getScale() {
        return 1.25F;
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.25D, 2.0D), 100.0D);
    }

    @Override
    public float getBombDamage() {
        return 15.0F;
    }

    @Override
    public int getBombCount() {
        return 10;
    }

    @Override
    public float getSpreadMult() {
        return 0.5F;
    }

    @Override
    public double getV0() {
        return 1.25D;
    }

    @Override
    protected float getArmorThresholdMultiplier() {
        return 2.0F;
    }

    @Override
    protected float getArmorResistanceMultiplier() {
        return 0.15F;
    }
}
