package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.CoordinateActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Legacy satellite coordinate action facade.
 */
public class SatCoordPacket implements LegacyPacketAdapter {
    public int x;
    public int y;
    public int z;
    public int freq;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public SatCoordPacket() {
    }

    public SatCoordPacket(int x, int y, int z, int freq) {
        this(InteractionHand.MAIN_HAND, x, y, z, freq);
    }

    public SatCoordPacket(InteractionHand hand, int x, int y, int z, int freq) {
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.x = x;
        this.y = y;
        this.z = z;
        this.freq = freq;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        freq = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(freq);
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public CoordinateActionPacket toModernPacket() {
        return ModMessages.satCoordPacket(hand, x, y, z, freq);
    }
}
