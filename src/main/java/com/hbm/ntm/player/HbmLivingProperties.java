package com.hbm.ntm.player;

import com.hbm.ntm.config.ServerConfig;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.packet.PlayerRadiationSyncPacket;
import com.hbm.ntm.radiation.RadiationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.AbstractList;
import java.util.List;

public final class HbmLivingProperties {
    public static final String TAG_ROOT = "HbmLivingProps";
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

    public static float getDigamma(LivingEntity entity) {
        return RadiationData.getDigamma(entity);
    }

    public static void setDigamma(LivingEntity entity, float digamma) {
        RadiationData.setDigamma(entity, digamma);
    }

    public static void incrementDigamma(LivingEntity entity, float amount) {
        RadiationData.incrementDigamma(entity, amount);
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

    public static void sync(ServerPlayer player, float chunkRadiation, float resistance) {
        ModMessages.sendToPlayer(new PlayerRadiationSyncPacket(writeSyncedData(player, chunkRadiation, resistance)), player);
    }

    public static SyncData emptySyncedData() {
        return new SyncData(0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                0, 0, 0, 0, 0, 0, 0, 0, 0, List.of());
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

    public static int getAsbestos(LivingEntity entity) {
        return RadiationData.getAsbestos(entity);
    }

    public static void setAsbestos(LivingEntity entity, int asbestos) {
        RadiationData.setAsbestos(entity, asbestos);
    }

    public static void incrementAsbestos(LivingEntity entity, int amount) {
        RadiationData.incrementAsbestos(entity, amount);
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

    public static int getTimer(LivingEntity entity) {
        return RadiationData.getBombTimer(entity);
    }

    public static void setTimer(LivingEntity entity, int bombTimer) {
        RadiationData.setBombTimer(entity, bombTimer);
    }

    public static int getBombTimer(LivingEntity entity) {
        return RadiationData.getBombTimer(entity);
    }

    public static void setBombTimer(LivingEntity entity, int bombTimer) {
        RadiationData.setBombTimer(entity, bombTimer);
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

    public static void copyForRespawn(LivingEntity original, LivingEntity replacement) {
        RadiationData.copyForRespawn(original, replacement);
    }

    private static ListTag toListTag(List<ContaminationEffect> effects) {
        ListTag tag = new ListTag();
        for (ContaminationEffect effect : effects) {
            tag.add(effect.toTag());
        }
        return tag;
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
    }

    public record SyncData(float radiation, float digamma, float radBuf, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire,
            List<ContaminationEffect> contaminationEffects) {
        public SyncData {
            contaminationEffects = List.copyOf(contaminationEffects);
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
