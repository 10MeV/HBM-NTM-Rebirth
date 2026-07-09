package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.AmmoPressRecipe;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LegacyAmmoPressRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmAmmoPress.json";
    private static final String LEGACY_CLASS = "AmmoPressRecipes";
    private static final String OUTPUT_FOLDER = "ammo_press";
    private static final Pattern FLUID_CONTAINER_DICT =
            Pattern.compile("^(?:ntm)?container([1-9][0-9]*)([a-z0-9]+)$", Pattern.CASE_INSENSITIVE);

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyAmmoPressRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_ammo_press_recipe_import_report.json");
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
            HbmNtm.LOGGER.info("No legacy ammo press recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy ammo press recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy ammo press recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy ammo press recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy ammo press recipe file is missing recipes array");
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
                    JsonObject modern = ammoPressJson(legacy, sourceIndex);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy ammo press recipe file " + source,
                    exception);
        }
    }

    private static JsonObject ammoPressJson(JsonObject legacy, int sourceIndex) {
        ItemStack output = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("output"));
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Legacy ammo press output resolved to empty");
        }
        HbmIngredient[] inputs = inputs(legacy.getAsJsonArray("input"));
        return CompatRecipeRegistry.createAmmoPress(inputs, output, sourceIndex);
    }

    private static HbmIngredient[] inputs(JsonArray legacyInputs) {
        if (legacyInputs == null) {
            throw new JsonSyntaxException("Legacy ammo press recipe is missing input array");
        }
        if (legacyInputs.size() != AmmoPressRecipe.INPUT_SLOTS) {
            throw new JsonSyntaxException("Legacy ammo press input array must have exactly "
                    + AmmoPressRecipe.INPUT_SLOTS + " entries");
        }
        HbmIngredient[] inputs = new HbmIngredient[AmmoPressRecipe.INPUT_SLOTS];
        for (int slot = 0; slot < AmmoPressRecipe.INPUT_SLOTS; slot++) {
            JsonElement element = legacyInputs.get(slot);
            inputs[slot] = element == null || element.isJsonNull()
                    ? null
                    : legacyAStack(element.getAsJsonArray());
        }
        return inputs;
    }

    private static HbmIngredient legacyAStack(JsonArray array) {
        if (array.size() >= 2 && "dict".equals(array.get(0).getAsString())) {
            String legacyOreName = array.get(1).getAsString();
            int count = array.size() > 2 ? array.get(2).getAsInt() : 1;
            FluidContainerKey fluidContainer = fluidContainerKey(legacyOreName);
            if (fluidContainer != null) {
                return HbmIngredient.fluidContainer(fluidContainer.type(), fluidContainer.amount(), count,
                        legacyOreName);
            }
            if (FLUID_CONTAINER_DICT.matcher(legacyOreName).matches()) {
                throw new JsonSyntaxException("Unknown legacy ammo press fluid-container dict: " + legacyOreName);
            }
        }
        return LegacyGenericRecipeFormat.readLegacyRecipeAStack(array);
    }

    private static FluidContainerKey fluidContainerKey(String legacyOreName) {
        Matcher matcher = FLUID_CONTAINER_DICT.matcher(legacyOreName);
        if (!matcher.matches()) {
            return null;
        }
        int amount = Integer.parseInt(matcher.group(1));
        FluidType type = HbmFluids.fromNameCompat(matcher.group(2));
        if (type == HbmFluids.NONE) {
            return null;
        }
        return new FluidContainerKey(type, amount);
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
        String input = legacy.has("input") ? inputGridName(legacy.getAsJsonArray("input")) : "input";
        String name = sanitizeName(output + "_from_" + input);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String inputGridName(JsonArray inputs) {
        if (inputs == null || inputs.size() == 0) {
            return "empty";
        }
        List<String> names = new ArrayList<>();
        for (JsonElement element : inputs) {
            if (element != null && !element.isJsonNull()) {
                names.add(legacyAStackName(element.getAsJsonArray()));
            }
        }
        return names.isEmpty() ? "empty" : String.join("_", names);
    }

    private static String legacyAStackName(JsonArray array) {
        if (array.size() < 2) {
            return "legacy_stack";
        }
        String type = array.get(0).getAsString();
        String id = array.get(1).getAsString();
        FluidContainerKey fluidContainer = "dict".equals(type) ? fluidContainerKey(id) : null;
        if (fluidContainer != null) {
            return "container_" + fluidContainer.amount() + "_" + fluidContainer.type().toPath();
        }
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

    private record FluidContainerKey(FluidType type, int amount) {
    }
}
