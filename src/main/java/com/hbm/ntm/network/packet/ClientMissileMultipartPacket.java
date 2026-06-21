package com.hbm.ntm.network.packet;

import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmClientMissileMultipartReceiver;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.network.MissileMultipartSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientMissileMultipartPacket(BlockPos pos, MissileMultipartSnapshot multipart) implements HbmPreparablePacket {
    public ClientMissileMultipartPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
        multipart = multipart == null ? MissileMultipartSnapshot.EMPTY : multipart;
    }

    public static ClientMissileMultipartPacket decode(FriendlyByteBuf buffer) {
        return new ClientMissileMultipartPacket(buffer.readBlockPos(), MissileMultipartSnapshot.decode(buffer));
    }

    public static void encode(ClientMissileMultipartPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        packet.multipart.encode(buffer);
    }

    public static void handle(ClientMissileMultipartPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(minecraft.level, packet.pos);
            if (blockEntity instanceof HbmClientMissileMultipartReceiver receiver) {
                receiver.handleClientMissileMultipart(packet.multipart);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ClientMissileMultipartPacket(pos, multipart);
    }
}
