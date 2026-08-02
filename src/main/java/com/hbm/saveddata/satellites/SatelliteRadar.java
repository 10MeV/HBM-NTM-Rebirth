package com.hbm.saveddata.satellites;

import com.hbm.ntm.api.entity.RadarScanner;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.satellite.LegacySatelliteType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Legacy low-earth-orbit Radar satellite command surface. */
public class SatelliteRadar extends SatelliteBase {
    public static final int MAX_SCAN_RANGE = 1_000;
    public static final String CMD_SURVEY = "survey";
    public static final String CMD_FILTER = "filter";
    public static final String CMD_COUNT = "count";
    public static final String CMD_GETTARGETID = "gettargetid";
    public static final String CMD_GETPOSITION = "getposition";
    public static final String CMD_GETNAME = "getname";

    public List<Entity> cachedRadarResults = new ArrayList<>();
    public List<Entity> filteredRadarResults = new ArrayList<>();

    public SatelliteRadar() {
        this.ifaceAcs.add(InterfaceActions.HAS_MAP);
        this.ifaceAcs.add(InterfaceActions.HAS_RADAR);
        this.satIface = Interfaces.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RADAR;
    }

    @Override
    public String getType() {
        return "LEO_RADAR";
    }

    @Override
    public void onCommandImpl(ServerLevel level, String... command) {
        if (command == null || command.length == 0) {
            return;
        }
        if (CMD_SURVEY.equals(command[0])) {
            cachedRadarResults.clear();
            for (Entity entity : RadarScanner.matchingEntitiesSnapshot()) {
                if (!entity.level().dimension().equals(level.dimension())) {
                    continue;
                }
                int x = (int) Math.floor(entity.getX());
                int z = (int) Math.floor(entity.getZ());
                double deltaX = x - targetX;
                double deltaZ = z - targetZ;
                if (deltaX * deltaX + deltaZ * deltaZ <= MAX_SCAN_RANGE * MAX_SCAN_RANGE) {
                    cachedRadarResults.add(entity);
                }
            }
            filteredRadarResults = new ArrayList<>(cachedRadarResults);
            return;
        }
        if (CMD_FILTER.equals(command[0]) && command.length == 2) {
            filteredRadarResults.clear();
            String filter = command[1].toLowerCase(Locale.US);
            for (Entity entity : cachedRadarResults) {
                if (!entity.isRemoved() && entity.getClass().getSimpleName().toLowerCase(Locale.US).contains(filter)) {
                    filteredRadarResults.add(entity);
                }
            }
            return;
        }
        if (CMD_COUNT.equals(command[0])) {
            tx = Integer.toString(filteredRadarResults.size());
            return;
        }
        if (CMD_GETTARGETID.equals(command[0]) && command.length == 2) {
            Entity target = getTargetFromIndex(command[1]);
            tx = target == null ? "" : Integer.toString(target.getId());
            return;
        }
        if (CMD_GETPOSITION.equals(command[0]) && command.length == 2) {
            Entity target = getTargetFromIndex(command[1]);
            tx = target == null ? "" : (int) Math.floor(target.getX()) + ";"
                    + (int) Math.floor(target.getY()) + ";" + (int) Math.floor(target.getZ());
            return;
        }
        if (CMD_GETNAME.equals(command[0]) && command.length == 2) {
            Entity target = getTargetFromIndex(command[1]);
            tx = target == null ? "" : target.getClass().getSimpleName().toLowerCase(Locale.US);
        }
    }

    public Entity getTargetFromIndex(String value) {
        if (filteredRadarResults.isEmpty()) {
            return null;
        }
        int index = RORInteractive.parseInt(value, 1, filteredRadarResults.size()) - 1;
        Entity target = filteredRadarResults.get(index);
        return target.isRemoved() ? null : target;
    }
}
