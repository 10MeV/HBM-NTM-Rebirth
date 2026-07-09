package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.DiFurnaceRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModItems;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class LegacyDiFurnaceRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmBlastFurnaceLegacy.json";
    private static final String LEGACY_CLASS = "BlastFurnaceRecipes";
    private static final String OUTPUT_FOLDER = "difurnace";
    private static final int LEGACY_GASOLINE_META = 86;

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyDiFurnaceRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_difurnace_recipe_import_report.json");
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
        root.addProperty("note", "Imports deprecated BlastFurnaceRecipes into hbm_ntm_rebirth:difurnace datapack recipes; old runtime map/config hot-loading is not restored.");

        Path source = resolveLegacyFile();
        if (source == null) {
            root.addProperty("status", "missing_template");
            root.addProperty("source_recipe_count", 0);
            root.addProperty("imported_recipe_count", 0);
            root.addProperty("skipped_recipe_count", 0);
            saves.add(DataProvider.saveStable(output, root, reportPath));
            HbmNtm.LOGGER.info("No legacy DiFurnace recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy DiFurnace recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy DiFurnace recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy DiFurnace recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy DiFurnace recipe file is missing recipes array");
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
                    JsonObject modern = diFurnaceJson(legacy, sourceIndex);
                    validate(importedId, modern);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy DiFurnace recipe file " + source,
                    exception);
        }
    }

    private static JsonObject diFurnaceJson(JsonObject legacy, int sourceIndex) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", ModRecipes.DIFURNACE.serializer().getId().toString());
        modern.addProperty("internal_name", "difurnace.legacyImport." + sourceIndex);
        modern.addProperty("source_order", sourceIndex);

        JsonArray inputs = new JsonArray();
        inputs.add(readLegacyDiFurnaceInput(requiredArray(legacy, "input1")));
        inputs.add(readLegacyDiFurnaceInput(requiredArray(legacy, "input2")));
        modern.add("inputs", inputs);

        ItemStack output = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(requiredArray(legacy, "output"));
        modern.add("output", HbmItemOutput.of(output).toJson());
        if (legacy.has("hidden") && legacy.get("hidden").getAsBoolean()) {
            modern.addProperty("legacy_hidden", true);
        }
        return modern;
    }

    private static JsonObject readLegacyDiFurnaceInput(JsonArray array) {
        String type = array.get(0).getAsString();
        if ("dictframe".equals(type)) {
            if (array.size() < 2) {
                throw new JsonSyntaxException("Legacy dictframe input is missing material name");
            }
            return dictFrameInput(array.get(1).getAsString());
        }
        if ("item".equals(type) && isLegacyGasolineCanister(array)) {
            int count = array.size() > 2 ? array.get(2).getAsInt() : 1;
            return gasolineCanisterInput(count);
        }
        if ("dict".equals(type)) {
            return HbmIngredient.legacyOre(array.get(1).getAsString(), 1).toJson();
        }
        return LegacyGenericRecipeFormat.readLegacyRecipeAStack(array).toJson();
    }

    private static JsonObject dictFrameInput(String materialName) {
        JsonArray alternatives = new JsonArray();
        addLegacyOreAlternative(alternatives, "ingot" + materialName);
        addLegacyOreAlternative(alternatives, "plate" + materialName);
        addLegacyOreAlternative(alternatives, "gem" + materialName);
        addLegacyOreAlternative(alternatives, "dust" + materialName);
        JsonObject input = HbmIngredient.of(Ingredient.fromJson(alternatives), 1).toJson();
        input.addProperty("legacy_dictframe", materialName);
        return input;
    }

    private static void addLegacyOreAlternative(JsonArray alternatives, String legacyOreName) {
        JsonObject tag = new JsonObject();
        tag.addProperty("tag", LegacyOreDictionaryMappings.itemTagId(legacyOreName).toString());
        alternatives.add(tag);
    }

    private static boolean isLegacyGasolineCanister(JsonArray array) {
        ResourceLocation itemId = normalizeLegacyId(array.get(1).getAsString());
        int legacyMeta = array.size() > 3 ? array.get(3).getAsInt() : 0;
        return new ResourceLocation("hbm", "canister_full").equals(itemId)
                && legacyMeta == LEGACY_GASOLINE_META;
    }

    private static JsonObject gasolineCanisterInput(int count) {
        ItemStack stack = new ItemStack(ModItems.CANISTER_FULL.get(), count);
        CompoundTag tag = new CompoundTag();
        tag.putString("hbm_fluid", HbmFluids.GASOLINE.getName());
        tag.putInt("hbm_fluid_amount", 1_000);
        tag.putInt("hbm_fluid_pressure", 0);
        stack.setTag(tag);
        JsonObject input = HbmIngredient.partialNbt(stack).toJson();
        input.addProperty("legacy_item", "canister_full");
        return input;
    }

    private static JsonArray requiredArray(JsonObject object, String name) {
        JsonArray array = object.getAsJsonArray(name);
        if (array == null) {
            throw new JsonSyntaxException("Legacy DiFurnace recipe is missing " + name + " array");
        }
        return array;
    }

    private static void validate(ResourceLocation id, JsonObject modern) {
        new DiFurnaceRecipe.Serializer().fromJson(id, modern);
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
        if (legacy.has("name") && !legacy.get("name").getAsString().isBlank()) {
            return sanitizeName(legacy.get("name").getAsString());
        }
        String output = legacy.has("output") ? outputName(legacy.getAsJsonArray("output")) : "output";
        String first = legacy.has("input1") ? inputName(legacy.getAsJsonArray("input1")) : "input1";
        String second = legacy.has("input2") ? inputName(legacy.getAsJsonArray("input2")) : "input2";
        String name = sanitizeName(output + "_from_" + first + "_" + second);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String outputName(JsonArray array) {
        try {
            ItemStack stack = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(array);
            ResourceLocation itemId = HbmRegistryUtil.itemKey(stack.getItem());
            if (itemId != null) {
                return itemId.toString() + (stack.getCount() > 1 ? "_x" + stack.getCount() : "");
            }
        } catch (RuntimeException ignored) {
        }
        return "output";
    }

    private static String inputName(JsonArray array) {
        if (array.size() < 2) {
            return "input";
        }
        String type = array.get(0).getAsString();
        String id = array.get(1).getAsString();
        String count = array.size() > 2 && array.get(2).getAsInt() != 1 ? "_x" + array.get(2).getAsInt() : "";
        String meta = array.size() > 3 ? "_m" + array.get(3).getAsInt() : "";
        return type + "_" + id + count + meta;
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

    private static ResourceLocation normalizeLegacyId(String id) {
        ResourceLocation parsed = id.contains(":") ? new ResourceLocation(id) : new ResourceLocation("hbm", id);
        return HbmNtm.MOD_ID.equals(parsed.getNamespace()) ? new ResourceLocation("hbm", parsed.getPath()) : parsed;
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
