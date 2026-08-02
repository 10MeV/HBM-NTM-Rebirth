package com.hbm.saveddata.satellites;

import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Legacy Spy/Mapper satellite command surface. */
public class SatelliteMapper extends SatelliteBase {
    public static final String CMD_TARGET_LOADED = "targetloaded";
    public static final String CMD_GETSMOG = "getsmog";
    public static final String CMD_SPOT_PLAYER = "spotplayers";
    public static final int SPOT_PLAYER_MAX_RANGE = 250;

    public SatelliteMapper() {
        this.ifaceAcs.add(InterfaceActions.HAS_MAP);
        this.satIface = Interfaces.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MAPPER;
    }

    @Override
    public String getType() {
        return "NOT_A_SPY_SATELLITE_:)";
    }

    @Override
    public void onCommandImpl(ServerLevel level, String... command) {
        if (command == null || command.length == 0) {
            return;
        }
        if (CMD_TARGET_LOADED.equals(command[0])) {
            tx = Boolean.toString(level.hasChunk(targetX >> 4, targetZ >> 4)).toUpperCase(Locale.US);
            return;
        }
        if (CMD_GETSMOG.equals(command[0])) {
            float soot = PollutionManager.getPollution(level, new BlockPos(targetX, 255, targetZ), PollutionType.SOOT);
            tx = Integer.toString((int) Math.ceil(soot));
            return;
        }
        if (CMD_SPOT_PLAYER.equals(command[0])) {
            List<String> names = new ArrayList<>();
            for (ServerPlayer player : level.players()) {
                int x = (int) Math.floor(player.getX());
                int z = (int) Math.floor(player.getZ());
                double deltaX = x - targetX;
                double deltaZ = z - targetZ;
                if (deltaX * deltaX + deltaZ * deltaZ > SPOT_PLAYER_MAX_RANGE * SPOT_PLAYER_MAX_RANGE) {
                    continue;
                }
                int height = WorldUtil.legacyGetHeightValue(level, x, z);
                if (height < player.getY() + 2.0D) {
                    names.add(player.getName().getString());
                }
            }
            tx = names.isEmpty() ? "NONE" : String.join(";", names);
        }
    }
}
