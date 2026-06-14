package com.hbm.ntm.item;

import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GasMaskFilterItem extends Item {
    public GasMaskFilterItem(Properties properties) {
        super(properties.stacksTo(1).durability(20_000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack filter = player.getItemInHand(hand);
        ArmorUtil.GasMaskFilterInstallResult result = ArmorUtil.installWornGasMaskFilter(player, filter);
        if (!result.installed()) {
            return InteractionResultHolder.pass(filter);
        }
        if (!level.isClientSide) {
            LegacySoundPlayer.playLegacyGasMaskScrew(player);
        }
        return InteractionResultHolder.sidedSuccess(result.replacement(), level.isClientSide);
    }
}
