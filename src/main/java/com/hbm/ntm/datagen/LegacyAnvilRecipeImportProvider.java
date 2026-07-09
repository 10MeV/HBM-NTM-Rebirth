package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.util.HbmRegistryUtil;
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

public final class LegacyAnvilRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmAnvil.json";
    private static final String LEGACY_CLASS = "AnvilRecipes";
    private static final String OUTPUT_FOLDER = "anvil_construction";

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyAnvilRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_anvil_recipe_import_report.json");
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
        root.addProperty("note",
                "1.7.10 hbmAnvil.json serializes constructionRecipes only; smithing defaults are Java-registered separately.");

        Path source = resolveLegacyFile();
        if (source == null) {
            root.addProperty("status", "missing_template");
            root.addProperty("source_recipe_count", 0);
            root.addProperty("imported_recipe_count", 0);
            root.addProperty("skipped_recipe_count", 0);
            saves.add(DataProvider.saveStable(output, root, reportPath));
            HbmNtm.LOGGER.info("No legacy anvil recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy anvil recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy anvil recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy anvil recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy anvil recipe file is missing recipes array");
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
                    JsonObject modern = anvilConstructionJson(legacy, sourceIndex);
                    validate(importedId, modern);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy anvil recipe file " + source, exception);
        }
    }

    private static JsonObject anvilConstructionJson(JsonObject legacy, int sourceIndex) {
        List<HbmIngredient> inputs = inputs(requireArray(legacy, "inputs"));
        List<HbmItemOutput> outputs = outputs(requireArray(legacy, "outputs"));
        int tierLower = requireInt(legacy, "tierLower");
        int tierUpper = legacy.has("tierUpper") ? legacy.get("tierUpper").getAsInt() : -1;
        AnvilConstructionRecipe.OverlayType overlay = legacy.has("overlay")
                ? AnvilConstructionRecipe.OverlayType.byName(legacy.get("overlay").getAsString())
                : AnvilConstructionRecipe.OverlayType.NONE;

        JsonObject modern = CompatRecipeRegistry.createAnvilConstruction(inputs, outputs, tierLower, tierUpper,
                overlay);
        modern.addProperty("source_order", sourceIndex);
        return modern;
    }

    private static List<HbmIngredient> inputs(JsonArray legacyInputs) {
        List<HbmIngredient> inputs = new ArrayList<>();
        for (JsonElement element : legacyInputs) {
            if (element == null || element.isJsonNull()) {
                continue;
            }
            inputs.add(LegacyGenericRecipeFormat.readLegacyRecipeAStack(element.getAsJsonArray()));
        }
        if (inputs.isEmpty()) {
            throw new JsonSyntaxException("Legacy anvil recipe resolved to no inputs");
        }
        return List.copyOf(inputs);
    }

    private static List<HbmItemOutput> outputs(JsonArray legacyOutputs) {
        List<HbmItemOutput> outputs = new ArrayList<>();
        for (JsonElement element : legacyOutputs) {
            if (element == null || element.isJsonNull()) {
                continue;
            }
            outputs.add(readLegacyChanceOutput(element.getAsJsonArray()));
        }
        if (outputs.isEmpty()) {
            throw new JsonSyntaxException("Legacy anvil recipe resolved to no outputs");
        }
        return List.copyOf(outputs);
    }

    private static HbmItemOutput readLegacyChanceOutput(JsonArray legacyOutput) {
        ItemStack stack = readLegacyChanceStack(legacyOutput);
        if (stack.isEmpty()) {
            throw new JsonSyntaxException("Legacy anvil output resolved to empty");
        }
        float chance = legacyOutput.get(legacyOutput.size() - 1).getAsFloat();
        return HbmItemOutput.chance(stack, chance);
    }

    private static ItemStack readLegacyChanceStack(JsonArray legacyOutput) {
        if (legacyOutput.size() < 2) {
            throw new JsonSyntaxException("Legacy anvil output must contain an ItemStack and chance");
        }
        JsonArray stack = new JsonArray();
        for (int index = 0; index < legacyOutput.size() - 1; index++) {
            stack.add(legacyOutput.get(index).deepCopy());
        }
        return LegacyGenericRecipeFormat.readLegacyRecipeItemStack(stack);
    }

    private static void validate(ResourceLocation id, JsonObject modern) {
        new AnvilConstructionRecipe.Serializer().fromJson(id, modern);
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
        String output = legacy.has("outputs") ? firstOutputName(legacy.getAsJsonArray("outputs")) : "output";
        String input = legacy.has("inputs") ? inputListName(legacy.getAsJsonArray("inputs")) : "input";
        String tier = legacy.has("tierLower") ? "_tier_" + legacy.get("tierLower").getAsInt() : "";
        String overlay = legacy.has("overlay") ? "_" + legacy.get("overlay").getAsString() : "";
        String name = sanitizeName(output + "_from_" + input + tier + overlay + "_legacy_" + sourceIndex);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String inputListName(JsonArray inputs) {
        if (inputs == null || inputs.size() == 0) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        int nonEmpty = 0;
        for (JsonElement element : inputs) {
            if (element == null || element.isJsonNull()) {
                continue;
            }
            nonEmpty++;
            if (names.size() < 3) {
                names.add(legacyAStackName(element.getAsJsonArray()));
            }
        }
        if (names.isEmpty()) {
            return "empty";
        }
        if (nonEmpty > names.size()) {
            names.add("and_" + (nonEmpty - names.size()) + "_more");
        }
        return String.join("_", names);
    }

    private static String firstOutputName(JsonArray outputs) {
        if (outputs == null || outputs.size() == 0) {
            return "legacy_output";
        }
        for (JsonElement element : outputs) {
            if (element != null && !element.isJsonNull()) {
                return legacyChanceOutputName(element.getAsJsonArray());
            }
        }
        return "legacy_output";
    }

    private static String legacyAStackName(JsonArray array) {
        if (array.size() < 2) {
            return "legacy_stack";
        }
        String type = array.get(0).getAsString();
        String id = array.get(1).getAsString();
        String count = array.size() > 2 && array.get(2).getAsInt() != 1 ? "_x" + array.get(2).getAsInt() : "";
        String meta = array.size() > 3 ? "_m" + array.get(3).getAsInt() : "";
        return type + "_" + id + count + meta;
    }

    private static String legacyChanceOutputName(JsonArray array) {
        try {
            ItemStack stack = readLegacyChanceStack(array);
            ResourceLocation itemId = HbmRegistryUtil.itemKey(stack.getItem());
            if (itemId != null) {
                return itemId.toString() + (stack.getCount() > 1 ? "_x" + stack.getCount() : "");
            }
        } catch (RuntimeException ignored) {
        }
        if (array.size() < 1) {
            return "legacy_output";
        }
        int stackFields = Math.max(1, array.size() - 1);
        String count = stackFields > 1 && array.get(1).getAsInt() != 1 ? "_x" + array.get(1).getAsInt() : "";
        String meta = stackFields > 2 ? "_m" + array.get(2).getAsInt() : "";
        return array.get(0).getAsString() + count + meta;
    }

    private static JsonArray requireArray(JsonObject legacy, String name) {
        JsonArray array = legacy.getAsJsonArray(name);
        if (array == null) {
            throw new JsonSyntaxException("Legacy anvil recipe is missing " + name + " array");
        }
        return array;
    }

    private static int requireInt(JsonObject legacy, String name) {
        if (!legacy.has(name)) {
            throw new JsonSyntaxException("Legacy anvil recipe is missing " + name);
        }
        return legacy.get(name).getAsInt();
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
