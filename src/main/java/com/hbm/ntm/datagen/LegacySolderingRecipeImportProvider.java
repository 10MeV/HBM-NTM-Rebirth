package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.HbmIngredient;
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

public final class LegacySolderingRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmSoldering.json";
    private static final String LEGACY_CLASS = "SolderingRecipes";
    private static final String OUTPUT_FOLDER = "soldering_station";

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacySolderingRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_soldering_recipe_import_report.json");
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
            HbmNtm.LOGGER.info("No legacy soldering recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy soldering recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy soldering recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy soldering recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy soldering recipe file is missing recipes array");
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
                    JsonObject modern = solderingJson(legacy, sourceIndex);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy soldering recipe file " + source,
                    exception);
        }
    }

    private static JsonObject solderingJson(JsonObject legacy, int sourceIndex) {
        ItemStack output = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("output"));
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Legacy soldering output resolved to empty");
        }
        List<HbmIngredient> toppings = inputs(legacy.getAsJsonArray("toppings"), "toppings", 3);
        List<HbmIngredient> pcb = inputs(legacy.getAsJsonArray("pcb"), "pcb", 2);
        List<HbmIngredient> solder = inputs(legacy.getAsJsonArray("solder"), "solder", 1);
        HbmFluidStack fluid = legacy.has("fluid") ? fluidStack(legacy.getAsJsonArray("fluid")) : null;
        int duration = legacy.get("duration").getAsInt();
        long consumption = legacy.get("consumption").getAsLong();
        return CompatRecipeRegistry.createSoldering(output, duration, consumption, fluid, toppings, pcb, solder,
                sourceIndex);
    }

    private static List<HbmIngredient> inputs(JsonArray legacyInputs, String name, int maxSlots) {
        if (legacyInputs == null) {
            throw new JsonSyntaxException("Legacy soldering recipe is missing " + name + " array");
        }
        if (legacyInputs.size() > maxSlots) {
            throw new JsonSyntaxException("Legacy soldering " + name + " array exceeds " + maxSlots + " slots");
        }
        List<HbmIngredient> inputs = new ArrayList<>();
        for (JsonElement element : legacyInputs) {
            inputs.add(LegacyGenericRecipeFormat.readLegacyRecipeAStack(element.getAsJsonArray()));
        }
        return List.copyOf(inputs);
    }

    private static HbmFluidStack fluidStack(JsonArray legacy) {
        if (legacy == null || legacy.size() < 2) {
            throw new JsonSyntaxException("Legacy soldering fluid stack needs fluid and amount: " + legacy);
        }
        JsonObject object = new JsonObject();
        object.add("fluid", legacy.get(0).deepCopy());
        object.add("amount", legacy.get(1).deepCopy());
        if (legacy.size() > 2) {
            object.add("pressure", legacy.get(2).deepCopy());
        }
        return HbmFluidJsonUtil.readFluidStack(object, "legacy soldering fluid");
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
        String output = legacy.has("output") ? legacyItemStackName(legacy.getAsJsonArray("output")) : "output";
        String fluid = legacy.has("fluid") ? "_fluid_" + fluidName(legacy.getAsJsonArray("fluid")) : "";
        String toppings = legacy.has("toppings") ? groupName(legacy.getAsJsonArray("toppings")) : "no_toppings";
        String pcb = legacy.has("pcb") ? groupName(legacy.getAsJsonArray("pcb")) : "no_pcb";
        String solder = legacy.has("solder") ? groupName(legacy.getAsJsonArray("solder")) : "no_solder";
        String name = sanitizeName(output + fluid + "_from_" + toppings + "_" + pcb + "_" + solder);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String groupName(JsonArray inputs) {
        if (inputs == null || inputs.size() == 0) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (JsonElement element : inputs) {
            names.add(legacyAStackName(element.getAsJsonArray()));
        }
        return String.join("_", names);
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

    private static String legacyItemStackName(JsonArray array) {
        if (array.size() < 1) {
            return "legacy_output";
        }
        try {
            ItemStack stack = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(array);
            ResourceLocation itemId = HbmRegistryUtil.itemKey(stack.getItem());
            if (itemId != null) {
                return itemId.toString() + (stack.getCount() > 1 ? "_x" + stack.getCount() : "");
            }
        } catch (RuntimeException ignored) {
        }
        String count = array.size() > 1 && array.get(1).getAsInt() != 1 ? "_x" + array.get(1).getAsInt() : "";
        String meta = array.size() > 2 ? "_m" + array.get(2).getAsInt() : "";
        return array.get(0).getAsString() + count + meta;
    }

    private static String fluidName(JsonArray array) {
        if (array.size() < 2) {
            return "fluid";
        }
        String pressure = array.size() > 2 ? "_p" + array.get(2).getAsInt() : "";
        return array.get(0).getAsString() + "_m" + array.get(1).getAsInt() + pressure;
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
