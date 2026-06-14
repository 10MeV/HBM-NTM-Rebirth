package com.hbm.ntm.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class LegacyOreDictionaryMappings {
    private static final String FORGE = "forge";
    private static final String MINECRAFT = "minecraft";
    private static final Map<String, TagId> EXACT = new LinkedHashMap<>();
    private static final Map<String, String> SHAPE_PREFIXES = new LinkedHashMap<>();

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
        registerShape("plateSextuple", "welded_plates");
        registerShape("plate", "plates");
        registerShape("shell", "shells");
        registerShape("ntmpipe", "pipes");
        registerShape("block", "storage_blocks");
        registerShape("barrelLight", "light_barrels");
        registerShape("barrelHeavy", "heavy_barrels");
        registerShape("receiverLight", "light_receivers");
        registerShape("receiverHeavy", "heavy_receivers");
        registerShape("gunMechanism", "gun_mechanisms");
        registerShape("stock", "stocks");
        registerShape("grip", "grips");
        registerShape("circuit", "circuits");
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

    private static void registerExact(String legacyName, String namespace, String path) {
        EXACT.put(legacyName, new TagId(namespace, path));
    }

    private static void registerShape(String legacyPrefix, String tagDirectory) {
        SHAPE_PREFIXES.put(legacyPrefix, tagDirectory);
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
