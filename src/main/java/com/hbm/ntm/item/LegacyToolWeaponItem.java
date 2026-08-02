package com.hbm.ntm.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.api.block.Toolable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

/** Source-backed weapon-stat extension for old {@code ItemToolingWeapon}. */
public final class LegacyToolWeaponItem extends LegacyToolItem {
    private final Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public LegacyToolWeaponItem(Item.Properties properties, Toolable.ToolType toolType, float attackDamage) {
        super(properties, toolType);
        mainHandModifiers = ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage,
                        AttributeModifier.Operation.ADDITION));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? mainHandModifiers : super.getDefaultAttributeModifiers(slot);
    }
}
