package com.hbm.ntm.client.obj;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ObjPartModel implements LegacyObjModel {
    private final Map<String, PartEntry> parts = new LinkedHashMap<>();
    private final Map<String, String> aliases = new LinkedHashMap<>();
    private final Map<String, String> aliasNames = new LinkedHashMap<>();

    public ObjPartModel part(String legacyName, ObjModelPart part, String... legacyAliases) {
        String key = normalize(legacyName);
        parts.put(key, new PartEntry(legacyName, part));
        aliases.put(key, key);
        aliasNames.put(key, legacyName);
        for (String alias : legacyAliases) {
            String aliasKey = normalize(alias);
            aliases.put(aliasKey, key);
            aliasNames.put(aliasKey, alias);
        }
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
        PartEntry part = parts.get(resolve(name));
        if (part != null) {
            part.modelPart().render(context);
        }
    }

    @Override
    public void renderOnly(ObjRenderContext context, String... names) {
        Set<String> included = new LinkedHashSet<>(Arrays.stream(names).map(this::resolve).toList());
        Set<String> rendered = new LinkedHashSet<>();
        for (Map.Entry<String, PartEntry> entry : parts.entrySet()) {
            String key = entry.getKey();
            if (included.contains(key) && rendered.add(key)) {
                entry.getValue().modelPart().render(context);
            }
        }
    }

    public void renderOnlyInCallOrder(ObjRenderContext context, String... names) {
        Set<String> rendered = new LinkedHashSet<>();
        for (String name : names) {
            String key = resolve(name);
            if (rendered.add(key)) {
                PartEntry part = parts.get(key);
                if (part != null) {
                    part.modelPart().render(context);
                }
            }
        }
    }

    @Override
    public void renderAllExcept(ObjRenderContext context, String... excludedNames) {
        Set<String> excluded = new LinkedHashSet<>(Arrays.stream(excludedNames).map(this::resolve).toList());
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

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliasNames.values().stream().toList());
    }

    private String resolve(String name) {
        String key = normalize(name);
        return aliases.getOrDefault(key, key);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private record PartEntry(String legacyName, ObjModelPart modelPart) {
    }
}
