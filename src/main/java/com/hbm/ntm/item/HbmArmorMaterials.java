package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public enum HbmArmorMaterials implements ArmorMaterial {
    STEEL("steel", 30, 5, () -> legacyIngredient("ingot_steel")),
    TITANIUM("titanium", 25, 9, () -> legacyIngredient("ingot_titanium")),
    ALLOY("alloy", 40, 12, () -> Ingredient.EMPTY),
    COBALT("cobalt", 70, 60, () -> legacyIngredient("ingot_cobalt")),
    HAZMAT("hazmat", 60, 5, () -> Ingredient.of(ModItems.legacyItem("hazmat_cloth").get())),
    HAZMAT_RED("hazmat_red", 60, 5, () -> Ingredient.of(ModItems.legacyItem("hazmat_cloth_red").get())),
    HAZMAT_GREY("hazmat_grey", 60, 5, () -> Ingredient.of(ModItems.legacyItem("hazmat_cloth_grey").get())),
    PAA("paa", 75, 25, () -> legacyIngredient("plate_paa")),
    HAZMAT_PAA("hazmat_paa", 75, 25, () -> legacyIngredient("plate_paa")),
    LIQUIDATOR("liquidator", 750, 10, () -> Ingredient.of(ModItems.legacyItem("plate_lead").get())),
    SCHRABIDIUM("schrabidium", 100, 50, () -> Ingredient.of(ModItems.legacyItem("ingot_schrabidium").get())),
    EUPHEMIUM("euphemium", 15_000_000, 100, () -> Ingredient.EMPTY),
    CMB("cmb", 60, 50, () -> legacyIngredient("ingot_combine_steel")),
    SECURITY("security", 100, 15, () -> legacyIngredient("plate_kevlar")),
    STARMETAL("starmetal", 150, 100, () -> legacyIngredient("ingot_starmetal")),
    BISMUTH("bismuth", 100, 100, () -> legacyIngredient("plate_bismuth")),
    ROBES("robes", 15, 12, () -> legacyIngredient("rag")),
    ZIRCONIUM("zirconium", 1000, 1000, () -> legacyIngredient("ingot_zirconium")),
    DNT("dnt", 3, 0, () -> legacyIngredient("ingot_dineutronium")),
    JACKET("jackt", 30, 5, () -> legacyIngredient("ingot_steel")),
    JACKET2("jackt2", 30, 5, () -> legacyIngredient("ingot_steel")),
    T51("t51", 150, 0, () -> legacyIngredient("plate_armor_titanium")),
    DESH_POWERED("desh_powered", 150, 0, () -> legacyIngredient("ingot_desh")),
    DIESEL("dieselsuit", 150, 0, () -> legacyIngredient("plate_copper"), 0.25F),
    AJR("ajr", 150, 0, () -> legacyIngredient("plate_armor_ajr")),
    BJ("bj", 150, 0, () -> legacyIngredient("plate_armor_lunar")),
    ENV("env", 150, 10, () -> legacyIngredient("plate_armor_hev")),
    HEV("hev", 150, 0, () -> legacyIngredient("plate_armor_hev")),
    FAU("fau", 150, 0, () -> legacyIngredient("plate_armor_fau")),
    DNS("dns", 150, 0, () -> legacyIngredient("plate_armor_dnt")),
    TAURUN("taurun", 150, 10, () -> legacyIngredient("plate_iron")),
    TRENCHMASTER("trenchmaster", 150, 0, () -> legacyIngredient("plate_iron")),
    ASBESTOS("asbestos", 20, 5, () -> Ingredient.of(ModItems.legacyItem("asbestos_cloth").get())),
    RAGS("rags", 150, 0, () -> Ingredient.EMPTY);

    private final String name;
    private final int durabilityMultiplier;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;
    private final float knockbackResistance;

    HbmArmorMaterials(String name, int durabilityMultiplier, int enchantmentValue,
            Supplier<Ingredient> repairIngredient) {
        this(name, durabilityMultiplier, enchantmentValue, repairIngredient, 0.0F);
    }

    HbmArmorMaterials(String name, int durabilityMultiplier, int enchantmentValue,
            Supplier<Ingredient> repairIngredient, float knockbackResistance) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
        this.knockbackResistance = Math.max(0.0F, knockbackResistance);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 11;
            case CHESTPLATE -> 16;
            case LEGGINGS -> 15;
            case BOOTS -> 13;
        } * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        if (this == RAGS) {
            return 1;
        }
        if (this == ASBESTOS) {
            return switch (type) {
                case HELMET, BOOTS -> 1;
                case CHESTPLATE -> 4;
                case LEGGINGS -> 3;
            };
        }
        if (this == ZIRCONIUM) {
            return switch (type) {
                case HELMET -> 2;
                case CHESTPLATE -> 5;
                case LEGGINGS -> 3;
                case BOOTS -> 1;
            };
        }
        if (this == DNT) {
            return 1;
        }
        if (this == STEEL || this == TITANIUM || this == ALLOY || this == COBALT || this == PAA
                || this == HAZMAT_PAA || this == LIQUIDATOR || this == SCHRABIDIUM || this == EUPHEMIUM
                || this == CMB || this == SECURITY || this == STARMETAL || this == T51 || this == DESH_POWERED
                || this == BISMUTH || this == DIESEL || this == AJR || this == BJ || this == ENV || this == HEV || this == FAU || this == DNS
                || this == TAURUN || this == TRENCHMASTER || this == JACKET || this == JACKET2) {
            return switch (type) {
                case HELMET, BOOTS -> 3;
                case CHESTPLATE -> 8;
                case LEGGINGS -> 6;
            };
        }
        return switch (type) {
            case HELMET -> 2;
            case CHESTPLATE -> 5;
            case LEGGINGS -> 4;
            case BOOTS -> 1;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return HbmNtm.MOD_ID + ":" + name;
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }

    private static Ingredient legacyIngredient(String name) {
        var item = ModItems.legacyItem(name);
        if (item == null) {
            return Ingredient.EMPTY;
        }
        return Ingredient.of(item.get());
    }
}
