package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HbmRagItem extends Item {
    public HbmRagItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide
                && entity.level().getFluidState(entity.blockPosition()).is(FluidTags.WATER)) {
            entity.setItem(new ItemStack(ModItems.legacyItem("rag_damp").get(), stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!level.isClientSide) {
            ItemStack soaked = new ItemStack(ModItems.legacyItem("rag_piss").get());
            HbmItemStackUtil.giveOrDrop(player, soaked);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
