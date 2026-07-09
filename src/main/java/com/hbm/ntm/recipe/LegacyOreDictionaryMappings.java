package com.hbm.ntm.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class LegacyOreDictionaryMappings {
    private static final String FORGE = "forge";
    private static final String MINECRAFT = "minecraft";
    private static final Map<String, TagId> EXACT = new LinkedHashMap<>();
    private static final Map<String, String> SHAPE_PREFIXES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, List<String>> REVERSE_EXACT = new LinkedHashMap<>();
    private static final Map<ResourceLocation, List<String>> REVERSE_SHAPE_ALIASES = new LinkedHashMap<>();
    private static final Map<String, List<String>> REVERSE_MATERIAL_ALIASES = new LinkedHashMap<>();

    static {
        registerExact("stickWood", FORGE, "rods/wooden");
        registerExact("blockGlass", FORGE, "glass");
        registerExact("blockGlassColorless", FORGE, "glass/colorless");
        registerExact("paneGlass", FORGE, "glass_panes");
        registerExact("paneGlassColorless", FORGE, "glass_panes/colorless");
        registerExact("ingotBrick", FORGE, "ingots/brick");
        registerExact("ingotBrickNether", FORGE, "ingots/nether_brick");
        registerExact("slimeball", FORGE, "slimeballs");
        registerExact("logWood", MINECRAFT, "logs");
        registerExact("plankWood", MINECRAFT, "planks");
        registerExact("slabWood", MINECRAFT, "wooden_slabs");
        registerExact("treeLeaves", MINECRAFT, "leaves");
        registerExact("treeSapling", MINECRAFT, "saplings");
        registerExact("sand", FORGE, "sand");
        registerExact("stone", FORGE, "stone");
        registerExact("cobblestone", FORGE, "cobblestone");

        registerExact("dye", FORGE, "dyes");
        registerExact("dyeBlack", FORGE, "dyes/black");
        registerExact("dyeRed", FORGE, "dyes/red");
        registerExact("dyeGreen", FORGE, "dyes/green");
        registerExact("dyeBrown", FORGE, "dyes/brown");
        registerExact("dyeBlue", FORGE, "dyes/blue");
        registerExact("dyePurple", FORGE, "dyes/purple");
        registerExact("dyeCyan", FORGE, "dyes/cyan");
        registerExact("dyeLightGray", FORGE, "dyes/light_gray");
        registerExact("dyeGray", FORGE, "dyes/gray");
        registerExact("dyePink", FORGE, "dyes/pink");
        registerExact("dyeLime", FORGE, "dyes/lime");
        registerExact("dyeYellow", FORGE, "dyes/yellow");
        registerExact("dyeLightBlue", FORGE, "dyes/light_blue");
        registerExact("dyeMagenta", FORGE, "dyes/magenta");
        registerExact("dyeOrange", FORGE, "dyes/orange");
        registerExact("dyeWhite", FORGE, "dyes/white");

        registerExact("itemRubber", FORGE, "rubber");
        registerExact("ingotAnyPlasticexplosive", FORGE, "ingots/any_plastic_explosive");
        registerExact("ingotAnyPlasticExplosive", FORGE, "ingots/any_plastic_explosive");
        registerExact("ingotAnyHardplastic", FORGE, "ingots/any_hardplastic");
        registerExact("ingotAnyHardPlastic", FORGE, "ingots/any_hardplastic");
        registerExact("ingotAnyHighexplosive", FORGE, "ingots/any_high_explosive");
        registerExact("ingotAnyHighExplosive", FORGE, "ingots/any_high_explosive");
        registerExact("dustAnySmokeless", FORGE, "dusts/any_smokeless");
        registerExact("coalCoke", FORGE, "gems/coal_coke");
        registerExact("fuelCoke", FORGE, "gems/coke");
        registerExact("coke", FORGE, "gems/coke");
        registerExact("briquetteCoal", FORGE, "briquettes/coal");
        registerExact("briquetteLignite", FORGE, "briquettes/lignite");
        registerExact("briquetteWood", FORGE, "briquettes/wood");
        registerExact("dustPhosphorus", FORGE, "dusts/red_phosphorus");
        registerExact("logWoodPink", FORGE, "logs/pink");
        registerExact("plankWoodPink", FORGE, "planks/pink");
        registerExact("slabWoodPink", FORGE, "wooden_slabs/pink");
        registerExact("stairWoodPink", FORGE, "wooden_stairs/pink");
        registerExact("glyphidMeat", FORGE, "foods/glyphid_meat");
        registerExact("oiltar", FORGE, "tar/oil");
        registerExact("cracktar", FORGE, "tar/crack");
        registerExact("coaltar", FORGE, "tar/coal");
        registerExact("woodtar", FORGE, "tar/wood");
        registerExact("ntmuniversaltank", FORGE, "ntm/universal_tanks");
        registerExact("ntmhazardtank", FORGE, "ntm/hazard_tanks");
        registerExact("ntmuniversalbarrel", FORGE, "ntm/universal_barrels");
        registerExact("ntmscrewdriver", FORGE, "tools/screwdrivers");
        registerExact("ntmhanddrill", FORGE, "tools/hand_drills");
        registerExact("ntmchemistryset", FORGE, "tools/chemistry_sets");
        registerExact("ntmtorch", FORGE, "tools/torches");
        registerExact("pipeSteel", FORGE, "pipes/steel");

        registerShape("any", "any");
        registerShape("oreNether", "ores/nether");
        registerShape("ore", "ores");
        registerShape("nugget", "nuggets");
        registerShape("tiny", "nuggets");
        registerShape("bedrockorefragment", "bedrock_ore_fragments");
        registerShape("dustTiny", "tiny_dusts");
        registerShape("wireFine", "wires");
        registerShape("bolt", "bolts");
        registerShape("billet", "billets");
        registerShape("ingot", "ingots");
        registerShape("gem", "gems");
        registerShape("crystal", "crystals");
        registerShape("dust", "dusts");
        registerShape("wireDense", "dense_wires");
        registerShape("plateTriple", "cast_plates");
        registerShape("plateCast", "cast_plates");
        registerShape("plateWelded", "welded_plates");
        registerShape("plateSextuple", "welded_plates");
        registerShape("plate", "plates");
        registerShape("shell", "shells");
        registerShape("ntmpipe", "pipes");
        registerShape("pipe", "pipes");
        registerShape("block", "storage_blocks");
        registerShape("barrelLight", "light_barrels");
        registerShape("barrelHeavy", "heavy_barrels");
        registerShape("receiverLight", "light_receivers");
        registerShape("receiverHeavy", "heavy_receivers");
        registerShape("gunMechanism", "gun_mechanisms");
        registerShape("stock", "stocks");
        registerShape("grip", "grips");
        registerShape("circuit", "circuits");

        registerReverseMaterialAliases("aluminium", "Aluminum");
        registerReverseMaterialAliases("combine_steel", "CMBSteel");
        registerReverseMaterialAliases("bscco", "BSCCO");
        registerReverseMaterialAliases("pvc", "PVC");
        registerReverseMaterialAliases("pet", "PET");
        registerReverseMaterialAliases("cft", "CFT");
        registerReverseMaterialAliases("gunmetal", "GunMetal");
        registerReverseMaterialAliases("weaponsteel", "WeaponSteel");
        registerReverseMaterialAliases("plutonium_rg", "PlutoniumRG");
        registerReverseMaterialAliases("americium_rg", "AmericiumRG");
        registerReverseMaterialAliases("any_hardplastic", "AnyHardPlastic");

        registerReverseShapeAliases("dust", "NetherQuartz", "Quartz");
        registerReverseShapeAliases("dust", "RedPhosphorus");
        registerReverseShapeAliases("ntmpipe", "Steel");
        registerReverseShapeAliases("shell", "Aluminum", "Aluminium");
        registerReverseShapeAliases("wireFine", "Aluminum", "Aluminium");
        registerReverseShapeAliases("plateSextuple", "Aluminum", "Aluminium");
        registerReverseShapeAliases("plateSextuple", "CMBSteel", "CombineSteel");
        registerReverseShapeAliases("plateWelded", "CMBSteel", "CombineSteel");
        registerReverseTagAliases(new ResourceLocation(FORGE, "ingots/biorubber"), "ingotLatex");
    }

    private LegacyOreDictionaryMappings() {
    }

    public static TagKey<Item> itemTag(String legacyName) {
        return ItemTags.create(itemTagId(legacyName));
    }

    public static ResourceLocation itemTagId(String legacyName) {
        return resolve(legacyName).tagId();
    }

    public static Mapping resolve(String legacyName) {
        TagId exact = EXACT.get(legacyName);
        if (exact != null) {
            return new Mapping(legacyName, exact.location(), Kind.EXACT, legacyName, exact.path());
        }
        for (Map.Entry<String, String> entry : SHAPE_PREFIXES.entrySet()) {
            String legacyPrefix = entry.getKey();
            if (legacyName.startsWith(legacyPrefix) && legacyName.length() > legacyPrefix.length()) {
                String material = materialPath(legacyName.substring(legacyPrefix.length()));
                return new Mapping(legacyName, new ResourceLocation(FORGE, entry.getValue() + "/" + material),
                        Kind.SHAPE_PREFIX, legacyPrefix, material);
            }
        }

        String fallback = fallbackPath(legacyName);
        return new Mapping(legacyName, new ResourceLocation(FORGE, fallback), Kind.FALLBACK, "", fallback);
    }

    public static String itemTagPath(String legacyName) {
        return itemTagId(legacyName).getPath();
    }

    public static List<String> legacyNamesForTag(ResourceLocation tagId) {
        if (tagId == null) {
            return List.of();
        }

        Set<String> names = new LinkedHashSet<>();
        List<String> exactNames = REVERSE_EXACT.get(tagId);
        if (exactNames != null) {
            names.addAll(exactNames);
        }

        List<String> shapeAliases = REVERSE_SHAPE_ALIASES.get(tagId);
        if (shapeAliases != null) {
            names.addAll(shapeAliases);
            return List.copyOf(names);
        }

        if (exactNames != null || !FORGE.equals(tagId.getNamespace())) {
            return List.copyOf(names);
        }

        String tagPath = tagId.getPath();
        for (Map.Entry<String, String> entry : SHAPE_PREFIXES.entrySet()) {
            String tagDirectory = entry.getValue();
            String pathPrefix = tagDirectory + "/";
            if (tagPath.startsWith(pathPrefix) && tagPath.length() > pathPrefix.length()) {
                String materialPath = tagPath.substring(pathPrefix.length());
                for (String legacyMaterial : reverseMaterialNames(materialPath)) {
                    names.add(entry.getKey() + legacyMaterial);
                }
            }
        }

        return List.copyOf(names);
    }

    private static void registerExact(String legacyName, String namespace, String path) {
        TagId tag = new TagId(namespace, path);
        EXACT.put(legacyName, tag);
        addReverse(REVERSE_EXACT, tag.location(), legacyName);
    }

    private static void registerShape(String legacyPrefix, String tagDirectory) {
        SHAPE_PREFIXES.put(legacyPrefix, tagDirectory);
    }

    private static void registerReverseMaterialAliases(String modernMaterialPath, String... legacyMaterials) {
        REVERSE_MATERIAL_ALIASES.put(modernMaterialPath, List.of(legacyMaterials));
    }

    private static void registerReverseShapeAliases(String legacyPrefix, String... legacyMaterials) {
        for (String legacyMaterial : legacyMaterials) {
            String legacyName = legacyPrefix + legacyMaterial;
            addReverse(REVERSE_SHAPE_ALIASES, itemTagId(legacyName), legacyName);
        }
    }

    private static void registerReverseTagAliases(ResourceLocation tagId, String... legacyNames) {
        for (String legacyName : legacyNames) {
            addReverse(REVERSE_SHAPE_ALIASES, tagId, legacyName);
        }
    }

    private static void addReverse(Map<ResourceLocation, List<String>> reverse, ResourceLocation tagId, String legacyName) {
        List<String> names = reverse.computeIfAbsent(tagId, key -> new ArrayList<>());
        if (!names.contains(legacyName)) {
            names.add(legacyName);
        }
    }

    private static List<String> reverseMaterialNames(String materialPath) {
        List<String> aliases = REVERSE_MATERIAL_ALIASES.get(materialPath);
        if (aliases != null) {
            return aliases;
        }
        return List.of(legacyMaterialName(materialPath));
    }

    private static String legacyMaterialName(String materialPath) {
        String[] parts = materialPath.split("[_/]+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
                if (part.length() > 1) {
                    builder.append(part.substring(1));
                }
            }
        }
        return builder.toString();
    }

    private static String materialPath(String materialName) {
        return splitCamel(materialName)
                .replace("-", "_")
                .replaceAll("_+", "_")
                .replace("aluminum", "aluminium")
                .replace("nether_quartz", "quartz")
                .replace("advanced_alloy", "advanced_alloy")
                .replace("tc_alloy", "tc_alloy")
                .replace("cd_alloy", "cd_alloy")
                .replace("bscco", "bscco")
                .replace("cmb_steel", "combine_steel");
    }

    private static String fallbackPath(String legacyName) {
        if (legacyName.startsWith("container")) {
            return legacyName.toLowerCase(Locale.ROOT);
        }
        return splitCamel(legacyName);
    }

    private static String splitCamel(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
    }

    private record TagId(String namespace, String path) {
        private ResourceLocation location() {
            return new ResourceLocation(namespace, path);
        }
    }

    public record Mapping(String legacyName, ResourceLocation tagId, Kind kind, String matchedRule, String materialOrPath) {
    }

    public enum Kind {
        EXACT,
        SHAPE_PREFIX,
        FALLBACK
    }
}
