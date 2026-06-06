package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.HbmFluidStack;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class LegacyGenericRecipeImporter {
    private LegacyGenericRecipeImporter() {
    }

    public static List<ImportedRecipe> read(GenericMachineRecipe.Machine machine, ResourceLocation folder, Reader reader) {
        return readWithReport(machine, folder, reader).recipes();
    }

    public static List<ImportedRecipe> read(String legacyFileName, Reader reader) {
        return readWithReport(legacyFileName, reader).recipes();
    }

    public static ImportReport readWithReport(GenericMachineRecipe.Machine machine, ResourceLocation folder, Reader reader) {
        return readWithReport(LegacySerializableRecipeHandlers.MANUAL_SOURCE, LegacySerializableRecipeHandlers.MANUAL_SOURCE,
                machine, folder, reader, false);
    }

    public static ImportReport readWithReport(String legacyFileName, Reader reader) {
        LegacySerializableRecipeHandlers.Handler serializableHandler =
                LegacySerializableRecipeHandlers.requireSupportedGeneric(legacyFileName);
        LegacyGenericRecipeHandlers.Handler handler = LegacyGenericRecipeHandlers.requireSupported(
                serializableHandler.legacyFileName());
        return readWithReport(serializableHandler.legacyFileName(), serializableHandler.legacyClassName(),
                handler.requireMachine(), handler.outputFolder(), reader, false);
    }

    public static ImportReport readLenientWithReport(GenericMachineRecipe.Machine machine, ResourceLocation folder, Reader reader) {
        return readWithReport(LegacySerializableRecipeHandlers.MANUAL_SOURCE, LegacySerializableRecipeHandlers.MANUAL_SOURCE,
                machine, folder, reader, true);
    }

    public static ImportReport readLenientWithReport(String legacyFileName, Reader reader) {
        LegacySerializableRecipeHandlers.Handler serializableHandler =
                LegacySerializableRecipeHandlers.requireSupportedGeneric(legacyFileName);
        LegacyGenericRecipeHandlers.Handler handler = LegacyGenericRecipeHandlers.requireSupported(
                serializableHandler.legacyFileName());
        return readWithReport(serializableHandler.legacyFileName(), serializableHandler.legacyClassName(),
                handler.requireMachine(), handler.outputFolder(), reader, true);
    }

    private static ImportReport readWithReport(String legacyFileName, String legacyClassName,
            GenericMachineRecipe.Machine machine, ResourceLocation folder, Reader reader, boolean lenient) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray recipes = root.getAsJsonArray("recipes");
        if (recipes == null) {
            throw new JsonSyntaxException("Legacy generic recipe file is missing recipes array");
        }

        List<ImportedRecipe> imported = new ArrayList<>();
        Map<ResourceLocation, Integer> usedIds = new LinkedHashMap<>();
        Map<String, Integer> usedInternalNames = new LinkedHashMap<>();
        Map<LegacyBlueprintPools.Kind, Integer> poolKindCounts = new EnumMap<>(LegacyBlueprintPools.Kind.class);
        List<IdRemap> idRemaps = new ArrayList<>();
        List<DuplicateInternalName> duplicateInternalNames = new ArrayList<>();
        List<ImportFailure> failures = new ArrayList<>();
        for (int sourceIndex = 0; sourceIndex < recipes.size(); sourceIndex++) {
            JsonElement element = recipes.get(sourceIndex);
            if (element == null || element.isJsonNull()) {
                continue;
            }
            JsonObject legacy = element.getAsJsonObject();
            String internalName = LegacyGenericRecipeFormat.internalName(folder, legacy);
            int internalNameCount = usedInternalNames.merge(internalName, 1, Integer::sum);
            if (internalNameCount > 1) {
                duplicateInternalNames.add(new DuplicateInternalName(internalName, internalNameCount));
            }
            for (String pool : LegacyGenericRecipeFormat.readPools(legacy)) {
                poolKindCounts.merge(LegacyBlueprintPools.kind(pool), 1, Integer::sum);
            }
            ResourceLocation baseId = importedId(folder, internalName, imported.size());
            ResourceLocation id = uniqueId(baseId, usedIds);
            if (!id.equals(baseId)) {
                idRemaps.add(new IdRemap(internalName, baseId, id));
            }
            try {
                JsonObject modern = toModernJson(machine, id, legacy);
                modern.addProperty("source_order", sourceIndex);
                imported.add(new ImportedRecipe(id, modern));
            } catch (RuntimeException exception) {
                if (!lenient) {
                    throw exception;
                }
                failures.add(new ImportFailure(sourceIndex, internalName, baseId, exception.getMessage()));
            }
        }
        return new ImportReport(legacyFileName, legacyClassName, machine, folder, recipes.size(), List.copyOf(imported), List.copyOf(idRemaps),
                List.copyOf(duplicateInternalNames), Map.copyOf(poolKindCounts), List.copyOf(failures));
    }

    public static JsonObject toModernJson(GenericMachineRecipe.Machine machine, ResourceLocation id, JsonObject legacy) {
        List<HbmIngredient> itemInputs = LegacyGenericRecipeFormat.readItemInputs(legacy);
        List<HbmFluidStack> fluidInputs = LegacyGenericRecipeFormat.readFluidInputs(legacy);
        List<HbmItemOutput> itemOutputs = LegacyGenericRecipeFormat.readItemOutputs(legacy);
        List<HbmFluidStack> fluidOutputs = LegacyGenericRecipeFormat.readFluidOutputs(legacy);
        machine.validateRecipeLimits(id, itemInputs.size(), fluidInputs.size(), itemOutputs.size(), fluidOutputs.size());
        validateItemInputStackLimits(id, itemInputs);

        JsonObject modern = new JsonObject();
        modern.addProperty("type", machine.serializerId().toString());
        modern.addProperty("internal_name", LegacyGenericRecipeFormat.internalName(id, legacy));
        if (legacy.has("duration")) {
            modern.addProperty("duration", legacy.get("duration").getAsInt());
        }
        if (legacy.has("power")) {
            modern.addProperty("power", legacy.get("power").getAsLong());
        }

        JsonArray inputItems = new JsonArray();
        itemInputs.forEach(input -> inputItems.add(input.toJson()));
        modern.add("input_items", inputItems);

        JsonArray inputFluids = new JsonArray();
        fluidInputs.forEach(stack -> inputFluids.add(fluidJson(stack)));
        modern.add("input_fluids", inputFluids);

        JsonArray outputItems = new JsonArray();
        itemOutputs.forEach(output -> outputItems.add(output.toJson()));
        modern.add("output_items", outputItems);

        JsonArray outputFluids = new JsonArray();
        fluidOutputs.forEach(stack -> outputFluids.add(fluidJson(stack)));
        modern.add("output_fluids", outputFluids);

        JsonArray pools = new JsonArray();
        LegacyGenericRecipeFormat.readPools(legacy).forEach(pools::add);
        modern.add("pools", pools);

        if (!LegacyGenericRecipeFormat.readIcon(legacy).isEmpty()) {
            modern.add("icon", HbmItemOutput.of(LegacyGenericRecipeFormat.readIcon(legacy)).toJson());
        }
        if (LegacyGenericRecipeFormat.readCustomLocalization(legacy)) {
            modern.addProperty("custom_localization", true);
        }
        String autoSwitchGroup = LegacyGenericRecipeFormat.readAutoSwitchGroup(legacy);
        if (autoSwitchGroup != null) {
            modern.addProperty("auto_switch_group", autoSwitchGroup);
        }
        String nameWrapper = LegacyGenericRecipeFormat.readNameWrapper(legacy);
        if (nameWrapper != null) {
            modern.addProperty("name_wrapper", nameWrapper);
        }
        GenericMachineRecipeExtraData.fromJson(legacy).writeToJson(modern);
        return modern;
    }

    private static void validateItemInputStackLimits(ResourceLocation id, List<HbmIngredient> itemInputs) {
        for (HbmIngredient input : itemInputs) {
            if (input.exceedsStackLimit()) {
                int limit = input.stackLimit().orElse(64);
                throw new JsonSyntaxException("HBM machine recipe " + id + " input count exceeds stack limit: "
                        + input.count() + " > " + limit);
            }
        }
    }

    private static JsonObject fluidJson(HbmFluidStack stack) {
        JsonObject object = new JsonObject();
        object.addProperty("fluid", stack.type().getName());
        object.addProperty("amount", stack.amount());
        if (stack.pressure() != 0) {
            object.addProperty("pressure", stack.pressure());
        }
        return object;
    }

    private static ResourceLocation importedId(ResourceLocation folder, String internalName, int index) {
        String path = internalName.toLowerCase(Locale.ROOT)
                .replace(':', '/')
                .replaceAll("[^a-z0-9_./-]", "_")
                .replaceAll("/+", "/");
        if (path.isBlank()) {
            path = "recipe_" + index;
        }
        return new ResourceLocation(folder.getNamespace(), folder.getPath() + "/" + path);
    }

    private static ResourceLocation uniqueId(ResourceLocation baseId, Map<ResourceLocation, Integer> usedIds) {
        Integer previous = usedIds.putIfAbsent(baseId, 1);
        if (previous == null) {
            return baseId;
        }

        int next = previous + 1;
        ResourceLocation candidate;
        do {
            candidate = new ResourceLocation(baseId.getNamespace(), baseId.getPath() + "_" + next);
            next++;
        } while (usedIds.containsKey(candidate));
        usedIds.put(baseId, next - 1);
        usedIds.put(candidate, 1);
        return candidate;
    }

    public record ImportReport(String legacyFileName, String legacyClassName, GenericMachineRecipe.Machine machine,
                               ResourceLocation outputFolder, int sourceRecipeCount, List<ImportedRecipe> recipes, List<IdRemap> idRemaps,
                               List<DuplicateInternalName> duplicateInternalNames,
                               Map<LegacyBlueprintPools.Kind, Integer> poolKindCounts,
                               List<ImportFailure> failures) {
        public boolean hasIdRemaps() {
            return !idRemaps.isEmpty();
        }

        public boolean hasDuplicateInternalNames() {
            return !duplicateInternalNames.isEmpty();
        }

        public boolean hasFailures() {
            return !failures.isEmpty();
        }

        public int poolCount(LegacyBlueprintPools.Kind kind) {
            return poolKindCounts.getOrDefault(kind, 0);
        }

        public int pooledRecipeReferences() {
            return poolKindCounts.values().stream().mapToInt(Integer::intValue).sum();
        }

        public int skippedRecipeCount() {
            return sourceRecipeCount - recipes.size();
        }
    }

    public record ImportedRecipe(ResourceLocation id, JsonObject json) {
    }

    public record IdRemap(String internalName, ResourceLocation requestedId, ResourceLocation importedId) {
    }

    public record DuplicateInternalName(String internalName, int occurrence) {
    }

    public record ImportFailure(int sourceIndex, String internalName, ResourceLocation requestedId, String message) {
    }
}
