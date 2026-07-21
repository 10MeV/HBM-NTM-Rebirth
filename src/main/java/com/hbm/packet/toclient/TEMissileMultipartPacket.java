package com.hbm.packet.toclient;

import com.hbm.handler.MissileStruct;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientMissileMultipartPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy packet facade retaining the old coordinate and four-part carrier
 * layout while delegating real delivery to the registered modern S2C packet.
 */
public class TEMissileMultipartPacket extends ThreadedPacket {
    public int x;
    public int y;
    public int z;
    public MissileStruct missile;

    public TEMissileMultipartPacket() {
        this(0, 0, 0, new MissileStruct());
    }

    public TEMissileMultipartPacket(int x, int y, int z, MissileStruct missile) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.missile = missile == null ? new MissileStruct() : missile;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        missile = MissileStruct.readFromByteBuffer(buffer);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        missile.writeToByteBuffer(buffer);
    }

    @Override
    public ClientMissileMultipartPacket toModernPacket() {
        return ModMessages.teMissileMultipartPacket(x, y, z, missile.toSnapshot());
    }
}
