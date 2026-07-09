package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.CompressorRecipe;
import com.hbm.ntm.recipe.ElectrolyserFluidRecipe;
import com.hbm.ntm.recipe.ElectrolyserMetalRecipe;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.LiquefactionRecipe;
import com.hbm.ntm.recipe.MixerRecipe;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.OilProcessingRecipe;
import com.hbm.ntm.recipe.PyroOvenRecipe;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public final class LegacyFluidProcessingRecipeExportProvider implements DataProvider {
    private static final HbmFluidStack EMPTY_FLUID = new HbmFluidStack(HbmFluids.NONE, 0);
    private static final List<LegacyFile> LEGACY_FILES = List.of(
            oil("hbmRefinery.json", "RefineryRecipes", OilProcessingRecipe.Machine.REFINERY),
            oil("hbmVacRefinery.json", "VacuumRefineryRecipes", OilProcessingRecipe.Machine.VACUUM_DISTILL),
            oil("hbmFractions.json", "FractionRecipes", OilProcessingRecipe.Machine.FRACTION_TOWER),
            oil("hbmCracking.json", "CrackingRecipes", OilProcessingRecipe.Machine.CATALYTIC_CRACKER),
            oil("hbmReforming.json", "ReformingRecipes", OilProcessingRecipe.Machine.CATALYTIC_REFORMER),
            oil("hbmHydrotreating.json", "HydrotreatingRecipes", OilProcessingRecipe.Machine.HYDROTREATER),
            new LegacyFile("hbmLiquefactor.json", "LiquefactionRecipes", "liquefaction",
                    Kind.LIQUEFACTION),
            oil("hbmSolidifier.json", "SolidificationRecipes", OilProcessingRecipe.Machine.SOLIDIFIER),
            oil("hbmCoker.json", "CokerRecipes", OilProcessingRecipe.Machine.COKER),
            new LegacyFile("hbmCompressor.json", "CompressorRecipes", "compressor", Kind.COMPRESSOR),
            new LegacyFile("hbmElectrolyzerFluid.json", "ElectrolyserFluidRecipes",
                    "electrolyzer_fluid", Kind.ELECTROLYZER_FLUID),
            new LegacyFile("hbmElectrolyzerMetal.json", "ElectrolyserMetalRecipes",
                    "electrolyzer_metal", Kind.ELECTROLYZER_METAL),
            new LegacyFile("hbmPyrolysis.json", "PyroOvenRecipes", "pyro_oven", Kind.PYRO_OVEN),
            new LegacyFile("hbmMixer.json", "MixerRecipes", "mixer", Kind.MIXER));

    private final Path mainRecipeRoot;
    private final Path exportDir;
    private final Path reportPath;

    public LegacyFluidProcessingRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeRoot = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes");
        this.exportDir = projectRoot.resolve("reports").resolve("legacy_recipe_exports");
        this.reportPath = projectRoot.resolve("reports")
                .resolve("legacy_fluid_processing_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_root", reportPath(mainRecipeRoot));
        root.addProperty("export_dir", reportPath(exportDir));
        root.addProperty("note",
                "Old fluid-processing JSON exports are datapack/debug tooling only; main-resource datapack recipes remain the runtime source of truth.");

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
                HbmNtm.LOGGER.warn("Skipped legacy fluid-processing recipe export {} from {}: {}",
                        failure.recipeId(), failure.source(), failure.message());
            }
            handlers.add(handlerReport);

            if (!report.exportedRecipes().isEmpty()) {
                JsonObject legacyRoot = new JsonObject();
                legacyRoot.add("recipes", legacyRecipeArray(legacyFile, report.exportedRecipes()));
                saves.add(DataProvider.saveStable(output, legacyRoot, exportPath(legacyFile)));
                writtenFileCount++;
                HbmNtm.LOGGER.info("Exported {} legacy fluid-processing recipes to {}",
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
        return "HBM legacy fluid-processing recipe export";
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
                    if (json.has("type") && !id(legacyFile.outputFolder())
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
        int sourceOrder = sourceOrder(json);
        return switch (legacyFile.kind()) {
            case OIL -> {
                OilProcessingRecipe recipe = legacyFile.machine().serializer().fromJson(recipeId, json);
                if (recipe.machine() != legacyFile.machine()) {
                    throw new IllegalArgumentException("Expected " + legacyFile.machine() + " recipe, got "
                            + recipe.machine());
                }
                yield new ExportedRecipe(recipeId, sourceOrder, null, legacyOilJson(recipe));
            }
            case COMPRESSOR -> {
                CompressorRecipe recipe = ModRecipes.COMPRESSOR.serializer().get().fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, sourceOrder, null, legacyCompressorJson(recipe));
            }
            case ELECTROLYZER_FLUID -> {
                ElectrolyserFluidRecipe recipe = ModRecipes.ELECTROLYZER_FLUID.serializer().get()
                        .fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, sourceOrder, null, legacyElectrolyzerFluidJson(recipe));
            }
            case ELECTROLYZER_METAL -> {
                ElectrolyserMetalRecipe recipe = ModRecipes.ELECTROLYZER_METAL.serializer().get()
                        .fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, sourceOrder, null, legacyElectrolyzerMetalJson(recipe));
            }
            case LIQUEFACTION -> {
                LiquefactionRecipe recipe = ModRecipes.LIQUEFACTION.serializer().get().fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, sourceOrder, null, legacyLiquefactionJson(recipe));
            }
            case PYRO_OVEN -> {
                PyroOvenRecipe recipe = ModRecipes.PYRO_OVEN.serializer().get().fromJson(recipeId, json);
                yield new ExportedRecipe(recipeId, sourceOrder, null, legacyPyroOvenJson(recipe));
            }
            case MIXER -> {
                MixerRecipe recipe = ModRecipes.MIXER.serializer().get().fromJson(recipeId, json);
                String outputType = recipe.output().type().getName();
                yield new ExportedRecipe(recipeId, sourceOrder, outputType, legacyMixerSubRecipeJson(recipe));
            }
        };
    }

    private static JsonObject legacyOilJson(OilProcessingRecipe recipe) {
        return switch (recipe.machine()) {
            case REFINERY -> {
                JsonObject object = oilInputString(recipe);
                for (int i = 0; i < 4; i++) {
                    object.add("output" + i, legacyFluid(outputAt(recipe.fluidOutputs(), i)));
                }
                ItemStack solid = recipe.itemOutput();
                if (solid.isEmpty()) {
                    throw new IllegalArgumentException("Legacy hbmRefinery.json requires a solid output");
                }
                object.add("solid", LegacyGenericRecipeFormat.writeLegacyItemStack(solid));
                yield object;
            }
            case VACUUM_DISTILL -> {
                JsonObject object = oilInputString(recipe);
                for (int i = 0; i < 4; i++) {
                    object.add("output" + i, legacyFluid(outputAt(recipe.fluidOutputs(), i)));
                }
                yield object;
            }
            case FRACTION_TOWER, CATALYTIC_CRACKER -> {
                JsonObject object = oilInputString(recipe);
                object.add("output1", legacyFluid(outputAt(recipe.fluidOutputs(), 0)));
                object.add("output2", legacyFluid(outputAt(recipe.fluidOutputs(), 1)));
                yield object;
            }
            case CATALYTIC_REFORMER -> {
                JsonObject object = oilInputString(recipe);
                object.add("output1", legacyFluid(outputAt(recipe.fluidOutputs(), 0)));
                object.add("output2", legacyFluid(outputAt(recipe.fluidOutputs(), 1)));
                object.add("output3", legacyFluid(outputAt(recipe.fluidOutputs(), 2)));
                yield object;
            }
            case HYDROTREATER -> {
                if (recipe.fluidInputs().size() < 2) {
                    throw new IllegalArgumentException("Legacy hbmHydrotreating.json requires hydrogen input");
                }
                JsonObject object = oilInputString(recipe);
                object.add("hydrogen", legacyFluid(recipe.fluidInputs().get(1)));
                object.add("output1", legacyFluid(outputAt(recipe.fluidOutputs(), 0)));
                object.add("output2", legacyFluid(outputAt(recipe.fluidOutputs(), 1)));
                yield object;
            }
            case SOLIDIFIER -> {
                JsonObject object = new JsonObject();
                object.add("input", legacyFluid(recipe.primaryInput()));
                ItemStack output = recipe.itemOutput();
                if (output.isEmpty()) {
                    throw new IllegalArgumentException("Legacy hbmSolidifier.json requires an item output");
                }
                object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(output));
                yield object;
            }
            case COKER -> {
                JsonObject object = new JsonObject();
                object.add("input", legacyFluid(recipe.primaryInput()));
                ItemStack output = recipe.itemOutput();
                if (!output.isEmpty()) {
                    object.add("output", LegacyGenericRecipeFormat.writeLegacyItemStack(output));
                }
                HbmFluidStack byproduct = outputAt(recipe.fluidOutputs(), 0);
                if (!byproduct.isEmpty()) {
                    object.add("byproduct", legacyFluid(byproduct));
                }
                yield object;
            }
        };
    }

    private static JsonObject legacyCompressorJson(CompressorRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", legacyFluid(recipe.input()));
        object.add("output", legacyFluid(recipe.output()));
        return object;
    }

    private static JsonObject legacyElectrolyzerFluidJson(ElectrolyserFluidRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", legacyFluid(recipe.input()));
        object.add("output1", legacyFluid(recipe.output1()));
        object.add("output2", legacyFluid(recipe.output2()));
        if (!recipe.byproducts().isEmpty()) {
            JsonArray byproducts = new JsonArray();
            recipe.byproducts().forEach(stack -> byproducts.add(LegacyGenericRecipeFormat.writeLegacyItemStack(stack)));
            object.add("byproducts", byproducts);
        }
        object.addProperty("duration", recipe.duration());
        return object;
    }

    private static JsonObject legacyElectrolyzerMetalJson(ElectrolyserMetalRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        addMaterialStack(object, "output1", recipe.output1());
        addMaterialStack(object, "output2", recipe.output2());
        if (!recipe.byproducts().isEmpty()) {
            JsonArray byproducts = new JsonArray();
            recipe.byproducts().forEach(stack -> byproducts.add(LegacyGenericRecipeFormat.writeLegacyItemStack(stack)));
            object.add("byproducts", byproducts);
        }
        object.addProperty("duration", recipe.duration());
        return object;
    }

    private static JsonObject legacyLiquefactionJson(LiquefactionRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("input", LegacyGenericRecipeFormat.writeLegacyAStack(recipe.input()));
        object.add("output", legacyFluid(recipe.getOutputFluid()));
        return object;
    }

    private static JsonObject legacyPyroOvenJson(PyroOvenRecipe recipe) {
        JsonObject object = new JsonObject();
        recipe.inputFluid().ifPresent(fluid -> object.add("inputFluid", legacyFluid(fluid)));
        recipe.inputItem().ifPresent(input -> object.add("inputItem",
                LegacyGenericRecipeFormat.writeLegacyAStack(input)));
        recipe.outputFluid().ifPresent(fluid -> object.add("outputFluid", legacyFluid(fluid)));
        recipe.outputItem().ifPresent(output -> object.add("outputItem",
                LegacyGenericRecipeFormat.writeLegacyItemStack(singleOutputStack(output,
                        "Legacy hbmPyrolysis.json cannot represent chance or one_of item outputs"))));
        object.addProperty("duration", recipe.duration());
        return object;
    }

    private static JsonObject legacyMixerSubRecipeJson(MixerRecipe recipe) {
        JsonObject object = new JsonObject();
        object.addProperty("duration", recipe.duration());
        object.addProperty("outputAmount", recipe.output().amount());
        recipe.input1().ifPresent(input -> object.add("input1", legacyFluid(input)));
        recipe.input2().ifPresent(input -> object.add("input2", legacyFluid(input)));
        recipe.solidInput().ifPresent(input -> object.add("solidInput",
                LegacyGenericRecipeFormat.writeLegacyAStack(input)));
        return object;
    }

    private static JsonObject oilInputString(OilProcessingRecipe recipe) {
        JsonObject object = new JsonObject();
        object.addProperty("input", recipe.primaryInput().type().getName());
        return object;
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

    private static void addMaterialStack(JsonObject object, String key, MaterialStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        JsonArray array = new JsonArray();
        array.add(stack.material.names[0]);
        array.add(stack.amount);
        object.add(key, array);
    }

    private static JsonArray legacyRecipeArray(LegacyFile legacyFile, List<ExportedRecipe> exportedRecipes) {
        JsonArray recipeArray = new JsonArray();
        if (legacyFile.kind() == Kind.MIXER) {
            mixerGroups(exportedRecipes).forEach(recipeArray::add);
            return recipeArray;
        }
        exportedRecipes.stream()
                .sorted(legacyOrder())
                .map(ExportedRecipe::legacyJson)
                .forEach(recipeArray::add);
        return recipeArray;
    }

    private static List<JsonObject> mixerGroups(List<ExportedRecipe> exportedRecipes) {
        Map<String, List<ExportedRecipe>> groups = new LinkedHashMap<>();
        exportedRecipes.stream()
                .sorted(legacyOrder())
                .forEach(recipe -> groups.computeIfAbsent(recipe.groupKey(), key -> new ArrayList<>())
                        .add(recipe));

        return groups.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, List<ExportedRecipe>>>comparingInt(entry -> entry.getValue()
                        .stream()
                        .mapToInt(ExportedRecipe::sourceOrder)
                        .min()
                        .orElse(Integer.MAX_VALUE))
                        .thenComparing(Map.Entry::getKey))
                .map(entry -> {
                    JsonObject object = new JsonObject();
                    object.addProperty("outputType", entry.getKey());
                    JsonArray recipes = new JsonArray();
                    entry.getValue().stream()
                            .sorted(legacyOrder())
                            .map(ExportedRecipe::legacyJson)
                            .forEach(recipes::add);
                    object.add("recipes", recipes);
                    return object;
                })
                .toList();
    }

    private static Comparator<ExportedRecipe> legacyOrder() {
        return Comparator.comparingInt(ExportedRecipe::sourceOrder)
                .thenComparing(recipe -> recipe.recipeId().toString());
    }

    private static HbmFluidStack outputAt(List<HbmFluidStack> outputs, int index) {
        if (index < outputs.size()) {
            return outputs.get(index);
        }
        return EMPTY_FLUID;
    }

    private static JsonArray legacyFluid(HbmFluidStack stack) {
        return LegacyGenericRecipeFormat.writeLegacyFluidStack(stack == null ? EMPTY_FLUID : stack);
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

    private static ResourceLocation id(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static LegacyFile oil(String fileName, String legacyClassName, OilProcessingRecipe.Machine machine) {
        return new LegacyFile(fileName, legacyClassName, machine.typeName(), Kind.OIL, machine);
    }

    private record LegacyFile(String fileName, String legacyClassName, String outputFolder, Kind kind,
                              OilProcessingRecipe.Machine machine) {
        private LegacyFile(String fileName, String legacyClassName, String outputFolder, Kind kind) {
            this(fileName, legacyClassName, outputFolder, kind, null);
        }
    }

    private enum Kind {
        OIL,
        COMPRESSOR,
        ELECTROLYZER_FLUID,
        ELECTROLYZER_METAL,
        LIQUEFACTION,
        PYRO_OVEN,
        MIXER
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures) {
    }

    private record ExportedRecipe(ResourceLocation recipeId, int sourceOrder, String groupKey,
                                  JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
