package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Legacy package facade for the 1.7.10 iron cladding armor module.
 */
@Deprecated(forRemoval = false)
public class ItemModIron extends ItemArmorMod {
    public ItemModIron() {
        super(ArmorModHandler.cladding, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+0.5 knockback resistance").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (+0.5 knockback resistence)"))
                .withStyle(ChatFormatting.WHITE));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack armor) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (armor.getItem() instanceof ArmorItem armorItem) {
            modifiers.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(
                    ArmorModHandler.modifierUuidFor(armorItem.getType()),
                    "NTM Armor Mod Knockback",
                    0.5D,
                    AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }
}
