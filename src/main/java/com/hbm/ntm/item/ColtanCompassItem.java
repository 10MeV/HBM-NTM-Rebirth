package com.hbm.ntm.item;

import com.hbm.ntm.worldgen.ColtanDepositUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class ColtanCompassItem extends Item {
    private static final String TAG_COLTAN_X = "colX";
    private static final String TAG_COLTAN_Z = "colZ";

    public ColtanCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Points towards the coltan deposit.").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("The deposit is a large area where coltan ore spawns like standard ore,")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("it's not one large blob of ore on that exact location.")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) {
            writeTarget(stack, level);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BlockPos target = writeTarget(stack, level);
            int distance = (int) Math.sqrt(player.distanceToSqr(target.getX(), player.getY(), target.getZ()));
            player.sendSystemMessage(Component.literal("Coltan deposit: X " + target.getX()
                    + ", Z " + target.getZ() + " (" + distance + "m)").withStyle(ChatFormatting.GOLD));
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static BlockPos writeTarget(ItemStack stack, Level level) {
        long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : 0L;
        BlockPos target = ColtanDepositUtil.center(seed);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_COLTAN_X, target.getX());
        tag.putInt(TAG_COLTAN_Z, target.getZ());
        return target;
    }
}
