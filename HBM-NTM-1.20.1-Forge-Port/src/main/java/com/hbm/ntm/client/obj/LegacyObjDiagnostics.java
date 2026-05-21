package com.hbm.ntm.client.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LegacyObjDiagnostics {
    public static Summary inspect(Path objFile) {
        try (Reader reader = Files.newBufferedReader(objFile)) {
            return inspect(reader);
        } catch (IOException | RuntimeException ex) {
            return Summary.EMPTY;
        }
    }

    public static Summary inspect(Reader source) throws IOException {
        List<String> groupOrder = new ArrayList<>();
        List<String> materialLibraries = new ArrayList<>();
        List<String> materialsUsed = new ArrayList<>();

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
        boolean hasCurrentGroup = false;
        boolean sawFaceWithoutGroup = false;

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
                if (currentLine.startsWith("g ") || currentLine.startsWith("o ")) {
                    String group = parseLegacyGroupName(currentLine);
                    if (group != null) {
                        groupOrder.add(group);
                        hasCurrentGroup = true;
                    }
                    continue;
                }
                if (currentLine.startsWith("f ")) {
                    if (!hasCurrentGroup && !sawFaceWithoutGroup) {
                        groupOrder.add("Default");
                        sawFaceWithoutGroup = true;
                    }

                    String[] faceVertices = currentLine.substring(currentLine.indexOf(' ') + 1).split(" ");
                    faces++;
                    if (faceVertices.length == 3) {
                        triangles++;
                    } else if (faceVertices.length == 4) {
                        quads++;
                    } else {
                        otherFaces++;
                    }

                    FaceKind faceKind = classifyFace(faceVertices);
                    if (faceKind.hasTextureCoordinates()) {
                        facesWithTextureCoordinates++;
                    } else {
                        facesWithoutTextureCoordinates++;
                    }
                    if (faceKind.hasNormals()) {
                        facesWithNormals++;
                    } else {
                        facesWithoutNormals++;
                    }
                }
            }
        }

        return new Summary(
                groupOrder,
                materialLibraries,
                materialsUsed,
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
                facesWithoutNormals);
    }

    static String parseLegacyGroupName(String line) {
        if (!line.matches("[go] [\\w\\d.]+")) {
            return null;
        }
        return line.substring(line.indexOf(' ') + 1);
    }

    private static FaceKind classifyFace(String[] faceVertices) {
        boolean hasTextureCoordinates = true;
        boolean hasNormals = true;
        for (String vertex : faceVertices) {
            String[] parts = vertex.split("/", -1);
            hasTextureCoordinates &= parts.length >= 2 && !parts[1].isEmpty();
            hasNormals &= parts.length >= 3 && !parts[2].isEmpty();
        }
        return new FaceKind(hasTextureCoordinates, hasNormals);
    }

    private static void addUnique(List<String> values, String value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }

    public record Summary(
            List<String> groupOrder,
            List<String> materialLibraries,
            List<String> materialsUsed,
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
            int facesWithoutNormals) {
        private static final Summary EMPTY = new Summary(List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        public Summary {
            groupOrder = Collections.unmodifiableList(new ArrayList<>(groupOrder));
            materialLibraries = Collections.unmodifiableList(new ArrayList<>(materialLibraries));
            materialsUsed = Collections.unmodifiableList(new ArrayList<>(materialsUsed));
        }

        public boolean hasMixedTriangleQuadFaces() {
            return triangles > 0 && quads > 0;
        }

        public boolean hasFacesWithoutTextureCoordinates() {
            return facesWithoutTextureCoordinates > 0;
        }

        public boolean hasFacesWithoutNormals() {
            return facesWithoutNormals > 0;
        }
    }

    private record FaceKind(boolean hasTextureCoordinates, boolean hasNormals) {
    }

    private LegacyObjDiagnostics() {
    }
}
