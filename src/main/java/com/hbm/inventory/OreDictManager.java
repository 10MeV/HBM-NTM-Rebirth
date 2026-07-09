package com.hbm.inventory;

import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.ntm.recipe.LegacyOreDictionaryMappings;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Source-shaped bridge for the 1.7.10 ore dictionary helper.
 * <p>
 * Modern tag contents stay datapack-driven; this facade preserves the legacy
 * string/DictFrame API and routes hazard registration through item tags.
 */
@Deprecated(forRemoval = false)
public class OreDictManager {
    private static final Map<String, Set<String>> RE_REGISTRATION = new HashMap<>();

    public static final String KEY_STICK = "stickWood";
    public static final String KEY_ANYGLASS = "blockGlass";
    public static final String KEY_CLEARGLASS = "blockGlassColorless";
    public static final String KEY_ANYPANE = "paneGlass";
    public static final String KEY_CLEARPANE = "paneGlassColorless";
    public static final String KEY_BRICK = "ingotBrick";
    public static final String KEY_NETHERBRICK = "ingotBrickNether";
    public static final String KEY_SLIME = "slimeball";
    public static final String KEY_LOG = "logWood";
    public static final String KEY_PLANKS = "plankWood";
    public static final String KEY_SLAB = "slabWood";
    public static final String KEY_LEAVES = "treeLeaves";
    public static final String KEY_SAPLING = "treeSapling";
    public static final String KEY_SAND = "sand";
    public static final String KEY_COBBLESTONE = "cobblestone";

    public static final String KEY_BLACK = "dyeBlack";
    public static final String KEY_RED = "dyeRed";
    public static final String KEY_GREEN = "dyeGreen";
    public static final String KEY_BROWN = "dyeBrown";
    public static final String KEY_BLUE = "dyeBlue";
    public static final String KEY_PURPLE = "dyePurple";
    public static final String KEY_CYAN = "dyeCyan";
    public static final String KEY_LIGHTGRAY = "dyeLightGray";
    public static final String KEY_GRAY = "dyeGray";
    public static final String KEY_PINK = "dyePink";
    public static final String KEY_LIME = "dyeLime";
    public static final String KEY_YELLOW = "dyeYellow";
    public static final String KEY_LIGHTBLUE = "dyeLightBlue";
    public static final String KEY_MAGENTA = "dyeMagenta";
    public static final String KEY_ORANGE = "dyeOrange";
    public static final String KEY_WHITE = "dyeWhite";

    public static final String KEY_OIL_TAR = "oiltar";
    public static final String KEY_CRACK_TAR = "cracktar";
    public static final String KEY_COAL_TAR = "coaltar";
    public static final String KEY_WOOD_TAR = "woodtar";

    public static final String KEY_UNIVERSAL_TANK = "ntmuniversaltank";
    public static final String KEY_HAZARD_TANK = "ntmhazardtank";
    public static final String KEY_UNIVERSAL_BARREL = "ntmuniversalbarrel";

    public static final String KEY_TOOL_SCREWDRIVER = "ntmscrewdriver";
    public static final String KEY_TOOL_HANDDRILL = "ntmhanddrill";
    public static final String KEY_TOOL_CHEMISTRYSET = "ntmchemistryset";
    public static final String KEY_TOOL_TORCH = "ntmtorch";

    public static final String KEY_GLYPHID_MEAT = "glyphidMeat";

    public static final DictFrame WOOD = new DictFrame("Wood");
    public static final DictFrame BONE = new DictFrame("Bone");
    public static final DictFrame COAL = new DictFrame("Coal");
    public static final DictFrame IRON = new DictFrame("Iron");
    public static final DictFrame GOLD = new DictFrame("Gold");
    public static final DictFrame LAPIS = new DictFrame("Lapis");
    public static final DictFrame REDSTONE = new DictFrame("Redstone");
    public static final DictFrame NETHERQUARTZ = new DictFrame("NetherQuartz");
    public static final DictFrame QUARTZ = new DictFrame("Quartz");
    public static final DictFrame DIAMOND = new DictFrame("Diamond");
    public static final DictFrame EMERALD = new DictFrame("Emerald");

    public static final DictFrame U = new DictFrame("Uranium");
    public static final DictFrame U233 = new DictFrame("Uranium233", "U233");
    public static final DictFrame U235 = new DictFrame("Uranium235", "U235");
    public static final DictFrame U238 = new DictFrame("Uranium238", "U238");
    public static final DictFrame TH232 = new DictFrame("Thorium232", "Th232", "Thorium");
    public static final DictFrame PU = new DictFrame("Plutonium");
    public static final DictFrame PURG = new DictFrame("PlutoniumRG");
    public static final DictFrame PU238 = new DictFrame("Plutonium238", "Pu238");
    public static final DictFrame PU239 = new DictFrame("Plutonium239", "Pu239");
    public static final DictFrame PU240 = new DictFrame("Plutonium240", "Pu240");
    public static final DictFrame PU241 = new DictFrame("Plutonium241", "Pu241");
    public static final DictFrame AM241 = new DictFrame("Americium241", "Am241");
    public static final DictFrame AM242 = new DictFrame("Americium242", "Am242");
    public static final DictFrame AMRG = new DictFrame("AmericiumRG");
    public static final DictFrame NP237 = new DictFrame("Neptunium237", "Np237", "Neptunium");
    public static final DictFrame PO210 = new DictFrame("Polonium210", "Po210", "Polonium");
    public static final DictFrame TC99 = new DictFrame("Technetium99", "Tc99");
    public static final DictFrame RA226 = new DictFrame("Radium226", "Ra226");
    public static final DictFrame AC227 = new DictFrame("Actinium227", "Ac227");
    public static final DictFrame CO60 = new DictFrame("Cobalt60", "Co60");
    public static final DictFrame AU198 = new DictFrame("Gold198", "Au198");
    public static final DictFrame PB209 = new DictFrame("Lead209", "Pb209");
    public static final DictFrame SA326 = new DictFrame("Schrabidium");
    public static final DictFrame SA327 = new DictFrame("Solinium");
    public static final DictFrame SBD = new DictFrame("Schrabidate");
    public static final DictFrame SRN = new DictFrame("Schraranium");
    public static final DictFrame GH336 = new DictFrame("Ghiorsium336", "Gh336");
    public static final DictFrame MUD = new DictFrame("WatzMud");

    public static final DictFrame TI = new DictFrame("Titanium");
    public static final DictFrame CU = new DictFrame("Copper");
    public static final DictFrame MINGRADE = new DictFrame("Mingrade");
    public static final DictFrame W = new DictFrame("Tungsten");
    public static final DictFrame WC = new DictFrame("TungstenCarbide");
    public static final DictFrame AL = new DictFrame("Aluminum");
    public static final DictFrame STEEL = new DictFrame("Steel");
    public static final DictFrame TCALLOY = new DictFrame("TcAlloy");
    public static final DictFrame CDALLOY = new DictFrame("CdAlloy");
    public static final DictFrame BBRONZE = new DictFrame("BismuthBronze");
    public static final DictFrame ABRONZE = new DictFrame("ArsenicBronze");
    public static final DictFrame BSCCO = new DictFrame("BSCCO");
    public static final DictFrame PB = new DictFrame("Lead");
    public static final DictFrame BI = new DictFrame("Bismuth");
    public static final DictFrame AS = new DictFrame("Arsenic");
    public static final DictFrame CA = new DictFrame("Calcium");
    public static final DictFrame CD = new DictFrame("Cadmium");
    public static final DictFrame TA = new DictFrame("Tantalum");
    public static final DictFrame COLTAN = new DictFrame("Coltan");
    public static final DictFrame NB = new DictFrame("Niobium");
    public static final DictFrame BE = new DictFrame("Beryllium");
    public static final DictFrame CO = new DictFrame("Cobalt");
    public static final DictFrame B = new DictFrame("Boron");
    public static final DictFrame SI = new DictFrame("Silicon");
    public static final DictFrame GRAPHITE = new DictFrame("Graphite");
    public static final DictFrame CARBON = new DictFrame("Carbon");
    public static final DictFrame DURA = new DictFrame("DuraSteel");
    public static final DictFrame POLYMER = new DictFrame("Polymer");
    public static final DictFrame BAKELITE = new DictFrame("Bakelite");
    public static final DictFrame PET = new DictFrame("PET");
    public static final DictFrame PC = new DictFrame("Polycarbonate");
    public static final DictFrame PVC = new DictFrame("PVC");
    public static final DictFrame LATEX = new DictFrame("Latex");
    public static final DictFrame RUBBER = new DictFrame("Rubber");
    public static final DictFrame MAGTUNG = new DictFrame("MagnetizedTungsten");
    public static final DictFrame CMB = new DictFrame("CMBSteel");
    public static final DictFrame DESH = new DictFrame("WorkersAlloy");
    public static final DictFrame STAR = new DictFrame("Starmetal");
    public static final DictFrame GUNMETAL = new DictFrame("GunMetal");
    public static final DictFrame WEAPONSTEEL = new DictFrame("WeaponSteel");
    public static final DictFrame BIGMT = new DictFrame("Saturnite");
    public static final DictFrame FERRO = new DictFrame("Ferrouranium");
    public static final DictFrame EUPH = new DictFrame("Euphemium");
    public static final DictFrame DNT = new DictFrame("Dineutronium");
    public static final DictFrame FIBER = new DictFrame("Fiberglass");
    public static final DictFrame ASBESTOS = new DictFrame("Asbestos");
    public static final DictFrame OSMIRIDIUM = new DictFrame("Osmiridium");

    public static final DictFrame S = new DictFrame("Sulfur");
    public static final DictFrame KNO = new DictFrame("Saltpeter");
    public static final DictFrame F = new DictFrame("Fluorite");
    public static final DictFrame LIGNITE = new DictFrame("Lignite");
    public static final DictFrame COALCOKE = new DictFrame("CoalCoke");
    public static final DictFrame PETCOKE = new DictFrame("PetCoke");
    public static final DictFrame LIGCOKE = new DictFrame("LigniteCoke");
    public static final DictFrame CINNABAR = new DictFrame("Cinnabar");
    public static final DictFrame BORAX = new DictFrame("Borax");
    public static final DictFrame CHLOROCALCITE = new DictFrame("Chlorocalcite");
    public static final DictFrame MOLYSITE = new DictFrame("Molysite");
    public static final DictFrame SODALITE = new DictFrame("Sodalite");
    public static final DictFrame VOLCANIC = new DictFrame("Volcanic");
    public static final DictFrame HEMATITE = new DictFrame("Hematite");
    public static final DictFrame MALACHITE = new DictFrame("Malachite");
    public static final DictFrame LIMESTONE = new DictFrame("Limestone");
    public static final DictFrame SLAG = new DictFrame("Slag");
    public static final DictFrame BAUXITE = new DictFrame("Bauxite");
    public static final DictFrame CRYOLITE = new DictFrame("Cryolite");

    public static final DictFrame LI = new DictFrame("Lithium");
    public static final DictFrame NA = new DictFrame("Sodium");
    public static final DictFrame P_WHITE = new DictFrame("WhitePhosphorus");
    public static final DictFrame P_RED = new DictFrame("RedPhosphorus");
    public static final DictFrame AUSTRALIUM = new DictFrame("Australium");
    public static final DictFrame RAREEARTH = new DictFrame("RareEarth");
    public static final DictFrame LA = new DictFrame("Lanthanum");
    public static final DictFrame ZR = new DictFrame("Zirconium");
    public static final DictFrame ND = new DictFrame("Neodymium");
    public static final DictFrame CE = new DictFrame("Cerium");
    public static final DictFrame I = new DictFrame("Iodine");
    public static final DictFrame AT = new DictFrame("Astatine");
    public static final DictFrame CS = new DictFrame("Caesium");
    public static final DictFrame ST = new DictFrame("Strontium");
    public static final DictFrame BR = new DictFrame("Bromine");
    public static final DictFrame TS = new DictFrame("Tennessine");
    public static final DictFrame SR = new DictFrame("Strontium");
    public static final DictFrame SR90 = new DictFrame("Strontium90", "Sr90");
    public static final DictFrame I131 = new DictFrame("Iodine131", "I131");
    public static final DictFrame XE135 = new DictFrame("Xenon135", "Xe135");
    public static final DictFrame CS137 = new DictFrame("Caesium137", "Cs137");
    public static final DictFrame AT209 = new DictFrame("Astatine209", "At209");

    public static final DictGroup ANY_RUBBER = new DictGroup("AnyRubber", LATEX, RUBBER);
    public static final DictGroup ANY_PLASTIC = new DictGroup("AnyPlastic", POLYMER, BAKELITE);
    public static final DictGroup ANY_HARDPLASTIC = new DictGroup("AnyHardPlastic", PC, PVC);
    public static final DictGroup ANY_RESISTANTALLOY = new DictGroup("AnyResistantAlloy", TCALLOY, CDALLOY);
    public static final DictGroup ANY_BISMOIDBRONZE = new DictGroup("AnyBismoidBronze", BBRONZE, ABRONZE);
    public static final DictFrame ANY_GUNPOWDER = new DictFrame("AnyPropellant");
    public static final DictFrame ANY_SMOKELESS = new DictFrame("AnySmokeless");
    public static final DictFrame ANY_PLASTICEXPLOSIVE = new DictFrame("AnyPlasticexplosive");
    public static final DictFrame ANY_HIGHEXPLOSIVE = new DictFrame("AnyHighexplosive");
    public static final DictFrame ANY_COKE = new DictFrame("AnyCoke", "Coke");
    public static final DictFrame ANY_CONCRETE = new DictFrame("Concrete");
    public static final DictGroup ANY_TAR = new DictGroup("Tar", KEY_OIL_TAR, KEY_COAL_TAR, KEY_CRACK_TAR, KEY_WOOD_TAR);
    public static final DictFrame ANY_BISMOID = new DictFrame("AnyBismoid");
    public static final DictFrame ANY_ASH = new DictFrame("Ash");

    public static final HashSet<ComparableStack> arcSmeltable = new HashSet<>();

    public static void registerOres() {
        registerGroups();
        compensateMojangSpaghettiBullshit();
    }

    public static void registerGroups() {
        ANY_RUBBER.addPrefix(MaterialShapes.INGOT, true);
        ANY_PLASTIC.addPrefix(MaterialShapes.INGOT, true)
                .addPrefix(MaterialShapes.DUST, true)
                .addPrefix(MaterialShapes.BLOCK, true)
                .addPrefix(MaterialShapes.GRIP, true)
                .addPrefix(MaterialShapes.STOCK, true);
        ANY_HARDPLASTIC.addPrefix(MaterialShapes.INGOT, true)
                .addPrefix(MaterialShapes.STOCK, true)
                .addPrefix(MaterialShapes.GRIP, true);
        ANY_RESISTANTALLOY.addPrefix(MaterialShapes.INGOT, true)
                .addPrefix(MaterialShapes.DUST, true)
                .addPrefix(MaterialShapes.CASTPLATE, true)
                .addPrefix(MaterialShapes.WELDEDPLATE, true)
                .addPrefix(MaterialShapes.BLOCK, true)
                .addPrefix(MaterialShapes.LIGHTBARREL, true)
                .addPrefix(MaterialShapes.HEAVYBARREL, true)
                .addPrefix(MaterialShapes.LIGHTRECEIVER, true)
                .addPrefix(MaterialShapes.HEAVYRECEIVER, true);
        ANY_BISMOIDBRONZE.addPrefix(MaterialShapes.INGOT, true)
                .addPrefix(MaterialShapes.CASTPLATE, true)
                .addPrefix(MaterialShapes.LIGHTBARREL, true)
                .addPrefix(MaterialShapes.HEAVYBARREL, true)
                .addPrefix(MaterialShapes.LIGHTRECEIVER, true)
                .addPrefix(MaterialShapes.HEAVYRECEIVER, true);
        ANY_TAR.addPrefix(MaterialShapes.ANY, false);
    }

    public static Set<String> reRegistrationsFor(String legacyOreName) {
        Set<String> aliases = RE_REGISTRATION.get(legacyOreName);
        return aliases == null ? Set.of() : Set.copyOf(aliases);
    }

    public static TagKey<Item> itemTag(String legacyOreName) {
        return LegacyOreDictionaryMappings.itemTag(legacyOreName);
    }

    public static void compensateMojangSpaghettiBullshit() {
        arcSmeltable.add(new ComparableStack(Blocks.GOLD_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.IRON_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.LAPIS_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.DIAMOND_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.REDSTONE_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.EMERALD_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.NETHER_QUARTZ_ORE));
        arcSmeltable.add(new ComparableStack(Blocks.GOLD_BLOCK));
        arcSmeltable.add(new ComparableStack(Blocks.IRON_BLOCK));
        arcSmeltable.add(new ComparableStack(Blocks.LAPIS_BLOCK));
        arcSmeltable.add(new ComparableStack(Blocks.DIAMOND_BLOCK));
        arcSmeltable.add(new ComparableStack(Blocks.REDSTONE_BLOCK));
        arcSmeltable.add(new ComparableStack(Blocks.EMERALD_BLOCK));
        arcSmeltable.add(new ComparableStack(Blocks.QUARTZ_BLOCK));
        arcSmeltable.add(new ComparableStack(Items.IRON_INGOT));
        arcSmeltable.add(new ComparableStack(Items.GOLD_INGOT));
        arcSmeltable.add(new ComparableStack(Items.BRICK));
        arcSmeltable.add(new ComparableStack(Items.NETHER_BRICK));
    }

    private static void addReRegistration(String original, String additional) {
        RE_REGISTRATION.computeIfAbsent(original, key -> new HashSet<>()).add(additional);
    }

    public static class DictFrame {
        public String[] mats;
        float hazMult = 1.0F;
        List<HazardEntry> hazards = new ArrayList<>();

        public DictFrame(String... mats) {
            this.mats = mats;
        }

        public String any() {
            return name(MaterialShapes.ANY);
        }

        public String nugget() {
            return name(MaterialShapes.NUGGET);
        }

        public String tiny() {
            return name(MaterialShapes.TINY);
        }

        public String bolt() {
            return name(MaterialShapes.BOLT);
        }

        public String ingot() {
            return name(MaterialShapes.INGOT);
        }

        public String dustTiny() {
            return name(MaterialShapes.DUSTTINY);
        }

        public String dust() {
            return name(MaterialShapes.DUST);
        }

        public String gem() {
            return name(MaterialShapes.GEM);
        }

        public String crystal() {
            return name(MaterialShapes.CRYSTAL);
        }

        public String plate() {
            return name(MaterialShapes.PLATE);
        }

        public String plateCast() {
            return name(MaterialShapes.CASTPLATE);
        }

        public String plateWelded() {
            return name(MaterialShapes.WELDEDPLATE);
        }

        @Deprecated
        public String heavyComp() {
            return plateWelded();
        }

        public String wireFine() {
            return name(MaterialShapes.WIRE);
        }

        public String wireDense() {
            return name(MaterialShapes.DENSEWIRE);
        }

        public String shell() {
            return name(MaterialShapes.SHELL);
        }

        public String pipe() {
            return name(MaterialShapes.PIPE);
        }

        public String billet() {
            return name(MaterialShapes.BILLET);
        }

        public String block() {
            return name(MaterialShapes.BLOCK);
        }

        public String ore() {
            return name(MaterialShapes.ORE);
        }

        public String fragment() {
            return name(MaterialShapes.FRAGMENT);
        }

        public String lightBarrel() {
            return name(MaterialShapes.LIGHTBARREL);
        }

        public String heavyBarrel() {
            return name(MaterialShapes.HEAVYBARREL);
        }

        public String lightReceiver() {
            return name(MaterialShapes.LIGHTRECEIVER);
        }

        public String heavyReceiver() {
            return name(MaterialShapes.HEAVYRECEIVER);
        }

        public String mechanism() {
            return name(MaterialShapes.MECHANISM);
        }

        public String stock() {
            return name(MaterialShapes.STOCK);
        }

        public String grip() {
            return name(MaterialShapes.GRIP);
        }

        public String[] all(MaterialShapes shape) {
            return appendToAll(shape.prefixes);
        }

        private String name(MaterialShapes shape) {
            return shape.name() + mats[0];
        }

        private String[] appendToAll(String... prefixes) {
            String[] names = new String[mats.length * prefixes.length];
            for (int i = 0; i < mats.length; i++) {
                for (int j = 0; j < prefixes.length; j++) {
                    names[i * prefixes.length + j] = prefixes[j] + mats[i];
                }
            }
            return names;
        }

        public DictFrame rad(float rad) {
            return haz(new HazardEntry(HazardRegistry.RADIATION, rad));
        }

        public DictFrame hot(float time) {
            return haz(new HazardEntry(HazardRegistry.HOT, time));
        }

        public DictFrame blinding(float time) {
            return haz(new HazardEntry(HazardRegistry.BLINDING, time));
        }

        public DictFrame asbestos(float asbestos) {
            return haz(new HazardEntry(HazardRegistry.ASBESTOS, asbestos));
        }

        public DictFrame hydro(float hydro) {
            return haz(new HazardEntry(HazardRegistry.HYDROACTIVE, hydro));
        }

        public DictFrame haz(HazardEntry hazard) {
            hazards.add(hazard);
            return this;
        }

        public static ItemStack fromOne(ItemLike item, Enum<?> meta) {
            return fromOne(item, meta, 1);
        }

        public static ItemStack fromOne(ItemLike item, Enum<?> meta, int stacksize) {
            return stack(item, meta == null ? 0 : meta.ordinal(), stacksize);
        }

        public static ItemStack fromOne(ItemLike item, int meta) {
            return stack(item, meta, 1);
        }

        public static ItemStack fromOne(ItemLike item, int meta, int stacksize) {
            return stack(item, meta, stacksize);
        }

        public static Object[] fromAll(ItemLike item, Class<? extends Enum<?>> enumClass) {
            Enum<?>[] values = enumClass.getEnumConstants();
            Object[] stacks = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                stacks[i] = fromOne(item, values[i]);
            }
            return stacks;
        }

        private static ItemStack stack(ItemLike item, int meta, int stacksize) {
            if (item == null) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = new ItemStack(item, Math.max(1, stacksize));
            if (meta > 0) {
                stack.setDamageValue(meta);
            }
            return stack;
        }

        public DictFrame any(Object... objects) {
            return makeObject(MaterialShapes.ANY, objects);
        }

        public DictFrame nugget(Object... objects) {
            hazMult = HazardRegistry.nugget;
            return makeObject(MaterialShapes.NUGGET, objects).makeObject(MaterialShapes.TINY, objects);
        }

        public DictFrame ingot(Object... objects) {
            hazMult = HazardRegistry.ingot;
            return makeObject(MaterialShapes.INGOT, objects);
        }

        public DictFrame dustSmall(Object... objects) {
            hazMult = HazardRegistry.powder_tiny;
            return makeObject(MaterialShapes.DUSTTINY, objects);
        }

        public DictFrame dust(Object... objects) {
            hazMult = HazardRegistry.powder;
            return makeObject(MaterialShapes.DUST, objects);
        }

        public DictFrame gem(Object... objects) {
            hazMult = HazardRegistry.gem;
            return makeObject(MaterialShapes.GEM, objects);
        }

        public DictFrame crystal(Object... objects) {
            hazMult = HazardRegistry.gem;
            return makeObject(MaterialShapes.CRYSTAL, objects);
        }

        public DictFrame plate(Object... objects) {
            hazMult = HazardRegistry.plate;
            return makeObject(MaterialShapes.PLATE, objects);
        }

        public DictFrame plateCast(Object... objects) {
            hazMult = HazardRegistry.plateCast;
            return makeObject(MaterialShapes.CASTPLATE, objects);
        }

        public DictFrame billet(Object... objects) {
            hazMult = HazardRegistry.billet;
            return makeObject(MaterialShapes.BILLET, objects);
        }

        public DictFrame block(Object... objects) {
            hazMult = HazardRegistry.block;
            return makeObject(MaterialShapes.BLOCK, objects);
        }

        public DictFrame ore(Object... objects) {
            hazMult = HazardRegistry.ore;
            return makeObject(MaterialShapes.ORE, objects);
        }

        public DictFrame oreNether(Object... objects) {
            hazMult = HazardRegistry.ore;
            return makeObject(MaterialShapes.ORENETHER, objects);
        }

        public DictFrame makeObject(MaterialShapes shape, Object... objects) {
            String tag = shape.name();
            for (Object object : objects) {
                if (object instanceof ItemStack stack) {
                    registerStack(tag, stack);
                } else if (object instanceof ItemLike item) {
                    registerStack(tag, new ItemStack(item));
                } else if (object instanceof ComparableStack stack) {
                    registerStack(tag, stack.toStack());
                }
            }
            return this;
        }

        public DictFrame makeItem(String tag, Item... items) {
            for (Item item : items) {
                registerStack(tag, new ItemStack(item));
            }
            return this;
        }

        public DictFrame makeStack(String tag, ItemStack... stacks) {
            for (ItemStack stack : stacks) {
                registerStack(tag, stack);
            }
            return this;
        }

        public DictFrame makeBlocks(String tag, Block... blocks) {
            for (Block block : blocks) {
                registerStack(tag, new ItemStack(block));
            }
            return this;
        }

        public DictFrame hazIngot() {
            hazMult = HazardRegistry.ingot;
            return autoRegHazard(MaterialShapes.INGOT);
        }

        public DictFrame autoRegHazard(MaterialShapes shape) {
            String tag = shape.name();
            for (String mat : mats) {
                registerHazards(hazards, hazMult, tag + mat);
            }
            return this;
        }

        public static void registerHazards(List<HazardEntry> hazards, float hazMult, String dictKey) {
            if (!hazards.isEmpty() && hazMult > 0.0F) {
                HazardData data = new HazardData().setMutex(0b1);
                for (HazardEntry hazard : hazards) {
                    data.addEntry(hazard.clone(hazMult));
                }
                HazardSystem.register(dictKey, data);
            }
        }

        public void registerStack(String tag, ItemStack stack) {
            for (String mat : mats) {
                registerHazards(hazards, hazMult, tag + mat);
            }
            if ("ingot".equals(tag)) {
                registerStack("", stack);
            }
        }
    }

    public static class DictGroup {
        private final String groupName;
        private final Set<String> names = new HashSet<>();

        public DictGroup(String groupName) {
            this.groupName = groupName;
        }

        public DictGroup(String groupName, String... names) {
            this(groupName);
            addNames(names);
        }

        public DictGroup(String groupName, DictFrame... frames) {
            this(groupName);
            addFrames(frames);
        }

        public DictGroup addNames(String... names) {
            for (String name : names) {
                this.names.add(name);
            }
            return this;
        }

        public DictGroup addFrames(DictFrame... frames) {
            for (DictFrame frame : frames) {
                addNames(frame.mats);
            }
            return this;
        }

        public DictGroup addPrefix(MaterialShapes shape, boolean inputPrefix) {
            String prefix = shape.name();
            String group = prefix + groupName;
            for (String name : names) {
                String original = (inputPrefix ? prefix : "") + name;
                addReRegistration(original, group);
            }
            return this;
        }

        public DictGroup addFixed(String prefix, String original) {
            addReRegistration(original, prefix + groupName);
            return this;
        }

        public String any() {
            return name(MaterialShapes.ANY);
        }

        public String nugget() {
            return name(MaterialShapes.NUGGET);
        }

        public String tiny() {
            return name(MaterialShapes.TINY);
        }

        public String bolt() {
            return name(MaterialShapes.BOLT);
        }

        public String ingot() {
            return name(MaterialShapes.INGOT);
        }

        public String dustTiny() {
            return name(MaterialShapes.DUSTTINY);
        }

        public String dust() {
            return name(MaterialShapes.DUST);
        }

        public String gem() {
            return name(MaterialShapes.GEM);
        }

        public String crystal() {
            return name(MaterialShapes.CRYSTAL);
        }

        public String plate() {
            return name(MaterialShapes.PLATE);
        }

        public String plateCast() {
            return name(MaterialShapes.CASTPLATE);
        }

        public String plateWelded() {
            return name(MaterialShapes.WELDEDPLATE);
        }

        @Deprecated
        public String heavyComp() {
            return plateWelded();
        }

        public String wireFine() {
            return name(MaterialShapes.WIRE);
        }

        public String wireDense() {
            return name(MaterialShapes.DENSEWIRE);
        }

        public String billet() {
            return name(MaterialShapes.BILLET);
        }

        public String block() {
            return name(MaterialShapes.BLOCK);
        }

        public String ore() {
            return name(MaterialShapes.ORE);
        }

        public String lightBarrel() {
            return name(MaterialShapes.LIGHTBARREL);
        }

        public String heavyBarrel() {
            return name(MaterialShapes.HEAVYBARREL);
        }

        public String lightReceiver() {
            return name(MaterialShapes.LIGHTRECEIVER);
        }

        public String heavyReceiver() {
            return name(MaterialShapes.HEAVYRECEIVER);
        }

        public String mechanism() {
            return name(MaterialShapes.MECHANISM);
        }

        public String stock() {
            return name(MaterialShapes.STOCK);
        }

        public String grip() {
            return name(MaterialShapes.GRIP);
        }

        private String name(MaterialShapes shape) {
            return shape.name() + groupName;
        }
    }
}
