package com.hbm.ntm.satellite;

import com.hbm.ntm.util.RayTraceUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class SatelliteDesignatorItem extends SatelliteChipItem {
    public SatelliteDesignatorItem(Properties properties) {
        super(properties, null, null);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Satellite satellite = SatelliteSavedData.get(serverPlayer.serverLevel()).getSatellite(getFrequency(stack));
            if (satellite != null) {
                BlockHitResult hit = RayTraceUtil.rayTrace(serverPlayer, 300.0D, 1.0F);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    Direction direction = hit.getDirection();
                    BlockPos target = hit.getBlockPos().relative(direction);
                    if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_COORD) {
                        satellite.onCoordAction(serverPlayer.serverLevel(), serverPlayer, target.getX(), target.getY(), target.getZ());
                    } else if (satellite.satelliteInterface() == Satellite.SatelliteInterface.SAT_PANEL) {
                        satellite.onClick(serverPlayer.serverLevel(), target.getX(), target.getZ());
                    }
                    SatelliteSavedData.get(serverPlayer.serverLevel()).setDirty();
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
