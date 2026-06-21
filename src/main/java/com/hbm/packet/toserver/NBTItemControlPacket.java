package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ItemControlPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

/**
 * Legacy C2S held-item NBT control facade. The old packet implicitly targeted
 * the held main-hand item; an explicit hand overload is provided for migrated
 * modern call sites.
 */
public class NBTItemControlPacket implements LegacyPacketAdapter {
    public CompoundTag data = new CompoundTag();
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    public NBTItemControlPacket() {
    }

    public NBTItemControlPacket(CompoundTag data) {
        this(InteractionHand.MAIN_HAND, data);
    }

    public NBTItemControlPacket(InteractionHand hand, CompoundTag data) {
        this.hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        this.data = data == null ? new CompoundTag() : data.copy();
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        data = tag == null ? new CompoundTag() : tag;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(data);
    }

    public InteractionHand hand() {
        return hand;
    }

    @Override
    public ItemControlPacket toModernPacket() {
        return ModMessages.nbtItemControlPacket(hand, data);
    }
}
