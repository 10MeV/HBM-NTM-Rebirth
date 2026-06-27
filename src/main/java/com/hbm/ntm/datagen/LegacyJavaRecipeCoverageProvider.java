package com.hbm.ntm.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyJavaRecipeCoverageProvider implements DataProvider {
    private static final Pattern LEGACY_RECIPE_NAME =
            Pattern.compile("new\\s+(?:GenericRecipe|FusionRecipe|PUREXRecipe|PlasmaForgeRecipe)\\(\"([^\"]+)\"\\)");
    private static final Set<String> REACTOR_PLASMA_FORGE_RECIPES = Set.of(
            "plsm.fusionvessel",
            "plsm.icfcell",
            "plsm.icfemitter",
            "plsm.icfcapacitor",
            "plsm.icfturbo",
            "plsm.icfcasing",
            "plsm.icfport",
            "plsm.icfcontroller",
            "plsm.icfscaffold",
            "plsm.icfvessel",
            "plsm.icfstructural",
            "plsm.icfcore",
            "plsm.icfpress",
            "plsm.dfccore",
            "plsm.dfcemitter",
            "plsm.dfcreceiver",
            "plsm.dfcinjector",
            "plsm.dfcstabilizer");
    private static final Map<String, String> RUNTIME_PAUSED_RECIPE_BLOCKERS = Map.of(
            "plsm.dfccore", "DFC runtime migration is paused by current reactor-first boundary; recipe anchor is materialized but the machine family is not counted complete",
            "plsm.dfcemitter", "DFC runtime migration is paused by current reactor-first boundary; recipe anchor is materialized but the machine family is not counted complete",
            "plsm.dfcreceiver", "DFC runtime migration is paused by current reactor-first boundary; recipe anchor is materialized but the machine family is not counted complete",
            "plsm.dfcinjector", "DFC runtime migration is paused by current reactor-first boundary; recipe anchor is materialized but the machine family is not counted complete",
            "plsm.dfcstabilizer", "DFC runtime migration is paused by current reactor-first boundary; recipe anchor is materialized but the machine family is not counted complete");
    private static final List<CountRecipeFamily> COUNT_RECIPE_FAMILIES = List.of(
            new CountRecipeFamily("breeding_reactor", "BreederRecipes.java", "hbmBreeder.json", "breeding_reactor",
                    31),
            new CountRecipeFamily("fuel_pool", "FuelPoolRecipes.java", "hbmFuelpool.json", "fuel_pool", 31),
            new CountRecipeFamily("outgasser", "OutgasserRecipes.java", "hbmIrradiation.json", "outgasser", 19),
            new CountRecipeFamily("exposure_chamber", "ExposureChamberRecipes.java", "hbmExposureChamber.json",
                    "exposure_chamber", 4),
            new CountRecipeFamily("fusion_fluid_breeder", "FluidBreederRecipes.java", "hbmIrradiationFluids.json",
                    "fusion_fluid_breeder", 3));
    private static final Set<String> DECORATIVE_DOOR_RECIPE_EXCLUSIONS = Set.of(
            "ass.sealframe",
            "ass.sealcontroller",
            "ass.vaultdoor",
            "ass.blastdoor",
            "ass.firedoor",
            "ass.seal",
            "ass.slidingdoor",
            "ass.vehicledoor",
            "ass.waterdoor",
            "ass.qedoor",
            "ass.queslidingdoor",
            "ass.roundairlock",
            "ass.secureaccess",
            "ass.slidingseal",
            "ass.silohatch",
            "ass.silohatchlarge");
    private static final Map<String, String> MOD_INTEGRATION_RECIPE_EXCLUSIONS = Map.of(
            "ass.digimemer", "legacy conditional Mekanism integration output is excluded by the mod-integration freeze");
    private static final Map<String, String> UNMIGRATED_OUTPUT_BLOCKERS = Map.of(
            "ass.chopper", "output spawn_chopper and the Hunter Chopper spawn/entity item chain are not registered in the clean port",
            "ass.ballsotron", "output spawn_worm and the mechanical worm spawn/entity item chain are not registered in the clean port",
            "ass.minenaval", "output mine_naval block/runtime is not registered in the clean port",
            "ass.levibomb", "output float_bomb block/runtime is not registered in the clean port",
            "ass.endobomb", "output therm_endo block/runtime is not registered in the clean port",
            "ass.exobomb", "output therm_exo block/runtime is not registered in the clean port");

    private final PackOutput output;
    private final Path projectRoot;
    private final Path reportPath;

    public LegacyJavaRecipeCoverageProvider(PackOutput output, Path projectRoot) {
        this.output = output;
        this.projectRoot = projectRoot;
        this.reportPath = projectRoot.resolve("reports").resolve("legacy_java_recipe_coverage_report.json");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject root = new JsonObject();
        root.addProperty("note", "Coverage is extracted from 1.7.10 Java GenericRecipe registrations, not legacy template JSON.");
        JsonArray machines = new JsonArray();
        root.add("machines", machines);
        JsonArray recipeFamilies = new JsonArray();
        root.add("recipe_families", recipeFamilies);

        Map<String, ModernRecipe> modernRecipes = collectModernRecipes();
        addMachineReport(machines, "chemical_plant", "ChemicalPlantRecipes.java", "chem.", modernRecipes);
        addMachineReport(machines, "assembly_machine", "AssemblyMachineRecipes.java", "ass.", modernRecipes);
        addMachineReport(machines, "purex", "PUREXRecipes.java", "purex.", modernRecipes);
        addMachineReport(machines, "fusion_reactor", "FusionRecipes.java", "fus.", modernRecipes);
        addMachineReport(machines, "plasma_forge_reactor_scope", "PlasmaForgeRecipes.java", "plsm.",
                REACTOR_PLASMA_FORGE_RECIPES, modernRecipes);
        for (CountRecipeFamily family : COUNT_RECIPE_FAMILIES) {
            addCountFamilyReport(recipeFamilies, family);
        }

        return DataProvider.saveStable(cachedOutput, root, reportPath);
    }

    @Override
    public String getName() {
        return "HBM legacy Java machine recipe coverage";
    }

    private void addMachineReport(JsonArray machines, String machine, String legacyFileName, String prefix,
            Map<String, ModernRecipe> modernRecipes) {
        addMachineReport(machines, machine, legacyFileName, prefix, null, modernRecipes);
    }

    private void addMachineReport(JsonArray machines, String machine, String legacyFileName, String prefix,
            Set<String> includedInternalNames, Map<String, ModernRecipe> modernRecipes) {
        JsonObject report = new JsonObject();
        machines.add(report);
        report.addProperty("machine", machine);
        report.addProperty("legacy_file", legacyFileName);
        Path source = legacyRecipeSource(legacyFileName);
        report.addProperty("legacy_source", reportPath(source));

        JsonArray present = new JsonArray();
        JsonArray missing = new JsonArray();
        JsonArray excluded = new JsonArray();
        JsonArray blocked = new JsonArray();
        report.add("present", present);
        report.add("missing", missing);
        report.add("excluded", excluded);
        report.add("blocked", blocked);

        if (!Files.isRegularFile(source)) {
            report.addProperty("status", "missing_legacy_source");
            report.addProperty("legacy_count", 0);
            report.addProperty("modern_count", modernRecipes.values().stream()
                    .filter(recipe -> recipe.internalName().startsWith(prefix))
                    .count());
            return;
        }

        report.addProperty("status", "checked");
        int legacyCount = 0;
        for (LegacyRecipe recipe : extractLegacyRecipes(source)) {
            if (includedInternalNames != null && !includedInternalNames.contains(recipe.internalName())) {
                continue;
            }
            legacyCount++;
            ModernRecipe modern = modernRecipes.get(recipe.internalName());
            JsonObject entry = new JsonObject();
            entry.addProperty("source_order", recipe.sourceOrder());
            entry.addProperty("internal_name", recipe.internalName());
            if (RUNTIME_PAUSED_RECIPE_BLOCKERS.containsKey(recipe.internalName())) {
                entry.addProperty("reason", RUNTIME_PAUSED_RECIPE_BLOCKERS.get(recipe.internalName()));
                if (modern != null) {
                    entry.addProperty("modern_id", modern.id());
                    entry.addProperty("modern_source_order", modern.sourceOrder());
                }
                blocked.add(entry);
            } else if (modern == null) {
                if (DECORATIVE_DOOR_RECIPE_EXCLUSIONS.contains(recipe.internalName())) {
                    entry.addProperty("reason", "old door/seal-door recipes are decorative/deferred by project rule");
                    excluded.add(entry);
                } else if (MOD_INTEGRATION_RECIPE_EXCLUSIONS.containsKey(recipe.internalName())) {
                    entry.addProperty("reason", MOD_INTEGRATION_RECIPE_EXCLUSIONS.get(recipe.internalName()));
                    excluded.add(entry);
                } else if (UNMIGRATED_OUTPUT_BLOCKERS.containsKey(recipe.internalName())) {
                    entry.addProperty("reason", UNMIGRATED_OUTPUT_BLOCKERS.get(recipe.internalName()));
                    blocked.add(entry);
                } else {
                    missing.add(entry);
                }
            } else {
                entry.addProperty("modern_id", modern.id());
                entry.addProperty("modern_source_order", modern.sourceOrder());
                present.add(entry);
            }
        }
        long modernCount = modernRecipes.values().stream()
                .filter(recipe -> recipe.internalName().startsWith(prefix))
                .filter(recipe -> includedInternalNames == null || includedInternalNames.contains(recipe.internalName()))
                .count();
        report.addProperty("legacy_count", legacyCount);
        report.addProperty("modern_count", modernCount);
        report.addProperty("present_count", present.size());
        report.addProperty("excluded_count", excluded.size());
        report.addProperty("blocked_count", blocked.size());
        report.addProperty("missing_count", missing.size());
    }

    private void addCountFamilyReport(JsonArray families, CountRecipeFamily family) {
        JsonObject report = new JsonObject();
        families.add(report);
        report.addProperty("machine", family.machine());
        report.addProperty("legacy_file", family.legacyFileName());
        report.addProperty("legacy_json_file", family.legacyJsonFileName());
        report.addProperty("modern_recipe_type", family.modernRecipeFolder());
        report.addProperty("legacy_source", reportPath(legacyRecipeSource(family.legacyFileName())));

        Path mainResourceDir = recipeDirectory(family.modernRecipeFolder());
        report.addProperty("main_resource_recipe_dir", reportPath(mainResourceDir));
        report.addProperty("legacy_count", family.legacyDefaultCount());
        int modernCount = countMainResourceRecipes(mainResourceDir);
        report.addProperty("modern_count", modernCount);
        report.addProperty("present_count", Math.min(family.legacyDefaultCount(), modernCount));
        report.addProperty("missing_count", Math.max(0, family.legacyDefaultCount() - modernCount));
        report.addProperty("blocked_count", 0);
        report.addProperty("status", modernCount >= family.legacyDefaultCount() ? "main_resources_aligned"
                : "main_resources_short");
    }

    private Map<String, ModernRecipe> collectModernRecipes() {
        Map<String, ModernRecipe> recipes = new LinkedHashMap<>();
        new HbmRecipeProvider(output).buildRecipes(recipe -> {
            JsonObject json = new JsonObject();
            recipe.serializeRecipeData(json);
            if (!json.has("internal_name")) {
                return;
            }
            String internalName = json.get("internal_name").getAsString();
            int sourceOrder = json.has("source_order") ? json.get("source_order").getAsInt() : Integer.MAX_VALUE;
            recipes.put(internalName, new ModernRecipe(recipe.getId().toString(), internalName, sourceOrder));
        });
        collectMaterializedModernRecipes(recipes);
        return recipes;
    }

    private void collectMaterializedModernRecipes(Map<String, ModernRecipe> recipes) {
        Path recipeRoot = projectRoot.resolve("src").resolve("main").resolve("resources").resolve("data")
                .resolve("hbm_ntm_rebirth").resolve("recipes");
        if (!Files.isDirectory(recipeRoot)) {
            return;
        }
        try (var paths = Files.walk(recipeRoot)) {
            paths.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> collectMaterializedModernRecipe(recipeRoot, path, recipes));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read materialized modern recipes under " + recipeRoot,
                    exception);
        }
    }

    private void collectMaterializedModernRecipe(Path recipeRoot, Path path, Map<String, ModernRecipe> recipes) {
        try {
            JsonElement element = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
            if (!element.isJsonObject()) {
                return;
            }
            JsonObject json = element.getAsJsonObject();
            if (!json.has("internal_name")) {
                return;
            }
            String internalName = json.get("internal_name").getAsString();
            int sourceOrder = json.has("source_order") ? json.get("source_order").getAsInt() : Integer.MAX_VALUE;
            Path relative = recipeRoot.relativize(path);
            String idPath = relative.toString().replace('\\', '/').replaceFirst("\\.json$", "");
            recipes.putIfAbsent(internalName,
                    new ModernRecipe("hbm_ntm_rebirth:" + idPath, internalName, sourceOrder));
        } catch (RuntimeException | IOException exception) {
            throw new IllegalStateException("Failed to parse materialized modern recipe " + path, exception);
        }
    }

    private static Iterable<LegacyRecipe> extractLegacyRecipes(Path source) {
        try {
            String text = Files.readString(source, StandardCharsets.UTF_8);
            Matcher matcher = LEGACY_RECIPE_NAME.matcher(text);
            java.util.List<LegacyRecipe> recipes = new java.util.ArrayList<>();
            int order = 0;
            while (matcher.find()) {
                recipes.add(new LegacyRecipe(order++, matcher.group(1)));
            }
            return recipes;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read legacy recipe source " + source, exception);
        }
    }

    private Path legacyRecipeSource(String legacyFileName) {
        Path envRoot = envPath("HBM_LEGACY_1710_ROOT");
        Path root = envRoot != null ? envRoot : defaultLegacyRoot();
        return root.resolve("src").resolve("main").resolve("java").resolve("com").resolve("hbm")
                .resolve("inventory").resolve("recipes").resolve(legacyFileName);
    }

    private Path recipeDirectory(String recipeFolder) {
        return projectRoot.resolve("src").resolve("main").resolve("resources").resolve("data")
                .resolve("hbm_ntm_rebirth").resolve("recipes").resolve(recipeFolder);
    }

    private static int countMainResourceRecipes(Path directory) {
        if (!Files.isDirectory(directory)) {
            return 0;
        }
        try (var paths = Files.list(directory)) {
            return (int) paths.filter(path -> Files.isRegularFile(path)
                    && path.getFileName().toString().endsWith(".json")).count();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to count materialized modern recipes under " + directory,
                    exception);
        }
    }

    private Path defaultLegacyRoot() {
        return Path.of("E:", "游戏", "我的世界", "源码包", "Hbm-s-Nuclear-Tech-GIT-master");
    }

    private static Path envPath(String name) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? null : Path.of(value).toAbsolutePath().normalize();
    }

    private static String reportPath(Path path) {
        return path.toString().replace('\\', '/');
    }

    private record LegacyRecipe(int sourceOrder, String internalName) {
    }

    private record ModernRecipe(String id, String internalName, int sourceOrder) {
    }

    private record CountRecipeFamily(String machine, String legacyFileName, String legacyJsonFileName,
            String modernRecipeFolder, int legacyDefaultCount) {
    }
}
