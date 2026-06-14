package com.hbm.extprop;

import com.hbm.ntm.client.ClientHbmLivingProperties;

import java.util.List;

/**
 * Client-side read facade for migrated HUD/render code. Server authority still
 * lives in HbmLivingProperties/RadiationData.
 */
public final class ClientHbmLivingProps {
    public static float getRadiation() {
        return ClientHbmLivingProperties.getRadiation();
    }

    public static float getDigamma() {
        return ClientHbmLivingProperties.getDigamma();
    }

    public static float getRadBuf() {
        return ClientHbmLivingProperties.getRadBuf();
    }

    public static float getRadEnv() {
        return ClientHbmLivingProperties.getEnvironment();
    }

    public static float getChunkRadiation() {
        return ClientHbmLivingProperties.getChunkRadiation();
    }

    public static float getResistance() {
        return ClientHbmLivingProperties.getResistance();
    }

    public static int getAsbestos() {
        return ClientHbmLivingProperties.getAsbestos();
    }

    public static int getBlackLung() {
        return ClientHbmLivingProperties.getBlackLung();
    }

    public static int getTimer() {
        return ClientHbmLivingProperties.getTimer();
    }

    public static int getContagion() {
        return ClientHbmLivingProperties.getContagion();
    }

    public static int getOil() {
        return ClientHbmLivingProperties.getOil();
    }

    public static int getFire() {
        return ClientHbmLivingProperties.getFire();
    }

    public static int getPhosphorus() {
        return ClientHbmLivingProperties.getPhosphorus();
    }

    public static int getBalefire() {
        return ClientHbmLivingProperties.getBalefire();
    }

    public static int getBlackFire() {
        return ClientHbmLivingProperties.getBlackFire();
    }

    public static List<ClientHbmLivingProperties.ContaminationEffectData> getCont() {
        return ClientHbmLivingProperties.getCont();
    }

    public static ClientHbmLivingProperties.ClientLivingSyncData snapshot() {
        return ClientHbmLivingProperties.snapshot();
    }

    private ClientHbmLivingProps() {
    }
}
