package com.hbm.ntm.item;

import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GeigerCounterItem extends Item {
    public GeigerCounterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), ModSounds.TOOL_TECH_BOOP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            RadiationUtil.printGeigerData(player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
