package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.recipe.CrucibleRecipe;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
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
import net.minecraft.world.item.ItemStack;

public final class LegacyCrucibleRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmCrucible.json";
    private static final String LEGACY_CLASS = "CrucibleRecipes";
    private static final String OUTPUT_FOLDER = "crucible";

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyCrucibleRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_crucible_recipe_import_report.json");
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

        Path source = resolveLegacyFile();
        if (source == null) {
            root.addProperty("status", "missing_template");
            root.addProperty("source_recipe_count", 0);
            root.addProperty("imported_recipe_count", 0);
            root.addProperty("skipped_recipe_count", 0);
            saves.add(DataProvider.saveStable(output, root, reportPath));
            HbmNtm.LOGGER.info("No legacy crucible recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy crucible recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy crucible recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy crucible recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy crucible recipe file is missing recipes array");
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
                    JsonObject modern = crucibleJson(legacy, sourceIndex);
                    validate(importedId, modern);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy crucible recipe file " + source, exception);
        }
    }

    private static JsonObject crucibleJson(JsonObject legacy, int sourceIndex) {
        String name = requiredString(legacy, "name");
        int frequency = requiredInt(legacy, "frequency");
        ItemStack icon = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(requiredArray(legacy, "icon"));
        MaterialStack[] input = materialStacks(requiredArray(legacy, "input"), "input");
        MaterialStack[] output = materialStacks(requiredArray(legacy, "output"), "output");
        return CompatRecipeRegistry.createCrucible(name, name, icon, frequency, input, output, sourceIndex);
    }

    private static MaterialStack[] materialStacks(JsonArray legacy, String field) {
        List<MaterialStack> materials = new ArrayList<>();
        for (int i = 0; i < legacy.size(); i++) {
            materials.add(materialStack(legacy.get(i).getAsJsonArray(), field + "[" + i + "]"));
        }
        return materials.toArray(MaterialStack[]::new);
    }

    private static MaterialStack materialStack(JsonArray legacy, String name) {
        if (legacy == null || legacy.size() < 2) {
            throw new JsonSyntaxException("Legacy crucible material stack for " + name
                    + " needs material and amount: " + legacy);
        }
        String materialName = legacy.get(0).getAsString();
        NTMMaterial material = materialByName(materialName);
        if (material == null) {
            throw new JsonSyntaxException("Unknown crucible material '" + materialName + "' in " + name);
        }
        int amount = legacy.get(1).getAsInt();
        if (amount <= 0) {
            throw new JsonSyntaxException("Invalid crucible material amount " + amount + " in " + name);
        }
        return new MaterialStack(material, amount);
    }

    private static NTMMaterial materialByName(String name) {
        NTMMaterial material = Mats.matByName.get(name);
        if (material != null) {
            return material;
        }
        for (NTMMaterial candidate : Mats.orderedList) {
            for (String candidateName : candidate.names) {
                if (candidateName.equalsIgnoreCase(name)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static void validate(ResourceLocation id, JsonObject modern) {
        new CrucibleRecipe.Serializer().fromJson(id, modern);
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
        String name = legacy.has("name") ? legacy.get("name").getAsString() : "legacy_import_" + sourceIndex;
        name = sanitizeName(name);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static JsonArray requiredArray(JsonObject object, String key) {
        JsonArray array = object.getAsJsonArray(key);
        if (array == null) {
            throw new JsonSyntaxException("Legacy crucible recipe is missing " + key + " array");
        }
        return array;
    }

    private static String requiredString(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new JsonSyntaxException("Legacy crucible recipe is missing " + key);
        }
        return object.get(key).getAsString();
    }

    private static int requiredInt(JsonObject object, String key) {
        if (!object.has(key)) {
            throw new JsonSyntaxException("Legacy crucible recipe is missing " + key);
        }
        return object.get(key).getAsInt();
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
