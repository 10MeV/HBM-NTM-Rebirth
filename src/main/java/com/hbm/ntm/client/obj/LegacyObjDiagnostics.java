package com.hbm.ntm.client.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class LegacyObjDiagnostics {
    public static Summary inspect(Path objFile) {
        try (Reader reader = Files.newBufferedReader(objFile)) {
            Summary summary = inspect(reader);
            return withMaterialFileStatus(summary, objFile.getParent());
        } catch (IOException | RuntimeException ex) {
            return Summary.EMPTY;
        }
    }

    public static Summary inspect(Reader source) throws IOException {
        List<String> groupOrder = new ArrayList<>();
        List<String> materialLibraries = new ArrayList<>();
        List<String> materialsUsed = new ArrayList<>();
        Set<String> undefinedMaterialUses = new LinkedHashSet<>();
        Set<String> declaredMaterials = new LinkedHashSet<>();
        List<MaterialTexture> diffuseTextureMaps = new ArrayList<>();
        String currentMaterial = null;
        List<GroupStatsBuilder> groupStats = new ArrayList<>();
        GroupStatsBuilder currentGroup = null;

        int vertices = 0;
        int textureCoordinates = 0;
        int vertexNormals = 0;
        int faces = 0;
        int triangles = 0;
        int quads = 0;
        int otherFaces = 0;
        int facesWithTextureCoordinates = 0;
        int facesWithoutTextureCoordinates = 0;
        int facesWithNormals = 0;
        int facesWithoutNormals = 0;
        int facesWithInvalidVertexIndices = 0;
        int facesWithInvalidTextureCoordinateIndices = 0;
        int facesWithInvalidNormalIndices = 0;

        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String currentLine = line.replaceAll("\\s+", " ").trim();
                if (currentLine.isEmpty() || currentLine.startsWith("#")) {
                    continue;
                }

                if (currentLine.startsWith("v ")) {
                    vertices++;
                    continue;
                }
                if (currentLine.startsWith("vt ")) {
                    textureCoordinates++;
                    continue;
                }
                if (currentLine.startsWith("vn ")) {
                    vertexNormals++;
                    continue;
                }
                if (currentLine.startsWith("mtllib ")) {
                    addUnique(materialLibraries, currentLine.substring("mtllib ".length()));
                    continue;
                }
                if (currentLine.startsWith("usemtl ")) {
                    addUnique(materialsUsed, currentLine.substring("usemtl ".length()));
                    continue;
                }
                if (currentLine.startsWith("newmtl ")) {
                    currentMaterial = currentLine.substring("newmtl ".length());
                    declaredMaterials.add(currentMaterial);
                    continue;
                }
                if (currentLine.startsWith("map_Kd ") && currentMaterial != null) {
                    addUnique(diffuseTextureMaps, new MaterialTexture(currentMaterial, currentLine.substring("map_Kd ".length())));
                    continue;
                }
                if (currentLine.startsWith("g ") || currentLine.startsWith("o ")) {
                    String group = parseLegacyGroupName(currentLine);
                    if (group != null) {
                        groupOrder.add(group);
                        currentGroup = new GroupStatsBuilder(group);
                        groupStats.add(currentGroup);
                    }
                    continue;
                }
                if (currentLine.startsWith("f ")) {
                    if (currentGroup == null) {
                        groupOrder.add("Default");
                        currentGroup = new GroupStatsBuilder("Default");
                        groupStats.add(currentGroup);
                    }

                    String[] faceVertices = currentLine.substring(currentLine.indexOf(' ') + 1).split(" ");
                    faces++;
                    if (faceVertices.length == 3) {
                        triangles++;
                        currentGroup.triangles++;
                    } else if (faceVertices.length == 4) {
                        quads++;
                        currentGroup.quads++;
                    } else {
                        otherFaces++;
                        currentGroup.otherFaces++;
                    }
                    currentGroup.faces++;

                    FaceKind faceKind = classifyFace(faceVertices, vertices, textureCoordinates, vertexNormals);
                    if (faceKind.hasTextureCoordinates()) {
                        facesWithTextureCoordinates++;
                    } else {
                        facesWithoutTextureCoordinates++;
                        currentGroup.facesWithoutTextureCoordinates++;
                    }
                    if (faceKind.hasNormals()) {
                        facesWithNormals++;
                    } else {
                        facesWithoutNormals++;
                        currentGroup.facesWithoutNormals++;
                    }
                    if (faceKind.hasInvalidVertexIndex()) {
                        facesWithInvalidVertexIndices++;
                        currentGroup.facesWithInvalidVertexIndices++;
                    }
                    if (faceKind.hasInvalidTextureCoordinateIndex()) {
                        facesWithInvalidTextureCoordinateIndices++;
                        currentGroup.facesWithInvalidTextureCoordinateIndices++;
                    }
                    if (faceKind.hasInvalidNormalIndex()) {
                        facesWithInvalidNormalIndices++;
                        currentGroup.facesWithInvalidNormalIndices++;
                    }
                }
            }
        }

        for (String material : materialsUsed) {
            if (!declaredMaterials.isEmpty() && !declaredMaterials.contains(material)) {
                undefinedMaterialUses.add(material);
            }
        }

        return new Summary(
                groupOrder,
                materialLibraries,
                materialsUsed,
                List.of(),
                new ArrayList<>(declaredMaterials),
                new ArrayList<>(undefinedMaterialUses),
                diffuseTextureMaps,
                findMaterialsWithoutDiffuseMaps(declaredMaterials, diffuseTextureMaps),
                buildGroupSummaries(groupStats),
                vertices,
                textureCoordinates,
                vertexNormals,
                faces,
                triangles,
                quads,
                otherFaces,
                facesWithTextureCoordinates,
                facesWithoutTextureCoordinates,
                facesWithNormals,
                facesWithoutNormals,
                facesWithInvalidVertexIndices,
                facesWithInvalidTextureCoordinateIndices,
                facesWithInvalidNormalIndices);
    }

    static String parseLegacyGroupName(String line) {
        if (!line.matches("[go] [\\w\\d.]+")) {
            return null;
        }
        return line.substring(line.indexOf(' ') + 1);
    }

    private static FaceKind classifyFace(String[] faceVertices, int vertices, int textureCoordinates, int vertexNormals) {
        boolean hasTextureCoordinates = true;
        boolean hasNormals = true;
        boolean hasInvalidVertexIndex = false;
        boolean hasInvalidTextureCoordinateIndex = false;
        boolean hasInvalidNormalIndex = false;
        for (String vertex : faceVertices) {
            String[] parts = vertex.split("/", -1);
            hasTextureCoordinates &= parts.length >= 2 && !parts[1].isEmpty();
            hasNormals &= parts.length >= 3 && !parts[2].isEmpty();
            hasInvalidVertexIndex |= isInvalidIndex(parts, 0, vertices);
            if (parts.length >= 2 && !parts[1].isEmpty()) {
                hasInvalidTextureCoordinateIndex |= isInvalidIndex(parts, 1, textureCoordinates);
            }
            if (parts.length >= 3 && !parts[2].isEmpty()) {
                hasInvalidNormalIndex |= isInvalidIndex(parts, 2, vertexNormals);
            }
        }
        return new FaceKind(hasTextureCoordinates, hasNormals, hasInvalidVertexIndex, hasInvalidTextureCoordinateIndex, hasInvalidNormalIndex);
    }

    private static boolean isInvalidIndex(String[] parts, int index, int size) {
        int parsed = parseIndex(parts, index);
        return parsed <= 0 || parsed > size;
    }

    private static int parseIndex(String[] parts, int index) {
        if (parts.length <= index || parts[index].isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static <T> void addUnique(List<T> values, T value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }

    private static Summary withMaterialFileStatus(Summary summary, Path modelDirectory) {
        if (modelDirectory == null || summary.materialLibraries().isEmpty()) {
            return summary;
        }
        List<String> missing = new ArrayList<>();
        Set<String> declaredMaterials = new LinkedHashSet<>(summary.declaredMaterials());
        List<MaterialTexture> diffuseTextureMaps = new ArrayList<>(summary.diffuseTextureMaps());
        for (String library : summary.materialLibraries()) {
            Path materialPath = modelDirectory.resolve(library);
            if (!Files.exists(materialPath)) {
                missing.add(library);
                continue;
            }
            MaterialFileStatus materialStatus = readMaterialFileStatus(materialPath);
            declaredMaterials.addAll(materialStatus.declaredMaterials());
            diffuseTextureMaps.addAll(materialStatus.diffuseTextureMaps());
        }
        List<String> undefined = new ArrayList<>();
        for (String material : summary.materialsUsed()) {
            if (!declaredMaterials.isEmpty() && !declaredMaterials.contains(material)) {
                undefined.add(material);
            }
        }
        return summary.withMaterialStatus(
                missing,
                new ArrayList<>(declaredMaterials),
                undefined,
                diffuseTextureMaps,
                findMaterialsWithoutDiffuseMaps(declaredMaterials, diffuseTextureMaps));
    }

    private static MaterialFileStatus readMaterialFileStatus(Path materialPath) {
        Set<String> declaredMaterials = new LinkedHashSet<>();
        List<MaterialTexture> diffuseTextureMaps = new ArrayList<>();
        String currentMaterial = null;
        try (BufferedReader reader = Files.newBufferedReader(materialPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String currentLine = line.replaceAll("\\s+", " ").trim();
                if (currentLine.startsWith("newmtl ")) {
                    currentMaterial = currentLine.substring("newmtl ".length());
                    declaredMaterials.add(currentMaterial);
                    continue;
                }
                if (currentLine.startsWith("map_Kd ") && currentMaterial != null) {
                    addUnique(diffuseTextureMaps, new MaterialTexture(currentMaterial, currentLine.substring("map_Kd ".length())));
                }
            }
        } catch (IOException | RuntimeException ignored) {
            return MaterialFileStatus.EMPTY;
        }
        return new MaterialFileStatus(new ArrayList<>(declaredMaterials), diffuseTextureMaps);
    }

    private static List<String> findMaterialsWithoutDiffuseMaps(Set<String> declaredMaterials, List<MaterialTexture> diffuseTextureMaps) {
        if (declaredMaterials.isEmpty()) {
            return List.of();
        }
        Set<String> mappedMaterials = new LinkedHashSet<>();
        for (MaterialTexture textureMap : diffuseTextureMaps) {
            mappedMaterials.add(textureMap.material());
        }
        List<String> missing = new ArrayList<>();
        for (String material : declaredMaterials) {
            if (!mappedMaterials.contains(material)) {
                missing.add(material);
            }
        }
        return missing;
    }

    private static List<GroupSummary> buildGroupSummaries(List<GroupStatsBuilder> groupStats) {
        List<GroupSummary> summaries = new ArrayList<>();
        for (GroupStatsBuilder group : groupStats) {
            summaries.add(group.toSummary());
        }
        return summaries;
    }

    public record Summary(
            List<String> groupOrder,
            List<String> materialLibraries,
            List<String> materialsUsed,
            List<String> missingMaterialLibraries,
            List<String> declaredMaterials,
            List<String> undefinedMaterialUses,
            List<MaterialTexture> diffuseTextureMaps,
            List<String> materialsWithoutDiffuseTextureMaps,
            List<GroupSummary> groupSummaries,
            int vertices,
            int textureCoordinates,
            int vertexNormals,
            int faces,
            int triangles,
            int quads,
            int otherFaces,
            int facesWithTextureCoordinates,
            int facesWithoutTextureCoordinates,
            int facesWithNormals,
            int facesWithoutNormals,
            int facesWithInvalidVertexIndices,
            int facesWithInvalidTextureCoordinateIndices,
            int facesWithInvalidNormalIndices) {
        private static final Summary EMPTY = new Summary(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        public Summary {
            groupOrder = Collections.unmodifiableList(new ArrayList<>(groupOrder));
            materialLibraries = Collections.unmodifiableList(new ArrayList<>(materialLibraries));
            materialsUsed = Collections.unmodifiableList(new ArrayList<>(materialsUsed));
            missingMaterialLibraries = Collections.unmodifiableList(new ArrayList<>(missingMaterialLibraries));
            declaredMaterials = Collections.unmodifiableList(new ArrayList<>(declaredMaterials));
            undefinedMaterialUses = Collections.unmodifiableList(new ArrayList<>(undefinedMaterialUses));
            diffuseTextureMaps = Collections.unmodifiableList(new ArrayList<>(diffuseTextureMaps));
            materialsWithoutDiffuseTextureMaps = Collections.unmodifiableList(new ArrayList<>(materialsWithoutDiffuseTextureMaps));
            groupSummaries = Collections.unmodifiableList(new ArrayList<>(groupSummaries));
        }

        private Summary withMaterialStatus(
                List<String> missingMaterialLibraries,
                List<String> declaredMaterials,
                List<String> undefinedMaterialUses,
                List<MaterialTexture> diffuseTextureMaps,
                List<String> materialsWithoutDiffuseTextureMaps) {
            return new Summary(
                    groupOrder,
                    materialLibraries,
                    materialsUsed,
                    missingMaterialLibraries,
                    declaredMaterials,
                    undefinedMaterialUses,
                    diffuseTextureMaps,
                    materialsWithoutDiffuseTextureMaps,
                    groupSummaries,
                    vertices,
                    textureCoordinates,
                    vertexNormals,
                    faces,
                    triangles,
                    quads,
                    otherFaces,
                    facesWithTextureCoordinates,
                    facesWithoutTextureCoordinates,
                    facesWithNormals,
                    facesWithoutNormals,
                    facesWithInvalidVertexIndices,
                    facesWithInvalidTextureCoordinateIndices,
                    facesWithInvalidNormalIndices);
        }

        public boolean hasMixedTriangleQuadFaces() {
            for (GroupSummary groupSummary : groupSummaries) {
                if (groupSummary.hasMixedTriangleQuadFaces()) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasOtherFaceShapes() {
            return otherFaces > 0;
        }

        public boolean hasFacesWithoutTextureCoordinates() {
            return facesWithoutTextureCoordinates > 0;
        }

        public boolean hasFacesWithoutNormals() {
            return facesWithoutNormals > 0;
        }

        public boolean hasInvalidIndices() {
            return facesWithInvalidVertexIndices > 0 || facesWithInvalidTextureCoordinateIndices > 0 || facesWithInvalidNormalIndices > 0;
        }

        public boolean hasMissingMaterialLibraries() {
            return !missingMaterialLibraries.isEmpty();
        }

        public boolean hasUndefinedMaterialUses() {
            return !undefinedMaterialUses.isEmpty();
        }

        public boolean hasMaterialsWithoutDiffuseTextureMaps() {
            return !materialsWithoutDiffuseTextureMaps.isEmpty();
        }
    }

    public record MaterialTexture(String material, String texture) {
    }

    public record GroupSummary(
            String name,
            int faces,
            int triangles,
            int quads,
            int otherFaces,
            int facesWithoutTextureCoordinates,
            int facesWithoutNormals,
            int facesWithInvalidVertexIndices,
            int facesWithInvalidTextureCoordinateIndices,
            int facesWithInvalidNormalIndices) {
        public boolean hasMixedTriangleQuadFaces() {
            return triangles > 0 && quads > 0;
        }

        public boolean hasOtherFaceShapes() {
            return otherFaces > 0;
        }

        public boolean hasFacesWithoutTextureCoordinates() {
            return facesWithoutTextureCoordinates > 0;
        }

        public boolean hasFacesWithoutNormals() {
            return facesWithoutNormals > 0;
        }

        public boolean hasInvalidIndices() {
            return facesWithInvalidVertexIndices > 0 || facesWithInvalidTextureCoordinateIndices > 0 || facesWithInvalidNormalIndices > 0;
        }
    }

    private record MaterialFileStatus(List<String> declaredMaterials, List<MaterialTexture> diffuseTextureMaps) {
        private static final MaterialFileStatus EMPTY = new MaterialFileStatus(List.of(), List.of());
    }

    private static final class GroupStatsBuilder {
        private final String name;
        private int faces;
        private int triangles;
        private int quads;
        private int otherFaces;
        private int facesWithoutTextureCoordinates;
        private int facesWithoutNormals;
        private int facesWithInvalidVertexIndices;
        private int facesWithInvalidTextureCoordinateIndices;
        private int facesWithInvalidNormalIndices;

        private GroupStatsBuilder(String name) {
            this.name = name;
        }

        private GroupSummary toSummary() {
            return new GroupSummary(
                    name,
                    faces,
                    triangles,
                    quads,
                    otherFaces,
                    facesWithoutTextureCoordinates,
                    facesWithoutNormals,
                    facesWithInvalidVertexIndices,
                    facesWithInvalidTextureCoordinateIndices,
                    facesWithInvalidNormalIndices);
        }
    }

    private record FaceKind(
            boolean hasTextureCoordinates,
            boolean hasNormals,
            boolean hasInvalidVertexIndex,
            boolean hasInvalidTextureCoordinateIndex,
            boolean hasInvalidNormalIndex) {
    }

    private LegacyObjDiagnostics() {
    }
}
