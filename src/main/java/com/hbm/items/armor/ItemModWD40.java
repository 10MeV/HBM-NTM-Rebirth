package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.ntm.particle.ParticleUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Legacy package facade for the 1.7.10 WD-40 armor module item.
 */
@Deprecated(forRemoval = false)
public class ItemModWD40 extends ItemArmorMod {
    public ItemModWD40() {
        super(ArmorModHandler.extra, true, true, true, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Highly reduces damage taken by armor, +2 HP").withStyle(blink()));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (-80% armor wear / +2 HP)"))
                .withStyle(blink()));
    }

    @Override
    public void modDamage(LivingHurtEvent event, ItemStack armor) {
        if (!event.getEntity().level().isClientSide && armor.getDamageValue() > 0
                && event.getEntity().getRandom().nextInt(5) != 0) {
            armor.setDamageValue(armor.getDamageValue() - 1);
        }
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if (entity.level().isClientSide && entity.hurtTime > 0) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth() * 2.0D;
            double y = entity.getY() + entity.getRandom().nextDouble() * entity.getBbHeight();
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth() * 2.0D;
            ParticleUtil.spawnVanillaExtRedDust(entity.level(), x, y, z, 0.01D, 0.5D, 0.8D);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack armor) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (armor.getItem() instanceof ArmorItem armorItem) {
            modifiers.put(Attributes.MAX_HEALTH, new AttributeModifier(
                    ArmorModHandler.modifierUuidFor(armorItem.getType()),
                    "NTM Armor Mod Health",
                    4.0D,
                    AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    private static ChatFormatting blink() {
        return System.currentTimeMillis() % 1000L < 500L ? ChatFormatting.BLUE : ChatFormatting.YELLOW;
    }
}
