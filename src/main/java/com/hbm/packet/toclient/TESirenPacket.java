package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientTileEventPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy siren tile packet facade. Modern client sound lifecycle is handled by
 * ClientTileEventPacket and the siren sound event receiver.
 */
public class TESirenPacket extends ThreadedPacket {
    public int x;
    public int y;
    public int z;
    public int id;
    public boolean active;

    public TESirenPacket() {
    }

    public TESirenPacket(int x, int y, int z, int id, boolean active) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.id = id;
        this.active = active;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        id = buffer.readInt();
        active = buffer.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(id);
        buffer.writeBoolean(active);
    }

    @Override
    public ClientTileEventPacket toModernPacket() {
        return ModMessages.teSirenPacket(x, y, z, id, active);
    }
}
