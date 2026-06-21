package com.hbm.packet.toserver;

import com.hbm.ntm.network.LegacyPacketAdapter;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.TileControlPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy C2S tile NBT control facade. Business validation and multiblock core
 * resolution remain in the modern TileControlPacket receiver.
 */
public class NBTControlPacket implements LegacyPacketAdapter {
    public CompoundTag data = new CompoundTag();
    public int x;
    public int y;
    public int z;

    public NBTControlPacket() {
    }

    public NBTControlPacket(CompoundTag data, int x, int y, int z) {
        this.data = data == null ? new CompoundTag() : data.copy();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public NBTControlPacket(CompoundTag data, BlockPos pos) {
        this(data, pos == null ? 0 : pos.getX(), pos == null ? 0 : pos.getY(), pos == null ? 0 : pos.getZ());
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        CompoundTag tag = buffer.readNbt();
        data = tag == null ? new CompoundTag() : tag;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeNbt(data);
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public TileControlPacket toModernPacket() {
        return ModMessages.nbtControlPacket(x, y, z, data);
    }
}
