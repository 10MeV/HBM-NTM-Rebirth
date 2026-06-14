package com.hbm.packet.toclient;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.ExtPropertiesSyncPacket;
import com.hbm.ntm.player.HbmExtendedProperties;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.packet.threading.PrecompiledPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Legacy ExtPropPacket facade. The old wire order is still available for
 * diagnostics, while actual sends are adapted to ExtPropertiesSyncPacket.
 */
public class ExtPropPacket extends PrecompiledPacket {
    private HbmExtendedProperties.SyncData data;
    private HbmExtendedProperties.LegacySyncData legacyData;

    public ExtPropPacket() {
        this(HbmExtendedProperties.emptySyncedData(), HbmExtendedProperties.emptyLegacySyncedData());
    }

    public ExtPropPacket(HbmLivingProps props, HbmPlayerProps pprps) {
        this(fromProps(props, pprps), legacyFromProps(props, pprps));
    }

    public ExtPropPacket(ServerPlayer player, float chunkRadiation, float resistance) {
        this(HbmExtendedProperties.writeSyncedData(player, chunkRadiation, resistance),
                HbmExtendedProperties.writeLegacySyncedData(player));
    }

    public ExtPropPacket(HbmExtendedProperties.SyncData data) {
        this(data, legacyFromModern(data));
    }

    private ExtPropPacket(HbmExtendedProperties.SyncData data, HbmExtendedProperties.LegacySyncData legacyData) {
        this.data = data == null ? HbmExtendedProperties.emptySyncedData() : data;
        this.legacyData = legacyData == null ? legacyFromModern(this.data) : legacyData;
    }

    public static ExtPropPacket decode(FriendlyByteBuf buffer) {
        ExtPropPacket packet = new ExtPropPacket();
        packet.fromBytes(buffer);
        return packet;
    }

    public void fromBytes(FriendlyByteBuf buffer) {
        legacyData = HbmExtendedProperties.decodeLegacySyncedData(buffer);
        data = modernFromLegacy(legacyData);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        HbmExtendedProperties.encodeLegacySyncedData(legacyData == null ? legacyFromModern(data) : legacyData, buffer);
    }

    public void encode(FriendlyByteBuf buffer) {
        toBytes(buffer);
    }

    public HbmExtendedProperties.SyncData data() {
        return data == null ? HbmExtendedProperties.emptySyncedData() : data;
    }

    public HbmExtendedProperties.LegacySyncData legacyData() {
        return legacyData == null ? legacyFromModern(data()) : legacyData;
    }

    @Override
    public ExtPropertiesSyncPacket toModernPacket() {
        return ModMessages.extPropertiesPacket(data());
    }

    private static HbmExtendedProperties.SyncData fromProps(HbmLivingProps props, HbmPlayerProps pprps) {
        LivingEntity living = livingEntity(props, pprps);
        Player player = player(pprps, living);
        return new HbmExtendedProperties.SyncData(
                living == null ? HbmLivingProperties.emptySyncedData() : HbmLivingProperties.writeSyncedData(living, 0.0F, 0.0F),
                player == null ? HbmPlayerProperties.emptySyncedData() : HbmPlayerProperties.writePlayerSyncData(player));
    }

    private static HbmExtendedProperties.LegacySyncData legacyFromProps(HbmLivingProps props, HbmPlayerProps pprps) {
        LivingEntity living = livingEntity(props, pprps);
        Player player = player(pprps, living);
        return new HbmExtendedProperties.LegacySyncData(
                living == null ? HbmLivingProperties.emptyLegacySyncedData() : HbmLivingProperties.writeLegacySyncedData(living),
                player == null ? HbmPlayerProperties.emptySyncedData() : HbmPlayerProperties.writePlayerSyncData(player));
    }

    private static LivingEntity livingEntity(HbmLivingProps props, HbmPlayerProps pprps) {
        if (props != null) {
            return props.entity();
        }
        return pprps == null ? null : pprps.player();
    }

    private static Player player(HbmPlayerProps pprps, LivingEntity living) {
        if (pprps != null) {
            return pprps.player();
        }
        return living instanceof Player player ? player : null;
    }

    private static HbmExtendedProperties.SyncData modernFromLegacy(HbmExtendedProperties.LegacySyncData legacy) {
        HbmExtendedProperties.LegacySyncData safeLegacy =
                legacy == null ? HbmExtendedProperties.emptyLegacySyncedData() : legacy;
        HbmLivingProperties.LegacySyncData living = safeLegacy.living();
        HbmLivingProperties.SyncData modernLiving = new HbmLivingProperties.SyncData(
                living.radiation(),
                living.digamma(),
                0.0F,
                0.0F,
                0.0F,
                living.asbestos(),
                living.blackLung(),
                living.bombTimer(),
                living.contagion(),
                living.oil(),
                0,
                0,
                0,
                0,
                living.contaminationEffects());
        return new HbmExtendedProperties.SyncData(modernLiving, safeLegacy.player());
    }

    private static HbmExtendedProperties.LegacySyncData legacyFromModern(HbmExtendedProperties.SyncData data) {
        HbmExtendedProperties.SyncData safeData = data == null ? HbmExtendedProperties.emptySyncedData() : data;
        HbmLivingProperties.SyncData living = safeData.living();
        HbmLivingProperties.LegacySyncData legacyLiving = new HbmLivingProperties.LegacySyncData(
                living.radiation(),
                living.digamma(),
                living.asbestos(),
                living.bombTimer(),
                living.contagion(),
                living.blackLung(),
                living.oil(),
                living.contaminationEffects());
        return new HbmExtendedProperties.LegacySyncData(legacyLiving, safeData.player());
    }
}
