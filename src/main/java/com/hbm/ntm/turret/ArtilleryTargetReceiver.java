package com.hbm.ntm.turret;

import com.hbm.ntm.api.entity.RadarCommandReceiver;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;

public interface ArtilleryTargetReceiver extends RadarCommandReceiver, RORInteractive {
    boolean enqueueTarget(double x, double y, double z);
}
