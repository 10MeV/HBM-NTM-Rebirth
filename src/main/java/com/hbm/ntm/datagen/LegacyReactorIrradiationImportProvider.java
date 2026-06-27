package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class LegacyReactorIrradiationImportProvider implements DataProvider {
    private static final LegacyFile OUTGASSER = new LegacyFile("hbmIrradiation.json", "outgasser");
    private static final LegacyFile EXPOSURE_CHAMBER =
            new LegacyFile("hbmExposureChamber.json", "exposure_chamber");

    private final PackOutput.PathProvider recipePathProvider;
    private final Path reportPath;
    private final Path legacyRecipeDir;
    private final Path mainRecipeDir;

    public LegacyReactorIrradiationImportProvider(PackOutput output, Path projectRoot) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
        this.reportPath = projectRoot.resolve("reports")
                .resolve("legacy_reactor_irradiation_import_report.json");
        this.legacyRecipeDir = projectRoot.resolve("legacy_recipes");
        this.mainRecipeDir = projectRoot.resolve("src").resolve("main").resolve("resources")
                .resolve("data").resolve(HbmNtm.MOD_ID).resolve("recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject report = new JsonObject();
        report.addProperty("legacy_recipe_dir", reportPath(legacyRecipeDir));
        JsonArray handlers = new JsonArray();
        report.add("handlers", handlers);

        saves.addAll(importOutgasser(output, handlers));
        saves.addAll(importExposureChamber(output, handlers));
        saves.add(DataProvider.saveStable(output, report, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy reactor irradiation recipe import";
    }

    private List<CompletableFuture<?>> importOutgasser(CachedOutput output, JsonArray handlers) {
        return importLegacyFile(output, handlers, OUTGASSER, (sourceIndex, object) -> {
            HbmIngredient input = readLegacyAStack(object.getAsJsonArray("input"), "input", sourceIndex);
            Optional<ItemStack> solidOutput = object.has("solidOutput")
                    ? Optional.of(readLegacyItemStack(object.getAsJsonArray("solidOutput"), "solidOutput",
                            sourceIndex))
                    : Optional.empty();
            Optional<HbmFluidStack> fluidOutput = object.has("fluidOutput")
                    ? Optional.of(readLegacyFluidStack(object.getAsJsonArray("fluidOutput"), "fluidOutput",
                            sourceIndex))
                    : Optional.empty();
            if (solidOutput.isEmpty() && fluidOutput.isEmpty()) {
                throw new JsonSyntaxException("recipe #" + sourceIndex + " has no outgasser outputs");
            }

            JsonObject json = new JsonObject();
            json.addProperty("type", ModRecipes.OUTGASSER.serializer().getId().toString());
            json.add("input", input.toJson());
            solidOutput.ifPresent(stack -> json.add("solid_output", HbmItemOutput.of(stack).toJson()));
            fluidOutput.ifPresent(stack -> json.add("fluid_output", writeModernFluidStack(stack)));
            if (object.has("fusionOnly") && object.get("fusionOnly").getAsBoolean()) {
                json.addProperty("fusion_only", true);
            }

            return new ImportedRecipe(new ResourceLocation(HbmNtm.MOD_ID,
                    "outgasser/legacy_import_" + sourceIndex), json);
        });
    }

    private List<CompletableFuture<?>> importExposureChamber(CachedOutput output, JsonArray handlers) {
        return importLegacyFile(output, handlers, EXPOSURE_CHAMBER, (sourceIndex, object) -> {
            HbmIngredient particle = readLegacyAStack(object.getAsJsonArray("particle"), "particle", sourceIndex);
            HbmIngredient ingredient =
                    readLegacyAStack(object.getAsJsonArray("ingredient"), "ingredient", sourceIndex);
            ItemStack outputStack = readLegacyItemStack(object.getAsJsonArray("output"), "output", sourceIndex);

            JsonObject json = new JsonObject();
            json.addProperty("type", ModRecipes.EXPOSURE_CHAMBER.serializer().getId().toString());
            json.add("particle", particle.toJson());
            json.add("ingredient", ingredient.toJson());
            json.add("output", HbmItemOutput.of(outputStack).toJson());
            json.addProperty("source_order", sourceIndex);

            return new ImportedRecipe(new ResourceLocation(HbmNtm.MOD_ID,
                    "exposure_chamber/legacy_import_" + sourceIndex), json);
        });
    }

    private List<CompletableFuture<?>> importLegacyFile(CachedOutput output, JsonArray handlers, LegacyFile legacyFile,
            RecipeReader readerFunction) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject handler = new JsonObject();
        handlers.add(handler);
        handler.addProperty("legacy_file", legacyFile.fileName());
        handler.addProperty("modern_recipe_type", legacyFile.modernType());

        Path source = resolveLegacyFile(legacyFile.fileName());
        if (source == null) {
            reportMainResourceFallback(handler, legacyFile);
            return saves;
        }
        handler.addProperty("source", reportPath(source));

        JsonArray imported = new JsonArray();
        JsonArray failures = new JsonArray();
        handler.add("imported", imported);
        handler.add("failures", failures);
        int sourceRecipeCount = 0;

        try (Reader fileReader = Files.newBufferedReader(source)) {
            JsonObject root = JsonParser.parseReader(fileReader).getAsJsonObject();
            JsonArray recipes = root.getAsJsonArray("recipes");
            if (recipes == null) {
                throw new JsonSyntaxException("Missing recipes array");
            }
            sourceRecipeCount = recipes.size();
            for (int i = 0; i < recipes.size(); i++) {
                try {
                    ImportedRecipe recipe = readerFunction.read(i, recipes.get(i).getAsJsonObject());
                    JsonObject entry = new JsonObject();
                    entry.addProperty("source_index", i);
                    entry.addProperty("id", recipe.id().toString());
                    imported.add(entry);
                    saves.add(DataProvider.saveStable(output, recipe.json(), recipePathProvider.json(recipe.id())));
                } catch (RuntimeException exception) {
                    JsonObject failure = new JsonObject();
                    failure.addProperty("source_index", i);
                    failure.addProperty("message", exception.getMessage());
                    failures.add(failure);
                    HbmNtm.LOGGER.warn("Skipped legacy reactor irradiation recipe {} #{}: {}",
                            legacyFile.fileName(), i, exception.getMessage());
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to import legacy reactor irradiation recipe file " + source,
                    exception);
        }

        handler.addProperty("status", failures.isEmpty() ? "imported" : "imported_with_skips");
        handler.addProperty("source_recipe_count", sourceRecipeCount);
        handler.addProperty("imported_recipe_count", imported.size());
        handler.addProperty("skipped_recipe_count", failures.size());
        HbmNtm.LOGGER.info("Imported legacy reactor irradiation recipes {} {}/{} from {} skipped={}",
                legacyFile.fileName(), imported.size(), sourceRecipeCount, source, failures.size());
        return saves;
    }

    private static HbmIngredient readLegacyAStack(JsonArray array, String key, int sourceIndex) {
        if (array == null || array.size() < 2) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " missing " + key + " stack");
        }
        String kind = array.get(0).getAsString();
        if ("dict".equals(kind)) {
            int count = array.size() > 2 ? array.get(2).getAsInt() : 1;
            return HbmIngredient.legacyOre(array.get(1).getAsString(), count);
        }
        if (!"item".equals(kind) && !"nbt".equals(kind)) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " unsupported " + key
                    + " stack kind " + kind);
        }
        ResourceLocation legacyId = readLegacyId(array.get(1).getAsString());
        int count = array.size() > 2 ? array.get(2).getAsInt() : 1;
        int meta = array.size() > 3 ? array.get(3).getAsInt() : 0;
        CompoundTag nbt = array.size() > 4 ? parseNbt(array.get(4).getAsString(), key, sourceIndex)
                : new CompoundTag();
        if (nbt.isEmpty() && LegacyMetaItemMappings.item(legacyId, meta).isPresent()) {
            return HbmIngredient.legacyMeta(legacyId, meta, count);
        }
        ItemStack stack = mapLegacyItemStack(legacyId, count, meta, nbt, key, sourceIndex);
        return nbt.isEmpty() ? HbmIngredient.exact(stack) : HbmIngredient.partialNbt(stack);
    }

    private static ItemStack readLegacyItemStack(JsonArray array, String key, int sourceIndex) {
        if (array == null || array.size() < 1) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " missing " + key + " item stack");
        }
        ResourceLocation legacyId = readLegacyId(array.get(0).getAsString());
        int count = array.size() > 1 ? array.get(1).getAsInt() : 1;
        int meta = array.size() > 2 ? array.get(2).getAsInt() : 0;
        CompoundTag nbt = array.size() > 3 ? parseNbt(array.get(3).getAsString(), key, sourceIndex)
                : new CompoundTag();
        return mapLegacyItemStack(legacyId, count, meta, nbt, key, sourceIndex);
    }

    private static HbmFluidStack readLegacyFluidStack(JsonArray array, String key, int sourceIndex) {
        if (array == null || array.size() < 2) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " missing " + key + " fluid stack");
        }
        FluidType type = HbmFluidJsonUtil.requireFluidReference(array.get(0),
                "recipe #" + sourceIndex + " " + key);
        int amount = array.get(1).getAsInt();
        int pressure = array.size() < 3 ? 0 : array.get(2).getAsInt();
        HbmFluidStack stack = new HbmFluidStack(type, amount, pressure);
        if (stack.isEmpty()) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " empty " + key + " fluid stack");
        }
        return stack;
    }

    private static ItemStack mapLegacyItemStack(ResourceLocation legacyId, int count, int meta, CompoundTag nbt,
            String key, int sourceIndex) {
        Optional<ItemStack> mappedMeta = LegacyMetaItemMappings.stack(legacyId, meta, count);
        ItemStack stack = mappedMeta.orElseGet(() -> {
            ResourceLocation modernId = modernItemId(legacyId);
            Item item = HbmRegistryUtil.item(modernId)
                    .orElseThrow(() -> new JsonSyntaxException("recipe #" + sourceIndex + " unknown " + key
                            + " item '" + legacyId + "' meta " + meta));
            return new ItemStack(item, Math.max(1, count));
        });
        if (!nbt.isEmpty()) {
            stack.setTag(nbt.copy());
        }
        return stack;
    }

    private static JsonObject writeModernFluidStack(HbmFluidStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", stack.type().getName());
        object.addProperty("amount", stack.amount());
        if (stack.pressure() != 0) {
            object.addProperty("pressure", stack.pressure());
        }
        return object;
    }

    private static ResourceLocation modernItemId(ResourceLocation legacyId) {
        if ("hbm".equals(legacyId.getNamespace())) {
            return new ResourceLocation(HbmNtm.MOD_ID, legacyId.getPath());
        }
        return legacyId;
    }

    private static ResourceLocation readLegacyId(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new JsonSyntaxException("Invalid item id '" + id + "'");
        }
        return location;
    }

    private static CompoundTag parseNbt(String nbt, String key, int sourceIndex) {
        try {
            return TagParser.parseTag(nbt);
        } catch (CommandSyntaxException exception) {
            throw new JsonSyntaxException("recipe #" + sourceIndex + " invalid " + key + " NBT: "
                    + exception.getMessage(), exception);
        }
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

    private void reportMainResourceFallback(JsonObject handler, LegacyFile legacyFile) {
        Path recipeDir = mainRecipeDir.resolve(legacyFile.modernType());
        int count = countJsonFiles(recipeDir);
        handler.addProperty("status", count > 0 ? "main_resources_only" : "missing_template");
        handler.addProperty("external_template_status", "missing");
        handler.addProperty("main_resource_recipe_dir", reportPath(recipeDir));
        handler.addProperty("main_resource_recipe_count", count);
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

    private interface RecipeReader {
        ImportedRecipe read(int sourceIndex, JsonObject object);
    }

    private record LegacyFile(String fileName, String modernType) {
    }

    private record ImportedRecipe(ResourceLocation id, JsonObject json) {
    }
}
