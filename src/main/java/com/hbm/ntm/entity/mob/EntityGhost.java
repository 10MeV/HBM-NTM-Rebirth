package com.hbm.ntm.entity.mob;

import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Direct 1.20.1 migration of 1.7.10 {@code EntityGhost}. */
public final class EntityGhost extends PathfinderMob {
    private static final double LEGACY_DESPAWN_RANGE = 50.0D;

    public EntityGhost(EntityType<? extends EntityGhost> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new RandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server && !server.getEntitiesOfClass(Player.class,
                getBoundingBox().inflate(LEGACY_DESPAWN_RANGE)).isEmpty()) {
            discard();
        }
    }

    @Override
    public void setHealth(float health) {
        super.setHealth(getMaxHealth());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }
}
