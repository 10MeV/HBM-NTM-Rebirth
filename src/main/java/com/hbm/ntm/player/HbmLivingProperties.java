package com.hbm.ntm.player;

import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.radiation.RadiationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.List;
import java.util.UUID;

public final class HbmLivingProperties {
    public static final String TAG_ROOT = "HbmLivingProps";
    @Deprecated(forRemoval = false)
    public static final String key = "NTM_EXT_LIVING";
    public static final UUID DIGAMMA_UUID = RadiationData.DIGAMMA_UUID;
    @Deprecated(forRemoval = false)
    public static final UUID digamma_UUID = DIGAMMA_UUID;
    public static final int MKU_CONTAGION_TICKS = 3 * 60 * 60 * 20;
    public static final int MAX_ASBESTOS = RadiationData.MAX_ASBESTOS;
    public static final int MAX_BLACK_LUNG = RadiationData.MAX_BLACK_LUNG;
    @Deprecated
    public static final int maxAsbestos = MAX_ASBESTOS;
    @Deprecated
    public static final int maxBlacklung = MAX_BLACK_LUNG;

    public static float getRadiation(LivingEntity entity) {
        return RadiationData.getRadiation(entity);
    }

    public static void setRadiation(LivingEntity entity, float radiation) {
        RadiationData.setRadiation(entity, radiation);
    }

    public static void incrementRadiation(LivingEntity entity, float amount) {
        RadiationData.incrementRadiation(entity, amount);
    }

    public static float addRadiation(LivingEntity entity, float amount) {
        incrementRadiation(entity, amount);
        return getRadiation(entity);
    }

    public static float reduceRadiation(LivingEntity entity, float amount) {
        incrementRadiation(entity, -Math.max(0.0F, amount));
        return getRadiation(entity);
    }

    public static void clearRadiation(LivingEntity entity) {
        setRadiation(entity, 0.0F);
    }

    public static float getDigamma(LivingEntity entity) {
        return RadiationData.getDigamma(entity);
    }

    public static void setDigamma(LivingEntity entity, float digamma) {
        RadiationData.setDigamma(entity, digamma);
    }

    public static void incrementDigamma(LivingEntity entity, float amount) {
        RadiationData.incrementDigamma(entity, amount);
    }

    public static float addDigamma(LivingEntity entity, float amount) {
        incrementDigamma(entity, amount);
        return getDigamma(entity);
    }

    public static float reduceDigamma(LivingEntity entity, float amount) {
        incrementDigamma(entity, -Math.max(0.0F, amount));
        return getDigamma(entity);
    }

    public static float capDigamma(LivingEntity entity, float maximum) {
        float cap = Math.max(0.0F, maximum);
        if (getDigamma(entity) > cap) {
            setDigamma(entity, cap);
        }
        return getDigamma(entity);
    }

    public static void clearDigamma(LivingEntity entity) {
        setDigamma(entity, 0.0F);
    }

    public static void applyDigammaModifier(LivingEntity entity) {
        RadiationData.applyDigammaModifier(entity);
    }

    public static float getRadEnv(LivingEntity entity) {
        return RadiationData.getRadEnv(entity);
    }

    public static void setRadEnv(LivingEntity entity, float radiation) {
        RadiationData.setRadEnv(entity, radiation);
    }

    public static float getRadBuf(LivingEntity entity) {
        return RadiationData.getRadBuf(entity);
    }

    public static void setRadBuf(LivingEntity entity, float radiation) {
        RadiationData.setRadBuf(entity, radiation);
    }

    public static void flushEnvironmentBuffer(LivingEntity entity) {
        RadiationData.flushEnvironmentBuffer(entity);
    }

    public static List<ContaminationEffect> getCont(LivingEntity entity) {
        return new ContaminationList(entity);
    }

    public static void addCont(LivingEntity entity, ContaminationEffect effect) {
        if (effect != null) {
            RadiationData.addContamination(entity, effect.maxRad, effect.maxTime, effect.time, effect.ignoreArmor);
        }
    }

    public static void addCont(LivingEntity entity, float radiation, int time, boolean ignoreArmor) {
        addCont(entity, new ContaminationEffect(radiation, time, ignoreArmor));
    }

    public static int clearCont(LivingEntity entity) {
        return RadiationData.clearContamination(entity);
    }

    public static int getContaminationCount(LivingEntity entity) {
        return RadiationData.getContaminationCount(entity);
    }

    public static boolean removeCont(LivingEntity entity, int index) {
        return RadiationData.removeContamination(entity, index);
    }

    public static List<ContaminationEffect> tickContamination(LivingEntity entity) {
        return RadiationData.tickContamination(entity).stream()
                .map(HbmLivingProperties::fromData)
                .toList();
    }

    public static List<ContaminationEffect> getContaminationEffectsForSync(LivingEntity entity) {
        return RadiationData.getContaminationEffects(entity).stream()
                .map(HbmLivingProperties::fromData)
                .toList();
    }

    public static SyncData writeSyncedData(LivingEntity entity, float chunkRadiation, float resistance) {
        return new SyncData(
                getRadiation(entity),
                getDigamma(entity),
                getRadBuf(entity),
                chunkRadiation,
                resistance,
                getAsbestos(entity),
                getBlackLung(entity),
                getBombTimer(entity),
                getContagion(entity),
                getOil(entity),
                getFire(entity),
                getPhosphorus(entity),
                getBalefire(entity),
                getBlackFire(entity),
                getContaminationEffectsForSync(entity));
    }

    public static CompoundTag writeSyncedDataTag(LivingEntity entity, float chunkRadiation, float resistance) {
        return writeSyncedData(entity, chunkRadiation, resistance).toTag();
    }

    public static void sync(ServerPlayer player, float chunkRadiation, float resistance) {
        ModMessages.syncPlayerRadiation(player, chunkRadiation, resistance);
    }

    public static void syncThreaded(ServerPlayer player, float chunkRadiation, float resistance) {
        ModMessages.syncPlayerRadiationThreaded(player, chunkRadiation, resistance);
    }

    public static SyncData emptySyncedData() {
        return new SyncData(0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                0, 0, 0, 0, 0, 0, 0, 0, 0, List.of());
    }

    public static LegacySyncData writeLegacySyncedData(LivingEntity entity) {
        return new LegacySyncData(
                getRadiation(entity),
                getDigamma(entity),
                getAsbestos(entity),
                getBombTimer(entity),
                getContagion(entity),
                getBlackLung(entity),
                getOil(entity),
                getContaminationEffectsForSync(entity));
    }

    public static LegacySyncData emptyLegacySyncedData() {
        return new LegacySyncData(0.0F, 0.0F, 0, 0, 0, 0, 0, List.of());
    }

    public static void encodeSyncedData(SyncData data, FriendlyByteBuf buffer) {
        SyncData safeData = data == null ? emptySyncedData() : data;
        buffer.writeFloat(safeData.radiation());
        buffer.writeFloat(safeData.digamma());
        buffer.writeFloat(safeData.radBuf());
        buffer.writeFloat(safeData.chunkRadiation());
        buffer.writeFloat(safeData.resistance());
        buffer.writeVarInt(safeData.asbestos());
        buffer.writeVarInt(safeData.blackLung());
        buffer.writeVarInt(safeData.bombTimer());
        buffer.writeVarInt(safeData.contagion());
        buffer.writeVarInt(safeData.oil());
        buffer.writeVarInt(safeData.fire());
        buffer.writeVarInt(safeData.phosphorus());
        buffer.writeVarInt(safeData.balefire());
        buffer.writeVarInt(safeData.blackFire());
        buffer.writeVarInt(safeData.contaminationEffects().size());
        for (ContaminationEffect effect : safeData.contaminationEffects()) {
            encodeContaminationEffect(effect, buffer);
        }
    }

    public static SyncData decodeSyncedData(FriendlyByteBuf buffer) {
        float radiation = buffer.readFloat();
        float digamma = buffer.readFloat();
        float radBuf = buffer.readFloat();
        float chunkRadiation = buffer.readFloat();
        float resistance = buffer.readFloat();
        int asbestos = buffer.readVarInt();
        int blackLung = buffer.readVarInt();
        int bombTimer = buffer.readVarInt();
        int contagion = buffer.readVarInt();
        int oil = buffer.readVarInt();
        int fire = buffer.readVarInt();
        int phosphorus = buffer.readVarInt();
        int balefire = buffer.readVarInt();
        int blackFire = buffer.readVarInt();
        int contaminationCount = buffer.readVarInt();
        List<ContaminationEffect> contaminationEffects = new ArrayList<>(contaminationCount);
        for (int i = 0; i < contaminationCount; i++) {
            contaminationEffects.add(decodeContaminationEffect(buffer));
        }
        return new SyncData(radiation, digamma, radBuf, chunkRadiation, resistance,
                asbestos, blackLung, bombTimer, contagion, oil, fire, phosphorus, balefire, blackFire,
                contaminationEffects);
    }

    public static void encodeLegacySyncedData(LegacySyncData data, FriendlyByteBuf buffer) {
        LegacySyncData safeData = data == null ? emptyLegacySyncedData() : data;
        buffer.writeFloat(safeData.radiation());
        buffer.writeFloat(safeData.digamma());
        buffer.writeInt(safeData.asbestos());
        buffer.writeInt(safeData.bombTimer());
        buffer.writeInt(safeData.contagion());
        buffer.writeInt(safeData.blackLung());
        buffer.writeInt(safeData.oil());
        buffer.writeInt(safeData.contaminationEffects().size());
        for (ContaminationEffect effect : safeData.contaminationEffects()) {
            encodeLegacyContaminationEffect(effect, buffer);
        }
    }

    public static LegacySyncData decodeLegacySyncedData(FriendlyByteBuf buffer) {
        if (buffer == null || buffer.readableBytes() <= 0) {
            return emptyLegacySyncedData();
        }
        float radiation = buffer.readFloat();
        float digamma = buffer.readFloat();
        int asbestos = buffer.readInt();
        int bombTimer = buffer.readInt();
        int contagion = buffer.readInt();
        int blackLung = buffer.readInt();
        int oil = buffer.readInt();
        int contaminationCount = buffer.readInt();
        List<ContaminationEffect> contaminationEffects = new ArrayList<>(contaminationCount);
        for (int i = 0; i < contaminationCount; i++) {
            contaminationEffects.add(decodeLegacyContaminationEffect(buffer));
        }
        return new LegacySyncData(radiation, digamma, asbestos, bombTimer, contagion, blackLung, oil, contaminationEffects);
    }

    public static SyncData readSyncedData(CompoundTag data) {
        CompoundTag safeData = data == null ? new CompoundTag() : data;
        return new SyncData(
                safeData.getFloat("hfr_radiation"),
                safeData.getFloat("hfr_digamma"),
                safeData.getFloat("hfr_rad_buf"),
                safeData.getFloat("chunkRadiation"),
                safeData.getFloat("resistance"),
                safeData.getInt("hfr_asbestos"),
                safeData.getInt("hfr_blacklung"),
                safeData.getInt("hfr_bomb"),
                safeData.getInt("hfr_contagion"),
                safeData.getInt("hfr_oil"),
                safeData.getInt("hfr_fire"),
                safeData.getInt("hfr_phosphorus"),
                safeData.getInt("hfr_balefire"),
                safeData.getInt("hfr_blackfire"),
                contaminationEffectsFromTag(safeData.getList("contamination", Tag.TAG_COMPOUND)));
    }

    public static void serializeSyncedData(LivingEntity entity, FriendlyByteBuf buffer, float chunkRadiation, float resistance) {
        encodeSyncedData(writeSyncedData(entity, chunkRadiation, resistance), buffer);
    }

    public static void deserializeSyncedData(LivingEntity entity, FriendlyByteBuf buffer) {
        applySyncedData(entity, decodeSyncedData(buffer));
    }

    public static void deserializeSyncedData(LivingEntity entity, CompoundTag data) {
        applySyncedData(entity, readSyncedData(data));
    }

    public static void serializeLegacySyncedData(LivingEntity entity, FriendlyByteBuf buffer) {
        encodeLegacySyncedData(writeLegacySyncedData(entity), buffer);
    }

    public static void deserializeLegacySyncedData(LivingEntity entity, FriendlyByteBuf buffer) {
        applyLegacySyncedData(entity, decodeLegacySyncedData(buffer));
    }

    public static void applySyncedData(LivingEntity entity, SyncData data) {
        if (entity == null) {
            return;
        }
        SyncData safeData = data == null ? emptySyncedData() : data;
        RadiationData.applySyncedData(entity,
                safeData.radiation(),
                safeData.digamma(),
                safeData.radBuf(),
                safeData.asbestos(),
                safeData.blackLung(),
                safeData.bombTimer(),
                safeData.contagion(),
                safeData.oil(),
                safeData.fire(),
                safeData.phosphorus(),
                safeData.balefire(),
                safeData.blackFire(),
                toListTag(safeData.contaminationEffects()));
    }

    public static void applyLegacySyncedData(LivingEntity entity, LegacySyncData data) {
        if (entity == null) {
            return;
        }
        LegacySyncData safeData = data == null ? emptyLegacySyncedData() : data;
        RadiationData.applyLegacySyncedData(entity,
                safeData.radiation(),
                safeData.digamma(),
                safeData.asbestos(),
                safeData.bombTimer(),
                safeData.contagion(),
                safeData.blackLung(),
                safeData.oil(),
                toListTag(safeData.contaminationEffects()));
    }

    public static void encodeContaminationEffect(ContaminationEffect effect, FriendlyByteBuf buffer) {
        ContaminationEffect safeEffect = effect == null ? new ContaminationEffect(0.0F, 1, 0, false) : effect;
        buffer.writeFloat(safeEffect.maxRad);
        buffer.writeVarInt(safeEffect.maxTime);
        buffer.writeVarInt(safeEffect.time);
        buffer.writeBoolean(safeEffect.ignoresArmor());
    }

    public static ContaminationEffect decodeContaminationEffect(FriendlyByteBuf buffer) {
        return new ContaminationEffect(
                buffer.readFloat(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readBoolean());
    }

    public static void encodeLegacyContaminationEffect(ContaminationEffect effect, FriendlyByteBuf buffer) {
        ContaminationEffect safeEffect = effect == null ? new ContaminationEffect(0.0F, 1, 0, false) : effect;
        buffer.writeFloat(safeEffect.maxRad);
        buffer.writeInt(safeEffect.maxTime);
        buffer.writeInt(safeEffect.time);
        buffer.writeBoolean(safeEffect.ignoresArmor());
    }

    public static ContaminationEffect decodeLegacyContaminationEffect(FriendlyByteBuf buffer) {
        return new ContaminationEffect(
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean());
    }

    public static int getAsbestos(LivingEntity entity) {
        return RadiationData.getAsbestos(entity);
    }

    public static void setAsbestos(LivingEntity entity, int asbestos) {
        RadiationData.setAsbestos(entity, asbestos);
    }

    public static void incrementAsbestos(LivingEntity entity, int amount) {
        RadiationData.incrementAsbestos(entity, amount);
    }

    public static void ensureAsbestos(LivingEntity entity, int minimum) {
        if (getAsbestos(entity) < minimum) {
            setAsbestos(entity, minimum);
        }
    }

    public static int reduceAsbestos(LivingEntity entity, int amount) {
        setAsbestos(entity, getAsbestos(entity) - Math.max(0, amount));
        return getAsbestos(entity);
    }

    public static int capAsbestos(LivingEntity entity, int maximum) {
        int cap = Math.max(0, maximum);
        if (getAsbestos(entity) > cap) {
            setAsbestos(entity, cap);
        }
        return getAsbestos(entity);
    }

    public static void clearAsbestos(LivingEntity entity) {
        setAsbestos(entity, 0);
    }

    public static int getBlackLung(LivingEntity entity) {
        return RadiationData.getBlackLung(entity);
    }

    public static void setBlackLung(LivingEntity entity, int blackLung) {
        RadiationData.setBlackLung(entity, blackLung);
    }

    public static void incrementBlackLung(LivingEntity entity, int amount) {
        RadiationData.incrementBlackLung(entity, amount);
    }

    public static void ensureBlackLung(LivingEntity entity, int minimum) {
        if (getBlackLung(entity) < minimum) {
            setBlackLung(entity, minimum);
        }
    }

    public static int reduceBlackLung(LivingEntity entity, int amount) {
        setBlackLung(entity, getBlackLung(entity) - Math.max(0, amount));
        return getBlackLung(entity);
    }

    public static int capBlackLung(LivingEntity entity, int maximum) {
        int cap = Math.max(0, maximum);
        if (getBlackLung(entity) > cap) {
            setBlackLung(entity, cap);
        }
        return getBlackLung(entity);
    }

    public static void clearBlackLung(LivingEntity entity) {
        setBlackLung(entity, 0);
    }

    public static int getTimer(LivingEntity entity) {
        return RadiationData.getBombTimer(entity);
    }

    public static void setTimer(LivingEntity entity, int bombTimer) {
        RadiationData.setBombTimer(entity, bombTimer);
    }

    public static int addTimer(LivingEntity entity, int amount) {
        int timer = Math.max(0, getTimer(entity) + amount);
        setTimer(entity, timer);
        return timer;
    }

    public static int decrementTimer(LivingEntity entity) {
        return addTimer(entity, -1);
    }

    public static void ensureTimer(LivingEntity entity, int minimum) {
        if (getTimer(entity) < minimum) {
            setTimer(entity, minimum);
        }
    }

    public static void clearTimer(LivingEntity entity) {
        setTimer(entity, 0);
    }

    public static int getBombTimer(LivingEntity entity) {
        return RadiationData.getBombTimer(entity);
    }

    public static void setBombTimer(LivingEntity entity, int bombTimer) {
        RadiationData.setBombTimer(entity, bombTimer);
    }

    public static int decrementBombTimer(LivingEntity entity) {
        return addTimer(entity, -1);
    }

    public static void clearBombTimer(LivingEntity entity) {
        setBombTimer(entity, 0);
    }

    public static int getContagion(LivingEntity entity) {
        if (!ServerConfig.mkuEnabled()) {
            return 0;
        }
        return RadiationData.getContagion(entity);
    }

    public static void setContagion(LivingEntity entity, int contagion) {
        RadiationData.setContagion(entity, contagion);
    }

    public static int addContagion(LivingEntity entity, int amount) {
        int contagion = Math.max(0, RadiationData.getContagion(entity) + amount);
        setContagion(entity, contagion);
        return contagion;
    }

    public static void ensureContagion(LivingEntity entity, int minimum) {
        if (RadiationData.getContagion(entity) < minimum) {
            setContagion(entity, minimum);
        }
    }

    public static void applyMkuContagion(LivingEntity entity) {
        setContagion(entity, MKU_CONTAGION_TICKS);
    }

    public static int decrementContagion(LivingEntity entity) {
        return addContagion(entity, -1);
    }

    public static int reduceContagion(LivingEntity entity, int amount) {
        return addContagion(entity, -Math.max(0, amount));
    }

    public static void clearContagion(LivingEntity entity) {
        setContagion(entity, 0);
    }

    public static int getOil(LivingEntity entity) {
        return RadiationData.getOil(entity);
    }

    public static void setOil(LivingEntity entity, int oil) {
        RadiationData.setOil(entity, oil);
    }

    public static void addOil(LivingEntity entity, int amount) {
        setOil(entity, getOil(entity) + amount);
    }

    public static void ensureOil(LivingEntity entity, int minimum) {
        if (getOil(entity) < minimum) {
            setOil(entity, minimum);
        }
    }

    public static int decrementOil(LivingEntity entity) {
        setOil(entity, getOil(entity) - 1);
        return getOil(entity);
    }

    public static int reduceOil(LivingEntity entity, int amount) {
        setOil(entity, getOil(entity) - Math.max(0, amount));
        return getOil(entity);
    }

    public static void clearOil(LivingEntity entity) {
        setOil(entity, 0);
    }

    public static int getFire(LivingEntity entity) {
        return RadiationData.getFire(entity);
    }

    public static void setFire(LivingEntity entity, int fire) {
        RadiationData.setFire(entity, fire);
    }

    public static void addFire(LivingEntity entity, int amount) {
        setFire(entity, getFire(entity) + amount);
    }

    public static void ensureFire(LivingEntity entity, int minimum) {
        if (getFire(entity) < minimum) {
            setFire(entity, minimum);
        }
    }

    public static int decrementFire(LivingEntity entity) {
        setFire(entity, getFire(entity) - 1);
        return getFire(entity);
    }

    public static int reduceFire(LivingEntity entity, int amount) {
        setFire(entity, getFire(entity) - Math.max(0, amount));
        return getFire(entity);
    }

    public static void clearFire(LivingEntity entity) {
        setFire(entity, 0);
    }

    public static int getPhosphorus(LivingEntity entity) {
        return RadiationData.getPhosphorus(entity);
    }

    public static void setPhosphorus(LivingEntity entity, int phosphorus) {
        RadiationData.setPhosphorus(entity, phosphorus);
    }

    public static void addPhosphorus(LivingEntity entity, int amount) {
        setPhosphorus(entity, getPhosphorus(entity) + amount);
    }

    public static void ensurePhosphorus(LivingEntity entity, int minimum) {
        if (getPhosphorus(entity) < minimum) {
            setPhosphorus(entity, minimum);
        }
    }

    public static int decrementPhosphorus(LivingEntity entity) {
        setPhosphorus(entity, getPhosphorus(entity) - 1);
        return getPhosphorus(entity);
    }

    public static int reducePhosphorus(LivingEntity entity, int amount) {
        setPhosphorus(entity, getPhosphorus(entity) - Math.max(0, amount));
        return getPhosphorus(entity);
    }

    public static void clearPhosphorus(LivingEntity entity) {
        setPhosphorus(entity, 0);
    }

    public static int getBalefire(LivingEntity entity) {
        return RadiationData.getBalefire(entity);
    }

    public static void setBalefire(LivingEntity entity, int balefire) {
        RadiationData.setBalefire(entity, balefire);
    }

    public static void addBalefire(LivingEntity entity, int amount) {
        setBalefire(entity, getBalefire(entity) + amount);
    }

    public static void ensureBalefire(LivingEntity entity, int minimum) {
        if (getBalefire(entity) < minimum) {
            setBalefire(entity, minimum);
        }
    }

    public static int decrementBalefire(LivingEntity entity) {
        setBalefire(entity, getBalefire(entity) - 1);
        return getBalefire(entity);
    }

    public static int reduceBalefire(LivingEntity entity, int amount) {
        setBalefire(entity, getBalefire(entity) - Math.max(0, amount));
        return getBalefire(entity);
    }

    public static void clearBalefire(LivingEntity entity) {
        setBalefire(entity, 0);
    }

    public static int getBlackFire(LivingEntity entity) {
        return RadiationData.getBlackFire(entity);
    }

    public static void setBlackFire(LivingEntity entity, int blackFire) {
        RadiationData.setBlackFire(entity, blackFire);
    }

    public static void addBlackFire(LivingEntity entity, int amount) {
        setBlackFire(entity, getBlackFire(entity) + amount);
    }

    public static void ensureBlackFire(LivingEntity entity, int minimum) {
        if (getBlackFire(entity) < minimum) {
            setBlackFire(entity, minimum);
        }
    }

    public static int decrementBlackFire(LivingEntity entity) {
        setBlackFire(entity, getBlackFire(entity) - 1);
        return getBlackFire(entity);
    }

    public static int reduceBlackFire(LivingEntity entity, int amount) {
        setBlackFire(entity, getBlackFire(entity) - Math.max(0, amount));
        return getBlackFire(entity);
    }

    public static void clearBlackFire(LivingEntity entity) {
        setBlackFire(entity, 0);
    }

    public static int ensureBlackFireOrAdd(LivingEntity entity, int minimum, int amount) {
        int blackFire = getBlackFire(entity);
        if (blackFire < minimum) {
            setBlackFire(entity, minimum);
            return minimum;
        }
        addBlackFire(entity, amount);
        return getBlackFire(entity);
    }

    public static boolean hasTemperatureEffects(LivingEntity entity) {
        return getFire(entity) > 0
                || getPhosphorus(entity) > 0
                || getBalefire(entity) > 0
                || getBlackFire(entity) > 0;
    }

    public static void clearTemperatureEffects(LivingEntity entity) {
        clearFire(entity);
        clearPhosphorus(entity);
        clearBalefire(entity);
        clearBlackFire(entity);
    }

    public static void copyForRespawn(LivingEntity original, LivingEntity replacement) {
        RadiationData.copyForRespawn(original, replacement);
    }

    public static CompoundTag writePersistentData(LivingEntity entity) {
        return RadiationData.writePersistentData(entity);
    }

    public static void readPersistentData(LivingEntity entity, CompoundTag data) {
        RadiationData.readPersistentData(entity, data);
    }

    public static void saveNBTData(LivingEntity entity, CompoundTag nbt) {
        RadiationData.saveNBTData(entity, nbt);
    }

    public static void loadNBTData(LivingEntity entity, CompoundTag nbt) {
        RadiationData.loadNBTData(entity, nbt);
    }

    private static ListTag toListTag(List<ContaminationEffect> effects) {
        ListTag tag = new ListTag();
        if (effects == null) {
            return tag;
        }
        for (ContaminationEffect effect : effects) {
            if (effect != null) {
                tag.add(effect.toTag());
            }
        }
        return tag;
    }

    private static List<ContaminationEffect> contaminationEffectsFromTag(ListTag tag) {
        List<ContaminationEffect> effects = new ArrayList<>(tag.size());
        for (int i = 0; i < tag.size(); i++) {
            effects.add(ContaminationEffect.fromTag(tag.getCompound(i)));
        }
        return effects;
    }

    private static ContaminationEffect fromData(RadiationData.ContaminationEffect effect) {
        return new ContaminationEffect(effect.maxRad(), effect.maxTime(), effect.time(), effect.ignoreArmor());
    }

    public static final class ContaminationEffect {
        public float maxRad;
        public int maxTime;
        public int time;
        public boolean ignoreArmor;

        public ContaminationEffect(float radiation, int time, boolean ignoreArmor) {
            this(radiation, time, time, ignoreArmor);
        }

        public ContaminationEffect(float maxRad, int maxTime, int time, boolean ignoreArmor) {
            this.maxRad = maxRad;
            this.maxTime = Math.max(1, maxTime);
            this.time = Math.max(0, time);
            this.ignoreArmor = ignoreArmor;
        }

        public float getRad() {
            return maxRad * ((float) time / (float) Math.max(1, maxTime));
        }

        public float currentRadiation() {
            return getRad();
        }

        public boolean ignoresArmor() {
            return ignoreArmor;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("maxRad", maxRad);
            tag.putInt("maxTime", Math.max(1, maxTime));
            tag.putInt("time", Math.max(0, time));
            tag.putBoolean("ignoreArmor", ignoreArmor);
            return tag;
        }

        public static ContaminationEffect fromTag(CompoundTag tag) {
            CompoundTag safeTag = tag == null ? new CompoundTag() : tag;
            return new ContaminationEffect(
                    safeTag.getFloat("maxRad"),
                    Math.max(1, safeTag.getInt("maxTime")),
                    Math.max(0, safeTag.getInt("time")),
                    safeTag.getBoolean("ignoreArmor"));
        }
    }

    public record SyncData(float radiation, float digamma, float radBuf, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire,
            List<ContaminationEffect> contaminationEffects) {
        public SyncData {
            contaminationEffects = contaminationEffects == null ? List.of() : List.copyOf(contaminationEffects);
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("hfr_radiation", radiation);
            tag.putFloat("hfr_digamma", digamma);
            tag.putFloat("hfr_rad_buf", radBuf);
            tag.putFloat("chunkRadiation", chunkRadiation);
            tag.putFloat("resistance", resistance);
            tag.putInt("hfr_asbestos", asbestos);
            tag.putInt("hfr_blacklung", blackLung);
            tag.putInt("hfr_bomb", bombTimer);
            tag.putInt("hfr_contagion", contagion);
            tag.putInt("hfr_oil", oil);
            tag.putInt("hfr_fire", fire);
            tag.putInt("hfr_phosphorus", phosphorus);
            tag.putInt("hfr_balefire", balefire);
            tag.putInt("hfr_blackfire", blackFire);
            tag.put("contamination", toListTag(contaminationEffects));
            return tag;
        }
    }

    public record LegacySyncData(float radiation, float digamma, int asbestos, int bombTimer, int contagion,
            int blackLung, int oil, List<ContaminationEffect> contaminationEffects) {
        public LegacySyncData {
            contaminationEffects = contaminationEffects == null ? List.of() : List.copyOf(contaminationEffects);
        }

        public CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("hfr_radiation", radiation);
            tag.putFloat("hfr_digamma", digamma);
            tag.putInt("hfr_asbestos", asbestos);
            tag.putInt("hfr_bomb", bombTimer);
            tag.putInt("hfr_contagion", contagion);
            tag.putInt("hfr_blacklung", blackLung);
            tag.putInt("hfr_oil", oil);
            tag.put("contamination", toListTag(contaminationEffects));
            return tag;
        }
    }

    private static final class ContaminationList extends AbstractList<ContaminationEffect> {
        private final LivingEntity entity;

        private ContaminationList(LivingEntity entity) {
            this.entity = entity;
        }

        @Override
        public ContaminationEffect get(int index) {
            return fromData(RadiationData.getContaminationEffects(entity).get(index));
        }

        @Override
        public int size() {
            return RadiationData.getContaminationCount(entity);
        }

        @Override
        public void add(int index, ContaminationEffect element) {
            List<ContaminationEffect> effects = snapshot();
            effects.add(index, element);
            RadiationData.setContamination(entity, toListTag(effects));
        }

        @Override
        public ContaminationEffect set(int index, ContaminationEffect element) {
            List<ContaminationEffect> effects = snapshot();
            ContaminationEffect previous = effects.set(index, element);
            RadiationData.setContamination(entity, toListTag(effects));
            return previous;
        }

        @Override
        public ContaminationEffect remove(int index) {
            List<ContaminationEffect> effects = snapshot();
            ContaminationEffect previous = effects.remove(index);
            RadiationData.setContamination(entity, toListTag(effects));
            return previous;
        }

        @Override
        public void clear() {
            RadiationData.clearContamination(entity);
        }

        private List<ContaminationEffect> snapshot() {
            return RadiationData.getContaminationEffects(entity).stream()
                    .map(HbmLivingProperties::fromData)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        }
    }

    private HbmLivingProperties() {
    }
}
