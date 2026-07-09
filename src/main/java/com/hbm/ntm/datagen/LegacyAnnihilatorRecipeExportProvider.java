package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.recipe.AnnihilatorRecipe;
import com.hbm.ntm.recipe.LegacyGenericRecipeFormat;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.hbm.ntm.world.saveddata.AnnihilatorSavedData;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class LegacyAnnihilatorRecipeExportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmAnnihilator.json";
    private static final String OUTPUT_FOLDER = "annihilator";
    private static final String EMPTY_DEFAULT_STATUS = "exported_empty_defaults";

    private final Path mainRecipeDir;
    private final Path exportPath;
    private final Path reportPath;

    public LegacyAnnihilatorRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeDir = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes")
                .resolve(OUTPUT_FOLDER);
        this.exportPath = projectRoot.resolve("reports").resolve("legacy_recipe_exports").resolve(LEGACY_FILE);
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_annihilator_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_dir", reportPath(mainRecipeDir));
        root.addProperty("export_file", reportPath(exportPath));
        root.addProperty("legacy_file", LEGACY_FILE);
        root.addProperty("modern_recipe_type", OUTPUT_FOLDER);
        root.addProperty("note", "1.7.10 default milestones are 528-only and remain excluded from shipped defaults.");

        ExportReport report = exportRecipes();
        boolean writeLegacyFile = !report.exportedRecipes().isEmpty()
                || EMPTY_DEFAULT_STATUS.equals(report.status());
        root.addProperty("status", report.status());
        root.addProperty("written_file_count", writeLegacyFile ? 1 : 0);
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
            HbmNtm.LOGGER.warn("Skipped legacy annihilator recipe export {} from {}: {}",
                    failure.recipeId(), failure.source(), failure.message());
        }

        if (writeLegacyFile) {
            JsonObject legacyRoot = new JsonObject();
            JsonArray recipeArray = new JsonArray();
            legacyRoot.add("recipes", recipeArray);
            report.exportedRecipes().stream()
                    .sorted(Comparator.comparingInt(ExportedRecipe::sourceOrder)
                            .thenComparing(recipe -> recipe.recipe().getId().toString()))
                    .map(ExportedRecipe::legacyJson)
                    .forEach(recipeArray::add);
            saves.add(DataProvider.saveStable(output, legacyRoot, exportPath));
            HbmNtm.LOGGER.info("Exported {} legacy annihilator recipes to {}",
                    report.exportedRecipes().size(), exportPath);
        }

        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy annihilator recipe export";
    }

    private ExportReport exportRecipes() {
        if (!Files.isDirectory(mainRecipeDir)) {
            return new ExportReport(EMPTY_DEFAULT_STATUS, List.of(), List.of());
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
                    if (json.has("type") && !ModRecipes.ANNIHILATOR.serializer().getId()
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    AnnihilatorRecipe recipe = ModRecipes.ANNIHILATOR.serializer().get()
                            .fromJson(recipeId, json);
                    exported.add(new ExportedRecipe(recipe, legacyJson(recipe), sourceOrder(json)));
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

    private static JsonObject legacyJson(AnnihilatorRecipe recipe) {
        JsonObject object = new JsonObject();
        object.add("key", legacyKey(recipe.key()));
        JsonArray milestones = new JsonArray();
        for (AnnihilatorRecipe.Milestone milestone : recipe.milestones()) {
            JsonObject legacyMilestone = new JsonObject();
            legacyMilestone.addProperty("amount", milestone.amount());
            legacyMilestone.add("payout", LegacyGenericRecipeFormat.writeLegacyItemStack(milestone.payout()));
            milestones.add(legacyMilestone);
        }
        object.add("milestones", milestones);
        return object;
    }

    private static JsonObject legacyKey(AnnihilatorSavedData.PoolKey key) {
        JsonObject object = new JsonObject();
        switch (key.kind()) {
            case ITEM -> {
                LegacyKeyIdentity identity = legacyItemIdentity(key.item(), 0);
                if (identity.meta() != 0) {
                    throw new IllegalArgumentException("Legacy hbmAnnihilator.json item key cannot represent split "
                            + "legacy metadata item " + identity.id() + " meta " + identity.meta());
                }
                object.addProperty("type", "item");
                object.addProperty("item", identity.id().toString());
            }
            case ITEM_META -> {
                LegacyKeyIdentity identity = legacyItemIdentity(key.item(), key.meta());
                object.addProperty("type", "comp");
                object.addProperty("item", identity.id().toString());
                object.addProperty("meta", identity.meta());
            }
            case FLUID -> {
                object.addProperty("type", "fluid");
                object.addProperty("fluid", legacyFluidName(key.fluid()));
            }
            case ORE_DICT -> {
                object.addProperty("type", "dict");
                object.addProperty("dict", key.oreDict());
            }
            case UNKNOWN -> throw new IllegalArgumentException("Unknown annihilator key kind");
        }
        return object;
    }

    private static LegacyKeyIdentity legacyItemIdentity(ResourceLocation itemId, int meta) {
        Optional<Item> item = HbmRegistryUtil.item(itemId);
        if (item.isPresent()) {
            ItemStack stack = new ItemStack(item.get());
            if (meta != 0) {
                stack.setDamageValue(Math.max(0, meta));
            }
            Optional<LegacyMetaItemMappings.LegacyStackIdentity> identity =
                    LegacyMetaItemMappings.legacyIdentity(stack);
            if (identity.isPresent()) {
                return new LegacyKeyIdentity(identity.get().legacyId(), identity.get().legacyMeta());
            }
        }
        return new LegacyKeyIdentity(legacyItemId(itemId), meta);
    }

    private static ResourceLocation legacyItemId(ResourceLocation itemId) {
        return HbmNtm.MOD_ID.equals(itemId.getNamespace()) ? new ResourceLocation("hbm", itemId.getPath()) : itemId;
    }

    private static String legacyFluidName(String fluid) {
        FluidType type = HbmFluidJsonUtil.readFluidReference(fluid);
        return type == HbmFluids.NONE ? fluid : type.getUnlocalizedName();
    }

    private static int sourceOrder(JsonObject json) {
        return json.has("source_order") ? json.get("source_order").getAsInt() : Integer.MAX_VALUE;
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

    private record LegacyKeyIdentity(ResourceLocation id, int meta) {
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures) {
    }

    private record ExportedRecipe(AnnihilatorRecipe recipe, JsonObject legacyJson, int sourceOrder) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }
}
