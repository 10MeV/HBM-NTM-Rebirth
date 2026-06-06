package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientItemAnimationHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ItemAnimationPacket(int slot, int rail, String itemKey, ResourceLocation animationFile, String animationName, boolean holdLastFrame) {
    public static ItemAnimationPacket decode(FriendlyByteBuf buffer) {
        return new ItemAnimationPacket(
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readResourceLocation(),
                buffer.readUtf(),
                buffer.readBoolean());
    }

    public static void encode(ItemAnimationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot);
        buffer.writeVarInt(packet.rail);
        buffer.writeUtf(packet.itemKey);
        buffer.writeResourceLocation(packet.animationFile);
        buffer.writeUtf(packet.animationName);
        buffer.writeBoolean(packet.holdLastFrame);
    }

    public static void handle(ItemAnimationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientItemAnimationHandler.handle(packet.slot, packet.rail, packet.itemKey,
                packet.animationFile, packet.animationName, packet.holdLastFrame));
        context.setPacketHandled(true);
    }
}
