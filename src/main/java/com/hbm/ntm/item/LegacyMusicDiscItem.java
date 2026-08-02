package com.hbm.ntm.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

/** Preserves the legacy HBM record_11 display-name contract while using modern jukebox behavior. */
public class LegacyMusicDiscItem extends RecordItem {
    public LegacyMusicDiscItem(int comparatorOutput, SoundEvent sound, Item.Properties properties, int lengthInSeconds) {
        super(comparatorOutput, sound, properties, lengthInSeconds);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.minecraft.music_disc_11.desc");
    }
}
