package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyGavelItem extends HbmAbilitySwordItem {
    private final boolean lead;
    private final String tooltipKey;

    public LegacyGavelItem(Tier tier, float attackDamage, boolean lead, String tooltipKey, Item.Properties properties) {
        super(tier, attackDamage, 0.0D, properties);
        this.lead = lead;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide) {
            LegacySoundPlayer.playLegacyGavelWhack(level, target.getX(), target.getY(), target.getZ(), 3.0F, 1.0F);
            if (lead) {
                target.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 15 * 20, 4));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(tooltipKey));
    }
}
