package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record KeybindPacket(HbmKeybind keybind, boolean pressed) {
    public static KeybindPacket decode(FriendlyByteBuf buffer) {
        int ordinal = buffer.readVarInt();
        boolean pressed = buffer.readBoolean();
        HbmKeybind[] values = HbmKeybind.values();
        HbmKeybind keybind = ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
        return new KeybindPacket(keybind, pressed);
    }

    public static void encode(KeybindPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.keybind == null ? -1 : packet.keybind.ordinal());
        buffer.writeBoolean(packet.pressed);
    }

    public static void handle(KeybindPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> handleServer(packet, context));
        context.setPacketHandled(true);
    }

    private static void handleServer(KeybindPacket packet, NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || packet.keybind == null) {
            if (player != null) {
                HbmNtm.LOGGER.warn("Player {} sent invalid HBM keybind packet.", player.getGameProfile().getName());
            }
            return;
        }
        HbmServerKeybinds.handle(player, packet.keybind, packet.pressed);
    }
}
