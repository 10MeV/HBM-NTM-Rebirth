package com.hbm.ntm.client.obj;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ObjAnimatedModel implements LegacyObjModel {
    private final Map<String, PartEntry> parts = new LinkedHashMap<>();

    public ObjAnimatedModel part(String name, ObjModelPart part) {
        parts.put(normalize(name), new PartEntry(name, part));
        return this;
    }

    @Override
    public void renderAll(ObjRenderContext context) {
        for (PartEntry part : parts.values()) {
            part.modelPart().render(context);
        }
    }

    @Override
    public void renderPart(String name, ObjRenderContext context) {
        PartEntry part = parts.get(normalize(name));
        if (part != null) {
            part.modelPart().render(context);
        }
    }

    @Override
    public void renderOnly(ObjRenderContext context, String... names) {
        List<String> included = Arrays.stream(names).map(ObjAnimatedModel::normalize).toList();
        for (Map.Entry<String, PartEntry> entry : parts.entrySet()) {
            if (included.contains(entry.getKey())) {
                entry.getValue().modelPart().render(context);
            }
        }
    }

    @Override
    public void renderAllExcept(ObjRenderContext context, String... excludedNames) {
        List<String> excluded = Arrays.stream(excludedNames).map(ObjAnimatedModel::normalize).toList();
        for (Map.Entry<String, PartEntry> entry : parts.entrySet()) {
            if (!excluded.contains(entry.getKey())) {
                entry.getValue().modelPart().render(context);
            }
        }
    }

    @Override
    public List<String> getPartNames() {
        return Collections.unmodifiableList(parts.values().stream().map(PartEntry::legacyName).toList());
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private record PartEntry(String legacyName, ObjModelPart modelPart) {
    }
}
