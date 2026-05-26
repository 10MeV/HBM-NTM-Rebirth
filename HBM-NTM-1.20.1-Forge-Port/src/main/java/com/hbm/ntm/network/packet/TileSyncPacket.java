package com.hbm.ntm.network.packet;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.HbmPreparablePacket;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record TileSyncPacket(BlockPos pos, CompoundTag data) implements HbmPreparablePacket {
    private static final long REQUEST_COOLDOWN_TICKS = 20L;
    private static final Map<BlockPos, Long> LAST_SYNC_REQUESTS = new HashMap<>();

    public TileSyncPacket {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static TileSyncPacket decode(FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        CompoundTag tag = buffer.readNbt();
        return new TileSyncPacket(pos, tag == null ? new CompoundTag() : tag);
    }

    public static void encode(TileSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeNbt(packet.data);
    }

    public static void handle(TileSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet)));
        context.setPacketHandled(true);
    }

    private static void handleClient(TileSyncPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.hasChunk(packet.pos.getX() >> 4, packet.pos.getZ() >> 4)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(packet.pos);
        if (blockEntity instanceof HbmTileSyncable syncable) {
            syncable.handleClientSyncTag(packet.data);
        } else {
            HbmNtm.LOGGER.debug("Tile sync packet at {} had no HbmTileSyncable receiver.", packet.pos);
            requestResync(level, packet.pos);
        }
    }

    private static void requestResync(ClientLevel level, BlockPos pos) {
        long gameTime = level.getGameTime();
        Long lastRequest = LAST_SYNC_REQUESTS.get(pos);
        if (lastRequest != null && gameTime - lastRequest < REQUEST_COOLDOWN_TICKS) {
            return;
        }
        LAST_SYNC_REQUESTS.put(pos.immutable(), gameTime);
        ModMessages.sendToServer(new TileSyncRequestPacket(pos));
    }

    public static void clearClientResyncRequests() {
        LAST_SYNC_REQUESTS.clear();
    }

    public static int pendingClientResyncRequests() {
        return LAST_SYNC_REQUESTS.size();
    }

    public static long clientResyncRequestCooldownTicks() {
        return REQUEST_COOLDOWN_TICKS;
    }

    @Override
    public Object prepareForThreadedSend() {
        return new TileSyncPacket(pos, data);
    }
}
