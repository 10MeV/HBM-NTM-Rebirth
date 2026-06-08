package com.hbm.ntm.fluid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HbmFluidContainerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "hbmFluidContainers.json";
    private static volatile LoadReport report = new LoadReport(false, false, 0, 0, List.of());

    public static LoadReport initialize(Path configDir) {
        Path hbmDir = configDir.resolve("hbm");
        Path config = hbmDir.resolve(CONFIG_FILE);
        boolean createdDefault = false;
        try {
            Files.createDirectories(hbmDir);
            if (Files.notExists(config)) {
                writeDefault(config);
                createdDefault = true;
            }
            try (Reader reader = Files.newBufferedReader(config)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                return remember(readContainers(json, createdDefault));
            }
        } catch (IOException | RuntimeException ex) {
            HbmNtm.LOGGER.warn("Failed to load HBM fluid container config.", ex);
            return remember(new LoadReport(false, createdDefault, 0, 1, List.of("failed to read " + CONFIG_FILE)));
        }
    }

    public static LoadReport loadReport() {
        return report;
    }

    private static LoadReport remember(LoadReport current) {
        report = current;
        return current;
    }

    private static LoadReport readContainers(JsonObject root, boolean createdDefault) {
        List<String> warnings = new ArrayList<>();
        int loaded = 0;
        int skipped = 0;
        if (root == null) {
            return new LoadReport(true, createdDefault, 0, 1, List.of("empty root"));
        }

        JsonArray entries = root.has("entries") && root.get("entries").isJsonArray()
                ? root.getAsJsonArray("entries")
                : new JsonArray();
        for (int i = 0; i < entries.size(); i++) {
            JsonElement element = entries.get(i);
            if (!element.isJsonObject()) {
                skipped++;
                warnings.add("entry " + i + " is not an object");
                continue;
            }
            try {
                JsonObject object = element.getAsJsonObject();
                ItemStack full = readStack(object.get("full"), "entry " + i + " full", true);
                ItemStack empty = readStack(object.get("empty"), "entry " + i + " empty", false);
                FluidType fluid = readFluid(object.get("fluid"));
                int amount = object.has("amount") ? object.get("amount").getAsInt() : object.get("content").getAsInt();
                if (fluid == HbmFluids.NONE || amount <= 0) {
                    skipped++;
                    warnings.add("entry " + i + " has invalid fluid or amount");
                    continue;
                }
                if (HbmFluidContainerRegistry.registerContainer(full, empty, fluid, amount)) {
                    loaded++;
                } else {
                    skipped++;
                    warnings.add("entry " + i + " was rejected by the container registry");
                }
            } catch (RuntimeException ex) {
                skipped++;
                warnings.add("entry " + i + " failed: " + ex.getMessage());
            }
        }
        return new LoadReport(true, createdDefault, loaded, skipped, warnings);
    }

    private static ItemStack readStack(JsonElement element, String name, boolean required) {
        if (element == null || element.isJsonNull()) {
            if (required) {
                throw new IllegalArgumentException(name + " is missing");
            }
            return ItemStack.EMPTY;
        }
        String itemName;
        int count = 1;
        String nbt = null;
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            itemName = object.get("item").getAsString();
            count = object.has("count") ? object.get("count").getAsInt() : 1;
            nbt = object.has("nbt") ? object.get("nbt").getAsString() : null;
        } else {
            itemName = element.getAsString();
        }
        ResourceLocation id = ResourceLocation.tryParse(itemName);
        if (id == null) {
            throw new IllegalArgumentException(name + " has invalid item id " + itemName);
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalArgumentException(name + " item is not registered: " + id));
        ItemStack stack = new ItemStack(item, Math.max(1, count));
        if (nbt != null && !nbt.isBlank()) {
            try {
                CompoundTag tag = TagParser.parseTag(nbt);
                stack.setTag(tag);
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException ex) {
                throw new IllegalArgumentException(name + " has invalid nbt: " + ex.getMessage(), ex);
            }
        }
        return stack;
    }

    private static FluidType readFluid(JsonElement element) {
        if (element == null) {
            return HbmFluids.NONE;
        }
        String name = element.getAsString();
        if (name.contains(":")) {
            ResourceLocation id = ResourceLocation.tryParse(name);
            name = id == null ? name : id.getPath();
        }
        return HbmFluids.fromName(name);
    }

    private static void writeDefault(Path config) throws IOException {
        JsonObject root = new JsonObject();
        root.add("entries", new JsonArray());
        try (Writer writer = Files.newBufferedWriter(config)) {
            GSON.toJson(root, writer);
        }
    }

    public record LoadReport(boolean loadedConfig, boolean createdDefault, int containers, int skipped,
            List<String> warnings) {
        public String summary() {
            return "fluid container config loadedConfig=" + loadedConfig + " createdDefault=" + createdDefault
                    + " containers=" + containers + " skipped=" + skipped;
        }
    }

    private HbmFluidContainerConfig() {
    }
}
