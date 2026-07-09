package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Legacy package facade for the 1.7.10 armor card module item.
 */
@Deprecated(forRemoval = false)
public class ItemModCard extends ItemArmorMod {
    public ItemModCard() {
        super(ArmorModHandler.helmet_only, true, true, false, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.is(ModItems.CARD_AOS.get())) {
            tooltip.add(Component.literal("Top of the line!").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Guns now have a 33% chance to not consume ammo.")
                    .withStyle(ChatFormatting.RED));
        }
        if (stack.is(ModItems.CARD_QOS.get())) {
            tooltip.add(Component.literal("Power!").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("Adds a 33% chance to tank damage with no cap.")
                    .withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(stack.getHoverName().copy().withStyle(ChatFormatting.RED));
    }

    @Override
    public void modDamage(LivingHurtEvent event, ItemStack armor) {
        LivingEntity entity = event.getEntity();
        if (this == ModItems.CARD_QOS.get() && entity instanceof Player player
                && entity.getRandom().nextInt(3) == 0) {
            HbmPlayerProperties.plink(player, SoundEvents.ITEM_BREAK, 0.5F,
                    1.0F + entity.getRandom().nextFloat() * 0.5F);
            event.setAmount(0.0F);
            event.setCanceled(true);
        }
    }
}
