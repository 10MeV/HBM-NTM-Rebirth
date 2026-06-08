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
import com.hbm.ntm.network.packet.ServerEntityActionPacket;
import com.hbm.ntm.network.packet.ServerTileActionPacket;
import com.hbm.ntm.network.packet.TileControlPacket;
import com.hbm.ntm.network.packet.TileSyncPacket;
import com.hbm.ntm.network.packet.TileSyncRequestPacket;
import com.hbm.ntm.network.packet.TypedMenuActionPacket;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static final int LIBRARY_FOUNDATION_PROGRESS_PERCENT = 99;
    private static int packetId;
    private static final AtomicBoolean REGISTERED = new AtomicBoolean();
    private static final List<PacketRegistration> PACKET_REGISTRATIONS = new ArrayList<>();
    private static final Map<Class<?>, PacketRegistration> PACKET_REGISTRATIONS_BY_TYPE = new HashMap<>();
    private static final AtomicLong BLOCKED_UNREGISTERED_SENDS = new AtomicLong();
    private static final AtomicLong BLOCKED_WRONG_DIRECTION_SENDS = new AtomicLong();
    private static final AtomicLong BLOCKED_INVALID_TARGET_SENDS = new AtomicLong();
    private static volatile String lastBlockedSend = "";
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
            new LegacyPacketMapping("ExplosionKnockbackPacket", "ExplosionKnockbackPacket", "S2C",
                    "client motion impulse for explosion effects"),
            new LegacyPacketMapping("ExplosionVanillaNewTechnologyCompressedAffectedBlockPositionDataForClientEffectsAndParticleHandlingPacket",
                    "CompressedExplosionEffectPacket", "S2C",
                    "compressed affected block positions for client explosion effects"),
            new LegacyPacketMapping("AuxButtonPacket", "LegacyButtonPacket", "C2S",
                    "legacy block position plus value/id button packet"),
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
                    "key id plus pressed state"),
            new LegacyPacketMapping("MachineBatteryButtonPacket", "MachineBatteryButtonPacket", "C2S",
                    "early dedicated machine battery button compatibility"));

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(HbmNtm.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static LegacyNetworkDispatcher wrapper() {
        return LegacyNetworkDispatcher.WRAPPER;
    }

    public static String legacyWrapperSummary() {
        return "legacyWrapper=PacketDispatcher.wrapper facade"
                + " directHelpers=" + LegacyNetworkDispatcher.directSendHelperCount()
                + " threadedHelpers=" + LegacyNetworkDispatcher.threadedSendHelperCount()
                + " packetThreadingHelpers=" + LegacyNetworkDispatcher.packetThreadingHelperCount()
                + " flushCalls=" + LegacyNetworkDispatcher.legacyFlushCallCount()
                + " rawBufferBlocked=" + LegacyRawBufferNetwork.blockedRawBufferSendCount()
                + " packetThreadingWaitCalls=" + LegacyPacketThreading.legacyWaitCallCount()
                + " note=" + LegacyNetworkDispatcher.compatibilityNote();
    }

    public static String protocolVersion() {
        return PROTOCOL_VERSION;
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

    public static void resetSendSafetyCounters() {
        BLOCKED_UNREGISTERED_SENDS.set(0L);
        BLOCKED_WRONG_DIRECTION_SENDS.set(0L);
        BLOCKED_INVALID_TARGET_SENDS.set(0L);
        lastBlockedSend = "";
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
                ThreadedPacketDispatcher.snapshot(),
                LegacyPacketThreading.legacyCommandSnapshot(),
                LegacyNetworkDispatcher.legacyFlushCallCount(),
                LegacyPacketThreading.legacyWaitCallCount(),
                LegacyRawBufferNetwork.blockedRawBufferSendCount(),
                LegacyRawBufferNetwork.lastBlockedRawBufferSend(),
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
                + " threadedPending=" + snapshot.threaded().pending()
                + " threadedDiscarded=" + snapshot.threaded().totalDiscarded()
                + " threadedFallback=" + snapshot.threaded().fallbackToMainThread()
                + " rawBufferBlocked=" + snapshot.rawBufferBlockedSends()
                + " legacyWaitCalls=" + snapshot.legacyWaitCalls()
                + " legacyLastTickTotal=" + snapshot.legacyPacketThreading().lastTickTotal()
                + " helperSurface=" + snapshot.totalHelperCount();
    }

    public static void resetNetworkRuntimeDiagnostics() {
        resetSendSafetyCounters();
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
        HbmNtm.LOGGER.info("HBM network protocol audit passed: {}", protocolAuditSummary());
    }

    private static List<String> duplicateValues(List<String> values) {
        return values.stream()
                .filter(value -> Collections.frequency(values, value) > 1)
                .distinct()
                .toList();
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
    }

    public static void sendToServer(Object message) {
        if (!validateMessageForSend(message, "server", "C2S")) {
            return;
        }
        CHANNEL.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        if (!validateTarget(player != null, message, "player:null")) {
            return;
        }
        if (!validateMessageForSend(message, player == null ? "player:null" : "player:" + player.getGameProfile().getName(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendToEntityTrackers(Object message, Entity entity) {
        if (!validateTarget(entity != null, message, "entityTrackers:null")) {
            return;
        }
        if (!validateMessageForSend(message, entity == null ? "entityTrackers:null" : "entityTrackers:" + entity.getId(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public static void sendToEntityAndSelf(Object message, Entity entity) {
        if (!validateTarget(entity != null, message, "entityAndSelf:null")) {
            return;
        }
        if (!validateMessageForSend(message, entity == null ? "entityAndSelf:null" : "entityAndSelf:" + entity.getId(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static void sendToDimension(Object message, ServerLevel level) {
        if (!validateTarget(level != null, message, "dimension:null")) {
            return;
        }
        if (!validateMessageForSend(message, level == null ? "dimension:null" : "dimension:" + level.dimension().location(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.DIMENSION.with(level::dimension), message);
    }

    public static void sendToDimension(Object message, ResourceKey<Level> dimension) {
        if (!validateTarget(dimension != null, message, "dimensionKey:null")) {
            return;
        }
        if (!validateMessageForSend(message, dimension == null ? "dimensionKey:null" : "dimensionKey:" + dimension.location(), "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
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
        if (!validateTarget(point != null, message, "near-point:null")) {
            return;
        }
        if (!validateMessageForSend(message, "near", "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.NEAR.with(() -> point), message);
    }

    public static void sendToAll(Object message) {
        if (!validateMessageForSend(message, "all", "S2C")) {
            return;
        }
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
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
        sendToTrackingChunk(new TileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag()), blockEntity);
    }

    public static void syncTileToTrackingThreaded(HbmTileSyncable syncable, BlockEntity blockEntity) {
        ThreadedPacketDispatcher.sendToTrackingChunk(new TileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag()), blockEntity);
    }

    public static void syncTileToPlayer(HbmTileSyncable syncable, BlockEntity blockEntity, ServerPlayer player) {
        sendToPlayer(new TileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag()), player);
    }

    public static void syncTileToPlayerThreaded(HbmTileSyncable syncable, BlockEntity blockEntity, ServerPlayer player) {
        ThreadedPacketDispatcher.sendToPlayer(new TileSyncPacket(blockEntity.getBlockPos(), syncable.getClientSyncTag()), player);
    }

    public static void syncEntityToTracking(HbmEntitySyncable syncable, Entity entity) {
        sendToEntityTrackers(new EntitySyncPacket(entity.getId(), syncable.getClientSyncTag()), entity);
    }

    public static void syncEntityToTrackingThreaded(HbmEntitySyncable syncable, Entity entity) {
        ThreadedPacketDispatcher.sendToEntityTrackers(new EntitySyncPacket(entity.getId(), syncable.getClientSyncTag()), entity);
    }

    public static void syncEntityToPlayer(HbmEntitySyncable syncable, Entity entity, ServerPlayer player) {
        if (syncable.canSendClientSyncTo(player)) {
            sendToPlayer(new EntitySyncPacket(entity.getId(), syncable.getClientSyncTag()), player);
        }
    }

    public static void syncEntityToPlayerThreaded(HbmEntitySyncable syncable, Entity entity, ServerPlayer player) {
        if (syncable.canSendClientSyncTo(player)) {
            ThreadedPacketDispatcher.sendToPlayer(new EntitySyncPacket(entity.getId(), syncable.getClientSyncTag()), player);
        }
    }

    public static void sendClientEntityEvent(Entity entity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToEntityTrackers(new ClientEntityEventPacket(entity.getId(), eventType, data), entity);
    }

    public static void sendClientEntityEventThreaded(Entity entity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToEntityTrackers(new ClientEntityEventPacket(entity.getId(), eventType, data), entity);
    }

    public static void sendClientEntityEventAndSelf(Entity entity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToEntityAndSelf(new ClientEntityEventPacket(entity.getId(), eventType, data), entity);
    }

    public static void sendClientEntityEventAndSelfThreaded(Entity entity, ResourceLocation eventType,
                                                            net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToEntityAndSelf(new ClientEntityEventPacket(entity.getId(), eventType, data), entity);
    }

    public static void sendClientEntityEvent(ServerPlayer player, Entity entity, ResourceLocation eventType,
                                             net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new ClientEntityEventPacket(entity.getId(), eventType, data), player);
    }

    public static void sendClientEntityEventThreaded(ServerPlayer player, Entity entity, ResourceLocation eventType,
                                                     net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(new ClientEntityEventPacket(entity.getId(), eventType, data), player);
    }

    public static void sendEntityAction(Entity entity, ResourceLocation actionType, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new ServerEntityActionPacket(entity.getId(), actionType, data));
    }

    public static void informPlayer(ServerPlayer player, Component message, int id, int millis) {
        sendToPlayer(new ClientInformPacket(message, id, millis), player);
    }

    public static void sendAuxParticle(ServerLevel level, double x, double y, double z, net.minecraft.nbt.CompoundTag data,
                                       double range) {
        sendToAllAround(new AuxParticlePacket(auxParticlePayload(data, x, y, z)), level, x, y, z, range);
    }

    public static void sendAuxParticleThreaded(ServerLevel level, double x, double y, double z,
                                               net.minecraft.nbt.CompoundTag data, double range) {
        ThreadedPacketDispatcher.sendToAllAround(new AuxParticlePacket(auxParticlePayload(data, x, y, z)),
                level, x, y, z, range);
    }

    public static void sendAuxParticle(ServerPlayer player, double x, double y, double z,
                                       net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new AuxParticlePacket(auxParticlePayload(data, x, y, z)), player);
    }

    public static void sendAuxParticleThreaded(ServerPlayer player, double x, double y, double z,
                                               net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(new AuxParticlePacket(auxParticlePayload(data, x, y, z)), player);
    }

    public static AuxParticlePacket auxParticlePacket(double x, double y, double z, net.minecraft.nbt.CompoundTag data) {
        return new AuxParticlePacket(auxParticlePayload(data, x, y, z));
    }

    public static void sendItemAnimation(ServerPlayer player, int slot, int rail, String itemKey,
                                         ResourceLocation animationFile, String animationName, boolean holdLastFrame) {
        sendToPlayer(new ItemAnimationPacket(slot, rail, itemKey, animationFile, animationName, holdLastFrame), player);
    }

    public static void sendLegacyItemAnimation(ServerPlayer player, int animationType, int receiverIndex, int itemIndex) {
        sendToPlayer(new LegacyItemAnimationPacket((short) animationType, receiverIndex, itemIndex), player);
    }

    public static void sendMuzzleFlash(Entity entity) {
        sendToEntityTrackers(new MuzzleFlashPacket(entity.getId()), entity);
    }

    public static void sendItemControl(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        sendToServer(new ItemControlPacket(hand, tag));
    }

    public static void sendNbtItemControl(InteractionHand hand, net.minecraft.nbt.CompoundTag tag) {
        net.minecraft.nbt.CompoundTag data = tag == null ? new net.minecraft.nbt.CompoundTag() : tag.copy();
        data.putString("legacyPacket", HbmNetworkActions.NBT_ITEM_CONTROL.toString());
        sendItemControl(hand, data);
    }

    public static void sendItemAction(InteractionHand hand, ResourceLocation actionType, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new ItemActionPacket(hand, actionType, data));
    }

    public static void sendItemAction(InteractionHand hand, ResourceLocation actionType) {
        sendItemAction(hand, actionType, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendDesignatorAction(InteractionHand hand, int operator, int value, int reference) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("operator", operator);
        data.putInt("value", value);
        data.putInt("reference", reference);
        sendItemAction(hand, HbmNetworkActions.DESIGNATOR, data);
    }

    public static void sendBobmazonOffer(InteractionHand hand, int offerIndex) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("offer", offerIndex);
        sendItemAction(hand, HbmNetworkActions.BOBMAZON_OFFER, data);
    }

    public static void sendSatelliteCoordinateAction(InteractionHand hand, BlockPos pos, int frequency) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putString("actionType", HbmNetworkActions.SATELLITE_COORDINATE.toString());
        sendCoordinateAction(hand, pos, 0, 0, frequency, data);
    }

    public static void sendSatelliteLaserAction(InteractionHand hand, int x, int z, int frequency) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putBoolean("laser", true);
        data.putString("actionType", HbmNetworkActions.SATELLITE_LASER.toString());
        sendCoordinateAction(hand, new BlockPos(x, 0, z), 1, 0, frequency, data);
    }

    public static void sendSatCoord(InteractionHand hand, int x, int y, int z, int frequency) {
        sendSatelliteCoordinateAction(hand, new BlockPos(x, y, z), frequency);
    }

    public static void sendSatLaser(InteractionHand hand, int x, int z, int frequency) {
        sendSatelliteLaserAction(hand, x, z, frequency);
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

    public static void sendAuxButton(BlockPos pos, int value, int id) {
        sendLegacyButton(pos, value, id);
    }

    public static void sendTileControl(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new TileControlPacket(pos, data));
    }

    public static void sendNbtControl(BlockPos pos, net.minecraft.nbt.CompoundTag data) {
        sendTileControl(pos, data);
    }

    public static void sendTypedTileAction(BlockPos pos, ResourceLocation actionType, int value,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToServer(new ServerTileActionPacket(pos, actionType, value, data));
    }

    public static void sendTypedTileAction(BlockPos pos, ResourceLocation actionType) {
        sendTypedTileAction(pos, actionType, 0, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendTypedTileAction(BlockPos pos, ResourceLocation actionType, int value) {
        sendTypedTileAction(pos, actionType, value, new net.minecraft.nbt.CompoundTag());
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

    public static void sendTileBinaryControl(BlockPos pos, ResourceLocation channel, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ServerTileBinaryControlPacket.MAX_PAYLOAD_BYTES) {
            sendToServer(new ServerTileBinaryControlPacket(pos, channel, safePayload));
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

    public static void syncPermaData(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new PermaSyncPacket(data), player);
    }

    public static void syncPermaDataThreaded(ServerPlayer player, net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(new PermaSyncPacket(data), player);
    }

    public static void sendExplosionKnockback(ServerPlayer player, Vec3 motion) {
        sendToPlayer(new ExplosionKnockbackPacket(motion), player);
    }

    public static void sendCoordinateAction(InteractionHand hand, BlockPos pos, int action, int value, int frequency,
                                            net.minecraft.nbt.CompoundTag data) {
        sendToServer(new CoordinateActionPacket(hand, pos, action, value, frequency, data));
    }

    public static void syncClientBinaryData(ServerPlayer player, ResourceLocation channel, String name, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendToPlayer(new ClientBinaryDataPacket(channel, name, safePayload, false), player);
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
            ThreadedPacketDispatcher.sendToPlayer(new ClientBinaryDataPacket(channel, name, safePayload, false), player);
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

    public static void clearClientBinaryData(ServerPlayer player, ResourceLocation channel) {
        sendToPlayer(new ClientBinaryDataPacket(channel, "", new byte[0], true), player);
    }

    public static void clearClientBinaryDataThreaded(ServerPlayer player, ResourceLocation channel) {
        ThreadedPacketDispatcher.sendToPlayer(new ClientBinaryDataPacket(channel, "", new byte[0], true), player);
    }

    public static void markClientBinaryDataReady(ServerPlayer player, ResourceLocation channel) {
        sendToPlayer(new ClientBinaryDataReadyPacket(channel), player);
    }

    public static void sendClientTileEvent(BlockEntity blockEntity, ResourceLocation eventType, net.minecraft.nbt.CompoundTag data) {
        sendToTrackingChunk(new ClientTileEventPacket(blockEntity.getBlockPos(), eventType, data), blockEntity);
    }

    public static void sendClientTileEventThreaded(BlockEntity blockEntity, ResourceLocation eventType,
                                                   net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToTrackingChunk(new ClientTileEventPacket(blockEntity.getBlockPos(), eventType, data), blockEntity);
    }

    public static void syncTileBinaryToTracking(HbmTileBinarySyncProvider provider, BlockEntity blockEntity) {
        syncTileBinaryToTracking(provider, blockEntity, provider.getClientTileBinarySyncChannel());
    }

    public static void syncTileBinaryToTracking(HbmTileBinarySyncProvider provider, BlockEntity blockEntity,
                                                ResourceLocation channel) {
        sendClientTileBinaryData(blockEntity, channel, provider::writeClientTileBinaryData);
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

    public static void syncMissileMultipart(BlockEntity blockEntity, MissileMultipartSnapshot multipart) {
        sendToTrackingChunk(new ClientMissileMultipartPacket(blockEntity.getBlockPos(), multipart), blockEntity);
    }

    public static void syncMissileMultipart(BlockEntity blockEntity, ItemStack warhead, ItemStack fuselage,
                                            ItemStack fins, ItemStack thruster) {
        syncMissileMultipart(blockEntity, MissileMultipartSnapshot.of(warhead, fuselage, fins, thruster));
    }

    public static void syncMissileMultipart(Level level, BlockPos pos, MissileMultipartSnapshot multipart) {
        sendToTrackingChunk(new ClientMissileMultipartPacket(pos, multipart), level, pos);
    }

    public static void syncMissileMultipart(ServerPlayer player, BlockPos pos, MissileMultipartSnapshot multipart) {
        sendToPlayer(new ClientMissileMultipartPacket(pos, multipart), player);
    }

    public static void sendClientTileEvent(ServerPlayer player, BlockPos pos, ResourceLocation eventType,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new ClientTileEventPacket(pos, eventType, data), player);
    }

    public static void sendVaultDoorEvent(BlockEntity blockEntity, boolean opening, int state, boolean resetClientTime, int type) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putBoolean("opening", opening);
        data.putInt("state", state);
        data.putBoolean("resetClientTime", resetClientTime);
        data.putInt("type", type);
        sendClientTileEvent(blockEntity, HbmNetworkActions.VAULT_DOOR, data);
    }

    public static void sendSirenEvent(BlockEntity blockEntity, int trackId, boolean active) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("trackId", trackId);
        data.putBoolean("active", active);
        sendClientTileEvent(blockEntity, HbmNetworkActions.SIREN, data);
    }

    public static void syncForceFieldState(BlockEntity blockEntity, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putFloat("radius", radius);
        data.putInt("health", health);
        data.putInt("maxHealth", maxHealth);
        data.putInt("power", power);
        data.putBoolean("active", active);
        data.putInt("color", color);
        data.putInt("cooldown", cooldown);
        sendToTrackingChunk(new TileSyncPacket(blockEntity.getBlockPos(), data), blockEntity);
    }

    public static void sendForceFieldState(BlockEntity blockEntity, float radius, int health, int maxHealth,
                                           int power, boolean active, int color, int cooldown) {
        syncForceFieldState(blockEntity, radius, health, maxHealth, power, active, color, cooldown);
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

    public static void sendClientTileBinaryData(BlockEntity blockEntity, ResourceLocation channel, byte[] payload) {
        sendClientTileBinaryData(blockEntity.getLevel(), blockEntity.getBlockPos(), channel, payload);
    }

    public static void sendClientTileBinaryData(ServerPlayer player, BlockPos pos, ResourceLocation channel, byte[] payload) {
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendToPlayer(new ClientTileBinaryDataPacket(pos, channel, safePayload), player);
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

    public static void sendClientTileBinaryData(Level level, BlockPos pos, ResourceLocation channel, byte[] payload) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        byte[] safePayload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        if (safePayload.length <= ClientTileBinaryDataPacket.MAX_PAYLOAD_BYTES) {
            sendToTrackingChunk(new ClientTileBinaryDataPacket(pos, channel, safePayload), level, pos);
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

    public static void syncClientPanelData(ServerPlayer player, ResourceLocation panelType, int legacyType,
                                           net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new ClientPanelDataPacket(panelType, legacyType, data), player);
    }

    public static void syncClientPanelDataThreaded(ServerPlayer player, ResourceLocation panelType, int legacyType,
                                                   net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(new ClientPanelDataPacket(panelType, legacyType, data), player);
    }

    public static void syncSatellitePanelData(ServerPlayer player, int legacyType, net.minecraft.nbt.CompoundTag data) {
        syncClientPanelData(player, HbmNetworkActions.SATELLITE_PANEL, legacyType, data);
    }

    public static void syncPlayerProperties(ServerPlayer player, ResourceLocation dataType, net.minecraft.nbt.CompoundTag data) {
        sendToPlayer(new PlayerPropertiesPacket(dataType, data), player);
    }

    public static void syncPlayerPropertiesThreaded(ServerPlayer player, ResourceLocation dataType,
                                                    net.minecraft.nbt.CompoundTag data) {
        ThreadedPacketDispatcher.sendToPlayer(new PlayerPropertiesPacket(dataType, data), player);
    }

    public static void syncPlayerPropertiesBatch(ServerPlayer player,
                                                 Map<ResourceLocation, net.minecraft.nbt.CompoundTag> properties) {
        Map<ResourceLocation, net.minecraft.nbt.CompoundTag> safeProperties = properties == null ? Map.of() : properties;
        safeProperties.forEach((dataType, data) -> syncPlayerProperties(player, dataType, data));
    }

    public static void sendMenuAction(int action, int value, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new MenuActionPacket(action, value, data));
    }

    public static void sendTypedMenuAction(ResourceLocation actionType, int value, net.minecraft.nbt.CompoundTag data) {
        sendToServer(new TypedMenuActionPacket(actionType, value, data));
    }

    public static void sendTypedMenuAction(ResourceLocation actionType, int value) {
        sendTypedMenuAction(actionType, value, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendTypedMenuAction(ResourceLocation actionType) {
        sendTypedMenuAction(actionType, 0, new net.minecraft.nbt.CompoundTag());
    }

    public static void sendAnvilCraftAction(int recipeIndex, int mode) {
        net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
        data.putInt("recipeIndex", recipeIndex);
        data.putInt("mode", mode);
        sendTypedMenuAction(HbmNetworkActions.ANVIL_CRAFT, recipeIndex, data);
    }

    public static void sendAnvilCraft(int recipeIndex, int mode) {
        sendAnvilCraftAction(recipeIndex, mode);
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

    public static void sendCompressedExplosionEffectThreaded(ServerLevel level, Vec3 center, float size,
                                                             List<BlockPos> affectedBlocks, double range) {
        ThreadedPacketDispatcher.sendToAllAround(new CompressedExplosionEffectPacket(center, size, affectedBlocks),
                level, center.x, center.y, center.z, range);
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
        return syncState == null || syncState.shouldSend(payload, level.getGameTime());
    }

    private static <MSG> void registerServerToClient(
            Class<MSG> type,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        int id = packetId++;
        CHANNEL.registerMessage(id, type, encoder, decoder, handler, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
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
        CHANNEL.registerMessage(id, type, encoder, decoder, handler, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        PacketRegistration registration = new PacketRegistration(id, "C2S", type.getSimpleName());
        PACKET_REGISTRATIONS.add(registration);
        PACKET_REGISTRATIONS_BY_TYPE.put(type, registration);
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
            ThreadedPacketDispatcher.Snapshot threaded,
            LegacyPacketThreading.LegacyCommandSnapshot legacyPacketThreading,
            long legacyFlushCalls,
            long legacyWaitCalls,
            long rawBufferBlockedSends,
            String lastRawBufferBlockedSend,
            int directHelperCount,
            int threadedHelperCount,
            int packetThreadingHelperCount) {

        public int totalHelperCount() {
            return directHelperCount + threadedHelperCount + packetThreadingHelperCount;
        }

        public boolean hasRuntimeWarnings() {
            return auditProblems
                    || sendSafety.totalBlockedSends() > 0L
                    || threaded.totalFailed() > 0L
                    || threaded.totalDiscarded() > 0L
                    || threaded.fallbackToMainThread()
                    || rawBufferBlockedSends > 0L
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
