package com.hbm.ntm.client;

import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.satellite.Satellite;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

public final class ClientSatelliteData {
    public static Optional<SatelliteSnapshot> current() {
        return ClientPanelData.get(HbmNetworkActions.SATELLITE_PANEL)
                .map(ClientSatelliteData::readLegacyPanelSnapshot);
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

    private static SatelliteSnapshot readLegacyPanelSnapshot(ClientPanelData.PanelData data) {
        Satellite satellite = Satellite.create(data.legacyType());
        if (satellite == null) {
            return null;
        }
        CompoundTag tag = data.data() == null ? new CompoundTag() : data.data().copy();
        try {
            satellite.readFromNBT(tag);
        } catch (Exception ignored) {
        }
        return new SatelliteSnapshot(satellite, tag.copy());
    }

    private ClientSatelliteData() {
    }
}
