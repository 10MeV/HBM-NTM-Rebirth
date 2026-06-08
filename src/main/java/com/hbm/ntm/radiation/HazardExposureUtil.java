package com.hbm.ntm.radiation;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HazardExposureUtil {
    public static void updatePlayerInventory(Player player) {
        applyInventoryHazards(player, player.getInventory().items);
        applyInventoryHazards(player, player.getInventory().armor);
        applyInventoryHazards(player, player.getInventory().offhand);
    }

    public static void updateLivingInventory(LivingEntity entity) {
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.MAINHAND));
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.FEET));
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.LEGS));
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.CHEST));
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.HEAD));
    }

    private static void applyInventoryHazards(LivingEntity entity, Iterable<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            applyEquippedHazards(entity, stack);
        }
    }

    private static void applyEquippedHazards(LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        for (HazardEntry entry : HazardRegistry.getHazards(stack)) {
            applyHazard(entity, stack, entry.type(), entry.modifiedLevel(stack, entity));
        }
    }

    private static void applyHazard(LivingEntity entity, ItemStack stack, HazardType type, float level) {
        if (level <= 0.0F) {
            return;
        }

        switch (type) {
            case RADIATION -> RadiationUtil.contaminate(entity, HazardType.RADIATION,
                    RadiationUtil.ContaminationType.CREATIVE, level * stack.getCount() / 20.0F);
            case DIGAMMA -> RadiationUtil.applyDigammaData(entity, level / 20.0F);
            case ASBESTOS -> RadiationUtil.applyAsbestos(entity, (int) Math.min(level, 10.0F), (int) level);
            case COAL -> RadiationUtil.applyCoalDust(entity, (int) Math.min(level * stack.getCount(), 10.0F),
                    (int) level, Math.max(65 - stack.getCount(), 1));
            case HOT -> {
                if (!entity.isInWaterOrRain()) {
                    entity.setSecondsOnFire((int) Math.ceil(level));
                }
            }
            case BLINDING -> {
                if (!ArmorUtil.hasBlindingProtection(entity)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) Math.ceil(level), 0));
                }
            }
            case HYDROACTIVE -> {
                if (entity.isInWaterOrRain() && entity.level() instanceof ServerLevel levelAccessor) {
                    stack.shrink(stack.getCount());
                    WeaponExplosionUtil.explodeStandard(levelAccessor, entity.getX(), entity.getEyeY(), entity.getZ(),
                            level, entity, true, false);
                }
            }
            case EXPLOSIVE -> {
                if (entity.isOnFire() && entity.level() instanceof ServerLevel levelAccessor) {
                    stack.shrink(stack.getCount());
                    WeaponExplosionUtil.explodeStandard(levelAccessor, entity.getX(), entity.getEyeY(), entity.getZ(),
                            level, entity, true, true);
                }
            }
        }
    }

    private HazardExposureUtil() {
    }
}
