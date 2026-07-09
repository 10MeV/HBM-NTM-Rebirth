package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.LegacyGenericRecipeHandlers;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LegacyGenericRecipeExportProvider implements DataProvider {
    private final Path mainRecipeRoot;
    private final Path exportDir;
    private final Path reportPath;

    public LegacyGenericRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeRoot = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes");
        this.exportDir = projectRoot.resolve("reports").resolve("legacy_recipe_exports");
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_generic_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("main_recipe_root", reportPath(mainRecipeRoot));
        root.addProperty("export_dir", reportPath(exportDir));
        JsonArray handlers = new JsonArray();
        root.add("handlers", handlers);

        int exportedFileCount = 0;
        int exportedRecipeCount = 0;
        int skippedRecipeCount = 0;
        int omittedConditionRecipeCount = 0;
        for (LegacyGenericRecipeHandlers.Handler handler : LegacyGenericRecipeHandlers.supported()) {
            HandlerExport export = exportHandler(handler);
            JsonObject handlerReport = new JsonObject();
            handlerReport.addProperty("legacy_file", handler.legacyFileName());
            handlerReport.addProperty("modern_recipe_type", handler.requireMachine().name());
            handlerReport.addProperty("source_dir", reportPath(export.sourceDir()));
            handlerReport.addProperty("export_file", reportPath(export.exportPath()));
            handlerReport.addProperty("status", export.status());
            handlerReport.addProperty("exported_recipe_count", export.exportedRecipes().size());
            handlerReport.addProperty("skipped_recipe_count", export.failures().size());
            handlerReport.addProperty("omitted_condition_recipe_count", export.omittedConditions().size());
            JsonArray failures = new JsonArray();
            handlerReport.add("failures", failures);
            for (ExportFailure failure : export.failures()) {
                JsonObject failureReport = new JsonObject();
                failureReport.addProperty("source", reportPath(failure.source()));
                failureReport.addProperty("recipe_id", failure.recipeId().toString());
                failureReport.addProperty("message", failure.message());
                failures.add(failureReport);
                HbmNtm.LOGGER.warn("Skipped legacy generic recipe export {} from {}: {}",
                        failure.recipeId(), failure.source(), failure.message());
            }
            JsonArray omittedConditions = new JsonArray();
            handlerReport.add("omitted_conditions", omittedConditions);
            for (OmittedCondition omitted : export.omittedConditions()) {
                JsonObject omittedReport = new JsonObject();
                omittedReport.addProperty("source", reportPath(omitted.source()));
                omittedReport.addProperty("recipe_id", omitted.recipeId().toString());
                omittedReport.addProperty("message", omitted.message());
                omittedConditions.add(omittedReport);
                HbmNtm.LOGGER.info("Omitted condition-disabled legacy generic recipe export {} from {}: {}",
                        omitted.recipeId(), omitted.source(), omitted.message());
            }
            handlers.add(handlerReport);

            if (!export.exportedRecipes().isEmpty()) {
                JsonObject legacyRoot = new JsonObject();
                JsonArray recipeArray = new JsonArray();
                legacyRoot.add("recipes", recipeArray);
                export.exportedRecipes().stream()
                        .sorted(Comparator.comparing(ExportedRecipe::recipe, GenericMachineRecipe.LEGACY_ORDER))
                        .map(ExportedRecipe::legacyJson)
                        .forEach(recipeArray::add);
                saves.add(DataProvider.saveStable(output, legacyRoot, export.exportPath()));
                exportedFileCount++;
                exportedRecipeCount += export.exportedRecipes().size();
                HbmNtm.LOGGER.info("Exported {} legacy generic recipes to {}",
                        export.exportedRecipes().size(), export.exportPath());
            }
            skippedRecipeCount += export.failures().size();
            omittedConditionRecipeCount += export.omittedConditions().size();
        }

        root.addProperty("supported_generic_handler_count", LegacyGenericRecipeHandlers.supported().size());
        root.addProperty("exported_file_count", exportedFileCount);
        root.addProperty("written_file_count", exportedFileCount);
        root.addProperty("exported_recipe_count", exportedRecipeCount);
        root.addProperty("skipped_recipe_count", skippedRecipeCount);
        root.addProperty("omitted_condition_recipe_count", omittedConditionRecipeCount);
        root.addProperty("status", skippedRecipeCount == 0 ? "exported" : "exported_with_skips");
        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy generic machine recipe export";
    }

    private HandlerExport exportHandler(LegacyGenericRecipeHandlers.Handler handler) {
        Path sourceDir = mainRecipeRoot.resolve(handler.outputFolder().getPath());
        Path exportPath = exportDir.resolve(handler.legacyFileName());
        if (!Files.isDirectory(sourceDir)) {
            return new HandlerExport(sourceDir, exportPath, "missing_source_dir", List.of(), List.of(), List.of());
        }

        List<ExportedRecipe> exported = new ArrayList<>();
        List<ExportFailure> failures = new ArrayList<>();
        List<OmittedCondition> omittedConditions = new ArrayList<>();
        try (var stream = Files.walk(sourceDir)) {
            List<Path> recipeFiles = stream
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path recipeFile : recipeFiles) {
                ResourceLocation recipeId = recipeId(handler, sourceDir, recipeFile);
                try (Reader reader = Files.newBufferedReader(recipeFile)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("type") && !handler.requireMachine().serializerId()
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    ConditionEvaluation conditionEvaluation = evaluateRecipeConditions(json);
                    if (!conditionEvaluation.enabled()) {
                        omittedConditions.add(new OmittedCondition(recipeFile, recipeId,
                                conditionEvaluation.message()));
                        continue;
                    }
                    GenericMachineRecipe recipe = handler.requireMachine().serializer().fromJson(recipeId, json);
                    exported.add(new ExportedRecipe(recipe, LegacyGenericRecipeFormat.writeLegacyRecipe(recipe)));
                } catch (RuntimeException exception) {
                    failures.add(new ExportFailure(recipeFile, recipeId, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            failures.add(new ExportFailure(sourceDir,
                    new ResourceLocation(handler.outputFolder().getNamespace(), handler.outputFolder().getPath()),
                    exception.getMessage()));
        }

        String status = failures.isEmpty() ? "exported" : exported.isEmpty() ? "failed" : "exported_with_skips";
        return new HandlerExport(sourceDir, exportPath, status, List.copyOf(exported), List.copyOf(failures),
                List.copyOf(omittedConditions));
    }

    private static ConditionEvaluation evaluateRecipeConditions(JsonObject json) {
        if (!json.has("conditions")) {
            return ConditionEvaluation.enabledResult();
        }
        JsonElement conditions = json.get("conditions");
        if (!conditions.isJsonArray()) {
            throw new JsonSyntaxException("Recipe conditions must be an array");
        }
        int index = 0;
        for (JsonElement condition : conditions.getAsJsonArray()) {
            ConditionEvaluation evaluation = evaluateCondition(condition, "conditions[" + index + "]");
            if (!evaluation.enabled()) {
                return evaluation;
            }
            index++;
        }
        return ConditionEvaluation.enabledResult();
    }

    private static ConditionEvaluation evaluateCondition(JsonElement element, String path) {
        if (!element.isJsonObject()) {
            throw new JsonSyntaxException("Recipe condition at " + path + " must be an object");
        }
        JsonObject condition = element.getAsJsonObject();
        String type = GsonHelper.getAsString(condition, "type");
        return switch (type) {
            case "forge:not" -> {
                JsonElement value = condition.get("value");
                if (value == null) {
                    throw new JsonSyntaxException("Missing value for forge:not condition at " + path);
                }
                ConditionEvaluation inner = evaluateCondition(value, path + ".value");
                if (inner.enabled()) {
                    yield ConditionEvaluation.disabledResult("forge:not condition evaluated false because nested condition "
                            + describeCondition(value) + " evaluated true");
                }
                yield ConditionEvaluation.enabledResult();
            }
            case "forge:tag_empty" -> {
                String tagName = GsonHelper.getAsString(condition, "tag");
                ResourceLocation tagId = new ResourceLocation(tagName);
                if (itemTagEmpty(tagId)) {
                    yield ConditionEvaluation.enabledResult();
                }
                yield ConditionEvaluation.disabledResult("forge:tag_empty condition for item tag '#" + tagName
                        + "' evaluated false because the tag is not empty");
            }
            default -> throw new JsonSyntaxException("Unsupported recipe condition type '" + type
                    + "' at " + path + " while exporting legacy generic recipes");
        };
    }

    private static String describeCondition(JsonElement element) {
        if (!element.isJsonObject()) {
            return element.toString();
        }
        JsonObject condition = element.getAsJsonObject();
        String type = GsonHelper.getAsString(condition, "type", "unknown");
        if ("forge:tag_empty".equals(type) && condition.has("tag")) {
            return "forge:tag_empty #" + GsonHelper.getAsString(condition, "tag");
        }
        return type;
    }

    private static boolean itemTagEmpty(ResourceLocation tagId) {
        TagKey<Item> tagKey = ItemTags.create(tagId);
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
        if (tags == null) {
            throw new JsonSyntaxException("Item tags are not available while evaluating recipe condition '#"
                    + tagId + "'");
        }
        ITag<Item> tag = tags.getTag(tagKey);
        return tag.stream().findAny().isEmpty();
    }

    private static ResourceLocation recipeId(LegacyGenericRecipeHandlers.Handler handler, Path sourceDir,
            Path recipeFile) {
        Path relative = sourceDir.relativize(recipeFile);
        String path = relative.toString().replace('\\', '/');
        path = path.substring(0, path.length() - ".json".length());
        return new ResourceLocation(handler.outputFolder().getNamespace(), handler.outputFolder().getPath() + "/" + path);
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record HandlerExport(Path sourceDir, Path exportPath, String status,
                                 List<ExportedRecipe> exportedRecipes, List<ExportFailure> failures,
                                 List<OmittedCondition> omittedConditions) {
    }

    private record ExportedRecipe(GenericMachineRecipe recipe, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }

    private record OmittedCondition(Path source, ResourceLocation recipeId, String message) {
    }

    private record ConditionEvaluation(boolean enabled, String message) {
        private static ConditionEvaluation enabledResult() {
            return new ConditionEvaluation(true, "");
        }

        private static ConditionEvaluation disabledResult(String message) {
            return new ConditionEvaluation(false, message);
        }
    }
}
