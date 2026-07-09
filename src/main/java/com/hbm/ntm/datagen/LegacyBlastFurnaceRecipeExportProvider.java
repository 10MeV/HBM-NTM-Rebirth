package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
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
import net.minecraft.resources.ResourceLocation;

public final class LegacyBlastFurnaceRecipeExportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmBlastFurnace.json";
    private static final String OUTPUT_FOLDER = "blast_furnace";

    private final Path mainRecipeDir;
    private final Path exportPath;
    private final Path reportPath;

    public LegacyBlastFurnaceRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeDir = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes")
                .resolve(OUTPUT_FOLDER);
        this.exportPath = projectRoot.resolve("reports").resolve("legacy_recipe_exports").resolve(LEGACY_FILE);
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_blast_furnace_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_dir", reportPath(mainRecipeDir));
        root.addProperty("export_file", reportPath(exportPath));
        root.addProperty("legacy_file", LEGACY_FILE);
        root.addProperty("legacy_class", "BlastFurnaceRecipesNT");
        root.addProperty("modern_recipe_type", OUTPUT_FOLDER);
        root.addProperty("note", "Deprecated hbmBlastFurnaceLegacy.json belongs to BlastFurnaceRecipes and is not exported here.");

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
            HbmNtm.LOGGER.warn("Skipped legacy blast furnace recipe export {} from {}: {}",
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
            HbmNtm.LOGGER.info("Exported {} legacy blast furnace recipes to {}",
                    report.exportedRecipes().size(), exportPath);
        }

        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy blast furnace recipe export";
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
                    if (json.has("type") && !ModRecipes.BLAST_FURNACE.serializer().getId()
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    BlastFurnaceRecipe recipe = ModRecipes.BLAST_FURNACE.serializer().get()
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

    private static JsonObject legacyJson(BlastFurnaceRecipe recipe, JsonObject sourceJson) {
        JsonObject object = new JsonObject();
        object.addProperty("name", sourceJson.has("name")
                ? sourceJson.get("name").getAsString()
                : fallbackName(recipe.getId()));
        object.add("inputItem", legacyInputs(recipe.inputs()));
        object.add("outputItem", legacyOutputs(recipe.outputs()));
        object.addProperty("duration", recipe.duration());
        object.addProperty("power", sourceJson.has("power") ? sourceJson.get("power").getAsLong() : 0L);
        return object;
    }

    private static JsonArray legacyInputs(List<HbmIngredient> inputs) {
        JsonArray array = new JsonArray();
        for (HbmIngredient input : inputs) {
            array.add(LegacyGenericRecipeFormat.writeLegacyAStack(input));
        }
        return array;
    }

    private static JsonArray legacyOutputs(List<HbmItemOutput> outputs) {
        JsonArray array = new JsonArray();
        for (HbmItemOutput output : outputs) {
            array.add(LegacyGenericRecipeFormat.writeLegacyItemOutput(output));
        }
        return array;
    }

    private static int sourceOrder(JsonObject json) {
        return json.has("source_order") ? json.get("source_order").getAsInt()
                : Integer.MAX_VALUE;
    }

    private static String fallbackName(ResourceLocation recipeId) {
        String path = recipeId.getPath();
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
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

    private record ExportedRecipe(BlastFurnaceRecipe recipe, int sourceOrder, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
