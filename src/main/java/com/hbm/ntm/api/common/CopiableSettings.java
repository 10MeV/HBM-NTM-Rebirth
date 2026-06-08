package com.hbm.ntm.api.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public interface CopiableSettings {
    CompoundTag getSettings(Level level, BlockPos pos);

    void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos);

    default String getSettingsSourceId(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    default Component getSettingsSourceDisplay(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock().getName();
    }

    default List<Component> infoForDisplay(Level level, BlockPos pos) {
        return List.of();
    }
}
