package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.CombinationOvenRecipe;
import com.hbm.ntm.recipe.CyclotronRecipe;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class LegacySpecialMachineRecipeExportProvider implements DataProvider {
    private static final List<LegacyFile> LEGACY_FILES = List.of(
            new LegacyFile("hbmCombination.json", "CombinationRecipes", "combination_oven", Kind.COMBINATION),
            new LegacyFile("hbmCrystallizer.json", "CrystallizerRecipes", "crystallizer", Kind.CRYSTALLIZER),
            new LegacyFile("hbmCyclotron.json", "CyclotronRecipes", "cyclotron", Kind.CYCLOTRON));

    private static final Comparator<ExportedRecipe> LEGACY_ORDER = Comparator
            .comparingInt(ExportedRecipe::sourceOrder)
            .thenComparing(recipe -> recipe.recipeId().toString());

    private final Path mainRecipeRoot;
    private final Path exportDir;
    private final Path reportPath;

    public LegacySpecialMachineRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeRoot = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes");
        this.exportDir = projectRoot.resolve("reports").resolve("legacy_recipe_exports");
        this.reportPath = projectRoot.resolve("reports")
                .resolve("legacy_special_machine_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_root", reportPath(mainRecipeRoot));
        root.addProperty("export_dir", reportPath(exportDir));
        root.addProperty("note",
                "Old special-machine JSON exports are datapack/debug tooling only; main-resource datapack recipes remain the runtime source of truth.");

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
                HbmNtm.LOGGER.warn("Skipped legacy special-machine recipe export {} from {}: {}",
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
                HbmNtm.LOGGER.info("Exported {} legacy special-machine recipes to {}",
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
        return "HBM legacy special-machine recipe export";
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

    private static ExportedRecipe exportedRecipe(LegacyFile legacyFile, ResourceLocation recipeId,
            JsonObject json) {
        int rawSourceOrder = sourceOrder(json);
        return switch (legacyFile.kind()) {
            case COMBINATION -> {
                CombinationOvenRecipe recipe = ModRecipes.COMBINATION_OVEN.serializer().get()
                        .fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, rawSourceOrder, legacyCombinationJson(recipe));
            }
            case CRYSTALLIZER -> {
                ItemProcessingRecipe recipe = ModRecipes.CRYSTALLIZER.serializer().get()
                        .fromJson(recipeId, json);
                if (recipe.machine() != ItemProcessingRecipe.Machine.CRYSTALLIZER) {
                    throw new IllegalArgumentException("Expected CRYSTALLIZER recipe, got " + recipe.machine());
                }
                yield new ExportedRecipe(recipeId, recipe.sourceOrder(), legacyCrystallizerJson(recipe));
            }
            case CYCLOTRON -> {
                CyclotronRecipe recipe = ModRecipes.CYCLOTRON.serializer().get().fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, recipe.sourceOrder(), legacyCyclotronJson(recipe));
            }
        };
    }

    private static JsonObject legacyCombinationJson(CombinationOvenRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        recipe.outputItem().ifPresent(output -> object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(
                singleOutputStack(output, "Legacy hbmCombination.json cannot represent chance or one_of item outputs"))));
        recipe.outputFluid().ifPresent(fluid -> object.add("fluid",
                LegacyGenericRecipeFormat.writeLegacyFluidStack(fluid)));
        return object;
    }

    private static JsonObject legacyCrystallizerJson(ItemProcessingRecipe recipe) {
        JsonObject object = new JsonObject();
        object.addProperty("duration", recipe.duration());
        object.add("fluid", LegacyGenericRecipeFormat.writeLegacyFluidStack(recipe.fluidInput()
                .orElseThrow(() -> new IllegalArgumentException("Legacy hbmCrystallizer.json requires a fluid input"))));
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(
                singleOutputStack(singleOutput(recipe),
                        "Legacy hbmCrystallizer.json cannot represent chance, one_of, or multiple item outputs")));
        object.addProperty("productivity", recipe.productivity());
        return object;
    }

    private static JsonObject legacyCyclotronJson(CyclotronRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("particle", LegacyGenericRecipeFormat.writeLegacyItemStack(legacyParticleStack(recipe.particle())));
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(recipe.output()));
        object.addProperty("antimatter", recipe.antimatterMb());
        return object;
    }

    private static HbmItemOutput singleOutput(ItemProcessingRecipe recipe) {
        if (recipe.outputs().size() != 1) {
            throw new IllegalArgumentException("Legacy hbmCrystallizer.json requires exactly one item output");
        }
        return recipe.outputs().get(0);
    }

    private static ItemStack singleOutputStack(HbmItemOutput output, String unsupportedMessage) {
        if (output.oneOf() || output.entries().size() != 1) {
            throw new IllegalArgumentException(unsupportedMessage);
        }
        HbmItemOutput.Entry entry = output.entries().get(0);
        if (entry.chance() < 1.0F || entry.weight() > 0) {
            throw new IllegalArgumentException(unsupportedMessage);
        }
        return entry.stack();
    }

    private static ItemStack legacyParticleStack(HbmIngredient particle) {
        if (particle.legacyOreName() != null || particle.isTagIngredient() || particle.fluidContainerType() != null) {
            throw new IllegalArgumentException("Legacy hbmCyclotron.json particle must be a concrete item stack: "
                    + particle.diagnosticName());
        }
        List<ItemStack> stacks = particle.displayStacks();
        if (stacks.size() != 1 || stacks.get(0).isEmpty()) {
            throw new IllegalArgumentException("Legacy hbmCyclotron.json particle must resolve to one item stack: "
                    + particle.diagnosticName());
        }
        return stacks.get(0);
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

    private static int sourceOrder(JsonObject json) {
        return GsonHelper.getAsInt(json, "source_order",
                GsonHelper.getAsInt(json, "legacy_order", Integer.MAX_VALUE));
    }

    private static ResourceLocation serializerId(LegacyFile legacyFile) {
        return switch (legacyFile.kind()) {
            case COMBINATION -> ModRecipes.COMBINATION_OVEN.serializer().getId();
            case CRYSTALLIZER -> ModRecipes.CRYSTALLIZER.serializer().getId();
            case CYCLOTRON -> ModRecipes.CYCLOTRON.serializer().getId();
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
        COMBINATION,
        CRYSTALLIZER,
        CYCLOTRON
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures) {
    }

    private record ExportedRecipe(ResourceLocation recipeId, int sourceOrder, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
