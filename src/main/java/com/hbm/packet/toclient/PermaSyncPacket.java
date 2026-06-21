package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.HbmPermaSyncData;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Legacy PermaSyncPacket facade. Modern permanent sync is NBT based; this
 * bridge keeps the old package/class entry point and delegates sends to the
 * registered modern PermaSyncPacket.
 */
public class PermaSyncPacket extends ThreadedPacket {
    public ServerPlayer player;
    private CompoundTag data = new CompoundTag();

    public PermaSyncPacket() {
    }

    public PermaSyncPacket(ServerPlayer player) {
        this.player = player;
        this.data = player == null ? new CompoundTag() : HbmPermaSyncData.writeForPlayer(player);
    }

    public PermaSyncPacket(CompoundTag data) {
        this.data = data == null ? new CompoundTag() : data.copy();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(player == null ? data : HbmPermaSyncData.writeForPlayer(player));
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        CompoundTag payload = buffer.readNbt();
        data = payload == null ? new CompoundTag() : payload;
    }

    public CompoundTag data() {
        return data.copy();
    }

    @Override
    public com.hbm.ntm.network.packet.PermaSyncPacket toModernPacket() {
        return ModMessages.permaSyncPacket(player == null ? data : HbmPermaSyncData.writeForPlayer(player));
    }
}