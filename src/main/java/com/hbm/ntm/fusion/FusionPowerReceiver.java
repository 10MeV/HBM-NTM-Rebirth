package com.hbm.ntm.fusion;

public interface FusionPowerReceiver {
    boolean receivesFusionPower();

    void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b);
}
