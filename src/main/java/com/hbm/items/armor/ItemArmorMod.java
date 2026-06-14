package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

/**
 * Legacy package facade for the 1.7.10 armor module base item.
 */
@Deprecated(forRemoval = false)
public class ItemArmorMod extends ArmorModItem {
    public final int type;
    public final boolean helmet;
    public final boolean chestplate;
    public final boolean leggings;
    public final boolean boots;

    public ItemArmorMod(int type, boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
        this(new Item.Properties(), type, helmet, chestplate, leggings, boots);
    }

    protected ItemArmorMod(Item.Properties properties, int type, boolean helmet, boolean chestplate,
                           boolean leggings, boolean boots) {
        super(properties, ArmorModHandler.slotByLegacyIndex(type), helmet, chestplate, leggings, boots);
        this.type = type;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(stack.getHoverName());
    }

    public void modUpdate(LivingEntity entity, ItemStack armor) {
    }

    public void modDamage(LivingHurtEvent event, ItemStack armor) {
    }

    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack armor) {
        return null;
    }

    @Override
    public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
        modUpdate(entity, armor);
    }

    @Override
    public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
        modDamage(event, armor);
    }

    @Override
    public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                              Multimap<Attribute, AttributeModifier> modifiers) {
        Multimap<Attribute, AttributeModifier> legacyModifiers = getModifiers(armor);
        if (legacyModifiers != null) {
            modifiers.putAll(legacyModifiers);
        }
    }

    @Override
    public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                               TooltipFlag flag) {
        addDesc(tooltip, mod, armor);
    }
}
