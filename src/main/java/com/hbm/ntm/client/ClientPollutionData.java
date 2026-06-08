package com.hbm.ntm.client;

import com.hbm.ntm.pollution.PollutionType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

public final class ClientPollutionData {
    public static final String TAG_POLLUTION = "pollution";
    public static final String TAG_SOOT = "soot";
    public static final String TAG_POISON = "poison";
    public static final String TAG_HEAVYMETAL = "heavymetal";
    public static final String TAG_FALLOUT = "fallout";

    private static float soot;
    private static float poison;
    private static float heavyMetal;
    private static float fallout;
    private static final List<ClientPollutionDataListener> LISTENERS = new ArrayList<>();

    public static void updateFromPermaSync(CompoundTag data) {
        CompoundTag pollution = data == null ? new CompoundTag() : data.getCompound(TAG_POLLUTION);
        update(
                pollution.getFloat(TAG_SOOT),
                pollution.getFloat(TAG_POISON),
                pollution.getFloat(TAG_HEAVYMETAL),
                pollution.getFloat(TAG_FALLOUT));
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

    public static float get(PollutionType type) {
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
        return get(PollutionType.values()[ordinal]);
    }

    public static float[] toArray() {
        float[] values = new float[PollutionType.values().length];
        copyInto(values);
        return values;
    }

    public static void copyInto(float[] target) {
        for (PollutionType type : PollutionType.values()) {
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
            return get(PollutionType.values()[ordinal]);
        }

        public float[] toArray() {
            float[] values = new float[PollutionType.values().length];
            for (PollutionType type : PollutionType.values()) {
                values[type.ordinal()] = get(type);
            }
            return values;
        }
    }

    private static float valueOrZero(float[] values, PollutionType type) {
        return values != null && type.ordinal() < values.length ? values[type.ordinal()] : 0.0F;
    }
}
