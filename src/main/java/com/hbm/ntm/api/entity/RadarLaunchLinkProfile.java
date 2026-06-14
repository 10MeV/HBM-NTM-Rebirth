package com.hbm.ntm.api.entity;

import com.hbm.ntm.item.ItemCoordinateBase;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RadarLaunchLinkProfile {
    private RadarLaunchLinkProfile() {
    }

    public static RadarCommandResult dispatch(ServerLevel level, BlockPos radarPos, ServerPlayer player,
            ItemStack link, RadarLaunchCommand command) {
        if (level == null || radarPos == null || player == null || link == null || link.isEmpty()
                || command == null) {
            return RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        if (link.is(ModItems.SAT_RELAY.get())) {
            return dispatchSatellite(level, player, link, command);
        }
        if (link.is(ModItems.RADAR_LINKER.get())) {
            return dispatchRadarLinker(level, radarPos, link, command);
        }
        return RadarCommandResult.ERROR_INCOMPATIBLE;
    }

    private static RadarCommandResult dispatchSatellite(ServerLevel level, ServerPlayer player, ItemStack link,
            RadarLaunchCommand command) {
        if (command.targetsEntity()) {
            return RadarCommandResult.ERROR_INCOMPATIBLE;
        }
        Satellite satellite = SatelliteSavedData.get(level).getSatFromFreq(ISatelliteChip.getFrequencyFromStack(link));
        if (satellite == null) {
            return RadarCommandResult.ERROR_NO_TARGET;
        }
        return RadarSatelliteCommand.dispatch(level, player, satellite, command);
    }

    private static RadarCommandResult dispatchRadarLinker(ServerLevel level, BlockPos radarPos, ItemStack link,
            RadarLaunchCommand command) {
        BlockPos receiverPos = ItemCoordinateBase.getPosition(link);
        if (receiverPos == null) {
            return RadarCommandResult.ERROR_NO_TARGET;
        }
        return RadarLinkedReceiverCommand.dispatch(level, radarPos, receiverPos, command);
    }
}
