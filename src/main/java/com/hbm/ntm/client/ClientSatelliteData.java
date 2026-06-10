package com.hbm.ntm.client;

import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.satellite.Satellite;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

public final class ClientSatelliteData {
    public static Optional<SatelliteSnapshot> current() {
        return ClientPanelData.get(HbmNetworkActions.SATELLITE_PANEL)
                .map(data -> {
                    Satellite satellite = Satellite.load(data.legacyType(), data.data());
                    return satellite == null ? null : new SatelliteSnapshot(satellite, data.data().copy());
                });
    }

    public static Optional<SatelliteSnapshot> current(int frequency) {
        return current().filter(snapshot -> snapshot.frequency() == frequency);
    }

    public record SatelliteSnapshot(Satellite satellite, CompoundTag data) {
        public int frequency() {
            return data.getInt("frequency");
        }

        public String legacyName() {
            return data.getString("legacyName");
        }
    }

    private ClientSatelliteData() {
    }
}
