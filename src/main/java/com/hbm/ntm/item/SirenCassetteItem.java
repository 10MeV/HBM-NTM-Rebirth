package com.hbm.ntm.item;

import com.hbm.ntm.sound.LegacySirenTrack;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SirenCassetteItem extends Item {
    public static final String TAG_TRACK_ID = "trackId";

    public SirenCassetteItem(Properties properties) {
        super(properties);
    }

    public static LegacySirenTrack track(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SirenCassetteItem)) {
            return LegacySirenTrack.NULL;
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? LegacySirenTrack.NULL : LegacySirenTrack.byId(tag.getInt(TAG_TRACK_ID));
    }

    public static ItemStack stackForTrack(Item item, LegacySirenTrack track) {
        ItemStack stack = new ItemStack(item);
        if (track != LegacySirenTrack.NULL) {
            stack.getOrCreateTag().putInt(TAG_TRACK_ID, track.id());
        }
        return stack;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, SirenCassetteItem item) {
        for (LegacySirenTrack track : LegacySirenTrack.values()) {
            if (track != LegacySirenTrack.NULL) {
                output.accept(stackForTrack(item, track));
            }
        }
    }

    public int tint(ItemStack stack, int tintIndex) {
        return tintIndex == 1 ? track(stack).color() : 0xFFFFFF;
    }

    @Override
    public Component getName(ItemStack stack) {
        LegacySirenTrack track = track(stack);
        if (track == LegacySirenTrack.NULL) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId(stack)).append(" - ").append(track.title());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        LegacySirenTrack track = track(stack);
        tooltip.add(Component.literal("Siren sound cassette:").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("   Name: " + track.title()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("   Type: " + track.type().name()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("   Volume: " + track.volume()).withStyle(ChatFormatting.GRAY));
    }
}
