package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.util.HbmRegistryUtil;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class LegacyShredderRecipeExportProvider implements DataProvider {
    private static final String LEGACY_FILE = "hbmShredder.json";
    private static final String OUTPUT_FOLDER = "shredder";
    private static final int REGISTER_POST_SOURCE_ORDER = 1000;

    private static final Set<String> OMITTED_DEFAULT_COLLECTIONS = Set.of("logs", "planks", "saplings");
    private static final Map<String, List<LegacyStack>> LEGACY_INPUT_OVERRIDES = legacyInputOverrides();

    private final Path mainRecipeDir;
    private final Path exportPath;
    private final Path reportPath;

    public LegacyShredderRecipeExportProvider(PackOutput output, Path projectRoot) {
        this.mainRecipeDir = projectRoot.resolve("src")
                .resolve("main")
                .resolve("resources")
                .resolve("data")
                .resolve(HbmNtm.MOD_ID)
                .resolve("recipes")
                .resolve(OUTPUT_FOLDER);
        this.exportPath = projectRoot.resolve("reports").resolve("legacy_recipe_exports").resolve(LEGACY_FILE);
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_shredder_recipe_export_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> saves = new ArrayList<>();
        JsonObject root = new JsonObject();
        root.addProperty("source_dir", reportPath(mainRecipeDir));
        root.addProperty("export_file", reportPath(exportPath));
        root.addProperty("legacy_file", LEGACY_FILE);
        root.addProperty("modern_recipe_type", OUTPUT_FOLDER);
        root.addProperty("note",
                "1.7.10 hbmShredder.json is written before registerPost(); source_order >= 1000 post-generated tag recipes are intentionally omitted from the old file export.");

        ExportReport report = exportRecipes();
        root.addProperty("status", report.status());
        root.addProperty("written_file_count", report.exportedRecipes().isEmpty() ? 0 : 1);
        root.addProperty("exported_recipe_count", report.exportedRecipes().size());
        root.addProperty("skipped_recipe_count", report.failures().size());
        root.addProperty("omitted_post_generated_recipe_count", report.omittedPostGenerated().size());
        root.addProperty("omitted_default_collection_recipe_count", report.omittedDefaultCollections().size());

        JsonArray failures = new JsonArray();
        root.add("failures", failures);
        for (ExportFailure failure : report.failures()) {
            JsonObject failureReport = new JsonObject();
            failureReport.addProperty("source", reportPath(failure.source()));
            failureReport.addProperty("recipe_id", failure.recipeId().toString());
            failureReport.addProperty("message", failure.message());
            failures.add(failureReport);
            HbmNtm.LOGGER.warn("Skipped legacy shredder recipe export {} from {}: {}",
                    failure.recipeId(), failure.source(), failure.message());
        }

        root.add("omitted_post_generated", omittedArray(report.omittedPostGenerated()));
        root.add("omitted_default_collections", omittedArray(report.omittedDefaultCollections()));

        if (!report.exportedRecipes().isEmpty()) {
            JsonObject legacyRoot = new JsonObject();
            legacyRoot.addProperty("comment",
                    "Ingot/block/ore -> dust recipes are generated in post and can therefore not be changed with the config. Non-auto recipes do not use ore dict.");
            JsonArray recipeArray = new JsonArray();
            legacyRoot.add("recipes", recipeArray);
            report.exportedRecipes().stream()
                    .sorted(Comparator.comparingInt((ExportedRecipe exported) -> exported.recipe().sourceOrder())
                            .thenComparing(exported -> exported.recipe().getId().toString())
                            .thenComparingInt(ExportedRecipe::expansionIndex))
                    .map(ExportedRecipe::legacyJson)
                    .forEach(recipeArray::add);
            saves.add(DataProvider.saveStable(output, legacyRoot, exportPath));
            HbmNtm.LOGGER.info("Exported {} legacy shredder recipes to {}",
                    report.exportedRecipes().size(), exportPath);
        }

        saves.add(DataProvider.saveStable(output, root, reportPath));
        return CompletableFuture.allOf(saves.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "HBM legacy shredder recipe export";
    }

    private ExportReport exportRecipes() {
        if (!Files.isDirectory(mainRecipeDir)) {
            return new ExportReport("missing_source_dir", List.of(), List.of(), List.of(), List.of());
        }

        List<ExportedRecipe> exported = new ArrayList<>();
        List<ExportFailure> failures = new ArrayList<>();
        List<OmittedRecipe> omittedPostGenerated = new ArrayList<>();
        List<OmittedRecipe> omittedDefaultCollections = new ArrayList<>();
        try (var stream = Files.walk(mainRecipeDir)) {
            List<Path> recipeFiles = stream
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path recipeFile : recipeFiles) {
                ResourceLocation recipeId = recipeId(recipeFile);
                try (Reader reader = Files.newBufferedReader(recipeFile)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("type") && !ModRecipes.SHREDDER.serializer().getId()
                            .equals(new ResourceLocation(json.get("type").getAsString()))) {
                        continue;
                    }
                    ItemProcessingRecipe recipe = ModRecipes.SHREDDER.serializer().get()
                            .fromJson(recipeId, json);
                    if (recipe.machine() != ItemProcessingRecipe.Machine.SHREDDER) {
                        throw new IllegalArgumentException("Expected SHREDDER recipe, got " + recipe.machine());
                    }
                    String shortName = shortName(recipeId);
                    if (recipe.sourceOrder() >= REGISTER_POST_SOURCE_ORDER) {
                        omittedPostGenerated.add(new OmittedRecipe(recipeFile, recipeId,
                                "1.7.10 writes hbmShredder.json before ShredderRecipes.registerPost()"));
                        continue;
                    }
                    if (OMITTED_DEFAULT_COLLECTIONS.contains(shortName)) {
                        omittedDefaultCollections.add(new OmittedRecipe(recipeFile, recipeId,
                                "1.7.10 default uses OreDictionary collection expansion; modern aggregate tag is not a single old ItemStack"));
                        continue;
                    }

                    List<JsonArray> inputs = legacyInputStacks(recipe);
                    JsonArray output = legacyOutputStack(recipe);
                    for (int i = 0; i < inputs.size(); i++) {
                        JsonObject legacy = new JsonObject();
                        legacy.add("input", inputs.get(i));
                        legacy.add("output", output.deepCopy());
                        exported.add(new ExportedRecipe(recipe, i, legacy));
                    }
                } catch (RuntimeException exception) {
                    failures.add(new ExportFailure(recipeFile, recipeId, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            failures.add(new ExportFailure(mainRecipeDir, new ResourceLocation(HbmNtm.MOD_ID, OUTPUT_FOLDER),
                    exception.getMessage()));
        }

        String status = failures.isEmpty() ? "exported" : exported.isEmpty() ? "failed" : "exported_with_skips";
        return new ExportReport(status, List.copyOf(exported), List.copyOf(failures),
                List.copyOf(omittedPostGenerated), List.copyOf(omittedDefaultCollections));
    }

    private static List<JsonArray> legacyInputStacks(ItemProcessingRecipe recipe) {
        String shortName = shortName(recipe.getId());
        Integer bobbleheadMeta = bobbleheadMeta(shortName);
        if (bobbleheadMeta != null) {
            return List.of(hbm("tile.bobblehead", 1, bobbleheadMeta).toJson());
        }
        List<LegacyStack> overrides = LEGACY_INPUT_OVERRIDES.get(shortName);
        if (overrides != null) {
            return overrides.stream().map(LegacyStack::toJson).toList();
        }

        HbmIngredient input = recipe.input();
        if (input.count() != 1) {
            throw new IllegalArgumentException("Legacy hbmShredder.json ignores input stack size; refusing count "
                    + input.count());
        }
        if (input.legacyOreName() != null || input.isTagIngredient() || input.fluidContainerType() != null) {
            throw new IllegalArgumentException("Legacy hbmShredder.json input must be a concrete ItemStack, got "
                    + input.diagnosticName());
        }
        if (input.hasPartialNbt()) {
            throw new IllegalArgumentException("Legacy hbmShredder.json read path drops input NBT for "
                    + input.diagnosticName());
        }
        if (input.legacyId() != null) {
            int meta = input.legacyWildcard() ? HbmIngredient.WILDCARD_META : Math.max(0, input.legacyMeta());
            return List.of(new LegacyStack(legacyItemId(input.legacyId()), 1, meta, null).toJson());
        }

        List<ItemStack> stacks = input.displayStacks();
        if (stacks.size() != 1) {
            throw new IllegalArgumentException("Legacy hbmShredder.json input needs one concrete display stack, got "
                    + stacks.size() + " for " + input.diagnosticName());
        }
        ItemStack stack = stacks.get(0);
        if (stack.hasTag() && !stack.getTag().isEmpty()) {
            throw new IllegalArgumentException("Legacy hbmShredder.json read path drops input NBT for "
                    + input.diagnosticName());
        }
        return List.of(legacyStack(stack).toJson());
    }

    private static JsonArray legacyOutputStack(ItemProcessingRecipe recipe) {
        if (recipe.outputs().size() != 1) {
            throw new IllegalArgumentException("Legacy hbmShredder.json requires exactly one output");
        }
        HbmItemOutput output = recipe.outputs().get(0);
        if (output.oneOf() || output.entries().size() != 1) {
            throw new IllegalArgumentException("Legacy hbmShredder.json cannot represent one_of outputs");
        }
        HbmItemOutput.Entry entry = output.entries().get(0);
        if (entry.chance() < 1.0F || entry.weight() > 0) {
            throw new IllegalArgumentException("Legacy hbmShredder.json cannot represent chance outputs");
        }

        ItemStack stack = entry.stack();
        if (shortName(recipe.getId()).startsWith("bobblehead_")) {
            if (!stack.hasTag() || !stack.getTag().contains("ScrapType")) {
                throw new IllegalArgumentException("Bobblehead shredder output is missing ScrapType legacy variant");
            }
            return new LegacyStack(new ResourceLocation("hbm", "scrap_plastic"), stack.getCount(),
                    stack.getTag().getInt("ScrapType"), null).toJson();
        }
        if (stack.hasTag() && !stack.getTag().isEmpty()) {
            throw new IllegalArgumentException("Legacy hbmShredder.json output ItemStack cannot round-trip modern NBT: "
                    + stack.getTag());
        }
        return legacyStack(stack).toJson();
    }

    private static LegacyStack legacyStack(ItemStack stack) {
        return LegacyMetaItemMappings.legacyIdentity(stack)
                .map(identity -> new LegacyStack(identity.legacyId(), stack.getCount(), identity.legacyMeta(), null))
                .orElseGet(() -> {
                    ResourceLocation itemId = HbmRegistryUtil.itemKey(stack.getItem());
                    if (itemId == null) {
                        throw new IllegalArgumentException("Cannot resolve item id for legacy shredder stack: " + stack);
                    }
                    ResourceLocation legacyId = itemId;
                    if (HbmNtm.MOD_ID.equals(itemId.getNamespace())) {
                        legacyId = isModernHbmBlockItem(itemId)
                                ? new ResourceLocation("hbm", "tile." + itemId.getPath())
                                : new ResourceLocation("hbm", itemId.getPath());
                    }
                    return new LegacyStack(legacyId, stack.getCount(), stack.getDamageValue(),
                            stack.hasTag() ? stack.getTag() : null);
                });
    }

    private static boolean isModernHbmBlockItem(ResourceLocation itemId) {
        return ForgeRegistries.BLOCKS.containsKey(itemId);
    }

    private static ResourceLocation legacyItemId(ResourceLocation itemId) {
        return HbmNtm.MOD_ID.equals(itemId.getNamespace()) ? new ResourceLocation("hbm", itemId.getPath()) : itemId;
    }

    private ResourceLocation recipeId(Path recipeFile) {
        Path relative = mainRecipeDir.relativize(recipeFile);
        String path = relative.toString().replace('\\', '/');
        path = path.substring(0, path.length() - ".json".length());
        return new ResourceLocation(HbmNtm.MOD_ID, OUTPUT_FOLDER + "/" + path);
    }

    private static String shortName(ResourceLocation recipeId) {
        String path = recipeId.getPath();
        String prefix = OUTPUT_FOLDER + "/";
        return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
    }

    private static JsonArray omittedArray(List<OmittedRecipe> omitted) {
        JsonArray array = new JsonArray();
        for (OmittedRecipe recipe : omitted) {
            JsonObject object = new JsonObject();
            object.addProperty("source", reportPath(recipe.source()));
            object.addProperty("recipe_id", recipe.recipeId().toString());
            object.addProperty("reason", recipe.reason());
            array.add(object);
        }
        return array;
    }

    private static Map<String, List<LegacyStack>> legacyInputOverrides() {
        Map<String, List<LegacyStack>> overrides = new LinkedHashMap<>();

        overrides.put("chiseled_quartz_block", List.of(vanilla("quartz_block", 1, 1)));
        overrides.put("quartz_pillar", List.of(vanilla("quartz_block", 1, 2)));
        overrides.put("quartz_slab", List.of(vanilla("stone_slab", 1, 7)));
        overrides.put("quartz_ore", List.of(vanilla("quartz_ore")));
        overrides.put("stone_bricks", List.of(vanilla("stonebrick")));
        overrides.put("bricks", List.of(vanilla("brick_block")));
        overrides.put("terracotta", List.of(vanilla("hardened_clay")));
        overrides.put("sugar_cane", List.of(vanilla("reeds")));
        overrides.put("sand", List.of(vanilla("sand")));
        overrides.put("diamond_ore", List.of(vanilla("diamond_ore")));

        overrides.put("ore_rare", List.of(hbm("tile.ore_rare")));
        overrides.put("aluminium_ore", List.of(hbm("tile.ore_aluminium")));
        overrides.put("chunk_ore_rare", List.of(hbm("chunk_ore", 1, 0)));
        overrides.put("pipes_steel", List.of(hbm("pipes_steel")));
        overrides.put("steel_scaffold", List.of(hbm("tile.steel_scaffold", 1, HbmIngredient.WILDCARD_META)));

        String[] crystalRecipes = {
                "crystal_coal", "crystal_iron", "crystal_gold", "crystal_redstone", "crystal_lapis",
                "crystal_diamond", "crystal_uranium", "crystal_plutonium", "crystal_thorium",
                "crystal_titanium", "crystal_sulfur", "crystal_niter", "crystal_copper",
                "crystal_tungsten", "crystal_aluminium", "crystal_fluorite", "crystal_beryllium",
                "crystal_lead", "crystal_schraranium", "crystal_schrabidium", "crystal_rare",
                "crystal_phosphorus", "crystal_trixite", "crystal_lithium", "crystal_starmetal",
                "crystal_cobalt"
        };
        for (String crystal : crystalRecipes) {
            overrides.put(crystal, List.of(hbm(crystal)));
        }

        overrides.put("skeleton_skull", List.of(vanilla("skull", 1, 0)));
        overrides.put("wither_skeleton_skull", List.of(vanilla("skull", 1, 1)));
        overrides.put("zombie_head", List.of(vanilla("skull", 1, 2)));
        overrides.put("player_head", List.of(vanilla("skull", 1, 3)));
        overrides.put("creeper_head", List.of(vanilla("skull", 1, 4)));

        String[] terracotta = {
                "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
                "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
        };
        for (int meta = 0; meta < terracotta.length; meta++) {
            overrides.put(terracotta[meta] + "_terracotta", List.of(vanilla("stained_hardened_clay", 1, meta)));
        }

        List<LegacyStack> wool = new ArrayList<>();
        for (int meta = 0; meta < 16; meta++) {
            wool.add(vanilla("wool", 1, meta));
        }
        overrides.put("wool", List.copyOf(wool));

        for (int meta = 0; meta < 6; meta++) {
            overrides.put("sellafield_" + meta, List.of(hbm("tile.sellafield", 1, meta)));
        }

        return Map.copyOf(overrides);
    }

    private static Integer bobbleheadMeta(String shortName) {
        if (!shortName.startsWith("bobblehead_") || shortName.length() < "bobblehead_00".length()) {
            return null;
        }
        try {
            return Integer.parseInt(shortName.substring("bobblehead_".length(), "bobblehead_".length() + 2));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static LegacyStack vanilla(String path) {
        return vanilla(path, 1, 0);
    }

    private static LegacyStack vanilla(String path, int count, int meta) {
        return new LegacyStack(new ResourceLocation("minecraft", path), count, meta, null);
    }

    private static LegacyStack hbm(String path) {
        return hbm(path, 1, 0);
    }

    private static LegacyStack hbm(String path, int count, int meta) {
        return new LegacyStack(new ResourceLocation("hbm", path), count, meta, null);
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record LegacyStack(ResourceLocation id, int count, int meta, CompoundTag tag) {
        JsonArray toJson() {
            JsonArray array = new JsonArray();
            array.add(id.toString());
            boolean hasTag = tag != null && !tag.isEmpty();
            if (count != 1 || meta != 0 || hasTag) {
                array.add(count);
            }
            if (meta != 0 || hasTag) {
                array.add(meta);
            }
            if (hasTag) {
                array.add(tag.toString());
            }
            return array;
        }
    }

    private record ExportReport(String status, List<ExportedRecipe> exportedRecipes,
                                List<ExportFailure> failures, List<OmittedRecipe> omittedPostGenerated,
                                List<OmittedRecipe> omittedDefaultCollections) {
    }

    private record ExportedRecipe(ItemProcessingRecipe recipe, int expansionIndex, JsonObject legacyJson) {
    }

    private record ExportFailure(Path source, ResourceLocation recipeId, String message) {
    }

    private record OmittedRecipe(Path source, ResourceLocation recipeId, String reason) {
    }
}
