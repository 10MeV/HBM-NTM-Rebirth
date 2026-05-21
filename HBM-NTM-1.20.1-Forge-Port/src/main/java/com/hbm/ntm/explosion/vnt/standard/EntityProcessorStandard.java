package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.CustomDamageHandler;
import com.hbm.ntm.explosion.vnt.interfaces.EntityProcessor;
import com.hbm.ntm.explosion.vnt.interfaces.EntityRangeMutator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityProcessorStandard implements EntityProcessor {
    private EntityRangeMutator range;
    private CustomDamageHandler damage;
    private boolean allowSelfDamage;

    @Override
    public Map<Player, Vec3> process(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size) {
        Map<Player, Vec3> affectedPlayers = new HashMap<>();
        float diameter = size * 2.0F;
        if (range != null) {
            diameter = range.mutateRange(explosion, diameter);
        }

        AABB bounds = new AABB(
                position.x - diameter - 1.0D, position.y - diameter - 1.0D, position.z - diameter - 1.0D,
                position.x + diameter + 1.0D, position.y + diameter + 1.0D, position.z + diameter + 1.0D);
        List<Entity> entities = level.getEntities(allowSelfDamage ? null : explosion.exploder(), bounds);
        ForgeEventFactory.onExplosionDetonate(level, explosion.compat(), entities, diameter);

        for (Entity entity : entities) {
            if (entity.ignoreExplosion()) {
                continue;
            }

            double distanceScaled = Math.sqrt(entity.distanceToSqr(position)) / diameter;
            if (distanceScaled > 1.0D) {
                continue;
            }

            double deltaX = entity.getX() - position.x;
            double deltaY = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - position.y;
            double deltaZ = entity.getZ() - position.z;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (distance == 0.0D) {
                continue;
            }

            deltaX /= distance;
            deltaY /= distance;
            deltaZ /= distance;
            double density = Explosion.getSeenPercent(position, entity);
            double knockback = (1.0D - distanceScaled) * density;
            entity.hurt(explosion.damageSource(), calculateDamage(distanceScaled, density, knockback, diameter));

            double dampenedKnockback = entity instanceof LivingEntity livingEntity
                    ? ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, knockback)
                    : knockback;
            Vec3 push = new Vec3(deltaX * dampenedKnockback, deltaY * dampenedKnockback, deltaZ * dampenedKnockback);
            entity.setDeltaMovement(entity.getDeltaMovement().add(push));

            if (entity instanceof Player player && !player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                affectedPlayers.put(player, push);
            }

            if (damage != null) {
                damage.handleAttack(explosion, entity, distanceScaled);
            }
        }

        return affectedPlayers;
    }

    public float calculateDamage(double distanceScaled, double density, double knockback, float diameter) {
        return (float) ((int) ((knockback * knockback + knockback) / 2.0D * 7.0D * diameter + 1.0D));
    }

    public EntityProcessorStandard withRangeMod(float mod) {
        this.range = (explosion, range) -> range * mod;
        return this;
    }

    public EntityProcessorStandard withDamageMod(CustomDamageHandler damage) {
        this.damage = damage;
        return this;
    }

    public EntityProcessorStandard allowSelfDamage() {
        this.allowSelfDamage = true;
        return this;
    }
}
