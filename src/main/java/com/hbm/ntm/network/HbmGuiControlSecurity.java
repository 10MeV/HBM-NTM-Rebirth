package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.multiblock.MultiblockHelper;
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
        return validateTileControl(player, pos, blockEntityType, requiredMenu, packetName, true);
    }

    @Nullable
    public static <T extends BlockEntity> T validateTileControlWithoutDefaultDistance(ServerPlayer player, BlockPos pos,
            Class<T> blockEntityType, @Nullable Class<? extends AbstractContainerMenu> requiredMenu,
            String packetName) {
        return validateTileControl(player, pos, blockEntityType, requiredMenu, packetName, false);
    }

    @Nullable
    private static <T extends BlockEntity> T validateTileControl(ServerPlayer player, BlockPos pos,
            Class<T> blockEntityType, @Nullable Class<? extends AbstractContainerMenu> requiredMenu,
            String packetName, boolean enforceDefaultDistance) {
        if (player == null || pos == null) {
            return null;
        }
        ServerLevel level = player.serverLevel();
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return null;
        }
        BlockEntity blockEntity = MultiblockHelper.resolveOperationalCoreBlockEntity(level, pos);
        BlockPos receiverPos = blockEntity == null ? pos : blockEntity.getBlockPos();
        if (enforceDefaultDistance && player.distanceToSqr(receiverPos.getX() + 0.5D, receiverPos.getY() + 0.5D,
                receiverPos.getZ() + 0.5D) > DEFAULT_MAX_DISTANCE_SQ) {
            HbmNtm.LOGGER.warn("Blocked remote {} from {} at {} resolved to {}: too far",
                    packetName, player.getGameProfile().getName(), pos, receiverPos);
            return null;
        }
        if (requiredMenu != null && !requiredMenu.isInstance(player.containerMenu)) {
            HbmNtm.LOGGER.warn("Blocked remote {} from {} at {} resolved to {}: wrong menu {}",
                    packetName, player.getGameProfile().getName(), pos, receiverPos,
                    player.containerMenu == null ? "none" : player.containerMenu.getClass().getSimpleName());
            return null;
        }
        if (!blockEntityType.isInstance(blockEntity)) {
            HbmNtm.LOGGER.warn("Blocked remote {} from {} at {} resolved to {}: wrong block entity {}",
                    packetName, player.getGameProfile().getName(), pos, receiverPos,
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

    @Nullable
    public static BlockEntity validateTileControlWithoutDefaultDistance(ServerPlayer player, BlockPos pos,
            String packetName) {
        return validateTileControlWithoutDefaultDistance(player, pos, BlockEntity.class, null, packetName);
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
