package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.OilProcessingRecipe;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class LegacyFluidProcessingRecipeImportProvider implements DataProvider {
    private static final List<LegacyFile> LEGACY_FILES = List.of(
            oil("hbmRefinery.json", "RefineryRecipes", OilProcessingRecipe.Machine.REFINERY),
            oil("hbmVacRefinery.json", "VacuumRefineryRecipes", OilProcessingRecipe.Machine.VACUUM_DISTILL),
            oil("hbmFractions.json", "FractionRecipes", OilProcessingRecipe.Machine.FRACTION_TOWER),
            oil("hbmCracking.json", "CrackingRecipes", OilProcessingRecipe.Machine.CATALYTIC_CRACKER),
            oil("hbmReforming.json", "ReformingRecipes", OilProcessingRecipe.Machine.CATALYTIC_REFORMER),
            oil("hbmHydrotreating.json", "HydrotreatingRecipes", OilProcessingRecipe.Machine.HYDROTREATER),
            new LegacyFile("hbmLiquefactor.json", "LiquefactionRecipes", "liquefaction", Kind.LIQUEFACTION),
            oil("hbmSolidifier.json", "SolidificationRecipes", OilProcessingRecipe.Machine.SOLIDIFIER),
            oil("hbmCoker.json", "CokerRecipes", OilProcessingRecipe.Machine.COKER),
            new LegacyFile("hbmCompressor.json", "CompressorRecipes", "compressor", Kind.COMPRESSOR),
            new LegacyFile("hbmElectrolyzerFluid.json", "ElectrolyserFluidRecipes", "electrolyzer_fluid",
                    Kind.ELECTROLYZER_FLUID),
            new LegacyFile("hbmElectrolyzerMetal.json", "ElectrolyserMetalRecipes", "electrolyzer_metal",
                    Kind.ELECTROLYZER_METAL),
            new LegacyFile("hbmPyrolysis.json", "PyroOvenRecipes", "pyro_oven", Kind.PYRO_OVEN),
            new LegacyFile("hbmMixer.json", "MixerRecipes", "mixer", Kind.MIXER));

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;

    public LegacyFluidProcessingRecipeImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_fluid_processing_recipe_import_report.json");
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
                HbmNtm.LOGGER.warn("Skipped legacy fluid recipe {} #{}: {}",
                        legacyFile.fileName(), failure.sourceIndex(), failure.message());
            }
            for (ImportedRecipe recipe : report.imported()) {
                saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
            }
            HbmNtm.LOGGER.info("Imported {} {}/{} legacy fluid recipes from {} into {}",
                    legacyFile.fileName(), report.imported().size(), report.sourceRecipeCount(),
                    source, legacyFile.outputFolder());
        }
        root.addProperty("found_template_count", foundTemplateCount);
        root.addProperty("imported_recipe_count", importedRecipeCount);
        saves.add(DataProvider.saveStable(output, root, reportPath));
        if (foundTemplateCount == 0) {
            HbmNtm.LOGGER.info("No legacy fluid-processing recipe templates found in {}; skipping import.",
                    legacyRecipeDir);
        }
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy fluid-processing recipe import";
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
                JsonObject recipe = element.getAsJsonObject();
                try {
                    List<PendingRecipe> pendingRecipes = recipeJsons(legacyFile, recipe, sourceIndex);
                    for (PendingRecipe pending : pendingRecipes) {
                        ResourceLocation id = uniqueId(id(legacyFile.outputFolder() + "/" + pending.name()), usedIds);
                        JsonObject modern = pending.json();
                        if (!modern.has("source_order")) {
                            modern.addProperty("source_order", sourceIndex);
                        }
                        imported.add(new ImportedRecipe(id, modern));
                    }
                } catch (RuntimeException exception) {
                    failures.add(new ImportFailure(sourceIndex, importedId(legacyFile, recipe, sourceIndex),
                            exception.getMessage()));
                }
            }
            return new ImportReport(recipes.size(), List.copyOf(imported), List.copyOf(failures));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy fluid-processing recipe file " + source, exception);
        }
    }

    private static List<PendingRecipe> recipeJsons(LegacyFile legacyFile, JsonObject legacy, int sourceIndex) {
        return switch (legacyFile.kind()) {
            case COMPRESSOR -> List.of(new PendingRecipe(recipeName(legacyFile, legacy, sourceIndex),
                    compressorJson(legacy)));
            case ELECTROLYZER_FLUID -> List.of(new PendingRecipe(recipeName(legacyFile, legacy, sourceIndex),
                    electrolyzerFluidJson(legacy)));
            case ELECTROLYZER_METAL -> List.of(new PendingRecipe(recipeName(legacyFile, legacy, sourceIndex),
                    electrolyzerMetalJson(legacy)));
            case LIQUEFACTION -> List.of(new PendingRecipe(recipeName(legacyFile, legacy, sourceIndex),
                    liquefactionJson(legacy)));
            case PYRO_OVEN -> List.of(new PendingRecipe(recipeName(legacyFile, legacy, sourceIndex),
                    pyroOvenJson(legacy)));
            case MIXER -> mixerJsons(legacy, sourceIndex);
            case OIL -> List.of(new PendingRecipe(recipeName(legacyFile, legacy, sourceIndex),
                    oilJson(legacyFile, legacy)));
        };
    }

    private static JsonObject oilJson(LegacyFile legacyFile, JsonObject legacy) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", id(legacyFile.outputFolder()).toString());
        if (legacy.has("input")) {
            modern.add("input", inputElement(legacy.get("input")));
        }
        copyFluid(legacy, modern, "hydrogen");
        for (int i = 0; i < legacyFile.optionalMachine().orElseThrow().maxFluidOutputs(); i++) {
            copyFluid(legacy, modern, "output" + i);
            copyFluid(legacy, modern, "output" + (i + 1));
        }
        copyFluid(legacy, modern, "byproduct");
        if (legacy.has("solid")) {
            modern.add("solid", itemStackJson(LegacyGenericRecipeFormat.readLegacyRecipeItemStack(
                    legacy.getAsJsonArray("solid"))));
        }
        if (legacy.has("output") && legacyFile.kind() != Kind.COMPRESSOR) {
            modern.add("output", itemStackJson(LegacyGenericRecipeFormat.readLegacyRecipeItemStack(
                    legacy.getAsJsonArray("output"))));
        }
        return modern;
    }

    private static JsonObject compressorJson(JsonObject legacy) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", id("compressor").toString());
        modern.add("input", fluidStackJson(legacy.getAsJsonArray("input")));
        modern.add("output", fluidStackJson(legacy.getAsJsonArray("output")));
        modern.addProperty("duration", legacy.has("duration") ? legacy.get("duration").getAsInt() : 100);
        return modern;
    }

    private static JsonObject electrolyzerFluidJson(JsonObject legacy) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", id("electrolyzer_fluid").toString());
        modern.add("input", fluidStackJson(legacy.getAsJsonArray("input")));
        modern.add("output1", fluidStackJson(legacy.getAsJsonArray("output1")));
        modern.add("output2", fluidStackJson(legacy.getAsJsonArray("output2")));
        if (legacy.has("byproducts")) {
            JsonArray byproducts = new JsonArray();
            JsonArray legacyByproducts = legacy.getAsJsonArray("byproducts");
            for (JsonElement element : legacyByproducts) {
                byproducts.add(itemStackJson(LegacyGenericRecipeFormat.readLegacyRecipeItemStack(
                        element.getAsJsonArray())));
            }
            modern.add("byproducts", byproducts);
        }
        if (legacy.has("duration")) {
            modern.add("duration", legacy.get("duration").deepCopy());
        } else if (legacy.has("duraion")) {
            modern.add("duration", legacy.get("duraion").deepCopy());
        } else {
            modern.addProperty("duration", 20);
        }
        return modern;
    }

    private static JsonObject electrolyzerMetalJson(JsonObject legacy) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", id("electrolyzer_metal").toString());
        JsonArray input = legacy.getAsJsonArray("input");
        if (isExcludedBedrockOreElectrolyzerInput(input)) {
            throw new JsonSyntaxException("legacy bedrock ore electrolyzer loop is excluded by modernization rule");
        }
        modern.add("input", readLegacySingularAStack(input).toJson());
        if (legacy.has("output1")) {
            modern.add("output1", materialStackJson(legacy.getAsJsonArray("output1"), "output1"));
        }
        if (legacy.has("output2")) {
            modern.add("output2", materialStackJson(legacy.getAsJsonArray("output2"), "output2"));
        }
        if (legacy.has("byproducts")) {
            JsonArray byproducts = new JsonArray();
            JsonArray legacyByproducts = legacy.getAsJsonArray("byproducts");
            for (JsonElement element : legacyByproducts) {
                byproducts.add(itemStackJson(LegacyGenericRecipeFormat.readLegacyRecipeItemStack(
                        element.getAsJsonArray())));
            }
            modern.add("byproducts", byproducts);
        }
        modern.addProperty("duration", legacy.has("duration") ? legacy.get("duration").getAsInt() : 600);
        return modern;
    }

    private static JsonObject liquefactionJson(JsonObject legacy) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", id("liquefaction").toString());
        modern.add("input", readLegacySingularAStack(legacy.getAsJsonArray("input")).toJson());
        modern.add("output", fluidStackJson(legacy.getAsJsonArray("output")));
        return modern;
    }

    private static JsonObject pyroOvenJson(JsonObject legacy) {
        JsonObject modern = new JsonObject();
        modern.addProperty("type", id("pyro_oven").toString());
        if (legacy.has("inputItem")) {
            modern.add("input_item", LegacyGenericRecipeFormat.readLegacyRecipeAStack(
                    legacy.getAsJsonArray("inputItem")).toJson());
        }
        if (legacy.has("inputFluid")) {
            modern.add("input_fluid", fluidStackJson(legacy.getAsJsonArray("inputFluid")));
        }
        if (legacy.has("outputItem")) {
            modern.add("output_item", HbmItemOutput.of(LegacyGenericRecipeFormat.readLegacyRecipeItemStack(
                    legacy.getAsJsonArray("outputItem"))).toJson());
        }
        if (legacy.has("outputFluid")) {
            modern.add("output_fluid", fluidStackJson(legacy.getAsJsonArray("outputFluid")));
        }
        modern.addProperty("duration", legacy.get("duration").getAsInt());
        return modern;
    }

    private static List<PendingRecipe> mixerJsons(JsonObject legacy, int sourceIndex) {
        String outputType = legacy.get("outputType").getAsString();
        JsonArray recipes = legacy.getAsJsonArray("recipes");
        List<PendingRecipe> pending = new ArrayList<>();
        for (int index = 0; index < recipes.size(); index++) {
            JsonObject subRecipe = recipes.get(index).getAsJsonObject();
            JsonObject modern = new JsonObject();
            modern.addProperty("type", id("mixer").toString());
            JsonObject output = new JsonObject();
            output.addProperty("fluid", outputType);
            output.add("amount", subRecipe.get("outputAmount").deepCopy());
            modern.add("output", output);
            copyFluid(subRecipe, modern, "input1");
            copyFluid(subRecipe, modern, "input2");
            if (subRecipe.has("solidInput")) {
                modern.add("solid_input", LegacyGenericRecipeFormat.readLegacyRecipeAStack(
                        subRecipe.getAsJsonArray("solidInput")).toJson());
            }
            modern.add("duration", subRecipe.get("duration").deepCopy());
            modern.addProperty("source_order", index);
            pending.add(new PendingRecipe(sanitizeName(outputType) + "_" + sourceIndex + "_" + index, modern));
        }
        return List.copyOf(pending);
    }

    private static void copyFluid(JsonObject source, JsonObject target, String key) {
        if (source.has(key)) {
            target.add(key, fluidStackElement(source.get(key)));
        }
    }

    private static JsonElement inputElement(JsonElement element) {
        if (element.isJsonArray()) {
            return fluidStackJson(element.getAsJsonArray());
        }
        return element.deepCopy();
    }

    private static JsonElement fluidStackElement(JsonElement element) {
        if (element.isJsonArray()) {
            return fluidStackJson(element.getAsJsonArray());
        }
        return element.deepCopy();
    }

    private static JsonObject fluidStackJson(JsonArray legacy) {
        if (legacy.size() < 2) {
            throw new JsonSyntaxException("Legacy fluid stack needs fluid and amount: " + legacy);
        }
        JsonObject object = new JsonObject();
        object.add("fluid", legacy.get(0).deepCopy());
        object.add("amount", legacy.get(1).deepCopy());
        if (legacy.size() > 2) {
            object.add("pressure", legacy.get(2).deepCopy());
        }
        return object;
    }

    private static JsonObject itemStackJson(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new JsonSyntaxException("Legacy item stack resolved to empty");
        }
        JsonObject object = new JsonObject();
        object.addProperty("item", HbmRegistryUtil.itemKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            object.addProperty("count", stack.getCount());
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && !tag.isEmpty()) {
            object.addProperty("nbt", tag.toString());
        }
        return object;
    }

    private static JsonObject materialStackJson(JsonArray legacy, String name) {
        if (legacy.size() < 2) {
            throw new JsonSyntaxException("Legacy material stack for " + name + " needs material and amount: "
                    + legacy);
        }
        JsonObject object = new JsonObject();
        object.add("material", legacy.get(0).deepCopy());
        int amount = legacy.get(1).getAsInt();
        if (amount <= 0) {
            throw new JsonSyntaxException("Invalid legacy material amount " + amount + " in " + name);
        }
        object.addProperty("amount", amount);
        return object;
    }

    private static HbmIngredient readLegacySingularAStack(JsonArray legacy) {
        HbmIngredient ingredient = LegacyGenericRecipeFormat.readLegacyRecipeAStack(legacy);
        ItemStack exactStack = ingredient.exactStack();
        if (!exactStack.isEmpty()) {
            exactStack.setCount(1);
        }
        return new HbmIngredient(ingredient.ingredient(), 1, exactStack, ingredient.partialNbt(),
                ingredient.legacyId(), ingredient.legacyMeta(), ingredient.legacyWildcard(),
                ingredient.legacyOreName(), ingredient.fluidContainerType(), ingredient.fluidContainerAmount());
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

    private static ResourceLocation importedId(LegacyFile legacyFile, JsonObject recipe, int sourceIndex) {
        String base = legacyFile.outputFolder() + "/" + recipeName(recipe, sourceIndex);
        return id(base);
    }

    private static String recipeName(JsonObject recipe, int sourceIndex) {
        return recipeName(null, recipe, sourceIndex);
    }

    private static String recipeName(LegacyFile legacyFile, JsonObject recipe, int sourceIndex) {
        if (legacyFile != null
                && (legacyFile.kind() == Kind.LIQUEFACTION || legacyFile.kind() == Kind.ELECTROLYZER_METAL)
                && recipe.has("input")) {
            return sanitizeName("input_" + legacyAStackName(recipe.getAsJsonArray("input")));
        }
        if (legacyFile != null && legacyFile.kind() == Kind.PYRO_OVEN) {
            if (recipe.has("inputFluid")) {
                return sanitizeName("fluid_" + recipe.getAsJsonArray("inputFluid").get(0).getAsString());
            }
            if (recipe.has("inputItem")) {
                return sanitizeName("item_" + legacyAStackName(recipe.getAsJsonArray("inputItem")));
            }
        }
        if (!recipe.has("input")) {
            return "legacy_import_" + sourceIndex;
        }
        JsonElement input = recipe.get("input");
        String fluid = input.isJsonArray() ? input.getAsJsonArray().get(0).getAsString() : input.getAsString();
        String name = fluid.toLowerCase(Locale.ROOT)
                .replace(':', '/')
                .replaceAll("[^a-z0-9_./-]", "_")
                .replaceAll("/+", "/");
        if (input.isJsonArray() && input.getAsJsonArray().size() > 2) {
            name += "_p" + input.getAsJsonArray().get(2).getAsInt();
        }
        return name.isBlank() ? "legacy_import_" + sourceIndex : name;
    }

    private static boolean isExcludedBedrockOreElectrolyzerInput(JsonArray input) {
        String raw = input.toString().toLowerCase(Locale.ROOT);
        return raw.contains("bedrockore")
                || raw.contains("bedrock_ore")
                || raw.contains("ore_bedrock")
                || raw.contains("itembedrockore");
    }

    private static String legacyAStackName(JsonArray array) {
        if (array.size() < 2) {
            return "legacy_stack";
        }
        return array.get(0).getAsString() + "_" + array.get(1).getAsString();
    }

    private static String sanitizeName(String raw) {
        String name = raw.toLowerCase(Locale.ROOT)
                .replace(':', '/')
                .replaceAll("[^a-z0-9_./-]", "_")
                .replaceAll("/+", "/");
        return name.isBlank() ? "legacy_import" : name;
    }

    private static ResourceLocation uniqueId(ResourceLocation baseId, Map<ResourceLocation, Integer> usedIds) {
        Integer previous = usedIds.putIfAbsent(baseId, 1);
        if (previous == null) {
            return baseId;
        }
        int next = previous + 1;
        ResourceLocation candidate;
        do {
            candidate = new ResourceLocation(baseId.getNamespace(), baseId.getPath() + "_" + next++);
        } while (usedIds.containsKey(candidate));
        usedIds.put(candidate, 1);
        return candidate;
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

        private java.util.Optional<OilProcessingRecipe.Machine> optionalMachine() {
            return java.util.Optional.ofNullable(machine);
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

    private record PendingRecipe(String name, JsonObject json) {
    }

    private record ImportedRecipe(ResourceLocation id, JsonObject json) {
    }

    private record ImportReport(int sourceRecipeCount, List<ImportedRecipe> imported, List<ImportFailure> failures) {
    }

    private record ImportFailure(int sourceIndex, ResourceLocation requestedId, String message) {
    }
}
