package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public final class LegacyShredderRecipeImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmShredder.json";
    private static final String LEGACY_CLASS = "ShredderRecipes";
    private static final String OUTPUT_FOLDER = "shredder";

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyShredderRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_shredder_recipe_import_report.json");
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
            HbmNtm.LOGGER.info("No legacy shredder recipe template found in {}; skipping import.",
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
            HbmNtm.LOGGER.warn("Skipped legacy shredder recipe #{}: {}",
                    failure.sourceIndex(), failure.message());
        }
        for (ImportedRecipe recipe : report.imported()) {
            saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
        }
        saves.add(DataProvider.saveStable(output, root, reportPath));
        HbmNtm.LOGGER.info("Imported {}/{} legacy shredder recipes from {} into {}",
                report.imported().size(), report.sourceRecipeCount(), source, OUTPUT_FOLDER);
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy shredder recipe import";
    }

    private ImportReport readLenient(Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy shredder recipe file is missing recipes array");
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
                    JsonObject modern = shredderJson(legacy, sourceIndex);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy shredder recipe file " + source,
                    exception);
        }
    }

    private static JsonObject shredderJson(JsonObject legacy, int sourceIndex) {
        HbmIngredient input = shredderInput(legacy.getAsJsonArray("input"));
        ItemStack output = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("output"));
        if (output.isEmpty()) {
            throw new JsonSyntaxException("Legacy shredder output resolved to empty");
        }
        return CompatRecipeRegistry.createItemProcessing(ItemProcessingRecipe.Machine.SHREDDER, input,
                List.of(HbmItemOutput.of(output)), null, 0, 0.0F, sourceIndex);
    }

    private static HbmIngredient shredderInput(JsonArray legacyInput) {
        if (legacyInput == null || legacyInput.size() < 1) {
            throw new JsonSyntaxException("Legacy shredder input needs an item id");
        }
        ResourceLocation legacyId = normalizeLegacyId(legacyInput.get(0).getAsString());
        int legacyMeta = legacyInput.size() > 2 ? legacyInput.get(2).getAsInt() : 0;
        if (legacyMeta == HbmIngredient.WILDCARD_META) {
            if (!LegacyMetaItemMappings.stacks(legacyId, 1).isEmpty()) {
                return HbmIngredient.legacyWildcard(legacyId, 1);
            }
            return HbmIngredient.of(legacyItem(legacyId), 1);
        }
        if (legacyMeta != 0) {
            if (LegacyMetaItemMappings.hasMapping(legacyId, legacyMeta)) {
                return HbmIngredient.legacyMeta(legacyId, legacyMeta, 1);
            }
            throw new JsonSyntaxException("Missing legacy shredder input meta mapping: "
                    + legacyId + " meta " + legacyMeta);
        }
        return HbmIngredient.of(legacyItem(legacyId), 1);
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
        String input = legacy.has("input") ? legacyInputName(legacy.getAsJsonArray("input")) : "input";
        String output = legacy.has("output") ? legacyItemStackName(legacy.getAsJsonArray("output")) : "output";
        String name = sanitizeName(input + "_to_" + output);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String legacyInputName(JsonArray array) {
        if (array.size() < 1) {
            return "legacy_input";
        }
        String meta = array.size() > 2 ? "_m" + array.get(2).getAsInt() : "";
        return array.get(0).getAsString() + meta;
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

    private static Item legacyItem(ResourceLocation legacyId) {
        return HbmRegistryUtil.item(modernIdForRegistryLookup(legacyId))
                .or(() -> legacyRegistryObject(legacyId).map(RegistryObject::get))
                .orElseThrow(() -> new JsonSyntaxException("Unknown legacy shredder item: " + legacyId));
    }

    private static Optional<RegistryObject<Item>> legacyRegistryObject(ResourceLocation legacyId) {
        if (!isHbmLegacyNamespace(legacyId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(ModItems.legacyItem(legacyId.getPath()));
    }

    private static ResourceLocation normalizeLegacyId(String raw) {
        ResourceLocation parsed = raw.contains(":") ? new ResourceLocation(raw) : new ResourceLocation("hbm", raw);
        return isHbmLegacyNamespace(parsed) ? new ResourceLocation("hbm", parsed.getPath()) : parsed;
    }

    private static ResourceLocation modernIdForRegistryLookup(ResourceLocation legacyId) {
        return isHbmLegacyNamespace(legacyId) ? new ResourceLocation(HbmNtm.MOD_ID, legacyId.getPath()) : legacyId;
    }

    private static boolean isHbmLegacyNamespace(ResourceLocation id) {
        return "hbm".equals(id.getNamespace()) || HbmNtm.MOD_ID.equals(id.getNamespace());
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
