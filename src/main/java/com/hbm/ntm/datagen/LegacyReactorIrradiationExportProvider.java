package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.ExposureChamberRecipe;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.OutgasserRecipe;
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

public final class LegacyReactorIrradiationExportProvider implements DataProvider {
    private static final List<LegacyFile> LEGACY_FILES = List.of(
            new LegacyFile("hbmIrradiation.json", "OutgasserRecipes", "outgasser", Kind.OUTGASSER),
            new LegacyFile("hbmExposureChamber.json", "ExposureChamberRecipes", "exposure_chamber",
                    Kind.EXPOSURE_CHAMBER));

    private static final Comparator<ExportedRecipe> LEGACY_ORDER = Comparator
            .comparingInt(ExportedRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.recipeId().toString());

    private final Path mainRecipeRoot;
    private final Path exportDir;
    private final Path reportPath;

    public LegacyReactorIrradiationExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeRoot = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes");
        this.exportDir = projectRoot.resolve("reports").resolve("legacy_recipe_exports");
        this.reportPath = projectRoot.resolve("reports")
                .resolve("legacy_reactor_irradiation_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_root", reportPath(mainRecipeRoot));
        root.addProperty("export_dir", reportPath(exportDir));
        root.addProperty("note",
                "Old reactor irradiation JSON exports are datapack/debug tooling only; main-resource datapack recipes remain the runtime source of truth.");

        JsonArray handlers = new JsonArray();
        root.add("handlers", handlers);
        int writtenFileCount = 0;
        int exportedRecipeCount = 0;
        int skippedRecipeCount = 0;

        for (LegacyFile legacyFile : LEGACY_FILES) {
            ExportReport report = exportFile(legacyFile);
            exportedRecipeCount += report.exportedRecipes().size();
            skippedRecipeCount += report.failures().size();

            JsonObject handlerReport = new JsonObject();
            handlerReport.addProperty("legacy_file", legacyFile.fileName());
            handlerReport.addProperty("legacy_class", legacyFile.legacyClassName());
            handlerReport.addProperty("modern_recipe_type", legacyFile.outputFolder());
            handlerReport.addProperty("source_dir", reportPath(sourceDir(legacyFile)));
            handlerReport.addProperty("export_file", reportPath(exportPath(legacyFile)));
            handlerReport.addProperty("status", report.status());
            handlerReport.addProperty("exported_recipe_count", report.exportedRecipes().size());
            handlerReport.addProperty("skipped_recipe_count", report.failures().size());
            JsonArray failures = new JsonArray();
            handlerReport.add("failures", failures);
            for (ExportFailure failure : report.failures()) {
                JsonObject failureReport = new JsonObject();
                failureReport.addProperty("source", reportPath(failure.source()));
                failureReport.addProperty("recipe_id", failure.recipeId().toString());
                failureReport.addProperty("message", failure.message());
                failures.add(failureReport);
                HbmNtm.LOGGER.warn("Skipped legacy reactor irradiation recipe export {} from {}: {}",
                        failure.recipeId(), failure.source(), failure.message());
            }
            handlers.add(handlerReport);

            if (!report.exportedRecipes().isEmpty()) {
                JsonObject legacyRoot = new JsonObject();
                JsonArray recipeArray = new JsonArray();
                legacyRoot.add("recipes", recipeArray);
                report.exportedRecipes().stream()
                        .sorted(LEGACY_ORDER)
                        .map(ExportedRecipe::legacyJson)
                        .forEach(recipeArray::add);
                saves.add(DataProvider.saveStable(output, legacyRoot, exportPath(legacyFile)));
                writtenFileCount++;
                HbmNtm.LOGGER.info("Exported {} legacy reactor irradiation recipes to {}",
                        report.exportedRecipes().size(), exportPath(legacyFile));
            }
        }

        root.addProperty("written_file_count", writtenFileCount);
        root.addProperty("exported_recipe_count", exportedRecipeCount);
        root.addProperty("skipped_recipe_count", skippedRecipeCount);
        root.addProperty("status", skippedRecipeCount == 0 ? "exported" : "exported_with_skips");
        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy reactor irradiation recipe export";
    }

    private ExportReport exportFile(LegacyFile legacyFile) {
        Path sourceDir = sourceDir(legacyFile);
        if (!Files.isDirectory(sourceDir)) {
            return new ExportReport("missing_source_dir", List.of(), List.of());
        }

        List<ExportedRecipe> exported = new ArrayList<>();
        List<ExportFailure> failures = new ArrayList<>();
        try (var stream = Files.walk(sourceDir)) {
            List<Path> recipeFiles = stream
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path recipeFile : recipeFiles) {
                ResourceLocation recipeId = recipeId(recipeFile, legacyFile);
                try (Reader reader = Files.newBufferedReader(recipeFile)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("type") && !serializerId(legacyFile)
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    exported.add(exportedRecipe(legacyFile, recipeId, json));
                } catch (RuntimeException exception) {
                    failures.add(new ExportFailure(recipeFile, recipeId, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            failures.add(new ExportFailure(sourceDir, id(legacyFile.outputFolder()), exception.getMessage()));
        }

        String status = failures.isEmpty()
                ? exported.isEmpty() ? "empty_source_dir" : "exported"
                : exported.isEmpty() ? "failed" : "exported_with_skips";
        return new ExportReport(status, List.copyOf(exported), List.copyOf(failures));
    }

    private static ExportedRecipe exportedRecipe(LegacyFile legacyFile, ResourceLocation recipeId, JsonObject json) {
        return switch (legacyFile.kind()) {
            case OUTGASSER -> {
                OutgasserRecipe recipe = ModRecipes.OUTGASSER.serializer().get().fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, recipe.sourceOrder(), legacyOutgasserJson(recipe));
            }
            case EXPOSURE_CHAMBER -> {
                ExposureChamberRecipe recipe = ModRecipes.EXPOSURE_CHAMBER.serializer().get()
                        .fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, recipe.sourceOrder(), legacyExposureChamberJson(recipe));
            }
        };
    }

    private static JsonObject legacyOutgasserJson(OutgasserRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        recipe.solidOutput().ifPresent(stack -> object.add("solidOutput",
                LegacyGenericRecipeFormat.writeLegacyItemStack(stack)));
        recipe.fluidOutput().ifPresent(stack -> object.add("fluidOutput",
                LegacyGenericRecipeFormat.writeLegacyFluidStack(stack)));
        object.addProperty("fusionOnly", recipe.fusionOnly());
        return object;
    }

    private static JsonObject legacyExposureChamberJson(ExposureChamberRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("particle", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.particle()));
        object.add("ingredient", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.ingredient()));
        object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(recipe.output()));
        return object;
    }

    private Path sourceDir(LegacyFile legacyFile) {
        return mainRecipeRoot.resolve(legacyFile.outputFolder());
    }

    private Path exportPath(LegacyFile legacyFile) {
        return exportDir.resolve(legacyFile.fileName());
    }

    private ResourceLocation recipeId(Path recipeFile, LegacyFile legacyFile) {
        Path relative = sourceDir(legacyFile).relativize(recipeFile);
        String path = relative.toString().replace('\\', '/');
        path = path.substring(0, path.length() - ".json".length());
        return id(legacyFile.outputFolder() + "/" + path);
    }

    private static ResourceLocation serializerId(LegacyFile legacyFile) {
        return switch (legacyFile.kind()) {
            case OUTGASSER -> ModRecipes.OUTGASSER.serializer().getId();
            case EXPOSURE_CHAMBER -> ModRecipes.EXPOSURE_CHAMBER.serializer().getId();
        };
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
        OUTGASSER,
        EXPOSURE_CHAMBER
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures) {
    }

    private record ExportedRecipe(ResourceLocation recipeId, int sourceOrder, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
