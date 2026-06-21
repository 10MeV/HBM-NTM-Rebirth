package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.bullet.BulletBehaviorTag;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.CustomDamageHandler;
import com.hbm.ntm.explosion.vnt.interfaces.EntityProcessor;
import com.hbm.ntm.explosion.vnt.interfaces.EntityRangeMutator;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityProcessorCross implements EntityProcessor {
    protected final double nodeDistance;
    protected EntityRangeMutator range;
    protected CustomDamageHandler damage;
    protected double knockbackMultiplier = 1.0D;
    protected boolean allowSelfDamage;

    public EntityProcessorCross() {
        this(0.0D);
    }

    public EntityProcessorCross(double nodeDistance) {
        this.nodeDistance = nodeDistance;
    }

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

        Vec3[] nodes = nodes(position);
        Map<Entity, Float> damageByEntity = new HashMap<>();
        Map<Entity, Double> scaledDistanceByEntity = new HashMap<>();

        for (Entity entity : entities) {
            double distanceScaled = boxDistance(entity.getBoundingBox(), position) / diameter;
            if (distanceScaled > 1.0D) {
                continue;
            }

            Vec3 delta = new Vec3(entity.getX() - position.x, entity.getEyeY() - position.y, entity.getZ() - position.z);
            double distance = delta.length();
            if (distance == 0.0D) {
                continue;
            }

            Vec3 direction = delta.scale(1.0D / distance);
            double density = maxSeenPercent(entity, nodes);
            double knockback = (1.0D - distanceScaled) * density;
            float damageAmount = calculateDamage(distanceScaled, density, knockback, diameter);
            damageByEntity.merge(entity, damageAmount, Math::max);
            scaledDistanceByEntity.merge(entity, distanceScaled, Math::min);

            double dampenedKnockback = entity instanceof LivingEntity livingEntity
                    ? ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingEntity, knockback)
                    : knockback;
            Vec3 push = direction.scale(dampenedKnockback * knockbackMultiplier);
            if (shouldDealKnockback(entity)) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(push));
                entity.hurtMarked = true;
            }

            if (entity instanceof Player player) {
                affectedPlayers.put(player, direction.scale(knockback * knockbackMultiplier));
            }
        }

        damageByEntity.forEach((entity, amount) -> {
            attackEntity(entity, explosion, amount);
            if (damage != null) {
                damage.handleAttack(explosion, entity, scaledDistanceByEntity.getOrDefault(entity, 1.0D));
            }
        });

        return affectedPlayers;
    }

    protected boolean shouldDealKnockback(Entity entity) {
        if (!(entity instanceof BulletProjectileEntity projectile)) {
            return true;
        }
        BulletConfig config = projectile.config();
        return config == null || !config.hasBehavior(BulletBehaviorTag.VNT_CROSS_KNOCKBACK_IMMUNE);
    }

    protected void attackEntity(Entity entity, ExplosionVnt explosion, float amount) {
        EntityDamageUtil.attackEntityFromNt(entity, explosion.damageSource(), amount);
    }

    public float calculateDamage(double distanceScaled, double density, double knockback, float diameter) {
        return (float) ((int) ((knockback * knockback + knockback) / 2.0D * 8.0D * diameter + 1.0D));
    }

    public EntityProcessorCross withRangeMod(float mod) {
        this.range = (explosion, range) -> range * mod;
        return this;
    }

    public EntityProcessorCross withDamageMod(CustomDamageHandler damage) {
        this.damage = damage;
        return this;
    }

    public EntityProcessorCross setAllowSelfDamage() {
        this.allowSelfDamage = true;
        return this;
    }

    public EntityProcessorCross setKnockback(double multiplier) {
        this.knockbackMultiplier = multiplier;
        return this;
    }

    private Vec3[] nodes(Vec3 position) {
        if (nodeDistance <= 0.0D) {
            return new Vec3[] { position };
        }
        Vec3[] nodes = new Vec3[7];
        nodes[0] = position;
        int index = 1;
        for (Direction direction : Direction.values()) {
            nodes[index++] = position.add(
                    direction.getStepX() * nodeDistance,
                    direction.getStepY() * nodeDistance,
                    direction.getStepZ() * nodeDistance);
        }
        return nodes;
    }

    private static double maxSeenPercent(Entity entity, Vec3[] nodes) {
        double density = 0.0D;
        for (Vec3 node : nodes) {
            density = Math.max(density, Explosion.getSeenPercent(node, entity));
        }
        return density;
    }

    private static double boxDistance(AABB box, Vec3 position) {
        double x = box.minX <= position.x && box.maxX >= position.x ? 0.0D : Math.min(Math.abs(box.minX - position.x), Math.abs(box.maxX - position.x));
        double y = box.minY <= position.y && box.maxY >= position.y ? 0.0D : Math.min(Math.abs(box.minY - position.y), Math.abs(box.maxY - position.y));
        double z = box.minZ <= position.z && box.maxZ >= position.z ? 0.0D : Math.min(Math.abs(box.minZ - position.z), Math.abs(box.maxZ - position.z));
        return Math.sqrt(x * x + y * y + z * z);
    }
}
