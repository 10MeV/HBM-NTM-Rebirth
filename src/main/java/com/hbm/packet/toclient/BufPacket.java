package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientTileBinaryDataPacket;
import com.hbm.packet.threading.PrecompiledPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy BufPacket facade. The old packet wrote x/y/z followed by receiver
 * supplied binary data; modern dispatch uses the registered tile binary packet.
 */
public class BufPacket extends PrecompiledPacket {
    public int x;
    public int y;
    public int z;
    public IBufPacketReceiver rec;
    private byte[] payload = new byte[0];

    public BufPacket() {
    }

    public BufPacket(int x, int y, int z, IBufPacketReceiver rec) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rec = rec;
    }

    public BufPacket(BlockPos pos, byte[] payload) {
        this(pos == null ? 0 : pos.getX(), pos == null ? 0 : pos.getY(), pos == null ? 0 : pos.getZ(), payload);
    }

    public BufPacket(int x, int y, int z, byte[] payload) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.payload = payload == null ? new byte[0] : payload.clone();
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        payload = new byte[buffer.readableBytes()];
        buffer.readBytes(payload);
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBytes(payloadBytes());
    }

    public byte[] payloadBytes() {
        if (rec == null) {
            return payload.clone();
        }
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        try {
            rec.serialize(data);
            byte[] bytes = new byte[data.readableBytes()];
            data.readBytes(bytes);
            return bytes;
        } finally {
            data.release();
        }
    }

    @Override
    public ClientTileBinaryDataPacket toModernPacket() {
        return ModMessages.bufPacket(new BlockPos(x, y, z), payloadBytes());
    }
}