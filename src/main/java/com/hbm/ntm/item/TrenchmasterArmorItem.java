package com.hbm.ntm.item;

import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.jetbrains.annotations.Nullable;

public class TrenchmasterArmorItem extends FsbArmorItem {
    public TrenchmasterArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, boolean noHelmet, int dashCount, FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, noHelmet, dashCount, fullSetTraits);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("  ")
                .append(Component.translatable("armor.moreAmmo"))
                .withStyle(ChatFormatting.RED));
    }

    public static boolean hasTrenchmasterFullSet(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof TrenchmasterArmorItem armor && armor.hasFullSet(player);
    }

    public static boolean hasAceOfSpades(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack mod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
        return !mod.isEmpty() && mod.is(ModItems.CARD_AOS.get());
    }

    public static boolean shouldUseUpTrenchmasterAmmo(Player player) {
        if (hasTrenchmasterFullSet(player) || hasAceOfSpades(player)) {
            return player.getRandom().nextInt(3) < 2;
        }
        return true;
    }

    public static boolean tryCancelIncomingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player) || !hasTrenchmasterFullSet(player)) {
            return false;
        }
        Level level = player.level();
        if (level.random.nextInt(3) != 0) {
            return false;
        }
        HbmPlayerProperties.plink(player, SoundEvents.ITEM_BREAK, 0.5F, 1.0F + level.random.nextFloat() * 0.5F);
        event.setCanceled(true);
        return true;
    }

    public static boolean ignoresSelfExplosion(Player player, DamageSource source) {
        if (player == null || source == null || !hasTrenchmasterFullSet(player)
                || !source.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return source.getDirectEntity() == player || source.getEntity() == player;
    }
}
