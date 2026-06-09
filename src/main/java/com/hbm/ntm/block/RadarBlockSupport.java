package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.blockentity.RadarRedstoneSource;
import com.hbm.ntm.config.RadarConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;

final class RadarBlockSupport {
    private RadarBlockSupport() {
    }

    static InteractionResult useRadarCore(Level level, BlockPos pos, Player player) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (pos.getY() < RadarConfig.radarAltitude()) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                player.displayClientMessage(Component.literal("[Radar] Error: Radar altitude not sufficient.")
                        .withStyle(ChatFormatting.RED), false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof RadarBlockEntity radar) {
            NetworkHooks.openScreen(serverPlayer, radar, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    static int redstoneOutput(BlockGetter level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof RadarRedstoneSource source ? source.redstoneOutput() : 0;
    }

    static void refreshEnergyConnections(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RadarBlockEntity radar) {
            radar.refreshEnergyConnections();
        }
    }

    static void dropInventory(Level level, BlockPos pos) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RadarBlockEntity radar) {
            for (ItemStack stack : radar.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
