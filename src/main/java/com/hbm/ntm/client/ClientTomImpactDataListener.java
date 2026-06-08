package com.hbm.ntm.client;

import com.hbm.ntm.world.saveddata.TomImpactSavedData.Snapshot;

public interface ClientTomImpactDataListener {
    void onClientTomImpactData(Snapshot data);
}
