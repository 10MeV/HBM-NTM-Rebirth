package com.hbm.ntm.client.obj;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ObjAnimatedModel {
    private final Map<String, ObjModelPart> parts = new LinkedHashMap<>();

    public ObjAnimatedModel part(String name, ObjModelPart part) {
        parts.put(normalize(name), part);
        return this;
    }

    public void renderAll(ObjRenderContext context) {
        for (ObjModelPart part : parts.values()) {
            part.render(context);
        }
    }

    public void renderPart(String name, ObjRenderContext context) {
        ObjModelPart part = parts.get(normalize(name));
        if (part != null) {
            part.render(context);
        }
    }

    public void renderOnly(ObjRenderContext context, String... names) {
        for (String name : names) {
            renderPart(name, context);
        }
    }

    public void renderAllExcept(ObjRenderContext context, String... excludedNames) {
        List<String> excluded = Arrays.stream(excludedNames).map(ObjAnimatedModel::normalize).toList();
        for (Map.Entry<String, ObjModelPart> entry : parts.entrySet()) {
            if (!excluded.contains(entry.getKey())) {
                entry.getValue().render(context);
            }
        }
    }

    public List<String> getPartNames() {
        return Collections.unmodifiableList(parts.keySet().stream().toList());
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
