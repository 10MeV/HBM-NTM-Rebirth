package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.ClientExplosionEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record CompressedExplosionEffectPacket(Vec3 center, float size, List<BlockPos> affectedBlocks) {
    private static final int MAX_BLOCKS = 32_768;

    public CompressedExplosionEffectPacket {
        center = center == null ? Vec3.ZERO : center;
        affectedBlocks = affectedBlocks == null ? List.of() : List.copyOf(affectedBlocks);
    }

    public static CompressedExplosionEffectPacket decode(FriendlyByteBuf buffer) {
        Vec3 center = new Vec3(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        float size = buffer.readFloat();
        int count = Math.min(buffer.readVarInt(), MAX_BLOCKS);
        int baseX = (int) center.x;
        int baseY = (int) center.y;
        int baseZ = (int) center.z;
        List<BlockPos> affectedBlocks = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            affectedBlocks.add(new BlockPos(baseX + buffer.readByte(), baseY + buffer.readByte(), baseZ + buffer.readByte()));
        }
        return new CompressedExplosionEffectPacket(center, size, affectedBlocks);
    }

    public static void encode(CompressedExplosionEffectPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat((float) packet.center.x);
        buffer.writeFloat((float) packet.center.y);
        buffer.writeFloat((float) packet.center.z);
        buffer.writeFloat(packet.size);

        int baseX = (int) packet.center.x;
        int baseY = (int) packet.center.y;
        int baseZ = (int) packet.center.z;
        List<BlockPos> encodable = packet.affectedBlocks.stream()
                .filter(pos -> canEncodeRelative(pos.getX() - baseX) && canEncodeRelative(pos.getY() - baseY) && canEncodeRelative(pos.getZ() - baseZ))
                .limit(MAX_BLOCKS)
                .toList();
        buffer.writeVarInt(encodable.size());
        for (BlockPos pos : encodable) {
            buffer.writeByte(pos.getX() - baseX);
            buffer.writeByte(pos.getY() - baseY);
            buffer.writeByte(pos.getZ() - baseZ);
        }
    }

    public static void handle(CompressedExplosionEffectPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientExplosionEffects.standard(packet.center, packet.size, packet.affectedBlocks));
        context.setPacketHandled(true);
    }

    private static boolean canEncodeRelative(int value) {
        return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
    }
}
