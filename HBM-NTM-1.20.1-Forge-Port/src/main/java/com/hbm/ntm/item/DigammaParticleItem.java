package com.hbm.ntm.item;

import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DigammaParticleItem extends Item {
    private final int digammaHalfLifeTicks;

    public DigammaParticleItem(Properties properties, int digammaHalfLifeTicks) {
        super(properties);
        this.digammaHalfLifeTicks = Math.max(1, digammaHalfLifeTicks);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof Player player) {
            RadiationUtil.applyDigammaData(player, 1.0F / digammaHalfLifeTicks);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.particle_digamma.desc.half_particle").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.particle_digamma.desc.half_player",
                String.format(java.util.Locale.ROOT, "%.1fs", digammaHalfLifeTicks / 20.0F)).withStyle(ChatFormatting.RED));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.particle_digamma.desc.digamma",
                String.format(java.util.Locale.ROOT, "%.1f", ((1000.0F / digammaHalfLifeTicks) * 200.0F) / 10.0F))
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.trait.drop").withStyle(ChatFormatting.RED));
    }
}
