package com.hbm.ntm.client.obj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class LegacyObjScan {
    public static ScanReport scanDirectory(Path modelRoot) {
        if (modelRoot == null || !Files.isDirectory(modelRoot)) {
            return ScanReport.EMPTY;
        }

        List<ModelReport> reports = new ArrayList<>();
        try (var paths = Files.walk(modelRoot)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".obj"))
                    .sorted()
                    .forEach(path -> reports.add(scanModel(modelRoot, path)));
        } catch (IOException | RuntimeException ignored) {
            return ScanReport.EMPTY;
        }
        return new ScanReport(reports);
    }

    public static ModelReport scanModel(Path modelRoot, Path objFile) {
        LegacyObjDiagnostics.Summary summary = LegacyObjDiagnostics.inspect(objFile);
        String relativePath = relativize(modelRoot, objFile);
        return new ModelReport(relativePath, objFile, summary, classify(summary));
    }

    private static String relativize(Path modelRoot, Path objFile) {
        if (modelRoot == null) {
            return objFile.toString().replace('\\', '/');
        }
        try {
            return modelRoot.relativize(objFile).toString().replace('\\', '/');
        } catch (IllegalArgumentException ex) {
            return objFile.toString().replace('\\', '/');
        }
    }

    private static Compatibility classify(LegacyObjDiagnostics.Summary summary) {
        if (summary.faces() == 0 || summary.hasInvalidIndices() || summary.hasOtherFaceShapes()) {
            return Compatibility.REQUIRES_REPAIR;
        }
        if (summary.hasMixedTriangleQuadFaces()) {
            return Compatibility.REQUIRES_SPLIT_OR_CUSTOM_RENDERER;
        }
        if (summary.hasFacesWithoutTextureCoordinates() || summary.hasFacesWithoutNormals()) {
            return Compatibility.NEEDS_RENDERER_REVIEW;
        }
        if (summary.hasMissingMaterialLibraries() || summary.hasUndefinedMaterialUses() || summary.hasMaterialsWithoutDiffuseTextureMaps()) {
            return Compatibility.NEEDS_MATERIAL_REVIEW;
        }
        return Compatibility.BAKE_CANDIDATE;
    }

    public enum Compatibility {
        BAKE_CANDIDATE,
        NEEDS_MATERIAL_REVIEW,
        NEEDS_RENDERER_REVIEW,
        REQUIRES_SPLIT_OR_CUSTOM_RENDERER,
        REQUIRES_REPAIR
    }

    public record ModelReport(
            String relativePath,
            Path objFile,
            LegacyObjDiagnostics.Summary summary,
            Compatibility compatibility) {
    }

    public record ScanReport(List<ModelReport> models) {
        private static final ScanReport EMPTY = new ScanReport(List.of());

        public ScanReport {
            models = Collections.unmodifiableList(new ArrayList<>(models));
        }

        public int count(Compatibility compatibility) {
            int count = 0;
            for (ModelReport model : models) {
                if (model.compatibility() == compatibility) {
                    count++;
                }
            }
            return count;
        }

        public Map<Compatibility, Integer> countsByCompatibility() {
            Map<Compatibility, Integer> counts = new EnumMap<>(Compatibility.class);
            for (Compatibility compatibility : Compatibility.values()) {
                counts.put(compatibility, count(compatibility));
            }
            return Collections.unmodifiableMap(counts);
        }

        public void writeMarkdown(Path outputFile) throws IOException {
            write(outputFile, toMarkdown());
        }

        public void writeCsv(Path outputFile) throws IOException {
            write(outputFile, toCsv());
        }

        public String toMarkdown() {
            StringBuilder builder = new StringBuilder();
            builder.append("# Legacy OBJ Scan Report\n\n");
            builder.append("Total models: ").append(models.size()).append("\n\n");
            builder.append("## Compatibility Counts\n\n");
            builder.append("| Compatibility | Count |\n");
            builder.append("| --- | ---: |\n");
            for (Compatibility compatibility : Compatibility.values()) {
                builder.append("| `").append(compatibility).append("` | ").append(count(compatibility)).append(" |\n");
            }
            builder.append("\n");
            for (Compatibility compatibility : Compatibility.values()) {
                appendCompatibilitySection(builder, compatibility);
            }
            return builder.toString();
        }

        public String toCsv() {
            StringBuilder builder = new StringBuilder();
            builder.append("path,compatibility,groups,faces,triangles,quads,otherFaces,missingMaterialLibraries,undefinedMaterialUses,facesWithoutTextureCoordinates,facesWithoutNormals,invalidVertexFaces,invalidTextureCoordinateFaces,invalidNormalFaces,mixedTriangleQuadGroups\n");
            for (ModelReport model : sortedModels()) {
                LegacyObjDiagnostics.Summary summary = model.summary();
                builder.append(csv(model.relativePath())).append(',');
                builder.append(model.compatibility()).append(',');
                builder.append(summary.groupOrder().size()).append(',');
                builder.append(summary.faces()).append(',');
                builder.append(summary.triangles()).append(',');
                builder.append(summary.quads()).append(',');
                builder.append(summary.otherFaces()).append(',');
                builder.append(summary.missingMaterialLibraries().size()).append(',');
                builder.append(summary.undefinedMaterialUses().size()).append(',');
                builder.append(summary.facesWithoutTextureCoordinates()).append(',');
                builder.append(summary.facesWithoutNormals()).append(',');
                builder.append(summary.facesWithInvalidVertexIndices()).append(',');
                builder.append(summary.facesWithInvalidTextureCoordinateIndices()).append(',');
                builder.append(summary.facesWithInvalidNormalIndices()).append(',');
                builder.append(countMixedTriangleQuadGroups(summary)).append('\n');
            }
            return builder.toString();
        }

        public List<ModelReport> byCompatibility(Compatibility compatibility) {
            return models.stream()
                    .filter(model -> model.compatibility() == compatibility)
                    .sorted(Comparator.comparing(ModelReport::relativePath))
                    .toList();
        }

        public List<ModelReport> withMissingMaterialLibraries() {
            return models.stream()
                    .filter(model -> model.summary().hasMissingMaterialLibraries())
                    .sorted(Comparator.comparing(ModelReport::relativePath))
                    .toList();
        }

        public List<ModelReport> withFacesWithoutTextureCoordinates() {
            return models.stream()
                    .filter(model -> model.summary().hasFacesWithoutTextureCoordinates())
                    .sorted(Comparator.comparing(ModelReport::relativePath))
                    .toList();
        }

        public List<ModelReport> withMixedTriangleQuadGroups() {
            return models.stream()
                    .filter(model -> model.summary().hasMixedTriangleQuadFaces())
                    .sorted(Comparator.comparing(ModelReport::relativePath))
                    .toList();
        }

        private List<ModelReport> sortedModels() {
            return models.stream()
                    .sorted(Comparator.comparing(ModelReport::relativePath))
                    .toList();
        }

        private static void write(Path outputFile, String content) throws IOException {
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputFile, content);
        }

        private void appendCompatibilitySection(StringBuilder builder, Compatibility compatibility) {
            List<ModelReport> matching = byCompatibility(compatibility);
            if (matching.isEmpty()) {
                return;
            }

            builder.append("## ").append(compatibility).append("\n\n");
            builder.append("| Path | Groups | Faces | Issues |\n");
            builder.append("| --- | ---: | ---: | --- |\n");
            for (ModelReport model : matching) {
                LegacyObjDiagnostics.Summary summary = model.summary();
                builder.append("| `").append(model.relativePath()).append("` | ");
                builder.append(summary.groupOrder().size()).append(" | ");
                builder.append(summary.faces()).append(" | ");
                builder.append(String.join("; ", describeIssues(summary))).append(" |\n");
            }
            builder.append("\n");
        }

        private static List<String> describeIssues(LegacyObjDiagnostics.Summary summary) {
            List<String> issues = new ArrayList<>();
            if (summary.hasMissingMaterialLibraries()) {
                issues.add("missing mtllib=" + summary.missingMaterialLibraries().size());
            }
            if (summary.hasUndefinedMaterialUses()) {
                issues.add("undefined usemtl=" + summary.undefinedMaterialUses().size());
            }
            if (summary.hasMaterialsWithoutDiffuseTextureMaps()) {
                issues.add("materials without map_Kd=" + summary.materialsWithoutDiffuseTextureMaps().size());
            }
            if (summary.hasFacesWithoutTextureCoordinates()) {
                issues.add("faces without UV=" + summary.facesWithoutTextureCoordinates());
            }
            if (summary.hasFacesWithoutNormals()) {
                issues.add("faces without normals=" + summary.facesWithoutNormals());
            }
            if (summary.hasMixedTriangleQuadFaces()) {
                issues.add("mixed triangle/quad groups=" + countMixedTriangleQuadGroups(summary));
            }
            if (summary.hasOtherFaceShapes()) {
                issues.add("other face shapes=" + summary.otherFaces());
            }
            if (summary.hasInvalidIndices()) {
                issues.add("invalid indices");
            }
            if (issues.isEmpty()) {
                issues.add("none");
            }
            return issues;
        }

        private static int countMixedTriangleQuadGroups(LegacyObjDiagnostics.Summary summary) {
            int count = 0;
            for (LegacyObjDiagnostics.GroupSummary group : summary.groupSummaries()) {
                if (group.hasMixedTriangleQuadFaces()) {
                    count++;
                }
            }
            return count;
        }

        private static String csv(String value) {
            if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0) {
                return value;
            }
            return '"' + value.replace("\"", "\"\"") + '"';
        }
    }

    private LegacyObjScan() {
    }
}
