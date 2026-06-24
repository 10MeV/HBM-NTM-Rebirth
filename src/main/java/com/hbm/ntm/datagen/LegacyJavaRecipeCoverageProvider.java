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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyJavaRecipeCoverageProvider implements DataProvider {
    private static final Pattern LEGACY_RECIPE_NAME =
            Pattern.compile("new\\s+(?:GenericRecipe|FusionRecipe|PUREXRecipe)\\(\"([^\"]+)\"\\)");
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
    private static final Map<String, String> UNMIGRATED_OUTPUT_BLOCKERS = Map.of(
            "ass.excavator", "output machine_excavator is not registered in the clean port; requires the excavator machine BE/UI/renderer/runtime slice",
            "ass.chopper", "output spawn_chopper and the Hunter Chopper spawn/entity item chain are not registered in the clean port",
            "ass.ballsotron", "output spawn_worm and the mechanical worm spawn/entity item chain are not registered in the clean port",
            "ass.minenaval", "output mine_naval block/runtime is not registered in the clean port",
            "ass.levibomb", "output float_bomb block/runtime is not registered in the clean port",
            "ass.endobomb", "output therm_endo block/runtime is not registered in the clean port",
            "ass.exobomb", "output therm_exo block/runtime is not registered in the clean port",
            "ass.digimemer", "legacy recipe is conditional Mekanism integration outputting external Mekanism:MachineBlock meta 4; no modern HBM output anchor",
            "purex.watznaqadah", "legacy PUREX recipe is conditional on OreDictionary nuggetNaquadria; the clean port has no source-backed Naquadah/Naquadria nugget output anchor",
            "purex.watznaqadria", "legacy PUREX recipe is conditional on OreDictionary nuggetNaquadria; the clean port has no source-backed Naquadah/Naquadria nugget output anchor");

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

        Map<String, ModernRecipe> modernRecipes = collectModernRecipes();
        addMachineReport(machines, "chemical_plant", "ChemicalPlantRecipes.java", "chem.", modernRecipes);
        addMachineReport(machines, "assembly_machine", "AssemblyMachineRecipes.java", "ass.", modernRecipes);
        addMachineReport(machines, "purex", "PUREXRecipes.java", "purex.", modernRecipes);
        addMachineReport(machines, "fusion_reactor", "FusionRecipes.java", "fus.", modernRecipes);

        return DataProvider.saveStable(cachedOutput, root, reportPath);
    }

    @Override
    public String getName() {
        return "HBM legacy Java machine recipe coverage";
    }

    private void addMachineReport(JsonArray machines, String machine, String legacyFileName, String prefix,
            Map<String, ModernRecipe> modernRecipes) {
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
            legacyCount++;
            ModernRecipe modern = modernRecipes.get(recipe.internalName());
            JsonObject entry = new JsonObject();
            entry.addProperty("source_order", recipe.sourceOrder());
            entry.addProperty("internal_name", recipe.internalName());
            if (modern == null) {
                if (DECORATIVE_DOOR_RECIPE_EXCLUSIONS.contains(recipe.internalName())) {
                    entry.addProperty("reason", "old door/seal-door recipes are decorative/deferred by project rule");
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
                .count();
        report.addProperty("legacy_count", legacyCount);
        report.addProperty("modern_count", modernCount);
        report.addProperty("present_count", present.size());
        report.addProperty("excluded_count", excluded.size());
        report.addProperty("blocked_count", blocked.size());
        report.addProperty("missing_count", missing.size());
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
}
