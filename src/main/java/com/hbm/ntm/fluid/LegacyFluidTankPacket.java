package com.hbm.ntm.fluid;

import net.minecraft.network.FriendlyByteBuf;

public final class LegacyFluidTankPacket {
    private LegacyFluidTankPacket() {
    }

    public static void write(FriendlyByteBuf data, HbmFluidTank tank) {
        data.writeInt(tank.getFill());
        data.writeInt(tank.getMaxFill());
        data.writeInt(tank.getTankType().getId());
        data.writeShort((short) tank.getPressure());
    }

    public static void read(FriendlyByteBuf data, HbmFluidTank tank) {
        int fill = data.readInt();
        int maxFill = data.readInt();
        FluidType type = HbmFluids.fromId(data.readInt());
        int pressure = data.readShort();
        tank.changeTankSize(maxFill);
        tank.withPressure(pressure);
        tank.setTankType(type);
        tank.setFill(fill);
    }
}
