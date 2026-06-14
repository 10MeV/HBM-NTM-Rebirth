package com.hbm.ntm.item;

import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DigammaDiagnosticItem extends Item {
    public DigammaDiagnosticItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            LegacySoundPlayer.playLegacyTechBoop(player, 1.0F, 1.0F);
            RadiationUtil.printDiagnosticData(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
