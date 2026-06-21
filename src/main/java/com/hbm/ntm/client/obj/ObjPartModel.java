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
    private final Map<String, OrderedSelection> renderOnlyCache = new LinkedHashMap<>();
    private final Map<String, OrderedSelection> renderOnlyCallOrderCache = new LinkedHashMap<>();
    private final Map<String, OrderedSelection> renderAllExceptCache = new LinkedHashMap<>();
    private List<OrderedPart> orderedPartsCache;
    private OrderedSelection orderedSelectionCache;

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
        invalidateCaches();
        return this;
    }

    public ObjPartModel legacyOrder(String... legacyNames) {
        legacyOrder.clear();
        legacyOrder.addAll(Arrays.asList(legacyNames));
        invalidateCaches();
        return this;
    }

    public ObjPartModel legacyOrder(List<String> legacyNames) {
        legacyOrder.clear();
        legacyOrder.addAll(legacyNames);
        invalidateCaches();
        return this;
    }

    @Override
    public void renderAll(ObjRenderContext context) {
        renderSelection(orderedSelection(), context);
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
        String selectionKey = namesKey(names);
        OrderedSelection cached = renderOnlyCache.get(selectionKey);
        if (cached != null) {
            renderSelection(cached, context);
            return;
        }
        Set<String> included = new LinkedHashSet<>(Arrays.stream(names).map(this::resolve).toList());
        Set<String> rendered = new LinkedHashSet<>();
        List<OrderedPart> selected = new ArrayList<>();
        for (OrderedPart part : orderedParts()) {
            if (included.contains(part.key()) && rendered.add(part.key())) {
                selected.add(part);
            }
        }
        OrderedSelection selection = splitSelection(selected);
        renderOnlyCache.put(selectionKey, selection);
        renderSelection(selection, context);
    }

    public void renderOnlyInCallOrder(ObjRenderContext context, String... names) {
        String selectionKey = namesKey(names);
        OrderedSelection cached = renderOnlyCallOrderCache.get(selectionKey);
        if (cached != null) {
            renderSelection(cached, context);
            return;
        }
        Set<String> rendered = new LinkedHashSet<>();
        List<OrderedPart> selected = new ArrayList<>();
        for (String name : names) {
            String key = resolve(name);
            if (rendered.add(key)) {
                PartEntry part = parts.get(key);
                if (part != null) {
                    selected.add(new OrderedPart(key, part));
                }
            }
        }
        OrderedSelection selection = splitSelection(selected);
        renderOnlyCallOrderCache.put(selectionKey, selection);
        renderSelection(selection, context);
    }

    @Override
    public void renderAllExcept(ObjRenderContext context, String... excludedNames) {
        String selectionKey = namesKey(excludedNames);
        OrderedSelection cached = renderAllExceptCache.get(selectionKey);
        if (cached != null) {
            renderSelection(cached, context);
            return;
        }
        Set<String> excluded = new LinkedHashSet<>(Arrays.stream(excludedNames).map(this::resolve).toList());
        List<OrderedPart> selected = new ArrayList<>();
        for (OrderedPart part : orderedParts()) {
            if (!excluded.contains(part.key())) {
                selected.add(part);
            }
        }
        OrderedSelection selection = splitSelection(selected);
        renderAllExceptCache.put(selectionKey, selection);
        renderSelection(selection, context);
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
        if (orderedPartsCache != null) {
            return orderedPartsCache;
        }

        List<OrderedPart> ordered;
        if (legacyOrder.isEmpty()) {
            ordered = parts.entrySet().stream()
                    .map(entry -> new OrderedPart(entry.getKey(), entry.getValue()))
                    .toList();
        } else {
            ordered = new ArrayList<>();
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
        }
        orderedPartsCache = List.copyOf(ordered);
        return orderedPartsCache;
    }

    private OrderedSelection orderedSelection() {
        if (orderedSelectionCache == null) {
            orderedSelectionCache = splitSelection(orderedParts());
        }
        return orderedSelectionCache;
    }

    private static OrderedSelection splitSelection(List<OrderedPart> orderedParts) {
        List<OrderedPart> opaque = new ArrayList<>();
        List<OrderedPart> translucent = new ArrayList<>();
        for (OrderedPart part : orderedParts) {
            (part.entry().modelPart().translucent() ? translucent : opaque).add(part);
        }
        return new OrderedSelection(List.copyOf(opaque), List.copyOf(translucent));
    }

    private static void renderSelection(OrderedSelection selection, ObjRenderContext context) {
        renderParts(selection.opaque(), context);
        renderParts(selection.translucent(), context);
    }

    private static void renderParts(List<OrderedPart> orderedParts, ObjRenderContext context) {
        for (OrderedPart part : orderedParts) {
            part.entry().modelPart().render(context);
        }
    }

    private String resolve(String name) {
        String key = normalize(name);
        return aliases.getOrDefault(key, key);
    }

    private String namesKey(String... names) {
        StringBuilder key = new StringBuilder();
        for (String name : names) {
            if (!key.isEmpty()) {
                key.append('\u0000');
            }
            key.append(resolve(name));
        }
        return key.toString();
    }

    private void invalidateCaches() {
        orderedPartsCache = null;
        orderedSelectionCache = null;
        renderOnlyCache.clear();
        renderOnlyCallOrderCache.clear();
        renderAllExceptCache.clear();
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private record PartEntry(String legacyName, ObjModelPart modelPart) {
    }

    private record OrderedPart(String key, PartEntry entry) {
    }

    private record OrderedSelection(List<OrderedPart> opaque, List<OrderedPart> translucent) {
    }
}
