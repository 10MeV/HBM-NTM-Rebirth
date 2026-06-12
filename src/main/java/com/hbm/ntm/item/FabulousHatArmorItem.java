package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class FabulousHatArmorItem extends ObjArmorItem {
    public FabulousHatArmorItem(ArmorMaterial material, Properties properties) {
        super(material, Type.HELMET, properties,
                List.of(new TooltipLine("tooltip.hbm_ntm_rebirth.armor.dt_2", ChatFormatting.BLUE)));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {
            entity.discard();
        }
        return true;
    }
}
