package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class HbmLanguageProvider extends LanguageProvider {
    public HbmLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.hbm.parts", "HBM Parts");
        add("itemGroup.hbm.machines", "HBM Machines");
        add("itemGroup.hbm.consumables", "HBM Consumables");
        add("itemGroup.hbm.control", "HBM Control");
        add("itemGroup.hbm.nukes", "HBM Nukes");
        add("item.hbm.ingot_uranium", "Uranium Ingot");
        add("item.hbm.ingot_u233", "Uranium-233 Ingot");
        add("item.hbm.ingot_u235", "Uranium-235 Ingot");
        add("item.hbm.ingot_u238", "Uranium-238 Ingot");
        add("item.hbm.ingot_plutonium", "Plutonium Ingot");
        add("item.hbm.ingot_pu238", "Plutonium-238 Ingot");
        add("item.hbm.ingot_pu239", "Plutonium-239 Ingot");
        add("item.hbm.ingot_pu240", "Plutonium-240 Ingot");
        add("item.hbm.ingot_pu241", "Plutonium-241 Ingot");
        add("item.hbm.ingot_neptunium", "Neptunium Ingot");
        add("item.hbm.ingot_polonium", "Polonium-210 Ingot");
        add("item.hbm.ingot_th232", "Thorium-232 Ingot");
        add("item.hbm.ingot_titanium", "Titanium Ingot");
        add("item.hbm.ingot_tungsten", "Tungsten Ingot");
        add("item.hbm.ingot_copper", "Industrial Grade Copper");
        add("item.hbm.ingot_lead", "Lead Ingot");
        add("item.hbm.ingot_steel", "Steel Ingot");
        add("item.hbm.ingot_cobalt", "Cobalt Ingot");
        add("item.hbm.ingot_aluminium", "Aluminium Ingot");
        add("item.hbm.ingot_beryllium", "Beryllium Ingot");
        add("item.hbm.ingot_schrabidium", "Schrabidium Ingot");
        add("item.hbm.ingot_advanced_alloy", "Advanced Alloy Ingot");
        add("item.hbm.plate_steel", "Steel Plate");
        add("item.hbm.plate_iron", "Iron Plate");
        add("item.hbm.plate_copper", "Copper Plate");
        add("item.hbm.plate_lead", "Lead Plate");
        add("item.hbm.plate_titanium", "Titanium Plate");
        add("item.hbm.plate_aluminium", "Aluminium Plate");
        add("item.hbm.powder_uranium", "Uranium Powder");
        add("item.hbm.powder_plutonium", "Plutonium Powder");
        add("item.hbm.powder_thorium", "Thorium Powder");
        add("item.hbm.powder_titanium", "Titanium Powder");
        add("item.hbm.powder_tungsten", "Tungsten Powder");
        add("item.hbm.powder_copper", "Copper Powder");
        add("item.hbm.powder_iron", "Iron Powder");
        add("item.hbm.powder_steel", "Steel Powder");
        add("item.hbm.powder_lead", "Lead Powder");
        add("item.hbm.coil_copper", "Copper Coil");
        add("item.hbm.coil_tungsten", "Heating Coil");
        add("item.hbm.coil_gold", "Gold Coil");
        add("item.hbm.motor", "Motor");
        add("item.hbm.stamp_iron_plate", "Plate Stamp (Iron)");
        add("item.hbm.geiger_counter", "Geiger Counter");
        add("item.hbm.digamma_diagnostic", "Digamma Diagnostic");
        add("item.hbm.radaway", "RadAway");
        add("item.hbm.radaway_strong", "RadAway Strong");
        add("item.hbm.radaway_flush", "RadAway Flush");
        add("item.hbm.radx", "Rad-X");
        add("item.hbm.radx.desc", "Increases radiation resistance by 0.2 (37%) for 3 minutes");
        add("item.hbm.containment_box", "Lead-Lined Box");
        add("item.hbm.plastic_bag", "Plastic Bag");
        add("item.hbm.toolbox", "Toolbox");
        add("item.hbm.toolbox.desc.swap", "Click with the toolbox to swap hotbars in/out of the toolbox.");
        add("item.hbm.toolbox.desc.open", "Shift-click with the toolbox to open the toolbox.");
        add("desc.item.wasteCooling", "Cooling down");
        add("effect.hbm.radiation", "Radiation");
        add("effect.hbm.radaway", "RadAway");
        add("effect.hbm.radx", "Rad-X");
        add("effect.hbm.mutation", "Tainted Heart");
        add("effect.hbm.stability", "Stability");
        add("geiger.title", "Geiger Counter");
        add("geiger.chunkRad", "Chunk radiation: %s RAD/s");
        add("geiger.envRad", "Environmental dose: %s RAD/s");
        add("geiger.playerRad", "Player dose: %s RAD");
        add("geiger.playerRes", "Radiation resistance: %s%%");
        add("digamma.title", "DIGAMMA DIAGNOSTIC");
        add("digamma.playerDigamma", "Digamma exposure: %s DRX");
        add("digamma.playerHealth", "Digamma influence: %s%%");
        add("digamma.playerRes", "Digamma resistance: %s");
        add("tooltip.hbm.radiation.single", "Radiation: %s RAD/s");
        add("tooltip.hbm.radiation.total", "Stack total: %s RAD/s");
        add("tooltip.hbm.radiation.resistance", "Radiation resistance: %s (%s%% blocked)");
        add("tooltip.hbm.hazard.digamma", "Digamma: %s DRX");
        add("tooltip.hbm.hazard.hot", "Heat: %s");
        add("tooltip.hbm.hazard.blinding", "Blinding: %s");
        add("tooltip.hbm.hazard.asbestos", "Asbestos: %s");
        add("tooltip.hbm.hazard.coal", "Coal dust: %s");
        add("tooltip.hbm.hazard.hydroactive", "Hydroactive: %s");
        add("tooltip.hbm.hazard.explosive", "Explosive: %s");
        add("block.hbm.machine_press", "Burner Press");
        add("subtitles.hbm.block.press_operate", "Burner Press operates");
        add("subtitles.hbm.tool.geiger", "Geiger counter clicks");
        add("subtitles.hbm.tool.tech_boop", "Device beeps");
        add("subtitles.hbm.tool.radaway", "RadAway injector hisses");
        add("block.hbm.machine_difurnace_off", "Blast Furnace");
        add("block.hbm.machine_electric_furnace_off", "Electric Furnace");
        add("block.hbm.machine_boiler_off", "Old Boiler");
        add("block.hbm.machine_shredder", "Shredder");
        add("block.hbm.decon", "Decontaminator");
        add("block.hbm.waste_earth", "Waste Earth");
        add("block.hbm.waste_mycelium", "Waste Mycelium");
        add("block.hbm.waste_leaves", "Waste Leaves");
        add("block.hbm.nuke_gadget", "The Gadget");
        add("block.hbm.nuke_boy", "Little Boy");
        add("block.hbm.nuke_man", "Fat Man");
        add("block.hbm.nuke_tsar", "Tsar Bomba");
        add("block.hbm.nuke_mike", "Ivy Mike");
        add("block.hbm.nuke_prototype", "The Prototype");
        add("block.hbm.nuke_fleija", "F.L.E.I.J.A.");
        add("block.hbm.nuke_solinium", "The Blue Rinse");
        add("block.hbm.nuke_n2", "N2 Mine");
        add("block.hbm.nuke_fstbmb", "Balefire Bomb");
        add("block.hbm.bomb_multi", "Multi Purpose Bomb");
        ModItems.EXTRA_PARTS_TAB_ITEMS.forEach(item -> addItem(item, title(item.getId().getPath())));
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> addItem(item, title(item.getId().getPath())));
        ModItems.NUKE_TAB_ITEMS.forEach(item -> addItem(item, title(item.getId().getPath())));
    }

    private static String title(String id) {
        StringBuilder builder = new StringBuilder();
        for (String part : id.split("_")) {
            if (part.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString()
                .replace("U233", "U-233")
                .replace("U235", "U-235")
                .replace("U238", "U-238")
                .replace("Pu238", "Pu-238")
                .replace("Pu239", "Pu-239")
                .replace("Pu240", "Pu-240")
                .replace("Pu241", "Pu-241")
                .replace("Am241", "Am-241")
                .replace("Am242", "Am-242")
                .replace("Co60", "Co-60")
                .replace("Sr90", "Sr-90")
                .replace("Au198", "Au-198")
                .replace("Pb209", "Pb-209")
                .replace("Ra226", "Ra-226")
                .replace("Th232", "Th-232");
    }
}
