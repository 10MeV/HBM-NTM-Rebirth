package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DemonCoreItem extends Item {
    public DemonCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide && entity.onGround()) {
            entity.setItem(new ItemStack(ModItems.DEMON_CORE_CLOSED.get()));
            HbmItemStackUtil.dropStack(entity.level(), entity.getX(), entity.getY(), entity.getZ(),
                    new ItemStack(ModItems.SCREWDRIVER.get()));
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.trait.drop").withStyle(ChatFormatting.RED));
    }
}
