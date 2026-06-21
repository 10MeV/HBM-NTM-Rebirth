package com.hbm.ntm.client;

import com.hbm.ntm.player.HbmLivingProperties;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ClientHbmLivingProperties {
    private static final Map<ClientHbmLivingPropertiesListener, ClientRadiationDataListener> LISTENERS = new IdentityHashMap<>();

    public static void update(ClientLivingSyncData data) {
        ClientLivingSyncData safeData = data == null ? emptySyncData() : data;
        ClientRadiationData.update(safeData.radiation(), safeData.digamma(), safeData.radBuf(), safeData.chunkRadiation(), safeData.resistance(),
                safeData.asbestos(), safeData.blackLung(), safeData.bombTimer(), safeData.contagion(), safeData.oil(),
                safeData.fire(), safeData.phosphorus(), safeData.balefire(), safeData.blackFire(),
                safeData.contaminationEffects().stream()
                        .map(effect -> new ClientRadiationData.ContaminationEffectData(
                                effect.maxRad(), effect.maxTime(), effect.time(), effect.ignoreArmor()))
                        .toList());
    }

    public static void update(HbmLivingProperties.SyncData data) {
        HbmLivingProperties.SyncData safeData = data == null ? HbmLivingProperties.emptySyncedData() : data;
        update(new ClientLivingSyncData(safeData.radiation(), safeData.digamma(), safeData.radBuf(), safeData.chunkRadiation(), safeData.resistance(),
                safeData.asbestos(), safeData.blackLung(), safeData.bombTimer(), safeData.contagion(), safeData.oil(),
                safeData.fire(), safeData.phosphorus(), safeData.balefire(), safeData.blackFire(),
                safeData.contaminationEffects().stream()
                        .map(effect -> new ContaminationEffectData(effect.maxRad, effect.maxTime, effect.time, effect.ignoresArmor()))
                        .toList()));
    }

    public static float getRadiation() {
        return ClientRadiationData.getRadiation();
    }

    public static float getDigamma() {
        return ClientRadiationData.getDigamma();
    }

    public static float getRadBuf() {
        return ClientRadiationData.getEnvironment();
    }

    public static float getEnvironment() {
        return ClientRadiationData.getEnvironment();
    }

    public static float getChunkRadiation() {
        return ClientRadiationData.getChunkRadiation();
    }

    public static float getResistance() {
        return ClientRadiationData.getResistance();
    }

    public static int getAsbestos() {
        return ClientRadiationData.getAsbestos();
    }

    public static int getBlackLung() {
        return ClientRadiationData.getBlackLung();
    }

    public static int getBombTimer() {
        return ClientRadiationData.getBombTimer();
    }

    public static int getTimer() {
        return getBombTimer();
    }

    public static int getContagion() {
        return ClientRadiationData.getContagion();
    }

    public static int getOil() {
        return ClientRadiationData.getOil();
    }

    public static int getFire() {
        return ClientRadiationData.getFire();
    }

    public static int getPhosphorus() {
        return ClientRadiationData.getPhosphorus();
    }

    public static int getBalefire() {
        return ClientRadiationData.getBalefire();
    }

    public static int getBlackFire() {
        return ClientRadiationData.getBlackFire();
    }

    public static List<ContaminationEffectData> getContaminationEffects() {
        return ClientRadiationData.getContaminationEffects().stream()
                .map(effect -> new ContaminationEffectData(effect.maxRad(), effect.maxTime(), effect.time(), effect.ignoreArmor()))
                .toList();
    }

    public static List<ContaminationEffectData> getCont() {
        return getContaminationEffects();
    }

    public static int getContaminationCount() {
        return ClientRadiationData.getContaminationCount();
    }

    public static ClientLivingSyncData snapshot() {
        ClientRadiationData.PlayerRadiationSyncData data = ClientRadiationData.snapshot();
        return new ClientLivingSyncData(data.radiation(), data.digamma(), data.environment(), data.chunkRadiation(), data.resistance(),
                data.asbestos(), data.blackLung(), data.bombTimer(), data.contagion(), data.oil(),
                data.fire(), data.phosphorus(), data.balefire(), data.blackFire(), getContaminationEffects());
    }

    public static ClientLivingSyncData emptySyncData() {
        return new ClientLivingSyncData(0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                0, 0, 0, 0, 0, 0, 0, 0, 0, List.of());
    }

    public static void addListener(ClientHbmLivingPropertiesListener listener) {
        if (listener == null || LISTENERS.containsKey(listener)) {
            return;
        }
        ClientRadiationDataListener bridge = data -> listener.onClientHbmLivingProperties(toLivingSyncData(data));
        LISTENERS.put(listener, bridge);
        ClientRadiationData.addListener(bridge);
    }

    public static void removeListener(ClientHbmLivingPropertiesListener listener) {
        ClientRadiationDataListener bridge = LISTENERS.remove(listener);
        if (bridge != null) {
            ClientRadiationData.removeListener(bridge);
        }
    }

    public static void clearListeners() {
        LISTENERS.clear();
        ClientRadiationData.clearListeners();
    }

    public static void clearAll() {
        LISTENERS.clear();
        ClientRadiationData.clearAll();
    }

    public record ClientLivingSyncData(float radiation, float digamma, float radBuf, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire,
            List<ContaminationEffectData> contaminationEffects) {
        public ClientLivingSyncData {
            contaminationEffects = contaminationEffects == null ? List.of() : List.copyOf(contaminationEffects);
        }
    }

    public record ContaminationEffectData(float maxRad, int maxTime, int time, boolean ignoreArmor) {
        public float getRad() {
            return currentRadiation();
        }

        public float currentRadiation() {
            return maxRad * ((float) time / (float) Math.max(1, maxTime));
        }

        public boolean ignoresArmor() {
            return ignoreArmor;
        }
    }

    private ClientHbmLivingProperties() {
    }

    private static ClientLivingSyncData toLivingSyncData(ClientRadiationData.PlayerRadiationSyncData data) {
        return new ClientLivingSyncData(data.radiation(), data.digamma(), data.environment(), data.chunkRadiation(), data.resistance(),
                data.asbestos(), data.blackLung(), data.bombTimer(), data.contagion(), data.oil(),
                data.fire(), data.phosphorus(), data.balefire(), data.blackFire(),
                data.contaminationEffects().stream()
                        .map(effect -> new ContaminationEffectData(effect.maxRad(), effect.maxTime(), effect.time(), effect.ignoreArmor()))
                        .toList());
    }
}
