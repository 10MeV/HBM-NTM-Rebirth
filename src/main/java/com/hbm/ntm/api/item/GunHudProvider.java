package com.hbm.ntm.api.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GunHudProvider {
    List<StatusBar> getStatusBars(ItemStack stack, Player player);

    List<AmmoInfo> getAmmoInfo(ItemStack stack, Player player);

    record StatusBar(double progress, int foregroundColor, int backgroundColor) {
    }

    record AmmoInfo(@Nullable ResourceLocation icon, String text) {
    }
}
