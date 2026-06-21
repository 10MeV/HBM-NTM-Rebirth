package com.hbm.ntm.network.packet;

import com.hbm.ntm.client.sound.SoundLoopSiren;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmClientTileEventReceiver;
import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.network.HbmPreparablePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientTileEventPacket(BlockPos pos, ResourceLocation eventType, CompoundTag data) implements HbmPreparablePacket {
    public ClientTileEventPacket {
        pos = pos == null ? BlockPos.ZERO : pos;
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static ClientTileEventPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        ResourceLocation eventType = buffer.readResourceLocation();
        CompoundTag tag = buffer.readNbt();
        return new ClientTileEventPacket(pos, eventType, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(ClientTileEventPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeResourceLocation(packet.eventType);
        buffer.writeNbt(packet.data);
    }

    public static void handle(ClientTileEventPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(minecraft.level, packet.pos);
            if (HbmNetworkActions.SIREN.equals(packet.eventType)) {
                SoundLoopSiren.handleClientTileEvent(blockEntity, packet.data);
                return;
            }
            if (blockEntity instanceof HbmClientTileEventReceiver receiver) {
                receiver.handleClientTileEvent(packet.eventType, packet.data);
            }
        });
        context.setPacketHandled(true);
    }

    @Override
    public Object prepareForThreadedSend() {
        return new ClientTileEventPacket(pos, eventType, data);
    }
}
