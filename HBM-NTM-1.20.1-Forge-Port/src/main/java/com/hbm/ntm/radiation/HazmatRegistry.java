package com.hbm.ntm.radiation;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.IdentityHashMap;
import java.util.Map;

public final class HazmatRegistry {
    public static final double HELMET = 0.2D;
    public static final double CHEST = 0.4D;
    public static final double LEGS = 0.3D;
    public static final double BOOTS = 0.1D;

    private static final Map<Item, Double> RESISTANCE = new IdentityHashMap<>();

    public static void registerDefaults() {
        double iron = 0.0225D;
        double gold = 0.0225D;
        double diamond = 0.07D;
        double netherite = 0.125D;
        double chainmail = 0.0225D;

        registerArmorSet(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, iron);
        registerArmorSet(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, gold);
        registerArmorSet(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, chainmail);
        registerArmorSet(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, diamond);
        registerArmorSet(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, netherite);
    }

    public static void registerHazmat(Item item, double resistance) {
        RESISTANCE.put(item, Math.max(0.0D, resistance));
        RadiationShieldingRegistry.register(item, (float) Math.max(0.0D, resistance));
    }

    public static double getResistance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }
        double cladding = stack.hasTag() ? stack.getTag().getFloat("hfr_cladding") : 0.0D;
        return RESISTANCE.getOrDefault(stack.getItem(), 0.0D) + cladding;
    }

    public static float getResistance(LivingEntity entity) {
        float resistance = 0.0F;
        for (ItemStack stack : entity.getArmorSlots()) {
            resistance += (float) getResistance(stack);
        }
        if (entity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
            resistance += 0.2F;
        }
        return resistance;
    }

    public static float calculateRadiationModifier(LivingEntity entity) {
        if (entity instanceof Player player && player.isCreative()) {
            return 0.0F;
        }
        return (float) Math.pow(10.0F, -getResistance(entity));
    }

    private static void registerArmorSet(Item helmet, Item chest, Item legs, Item boots, double material) {
        registerHazmat(helmet, material * HELMET);
        registerHazmat(chest, material * CHEST);
        registerHazmat(legs, material * LEGS);
        registerHazmat(boots, material * BOOTS);
    }

    private HazmatRegistry() {
    }
}
