package com.hbm.ntm.satellite;

import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.hbm.ntm.itempool.HbmItemPoolIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

final class SatelliteMapper extends Satellite {
    SatelliteMapper() {
        interfaceActions.add(InterfaceAction.HAS_MAP);
        satelliteInterface = SatelliteInterface.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MAPPER;
    }
}

final class SatelliteScanner extends Satellite {
    SatelliteScanner() {
        interfaceActions.add(InterfaceAction.HAS_ORES);
        satelliteInterface = SatelliteInterface.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.SCANNER;
    }
}

final class SatelliteRadar extends Satellite {
    SatelliteRadar() {
        interfaceActions.add(InterfaceAction.HAS_MAP);
        interfaceActions.add(InterfaceAction.HAS_RADAR);
        satelliteInterface = SatelliteInterface.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RADAR;
    }
}

final class SatelliteLaser extends Satellite {
    private long lastOperationMillis;

    SatelliteLaser() {
        interfaceActions.add(InterfaceAction.HAS_MAP);
        interfaceActions.add(InterfaceAction.SHOW_COORDS);
        interfaceActions.add(InterfaceAction.CAN_CLICK);
        satelliteInterface = SatelliteInterface.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.LASER;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("lastOp", lastOperationMillis);
    }

    @Override
    public void load(CompoundTag tag) {
        lastOperationMillis = tag.getLong("lastOp");
    }

    public long lastOperationMillis() {
        return lastOperationMillis;
    }

    @Override
    public void onClick(ServerLevel level, int x, int z) {
        if (lastOperationMillis + 10_000L >= System.currentTimeMillis()) {
            return;
        }
        lastOperationMillis = System.currentTimeMillis();
        SatelliteSavedData.get(level).markDirty();

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        DeathBlastEntity blast = new DeathBlastEntity(level);
        blast.setPos(x, y, z);
        level.addFreshEntity(blast);
    }
}

final class SatelliteResonator extends Satellite {
    SatelliteResonator() {
        coordActions.add(CoordAction.HAS_Y);
        satelliteInterface = SatelliteInterface.SAT_COORD;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RESONATOR;
    }

    @Override
    public void onCoordAction(ServerLevel level, ServerPlayer player, int x, int y, int z) {
        playTeleportSound(level, player);
        player.stopRiding();
        player.teleportTo(level, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
        playTeleportSound(level, player);
    }
}

final class SatelliteRelay extends Satellite {
    SatelliteRelay() {
        satelliteInterface = SatelliteInterface.NONE;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.RELAY;
    }
}

class SatelliteMiner extends Satellite {
    static final String POOL_SAT_MINER = HbmItemPoolIds.POOL_SAT_MINER;
    static final String POOL_SAT_LUNAR = HbmItemPoolIds.POOL_SAT_LUNAR;

    private long lastOperationMillis;

    SatelliteMiner() {
        satelliteInterface = SatelliteInterface.NONE;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.MINER;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("lastOp", lastOperationMillis);
    }

    @Override
    public void load(CompoundTag tag) {
        lastOperationMillis = tag.getLong("lastOp");
    }

    public java.util.Optional<String> cargoPool() {
        return java.util.Optional.of(POOL_SAT_MINER);
    }

    @Override
    public long lastOperationMillis() {
        return lastOperationMillis;
    }

    @Override
    public void setLastOperationMillis(long timeMillis) {
        lastOperationMillis = timeMillis;
    }
}

final class SatelliteLunarMiner extends SatelliteMiner {
    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.LUNAR_MINER;
    }

    @Override
    public java.util.Optional<String> cargoPool() {
        return java.util.Optional.of(POOL_SAT_LUNAR);
    }
}

final class SatelliteHorizons extends Satellite {
    private boolean used;

    SatelliteHorizons() {
        satelliteInterface = SatelliteInterface.SAT_COORD;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.HORIZONS;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putBoolean("used", used);
    }

    @Override
    public void load(CompoundTag tag) {
        used = tag.getBoolean("used");
    }

    public boolean used() {
        return used;
    }
}
