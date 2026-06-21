package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientTileEventPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy blast-door/vault tile packet facade. The old primitive payload is
 * converted to the modern typed client tile event.
 */
public class TEVaultPacket extends ThreadedPacket {
    public int x;
    public int y;
    public int z;
    public boolean isOpening;
    public int state;
    public long sysTime;
    public int type;

    public TEVaultPacket() {
    }

    public TEVaultPacket(int x, int y, int z, boolean isOpening, int state, long sysTime, int type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.isOpening = isOpening;
        this.state = state;
        this.sysTime = sysTime;
        this.type = type;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        isOpening = buffer.readBoolean();
        state = buffer.readInt();
        sysTime = buffer.readLong();
        type = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBoolean(isOpening);
        buffer.writeInt(state);
        buffer.writeLong(sysTime);
        buffer.writeInt(type);
    }

    @Override
    public ClientTileEventPacket toModernPacket() {
        return ModMessages.teVaultPacket(x, y, z, isOpening, state, sysTime, type);
    }
}
