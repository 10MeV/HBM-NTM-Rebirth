package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ClientPanelData {
    private static final Map<ResourceLocation, PanelData> DATA = new HashMap<>();
    private static final List<ClientPanelDataListener> LISTENERS = new ArrayList<>();

    public static void update(ResourceLocation panelType, int legacyType, CompoundTag tag) {
        CompoundTag safeTag = tag == null ? new CompoundTag() : tag.copy();
        DATA.put(panelType, new PanelData(legacyType, safeTag.copy()));
        for (ClientPanelDataListener listener : List.copyOf(LISTENERS)) {
            listener.onClientPanelData(panelType, legacyType, safeTag.copy());
        }
    }

    public static Optional<PanelData> get(ResourceLocation panelType) {
        PanelData data = DATA.get(panelType);
        return data == null ? Optional.empty() : Optional.of(new PanelData(data.legacyType, data.data.copy()));
    }

    public static void addListener(ClientPanelDataListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void removeListener(ClientPanelDataListener listener) {
        LISTENERS.remove(listener);
    }

    public static void clearListeners() {
        LISTENERS.clear();
    }

    public static void clearAll() {
        DATA.clear();
        LISTENERS.clear();
    }

    public record PanelData(int legacyType, CompoundTag data) {
    }

    private ClientPanelData() {
    }
}
