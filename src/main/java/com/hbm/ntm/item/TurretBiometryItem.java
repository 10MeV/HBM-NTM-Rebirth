package com.hbm.ntm.item;

import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TurretBiometryItem extends Item {
    private static final String TAG_PLAYER_COUNT = "playercount";
    private static final String TAG_PLAYER_PREFIX = "player_";

    public TurretBiometryItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (String name : getNames(stack)) {
            tooltip.add(Component.literal(name).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        addName(stack, player.getDisplayName().getString());
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("item.hbm_ntm_rebirth.turret_biometry.added"), true);
        }
        LegacySoundPlayer.playLegacyTechBleep(player, 1.0F, 1.0F);
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static List<String> getNames(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            stack.setTag(new CompoundTag());
            return List.of();
        }

        int count = Math.max(0, tag.getInt(TAG_PLAYER_COUNT));
        List<String> names = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            String name = tag.getString(TAG_PLAYER_PREFIX + index);
            if (!name.isBlank()) {
                names.add(name);
            }
        }
        return names;
    }

    public static void addName(ItemStack stack, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        List<String> names = getNames(stack);
        if (names.contains(name)) {
            return;
        }
        tag.putInt(TAG_PLAYER_COUNT, names.size() + 1);
        tag.putString(TAG_PLAYER_PREFIX + names.size(), name);
    }

    public static void removeName(ItemStack stack, int index) {
        List<String> names = new ArrayList<>(getNames(stack));
        if (index < 0 || index >= names.size()) {
            return;
        }
        names.remove(index);
        clearNames(stack);
        for (String name : names) {
            addName(stack, name);
        }
    }

    public static void clearNames(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_PLAYER_COUNT, 0);
    }
}
