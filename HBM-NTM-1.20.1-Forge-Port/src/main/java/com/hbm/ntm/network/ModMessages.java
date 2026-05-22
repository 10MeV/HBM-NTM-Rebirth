package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.packet.AuxParticlePacket;
import com.hbm.ntm.network.packet.ClientBinaryDataPacket;
import com.hbm.ntm.network.packet.ClientBiomeSyncPacket;
import com.hbm.ntm.network.packet.ClientInformPacket;
import com.hbm.ntm.network.packet.ClientPanelDataPacket;
import com.hbm.ntm.network.packet.ClientTileEventPacket;
import com.hbm.ntm.network.packet.CompressedExplosionEffectPacket;
import com.hbm.ntm.network.packet.CoordinateActionPacket;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.ExplosionKnockbackPacket;
import com.hbm.ntm.network.packet.HeldItemNbtPacket;
import com.hbm.ntm.network.packet.ItemControlPacket;
import com.hbm.ntm.network.packet.ItemAnimationPacket;
import com.hbm.ntm.network.packet.KeybindPacket;
import com.hbm.ntm.network.packet.LegacyButtonPacket;
import com.hbm.ntm.network.packet.MachineBatteryButtonPacket;
import com.hbm.ntm.network.packet.MenuActionPacket;
import com.hbm.ntm.network.packet.MuzzleFlashPacket;
import com.hbm.ntm.network.packet.ParticleBurstPacket;
import com.hbm.ntm.network.packet.PermaSyncPacket;
import com.hbm.ntm.network.packet.PlayerPropertiesPacket;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.network.packet.TileSyncRequestPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.List;
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
        registerClientToServer(TileSyncRequestPacket.class,
                TileSyncRequestPacket::decode,
                TileSyncRequestPacket::encode,
                TileSyncRequestPacket::handle);
        registerServerToClient(EntitySyncPacket.class,
                EntitySyncPacket::decode,
                EntitySyncPacket::encode,
                EntitySyncPacket::handle);
        registerClientToServer(KeybindPacket.class,
                KeybindPacket::decode,
                KeybindPacket::encode,
                KeybindPacket::handle);
        registerServerToClient(ClientInformPacket.class,
                ClientInformPacket::decode,
                ClientInformPacket::encode,
                ClientInformPacket::handle);
        registerServerToClient(ItemAnimationPacket.class,
                ItemAnimationPacket::decode,
                ItemAnimationPacket::encode,
                ItemAnimationPacket::handle);
        registerServerToClient(MuzzleFlashPacket.class,
                MuzzleFlashPacket::decode,
                MuzzleFlashPacket::encode,
                MuzzleFlashPacket::handle);
        registerClientToServer(ItemControlPacket.class,
                ItemControlPacket::decode,
                ItemControlPacket::encode,
                ItemControlPacket::handle);
        registerServerToClient(HeldItemNbtPacket.class,
                HeldItemNbtPacket::decode,
                HeldItemNbtPacket::encode,
                HeldItemNbtPacket::handle);
        registerClientToServer(LegacyButtonPacket.class,
                LegacyButtonPacket::decode,
                LegacyButtonPacket::encode,
                LegacyButtonPacket::handle);
        registerServerToClient(PermaSyncPacket.class,
                PermaSyncPacket::decode,
                PermaSyncPacket::encode,
                PermaSyncPacket::handle);
        registerServerToClient(ExplosionKnockbackPacket.class,
                ExplosionKnockbackPacket::decode,
                ExplosionKnockbackPacket::encode,
                ExplosionKnockbackPacket::handle);
        registerClientToServer(CoordinateActionPacket.class,
                CoordinateActionPacket::decode,
                CoordinateActionPacket::encode,
                CoordinateActionPacket::handle);
        registerServerToClient(ClientBinaryDataPacket.class,
                ClientBinaryDataPacket::decode,
                ClientBinaryDataPacket::encode,
                ClientBinaryDataPacket::handle);
        registerServerToClient(ClientTileEventPacket.class,
                ClientTileEventPacket::decode,
                ClientTileEventPacket::encode,
                ClientTileEventPacket::handle);
        registerServerToClient(ClientPanelDataPacket.class,
                ClientPanelDataPacket::decode,
                ClientPanelDataPacket::encode,
                ClientPanelDataPacket::handle);
        registerServerToClient(PlayerPropertiesPacket.class,
                PlayerPropertiesPacket::decode,
                PlayerPropertiesPacket::encode,
                PlayerPropertiesPacket::handle);
        registerClientToServer(MenuActionPacket.class,
                MenuActionPacket::decode,
                MenuActionPacket::encode,
                MenuActionPacket::handle);
        registerServerToClient(ClientBiomeSyncPacket.class,
                ClientBiomeSyncPacket::decode,
                ClientBiomeSyncPacket::encode,
                ClientBiomeSyncPacket::handle);
        registerServerToClient(CompressedExplosionEffectPacket.class,
                CompressedExplosionEffectPacket::decode,
                CompressedExplosionEffectPacket::encode,
                CompressedExplosionEffectPacket::handle);
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

    public static void syncTileToPlayer(HbmTileSyncable syncable, BlockEntity blockEntity, ServerPlayer player) {
        sendToPlayer(new TileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag()), player);
    }

    public static void syncEntityToTracking(HbmEntitySyncable syncable, Entity entity) {
        sendToEntityTrackers(new EntitySyncPacket(entity.getId(), syncable.getClientSyncTag()), entity);
    }

    public static void syncEntityToPlayer(HbmEntitySyncable syncable, Entity entity, ServerPlayer player) {
        if (syncable.canSendClientSyncTo(player)) {
            sendToPlayer(new EntitySyncPacket(entity.getId(), syncable.getClientSyncTag()), player);
        }
    }

    public static void informPlayer(ServerPlayer player, Component message, int id, int millis) {
        sendToPlayer(new ClientInformPacket(message, id, millis), player);
    }

    public static void sendItemAnimation(ServerPlayer player, int slot, int rail, String itemKey,
                                         ResourceLocation animationFile, String animationName, boolean holdLastFrame) {
        sendToPlayer(new ItemAnimationPacket(slot, rail, itemKey, animationFile, animationName, holdLastFrame), player);
    }

    public static void sendMuzzleFlash(Entity entity) {
        sendToEntityTrackers(new MuzzleFlashPacket(entity.getId()), entity);
    }

    public static void sendItemControl(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        sendToServer(new ItemControlPacket(hand, tag));
    }

    public static void syncHeldItemNbt(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null) {
            sendToPlayer(new HeldItemNbtPacket(hand, itemId, stack.getDamageValue(), stack.getOrCreateTag().copy()), player);
        }
    }

    public static void sendLegacyButton(BlockPos pos, int value, int id) {
        sendToServer(new LegacyButtonPacket(pos, value, id));
    }

    public static void syncPermaData(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new PermaSyncPacket(data), player);
    }

    public static void sendExplosionKnockback(ServerPlayer player, Vec3 motion) {
        sendToPlayer(new ExplosionKnockbackPacket(motion), player);
    }

    public static void sendCoordinateAction(InteractionHand hand, BlockPos pos, int action, int value, int frequency,
                                            net.minecraft.nbt.CompoundTag data) {
        sendToServer(new CoordinateActionPacket(hand, pos, action, value, frequency, data));
    }

    public static void syncClientBinaryData(ServerPlayer player, ResourceLocation channel, String name, byte[] payload) {
        sendToPlayer(new ClientBinaryDataPacket(channel, name, payload, false), player);
    }

    public static void clearClientBinaryData(ServerPlayer player, ResourceLocation channel) {
        sendToPlayer(new ClientBinaryDataPacket(channel, "", new byte[0], true), player);
    }

    public static void sendClientTileEvent(BlockEntity blockEntity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToTrackingChunk(new ClientTileEventPacket(blockEntity.getBlockPos(), eventType, data), blockEntity);
    }

    public static void syncClientPanelData(ServerPlayer player, ResourceLocation panelType, int legacyType,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new ClientPanelDataPacket(panelType, legacyType, data), player);
    }

    public static void syncPlayerProperties(ServerPlayer player, ResourceLocation dataType, net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new PlayerPropertiesPacket(dataType, data), player);
    }

    public static void sendMenuAction(int action, int value, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new MenuActionPacket(action, value, data));
    }

    public static void syncClientBiome(ServerPlayer player, int blockX, int blockZ, short biome) {
        sendToPlayer(ClientBiomeSyncPacket.single(blockX, blockZ, biome), player);
    }

    public static void syncClientBiomeChunk(ServerPlayer player, int chunkX, int chunkZ, short[] biomeArray) {
        sendToPlayer(ClientBiomeSyncPacket.chunk(chunkX, chunkZ, biomeArray), player);
    }

    public static void sendCompressedExplosionEffect(ServerLevel level, Vec3 center, float size, List<BlockPos> affectedBlocks, double range) {
        sendToAllAround(new CompressedExplosionEffectPacket(center, size, affectedBlocks), level, center.x, center.y, center.z, range);
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
