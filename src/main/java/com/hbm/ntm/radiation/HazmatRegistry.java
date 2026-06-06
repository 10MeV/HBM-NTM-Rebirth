package com.hbm.ntm.radiation;

import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

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
        double steel = 0.045D;
        double titanium = 0.045D;
        double alloy = 0.07D;
        double cobalt = 0.125D;
        double hazYellow = 0.6D;
        double hazRed = 1.0D;
        double hazGray = 2.0D;
        double paa = 1.7D;
        double liquidator = 2.4D;
        double security = 0.825D;
        double star = 1.0D;
        double cmb = 1.3D;
        double schrab = 3.0D;
        double euph = 10.0D;

        registerArmorSet(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, iron);
        registerArmorSet(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS, gold);
        registerArmorSet(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, chainmail);
        registerArmorSet(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, diamond);
        registerArmorSet(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, netherite);

        registerLegacyArmorSet("steel", steel);
        registerLegacyArmorSet("titanium", titanium);
        registerLegacyArmorSet("alloy", alloy);
        registerLegacyArmorSet("cobalt", cobalt);
        registerLegacyArmorSet("hazmat", hazYellow);
        registerLegacyArmorSet("hazmat_red", hazRed);
        registerLegacyArmorSet("hazmat_grey", hazGray);
        registerLegacyArmorSet("hazmat_paa", paa);
        registerLegacyArmorSet("liquidator", liquidator);
        registerLegacyArmorSet("security", security);
        registerLegacyArmorSet("starmetal", star);
        registerLegacyArmorSet("cmb", cmb);
        registerLegacyArmorSet("schrabidium", schrab);
        registerLegacyArmorSet("euphemium", euph);
        registerLegacyPiece("paa_plate", paa * CHEST);
        registerLegacyPiece("paa_legs", paa * LEGS);
        registerLegacyPiece("paa_boots", paa * BOOTS);
        registerLegacyPiece("jackt", 0.1D);
        registerLegacyPiece("jackt2", 0.1D);
        registerLegacyPiece("gas_mask", 0.07D);
        registerLegacyPiece("gas_mask_m65", 0.095D);
    }

    public static void registerHazmat(Item item, double resistance) {
        RESISTANCE.put(item, Math.max(0.0D, resistance));
        RadiationShieldingRegistry.register(item, (float) Math.max(0.0D, resistance));
    }

    public static double getResistance(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0D;
        }
        double cladding = 0.0D;
        if (stack.hasTag()) {
            cladding = stack.getTag().getFloat("hfr_cladding");
            if (cladding == 0.0D) {
                cladding = stack.getTag().getFloat("ntm_cladding");
            }
            if (cladding == 0.0D) {
                cladding = stack.getTag().getFloat("cladding");
            }
        }
        return RESISTANCE.getOrDefault(stack.getItem(), 0.0D) + cladding;
    }

    public static float getResistance(LivingEntity entity) {
        float resistance = 0.0F;
        for (ItemStack stack : entity.getArmorSlots()) {
            resistance += (float) getResistance(stack);
        }
        if (entity.hasEffect(ModEffects.RADX.get())) {
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

    private static void registerLegacyArmorSet(String prefix, double material) {
        registerLegacyPiece(prefix + "_helmet", material * HELMET);
        registerLegacyPiece(prefix + "_plate", material * CHEST);
        registerLegacyPiece(prefix + "_legs", material * LEGS);
        registerLegacyPiece(prefix + "_boots", material * BOOTS);
    }

    private static void registerLegacyPiece(String name, double resistance) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item != null) {
            registerHazmat(item.get(), resistance);
        }
    }

    private HazmatRegistry() {
    }
}
