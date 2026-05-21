package com.hbm.ntm.client;

import java.util.List;

public final class ClientRadiationData {
    private static float radiation;
    private static float digamma;
    private static float environment;
    private static float chunkRadiation;
    private static float resistance;
    private static int asbestos;
    private static int blackLung;
    private static int bombTimer;
    private static int contagion;
    private static int oil;
    private static int fire;
    private static int phosphorus;
    private static int balefire;
    private static int blackFire;
    private static List<ContaminationEffectData> contaminationEffects = List.of();

    public static void update(PlayerRadiationSyncData data) {
        update(data.radiation(), data.digamma(), data.environment(), data.chunkRadiation(), data.resistance(),
                data.asbestos(), data.blackLung(), data.bombTimer(), data.contagion(), data.oil(),
                data.fire(), data.phosphorus(), data.balefire(), data.blackFire(), data.contaminationEffects());
    }

    public static void update(float radiation, float digamma, float environment, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire,
            List<ContaminationEffectData> contaminationEffects) {
        ClientRadiationData.radiation = radiation;
        ClientRadiationData.digamma = digamma;
        ClientRadiationData.environment = environment;
        ClientRadiationData.chunkRadiation = chunkRadiation;
        ClientRadiationData.resistance = resistance;
        ClientRadiationData.asbestos = asbestos;
        ClientRadiationData.blackLung = blackLung;
        ClientRadiationData.bombTimer = bombTimer;
        ClientRadiationData.contagion = contagion;
        ClientRadiationData.oil = oil;
        ClientRadiationData.fire = fire;
        ClientRadiationData.phosphorus = phosphorus;
        ClientRadiationData.balefire = balefire;
        ClientRadiationData.blackFire = blackFire;
        ClientRadiationData.contaminationEffects = List.copyOf(contaminationEffects);
    }

    public static float getRadiation() {
        return radiation;
    }

    public static float getDigamma() {
        return digamma;
    }

    public static float getEnvironment() {
        return environment;
    }

    public static float getChunkRadiation() {
        return chunkRadiation;
    }

    public static float getResistance() {
        return resistance;
    }

    public static int getAsbestos() {
        return asbestos;
    }

    public static int getBlackLung() {
        return blackLung;
    }

    public static int getBombTimer() {
        return bombTimer;
    }

    public static int getContagion() {
        return contagion;
    }

    public static int getOil() {
        return oil;
    }

    public static int getFire() {
        return fire;
    }

    public static int getPhosphorus() {
        return phosphorus;
    }

    public static int getBalefire() {
        return balefire;
    }

    public static int getBlackFire() {
        return blackFire;
    }

    public static List<ContaminationEffectData> getContaminationEffects() {
        return contaminationEffects;
    }

    public static int getContaminationCount() {
        return contaminationEffects.size();
    }

    private ClientRadiationData() {
    }

    public record PlayerRadiationSyncData(float radiation, float digamma, float environment, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire,
            List<ContaminationEffectData> contaminationEffects) {
        public PlayerRadiationSyncData {
            contaminationEffects = List.copyOf(contaminationEffects);
        }
    }

    public record ContaminationEffectData(float maxRad, int maxTime, int time, boolean ignoreArmor) {
        public float currentRadiation() {
            return maxRad * ((float) time / (float) Math.max(1, maxTime));
        }
    }
}
