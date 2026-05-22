package com.hbm.ntm.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientPanelData {
    private static final Map<ResourceLocation, PanelData> DATA = new HashMap<>();

    public static void update(ResourceLocation panelType, int legacyType, CompoundTag tag) {
        DATA.put(panelType, new PanelData(legacyType, tag == null ? new CompoundTag() : tag.copy()));
    }

    public static Optional<PanelData> get(ResourceLocation panelType) {
        PanelData data = DATA.get(panelType);
        return data == null ? Optional.empty() : Optional.of(new PanelData(data.legacyType, data.data.copy()));
    }

    public record PanelData(int legacyType, CompoundTag data) {
    }

    private ClientPanelData() {
    }
}
