package com.hbm.packet.toclient;

import com.hbm.ntm.network.ModMessages;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy ExplosionKnockbackPacket facade. The old packet wrote three float
 * motion components and added them to the local player client-side.
 */
public class ExplosionKnockbackPacket extends ThreadedPacket {
    public float motionX;
    public float motionY;
    public float motionZ;

    public ExplosionKnockbackPacket() {
    }

    public ExplosionKnockbackPacket(Vec3 vec) {
        this((float) (vec == null ? 0.0D : vec.x),
                (float) (vec == null ? 0.0D : vec.y),
                (float) (vec == null ? 0.0D : vec.z));
    }

    public ExplosionKnockbackPacket(float motionX, float motionY, float motionZ) {
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buffer) {
        motionX = buffer.readFloat();
        motionY = buffer.readFloat();
        motionZ = buffer.readFloat();
    }

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeFloat(motionX);
        buffer.writeFloat(motionY);
        buffer.writeFloat(motionZ);
    }

    @Override
    public com.hbm.ntm.network.packet.ExplosionKnockbackPacket toModernPacket() {
        return ModMessages.explosionKnockbackPacket(motionX, motionY, motionZ);
    }
}