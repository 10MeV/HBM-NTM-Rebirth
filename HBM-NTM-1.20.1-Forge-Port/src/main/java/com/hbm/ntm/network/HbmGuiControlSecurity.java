package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class HbmGuiControlSecurity {
    public static final double DEFAULT_MAX_DISTANCE = 16.0D;
    public static final double DEFAULT_MAX_DISTANCE_SQ = DEFAULT_MAX_DISTANCE * DEFAULT_MAX_DISTANCE;

    private HbmGuiControlSecurity() {
    }

    @Nullable
    public static <T extends BlockEntity> T validateTileControl(ServerPlayer player, BlockPos pos, Class<T> blockEntityType,
            @Nullable Class<? extends AbstractContainerMenu> requiredMenu, String packetName) {
        if (player == null || pos == null) {
            return null;
        }
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > DEFAULT_MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote {} from {} at {}: too far",
                    packetName, player.getGameProfile().getName(), pos);
            return null;
        }
        if (requiredMenu != null && !requiredMenu.isInstance(player.containerMenu)) {
            HbmNtm.LOGGER.warn("Blocked remote {} from {} at {}: wrong menu {}",
                    packetName, player.getGameProfile().getName(), pos,
                    player.containerMenu == null ? "none" : player.containerMenu.getClass().getSimpleName());
            return null;
        }

        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!blockEntityType.isInstance(blockEntity)) {
            HbmNtm.LOGGER.warn("Blocked remote {} from {} at {}: wrong block entity {}",
                    packetName, player.getGameProfile().getName(), pos,
                    blockEntity == null ? "none" : blockEntity.getClass().getSimpleName());
            return null;
        }
        return blockEntityType.cast(blockEntity);
    }

    @Nullable
    public static BlockEntity validateTileControl(ServerPlayer player, BlockPos pos,
            @Nullable Class<? extends AbstractContainerMenu> requiredMenu, String packetName) {
        return validateTileControl(player, pos, BlockEntity.class, requiredMenu, packetName);
    }

    @Nullable
    public static BlockEntity validateTileControl(ServerPlayer player, BlockPos pos, String packetName) {
        return validateTileControl(player, pos, BlockEntity.class, null, packetName);
    }

    public static void markChangedAndUpdate(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return;
        }
        blockEntity.setChanged();
        blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(),
                blockEntity.getBlockState(), 3);
    }
}
