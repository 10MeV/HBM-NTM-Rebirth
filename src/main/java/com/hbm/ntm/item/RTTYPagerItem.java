package com.hbm.ntm.item;

import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem.RTTYChannel;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.network.HbmItemControlReceiver;
import com.hbm.ntm.network.ModMessages;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

public class RTTYPagerItem extends Item implements HbmItemControlReceiver {
    public static final String KEY_CHANNEL = "chan";
    private static final int NOTICE_ID_BASE = 1_000;
    private static final int NOTICE_MILLIS = 5_000;
    private static final String SELF_DESTRUCT = "selfdestruct";

    public RTTYPagerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }

        String channelName = getChannel(stack);
        if (channelName.isEmpty()) {
            return;
        }

        RTTYChannel channel = RTTYSystem.listen(level, channelName);
        if (channel == null || channel.timeStamp() < level.getGameTime() - 1L) {
            return;
        }

        String signal = channel.signalString();
        if (SELF_DESTRUCT.equals(signal)) {
            selfDestruct(stack, player);
            return;
        }

        int alive = Math.floorMod(player.tickCount, 1_000);
        Component message = Component.literal("[ " + channelName + " (" + alive + ") ] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(signal).withStyle(ChatFormatting.YELLOW));
        ModMessages.informPlayer(player, message, NOTICE_ID_BASE + slot, NOTICE_MILLIS);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    com.hbm.ntm.client.RTTYPagerScreenBridge.open(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String channelName = getChannel(stack);
        if (channelName.isEmpty()) {
            tooltip.add(Component.literal("No channel set!").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.literal("Channel: " + channelName).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Override
    public void handleItemControl(ServerPlayer player, ItemStack stack, CompoundTag tag) {
        if (tag.contains(KEY_CHANNEL, Tag.TAG_STRING)) {
            setChannel(stack, tag.getString(KEY_CHANNEL));
        }
    }

    public static String getChannel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(KEY_CHANNEL, Tag.TAG_STRING) ? tag.getString(KEY_CHANNEL) : "";
    }

    public static void setChannel(ItemStack stack, String channelName) {
        stack.getOrCreateTag().putString(KEY_CHANNEL, channelName == null ? "" : channelName);
    }

    private static void selfDestruct(ItemStack stack, ServerPlayer player) {
        WeaponExplosionUtil.smooth(player.level(), player.getX(), player.getY() + player.getBbHeight() / 2.0D,
                player.getZ(), 5.0F, null, 50.0F, 1.0D, false, 5.0F, 0.5F).explode();
        stack.shrink(1);
    }
}
