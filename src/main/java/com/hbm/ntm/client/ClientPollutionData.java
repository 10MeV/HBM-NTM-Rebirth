package com.hbm.ntm.client;

import com.hbm.ntm.pollution.PollutionSavedData;
import com.hbm.ntm.pollution.PollutionType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public final class ClientPollutionData {
    private static float soot;
    private static float poison;
    private static float heavyMetal;
    private static float fallout;
    private static final List<ClientPollutionDataListener> LISTENERS = new ArrayList<>();

    public static void updateFromPermaSync(CompoundTag data) {
        update(PollutionSavedData.readPermaSyncData(data));
    }

    public static void update(float soot, float poison, float heavyMetal, float fallout) {
        ClientPollutionData.soot = soot;
        ClientPollutionData.poison = poison;
        ClientPollutionData.heavyMetal = heavyMetal;
        ClientPollutionData.fallout = fallout;
        PlayerPollutionSyncData snapshot = snapshot();
        for (ClientPollutionDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientPollutionData(snapshot);
        }
    }

    public static void update(float[] values) {
        update(
                valueOrZero(values, PollutionType.SOOT),
                valueOrZero(values, PollutionType.POISON),
                valueOrZero(values, PollutionType.HEAVYMETAL),
                valueOrZero(values, PollutionType.FALLOUT));
    }

    public static void update(PollutionSavedData.PollutionSample sample) {
        update(
                sample == null ? 0.0F : sample.get(PollutionType.SOOT),
                sample == null ? 0.0F : sample.get(PollutionType.POISON),
                sample == null ? 0.0F : sample.get(PollutionType.HEAVYMETAL),
                sample == null ? 0.0F : sample.get(PollutionType.FALLOUT));
    }

    public static float get(PollutionType type) {
        if (type == null) {
            return 0.0F;
        }
        return switch (type) {
            case SOOT -> soot;
            case POISON -> poison;
            case HEAVYMETAL -> heavyMetal;
            case FALLOUT -> fallout;
        };
    }

    public static float getSoot() {
        return soot;
    }

    public static float getPoison() {
        return poison;
    }

    public static float getHeavyMetal() {
        return heavyMetal;
    }

    public static float getFallout() {
        return fallout;
    }

    public static float get(int ordinal) {
        return get(PollutionType.byOrdinal(ordinal));
    }

    public static float[] toArray() {
        float[] values = new float[PollutionType.count()];
        copyInto(values);
        return values;
    }

    public static void copyInto(float[] target) {
        if (target == null) {
            return;
        }
        for (PollutionType type : PollutionType.orderedValues()) {
            if (type.ordinal() < target.length) {
                target[type.ordinal()] = get(type);
            }
        }
    }

    public static PlayerPollutionSyncData snapshot() {
        return new PlayerPollutionSyncData(soot, poison, heavyMetal, fallout);
    }

    public static void addListener(ClientPollutionDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientPollutionDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    public static void clearAll() {
        soot = 0.0F;
        poison = 0.0F;
        heavyMetal = 0.0F;
        fallout = 0.0F;
        LISTENERS.clear();
    }

    private ClientPollutionData() {
    }

    public record PlayerPollutionSyncData(float soot, float poison, float heavyMetal, float fallout) {
        public float get(PollutionType type) {
            return switch (type) {
                case SOOT -> soot;
                case POISON -> poison;
                case HEAVYMETAL -> heavyMetal;
                case FALLOUT -> fallout;
            };
        }

        public float get(int ordinal) {
            return get(PollutionType.byOrdinal(ordinal));
        }

        public float[] toArray() {
            float[] values = new float[PollutionType.count()];
            for (PollutionType type : PollutionType.orderedValues()) {
                values[type.ordinal()] = get(type);
            }
            return values;
        }
    }

    private static float valueOrZero(float[] values, PollutionType type) {
        return values != null && type != null && type.ordinal() < values.length ? values[type.ordinal()] : 0.0F;
    }
}
