package com.hbm.ntm.entity.mob;

import com.hbm.ntm.damage.DamageResistanceHandler;
import com.hbm.ntm.entity.effect.MistEntity;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/** Exact 1.7.10 EntityCreeperPhosgene behavior. */
public class EntityCreeperPhosgene extends Creeper {
    public EntityCreeperPhosgene(EntityType<? extends EntityCreeperPhosgene> type, Level level) {
        super(type, level);
        this.maxSwell = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Creeper.createAttributes();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!DamageResistanceHandler.isAbsolute(source)
                && !DamageResistanceHandler.isUnblockableForLegacyResistance(source)) {
            amount -= 4.0F;
        }
        return amount < 0.0F ? false : super.hurt(source, amount);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        return super.checkSpawnRules(level, spawnType) && level().dimension().equals(Level.OVERWORLD);
    }

    @Override
    protected void explodeCreeper() {
        if (level().isClientSide()) {
            return;
        }
        discard();
        level().explode(this, getX(), getY() + getBbHeight() / 2.0D, getZ(), 2.0F, false,
                Level.ExplosionInteraction.MOB);
        level().addFreshEntity(MistEntity.create(level(), getX(), getY(), getZ(), HbmFluids.PHOSGENE,
                10.0F, 5.0F, 150));
    }
}
