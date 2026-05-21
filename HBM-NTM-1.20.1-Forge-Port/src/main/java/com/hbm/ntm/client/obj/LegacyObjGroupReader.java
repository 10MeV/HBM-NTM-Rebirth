package com.hbm.ntm.client.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class LegacyObjGroupReader {
    public static List<String> readGroupOrder(ResourceLocation modelLocation) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(modelLocation);
        if (resource.isEmpty()) {
            return List.of();
        }

        try (Reader reader = resource.get().openAsReader()) {
            return readGroupOrder(reader);
        } catch (IOException | RuntimeException ex) {
            return List.of();
        }
    }

    public static List<String> readGroupOrder(Path objFile) {
        try (Reader reader = Files.newBufferedReader(objFile)) {
            return readGroupOrder(reader);
        } catch (IOException | RuntimeException ex) {
            return List.of();
        }
    }

    public static List<String> readGroupOrder(Reader source) throws IOException {
        List<String> groups = new ArrayList<>();
        boolean hasCurrentGroup = false;
        boolean sawFaceWithoutGroup = false;

        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String currentLine = line.replaceAll("\\s+", " ").trim();
                if (currentLine.isEmpty() || currentLine.startsWith("#")) {
                    continue;
                }

                if (currentLine.startsWith("f ")) {
                    if (!hasCurrentGroup && !sawFaceWithoutGroup) {
                        groups.add("Default");
                        sawFaceWithoutGroup = true;
                    }
                    continue;
                }

                if (currentLine.startsWith("g ") || currentLine.startsWith("o ")) {
                    String groupName = LegacyObjDiagnostics.parseLegacyGroupName(currentLine);
                    if (groupName != null) {
                        groups.add(groupName);
                        hasCurrentGroup = true;
                    }
                }
            }
        }

        return Collections.unmodifiableList(groups);
    }

    private LegacyObjGroupReader() {
    }
}
