package com.hbm.ntm.client;

import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientBinaryData {
    private static final Map<ResourceLocation, Map<String, byte[]>> DATA = new HashMap<>();

    public static void put(ResourceLocation channel, String name, byte[] payload) {
        DATA.computeIfAbsent(channel, ignored -> new HashMap<>())
                .put(name, Arrays.copyOf(payload, payload.length));
    }

    public static void clear(ResourceLocation channel) {
        DATA.remove(channel);
    }

    public static Optional<byte[]> get(ResourceLocation channel, String name) {
        byte[] payload = DATA.getOrDefault(channel, Map.of()).get(name);
        return payload == null ? Optional.empty() : Optional.of(Arrays.copyOf(payload, payload.length));
    }

    private ClientBinaryData() {
    }
}
