package com.hbm.ntm.satellite;

import com.hbm.ntm.entity.logic.DeathBlastEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

public final class SatelliteLaser extends Satellite {
    private long lastOperationMillis;

    public SatelliteLaser() {
        ifaceAcs.add(InterfaceActions.HAS_MAP);
        ifaceAcs.add(InterfaceActions.SHOW_COORDS);
        ifaceAcs.add(InterfaceActions.CAN_CLICK);
        setSatelliteInterface(Interfaces.SAT_PANEL);
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
        tryClick(level, x, z);
    }

    @Override
    public boolean tryClick(ServerLevel level, int x, int z) {
        if (lastOperationMillis + 10_000L >= System.currentTimeMillis()) {
            return false;
        }
        lastOperationMillis = System.currentTimeMillis();
        SatelliteSavedData.get(level).markDirty();

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        DeathBlastEntity blast = new DeathBlastEntity(level);
        blast.setPos(x, y, z);
        level.addFreshEntity(blast);
        return true;
    }
}
