package com.hbm.entity.mob.glyphid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

@Deprecated(forRemoval = false)
public class EntityGlyphidDigger extends EntityGlyphid {
    public EntityGlyphidDigger(EntityType<? extends EntityGlyphidDigger> type, Level level) {
        super(type, level);
    }

    public EntityGlyphidDigger(Level level) {
        this(ModEntityTypes.GLYPHID_DIGGER.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D);
    }

    @Override
    public ResourceLocation getSkin() {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/glyphid_digger.png");
    }

    @Override
    public float getScale() {
        return 1.3F;
    }

    @Override
    protected boolean isArmorBroken(float amount) {
        return random.nextInt(100) <= Math.min(Math.pow(amount * 0.25D, 2.0D), 100.0D);
    }
}
