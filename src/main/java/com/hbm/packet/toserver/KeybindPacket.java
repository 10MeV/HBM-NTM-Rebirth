package com.hbm.packet.toserver;

import com.hbm.handler.HbmKeybinds;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy C2S keybind packet facade. Actual sends are adapted to the registered
 * modern KeybindPacket.
 */
public class KeybindPacket implements LegacyPacketAdapter {
    private int key;
    private boolean pressed;

    public KeybindPacket() {
        this(null, false);
    }

    public KeybindPacket(EnumKeybind key, boolean pressed) {
        this.key = key == null ? -1 : key.ordinal();
        this.pressed = pressed;
    }

    public KeybindPacket(int legacyOrdinal, boolean pressed) {
        this.key = legacyOrdinal;
        this.pressed = pressed;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        key = buffer.readInt();
        pressed = buffer.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(key);
        buffer.writeBoolean(pressed);
    }

    public int legacyOrdinal() {
        return key;
    }

    public boolean pressed() {
        return pressed;
    }

    public EnumKeybind keybind() {
        EnumKeybind[] values = EnumKeybind.values();
        return key >= 0 && key < values.length ? values[key] : null;
    }

    @Override
    public Object toModernPacket() {
        return ModMessages.keybindPacket(HbmKeybinds.toModern(keybind()), pressed);
    }
}
