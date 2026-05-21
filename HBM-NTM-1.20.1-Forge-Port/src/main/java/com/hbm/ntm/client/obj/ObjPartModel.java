package com.hbm.ntm.client.obj;

import java.util.ArrayList;
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
    private final List<String> legacyOrder = new ArrayList<>();

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

    public ObjPartModel legacyOrder(String... legacyNames) {
        legacyOrder.clear();
        legacyOrder.addAll(Arrays.asList(legacyNames));
        return this;
    }

    public ObjPartModel legacyOrder(List<String> legacyNames) {
        legacyOrder.clear();
        legacyOrder.addAll(legacyNames);
        return this;
    }

    @Override
    public void renderAll(ObjRenderContext context) {
        for (OrderedPart part : orderedParts()) {
            part.entry().modelPart().render(context);
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
        for (OrderedPart part : orderedParts()) {
            if (included.contains(part.key()) && rendered.add(part.key())) {
                part.entry().modelPart().render(context);
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
        for (OrderedPart part : orderedParts()) {
            if (!excluded.contains(part.key())) {
                part.entry().modelPart().render(context);
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

    public List<String> getLegacyOrder() {
        return Collections.unmodifiableList(legacyOrder);
    }

    public boolean hasPart(String name) {
        return parts.containsKey(resolve(name));
    }

    private List<OrderedPart> orderedParts() {
        if (legacyOrder.isEmpty()) {
            return parts.entrySet().stream()
                    .map(entry -> new OrderedPart(entry.getKey(), entry.getValue()))
                    .toList();
        }

        List<OrderedPart> ordered = new ArrayList<>();
        Set<String> added = new LinkedHashSet<>();
        for (String legacyName : legacyOrder) {
            String key = resolve(legacyName);
            PartEntry part = parts.get(key);
            if (part != null && added.add(key)) {
                ordered.add(new OrderedPart(key, part));
            }
        }
        for (Map.Entry<String, PartEntry> entry : parts.entrySet()) {
            if (added.add(entry.getKey())) {
                ordered.add(new OrderedPart(entry.getKey(), entry.getValue()));
            }
        }
        return ordered;
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

    private record OrderedPart(String key, PartEntry entry) {
    }
}
