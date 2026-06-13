package com.hbm.ntm.item;

import com.hbm.ntm.util.RayTraceUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RangefinderItem extends Item {
    public RangefinderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BlockHitResult hit = RayTraceUtil.rayTrace(player, 300.0D, 1.0F);
            if (hit.getType() == HitResult.Type.BLOCK) {
                Vec3 eye = player.getEyePosition(1.0F);
                double distance = eye.distanceTo(hit.getLocation());
                player.displayClientMessage(Component.literal(((int) (distance * 10.0D)) / 10.0D + "m"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
