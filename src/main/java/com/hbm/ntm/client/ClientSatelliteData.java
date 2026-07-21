package com.hbm.ntm.client;

import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.satellite.Satellite;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

public final class ClientSatelliteData {
    public static Optional<SatelliteSnapshot> current() {
        return ClientPanelData.get(HbmNetworkActions.SATELLITE_PANEL)
                // SatPanelPacket assigns ItemSatInterface.currentSat from
                // Satellite.create(type) inside its broad catch block. An
                // unknown legacy id therefore clears that old global to null
                // rather than crashing a later GUI read. Optional#map rejects
                // a null mapper result, whereas flatMap preserves that exact
                // absent-snapshot boundary.
                .flatMap(data -> Optional.ofNullable(readLegacyPanelSnapshot(data)));
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
        Satellite satellite;
        try {
            // SatPanelPacket creates the satellite inside the same broad
            // handler catch as readFromNBT.  Legacy Satellite.create can
            // throw for an out-of-range id, so keep that malformed-packet
            // boundary out of the later GUI/cache read as well.
            satellite = Satellite.create(data.legacyType());
        } catch (Exception ignored) {
            return null;
        }
        if (satellite == null) {
            return null;
        }
        CompoundTag tag = data.data() == null ? new CompoundTag() : data.data().copy();
        if (data.hasNbt()) {
            try {
                satellite.readFromNBT(tag);
            } catch (Exception ignored) {
            }
        }
        return new SatelliteSnapshot(satellite, tag.copy());
    }

    private ClientSatelliteData() {
    }
}
