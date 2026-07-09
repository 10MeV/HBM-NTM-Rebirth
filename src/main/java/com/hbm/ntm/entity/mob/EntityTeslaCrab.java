package com.hbm.ntm.entity.mob;

import com.hbm.ntm.blockentity.TeslaBlockEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityTeslaCrab extends EntityCyberCrab {
    public EntityTeslaCrab(EntityType<? extends EntityTeslaCrab> type, Level level) {
        super(type, level);
        noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide()) {
            setTeslaTargets(TeslaBlockEntity.zap(level(), new Vec3(getX(), getY() + 1.0D, getZ()), 3.0D, this));
        }
        super.aiStep();
    }
}
