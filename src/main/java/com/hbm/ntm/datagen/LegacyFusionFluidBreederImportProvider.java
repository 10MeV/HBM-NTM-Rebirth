package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.ModRecipes;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LegacyFusionFluidBreederImportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmIrradiationFluids.json";

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;
    private final Path mainRecipeDir;

    public LegacyFusionFluidBreederImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_fusion_fluid_breeder_import_report.json");
        this.legacyRecipeDir = projectRoot.resolve("legacy_recipes");
        this.mainRecipeDir = projectRoot.resolve("src").resolve("main").resolve("resources")
                .resolve("data").resolve(HbmNtm.MOD_ID).resolve("recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject report = new JsonObject();
        report.addProperty("legacy_recipe_dir", reportPath(legacyRecipeDir));
        report.addProperty("legacy_file", LEGACY_FILE);
        report.addProperty("modern_recipe_type", "fusion_fluid_breeder");

        Path source = resolveLegacyFile();
        if (source == null) {
            Path recipeDir = mainRecipeDir.resolve("fusion_fluid_breeder");
            int count = countJsonFiles(recipeDir);
            report.addProperty("status", count > 0 ? "main_resources_only" : "missing_template");
            report.addProperty("external_template_status", "missing");
            report.addProperty("main_resource_recipe_dir", reportPath(recipeDir));
            report.addProperty("main_resource_recipe_count", count);
            saves.add(DataProvider.saveStable(output, report, reportPath));
            HbmNtm.LOGGER.info("No legacy fusion fluid breeder template found in {}; skipping import.",
                    legacyRecipeDir);
            return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
        }

        report.addProperty("source", reportPath(source));
        JsonArray failures = new JsonArray();
        JsonArray imported = new JsonArray();
        report.add("failures", failures);
        report.add("imported", imported);

        int sourceRecipeCount = 0;
        try (Reader reader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Missing recipes array");
            }
            sourceRecipeCount = recipes.size();
            for (int i = 0; i < recipes.size(); i++) {
                try {
                    ImportedRecipe recipe = readRecipe(i, recipes.get(i));
                    JsonObject importedRecipe = new JsonObject();
                    importedRecipe.addProperty("source_index", i);
                    importedRecipe.addProperty("id", recipe.id().toString());
                    importedRecipe.addProperty("input", recipe.input().type().getName());
                    importedRecipe.addProperty("output", recipe.output().type().getName());
                    imported.add(importedRecipe);
                    saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
                } catch (RuntimeException exception) {
                    JsonObject failure = new JsonObject();
                    failure.addProperty("source_index", i);
                    failure.addProperty("message", exception.getMessage());
                    failures.add(failure);
                    HbmNtm.LOGGER.warn("Skipped legacy fusion fluid breeder recipe {} #{}: {}",
                            LEGACY_FILE, i, exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy fusion fluid breeder recipe file " + source,
                    exception);
        }

        report.addProperty("status", failures.isEmpty() ? "imported" : "imported_with_skips");
        report.addProperty("source_recipe_count", sourceRecipeCount);
        report.addProperty("imported_recipe_count", imported.size());
        report.addProperty("skipped_recipe_count", failures.size());
        HbmNtm.LOGGER.info("Imported fusion fluid breeder recipes {}/{} from {} skipped={}",
                imported.size(), sourceRecipeCount, source, failures.size());
        saves.add(DataProvider.saveStable(output, report, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy fusion fluid breeder recipe import";
    }

    private ImportedRecipe readRecipe(int sourceIndex, JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        HbmFluidStack input = readLegacyFluidStack(object.getAsJsonArray("input"), "input", sourceIndex);
        HbmFluidStack output = readLegacyFluidStack(object.getAsJsonArray("output"), "output", sourceIndex);
        if (input.isEmpty() || output.isEmpty()) {
            throw new JsonSyntaxException("input and output must be non-empty");
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", ModRecipes.FUSION_FLUID_BREEDER.serializer().getId().toString());
        json.add("input", writeModernFluidStack(input));
        json.add("output", writeModernFluidStack(output));
        return new ImportedRecipe(new ResourceLocation(HbmNtm.MOD_ID,
                "fusion_fluid_breeder/" + input.type().toPath() + "_to_" + output.type().toPath()),
                input, output, json);
    }

    private static HbmFluidStack readLegacyFluidStack(JsonArray array, String key, int sourceIndex) {
        if (array == null || array.size() < 2) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " missing " + key + " fluid stack");
        }
        FluidType type = HbmFluidJsonUtil.requireFluidReference(array.get(0),
                "fusion fluid breeder " + key + " recipe #" + sourceIndex);
        int amount = array.get(1).getAsInt();
        int pressure = array.size() < 3 ? 0 : array.get(2).getAsInt();
        return new HbmFluidStack(type, amount, pressure);
    }

    private static JsonObject writeModernFluidStack(HbmFluidStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", new ResourceLocation(HbmNtm.MOD_ID, stack.type().toPath()).toString());
        object.addProperty("amount", stack.amount());
        if (stack.pressure() != 0) {
            object.addProperty("pressure", stack.pressure());
        }
        return object;
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

    private static int countJsonFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            return 0;
        }
        try (var files = Files.walk(dir)) {
            return (int) files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .count();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to count main-resource recipes in " + dir, exception);
        }
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record ImportedRecipe(ResourceLocation id, HbmFluidStack input, HbmFluidStack output, JsonObject json) {
    }
}
