package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.DiFurnaceRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.ModRecipes;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class LegacyDiFurnaceRecipeExportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmBlastFurnaceLegacy.json";
    private static final String LEGACY_CLASS = "BlastFurnaceRecipes";
    private static final String OUTPUT_FOLDER = "difurnace";
    private static final int LEGACY_GASOLINE_META = 86;

    private final Path mainRecipeDir;
    private final Path exportPath;
    private final Path reportPath;

    public LegacyDiFurnaceRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeDir = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes")
                .resolve(OUTPUT_FOLDER);
        this.exportPath = projectRoot.resolve("reports").resolve("legacy_recipe_exports").resolve(LEGACY_FILE);
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_difurnace_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_dir", reportPath(mainRecipeDir));
        root.addProperty("export_file", reportPath(exportPath));
        root.addProperty("legacy_file", LEGACY_FILE);
        root.addProperty("legacy_class", LEGACY_CLASS);
        root.addProperty("modern_recipe_type", OUTPUT_FOLDER);
        root.addProperty("note", "Exports hbm_ntm_rebirth:difurnace datapack recipes to the deprecated BlastFurnaceRecipes JSON shape for tooling/debug parity only.");

        ExportReport report = exportRecipes();
        root.addProperty("status", report.status());
        root.addProperty("written_file_count", report.exportedRecipes().isEmpty() ? 0 : 1);
        root.addProperty("exported_recipe_count", report.exportedRecipes().size());
        root.addProperty("skipped_recipe_count", report.failures().size());
        JsonArray failures = new JsonArray();
        root.add("failures", failures);
        for (ExportFailure failure : report.failures()) {
            JsonObject failureReport = new JsonObject();
            failureReport.addProperty("source", reportPath(failure.source()));
            failureReport.addProperty("recipe_id", failure.recipeId().toString());
            failureReport.addProperty("message", failure.message());
            failures.add(failureReport);
            HbmNtm.LOGGER.warn("Skipped legacy DiFurnace recipe export {} from {}: {}",
                    failure.recipeId(), failure.source(), failure.message());
        }

        if (!report.exportedRecipes().isEmpty()) {
            JsonObject legacyRoot = new JsonObject();
            JsonArray recipeArray = new JsonArray();
            legacyRoot.add("recipes", recipeArray);
            report.exportedRecipes().stream()
                    .sorted(Comparator.comparingInt(ExportedRecipe::sourceOrder)
                            .thenComparing(recipe -> recipe.recipe().getId().toString()))
                    .map(ExportedRecipe::legacyJson)
                    .forEach(recipeArray::add);
            saves.add(DataProvider.saveStable(output, legacyRoot, exportPath));
            HbmNtm.LOGGER.info("Exported {} legacy DiFurnace recipes to {}",
                    report.exportedRecipes().size(), exportPath);
        }

        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy DiFurnace recipe export";
    }

    private ExportReport exportRecipes() {
        if (!Files.isDirectory(mainRecipeDir)) {
            return new ExportReport("missing_source_dir", List.of(), List.of());
        }

        List<ExportedRecipe> exported = new ArrayList<>();
        List<ExportFailure> failures = new ArrayList<>();
        try (var stream = Files.walk(mainRecipeDir)) {
            List<Path> recipeFiles = stream
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path recipeFile : recipeFiles) {
                ResourceLocation recipeId = recipeId(recipeFile);
                try (Reader reader = Files.newBufferedReader(recipeFile)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("type") && !ModRecipes.DIFURNACE.serializer().getId()
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    DiFurnaceRecipe recipe = ModRecipes.DIFURNACE.serializer().get()
                            .fromJson(recipeId, json);
                    exported.add(new ExportedRecipe(recipe, sourceOrder(json), legacyJson(recipe, json)));
                } catch (RuntimeException exception) {
                    failures.add(new ExportFailure(recipeFile, recipeId, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            failures.add(new ExportFailure(mainRecipeDir, new ResourceLocation(HbmNtm.MOD_ID, OUTPUT_FOLDER),
                    exception.getMessage()));
        }

        String status = failures.isEmpty() ? "exported" : exported.isEmpty() ? "failed" : "exported_with_skips";
        return new ExportReport(status, List.copyOf(exported), List.copyOf(failures));
    }

    private static JsonObject legacyJson(DiFurnaceRecipe recipe, JsonObject sourceJson) {
        JsonArray inputs = sourceJson.getAsJsonArray("inputs");
        if (inputs == null || inputs.size() != 2) {
            throw new JsonSyntaxException("DiFurnace source JSON needs exactly two inputs for legacy export");
        }

        JsonObject object = new JsonObject();
        object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(recipe.output().representativeStack()));
        object.add("input1", legacyInput(inputs.get(0).getAsJsonObject()));
        object.add("input2", legacyInput(inputs.get(1).getAsJsonObject()));
        if (sourceJson.has("legacy_hidden") && sourceJson.get("legacy_hidden").getAsBoolean()) {
            object.addProperty("hidden", true);
        }
        return object;
    }

    private static JsonArray legacyInput(JsonObject sourceInput) {
        if (sourceInput.has("legacy_dictframe")) {
            JsonArray array = new JsonArray();
            array.add("dictframe");
            array.add(sourceInput.get("legacy_dictframe").getAsString());
            return array;
        }

        HbmIngredient ingredient = HbmIngredient.fromJson(sourceInput);
        if (isGasolineCanister(sourceInput, ingredient)) {
            JsonArray array = new JsonArray();
            array.add("item");
            array.add("hbm:canister_full");
            if (ingredient.count() != 1 || LEGACY_GASOLINE_META != 0) {
                array.add(ingredient.count());
            }
            array.add(LEGACY_GASOLINE_META);
            return array;
        }

        return LegacyGenericRecipeFormat.writeLegacyAStack(ingredient);
    }

    private static boolean isGasolineCanister(JsonObject sourceInput, HbmIngredient ingredient) {
        if (!sourceInput.has("legacy_item")
                || !"canister_full".equals(sourceInput.get("legacy_item").getAsString())
                || !ingredient.hasPartialNbt()) {
            return false;
        }
        CompoundTag tag = ingredient.partialNbt();
        return "GASOLINE".equals(tag.getString("hbm_fluid"))
                && tag.getInt("hbm_fluid_amount") == 1_000;
    }

    private static int sourceOrder(JsonObject json) {
        return json.has("source_order") ? json.get("source_order").getAsInt()
                : Integer.MAX_VALUE;
    }

    private ResourceLocation recipeId(Path recipeFile) {
        Path relative = mainRecipeDir.relativize(recipeFile);
        String path = relative.toString().replace('\\', '/');
        path = path.substring(0, path.length() - ".json".length());
        return new ResourceLocation(HbmNtm.MOD_ID, OUTPUT_FOLDER + "/" + path);
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures) {
    }

    private record ExportedRecipe(DiFurnaceRecipe recipe, int sourceOrder, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
