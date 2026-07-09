package com.hbm.ntm.entity.mob;

import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModSounds;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;

public class EntityDuck extends Chicken {
    public EntityDuck(EntityType<? extends EntityDuck> type, Level level) {
        super(type, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_DUCC.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ENTITY_DUCC.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_DUCC.get();
    }

    @Nullable
    @Override
    public EntityDuck getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntityTypes.DUCK.get().create(level);
    }

    @Override
    public void die(DamageSource source) {
        if (!level().isClientSide && level().getServer() != null) {
            Component message = getCombatTracker().getDeathMessage();
            level().getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
        super.die(source);
    }
}
