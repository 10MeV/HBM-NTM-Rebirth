package com.hbm.ntm.client;

public final class ClientRadiationData {
    private static float radiation;
    private static float digamma;
    private static float environment;
    private static float chunkRadiation;
    private static float resistance;

    public static void update(float radiation, float digamma, float environment, float chunkRadiation, float resistance) {
        ClientRadiationData.radiation = radiation;
        ClientRadiationData.digamma = digamma;
        ClientRadiationData.environment = environment;
        ClientRadiationData.chunkRadiation = chunkRadiation;
        ClientRadiationData.resistance = resistance;
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

    private ClientRadiationData() {
    }
}
