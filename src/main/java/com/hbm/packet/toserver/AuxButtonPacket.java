package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.LegacyButtonPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy C2S AuxButtonPacket facade. The old packet carried raw tile
 * coordinates plus two button integers; modern handling is owned by the target
 * BlockEntity through HbmLegacyButtonReceiver.
 */
public class AuxButtonPacket implements LegacyPacketAdapter {
    public int x;
    public int y;
    public int z;
    public int value;
    public int id;

    public AuxButtonPacket() {
    }

    public AuxButtonPacket(int x, int y, int z, int value, int id) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = value;
        this.id = id;
    }

    public AuxButtonPacket(BlockPos pos, int value, int id) {
        this(pos == null ? 0 : pos.getX(), pos == null ? 0 : pos.getY(), pos == null ? 0 : pos.getZ(), value, id);
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        value = buffer.readInt();
        id = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(value);
        buffer.writeInt(id);
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public LegacyButtonPacket toModernPacket() {
        return ModMessages.auxButtonPacket(x, y, z, value, id);
    }
}
