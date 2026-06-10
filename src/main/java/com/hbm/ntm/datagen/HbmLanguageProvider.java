package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class HbmLanguageProvider extends LanguageProvider {
    private final Set<String> addedKeys = new HashSet<>();

    public HbmLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
    }

    @Override
    public void add(String key, String value) {
        if (addedKeys.add(key)) {
            super.add(key, value);
        }
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.hbm_ntm_rebirth.parts", "HBM Parts");
        add("itemGroup.hbm_ntm_rebirth.machines", "HBM Machines");
        add("itemGroup.hbm_ntm_rebirth.consumables", "HBM Consumables");
        add("itemGroup.hbm_ntm_rebirth.control", "HBM Control");
        add("itemGroup.hbm_ntm_rebirth.nukes", "HBM Nukes");
        add("itemGroup.hbm_ntm_rebirth.missiles", "HBM Missiles and Satellites");
        addAbilityTranslations();
        add("item.hbm_ntm_rebirth.ingot_uranium", "Uranium Ingot");
        add("item.hbm_ntm_rebirth.ingot_u233", "Uranium-233 Ingot");
        add("item.hbm_ntm_rebirth.ingot_u235", "Uranium-235 Ingot");
        add("item.hbm_ntm_rebirth.ingot_u238", "Uranium-238 Ingot");
        add("item.hbm_ntm_rebirth.ingot_plutonium", "Plutonium Ingot");
        add("item.hbm_ntm_rebirth.ingot_pu238", "Plutonium-238 Ingot");
        add("item.hbm_ntm_rebirth.ingot_pu239", "Plutonium-239 Ingot");
        add("item.hbm_ntm_rebirth.ingot_pu240", "Plutonium-240 Ingot");
        add("item.hbm_ntm_rebirth.ingot_pu241", "Plutonium-241 Ingot");
        add("item.hbm_ntm_rebirth.ingot_neptunium", "Neptunium Ingot");
        add("item.hbm_ntm_rebirth.ingot_polonium", "Polonium-210 Ingot");
        add("item.hbm_ntm_rebirth.ingot_th232", "Thorium-232 Ingot");
        add("item.hbm_ntm_rebirth.ingot_titanium", "Titanium Ingot");
        add("item.hbm_ntm_rebirth.ingot_tungsten", "Tungsten Ingot");
        add("item.hbm_ntm_rebirth.ingot_copper", "Industrial Grade Copper");
        add("item.hbm_ntm_rebirth.ingot_lead", "Lead Ingot");
        add("item.hbm_ntm_rebirth.ingot_steel", "Steel Ingot");
        add("item.hbm_ntm_rebirth.ingot_cobalt", "Cobalt Ingot");
        add("item.hbm_ntm_rebirth.ingot_aluminium", "Aluminium Ingot");
        add("item.hbm_ntm_rebirth.ingot_beryllium", "Beryllium Ingot");
        add("item.hbm_ntm_rebirth.ingot_schrabidium", "Schrabidium Ingot");
        add("item.hbm_ntm_rebirth.plate_steel", "Steel Plate");
        add("item.hbm_ntm_rebirth.plate_iron", "Iron Plate");
        add("item.hbm_ntm_rebirth.plate_copper", "Copper Plate");
        add("item.hbm_ntm_rebirth.plate_lead", "Lead Plate");
        add("item.hbm_ntm_rebirth.plate_titanium", "Titanium Plate");
        add("item.hbm_ntm_rebirth.plate_aluminium", "Aluminium Plate");
        add("item.hbm_ntm_rebirth.powder_uranium", "Uranium Powder");
        add("item.hbm_ntm_rebirth.powder_plutonium", "Plutonium Powder");
        add("item.hbm_ntm_rebirth.powder_thorium", "Thorium Powder");
        add("item.hbm_ntm_rebirth.powder_titanium", "Titanium Powder");
        add("item.hbm_ntm_rebirth.powder_tungsten", "Tungsten Powder");
        add("item.hbm_ntm_rebirth.powder_copper", "Copper Powder");
        add("item.hbm_ntm_rebirth.powder_iron", "Iron Powder");
        add("item.hbm_ntm_rebirth.powder_steel", "Steel Powder");
        add("item.hbm_ntm_rebirth.powder_lead", "Lead Powder");
        add("item.hbm_ntm_rebirth.coil_copper", "Copper Coil");
        add("item.hbm_ntm_rebirth.coil_tungsten", "Heating Coil");
        add("item.hbm_ntm_rebirth.coil_gold", "Gold Coil");
        add("item.hbm_ntm_rebirth.motor", "Motor");
        add("item.hbm_ntm_rebirth.upgrade_template", "Machine Upgrade Template");
        add("item.hbm_ntm_rebirth.blueprints", "Blueprints");
        add("item.hbm_ntm_rebirth.upgrade_speed_1", "Speed Upgrade I");
        add("item.hbm_ntm_rebirth.upgrade_speed_2", "Speed Upgrade II");
        add("item.hbm_ntm_rebirth.upgrade_speed_3", "Speed Upgrade III");
        add("item.hbm_ntm_rebirth.upgrade_effect_1", "Effectiveness Upgrade I");
        add("item.hbm_ntm_rebirth.upgrade_effect_2", "Effectiveness Upgrade II");
        add("item.hbm_ntm_rebirth.upgrade_effect_3", "Effectiveness Upgrade III");
        add("item.hbm_ntm_rebirth.upgrade_power_1", "Power Saving Upgrade I");
        add("item.hbm_ntm_rebirth.upgrade_power_2", "Power Saving Upgrade II");
        add("item.hbm_ntm_rebirth.upgrade_power_3", "Power Saving Upgrade III");
        add("item.hbm_ntm_rebirth.upgrade_overdrive_1", "Overdrive Upgrade I");
        add("item.hbm_ntm_rebirth.upgrade_overdrive_2", "Overdrive Upgrade II");
        add("item.hbm_ntm_rebirth.upgrade_overdrive_3", "Overdrive Upgrade III");
        add("item.hbm_ntm_rebirth.upgrade_afterburn_1", "Afterburner Upgrade I");
        add("item.hbm_ntm_rebirth.upgrade_afterburn_2", "Afterburner Upgrade II");
        add("item.hbm_ntm_rebirth.upgrade_afterburn_3", "Afterburner Upgrade III");
        add("item.hbm_ntm_rebirth.template_folder", "Machine Template Folder");
        add("item.hbm_ntm_rebirth.template_folder.desc", "Machine Templates: Paper + Dye$Press Stamps: Flat Stamp$Siren Tracks: Insulator + Steel Plate");
        add("item.hbm_ntm_rebirth.wiring_red_copper", "RED Copper Wiring");
        add("item.hbm_ntm_rebirth.stamp_iron_plate", "Plate Stamp (Iron)");
        add("item.hbm_ntm_rebirth.stamp_iron_flat", "Flat Stamp (Iron)");
        add("item.hbm_ntm_rebirth.stamp_iron_wire", "Wire Stamp (Iron)");
        add("item.hbm_ntm_rebirth.stamp_iron_circuit", "Circuit Stamp (Iron)");
        add("item.hbm_ntm_rebirth.stamp_357", ".357 Magnum Stamp");
        add("item.hbm_ntm_rebirth.stamp_44", ".44 Magnum Stamp");
        add("item.hbm_ntm_rebirth.stamp_50", "Large Caliber Stamp");
        add("item.hbm_ntm_rebirth.stamp_9", "Small Caliber Stamp");
        add("item.hbm_ntm_rebirth.stamp_book_printing1", "Printing Press Stamp (Part 1)");
        add("item.hbm_ntm_rebirth.stamp_book_printing2", "Printing Press Stamp (Part 2)");
        add("item.hbm_ntm_rebirth.stamp_book_printing3", "Printing Press Stamp (Part 3)");
        add("item.hbm_ntm_rebirth.stamp_book_printing4", "Printing Press Stamp (Part 4)");
        add("item.hbm_ntm_rebirth.stamp_book_printing5", "Printing Press Stamp (Part 5)");
        add("item.hbm_ntm_rebirth.stamp_book_printing6", "Printing Press Stamp (Part 6)");
        add("item.hbm_ntm_rebirth.stamp_book_printing7", "Printing Press Stamp (Part 7)");
        add("item.hbm_ntm_rebirth.stamp_book_printing8", "Printing Press Stamp (Part 8)");
        add("item.hbm_ntm_rebirth.page_of_page1", "Page 1");
        add("item.hbm_ntm_rebirth.page_of_page2", "Page 2");
        add("item.hbm_ntm_rebirth.page_of_page3", "Page 3");
        add("item.hbm_ntm_rebirth.page_of_page4", "Page 4");
        add("item.hbm_ntm_rebirth.page_of_page5", "Page 5");
        add("item.hbm_ntm_rebirth.page_of_page6", "Page 6");
        add("item.hbm_ntm_rebirth.page_of_page7", "Page 7");
        add("item.hbm_ntm_rebirth.page_of_page8", "Page 8");
        add("item.hbm_ntm_rebirth.geiger_counter", "Geiger Counter");
        add("item.hbm_ntm_rebirth.dosimeter", "Dosimeter");
        add("item.hbm_ntm_rebirth.digamma_diagnostic", "Digamma Diagnostic");
        add("item.hbm_ntm_rebirth.holotape_image_restored", "Holotape");
        add("item.hbm_ntm_rebirth.holotape_damaged", "Damaged Holotape");
        add("item.hbm_ntm_rebirth.pollution_detector", "Pollution Detector");
        add("item.hbm_ntm_rebirth.radaway", "RadAway");
        add("item.hbm_ntm_rebirth.radaway_strong", "RadAway Strong");
        add("item.hbm_ntm_rebirth.radaway_flush", "RadAway Flush");
        add("item.hbm_ntm_rebirth.radx", "Rad-X");
        add("item.hbm_ntm_rebirth.radx.desc", "Increases radiation resistance by 0.2 (37%) for 3 minutes");
        add("item.hbm_ntm_rebirth.five_htp", "5-HTP");
        add("block.hbm_ntm_rebirth.vending_machine", "Vending Machine");
        add("block.hbm_ntm_rebirth.vending_machine.snacks", "Vending Machine");
        add("item.hbm_ntm_rebirth.coin_token", "Vending Machine Token");
        add("item.hbm_ntm_rebirth.bottle_nuka", "Bottle of Nuka Cola");
        add("item.hbm_ntm_rebirth.bottle_cherry", "Bottle of Nuka Cherry");
        add("item.hbm_ntm_rebirth.bottle_quantum", "Bottle of Nuka Cola Quantum");
        add("item.hbm_ntm_rebirth.can_bepis", "Bepis");
        add("item.hbm_ntm_rebirth.can_luna", "Black Mesa Luna - Dark Cola");
        add("item.hbm_ntm_rebirth.can_mug", "MUG Root Beer");
        add("item.hbm_ntm_rebirth.can_breen", "Dr>Breens Private Reserve");
        add("item.hbm_ntm_rebirth.definitelyfood", "MRE");
        add("item.hbm_ntm_rebirth.twinkie", "Twinkie");
        add("item.hbm_ntm_rebirth.twinkie.desc", "Expired 600 years ago!");
        add("item.hbm_ntm_rebirth.chocolate", "Ithis-Brand Radium Chocolate");
        add("item.hbm_ntm_rebirth.chocolate.desc", "Radium Chocolate? Pretty sure this is just meth.");
        add("item.hbm_ntm_rebirth.canteen_vodka", "Vodka Canteen");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.cooldown", "Cooldown: 3 minutes");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.nausea", "Nausea I for 10 seconds");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.strength", "Strength III for 30 seconds");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.flavor", "Smells like disinfectant, tastes like disinfectant.");
        add("item.hbm_ntm_rebirth.glyphid_meat", "Glyphid Meat");
        add("item.hbm_ntm_rebirth.glyphid_meat_grilled", "Grilled Glyphid Meat");
        add("chem.meatprocessing", "Meat Processing");
        add("item.hbm_ntm_rebirth.gas_mask_filter", "Gas Mask Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_mono", "Catalytic Mask Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_combo", "Gas Mask Combo Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_rag", "Makeshift Gas Mask Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_piss", "Advanced Makeshift Gas Mask Filter");
        add("item.hbm_ntm_rebirth.attachment_mask", "Gas Mask Attachable");
        add("item.hbm_ntm_rebirth.attachment_mask_mono", "Half Mask Attachable");
        add("item.hbm_ntm_rebirth.goggles", "Protection Goggles");
        add("item.hbm_ntm_rebirth.ashglasses", "Ash Goggles");
        add("item.hbm_ntm_rebirth.gas_mask", "Gas Mask");
        add("item.hbm_ntm_rebirth.gas_mask_m65", "M65-Z Gas Mask");
        add("item.hbm_ntm_rebirth.gas_mask_mono", "Half Mask");
        add("item.hbm_ntm_rebirth.gas_mask_olde", "Leather Gas Mask");
        add("item.hbm_ntm_rebirth.mask_rag", "Crude Protective Mask");
        add("item.hbm_ntm_rebirth.mask_piss", "Trench Mask");
        add("item.hbm_ntm_rebirth.steel_helmet", "Steel Helmet");
        add("item.hbm_ntm_rebirth.steel_plate", "Steel Chestplate");
        add("item.hbm_ntm_rebirth.steel_legs", "Steel Leggings");
        add("item.hbm_ntm_rebirth.steel_boots", "Steel Boots");
        add("item.hbm_ntm_rebirth.titanium_helmet", "Titanium Helmet");
        add("item.hbm_ntm_rebirth.titanium_plate", "Titanium Chestplate");
        add("item.hbm_ntm_rebirth.titanium_legs", "Titanium Leggings");
        add("item.hbm_ntm_rebirth.titanium_boots", "Titanium Boots");
        add("item.hbm_ntm_rebirth.alloy_helmet", "Advanced Alloy Helmet (LEGACY)");
        add("item.hbm_ntm_rebirth.alloy_plate", "Advanced Alloy Chestplate (LEGACY)");
        add("item.hbm_ntm_rebirth.alloy_legs", "Advanced Alloy Leggings (LEGACY)");
        add("item.hbm_ntm_rebirth.alloy_boots", "Advanced Alloy Boots (LEGACY)");
        add("item.hbm_ntm_rebirth.cobalt_helmet", "Cobalt Helmet");
        add("item.hbm_ntm_rebirth.cobalt_plate", "Cobalt Chestplate");
        add("item.hbm_ntm_rebirth.cobalt_legs", "Cobalt Leggings");
        add("item.hbm_ntm_rebirth.cobalt_boots", "Cobalt Boots");
        add("item.hbm_ntm_rebirth.plate_paa", "PaA Plate");
        add("item.hbm_ntm_rebirth.plate_euphemium", "Euphemium Plate");
        add("item.hbm_ntm_rebirth.rag_damp", "Damp Rag");
        add("item.hbm_ntm_rebirth.rag_piss", "Soaked Rag");
        add("item.hbm_ntm_rebirth.watch", "Watch");
        add("item.hbm_ntm_rebirth.hazmat_cloth", "Hazmat Cloth");
        add("item.hbm_ntm_rebirth.hazmat_cloth_red", "Advanced Hazmat Cloth");
        add("item.hbm_ntm_rebirth.hazmat_cloth_grey", "Lead-Reinforced Hazmat Cloth");
        add("item.hbm_ntm_rebirth.hazmat_helmet", "Hazmat Helmet");
        add("item.hbm_ntm_rebirth.hazmat_plate", "Hazmat Chestplate");
        add("item.hbm_ntm_rebirth.hazmat_legs", "Hazmat Leggings");
        add("item.hbm_ntm_rebirth.hazmat_boots", "Hazmat Boots");
        add("item.hbm_ntm_rebirth.hazmat_helmet_red", "Advanced Hazmat Helmet");
        add("item.hbm_ntm_rebirth.hazmat_plate_red", "Advanced Hazmat Chestplate");
        add("item.hbm_ntm_rebirth.hazmat_legs_red", "Advanced Hazmat Leggings");
        add("item.hbm_ntm_rebirth.hazmat_boots_red", "Advanced Hazmat Boots");
        add("item.hbm_ntm_rebirth.hazmat_helmet_grey", "High-Performance Hazmat Helmet");
        add("item.hbm_ntm_rebirth.hazmat_plate_grey", "High-Performance Hazmat Chestplate");
        add("item.hbm_ntm_rebirth.hazmat_legs_grey", "High-Performance Hazmat Leggings");
        add("item.hbm_ntm_rebirth.hazmat_boots_grey", "High-Performance Hazmat Boots");
        add("item.hbm_ntm_rebirth.asbestos_helmet", "Fire Proximity Helmet");
        add("item.hbm_ntm_rebirth.asbestos_plate", "Fire Proximity Chestplate");
        add("item.hbm_ntm_rebirth.asbestos_legs", "Fire Proximity Leggings");
        add("item.hbm_ntm_rebirth.asbestos_boots", "Fire Proximity Boots");
        add("item.hbm_ntm_rebirth.cmb_helmet", "CMB Steel Helmet");
        add("item.hbm_ntm_rebirth.cmb_plate", "CMB Steel Chestplate");
        add("item.hbm_ntm_rebirth.cmb_legs", "CMB Steel Leggings");
        add("item.hbm_ntm_rebirth.cmb_boots", "CMB Steel Boots");
        add("item.hbm_ntm_rebirth.paa_plate", "PaA Chest Protection Plate");
        add("item.hbm_ntm_rebirth.paa_legs", "PaA Leg Reinforcements");
        add("item.hbm_ntm_rebirth.paa_boots", "PaA \"good ol' shoes\"");
        add("item.hbm_ntm_rebirth.jackt", "Damn Stylish Ballistic Jacket");
        add("item.hbm_ntm_rebirth.jackt2", "Damn Stylish Ballistic Jacket 2: Tokyo Drift");
        add("item.hbm_ntm_rebirth.security_helmet", "Security Helmet");
        add("item.hbm_ntm_rebirth.security_plate", "Security Chestplate");
        add("item.hbm_ntm_rebirth.security_legs", "Security Leggings");
        add("item.hbm_ntm_rebirth.security_boots", "Security Boots");
        add("item.hbm_ntm_rebirth.starmetal_helmet", "Starmetal Helmet");
        add("item.hbm_ntm_rebirth.starmetal_plate", "Starmetal Chestplate");
        add("item.hbm_ntm_rebirth.starmetal_legs", "Starmetal Leggings");
        add("item.hbm_ntm_rebirth.starmetal_boots", "Starmetal Boots");
        add("item.hbm_ntm_rebirth.hazmat_paa_helmet", "PaA Battle Hazmat Suit Helmet");
        add("item.hbm_ntm_rebirth.hazmat_paa_plate", "PaA Battle Hazmat Suit Chestplate");
        add("item.hbm_ntm_rebirth.hazmat_paa_legs", "PaA Battle Hazmat Suit Leggings");
        add("item.hbm_ntm_rebirth.hazmat_paa_boots", "PaA Battle Hazmat Suit Boots");
        add("item.hbm_ntm_rebirth.liquidator_helmet", "Liquidator Suit Helmet");
        add("item.hbm_ntm_rebirth.liquidator_plate", "Liquidator Suit Chestplate");
        add("item.hbm_ntm_rebirth.liquidator_legs", "Liquidator Suit Leggins");
        add("item.hbm_ntm_rebirth.liquidator_boots", "Liquidator Suit Boots");
        add("item.hbm_ntm_rebirth.schrabidium_helmet", "Schrabidium Helmet");
        add("item.hbm_ntm_rebirth.schrabidium_plate", "Schrabidium Chestplate");
        add("item.hbm_ntm_rebirth.schrabidium_legs", "Schrabidium Leggings");
        add("item.hbm_ntm_rebirth.schrabidium_boots", "Schrabidium Boots");
        add("item.hbm_ntm_rebirth.euphemium_helmet", "Euphemium Helmet");
        add("item.hbm_ntm_rebirth.euphemium_plate", "Euphemium Chestplate");
        add("item.hbm_ntm_rebirth.euphemium_legs", "Euphemium Leggings");
        add("item.hbm_ntm_rebirth.euphemium_boots", "Euphemium Boots");
        add("item.hbm_ntm_rebirth.t51_helmet", "T-51b Power Armor Helmet");
        add("item.hbm_ntm_rebirth.t51_plate", "T-51b Power Armor Chestplate");
        add("item.hbm_ntm_rebirth.t51_legs", "T-51b Power Armor Leggings");
        add("item.hbm_ntm_rebirth.t51_boots", "T-51b Power Armor Boots");
        add("item.hbm_ntm_rebirth.steamsuit_helmet", "Steam Suit Respirator Helmet");
        add("item.hbm_ntm_rebirth.steamsuit_plate", "Steam Suit Chestplate");
        add("item.hbm_ntm_rebirth.steamsuit_legs", "Steam Suit Leggings");
        add("item.hbm_ntm_rebirth.steamsuit_boots", "Steam Suit Boots");
        add("item.hbm_ntm_rebirth.ajr_helmet", "Steel Ranger Helmet");
        add("item.hbm_ntm_rebirth.ajr_plate", "Steel Ranger Chestplate");
        add("item.hbm_ntm_rebirth.ajr_legs", "Steel Ranger Leggings");
        add("item.hbm_ntm_rebirth.ajr_boots", "Steel Ranger Boots");
        add("item.hbm_ntm_rebirth.ajro_helmet", "AJR Power Armor Helmet");
        add("item.hbm_ntm_rebirth.ajro_plate", "AJR Power Armor Chestplate");
        add("item.hbm_ntm_rebirth.ajro_legs", "AJR Power Armor Leggings");
        add("item.hbm_ntm_rebirth.ajro_boots", "AJR Power Armor Boots");
        add("item.hbm_ntm_rebirth.rpa_helmet", "Remnants Power Armor Helmet");
        add("item.hbm_ntm_rebirth.rpa_plate", "Remnants Power Armor Chestplate");
        add("item.hbm_ntm_rebirth.rpa_legs", "Remnants Power Armor Leggings");
        add("item.hbm_ntm_rebirth.rpa_boots", "Remnants Power Armor Boots");
        add("item.hbm_ntm_rebirth.ncrpa_helmet", "NCR Ranger Power Armor Helmet");
        add("item.hbm_ntm_rebirth.ncrpa_plate", "NCR Ranger Power Armor Chestplate");
        add("item.hbm_ntm_rebirth.ncrpa_legs", "NCR Ranger Power Armor Leggings");
        add("item.hbm_ntm_rebirth.ncrpa_boots", "NCR Ranger Power Armor Boots");
        add("item.hbm_ntm_rebirth.bj_helmet", "Eyepatch with Thermal Sensor");
        add("item.hbm_ntm_rebirth.bj_plate", "Lunar Cybernetic Plating");
        add("item.hbm_ntm_rebirth.bj_legs", "Lunar Cybernetic Leg Replacements");
        add("item.hbm_ntm_rebirth.bj_boots", "Lunar Studded Boots");
        add("item.hbm_ntm_rebirth.envsuit_helmet", "M1TTY Environment Suit Helmet");
        add("item.hbm_ntm_rebirth.envsuit_plate", "M1TTY Environment Suit Chestplate");
        add("item.hbm_ntm_rebirth.envsuit_legs", "M1TTY Environment Suit Leggings");
        add("item.hbm_ntm_rebirth.envsuit_boots", "M1TTY Environment Suit Boots");
        add("item.hbm_ntm_rebirth.hev_helmet", "HEV Mark IV Helmet");
        add("item.hbm_ntm_rebirth.hev_plate", "HEV Mark IV Chestplate");
        add("item.hbm_ntm_rebirth.hev_legs", "HEV Mark IV Leggings");
        add("item.hbm_ntm_rebirth.hev_boots", "HEV Mark IV Boots");
        add("item.hbm_ntm_rebirth.fau_helmet", "Fau Helmet");
        add("item.hbm_ntm_rebirth.fau_plate", "Fau Chestplate");
        add("item.hbm_ntm_rebirth.fau_legs", "Fau Leggins");
        add("item.hbm_ntm_rebirth.fau_boots", "Fau Boots");
        add("item.hbm_ntm_rebirth.dns_helmet", "DNT Nano Suit Helmet");
        add("item.hbm_ntm_rebirth.dns_plate", "DNT Nano Suit Chestplate");
        add("item.hbm_ntm_rebirth.dns_legs", "DNT Nano Suit Leggings");
        add("item.hbm_ntm_rebirth.dns_boots", "DNT Nano Suit Boots");
        add("item.hbm_ntm_rebirth.taurun_helmet", "Taurun Helmet");
        add("item.hbm_ntm_rebirth.taurun_plate", "Taurun Chestplate");
        add("item.hbm_ntm_rebirth.taurun_legs", "Taurun Leggings");
        add("item.hbm_ntm_rebirth.taurun_boots", "Taurun Boots");
        add("item.hbm_ntm_rebirth.trenchmaster_helmet", "Trenchmaster's Helmet");
        add("item.hbm_ntm_rebirth.trenchmaster_plate", "Trenchmaster's Chestplate");
        add("item.hbm_ntm_rebirth.trenchmaster_legs", "Trenchmaster's Leggings");
        add("item.hbm_ntm_rebirth.trenchmaster_boots", "Trenchmaster's Boots");
        add("tooltip.hbm_ntm_rebirth.gasmask.no_filter", "No filter installed!");
        add("tooltip.hbm_ntm_rebirth.gasmask.installed_filter", "Installed filter:");
        addArmorModTranslations();
        add("item.hbm_ntm_rebirth.pads_rubber", "Rubber Pads");
        add("item.hbm_ntm_rebirth.pads_slime", "Slime Pads");
        add("item.hbm_ntm_rebirth.pads_static", "Static Pads");
        add("item.hbm_ntm_rebirth.cladding_paint", "Lead Paint");
        add("item.hbm_ntm_rebirth.cladding_rubber", "Rubber Cladding");
        add("item.hbm_ntm_rebirth.cladding_lead", "Lead Cladding");
        add("item.hbm_ntm_rebirth.cladding_desh", "Desh Cladding");
        add("item.hbm_ntm_rebirth.cladding_ghiorsium", "Ghiorsium Cladding");
        add("item.hbm_ntm_rebirth.cladding_iron", "Iron Cladding");
        add("item.hbm_ntm_rebirth.cladding_obsidian", "Obsidian Skin");
        add("item.hbm_ntm_rebirth.insert_kevlar", "Kevlar Insert");
        add("item.hbm_ntm_rebirth.insert_sapi", "SAPI Insert");
        add("item.hbm_ntm_rebirth.insert_esapi", "ESAPI Insert");
        add("item.hbm_ntm_rebirth.insert_xsapi", "XSAPI Insert");
        add("item.hbm_ntm_rebirth.insert_steel", "Heavy Steel Insert");
        add("item.hbm_ntm_rebirth.insert_du", "DU Insert");
        add("item.hbm_ntm_rebirth.insert_polonium", "Polonium Insert");
        add("item.hbm_ntm_rebirth.insert_ghiorsium", "Ghiorsium Insert");
        add("item.hbm_ntm_rebirth.insert_era", "Explosive Reactive Armor Insert");
        add("item.hbm_ntm_rebirth.insert_yharonite", "Yharonite Insert");
        add("item.hbm_ntm_rebirth.insert_doxium", "Astolfium-Doped Doxium Insert");
        add("item.hbm_ntm_rebirth.servo_set", "Servo Set");
        add("item.hbm_ntm_rebirth.servo_set_desh", "Desh Servo Set");
        add("item.hbm_ntm_rebirth.heart_piece", "Heart Piece");
        add("item.hbm_ntm_rebirth.heart_container", "Heart Container");
        add("item.hbm_ntm_rebirth.heart_booster", "Heart Booster");
        add("item.hbm_ntm_rebirth.heart_fab", "Heart of Darkness");
        add("item.hbm_ntm_rebirth.black_diamond", "Black Diamond");
        add("item.hbm_ntm_rebirth.wd40", "VT-40");
        add("item.hbm_ntm_rebirth.bottled_cloud", "Cloud in a Bottle");
        add("item.hbm_ntm_rebirth.jetpack_fly", "Jetpack");
        add("item.hbm_ntm_rebirth.jetpack_break", "Builder's Jetpack");
        add("item.hbm_ntm_rebirth.jetpack_vector", "Vectored Jetpack");
        add("item.hbm_ntm_rebirth.jetpack_boost", "Boostpack");
        add("item.hbm_ntm_rebirth.australium_iii", "Mark III Life Extender");
        add("item.hbm_ntm_rebirth.armor_polish", "ShiningArmor\u2122 Armor Polish");
        add("item.hbm_ntm_rebirth.bandaid", "Velvet Band-Aid");
        add("item.hbm_ntm_rebirth.serum", "Serum");
        add("item.hbm_ntm_rebirth.quartz_plutonium", "Plutonic Quartz");
        add("item.hbm_ntm_rebirth.morning_glory", "Morning Glory");
        add("item.hbm_ntm_rebirth.lodestone", "Lodestone");
        add("item.hbm_ntm_rebirth.horseshoe_magnet", "Horseshoe Magnet");
        add("item.hbm_ntm_rebirth.industrial_magnet", "Industrial Magnet");
        add("item.hbm_ntm_rebirth.bathwater", "Toxic Soapy Water");
        add("item.hbm_ntm_rebirth.bathwater_mk2", "Toxic Soapy Water (Horse Scented)");
        add("item.hbm_ntm_rebirth.spider_milk", "Bottle of Spider Milk");
        add("item.hbm_ntm_rebirth.ink", "\u6797 Ink");
        add("item.hbm_ntm_rebirth.injector_5htp", "5-HTP Autoinjector");
        add("item.hbm_ntm_rebirth.injector_knife", "8 Inch Blade Autoinjector");
        add("item.hbm_ntm_rebirth.defuser_gold", "Golden Wire Cutter");
        add("item.hbm_ntm_rebirth.neutrino_lens", "Neutrino Lens");
        add("item.hbm_ntm_rebirth.night_vision", "Night Vision Goggles");
        add("item.hbm_ntm_rebirth.back_tesla", "Back-Mounted Tesla Coil");
        add("item.hbm_ntm_rebirth.medal_liquidator", "Liquidator Medal");
        add("item.hbm_ntm_rebirth.ballistic_gauntlet", "Ballistic Gauntlet");
        add("item.hbm_ntm_rebirth.ammo_standard_stone", "Ball and Powder");
        add("item.hbm_ntm_rebirth.ammo_standard_stone_ap", "Flint and Powder");
        add("item.hbm_ntm_rebirth.ammo_standard_stone_iron", "Iron Ball and Powder");
        add("item.hbm_ntm_rebirth.ammo_standard_stone_shot", "Shot and Powder");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_bp", "12 Gauge Black Powder Shell");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_bp_magnum", "12 Gauge Black Powder Magnum Shell");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_bp_slug", "12 Gauge Black Powder Slug");
        add("item.hbm_ntm_rebirth.ammo_standard_g12", "12 Gauge Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_slug", "12 Gauge Slug");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_flechette", "12 Gauge Flechette");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_magnum", "12 Gauge Magnum Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_explosive", "12 Gauge Explosive Slug");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_phosphorus", "12 Gauge Phosphorus Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_g10", "10 Gauge Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_shrapnel", "10 Gauge Shrapnel Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_du", "10 Gauge Uranium Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_slug", "10 Gauge Slug");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_explosive", "10 Gauge Explosive Buckshot");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_sp", ".22 LR Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_fmj", ".22 LR Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_jhp", ".22 LR Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_ap", ".22 LR Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_sp", "9mm Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_fmj", "9mm Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_jhp", "9mm Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_ap", "9mm Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_sp", "5.56mm Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_fmj", "5.56mm Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_jhp", "5.56mm Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_ap", "5.56mm Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_bp", ".44 Magnum Round (Black Powder)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_sp", ".44 Magnum Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_fmj", ".44 Magnum Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_jhp", ".44 Magnum Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_ap", ".44 Magnum Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_express", ".44 Magnum Round (FMJ Express)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_bp", ".357 Magnum Round (Black Powder)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_sp", ".357 Magnum Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_fmj", ".357 Magnum Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_jhp", ".357 Magnum Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_ap", ".357 Magnum Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_express", ".357 Magnum Round (FMJ Express)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_sp", "7.62mm Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_fmj", "7.62mm Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_jhp", "7.62mm Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_ap", "7.62mm Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_du", "7.62mm Round (Depleted Uranium)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_he", "7.62mm Round (High-Explosive)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_sp", ".50 BMG Round (Soft Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_fmj", ".50 BMG Round (Full Metal Jacket)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_jhp", ".50 BMG Round (Jacketed Hollow Point)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_ap", ".50 BMG Round (Armor Piercing)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_du", ".50 BMG Round (Depleted Uranium)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_he", ".50 BMG Round (High-Explosive)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_sm", ".50 BMG Round (Starmetal)");
        add("item.hbm_ntm_rebirth.ammo_standard_b75", ".75 Bolt");
        add("item.hbm_ntm_rebirth.ammo_standard_b75_inc", ".75 Bolt (Incendiary)");
        add("item.hbm_ntm_rebirth.ammo_standard_b75_exp", ".75 Bolt (Explosive)");
        add("item.hbm_ntm_rebirth.ammo_standard_g26_flare", "26mm Signal Flare");
        add("item.hbm_ntm_rebirth.ammo_standard_g26_flare_supply", "26mm Signal Flare (Supply Airdrop)");
        add("item.hbm_ntm_rebirth.ammo_standard_g26_flare_weapon", "26mm Signal Flare (Weapon Airdrop)");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_he", "40mm Grenade, High-Explosive");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_heat", "40mm Grenade, Shaped Charge");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_demo", "40mm Grenade, Demolition");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_inc", "40mm Grenade, Incendiary");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_phosphorus", "40mm Grenade, White Phosphorus");
        add("item.hbm_ntm_rebirth.gun_pepperbox", "Pepperbox");
        add("item.hbm_ntm_rebirth.gun_maresleg", "Mare's Leg");
        add("item.hbm_ntm_rebirth.gun_maresleg_broken", "Broken Mare's Leg");
        add("item.hbm_ntm_rebirth.gun_liberator", "Liberator");
        add("item.hbm_ntm_rebirth.gun_spas12", "SPAS-12");
        add("item.hbm_ntm_rebirth.gun_autoshotgun", "Shredder");
        add("item.hbm_ntm_rebirth.gun_autoshotgun_sexy", "The Sexy");
        add("item.hbm_ntm_rebirth.gun_double_barrel", "An Old Classic");
        add("item.hbm_ntm_rebirth.gun_double_barrel_sacred_dragon", "Sacred Dragon");
        add("item.hbm_ntm_rebirth.gun_autoshotgun_heretic", "The Heretic");
        add("item.hbm_ntm_rebirth.gun_light_revolver", "Break-Action Revolver");
        add("item.hbm_ntm_rebirth.gun_light_revolver_atlas", "Atlas");
        add("item.hbm_ntm_rebirth.gun_henry", "Lever Action Rifle");
        add("item.hbm_ntm_rebirth.gun_henry_lincoln", "Lincoln's Repeater");
        add("item.hbm_ntm_rebirth.gun_heavy_revolver", "Heavy Revolver");
        add("item.hbm_ntm_rebirth.gun_heavy_revolver_lilmac", "Little Macintosh");
        add("item.hbm_ntm_rebirth.gun_heavy_revolver_protege", "Prot\u00e8ge");
        add("item.hbm_ntm_rebirth.gun_hangman", "Hangman");
        add("item.hbm_ntm_rebirth.gun_greasegun", "Grease Gun");
        add("item.hbm_ntm_rebirth.gun_lag", "Comically Long Pistol");
        add("item.hbm_ntm_rebirth.gun_uzi", "Uzi");
        add("item.hbm_ntm_rebirth.gun_am180", ".22 Submachine Gun");
        add("item.hbm_ntm_rebirth.gun_star_f", "Target Pistol");
        add("item.hbm_ntm_rebirth.gun_g3", "Assault Rifle");
        add("item.hbm_ntm_rebirth.gun_g3_zebra", "Zebra Rifle");
        add("item.hbm_ntm_rebirth.gun_stg77", "StG 77");
        add("item.hbm_ntm_rebirth.gun_carbine", "Carbine");
        add("item.hbm_ntm_rebirth.gun_mas36", "South Star");
        add("item.hbm_ntm_rebirth.gun_flaregun", "Flare Gun");
        add("item.hbm_ntm_rebirth.gun_congolake", "Congo Lake");
        add("item.hbm_ntm_rebirth.gun_mk108", "Grenade Machinegun");
        add("item.hbm_ntm_rebirth.gun_amat", "Anti-Materiel Rifle");
        add("item.hbm_ntm_rebirth.gun_amat_subtlety", "Subtlety");
        add("item.hbm_ntm_rebirth.gun_amat_penance", "Penance");
        add("item.hbm_ntm_rebirth.gun_bolter", "Bolter");
        add("tooltip.hbm_ntm_rebirth.sedna_gun.ammo", "Loaded: %s/%s");
        add("tooltip.hbm_ntm_rebirth.sedna_gun.default_ammo", "Default ammo: %s x%s");
        add("item.hbm_ntm_rebirth.card_aos", "Ace of Spades");
        add("item.hbm_ntm_rebirth.card_qos", "Queen of Spades");
        add("item.hbm_ntm_rebirth.protection_charm", "Charm of Protection");
        add("item.hbm_ntm_rebirth.meteor_charm", "Meteor Charm");
        add("item.hbm_ntm_rebirth.gas_tester", "Gas Sensor");
        add("item.hbm_ntm_rebirth.armor_battery", "Power Armor Battery Pack");
        add("item.hbm_ntm_rebirth.armor_battery_mk2", "Power Armor Battery Pack Mk2");
        add("item.hbm_ntm_rebirth.armor_battery_mk3", "Power Armor Battery Pack Mk3");
        add("item.hbm_ntm_rebirth.scrumpy", "Bottle of Scrumpy");
        add("item.hbm_ntm_rebirth.wild_p", "Wild Pegasus Dry Whiskey");
        add("item.hbm_ntm_rebirth.shackles", "Shackles");
        add("subtitles.hbm_ntm_rebirth.tool.gasmask_screw", "Gas mask filter installed");
        add("subtitles.hbm_ntm_rebirth.tool.pin_break", "Pin snaps");
        add("subtitles.hbm_ntm_rebirth.item.syringe", "Syringe injects");
        add("subtitles.hbm_ntm_rebirth.item.battery", "Suit battery installed");
        add("subtitles.hbm_ntm_rebirth.item.boltgun", "Rivet gun fires");
        add("subtitles.hbm_ntm_rebirth.item.gasmask_screw", "Gas mask filter installed");
        add("subtitles.hbm_ntm_rebirth.item.geiger", "Geiger counter clicks");
        add("subtitles.hbm_ntm_rebirth.item.jetpack_tank", "Jetpack tank installed");
        add("subtitles.hbm_ntm_rebirth.item.pin_break", "Pin snaps");
        add("subtitles.hbm_ntm_rebirth.item.pin_unlock", "Pin unlocks");
        add("subtitles.hbm_ntm_rebirth.item.radaway", "RadAway injector hisses");
        add("subtitles.hbm_ntm_rebirth.item.repair", "Tool repairs");
        add("subtitles.hbm_ntm_rebirth.item.spray", "Spray can hisses");
        add("subtitles.hbm_ntm_rebirth.item.tech_bleep", "Device bleeps");
        add("subtitles.hbm_ntm_rebirth.item.tech_boop", "Device beeps");
        add("subtitles.hbm_ntm_rebirth.item.unpack", "Package opens");
        add("subtitles.hbm_ntm_rebirth.item.upgrade_plug", "Upgrade plugs in");
        add("subtitles.hbm_ntm_rebirth.item.vice", "Vice clamps");
        addSatelliteTranslations();
        add("itemGroup.hbm_ntm_rebirth.weapons", "NTM Weapons");
        add("info.asbestos", "My lungs are burning.");
        add("info.coaldust", "It's hard to breathe here.");
        add("info.gasmask.no_filter", "Your mask has no filter!");
        add("tooltip.hbm_ntm_rebirth.protection.hold_shift", "Hold <%s> to display protection info");
        add("item.hbm_ntm_rebirth.containment_box", "Lead-Lined Box");
        add("item.hbm_ntm_rebirth.plastic_bag", "Plastic Bag");
        add("item.hbm_ntm_rebirth.toolbox", "Toolbox");
        add("item.hbm_ntm_rebirth.toolbox.desc.swap", "Click with the toolbox to swap hotbars in/out of the toolbox.");
        add("item.hbm_ntm_rebirth.toolbox.desc.open", "Shift-click with the toolbox to open the toolbox.");
        add("item.hbm_ntm_rebirth.screwdriver", "Screwdriver");
        add("item.hbm_ntm_rebirth.hand_drill", "Hand Drill");
        add("item.hbm_ntm_rebirth.defuser", "Bomb Defuser");
        add("item.hbm_ntm_rebirth.schrabidium_sword", "Schrabidium Sword");
        add("item.hbm_ntm_rebirth.schrabidium_pickaxe", "Schrabidium Pickaxe");
        add("item.hbm_ntm_rebirth.schrabidium_axe", "Schrabidium Axe");
        add("item.hbm_ntm_rebirth.schrabidium_shovel", "Schrabidium Shovel");
        add("item.hbm_ntm_rebirth.titanium_sword", "Titanium Sword");
        add("item.hbm_ntm_rebirth.titanium_pickaxe", "Titanium Pickaxe");
        add("item.hbm_ntm_rebirth.titanium_axe", "Titanium Axe");
        add("item.hbm_ntm_rebirth.titanium_shovel", "Titanium Shovel");
        add("item.hbm_ntm_rebirth.steel_sword", "Steel Sword");
        add("item.hbm_ntm_rebirth.steel_pickaxe", "Steel Pickaxe");
        add("item.hbm_ntm_rebirth.steel_axe", "Steel Axe");
        add("item.hbm_ntm_rebirth.steel_shovel", "Steel Shovel");
        add("item.hbm_ntm_rebirth.alloy_sword", "Alloy Sword");
        add("item.hbm_ntm_rebirth.alloy_pickaxe", "Alloy Pickaxe");
        add("item.hbm_ntm_rebirth.alloy_axe", "Alloy Axe");
        add("item.hbm_ntm_rebirth.alloy_shovel", "Alloy Shovel");
        add("item.hbm_ntm_rebirth.cmb_sword", "CMB Sword");
        add("item.hbm_ntm_rebirth.cmb_pickaxe", "CMB Pickaxe");
        add("item.hbm_ntm_rebirth.cmb_axe", "CMB Axe");
        add("item.hbm_ntm_rebirth.cmb_shovel", "CMB Shovel");
        add("item.hbm_ntm_rebirth.desh_sword", "Desh Sword");
        add("item.hbm_ntm_rebirth.desh_pickaxe", "Desh Pickaxe");
        add("item.hbm_ntm_rebirth.desh_axe", "Desh Axe");
        add("item.hbm_ntm_rebirth.desh_shovel", "Desh Shovel");
        add("item.hbm_ntm_rebirth.cobalt_sword", "Cobalt Sword");
        add("item.hbm_ntm_rebirth.cobalt_pickaxe", "Cobalt Pickaxe");
        add("item.hbm_ntm_rebirth.cobalt_axe", "Cobalt Axe");
        add("item.hbm_ntm_rebirth.cobalt_shovel", "Cobalt Shovel");
        add("item.hbm_ntm_rebirth.cobalt_decorated_sword", "Decorated Cobalt Sword");
        add("item.hbm_ntm_rebirth.cobalt_decorated_pickaxe", "Decorated Cobalt Pickaxe");
        add("item.hbm_ntm_rebirth.cobalt_decorated_axe", "Decorated Cobalt Axe");
        add("item.hbm_ntm_rebirth.cobalt_decorated_shovel", "Decorated Cobalt Shovel");
        add("item.hbm_ntm_rebirth.starmetal_sword", "Starmetal Sword");
        add("item.hbm_ntm_rebirth.starmetal_pickaxe", "Starmetal Pickaxe");
        add("item.hbm_ntm_rebirth.starmetal_axe", "Starmetal Axe");
        add("item.hbm_ntm_rebirth.starmetal_shovel", "Starmetal Shovel");
        add("item.hbm_ntm_rebirth.centri_stick", "Centrifugal Stick");
        add("item.hbm_ntm_rebirth.smashing_hammer", "Smashing Hammer");
        add("item.hbm_ntm_rebirth.elec_sword", "Stunstick");
        add("item.hbm_ntm_rebirth.elec_pickaxe", "Impact Drill");
        add("item.hbm_ntm_rebirth.elec_axe", "Electric Chainsaw");
        add("item.hbm_ntm_rebirth.elec_shovel", "Spiral Drill");
        add("item.hbm_ntm_rebirth.drax", "Terra Drill (LEGACY)");
        add("item.hbm_ntm_rebirth.drax_mk2", "Hardened Terra Drill (LEGACY)");
        add("item.hbm_ntm_rebirth.drax_mk3", "Schrabidic Terra Drill (LEGACY)");
        add("item.hbm_ntm_rebirth.bismuth_pickaxe", "Bismuth Pickaxe");
        add("item.hbm_ntm_rebirth.bismuth_axe", "Bismuth Axe");
        add("item.hbm_ntm_rebirth.volcanic_pickaxe", "Volcanic Pickaxe");
        add("item.hbm_ntm_rebirth.volcanic_axe", "Volcanic Axe");
        add("item.hbm_ntm_rebirth.chlorophyte_pickaxe", "Chlorophyte Pickaxe");
        add("item.hbm_ntm_rebirth.chlorophyte_axe", "Chlorophyte Axe");
        add("item.hbm_ntm_rebirth.mese_pickaxe", "Mese Pickaxe");
        add("item.hbm_ntm_rebirth.mese_axe", "Mese Axe");
        add("item.hbm_ntm_rebirth.dnt_sword", "DNT Sword");
        add("item.hbm_ntm_rebirth.dwarven_pickaxe", "Dwarven Pickaxe");
        add("item.hbm_ntm_rebirth.mese_gavel", "Mese Gavel");
        add("item.hbm_ntm_rebirth.chainsaw", "Chainsaw");
        add("item.hbm_ntm_rebirth.settings_tool", "Settings Tool");
        add("item.hbm_ntm_rebirth.settings_tool.desc1", "Can copy the settings (filters, fluid ID, etc) of machines");
        add("item.hbm_ntm_rebirth.settings_tool.desc2", "Shift right-click to copy, right click to paste");
        add("item.hbm_ntm_rebirth.settings_tool.desc3", "Ctrl click on pipes to paste settings to multiple pipes");
        add("item.hbm_ntm_rebirth.settings_tool.none", " None ");
        add("item.hbm_ntm_rebirth.settings_tool.unknown", "Unknown");
        add("item.hbm_ntm_rebirth.settings_tool.copied", "Copied settings of %s");
        add("item.hbm_ntm_rebirth.settings_tool.copy_failed", "Copy failed");
        add("item.hbm_ntm_rebirth.settings_tool.pasted", "Pasted settings");
        add("item.hbm_ntm_rebirth.settings_tool.paste_failed", "Paste failed");
        add("item.hbm_ntm_rebirth.conveyor_wand", "Conveyor Belt");
        add("item.hbm_ntm_rebirth.conveyor_wand.regular", "Conveyor Belt");
        add("item.hbm_ntm_rebirth.conveyor_wand.express", "Express Conveyor Belt");
        add("item.hbm_ntm_rebirth.conveyor_wand.double", "Double-Lane Conveyor Belt");
        add("item.hbm_ntm_rebirth.conveyor_wand.triple", "Triple-Lane Conveyor Belt");
        add("item.hbm_ntm_rebirth.conveyor_wand.desc", "Click two points to create a conveyor route");
        add("item.hbm_ntm_rebirth.conveyor_wand.vertical.desc", "Can place lifts and chutes for vertical item transport");
        add("item.hbm_ntm_rebirth.conveyor_wand.selected", "First point selected");
        add("item.hbm_ntm_rebirth.conveyor_wand.built", "Conveyor built");
        add("item.hbm_ntm_rebirth.conveyor_wand.not_enough", "Not enough conveyors");
        add("item.hbm_ntm_rebirth.conveyor_wand.obstructed", "Conveyor obstructed");
        add("item.hbm_ntm_rebirth.canister_empty", "Empty Canister");
        add("item.hbm_ntm_rebirth.canister_full", "Canister");
        add("item.hbm_ntm_rebirth.canister_napalm", "Napalm Canister");
        add("item.hbm_ntm_rebirth.gas_empty", "Empty Gas Bottle");
        add("item.hbm_ntm_rebirth.gas_full", "Gas Bottle");
        add("item.hbm_ntm_rebirth.fluid_tank_empty", "Empty Fluid Tank");
        add("item.hbm_ntm_rebirth.fluid_tank_full", "Fluid Tank");
        add("item.hbm_ntm_rebirth.fluid_tank_lead_empty", "Empty Lead-Lined Tank");
        add("item.hbm_ntm_rebirth.fluid_tank_lead_full", "Lead-Lined Tank");
        add("item.hbm_ntm_rebirth.fluid_barrel_empty", "Empty Fluid Barrel");
        add("item.hbm_ntm_rebirth.fluid_barrel_full", "Fluid Barrel");
        add("item.hbm_ntm_rebirth.fluid_barrel_infinite", "Infinite Fluid Barrel");
        add("item.hbm_ntm_rebirth.fluid_pack_empty", "Empty Fluid Pack");
        add("item.hbm_ntm_rebirth.fluid_pack_full", "Fluid Pack");
        add("item.hbm_ntm_rebirth.fluid_icon", "Fluid");
        add("item.hbm_ntm_rebirth.biomass", "Biomass");
        add("item.hbm_ntm_rebirth.biomass_compressed", "Compressed Biomass");
        add("item.hbm_ntm_rebirth.disperser_canister_empty", "Empty Disperser Canister");
        add("item.hbm_ntm_rebirth.disperser_canister", "Disperser Canister");
        add("item.hbm_ntm_rebirth.glyphid_gland_empty", "Empty Glyphid Gland");
        add("item.hbm_ntm_rebirth.glyphid_gland", "Glyphid Gland");
        add("item.hbm_ntm_rebirth.inf_water", "Infinite Water");
        add("item.hbm_ntm_rebirth.inf_water_mk2", "Infinite Water Mk2");
        add("item.hbm_ntm_rebirth.chlorine_pinwheel", "Chlorine Pinwheel");
        add("item.hbm_ntm_rebirth.fluid_identifier_multi", "Multi Fluid Identifier");
        add("item.hbm_ntm_rebirth.fluid_identifier_multi.info", "Fluid:");
        add("item.hbm_ntm_rebirth.fluid_identifier_multi.info2", "Secondary:");
        add("item.hbm_ntm_rebirth.battery_potato", "Potato Battery");
        add("item.hbm_ntm_rebirth.battery_creative", "Infinite Battery");
        add("item.hbm_ntm_rebirth.battery_redstone", "Redstone Battery");
        add("item.hbm_ntm_rebirth.battery_lead", "Lead-Acid Battery");
        add("item.hbm_ntm_rebirth.battery_lithium", "Lithium-Ion Battery");
        add("item.hbm_ntm_rebirth.battery_sodium", "Sodium-Iron Battery");
        add("item.hbm_ntm_rebirth.battery_schrabidium", "Schrabidium Battery");
        add("item.hbm_ntm_rebirth.battery_quantum", "Quantum Battery");
        add("item.hbm_ntm_rebirth.capacitor_copper", "Copper Capacitor");
        add("item.hbm_ntm_rebirth.capacitor_gold", "Gold Capacitor");
        add("item.hbm_ntm_rebirth.capacitor_niobium", "Niobium Capacitor");
        add("item.hbm_ntm_rebirth.capacitor_tantalum", "Tantalum Capacitor");
        add("item.hbm_ntm_rebirth.capacitor_bismuth", "Bismuth Capacitor");
        add("item.hbm_ntm_rebirth.capacitor_spark", "Spark Capacitor");
        add("item.hbm_ntm_rebirth.battery_sc.empty", "Empty Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.waste", "Spent Fuel Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.ra226", "Radium-226 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.tc99", "Technetium-99 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.co60", "Cobalt-60 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.pu238", "Plutonium-238 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.po210", "Polonium-210 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.au198", "Gold-198 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.pb209", "Lead-209 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.battery_sc.am241", "Americium-241 Self-Charging Battery");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_bismuth", "Decayed Bismuth RTG Pellet");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_lead", "Decayed Lead RTG Pellet");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_mercury", "Decayed Mercury RTG Pellet");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_neptunium", "Decayed Neptunium RTG Pellet");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_nickel", "Decayed Nickel RTG Pellet");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_zirconium", "Decayed Zirconium RTG Pellet");
        add("item.hbm_ntm_rebirth.rod_empty", "Empty Rod");
        add("item.hbm_ntm_rebirth.rod_lithium", "Lithium Rod");
        add("item.hbm_ntm_rebirth.rod_tritium", "Tritium Rod");
        add("item.hbm_ntm_rebirth.rod_co", "Cobalt Rod");
        add("item.hbm_ntm_rebirth.rod_co60", "Cobalt-60 Rod");
        add("item.hbm_ntm_rebirth.rod_th232", "Thorium-232 Rod");
        add("item.hbm_ntm_rebirth.rod_thf", "Thorium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_u235", "Uranium-235 Rod");
        add("item.hbm_ntm_rebirth.rod_np237", "Neptunium-237 Rod");
        add("item.hbm_ntm_rebirth.rod_u238", "Uranium-238 Rod");
        add("item.hbm_ntm_rebirth.rod_pu238", "Plutonium-238 Rod");
        add("item.hbm_ntm_rebirth.rod_pu239", "Plutonium-239 Rod");
        add("item.hbm_ntm_rebirth.rod_rgp", "Reactor-Grade Plutonium Rod");
        add("item.hbm_ntm_rebirth.rod_waste", "Nuclear Waste Rod");
        add("item.hbm_ntm_rebirth.rod_lead", "Lead Rod");
        add("item.hbm_ntm_rebirth.rod_uranium", "Uranium Rod");
        add("item.hbm_ntm_rebirth.rod_ra226", "Radium-226 Rod");
        add("item.hbm_ntm_rebirth.rod_ac227", "Actinium-227 Rod");
        add("item.hbm_ntm_rebirth.rod_dual_empty", "Empty Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_lithium", "Lithium Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_tritium", "Tritium Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_co", "Cobalt Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_co60", "Cobalt-60 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_th232", "Thorium-232 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_thf", "Thorium Fuel Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_u235", "Uranium-235 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_np237", "Neptunium-237 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_u238", "Uranium-238 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_pu238", "Plutonium-238 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_pu239", "Plutonium-239 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_rgp", "Reactor-Grade Plutonium Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_waste", "Nuclear Waste Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_lead", "Lead Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_uranium", "Uranium Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_ra226", "Radium-226 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_dual_ac227", "Actinium-227 Dual Rod");
        add("item.hbm_ntm_rebirth.rod_quad_empty", "Empty Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_lithium", "Lithium Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_tritium", "Tritium Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_co", "Cobalt Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_co60", "Cobalt-60 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_th232", "Thorium-232 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_thf", "Thorium Fuel Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_u235", "Uranium-235 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_np237", "Neptunium-237 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_u238", "Uranium-238 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_pu238", "Plutonium-238 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_pu239", "Plutonium-239 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_rgp", "Reactor-Grade Plutonium Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_waste", "Nuclear Waste Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_lead", "Lead Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_uranium", "Uranium Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_ra226", "Radium-226 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_quad_ac227", "Actinium-227 Quad Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_empty", "Empty ZIRNOX Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_natural_uranium_fuel", "ZIRNOX Natural Uranium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_uranium_fuel", "ZIRNOX Uranium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_th232", "ZIRNOX Thorium-232 Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_thorium_fuel", "ZIRNOX Thorium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_mox_fuel", "ZIRNOX MOX Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_plutonium_fuel", "ZIRNOX Plutonium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_u233_fuel", "ZIRNOX Uranium-233 Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_u235_fuel", "ZIRNOX Uranium-235 Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_les_fuel", "ZIRNOX LES Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_lithium", "ZIRNOX Lithium Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_zfb_mox", "ZIRNOX ZFB MOX Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_tritium", "ZIRNOX Tritium Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_natural_uranium_fuel_depleted", "Depleted ZIRNOX Natural Uranium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_uranium_fuel_depleted", "Depleted ZIRNOX Uranium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_thorium_fuel_depleted", "Depleted ZIRNOX Thorium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_mox_fuel_depleted", "Depleted ZIRNOX MOX Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_plutonium_fuel_depleted", "Depleted ZIRNOX Plutonium Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_u233_fuel_depleted", "Depleted ZIRNOX Uranium-233 Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_u235_fuel_depleted", "Depleted ZIRNOX Uranium-235 Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_les_fuel_depleted", "Depleted ZIRNOX LES Fuel Rod");
        add("item.hbm_ntm_rebirth.rod_zirnox_zfb_mox_depleted", "Depleted ZIRNOX ZFB MOX Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_meu", "MEU PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_heu233", "HEU-233 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_heu235", "HEU-235 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_men", "MEN PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hen237", "HEN-237 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_mox", "MOX PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_mep", "MEP PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hep239", "HEP-239 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hep241", "HEP-241 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_mea", "MEA PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hea242", "HEA-242 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hes326", "HES-326 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hes327", "HES-327 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_bfb_am_mix", "Fuel Grade Americium PWR BFB Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_bfb_pu241", "Plutonium-241 PWR BFB Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_meu", "Hot MEU PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_heu233", "Hot HEU-233 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_heu235", "Hot HEU-235 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_men", "Hot MEN PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hen237", "Hot HEN-237 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_mox", "Hot MOX PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_mep", "Hot MEP PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hep239", "Hot HEP-239 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hep241", "Hot HEP-241 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_mea", "Hot MEA PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hea242", "Hot HEA-242 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hes326", "Hot HES-326 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hes327", "Hot HES-327 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_bfb_am_mix", "Hot Fuel Grade Americium PWR BFB Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_bfb_pu241", "Hot Plutonium-241 PWR BFB Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_meu", "Depleted MEU PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_heu233", "Depleted HEU-233 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_heu235", "Depleted HEU-235 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_men", "Depleted MEN PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hen237", "Depleted HEN-237 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_mox", "Depleted MOX PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_mep", "Depleted MEP PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hep239", "Depleted HEP-239 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hep241", "Depleted HEP-241 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_mea", "Depleted MEA PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hea242", "Depleted HEA-242 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hes326", "Depleted HES-326 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hes327", "Depleted HES-327 PWR Fuel Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_bfb_am_mix", "Depleted Fuel Grade Americium PWR BFB Rod");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_bfb_pu241", "Depleted Plutonium-241 PWR BFB Rod");
        add("item.hbm_ntm_rebirth.watz_pellet_schrabidium", "Schrabidium Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_hes", "HES Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_mes", "MES Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_les", "LES Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_hen", "HEN Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_meu", "MEU Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_mep", "MEP Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_lead", "Lead Absorber Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_boron", "Boron Absorber Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_du", "Depleted Uranium Absorber Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_nqd", "Enriched Naquadah Watz Pellet");
        add("item.hbm_ntm_rebirth.watz_pellet_nqr", "Naquadria Watz Pellet");
        add("item.hbm_ntm_rebirth.rbmk_fuel_empty", "Empty RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_ueu", "NU RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_meu", "MEU RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_heu233", "HEU-233 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_heu235", "HEU-235 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_uzh", "UZrH RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_thmeu", "ThMEU RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_lep", "LEP-239 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mep", "MEP-239 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hep239", "HEP-239 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hep241", "HEP-241 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_lea", "LEA RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mea", "MEA RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hea241", "HEA-241 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hea242", "HEA-242 RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_men", "MEN RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hen", "HEN RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mox", "MOX RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_les", "LES RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mes", "MES RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hes", "HES RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_leaus", "LEAus RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_heaus", "HEAus RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_po210be", "Po210Be RBMK Neutron Source");
        add("item.hbm_ntm_rebirth.rbmk_fuel_ra226be", "Ra226Be RBMK Neutron Source");
        add("item.hbm_ntm_rebirth.rbmk_fuel_pu238be", "Pu238Be RBMK Neutron Source");
        add("item.hbm_ntm_rebirth.rbmk_fuel_balefire_gold", "Flashgold RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_flashlead", "Flashlead RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_balefire", "Balefire RBMK Fuel Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_zfb_bismuth", "Bismuth RBMK ZFB Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_zfb_pu241", "Pu-241 RBMK ZFB Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_zfb_am_mix", "Fuel Grade Americium RBMK ZFB Rod");
        add("item.hbm_ntm_rebirth.rbmk_fuel_drx", "\u00A7cDigamma RBMK Fuel Rod\u00A7r");
        add("item.hbm_ntm_rebirth.rbmk_pellet_ueu", "NU Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_meu", "MEU Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_heu233", "HEU-233 Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_heu235", "HEU-235 Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_uzh", "UZrH Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_thmeu", "ThMEU Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_lep", "LEP Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mep", "MEP Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hep239", "HEP-239 Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hep241", "HEP-241 Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_lea", "LEA Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mea", "MEA Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hea241", "HEA-241 Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hea242", "HEA-242 Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_men", "MEN Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hen", "HEN Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mox", "MOX Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_les", "LES Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mes", "MES Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hes", "HES Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_leaus", "LEAus Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_heaus", "HEAus Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_po210be", "Po210Be Neutron Source Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_ra226be", "Ra226Be Neutron Source Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_pu238be", "Pu238Be Neutron Source Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_balefire_gold", "Flashgold Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_flashlead", "Flashlead Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_balefire", "Balefire Fuel Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_zfb_bismuth", "Bismuth ZFB Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_zfb_pu241", "Pu-241 ZFB Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_zfb_am_mix", "Fuel Grade Americium ZFB Pellet");
        add("item.hbm_ntm_rebirth.rbmk_pellet_drx", "\u00A7cDigamma Fuel Pellet\u00A7r");
        add("desc.item.battery.charge", "Charge: %s / %sHE");
        add("desc.item.battery.chargePerc", "Charge: %s%%");
        add("desc.item.battery.chargeRate", "Charge rate: %sHE/tick");
        add("desc.item.battery.dischargeRate", "Discharge rate: %sHE/tick");
        add("desc.item.wasteCooling", "Cooling down");
        add("trait.rbmk.coreTemp", "Core temp: %s");
        add("trait.rbmk.depletion", "Depletion: %s");
        add("trait.rbmk.diffusion", "Diffusion: %s");
        add("trait.rbmk.fluxFunc", "Flux function: %s");
        add("trait.rbmk.funcType", "Function type: %s");
        add("trait.rbmk.heat", "Heat per flux: %s");
        add("trait.rbmk.melt", "Melting point: %s");
        add("trait.rbmk.neutron.any", "All Neutrons");
        add("trait.rbmk.neutron.fast", "Fast Neutrons");
        add("trait.rbmk.neutron.slow", "Slow Neutrons");
        add("trait.rbmk.skinTemp", "Skin temp: %s");
        add("trait.rbmk.source", "Self-igniting");
        add("trait.rbmk.splitsInto", "Splits into: %s");
        add("trait.rbmk.splitsWith", "Splits with: %s");
        add("trait.rbmk.xenon", "Xenon poison: %s");
        add("trait.rbmk.xenonBurn", "Xenon burn function: %s");
        add("trait.rbmk.xenonGen", "Xenon gen function: %s");
        add("trait.rbmx.coreTemp", "Core entropy: %s");
        add("trait.rbmx.depletion", "Crustyness: %s");
        add("trait.rbmx.diffusion", "Flow: %s");
        add("trait.rbmx.fluxFunc", "Doom function: %s");
        add("trait.rbmx.funcType", "Function specification: %s");
        add("trait.rbmx.heat", "Crust per tick at full power: %s");
        add("trait.rbmx.melt", "Crush depth: %s");
        add("trait.rbmk.neutron.any.x", "All non-euclidean shapes");
        add("trait.rbmk.neutron.fast.x", "Elliptic non-euclidean shapes");
        add("trait.rbmk.neutron.slow.x", "Hyperbolic non-euclidean shapes");
        add("trait.rbmx.skinTemp", "Skin entropy: %s");
        add("trait.rbmx.source", "Self-combusting");
        add("trait.rbmx.splitsInto", "Departs to: %s");
        add("trait.rbmx.splitsWith", "Arrives from: %s");
        add("trait.rbmx.xenon", "Lead poison: %s");
        add("trait.rbmx.xenonBurn", "Lead destruction function: %s");
        add("trait.rbmx.xenonGen", "Lead creation function: %s");
        add("effect.hbm_ntm_rebirth.radiation", "Radiation");
        add("effect.hbm_ntm_rebirth.radaway", "RadAway");
        add("effect.hbm_ntm_rebirth.radx", "Rad-X");
        add("effect.hbm_ntm_rebirth.taint", "Taint");
        add("effect.hbm_ntm_rebirth.mutation", "Tainted Heart");
        add("effect.hbm_ntm_rebirth.stability", "Stability");
        add("effect.hbm_ntm_rebirth.lead", "Lead Poisoning");
        add("effect.hbm_ntm_rebirth.potionsickness", "Potion Sickness");
        add("geiger.title", "Geiger Counter");
        add("geiger.title.dosimeter", "Dosimeter");
        add("geiger.chunkRad", "Chunk radiation: %s RAD/s");
        add("geiger.envRad", "Environmental dose: %s RAD/s");
        add("geiger.playerRad", "Player dose: %s RAD");
        add("geiger.playerRes", "Radiation resistance: %s%% (%s)");
        add("digamma.title", "DIGAMMA DIAGNOSTIC");
        add("digamma.playerDigamma", "Digamma exposure: %s DRX");
        add("digamma.playerHealth", "Digamma influence: %s%%");
        add("digamma.playerRes", "Digamma resistance: %s");
        add("tooltip.hbm_ntm_rebirth.radiation.single", "Radiation: %s RAD/s");
        add("tooltip.hbm_ntm_rebirth.radiation.total", "Stack total: %s RAD/s");
        add("tooltip.hbm_ntm_rebirth.radiation.resistance", "Radiation resistance: %s (%s%% blocked)");
        add("tooltip.hbm_ntm_rebirth.hazard.digamma", "Digamma: %s DRX");
        add("tooltip.hbm_ntm_rebirth.hazard.hot", "Heat: %s");
        add("tooltip.hbm_ntm_rebirth.hazard.blinding", "Blinding: %s");
        add("tooltip.hbm_ntm_rebirth.hazard.asbestos", "Asbestos: %s");
        add("tooltip.hbm_ntm_rebirth.hazard.coal", "Coal dust: %s");
        add("tooltip.hbm_ntm_rebirth.hazard.hydroactive", "Hydroactive: %s");
        add("tooltip.hbm_ntm_rebirth.hazard.explosive", "Explosive: %s");
        add("tooltip.hbm_ntm_rebirth.damage.set", "Armor set damage resistance");
        add("tooltip.hbm_ntm_rebirth.damage.item", "Item damage resistance");
        add("tooltip.hbm_ntm_rebirth.damage.line", "%s: %s/%s%%");
        add("tooltip.hbm_ntm_rebirth.damage.other", "Other");
        add("tooltip.hbm_ntm_rebirth.damage.category.EXPL", "Explosion");
        add("tooltip.hbm_ntm_rebirth.damage.category.FIRE", "Fire");
        add("tooltip.hbm_ntm_rebirth.damage.category.PHYS", "Physical");
        add("tooltip.hbm_ntm_rebirth.damage.category.EN", "Energy");
        add("tooltip.hbm_ntm_rebirth.damage.exact.drown", "Drowning");
        add("tooltip.hbm_ntm_rebirth.damage.exact.fall", "Fall");
        add("tooltip.hbm_ntm_rebirth.damage.exact.laser", "Laser");
        add("tooltip.hbm_ntm_rebirth.damage.exact.onfire", "Afterburn");
        add("tooltip.hbm_ntm_rebirth.damage.exact.acidplayer", "Acid");
        add("tooltip.hbm_ntm_rebirth.damage.exact.taublast", "Tau blast");
        add("tooltip.hbm_ntm_rebirth.damage.exact.revolverbullet", "Bullet");
        add("tooltip.hbm_ntm_rebirth.damage.exact.chopperbullet", "Chopper bullet");
        add("tooltip.hbm_ntm_rebirth.damage.exact.cmb", "Combine ball");
        add("tooltip.hbm_ntm_rebirth.damage.exact.nuclearblast", "Nuclear blast");
        add("tooltip.hbm_ntm_rebirth.damage.exact.mudpoisoning", "Mud poisoning");
        add("block.hbm_ntm_rebirth.machine_press", "Burner Press");
        add("subtitles.hbm_ntm_rebirth.block.press_operate", "Burner Press operates");
        add("subtitles.hbm_ntm_rebirth.block.motor", "Motor hums");
        add("subtitles.hbm_ntm_rebirth.block.engine", "Engine runs");
        add("subtitles.hbm_ntm_rebirth.block.turbine", "Turbine runs");
        add("subtitles.hbm_ntm_rebirth.block.fel", "Free electron laser hums");
        add("subtitles.hbm_ntm_rebirth.block.electric_hum", "Electric hum");
        add("subtitles.hbm_ntm_rebirth.block.fusion_reactor", "Fusion reactor spins");
        add("subtitles.hbm_ntm_rebirth.block.boiler_groan", "Boiler groans");
        add("subtitles.hbm_ntm_rebirth.block.centrifuge", "Centrifuge operates");
        add("subtitles.hbm_ntm_rebirth.block.turbofan", "Turbofan runs");
        add("subtitles.hbm_ntm_rebirth.block.damage", "Machine rattles");
        add("subtitles.hbm_ntm_rebirth.block.hephaestus", "Hephaestus runs");
        add("subtitles.hbm_ntm_rebirth.block.steam_engine", "Steam engine works");
        add("subtitles.hbm_ntm_rebirth.block.reactor_loop", "Reactor clicks");
        add("subtitles.hbm_ntm_rebirth.block.turbinegas", "Gas turbine runs");
        add("subtitles.hbm_ntm_rebirth.block.assembler_operate", "Assembly machine operates");
        add("subtitles.hbm_ntm_rebirth.block.assembler_strike", "Assembly machine strikes");
        add("subtitles.hbm_ntm_rebirth.block.assembler_start", "Assembly machine starts");
        add("subtitles.hbm_ntm_rebirth.block.assembler_stop", "Assembly machine stops");
        add("subtitles.hbm_ntm_rebirth.block.assembler_cut", "Assembly machine cuts");
        add("subtitles.hbm_ntm_rebirth.block.chemplant_operate", "Chemical plant operates");
        add("subtitles.hbm_ntm_rebirth.block.chemical_plant", "Chemical plant reacts");
        add("subtitles.hbm_ntm_rebirth.block.pipe_placed", "Pipe placed");
        add("subtitles.hbm_ntm_rebirth.block.boiler", "Boiler runs");
        add("subtitles.hbm_ntm_rebirth.block.pyro_operate", "Pyrolysis oven operates");
        add("subtitles.hbm_ntm_rebirth.block.debris", "Debris tumbles");
        add("subtitles.hbm_ntm_rebirth.block.sonar_ping", "Radar pings");
        add("subtitles.hbm_ntm_rebirth.block.soyuz_ready", "Soyuz ready");
        add("subtitles.hbm_ntm_rebirth.block.bobble", "Bobblehead jingle");
        add("subtitles.hbm_ntm_rebirth.block.broadcast", "Broadcaster transmits");
        add("subtitles.hbm_ntm_rebirth.block.lever", "Lever clanks");
        add("subtitles.hbm_ntm_rebirth.block.cover", "Cover moves");
        add("subtitles.hbm_ntm_rebirth.block.door", "Door moves");
        add("subtitles.hbm_ntm_rebirth.block.crate", "Crate opens");
        add("subtitles.hbm_ntm_rebirth.block.diesel", "Diesel engine runs");
        add("subtitles.hbm_ntm_rebirth.block.fensu", "FEnSU hums");
        add("subtitles.hbm_ntm_rebirth.block.horn", "Horn sounds");
        add("subtitles.hbm_ntm_rebirth.block.hunduns_magnificent_howl", "Hundun howls");
        add("subtitles.hbm_ntm_rebirth.block.igenerator", "Industrial generator runs");
        add("subtitles.hbm_ntm_rebirth.block.lock", "Lock clicks");
        add("subtitles.hbm_ntm_rebirth.block.metal_impact", "Metal impacts");
        add("subtitles.hbm_ntm_rebirth.block.miner", "Miner operates");
        add("subtitles.hbm_ntm_rebirth.block.missile_assembly", "Missile assembly moves");
        add("subtitles.hbm_ntm_rebirth.block.rbmk", "RBMK mechanism moves");
        add("subtitles.hbm_ntm_rebirth.block.reactor_stop", "Reactor stops");
        add("subtitles.hbm_ntm_rebirth.block.screm", "Machine screams");
        add("subtitles.hbm_ntm_rebirth.block.shutdown", "Shutdown alarm sounds");
        add("subtitles.hbm_ntm_rebirth.block.spark", "Sparks crackle");
        add("subtitles.hbm_ntm_rebirth.block.squeaky_toy", "Toy squeaks");
        add("subtitles.hbm_ntm_rebirth.block.storage", "Storage opens");
        add("subtitles.hbm_ntm_rebirth.block.vault", "Vault door moves");
        add("subtitles.hbm_ntm_rebirth.block.warn_overspeed", "Overspeed warning sounds");
        add("subtitles.hbm_ntm_rebirth.tool.geiger", "Geiger counter clicks");
        add("subtitles.hbm_ntm_rebirth.tool.tech_boop", "Device beeps");
        add("subtitles.hbm_ntm_rebirth.tool.tech_bleep", "Detonator bleeps");
        add("subtitles.hbm_ntm_rebirth.tool.radaway", "RadAway injector hisses");
        add("subtitles.hbm_ntm_rebirth.step.metal", "Metal footsteps");
        add("subtitles.hbm_ntm_rebirth.step.iron", "Iron armor moves");
        add("subtitles.hbm_ntm_rebirth.step.metal_block", "Metal block footsteps");
        add("subtitles.hbm_ntm_rebirth.step.powered", "Powered armor moves");
        add("subtitles.hbm_ntm_rebirth.player.cough", "Player coughs");
        add("subtitles.hbm_ntm_rebirth.player.vomit", "Something vomits");
        add("subtitles.hbm_ntm_rebirth.player.gulp", "Player gulps");
        add("subtitles.hbm_ntm_rebirth.player.groan", "Player groans");
        add("subtitles.hbm_ntm_rebirth.potatos.random", "PotatOS speaks");
        add("subtitles.hbm_ntm_rebirth.misc.null", "Chopper signal redirects");
        add("subtitles.hbm_ntm_rebirth.music.record_lambda_core", "Record plays");
        add("subtitles.hbm_ntm_rebirth.music.record_sector_sweep", "Record plays");
        add("subtitles.hbm_ntm_rebirth.music.record_vortal_combat", "Record plays");
        add("subtitles.hbm_ntm_rebirth.music.transmission", "Transmission plays");
        add("subtitles.hbm_ntm_rebirth.entity.cybercrab", "Cyber crab transmits");
        add("subtitles.hbm_ntm_rebirth.entity.ducc", "Duck quacks");
        add("subtitles.hbm_ntm_rebirth.entity.megaquacc", "Megaquacc quacks");
        add("subtitles.hbm_ntm_rebirth.entity.siege", "Siege craft moves");
        add("subtitles.hbm_ntm_rebirth.entity.ufo_beam", "UFO beam fires");
        add("subtitles.hbm_ntm_rebirth.entity.ufo_blast", "Energy discharge");
        add("subtitles.hbm_ntm_rebirth.entity.slicer", "Blade slices");
        add("subtitles.hbm_ntm_rebirth.entity.chopper", "Chopper drones");
        add("subtitles.hbm_ntm_rebirth.entity.soyuz_takeoff", "Soyuz rocket launches");
        add("subtitles.hbm_ntm_rebirth.entity.explosion", "Explosion rumbles");
        add("subtitles.hbm_ntm_rebirth.entity.rocket_takeoff", "Rocket launches");
        add("subtitles.hbm_ntm_rebirth.entity.bomb", "Bomb echoes");
        add("subtitles.hbm_ntm_rebirth.entity.bomber", "Bomber flies");
        add("subtitles.hbm_ntm_rebirth.entity.plane", "Aircraft damaged");
        add("subtitles.hbm_ntm_rebirth.entity.meteorite", "Meteorite falls");
        add("subtitles.hbm_ntm_rebirth.turret.fire", "Turret fires");
        add("subtitles.hbm_ntm_rebirth.turret.reload", "Turret reloads");
        add("subtitles.hbm_ntm_rebirth.turret.lockon", "Turret locks on");
        add("subtitles.hbm_ntm_rebirth.turret.mortar_whistle", "Mortar shell whistles");
        add("subtitles.hbm_ntm_rebirth.alarm.soyuzed", "Soyuz alarm blares");
        add("subtitles.hbm_ntm_rebirth.alarm.siren", "Siren blares");
        add("subtitles.hbm_ntm_rebirth.alarm.train_horn", "Train horn blares");
        add("subtitles.hbm_ntm_rebirth.alarm.gambit", "Duchess Gambit approaches");
        add("subtitles.hbm_ntm_rebirth.alarm.chime", "Chime rings");
        add("subtitles.hbm_ntm_rebirth.alarm.singer", "Singer vocalizes");
        add("subtitles.hbm_ntm_rebirth.door.transition_seal_open", "Transition seal opens");
        add("subtitles.hbm_ntm_rebirth.door.move", "Door moves");
        add("subtitles.hbm_ntm_rebirth.door.stop", "Door stops");
        add("subtitles.hbm_ntm_rebirth.door.alarm", "Door alarm blares");
        add("subtitles.hbm_ntm_rebirth.door.lever", "Lever clanks");
        add("subtitles.hbm_ntm_rebirth.weapon.muke_explosion", "Muke explosion");
        add("subtitles.hbm_ntm_rebirth.weapon.explosion", "Explosion rumbles");
        add("subtitles.hbm_ntm_rebirth.weapon.chainsaw", "Chainsaw revs");
        add("subtitles.hbm_ntm_rebirth.weapon.flamethrower", "Flamethrower fires");
        add("subtitles.hbm_ntm_rebirth.weapon.cal_shoot", "Rifle fires");
        add("subtitles.hbm_ntm_rebirth.weapon.tesla_shoot", "Tesla weapon fires");
        add("subtitles.hbm_ntm_rebirth.weapon.hk_shoot", "Rocket launcher fires");
        add("subtitles.hbm_ntm_rebirth.weapon.fire", "Weapon fires");
        add("subtitles.hbm_ntm_rebirth.weapon.reload", "Weapon reloads");
        add("subtitles.hbm_ntm_rebirth.weapon.foley", "Weapon handled");
        add("subtitles.hbm_ntm_rebirth.weapon.action", "Weapon used");
        add("subtitles.hbm_ntm_rebirth.weapon.ricochet", "Bullet ricochets");
        add("subtitles.hbm_ntm_rebirth.weapon.gbounce", "Grenade bounces");
        add("subtitles.hbm_ntm_rebirth.weapon.casing", "Casing drops");
        add("item.hbm_ntm_rebirth.detonator", "Detonator");
        add("item.hbm_ntm_rebirth.singularity", "Singularity");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant", "Contained Counter-Resonant Singularity");
        add("item.hbm_ntm_rebirth.singularity_super_heated", "Superheated Resonating Singularity");
        add("item.hbm_ntm_rebirth.singularity_spark", "Spark Singularity");
        add("item.hbm_ntm_rebirth.black_hole", "Miniature Black Hole");
        add("item.hbm_ntm_rebirth.particle_digamma", "The Digamma Particle");
        add("item.hbm_ntm_rebirth.pellet_antimatter", "Antimatter Cluster");
        add("item.hbm_ntm_rebirth.singularity.desc.1", "You may be asking:");
        add("item.hbm_ntm_rebirth.singularity.desc.2", "\"How is this possible?\"");
        add("item.hbm_ntm_rebirth.singularity.desc.3", "\"I have no idea!\"");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant.desc.1", "Nullifies resonance of objects in");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant.desc.2", "non-euclidean space, creating a");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant.desc.3", "variable gravity well.");
        add("item.hbm_ntm_rebirth.singularity_super_heated.desc.1", "Continuously heats up matter by");
        add("item.hbm_ntm_rebirth.singularity_super_heated.desc.2", "resonating every Planck second.");
        add("item.hbm_ntm_rebirth.singularity_super_heated.desc.3", "Not edible.");
        add("item.hbm_ntm_rebirth.singularity_spark.desc.1", "A violently unstable singularity");
        add("item.hbm_ntm_rebirth.singularity_spark.desc.2", "that pulses and tears space open.");
        add("item.hbm_ntm_rebirth.singularity_spark.desc.3", "Handle from very far away.");
        add("item.hbm_ntm_rebirth.black_hole.desc.1", "Contains a regular singularity");
        add("item.hbm_ntm_rebirth.black_hole.desc.2", "large enough to stay stable.");
        add("item.hbm_ntm_rebirth.black_hole.desc.3", "It's not the end of the world.");
        add("item.hbm_ntm_rebirth.particle_digamma.desc.half_particle", "Particle half-life: 1.67*10^21 years");
        add("item.hbm_ntm_rebirth.particle_digamma.desc.half_player", "Player half-life: %s");
        add("item.hbm_ntm_rebirth.particle_digamma.desc.digamma", "%s mDRX/s");
        add("item.hbm_ntm_rebirth.pellet_antimatter.desc.1", "Very heavy antimatter cluster.");
        add("item.hbm_ntm_rebirth.pellet_antimatter.desc.2", "Gets rid of black holes.");
        add("item.hbm_ntm_rebirth.trait.drop", "[Drops when dropped]");
        add("tooltip.hbm_ntm_rebirth.detonator.set", "Shift right-click to set position,");
        add("tooltip.hbm_ntm_rebirth.detonator.trigger", "right-click to detonate!");
        add("tooltip.hbm_ntm_rebirth.detonator.no_position", "No position set!");
        add("tooltip.hbm_ntm_rebirth.detonator.linked", "Linked to %s, %s, %s");
        add("msg.hbm_ntm_rebirth.detonator.position_set", "Position set!");
        add("msg.hbm_ntm_rebirth.detonator.no_position", "No position set!");
        add("bomb.detonated", "Detonated successfully!");
        add("bomb.incompatible", "Device can not be triggered!");
        add("bomb.launched", "Launched successfully!");
        add("bomb.missingComponent", "Component missing!");
        add("bomb.nobomb", "Linked position incompatible or unloaded!");
        add("bomb.triggered", "Triggered successfully!");
        add("block.hbm_ntm_rebirth.machine_difurnace_off", "Blast Furnace");
        add("block.hbm_ntm_rebirth.machine_electric_furnace_off", "Electric Furnace");
        add("block.hbm_ntm_rebirth.machine_boiler_off", "Old Boiler");
        add("block.hbm_ntm_rebirth.machine_shredder", "Shredder");
        add("block.hbm_ntm_rebirth.machine_turbine", "Steam Turbine");
        add("block.hbm_ntm_rebirth.machine_industrial_turbine", "Industrial Steam Turbine");
        add("block.hbm_ntm_rebirth.decon", "Decontaminator");
        add("block.hbm_ntm_rebirth.machine_armor_table", "Armor Modification Table");
        add("container.armorTable", "Armor Modification Table");
        add("block.hbm_ntm_rebirth.red_cable", "Red Copper Cable");
        add("block.hbm_ntm_rebirth.red_cable_gauge", "Power Gauge");
        add("block.hbm_ntm_rebirth.cable_switch", "Red Copper Cable Switch");
        add("block.hbm_ntm_rebirth.cable_detector", "Red Copper Cable Detector");
        add("block.hbm_ntm_rebirth.cable_diode", "Red Copper Diode");
        add("block.hbm_ntm_rebirth.red_connector", "Red Copper Connector");
        add("block.hbm_ntm_rebirth.red_connector_super", "High Performance Red Copper Connector");
        add("block.hbm_ntm_rebirth.red_pylon", "Electricity Pylon");
        add("block.hbm_ntm_rebirth.red_pylon_medium_wood", "Medium Wooden Electricity Pylon");
        add("block.hbm_ntm_rebirth.red_pylon_medium_wood_transformer", "Medium Wooden Electricity Pylon with Transformer");
        add("block.hbm_ntm_rebirth.red_pylon_medium_steel", "Medium Steel Electricity Pylon");
        add("block.hbm_ntm_rebirth.red_pylon_medium_steel_transformer", "Medium Steel Electricity Pylon with Transformer");
        add("block.hbm_ntm_rebirth.red_pylon_large", "Large Electricity Pylon");
        add("block.hbm_ntm_rebirth.substation", "Substation");
        add("block.hbm_ntm_rebirth.radio_torch_sender", "RTTY Sender");
        add("block.hbm_ntm_rebirth.radio_torch_receiver", "RTTY Receiver");
        add("block.hbm_ntm_rebirth.radio_torch_counter", "RTTY Counter");
        add("block.hbm_ntm_rebirth.radio_torch_logic", "RTTY Logic Receiver");
        add("block.hbm_ntm_rebirth.radio_torch_reader", "RTTY Reader");
        add("block.hbm_ntm_rebirth.radio_torch_controller", "RTTY Controller");
        add("block.hbm_ntm_rebirth.radio_autocal", "AUTOCAL");
        add("block.hbm_ntm_rebirth.radio_telex", "Telex");
        add("block.hbm_ntm_rebirth.rbmk_display_blank", "Blank Redstone-over-Radio Panel");
        add("block.hbm_ntm_rebirth.rbmk_gauge", "RBMK Gauge Panel");
        add("block.hbm_ntm_rebirth.rbmk_graph", "RBMK Graph Panel");
        add("block.hbm_ntm_rebirth.rbmk_indicator", "RBMK Indicator Panel");
        add("block.hbm_ntm_rebirth.rbmk_key_pad", "RBMK Keypad Panel");
        add("block.hbm_ntm_rebirth.rbmk_lever", "RBMK Lever Panel");
        add("block.hbm_ntm_rebirth.rbmk_numitron", "RBMK Numitron Panel");
        add("block.hbm_ntm_rebirth.block_graphite", "Block of Graphite");
        add("block.hbm_ntm_rebirth.block_graphite_drilled", "Drilled Graphite");
        add("block.hbm_ntm_rebirth.block_graphite_fuel", "Pile Fuel");
        add("block.hbm_ntm_rebirth.block_graphite_plutonium", "Pile Fuel (Bred)");
        add("block.hbm_ntm_rebirth.block_graphite_rod", "Pile Control Rod");
        add("block.hbm_ntm_rebirth.block_graphite_source", "Pile Neutron Source");
        add("block.hbm_ntm_rebirth.block_graphite_lithium", "Pile Lithium Fuel");
        add("block.hbm_ntm_rebirth.block_graphite_tritium", "Pile Lithium Fuel (Bred)");
        add("block.hbm_ntm_rebirth.block_graphite_detector", "Pile Neutron Detector");
        add("item.hbm_ntm_rebirth.rtty_pager", "RTTY Pager");
        add("container.rttyPager", "RTTY Pager");
        add("container.rbmkGauge", "RBMK Gauge Panel");
        add("container.rbmkGraph", "RBMK Graph Panel");
        add("container.rbmkIndicator", "RBMK Indicator Panel");
        add("container.rbmkKeyPad", "RBMK Keypad Panel");
        add("container.rbmkLever", "RBMK Lever Panel");
        add("container.rbmkNumitron", "RBMK Numitron Panel");
        add("block.hbm_ntm_rebirth.cable_diode.desc1", "Limits throughput and restricts flow direction");
        add("block.hbm_ntm_rebirth.cable_diode.desc2", "Use screwdriver to increase throughput");
        add("block.hbm_ntm_rebirth.cable_diode.desc3", "Use hand drill to decrease throughput");
        add("block.hbm_ntm_rebirth.cable_diode.desc4", "Use defuser to change network priority");
        add("block.hbm_ntm_rebirth.fluid_duct_neo", "Fluid Duct");
        add("item.hbm_ntm_rebirth.fluid_duct", "Fluid Duct:");
        add("block.hbm_ntm_rebirth.fluid_duct_box", "Universal Fluid Duct (Boxduct)");
        add("block.hbm_ntm_rebirth.fluid_duct_gauge", "Flow Gauge Pipe");
        add("block.hbm_ntm_rebirth.fluid_duct_exhaust", "Exhaust Pipe");
        add("block.hbm_ntm_rebirth.fluid_duct_paintable", "Paintable Fluid Duct");
        add("block.hbm_ntm_rebirth.fluid_duct_paintable_block_exhaust", "Paintable Exhaust Pipe");
        add("block.hbm_ntm_rebirth.pipe_anchor", "Pipe Anchor");
        add("block.hbm_ntm_rebirth.fluid_valve", "Fluid Valve");
        add("block.hbm_ntm_rebirth.fluid_switch", "Fluid Switch");
        add("block.hbm_ntm_rebirth.fluid_counter_valve", "Fluid Counter Valve");
        add("block.hbm_ntm_rebirth.fluid_pump", "Fluid Pump");
        add("block.hbm_ntm_rebirth.conveyor", "Conveyor Belt");
        add("block.hbm_ntm_rebirth.conveyor_express", "Express Conveyor Belt");
        add("block.hbm_ntm_rebirth.conveyor_double", "Double-Lane Conveyor Belt");
        add("block.hbm_ntm_rebirth.conveyor_triple", "Triple-Lane Conveyor Belt");
        add("block.hbm_ntm_rebirth.conveyor_lift", "Conveyor Chain Lift");
        add("block.hbm_ntm_rebirth.conveyor_chute", "Conveyor Chute");
        add("block.hbm_ntm_rebirth.machine_battery", "Energy Storage Block (LEGACY)");
        add("block.hbm_ntm_rebirth.machine_fensu", "FEnSU (LEGACY)");
        add("block.hbm_ntm_rebirth.machine_battery_socket", "Battery Socket");
        add("block.hbm_ntm_rebirth.machine_radar", "Radar");
        add("block.hbm_ntm_rebirth.machine_radar_large", "Large Radar");
        add("block.hbm_ntm_rebirth.radar_screen", "Radar Screen");
        add("block.hbm_ntm_rebirth.machine_satlinker", "Satellite ID Manager");
        add("block.hbm_ntm_rebirth.sat_dock", "Cargo Landing Pad");
        add("block.hbm_ntm_rebirth.soyuz_capsule", "Cargo Lander Capsule");
        add("block.hbm_ntm_rebirth.soyuz_launcher", "Soyuz Launch Platform");
        add("block.hbm_ntm_rebirth.launch_pad", "Silo Launch Pad");
        add("item.hbm_ntm_rebirth.radar_linker", "Radar Linker");
        add("radar.detectMissiles", "Detect Missiles");
        add("radar.detectShells", "Detect Shells");
        add("radar.detectPlayers", "Detect Players");
        add("radar.smartMode", "Smart Mode$Redstone output ignores ascending missiles");
        add("radar.redMode", "Redstone Mode$On: Redstone output based on range$Off: Redstone output based on tier");
        add("radar.showMap", "Show Map");
        add("radar.toggleGui", "Toggle GUI");
        add("radar.clearMap", "Clear Map");
        add("radar.target.abm", "Anti-Ballsitic Missile");
        add("radar.target.custom10", "Size 10 Custom Missile");
        add("radar.target.custom1015", "Size 10/15 Custom Missile");
        add("radar.target.custom15", "Size 15 Custom Missile");
        add("radar.target.custom1520", "Size 15/20 Custom Missile");
        add("radar.target.custom20", "Size 20 Custom Missile");
        add("radar.target.doomsday", "Doomsday Missile");
        add("radar.target.shuttle", "Reliant Robin Space Shuttle");
        add("radar.target.tier0", "Tier 0 Missile");
        add("radar.target.tier1", "Tier 1 Missile");
        add("radar.target.tier2", "Tier 2 Missile");
        add("radar.target.tier3", "Tier 3 Missile");
        add("radar.target.tier4", "Tier 4 Missile");
        add("container.hbm_ntm_rebirth.radar.enabled", "Enabled");
        add("container.hbm_ntm_rebirth.radar.disabled", "Disabled");
        add("block.hbm_ntm_rebirth.machine_assembly_machine", "Assembly Machine");
        add("block.hbm_ntm_rebirth.machine_chemical_plant", "Chemical Plant");
        add("block.hbm_ntm_rebirth.machine_liquefactor", "Industrial Liquefaction Machine");
        add("block.hbm_ntm_rebirth.machine_chemical_factory", "Chemical Factory");
        add("block.hbm_ntm_rebirth.machine_refinery", "Oil Refinery");
        add("block.hbm_ntm_rebirth.machine_catalytic_cracker", "Catalytic Cracking Tower");
        add("block.hbm_ntm_rebirth.machine_catalytic_reformer", "Catalytic Reformer");
        add("block.hbm_ntm_rebirth.machine_vacuum_distill", "Vacuum Refinery");
        add("block.hbm_ntm_rebirth.machine_fraction_tower", "Fractioning Tower");
        add("block.hbm_ntm_rebirth.machine_hydrotreater", "Hydrotreater");
        add("block.hbm_ntm_rebirth.machine_coker", "Coker Unit");
        add("block.hbm_ntm_rebirth.machine_pyrooven", "Pyrolysis Oven");
        add("block.hbm_ntm_rebirth.machine_solidifier", "Industrial Solidification Machine");
        add("block.hbm_ntm_rebirth.machine_compressor", "Compressor");
        add("block.hbm_ntm_rebirth.machine_bat9000", "Big-Ass Tank 9000 (LEGACY)");
        add("block.hbm_ntm_rebirth.machine_bigasstank", "Big-Ass Tank");
        add("block.hbm_ntm_rebirth.machine_fluidtank", "Tank");
        add("block.hbm_ntm_rebirth.steel_scaffold", "Steel Scaffold");
        add("block.hbm_ntm_rebirth.barrel_plastic", "Safe Barrel");
        add("block.hbm_ntm_rebirth.barrel_corroded", "Corroded Barrel");
        add("block.hbm_ntm_rebirth.barrel_steel", "Steel Barrel");
        add("block.hbm_ntm_rebirth.barrel_tcalloy", "Technetium Steel Barrel");
        add("block.hbm_ntm_rebirth.barrel_antimatter", "Magnetic Antimatter Container");
        add("block.hbm_ntm_rebirth.machine_pumpjack", "Pumpjack");
        add("block.hbm_ntm_rebirth.machine_well", "Oil Derrick");
        add("block.hbm_ntm_rebirth.machine_fracking_tower", "Hydraulic Fracking Tower");
        add("block.hbm_ntm_rebirth.machine_centrifuge", "Centrifuge");
        add("block.hbm_ntm_rebirth.machine_gascent", "Gas Centrifuge");
        add("block.hbm_ntm_rebirth.machine_ore_slopper", "Ore Slopper");
        add("block.hbm_ntm_rebirth.machine_sawmill", "Stirling Sawmill");
        add("block.hbm_ntm_rebirth.machine_crucible", "Crucible");
        add("block.hbm_ntm_rebirth.machine_gasflare", "Gas Flare");
        add("block.hbm_ntm_rebirth.chimney_brick", "Smokestack");
        add("block.hbm_ntm_rebirth.chimney_industrial", "Industrial Smokestack");
        add("block.hbm_ntm_rebirth.machine_intake", "Air Intake");
        add("block.hbm_ntm_rebirth.machine_drain", "Drainage Pipe");
        add("block.hbm_ntm_rebirth.machine_chungus", "Leviathan Steam Turbine");
        add("block.hbm_ntm_rebirth.machine_hephaestus", "Geothermal Heat Exchanger");
        add("block.hbm_ntm_rebirth.machine_boiler", "Boiler");
        add("block.hbm_ntm_rebirth.machine_industrial_boiler", "Industrial Boiler");
        add("block.hbm_ntm_rebirth.machine_combustion_engine", "Industrial Combustion Engine");
        add("block.hbm_ntm_rebirth.pump_steam", "Steam-Powered Groundwater Pump");
        add("block.hbm_ntm_rebirth.pump_electric", "Electric Groundwater Pump");
        add("block.hbm_ntm_rebirth.heater_heatex", "Heat Exchanging Heater");
        add("block.hbm_ntm_rebirth.heater_firebox", "Firebox");
        add("block.hbm_ntm_rebirth.heater_oven", "Heating Oven");
        add("block.hbm_ntm_rebirth.machine_ashpit", "Ashpit");
        add("block.hbm_ntm_rebirth.heater_oilburner", "Fluid Burner");
        add("block.hbm_ntm_rebirth.heater_electric", "Electric Heater");
        add("block.hbm_ntm_rebirth.machine_condenser_powered", "High-Power Steam Condenser");
        add("block.hbm_ntm_rebirth.machine_assembly_factory", "Assembly Factory");
        add("block.hbm_ntm_rebirth.machine_purex", "PUREX");
        add("block.hbm_ntm_rebirth.machine_silex", "Laser Isotope Separation Chamber (SILEX)");
        add("block.hbm_ntm_rebirth.machine_crystallizer", "Ore Acidizer");
        add("block.hbm_ntm_rebirth.machine_electrolyser", "Electrolyser");
        add("block.hbm_ntm_rebirth.machine_exposure_chamber", "Exposure Chamber");
        add("block.hbm_ntm_rebirth.machine_cyclotron", "Cyclotron");
        add("block.hbm_ntm_rebirth.machine_arc_welder", "Arc Welder");
        add("block.hbm_ntm_rebirth.machine_soldering_station", "Soldering Station");
        add("block.hbm_ntm_rebirth.machine_mixer", "Industrial Mixer");
        add("block.hbm_ntm_rebirth.machine_radiolysis", "Radiolysis Chamber");
        add("block.hbm_ntm_rebirth.machine_radgen", "Radioisotope Generator");
        add("block.hbm_ntm_rebirth.machine_rotary_furnace", "Rotary Furnace");
        add("block.hbm_ntm_rebirth.machine_steam_engine", "Steam Engine");
        add("block.hbm_ntm_rebirth.machine_solar_boiler", "Solar Boiler");
        add("block.hbm_ntm_rebirth.machine_tower_small", "Small Cooling Tower");
        add("block.hbm_ntm_rebirth.machine_tower_large", "Large Cooling Tower");
        add("block.hbm_ntm_rebirth.machine_turbofan", "Turbofan");
        add("block.hbm_ntm_rebirth.machine_turbinegas", "Gas Turbine");
        add("block.hbm_ntm_rebirth.machine_ammo_press", "Ammo Press");
        add("block.hbm_ntm_rebirth.furnace_iron", "Iron Furnace");
        add("block.hbm_ntm_rebirth.furnace_steel", "Steel Furnace");
        add("block.hbm_ntm_rebirth.furnace_combination", "Combination Oven");
        add("block.hbm_ntm_rebirth.machine_blast_furnace", "Blast Furnace");
        add("block.hbm_ntm_rebirth.machine_arc_furnace", "Electric Arc Furnace");
        add("block.hbm_ntm_rebirth.machine_annihilator", "Annihilator");
        add("block.hbm_ntm_rebirth.machine_fel", "Free Electron Laser");
        add("block.hbm_ntm_rebirth.machine_orbus", "Orbus");
        add("block.hbm_ntm_rebirth.machine_mining_laser", "Mining Laser");
        add("block.hbm_ntm_rebirth.machine_strand_caster", "Strand Caster");
        add("block.hbm_ntm_rebirth.machine_wood_burner", "Wood Burner");
        add("block.hbm_ntm_rebirth.machine_stirling", "Stirling Engine");
        add("block.hbm_ntm_rebirth.machine_stirling_steel", "Steel Stirling Engine");
        add("block.hbm_ntm_rebirth.machine_stirling_creative", "Creative Stirling Engine");
        add("block.hbm_ntm_rebirth.machine_deuterium_tower", "Deuterium Tower");
        add("block.hbm_ntm_rebirth.fraction_spacer", "Fraction Tower Spacer");
        add("block.hbm_ntm_rebirth.glass_boron", "Boron Glass");
        add("container.machineAssemblyMachine", "Assembly Machine");
        add("container.machineArcWelder", "Arc Welder");
        add("container.machineChemicalPlant", "Chemical Plant");
        add("container.machineLiquefactor", "Liquefactor");
        add("container.hbm_ntm_rebirth.battery", "Energy Storage Block");
        add("container.hbm_ntm_rebirth.fensu", "FEnSU");
        add("container.batterySocket", "Battery Socket");
        add("container.satLinker", "SatLink Device");
        add("container.hbm_ntm_rebirth.sat_linker", "SatLink Device");
        add("container.hbm_ntm_rebirth.sat_linker.copy.0", "The first slot will copy the satellite/chip's");
        add("container.hbm_ntm_rebirth.sat_linker.copy.1", "frequency and paste it to the second slot.");
        add("container.hbm_ntm_rebirth.sat_linker.randomize.0", "The third slot will randomize the");
        add("container.hbm_ntm_rebirth.sat_linker.randomize.1", "satellite/chip's frequency.");
        add("container.satDock", "Cargo Landing Pad");
        add("container.hbm_ntm_rebirth.sat_dock", "Cargo Landing Pad");
        add("container.hbm_ntm_rebirth.sat_dock.info.0", "Requires linked miner sat chip.");
        add("container.hbm_ntm_rebirth.sat_dock.info.1", "Cargo ship will land periodically to");
        add("container.hbm_ntm_rebirth.sat_dock.info.2", "deliver resources.");
        add("container.hbm_ntm_rebirth.ashpit", "Ashpit");
        add("container.hbm_ntm_rebirth.soyuz_capsule", "Cargo Lander Capsule");
        add("container.hbm_ntm_rebirth.soyuz_launcher", "Soyuz Launch Platform");
        add("container.hbm_ntm_rebirth.launch_pad", "Silo Launch Pad");
        add("gui.hbm_ntm_rebirth.launch_pad.not_ready", "Not ready");
        add("gui.hbm_ntm_rebirth.launch_pad.loading", "Loading...");
        add("gui.hbm_ntm_rebirth.launch_pad.ready", "Ready");
        add("container.hbm_ntm_rebirth.battery.red_low", "Low redstone mode");
        add("container.hbm_ntm_rebirth.battery.red_high", "High redstone mode");
        add("container.hbm_ntm_rebirth.battery.mode.input", "Input");
        add("container.hbm_ntm_rebirth.battery.mode.buffer", "Buffer");
        add("container.hbm_ntm_rebirth.battery.mode.output", "Output");
        add("container.hbm_ntm_rebirth.battery.mode.none", "Disabled");
        add("container.hbm_ntm_rebirth.battery.priority", "Network priority");
        add("container.hbm_ntm_rebirth.battery.priority.low", "Low");
        add("container.hbm_ntm_rebirth.battery.priority.normal", "Normal");
        add("container.hbm_ntm_rebirth.battery.priority.high", "High");
        add("container.hbm_ntm_rebirth.battery.priority.recommended", "Recommended: Low");
        HbmFluidLangEntries.addEnglish(this::add);
        add("container.fluidtank", "Fluid Tank");
        add("container.bat9000", "Big-Ass Tank 9000");
        add("container.barrel", "Fluid Barrel");
        add("container.bigAssTank", "Big-Ass Tank");
        add("container.gasFlare", "Gas Flare");
        add("container.fluidtank.mode", "Mode");
        add("container.fluidtank.mode.input", "Input");
        add("container.fluidtank.mode.buffer", "Buffer");
        add("container.fluidtank.mode.output", "Output");
        add("container.fluidtank.mode.none", "Disabled");
        add("container.fluidtank.damaged", "Damaged");
        add("container.fluidtank.burning", "Burning");
        add("barrel.tooltip.capacity.6000", "Capacity: 6,000mB");
        add("barrel.tooltip.capacity.12000", "Capacity: 12,000mB");
        add("barrel.tooltip.capacity.16000", "Capacity: 16,000mB");
        add("barrel.tooltip.capacity.24000", "Capacity: 24,000mB");
        add("barrel.tooltip.no_hot", "Cannot store hot fluids");
        add("barrel.tooltip.no_corrosive", "Cannot store corrosive fluids");
        add("barrel.tooltip.no_antimatter", "Cannot store antimatter");
        add("barrel.tooltip.can_hot", "Can store hot fluids");
        add("barrel.tooltip.can_corrosive", "Can store corrosive fluids");
        add("barrel.tooltip.can_high_corrosive", "Can store highly corrosive fluids");
        add("barrel.tooltip.can_antimatter", "Can store antimatter");
        add("barrel.tooltip.no_high_corrosive_properly", "Cannot store highly corrosive fluids properly");
        add("barrel.tooltip.leaky", "Leaky");
        add("block.hbm_ntm_rebirth.gas_meltdown", "Meltdown Gas");
        add("block.hbm_ntm_rebirth.gas_monoxide", "Carbon Monoxide");
        add("block.hbm_ntm_rebirth.gas_asbestos", "Asbestos Dust");
        add("block.hbm_ntm_rebirth.gas_coal", "Coal Dust");
        add("block.hbm_ntm_rebirth.chlorine_gas", "Chlorine Gas");
        add("block.hbm_ntm_rebirth.dirt_dead", "Dead Dirt");
        add("block.hbm_ntm_rebirth.dirt_oily", "Oily Dirt");
        add("block.hbm_ntm_rebirth.sand_dirty", "Oily Sand");
        add("block.hbm_ntm_rebirth.sand_dirty_red", "Red Oily Sand");
        add("block.hbm_ntm_rebirth.stone_cracked", "Cracked Stone");
        add("death.attack.monoxide", "%1$s suffocated from carbon monoxide");
        add("death.attack.taint", "%1$s was consumed by taint");
        add("death.attack.electric", "%1$s was electrocuted");
        add("death.attack.shrapnel", "%1$s was shredded by shrapnel");
        add("death.attack.rubble", "%1$s was crushed by flying rubble");
        add("death.attack.blackhole", "%1$s was consumed by a black hole");
        add("block.hbm_ntm_rebirth.rad_absorber", "Radiation Absorber");
        add("block.hbm_ntm_rebirth.rad_absorber.1", "Radiation Absorber Red");
        add("block.hbm_ntm_rebirth.rad_absorber.2", "Radiation Absorber Green");
        add("block.hbm_ntm_rebirth.rad_absorber.3", "Radiation Absorber Pink");
        add("block.hbm_ntm_rebirth.dummy_block", "Dummy Block");
        add("block.hbm_ntm_rebirth.waste_earth", "Waste Earth");
        add("block.hbm_ntm_rebirth.waste_mycelium", "Waste Mycelium");
        add("block.hbm_ntm_rebirth.waste_leaves", "Waste Leaves");
        add("block.hbm_ntm_rebirth.waste_log", "Waste Log");
        add("block.hbm_ntm_rebirth.waste_planks", "Waste Planks");
        add("block.hbm_ntm_rebirth.frozen_grass", "Frozen Grass");
        add("block.hbm_ntm_rebirth.frozen_dirt", "Frozen Dirt");
        add("block.hbm_ntm_rebirth.frozen_log", "Frozen Log");
        add("block.hbm_ntm_rebirth.frozen_planks", "Frozen Planks");
        add("block.hbm_ntm_rebirth.leaves_layer", "Fallen Leaves");
        add("block.hbm_ntm_rebirth.balefire", "Balefire");
        add("block.hbm_ntm_rebirth.sellafield", "Sellafite");
        add("block.hbm_ntm_rebirth.sellafield.1", "Sellafite");
        add("block.hbm_ntm_rebirth.sellafield.2", "Sellafite");
        add("block.hbm_ntm_rebirth.sellafield.3", "Sellafite");
        add("block.hbm_ntm_rebirth.sellafield.4", "Sellafite");
        add("block.hbm_ntm_rebirth.sellafield.5", "Sellafite");
        add("block.hbm_ntm_rebirth.sellafield_slaked", "Slaked Sellafite");
        add("block.hbm_ntm_rebirth.sellafield_bedrock", "Bedrock Sellafite");
        add("block.hbm_ntm_rebirth.ore_sellafield_diamond", "Sellafite Diamond Ore");
        add("block.hbm_ntm_rebirth.ore_sellafield_emerald", "Sellafite Emerald Ore");
        add("block.hbm_ntm_rebirth.ore_sellafield_radgem", "Sellafite Radioactive Gem Ore");
        add("block.hbm_ntm_rebirth.ore_sellafield_schrabidium", "Sellafite Schrabidium Ore");
        add("block.hbm_ntm_rebirth.ore_sellafield_uranium_scorched", "Scorched Sellafite Uranium Ore");
        add("block.hbm_ntm_rebirth.waste_trinitite", "Trinitite Ore");
        add("block.hbm_ntm_rebirth.waste_trinitite_red", "Red Trinitite Ore");
        add("block.hbm_ntm_rebirth.glass_trinitite", "Trinity Glass");
        add("block.hbm_ntm_rebirth.ash_digamma", "Ash");
        add("block.hbm_ntm_rebirth.fire_digamma", "Lingering Digamma");
        add("block.hbm_ntm_rebirth.pribris_digamma", "Blackened RBMK Debris");
        add("block.hbm_ntm_rebirth.volcanic_lava_block", "Volcanic Lava");
        add("block.hbm_ntm_rebirth.rad_lava_block", "Radioactive Volcanic Lava");
        add("block.hbm_ntm_rebirth.mud_block", "Poisonous Mud");
        add("block.hbm_ntm_rebirth.tektite", "Tektite");
        add("block.hbm_ntm_rebirth.ore_tektite_osmiridium", "Osmiridium-Infused Tektite");
        add("block.hbm_ntm_rebirth.crystal_virus", "Dark Crystal");
        add("block.hbm_ntm_rebirth.crystal_hardened", "Hardened Dark Crystal");
        add("block.hbm_ntm_rebirth.glyphid_spawner", "Glyphid Hive Spawner");
        add("block.hbm_ntm_rebirth.nuke_gadget", "The Gadget");
        add("block.hbm_ntm_rebirth.nuke_boy", "Little Boy");
        add("block.hbm_ntm_rebirth.nuke_man", "Fat Man");
        add("block.hbm_ntm_rebirth.nuke_tsar", "Tsar Bomba");
        add("block.hbm_ntm_rebirth.nuke_mike", "Ivy Mike");
        add("block.hbm_ntm_rebirth.nuke_prototype", "The Prototype");
        add("block.hbm_ntm_rebirth.nuke_fleija", "F.L.E.I.J.A.");
        add("block.hbm_ntm_rebirth.nuke_solinium", "The Blue Rinse");
        add("block.hbm_ntm_rebirth.nuke_n2", "N2 Mine");
        add("block.hbm_ntm_rebirth.nuke_custom", "Custom Nuke");
        add("block.hbm_ntm_rebirth.nuke_fstbmb", "Balefire Bomb");
        add("block.hbm_ntm_rebirth.bomb_multi", "Multi Purpose Bomb");
        add("container.nukeCustom", "Custom Nuke");
        add("item.hbm_ntm_rebirth.custom_tnt", "Custom Nuke Explosive Charge");
        add("item.hbm_ntm_rebirth.custom_nuke", "Custom Nuke Nuclear Rod");
        add("item.hbm_ntm_rebirth.custom_hydro", "Custom Nuke Hydrogen Rod");
        add("item.hbm_ntm_rebirth.custom_amat", "Custom Nuke Antimatter Rod");
        add("item.hbm_ntm_rebirth.custom_dirty", "Custom Nuke Dirty Rod");
        add("item.hbm_ntm_rebirth.custom_schrab", "Custom Nuke Schrabidium Rod");
        add("item.hbm_ntm_rebirth.custom_fall", "Custom Nuke Drop Upgrade");
        add("item.hbm_ntm_rebirth.custom_fall.desc", "Makes bomb drop upon activation");
        add("subtitles.hbm_ntm_rebirth.weapon.fstbmb", "Balefire bomb signal");
        add("subtitles.hbm_ntm_rebirth.weapon.nuclear_explosion", "Nuclear explosion");
        add("subtitles.hbm_ntm_rebirth.weapon.rocket_flame", "Rocket flame");
        add("block.hbm_ntm_rebirth.dynamite", "Dynamite");
        add("block.hbm_ntm_rebirth.tnt_ntm", "Actual TNT");
        add("block.hbm_ntm_rebirth.semtex", "Semtex");
        add("block.hbm_ntm_rebirth.c4", "C-4");
        add("block.hbm_ntm_rebirth.det_cord", "Det Cord");
        add("block.hbm_ntm_rebirth.det_charge", "Explosive Charge");
        add("block.hbm_ntm_rebirth.det_nuke", "Nuclear Charge");
        add("block.hbm_ntm_rebirth.charge_dynamite", "Time Bomb");
        add("block.hbm_ntm_rebirth.charge_miner", "Timed Mining Charge");
        add("block.hbm_ntm_rebirth.charge_c4", "Demolition Charge");
        add("block.hbm_ntm_rebirth.charge_semtex", "Semtex Mining Charge");
        add("tooltip.hbm_ntm_rebirth.charge.timer", "Right-click to change timer.");
        add("tooltip.hbm_ntm_rebirth.charge.arm", "Sneak-click to arm.");
        add("tooltip.hbm_ntm_rebirth.charge.defuser", "Can only be disarmed and removed with defuser.");
        add("tooltip.hbm_ntm_rebirth.charge.all_drop", "Will drop all blocks.");
        add("tooltip.hbm_ntm_rebirth.charge.no_damage", "Does not do damage.");
        add("tooltip.hbm_ntm_rebirth.charge.no_drop", "Does not drop blocks.");
        add("tooltip.hbm_ntm_rebirth.charge.fortune", "Fortune III");
        add("block.hbm_ntm_rebirth.red_barrel", "Flammable Barrel");
        add("block.hbm_ntm_rebirth.pink_barrel", "Pink Barrel");
        add("block.hbm_ntm_rebirth.lox_barrel", "Liquid Oxygen Barrel");
        add("block.hbm_ntm_rebirth.yellow_barrel", "Radioactive Barrel");
        add("block.hbm_ntm_rebirth.vitrified_barrel", "Vitrified Nuclear Waste Drum");
        add("item.hbm_ntm_rebirth.powder_tektite", "Tektite Powder");
        add("item.hbm_ntm_rebirth.powder_coal", "Coal Powder");
        add("item.hbm_ntm_rebirth.powder_coal_tiny", "Tiny Pile of Coal Powder");
        add("item.hbm_ntm_rebirth.coke_coal", "Coal Coke");
        add("item.hbm_ntm_rebirth.coke_lignite", "Lignite Coke");
        add("item.hbm_ntm_rebirth.coke_petroleum", "Petroleum Coke");
        add("item.hbm_ntm_rebirth.briquette_coal", "Coal Briquette");
        add("item.hbm_ntm_rebirth.briquette_lignite", "Lignite Briquette");
        add("item.hbm_ntm_rebirth.briquette_wood", "Sawdust Briquette");
        add("item.hbm_ntm_rebirth.oil_tar_crude", "Oil Tar");
        add("item.hbm_ntm_rebirth.oil_tar_crack", "Crack Oil Tar");
        add("item.hbm_ntm_rebirth.oil_tar_coal", "Coal Tar");
        add("item.hbm_ntm_rebirth.oil_tar_wood", "Wood Tar");
        add("item.hbm_ntm_rebirth.oil_tar_wax", "Chlorinated Petroleum Wax");
        add("item.hbm_ntm_rebirth.oil_tar_paraffin", "Paraffin Wax");
        add("item.hbm_ntm_rebirth.powder_ash_wood", "Wood Ash");
        add("item.hbm_ntm_rebirth.powder_ash_coal", "Coal Ash");
        add("item.hbm_ntm_rebirth.powder_ash_misc", "Ash");
        add("item.hbm_ntm_rebirth.powder_ash_fly", "Fly Ash");
        add("item.hbm_ntm_rebirth.powder_ash_soot", "Fine Soot");
        add("item.hbm_ntm_rebirth.powder_ash_fullerene", "Fullerene");
        add("item.hbm_ntm_rebirth.chunk_ore_rare", "Rare Earth Ore Chunk");
        add("item.hbm_ntm_rebirth.chunk_ore_malachite", "Malachite Chunk");
        add("item.hbm_ntm_rebirth.chunk_ore_cryolite", "Cryolite Chunk");
        add("item.hbm_ntm_rebirth.chunk_ore_moonstone", "Moonstone");
        add("item.hbm_ntm_rebirth.plant_item_tobacco", "Tobacco");
        add("item.hbm_ntm_rebirth.plant_item_rope", "Rope");
        add("item.hbm_ntm_rebirth.plant_item_mustardwillow", "Mustard Willow Leaf");
        add("item.hbm_ntm_rebirth.parts_legendary_tier1", "Legendary Parts");
        add("item.hbm_ntm_rebirth.parts_legendary_tier2", "Legendary Parts");
        add("item.hbm_ntm_rebirth.parts_legendary_tier3", "Legendary Parts");
        add("item.hbm_ntm_rebirth.part_generic_piston_pneumatic", "Pneumatic Piston");
        add("item.hbm_ntm_rebirth.part_generic_piston_hydraulic", "Hydraulic Piston");
        add("item.hbm_ntm_rebirth.part_generic_piston_electric", "Electric Piston");
        add("item.hbm_ntm_rebirth.part_generic_lde", "Low-Density Element");
        add("item.hbm_ntm_rebirth.part_generic_hde", "Heavy Duty Element");
        add("item.hbm_ntm_rebirth.part_generic_glass_polarized", "Polarized Lens");
        add("item.hbm_ntm_rebirth.item_expensive.desc", "Expensive mode item");
        add("item.hbm_ntm_rebirth.item_expensive_steel_plating", "Bolted Steel Plating");
        add("item.hbm_ntm_rebirth.item_expensive_heavy_frame", "Heavy Framework");
        add("item.hbm_ntm_rebirth.item_expensive_circuit", "Extensive Circuit Board");
        add("item.hbm_ntm_rebirth.item_expensive_lead_plating", "Radiation Resistant Plating");
        add("item.hbm_ntm_rebirth.item_expensive_ferro_plating", "Reinforced Ferrouranium Panels");
        add("item.hbm_ntm_rebirth.item_expensive_computer", "Mainframe");
        add("item.hbm_ntm_rebirth.item_expensive_bronze_tubes", "Bronze Structural Elements");
        add("item.hbm_ntm_rebirth.item_expensive_plastic", "Plastic Panels");
        add("item.hbm_ntm_rebirth.item_expensive_gold_dust", "Ultra Fine Gold Dust");
        add("item.hbm_ntm_rebirth.item_expensive_degenerate_matter", "Degenerate Matter");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_iron", "Crystalline Iron Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_copper", "Crystalline Copper Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_lithium", "Crystalline Lithium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_silicon", "Crystalline Silicon Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_lead", "Crystalline Lead Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_titanium", "Crystalline Titanium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_aluminium", "Crystalline Aluminium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_sulfur", "Crystalline Sulfur Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_calcium", "Crystalline Calcium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_bismuth", "Crystalline Bismuth Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_radium", "Crystalline Radium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_technetium", "Crystalline Technetium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_polonium", "Crystalline Polonium Fragment");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_uranium", "Crystalline Uranium Fragment");
        add("item.hbm_ntm_rebirth.casing_small", "Small Gunmetal Casing");
        add("item.hbm_ntm_rebirth.casing_large", "Large Gunmetal Casing");
        add("item.hbm_ntm_rebirth.casing_small_steel", "Small Weapon Steel Casing");
        add("item.hbm_ntm_rebirth.casing_large_steel", "Large Weapon Steel Casing");
        add("item.hbm_ntm_rebirth.casing_shotshell", "Black Powder Shotshell Casing");
        add("item.hbm_ntm_rebirth.casing_buckshot", "Plastic Shotshell Casing");
        add("item.hbm_ntm_rebirth.casing_buckshot_advanced", "Advanced Shotshell Casing");
        add("item.hbm_ntm_rebirth.ingot_weaponsteel", "Weapon Steel Ingot");
        add("item.hbm_ntm_rebirth.plate_weaponsteel", "Weapon Steel Plate");
        add("item.hbm_ntm_rebirth.ingot_dura_steel", "High-Speed Steel Ingot");
        add("item.hbm_ntm_rebirth.plate_schrabidium", "Schrabidium Plate");
        add("item.hbm_ntm_rebirth.plate_combine_steel", "CMB Steel Plate");
        add("item.hbm_ntm_rebirth.plate_saturnite", "Saturnite Plate");
        add("item.hbm_ntm_rebirth.fuel_additive_antiknock", "Tetraethyllead Antiknock Agent");
        add("item.hbm_ntm_rebirth.fuel_additive_deicer", "Deicer");
        add("item.hbm_ntm_rebirth.catalytic_converter", "Catalytic Converter");
        add("item.hbm_ntm_rebirth.powder_lignite", "Lignite Powder");
        add("item.hbm_ntm_rebirth.powder_quartz", "Quartz Powder");
        add("item.hbm_ntm_rebirth.powder_lapis", "Lapis Lazuli Powder");
        add("item.hbm_ntm_rebirth.powder_diamond", "Diamond Powder");
        add("item.hbm_ntm_rebirth.powder_emerald", "Emerald Powder");
        add("item.hbm_ntm_rebirth.powder_sawdust", "Sawdust");
        add("item.hbm_ntm_rebirth.ball_resin", "Latex");
        add("item.hbm_ntm_rebirth.powder_limestone", "Limestone Powder");
        add("item.hbm_ntm_rebirth.circuit_vacuum_tube", "Vacuum Tube");
        add("item.hbm_ntm_rebirth.circuit_capacitor", "Capacitor");
        add("item.hbm_ntm_rebirth.circuit_capacitor_tantalium", "Tantalum Capacitor");
        add("item.hbm_ntm_rebirth.circuit_pcb", "Printed Circuit Board");
        add("item.hbm_ntm_rebirth.circuit_silicon", "Printed Silicon Wafer");
        add("item.hbm_ntm_rebirth.circuit_chip", "Microchip");
        add("item.hbm_ntm_rebirth.circuit_chip_bismoid", "Versatile Integrated Circuit");
        add("item.hbm_ntm_rebirth.circuit_analog", "Analog Circuit Board");
        add("item.hbm_ntm_rebirth.circuit_basic", "Integrated Circuit Board");
        add("item.hbm_ntm_rebirth.circuit_advanced", "Military Grade Circuit Board");
        add("item.hbm_ntm_rebirth.circuit_capacitor_board", "Capacitor Board");
        add("item.hbm_ntm_rebirth.circuit_bismoid", "Versatile Circuit Board");
        add("item.hbm_ntm_rebirth.circuit_controller_chassis", "Control Unit Casing");
        add("item.hbm_ntm_rebirth.circuit_controller", "Control Unit");
        add("item.hbm_ntm_rebirth.circuit_controller_advanced", "Advanced Control Unit");
        add("item.hbm_ntm_rebirth.circuit_quantum", "Quantum Processing Unit");
        add("item.hbm_ntm_rebirth.circuit_chip_quantum", "Solid State Quantum Processor");
        add("item.hbm_ntm_rebirth.circuit_controller_quantum", "Quantum Computer");
        add("item.hbm_ntm_rebirth.circuit_atomic_clock", "Atomic Clock");
        add("item.hbm_ntm_rebirth.circuit_numitron", "Incandescent Seven Segment Display");
        add("item.hbm_ntm_rebirth.crt_display", "CRT Display");
        add("item.hbm_ntm_rebirth.sphere_steel", "Steel Sphere");
        add("item.hbm_ntm_rebirth.blade_titanium", "Titanium Turbine Blade");
        add("item.hbm_ntm_rebirth.turbine_titanium", "Titanium Turbine");
        add("item.hbm_ntm_rebirth.blade_tungsten", "Tungsten Turbine Blade");
        add("item.hbm_ntm_rebirth.turbine_tungsten", "Tungsten Turbine");
        add("item.hbm_ntm_rebirth.crystal_diamond", "Diamond Crystals");
        add("item.hbm_ntm_rebirth.crystal_xen", "Artificial Xen Crystal");
        add("item.hbm_ntm_rebirth.ducttape", "Duct Tape");
        add("item.hbm_ntm_rebirth.ingot_meteorite", "Meteorite Ingot");
        add("item.hbm_ntm_rebirth.thruster_medium", "Medium Thruster");
        add("item.hbm_ntm_rebirth.thruster_small", "Small Thruster");
        add("item.hbm_ntm_rebirth.powder_desh_mix", "Desh Blend");
        add("item.hbm_ntm_rebirth.powder_chlorophyte", "Chlorophyte Powder");
        add("item.hbm_ntm_rebirth.wire_dense_copper", "Dense Copper Wire");
        add("item.hbm_ntm_rebirth.wire_dense_titanium", "Dense Titanium Wire");
        add("item.hbm_ntm_rebirth.wire_dense_neodymium", "Dense Neodymium Wire");
        add("item.hbm_ntm_rebirth.pa_coil_gold", "Gold Particle Accelerator Coil");
        add("item.hbm_ntm_rebirth.pa_coil_niobium", "Niobium-Titanium Particle Accelerator Coil");
        add("item.hbm_ntm_rebirth.pa_coil_bscco", "BSCCO Particle Accelerator Coil");
        add("item.hbm_ntm_rebirth.pa_coil_chlorophyte", "Chlorophyte Particle Accelerator Coil");
        add("item.hbm_ntm_rebirth.arc_electrode_graphite", "Graphite Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_lanthanium", "Lanthanium Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_desh", "Desh Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_saturnite", "Saturnite Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_graphite", "Spent Graphite Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_lanthanium", "Spent Lanthanium Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_desh", "Spent Desh Arc Electrode");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_saturnite", "Spent Saturnite Arc Electrode");
        add("item.hbm_ntm_rebirth.drillbit_steel", "Steel Drillbit");
        add("item.hbm_ntm_rebirth.drillbit_steel_diamond", "Steel Drillbit (Diamond-Tipped)");
        add("item.hbm_ntm_rebirth.drillbit_hss", "High-Speed Steel Drillbit");
        add("item.hbm_ntm_rebirth.drillbit_hss_diamond", "High-Speed Steel Drillbit (Diamond-Tipped)");
        add("item.hbm_ntm_rebirth.drillbit_desh", "Desh Drillbit");
        add("item.hbm_ntm_rebirth.drillbit_desh_diamond", "Desh Drillbit (Diamond-Tipped)");
        add("item.hbm_ntm_rebirth.drillbit_tcalloy", "Technetium Steel Drillbit");
        add("item.hbm_ntm_rebirth.drillbit_tcalloy_diamond", "Technetium Steel Drillbit (Diamond-Tipped)");
        add("item.hbm_ntm_rebirth.drillbit_ferro", "Ferrouranium Drillbit");
        add("item.hbm_ntm_rebirth.drillbit_ferro_diamond", "Ferrouranium Drillbit (Diamond-Tipped)");
        add("item.hbm_ntm_rebirth.piston_set_steel", "Steel Piston Set");
        add("item.hbm_ntm_rebirth.piston_set_dura", "High-Speed Steel Piston Set");
        add("item.hbm_ntm_rebirth.piston_set_desh", "Desh Piston Set");
        add("item.hbm_ntm_rebirth.piston_set_starmetal", "Starmetal Piston Set");
        add("item.hbm_ntm_rebirth.nugget_mercury", "Drop of Mercury");
        add("item.hbm_ntm_rebirth.ingot_euphemium", "Euphemium Ingot");
        add("item.hbm_ntm_rebirth.nugget_euphemium", "Euphemium Nugget");
        add("item.hbm_ntm_rebirth.powder_euphemium", "Euphemium Powder");
        add("item.hbm_ntm_rebirth.ingot_mercury", "Mercury Ingot");
        add("item.hbm_ntm_rebirth.bottle_mercury", "Bottle of Mercury");
        add("item.hbm_ntm_rebirth.ingot_gh336", "Ghiorsium-336 Ingot");
        add("item.hbm_ntm_rebirth.nugget_gh336", "Ghiorsium-336 Nugget");
        add("item.hbm_ntm_rebirth.billet_gh336", "Ghiorsium-336 Billet");
        add("item.hbm_ntm_rebirth.ingot_starmetal", "Starmetal Ingot");
        add("item.hbm_ntm_rebirth.crystal_starmetal", "Starmetal Crystal");
        add("item.hbm_ntm_rebirth.gem_volcanic", "Volcanic Gem");
        add("item.hbm_ntm_rebirth.fragment_meteorite", "Meteorite Fragment");
        add("item.hbm_ntm_rebirth.ring_starmetal", "Starmetal Ring");
        add("item.hbm_ntm_rebirth.nugget_lead", "Lead Nugget");
        add("item.hbm_ntm_rebirth.wire_dense_mingrade", "Dense RED Copper Wire");
        add("item.hbm_ntm_rebirth.ball_dynamite", "Dynamite");
        add("item.hbm_ntm_rebirth.ball_tnt", "TNT");
        add("item.hbm_ntm_rebirth.ball_tatb", "TATB");
        add("item.hbm_ntm_rebirth.ingot_c4", "Bar of Composition C-4");
        add("item.hbm_ntm_rebirth.ingot_semtex", "Bar of Semtex");
        add("item.hbm_ntm_rebirth.ballistite", "Ballistite");
        add("item.hbm_ntm_rebirth.cordite", "Cordite");
        add("item.hbm_ntm_rebirth.powder_polonium", "Polonium-210 Powder");
        add("item.hbm_ntm_rebirth.powder_co60", "Cobalt-60 Powder");
        add("item.hbm_ntm_rebirth.powder_sr90", "Strontium-90 Powder");
        add("item.hbm_ntm_rebirth.powder_sr90_tiny", "Tiny Pile of Strontium-90 Powder");
        add("item.hbm_ntm_rebirth.powder_i131", "Iodine-131 Powder");
        add("item.hbm_ntm_rebirth.powder_i131_tiny", "Tiny Pile of Iodine-131 Powder");
        add("item.hbm_ntm_rebirth.powder_xe135", "Xenon-135 Powder");
        add("item.hbm_ntm_rebirth.powder_xe135_tiny", "Tiny Pile of Xenon-135 Powder");
        add("item.hbm_ntm_rebirth.powder_cs137_tiny", "Tiny Pile of Caesium-137 Powder");
        add("item.hbm_ntm_rebirth.powder_au198", "Gold-198 Powder");
        add("item.hbm_ntm_rebirth.powder_at209", "Astatine-209 Powder");
        add("item.hbm_ntm_rebirth.dust", "Dust");
        add("item.hbm_ntm_rebirth.fragment_coltan", "Coltan");
        add("item.hbm_ntm_rebirth.powder_coltan", "Purified Tantalite");
        add("item.hbm_ntm_rebirth.gem_tantalium", "Tantalum Polycrystal");
        add("item.hbm_ntm_rebirth.powder_tantalium", "Tantalum Powder");
        add("item.hbm_ntm_rebirth.nugget_tantalium", "Tantalum Nugget");
        add("item.hbm_ntm_rebirth.stick_c4", "Stick of C-4");
        add("item.hbm_ntm_rebirth.stick_semtex", "Stick of Semtex");
        add("item.hbm_ntm_rebirth.stick_tnt", "Stick of TNT");
        ModItems.EXTRA_PARTS_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitPartName(item.getId().getPath())) {
                addItem(item, title(item.getId().getPath()));
            }
        });
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitControlName(item.getId().getPath())) {
                addItem(item, title(item.getId().getPath()));
            }
        });
        ModItems.NUKE_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitNukeName(item.getId().getPath())) {
                addItem(item, title(item.getId().getPath()));
            }
        });
    }

    private void addSatelliteTranslations() {
        add("item.hbm_ntm_rebirth.designator", "Target Designator");
        add("item.hbm_ntm_rebirth.designator_range", "Laser Target Designator");
        add("item.hbm_ntm_rebirth.designator_manual", "Manual Target Designator");
        add("item.hbm_ntm_rebirth.sat_chip", "Satellite ID-Chip");
        add("item.hbm_ntm_rebirth.sat_coord", "Satellite Designator");
        add("item.hbm_ntm_rebirth.sat_designator", "Satellite Laser Designator");
        add("item.hbm_ntm_rebirth.sat_relay", "Satellite Radar Relay");
        add("item.hbm_ntm_rebirth.sat_foeq", "PEAF - Mk.I FOEQ Duna Probe with experimental Nuclear Propulsion");
        add("item.hbm_ntm_rebirth.sat_gerald", "Gerald The Construction Android");
        add("item.hbm_ntm_rebirth.sat_head_laser", "Death Ray");
        add("item.hbm_ntm_rebirth.sat_head_mapper", "High-Gain Optical Camera");
        add("item.hbm_ntm_rebirth.sat_head_radar", "Radar Dish");
        add("item.hbm_ntm_rebirth.sat_head_resonator", "Xenium Resonator");
        add("item.hbm_ntm_rebirth.sat_head_scanner", "M700 Survey Scanner");
        add("item.hbm_ntm_rebirth.sat_interface", "Satellite Control Interface");
        add("item.hbm_ntm_rebirth.sat_laser", "Orbital Death Ray");
        add("item.hbm_ntm_rebirth.sat_lunar_miner", "Lunar Mining Ship");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier0", "Tier 0");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier1", "Tier 1");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier2", "Tier 2");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier3", "Tier 3");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier4", "Tier 4");
        add("tooltip.hbm_ntm_rebirth.missile.not_launchable", "Not launchable");
        add("tooltip.hbm_ntm_rebirth.missile.fuel", "Fuel: %s");
        add("tooltip.hbm_ntm_rebirth.missile.fuel_capacity", "Fuel capacity: %smB");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.solid", "Solid fuel / prefueled");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.ethanol_peroxide", "Ethanol/Peroxide");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.kerosene_peroxide", "Kerosene/Peroxide");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.kerosene_loxy", "Kerosene/LOX");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.jetfuel_loxy", "Jet fuel/LOX");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.chip", "Guidance chip");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.warhead", "Warhead");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.fuselage", "Fuselage");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.fins", "Stability fins");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.thruster", "Thruster");
        add("tooltip.hbm_ntm_rebirth.custom_missile.empty", "No missile parts installed");
        add("tooltip.hbm_ntm_rebirth.custom_missile.chip", "Chip: %s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.warhead", "Warhead: %s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.fuselage", "Fuselage: %s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.stability", "Stability: %s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.thruster", "Thruster: %s");
        ModItems.MISSILE_TAB_ITEMS.forEach(item -> addItem(item, title(item.getId().getPath())));
        add("item.hbm_ntm_rebirth.sat_mapper", "Surface Mapping Satellite");
        add("item.hbm_ntm_rebirth.sat_miner", "Asteroid Mining Ship");
        add("item.hbm_ntm_rebirth.sat_radar", "Radar Survey Satellite");
        add("item.hbm_ntm_rebirth.sat_resonator", "Xenium Resonator Satellite");
        add("item.hbm_ntm_rebirth.sat_scanner", "Satellite with Depth-Resource Scanning Module");
        add("item.hbm_ntm_rebirth.missile_soyuz", "Soyuz-FG");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin", "Skin");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin.0", "Original");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin.1", "Luna Space Center");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin.2", "Post War");
        add("item.hbm_ntm_rebirth.missile_soyuz_lander", "Missile Soyuz Lander");
        add("satchip.frequency", "Satellite Frequency");
        add("satchip.foeq", "Gives you an achievement. That's it.");
        add("satchip.gerald.desc", "Single use.");
        add("satchip.gerald.desc.0", "Single use.");
        add("satchip.gerald.desc.1", "Requires orbital module.");
        add("satchip.gerald.desc.2", "Melter of CPUs, bane of every server owner.");
        add("satchip.laser", "Allows to summon lasers with a 10 second cooldown.");
        add("satchip.mapper", "Displays currently loaded chunks.");
        add("satchip.miner", "Will deliver ore powders to a cargo landing pad.");
        add("satchip.lunar_miner", "Mines moon turf to deliver it to a cargo landing pad.");
        add("satchip.radar", "Shows a map of active entities.");
        add("satchip.resonator", "Allows for teleportation with no cooldown.");
        add("satchip.scanner", "Creates a topdown map of underground ores.");
        add("satchip.no_satellite", "No satellite found on this frequency.");
        add("satchip.interface.ready", "%s online on frequency %s.");
        add("satchip.coord.ready", "%s coordinate link ready on frequency %s.");
        add("tooltip.hbm_ntm_rebirth.designator.no_target", "Please select a target.");
        add("tooltip.hbm_ntm_rebirth.designator.target", "Target Coordinates:");
    }

    private static boolean hasExplicitNukeName(String id) {
        return switch (id) {
            case "detonator",
                 "stick_c4",
                 "stick_semtex",
                 "stick_tnt",
                 "custom_tnt",
                 "custom_nuke",
                 "custom_hydro",
                 "custom_amat",
                 "custom_dirty",
                 "custom_schrab",
                 "custom_fall" -> true;
            default -> false;
        };
    }

    private static boolean hasExplicitControlName(String id) {
        if (id.startsWith("upgrade_")) {
            return true;
        }
        if (id.endsWith("_sword") || id.endsWith("_pickaxe") || id.endsWith("_axe") || id.endsWith("_shovel")) {
            return true;
        }
        if (id.startsWith("arc_electrode_")) {
            return true;
        }
        if (id.startsWith("pa_coil_")) {
            return true;
        }
        if (id.startsWith("hazmat_")) {
            return true;
        }
        if (id.startsWith("rod_")) {
            return true;
        }
        if (id.startsWith("pellet_rtg_depleted_")) {
            return true;
        }
        if (id.startsWith("pwr_fuel_")) {
            return true;
        }
        if (id.startsWith("watz_pellet_")) {
            return true;
        }
        return switch (id) {
            case "battery_potato",
                 "battery_creative",
                 "battery_redstone",
                 "battery_lead",
                 "battery_lithium",
                 "battery_sodium",
                 "battery_schrabidium",
                 "battery_quantum",
                 "capacitor_copper",
                 "capacitor_gold",
                 "capacitor_niobium",
                 "capacitor_tantalum",
                 "capacitor_bismuth",
                 "capacitor_spark",
                 "battery_sc.empty",
                 "battery_sc.waste",
                 "battery_sc.ra226",
                 "battery_sc.tc99",
                 "battery_sc.co60",
                 "battery_sc.pu238",
                 "battery_sc.po210",
                 "battery_sc.au198",
                 "battery_sc.pb209",
                 "battery_sc.am241",
                 "singularity",
                 "singularity_counter_resonant",
                 "singularity_super_heated",
                 "singularity_spark",
                 "black_hole",
                 "particle_digamma",
                 "blueprints",
                 "screwdriver",
                 "hand_drill",
                 "defuser",
                 "drillbit_steel",
                 "drillbit_steel_diamond",
                 "drillbit_hss",
                 "drillbit_hss_diamond",
                 "drillbit_desh",
                 "drillbit_desh_diamond",
                 "drillbit_tcalloy",
                 "drillbit_tcalloy_diamond",
                 "drillbit_ferro",
                 "drillbit_ferro_diamond",
                 "piston_set_steel",
                 "piston_set_dura",
                 "piston_set_desh",
                 "piston_set_starmetal",
                 "centri_stick",
                 "smashing_hammer",
                 "drax",
                 "drax_mk2",
                 "drax_mk3",
                 "mese_gavel",
                 "chainsaw",
                 "settings_tool",
                 "pellet_antimatter",
                 "crystal_xen",
                 "catalytic_converter",
                 "fuel_additive_antiknock",
                 "fuel_additive_deicer" -> true;
            default -> false;
        };
    }

    private static boolean hasExplicitPartName(String id) {
        if (id.startsWith("circuit_")) {
            return true;
        }
        if (id.startsWith("pa_coil_")) {
            return true;
        }
        if (id.startsWith("watz_pellet_")) {
            return true;
        }
        return switch (id) {
            case "ball_dynamite", "ball_tnt", "ball_tatb", "ingot_c4", "ballistite", "cordite" -> true;
            case "powder_tektite" -> true;
            case "nugget_mercury", "ingot_mercury", "bottle_mercury", "nugget_lead" -> true;
            case "ingot_euphemium", "nugget_euphemium", "powder_euphemium",
                 "plate_euphemium" -> true;
            case "ingot_gh336", "nugget_gh336", "billet_gh336" -> true;
            case "ingot_starmetal", "crystal_starmetal", "gem_volcanic",
                 "fragment_meteorite", "ring_starmetal" -> true;
            case "crystal_diamond", "ducttape", "ingot_meteorite", "thruster_medium", "thruster_small" -> true;
            case "plate_paa", "rag_damp", "rag_piss", "watch",
                 "hazmat_cloth", "hazmat_cloth_red", "hazmat_cloth_grey" -> true;
            case "ingot_weaponsteel", "plate_weaponsteel",
                  "ingot_dura_steel",
                  "plate_schrabidium",
                  "plate_combine_steel",
                  "plate_saturnite" -> true;
            case "wire_dense_copper", "wire_dense_titanium", "wire_dense_neodymium",
                 "wire_dense_mingrade" -> true;
            case "dust", "fragment_coltan", "powder_coltan", "gem_tantalium", "powder_tantalium",
                 "nugget_tantalium" -> true;
            case "powder_coal", "powder_coal_tiny", "powder_desh_mix", "powder_chlorophyte",
                 "powder_polonium", "powder_co60", "powder_sr90", "powder_sr90_tiny",
                 "powder_i131", "powder_i131_tiny", "powder_xe135", "powder_xe135_tiny",
                 "powder_cs137_tiny", "powder_au198", "powder_at209" -> true;
            case "coke_coal",
                 "coke_lignite",
                 "coke_petroleum",
                 "briquette_coal",
                 "briquette_lignite",
                 "briquette_wood" -> true;
            case "oil_tar_crude",
                 "oil_tar_crack",
                 "oil_tar_coal",
                 "oil_tar_wood",
                 "oil_tar_wax",
                 "oil_tar_paraffin" -> true;
            case "powder_ash_wood",
                 "powder_ash_coal",
                 "powder_ash_misc",
                 "powder_ash_fly",
                 "powder_ash_soot",
                 "powder_ash_fullerene" -> true;
            case "chunk_ore_rare",
                 "chunk_ore_malachite",
                 "chunk_ore_cryolite",
                 "chunk_ore_moonstone",
                 "plant_item_tobacco",
                 "plant_item_rope",
                 "plant_item_mustardwillow",
                 "parts_legendary_tier1",
                 "parts_legendary_tier2",
                 "parts_legendary_tier3",
                 "part_generic_piston_pneumatic",
                 "part_generic_piston_hydraulic",
                 "part_generic_piston_electric",
                 "part_generic_lde",
                 "part_generic_hde",
                 "part_generic_glass_polarized",
                 "item_expensive_steel_plating",
                 "item_expensive_heavy_frame",
                 "item_expensive_circuit",
                 "item_expensive_lead_plating",
                 "item_expensive_ferro_plating",
                 "item_expensive_computer",
                 "item_expensive_bronze_tubes",
                 "item_expensive_plastic",
                 "item_expensive_gold_dust",
                 "item_expensive_degenerate_matter",
                 "ore_byproduct_b_iron",
                 "ore_byproduct_b_copper",
                 "ore_byproduct_b_lithium",
                 "ore_byproduct_b_silicon",
                 "ore_byproduct_b_lead",
                 "ore_byproduct_b_titanium",
                 "ore_byproduct_b_aluminium",
                 "ore_byproduct_b_sulfur",
                 "ore_byproduct_b_calcium",
                 "ore_byproduct_b_bismuth",
                 "ore_byproduct_b_radium",
                 "ore_byproduct_b_technetium",
                 "ore_byproduct_b_polonium",
                 "ore_byproduct_b_uranium",
                 "casing_small",
                 "casing_large",
                 "casing_small_steel",
                 "casing_large_steel",
                 "casing_shotshell",
                 "casing_buckshot",
                 "casing_buckshot_advanced",
                 "powder_lignite",
                 "powder_quartz",
                 "powder_lapis",
                 "powder_diamond",
                  "powder_emerald",
                  "powder_sawdust",
                  "ball_resin",
                  "powder_limestone",
                  "drax",
                  "drax_mk2",
                  "drax_mk3",
                  "crt_display",
                  "sphere_steel",
                  "blade_titanium",
                  "turbine_titanium",
                  "blade_tungsten",
                  "turbine_tungsten",
                  "drillbit_steel",
                  "drillbit_steel_diamond",
                  "drillbit_hss",
                  "drillbit_hss_diamond",
                  "drillbit_desh",
                  "drillbit_desh_diamond",
                  "drillbit_tcalloy",
                  "drillbit_tcalloy_diamond",
                  "drillbit_ferro",
                  "drillbit_ferro_diamond",
                  "piston_set_steel",
                  "piston_set_dura",
                  "piston_set_desh",
                  "piston_set_starmetal" -> true;
            default -> false;
        };
    }

    private void addAbilityTranslations() {
        add("tool.ability.recursion", "Vein Miner");
        add("tool.ability.hammer", "Hammer");
        add("tool.ability.hammer_flat", "Flat Hammer");
        add("tool.ability.explosion", "Explosion");
        add("tool.ability.silktouch", "Silk Touch");
        add("tool.ability.luck", "Luck");
        add("tool.ability.smelter", "Smelter");
        add("tool.ability.shredder", "Shredder");
        add("tool.ability.centrifuge", "Centrifuge");
        add("tool.ability.crystallizer", "Crystallizer");
        add("tool.ability.mercury", "Mercury Touch");
        add("weapon.ability.radiation", "Radiation");
        add("weapon.ability.vampire", "Vampire");
        add("weapon.ability.stun", "Stun");
        add("weapon.ability.phosphorus", "Phosphorus");
        add("weapon.ability.fire", "Fire");
        add("weapon.ability.chainsaw", "Chainsaw");
        add("weapon.ability.beheader", "Beheader");
        add("weapon.ability.bobble", "Bobblehead");
        add("tooltip.hbm_ntm_rebirth.abilities", "Abilities:");
        add("tooltip.hbm_ntm_rebirth.abilities.cycle", "Right click to cycle through presets!");
        add("tooltip.hbm_ntm_rebirth.abilities.reset", "Sneak-click to go to first preset!");
        add("tooltip.hbm_ntm_rebirth.abilities.customize", "Alt-click to open customization GUI!");
        add("tooltip.hbm_ntm_rebirth.weapon_modifiers", "Weapon modifiers:");
        add("tooltip.hbm_ntm_rebirth.depth_rock_breaker", "Can break depth rock!");
        add("chat.hbm_ntm_rebirth.tool_ability.deactivated", "[Tool ability deactivated]");
        add("chat.hbm_ntm_rebirth.tool_ability.enabled", "[Enabled");
        add("container.hbm_ntm_rebirth.tool_ability", "Tool Abilities");
        add("container.hbm_ntm_rebirth.tool_ability.preset", "Preset %s/%s");
        add("container.hbm_ntm_rebirth.tool_ability.area", "Area");
        add("container.hbm_ntm_rebirth.tool_ability.harvest", "Harvest");
        add("container.hbm_ntm_rebirth.tool_ability.reset", "Reset");
        add("container.hbm_ntm_rebirth.tool_ability.delete", "Del");
        add("container.hbm_ntm_rebirth.tool_ability.add", "Add");
        add("container.hbm_ntm_rebirth.tool_ability.first", "First");
        add("container.hbm_ntm_rebirth.tool_ability.prev", "Prev");
        add("container.hbm_ntm_rebirth.tool_ability.next", "Next");
        add("container.hbm_ntm_rebirth.tool_ability.done", "Done");
    }

    private void addArmorModTranslations() {
        add("armorMod.all", "All");
        add("armorMod.applicableTo", "Applicable To:");
        add("armorMod.boots", "Boots");
        add("armorMod.chestplates", "Chestplates");
        add("armorMod.helmets", "Helmets");
        add("armorMod.leggings", "Leggings");
        add("armorMod.slot", "Slot:");
        add("armorMod.insertHere", "Insert armor here");
        add("armorMod.type.battery", "Battery");
        add("armorMod.type.boots", "Boots");
        add("armorMod.type.chestplate", "Chestplate");
        add("armorMod.type.cladding", "Cladding");
        add("armorMod.type.helmet", "Helmet");
        add("armorMod.type.insert", "Insert");
        add("armorMod.type.leggings", "Leggings");
        add("armorMod.type.servo", "Servos");
        add("armorMod.type.special", "Special");
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
