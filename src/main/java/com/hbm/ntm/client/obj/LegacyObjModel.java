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

    default void renderAllTranslucent(ObjRenderContext context) {
        renderAll(context.withTranslucencyNoDepthWrite());
    }

    default void renderAllTranslucentDepthWrite(ObjRenderContext context) {
        renderAll(context.withTranslucencyDepthWrite());
    }

    default void renderAllAdditive(ObjRenderContext context) {
        renderAll(context.withAdditiveTranslucency());
    }

    default void renderPartTranslucent(String name, ObjRenderContext context) {
        renderPart(name, context.withTranslucencyNoDepthWrite());
    }

    default void renderPartTranslucentDepthWrite(String name, ObjRenderContext context) {
        renderPart(name, context.withTranslucencyDepthWrite());
    }

    default void renderPartAdditive(String name, ObjRenderContext context) {
        renderPart(name, context.withAdditiveTranslucency());
    }

    default void renderOnlyTranslucent(ObjRenderContext context, String... names) {
        renderOnly(context.withTranslucencyNoDepthWrite(), names);
    }

    default void renderOnlyTranslucentDepthWrite(ObjRenderContext context, String... names) {
        renderOnly(context.withTranslucencyDepthWrite(), names);
    }

    default void renderOnlyAdditive(ObjRenderContext context, String... names) {
        renderOnly(context.withAdditiveTranslucency(), names);
    }

    default void renderAllExceptTranslucent(ObjRenderContext context, String... excludedNames) {
        renderAllExcept(context.withTranslucencyNoDepthWrite(), excludedNames);
    }

    default void renderAllExceptTranslucentDepthWrite(ObjRenderContext context, String... excludedNames) {
        renderAllExcept(context.withTranslucencyDepthWrite(), excludedNames);
    }

    default void renderAllExceptAdditive(ObjRenderContext context, String... excludedNames) {
        renderAllExcept(context.withAdditiveTranslucency(), excludedNames);
    }

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
