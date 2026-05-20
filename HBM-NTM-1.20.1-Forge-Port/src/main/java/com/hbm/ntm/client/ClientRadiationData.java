package com.hbm.ntm.client;

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

    public static void update(PlayerRadiationSyncData data) {
        update(data.radiation(), data.digamma(), data.environment(), data.chunkRadiation(), data.resistance(),
                data.asbestos(), data.blackLung(), data.bombTimer(), data.contagion(), data.oil(),
                data.fire(), data.phosphorus(), data.balefire(), data.blackFire());
    }

    public static void update(float radiation, float digamma, float environment, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire) {
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

    private ClientRadiationData() {
    }

    public record PlayerRadiationSyncData(float radiation, float digamma, float environment, float chunkRadiation, float resistance,
            int asbestos, int blackLung, int bombTimer, int contagion, int oil, int fire, int phosphorus, int balefire, int blackFire) {
    }
}
