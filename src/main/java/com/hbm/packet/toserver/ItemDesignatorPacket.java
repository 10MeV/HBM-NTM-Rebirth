package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ItemActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Legacy manual designator coordinate adjustment packet facade.
 */
public class ItemDesignatorPacket implements LegacyPacketAdapter {
    public int operator;
    public int value;
    public int reference;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public ItemDesignatorPacket() {
    }

    public ItemDesignatorPacket(int operator, int value, int reference) {
        this(InteractionHand.MAIN_HAND, operator, value, reference);
    }

    public ItemDesignatorPacket(InteractionHand hand, int operator, int value, int reference) {
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.operator = operator;
        this.value = value;
        this.reference = reference;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        operator = buffer.readInt();
        value = buffer.readInt();
        reference = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(operator);
        buffer.writeInt(value);
        buffer.writeInt(reference);
    }

    @Override
    public ItemActionPacket toModernPacket() {
        return ModMessages.itemDesignatorPacket(hand, operator, value, reference);
    }
}
