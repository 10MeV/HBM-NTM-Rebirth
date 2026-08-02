package com.hbm.ntm.client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmClientTileBinaryReceiver;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ClientTileBinarySyncRequestPacket;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientTileBinaryData {
    private static final long TRANSFER_TIMEOUT_TICKS = 20L * 30L;
    private static final long REQUEST_COOLDOWN_TICKS = 20L;
    /** Bound unfinished transfer identities so arbitrary S2C UUIDs cannot grow the map forever. */
    public static final int MAX_PENDING_TRANSFERS = 8;
    /** Bound all retained chunk arrays, independently of the number of transfers. */
    public static final long MAX_PENDING_PAYLOAD_BYTES = 256L * 1024L * 1024L;
    /** The final joined byte array must fit the same protocol maximum as the sender. */
    public static final int MAX_REASSEMBLED_PAYLOAD_BYTES =
            com.hbm.ntm.network.packet.ClientTileBinaryDataChunkPacket.MAX_REASSEMBLED_PAYLOAD_BYTES;
    private static final Map<UUID, ChunkAssembly> CHUNKS = new HashMap<>();
    private static final Map<RequestKey, Long> LAST_SYNC_REQUESTS = new HashMap<>();
    private static long pendingPayloadBytes;

    public static boolean putChunk(UUID transferId, BlockPos pos, ResourceLocation channel, int chunkIndex, int chunkCount, byte[] chunk, long gameTime) {
        if (transferId == null || pos == null || channel == null || chunk == null
                || chunkCount <= 0 || chunkCount > com.hbm.ntm.network.packet.ClientTileBinaryDataChunkPacket.MAX_CHUNKS
                || chunkIndex < 0 || chunkIndex >= chunkCount
                || chunk.length > com.hbm.ntm.network.packet.ClientTileBinaryDataChunkPacket.MAX_CHUNK_BYTES) {
            return false;
        }
        ChunkAssembly assembly = CHUNKS.get(transferId);
        if (assembly == null) {
            if (CHUNKS.size() >= MAX_PENDING_TRANSFERS) {
                return false;
            }
            assembly = new ChunkAssembly(pos, channel, chunkCount, gameTime);
            CHUNKS.put(transferId, assembly);
        }
        if (!assembly.accepts(pos, channel, chunkCount)) {
            removeAssembly(transferId, assembly);
            return false;
        }
        long byteDelta = assembly.byteDeltaFor(chunkIndex, chunk.length);
        if (assembly.bufferedBytes + byteDelta > MAX_REASSEMBLED_PAYLOAD_BYTES
                || pendingPayloadBytes + byteDelta > MAX_PENDING_PAYLOAD_BYTES) {
            return false;
        }
        assembly.put(chunkIndex, chunk, gameTime);
        pendingPayloadBytes += byteDelta;
        if (!assembly.isComplete()) {
            return false;
        }
        byte[] payload = assembly.join();
        removeAssembly(transferId, assembly);
        if (payload == null) {
            return false;
        }
        dispatch(pos, channel, payload);
        return true;
    }

    public static void dispatch(BlockPos pos, ResourceLocation channel, byte[] payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return;
        }
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        BlockPos receiverPos = core == null ? pos : core.pos();
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(level, pos);
        if (!(blockEntity instanceof HbmClientTileBinaryReceiver receiver)) {
            HbmNtm.LOGGER.debug("Tile binary data at {} resolved to {} but had no HbmClientTileBinaryReceiver receiver.",
                    pos, receiverPos);
            requestResync(level, receiverPos, channel);
            return;
        }
        FriendlyByteBuf payloadBuffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(payload));
        try {
            receiver.handleClientTileBinaryData(channel, payloadBuffer);
        } catch (Exception exception) {
            HbmNtm.LOGGER.warn("Tile binary data receiver failed at {} resolved to {} for channel {}.",
                    pos, receiverPos, channel, exception);
        } finally {
            payloadBuffer.release();
        }
    }

    public static int pendingTransfers() {
        return CHUNKS.size();
    }

    public static int pendingChunkCount() {
        int count = 0;
        for (ChunkAssembly assembly : CHUNKS.values()) {
            count += assembly.received;
        }
        return count;
    }

    public static long pendingPayloadBytes() {
        return pendingPayloadBytes;
    }

    public static int pendingClientResyncRequests() {
        return LAST_SYNC_REQUESTS.size();
    }

    public static long clientResyncRequestCooldownTicks() {
        return REQUEST_COOLDOWN_TICKS;
    }

    public static void clearAll() {
        CHUNKS.clear();
        pendingPayloadBytes = 0L;
        clearClientResyncRequests();
    }

    public static void clearClientResyncRequests() {
        LAST_SYNC_REQUESTS.clear();
    }

    public static int pruneExpired(long gameTime) {
        int before = CHUNKS.size();
        var iterator = CHUNKS.entrySet().iterator();
        while (iterator.hasNext()) {
            ChunkAssembly assembly = iterator.next().getValue();
            if (gameTime - assembly.lastTouchedGameTime > TRANSFER_TIMEOUT_TICKS) {
                pendingPayloadBytes -= assembly.bufferedBytes;
                iterator.remove();
            }
        }
        LAST_SYNC_REQUESTS.entrySet().removeIf(entry -> gameTime - entry.getValue() > TRANSFER_TIMEOUT_TICKS);
        return before - CHUNKS.size();
    }

    public static long transferTimeoutTicks() {
        return TRANSFER_TIMEOUT_TICKS;
    }

    private ClientTileBinaryData() {
    }

    private static void requestResync(ClientLevel level, BlockPos pos, ResourceLocation channel) {
        long gameTime = level.getGameTime();
        RequestKey key = new RequestKey(pos.immutable(), channel);
        Long lastRequest = LAST_SYNC_REQUESTS.get(key);
        if (lastRequest != null && gameTime - lastRequest < REQUEST_COOLDOWN_TICKS) {
            return;
        }
        LAST_SYNC_REQUESTS.put(key, gameTime);
        ModMessages.sendToServer(new ClientTileBinarySyncRequestPacket(pos, channel));
    }

    private static void removeAssembly(UUID transferId, ChunkAssembly expected) {
        if (CHUNKS.remove(transferId, expected)) {
            pendingPayloadBytes -= expected.bufferedBytes;
        }
    }

    private record RequestKey(BlockPos pos, ResourceLocation channel) {
    }

    private static final class ChunkAssembly {
        private final BlockPos pos;
        private final ResourceLocation channel;
        private final byte[][] chunks;
        private int received;
        private long bufferedBytes;
        private long lastTouchedGameTime;

        private ChunkAssembly(BlockPos pos, ResourceLocation channel, int chunkCount, long gameTime) {
            this.pos = pos.immutable();
            this.channel = channel;
            this.chunks = new byte[chunkCount][];
            this.lastTouchedGameTime = gameTime;
        }

        private boolean accepts(BlockPos pos, ResourceLocation channel, int chunkCount) {
            return this.pos.equals(pos) && this.channel.equals(channel) && chunks.length == chunkCount;
        }

        private long byteDeltaFor(int chunkIndex, int newLength) {
            byte[] previous = chunks[chunkIndex];
            return newLength - (previous == null ? 0L : previous.length);
        }

        private void put(int chunkIndex, byte[] chunk, long gameTime) {
            lastTouchedGameTime = gameTime;
            if (chunks[chunkIndex] == null) {
                received++;
            }
            bufferedBytes += byteDeltaFor(chunkIndex, chunk.length);
            chunks[chunkIndex] = Arrays.copyOf(chunk, chunk.length);
        }

        private boolean isComplete() {
            return received == chunks.length;
        }

        private byte[] join() {
            if (bufferedBytes < 0L || bufferedBytes > MAX_REASSEMBLED_PAYLOAD_BYTES) {
                return null;
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream((int) bufferedBytes);
            for (byte[] chunk : chunks) {
                output.writeBytes(chunk);
            }
            return output.toByteArray();
        }
    }
}
