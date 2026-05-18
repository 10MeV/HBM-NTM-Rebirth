package com.hbm.ntm.datagen;

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
        add("block.hbm.machine_press", "Burner Press");
        add("block.hbm.machine_difurnace_off", "Blast Furnace");
        add("block.hbm.machine_electric_furnace_off", "Electric Furnace");
        add("block.hbm.machine_boiler_off", "Old Boiler");
        add("block.hbm.machine_shredder", "Shredder");
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
    }
}
