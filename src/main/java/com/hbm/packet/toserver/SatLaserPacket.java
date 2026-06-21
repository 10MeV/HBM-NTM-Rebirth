package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.CoordinateActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Legacy satellite laser click facade.
 */
public class SatLaserPacket implements LegacyPacketAdapter {
    public int x;
    public int z;
    public int freq;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public SatLaserPacket() {
    }

    public SatLaserPacket(int x, int z, int freq) {
        this(InteractionHand.MAIN_HAND, x, z, freq);
    }

    public SatLaserPacket(InteractionHand hand, int x, int z, int freq) {
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.x = x;
        this.z = z;
        this.freq = freq;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        z = buffer.readInt();
        freq = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(z);
        buffer.writeInt(freq);
    }

    @Override
    public CoordinateActionPacket toModernPacket() {
        return ModMessages.satLaserPacket(hand, x, z, freq);
    }
}
