package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.AnnihilatorRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class LegacyAnnihilatorRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmAnnihilator.json";
    private static final String LEGACY_CLASS = "AnnihilatorRecipes";
    private static final String OUTPUT_FOLDER = "annihilator";

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyAnnihilatorRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_annihilator_recipe_import_report.json");
        this.legacyRecipeDir = projectRoot.resolve("legacy_recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("legacy_recipe_dir", reportPath(legacyRecipeDir));
        root.addProperty("legacy_file", LEGACY_FILE);
        root.addProperty("legacy_class", LEGACY_CLASS);
        root.addProperty("modern_recipe_type", OUTPUT_FOLDER);
        root.addProperty("note", "1.7.10 default milestones are 528-only and are not materialized here.");

        Path source = resolveLegacyFile();
        if (source == null) {
            root.addProperty("status", "missing_template");
            root.addProperty("source_recipe_count", 0);
            root.addProperty("imported_recipe_count", 0);
            root.addProperty("skipped_recipe_count", 0);
            saves.add(DataProvider.saveStable(output, root, reportPath));
            HbmNtm.LOGGER.info("No legacy annihilator recipe template found in {}; skipping import.",
                    legacyRecipeDir);
            return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
        }

        root.addProperty("source", reportPath(source));
        ImportReport report = readLenient(source);
        root.addProperty("status", report.failures().isEmpty() ? "imported" : "imported_with_skips");
        root.addProperty("source_recipe_count", report.sourceRecipeCount());
        root.addProperty("imported_recipe_count", report.imported().size());
        root.addProperty("skipped_recipe_count", report.failures().size());
        JsonArray failures = new JsonArray();
        root.add("failures", failures);
        for (ImportFailure failure : report.failures()) {
            JsonObject failureReport = new JsonObject();
            failureReport.addProperty("source_index", failure.sourceIndex());
            failureReport.addProperty("requested_id", failure.requestedId().toString());
            failureReport.addProperty("message", failure.message());
            failures.add(failureReport);
            HbmNtm.LOGGER.warn("Skipped legacy annihilator recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy annihilator recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy annihilator recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy annihilator recipe file is missing recipes array");
            }
            List<ImportedRecipe> imported = new ArrayList<>();
            List<ImportFailure> failures = new ArrayList<>();
            Map<ResourceLocation, Integer> usedIds = new LinkedHashMap<>();
            for (int sourceIndex = 0; sourceIndex < recipes.size(); sourceIndex++) {
                JsonElement element = recipes.get(sourceIndex);
                if (element == null || element.isJsonNull()) {
                    continue;
                }
                ResourceLocation requestedId = id(OUTPUT_FOLDER + "/legacy_import_" + sourceIndex);
                try {
                    JsonObject legacy = element.getAsJsonObject();
                    requestedId = id(OUTPUT_FOLDER + "/" + recipeName(legacy, sourceIndex));
                    ResourceLocation importedId = uniqueId(requestedId, usedIds);
                    JsonObject modern = annihilatorJson(legacy, sourceIndex);
                    validate(importedId, modern);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy annihilator recipe file " + source,
                    exception);
        }
    }

    private static JsonObject annihilatorJson(JsonObject legacy, int sourceIndex) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", ModRecipes.ANNIHILATOR.serializer().getId().toString());
        modern.add("key", requireObject(legacy, "key").deepCopy());
        modern.add("milestones", requireArray(legacy, "milestones").deepCopy());
        modern.addProperty("source_order", sourceIndex);
        return modern;
    }

    private static void validate(ResourceLocation id, JsonObject modern) {
        new AnnihilatorRecipe.Serializer().fromJson(id, modern);
    }

    private Path resolveLegacyFile() {
        Path direct = legacyRecipeDir.resolve(LEGACY_FILE);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        Path template = legacyRecipeDir.resolve("_" + LEGACY_FILE);
        if (Files.isRegularFile(template)) {
            return template;
        }
        return null;
    }

    private static String recipeName(JsonObject legacy, int sourceIndex) {
        String key = legacy.has("key") ? keyName(legacy.getAsJsonObject("key")) : "key";
        JsonArray milestones = legacy.has("milestones") ? legacy.getAsJsonArray("milestones") : null;
        String milestone = firstMilestoneName(milestones);
        String name = sanitizeName(key + "_" + milestone);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String keyName(JsonObject key) {
        String type = key.get("type").getAsString();
        return switch (type) {
            case "item" -> "item_" + key.get("item").getAsString();
            case "comp" -> "comp_" + key.get("item").getAsString() + "_m" + key.get("meta").getAsInt();
            case "fluid" -> "fluid_" + key.get("fluid").getAsString();
            case "dict" -> "dict_" + key.get("dict").getAsString();
            default -> type + "_key";
        };
    }

    private static String firstMilestoneName(JsonArray milestones) {
        if (milestones == null || milestones.size() == 0) {
            return "empty";
        }
        JsonObject milestone = milestones.get(0).getAsJsonObject();
        String amount = milestone.has("amount") ? milestone.get("amount").getAsString() : "amount";
        String payout = milestone.has("payout") ? payoutName(milestone.get("payout")) : "payout";
        return "milestone_" + amount + "_" + payout;
    }

    private static String payoutName(JsonElement payout) {
        if (payout == null || payout.isJsonNull()) {
            return "empty";
        }
        if (payout.isJsonArray()) {
            JsonArray array = payout.getAsJsonArray();
            if (array.size() == 0) {
                return "legacy_output";
            }
            String id = array.get(0).getAsString();
            String count = array.size() > 1 && array.get(1).getAsInt() != 1 ? "_x" + array.get(1).getAsInt() : "";
            String meta = array.size() > 2 ? "_m" + array.get(2).getAsInt() : "";
            return id + count + meta;
        }
        JsonObject object = payout.getAsJsonObject();
        String id = object.has("item") ? object.get("item").getAsString() : "modern_output";
        String count = object.has("count") && object.get("count").getAsInt() != 1
                ? "_x" + object.get("count").getAsInt()
                : "";
        return id + count;
    }

    private static JsonObject requireObject(JsonObject legacy, String name) {
        JsonObject object = legacy.getAsJsonObject(name);
        if (object == null) {
            throw new JsonSyntaxException("Legacy annihilator recipe is missing " + name + " object");
        }
        return object;
    }

    private static JsonArray requireArray(JsonObject legacy, String name) {
        JsonArray array = legacy.getAsJsonArray(name);
        if (array == null) {
            throw new JsonSyntaxException("Legacy annihilator recipe is missing " + name + " array");
        }
        return array;
    }

    private static String sanitizeName(String raw) {
        return raw.toLowerCase(Locale.ROOT)
                .replace(':', '_')
                .replaceAll("[^a-z0-9_./-]", "_")
                .replaceAll("/+", "/");
    }

    private static ResourceLocation uniqueId(ResourceLocation baseId, Map<ResourceLocation, Integer> usedIds) {
        Integer previous = usedIds.putIfAbsent(baseId, 1);
        if (previous == null) {
            return baseId;
        }
        int next = previous + 1;
        ResourceLocation candidate;
        do {
            candidate = new ResourceLocation(baseId.getNamespace(), baseId.getPath() + "_" + next);
            next++;
        } while (usedIds.containsKey(candidate));
        usedIds.put(baseId, next - 1);
        usedIds.put(candidate, 1);
        return candidate;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record ImportReport(int sourceRecipeCount, List<ImportedRecipe> imported, List<ImportFailure> failures) {
    }

    private record ImportedRecipe(ResourceLocation id, JsonObject json) {
    }

    private record ImportFailure(int sourceIndex, ResourceLocation requestedId, String message) {
    }
}
