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

public final class LegacySpecialMachineRecipeImportProvider implements DataProvider {
    private static final List<LegacyFile> LEGACY_FILES = List.of(
            new LegacyFile("hbmCombination.json", "CombinationRecipes", "combination_oven", Kind.COMBINATION),
            new LegacyFile("hbmCrystallizer.json", "CrystallizerRecipes", "crystallizer", Kind.CRYSTALLIZER),
            new LegacyFile("hbmCyclotron.json", "CyclotronRecipes", "cyclotron", Kind.CYCLOTRON));

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacySpecialMachineRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_special_machine_recipe_import_report.json");
        this.legacyRecipeDir = projectRoot.resolve("legacy_recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("legacy_recipe_dir", reportPath(legacyRecipeDir));
        JsonArray handlers = new JsonArray();
        root.add("handlers", handlers);
        int foundTemplateCount = 0;
        int importedRecipeCount = 0;

        for (LegacyFile legacyFile : LEGACY_FILES) {
            JsonObject handlerReport = new JsonObject();
            handlerReport.addProperty("legacy_file", legacyFile.fileName());
            handlerReport.addProperty("legacy_class", legacyFile.legacyClassName());
            handlerReport.addProperty("modern_recipe_type", legacyFile.outputFolder());
            handlers.add(handlerReport);

            Path source = resolveLegacyFile(legacyFile.fileName());
            if (source == null) {
                handlerReport.addProperty("status", "missing_template");
                handlerReport.addProperty("source_recipe_count", 0);
                handlerReport.addProperty("imported_recipe_count", 0);
                handlerReport.addProperty("skipped_recipe_count", 0);
                continue;
            }

            foundTemplateCount++;
            handlerReport.addProperty("source", reportPath(source));
            ImportReport report = readLenient(legacyFile, source);
            importedRecipeCount += report.imported().size();
            handlerReport.addProperty("status", report.failures().isEmpty() ? "imported" : "imported_with_skips");
            handlerReport.addProperty("source_recipe_count", report.sourceRecipeCount());
            handlerReport.addProperty("imported_recipe_count", report.imported().size());
            handlerReport.addProperty("skipped_recipe_count", report.failures().size());
            JsonArray failures = new JsonArray();
            handlerReport.add("failures", failures);
            for (ImportFailure failure : report.failures()) {
                JsonObject failureReport = new JsonObject();
                failureReport.addProperty("source_index", failure.sourceIndex());
                failureReport.addProperty("requested_id", failure.requestedId().toString());
                failureReport.addProperty("message", failure.message());
                failures.add(failureReport);
                HbmNtm.LOGGER.warn("Skipped legacy {} recipe #{}: {}",
                        legacyFile.legacyClassName(), failure.sourceIndex(), failure.message());
            }
            for (ImportedRecipe recipe : report.imported()) {
                saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
            }
            HbmNtm.LOGGER.info("Imported {} {}/{} legacy special machine recipes from {} into {}",
                    legacyFile.fileName(), report.imported().size(), report.sourceRecipeCount(), source,
                    legacyFile.outputFolder());
        }

        root.addProperty("found_template_count", foundTemplateCount);
        root.addProperty("imported_recipe_count", importedRecipeCount);
        LegacyRecipeReportUtil.addImportSummary(root, handlers);
        saves.add(DataProvider.saveStable(output, root, reportPath));
        if (foundTemplateCount == 0) {
            HbmNtm.LOGGER.info("No legacy special-machine recipe templates found in {}; skipping import.",
                    legacyRecipeDir);
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy special machine recipe import";
    }

    private ImportReport readLenient(LegacyFile legacyFile, Path source) {
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Legacy recipe file is missing recipes array");
            }
            List<ImportedRecipe> imported = new ArrayList<>();
            List<ImportFailure> failures = new ArrayList<>();
            Map<ResourceLocation, Integer> usedIds = new LinkedHashMap<>();
            for (int sourceIndex = 0; sourceIndex < recipes.size(); sourceIndex++) {
                JsonElement element = recipes.get(sourceIndex);
                if (element == null || element.isJsonNull()) {
                    continue;
                }
                ResourceLocation requestedId = id(legacyFile.outputFolder() + "/legacy_import_" + sourceIndex);
                try {
                    JsonObject legacy = element.getAsJsonObject();
                    rejectExcludedBedrockOreRecipe(legacyFile, legacy);
                    requestedId = id(legacyFile.outputFolder() + "/" + recipeName(legacyFile, legacy, sourceIndex));
                    ResourceLocation importedId = uniqueId(requestedId, usedIds);
                    JsonObject modern = recipeJson(legacyFile, legacy, sourceIndex);
                    imported.add(new ImportedRecipe(importedId, modern));
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, requestedId, exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy special machine recipe file " + source,
                    exception);
        }
    }

    private static JsonObject recipeJson(LegacyFile legacyFile, JsonObject legacy, int sourceIndex) {
        return switch (legacyFile.kind()) {
            case COMBINATION -> combinationJson(legacy);
            case CRYSTALLIZER -> crystallizerJson(legacy, sourceIndex);
            case CYCLOTRON -> cyclotronJson(legacy, sourceIndex);
        };
    }

    private static JsonObject combinationJson(JsonObject legacy) {
        HbmIngredient input = singleItemInput(LegacyGenericRecipeFormat.readLegacyRecipeAStack(
                legacy.getAsJsonArray("input")));
        ItemStack output = legacy.has("output")
                ? LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("output"))
                : ItemStack.EMPTY;
        HbmFluidStack fluid = legacy.has("fluid") ? fluidStack(legacy.getAsJsonArray("fluid")) : null;
        return CompatRecipeRegistry.createCombination(input, output, fluid);
    }

    private static JsonObject crystallizerJson(JsonObject legacy, int sourceIndex) {
        ItemStack output = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("output"));
        HbmIngredient input = LegacyGenericRecipeFormat.readLegacyRecipeAStack(legacy.getAsJsonArray("input"));
        HbmFluidStack fluid = fluidStack(legacy.getAsJsonArray("fluid"));
        int duration = legacy.get("duration").getAsInt();
        float productivity = legacy.has("productivity") ? legacy.get("productivity").getAsFloat() : 0.0F;
        return CompatRecipeRegistry.createCrystallizer(input, HbmItemOutput.of(output), duration, productivity,
                fluid, sourceIndex);
    }

    private static JsonObject cyclotronJson(JsonObject legacy, int sourceIndex) {
        ItemStack particle = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("particle"));
        HbmIngredient input = LegacyGenericRecipeFormat.readLegacyRecipeAStack(legacy.getAsJsonArray("input"));
        ItemStack output = LegacyGenericRecipeFormat.readLegacyRecipeItemStack(legacy.getAsJsonArray("output"));
        int antimatter = legacy.get("antimatter").getAsInt();
        return CompatRecipeRegistry.createCyclotron(HbmIngredient.exact(particle), input, output, antimatter,
                sourceIndex);
    }

    private static HbmIngredient singleItemInput(HbmIngredient input) {
        return new HbmIngredient(input.ingredient(), 1, input.exactStack(), input.partialNbt(),
                input.legacyId(), input.legacyMeta(), input.legacyWildcard(), input.legacyOreName(),
                input.fluidContainerType(), input.fluidContainerAmount());
    }

    private static HbmFluidStack fluidStack(JsonArray legacy) {
        if (legacy == null || legacy.size() < 2) {
            throw new JsonSyntaxException("Legacy fluid stack needs fluid and amount: " + legacy);
        }
        JsonObject object = new JsonObject();
        object.add("fluid", legacy.get(0).deepCopy());
        object.add("amount", legacy.get(1).deepCopy());
        if (legacy.size() > 2) {
            object.add("pressure", legacy.get(2).deepCopy());
        }
        return HbmFluidJsonUtil.readFluidStack(object, "legacy special machine fluid");
    }

    private Path resolveLegacyFile(String legacyFileName) {
        Path direct = legacyRecipeDir.resolve(legacyFileName);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        Path template = legacyRecipeDir.resolve("_" + legacyFileName);
        if (Files.isRegularFile(template)) {
            return template;
        }
        return null;
    }

    private static String recipeName(LegacyFile legacyFile, JsonObject legacy, int sourceIndex) {
        String name = switch (legacyFile.kind()) {
            case COMBINATION -> combinationName(legacy);
            case CRYSTALLIZER -> crystallizerName(legacy);
            case CYCLOTRON -> cyclotronName(legacy);
        };
        name = sanitizeName(name);
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static String combinationName(JsonObject legacy) {
        String input = legacy.has("input") ? legacyAStackName(legacy.getAsJsonArray("input")) : "input";
        String output = legacy.has("output") ? legacyItemStackName(legacy.getAsJsonArray("output")) : "no_item";
        String fluid = legacy.has("fluid") ? "_fluid_" + fluidName(legacy.getAsJsonArray("fluid")) : "";
        return input + "_to_" + output + fluid;
    }

    private static String crystallizerName(JsonObject legacy) {
        String input = legacy.has("input") ? legacyAStackName(legacy.getAsJsonArray("input")) : "input";
        String fluid = legacy.has("fluid") ? "_fluid_" + fluidName(legacy.getAsJsonArray("fluid")) : "";
        String output = legacy.has("output") ? legacyItemStackName(legacy.getAsJsonArray("output")) : "output";
        return input + fluid + "_to_" + output;
    }

    private static String cyclotronName(JsonObject legacy) {
        String particle = legacy.has("particle") ? legacyItemStackName(legacy.getAsJsonArray("particle")) : "particle";
        String input = legacy.has("input") ? legacyAStackName(legacy.getAsJsonArray("input")) : "input";
        String output = legacy.has("output") ? legacyItemStackName(legacy.getAsJsonArray("output")) : "output";
        return particle + "_" + input + "_to_" + output;
    }

    private static String legacyAStackName(JsonArray array) {
        if (array.size() < 2) {
            return "legacy_stack";
        }
        String type = array.get(0).getAsString();
        String item = array.get(1).getAsString();
        String count = array.size() > 2 && array.get(2).getAsInt() != 1 ? "_x" + array.get(2).getAsInt() : "";
        String meta = array.size() > 3 ? "_m" + array.get(3).getAsInt() : "";
        return type + "_" + item + count + meta;
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

    private static void rejectExcludedBedrockOreRecipe(LegacyFile legacyFile, JsonObject legacy) {
        if (legacyFile.kind() == Kind.CYCLOTRON) {
            return;
        }
        String raw = legacy.toString().toLowerCase(Locale.ROOT);
        if (raw.contains("bedrockore")
                || raw.contains("itembedrockore")
                || raw.contains("ore_bedrock")
                || raw.contains("ore_centrifuged")
                || raw.contains("ore_cleaned")
                || raw.contains("ore_separated")
                || raw.contains("ore_purified")
                || raw.contains("ore_nitrated")
                || raw.contains("ore_nitrocrystalline")
                || raw.contains("ore_deepcleaned")
                || raw.contains("ore_seared")
                || raw.contains("ore_enriched")) {
            throw new JsonSyntaxException("legacy bedrock ore processing loop is excluded by modernization rule");
        }
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record LegacyFile(String fileName, String legacyClassName, String outputFolder, Kind kind) {
    }

    private enum Kind {
        COMBINATION,
        CRYSTALLIZER,
        CYCLOTRON
    }

    private record ImportReport(int sourceRecipeCount, List<ImportedRecipe> imported, List<ImportFailure> failures) {
    }

    private record ImportedRecipe(ResourceLocation id, JsonObject json) {
    }

    private record ImportFailure(int sourceIndex, ResourceLocation requestedId, String message) {
    }
}
