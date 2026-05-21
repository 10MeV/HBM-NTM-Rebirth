package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.packet.AuxParticlePacket;
import com.hbm.ntm.network.packet.MachineBatteryButtonPacket;
import com.hbm.ntm.network.packet.ParticleBurstPacket;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId;
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(HbmNtm.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }

        /*
         * Keep this list append-only. Legacy 1.7.10 used numeric discriminators,
         * and modern Forge still requires client/server registration order parity.
         */
        registerServerToClient(PlayerRadiationSyncPacket.class,
                PlayerRadiationSyncPacket::decode,
                PlayerRadiationSyncPacket::encode,
                PlayerRadiationSyncPacket::handle);
        registerServerToClient(AuxParticlePacket.class,
                AuxParticlePacket::decode,
                AuxParticlePacket::encode,
                AuxParticlePacket::handle);
        registerServerToClient(ParticleBurstPacket.class,
                ParticleBurstPacket::decode,
                ParticleBurstPacket::encode,
                ParticleBurstPacket::handle);
        registerClientToServer(MachineBatteryButtonPacket.class,
                MachineBatteryButtonPacket::decode,
                MachineBatteryButtonPacket::encode,
                MachineBatteryButtonPacket::handle);
        registerServerToClient(TileSyncPacket.class,
                TileSyncPacket::decode,
                TileSyncPacket::encode,
                TileSyncPacket::handle);
        registerClientToServer(TileControlPacket.class,
                TileControlPacket::decode,
                TileControlPacket::encode,
                TileControlPacket::handle);
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToEntityTrackers(Object message, Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public static void sendToEntityAndSelf(Object message, Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static void sendToDimension(Object message, ServerLevel level) {
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public static void sendToTracking(Object message, ServerLevel level, double x, double y, double z, double range) {
        sendToAllAround(message, level, x, y, z, range);
    }

    public static void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, level.dimension()));
    }

    public static void sendToAllAround(Object message, Entity entity, double range) {
        sendToAllAround(message, new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), range, entity.level().dimension()));
    }

    public static void sendToAllAround(Object message, PacketDistributor.TargetPoint point) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> point), message);
    }

    public static void sendToAll(Object message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendToTrackingChunk(Object message, BlockEntity blockEntity) {
        sendToTrackingChunk(message, blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static void sendToTrackingChunk(Object message, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)
                    .forEach(player -> sendToPlayer(message, player));
        }
    }

    public static void syncTileToTracking(HbmTileSyncable syncable, BlockEntity blockEntity) {
        sendToTrackingChunk(new TileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag()), blockEntity);
    }

    private static <MSG> void registerServerToClient(
            Class<MSG> type,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(packetId++, type, encoder, decoder, handler, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static <MSG> void registerClientToServer(
            Class<MSG> type,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        CHANNEL.registerMessage(packetId++, type, encoder, decoder, handler, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private ModMessages() {
    }
}
