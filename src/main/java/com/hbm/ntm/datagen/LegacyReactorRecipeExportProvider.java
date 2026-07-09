package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.BreedingReactorRecipe;
import com.hbm.ntm.recipe.FuelPoolRecipe;
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
import net.minecraft.world.item.ItemStack;

public final class LegacyReactorRecipeExportProvider implements DataProvider {
    private static final LegacyFile BREEDER = new LegacyFile("hbmBreeder.json", "BreederRecipes",
            "breeding_reactor", Kind.BREEDER);
    private static final LegacyFile FUEL_POOL = new LegacyFile("hbmFuelpool.json", "FuelPoolRecipes",
            "fuel_pool", Kind.FUEL_POOL);

    private static final List<LegacyFile> FILES = List.of(BREEDER, FUEL_POOL);
    private static final Comparator<ExportedRecipe> LEGACY_ORDER = Comparator
            .comparingInt(ExportedRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.recipeId().toString());

    private final Path mainRecipeRoot;
    private final Path exportDir;
    private final Path reportPath;

    public LegacyReactorRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeRoot = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes");
        this.exportDir = projectRoot.resolve("reports").resolve("legacy_recipe_exports");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_reactor_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_recipe_root", reportPath(mainRecipeRoot));
        root.addProperty("export_dir", reportPath(exportDir));
        root.addProperty("note",
                "Old reactor recipe export is datapack/debug tooling only; main-resource datapack recipes remain the runtime source of truth.");
        JsonArray handlers = new JsonArray();
        root.add("handlers", handlers);
        int writtenFileCount = 0;
        int exportedRecipeCount = 0;
        int skippedRecipeCount = 0;

        for (LegacyFile legacyFile : FILES) {
            ExportReport report = exportFile(legacyFile);
            exportedRecipeCount += report.exportedRecipes().size();
            skippedRecipeCount += report.failures().size();
            JsonObject handler = new JsonObject();
            handler.addProperty("legacy_file", legacyFile.fileName());
            handler.addProperty("legacy_class", legacyFile.legacyClassName());
            handler.addProperty("modern_recipe_type", legacyFile.outputFolder());
            handler.addProperty("source_dir", reportPath(sourceDir(legacyFile)));
            handler.addProperty("export_file", reportPath(exportPath(legacyFile)));
            handler.addProperty("status", report.status());
            handler.addProperty("exported_recipe_count", report.exportedRecipes().size());
            handler.addProperty("skipped_recipe_count", report.failures().size());
            JsonArray failures = new JsonArray();
            handler.add("failures", failures);
            for (ExportFailure failure : report.failures()) {
                JsonObject failureReport = new JsonObject();
                failureReport.addProperty("source", reportPath(failure.source()));
                failureReport.addProperty("recipe_id", failure.recipeId().toString());
                failureReport.addProperty("message", failure.message());
                failures.add(failureReport);
                HbmNtm.LOGGER.warn("Skipped legacy reactor recipe export {} from {}: {}",
                        failure.recipeId(), failure.source(), failure.message());
            }
            handlers.add(handler);

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
                HbmNtm.LOGGER.info("Exported {} legacy reactor recipes to {}",
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
        return "HBM legacy reactor recipe export";
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
                ResourceLocation recipeId = recipeId(legacyFile, recipeFile);
                try (Reader reader = Files.newBufferedReader(recipeFile)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("type") && !expectedType(legacyFile)
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    exported.add(exportRecipe(legacyFile, recipeId, json));
                } catch (RuntimeException exception) {
                    failures.add(new ExportFailure(recipeFile, recipeId, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            failures.add(new ExportFailure(sourceDir, new ResourceLocation(HbmNtm.MOD_ID, legacyFile.outputFolder()),
                    exception.getMessage()));
        }

        String status = failures.isEmpty() ? "exported" : exported.isEmpty() ? "failed" : "exported_with_skips";
        return new ExportReport(status, List.copyOf(exported), List.copyOf(failures));
    }

    private static ExportedRecipe exportRecipe(LegacyFile legacyFile, ResourceLocation recipeId, JsonObject json) {
        return switch (legacyFile.kind()) {
            case BREEDER -> {
                BreedingReactorRecipe recipe = ModRecipes.BREEDING_REACTOR.serializer().get()
                        .fromJson(recipeId, json);
                yield new ExportedRecipe(recipe.sourceOrder(), recipe.getId(), legacyBreederJson(recipe));
            }
            case FUEL_POOL -> {
                FuelPoolRecipe recipe = ModRecipes.FUEL_POOL.serializer().get().fromJson(recipeId, json);
                yield new ExportedRecipe(recipe.sourceOrder(), recipe.getId(), legacyFuelPoolJson(recipe));
            }
        };
    }

    private static JsonObject legacyBreederJson(BreedingReactorRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        object.addProperty("flux", recipe.flux());
        object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(
                deterministicOutput(recipe.output(), "hbmBreeder.json")));
        return object;
    }

    private static JsonObject legacyFuelPoolJson(FuelPoolRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", LegacyGenericRecipeFormat.writeLegacyItemStack(
                concreteFuelPoolInput(recipe.input())));
        object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(
                deterministicOutput(recipe.output(), "hbmFuelpool.json")));
        return object;
    }

    private static ItemStack concreteFuelPoolInput(HbmIngredient ingredient) {
        if (ingredient.legacyOreName() != null || ingredient.isTagIngredient()
                || ingredient.fluidContainerType() != null) {
            throw new IllegalArgumentException("Legacy hbmFuelpool.json requires a concrete ItemStack input, got "
                    + ingredient.diagnosticName());
        }
        if (ingredient.legacyId() != null) {
            if (ingredient.legacyWildcard()) {
                throw new IllegalArgumentException("Legacy hbmFuelpool.json cannot represent wildcard input "
                        + ingredient.diagnosticName());
            }
            return ingredient.mappedLegacyStack()
                    .orElseThrow(() -> new IllegalArgumentException("Missing legacy stack for "
                            + ingredient.diagnosticName()));
        }
        if (ingredient.hasExactStack()) {
            return ingredient.exactStack();
        }
        List<ItemStack> stacks = ingredient.displayStacks();
        if (stacks.size() != 1) {
            throw new IllegalArgumentException("Legacy hbmFuelpool.json requires exactly one concrete input stack, got "
                    + stacks.size() + " for " + ingredient.diagnosticName());
        }
        return stacks.get(0);
    }

    private static ItemStack deterministicOutput(HbmItemOutput output, String legacyFile) {
        if (output.oneOf() || output.entries().size() != 1) {
            throw new IllegalArgumentException("Legacy " + legacyFile + " cannot represent one_of outputs");
        }
        HbmItemOutput.Entry entry = output.entries().get(0);
        if (entry.chance() < 1.0F || entry.weight() > 0) {
            throw new IllegalArgumentException("Legacy " + legacyFile + " requires deterministic outputs");
        }
        return entry.stack();
    }

    private Path sourceDir(LegacyFile legacyFile) {
        return mainRecipeRoot.resolve(legacyFile.outputFolder());
    }

    private Path exportPath(LegacyFile legacyFile) {
        return exportDir.resolve(legacyFile.fileName());
    }

    private ResourceLocation recipeId(LegacyFile legacyFile, Path recipeFile) {
        Path relative = sourceDir(legacyFile).relativize(recipeFile);
        String path = relative.toString().replace('\\', '/');
        path = path.substring(0, path.length() - ".json".length());
        return new ResourceLocation(HbmNtm.MOD_ID, legacyFile.outputFolder() + "/" + path);
    }

    private static ResourceLocation expectedType(LegacyFile legacyFile) {
        return switch (legacyFile.kind()) {
            case BREEDER -> ModRecipes.BREEDING_REACTOR.serializer().getId();
            case FUEL_POOL -> ModRecipes.FUEL_POOL.serializer().getId();
        };
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private enum Kind {
        BREEDER,
        FUEL_POOL
    }

    private record LegacyFile(String fileName, String legacyClassName, String outputFolder, Kind kind) {
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures) {
    }

    private record ExportedRecipe(int sourceOrder, ResourceLocation recipeId, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
