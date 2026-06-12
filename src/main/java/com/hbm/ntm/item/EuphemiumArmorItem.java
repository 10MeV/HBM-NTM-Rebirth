package com.hbm.ntm.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class EuphemiumArmorItem extends ArmorItem {
    public EuphemiumArmorItem(Type type, Properties properties) {
        super(HbmArmorMaterials.EUPHEMIUM, type, properties.stacksTo(1));
    }

    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && hasFullSet(player)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 5, 127, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 5, 127, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 5, 127, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 5, 127, true, true));
            if (player.getDeltaMovement().y < -0.25D) {
                player.setDeltaMovement(player.getDeltaMovement().x, -0.25D, player.getDeltaMovement().z);
                player.fallDistance = 0.0F;
            }
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    private static boolean hasFullSet(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[] {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(player.getItemBySlot(slot).getItem() instanceof EuphemiumArmorItem)) {
                return false;
            }
        }
        return true;
    }
}
