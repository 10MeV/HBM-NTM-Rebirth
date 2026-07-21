package com.hbm.ntm.item;

import com.hbm.items.armor.IAttackHandler;
import com.hbm.items.armor.IDamageHandler;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class No9ArmorItem extends ObjArmorItem implements IAttackHandler, IDamageHandler {
    private static final String TAG_IS_ON = "isOn";

    public No9ArmorItem(Properties properties) {
        super(HbmArmorMaterials.STEEL, Type.HELMET, properties,
                List.of(new TooltipLine("tooltip.hbm_ntm_rebirth.armor.dt_0_5", ChatFormatting.BLUE),
                        new TooltipLine("tooltip.hbm_ntm_rebirth.no9.coal_breathing", ChatFormatting.YELLOW)));
    }

    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) {
            return;
        }

        boolean turnOn = HbmPlayerProperties.isHudEnabled(player);
        boolean wasOn = stack.getOrCreateTag().getBoolean(TAG_IS_ON);
        if (turnOn != wasOn) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    turnOn ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS, turnOn ? 1.0F : 0.5F, turnOn ? 1.5F : 2.0F);
        }
        stack.getOrCreateTag().putBoolean(TAG_IS_ON, turnOn);

        int maxBlackLung = HbmLivingProperties.MAX_BLACK_LUNG;
        int cap = (int) (maxBlackLung * 0.9D);
        int blackLung = HbmLivingProperties.capBlackLung(player, cap);
        if (blackLung >= maxBlackLung * 0.25D) {
            HbmLivingProperties.reduceBlackLung(player, 1);
        }
    }

    @Override
    public void handleAttack(LivingAttackEvent event, ItemStack armor) {
        if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR) && event.getAmount() <= 0.5F) {
            event.setCanceled(true);
        }
    }

    @Override
    public void handleDamage(LivingHurtEvent event, ItemStack armor) {
        if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setAmount(Math.max(0.0F, event.getAmount() - 0.5F));
        }
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
