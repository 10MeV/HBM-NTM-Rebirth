package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.ntm.registry.ModItems;
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
 * Legacy package facade for the 1.7.10 health armor module item.
 */
@Deprecated(forRemoval = false)
public class ItemModHealth extends ItemArmorMod {
    private final float health;

    public ItemModHealth(float health) {
        super(ArmorModHandler.extra, false, true, false, false);
        this.health = health;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+" + formattedHealth() + " health").withStyle(blink()));
        tooltip.add(Component.empty());
        if (stack.is(ModItems.BLACK_DIAMOND.get())) {
            tooltip.add(Component.literal("Nostalgia").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.empty());
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (+" + formattedHealth() + " health)"))
                .withStyle(blink()));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack armor) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (armor.getItem() instanceof ArmorItem armorItem) {
            modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    ArmorModHandler.modifierUuidFor(armorItem.getType()),
                    "NTM Armor Mod Health",
                    health,
                    AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    private String formattedHealth() {
        return Float.toString(Math.round(health * 10.0F) * 0.1F);
    }

    private static ChatFormatting blink() {
        return System.currentTimeMillis() % 1000L < 500L ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE;
    }
}
