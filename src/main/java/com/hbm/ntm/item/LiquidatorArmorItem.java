package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.ntm.armor.ArmorModHandler;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorMaterial;

public class LiquidatorArmorItem extends ArmorFSB {
    static final FullSetTraits LIQUIDATOR_TRAITS = FullSetTraits.builder()
            .step("hbm:step.metal")
            .jump("hbm:step.iron_jump")
            .fall("hbm:step.iron_land")
            .build();

    public LiquidatorArmorItem(Type type, Properties properties) {
        this(HbmArmorMaterials.LIQUIDATOR, type, properties);
    }

    public LiquidatorArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties, List.of(), false, 0, LIQUIDATOR_TRAITS);
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
                ArmorModHandler.fixedUUIDs[legacySlot], "Liquidator armor modifier", -0.1D,
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
