package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ItemActionPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Legacy Bobmazon offer request facade. Offer validation, payment and delivery
 * stay with the modern item/action receiver.
 */
public class ItemBobmazonPacket implements LegacyPacketAdapter {
    public int offer;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public ItemBobmazonPacket() {
    }

    public ItemBobmazonPacket(int offer) {
        this(InteractionHand.MAIN_HAND, offer);
    }

    public ItemBobmazonPacket(InteractionHand hand, int offer) {
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.offer = offer;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        offer = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(offer);
    }

    @Override
    public ItemActionPacket toModernPacket() {
        return ModMessages.itemBobmazonPacket(hand, offer);
    }
}
