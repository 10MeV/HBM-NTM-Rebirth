package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.AuxParticlePacket;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Legacy AuxParticlePacketNT facade. The old packet wrote one NBT payload that
 * already included posX/posY/posZ; modern sending unwraps to AuxParticlePacket.
 */
public class AuxParticlePacketNT extends ThreadedPacket {
    private CompoundTag data = new CompoundTag();

    public AuxParticlePacketNT() {
    }

    public AuxParticlePacketNT(CompoundTag data, double x, double y, double z) {
        CompoundTag copy = data == null ? new CompoundTag() : data.copy();
        copy.putDouble("posX", x);
        copy.putDouble("posY", y);
        copy.putDouble("posZ", z);
        this.data = copy;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        data = tag == null ? new CompoundTag() : tag;
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeNbt(data);
    }

    public CompoundTag data() {
        return data.copy();
    }

    @Override
    public AuxParticlePacket toModernPacket() {
        return ModMessages.auxParticlePacketNT(data.copy(),
                data.getDouble("posX"), data.getDouble("posY"), data.getDouble("posZ"));
    }
}
