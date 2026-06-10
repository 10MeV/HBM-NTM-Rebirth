package com.hbm.ntm.network.packet;

import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ExplosionKnockbackPacket(Vec3 motion) implements HbmPreparablePacket {
    public ExplosionKnockbackPacket {
        motion = motion == null ? Vec3.ZERO : motion;
    }

    public static ExplosionKnockbackPacket decode(FriendlyByteBuf buffer) {
        return new ExplosionKnockbackPacket(new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat()));
    }

    public static void encode(ExplosionKnockbackPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat((float) packet.motion.x);
        buffer.writeFloat((float) packet.motion.y);
        buffer.writeFloat((float) packet.motion.z);
    }

    public static void handle(ExplosionKnockbackPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.setDeltaMovement(minecraft.player.getDeltaMovement().add(packet.motion));
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ExplosionKnockbackPacket(motion);
    }
}
