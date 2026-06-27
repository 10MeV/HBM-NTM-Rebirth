package com.hbm.ntm.network;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.network.packet.AuxParticlePacket;
import com.hbm.ntm.network.packet.ClientBinaryDataPacket;
import com.hbm.ntm.network.packet.ClientBinaryDataChunkPacket;
import com.hbm.ntm.network.packet.ClientBinaryDataReadyPacket;
import com.hbm.ntm.network.packet.ClientBiomeSyncPacket;
import com.hbm.ntm.network.packet.ClientEntityEventPacket;
import com.hbm.ntm.network.packet.ClientInformPacket;
import com.hbm.ntm.network.packet.ClientMissileMultipartPacket;
import com.hbm.ntm.network.packet.ClientPanelDataPacket;
import com.hbm.ntm.network.packet.ClientTileBinaryDataPacket;
import com.hbm.ntm.network.packet.ClientTileBinaryDataChunkPacket;
import com.hbm.ntm.network.packet.ClientTileBinarySyncRequestPacket;
import com.hbm.ntm.network.packet.ClientTileEventPacket;
import com.hbm.ntm.network.packet.CompressedExplosionEffectPacket;
import com.hbm.ntm.network.packet.CoordinateActionPacket;
import com.hbm.ntm.network.packet.EntitySyncPacket;
import com.hbm.ntm.network.packet.EntitySyncRequestPacket;
import com.hbm.ntm.network.packet.ExplosionKnockbackPacket;
import com.hbm.ntm.network.packet.ExtPropertiesSyncPacket;
import com.hbm.ntm.network.packet.HeldItemNbtPacket;
import com.hbm.ntm.network.packet.ItemActionPacket;
import com.hbm.ntm.network.packet.ItemControlPacket;
import com.hbm.ntm.network.packet.ItemAnimationPacket;
import com.hbm.ntm.network.packet.KeybindPacket;
import com.hbm.ntm.network.packet.LegacyItemAnimationPacket;
import com.hbm.ntm.network.packet.LegacyButtonPacket;
import com.hbm.ntm.network.packet.MachineBatteryButtonPacket;
import com.hbm.ntm.network.packet.MenuActionPacket;
import com.hbm.ntm.network.packet.MuzzleFlashPacket;
import com.hbm.ntm.network.packet.ParticleBurstPacket;
import com.hbm.ntm.network.packet.PermaSyncPacket;
import com.hbm.ntm.network.packet.PlayerPropertiesPacket;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.network.packet.ServerTileBinaryControlPacket;
import com.hbm.ntm.network.packet.ServerTileBinaryControlChunkPacket;
import com.hbm.ntm.network.packet.PWRPrinterSnapshotPacket;
import com.hbm.ntm.network.packet.ServerEntityActionPacket;
import com.hbm.ntm.network.packet.ServerTileActionPacket;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.network.packet.TileSyncRequestPacket;
import com.hbm.ntm.network.packet.TypedMenuActionPacket;
import com.hbm.ntm.player.HbmExtendedProperties;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import io.netty.buffer.Unpooled;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.CRC32;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static final String LEGACY_CHANNEL_NAME = "hbm";
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(HbmNtm.MOD_ID, "main");
    public static final ResourceLocation LEGACY_SERIALIZABLE_RECIPE_CHANNEL =
            new ResourceLocation(HbmNtm.MOD_ID, "legacy_serializable_recipes");
    private static final int EXPECTED_LEGACY_PACKET_COUNT = 27;
    private static final int EXPECTED_FIRST_LEGACY_PACKET_ID = 0;
    private static final String EXPECTED_FIRST_LEGACY_PACKET_NAME = "TESirenPacket";
    private static final int EXPECTED_LAST_LEGACY_PACKET_ID = 26;
    private static final String EXPECTED_LAST_LEGACY_PACKET_NAME = "MuzzleFlashPacket";
    private static final int LEGACY_PLAYER_INFORM_DEFAULT_MILLIS = 1_000;
    private static final int LIBRARY_FOUNDATION_PROGRESS_PERCENT = 99;
    private static int packetId;
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final List<PacketRegistration> PACKET_REGISTRATIONS = new ArrayList<>();
    private static final Map<Class<?>, PacketRegistration> PACKET_REGISTRATIONS_BY_TYPE = new HashMap<>();
    private static final AtomicLong BLOCKED_UNREGISTERED_SENDS = new AtomicLong();
    private static final AtomicLong BLOCKED_WRONG_DIRECTION_SENDS = new AtomicLong();
    private static final AtomicLong BLOCKED_INVALID_TARGET_SENDS = new AtomicLong();
    private static final AtomicLong HANDLER_DISPATCHES = new AtomicLong();
    private static final AtomicLong HANDLER_DISPATCHES_S2C = new AtomicLong();
    private static final AtomicLong HANDLER_DISPATCHES_C2S = new AtomicLong();
    private static final AtomicLong HANDLER_FAILURES = new AtomicLong();
    private static final AtomicLong CODEC_ENCODES = new AtomicLong();
    private static final AtomicLong CODEC_DECODES = new AtomicLong();
    private static final AtomicLong CODEC_ENCODE_FAILURES = new AtomicLong();
    private static final AtomicLong CODEC_DECODE_FAILURES = new AtomicLong();
    private static final AtomicLong CODEC_ENCODED_BYTES = new AtomicLong();
    private static final AtomicLong CODEC_DECODED_BYTES = new AtomicLong();
    private static final AtomicLong CODEC_MAX_ENCODED_BYTES = new AtomicLong();
    private static final AtomicLong CODEC_MAX_DECODED_BYTES = new AtomicLong();
    private static final AtomicLong CODEC_DECODE_LEFTOVERS = new AtomicLong();
    private static final AtomicLong CODEC_DECODE_LEFTOVER_BYTES = new AtomicLong();
    private static final AtomicLong CODEC_MAX_DECODE_LEFTOVER_BYTES = new AtomicLong();
    private static volatile String lastBlockedSend = "";
    private static volatile String lastHandlerFailure = "";
    private static volatile String lastCodecFailure = "";
    private static volatile String lastCodecSizeWarning = "";
    private static final List<LegacyPacketRegistration> LEGACY_REGISTERED_PACKETS = List.of(
            new LegacyPacketRegistration(0, "TESirenPacket", "S2C"),
            new LegacyPacketRegistration(1, "ItemDesignatorPacket", "C2S"),
            new LegacyPacketRegistration(2, "SatLaserPacket", "C2S"),
            new LegacyPacketRegistration(3, "AuxButtonPacket", "C2S"),
            new LegacyPacketRegistration(4, "TEVaultPacket", "S2C"),
            new LegacyPacketRegistration(5, "SatPanelPacket", "S2C"),
            new LegacyPacketRegistration(6, "ParticleBurstPacket", "S2C"),
            new LegacyPacketRegistration(7, "ExtPropPacket", "S2C"),
            new LegacyPacketRegistration(8, "TEFFPacket", "S2C"),
            new LegacyPacketRegistration(9, "ItemBobmazonPacket", "C2S"),
            new LegacyPacketRegistration(10, "TEMissileMultipartPacket", "S2C"),
            new LegacyPacketRegistration(11, "AuxParticlePacketNT", "S2C"),
            new LegacyPacketRegistration(12, "SatCoordPacket", "C2S"),
            new LegacyPacketRegistration(13, "HbmAnimationPacket", "S2C"),
            new LegacyPacketRegistration(14, "PlayerInformPacket", "S2C"),
            new LegacyPacketRegistration(15, "KeybindPacket", "C2S"),
            new LegacyPacketRegistration(16, "NBTControlPacket", "C2S"),
            new LegacyPacketRegistration(17, "AnvilCraftPacket", "C2S"),
            new LegacyPacketRegistration(18, "ExplosionKnockbackPacket", "S2C"),
            new LegacyPacketRegistration(19, "ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket", "S2C"),
            new LegacyPacketRegistration(20, "NBTItemControlPacket", "C2S"),
            new LegacyPacketRegistration(21, "PermaSyncPacket", "S2C"),
            new LegacyPacketRegistration(22, "BiomeSyncPacket", "S2C"),
            new LegacyPacketRegistration(23, "BufPacket", "S2C"),
            new LegacyPacketRegistration(24, "SerializableRecipePacket", "S2C"),
            new LegacyPacketRegistration(25, "HeldItemNBTPacket", "S2C"),
            new LegacyPacketRegistration(26, "MuzzleFlashPacket", "S2C"));
    private static final List<LegacyPacketMapping> LEGACY_PACKET_MAPPINGS = List.of(
            new LegacyPacketMapping("ExtPropPacket", "PlayerRadiationSyncPacket", "S2C",
                    "radiation/status fields split out of legacy extended properties"),
            new LegacyPacketMapping("ExtPropPacket", "PlayerPropertiesPacket", "S2C",
                    "typed player property payloads not owned by radiation"),
            new LegacyPacketMapping("ExtPropPacket", "ExtPropertiesSyncPacket", "S2C",
                    "combined living and player extended properties sync matching the legacy single packet shape"),
            new LegacyPacketMapping("PermaSyncPacket", "PermaSyncPacket", "S2C",
                    "global/per-player persistent sync compound"),
            new LegacyPacketMapping("AuxParticlePacketNT", "AuxParticlePacket", "S2C",
                    "NBT particle payload with position fields"),
            new LegacyPacketMapping("ParticleBurstPacket", "ParticleBurstPacket", "S2C",
                    "block break particle burst by position and block state"),
            new LegacyPacketMapping("PlayerInformPacket", "ClientInformPacket", "S2C",
                    "HUD notice text with id and duration"),
            new LegacyPacketMapping("HbmAnimationPacket", "ItemAnimationPacket", "S2C",
                    "modern typed item animation payload"),
            new LegacyPacketMapping("HbmAnimationPacket", "LegacyItemAnimationPacket", "S2C",
                    "legacy small integer animation compatibility path"),
            new LegacyPacketMapping("MuzzleFlashPacket", "MuzzleFlashPacket", "S2C",
                    "entity muzzle flash timestamp marker"),
            new LegacyPacketMapping("HeldItemNBTPacket", "HeldItemNbtPacket", "S2C",
                    "held item NBT mirror for client render/HUD"),
            new LegacyPacketMapping("TEVaultPacket", "ClientTileEventPacket", "S2C",
                    "vault door client tile event hbm_ntm_rebirth:vault_door"),
            new LegacyPacketMapping("TESirenPacket", "ClientTileEventPacket", "S2C",
                    "siren client tile event hbm_ntm_rebirth:siren"),
            new LegacyPacketMapping("TEFFPacket", "TileSyncPacket", "S2C",
                    "force field state sync compound"),
            new LegacyPacketMapping("TEMissileMultipartPacket", "ClientMissileMultipartPacket", "S2C",
                    "missile multipart snapshot by block position"),
            new LegacyPacketMapping("SatPanelPacket", "ClientPanelDataPacket", "S2C",
                    "satellite panel type and NBT payload hbm_ntm_rebirth:satellite_panel"),
            new LegacyPacketMapping("BiomeSyncPacket", "ClientBiomeSyncPacket", "S2C",
                    "single-cell or whole-chunk biome short array"),
            new LegacyPacketMapping("BufPacket", "ClientTileBinaryDataPacket", "S2C",
                    "small tile binary payload by channel"),
            new LegacyPacketMapping("BufPacket", "ClientTileBinaryDataChunkPacket", "S2C",
                    "large tile binary payload split by transfer id"),
            new LegacyPacketMapping("SerializableRecipePacket", "ClientBinaryDataPacket", "S2C",
                    "generic non-recipe-manager client binary data fallback"),
            new LegacyPacketMapping("SerializableRecipePacket", "ClientBinaryDataChunkPacket", "S2C",
                    "large generic client binary data fallback"),
            new LegacyPacketMapping("SerializableRecipePacket", "ClientBinaryDataReadyPacket", "S2C",
                    "legacy reinit=true completion signal after all recipe files are received"),
            new LegacyPacketMapping("ExplosionKnockbackPacket", "ExplosionKnockbackPacket", "S2C",
                    "client motion impulse for explosion effects"),
            new LegacyPacketMapping("ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket",
                    "CompressedExplosionEffectPacket", "S2C",
                    "compressed affected block positions for client explosion effects"),
            new LegacyPacketMapping("AuxButtonPacket", "LegacyButtonPacket", "C2S",
                    "legacy block position plus value/id button packet"),
            new LegacyPacketMapping("AuxButtonPacket", "MachineBatteryButtonPacket", "C2S",
                    "machine battery GUI slot transfer buttons were value/id cases inside the legacy AuxButtonPacket handler"),
            new LegacyPacketMapping("AuxButtonPacket", "ServerTileActionPacket", "C2S",
                    "typed tile action replacement for migrated machines"),
            new LegacyPacketMapping("NBTControlPacket", "TileControlPacket", "C2S",
                    "legacy block position plus NBT control compound"),
            new LegacyPacketMapping("NBTControlPacket", "ServerTileActionPacket", "C2S",
                    "typed tile action replacement for named controls"),
            new LegacyPacketMapping("NBTItemControlPacket", "ItemControlPacket", "C2S",
                    "held item NBT control compound"),
            new LegacyPacketMapping("ItemDesignatorPacket", "ItemActionPacket", "C2S",
                    "hbm_ntm_rebirth:designator typed held item action"),
            new LegacyPacketMapping("ItemBobmazonPacket", "ItemActionPacket", "C2S",
                    "hbm_ntm_rebirth:bobmazon_offer typed held item action"),
            new LegacyPacketMapping("SatCoordPacket", "CoordinateActionPacket", "C2S",
                    "satellite coordinate action with frequency"),
            new LegacyPacketMapping("SatLaserPacket", "CoordinateActionPacket", "C2S",
                    "satellite laser/click action with frequency"),
            new LegacyPacketMapping("AnvilCraftPacket", "TypedMenuActionPacket", "C2S",
                    "hbm_ntm_rebirth:anvil_craft typed menu action"),
            new LegacyPacketMapping("KeybindPacket", "KeybindPacket", "C2S",
                    "key id plus pressed state"));

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(CHANNEL_NAME)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static LegacyNetworkDispatcher wrapper() {
        return LegacyNetworkDispatcher.WRAPPER;
    }

    public static Object unwrapLegacyPacket(Object message) {
        if (message instanceof LegacyPacketAdapter adapter) {
            Object modernPacket = adapter.toModernPacket();
            return modernPacket == null ? message : modernPacket;
        }
        return message;
    }

    public static String legacyWrapperSummary() {
        return "legacyWrapper=PacketDispatcher.wrapper facade"
                + " directHelpers=" + LegacyNetworkDispatcher.directSendHelperCount()
                + " threadedHelpers=" + LegacyNetworkDispatcher.threadedSendHelperCount()
                + " packetThreadingHelpers=" + LegacyNetworkDispatcher.packetThreadingHelperCount()
                + " targetPointFactories=" + LegacyTargetPoint.modernFactoryCount()
                + "/" + LegacyTargetPoint.legacyFactoryCount()
                + " flushCalls=" + LegacyNetworkDispatcher.legacyFlushCallCount()
                + " rawBufferBlocked=" + LegacyRawBufferNetwork.blockedRawBufferSendCount()
                + " dimensionIdBlocked=" + LegacyDimensionIdNetwork.blockedDimensionIdSendCount()
                + " packetThreadingWaitCalls=" + LegacyPacketThreading.legacyWaitCallCount()
                + " note=" + LegacyNetworkDispatcher.compatibilityNote();
    }

    public static String protocolVersion() {
        return PROTOCOL_VERSION;
    }

    public static ResourceLocation channelName() {
        return CHANNEL_NAME;
    }

    public static String legacyChannelName() {
        return LEGACY_CHANNEL_NAME;
    }

    public static NetworkChannelSnapshot networkChannelSnapshot() {
        PacketRegistration firstModern = PACKET_REGISTRATIONS.isEmpty() ? null : PACKET_REGISTRATIONS.get(0);
        PacketRegistration lastModern = PACKET_REGISTRATIONS.isEmpty() ? null : PACKET_REGISTRATIONS.get(PACKET_REGISTRATIONS.size() - 1);
        LegacyPacketRegistration firstLegacy = LEGACY_REGISTERED_PACKETS.isEmpty() ? null : LEGACY_REGISTERED_PACKETS.get(0);
        LegacyPacketRegistration lastLegacy = LEGACY_REGISTERED_PACKETS.isEmpty() ? null : LEGACY_REGISTERED_PACKETS.get(LEGACY_REGISTERED_PACKETS.size() - 1);
        return new NetworkChannelSnapshot(
                LEGACY_CHANNEL_NAME,
                CHANNEL_NAME,
                PROTOCOL_VERSION,
                PACKET_REGISTRATIONS.size(),
                firstModern == null ? -1 : firstModern.id(),
                firstModern == null ? "" : firstModern.typeName(),
                lastModern == null ? -1 : lastModern.id(),
                lastModern == null ? "" : lastModern.typeName(),
                LEGACY_REGISTERED_PACKETS.size(),
                firstLegacy == null ? -1 : firstLegacy.legacyId(),
                firstLegacy == null ? "" : firstLegacy.legacyName(),
                lastLegacy == null ? -1 : lastLegacy.legacyId(),
                lastLegacy == null ? "" : lastLegacy.legacyName(),
                packetIdsAreContiguous(PACKET_REGISTRATIONS.stream().map(PacketRegistration::id).toList()),
                packetIdsAreContiguous(LEGACY_REGISTERED_PACKETS.stream().map(LegacyPacketRegistration::legacyId).toList()),
                "SimpleChannel registration is append-only; legacy numeric discriminators are documented for migration lookup only");
    }

    public static ProtocolManifestSnapshot protocolManifestSnapshot() {
        ProtocolAudit audit = protocolAudit();
        List<ProtocolManifestRow> rows = LEGACY_REGISTERED_PACKETS.stream()
                .map(registration -> {
                    List<LegacyPacketMapping> mappings = legacyPacketMappings(registration.legacyName());
                    String modernPackets = mappings.stream()
                            .map(mapping -> packetRegistration(mapping.modernName())
                                    .map(modern -> "#" + modern.id() + " " + modern.typeName())
                                    .orElse("#? " + mapping.modernName()))
                            .distinct()
                            .toList()
                            .stream()
                            .reduce((left, right) -> left + ", " + right)
                            .orElse("unmapped");
                    String notes = mappings.stream()
                            .map(LegacyPacketMapping::notes)
                            .distinct()
                            .toList()
                            .stream()
                            .reduce((left, right) -> left + "; " + right)
                            .orElse("");
                    return new ProtocolManifestRow(
                            registration.legacyId(),
                            registration.legacyName(),
                            registration.direction(),
                            mappings.size(),
                            modernPackets,
                            notes);
                })
                .toList();
        return new ProtocolManifestSnapshot(
                LEGACY_CHANNEL_NAME,
                CHANNEL_NAME,
                PROTOCOL_VERSION,
                protocolManifestFingerprint(),
                PACKET_REGISTRATIONS.size(),
                LEGACY_REGISTERED_PACKETS.size(),
                LEGACY_PACKET_MAPPINGS.size(),
                rows,
                audit.hasProblems(),
                "Fingerprint covers modern registration order, legacy discriminator order, and legacy-to-modern mapping rows");
    }

    public static ProtocolContractSnapshot protocolContractSnapshot() {
        NetworkChannelSnapshot channel = networkChannelSnapshot();
        ProtocolAudit audit = protocolAudit();
        ProtocolManifestSnapshot manifest = protocolManifestSnapshot();
        List<String> problems = new ArrayList<>();
        if (!LEGACY_CHANNEL_NAME.equals(channel.legacyChannelName())) {
            problems.add("legacy channel changed from " + LEGACY_CHANNEL_NAME + " to " + channel.legacyChannelName());
        }
        if (!CHANNEL_NAME.equals(channel.modernChannelName())) {
            problems.add("modern channel changed from " + CHANNEL_NAME + " to " + channel.modernChannelName());
        }
        if (!PROTOCOL_VERSION.equals(channel.protocolVersion())) {
            problems.add("protocol version changed from " + PROTOCOL_VERSION + " to " + channel.protocolVersion());
        }
        if (channel.legacyPacketRegistrationCount() != EXPECTED_LEGACY_PACKET_COUNT) {
            problems.add("legacy packet count expected " + EXPECTED_LEGACY_PACKET_COUNT
                    + " but was " + channel.legacyPacketRegistrationCount());
        }
        if (channel.firstLegacyPacketId() != EXPECTED_FIRST_LEGACY_PACKET_ID
                || !EXPECTED_FIRST_LEGACY_PACKET_NAME.equals(channel.firstLegacyPacketName())) {
            problems.add("legacy first packet expected #" + EXPECTED_FIRST_LEGACY_PACKET_ID
                    + " " + EXPECTED_FIRST_LEGACY_PACKET_NAME
                    + " but was #" + channel.firstLegacyPacketId() + " " + channel.firstLegacyPacketName());
        }
        if (channel.lastLegacyPacketId() != EXPECTED_LAST_LEGACY_PACKET_ID
                || !EXPECTED_LAST_LEGACY_PACKET_NAME.equals(channel.lastLegacyPacketName())) {
            problems.add("legacy last packet expected #" + EXPECTED_LAST_LEGACY_PACKET_ID
                    + " " + EXPECTED_LAST_LEGACY_PACKET_NAME
                    + " but was #" + channel.lastLegacyPacketId() + " " + channel.lastLegacyPacketName());
        }
        if (!channel.legacyPacketIdsContiguous()) {
            problems.add("legacy discriminator table is not contiguous");
        }
        if (!channel.modernPacketIdsContiguous()) {
            problems.add("modern packet id table is not contiguous");
        }
        if (mappedLegacyPacketCount() != LEGACY_REGISTERED_PACKETS.size()) {
            problems.add("not all legacy packets have modern carrier mappings: "
                    + mappedLegacyPacketCount() + "/" + LEGACY_REGISTERED_PACKETS.size());
        }
        if (audit.hasProblems()) {
            problems.add("protocol audit has problems: " + protocolAuditSummary());
        }
        return new ProtocolContractSnapshot(
                manifest.fingerprint(),
                channel.legacyChannelName(),
                channel.modernChannelName(),
                channel.protocolVersion(),
                channel.registeredPacketCount(),
                channel.legacyPacketRegistrationCount(),
                legacyPacketMappingCount(),
                (int) mappedLegacyPacketCount(),
                channel.modernPacketIdsContiguous(),
                channel.legacyPacketIdsContiguous(),
                audit.hasProblems(),
                List.copyOf(problems),
                "Contract checks legacy 1.7.10 packet table invariants plus modern append-only registration health");
    }

    public static String protocolContractSummary() {
        ProtocolContractSnapshot contract = protocolContractSnapshot();
        return "ok=" + contract.passed()
                + " fingerprint=" + contract.fingerprint()
                + " modernPackets=" + contract.modernPacketCount()
                + " legacyPackets=" + contract.legacyPacketCount()
                + " legacyMapped=" + contract.mappedLegacyPacketCount() + "/" + contract.legacyPacketCount()
                + " mappingRows=" + contract.mappingRowCount()
                + " modernContiguous=" + contract.modernPacketIdsContiguous()
                + " legacyContiguous=" + contract.legacyPacketIdsContiguous()
                + " auditProblems=" + contract.auditProblems()
                + " problems=" + contract.problems().size();
    }

    public static String protocolManifestFingerprint() {
        CRC32 crc = new CRC32();
        byte[] bytes = canonicalProtocolManifest().getBytes(StandardCharsets.UTF_8);
        crc.update(bytes, 0, bytes.length);
        return String.format("%08x", crc.getValue());
    }

    private static String canonicalProtocolManifest() {
        StringBuilder builder = new StringBuilder();
        builder.append("legacyChannel=").append(LEGACY_CHANNEL_NAME).append('\n');
        builder.append("modernChannel=").append(CHANNEL_NAME).append('\n');
        builder.append("protocol=").append(PROTOCOL_VERSION).append('\n');
        builder.append("[modern]\n");
        for (PacketRegistration registration : PACKET_REGISTRATIONS) {
            builder.append(registration.id()).append('|')
                    .append(registration.direction()).append('|')
                    .append(registration.typeName()).append('\n');
        }
        builder.append("[legacy]\n");
        for (LegacyPacketRegistration registration : LEGACY_REGISTERED_PACKETS) {
            builder.append(registration.legacyId()).append('|')
                    .append(registration.direction()).append('|')
                    .append(registration.legacyName()).append('\n');
        }
        builder.append("[mapping]\n");
        for (LegacyPacketMapping mapping : LEGACY_PACKET_MAPPINGS) {
            builder.append(mapping.direction()).append('|')
                    .append(mapping.legacyName()).append('|')
                    .append(mapping.modernName()).append('\n');
        }
        return builder.toString();
    }

    public static String networkChannelSummary() {
        NetworkChannelSnapshot snapshot = networkChannelSnapshot();
        return "legacyChannel=" + snapshot.legacyChannelName()
                + " modernChannel=" + snapshot.modernChannelName()
                + " protocol=" + snapshot.protocolVersion()
                + " modernPackets=" + snapshot.registeredPacketCount()
                + " modernRange=#" + snapshot.firstModernPacketId() + "..#" + snapshot.lastModernPacketId()
                + " modernContiguous=" + snapshot.modernPacketIdsContiguous()
                + " legacyPackets=" + snapshot.legacyPacketRegistrationCount()
                + " legacyRange=#" + snapshot.firstLegacyPacketId() + "..#" + snapshot.lastLegacyPacketId()
                + " legacyContiguous=" + snapshot.legacyPacketIdsContiguous();
    }

    public static int libraryFoundationProgressPercent() {
        return LIBRARY_FOUNDATION_PROGRESS_PERCENT;
    }

    public static int legacyPacketCoveragePercent() {
        int total = legacyPacketRegistrationCount();
        return total == 0 ? 100 : (int) Math.round(mappedLegacyPacketCount() * 100.0D / total);
    }

    public static String progressSummary() {
        return "legacyPacketCoverage=" + legacyPacketCoveragePercent()
                + "% (" + mappedLegacyPacketCount() + "/" + legacyPacketRegistrationCount() + ")"
                + " foundation=" + LIBRARY_FOUNDATION_PROGRESS_PERCENT + "%"
                + " note=remaining work is mostly receiver/business integration";
    }

    public static int registeredPacketCount() {
        return PACKET_REGISTRATIONS.size();
    }

    public static List<PacketRegistration> packetRegistrations() {
        return List.copyOf(PACKET_REGISTRATIONS);
    }

    public static Optional<PacketRegistration> packetRegistration(String typeName) {
        return PACKET_REGISTRATIONS.stream()
                .filter(registration -> registration.typeName().equals(typeName))
                .findFirst();
    }

    public static Optional<PacketRegistration> packetRegistration(int id) {
        return PACKET_REGISTRATIONS.stream()
                .filter(registration -> registration.id() == id)
                .findFirst();
    }

    public static Optional<PacketRegistration> packetRegistration(Class<?> type) {
        return Optional.ofNullable(PACKET_REGISTRATIONS_BY_TYPE.get(type));
    }

    public static boolean isRegisteredMessage(Object message) {
        return message != null && isRegisteredMessageType(message.getClass());
    }

    public static boolean isRegisteredMessageType(Class<?> type) {
        return PACKET_REGISTRATIONS_BY_TYPE.containsKey(type);
    }

    public static long blockedUnregisteredSendCount() {
        return BLOCKED_UNREGISTERED_SENDS.get();
    }

    public static long blockedWrongDirectionSendCount() {
        return BLOCKED_WRONG_DIRECTION_SENDS.get();
    }

    public static long blockedInvalidTargetSendCount() {
        return BLOCKED_INVALID_TARGET_SENDS.get();
    }

    public static String lastBlockedSend() {
        return lastBlockedSend;
    }

    public static String sendSafetySummary() {
        return "registeredTypes=" + PACKET_REGISTRATIONS_BY_TYPE.size()
                + " blockedUnregisteredSends=" + BLOCKED_UNREGISTERED_SENDS.get()
                + " blockedWrongDirectionSends=" + BLOCKED_WRONG_DIRECTION_SENDS.get()
                + " blockedInvalidTargetSends=" + BLOCKED_INVALID_TARGET_SENDS.get()
                + (lastBlockedSend.isBlank() ? "" : " lastBlocked=\"" + lastBlockedSend + "\"");
    }

    public static SendSafetySnapshot sendSafetySnapshot() {
        return new SendSafetySnapshot(
                PACKET_REGISTRATIONS_BY_TYPE.size(),
                BLOCKED_UNREGISTERED_SENDS.get(),
                BLOCKED_WRONG_DIRECTION_SENDS.get(),
                BLOCKED_INVALID_TARGET_SENDS.get(),
                lastBlockedSend);
    }

    public static HandlerRuntimeSnapshot handlerRuntimeSnapshot() {
        return new HandlerRuntimeSnapshot(
                HANDLER_DISPATCHES.get(),
                HANDLER_DISPATCHES_S2C.get(),
                HANDLER_DISPATCHES_C2S.get(),
                HANDLER_FAILURES.get(),
                lastHandlerFailure);
    }

    public static String handlerRuntimeSummary() {
        HandlerRuntimeSnapshot snapshot = handlerRuntimeSnapshot();
        return "handlerDispatches=" + snapshot.totalDispatches()
                + " s2c=" + snapshot.serverToClientDispatches()
                + " c2s=" + snapshot.clientToServerDispatches()
                + " failures=" + snapshot.failures()
                + (snapshot.lastFailure().isBlank() ? "" : " lastFailure=\"" + snapshot.lastFailure() + "\"");
    }

    public static CodecRuntimeSnapshot codecRuntimeSnapshot() {
        return new CodecRuntimeSnapshot(
                CODEC_ENCODES.get(),
                CODEC_DECODES.get(),
                CODEC_ENCODE_FAILURES.get(),
                CODEC_DECODE_FAILURES.get(),
                CODEC_ENCODED_BYTES.get(),
                CODEC_DECODED_BYTES.get(),
                CODEC_MAX_ENCODED_BYTES.get(),
                CODEC_MAX_DECODED_BYTES.get(),
                CODEC_DECODE_LEFTOVERS.get(),
                CODEC_DECODE_LEFTOVER_BYTES.get(),
                CODEC_MAX_DECODE_LEFTOVER_BYTES.get(),
                lastCodecFailure,
                lastCodecSizeWarning);
    }

    public static String codecRuntimeSummary() {
        CodecRuntimeSnapshot snapshot = codecRuntimeSnapshot();
        return "codecEncodes=" + snapshot.encodes()
                + " codecDecodes=" + snapshot.decodes()
                + " encodedBytes=" + snapshot.encodedBytes()
                + " decodedBytes=" + snapshot.decodedBytes()
                + " maxEncoded=" + snapshot.maxEncodedBytes()
                + " maxDecoded=" + snapshot.maxDecodedBytes()
                + " decodeLeftovers=" + snapshot.decodeLeftovers()
                + " leftoverBytes=" + snapshot.decodeLeftoverBytes()
                + " encodeFailures=" + snapshot.encodeFailures()
                + " decodeFailures=" + snapshot.decodeFailures()
                + (snapshot.lastFailure().isBlank() ? "" : " lastFailure=\"" + snapshot.lastFailure() + "\"");
    }

    public static void resetSendSafetyCounters() {
        BLOCKED_UNREGISTERED_SENDS.set(0L);
        BLOCKED_WRONG_DIRECTION_SENDS.set(0L);
        BLOCKED_INVALID_TARGET_SENDS.set(0L);
        lastBlockedSend = "";
    }

    public static void resetHandlerRuntimeDiagnostics() {
        HANDLER_DISPATCHES.set(0L);
        HANDLER_DISPATCHES_S2C.set(0L);
        HANDLER_DISPATCHES_C2S.set(0L);
        HANDLER_FAILURES.set(0L);
        lastHandlerFailure = "";
    }

    public static void resetCodecRuntimeDiagnostics() {
        CODEC_ENCODES.set(0L);
        CODEC_DECODES.set(0L);
        CODEC_ENCODE_FAILURES.set(0L);
        CODEC_DECODE_FAILURES.set(0L);
        CODEC_ENCODED_BYTES.set(0L);
        CODEC_DECODED_BYTES.set(0L);
        CODEC_MAX_ENCODED_BYTES.set(0L);
        CODEC_MAX_DECODED_BYTES.set(0L);
        CODEC_DECODE_LEFTOVERS.set(0L);
        CODEC_DECODE_LEFTOVER_BYTES.set(0L);
        CODEC_MAX_DECODE_LEFTOVER_BYTES.set(0L);
        lastCodecFailure = "";
        lastCodecSizeWarning = "";
    }

    public static NetworkRuntimeSnapshot networkRuntimeSnapshot() {
        ProtocolAudit audit = protocolAudit();
        return new NetworkRuntimeSnapshot(
                PROTOCOL_VERSION,
                LIBRARY_FOUNDATION_PROGRESS_PERCENT,
                legacyPacketCoveragePercent(),
                registeredPacketCount(),
                legacyPacketRegistrationCount(),
                mappedLegacyPacketCount(),
                unmappedLegacyPacketRegistrations().size(),
                legacyPacketMappingCount(),
                audit.hasProblems(),
                audit.mappingsToUnregisteredModernPackets().size(),
                audit.mappingsFromUnknownLegacyPackets().size(),
                audit.mappingsWithDirectionMismatch().size(),
                audit.duplicateLegacyIds().size()
                        + audit.duplicateLegacyNames().size()
                        + audit.duplicateModernRegistrations().size(),
                sendSafetySnapshot(),
                codecRuntimeSnapshot(),
                handlerRuntimeSnapshot(),
                ThreadedPacketDispatcher.snapshot(),
                LegacyPacketThreading.legacyCommandSnapshot(),
                LegacyNetworkDispatcher.legacyFlushCallCount(),
                LegacyPacketThreading.legacyWaitCallCount(),
                LegacyRawBufferNetwork.blockedRawBufferSendCount(),
                LegacyRawBufferNetwork.lastBlockedRawBufferSend(),
                LegacyDimensionIdNetwork.blockedDimensionIdSendCount(),
                LegacyDimensionIdNetwork.lastBlockedDimensionIdSend(),
                LegacyNetworkDispatcher.directSendHelperCount(),
                LegacyNetworkDispatcher.threadedSendHelperCount(),
                LegacyNetworkDispatcher.packetThreadingHelperCount());
    }

    public static String networkRuntimeSummary() {
        NetworkRuntimeSnapshot snapshot = networkRuntimeSnapshot();
        return "protocol=" + snapshot.protocolVersion()
                + " foundation=" + snapshot.foundationProgressPercent() + "%"
                + " legacyCoverage=" + snapshot.legacyPacketCoveragePercent() + "%"
                + " packets=" + snapshot.registeredPacketCount()
                + " legacyMapped=" + snapshot.mappedLegacyPacketCount() + "/" + snapshot.legacyPacketRegistrationCount()
                + " auditProblems=" + snapshot.auditProblems()
                + " blockedSends=" + snapshot.sendSafety().totalBlockedSends()
                + " codecFailures=" + snapshot.codec().totalFailures()
                + " codecLeftovers=" + snapshot.codec().decodeLeftovers()
                + " handlerDispatches=" + snapshot.handlers().totalDispatches()
                + " handlerFailures=" + snapshot.handlers().failures()
                + " threadedPending=" + snapshot.threaded().pending()
                + " threadedPrepared=" + snapshot.threaded().totalPrepared()
                + " threadedPreparedCopies=" + snapshot.threaded().preparedCopyInstance()
                + " threadedDiscarded=" + snapshot.threaded().totalDiscarded()
                + " threadedFallback=" + snapshot.threaded().fallbackToMainThread()
                + " rawBufferBlocked=" + snapshot.rawBufferBlockedSends()
                + " dimensionIdBlocked=" + snapshot.dimensionIdBlockedSends()
                + " legacyWaitCalls=" + snapshot.legacyWaitCalls()
                + " legacyLastTickTotal=" + snapshot.legacyPacketThreading().lastTickTotal()
                + " helperSurface=" + snapshot.totalHelperCount();
    }

    public static void resetNetworkRuntimeDiagnostics() {
        resetSendSafetyCounters();
        resetCodecRuntimeDiagnostics();
        resetHandlerRuntimeDiagnostics();
        ThreadedPacketDispatcher.resetState();
        LegacyPacketThreading.resetLegacyCounters();
        LegacyNetworkDispatcher.resetLegacyCounters();
    }

    public static List<LegacyPacketMapping> legacyPacketMappings() {
        return LEGACY_PACKET_MAPPINGS;
    }

    public static List<LegacyPacketMapping> legacyPacketMappings(String legacyName) {
        return LEGACY_PACKET_MAPPINGS.stream()
                .filter(mapping -> mapping.legacyName().equals(legacyName))
                .toList();
    }

    public static List<LegacyPacketMapping> modernPacketMappings(String modernName) {
        return LEGACY_PACKET_MAPPINGS.stream()
                .filter(mapping -> mapping.modernName().equals(modernName))
                .toList();
    }

    public static int legacyPacketMappingCount() {
        return LEGACY_PACKET_MAPPINGS.size();
    }

    public static List<LegacyPacketRegistration> legacyPacketRegistrations() {
        return LEGACY_REGISTERED_PACKETS;
    }

    public static Optional<LegacyPacketRegistration> legacyPacketRegistration(String legacyName) {
        return LEGACY_REGISTERED_PACKETS.stream()
                .filter(registration -> registration.legacyName().equals(legacyName))
                .findFirst();
    }

    public static Optional<LegacyPacketRegistration> legacyPacketRegistration(int legacyId) {
        return LEGACY_REGISTERED_PACKETS.stream()
                .filter(registration -> registration.legacyId() == legacyId)
                .findFirst();
    }

    public static int legacyPacketRegistrationCount() {
        return LEGACY_REGISTERED_PACKETS.size();
    }

    public static long mappedLegacyPacketCount() {
        Set<String> mappedNames = LEGACY_PACKET_MAPPINGS.stream()
                .map(LegacyPacketMapping::legacyName)
                .collect(java.util.stream.Collectors.toSet());
        return LEGACY_REGISTERED_PACKETS.stream()
                .filter(registration -> mappedNames.contains(registration.legacyName))
                .count();
    }

    public static List<LegacyPacketRegistration> unmappedLegacyPacketRegistrations() {
        Set<String> mappedNames = LEGACY_PACKET_MAPPINGS.stream()
                .map(LegacyPacketMapping::legacyName)
                .collect(java.util.stream.Collectors.toSet());
        return LEGACY_REGISTERED_PACKETS.stream()
                .filter(registration -> !mappedNames.contains(registration.legacyName))
                .toList();
    }

    public static ProtocolAudit protocolAudit() {
        Set<String> registeredModernPackets = PACKET_REGISTRATIONS.stream()
                .map(PacketRegistration::typeName)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> legacyRegisteredPackets = LEGACY_REGISTERED_PACKETS.stream()
                .map(LegacyPacketRegistration::legacyName)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> mappedLegacyPackets = LEGACY_PACKET_MAPPINGS.stream()
                .map(LegacyPacketMapping::legacyName)
                .collect(java.util.stream.Collectors.toSet());
        List<LegacyPacketMapping> mappingsToUnregisteredModernPackets = LEGACY_PACKET_MAPPINGS.stream()
                .filter(mapping -> !registeredModernPackets.contains(mapping.modernName()))
                .toList();
        List<LegacyPacketMapping> mappingsFromUnknownLegacyPackets = LEGACY_PACKET_MAPPINGS.stream()
                .filter(mapping -> !legacyRegisteredPackets.contains(mapping.legacyName()))
                .toList();
        List<PacketRegistration> modernPacketsWithoutLegacyMappings = PACKET_REGISTRATIONS.stream()
                .filter(registration -> modernPacketMappings(registration.typeName()).isEmpty())
                .toList();
        List<LegacyPacketRegistration> unmappedLegacyPackets = LEGACY_REGISTERED_PACKETS.stream()
                .filter(registration -> !mappedLegacyPackets.contains(registration.legacyName()))
                .toList();
        List<LegacyPacketMapping> mappingsWithDirectionMismatch = LEGACY_PACKET_MAPPINGS.stream()
                .filter(mapping -> legacyPacketRegistration(mapping.legacyName())
                        .map(registration -> !registration.direction().equals(mapping.direction()))
                        .orElse(false))
                .toList();
        List<String> duplicateLegacyIds = duplicateValues(LEGACY_REGISTERED_PACKETS.stream()
                .map(registration -> Integer.toString(registration.legacyId()))
                .toList());
        List<String> duplicateLegacyNames = duplicateValues(LEGACY_REGISTERED_PACKETS.stream()
                .map(LegacyPacketRegistration::legacyName)
                .toList());
        List<String> duplicateModernRegistrations = duplicateValues(PACKET_REGISTRATIONS.stream()
                .map(PacketRegistration::typeName)
                .toList());
        return new ProtocolAudit(
                mappingsToUnregisteredModernPackets,
                mappingsFromUnknownLegacyPackets,
                modernPacketsWithoutLegacyMappings,
                unmappedLegacyPackets,
                mappingsWithDirectionMismatch,
                duplicateLegacyIds,
                duplicateLegacyNames,
                duplicateModernRegistrations);
    }

    public static String protocolAuditSummary() {
        ProtocolAudit audit = protocolAudit();
        return "problems=" + audit.hasProblems()
                + " missingModern=" + audit.mappingsToUnregisteredModernPackets().size()
                + " unknownLegacy=" + audit.mappingsFromUnknownLegacyPackets().size()
                + " directionMismatch=" + audit.mappingsWithDirectionMismatch().size()
                + " unmappedLegacy=" + audit.unmappedLegacyPackets().size()
                + " duplicateLegacyIds=" + audit.duplicateLegacyIds().size()
                + " duplicateLegacyNames=" + audit.duplicateLegacyNames().size()
                + " duplicateModernRegistrations=" + audit.duplicateModernRegistrations().size()
                + " modernWithoutLegacyMappings=" + audit.modernPacketsWithoutLegacyMappings().size();
    }

    public static void logProtocolAudit() {
        ProtocolAudit audit = protocolAudit();
        ProtocolContractSnapshot contract = protocolContractSnapshot();
        if (audit.hasProblems()) {
            HbmNtm.LOGGER.warn("HBM network protocol audit found issues: {}", protocolAuditSummary());
            audit.mappingsToUnregisteredModernPackets().forEach(mapping -> HbmNtm.LOGGER.warn(
                    "Network mapping points to unregistered modern packet: {} -> {}",
                    mapping.legacyName(), mapping.modernName()));
            audit.mappingsFromUnknownLegacyPackets().forEach(mapping -> HbmNtm.LOGGER.warn(
                    "Network mapping references unknown legacy packet: {} -> {}",
                    mapping.legacyName(), mapping.modernName()));
            audit.mappingsWithDirectionMismatch().forEach(mapping -> HbmNtm.LOGGER.warn(
                    "Network mapping direction mismatch: {} -> {} mappingDirection={}",
                    mapping.legacyName(), mapping.modernName(), mapping.direction()));
            audit.unmappedLegacyPackets().forEach(registration -> HbmNtm.LOGGER.warn(
                    "Unmapped legacy network packet: #{} {} {}",
                    registration.legacyId(), registration.direction(), registration.legacyName()));
            return;
        }
        if (!contract.passed()) {
            HbmNtm.LOGGER.warn("HBM network protocol contract found issues: {}", protocolContractSummary());
            contract.problems().forEach(problem -> HbmNtm.LOGGER.warn("Network protocol contract issue: {}", problem));
            return;
        }
        HbmNtm.LOGGER.info("HBM network protocol audit passed: {} contract={}",
                protocolAuditSummary(), protocolContractSummary());
    }

    private static List<String> duplicateValues(List<String> values) {
        return values.stream()
                .filter(value -> Collections.frequency(values, value) > 1)
                .distinct()
                .toList();
    }

    private static boolean packetIdsAreContiguous(List<Integer> ids) {
        if (ids.isEmpty()) {
            return true;
        }
        for (int index = 0; index < ids.size(); index++) {
            if (ids.get(index) != index) {
                return false;
            }
        }
        return true;
    }

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
        registerServerToClient(ClientBinaryDataChunkPacket.class,
                ClientBinaryDataChunkPacket::decode,
                ClientBinaryDataChunkPacket::encode,
                ClientBinaryDataChunkPacket::handle);
        registerServerToClient(ClientBinaryDataReadyPacket.class,
                ClientBinaryDataReadyPacket::decode,
                ClientBinaryDataReadyPacket::encode,
                ClientBinaryDataReadyPacket::handle);
        registerServerToClient(ClientTileBinaryDataPacket.class,
                ClientTileBinaryDataPacket::decode,
                ClientTileBinaryDataPacket::encode,
                ClientTileBinaryDataPacket::handle);
        registerServerToClient(ClientTileBinaryDataChunkPacket.class,
                ClientTileBinaryDataChunkPacket::decode,
                ClientTileBinaryDataChunkPacket::encode,
                ClientTileBinaryDataChunkPacket::handle);
        registerClientToServer(ServerTileBinaryControlPacket.class,
                ServerTileBinaryControlPacket::decode,
                ServerTileBinaryControlPacket::encode,
                ServerTileBinaryControlPacket::handle);
        registerClientToServer(ServerTileBinaryControlChunkPacket.class,
                ServerTileBinaryControlChunkPacket::decode,
                ServerTileBinaryControlChunkPacket::encode,
                ServerTileBinaryControlChunkPacket::handle);
        registerServerToClient(ClientEntityEventPacket.class,
                ClientEntityEventPacket::decode,
                ClientEntityEventPacket::encode,
                ClientEntityEventPacket::handle);
        registerClientToServer(ServerEntityActionPacket.class,
                ServerEntityActionPacket::decode,
                ServerEntityActionPacket::encode,
                ServerEntityActionPacket::handle);
        registerClientToServer(ItemActionPacket.class,
                ItemActionPacket::decode,
                ItemActionPacket::encode,
                ItemActionPacket::handle);
        registerClientToServer(TypedMenuActionPacket.class,
                TypedMenuActionPacket::decode,
                TypedMenuActionPacket::encode,
                TypedMenuActionPacket::handle);
        registerServerToClient(LegacyItemAnimationPacket.class,
                LegacyItemAnimationPacket::decode,
                LegacyItemAnimationPacket::encode,
                LegacyItemAnimationPacket::handle);
        registerClientToServer(EntitySyncRequestPacket.class,
                EntitySyncRequestPacket::decode,
                EntitySyncRequestPacket::encode,
                EntitySyncRequestPacket::handle);
        registerServerToClient(ClientMissileMultipartPacket.class,
                ClientMissileMultipartPacket::decode,
                ClientMissileMultipartPacket::encode,
                ClientMissileMultipartPacket::handle);
        registerClientToServer(ServerTileActionPacket.class,
                ServerTileActionPacket::decode,
                ServerTileActionPacket::encode,
                ServerTileActionPacket::handle);
        registerClientToServer(ClientTileBinarySyncRequestPacket.class,
                ClientTileBinarySyncRequestPacket::decode,
                ClientTileBinarySyncRequestPacket::encode,
                ClientTileBinarySyncRequestPacket::handle);
        registerServerToClient(ExtPropertiesSyncPacket.class,
                ExtPropertiesSyncPacket::decode,
                ExtPropertiesSyncPacket::encode,
                ExtPropertiesSyncPacket::handle);
        registerServerToClient(PWRPrinterSnapshotPacket.class,
                PWRPrinterSnapshotPacket::decode,
                PWRPrinterSnapshotPacket::encode,
                PWRPrinterSnapshotPacket::handle);
    }

    public static void sendToServer(Object message) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateMessageForSend(payload, "server", "C2S")) {
            return;
        }
        CHANNEL.sendToServer(payload);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateTarget(player != null, payload, "player:null")) {
            return;
        }
        if (!validateMessageForSend(payload, player == null ? "player:null" : "player:" + player.getGameProfile().getName(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToEntityTrackers(Object message, Entity entity) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateTarget(entity != null, payload, "entityTrackers:null")) {
            return;
        }
        if (!validateMessageForSend(payload, entity == null ? "entityTrackers:null" : "entityTrackers:" + entity.getId(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), payload);
    }

    public static void sendToEntityAndSelf(Object message, Entity entity) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateTarget(entity != null, payload, "entityAndSelf:null")) {
            return;
        }
        if (!validateMessageForSend(payload, entity == null ? "entityAndSelf:null" : "entityAndSelf:" + entity.getId(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), payload);
    }

    public static void sendToDimension(Object message, ServerLevel level) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateTarget(level != null, payload, "dimension:null")) {
            return;
        }
        if (!validateMessageForSend(payload, level == null ? "dimension:null" : "dimension:" + level.dimension().location(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), payload);
    }

    public static void sendToDimension(Object message, ResourceKey<Level> dimension) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateTarget(dimension != null, payload, "dimensionKey:null")) {
            return;
        }
        if (!validateMessageForSend(payload, dimension == null ? "dimensionKey:null" : "dimensionKey:" + dimension.location(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), payload);
    }

    public static void sendToTracking(Object message, ServerLevel level, double x, double y, double z, double range) {
        sendToAllAround(message, level, x, y, z, range);
    }

    public static void sendToAllAround(Object message, ServerLevel level, double x, double y, double z, double range) {
        if (!validateTarget(level != null, message, "near-level:null")) {
            return;
        }
        sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, level.dimension()));
    }

    public static void sendToAllAround(Object message, ServerLevel level, BlockPos pos, double range) {
        if (!validateTarget(pos != null, message, "near-pos:null")) {
            return;
        }
        sendToAllAround(message, level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range);
    }

    public static void sendToAllAround(Object message, ResourceKey<Level> dimension, double x, double y, double z, double range) {
        if (!validateTarget(dimension != null, message, "near-dimensionKey:null")) {
            return;
        }
        sendToAllAround(message, new PacketDistributor.TargetPoint(x, y, z, range, dimension));
    }

    public static void sendToAllAround(Object message, ResourceKey<Level> dimension, BlockPos pos, double range) {
        if (!validateTarget(pos != null, message, "near-pos:null")) {
            return;
        }
        sendToAllAround(message, dimension, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range);
    }

    public static void sendToAllAround(Object message, Entity entity, double range) {
        if (!validateTarget(entity != null, message, "near-entity:null")) {
            return;
        }
        sendToAllAround(message, new PacketDistributor.TargetPoint(entity.getX(), entity.getY(), entity.getZ(), range, entity.level().dimension()));
    }

    public static void sendToAllAround(Object message, PacketDistributor.TargetPoint point) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateTarget(point != null, payload, "near-point:null")) {
            return;
        }
        if (!validateMessageForSend(payload, "near", "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.NEAR.with(() -> point), payload);
    }

    public static void sendToAllAround(Object message, LegacyTargetPoint point) {
        if (!validateTarget(point != null, message, "near-legacyTargetPoint:null")) {
            return;
        }
        if (point.hasModernDimension()) {
            sendToAllAround(message, point.toModernTargetPoint());
            return;
        }
        LegacyDimensionIdNetwork.rejectAllAround(message, point, false);
    }

    public static void sendToAll(Object message) {
        Object payload = unwrapLegacyPacket(message);
        if (!validateMessageForSend(payload, "all", "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.ALL.noArg(), payload);
    }

    public static void sendToTrackingChunk(Object message, BlockEntity blockEntity) {
        if (!validateTarget(blockEntity != null, message, "trackingChunk:blockEntity:null")) {
            return;
        }
        sendToTrackingChunk(message, blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static void sendToTrackingChunk(Object message, Level level, BlockPos pos) {
        if (!validateTarget(level != null, message, "trackingChunk:level:null")
                || !validateTarget(pos != null, message, "trackingChunk:pos:null")
                || !validateTarget(!level.isClientSide, message, "trackingChunk:clientLevel")) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)
                    .forEach(player -> sendToPlayer(message, player));
        }
    }

    public static void syncTileToTracking(HbmTileSyncable syncable, BlockEntity blockEntity) {
        sendToTrackingChunk(tileSyncPacket(blockEntity, syncable), blockEntity);
    }

    public static void syncTileToTrackingThreaded(HbmTileSyncable syncable, BlockEntity blockEntity) {
        ThreadedPacketDispatcher.sendToTrackingChunk(tileSyncPacket(blockEntity, syncable), blockEntity);
    }

    public static void syncTileToPlayer(HbmTileSyncable syncable, BlockEntity blockEntity, ServerPlayer player) {
        sendToPlayer(tileSyncPacket(blockEntity, syncable), player);
    }

    public static void syncTileToPlayerThreaded(HbmTileSyncable syncable, BlockEntity blockEntity, ServerPlayer player) {
        ThreadedPacketDispatcher.sendToPlayer(tileSyncPacket(blockEntity, syncable), player);
    }

    public static void syncEntityToTracking(HbmEntitySyncable syncable, Entity entity) {
        sendToEntityTrackers(entitySyncPacket(entity, syncable), entity);
    }

    public static void syncEntityToTrackingThreaded(HbmEntitySyncable syncable, Entity entity) {
        ThreadedPacketDispatcher.sendToEntityTrackers(entitySyncPacket(entity, syncable), entity);
    }

    public static void syncEntityToPlayer(HbmEntitySyncable syncable, Entity entity, ServerPlayer player) {
        if (syncable.canSendClientSyncTo(player)) {
            sendToPlayer(entitySyncPacket(entity, syncable), player);
        }
    }

    public static void syncEntityToPlayerThreaded(HbmEntitySyncable syncable, Entity entity, ServerPlayer player) {
        if (syncable.canSendClientSyncTo(player)) {
            ThreadedPacketDispatcher.sendToPlayer(entitySyncPacket(entity, syncable), player);
        }
    }

    public static TileSyncPacket tileSyncPacket(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        return new TileSyncPacket(pos, data);
    }

    public static TileSyncPacket tileSyncPacket(int x, int y, int z, net.minecraft.nbt.CompoundTag data) {
        return tileSyncPacket(new BlockPos(x, y, z), data);
    }

    public static TileSyncPacket tileSyncPacket(BlockEntity blockEntity, HbmTileSyncable syncable) {
        return tileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag());
    }

    public static EntitySyncPacket entitySyncPacket(int entityId, net.minecraft.nbt.CompoundTag data) {
        return new EntitySyncPacket(entityId, data);
    }

    public static EntitySyncPacket entitySyncPacket(Entity entity, net.minecraft.nbt.CompoundTag data) {
        return entitySyncPacket(entity == null ? -1 : entity.getId(), data);
    }

    public static EntitySyncPacket entitySyncPacket(Entity entity, HbmEntitySyncable syncable) {
        return entitySyncPacket(entity, syncable.getClientSyncTag());
    }

    public static void sendClientEntityEvent(Entity entity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToEntityTrackers(clientEntityEventPacket(entity, eventType, data), entity);
    }

    public static void sendClientEntityEventThreaded(Entity entity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToEntityTrackers(clientEntityEventPacket(entity, eventType, data), entity);
    }

    public static void sendClientEntityEventAndSelf(Entity entity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToEntityAndSelf(clientEntityEventPacket(entity, eventType, data), entity);
    }

    public static void sendClientEntityEventAndSelfThreaded(Entity entity, ResourceLocation eventType,
                                                            net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToEntityAndSelf(clientEntityEventPacket(entity, eventType, data), entity);
    }

    public static void sendClientEntityEvent(ServerPlayer player, Entity entity, ResourceLocation eventType,
                                             net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(clientEntityEventPacket(entity, eventType, data), player);
    }

    public static void sendClientEntityEventThreaded(ServerPlayer player, Entity entity, ResourceLocation eventType,
                                                     net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(clientEntityEventPacket(entity, eventType, data), player);
    }

    public static ClientEntityEventPacket clientEntityEventPacket(int entityId, ResourceLocation eventType,
                                                                  net.minecraft.nbt.CompoundTag data) {
        return new ClientEntityEventPacket(entityId, eventType, data);
    }

    public static ClientEntityEventPacket clientEntityEventPacket(Entity entity, ResourceLocation eventType,
                                                                  net.minecraft.nbt.CompoundTag data) {
        return clientEntityEventPacket(entity == null ? -1 : entity.getId(), eventType, data);
    }

    public static void sendEntityAction(Entity entity, ResourceLocation actionType, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new ServerEntityActionPacket(entity.getId(), actionType, data));
    }

    public static void informPlayer(ServerPlayer player, Component message, int id, int millis) {
        sendToPlayer(playerInformPacket(message, id, millis), player);
    }

    public static void informPlayerThreaded(ServerPlayer player, Component message, int id, int millis) {
        ThreadedPacketDispatcher.sendToPlayer(playerInformPacket(message, id, millis), player);
    }

    public static void informPlayer(ServerPlayer player, Component message, int id) {
        informPlayer(player, message, id, LEGACY_PLAYER_INFORM_DEFAULT_MILLIS);
    }

    public static void informPlayerThreaded(ServerPlayer player, Component message, int id) {
        informPlayerThreaded(player, message, id, LEGACY_PLAYER_INFORM_DEFAULT_MILLIS);
    }

    public static void informPlayer(ServerPlayer player, String message, int id, int millis) {
        informPlayer(player, Component.literal(message == null ? "" : message), id, millis);
    }

    public static void informPlayerThreaded(ServerPlayer player, String message, int id, int millis) {
        informPlayerThreaded(player, Component.literal(message == null ? "" : message), id, millis);
    }

    public static void informPlayer(ServerPlayer player, String message, int id) {
        informPlayer(player, message, id, LEGACY_PLAYER_INFORM_DEFAULT_MILLIS);
    }

    public static void informPlayerThreaded(ServerPlayer player, String message, int id) {
        informPlayerThreaded(player, message, id, LEGACY_PLAYER_INFORM_DEFAULT_MILLIS);
    }

    public static void sendPlayerInform(ServerPlayer player, Component message, int id, int millis) {
        informPlayer(player, message, id, millis);
    }

    public static void sendPlayerInformThreaded(ServerPlayer player, Component message, int id, int millis) {
        informPlayerThreaded(player, message, id, millis);
    }

    public static void sendPlayerInform(ServerPlayer player, Component message, int id) {
        informPlayer(player, message, id);
    }

    public static void sendPlayerInformThreaded(ServerPlayer player, Component message, int id) {
        informPlayerThreaded(player, message, id);
    }

    public static void sendPlayerInform(ServerPlayer player, String message, int id, int millis) {
        informPlayer(player, message, id, millis);
    }

    public static void sendPlayerInformThreaded(ServerPlayer player, String message, int id, int millis) {
        informPlayerThreaded(player, message, id, millis);
    }

    public static void sendPlayerInform(ServerPlayer player, String message, int id) {
        informPlayer(player, message, id);
    }

    public static void sendPlayerInformThreaded(ServerPlayer player, String message, int id) {
        informPlayerThreaded(player, message, id);
    }

    public static ClientInformPacket playerInformPacket(Component message, int id, int millis) {
        return new ClientInformPacket(message, id, millis);
    }

    public static ClientInformPacket playerInformPacket(Component message, int id) {
        return playerInformPacket(message, id, LEGACY_PLAYER_INFORM_DEFAULT_MILLIS);
    }

    public static ClientInformPacket playerInformPacket(String message, int id, int millis) {
        return playerInformPacket(Component.literal(message == null ? "" : message), id, millis);
    }

    public static ClientInformPacket playerInformPacket(String message, int id) {
        return playerInformPacket(message, id, LEGACY_PLAYER_INFORM_DEFAULT_MILLIS);
    }

    public static ClientInformPacket legacyPlayerInformPacket(String message, int id, int millis) {
        return playerInformPacket(message, id, millis);
    }

    public static ClientInformPacket legacyPlayerInformPacket(String message, int id) {
        return playerInformPacket(message, id);
    }

    public static ClientInformPacket legacyPlayerInformPacket(Component message, int id, int millis) {
        return playerInformPacket(message, id, millis);
    }

    public static ClientInformPacket legacyPlayerInformPacket(Component message, int id) {
        return playerInformPacket(message, id);
    }

    public static void sendAuxParticle(ServerLevel level, double x, double y, double z, net.minecraft.nbt.CompoundTag data,
                                       double range) {
        sendToAllAround(auxParticlePacket(x, y, z, data), level, x, y, z, range);
    }

    public static void sendAuxParticle(ServerLevel level, double x, double y, double z,
                                       double targetX, double targetY, double targetZ,
                                       net.minecraft.nbt.CompoundTag data, double range) {
        sendToAllAround(auxParticlePacket(x, y, z, data), level, targetX, targetY, targetZ, range);
    }

    public static void sendAuxParticleNT(ServerLevel level, double x, double y, double z,
                                         net.minecraft.nbt.CompoundTag data, double range) {
        sendAuxParticle(level, x, y, z, data, range);
    }

    public static void sendAuxParticle(ServerLevel level, Vec3 pos, net.minecraft.nbt.CompoundTag data, double range) {
        Vec3 safePos = pos == null ? Vec3.ZERO : pos;
        sendAuxParticle(level, safePos.x, safePos.y, safePos.z, data, range);
    }

    public static void sendAuxParticleNT(ServerLevel level, Vec3 pos, net.minecraft.nbt.CompoundTag data, double range) {
        sendAuxParticle(level, pos, data, range);
    }

    public static void sendAuxParticleThreaded(ServerLevel level, double x, double y, double z,
                                               net.minecraft.nbt.CompoundTag data, double range) {
        ThreadedPacketDispatcher.sendToAllAround(auxParticlePacket(x, y, z, data), level, x, y, z, range);
    }

    public static void sendAuxParticleNTThreaded(ServerLevel level, double x, double y, double z,
                                                 net.minecraft.nbt.CompoundTag data, double range) {
        sendAuxParticleThreaded(level, x, y, z, data, range);
    }

    public static void sendAuxParticleThreaded(ServerLevel level, Vec3 pos, net.minecraft.nbt.CompoundTag data,
                                               double range) {
        Vec3 safePos = pos == null ? Vec3.ZERO : pos;
        sendAuxParticleThreaded(level, safePos.x, safePos.y, safePos.z, data, range);
    }

    public static void sendAuxParticleNTThreaded(ServerLevel level, Vec3 pos, net.minecraft.nbt.CompoundTag data,
                                                 double range) {
        sendAuxParticleThreaded(level, pos, data, range);
    }

    public static void sendAuxParticle(ServerPlayer player, double x, double y, double z,
                                       net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(auxParticlePacket(x, y, z, data), player);
    }

    public static void sendAuxParticleNT(ServerPlayer player, double x, double y, double z,
                                         net.minecraft.nbt.CompoundTag data) {
        sendAuxParticle(player, x, y, z, data);
    }

    public static void sendAuxParticleThreaded(ServerPlayer player, double x, double y, double z,
                                               net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(auxParticlePacket(x, y, z, data), player);
    }

    public static void sendAuxParticleNTThreaded(ServerPlayer player, double x, double y, double z,
                                                 net.minecraft.nbt.CompoundTag data) {
        sendAuxParticleThreaded(player, x, y, z, data);
    }

    public static AuxParticlePacket auxParticlePacket(double x, double y, double z, net.minecraft.nbt.CompoundTag data) {
        return new AuxParticlePacket(auxParticlePayload(data, x, y, z));
    }

    public static AuxParticlePacket auxParticlePacketNT(double x, double y, double z, net.minecraft.nbt.CompoundTag data) {
        return auxParticlePacket(x, y, z, data);
    }

    public static AuxParticlePacket auxParticlePacketNT(net.minecraft.nbt.CompoundTag data, double x, double y,
                                                        double z) {
        return auxParticlePacket(x, y, z, data);
    }

    public static void sendItemAnimation(ServerPlayer player, int slot, int rail, String itemKey,
                                         ResourceLocation animationFile, String animationName, boolean holdLastFrame) {
        sendToPlayer(itemAnimationPacket(slot, rail, itemKey, animationFile, animationName, holdLastFrame), player);
    }

    public static void sendItemAnimationThreaded(ServerPlayer player, int slot, int rail, String itemKey,
                                                 ResourceLocation animationFile, String animationName,
                                                 boolean holdLastFrame) {
        ThreadedPacketDispatcher.sendToPlayer(
                itemAnimationPacket(slot, rail, itemKey, animationFile, animationName, holdLastFrame), player);
    }

    public static void sendLegacyItemAnimation(ServerPlayer player, int animationType, int receiverIndex, int itemIndex) {
        sendToPlayer(legacyItemAnimationPacket(animationType, receiverIndex, itemIndex), player);
    }

    public static void sendLegacyItemAnimationThreaded(ServerPlayer player, int animationType, int receiverIndex,
                                                       int itemIndex) {
        ThreadedPacketDispatcher.sendToPlayer(
                legacyItemAnimationPacket(animationType, receiverIndex, itemIndex), player);
    }

    public static void sendLegacyItemAnimation(ServerPlayer player, int animationType, int receiverIndex) {
        sendLegacyItemAnimation(player, animationType, receiverIndex, 0);
    }

    public static void sendLegacyItemAnimationThreaded(ServerPlayer player, int animationType, int receiverIndex) {
        sendLegacyItemAnimationThreaded(player, animationType, receiverIndex, 0);
    }

    public static void sendLegacyItemAnimation(ServerPlayer player, int animationType) {
        sendLegacyItemAnimation(player, animationType, 0, 0);
    }

    public static void sendLegacyItemAnimationThreaded(ServerPlayer player, int animationType) {
        sendLegacyItemAnimationThreaded(player, animationType, 0, 0);
    }

    public static void sendHbmAnimation(ServerPlayer player, int animationType, int receiverIndex, int itemIndex) {
        sendLegacyItemAnimation(player, animationType, receiverIndex, itemIndex);
    }

    public static void sendHbmAnimationThreaded(ServerPlayer player, int animationType, int receiverIndex,
                                                int itemIndex) {
        sendLegacyItemAnimationThreaded(player, animationType, receiverIndex, itemIndex);
    }

    public static void sendHbmAnimation(ServerPlayer player, int animationType, int receiverIndex) {
        sendLegacyItemAnimation(player, animationType, receiverIndex);
    }

    public static void sendHbmAnimationThreaded(ServerPlayer player, int animationType, int receiverIndex) {
        sendLegacyItemAnimationThreaded(player, animationType, receiverIndex);
    }

    public static void sendHbmAnimation(ServerPlayer player, int animationType) {
        sendLegacyItemAnimation(player, animationType);
    }

    public static void sendHbmAnimationThreaded(ServerPlayer player, int animationType) {
        sendLegacyItemAnimationThreaded(player, animationType);
    }

    public static ItemAnimationPacket itemAnimationPacket(int slot, int rail, String itemKey,
                                                          ResourceLocation animationFile, String animationName,
                                                          boolean holdLastFrame) {
        return new ItemAnimationPacket(slot, rail, itemKey, animationFile, animationName, holdLastFrame);
    }

    public static LegacyItemAnimationPacket legacyItemAnimationPacket(int animationType, int receiverIndex,
                                                                      int itemIndex) {
        return new LegacyItemAnimationPacket((short) animationType, receiverIndex, itemIndex);
    }

    public static LegacyItemAnimationPacket legacyItemAnimationPacket(int animationType, int receiverIndex) {
        return legacyItemAnimationPacket(animationType, receiverIndex, 0);
    }

    public static LegacyItemAnimationPacket legacyItemAnimationPacket(int animationType) {
        return legacyItemAnimationPacket(animationType, 0, 0);
    }

    public static LegacyItemAnimationPacket hbmAnimationPacket(int animationType, int receiverIndex, int itemIndex) {
        return legacyItemAnimationPacket(animationType, receiverIndex, itemIndex);
    }

    public static LegacyItemAnimationPacket hbmAnimationPacket(int animationType, int receiverIndex) {
        return legacyItemAnimationPacket(animationType, receiverIndex);
    }

    public static LegacyItemAnimationPacket hbmAnimationPacket(int animationType) {
        return legacyItemAnimationPacket(animationType);
    }

    public static void sendMuzzleFlash(Entity entity) {
        sendToEntityTrackers(muzzleFlashPacket(entity), entity);
    }

    public static MuzzleFlashPacket muzzleFlashPacket(Entity entity) {
        return new MuzzleFlashPacket(entity == null ? -1 : entity.getId());
    }

    public static MuzzleFlashPacket muzzleFlashPacket(int entityId) {
        return new MuzzleFlashPacket(entityId);
    }

    public static void sendMuzzleFlash(ServerLevel level, Entity entity, double range) {
        if (entity != null) {
            sendToAllAround(muzzleFlashPacket(entity), level, entity.getX(), entity.getY(), entity.getZ(), range);
        }
    }

    public static void sendMuzzleFlashThreaded(ServerLevel level, Entity entity, double range) {
        if (entity != null) {
            ThreadedPacketDispatcher.sendToAllAround(muzzleFlashPacket(entity),
                    level, entity.getX(), entity.getY(), entity.getZ(), range);
        }
    }

    public static void sendKeybind(HbmKeybind keybind, boolean pressed) {
        sendToServer(keybindPacket(keybind, pressed));
    }

    public static KeybindPacket keybindPacket(HbmKeybind keybind, boolean pressed) {
        return new KeybindPacket(keybind, pressed);
    }

    public static KeybindPacket keybindPacket(int legacyOrdinal, boolean pressed) {
        HbmKeybind[] keybinds = HbmKeybind.values();
        HbmKeybind keybind = legacyOrdinal >= 0 && legacyOrdinal < keybinds.length ? keybinds[legacyOrdinal] : null;
        return keybindPacket(keybind, pressed);
    }

    public static void sendKeybind(int legacyOrdinal, boolean pressed) {
        sendToServer(keybindPacket(legacyOrdinal, pressed));
    }

    public static void sendItemControl(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        sendToServer(itemControlPacket(hand, tag));
    }

    public static ItemControlPacket itemControlPacket(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        return new ItemControlPacket(hand, tag);
    }

    public static void sendItemControl(net.minecraft.nbt.CompoundTag tag) {
        sendItemControl(InteractionHand.MAIN_HAND, tag);
    }

    public static void sendNbtItemControl(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        sendToServer(nbtItemControlPacket(hand, tag));
    }

    public static ItemControlPacket nbtItemControlPacket(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        net.minecraft.nbt.CompoundTag data = tag == null ? new net.minecraft.nbt.CompoundTag() : tag.copy();
        data.putString("legacyPacket", HbmNetworkActions.NBT_ITEM_CONTROL.toString());
        return itemControlPacket(hand, data);
    }

    public static ItemControlPacket nbtItemControlPacket(net.minecraft.nbt.CompoundTag tag) {
        return nbtItemControlPacket(InteractionHand.MAIN_HAND, tag);
    }

    public static void sendNbtItemControl(net.minecraft.nbt.CompoundTag tag) {
        sendNbtItemControl(InteractionHand.MAIN_HAND, tag);
    }

    public static void sendItemAction(InteractionHand hand, ResourceLocation actionType, net.minecraft.nbt.CompoundTag data) {
        sendToServer(itemActionPacket(hand, actionType, data));
    }

    public static ItemActionPacket itemActionPacket(InteractionHand hand, ResourceLocation actionType,
                                                    net.minecraft.nbt.CompoundTag data) {
        return new ItemActionPacket(hand, actionType, data);
    }

    public static void sendItemAction(InteractionHand hand, ResourceLocation actionType) {
        sendItemAction(hand, actionType, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendDesignatorAction(InteractionHand hand, int operator, int value, int reference) {
        sendToServer(itemDesignatorPacket(hand, operator, value, reference));
    }

    public static ItemActionPacket itemDesignatorPacket(InteractionHand hand, int operator, int value, int reference) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("operator", operator);
        data.putInt("value", value);
        data.putInt("reference", reference);
        return itemActionPacket(hand, HbmNetworkActions.DESIGNATOR, data);
    }

    public static ItemActionPacket itemDesignatorPacket(int operator, int value, int reference) {
        return itemDesignatorPacket(InteractionHand.MAIN_HAND, operator, value, reference);
    }

    public static void sendDesignatorAction(int operator, int value, int reference) {
        sendDesignatorAction(InteractionHand.MAIN_HAND, operator, value, reference);
    }

    public static void sendItemDesignatorPacket(InteractionHand hand, int operator, int value, int reference) {
        sendDesignatorAction(hand, operator, value, reference);
    }

    public static void sendItemDesignatorPacket(int operator, int value, int reference) {
        sendDesignatorAction(operator, value, reference);
    }

    public static void sendBobmazonOffer(InteractionHand hand, int offerIndex) {
        sendToServer(itemBobmazonPacket(hand, offerIndex));
    }

    public static ItemActionPacket itemBobmazonPacket(InteractionHand hand, int offerIndex) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("offer", offerIndex);
        return itemActionPacket(hand, HbmNetworkActions.BOBMAZON_OFFER, data);
    }

    public static ItemActionPacket itemBobmazonPacket(int offerIndex) {
        return itemBobmazonPacket(InteractionHand.MAIN_HAND, offerIndex);
    }

    public static void sendBobmazonOffer(int offerIndex) {
        sendBobmazonOffer(InteractionHand.MAIN_HAND, offerIndex);
    }

    public static void sendItemBobmazonPacket(InteractionHand hand, int offerIndex) {
        sendBobmazonOffer(hand, offerIndex);
    }

    public static void sendItemBobmazonPacket(int offerIndex) {
        sendBobmazonOffer(offerIndex);
    }

    public static void sendSatelliteCoordinateAction(InteractionHand hand, BlockPos pos, int frequency) {
        sendToServer(satCoordPacket(hand, pos, frequency));
    }

    public static CoordinateActionPacket coordinateActionPacket(InteractionHand hand, BlockPos pos, int action,
                                                               int value, int frequency,
                                                               net.minecraft.nbt.CompoundTag data) {
        return new CoordinateActionPacket(hand, pos, action, value, frequency, data);
    }

    public static CoordinateActionPacket coordinateActionPacket(InteractionHand hand, int x, int y, int z,
                                                               int action, int value, int frequency,
                                                               net.minecraft.nbt.CompoundTag data) {
        return coordinateActionPacket(hand, new BlockPos(x, y, z), action, value, frequency, data);
    }

    public static CoordinateActionPacket coordinateActionPacket(BlockPos pos, int action, int value, int frequency,
                                                               net.minecraft.nbt.CompoundTag data) {
        return coordinateActionPacket(InteractionHand.MAIN_HAND, pos, action, value, frequency, data);
    }

    public static CoordinateActionPacket satCoordPacket(InteractionHand hand, BlockPos pos, int frequency) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putString("actionType", HbmNetworkActions.SATELLITE_COORDINATE.toString());
        return coordinateActionPacket(hand, pos, 0, 0, frequency, data);
    }

    public static CoordinateActionPacket satCoordPacket(BlockPos pos, int frequency) {
        return satCoordPacket(InteractionHand.MAIN_HAND, pos, frequency);
    }

    public static CoordinateActionPacket satCoordPacket(InteractionHand hand, int x, int y, int z, int frequency) {
        return satCoordPacket(hand, new BlockPos(x, y, z), frequency);
    }

    public static CoordinateActionPacket satCoordPacket(int x, int y, int z, int frequency) {
        return satCoordPacket(InteractionHand.MAIN_HAND, x, y, z, frequency);
    }

    public static void sendSatelliteCoordinateAction(BlockPos pos, int frequency) {
        sendSatelliteCoordinateAction(InteractionHand.MAIN_HAND, pos, frequency);
    }

    public static void sendSatelliteCoordinateAction(int x, int y, int z, int frequency) {
        sendSatelliteCoordinateAction(new BlockPos(x, y, z), frequency);
    }

    public static void sendSatelliteLaserAction(InteractionHand hand, int x, int z, int frequency) {
        sendToServer(satLaserPacket(hand, x, z, frequency));
    }

    public static CoordinateActionPacket satLaserPacket(InteractionHand hand, int x, int z, int frequency) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putBoolean("laser", true);
        data.putString("actionType", HbmNetworkActions.SATELLITE_LASER.toString());
        return coordinateActionPacket(hand, new BlockPos(x, 0, z), 1, 0, frequency, data);
    }

    public static CoordinateActionPacket satLaserPacket(int x, int z, int frequency) {
        return satLaserPacket(InteractionHand.MAIN_HAND, x, z, frequency);
    }

    public static CoordinateActionPacket satLaserPacket(BlockPos pos, int frequency) {
        BlockPos safePos = pos == null ? BlockPos.ZERO : pos;
        return satLaserPacket(safePos.getX(), safePos.getZ(), frequency);
    }

    public static void sendSatelliteLaserAction(int x, int z, int frequency) {
        sendSatelliteLaserAction(InteractionHand.MAIN_HAND, x, z, frequency);
    }

    public static void sendSatelliteLaserAction(BlockPos pos, int frequency) {
        BlockPos safePos = pos == null ? BlockPos.ZERO : pos;
        sendSatelliteLaserAction(safePos.getX(), safePos.getZ(), frequency);
    }

    public static void sendSatCoord(InteractionHand hand, int x, int y, int z, int frequency) {
        sendSatelliteCoordinateAction(hand, new BlockPos(x, y, z), frequency);
    }

    public static void sendSatCoord(BlockPos pos, int frequency) {
        sendSatelliteCoordinateAction(pos, frequency);
    }

    public static void sendSatCoord(int x, int y, int z, int frequency) {
        sendSatCoord(InteractionHand.MAIN_HAND, x, y, z, frequency);
    }

    public static void sendSatLaser(InteractionHand hand, int x, int z, int frequency) {
        sendSatelliteLaserAction(hand, x, z, frequency);
    }

    public static void sendSatLaser(int x, int z, int frequency) {
        sendSatLaser(InteractionHand.MAIN_HAND, x, z, frequency);
    }

    public static void sendSatLaser(BlockPos pos, int frequency) {
        sendSatelliteLaserAction(pos, frequency);
    }

    public static void syncHeldItemNbt(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        heldItemNbtPacket(hand, stack).ifPresent(packet -> sendToPlayer(packet, player));
    }

    public static void syncHeldItemNbtThreaded(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        heldItemNbtPacket(hand, stack).ifPresent(packet -> ThreadedPacketDispatcher.sendToPlayer(packet, player));
    }

    public static Optional<HeldItemNbtPacket> heldItemNbtPacket(InteractionHand hand, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            return Optional.empty();
        }
        return Optional.of(new HeldItemNbtPacket(hand, itemId, stack.getDamageValue(), stack.getOrCreateTag().copy()));
    }

    public static Optional<HeldItemNbtPacket> heldItemNbtPacket(ItemStack stack) {
        return heldItemNbtPacket(InteractionHand.MAIN_HAND, stack);
    }

    public static Optional<HeldItemNbtPacket> legacyHeldItemNbtPacket(ItemStack stack) {
        return heldItemNbtPacket(stack);
    }

    public static void syncHeldItemNbt(ServerPlayer player, ItemStack stack) {
        syncHeldItemNbt(player, InteractionHand.MAIN_HAND, stack);
    }

    public static void syncHeldItemNbtThreaded(ServerPlayer player, ItemStack stack) {
        syncHeldItemNbtThreaded(player, InteractionHand.MAIN_HAND, stack);
    }

    public static void syncHeldItemNbt(ServerPlayer player, InteractionHand hand) {
        if (player != null) {
            syncHeldItemNbt(player, hand, player.getItemInHand(hand));
        }
    }

    public static void syncHeldItemNbt(ServerPlayer player) {
        syncHeldItemNbt(player, InteractionHand.MAIN_HAND);
    }

    public static void syncHeldItemNbtThreaded(ServerPlayer player, InteractionHand hand) {
        if (player != null) {
            syncHeldItemNbtThreaded(player, hand, player.getItemInHand(hand));
        }
    }

    public static void syncHeldItemNbtThreaded(ServerPlayer player) {
        syncHeldItemNbtThreaded(player, InteractionHand.MAIN_HAND);
    }

    public static void sendHeldItemNbt(ServerPlayer player, ItemStack stack) {
        syncHeldItemNbt(player, stack);
    }

    public static void sendHeldItemNbt(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        syncHeldItemNbt(player, hand, stack);
    }

    public static void sendHeldItemNbt(ServerPlayer player, InteractionHand hand) {
        syncHeldItemNbt(player, hand);
    }

    public static void sendHeldItemNbt(ServerPlayer player) {
        syncHeldItemNbt(player);
    }

    public static void sendHeldItemNbtThreaded(ServerPlayer player, ItemStack stack) {
        syncHeldItemNbtThreaded(player, stack);
    }

    public static void sendHeldItemNbtThreaded(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        syncHeldItemNbtThreaded(player, hand, stack);
    }

    public static void sendHeldItemNbtThreaded(ServerPlayer player, InteractionHand hand) {
        syncHeldItemNbtThreaded(player, hand);
    }

    public static void sendHeldItemNbtThreaded(ServerPlayer player) {
        syncHeldItemNbtThreaded(player);
    }

    public static void sendLegacyButton(BlockPos pos, int value, int id) {
        sendToServer(auxButtonPacket(pos, value, id));
    }

    public static void sendLegacyButton(BlockEntity blockEntity, int value, int id) {
        sendLegacyButton(blockEntity.getBlockPos(), value, id);
    }

    public static LegacyButtonPacket auxButtonPacket(BlockPos pos, int value, int id) {
        return new LegacyButtonPacket(pos, value, id);
    }

    public static LegacyButtonPacket auxButtonPacket(BlockEntity blockEntity, int value, int id) {
        return auxButtonPacket(blockEntity.getBlockPos(), value, id);
    }

    public static LegacyButtonPacket auxButtonPacket(int x, int y, int z, int value, int id) {
        return auxButtonPacket(new BlockPos(x, y, z), value, id);
    }

    public static LegacyButtonPacket legacyButtonPacket(BlockPos pos, int value, int id) {
        return auxButtonPacket(pos, value, id);
    }

    public static LegacyButtonPacket legacyButtonPacket(BlockEntity blockEntity, int value, int id) {
        return auxButtonPacket(blockEntity, value, id);
    }

    public static LegacyButtonPacket legacyButtonPacket(int x, int y, int z, int value, int id) {
        return auxButtonPacket(x, y, z, value, id);
    }

    public static void sendLegacyButton(int x, int y, int z, int value, int id) {
        sendLegacyButton(new BlockPos(x, y, z), value, id);
    }

    public static void sendAuxButton(BlockPos pos, int value, int id) {
        sendLegacyButton(pos, value, id);
    }

    public static void sendAuxButton(BlockEntity blockEntity, int value, int id) {
        sendLegacyButton(blockEntity, value, id);
    }

    public static void sendAuxButton(int x, int y, int z, int value, int id) {
        sendLegacyButton(x, y, z, value, id);
    }

    public static void sendTileControl(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        sendToServer(nbtControlPacket(pos, data));
    }

    public static void sendTileControl(BlockEntity blockEntity, net.minecraft.nbt.CompoundTag data) {
        sendTileControl(blockEntity.getBlockPos(), data);
    }

    public static TileControlPacket nbtControlPacket(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        return new TileControlPacket(pos, data);
    }

    public static TileControlPacket nbtControlPacket(BlockEntity blockEntity, net.minecraft.nbt.CompoundTag data) {
        return nbtControlPacket(blockEntity.getBlockPos(), data);
    }

    public static TileControlPacket nbtControlPacket(int x, int y, int z, net.minecraft.nbt.CompoundTag data) {
        return nbtControlPacket(new BlockPos(x, y, z), data);
    }

    public static TileControlPacket nbtControlPacket(net.minecraft.nbt.CompoundTag data, int x, int y, int z) {
        return nbtControlPacket(x, y, z, data);
    }

    public static TileControlPacket tileControlPacket(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        return nbtControlPacket(pos, data);
    }

    public static TileControlPacket tileControlPacket(BlockEntity blockEntity, net.minecraft.nbt.CompoundTag data) {
        return nbtControlPacket(blockEntity, data);
    }

    public static void sendTileControl(int x, int y, int z, net.minecraft.nbt.CompoundTag data) {
        sendTileControl(new BlockPos(x, y, z), data);
    }

    public static void sendNbtControl(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        sendTileControl(pos, data);
    }

    public static void sendNbtControl(BlockEntity blockEntity, net.minecraft.nbt.CompoundTag data) {
        sendTileControl(blockEntity, data);
    }

    public static void sendNbtControl(net.minecraft.nbt.CompoundTag data, BlockEntity blockEntity) {
        sendTileControl(blockEntity, data);
    }

    public static void sendNbtControl(int x, int y, int z, net.minecraft.nbt.CompoundTag data) {
        sendTileControl(x, y, z, data);
    }

    public static void sendNbtControl(net.minecraft.nbt.CompoundTag data, int x, int y, int z) {
        sendTileControl(x, y, z, data);
    }

    public static void sendTypedTileAction(BlockPos pos, ResourceLocation actionType, int value,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToServer(typedTileActionPacket(pos, actionType, value, data));
    }

    public static ServerTileActionPacket typedTileActionPacket(BlockPos pos, ResourceLocation actionType, int value,
                                                               net.minecraft.nbt.CompoundTag data) {
        return new ServerTileActionPacket(pos, actionType, value, data);
    }

    public static ServerTileActionPacket typedTileActionPacket(BlockEntity blockEntity, ResourceLocation actionType,
                                                               int value, net.minecraft.nbt.CompoundTag data) {
        return typedTileActionPacket(blockEntity.getBlockPos(), actionType, value, data);
    }

    public static ServerTileActionPacket typedTileActionPacket(int x, int y, int z, ResourceLocation actionType,
                                                               int value, net.minecraft.nbt.CompoundTag data) {
        return typedTileActionPacket(new BlockPos(x, y, z), actionType, value, data);
    }

    public static void sendTypedTileAction(BlockEntity blockEntity, ResourceLocation actionType, int value,
                                           net.minecraft.nbt.CompoundTag data) {
        sendTypedTileAction(blockEntity.getBlockPos(), actionType, value, data);
    }

    public static void sendTypedTileAction(int x, int y, int z, ResourceLocation actionType, int value,
                                           net.minecraft.nbt.CompoundTag data) {
        sendTypedTileAction(new BlockPos(x, y, z), actionType, value, data);
    }

    public static void sendTypedTileAction(BlockPos pos, ResourceLocation actionType) {
        sendTypedTileAction(pos, actionType, 0, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendTypedTileAction(BlockPos pos, ResourceLocation actionType, int value) {
        sendTypedTileAction(pos, actionType, value, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendTypedTileAction(BlockEntity blockEntity, ResourceLocation actionType) {
        sendTypedTileAction(blockEntity.getBlockPos(), actionType);
    }

    public static void sendTypedTileAction(BlockEntity blockEntity, ResourceLocation actionType, int value) {
        sendTypedTileAction(blockEntity.getBlockPos(), actionType, value);
    }

    public static void sendTileBinaryControl(BlockPos pos, ResourceLocation channel,
                                             java.util.function.Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            sendTileBinaryControl(pos, channel, payload);
        } finally {
            buffer.release();
        }
    }

    public static void sendTileBinaryControl(BlockEntity blockEntity, ResourceLocation channel,
                                             java.util.function.Consumer<FriendlyByteBuf> writer) {
        sendTileBinaryControl(blockEntity.getBlockPos(), channel, writer);
    }

    public static void sendTileBinaryControl(BlockPos pos, ResourceLocation channel, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ServerTileBinaryControlPacket.MAX_PAYLOAD_BYTES) {
            sendToServer(serverTileBinaryControlPacket(pos, channel, safePayload));
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ServerTileBinaryControlChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            sendToServer(new ServerTileBinaryControlChunkPacket(
                    transferId,
                    pos,
                    channel,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)));
        }
    }

    public static void sendTileBinaryControl(BlockEntity blockEntity, ResourceLocation channel, byte[] payload) {
        sendTileBinaryControl(blockEntity.getBlockPos(), channel, payload);
    }

    public static ServerTileBinaryControlPacket serverTileBinaryControlPacket(BlockPos pos, ResourceLocation channel,
                                                                              byte[] payload) {
        return new ServerTileBinaryControlPacket(pos, channel, payload);
    }

    public static ServerTileBinaryControlPacket serverTileBinaryControlPacket(int x, int y, int z,
                                                                              ResourceLocation channel,
                                                                              byte[] payload) {
        return serverTileBinaryControlPacket(new BlockPos(x, y, z), channel, payload);
    }

    public static ServerTileBinaryControlPacket serverTileBinaryControlPacket(BlockEntity blockEntity,
                                                                              ResourceLocation channel,
                                                                              byte[] payload) {
        return serverTileBinaryControlPacket(blockEntity.getBlockPos(), channel, payload);
    }

    public static ServerTileBinaryControlPacket tileBinaryControlPacket(BlockPos pos, ResourceLocation channel,
                                                                        byte[] payload) {
        return serverTileBinaryControlPacket(pos, channel, payload);
    }

    public static ServerTileBinaryControlPacket tileBinaryControlPacket(BlockEntity blockEntity,
                                                                        ResourceLocation channel, byte[] payload) {
        return serverTileBinaryControlPacket(blockEntity, channel, payload);
    }

    public static void syncPermaData(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(permaSyncPacket(data), player);
    }

    public static void syncPermaData(ServerPlayer player) {
        syncPermaData(player, HbmPermaSyncData.writeForPlayer(player));
    }

    public static void sendPermaSync(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        syncPermaData(player, data);
    }

    public static void sendPermaSync(ServerPlayer player) {
        syncPermaData(player);
    }

    public static void syncPermaDataThreaded(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(permaSyncPacket(data), player);
    }

    public static void syncPermaDataThreaded(ServerPlayer player) {
        syncPermaDataThreaded(player, HbmPermaSyncData.writeForPlayer(player));
    }

    public static void sendPermaSyncThreaded(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        syncPermaDataThreaded(player, data);
    }

    public static void sendPermaSyncThreaded(ServerPlayer player) {
        syncPermaDataThreaded(player);
    }

    public static PermaSyncPacket permaSyncPacket(net.minecraft.nbt.CompoundTag data) {
        return new PermaSyncPacket(data);
    }

    public static PermaSyncPacket permaSyncPacket(ServerPlayer player) {
        return permaSyncPacket(player == null ? new net.minecraft.nbt.CompoundTag() : HbmPermaSyncData.writeForPlayer(player));
    }

    public static void sendExplosionKnockback(ServerPlayer player, Vec3 motion) {
        sendToPlayer(explosionKnockbackPacket(motion), player);
    }

    public static ExplosionKnockbackPacket explosionKnockbackPacket(Vec3 motion) {
        return new ExplosionKnockbackPacket(motion);
    }

    public static ExplosionKnockbackPacket explosionKnockbackPacket(double x, double y, double z) {
        return explosionKnockbackPacket(new Vec3(x, y, z));
    }

    public static void sendExplosionKnockback(ServerPlayer player, double x, double y, double z) {
        sendExplosionKnockback(player, new Vec3(x, y, z));
    }

    public static void sendCoordinateAction(InteractionHand hand, BlockPos pos, int action, int value, int frequency,
                                            net.minecraft.nbt.CompoundTag data) {
        sendToServer(coordinateActionPacket(hand, pos, action, value, frequency, data));
    }

    public static void sendCoordinateAction(BlockPos pos, int action, int value, int frequency,
                                            net.minecraft.nbt.CompoundTag data) {
        sendCoordinateAction(InteractionHand.MAIN_HAND, pos, action, value, frequency, data);
    }

    public static void syncClientBinaryData(ServerPlayer player, ResourceLocation channel, String name, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendToPlayer(clientBinaryDataPacket(channel, name, safePayload, false), player);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            sendToPlayer(new ClientBinaryDataChunkPacket(
                    transferId,
                    channel,
                    name,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), player);
        }
    }

    public static void syncClientBinaryDataThreaded(ServerPlayer player, ResourceLocation channel, String name,
                                                    byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            ThreadedPacketDispatcher.sendToPlayer(clientBinaryDataPacket(channel, name, safePayload, false), player);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            ThreadedPacketDispatcher.sendToPlayer(new ClientBinaryDataChunkPacket(
                    transferId,
                    channel,
                    name,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), player);
        }
    }

    public static void syncClientBinaryDataBatch(ServerPlayer player, ResourceLocation channel, Map<String, byte[]> payloads,
                                                 boolean clearFirst, boolean markReady) {
        if (clearFirst) {
            clearClientBinaryData(player, channel);
        }
        Map<String, byte[]> safePayloads = payloads == null ? Map.of() : payloads;
        safePayloads.forEach((name, payload) -> syncClientBinaryData(player, channel, name, payload));
        if (markReady) {
            markClientBinaryDataReady(player, channel);
        }
    }

    public static void syncClientBinaryDataBatchThreaded(ServerPlayer player, ResourceLocation channel,
                                                         Map<String, byte[]> payloads, boolean clearFirst,
                                                         boolean markReady) {
        if (clearFirst) {
            clearClientBinaryDataThreaded(player, channel);
        }
        Map<String, byte[]> safePayloads = payloads == null ? Map.of() : payloads;
        safePayloads.forEach((name, payload) -> syncClientBinaryDataThreaded(player, channel, name, payload));
        if (markReady) {
            markClientBinaryDataReadyThreaded(player, channel);
        }
    }

    public static void clearClientBinaryData(ServerPlayer player, ResourceLocation channel) {
        sendToPlayer(clientBinaryDataPacket(channel, "", new byte[0], true), player);
    }

    public static void clearClientBinaryDataThreaded(ServerPlayer player, ResourceLocation channel) {
        ThreadedPacketDispatcher.sendToPlayer(clientBinaryDataPacket(channel, "", new byte[0], true), player);
    }

    public static void markClientBinaryDataReady(ServerPlayer player, ResourceLocation channel) {
        sendToPlayer(clientBinaryDataReadyPacket(channel), player);
    }

    public static void markClientBinaryDataReadyThreaded(ServerPlayer player, ResourceLocation channel) {
        ThreadedPacketDispatcher.sendToPlayer(clientBinaryDataReadyPacket(channel), player);
    }

    public static ClientBinaryDataPacket clientBinaryDataPacket(ResourceLocation channel, String name, byte[] payload,
                                                               boolean clearChannel) {
        return new ClientBinaryDataPacket(channel, name, payload, clearChannel);
    }

    public static ClientBinaryDataReadyPacket clientBinaryDataReadyPacket(ResourceLocation channel) {
        return new ClientBinaryDataReadyPacket(channel);
    }

    public static ClientBinaryDataPacket serializableRecipePacket(String filename, byte[] fileBytes) {
        return clientBinaryDataPacket(LEGACY_SERIALIZABLE_RECIPE_CHANNEL, filename, fileBytes, false);
    }

    public static Optional<ClientBinaryDataPacket> serializableRecipePacket(File recipeFile) {
        return readSerializableRecipeFile(recipeFile)
                .map(recipe -> serializableRecipePacket(recipe.filename(), recipe.fileBytes()));
    }

    public static ClientBinaryDataPacket clearSerializableRecipesPacket() {
        return clientBinaryDataPacket(LEGACY_SERIALIZABLE_RECIPE_CHANNEL, "", new byte[0], true);
    }

    public static ClientBinaryDataReadyPacket serializableRecipeReinitPacket() {
        return clientBinaryDataReadyPacket(LEGACY_SERIALIZABLE_RECIPE_CHANNEL);
    }

    public static void sendSerializableRecipe(ServerPlayer player, String filename, byte[] fileBytes) {
        syncClientBinaryData(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL, filename, fileBytes);
    }

    public static void sendSerializableRecipeThreaded(ServerPlayer player, String filename, byte[] fileBytes) {
        syncClientBinaryDataThreaded(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL, filename, fileBytes);
    }

    public static void sendSerializableRecipe(ServerPlayer player, File recipeFile) {
        readSerializableRecipeFile(recipeFile)
                .ifPresent(recipe -> sendSerializableRecipe(player, recipe.filename(), recipe.fileBytes()));
    }

    public static void sendSerializableRecipeThreaded(ServerPlayer player, File recipeFile) {
        readSerializableRecipeFile(recipeFile)
                .ifPresent(recipe -> sendSerializableRecipeThreaded(player, recipe.filename(), recipe.fileBytes()));
    }

    public static void sendSerializableRecipes(ServerPlayer player, Map<String, byte[]> recipeFiles) {
        syncClientBinaryDataBatch(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL, recipeFiles, true, true);
    }

    public static void sendSerializableRecipesThreaded(ServerPlayer player, Map<String, byte[]> recipeFiles) {
        syncClientBinaryDataBatchThreaded(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL, recipeFiles, true, true);
    }

    public static void sendSerializableRecipeFiles(ServerPlayer player, Iterable<File> recipeFiles) {
        sendSerializableRecipes(player, readSerializableRecipeFiles(recipeFiles));
    }

    public static void sendSerializableRecipeFilesThreaded(ServerPlayer player, Iterable<File> recipeFiles) {
        sendSerializableRecipesThreaded(player, readSerializableRecipeFiles(recipeFiles));
    }

    public static void clearSerializableRecipes(ServerPlayer player) {
        clearClientBinaryData(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL);
    }

    public static void clearSerializableRecipesThreaded(ServerPlayer player) {
        clearClientBinaryDataThreaded(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL);
    }

    public static void sendSerializableRecipeReinit(ServerPlayer player) {
        markClientBinaryDataReady(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL);
    }

    public static void sendSerializableRecipeReinitThreaded(ServerPlayer player) {
        markClientBinaryDataReadyThreaded(player, LEGACY_SERIALIZABLE_RECIPE_CHANNEL);
    }

    private static Optional<LegacySerializableRecipeFile> readSerializableRecipeFile(File recipeFile) {
        if (recipeFile == null || !recipeFile.isFile()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new LegacySerializableRecipeFile(recipeFile.getName(), Files.readAllBytes(recipeFile.toPath())));
        } catch (IOException exception) {
            HbmNtm.LOGGER.warn("Unable to read legacy serializable recipe file '{}'.", recipeFile, exception);
            return Optional.empty();
        }
    }

    private static Map<String, byte[]> readSerializableRecipeFiles(Iterable<File> recipeFiles) {
        if (recipeFiles == null) {
            return Map.of();
        }
        Map<String, byte[]> recipes = new LinkedHashMap<>();
        for (File recipeFile : recipeFiles) {
            readSerializableRecipeFile(recipeFile)
                    .ifPresent(recipe -> recipes.put(recipe.filename(), recipe.fileBytes()));
        }
        return recipes;
    }

    public static void sendClientTileEvent(BlockEntity blockEntity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToTrackingChunk(clientTileEventPacket(blockEntity.getBlockPos(), eventType, data), blockEntity);
    }

    public static void sendClientTileEvent(Level level, BlockPos pos, ResourceLocation eventType,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToTrackingChunk(clientTileEventPacket(pos, eventType, data), level, pos);
    }

    public static void sendClientTileEventThreaded(BlockEntity blockEntity, ResourceLocation eventType,
                                                   net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToTrackingChunk(clientTileEventPacket(blockEntity.getBlockPos(), eventType, data), blockEntity);
    }

    public static void sendClientTileEventThreaded(Level level, BlockPos pos, ResourceLocation eventType,
                                                   net.minecraft.nbt.CompoundTag data) {
        if (level instanceof ServerLevel serverLevel) {
            ThreadedPacketDispatcher.sendToTrackingChunk(clientTileEventPacket(pos, eventType, data), serverLevel, pos);
        }
    }

    public static ClientTileEventPacket clientTileEventPacket(BlockPos pos, ResourceLocation eventType,
                                                              net.minecraft.nbt.CompoundTag data) {
        return new ClientTileEventPacket(pos, eventType, data);
    }

    public static ClientTileEventPacket clientTileEventPacket(int x, int y, int z, ResourceLocation eventType,
                                                              net.minecraft.nbt.CompoundTag data) {
        return clientTileEventPacket(new BlockPos(x, y, z), eventType, data);
    }

    public static void syncTileBinaryToTracking(HbmTileBinarySyncProvider provider, BlockEntity blockEntity) {
        syncTileBinaryToTracking(provider, blockEntity, provider.getClientTileBinarySyncChannel());
    }

    public static void syncTileBinaryToTracking(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                ResourceLocation channel) {
        sendClientTileBinaryData(blockEntity, channel, provider::writeClientTileBinaryData);
    }

    public static void syncTileBinaryToTrackingThreaded(HbmTileBinarySyncProvider provider, BlockEntity blockEntity) {
        syncTileBinaryToTrackingThreaded(provider, blockEntity, provider.getClientTileBinarySyncChannel());
    }

    public static void syncTileBinaryToTrackingThreaded(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                        ResourceLocation channel) {
        sendClientTileBinaryDataThreaded(blockEntity, channel, provider::writeClientTileBinaryData);
    }

    public static boolean syncTileBinaryToTrackingIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                            HbmTileBinarySyncState syncState) {
        return syncTileBinaryToTrackingIfChanged(provider, blockEntity, provider.getClientTileBinarySyncChannel(), syncState);
    }

    public static boolean syncTileBinaryToTrackingIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                            ResourceLocation channel, HbmTileBinarySyncState syncState) {
        byte[] payload = writeTileBinaryPayload(provider);
        Level level = blockEntity.getLevel();
        if (!shouldSendTileBinaryPayload(syncState, payload, level)) {
            return false;
        }
        sendClientTileBinaryData(level, blockEntity.getBlockPos(), channel, payload);
        return true;
    }

    public static void syncTileBinaryToPlayer(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                              ServerPlayer player) {
        syncTileBinaryToPlayer(provider, blockEntity, player, provider.getClientTileBinarySyncChannel());
    }

    public static void syncTileBinaryToPlayer(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                              ServerPlayer player, ResourceLocation channel) {
        sendClientTileBinaryData(player, blockEntity.getBlockPos(), channel, provider::writeClientTileBinaryData);
    }

    public static void syncTileBinaryToPlayerThreaded(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                      ServerPlayer player) {
        syncTileBinaryToPlayerThreaded(provider, blockEntity, player, provider.getClientTileBinarySyncChannel());
    }

    public static void syncTileBinaryToPlayerThreaded(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                      ServerPlayer player, ResourceLocation channel) {
        sendClientTileBinaryDataThreaded(player, blockEntity.getBlockPos(), channel, provider::writeClientTileBinaryData);
    }

    public static boolean syncTileBinaryToPlayerIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                          ServerPlayer player, HbmTileBinarySyncState syncState) {
        return syncTileBinaryToPlayerIfChanged(provider, blockEntity, player,
                provider.getClientTileBinarySyncChannel(), syncState);
    }

    public static boolean syncTileBinaryToPlayerIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                          ServerPlayer player, ResourceLocation channel,
                                                          HbmTileBinarySyncState syncState) {
        byte[] payload = writeTileBinaryPayload(provider);
        Level level = blockEntity.getLevel();
        if (!shouldSendTileBinaryPayload(syncState, payload, level)) {
            return false;
        }
        sendClientTileBinaryData(player, blockEntity.getBlockPos(), channel, payload);
        return true;
    }

    public static void syncTileBinaryAround(HbmTileBinarySyncProvider provider, BlockEntity blockEntity, double range) {
        syncTileBinaryAround(provider, blockEntity, provider.getClientTileBinarySyncChannel(), range);
    }

    public static void syncTileBinaryAround(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                            ResourceLocation channel, double range) {
        byte[] payload = writeTileBinaryPayload(provider);
        Level level = blockEntity.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = blockEntity.getBlockPos();
            sendClientTileBinaryDataAround(serverLevel, pos, channel, payload, range, false);
        }
    }

    public static boolean syncTileBinaryAroundIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                        double range, HbmTileBinarySyncState syncState) {
        return syncTileBinaryAroundIfChanged(provider, blockEntity, provider.getClientTileBinarySyncChannel(),
                range, syncState);
    }

    public static boolean syncTileBinaryAroundIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                        ResourceLocation channel, double range,
                                                        HbmTileBinarySyncState syncState) {
        byte[] payload = writeTileBinaryPayload(provider);
        Level level = blockEntity.getLevel();
        if (!shouldSendTileBinaryPayload(syncState, payload, level)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            sendClientTileBinaryDataAround(serverLevel, blockEntity.getBlockPos(), channel, payload, range, false);
            return true;
        }
        return false;
    }

    public static void syncTileBinaryAroundThreaded(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                    double range) {
        syncTileBinaryAroundThreaded(provider, blockEntity, provider.getClientTileBinarySyncChannel(), range);
    }

    public static void syncTileBinaryAroundThreaded(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                    ResourceLocation channel, double range) {
        byte[] payload = writeTileBinaryPayload(provider);
        Level level = blockEntity.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = blockEntity.getBlockPos();
            sendClientTileBinaryDataAround(serverLevel, pos, channel, payload, range, true);
        }
    }

    public static boolean syncTileBinaryAroundThreadedIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                                double range, HbmTileBinarySyncState syncState) {
        return syncTileBinaryAroundThreadedIfChanged(provider, blockEntity, provider.getClientTileBinarySyncChannel(),
                range, syncState);
    }

    public static boolean syncTileBinaryAroundThreadedIfChanged(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                                ResourceLocation channel, double range,
                                                                HbmTileBinarySyncState syncState) {
        byte[] payload = writeTileBinaryPayload(provider);
        Level level = blockEntity.getLevel();
        if (!shouldSendTileBinaryPayload(syncState, payload, level)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            sendClientTileBinaryDataAround(serverLevel, blockEntity.getBlockPos(), channel, payload, range, true);
            return true;
        }
        return false;
    }

    public static boolean networkPackNT(HbmTileBinarySyncProvider provider, BlockEntity blockEntity, int range,
                                        HbmTileBinarySyncState syncState) {
        return networkPackNT(provider, blockEntity, (double) range, syncState);
    }

    public static boolean networkPackNT(HbmTileBinarySyncProvider provider, BlockEntity blockEntity, double range,
                                        HbmTileBinarySyncState syncState) {
        return syncTileBinaryAroundThreadedIfChanged(provider, blockEntity, range, syncState);
    }

    public static boolean networkPackNT(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                        ResourceLocation channel, double range, HbmTileBinarySyncState syncState) {
        return syncTileBinaryAroundThreadedIfChanged(provider, blockEntity, channel, range, syncState);
    }

    public static boolean networkPackNT(BlockEntity blockEntity, int range) {
        return networkPackNT(blockEntity, (double) range);
    }

    public static boolean networkPackNT(BlockEntity blockEntity, double range) {
        if (blockEntity instanceof HbmTileBinarySyncProvider provider) {
            return provider.networkPackNT(range);
        }
        return false;
    }

    public static boolean networkPackNT(BlockEntity blockEntity, ResourceLocation channel, int range) {
        return networkPackNT(blockEntity, channel, (double) range);
    }

    public static boolean networkPackNT(BlockEntity blockEntity, ResourceLocation channel, double range) {
        if (blockEntity instanceof HbmTileBinarySyncProvider provider) {
            return provider.networkPackNT(channel, range);
        }
        return false;
    }

    public static boolean networkPackNT(BlockEntity blockEntity, double range, HbmTileBinarySyncState syncState) {
        if (blockEntity instanceof HbmTileBinarySyncProvider provider) {
            return networkPackNT(provider, blockEntity, range, syncState);
        }
        return false;
    }

    public static boolean networkPackNT(BlockEntity blockEntity, int range, HbmTileBinarySyncState syncState) {
        return networkPackNT(blockEntity, (double) range, syncState);
    }

    public static void sendBufPacket(BlockEntity blockEntity) {
        if (blockEntity instanceof HbmLegacyBufPacketReceiver receiver) {
            syncTileBinaryToTracking(receiver, blockEntity, receiver.getClientTileBinarySyncChannel());
        }
    }

    public static void sendBufPacketThreaded(BlockEntity blockEntity) {
        if (blockEntity instanceof HbmLegacyBufPacketReceiver receiver) {
            syncTileBinaryToTrackingThreaded(receiver, blockEntity, receiver.getClientTileBinarySyncChannel());
        }
    }

    public static void sendBufPacket(BlockEntity blockEntity, int range) {
        sendBufPacket(blockEntity, (double) range);
    }

    public static void sendBufPacket(BlockEntity blockEntity, double range) {
        if (blockEntity instanceof HbmLegacyBufPacketReceiver receiver) {
            syncTileBinaryAround(receiver, blockEntity, receiver.getClientTileBinarySyncChannel(), range);
        }
    }

    public static void sendBufPacketThreaded(BlockEntity blockEntity, int range) {
        sendBufPacketThreaded(blockEntity, (double) range);
    }

    public static void sendBufPacketThreaded(BlockEntity blockEntity, double range) {
        if (blockEntity instanceof HbmLegacyBufPacketReceiver receiver) {
            syncTileBinaryAroundThreaded(receiver, blockEntity, receiver.getClientTileBinarySyncChannel(), range);
        }
    }

    public static void sendBufPacket(ServerPlayer player, BlockEntity blockEntity) {
        if (blockEntity instanceof HbmLegacyBufPacketReceiver receiver) {
            syncTileBinaryToPlayer(receiver, blockEntity, player, receiver.getClientTileBinarySyncChannel());
        }
    }

    public static void sendBufPacketThreaded(ServerPlayer player, BlockEntity blockEntity) {
        if (blockEntity instanceof HbmLegacyBufPacketReceiver receiver) {
            syncTileBinaryToPlayerThreaded(receiver, blockEntity, player, receiver.getClientTileBinarySyncChannel());
        }
    }

    public static void syncMissileMultipart(BlockEntity blockEntity, MissileMultipartSnapshot multipart) {
        sendToTrackingChunk(missileMultipartPacket(blockEntity.getBlockPos(), multipart), blockEntity);
    }

    public static void syncMissileMultipart(BlockEntity blockEntity, ItemStack warhead, ItemStack fuselage,
                                            ItemStack fins, ItemStack thruster) {
        syncMissileMultipart(blockEntity, MissileMultipartSnapshot.of(warhead, fuselage, fins, thruster));
    }

    public static void syncMissileMultipart(Level level, BlockPos pos, MissileMultipartSnapshot multipart) {
        sendToTrackingChunk(missileMultipartPacket(pos, multipart), level, pos);
    }

    public static void syncMissileMultipart(ServerPlayer player, BlockPos pos, MissileMultipartSnapshot multipart) {
        sendToPlayer(missileMultipartPacket(pos, multipart), player);
    }

    public static void syncMissileMultipartThreaded(BlockEntity blockEntity, MissileMultipartSnapshot multipart) {
        ThreadedPacketDispatcher.sendToTrackingChunk(missileMultipartPacket(blockEntity.getBlockPos(), multipart), blockEntity);
    }

    public static void syncMissileMultipartThreaded(BlockEntity blockEntity, ItemStack warhead, ItemStack fuselage,
                                                    ItemStack fins, ItemStack thruster) {
        syncMissileMultipartThreaded(blockEntity, MissileMultipartSnapshot.of(warhead, fuselage, fins, thruster));
    }

    public static void syncMissileMultipartThreaded(Level level, BlockPos pos, MissileMultipartSnapshot multipart) {
        if (level instanceof ServerLevel serverLevel) {
            ThreadedPacketDispatcher.sendToTrackingChunk(missileMultipartPacket(pos, multipart), serverLevel, pos);
        }
    }

    public static void syncMissileMultipartThreaded(ServerPlayer player, BlockPos pos, MissileMultipartSnapshot multipart) {
        ThreadedPacketDispatcher.sendToPlayer(missileMultipartPacket(pos, multipart), player);
    }

    public static ClientMissileMultipartPacket missileMultipartPacket(BlockPos pos, MissileMultipartSnapshot multipart) {
        return new ClientMissileMultipartPacket(pos, multipart);
    }

    public static ClientMissileMultipartPacket missileMultipartPacket(BlockPos pos, ItemStack warhead,
                                                                      ItemStack fuselage, ItemStack fins,
                                                                      ItemStack thruster) {
        return missileMultipartPacket(pos, MissileMultipartSnapshot.of(warhead, fuselage, fins, thruster));
    }

    public static ClientMissileMultipartPacket teMissileMultipartPacket(int x, int y, int z,
                                                                        MissileMultipartSnapshot multipart) {
        return missileMultipartPacket(new BlockPos(x, y, z), multipart);
    }

    public static void sendClientTileEvent(ServerPlayer player, BlockPos pos, ResourceLocation eventType,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(clientTileEventPacket(pos, eventType, data), player);
    }

    public static void sendVaultDoorEvent(BlockEntity blockEntity, boolean opening, int state, boolean resetClientTime, int type) {
        sendToTrackingChunk(vaultDoorPacket(blockEntity.getBlockPos(), opening, state, resetClientTime, type), blockEntity);
    }

    public static void sendVaultDoorEvent(Level level, BlockPos pos, boolean opening, int state,
                                          boolean resetClientTime, int type) {
        sendToTrackingChunk(vaultDoorPacket(pos, opening, state, resetClientTime, type), level, pos);
    }

    public static ClientTileEventPacket vaultDoorPacket(BlockPos pos, boolean opening, int state,
                                                        boolean resetClientTime, int type) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putBoolean("opening", opening);
        data.putInt("state", state);
        data.putBoolean("resetClientTime", resetClientTime);
        data.putInt("type", type);
        return clientTileEventPacket(pos, HbmNetworkActions.VAULT_DOOR, data);
    }

    public static ClientTileEventPacket vaultDoorPacket(int x, int y, int z, boolean opening, int state,
                                                        boolean resetClientTime, int type) {
        return vaultDoorPacket(new BlockPos(x, y, z), opening, state, resetClientTime, type);
    }

    public static ClientTileEventPacket teVaultPacket(BlockPos pos, boolean opening, int state, long sysTime, int type) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putBoolean("opening", opening);
        data.putInt("state", state);
        data.putLong("sysTime", sysTime);
        data.putBoolean("resetClientTime", sysTime == 1L);
        data.putInt("type", type);
        return clientTileEventPacket(pos, HbmNetworkActions.VAULT_DOOR, data);
    }

    public static ClientTileEventPacket teVaultPacket(int x, int y, int z, boolean opening, int state,
                                                      long sysTime, int type) {
        return teVaultPacket(new BlockPos(x, y, z), opening, state, sysTime, type);
    }

    public static void sendVaultDoorEvent(Level level, int x, int y, int z, boolean opening, int state,
                                          boolean resetClientTime, int type) {
        sendVaultDoorEvent(level, new BlockPos(x, y, z), opening, state, resetClientTime, type);
    }

    public static void sendSirenEvent(BlockEntity blockEntity, int trackId, boolean active) {
        sendToTrackingChunk(sirenPacket(blockEntity.getBlockPos(), trackId, active), blockEntity);
    }

    public static ClientTileEventPacket sirenPacket(BlockPos pos, int trackId, boolean active) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("trackId", trackId);
        data.putBoolean("active", active);
        return clientTileEventPacket(pos, HbmNetworkActions.SIREN, data);
    }

    public static ClientTileEventPacket sirenPacket(int x, int y, int z, int trackId, boolean active) {
        return sirenPacket(new BlockPos(x, y, z), trackId, active);
    }

    public static ClientTileEventPacket teSirenPacket(BlockPos pos, int trackId, boolean active) {
        return sirenPacket(pos, trackId, active);
    }

    public static ClientTileEventPacket teSirenPacket(int x, int y, int z, int trackId, boolean active) {
        return sirenPacket(x, y, z, trackId, active);
    }

    public static void sendSirenEvent(Level level, BlockPos pos, int trackId, boolean active) {
        sendToTrackingChunk(sirenPacket(pos, trackId, active), level, pos);
    }

    public static void sendSirenEvent(Level level, int x, int y, int z, int trackId, boolean active) {
        sendSirenEvent(level, new BlockPos(x, y, z), trackId, active);
    }

    public static void syncForceFieldState(BlockEntity blockEntity, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        sendToTrackingChunk(forceFieldPacket(blockEntity.getBlockPos(), radius, health, maxHealth,
                power, active, color, cooldown), blockEntity);
    }

    public static TileSyncPacket forceFieldPacket(BlockPos pos, float radius, int health, int maxHealth,
                                                  int power, boolean active, int color, int cooldown) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putFloat("radius", radius);
        data.putInt("health", health);
        data.putInt("maxHealth", maxHealth);
        data.putInt("power", power);
        data.putBoolean("active", active);
        data.putInt("color", color);
        data.putInt("cooldown", cooldown);
        return new TileSyncPacket(pos, data);
    }

    public static TileSyncPacket forceFieldPacket(int x, int y, int z, float radius, int health, int maxHealth,
                                                  int power, boolean active, int color, int cooldown) {
        return forceFieldPacket(new BlockPos(x, y, z), radius, health, maxHealth, power, active, color, cooldown);
    }

    public static TileSyncPacket teffPacket(BlockPos pos, float radius, int health, int maxHealth,
                                            int power, boolean active, int color, int cooldown) {
        return forceFieldPacket(pos, radius, health, maxHealth, power, active, color, cooldown);
    }

    public static TileSyncPacket teffPacket(int x, int y, int z, float radius, int health, int maxHealth,
                                            int power, boolean active, int color, int cooldown) {
        return forceFieldPacket(x, y, z, radius, health, maxHealth, power, active, color, cooldown);
    }

    public static void syncForceFieldState(Level level, BlockPos pos, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        sendToTrackingChunk(forceFieldPacket(pos, radius, health, maxHealth, power, active, color, cooldown), level, pos);
    }

    public static void syncForceFieldState(Level level, int x, int y, int z, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        syncForceFieldState(level, new BlockPos(x, y, z), radius, health, maxHealth, power, active, color, cooldown);
    }

    public static void sendForceFieldState(BlockEntity blockEntity, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        syncForceFieldState(blockEntity, radius, health, maxHealth, power, active, color, cooldown);
    }

    public static void sendForceFieldState(Level level, BlockPos pos, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        syncForceFieldState(level, pos, radius, health, maxHealth, power, active, color, cooldown);
    }

    public static void sendForceFieldState(Level level, int x, int y, int z, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        syncForceFieldState(level, x, y, z, radius, health, maxHealth, power, active, color, cooldown);
    }

    public static void sendClientTileBinaryData(BlockEntity blockEntity, ResourceLocation channel,
                                                java.util.function.Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            sendClientTileBinaryData(blockEntity, channel, payload);
        } finally {
            buffer.release();
        }
    }

    public static void sendClientTileBinaryData(ServerPlayer player, BlockPos pos, ResourceLocation channel,
                                                java.util.function.Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            sendClientTileBinaryData(player, pos, channel, payload);
        } finally {
            buffer.release();
        }
    }

    public static void sendClientTileBinaryDataThreaded(BlockEntity blockEntity, ResourceLocation channel,
                                                        java.util.function.Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            sendClientTileBinaryDataThreaded(blockEntity, channel, payload);
        } finally {
            buffer.release();
        }
    }

    public static void sendClientTileBinaryDataThreaded(ServerPlayer player, BlockPos pos, ResourceLocation channel,
                                                        java.util.function.Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            writer.accept(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            sendClientTileBinaryDataThreaded(player, pos, channel, payload);
        } finally {
            buffer.release();
        }
    }

    public static void sendClientTileBinaryData(BlockEntity blockEntity, ResourceLocation channel, byte[] payload) {
        sendClientTileBinaryData(blockEntity.getLevel(), blockEntity.getBlockPos(), channel, payload);
    }

    public static void sendClientTileBinaryDataThreaded(BlockEntity blockEntity, ResourceLocation channel, byte[] payload) {
        sendClientTileBinaryDataThreaded(blockEntity.getLevel(), blockEntity.getBlockPos(), channel, payload);
    }

    public static void sendClientTileBinaryData(ServerPlayer player, BlockPos pos, ResourceLocation channel, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendToPlayer(clientTileBinaryDataPacket(pos, channel, safePayload), player);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientTileBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            sendToPlayer(new ClientTileBinaryDataChunkPacket(
                    transferId,
                    pos,
                    channel,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), player);
        }
    }

    public static void sendClientTileBinaryDataThreaded(ServerPlayer player, BlockPos pos,
                                                        ResourceLocation channel, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            ThreadedPacketDispatcher.sendToPlayer(clientTileBinaryDataPacket(pos, channel, safePayload), player);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientTileBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            ThreadedPacketDispatcher.sendToPlayer(new ClientTileBinaryDataChunkPacket(
                    transferId,
                    pos,
                    channel,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), player);
        }
    }

    public static void sendClientTileBinaryData(Level level, BlockPos pos, ResourceLocation channel, byte[] payload) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendToTrackingChunk(clientTileBinaryDataPacket(pos, channel, safePayload), level, pos);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientTileBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            sendToTrackingChunk(new ClientTileBinaryDataChunkPacket(
                    transferId,
                    pos,
                    channel,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), level, pos);
        }
    }

    public static void sendClientTileBinaryDataThreaded(Level level, BlockPos pos,
                                                        ResourceLocation channel, byte[] payload) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            ThreadedPacketDispatcher.sendToTrackingChunk(clientTileBinaryDataPacket(pos, channel, safePayload),
                    serverLevel, pos);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientTileBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            ThreadedPacketDispatcher.sendToTrackingChunk(new ClientTileBinaryDataChunkPacket(
                    transferId,
                    pos,
                    channel,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), serverLevel, pos);
        }
    }

    public static ClientTileBinaryDataPacket clientTileBinaryDataPacket(BlockPos pos, ResourceLocation channel,
                                                                        byte[] payload) {
        return new ClientTileBinaryDataPacket(pos, channel, payload);
    }

    public static ClientTileBinaryDataPacket bufPacket(BlockPos pos, ResourceLocation channel, byte[] payload) {
        return clientTileBinaryDataPacket(pos, channel, payload);
    }

    public static ClientTileBinaryDataPacket bufPacket(BlockPos pos, byte[] payload) {
        return bufPacket(pos, HbmNetworkActions.BUF_PACKET, payload);
    }

    public static ClientTileBinaryDataPacket bufPacket(int x, int y, int z, byte[] payload) {
        return bufPacket(new BlockPos(x, y, z), payload);
    }

    public static ClientTileBinaryDataPacket bufPacket(BlockEntity blockEntity, HbmTileBinarySyncProvider provider,
                                                       ResourceLocation channel) {
        return bufPacket(blockEntity.getBlockPos(), channel, writeTileBinaryPayload(provider));
    }

    public static ClientTileBinaryDataPacket bufPacket(BlockEntity blockEntity, HbmTileBinarySyncProvider provider) {
        return bufPacket(blockEntity, provider, provider.getClientTileBinarySyncChannel());
    }

    public static void syncClientPanelData(ServerPlayer player, ResourceLocation panelType, int legacyType,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(clientPanelDataPacket(panelType, legacyType, data), player);
    }

    public static void syncClientPanelDataThreaded(ServerPlayer player, ResourceLocation panelType, int legacyType,
                                                   net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(clientPanelDataPacket(panelType, legacyType, data), player);
    }

    public static void syncSatellitePanelData(ServerPlayer player, int legacyType, net.minecraft.nbt.CompoundTag data) {
        syncClientPanelData(player, HbmNetworkActions.SATELLITE_PANEL, legacyType, data);
    }

    public static void syncSatellitePanelDataThreaded(ServerPlayer player, int legacyType,
                                                      net.minecraft.nbt.CompoundTag data) {
        syncClientPanelDataThreaded(player, HbmNetworkActions.SATELLITE_PANEL, legacyType, data);
    }

    public static void sendSatPanel(ServerPlayer player, int legacyType, net.minecraft.nbt.CompoundTag data) {
        syncSatellitePanelData(player, legacyType, data);
    }

    public static void sendSatPanelThreaded(ServerPlayer player, int legacyType, net.minecraft.nbt.CompoundTag data) {
        syncSatellitePanelDataThreaded(player, legacyType, data);
    }

    public static void sendSatellitePanel(ServerPlayer player, int legacyType, net.minecraft.nbt.CompoundTag data) {
        syncSatellitePanelData(player, legacyType, data);
    }

    public static void sendSatellitePanelThreaded(ServerPlayer player, int legacyType,
                                                  net.minecraft.nbt.CompoundTag data) {
        syncSatellitePanelDataThreaded(player, legacyType, data);
    }

    public static ClientPanelDataPacket clientPanelDataPacket(ResourceLocation panelType, int legacyType,
                                                              net.minecraft.nbt.CompoundTag data) {
        return new ClientPanelDataPacket(panelType, legacyType, data);
    }

    public static ClientPanelDataPacket satellitePanelDataPacket(int legacyType,
                                                                 net.minecraft.nbt.CompoundTag data) {
        return clientPanelDataPacket(HbmNetworkActions.SATELLITE_PANEL, legacyType, data);
    }

    public static ClientPanelDataPacket satPanelPacket(int legacyType, net.minecraft.nbt.CompoundTag data) {
        return satellitePanelDataPacket(legacyType, data);
    }

    public static ClientPanelDataPacket satellitePanelPacket(int legacyType, net.minecraft.nbt.CompoundTag data) {
        return satellitePanelDataPacket(legacyType, data);
    }

    public static void sendPlayerRadiation(ServerPlayer player, HbmLivingProperties.SyncData data) {
        sendToPlayer(playerRadiationPacket(data), player);
    }

    public static void sendPlayerRadiationThreaded(ServerPlayer player, HbmLivingProperties.SyncData data) {
        ThreadedPacketDispatcher.sendToPlayer(playerRadiationPacket(data), player);
    }

    public static void syncPlayerRadiation(ServerPlayer player, float chunkRadiation, float resistance) {
        if (player == null) {
            return;
        }
        sendPlayerRadiation(player, HbmLivingProperties.writeSyncedData(player, chunkRadiation, resistance));
    }

    public static void syncPlayerRadiationThreaded(ServerPlayer player, float chunkRadiation, float resistance) {
        if (player == null) {
            return;
        }
        sendPlayerRadiationThreaded(player, HbmLivingProperties.writeSyncedData(player, chunkRadiation, resistance));
    }

    public static void sendExtProperties(ServerPlayer player, HbmExtendedProperties.SyncData data) {
        sendToPlayer(extPropertiesPacket(data), player);
    }

    public static void sendExtPropertiesThreaded(ServerPlayer player, HbmExtendedProperties.SyncData data) {
        ThreadedPacketDispatcher.sendToPlayer(extPropertiesPacket(data), player);
    }

    public static void syncExtendedProperties(ServerPlayer player, float chunkRadiation, float resistance) {
        if (player == null) {
            return;
        }
        sendExtProperties(player, HbmExtendedProperties.writeSyncedData(player, chunkRadiation, resistance));
    }

    public static void syncExtendedPropertiesThreaded(ServerPlayer player, float chunkRadiation, float resistance) {
        if (player == null) {
            return;
        }
        sendExtPropertiesThreaded(player, HbmExtendedProperties.writeSyncedData(player, chunkRadiation, resistance));
    }

    public static void sendExtPropPacket(ServerPlayer player, float chunkRadiation, float resistance) {
        syncExtendedProperties(player, chunkRadiation, resistance);
    }

    public static void sendExtPropPacketThreaded(ServerPlayer player, float chunkRadiation, float resistance) {
        syncExtendedPropertiesThreaded(player, chunkRadiation, resistance);
    }

    public static void syncPlayerProperties(ServerPlayer player, ResourceLocation dataType, net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(playerPropertiesPacket(dataType, data), player);
    }

    public static void syncPlayerProperties(ServerPlayer player) {
        syncPlayerProperties(player, HbmPlayerProperties.DATA_TYPE, HbmPlayerProperties.writeSyncedData(player));
    }

    public static void syncPlayerPropertiesThreaded(ServerPlayer player, ResourceLocation dataType,
                                                    net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(playerPropertiesPacket(dataType, data), player);
    }

    public static void syncPlayerPropertiesThreaded(ServerPlayer player) {
        syncPlayerPropertiesThreaded(player, HbmPlayerProperties.DATA_TYPE, HbmPlayerProperties.writeSyncedData(player));
    }

    public static void syncPlayerPropertiesBatch(ServerPlayer player,
                                                 Map<ResourceLocation, net.minecraft.nbt.CompoundTag> properties) {
        Map<ResourceLocation, net.minecraft.nbt.CompoundTag> safeProperties = properties == null ? Map.of() : properties;
        safeProperties.forEach((dataType, data) -> syncPlayerProperties(player, dataType, data));
    }

    public static PlayerRadiationSyncPacket playerRadiationPacket(HbmLivingProperties.SyncData data) {
        return new PlayerRadiationSyncPacket(data);
    }

    public static PlayerRadiationSyncPacket playerRadiationPacket(ServerPlayer player, float chunkRadiation,
                                                                  float resistance) {
        return playerRadiationPacket(HbmLivingProperties.writeSyncedData(player, chunkRadiation, resistance));
    }

    public static ExtPropertiesSyncPacket extPropertiesPacket(HbmExtendedProperties.SyncData data) {
        return new ExtPropertiesSyncPacket(data);
    }

    public static ExtPropertiesSyncPacket extPropertiesPacket(ServerPlayer player, float chunkRadiation,
                                                              float resistance) {
        return extPropertiesPacket(HbmExtendedProperties.writeSyncedData(player, chunkRadiation, resistance));
    }

    public static ExtPropertiesSyncPacket extPropPacket(ServerPlayer player, float chunkRadiation, float resistance) {
        return extPropertiesPacket(player, chunkRadiation, resistance);
    }

    public static ExtPropertiesSyncPacket extPropPacket(HbmExtendedProperties.SyncData data) {
        return extPropertiesPacket(data);
    }

    public static PlayerPropertiesPacket playerPropertiesPacket(ResourceLocation dataType,
                                                                net.minecraft.nbt.CompoundTag data) {
        return new PlayerPropertiesPacket(dataType, data);
    }

    public static PlayerPropertiesPacket playerPropertiesPacket(ServerPlayer player) {
        return playerPropertiesPacket(HbmPlayerProperties.DATA_TYPE, HbmPlayerProperties.writeSyncedData(player));
    }

    public static void sendMenuAction(int action, int value, net.minecraft.nbt.CompoundTag data) {
        sendToServer(menuActionPacket(action, value, data));
    }

    public static MenuActionPacket menuActionPacket(int action, int value, net.minecraft.nbt.CompoundTag data) {
        return new MenuActionPacket(action, value, data);
    }

    public static void sendMenuAction(int action, int value) {
        sendMenuAction(action, value, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendMenuAction(int action) {
        sendMenuAction(action, 0, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendTypedMenuAction(ResourceLocation actionType, int value, net.minecraft.nbt.CompoundTag data) {
        sendToServer(typedMenuActionPacket(actionType, value, data));
    }

    public static TypedMenuActionPacket typedMenuActionPacket(ResourceLocation actionType, int value,
                                                              net.minecraft.nbt.CompoundTag data) {
        return new TypedMenuActionPacket(actionType, value, data);
    }

    public static void sendTypedMenuAction(ResourceLocation actionType, int value) {
        sendTypedMenuAction(actionType, value, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendTypedMenuAction(ResourceLocation actionType) {
        sendTypedMenuAction(actionType, 0, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendAnvilCraftAction(int recipeIndex, int mode) {
        sendToServer(anvilCraftPacket(recipeIndex, mode));
    }

    public static TypedMenuActionPacket anvilCraftPacket(int recipeIndex, int mode) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("recipeIndex", recipeIndex);
        data.putInt("mode", mode);
        return typedMenuActionPacket(HbmNetworkActions.ANVIL_CRAFT, recipeIndex, data);
    }

    public static TypedMenuActionPacket anvilCraftPacket(int recipeIndex) {
        return anvilCraftPacket(recipeIndex, 0);
    }

    public static void sendAnvilCraft(int recipeIndex, int mode) {
        sendAnvilCraftAction(recipeIndex, mode);
    }

    public static void sendAnvilCraft(int recipeIndex) {
        sendAnvilCraftAction(recipeIndex, 0);
    }

    public static void sendAnvilCraftPacket(int recipeIndex, int mode) {
        sendAnvilCraftAction(recipeIndex, mode);
    }

    public static void sendAnvilCraftPacket(int recipeIndex) {
        sendAnvilCraftAction(recipeIndex, 0);
    }

    public static void syncClientBiome(ServerPlayer player, int blockX, int blockZ, short biome) {
        sendToPlayer(biomeSyncPacket(blockX, blockZ, biome), player);
    }

    public static ClientBiomeSyncPacket biomeSyncPacket(int blockX, int blockZ, byte biome) {
        return biomeSyncPacket(blockX, blockZ, (short) biome);
    }

    public static ClientBiomeSyncPacket biomeSyncPacket(int blockX, int blockZ, short biome) {
        return ClientBiomeSyncPacket.single(blockX, blockZ, biome);
    }

    public static ClientBiomeSyncPacket biomeSyncChunkPacket(int chunkX, int chunkZ, byte[] biomeArray) {
        if (biomeArray == null) {
            return ClientBiomeSyncPacket.chunk(chunkX, chunkZ, null);
        }
        short[] biomes = new short[biomeArray.length];
        for (int i = 0; i < biomeArray.length; i++) {
            biomes[i] = (short) biomeArray[i];
        }
        return biomeSyncChunkPacket(chunkX, chunkZ, biomes);
    }

    public static ClientBiomeSyncPacket biomeSyncChunkPacket(int chunkX, int chunkZ, short[] biomeArray) {
        return ClientBiomeSyncPacket.chunk(chunkX, chunkZ, biomeArray);
    }

    public static void syncClientBiome(ServerLevel level, int blockX, int blockZ, short biome, double range) {
        sendToAllAround(biomeSyncPacket(blockX, blockZ, biome),
                level, blockX, legacyBiomeSyncY(level), blockZ, range);
    }

    public static void syncClientBiomeChunk(ServerPlayer player, int chunkX, int chunkZ, short[] biomeArray) {
        sendToPlayer(biomeSyncChunkPacket(chunkX, chunkZ, biomeArray), player);
    }

    public static void syncClientBiomeChunk(ServerPlayer player, int chunkX, int chunkZ, byte[] biomeArray) {
        sendToPlayer(biomeSyncChunkPacket(chunkX, chunkZ, biomeArray), player);
    }

    public static void syncClientBiomeChunk(ServerLevel level, int chunkX, int chunkZ, short[] biomeArray, double range) {
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        sendToAllAround(biomeSyncChunkPacket(chunkX, chunkZ, biomeArray),
                level, centerX, legacyBiomeSyncY(level), centerZ, range);
    }

    public static void syncClientBiomeChunk(ServerLevel level, int chunkX, int chunkZ, byte[] biomeArray, double range) {
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        sendToAllAround(biomeSyncChunkPacket(chunkX, chunkZ, biomeArray),
                level, centerX, legacyBiomeSyncY(level), centerZ, range);
    }

    public static void sendParticleBurst(ServerLevel level, BlockPos pos, BlockState state, double range) {
        if (pos == null || state == null) {
            return;
        }
        sendToAllAround(particleBurstPacket(pos, state), level,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range);
    }

    public static ParticleBurstPacket particleBurstPacket(BlockPos pos, BlockState state) {
        return new ParticleBurstPacket(pos, state);
    }

    public static ParticleBurstPacket particleBurstPacket(int x, int y, int z, BlockState state) {
        return particleBurstPacket(new BlockPos(x, y, z), state);
    }

    public static void sendParticleBurst(ServerLevel level, int x, int y, int z, BlockState state, double range) {
        sendParticleBurst(level, new BlockPos(x, y, z), state, range);
    }

    public static void sendParticleBurstThreaded(ServerLevel level, BlockPos pos, BlockState state, double range) {
        if (pos == null || state == null) {
            return;
        }
        ThreadedPacketDispatcher.sendToAllAround(particleBurstPacket(pos, state),
                level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, range);
    }

    public static void sendParticleBurstThreaded(ServerLevel level, int x, int y, int z, BlockState state, double range) {
        sendParticleBurstThreaded(level, new BlockPos(x, y, z), state, range);
    }

    public static void sendCompressedExplosionEffect(ServerLevel level, Vec3 center, float size, List<BlockPos> affectedBlocks, double range) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        sendToAllAround(compressedExplosionEffectPacket(safeCenter, size, affectedBlocks),
                level, safeCenter.x, safeCenter.y, safeCenter.z, range);
    }

    public static CompressedExplosionEffectPacket compressedExplosionEffectPacket(Vec3 center, float size,
                                                                                 List<BlockPos> affectedBlocks) {
        return new CompressedExplosionEffectPacket(center, size, affectedBlocks);
    }

    public static CompressedExplosionEffectPacket compressedExplosionEffectPacket(double x, double y, double z,
                                                                                 float size,
                                                                                 List<BlockPos> affectedBlocks) {
        return compressedExplosionEffectPacket(new Vec3(x, y, z), size, affectedBlocks);
    }

    public static CompressedExplosionEffectPacket explosionEffectPacket(Vec3 center, float size,
                                                                       List<BlockPos> affectedBlocks) {
        return compressedExplosionEffectPacket(center, size, affectedBlocks);
    }

    public static void sendCompressedExplosionEffect(ServerLevel level, double x, double y, double z, float size,
                                                     List<BlockPos> affectedBlocks, double range) {
        sendCompressedExplosionEffect(level, new Vec3(x, y, z), size, affectedBlocks, range);
    }

    public static void sendCompressedExplosionEffectThreaded(ServerLevel level, Vec3 center, float size,
                                                             List<BlockPos> affectedBlocks, double range) {
        Vec3 safeCenter = center == null ? Vec3.ZERO : center;
        ThreadedPacketDispatcher.sendToAllAround(compressedExplosionEffectPacket(safeCenter, size, affectedBlocks),
                level, safeCenter.x, safeCenter.y, safeCenter.z, range);
    }

    public static void sendCompressedExplosionEffectThreaded(ServerLevel level, double x, double y, double z,
                                                             float size, List<BlockPos> affectedBlocks, double range) {
        sendCompressedExplosionEffectThreaded(level, new Vec3(x, y, z), size, affectedBlocks, range);
    }

    private static void sendClientTileBinaryDataAround(ServerLevel level, BlockPos pos, ResourceLocation channel,
                                                       byte[] payload, double range, boolean threaded) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendTileBinaryMessageAround(new ClientTileBinaryDataPacket(pos, channel, safePayload), level, pos, range, threaded);
            return;
        }
        UUID transferId = UUID.randomUUID();
        int chunkSize = ClientTileBinaryDataChunkPacket.MAX_CHUNK_BYTES;
        int chunkCount = (safePayload.length + chunkSize - 1) / chunkSize;
        for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
            int start = chunkIndex * chunkSize;
            int end = Math.min(start + chunkSize, safePayload.length);
            sendTileBinaryMessageAround(new ClientTileBinaryDataChunkPacket(
                    transferId,
                    pos,
                    channel,
                    chunkIndex,
                    chunkCount,
                    Arrays.copyOfRange(safePayload, start, end)), level, pos, range, threaded);
        }
    }

    private static void sendTileBinaryMessageAround(Object message, ServerLevel level, BlockPos pos,
                                                    double range, boolean threaded) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        if (threaded) {
            ThreadedPacketDispatcher.sendToAllAround(message, level, x, y, z, range);
        } else {
            sendToAllAround(message, level, x, y, z, range);
        }
    }

    private static byte[] writeTileBinaryPayload(HbmTileBinarySyncProvider provider) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            provider.writeClientTileBinaryData(buffer);
            byte[] payload = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), payload);
            return payload;
        } finally {
            buffer.release();
        }
    }

    private static boolean shouldSendTileBinaryPayload(HbmTileBinarySyncState syncState, byte[] payload, Level level) {
        if (level == null || level.isClientSide) {
            return false;
        }
        boolean sent = syncState == null || syncState.shouldSend(payload, level.getGameTime());
        HbmMachinePerformanceCounters.networkPack(payload == null ? 0 : payload.length, sent);
        return sent;
    }

    private static double legacyBiomeSyncY(ServerLevel level) {
        return (level.getMinBuildHeight() + level.getMaxBuildHeight()) * 0.5D;
    }

    private static <MSG> void registerServerToClient(
            Class<MSG> type,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        int id = packetId++;
        CHANNEL.registerMessage(id, type,
                trackedEncoder(type, "S2C", encoder),
                trackedDecoder(type, "S2C", decoder),
                trackedHandler(type, "S2C", handler),
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        PacketRegistration registration = new PacketRegistration(id, "S2C", type.getSimpleName());
        PACKET_REGISTRATIONS.add(registration);
        PACKET_REGISTRATIONS_BY_TYPE.put(type, registration);
    }

    private static <MSG> void registerClientToServer(
            Class<MSG> type,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        int id = packetId++;
        CHANNEL.registerMessage(id, type,
                trackedEncoder(type, "C2S", encoder),
                trackedDecoder(type, "C2S", decoder),
                trackedHandler(type, "C2S", handler),
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        PacketRegistration registration = new PacketRegistration(id, "C2S", type.getSimpleName());
        PACKET_REGISTRATIONS.add(registration);
        PACKET_REGISTRATIONS_BY_TYPE.put(type, registration);
    }

    private static <MSG> BiConsumer<MSG, FriendlyByteBuf> trackedEncoder(
            Class<MSG> type,
            String direction,
            BiConsumer<MSG, FriendlyByteBuf> encoder) {
        return (message, buffer) -> {
            CODEC_ENCODES.incrementAndGet();
            int startWriterIndex = buffer.writerIndex();
            try {
                encoder.accept(message, buffer);
                int bytes = Math.max(0, buffer.writerIndex() - startWriterIndex);
                CODEC_ENCODED_BYTES.addAndGet(bytes);
                updateMax(CODEC_MAX_ENCODED_BYTES, bytes);
            } catch (RuntimeException | Error exception) {
                recordCodecFailure(type, direction, "encode", exception);
                throw exception;
            }
        };
    }

    private static <MSG> Function<FriendlyByteBuf, MSG> trackedDecoder(
            Class<MSG> type,
            String direction,
            Function<FriendlyByteBuf, MSG> decoder) {
        return buffer -> {
            CODEC_DECODES.incrementAndGet();
            int startReadableBytes = buffer.readableBytes();
            try {
                MSG message = decoder.apply(buffer);
                int remainingBytes = Math.max(0, buffer.readableBytes());
                int consumedBytes = Math.max(0, startReadableBytes - remainingBytes);
                CODEC_DECODED_BYTES.addAndGet(consumedBytes);
                updateMax(CODEC_MAX_DECODED_BYTES, consumedBytes);
                if (remainingBytes > 0) {
                    CODEC_DECODE_LEFTOVERS.incrementAndGet();
                    CODEC_DECODE_LEFTOVER_BYTES.addAndGet(remainingBytes);
                    updateMax(CODEC_MAX_DECODE_LEFTOVER_BYTES, remainingBytes);
                    lastCodecSizeWarning = direction + " decode " + type.getName()
                            + " left " + remainingBytes + " unread byte(s)";
                }
                return message;
            } catch (RuntimeException | Error exception) {
                recordCodecFailure(type, direction, "decode", exception);
                throw exception;
            }
        };
    }

    private static <MSG> BiConsumer<MSG, Supplier<NetworkEvent.Context>> trackedHandler(
            Class<MSG> type,
            String direction,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        return (message, contextSupplier) -> {
            HANDLER_DISPATCHES.incrementAndGet();
            if ("S2C".equals(direction)) {
                HANDLER_DISPATCHES_S2C.incrementAndGet();
            } else if ("C2S".equals(direction)) {
                HANDLER_DISPATCHES_C2S.incrementAndGet();
            }
            try {
                handler.accept(message, contextSupplier);
            } catch (RuntimeException | Error exception) {
                recordHandlerFailure(type, direction, exception);
                throw exception;
            }
        };
    }

    private static void recordHandlerFailure(Class<?> type, String direction, Throwable exception) {
        HANDLER_FAILURES.incrementAndGet();
        lastHandlerFailure = direction + " " + type.getName() + ": " + exception.getClass().getSimpleName()
                + (exception.getMessage() == null ? "" : " " + exception.getMessage());
        HbmNtm.LOGGER.warn("HBM network handler failed for {} {}.", direction, type.getName(), exception);
    }

    private static void recordCodecFailure(Class<?> type, String direction, String phase, Throwable exception) {
        if ("encode".equals(phase)) {
            CODEC_ENCODE_FAILURES.incrementAndGet();
        } else if ("decode".equals(phase)) {
            CODEC_DECODE_FAILURES.incrementAndGet();
        }
        lastCodecFailure = direction + " " + phase + " " + type.getName() + ": "
                + exception.getClass().getSimpleName()
                + (exception.getMessage() == null ? "" : " " + exception.getMessage());
        HbmNtm.LOGGER.warn("HBM network codec {} failed for {} {}.", phase, direction, type.getName(), exception);
    }

    private static void updateMax(AtomicLong target, long value) {
        long previous;
        do {
            previous = target.get();
            if (value <= previous) {
                return;
            }
        } while (!target.compareAndSet(previous, value));
    }

    private static boolean canSendMessage(Object message, String target, String expectedDirection) {
        if (message == null) {
            recordBlockedUnregisteredSend("null message to " + target);
            return false;
        }
        Optional<PacketRegistration> registration = packetRegistration(message.getClass());
        if (registration.isEmpty()) {
            recordBlockedUnregisteredSend(message.getClass().getName() + " to " + target);
            return false;
        }
        if (expectedDirection != null && !expectedDirection.equals(registration.get().direction())) {
            recordBlockedWrongDirectionSend(message.getClass().getName() + " to " + target
                    + " expected=" + expectedDirection + " actual=" + registration.get().direction());
            return false;
        }
        return true;
    }

    private static void recordBlockedUnregisteredSend(String message) {
        BLOCKED_UNREGISTERED_SENDS.incrementAndGet();
        lastBlockedSend = message;
    }

    private static void recordBlockedWrongDirectionSend(String message) {
        BLOCKED_WRONG_DIRECTION_SENDS.incrementAndGet();
        lastBlockedSend = message;
    }

    private static void recordBlockedInvalidTargetSend(String message) {
        BLOCKED_INVALID_TARGET_SENDS.incrementAndGet();
        lastBlockedSend = message;
    }

    private static boolean validateTarget(boolean valid, Object message, String target) {
        if (valid) {
            return true;
        }
        String type = message == null ? "null" : message.getClass().getName();
        recordBlockedInvalidTargetSend(type + " to " + target);
        HbmNtm.LOGGER.warn("Blocked HBM network message {} due to invalid target {}.", type, target);
        return false;
    }

    public static boolean validateTargetForSend(Object message, String target, boolean valid) {
        return validateTarget(valid, message, target);
    }

    public static boolean validateMessageForSend(Object message, String target) {
        return validateMessageForSend(message, target, null);
    }

    public static boolean validateMessageForSend(Object message, String target, String expectedDirection) {
        if (canSendMessage(message, target, expectedDirection)) {
            return true;
        }
        String type = message == null ? "null" : message.getClass().getName();
        HbmNtm.LOGGER.warn("Blocked invalid HBM network message {} to {} expectedDirection={}.",
                type, target, expectedDirection == null ? "any" : expectedDirection);
        return false;
    }

    private static net.minecraft.nbt.CompoundTag auxParticlePayload(net.minecraft.nbt.CompoundTag data,
                                                                    double x, double y, double z) {
        net.minecraft.nbt.CompoundTag payload = data == null ? new net.minecraft.nbt.CompoundTag() : data.copy();
        payload.putDouble("posX", x);
        payload.putDouble("posY", y);
        payload.putDouble("posZ", z);
        return payload;
    }

    public record PacketRegistration(int id, String direction, String typeName) {
    }

    public record LegacyPacketMapping(String legacyName, String modernName, String direction, String notes) {
    }

    public record LegacyPacketRegistration(int legacyId, String legacyName, String direction) {
    }

    private record LegacySerializableRecipeFile(String filename, byte[] fileBytes) {
        private LegacySerializableRecipeFile {
            filename = filename == null ? "" : filename;
            fileBytes = fileBytes == null ? new byte[0] : Arrays.copyOf(fileBytes, fileBytes.length);
        }
    }

    public record NetworkChannelSnapshot(
            String legacyChannelName,
            ResourceLocation modernChannelName,
            String protocolVersion,
            int registeredPacketCount,
            int firstModernPacketId,
            String firstModernPacketName,
            int lastModernPacketId,
            String lastModernPacketName,
            int legacyPacketRegistrationCount,
            int firstLegacyPacketId,
            String firstLegacyPacketName,
            int lastLegacyPacketId,
            String lastLegacyPacketName,
            boolean modernPacketIdsContiguous,
            boolean legacyPacketIdsContiguous,
            String notes) {
    }

    public record ProtocolManifestSnapshot(
            String legacyChannelName,
            ResourceLocation modernChannelName,
            String protocolVersion,
            String fingerprint,
            int modernPacketCount,
            int legacyPacketCount,
            int mappingRowCount,
            List<ProtocolManifestRow> rows,
            boolean auditProblems,
            String notes) {
    }

    public record ProtocolManifestRow(
            int legacyId,
            String legacyName,
            String direction,
            int mappingCount,
            String modernPackets,
            String notes) {
    }

    public record ProtocolContractSnapshot(
            String fingerprint,
            String legacyChannelName,
            ResourceLocation modernChannelName,
            String protocolVersion,
            int modernPacketCount,
            int legacyPacketCount,
            int mappingRowCount,
            int mappedLegacyPacketCount,
            boolean modernPacketIdsContiguous,
            boolean legacyPacketIdsContiguous,
            boolean auditProblems,
            List<String> problems,
            String notes) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    public record SendSafetySnapshot(
            int registeredTypes,
            long blockedUnregisteredSends,
            long blockedWrongDirectionSends,
            long blockedInvalidTargetSends,
            String lastBlockedSend) {
        public long totalBlockedSends() {
            return blockedUnregisteredSends + blockedWrongDirectionSends + blockedInvalidTargetSends;
        }
    }

    public record HandlerRuntimeSnapshot(
            long totalDispatches,
            long serverToClientDispatches,
            long clientToServerDispatches,
            long failures,
            String lastFailure) {
        public boolean hasFailures() {
            return failures > 0L;
        }
    }

    public record CodecRuntimeSnapshot(
            long encodes,
            long decodes,
            long encodeFailures,
            long decodeFailures,
            long encodedBytes,
            long decodedBytes,
            long maxEncodedBytes,
            long maxDecodedBytes,
            long decodeLeftovers,
            long decodeLeftoverBytes,
            long maxDecodeLeftoverBytes,
            String lastFailure,
            String lastSizeWarning) {
        public long totalFailures() {
            return encodeFailures + decodeFailures;
        }

        public boolean hasFailures() {
            return totalFailures() > 0L;
        }

        public boolean hasWarnings() {
            return hasFailures() || decodeLeftovers > 0L;
        }
    }

    public record NetworkRuntimeSnapshot(
            String protocolVersion,
            int foundationProgressPercent,
            int legacyPacketCoveragePercent,
            int registeredPacketCount,
            int legacyPacketRegistrationCount,
            long mappedLegacyPacketCount,
            int unmappedLegacyPacketCount,
            int legacyPacketMappingCount,
            boolean auditProblems,
            int auditMissingModern,
            int auditUnknownLegacy,
            int auditDirectionMismatch,
            int auditDuplicateEntries,
            SendSafetySnapshot sendSafety,
            CodecRuntimeSnapshot codec,
            HandlerRuntimeSnapshot handlers,
            ThreadedPacketDispatcher.Snapshot threaded,
            LegacyPacketThreading.LegacyCommandSnapshot legacyPacketThreading,
            long legacyFlushCalls,
            long legacyWaitCalls,
            long rawBufferBlockedSends,
            String lastRawBufferBlockedSend,
            long dimensionIdBlockedSends,
            String lastDimensionIdBlockedSend,
            int directHelperCount,
            int threadedHelperCount,
            int packetThreadingHelperCount) {

        public int totalHelperCount() {
            return directHelperCount + threadedHelperCount + packetThreadingHelperCount;
        }

        public boolean hasRuntimeWarnings() {
            return auditProblems
                    || sendSafety.totalBlockedSends() > 0L
                    || codec.hasWarnings()
                    || handlers.hasFailures()
                    || threaded.totalFailed() > 0L
                    || threaded.totalDiscarded() > 0L
                    || threaded.fallbackToMainThread()
                    || rawBufferBlockedSends > 0L
                    || dimensionIdBlockedSends > 0L
                    || legacyPacketThreading.triggered();
        }
    }

    public record ProtocolAudit(
            List<LegacyPacketMapping> mappingsToUnregisteredModernPackets,
            List<LegacyPacketMapping> mappingsFromUnknownLegacyPackets,
            List<PacketRegistration> modernPacketsWithoutLegacyMappings,
            List<LegacyPacketRegistration> unmappedLegacyPackets,
            List<LegacyPacketMapping> mappingsWithDirectionMismatch,
            List<String> duplicateLegacyIds,
            List<String> duplicateLegacyNames,
            List<String> duplicateModernRegistrations) {
        public boolean hasProblems() {
            return !mappingsToUnregisteredModernPackets.isEmpty()
                    || !mappingsFromUnknownLegacyPackets.isEmpty()
                    || !unmappedLegacyPackets.isEmpty()
                    || !mappingsWithDirectionMismatch.isEmpty()
                    || !duplicateLegacyIds.isEmpty()
                    || !duplicateLegacyNames.isEmpty()
                    || !duplicateModernRegistrations.isEmpty();
        }
    }

    private ModMessages() {
    }
}
