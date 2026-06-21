package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.LegacyItemAnimationPacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy HbmAnimationPacket facade. The old packet carried three primitive
 * animation selectors; modern handling delegates them to the registered legacy
 * item animation packet and the current held-item receiver.
 */
public class HbmAnimationPacket extends ThreadedPacket {
    public short type;
    public int receiverIndex;
    public int itemIndex;

    public HbmAnimationPacket() {
    }

    public HbmAnimationPacket(int type) {
        this(type, 0, 0);
    }

    public HbmAnimationPacket(int type, int receiverIndex) {
        this(type, receiverIndex, 0);
    }

    public HbmAnimationPacket(int type, int receiverIndex, int itemIndex) {
        this.type = (short) type;
        this.receiverIndex = receiverIndex;
        this.itemIndex = itemIndex;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        type = buffer.readShort();
        receiverIndex = buffer.readInt();
        itemIndex = buffer.readInt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeShort(type);
        buffer.writeInt(receiverIndex);
        buffer.writeInt(itemIndex);
    }

    @Override
    public LegacyItemAnimationPacket toModernPacket() {
        return ModMessages.hbmAnimationPacket(type, receiverIndex, itemIndex);
    }
}
