package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

public enum HbmToolTiers implements Tier {
    SCHRABIDIUM(3, 10_000, 50.0F, 100.0F, 200, () -> Ingredient.of(ModItems.SCHRABIDIUM_INGOT.get())),
    TITANIUM(3, 1_000, 9.0F, 2.5F, 15, () -> Ingredient.of(ModItems.TITANIUM_INGOT.get())),
    STEEL(3, 750, 8.0F, 2.0F, 10, () -> Ingredient.of(ModItems.STEEL_INGOT.get())),
    ALLOY(3, 2_000, 15.0F, 5.0F, 5, () -> Ingredient.EMPTY),
    CMB(3, 8_500, 40.0F, 55.0F, 100, () -> Ingredient.of(ModItems.legacyItem("ingot_combine_steel").get())),
    ELEC(3, 0, 30.0F, 12.0F, 2, () -> Ingredient.EMPTY),
    DESH(2, 0, 7.5F, 2.0F, 10, () -> Ingredient.of(ModItems.legacyItem("ingot_desh").get())),
    COBALT(3, 750, 9.0F, 2.5F, 60, () -> Ingredient.of(ModItems.COBALT_INGOT.get())),
    COBALT_DECORATED(3, 2_500, 15.0F, 2.5F, 75, () -> Ingredient.of(ModItems.COBALT_INGOT.get())),
    STARMETAL(3, 3_000, 20.0F, 2.5F, 100, () -> legacyIngredient("ingot_starmetal")),
    BISMUTH(4, 0, 50.0F, 0.0F, 200, () -> Ingredient.of(ModItems.legacyItem("ingot_bismuth").get())),
    VOLCANIC(4, 0, 50.0F, 0.0F, 200, () -> Ingredient.of(ModItems.legacyItem("ingot_bismuth").get())),
    CHLOROPHYTE(4, 0, 75.0F, 0.0F, 200, () -> legacyIngredient("powder_chlorophyte")),
    METEORITE(4, 0, 50.0F, 0.0F, 200, () -> legacyIngredient("plate_paa")),
    MESE(4, 0, 100.0F, 0.0F, 200, () -> legacyIngredient("plate_paa")),
    MESE_GAVEL(4, 0, 50.0F, 0.0F, 200, () -> legacyIngredient("plate_paa")),
    DWARVEN(2, 0, 4.0F, 0.0F, 10, () -> Ingredient.of(ModItems.COPPER_INGOT.get()));

    private final int level;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    HbmToolTiers(int level, int uses, float speed, float attackDamageBonus, int enchantmentValue,
                 Supplier<Ingredient> repairIngredient) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    private static Ingredient legacyIngredient(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item == null ? Ingredient.EMPTY : Ingredient.of(item.get());
    }
}
