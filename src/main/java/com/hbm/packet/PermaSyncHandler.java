package com.hbm.packet;

import com.hbm.ntm.client.ClientPermaSyncData;
import com.hbm.ntm.client.ClientPollutionData;
import com.hbm.ntm.client.ClientTomImpactData;
import com.hbm.ntm.network.HbmPermaSyncData;
import com.hbm.ntm.pollution.PollutionManager;
import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.Set;

/**
 * Legacy permanent-sync wire facade. Modern runtime packets use NBT, but this
 * preserves the 1.7.10 ByteBuf order for migrated old packet call sites.
 */
public final class PermaSyncHandler {
    public static final Set<Integer> boykissers = new HashSet<>();
    public static final float[] pollution = new float[PollutionType.count()];

    public static void writePacket(ByteBuf buffer, ServerPlayer player) {
        if (player == null) {
            writePacket(buffer, (ServerLevel) null, null);
            return;
        }
        writePacket(buffer, player.serverLevel(), player);
    }

    public static void writePacket(ByteBuf buffer, ServerLevel level, ServerPlayer player) {
        if (buffer == null) {
            return;
        }

        CompoundTag data = player == null ? new CompoundTag() : HbmPermaSyncData.writeForPlayer(player);
        TomImpactSavedData.Snapshot impact = TomImpactSavedData.readPermaSyncData(data);
        buffer.writeFloat(impact.fire());
        buffer.writeFloat(impact.dust());
        buffer.writeBoolean(impact.impact());

        int[] deathEffectIds = player == null ? new int[0] : HbmPermaSyncData.deathEffectEntityIds(player);
        buffer.writeShort((short) Math.min(deathEffectIds.length, Short.MAX_VALUE));
        for (int i = 0; i < deathEffectIds.length && i < Short.MAX_VALUE; i++) {
            buffer.writeInt(deathEffectIds[i]);
        }

        PollutionSavedData.PollutionSample sample = player == null
                ? new PollutionSavedData.PollutionSample()
                : PollutionManager.getPollutionData(player.level(), player.blockPosition());
        for (PollutionType type : PollutionType.orderedValues()) {
            buffer.writeFloat(sample.get(type));
        }
    }

    public static void readPacket(ByteBuf buffer) {
        readPacket(buffer, null, null);
    }

    public static void readPacket(ByteBuf buffer, Level level, Player player) {
        if (buffer == null) {
            return;
        }

        CompoundTag data = new CompoundTag();
        CompoundTag impact = new CompoundTag();
        impact.putFloat(TomImpactSavedData.TAG_FIRE, buffer.readFloat());
        impact.putFloat(TomImpactSavedData.TAG_DUST, buffer.readFloat());
        impact.putBoolean(TomImpactSavedData.TAG_IMPACT, buffer.readBoolean());
        data.put(TomImpactSavedData.TAG_PERMA_SYNC, impact);

        boykissers.clear();
        int count = buffer.readShort() & 0xFFFF;
        int[] deathEffectIds = new int[count];
        for (int i = 0; i < count; i++) {
            int id = buffer.readInt();
            boykissers.add(id);
            deathEffectIds[i] = id;
        }
        HbmPermaSyncData.appendDeathEffectEntityIds(data, deathEffectIds);

        PollutionSavedData.PollutionSample sample = new PollutionSavedData.PollutionSample();
        for (PollutionType type : PollutionType.orderedValues()) {
            float value = buffer.readFloat();
            sample.set(type, value);
            pollution[type.ordinal()] = value;
        }
        data.put(PollutionSavedData.TAG_PERMA_SYNC, PollutionSavedData.writeSampleTag(sample));

        ClientPermaSyncData.update(data);
        ClientPollutionData.updateFromPermaSync(data);
        ClientTomImpactData.updateFromPermaSync(data);
    }

    private PermaSyncHandler() {
    }
}
