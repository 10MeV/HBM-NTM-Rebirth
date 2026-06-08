package com.hbm.ntm.client;

import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public final class ClientTomImpactData {
    private static Level lastSyncLevel;
    private static float fire;
    private static float dust;
    private static boolean impact;
    private static final List<ClientTomImpactDataListener> LISTENERS = new ArrayList<>();

    public static void updateFromPermaSync(CompoundTag data) {
        TomImpactSavedData.Snapshot snapshot = TomImpactSavedData.readPermaSyncData(data);
        update(Minecraft.getInstance().level, snapshot);
    }

    public static void update(Level level, TomImpactSavedData.Snapshot snapshot) {
        lastSyncLevel = level;
        fire = snapshot == null ? 0.0F : snapshot.fire();
        dust = snapshot == null ? 0.0F : snapshot.dust();
        impact = snapshot != null && snapshot.impact();
        TomImpactSavedData.Snapshot current = snapshot();
        for (ClientTomImpactDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientTomImpactData(current);
        }
    }

    public static float getFireForClient(Level level) {
        return level == lastSyncLevel ? fire : 0.0F;
    }

    public static float getDustForClient(Level level) {
        return level == lastSyncLevel ? dust : 0.0F;
    }

    public static boolean getImpactForClient(Level level) {
        return level == lastSyncLevel && impact;
    }

    public static float getFire() {
        return fire;
    }

    public static float getDust() {
        return dust;
    }

    public static boolean getImpact() {
        return impact;
    }

    public static TomImpactSavedData.Snapshot snapshot() {
        return new TomImpactSavedData.Snapshot(dust, fire, impact);
    }

    public static void addListener(ClientTomImpactDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientTomImpactDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    public static void clearAll() {
        lastSyncLevel = null;
        fire = 0.0F;
        dust = 0.0F;
        impact = false;
        LISTENERS.clear();
    }

    private ClientTomImpactData() {
    }
}
