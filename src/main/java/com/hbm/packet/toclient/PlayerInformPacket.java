package com.hbm.packet.toclient;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

/**
 * Legacy PlayerInformPacket facade for old tooltip/notice sends.
 */
public class PlayerInformPacket implements LegacyPacketAdapter {
    private boolean fancy;
    private String message = "";
    private int id;
    private Component component;
    private int millis;

    public PlayerInformPacket() {
    }

    public PlayerInformPacket(String message, int id) {
        this(message, id, 0);
    }

    public PlayerInformPacket(Component component, int id) {
        this(component, id, 0);
    }

    public PlayerInformPacket(String message, int id, int millis) {
        this.fancy = false;
        this.message = message == null ? "" : message;
        this.id = id;
        this.millis = millis;
    }

    public PlayerInformPacket(Component component, int id, int millis) {
        this.fancy = true;
        this.component = component == null ? Component.empty() : component;
        this.id = id;
        this.millis = millis;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        id = buffer.readInt();
        millis = buffer.readInt();
        fancy = buffer.readBoolean();
        String encoded = buffer.readUtf();
        if (fancy) {
            component = Component.Serializer.fromJson(encoded);
            if (component == null) {
                component = Component.literal(encoded);
            }
        } else {
            message = encoded;
            component = null;
        }
    }

    public void fromBytes(ByteBuf buffer) {
        fromBytes(new FriendlyByteBuf(buffer));
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(id);
        buffer.writeInt(millis);
        buffer.writeBoolean(fancy);
        buffer.writeUtf(fancy ? Component.Serializer.toJson(component == null ? Component.empty() : component) : message);
    }

    public void toBytes(ByteBuf buffer) {
        toBytes(new FriendlyByteBuf(buffer));
    }

    public Component message() {
        return fancy ? (component == null ? Component.empty() : component) : Component.literal(message);
    }

    public int id() {
        return id;
    }

    public int millis() {
        return millis;
    }

    @Override
    public Object toModernPacket() {
        return ModMessages.playerInformPacket(message(), id, millis);
    }
}
