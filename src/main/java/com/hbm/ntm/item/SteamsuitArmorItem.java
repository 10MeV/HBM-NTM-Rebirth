package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.fluid.FluidType;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SteamsuitArmorItem extends FsbFueledArmorItem {
    public SteamsuitArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, FluidType fuelType, int maxFuel, int fillRate, int consumption,
            int drain, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, fuelType, maxFuel, fillRate, consumption, drain,
                fullSetTraits);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != slotForType(getType())) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getDefaultAttributeModifiers(slot));
        int legacySlot = ArmorModHandler.legacyArmorTypeIndex(getType());
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                ArmorModHandler.fixedUUIDs[legacySlot], "Steamsuit armor modifier", -0.025D,
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
