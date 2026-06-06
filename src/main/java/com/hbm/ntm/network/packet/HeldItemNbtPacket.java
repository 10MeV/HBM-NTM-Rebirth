package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public record HeldItemNbtPacket(InteractionHand hand, ResourceLocation itemId, int damageValue, CompoundTag tag) implements HbmPreparablePacket {
    public HeldItemNbtPacket {
        hand = hand == null ? InteractionHand.MAIN_HAND : hand;
        tag = tag == null ? new CompoundTag() : tag.copy();
    }

    public static HeldItemNbtPacket decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readBoolean() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ResourceLocation itemId = buffer.readResourceLocation();
        int damageValue = buffer.readVarInt();
        CompoundTag tag = buffer.readNbt();
        return new HeldItemNbtPacket(hand, itemId, damageValue, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(HeldItemNbtPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.hand == InteractionHand.OFF_HAND);
        buffer.writeResourceLocation(packet.itemId);
        buffer.writeVarInt(packet.damageValue);
        buffer.writeNbt(packet.tag);
    }

    public static void handle(HeldItemNbtPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleClient(packet));
        context.setPacketHandled(true);
    }

    private static void handleClient(HeldItemNbtPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ItemStack held = minecraft.player.getItemInHand(packet.hand);
        if (held.isEmpty() || ForgeRegistries.ITEMS.getKey(held.getItem()) == null) {
            return;
        }
        if (!packet.itemId.equals(ForgeRegistries.ITEMS.getKey(held.getItem())) || held.getDamageValue() != packet.damageValue) {
            return;
        }
        held.setTag(packet.tag.copy());
    }

    @Override
    public Object prepareForThreadedSend() {
        return new HeldItemNbtPacket(hand, itemId, damageValue, tag);
    }
}
