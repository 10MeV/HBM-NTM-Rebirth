package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientPanelDataPacket;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy SatPanelPacket facade. The old packet carried a satellite legacy id
 * and an NBT payload; modern client state is owned by ClientPanelData.
 */
public class SatPanelPacket extends ThreadedPacket {
    public int type;
    public CompoundTag data;

    public SatPanelPacket() {
    }

    public SatPanelPacket(Satellite satellite) {
        if (satellite != null) {
            type = satellite.getID();
            data = satellite.saveData();
        }
    }

    public SatPanelPacket(int type, CompoundTag data) {
        this.type = type;
        this.data = data == null ? null : data.copy();
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        type = buffer.readInt();
        data = buffer.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(type);
        buffer.writeNbt(data);
    }

    @Override
    public ClientPanelDataPacket toModernPacket() {
        return ModMessages.satPanelPacket(type, data);
    }
}
