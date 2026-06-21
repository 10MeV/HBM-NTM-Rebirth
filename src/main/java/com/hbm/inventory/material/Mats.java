package com.hbm.inventory.material;

import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.item.FoundryScrapsItem;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static final int _VS = 0;
    public static final int _AS = 30;
    public static final int _ES = 20_000;

    public static final NTMMaterial MAT_STONE = makeSmeltable(_VS, "Stone", 0x7F7F7F, 0x353535, 0x4D2F23).n();
    public static final NTMMaterial MAT_CARBON = makeAdditive(699, "Carbon", 0x363636, 0x030303, 0x404040).n();
    public static final NTMMaterial MAT_IRON = makeSmeltable(2600, "Iron", 0xFFFFFF, 0x353535, 0xFFA259).m();
    public static final NTMMaterial MAT_GOLD = makeSmeltable(7900, "Gold", 0xFFFF8B, 0xC26E00, 0xE8D754).m();
    public static final NTMMaterial MAT_REDSTONE = makeSmeltable(_VS + 1, "Redstone", 0xE3260C, 0x700E06, 0xFF1000).n();
    public static final NTMMaterial MAT_HEMATITE = makeAdditive(2601, "Hematite", 0xDFB7AE, 0x5F372E, 0x6E463D).m();
    public static final NTMMaterial MAT_WROUGHTIRON = makeSmeltable(2602, "WroughtIron", 0xFAAB89).m();
    public static final NTMMaterial MAT_PIGIRON = makeSmeltable(2603, "PigIron", 0xFF8B59).m();
    public static final NTMMaterial MAT_TITANIUM = makeSmeltable(2200, "Titanium", "Ti", 0xF7F3F2, 0x4F4C4B, 0xA99E79).m();
    public static final NTMMaterial MAT_COPPER = makeSmeltable(2900, "Copper", "Cu", 0xFDCA88, 0x601E0D, 0xC18336).m();
    public static final NTMMaterial MAT_TUNGSTEN = makeSmeltable(7400, "Tungsten", "W", 0x868686, 0x000000, 0x977474).m();
    public static final NTMMaterial MAT_ALUMINIUM = makeSmeltable(1300, "Aluminium", "Aluminum", "Al", 0xFFFFFF, 0x344550, 0xD0B8EB).m();
    public static final NTMMaterial MAT_LEAD = makeSmeltable(8200, "Lead", "Pb", 0xA6A6B2, 0x03030F, 0x646470).m();
    public static final NTMMaterial MAT_BISMUTH = makeSmeltable(8300, "Bismuth", "Bi", 0xB200FF, 0xB200FF, 0xB200FF).m();
    public static final NTMMaterial MAT_TANTALIUM = makeSmeltable(7300, "Tantalium", "Tantalum", "Ta", 0xFFFFFF, 0x1D1D36, 0xA89B74).m();
    public static final NTMMaterial MAT_NEODYMIUM = makeSmeltable(6000, "Neodymium", "Nd", 0xE6E6B6, 0x1C1C00, 0x8F8F5F).m();
    public static final NTMMaterial MAT_NIOBIUM = makeSmeltable(4100, "Niobium", "Nb", 0xB76EC9, 0x2F2D42, 0xD576B1).m();
    public static final NTMMaterial MAT_BERYLLIUM = makeSmeltable(400, "Beryllium", "Be", 0xB2B2A6, 0x0F0F03, 0xAE9572).m();
    public static final NTMMaterial MAT_COBALT = makeSmeltable(2700, "Cobalt", "Co", 0xC2D1EE, 0x353554, 0x8F72AE).m();
    public static final NTMMaterial MAT_BORON = makeSmeltable(500, "Boron", "B", 0xBDC8D2, 0x29343E, 0xAD72AE).m();
    public static final NTMMaterial MAT_ZIRCONIUM = makeSmeltable(4000, "Zirconium", "Zr", 0xE3DCBE, 0x3E3719, 0xADA688).m();
    public static final NTMMaterial MAT_LITHIUM = makeSmeltable(300, "Lithium", "Li", 0xFFFFFF, 0x818181, 0xD6D6D6).m();
    public static final NTMMaterial MAT_ASBESTOS = makeSmeltable(1401, "Asbestos", 0xD8D9CF, 0x616258, 0xB0B3A8).n();
    public static final NTMMaterial MAT_SILICON = makeSmeltable(1400, "Silicon", "Si", 0xD1D7DF, 0x1A1A3D, 0x878B9E).m();
    public static final NTMMaterial MAT_URANIUM = makeSmeltable(9200, "Uranium", "U", 0xC1C7BD, 0x2B3227, 0x9AA196).m();
    public static final NTMMaterial MAT_U238 = makeSmeltable(9238, "Uranium238", "U238", 0xC1C7BD, 0x2B3227, 0x9AA196).m();
    public static final NTMMaterial MAT_THORIUM = makeSmeltable(9032, "Thorium232", "Th232", "Thorium", 0xBF825F, 0x1C0000, 0xBF825F).m();
    public static final NTMMaterial MAT_PLUTONIUM = makeSmeltable(9400, "Plutonium", "Pu", 0x9AA3A0, 0x111A17, 0x78817E).m();
    public static final NTMMaterial MAT_POLONIUM = makeSmeltable(8410, "Polonium210", "Po210", "Polonium", 0x968779, 0x3D1509, 0x715E4A).m();
    public static final NTMMaterial MAT_TECHNETIUM = makeSmeltable(4399, "Tc99", "Technetium", 0xFAFFFF, 0x576C6C, 0xCADFDF).m();
    public static final NTMMaterial MAT_RADIUM = makeSmeltable(8826, "Radium226", "Ra226", "Radium", 0xFCFCFC, 0xADBFBA, 0xE9FAF6).m();
    public static final NTMMaterial MAT_SCHRABIDIUM = makeSmeltable(12626, "Schrabidium", "Sa326", 0x32FFFF, 0x005C5C, 0x32FFFF).m();
    public static final NTMMaterial MAT_ARSENIC = makeSmeltable(3300, "Arsenic", "As", 0x6CBABA, 0x242525, 0x558080).m();
    public static final NTMMaterial MAT_STRONTIUM = makeSmeltable(3800, "Strontium", "Sr", 0xF1E8BA, 0x271E00, 0xCAC193).m();
    public static final NTMMaterial MAT_CALCIUM = makeSmeltable(2000, "Calcium", "Ca", 0xCFCFA6, 0x747F6E, 0xB7B784).m();
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
    public static final NTMMaterial MAT_SLAG = makeSmeltable(_AS + 11, "Slag", 0x554940, 0x34281F, 0x6C6562).n();
    public static final NTMMaterial MAT_MUD = makeSmeltable(_AS + 14, "Mud", 0xBCB5A9, 0x481213, 0x96783B).n();
    public static final NTMMaterial MAT_BSCCO = makeSmeltable(_AS + 18, "BSCCO", 0x767BF1, 0x000000, 0x5E62C0).m();
    public static final NTMMaterial MAT_GUNMETAL = makeSmeltable(_AS + 19, "Gunmetal", 0xFFEF3F, 0xAD3600, 0xF9C62C).n();
    public static final NTMMaterial MAT_WEAPONSTEEL = makeSmeltable(_AS + 20, "WeaponSteel", 0xA0A0A0, 0x000000, 0x808080).n();
    public static final NTMMaterial MAT_SATURN = makeSmeltable(_AS + 4, "Saturnite", "BigMT", 0x3AC4DA, 0x09282C, 0x30A4B7).m();

    static {
        registerModernPrefix("ingot_", INGOT);
        registerModernPrefix("plate_cast_", CASTPLATE);
        registerModernPrefix("plate_welded_", WELDEDPLATE);
        registerModernPrefix("plate_", PLATE);
        registerModernPrefix("dust_tiny_", DUSTTINY);
        registerModernPrefix("dust_", DUST);
        registerModernPrefix("nugget_", NUGGET);
        registerModernPrefix("bolt_", BOLT);
        registerModernPrefix("wire_dense_", DENSEWIRE);
        registerModernPrefix("wire_", WIRE);
        registerModernPrefix("block_", BLOCK);

        registerModernMaterial(MAT_IRON, "iron");
        registerModernMaterial(MAT_GOLD, "gold");
        registerModernMaterial(MAT_COPPER, "copper");
        registerModernMaterial(MAT_TITANIUM, "titanium");
        registerModernMaterial(MAT_TUNGSTEN, "tungsten");
        registerModernMaterial(MAT_ALUMINIUM, "aluminium", "aluminum");
        registerModernMaterial(MAT_LEAD, "lead");
        registerModernMaterial(MAT_BISMUTH, "bismuth");
        registerModernMaterial(MAT_TANTALIUM, "tantalium", "tantalum");
        registerModernMaterial(MAT_NEODYMIUM, "neodymium");
        registerModernMaterial(MAT_NIOBIUM, "niobium");
        registerModernMaterial(MAT_BERYLLIUM, "beryllium");
        registerModernMaterial(MAT_COBALT, "cobalt");
        registerModernMaterial(MAT_BORON, "boron");
        registerModernMaterial(MAT_ZIRCONIUM, "zirconium");
        registerModernMaterial(MAT_LITHIUM, "lithium");
        registerModernMaterial(MAT_ASBESTOS, "asbestos");
        registerModernMaterial(MAT_SILICON, "silicon");
        registerModernMaterial(MAT_URANIUM, "uranium");
        registerModernMaterial(MAT_U238, "u238", "uranium_238");
        registerModernMaterial(MAT_THORIUM, "th232", "thorium_232", "thorium");
        registerModernMaterial(MAT_PLUTONIUM, "plutonium");
        registerModernMaterial(MAT_POLONIUM, "po210", "polonium_210", "polonium");
        registerModernMaterial(MAT_TECHNETIUM, "tc99", "technetium");
        registerModernMaterial(MAT_RADIUM, "ra226", "radium_226", "radium");
        registerModernMaterial(MAT_SCHRABIDIUM, "sa326", "schrabidium");
        registerModernMaterial(MAT_ARSENIC, "arsenic");
        registerModernMaterial(MAT_STRONTIUM, "strontium");
        registerModernMaterial(MAT_CALCIUM, "calcium");
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
    }

    private static void registerModernMaterial(NTMMaterial material, String... names) {
        for (String name : names) {
            MODERN_PATH_MATERIALS.put(name.toLowerCase(Locale.ROOT), material);
        }
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
        for (Map.Entry<String, MaterialShapes> entry : MODERN_PATH_PREFIXES.entrySet()) {
            String prefix = entry.getKey();
            if (path.startsWith(prefix)) {
                NTMMaterial material = MODERN_PATH_MATERIALS.get(path.substring(prefix.length()));
                return material == null ? null : new MaterialStack(material, entry.getValue().q(1));
            }
        }
        return null;
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
