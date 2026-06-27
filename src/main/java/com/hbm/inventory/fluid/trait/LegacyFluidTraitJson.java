package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class LegacyFluidTraitJson {
    static int intValue(JsonObject object, String key, int fallback) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null ? fallback : intValue(element);
    }

    static int intValue(JsonElement element) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Integer.decode(element.getAsString());
        }
        return element.getAsInt();
    }

    static long longValue(JsonObject object, String key, long fallback) {
        JsonElement element = object == null ? null : object.get(key);
        if (element == null) {
            return fallback;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Long.decode(element.getAsString());
        }
        return element.getAsLong();
    }

    static float floatValue(JsonObject object, String key, float fallback) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null ? fallback : floatValue(element);
    }

    static float floatValue(JsonElement element) {
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? Float.parseFloat(element.getAsString())
                : element.getAsFloat();
    }

    static double doubleValue(JsonObject object, String key, double fallback) {
        JsonElement element = object == null ? null : object.get(key);
        if (element == null) {
            return fallback;
        }
        return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                ? Double.parseDouble(element.getAsString())
                : element.getAsDouble();
    }

    static boolean booleanValue(JsonObject object, String key, boolean fallback) {
        JsonElement element = object == null ? null : object.get(key);
        return element == null ? fallback : booleanValue(element);
    }

    static boolean booleanValue(JsonElement element) {
        return element.getAsBoolean();
    }

    private LegacyFluidTraitJson() {
    }
}
