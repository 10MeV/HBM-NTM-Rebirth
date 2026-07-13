package com.hbm.ntm.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.ntm.pollution.PollutionType;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PollutingFluidTrait extends FluidTrait {
    private final Map<PollutionKind, Float> releasePollution = new EnumMap<>(PollutionKind.class);
    private final Map<PollutionKind, Float> burnPollution = new EnumMap<>(PollutionKind.class);

    public PollutingFluidTrait release(PollutionKind kind, float amountPerMb) {
        releasePollution.put(kind, amountPerMb);
        return this;
    }

    public PollutingFluidTrait burn(PollutionKind kind, float amountPerMb) {
        burnPollution.put(kind, amountPerMb);
        return this;
    }

    public Map<PollutionKind, Float> getReleasePollution() {
        return Collections.unmodifiableMap(releasePollution);
    }

    public Map<PollutionKind, Float> getBurnPollution() {
        return Collections.unmodifiableMap(burnPollution);
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Polluting]").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        if (!releasePollution.isEmpty()) {
            info.add(Component.literal("When spilled:").withStyle(ChatFormatting.GREEN));
            for (Entry<PollutionKind, Float> entry : releasePollution.entrySet()) {
                info.add(Component.literal(" - " + entry.getValue() + " " + entry.getKey().legacyName() + " per mB")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
        if (!burnPollution.isEmpty()) {
            info.add(Component.literal("When burned:").withStyle(ChatFormatting.RED));
            for (Entry<PollutionKind, Float> entry : burnPollution.entrySet()) {
                info.add(Component.literal(" - " + entry.getValue() + " " + entry.getKey().legacyName() + " per mB")
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public void writeJson(JsonObject object) {
        JsonObject release = new JsonObject();
        for (Entry<PollutionKind, Float> entry : releasePollution.entrySet()) {
            release.addProperty(entry.getKey().legacyName(), entry.getValue());
        }
        object.add("release", release);

        JsonObject burn = new JsonObject();
        for (Entry<PollutionKind, Float> entry : burnPollution.entrySet()) {
            burn.addProperty(entry.getKey().legacyName(), entry.getValue());
        }
        object.add("burn", burn);
    }

    @Override
    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("release").beginObject();
        for (Entry<PollutionKind, Float> entry : releasePollution.entrySet()) {
            writer.name(entry.getKey().legacyName()).value(entry.getValue());
        }
        writer.endObject();
        writer.name("burn").beginObject();
        for (Entry<PollutionKind, Float> entry : burnPollution.entrySet()) {
            writer.name(entry.getKey().legacyName()).value(entry.getValue());
        }
        writer.endObject();
    }

    @Override
    public void deserializeJSON(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("release")) {
            JsonObject release = object.getAsJsonObject("release");
            for (PollutionKind kind : PollutionKind.orderedValues()) {
                if (release.has(kind.legacyName())) {
                    release(kind, release.get(kind.legacyName()).getAsFloat());
                }
            }
        }
        if (object.has("burn")) {
            JsonObject burn = object.getAsJsonObject("burn");
            for (PollutionKind kind : PollutionKind.orderedValues()) {
                if (burn.has(kind.legacyName())) {
                    burn(kind, burn.get(kind.legacyName()).getAsFloat());
                }
            }
        }
    }

    public enum PollutionKind {
        SOOT,
        POISON,
        HEAVY_METAL,
        FALLOUT;

        private static final PollutionKind[] ORDERED_VALUES = values();
        private static final List<PollutionKind> ORDERED_LIST = List.of(ORDERED_VALUES);

        public static List<PollutionKind> orderedValues() {
            return ORDERED_LIST;
        }

        public PollutionType pollutionType() {
            return switch (this) {
                case SOOT -> PollutionType.SOOT;
                case POISON -> PollutionType.POISON;
                case HEAVY_METAL -> PollutionType.HEAVYMETAL;
                case FALLOUT -> PollutionType.FALLOUT;
            };
        }

        public static PollutionKind byPollutionType(PollutionType type) {
            if (type == null) {
                return null;
            }
            return switch (type) {
                case SOOT -> SOOT;
                case POISON -> POISON;
                case HEAVYMETAL -> HEAVY_METAL;
                case FALLOUT -> FALLOUT;
            };
        }

        public String legacyName() {
            return pollutionType().name();
        }
    }
}
