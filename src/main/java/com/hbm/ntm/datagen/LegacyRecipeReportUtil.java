package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class LegacyRecipeReportUtil {
    private LegacyRecipeReportUtil() {
    }

    static void addImportSummary(JsonObject root, JsonArray handlers) {
        int foundTemplateCount = 0;
        int importedRecipeCount = 0;
        int skippedRecipeCount = 0;
        int mainResourceRecipeCount = 0;
        boolean sawImported = false;
        boolean sawMainResourcesOnly = false;

        for (JsonElement element : handlers) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject handler = element.getAsJsonObject();
            String status = string(handler, "status");
            if ("imported".equals(status) || "imported_with_skips".equals(status)) {
                foundTemplateCount++;
                sawImported = true;
            } else if ("main_resources_only".equals(status)) {
                sawMainResourcesOnly = true;
            }
            importedRecipeCount += integer(handler, "imported_recipe_count");
            skippedRecipeCount += integer(handler, "skipped_recipe_count");
            mainResourceRecipeCount += integer(handler, "main_resource_recipe_count");
        }

        root.addProperty("found_template_count", foundTemplateCount);
        root.addProperty("imported_recipe_count", importedRecipeCount);
        root.addProperty("skipped_recipe_count", skippedRecipeCount);
        if (mainResourceRecipeCount > 0) {
            root.addProperty("main_resource_recipe_count", mainResourceRecipeCount);
        }

        String status;
        if (skippedRecipeCount > 0) {
            status = "imported_with_skips";
        } else if (sawImported) {
            status = "imported";
        } else if (sawMainResourcesOnly) {
            status = "main_resources_only";
        } else {
            status = "missing_template";
        }
        root.addProperty("status", status);
    }

    private static int integer(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? 0 : element.getAsInt();
    }

    private static String string(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }
}
