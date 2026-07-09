package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.GenericMachineRecipe;
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

public final class LegacyArcFurnaceRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmArcFurnace.json";
    private static final String LEGACY_CLASS = "ArcFurnaceRecipes";
    private static final String OUTPUT_FOLDER = "arc_furnace";
    private static final int DURATION = 400;
    private static final long POWER = 1_000L;

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyArcFurnaceRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_arc_furnace_recipe_import_report.json");
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
        root.addProperty("duration", DURATION);
        root.addProperty("power", POWER);

        Path source = resolveLegacyFile();
        if (source == null) {
            root.addProperty("status", "missing_template");
            root.addProperty("source_recipe_count", 0);
            root.addProperty("imported_recipe_count", 0);
            root.addProperty("skipped_recipe_count", 0);
            saves.add(DataProvider.saveStable(output, root, reportPath));
            HbmNtm.LOGGER.info("No legacy arc furnace recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy arc furnace recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy arc furnace recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy arc furnace recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy arc furnace recipe file is missing recipes array");
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
                    JsonObject modern = arcFurnaceJson(legacy, sourceIndex);
                    validate(importedId, modern);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy arc furnace recipe file " + source,
                    exception);
        }
    }

    private static JsonObject arcFurnaceJson(JsonObject legacy, int sourceIndex) {
        HbmIngredient input = LegacyGenericRecipeFormat.readLegacyRecipeAStack(legacy.getAsJsonArray("input"));
        ItemStack solid = legacy.has("solid")
                ? LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("solid"))
                : ItemStack.EMPTY;
        List<MaterialStack> materials = legacy.has("fluid")
                ? materialOutputs(legacy.getAsJsonArray("fluid"))
                : List.of();
        if (solid.isEmpty() && materials.isEmpty()) {
            throw new JsonSyntaxException("Legacy arc furnace recipe has neither solid output nor smeltable material output");
        }

        JsonObject modern = new JsonObject();
        modern.addProperty("type", GenericMachineRecipe.Machine.ARC_FURNACE.serializerId().toString());
        modern.addProperty("internal_name", recipeName(legacy, sourceIndex));
        modern.addProperty("duration", DURATION);
        modern.addProperty("power", POWER);
        JsonArray inputs = new JsonArray();
        inputs.add(input.toJson());
        modern.add("input_items", inputs);
        modern.add("input_fluids", new JsonArray());
        JsonArray outputs = new JsonArray();
        if (!solid.isEmpty()) {
            outputs.add(HbmItemOutput.of(solid).toJson());
        }
        modern.add("output_items", outputs);
        modern.add("output_fluids", new JsonArray());
        modern.add("pools", new JsonArray());
        if (!materials.isEmpty()) {
            JsonArray materialOutputs = new JsonArray();
            for (MaterialStack material : materials) {
                materialOutputs.add(materialJson(material));
            }
            modern.add("arc_material_outputs", materialOutputs);
        }
        modern.addProperty("source_order", sourceIndex);
        return modern;
    }

    private static List<MaterialStack> materialOutputs(JsonArray legacy) {
        if (legacy == null) {
            return List.of();
        }
        List<MaterialStack> materials = new ArrayList<>();
        for (int i = 0; i < legacy.size(); i++) {
            MaterialStack stack = materialStack(legacy.get(i).getAsJsonArray(), "fluid[" + i + "]");
            if (stack.material.smeltable == SmeltingBehavior.SMELTABLE) {
                materials.add(stack);
            }
        }
        return List.copyOf(materials);
    }

    private static MaterialStack materialStack(JsonArray legacy, String name) {
        if (legacy == null || legacy.size() < 2) {
            throw new JsonSyntaxException("Legacy arc furnace material stack for " + name
                    + " needs material and amount: " + legacy);
        }
        String materialName = legacy.get(0).getAsString();
        NTMMaterial material = materialByName(materialName);
        if (material == null) {
            throw new JsonSyntaxException("Unknown arc furnace material '" + materialName + "' in " + name);
        }
        int amount = legacy.get(1).getAsInt();
        if (amount <= 0) {
            throw new JsonSyntaxException("Invalid arc furnace material amount " + amount + " in " + name);
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

    private static JsonObject materialJson(MaterialStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("material", stack.material.names[0]);
        object.addProperty("amount", stack.amount);
        return object;
    }

    private static void validate(ResourceLocation id, JsonObject modern) {
        new GenericMachineRecipe.Serializer(GenericMachineRecipe.Machine.ARC_FURNACE).fromJson(id, modern);
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
        String solid = legacy.has("solid") ? legacyItemStackName(legacy.getAsJsonArray("solid")) : "no_solid";
        String fluid = legacy.has("fluid") ? materialOutputsName(legacy.getAsJsonArray("fluid")) : "no_fluid";
        String input = legacy.has("input") ? legacyAStackName(legacy.getAsJsonArray("input")) : "input";
        String name = sanitizeName(solid + "_" + fluid + "_from_" + input);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
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

    private static String materialOutputsName(JsonArray outputs) {
        if (outputs == null || outputs.size() == 0) {
            return "no_fluid";
        }
        List<String> names = new ArrayList<>();
        for (JsonElement element : outputs) {
            JsonArray material = element.getAsJsonArray();
            if (material.size() < 2) {
                names.add("material");
            } else {
                names.add("mat_" + material.get(0).getAsString() + "_a" + material.get(1).getAsInt());
            }
        }
        return String.join("_", names);
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
