package com.hbm.saveddata.satellites;

import com.hbm.ntm.entity.logic.DeathBlastEntity;
import com.hbm.ntm.satellite.LegacySatelliteType;
import com.hbm.ntm.world.WorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public class SatelliteLaser extends Satellite {
    public long lastOp;

    public SatelliteLaser() {
        this.ifaceAcs.add(InterfaceActions.HAS_MAP);
        this.ifaceAcs.add(InterfaceActions.SHOW_COORDS);
        this.ifaceAcs.add(InterfaceActions.CAN_CLICK);
        this.satIface = Interfaces.SAT_PANEL;
    }

    @Override
    public LegacySatelliteType type() {
        return LegacySatelliteType.LASER;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putLong("lastOp", lastOp);
    }

    @Override
    public void load(CompoundTag tag) {
        lastOp = tag.getLong("lastOp");
    }

    @Override
    public long lastOperationMillis() {
        return lastOp;
    }

    @Override
    public void setLastOperationMillis(long timeMillis) {
        lastOp = timeMillis;
    }

    @Override
    public void onClick(ServerLevel level, int x, int z) {
        tryClick(level, x, z);
    }

    @Override
    public boolean tryClick(ServerLevel level, int x, int z) {
        if (lastOp + 10_000L >= System.currentTimeMillis()) {
            return false;
        }
        lastOp = System.currentTimeMillis();

        int y = WorldUtil.legacyGetHeightValue(level, x, z);
        DeathBlastEntity blast = new DeathBlastEntity(level);
        blast.setPos(x, y, z);
        level.addFreshEntity(blast);
        return true;
    }
}
