package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.armor.ArmorModHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;

public class LiquidatorArmorItem extends ArmorItem {
    public LiquidatorArmorItem(Type type, Properties properties) {
        super(HbmArmorMaterials.LIQUIDATOR, type, properties.stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != slotForType(getType())) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return withLiquidatorModifiers(super.getDefaultAttributeModifiers(slot), getType());
    }

    static Multimap<Attribute, AttributeModifier> withLiquidatorModifiers(
            Multimap<Attribute, AttributeModifier> base, Type type) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        int legacySlot = ArmorModHandler.legacyArmorTypeIndex(type);
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                ArmorModHandler.fixedUUIDs[legacySlot], "Liquidator armor modifier", 100.0D,
                AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                ArmorModHandler.UUIDs[legacySlot], "Liquidator armor modifier", -0.1D,
                AttributeModifier.Operation.MULTIPLY_BASE));
        return builder.build();
    }

    private static EquipmentSlot slotForType(Type type) {
        return switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }
}
