package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

final class LegacyMachinePartRenderSelection {
    private static final Map<LegacyMachineDefinition, Selection> WORLD_CACHE = new IdentityHashMap<>();
    private static final Map<LegacyMachineDefinition, Selection> ITEM_CACHE = new IdentityHashMap<>();

    private LegacyMachinePartRenderSelection() {
    }

    static Selection world(LegacyMachineDefinition definition) {
        return WORLD_CACHE.computeIfAbsent(definition, LegacyMachinePartRenderSelection::buildWorld);
    }

    static Selection item(LegacyMachineDefinition definition) {
        return ITEM_CACHE.computeIfAbsent(definition, LegacyMachinePartRenderSelection::buildItem);
    }

    private static Selection buildWorld(LegacyMachineDefinition definition) {
        return build(definition.renderParts(), definition.partTextures(), definition.partRenderProperties(),
                definition.textureLocation());
    }

    private static Selection buildItem(LegacyMachineDefinition definition) {
        return build(definition.itemRenderParts(), definition.itemPartTextures(), definition.itemPartRenderProperties(),
                definition.textureLocation());
    }

    private static Selection build(List<String> partNames, Map<String, ResourceLocation> textures,
            Map<String, LegacyMachinePartRenderProperties> properties, ResourceLocation defaultTexture) {
        List<Entry> opaque = new ArrayList<>();
        List<Entry> translucent = new ArrayList<>();
        String[] names = partNames.toArray(String[]::new);
        for (String partName : partNames) {
            LegacyMachinePartRenderProperties partProperties = properties.get(partName);
            ResourceLocation texture = textures.getOrDefault(partName, defaultTexture);
            Entry entry = new Entry(partName, texture, partProperties);
            (LegacyMachinePartRenderContexts.translucent(partProperties) ? translucent : opaque).add(entry);
        }
        return new Selection(List.copyOf(opaque), buildRuns(opaque), List.copyOf(translucent),
                buildRuns(translucent), names);
    }

    private static List<Run> buildRuns(List<Entry> entries) {
        List<Run> runs = new ArrayList<>();
        int start = 0;
        while (start < entries.size()) {
            Entry first = entries.get(start);
            int end = start + 1;
            while (end < entries.size() && first.sameBatch(entries.get(end))) {
                end++;
            }
            Entry[] runEntries = new Entry[end - start];
            String[] names = new String[end - start];
            for (int index = start; index < end; index++) {
                Entry entry = entries.get(index);
                runEntries[index - start] = entry;
                names[index - start] = entry.partName();
            }
            runs.add(new Run(List.of(runEntries), names));
            start = end;
        }
        return List.copyOf(runs);
    }

    record Selection(List<Entry> opaque, List<Run> opaqueRuns, List<Entry> translucent,
            List<Run> translucentRuns, String[] partNames) {
    }

    record Entry(String partName, ResourceLocation texture, LegacyMachinePartRenderProperties properties) {
        boolean sameBatch(Entry next) {
            return texture.equals(next.texture) && java.util.Objects.equals(properties, next.properties);
        }
    }

    static final class Run {
        private final List<Entry> entries;
        private final String[] partNames;
        private LegacyWavefrontModel.SelectionHandle selectionHandle;

        private Run(List<Entry> entries, String[] partNames) {
            this.entries = entries;
            this.partNames = partNames;
        }

        List<Entry> entries() {
            return entries;
        }

        String[] partNames() {
            return partNames;
        }

        LegacyWavefrontModel.SelectionHandle selectionHandle(LegacyWavefrontModel model) {
            if (selectionHandle == null) {
                selectionHandle = model.prepareRenderOnly(partNames);
            }
            return selectionHandle;
        }
    }
}
