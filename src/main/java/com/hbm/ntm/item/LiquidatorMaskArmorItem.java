package com.hbm.ntm.item;

import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class LiquidatorMaskArmorItem extends GasMaskArmorItem {
    public LiquidatorMaskArmorItem(Properties properties) {
        super(HbmArmorMaterials.LIQUIDATOR, properties, List.of());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.HEAD) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return LiquidatorArmorItem.withLiquidatorModifiers(super.getDefaultAttributeModifiers(slot), getType());
    }
}
