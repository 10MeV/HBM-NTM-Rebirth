package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.LegacyGenericRecipeImporter;
import com.hbm.ntm.recipe.LegacySerializableRecipeHandlers;
import com.hbm.ntm.recipe.LegacyBlueprintPools;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LegacyGenericRecipeImportProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyGenericRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_generic_recipe_import_report.json");
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
        for (LegacySerializableRecipeHandlers.Handler handler : LegacySerializableRecipeHandlers.supportedGeneric()) {
            JsonObject handlerReport = new JsonObject();
            handlerReport.addProperty("legacy_file", handler.legacyFileName());
            handlerReport.addProperty("legacy_class", handler.legacyClassName());
            handlerReport.addProperty("modern_recipe_type", handler.modernRecipeType());
            handlers.add(handlerReport);

            Path source = resolveLegacyFile(handler.legacyFileName());
            if (source == null) {
                handlerReport.addProperty("status", "missing_template");
                continue;
            }
            foundTemplateCount++;
            handlerReport.addProperty("source", reportPath(source));
            try (Reader reader = Files.newBufferedReader(source)) {
                LegacyGenericRecipeImporter.ImportReport report =
                        LegacyGenericRecipeImporter.readLenientWithReport(handler.legacyFileName(), reader);
                handlerReport.addProperty("status", report.failures().isEmpty() ? "imported" : "imported_with_skips");
                handlerReport.addProperty("output_folder", report.outputFolder().toString());
                handlerReport.addProperty("source_recipe_count", report.sourceRecipeCount());
                handlerReport.addProperty("imported_recipe_count", report.recipes().size());
                handlerReport.addProperty("skipped_recipe_count", report.skippedRecipeCount());
                JsonObject poolCounts = new JsonObject();
                poolCounts.addProperty("alternate", report.poolCount(LegacyBlueprintPools.Kind.ALTERNATE));
                poolCounts.addProperty("discoverable", report.poolCount(LegacyBlueprintPools.Kind.DISCOVERABLE));
                poolCounts.addProperty("secret", report.poolCount(LegacyBlueprintPools.Kind.SECRET));
                poolCounts.addProperty("mode_528", report.poolCount(LegacyBlueprintPools.Kind.MODE_528));
                handlerReport.add("pool_counts", poolCounts);
                JsonArray failures = new JsonArray();
                handlerReport.add("failures", failures);
                for (LegacyGenericRecipeImporter.ImportFailure failure : report.failures()) {
                    JsonObject failureReport = new JsonObject();
                    failureReport.addProperty("source_index", failure.sourceIndex());
                    failureReport.addProperty("internal_name", failure.internalName());
                    failureReport.addProperty("requested_id", failure.requestedId().toString());
                    failureReport.addProperty("message", failure.message());
                    failures.add(failureReport);
                }
                HbmNtm.LOGGER.info("Imported {} {}/{} legacy recipes from {} into modern JSON folder {}. Pools alt/discover/secret/528={}/{}/{}/{} skipped={}",
                        handler.legacyFileName(),
                        report.recipes().size(),
                        report.sourceRecipeCount(),
                        source,
                        report.outputFolder(),
                        report.poolCount(LegacyBlueprintPools.Kind.ALTERNATE),
                        report.poolCount(LegacyBlueprintPools.Kind.DISCOVERABLE),
                        report.poolCount(LegacyBlueprintPools.Kind.SECRET),
                        report.poolCount(LegacyBlueprintPools.Kind.MODE_528),
                        report.skippedRecipeCount());
                for (LegacyGenericRecipeImporter.ImportFailure failure : report.failures()) {
                    HbmNtm.LOGGER.warn("Skipped legacy recipe {} #{} {}: {}",
                            handler.legacyFileName(), failure.sourceIndex(), failure.internalName(), failure.message());
                }
                for (LegacyGenericRecipeImporter.ImportedRecipe recipe : report.recipes()) {
                    JsonObject json = recipe.json();
                    saves.add(DataProvider.saveStable(output, json, recipePathProvider.json(recipe.id())));
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to import legacy recipe file " + source, exception);
            }
        }
        root.addProperty("found_template_count", foundTemplateCount);
        root.addProperty("supported_generic_handler_count", LegacySerializableRecipeHandlers.supportedGeneric().size());
        saves.add(DataProvider.saveStable(output, root, reportPath));
        if (foundTemplateCount == 0) {
            HbmNtm.LOGGER.info("No legacy generic recipe templates found in {}; skipping generic machine recipe import.", legacyRecipeDir);
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy generic machine recipe import";
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

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
