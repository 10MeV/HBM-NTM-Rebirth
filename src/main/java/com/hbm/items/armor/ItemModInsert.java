package com.hbm.items.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Legacy package facade for the 1.7.10 ballistic insert armor modules.
 */
@Deprecated(forRemoval = false)
public class ItemModInsert extends ItemArmorMod {
    public final float damageMod;
    public final float projectileMod;
    public final float explosionMod;
    public final float speed;

    public ItemModInsert(int durability, float damageMod, float projectileMod, float explosionMod, float speed) {
        super(new Item.Properties().durability(durability), ArmorModHandler.kevlar, false, true, false, false,
                false);
        this.damageMod = damageMod;
        this.projectileMod = projectileMod;
        this.explosionMod = explosionMod;
        this.speed = speed;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (damageMod != 1.0F) {
            tooltip.add(Component.literal(percentLine("damage", damageMod)).withStyle(ChatFormatting.RED));
        }
        if (projectileMod != 1.0F) {
            tooltip.add(Component.literal("-" + Math.round((1.0F - projectileMod) * 100.0F)
                    + "% projectile damage").withStyle(ChatFormatting.YELLOW));
        }
        if (explosionMod != 1.0F) {
            tooltip.add(Component.literal("-" + Math.round((1.0F - explosionMod) * 100.0F)
                    + "% explosion damage").withStyle(ChatFormatting.YELLOW));
        }
        if (speed != 1.0F) {
            tooltip.add(Component.literal("-" + Math.round((1.0F - speed) * 100.0F) + "% speed")
                    .withStyle(ChatFormatting.BLUE));
        }
        if (stack.is(ModItems.INSERT_POLONIUM.get())) {
            tooltip.add(Component.literal("+100 RAD/s").withStyle(ChatFormatting.DARK_RED));
        }
        tooltip.add(Component.literal((stack.getMaxDamage() - stack.getDamageValue()) + "/"
                + stack.getMaxDamage() + "HP"));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        List<String> desc = new ArrayList<>();
        if (damageMod != 1.0F) {
            desc.add((damageMod < 1.0F ? "-" : "+")
                    + Math.abs(Math.round((1.0F - damageMod) * 100.0F)) + "% dmg");
        }
        if (projectileMod != 1.0F) {
            desc.add("-" + Math.round((1.0F - projectileMod) * 100.0F) + "% proj");
        }
        if (explosionMod != 1.0F) {
            desc.add("-" + Math.round((1.0F - explosionMod) * 100.0F) + "% exp");
            desc.add("-" + Math.round((1.0F - speed) * 100.0F) + "% speed");
        }
        if (stack.is(ModItems.INSERT_POLONIUM.get())) {
            desc.add("+100 RAD/s");
        }
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (" + String.join(" / ", desc) + " / "
                        + (stack.getMaxDamage() - stack.getDamageValue()) + "HP)"))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public void modDamage(LivingHurtEvent event, ItemStack armor) {
        float amount = event.getAmount() * damageMod;
        if (event.getSource().is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= projectileMod;
        }
        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= explosionMod;
        }
        event.setAmount(amount);

        ItemStack insert = ArmorModHandler.pryMod(armor, ArmorModHandler.kevlar);
        if (insert.isEmpty()) {
            return;
        }

        insert.setDamageValue(insert.getDamageValue() + 1);

        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && this == ModItems.INSERT_ERA.get()) {
            entity.level().explode(event.getSource().getEntity(), entity.getX(),
                    entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                    0.05F, false, Level.ExplosionInteraction.NONE);
        }

        if (insert.getDamageValue() >= insert.getMaxDamage()) {
            ArmorModHandler.removeMod(armor, ArmorModHandler.kevlar);
        } else {
            ArmorModHandler.applyMod(armor, insert);
        }
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if (!entity.level().isClientSide && this == ModItems.INSERT_POLONIUM.get()) {
            HbmLivingProps.incrementRadiation(entity, 100.0F);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack armor) {
        if (speed == 1.0F || !(armor.getItem() instanceof ArmorItem armorItem)) {
            return null;
        }
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        modifiers.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(
                ArmorModHandler.modifierUuidFor(armorItem.getType()),
                "NTM Armor Mod Speed",
                -1.0F + speed,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    private static String percentLine(String label, float modifier) {
        return (modifier < 1.0F ? "-" : "+")
                + Math.abs(Math.round((1.0F - modifier) * 100.0F)) + "% " + label;
    }
}
