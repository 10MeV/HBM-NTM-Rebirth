package com.hbm.inventory.material;

import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.item.BedrockOreFragmentItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import static com.hbm.inventory.material.MaterialShapes.*;

public class Mats {
    public static final List<NTMMaterial> orderedList = new ArrayList<>();
    public static final HashMap<String, MaterialShapes> prefixByName = new HashMap<>();
    public static final HashMap<Integer, NTMMaterial> matById = new HashMap<>();
    public static final HashMap<String, NTMMaterial> matByName = new HashMap<>();
    private static final Map<String, MaterialShapes> MODERN_PATH_PREFIXES = new HashMap<>();
    private static final Map<String, NTMMaterial> MODERN_PATH_MATERIALS = new HashMap<>();
    private static final Map<MaterialShapes, List<String>> MODERN_SHAPE_PATH_PREFIXES = new HashMap<>();
    private static final Map<NTMMaterial, List<String>> MODERN_MATERIAL_PATH_NAMES = new HashMap<>();

    public static final int _VS = 0;
    public static final int _AS = 30;
    public static final int _ES = 20_000;

    public static final NTMMaterial MAT_WOOD = makeNonSmeltable(_VS + 3, "Wood", 0x896727, 0x281E0B, 0x896727).setAutogen(STOCK, GRIP).n();
    public static final NTMMaterial MAT_IVORY = makeNonSmeltable(_VS + 4, "Bone", 0xFFFEEE, 0x797870, 0xEDEBCA).setAutogen(GRIP).n();
    public static final NTMMaterial MAT_STONE = makeSmeltable(_VS, "Stone", 0x7F7F7F, 0x353535, 0x4D2F23).n();
    public static final NTMMaterial MAT_CARBON = makeAdditive(699, "Carbon", 0x363636, 0x030303, 0x404040).n();
    public static final NTMMaterial MAT_COAL = makeNonSmeltable(600, "Coal", 0x363636, 0x030303, 0x404040).setConversion(MAT_CARBON, 2, 1).n();
    public static final NTMMaterial MAT_LIGNITE = makeNonSmeltable(601, "Lignite", 0x542D0F, 0x261508, 0x472913).setConversion(MAT_CARBON, 3, 1).n();
    public static final NTMMaterial MAT_COALCOKE = make(610, "CoalCoke").setConversion(MAT_CARBON, 4, 3).n();
    public static final NTMMaterial MAT_PETCOKE = make(611, "PetCoke").setConversion(MAT_CARBON, 4, 3).n();
    public static final NTMMaterial MAT_LIGCOKE = make(612, "LigniteCoke").setConversion(MAT_CARBON, 4, 3).n();
    public static final NTMMaterial MAT_GRAPHITE = make(620, "Graphite").setConversion(MAT_CARBON, 1, 1).n();
    public static final NTMMaterial MAT_DIAMOND = makeNonSmeltable(1430, "Diamond", 0xFFFFFF, 0x1B7B6B, 0x8CF4E2).setConversion(MAT_CARBON, 1, 1).n();
    public static final NTMMaterial MAT_IRON = makeSmeltable(2600, "Iron", 0xFFFFFF, 0x353535, 0xFFA259).m();
    public static final NTMMaterial MAT_GOLD = makeSmeltable(7900, "Gold", 0xFFFF8B, 0xC26E00, 0xE8D754).m();
    public static final NTMMaterial MAT_REDSTONE = makeSmeltable(_VS + 1, "Redstone", 0xE3260C, 0x700E06, 0xFF1000).n();
    public static final NTMMaterial MAT_OBSIDIAN = makeSmeltable(_VS + 2, "Obsidian", 0x3D234D).n();
    public static final NTMMaterial MAT_HEMATITE = makeAdditive(2601, "Hematite", 0xDFB7AE, 0x5F372E, 0x6E463D).m();
    public static final NTMMaterial MAT_WROUGHTIRON = makeSmeltable(2602, "WroughtIron", 0xFAAB89).m();
    public static final NTMMaterial MAT_PIGIRON = makeSmeltable(2603, "PigIron", 0xFF8B59).m();
    public static final NTMMaterial MAT_METEORICIRON = makeSmeltable(2604, "MeteoricIron", 0x715347).m();
    public static final NTMMaterial MAT_TITANIUM = makeSmeltable(2200, "Titanium", "Ti", 0xF7F3F2, 0x4F4C4B, 0xA99E79).m();
    public static final NTMMaterial MAT_COPPER = makeSmeltable(2900, "Copper", "Cu", 0xFDCA88, 0x601E0D, 0xC18336).m();
    public static final NTMMaterial MAT_MALACHITE = makeAdditive(2901, "Malachite", 0xA2F0C8, 0x227048, 0x61AF87).m();
    public static final NTMMaterial MAT_BAUXITE = makeNonSmeltable(2902, "Bauxite", 0xF4BA30, 0xAA320A, 0xE2560F).n();
    public static final NTMMaterial MAT_CRYOLITE = makeNonSmeltable(2903, "Cryolite", 0xCBC2A4, 0x8B711F, 0x8B701A).n();
    public static final NTMMaterial MAT_TUNGSTEN = makeSmeltable(7400, "Tungsten", "W", 0x868686, 0x000000, 0x977474).m();
    public static final NTMMaterial MAT_ALUMINIUM = makeSmeltable(1300, "Aluminium", "Aluminum", "Al", 0xFFFFFF, 0x344550, 0xD0B8EB).m();
    public static final NTMMaterial MAT_LEAD = makeSmeltable(8200, "Lead", "Pb", 0xA6A6B2, 0x03030F, 0x646470).m();
    public static final NTMMaterial MAT_BISMUTH = makeSmeltable(8300, "Bismuth", "Bi", 0xB200FF, 0xB200FF, 0xB200FF).m();
    public static final NTMMaterial MAT_TANTALIUM = makeSmeltable(7300, "Tantalium", "Tantalum", "Ta", 0xFFFFFF, 0x1D1D36, 0xA89B74).m();
    public static final NTMMaterial MAT_NEODYMIUM = makeSmeltable(6000, "Neodymium", "Nd", 0xE6E6B6, 0x1C1C00, 0x8F8F5F).setAutogen(FRAGMENT, NUGGET, DUSTTINY, INGOT, DUST, DENSEWIRE, BLOCK).m();
    public static final NTMMaterial MAT_NIOBIUM = makeSmeltable(4100, "Niobium", "Nb", 0xB76EC9, 0x2F2D42, 0xD576B1).m();
    public static final NTMMaterial MAT_BERYLLIUM = makeSmeltable(400, "Beryllium", "Be", 0xB2B2A6, 0x0F0F03, 0xAE9572).m();
    public static final NTMMaterial MAT_EMERALD = makeNonSmeltable(401, "Emerald", 0xBAFFD4, 0x003900, 0x17DD62).setConversion(MAT_BERYLLIUM, 4, 3).n();
    public static final NTMMaterial MAT_COBALT = makeSmeltable(2700, "Cobalt", "Co", 0xC2D1EE, 0x353554, 0x8F72AE).m();
    public static final NTMMaterial MAT_BORON = makeSmeltable(500, "Boron", "B", 0xBDC8D2, 0x29343E, 0xAD72AE).m();
    public static final NTMMaterial MAT_BORAX = makeSmeltable(501, "Borax", 0xFFFFFF, 0x946E23, 0xFFECC6).setAutogen(FRAGMENT, INGOT, DUST).n();
    public static final NTMMaterial MAT_LANTHANIUM = makeSmeltable(5700, "Lanthanum", "Lanthanium", "La", 0xC8E0E0, 0x3B5353, 0xA1B9B9).m();
    public static final NTMMaterial MAT_ZIRCONIUM = makeSmeltable(4000, "Zirconium", "Zr", 0xE3DCBE, 0x3E3719, 0xADA688).m();
    public static final NTMMaterial MAT_SODALITE = makeNonSmeltable(1101, "Sodalite", 0xDCE5F6, 0x4927B4, 0x96A7E6).n();
    public static final NTMMaterial MAT_LITHIUM = makeSmeltable(300, "Lithium", "Li", 0xFFFFFF, 0x818181, 0xD6D6D6).m();
    public static final NTMMaterial MAT_SULFUR = makeNonSmeltable(1600, "Sulfur", "Sulphur", 0xFCEE80, 0xBDA022, 0xF1DF68).n();
    public static final NTMMaterial MAT_KNO = makeNonSmeltable(700, "Saltpeter", "Niter", "KNO", 0xD4D4D4, 0x969696, 0xC9C9C9).n();
    public static final NTMMaterial MAT_FLUORITE = makeNonSmeltable(900, "Fluorite", 0xFFFFFF, 0xB0A192, 0xE1DBD4).n();
    public static final NTMMaterial MAT_PHOSPHORUS = makeNonSmeltable(1500, "RedPhosphorus", "RedPhosphorus", "P", 0xCB0213, 0x600006, 0xBA0615).n();
    public static final NTMMaterial MAT_CHLOROCALCITE = makeNonSmeltable(1701, "Chlorocalcite", 0xF7E761, 0x475B46, 0xB8B963).n();
    public static final NTMMaterial MAT_MOLYSITE = makeNonSmeltable(1702, "Molysite", 0xF9E97B, 0x216E00, 0xD0D264).n();
    public static final NTMMaterial MAT_CINNABAR = makeNonSmeltable(8001, "Cinnabar", 0xD87070, 0x993030, 0xBF4E4E).n();
    public static final NTMMaterial MAT_ASBESTOS = makeSmeltable(1401, "Asbestos", 0xD8D9CF, 0x616258, 0xB0B3A8).n();
    public static final NTMMaterial MAT_OSMIRIDIUM = makeSmeltable(7699, "Osmiridium", 0xDBE3EF, 0x7891BE, 0xACBDD9).setAutogen(NUGGET, CASTPLATE, WELDEDPLATE).m();
    public static final NTMMaterial MAT_SILICON = makeSmeltable(1400, "Silicon", "Si", 0xD1D7DF, 0x1A1A3D, 0x878B9E).m();
    public static final NTMMaterial MAT_URANIUM = makeSmeltable(9200, "Uranium", "U", 0xC1C7BD, 0x2B3227, 0x9AA196).m();
    public static final NTMMaterial MAT_U233 = makeSmeltable(9233, "Uranium233", "U233", 0xC1C7BD, 0x2B3227, 0x9AA196).setAutogen(NUGGET, BILLET, DUST, BLOCK).m();
    public static final NTMMaterial MAT_U235 = makeSmeltable(9235, "Uranium235", "U235", 0xC1C7BD, 0x2B3227, 0x9AA196).setAutogen(NUGGET, BILLET, DUST, BLOCK).m();
    public static final NTMMaterial MAT_U238 = makeSmeltable(9238, "Uranium238", "U238", 0xC1C7BD, 0x2B3227, 0x9AA196).m();
    public static final NTMMaterial MAT_THORIUM = makeSmeltable(9032, "Thorium232", "Th232", "Thorium", 0xBF825F, 0x1C0000, 0xBF825F).m();
    public static final NTMMaterial MAT_PLUTONIUM = makeSmeltable(9400, "Plutonium", "Pu", 0x9AA3A0, 0x111A17, 0x78817E).m();
    public static final NTMMaterial MAT_RGP = makeSmeltable(9401, "PlutoniumRG", 0x9AA3A0, 0x111A17, 0x78817E).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_PU238 = makeSmeltable(9438, "Plutonium238", "Pu238", 0xFFBC59, 0xFF8E2B, 0x78817E).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_PU239 = makeSmeltable(9439, "Plutonium239", "Pu239", 0x9AA3A0, 0x111A17, 0x78817E).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_PU240 = makeSmeltable(9440, "Plutonium240", "Pu240", 0x9AA3A0, 0x111A17, 0x78817E).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_PU241 = makeSmeltable(9441, "Plutonium241", "Pu241", 0x9AA3A0, 0x111A17, 0x78817E).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_RGA = makeSmeltable(9501, "AmericiumRG", 0xCEB3B9, 0x3A1C21, 0x93767B).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_AM241 = makeSmeltable(9541, "Americium241", "Am241", 0xCEB3B9, 0x3A1C21, 0x93767B).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_AM242 = makeSmeltable(9542, "Americium242", "Am242", 0xCEB3B9, 0x3A1C21, 0x93767B).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_NEPTUNIUM = makeSmeltable(9337, "Neptunium237", "Np237", "Neptunium", 0xA6B2A6, 0x030F03, 0x647064).setAutogen(NUGGET, BILLET, DUST, BLOCK).m();
    public static final NTMMaterial MAT_POLONIUM = makeSmeltable(8410, "Polonium210", "Po210", "Polonium", 0x968779, 0x3D1509, 0x715E4A).m();
    public static final NTMMaterial MAT_TECHNETIUM = makeSmeltable(4399, "Tc99", "Technetium", 0xFAFFFF, 0x576C6C, 0xCADFDF).m();
    public static final NTMMaterial MAT_RADIUM = makeSmeltable(8826, "Radium226", "Ra226", "Radium", 0xFCFCFC, 0xADBFBA, 0xE9FAF6).m();
    public static final NTMMaterial MAT_ACTINIUM = makeSmeltable(8927, "Actinium227", "Ac227", 0xECE0E0, 0x221616, 0x958989).setAutogen(NUGGET, BILLET).m();
    public static final NTMMaterial MAT_CO60 = makeSmeltable(2760, "Cobalt60", "Co60", 0xC2D1EE, 0x353554, 0x8F72AE).setAutogen(NUGGET, BILLET, DUST).m();
    public static final NTMMaterial MAT_AU198 = makeSmeltable(7998, "Gold198", "Au198", 0xFFFF8B, 0xC26E00, 0xE8D754).setAutogen(NUGGET, BILLET, DUST).m();
    public static final NTMMaterial MAT_PB209 = makeSmeltable(8209, "Lead209", "Pb209", 0xB38A94, 0x12020E, 0x7B535D).setAutogen(NUGGET, BILLET, DUST).m();
    public static final NTMMaterial MAT_SCHRABIDIUM = makeSmeltable(12626, "Schrabidium", "Sa326", 0x32FFFF, 0x005C5C, 0x32FFFF).m();
    public static final NTMMaterial MAT_SOLINIUM = makeSmeltable(12627, "Solinium", 0xA2E6E0, 0x00433D, 0x72B6B0).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_SCHRABIDATE = makeSmeltable(12600, "Schrabidate", 0x77C0D7, 0x39005E, 0x6589B4).setAutogen(DUST, DENSEWIRE, CASTPLATE, BLOCK).m();
    public static final NTMMaterial MAT_SCHRARANIUM = makeSmeltable(12601, "Schraranium", 0x2B3227, 0x2B3227, 0x24AFAC).setAutogen(BLOCK).m();
    public static final NTMMaterial MAT_GHIORSIUM = makeSmeltable(12836, "Ghiorsium336", "Gh336", 0xF4EFE1, 0x2A3306, 0xC6C6A1).setAutogen(NUGGET, BILLET, BLOCK).m();
    public static final NTMMaterial MAT_ARSENIC = makeSmeltable(3300, "Arsenic", "As", 0x6CBABA, 0x242525, 0x558080).m();
    public static final NTMMaterial MAT_STRONTIUM = makeSmeltable(3800, "Strontium", "Sr", 0xF1E8BA, 0x271E00, 0xCAC193).setAutogen(FRAGMENT, INGOT, DUST).m();
    public static final NTMMaterial MAT_CALCIUM = makeSmeltable(2000, "Calcium", "Ca", 0xCFCFA6, 0x747F6E, 0xB7B784).m();
    public static final NTMMaterial MAT_SODIUM = makeSmeltable(1100, "Sodium", "Na", 0xD3BF9E, 0x3A5A6B, 0x7E9493).setAutogen(FRAGMENT, INGOT, DUST).m();
    public static final NTMMaterial MAT_CADMIUM = makeSmeltable(4800, "Cadmium", "Cd", 0xFFFADE, 0x350000, 0xA85600).m();
    public static final NTMMaterial MAT_STEEL = makeSmeltable(_AS, "Steel", 0xAFAFAF, 0x0F0F0F, 0x4A4A4A).m();
    public static final NTMMaterial MAT_MINGRADE = makeSmeltable(_AS + 1, "Mingrade", "RedCopper", 0xFFBA7D, 0xAF1700, 0xE44C0F).m();
    public static final NTMMaterial MAT_DURA = makeSmeltable(_AS + 3, "DuraSteel", "Dura", 0x82A59C, 0x06281E, 0x42665C).m();
    public static final NTMMaterial MAT_DESH = makeSmeltable(_AS + 12, "Desh", 0xFF6D6D, 0x720000, 0xF22929).m();
    public static final NTMMaterial MAT_STAR = makeSmeltable(_AS + 5, "Starmetal", "Star", 0xCCCCEA, 0x11111A, 0xA5A5D3).m();
    public static final NTMMaterial MAT_FERRO = makeSmeltable(_AS + 7, "Ferrouranium", "Ferro", 0xB7B7C9, 0x101022, 0x6B6B8B).m();
    public static final NTMMaterial MAT_TCALLOY = makeSmeltable(_AS + 6, "TcAlloy", "TCAlloy", 0xD4D6D6, 0x323D3D, 0x9CA6A6).m();
    public static final NTMMaterial MAT_CDALLOY = makeSmeltable(_AS + 13, "CdAlloy", 0xF7DF8F, 0x604308, 0xFBD368).m();
    public static final NTMMaterial MAT_BBRONZE = makeSmeltable(_AS + 16, "BismuthBronze", "BBronze", 0xE19A69, 0x485353, 0x987D65).m();
    public static final NTMMaterial MAT_ABRONZE = makeSmeltable(_AS + 17, "ArsenicBronze", "ABronze", 0xDB9462, 0x203331, 0x77644D).m();
    public static final NTMMaterial MAT_MAGTUNG = makeSmeltable(_AS + 8, "MagnetizedTungsten", "MagTung", 0x22A2A2, 0x0F0F0F, 0x22A2A2).m();
    public static final NTMMaterial MAT_CMB = makeSmeltable(_AS + 9, "CMBSteel", "CMB", 0x6F6FB4, 0x000011, 0x6F6FB4).m();
    public static final NTMMaterial MAT_DNT = makeSmeltable(_AS + 15, "Dineutronium", "DNT", 0x7582B9, 0x16000E, 0x455289).m();
    public static final NTMMaterial MAT_FLUX = makeAdditive(_AS + 10, "Flux", 0xF1E0BB, 0x6F6256, 0xDECCAD).n();
    public static final NTMMaterial MAT_SLAG = makeSmeltable(_AS + 11, "Slag", 0x554940, 0x34281F, 0x6C6562).setAutogen(INGOT, BLOCK).n();
    public static final NTMMaterial MAT_MUD = makeSmeltable(_AS + 14, "Mud", 0xBCB5A9, 0x481213, 0x96783B).n();
    public static final NTMMaterial MAT_BSCCO = makeSmeltable(_AS + 18, "BSCCO", 0x767BF1, 0x000000, 0x5E62C0).m();
    public static final NTMMaterial MAT_GUNMETAL = makeSmeltable(_AS + 19, "Gunmetal", 0xFFEF3F, 0xAD3600, 0xF9C62C).n();
    public static final NTMMaterial MAT_WEAPONSTEEL = makeSmeltable(_AS + 20, "WeaponSteel", 0xA0A0A0, 0x000000, 0x808080).n();
    public static final NTMMaterial MAT_SATURN = makeSmeltable(_AS + 4, "Saturnite", "BigMT", 0x3AC4DA, 0x09282C, 0x30A4B7).m();
    public static final NTMMaterial MAT_RAREEARTH = makeNonSmeltable(_ES, "RareEarth", "RareEarth", 0xC1BDBD, 0x384646, 0x7B7F7F).n();
    public static final NTMMaterial MAT_POLYMER = makeNonSmeltable(_ES + 1, "Polymer", 0x363636, 0x040404, 0x272727).setAutogen(STOCK, GRIP).n();
    public static final NTMMaterial MAT_BAKELITE = makeNonSmeltable(_ES + 2, "Bakelite", 0xF28086, 0x2B0608, 0xC93940).setAutogen(STOCK, GRIP).n();
    public static final NTMMaterial MAT_RUBBER = makeNonSmeltable(_ES + 3, "Rubber", 0x817F75, 0x0F0D03, 0x4B4A3F).setAutogen(PIPE, GRIP).n();
    public static final NTMMaterial MAT_HARDPLASTIC = makeNonSmeltable(_ES + 4, "Polycarbonate", 0xEDE7C4, 0x908A67, 0xE1DBB8).setAutogen(STOCK, GRIP).n();
    public static final NTMMaterial MAT_PVC = makeNonSmeltable(_ES + 5, "PVC", 0xFCFCFC, 0x9F9F9F, 0xF0F0F0).setAutogen(STOCK, GRIP).n();

    static {
        registerLegacyShapePrefixes();
        registerModernPrefix("ingot_", INGOT);
        registerModernPrefix("plate_cast_", CASTPLATE);
        registerModernPrefix("plate_welded_", WELDEDPLATE);
        registerModernPrefix("plate_", PLATE);
        registerModernPrefix("dust_tiny_", DUSTTINY);
        registerModernPrefix("dust_", DUST);
        registerModernPrefix("powder_tiny_", DUSTTINY);
        registerModernPrefix("powder_", DUST);
        registerModernPrefix("nugget_", NUGGET);
        registerModernPrefix("billet_", BILLET);
        registerModernPrefix("bolt_", BOLT);
        registerModernPrefix("shell_", SHELL);
        registerModernPrefix("pipes_", PIPE);
        registerModernPrefix("wire_dense_", DENSEWIRE);
        registerModernPrefix("wire_fine_", WIRE);
        registerModernPrefix("wire_", WIRE);
        registerModernPrefix("barrel_light_", LIGHTBARREL);
        registerModernPrefix("barrel_heavy_", HEAVYBARREL);
        registerModernPrefix("receiver_light_", LIGHTRECEIVER);
        registerModernPrefix("receiver_heavy_", HEAVYRECEIVER);
        registerModernPrefix("mechanism_", MECHANISM);
        registerModernPrefix("stock_", STOCK);
        registerModernPrefix("grip_", GRIP);
        registerModernPrefix("block_", BLOCK);

        registerModernMaterial(MAT_IRON, "iron");
        registerModernMaterial(MAT_WOOD, "wood");
        registerModernMaterial(MAT_IVORY, "ivory");
        registerModernMaterial(MAT_COAL, "coal");
        registerModernMaterial(MAT_LIGNITE, "lignite");
        registerModernMaterial(MAT_GRAPHITE, "graphite");
        registerModernMaterial(MAT_DIAMOND, "diamond");
        registerModernMaterial(MAT_GOLD, "gold");
        registerModernMaterial(MAT_REDSTONE, "redstone");
        registerModernMaterial(MAT_OBSIDIAN, "obsidian");
        registerModernMaterial(MAT_HEMATITE, "hematite");
        registerModernMaterial(MAT_METEORICIRON, "meteorite");
        registerModernMaterial(MAT_COPPER, "copper");
        registerModernMaterial(MAT_MALACHITE, "malachite");
        registerModernMaterial(MAT_BAUXITE, "bauxite");
        registerModernMaterial(MAT_CRYOLITE, "cryolite");
        registerModernMaterial(MAT_TITANIUM, "titanium");
        registerModernMaterial(MAT_TUNGSTEN, "tungsten");
        registerModernMaterial(MAT_ALUMINIUM, "aluminium", "aluminum");
        registerModernMaterial(MAT_LEAD, "lead");
        registerModernMaterial(MAT_BISMUTH, "bismuth");
        registerModernMaterial(MAT_TANTALIUM, "tantalium", "tantalum");
        registerModernMaterial(MAT_NEODYMIUM, "neodymium");
        registerModernMaterial(MAT_NIOBIUM, "niobium");
        registerModernMaterial(MAT_BERYLLIUM, "beryllium");
        registerModernMaterial(MAT_EMERALD, "emerald");
        registerModernMaterial(MAT_COBALT, "cobalt");
        registerModernMaterial(MAT_BORON, "boron");
        registerModernMaterial(MAT_BORAX, "borax");
        registerModernMaterial(MAT_LANTHANIUM, "lanthanum", "lanthanium");
        registerModernMaterial(MAT_ZIRCONIUM, "zirconium");
        registerModernMaterial(MAT_SODALITE, "sodalite");
        registerModernMaterial(MAT_LITHIUM, "lithium");
        registerModernMaterial(MAT_SULFUR, "sulfur", "sulphur");
        registerModernMaterial(MAT_KNO, "saltpeter", "niter", "kno");
        registerModernMaterial(MAT_FLUORITE, "fluorite");
        registerModernMaterial(MAT_PHOSPHORUS, "red_phosphorus", "redphosphorus", "phosphorus");
        registerModernMaterial(MAT_CHLOROCALCITE, "chlorocalcite");
        registerModernMaterial(MAT_MOLYSITE, "molysite");
        registerModernMaterial(MAT_CINNABAR, "cinnabar");
        registerModernMaterial(MAT_ASBESTOS, "asbestos");
        registerModernMaterial(MAT_OSMIRIDIUM, "osmiridium");
        registerModernMaterial(MAT_SILICON, "silicon");
        registerModernMaterial(MAT_URANIUM, "uranium");
        registerModernMaterial(MAT_U233, "u233", "uranium_233");
        registerModernMaterial(MAT_U235, "u235", "uranium_235");
        registerModernMaterial(MAT_U238, "u238", "uranium_238");
        registerModernMaterial(MAT_THORIUM, "th232", "thorium_232", "thorium");
        registerModernMaterial(MAT_PLUTONIUM, "plutonium");
        registerModernMaterial(MAT_RGP, "pu_mix", "plutonium_rg");
        registerModernMaterial(MAT_PU238, "pu238", "plutonium_238");
        registerModernMaterial(MAT_PU239, "pu239", "plutonium_239");
        registerModernMaterial(MAT_PU240, "pu240", "plutonium_240");
        registerModernMaterial(MAT_PU241, "pu241", "plutonium_241");
        registerModernMaterial(MAT_RGA, "am_mix", "americium_rg");
        registerModernMaterial(MAT_AM241, "am241", "americium_241");
        registerModernMaterial(MAT_AM242, "am242", "americium_242");
        registerModernMaterial(MAT_NEPTUNIUM, "neptunium", "neptunium_237");
        registerModernMaterial(MAT_POLONIUM, "po210", "polonium_210", "polonium");
        registerModernMaterial(MAT_TECHNETIUM, "tc99", "technetium");
        registerModernMaterial(MAT_RADIUM, "ra226", "radium_226", "radium");
        registerModernMaterial(MAT_ACTINIUM, "actinium", "actinium_227");
        registerModernMaterial(MAT_CO60, "co60", "cobalt_60");
        registerModernMaterial(MAT_AU198, "au198", "gold_198");
        registerModernMaterial(MAT_PB209, "pb209", "lead_209");
        registerModernMaterial(MAT_SCHRABIDIUM, "sa326", "schrabidium");
        registerModernMaterial(MAT_SOLINIUM, "solinium");
        registerModernMaterial(MAT_SCHRABIDATE, "schrabidate");
        registerModernMaterial(MAT_SCHRARANIUM, "schraranium");
        registerModernMaterial(MAT_GHIORSIUM, "gh336", "ghiorsium336");
        registerModernMaterial(MAT_ARSENIC, "arsenic");
        registerModernMaterial(MAT_STRONTIUM, "strontium");
        registerModernMaterial(MAT_CALCIUM, "calcium");
        registerModernMaterial(MAT_SODIUM, "sodium");
        registerModernMaterial(MAT_CADMIUM, "cadmium");
        registerModernMaterial(MAT_STEEL, "steel");
        registerModernMaterial(MAT_MINGRADE, "mingrade", "red_copper", "redcopper");
        registerModernMaterial(MAT_DURA, "dura", "dura_steel", "durasteel");
        registerModernMaterial(MAT_DESH, "desh");
        registerModernMaterial(MAT_STAR, "star", "starmetal");
        registerModernMaterial(MAT_FERRO, "ferro", "ferrouranium");
        registerModernMaterial(MAT_TCALLOY, "tcalloy", "tc_alloy");
        registerModernMaterial(MAT_CDALLOY, "cdalloy", "cd_alloy");
        registerModernMaterial(MAT_BBRONZE, "bbronze", "bismuth_bronze");
        registerModernMaterial(MAT_ABRONZE, "abronze", "arsenic_bronze");
        registerModernMaterial(MAT_MAGTUNG, "magtung", "magnetized_tungsten");
        registerModernMaterial(MAT_CMB, "cmb", "cmbsteel", "combine_steel");
        registerModernMaterial(MAT_DNT, "dnt", "dineutronium");
        registerModernMaterial(MAT_FLUX, "flux");
        registerModernMaterial(MAT_SLAG, "slag");
        registerModernMaterial(MAT_MUD, "mud");
        registerModernMaterial(MAT_BSCCO, "bscco");
        registerModernMaterial(MAT_GUNMETAL, "gunmetal");
        registerModernMaterial(MAT_WEAPONSTEEL, "weaponsteel", "weapon_steel");
        registerModernMaterial(MAT_SATURN, "saturn", "saturnite", "bigmt");
        registerModernMaterial(MAT_RAREEARTH, "rareearth", "rare_earth");
        registerModernMaterial(MAT_POLYMER, "polymer");
        registerModernMaterial(MAT_BAKELITE, "bakelite");
        registerModernMaterial(MAT_RUBBER, "rubber");
        registerModernMaterial(MAT_HARDPLASTIC, "pc", "polycarbonate");
        registerModernMaterial(MAT_PVC, "pvc");
    }

    public static NTMMaterial make(int id, String... names) {
        return new NTMMaterial(id, names);
    }

    public static NTMMaterial makeSmeltable(int id, String name, int color) {
        return makeSmeltable(id, new String[] { name }, color, color, color);
    }

    public static NTMMaterial makeSmeltable(int id, String name, int solidColorLight, int solidColorDark, int moltenColor) {
        return makeSmeltable(id, new String[] { name }, solidColorLight, solidColorDark, moltenColor);
    }

    public static NTMMaterial makeSmeltable(int id, String name, String alias, int solidColorLight, int solidColorDark, int moltenColor) {
        return makeSmeltable(id, new String[] { name, alias }, solidColorLight, solidColorDark, moltenColor);
    }

    public static NTMMaterial makeSmeltable(int id, String name, String aliasA, String aliasB, int solidColorLight, int solidColorDark, int moltenColor) {
        return makeSmeltable(id, new String[] { name, aliasA, aliasB }, solidColorLight, solidColorDark, moltenColor);
    }

    public static NTMMaterial makeSmeltable(int id, String[] names, int solidColorLight, int solidColorDark, int moltenColor) {
        return make(id, names).smeltable(SmeltingBehavior.SMELTABLE)
                .setSolidColor(solidColorLight, solidColorDark)
                .setMoltenColor(moltenColor);
    }

    public static NTMMaterial makeNonSmeltable(int id, String name, int solidColorLight, int solidColorDark, int moltenColor) {
        return makeNonSmeltable(id, new String[] { name }, solidColorLight, solidColorDark, moltenColor);
    }

    public static NTMMaterial makeNonSmeltable(int id, String name, String alias, int solidColorLight, int solidColorDark, int moltenColor) {
        return makeNonSmeltable(id, new String[] { name, alias }, solidColorLight, solidColorDark, moltenColor);
    }

    public static NTMMaterial makeNonSmeltable(int id, String name, String aliasA, String aliasB, int solidColorLight, int solidColorDark, int moltenColor) {
        return makeNonSmeltable(id, new String[] { name, aliasA, aliasB }, solidColorLight, solidColorDark, moltenColor);
    }

    public static NTMMaterial makeNonSmeltable(int id, String[] names, int solidColorLight, int solidColorDark, int moltenColor) {
        return make(id, names).setSolidColor(solidColorLight, solidColorDark).setMoltenColor(moltenColor);
    }

    public static NTMMaterial makeAdditive(int id, String name, int solidColorLight, int solidColorDark, int moltenColor) {
        return make(id, name).smeltable(SmeltingBehavior.ADDITIVE)
                .setSolidColor(solidColorLight, solidColorDark)
                .setMoltenColor(moltenColor);
    }

    static void register(NTMMaterial material) {
        orderedList.add(material);
        matById.put(material.id, material);
        for (String name : material.names) {
            matByName.put(name, material);
        }
    }

    private static void registerModernPrefix(String prefix, MaterialShapes shape) {
        MODERN_PATH_PREFIXES.put(prefix, shape);
        MODERN_SHAPE_PATH_PREFIXES.computeIfAbsent(shape, key -> new ArrayList<>()).add(prefix);
    }

    private static void registerLegacyShapePrefixes() {
        for (MaterialShapes shape : MaterialShapes.allShapes) {
            if (shape.prefixes == null) {
                continue;
            }
            for (String prefix : shape.prefixes) {
                prefixByName.put(prefix, shape);
            }
        }
    }

    private static void registerModernMaterial(NTMMaterial material, String... names) {
        for (String name : names) {
            String pathName = name.toLowerCase(Locale.ROOT);
            MODERN_PATH_MATERIALS.put(pathName, material);
            MODERN_MATERIAL_PATH_NAMES.computeIfAbsent(material, key -> new ArrayList<>()).add(pathName);
        }
    }

    public static List<String> modernPathPrefixes(MaterialShapes shape) {
        List<String> prefixes = MODERN_SHAPE_PATH_PREFIXES.get(shape);
        return prefixes == null ? List.of() : List.copyOf(prefixes);
    }

    public static List<String> modernPathNames(NTMMaterial material) {
        if (material == null) {
            return List.of();
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        List<String> registered = MODERN_MATERIAL_PATH_NAMES.get(material);
        if (registered != null) {
            names.addAll(registered);
        }
        for (String name : material.names) {
            names.add(name.toLowerCase(Locale.ROOT));
        }
        return List.copyOf(names);
    }

    public static List<MaterialStack> getMaterialsFromItem(ItemStack stack) {
        List<MaterialStack> materials = new ArrayList<>();
        if (stack == null || stack.isEmpty()) {
            return materials;
        }
        MaterialStack scrap = FoundryScrapsItem.getMaterial(stack);
        if (scrap != null && !scrap.isEmpty()) {
            materials.add(scrap);
            return materials;
        }
        MaterialStack bedrockFragment = BedrockOreFragmentItem.getMaterialStack(stack);
        if (bedrockFragment != null && !bedrockFragment.isEmpty()) {
            materials.add(bedrockFragment);
            return materials;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            return materials;
        }
        MaterialStack direct = materialFromPath(key);
        if (direct != null && !direct.isEmpty()) {
            materials.add(direct);
        }
        return materials;
    }

    public static List<MaterialStack> getSmeltingMaterialsFromItem(ItemStack stack) {
        List<MaterialStack> base = getMaterialsFromItem(stack);
        List<MaterialStack> smelting = new ArrayList<>();
        for (MaterialStack material : base) {
            if (material == null || material.material == null) {
                continue;
            }
            NTMMaterial smeltsInto = material.material.smeltsInto == null ? material.material : material.material.smeltsInto;
            if (smeltsInto.smeltable == SmeltingBehavior.SMELTABLE
                    || smeltsInto.smeltable == SmeltingBehavior.ADDITIVE) {
                smelting.add(new MaterialStack(smeltsInto, material.amount * material.material.convOut / material.material.convIn));
            }
        }
        return smelting;
    }

    private static MaterialStack materialFromPath(ResourceLocation key) {
        String namespace = key.getNamespace();
        String path = key.getPath().toLowerCase(Locale.ROOT);
        if ("minecraft".equals(namespace)) {
            if ("iron_ingot".equals(path)) {
                return new MaterialStack(MAT_IRON, INGOT.q(1));
            }
            if ("gold_ingot".equals(path)) {
                return new MaterialStack(MAT_GOLD, INGOT.q(1));
            }
            if ("copper_ingot".equals(path)) {
                return new MaterialStack(MAT_COPPER, INGOT.q(1));
            }
            if ("redstone".equals(path)) {
                return new MaterialStack(MAT_REDSTONE, DUST.q(1));
            }
            if ("iron_block".equals(path)) {
                return new MaterialStack(MAT_IRON, BLOCK.q(1));
            }
            if ("gold_block".equals(path)) {
                return new MaterialStack(MAT_GOLD, BLOCK.q(1));
            }
            if ("copper_block".equals(path)) {
                return new MaterialStack(MAT_COPPER, BLOCK.q(1));
            }
            return null;
        }
        NTMMaterial bareIngot = "lithium".equals(path) ? MAT_LITHIUM : null;
        if (bareIngot != null) {
            return new MaterialStack(bareIngot, INGOT.q(1));
        }
        if ("stone_resource_hematite".equals(path)) {
            return new MaterialStack(MAT_HEMATITE, INGOT.q(1));
        }
        if ("stone_resource_malachite".equals(path)) {
            return new MaterialStack(MAT_MALACHITE, INGOT.q(6));
        }
        if ("chunk_ore_malachite".equals(path)) {
            return new MaterialStack(MAT_MALACHITE, INGOT.q(1));
        }
        MaterialStack best = null;
        int bestPrefixLength = -1;
        for (Map.Entry<String, MaterialShapes> entry : MODERN_PATH_PREFIXES.entrySet()) {
            String prefix = entry.getKey();
            if (path.startsWith(prefix)) {
                NTMMaterial material = MODERN_PATH_MATERIALS.get(path.substring(prefix.length()));
                if (material != null && prefix.length() > bestPrefixLength) {
                    best = new MaterialStack(material, entry.getValue().q(1));
                    bestPrefixLength = prefix.length();
                }
            }
        }
        return best;
    }

    public static String formatAmount(int amount, boolean showInMb) {
        if (showInMb) {
            return amount * 2 + "mB";
        }
        int blocks = amount / BLOCK.q(1);
        amount -= blocks * BLOCK.q(1);
        int ingots = amount / INGOT.q(1);
        amount -= ingots * INGOT.q(1);
        int nuggets = amount / NUGGET.q(1);
        amount -= nuggets * NUGGET.q(1);
        StringBuilder format = new StringBuilder();
        if (blocks > 0) {
            format.append(blocks).append(" Blocks ");
        }
        if (ingots > 0) {
            format.append(ingots).append(" Ingots ");
        }
        if (nuggets > 0) {
            format.append(nuggets).append(" Nuggets ");
        }
        if (amount > 0 || format.length() == 0) {
            format.append(amount).append(" Quanta");
        }
        return format.toString().trim();
    }

    public static class MaterialStack {
        public final NTMMaterial material;
        public int amount;

        public MaterialStack(NTMMaterial material, int amount) {
            this.material = material;
            this.amount = Math.max(0, amount);
        }

        public MaterialStack copy() {
            return new MaterialStack(material, amount);
        }

        public boolean isEmpty() {
            return material == null || amount <= 0;
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            if (material != null) {
                tag.putInt("mat", material.id);
                tag.putString("name", material.names[0]);
            }
            tag.putInt("amount", amount);
            return tag;
        }

        public static MaterialStack fromNBT(CompoundTag tag) {
            if (tag == null || tag.isEmpty()) {
                return null;
            }
            NTMMaterial material = tag.contains("mat") ? matById.get(tag.getInt("mat")) : null;
            if (material == null && tag.contains("name")) {
                material = matByName.get(tag.getString("name"));
            }
            if (material == null) {
                return null;
            }
            return new MaterialStack(material, tag.getInt("amount"));
        }
    }

    public static ListTag writeList(List<MaterialStack> stacks) {
        ListTag list = new ListTag();
        if (stacks != null) {
            for (MaterialStack stack : stacks) {
                if (stack != null && !stack.isEmpty()) {
                    list.add(stack.serializeNBT());
                }
            }
        }
        return list;
    }

    public static List<MaterialStack> readList(ListTag list) {
        List<MaterialStack> stacks = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getId() == Tag.TAG_COMPOUND) {
                    MaterialStack stack = MaterialStack.fromNBT(list.getCompound(i));
                    if (stack != null && !stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
        }
        return stacks;
    }
}
