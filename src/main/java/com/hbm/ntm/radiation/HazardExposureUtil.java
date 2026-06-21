package com.hbm.ntm.radiation;

import com.hbm.ntm.api.item.HazardClass;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmMathUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public final class HazardExposureUtil {
    public static void updatePlayerInventory(Player player) {
        HazardExposureContext context = HazardExposureContext.of(player);
        applyInventoryHazards(player, player.getInventory().items, context);
        applyInventoryHazards(player, player.getInventory().armor, context);
        applyInventoryHazards(player, player.getInventory().offhand, context);
    }

    public static void updateLivingInventory(LivingEntity entity) {
        HazardExposureContext context = HazardExposureContext.of(entity);
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.MAINHAND), context);
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.FEET), context);
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.LEGS), context);
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.CHEST), context);
        applyEquippedHazards(entity, entity.getItemBySlot(EquipmentSlot.HEAD), context);
    }

    public static boolean updateDroppedItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty() || itemEntity.isRemoved()) {
            return false;
        }

        boolean applied = false;
        for (HazardEntry entry : HazardRegistry.getHazards(stack)) {
            applied |= applyDroppedHazard(itemEntity, entry.type(), entry.modifiedLevel(stack, null));
        }
        return applied;
    }

    public static void applyHazards(LivingEntity entity, ItemStack stack) {
        applyEquippedHazards(entity, stack, HazardExposureContext.of(entity));
    }

    public static void applyHazard(LivingEntity entity, ItemStack stack, HazardType type, float level) {
        applyHazard(entity, stack, type, level, HazardExposureContext.of(entity));
    }

    private static void applyInventoryHazards(LivingEntity entity, Iterable<ItemStack> stacks, HazardExposureContext context) {
        for (ItemStack stack : stacks) {
            applyEquippedHazards(entity, stack, context);
        }
    }

    private static void applyEquippedHazards(LivingEntity entity, ItemStack stack, HazardExposureContext context) {
        if (stack.isEmpty()) {
            return;
        }

        for (HazardEntry entry : HazardRegistry.getHazards(stack)) {
            applyHazard(entity, stack, entry.type(), entry.modifiedLevel(stack, entity), context);
        }
    }

    private static void applyHazard(LivingEntity entity, ItemStack stack, HazardType type, float level, HazardExposureContext context) {
        if (HazardRegistry.isHazardTypeDisabled(type)) {
            return;
        }

        switch (type) {
            case RADIATION -> {
                if (level <= 0.0F) {
                    return;
                }
                float rad = level * stack.getCount() / 20.0F;
                if (context.hasReacher()) {
                    rad = (float) HbmMathUtil.squirt(rad);
                }
                RadiationUtil.contaminate(entity, HazardType.RADIATION, RadiationUtil.ContaminationType.CREATIVE, rad);
            }
            case DIGAMMA -> RadiationUtil.applyDigammaData(entity, level / 20.0F);
            case ASBESTOS -> RadiationUtil.applyAsbestos(entity, (int) Math.min(level, 10.0F), (int) level);
            case COAL -> RadiationUtil.applyCoalDust(entity, (int) Math.min(level * stack.getCount(), 10.0F),
                    (int) level, Math.max(65 - stack.getCount(), 1));
            case HOT -> {
                if (level > 0.0F && !context.hasReacher() && !entity.isInWaterOrRain()) {
                    entity.setSecondsOnFire((int) Math.ceil(level));
                }
            }
            case BLINDING -> {
                if (!ArmorUtil.hasProtection(entity, 3, HazardClass.LIGHT)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) Math.ceil(level), 0));
                }
            }
            case HYDROACTIVE -> {
                if (!stack.isEmpty() && entity.isInWaterOrRain() && entity.level() instanceof ServerLevel levelAccessor) {
                    stack.shrink(stack.getCount());
                    WeaponExplosionUtil.explodeStandard(levelAccessor, entity.getX(), entity.getEyeY(), entity.getZ(),
                            level, null, true, false);
                }
            }
            case EXPLOSIVE -> {
                if (!stack.isEmpty() && entity.isOnFire() && entity.level() instanceof ServerLevel levelAccessor) {
                    stack.shrink(stack.getCount());
                    WeaponExplosionUtil.explodeStandard(levelAccessor, entity.getX(), entity.getEyeY(), entity.getZ(),
                            level, null, true, false);
                }
            }
        }
    }

    private static boolean hasReacher(Player player) {
        RegistryObject<Item> reacher = ModItems.legacyItem("reacher");
        if (reacher == null) {
            return false;
        }
        Item reacherItem = reacher.get();
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(reacherItem)) {
                return true;
            }
        }
        return false;
    }

    private record HazardExposureContext(boolean hasReacher) {
        private static HazardExposureContext of(LivingEntity entity) {
            if (!(entity instanceof Player player)) {
                return new HazardExposureContext(false);
            }
            return new HazardExposureContext(HazardExposureUtil.hasReacher(player));
        }
    }

    public static boolean applyDroppedHazard(ItemEntity itemEntity, HazardType type, float level) {
        if (HazardRegistry.isHazardTypeDisabled(type)) {
            return false;
        }

        switch (type) {
            case HYDROACTIVE -> {
                if (itemEntity.level() instanceof ServerLevel levelAccessor && isDroppedHydroactiveWet(itemEntity)) {
                    itemEntity.discard();
                    WeaponExplosionUtil.explodeStandard(levelAccessor, itemEntity.getX(),
                            itemEntity.getY() + itemEntity.getBbHeight() * 0.5D, itemEntity.getZ(),
                            level, null, true, false);
                    return true;
                }
            }
            case EXPLOSIVE -> {
                if (itemEntity.isOnFire() && itemEntity.level() instanceof ServerLevel levelAccessor) {
                    itemEntity.discard();
                    WeaponExplosionUtil.explodeStandard(levelAccessor, itemEntity.getX(),
                            itemEntity.getY() + itemEntity.getBbHeight() * 0.5D, itemEntity.getZ(),
                            level, null, true, false);
                    return true;
                }
            }
            default -> {
            }
        }
        return false;
    }

    private static boolean isDroppedHydroactiveWet(ItemEntity itemEntity) {
        return itemEntity.isInWaterOrRain()
                || itemEntity.level().getFluidState(itemEntity.blockPosition()).is(FluidTags.WATER);
    }

    private HazardExposureUtil() {
    }
}
