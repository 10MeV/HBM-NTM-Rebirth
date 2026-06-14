package com.hbm.ntm.armor;

import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorModItem extends Item {
    private final ArmorModHandler.ArmorModSlot slot;
    private final boolean helmet;
    private final boolean chestplate;
    private final boolean leggings;
    private final boolean boots;

    public ArmorModItem(Properties properties, ArmorModHandler.ArmorModSlot slot,
                        boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
        this(properties, slot, helmet, chestplate, leggings, boots, true);
    }

    protected ArmorModItem(Properties properties, ArmorModHandler.ArmorModSlot slot,
                           boolean helmet, boolean chestplate, boolean leggings, boolean boots,
                           boolean forceSingleStack) {
        super(forceSingleStack ? properties.stacksTo(1) : properties);
        this.slot = slot;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public ArmorModHandler.ArmorModSlot slot() {
        return slot;
    }

    public boolean supports(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> helmet;
            case CHESTPLATE -> chestplate;
            case LEGGINGS -> leggings;
            case BOOTS -> boots;
        };
    }

    public boolean supportsHelmet() {
        return helmet;
    }

    public boolean supportsChestplate() {
        return chestplate;
    }

    public boolean supportsLeggings() {
        return leggings;
    }

    public boolean supportsBoots() {
        return boots;
    }

    public void onArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
    }

    public void onClientArmorModTick(LivingEntity entity, ItemStack armor, ItemStack mod) {
    }

    public void onArmorModHurt(LivingHurtEvent event, ItemStack armor, ItemStack mod) {
    }

    public void addArmorModAttributeModifiers(ItemStack armor, ItemStack mod,
                                              Multimap<Attribute, AttributeModifier> modifiers) {
    }

    public void appendInstalledArmorModTooltip(ItemStack mod, ItemStack armor, List<Component> tooltip,
                                               TooltipFlag flag) {
        tooltip.add(mod.getHoverName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("armorMod.applicableTo").withStyle(ChatFormatting.DARK_PURPLE));
        if (helmet && chestplate && leggings && boots) {
            tooltip.add(Component.literal("  ").append(Component.translatable("armorMod.all")));
        } else {
            if (helmet) {
                tooltip.add(Component.literal("  ").append(Component.translatable("armorMod.helmets")));
            }
            if (chestplate) {
                tooltip.add(Component.literal("  ").append(Component.translatable("armorMod.chestplates")));
            }
            if (leggings) {
                tooltip.add(Component.literal("  ").append(Component.translatable("armorMod.leggings")));
            }
            if (boots) {
                tooltip.add(Component.literal("  ").append(Component.translatable("armorMod.boots")));
            }
        }
        tooltip.add(Component.translatable("armorMod.slot").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.literal("  ").append(Component.translatable(slotTranslationKey())));
    }

    private String slotTranslationKey() {
        return switch (slot) {
            case HELMET_ONLY -> "armorMod.type.helmet";
            case PLATE_ONLY -> "armorMod.type.chestplate";
            case LEGS_ONLY -> "armorMod.type.leggings";
            case BOOTS_ONLY -> "armorMod.type.boots";
            case SERVOS -> "armorMod.type.servo";
            case CLADDING -> "armorMod.type.cladding";
            case KEVLAR -> "armorMod.type.insert";
            case EXTRA -> "armorMod.type.special";
            case BATTERY -> "armorMod.type.battery";
        };
    }
}
