package com.hbm.ntm.client.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.HbmClientConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class HbmBakedObjModelDiagnostics {
    private static final Snapshot EMPTY = new Snapshot(0, 0L, 0L, 0, 0, List.of(), List.of());
    private static volatile Snapshot snapshot = EMPTY;

    private HbmBakedObjModelDiagnostics() {
    }

    public static void reload(ResourceManager resourceManager) {
        Snapshot scanned = scan(resourceManager);
        snapshot = scanned;
        if (HbmClientConfig.renderBackendDiagnostics() && scanned.modelCount() > 0) {
            HbmNtm.LOGGER.info("[HBM RenderBackend] bakedObj resource audit: {}", scanned.summary(10));
        }
    }

    public static String summary(int limit) {
        return snapshot.summary(limit);
    }

    public static boolean hasModels() {
        return snapshot.modelCount() > 0;
    }

    private static Snapshot scan(ResourceManager resourceManager) {
        Map<ResourceLocation, Set<String>> modelBlocks = collectBlockstateModelReferences(resourceManager);
        Map<ResourceLocation, Resource> modelResources = resourceManager.listResources("models/block",
                location -> HbmNtm.MOD_ID.equals(location.getNamespace()) && location.getPath().endsWith(".json"));
        List<Entry> entries = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Set<String>> referencedModel : modelBlocks.entrySet()) {
            ResourceLocation modelLocation = referencedModel.getKey();
            Resource modelResource = modelResources.get(modelLocation);
            if (modelResource == null) {
                continue;
            }
            try (Reader reader = modelResource.openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject json = element.getAsJsonObject();
                if (!"forge:obj".equals(stringValue(json, "loader"))) {
                    continue;
                }
                ResourceLocation objLocation = objResourceLocation(stringValue(json, "model"));
                if (objLocation == null) {
                    failures.add(shortPath(modelLocation) + ":missing-model");
                    continue;
                }
                Optional<Resource> objResource = resourceManager.getResource(objLocation);
                if (objResource.isEmpty()) {
                    failures.add(shortPath(modelLocation) + ":missing-obj=" + shortPath(objLocation));
                    continue;
                }
                ObjStats objStats = parseObj(objResource.get());
                Set<String> hiddenGroups = hiddenGroups(json);
                long visibleTriangles = objStats.groups().entrySet().stream()
                        .filter(group -> !hiddenGroups.contains(group.getKey()))
                        .mapToLong(group -> group.getValue().triangles())
                        .sum();
                long visibleFaceVertices = objStats.groups().entrySet().stream()
                        .filter(group -> !hiddenGroups.contains(group.getKey()))
                        .mapToLong(group -> group.getValue().faceVertices())
                        .sum();
                int visibleGroups = (int) objStats.groups().keySet().stream()
                        .filter(group -> !hiddenGroups.contains(group))
                        .count();
                String renderType = stringValue(json, "render_type");
                Boolean automaticCulling = booleanValue(json, "automatic_culling");
                entries.add(new Entry(
                        modelLocation,
                        objLocation,
                        List.copyOf(referencedModel.getValue()),
                        visibleTriangles,
                        visibleFaceVertices,
                        visibleGroups,
                        objStats.groups().size(),
                        objStats.totalTriangles(),
                        renderType == null ? "default" : renderType,
                        automaticCulling));
            } catch (RuntimeException | IOException exception) {
                failures.add(shortPath(modelLocation) + ":" + exception.getClass().getSimpleName());
            }
        }
        entries.sort(Comparator.comparingLong(Entry::visibleTriangles).reversed()
                .thenComparing(entry -> shortPath(entry.modelLocation())));
        long totalVisibleTriangles = entries.stream().mapToLong(Entry::visibleTriangles).sum();
        long totalVisibleFaceVertices = entries.stream().mapToLong(Entry::visibleFaceVertices).sum();
        int cutoutModels = (int) entries.stream().filter(Entry::isCutout).count();
        int noAutomaticCullingModels = (int) entries.stream().filter(Entry::isAutomaticCullingDisabled).count();
        return new Snapshot(entries.size(), totalVisibleTriangles, totalVisibleFaceVertices, cutoutModels, noAutomaticCullingModels,
                List.copyOf(entries), List.copyOf(failures));
    }

    private static Map<ResourceLocation, Set<String>> collectBlockstateModelReferences(ResourceManager resourceManager) {
        Map<ResourceLocation, Set<String>> modelBlocks = new HashMap<>();
        Map<ResourceLocation, Resource> blockstates = resourceManager.listResources("blockstates",
                location -> HbmNtm.MOD_ID.equals(location.getNamespace()) && location.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : blockstates.entrySet()) {
            String blockId = blockId(entry.getKey());
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement element = JsonParser.parseReader(reader);
                collectModelReferences(element, modelLocation -> modelBlocks
                        .computeIfAbsent(modelLocation, ignored -> new HashSet<>())
                        .add(blockId));
            } catch (RuntimeException | IOException ignored) {
            }
        }
        return modelBlocks;
    }

    private static void collectModelReferences(JsonElement element, ModelReferenceSink sink) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> collectModelReferences(child, sink));
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        String model = stringValue(object, "model");
        ResourceLocation modelLocation = blockModelResourceLocation(model);
        if (modelLocation != null) {
            sink.accept(modelLocation);
        }
        object.entrySet().forEach(child -> collectModelReferences(child.getValue(), sink));
    }

    private static ObjStats parseObj(Resource resource) throws IOException {
        Map<String, GroupStats> groups = new LinkedHashMap<>();
        String currentGroup = "__default__";
        groups.put(currentGroup, GroupStats.EMPTY);
        long totalTriangles = 0L;
        try (BufferedReader reader = new BufferedReader(resource.openAsReader())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 2 || line.charAt(0) == '#') {
                    continue;
                }
                if (line.startsWith("o ") || line.startsWith("g ")) {
                    String group = firstToken(line.substring(2).trim());
                    if (!group.isEmpty()) {
                        currentGroup = group;
                        groups.putIfAbsent(currentGroup, GroupStats.EMPTY);
                    }
                    continue;
                }
                if (!line.startsWith("f ")) {
                    continue;
                }
                int vertices = tokenCount(line.substring(2));
                if (vertices < 3) {
                    continue;
                }
                long triangles = vertices - 2L;
                totalTriangles += triangles;
                GroupStats previous = groups.getOrDefault(currentGroup, GroupStats.EMPTY);
                groups.put(currentGroup, new GroupStats(
                        previous.faces() + 1L,
                        previous.triangles() + triangles,
                        previous.faceVertices() + vertices));
            }
        }
        groups.entrySet().removeIf(entry -> entry.getValue().faces() == 0L);
        if (groups.isEmpty()) {
            groups.put("__default__", GroupStats.EMPTY);
        }
        return new ObjStats(totalTriangles, groups);
    }

    private static Set<String> hiddenGroups(JsonObject json) {
        JsonElement visibility = json.get("visibility");
        if (visibility == null || !visibility.isJsonObject()) {
            return Set.of();
        }
        Set<String> hidden = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : visibility.getAsJsonObject().entrySet()) {
            JsonElement value = entry.getValue();
            if (value == null || !value.isJsonPrimitive() || !value.getAsBoolean()) {
                hidden.add(entry.getKey());
            }
        }
        return hidden;
    }

    private static ResourceLocation blockModelResourceLocation(String model) {
        ResourceLocation location = parseResourceLocation(model);
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        if (!path.startsWith("models/")) {
            path = "models/" + path;
        }
        if (!path.endsWith(".json")) {
            path += ".json";
        }
        return new ResourceLocation(location.getNamespace(), path);
    }

    private static ResourceLocation objResourceLocation(String model) {
        ResourceLocation location = parseResourceLocation(model);
        if (location == null) {
            return null;
        }
        String path = location.getPath();
        if (!path.endsWith(".obj")) {
            path += ".obj";
        }
        return new ResourceLocation(location.getNamespace(), path);
    }

    private static ResourceLocation parseResourceLocation(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String namespace = HbmNtm.MOD_ID;
        String path = value;
        int separator = value.indexOf(':');
        if (separator >= 0) {
            namespace = value.substring(0, separator);
            path = value.substring(separator + 1);
        }
        try {
            return new ResourceLocation(namespace, path);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static String stringValue(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
                ? value.getAsString()
                : null;
    }

    private static Boolean booleanValue(JsonObject json, String key) {
        JsonElement value = json.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()
                ? value.getAsBoolean()
                : null;
    }

    private static String blockId(ResourceLocation blockstateLocation) {
        String path = blockstateLocation.getPath();
        if (path.startsWith("blockstates/")) {
            path = path.substring("blockstates/".length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        return path;
    }

    private static String firstToken(String value) {
        int index = 0;
        while (index < value.length() && !Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return value.substring(0, index);
    }

    private static int tokenCount(String value) {
        int count = 0;
        boolean inToken = false;
        for (int i = 0; i < value.length(); i++) {
            boolean whitespace = Character.isWhitespace(value.charAt(i));
            if (whitespace) {
                inToken = false;
            } else if (!inToken) {
                count++;
                inToken = true;
            }
        }
        return count;
    }

    private static String shortPath(ResourceLocation location) {
        if (location == null) {
            return "-";
        }
        String path = location.getPath();
        if (path.startsWith("models/block/")) {
            path = path.substring("models/block/".length());
        }
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - ".json".length());
        }
        if (path.endsWith(".obj")) {
            path = path.substring(0, path.length() - ".obj".length());
        }
        return path;
    }

    private interface ModelReferenceSink {
        void accept(ResourceLocation modelLocation);
    }

    private record ObjStats(long totalTriangles, Map<String, GroupStats> groups) {
    }

    private record GroupStats(long faces, long triangles, long faceVertices) {
        private static final GroupStats EMPTY = new GroupStats(0L, 0L, 0L);
    }

    private record Entry(
            ResourceLocation modelLocation,
            ResourceLocation objLocation,
            List<String> blocks,
            long visibleTriangles,
            long visibleFaceVertices,
            int visibleGroups,
            int totalGroups,
            long totalTriangles,
            String renderType,
            Boolean automaticCulling) {
        private boolean isCutout() {
            return renderType.toLowerCase(Locale.ROOT).contains("cutout");
        }

        private boolean isAutomaticCullingDisabled() {
            return Boolean.FALSE.equals(automaticCulling);
        }

        private String summary() {
            String culling = automaticCulling == null ? "autoCull=default" : "autoCull=" + automaticCulling;
            String blockList = blocks.isEmpty() ? "-" : String.join("|", blocks);
            return String.format(Locale.ROOT, "%s:%dt/%dfv:%s:%s:%dg/%dg:%s",
                    blockList,
                    visibleTriangles,
                    visibleFaceVertices,
                    renderType,
                    culling,
                    visibleGroups,
                    totalGroups,
                    shortPath(modelLocation));
        }
    }

    private record Snapshot(
            int modelCount,
            long totalVisibleTriangles,
            long totalVisibleFaceVertices,
            int cutoutModels,
            int noAutomaticCullingModels,
            List<Entry> topEntries,
            List<String> failures) {
        private String summary(int limit) {
            if (modelCount == 0) {
                return "models=0";
            }
            String top = topEntries.stream()
                    .limit(Math.max(1, limit))
                    .map(Entry::summary)
                    .collect(Collectors.joining(";"));
            String failureSummary = failures.isEmpty()
                    ? ""
                    : ",fail=" + failures.stream().limit(5).collect(Collectors.joining("|"));
            return String.format(Locale.ROOT, "models=%d,tris=%d,fv=%d,cutout=%d,noAutoCull=%d,top=%s%s",
                    modelCount,
                    totalVisibleTriangles,
                    totalVisibleFaceVertices,
                    cutoutModels,
                    noAutomaticCullingModels,
                    top,
                    failureSummary);
        }
    }
}
