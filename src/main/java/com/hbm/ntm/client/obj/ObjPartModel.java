package com.hbm.ntm.client.obj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ObjPartModel {
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
