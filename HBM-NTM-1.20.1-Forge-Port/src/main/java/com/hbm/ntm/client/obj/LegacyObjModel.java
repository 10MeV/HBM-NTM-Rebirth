package com.hbm.ntm.client.obj;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public interface LegacyObjModel {
    void renderAll(ObjRenderContext context);

    void renderPart(String name, ObjRenderContext context);

    void renderOnly(ObjRenderContext context, String... names);

    void renderAllExcept(ObjRenderContext context, String... excludedNames);

    List<String> getPartNames();

    default LegacyObjModel mixedMode() {
        return this;
    }

    default LegacyObjModel asVBO() {
        return this;
    }

    default void renderOnlyInCallOrder(ObjRenderContext context, String... names) {
        renderOnly(context, names);
    }

    default boolean hasPart(String name) {
        String normalized = normalize(name);
        return getPartNames().stream().map(LegacyObjModel::normalize).anyMatch(normalized::equals);
    }

    default List<String> getAliases() {
        return List.of();
    }

    default List<String> getLegacyOrder() {
        return getPartNames();
    }

    private static String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    static List<String> normalizeAll(String... names) {
        return Arrays.stream(names).map(LegacyObjModel::normalize).toList();
    }
}
