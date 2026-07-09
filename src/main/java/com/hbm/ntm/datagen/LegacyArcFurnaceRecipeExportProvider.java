package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.GenericMachineRecipe;
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

public final class LegacyArcFurnaceRecipeExportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmArcFurnace.json";
    private static final String OUTPUT_FOLDER = "arc_furnace";
    private static final int LEGACY_DURATION = 400;
    private static final long LEGACY_POWER = 1_000L;

    private final Path mainRecipeDir;
    private final Path exportPath;
    private final Path reportPath;

    public LegacyArcFurnaceRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeDir = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes")
                .resolve(OUTPUT_FOLDER);
        this.exportPath = projectRoot.resolve("reports").resolve("legacy_recipe_exports").resolve(LEGACY_FILE);
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_arc_furnace_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_dir", reportPath(mainRecipeDir));
        root.addProperty("export_file", reportPath(exportPath));
        root.addProperty("legacy_file", LEGACY_FILE);
        root.addProperty("modern_recipe_type", OUTPUT_FOLDER);

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
            HbmNtm.LOGGER.warn("Skipped legacy arc furnace recipe export {} from {}: {}",
                    failure.recipeId(), failure.source(), failure.message());
        }

        if (!report.exportedRecipes().isEmpty()) {
            JsonObject legacyRoot = new JsonObject();
            JsonArray recipeArray = new JsonArray();
            legacyRoot.add("recipes", recipeArray);
            report.exportedRecipes().stream()
                    .sorted(Comparator.comparing(ExportedRecipe::recipe, GenericMachineRecipe.LEGACY_ORDER))
                    .map(ExportedRecipe::legacyJson)
                    .forEach(recipeArray::add);
            saves.add(DataProvider.saveStable(output, legacyRoot, exportPath));
            HbmNtm.LOGGER.info("Exported {} legacy arc furnace recipes to {}",
                    report.exportedRecipes().size(), exportPath);
        }

        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy arc furnace recipe export";
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
                    if (json.has("type") && !ModRecipes.ARC_FURNACE.serializer().getId()
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    GenericMachineRecipe recipe = ModRecipes.ARC_FURNACE.serializer().get()
                            .fromJson(recipeId, json);
                    if (recipe.getMachine() != GenericMachineRecipe.Machine.ARC_FURNACE) {
                        throw new IllegalArgumentException("Expected ARC_FURNACE recipe, got "
                                + recipe.getMachine());
                    }
                    exported.add(new ExportedRecipe(recipe, legacyJson(recipe)));
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

    private static JsonObject legacyJson(GenericMachineRecipe recipe) {
        if (recipe.getDuration() != LEGACY_DURATION || recipe.getPower() != LEGACY_POWER) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json cannot represent duration/power overrides");
        }
        if (recipe.getItemInputs().size() != 1) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json requires exactly one item input");
        }
        if (!recipe.getFluidInputs().isEmpty() || !recipe.getFluidOutputs().isEmpty()) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json cannot represent fluid inputs or outputs");
        }
        if (recipe.getItemOutputEntries().size() > 1) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json cannot represent multiple item outputs");
        }
        if (!recipe.getPools().isEmpty() || recipe.hasCustomLocalization()
                || recipe.getAutoSwitchGroup() != null || recipe.getNameWrapper() != null) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json cannot represent generic recipe metadata");
        }

        List<MaterialStack> materialOutputs = recipe.getExtraData().arcMaterialOutputs();
        if (recipe.getItemOutputEntries().isEmpty() && materialOutputs.isEmpty()) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json requires a solid or material output");
        }

        JsonObject object = new JsonObject();
        HbmIngredient input = recipe.getItemInputs().get(0);
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(input));
        if (!recipe.getItemOutputEntries().isEmpty()) {
            object.add("solid", LegacyGenericRecipeFormat.writeLegacyItemStack(
                    singleOutputStack(recipe.getItemOutputEntries().get(0))));
        }
        if (!materialOutputs.isEmpty()) {
            object.add("fluid", legacyMaterialStacks(materialOutputs));
        }
        return object;
    }

    private static ItemStack singleOutputStack(HbmItemOutput output) {
        if (output.oneOf() || output.entries().size() != 1) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json cannot represent one_of outputs");
        }
        HbmItemOutput.Entry entry = output.entries().get(0);
        if (entry.chance() < 1.0F || entry.weight() > 0) {
            throw new IllegalArgumentException("Legacy hbmArcFurnace.json cannot represent chance outputs");
        }
        return entry.stack();
    }

    private static JsonArray legacyMaterialStacks(List<MaterialStack> outputs) {
        JsonArray array = new JsonArray();
        for (MaterialStack output : outputs) {
            JsonArray material = new JsonArray();
            material.add(output.material.names[0]);
            material.add(output.amount);
            array.add(material);
        }
        return array;
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

    private record ExportedRecipe(GenericMachineRecipe recipe, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
