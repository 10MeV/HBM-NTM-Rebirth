package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyUllapoolCaberItem extends HbmAbilitySwordItem {
    public LegacyUllapoolCaberItem(Properties properties) {
        super(HbmToolTiers.STEEL, 6.0F, 0.0D, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide) {
            level.explode(null, target.getX(), target.getY(), target.getZ(), 7.5F, true,
                    Level.ExplosionInteraction.BLOCK);
            stack.hurtAndBreak(505, attacker, owner -> owner.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(getDescriptionId() + ".desc.0"));
        tooltip.add(Component.translatable(getDescriptionId() + ".desc.1"));
    }
}
