package com.hbm.ntm.item;

import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BjArmorItem extends FsbPoweredArmorItem {
    public BjArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain, fullSetTraits);
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (level.isClientSide || getType() != Type.HELMET || hasFullSet(player) || !hasFullSetIgnoreCharge(player)) {
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD).copy();
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        HbmItemStackUtil.giveOrDrop(player, helmet);
        player.hurt(ModDamageSources.source(level, ModDamageSources.LUNAR, player), 1000.0F);
    }
}
