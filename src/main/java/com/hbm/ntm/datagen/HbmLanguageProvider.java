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
        add("item.hbm_ntm_rebirth.digamma_diagnostic", "Digamma Diagnostic");
        add("item.hbm_ntm_rebirth.pollution_detector", "Pollution Detector");
        add("item.hbm_ntm_rebirth.radaway", "RadAway");
        add("item.hbm_ntm_rebirth.radaway_strong", "RadAway Strong");
        add("item.hbm_ntm_rebirth.radaway_flush", "RadAway Flush");
        add("item.hbm_ntm_rebirth.radx", "Rad-X");
        add("item.hbm_ntm_rebirth.radx.desc", "Increases radiation resistance by 0.2 (37%) for 3 minutes");
        add("item.hbm_ntm_rebirth.gas_mask_filter", "Gas Mask Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_mono", "Catalytic Mask Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_combo", "Gas Mask Combo Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_rag", "Makeshift Gas Mask Filter");
        add("item.hbm_ntm_rebirth.gas_mask_filter_piss", "Advanced Makeshift Gas Mask Filter");
        add("item.hbm_ntm_rebirth.attachment_mask", "Gas Mask Attachable");
        add("item.hbm_ntm_rebirth.attachment_mask_mono", "Half Mask Attachable");
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
        add("item.hbm_ntm_rebirth.medal_liquidator", "Liquidator Medal");
        add("subtitles.hbm_ntm_rebirth.tool.gasmask_screw", "Gas mask filter installed");
        add("subtitles.hbm_ntm_rebirth.item.syringe", "Syringe injects");
        addSatelliteTranslations();
        add("info.asbestos", "My lungs are burning.");
        add("info.coaldust", "It's hard to breathe here.");
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
        add("desc.item.battery.charge", "Charge: %s / %sHE");
        add("desc.item.battery.chargePerc", "Charge: %s%%");
        add("desc.item.battery.chargeRate", "Charge rate: %sHE/tick");
        add("desc.item.battery.dischargeRate", "Discharge rate: %sHE/tick");
        add("desc.item.wasteCooling", "Cooling down");
        add("effect.hbm_ntm_rebirth.radiation", "Radiation");
        add("effect.hbm_ntm_rebirth.radaway", "RadAway");
        add("effect.hbm_ntm_rebirth.radx", "Rad-X");
        add("effect.hbm_ntm_rebirth.taint", "Taint");
        add("effect.hbm_ntm_rebirth.mutation", "Tainted Heart");
        add("effect.hbm_ntm_rebirth.stability", "Stability");
        add("effect.hbm_ntm_rebirth.lead", "Lead Poisoning");
        add("geiger.title", "Geiger Counter");
        add("geiger.chunkRad", "Chunk radiation: %s RAD/s");
        add("geiger.envRad", "Environmental dose: %s RAD/s");
        add("geiger.playerRad", "Player dose: %s RAD");
        add("geiger.playerRes", "Radiation resistance: %s%%");
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
        add("subtitles.hbm_ntm_rebirth.block.assembler_operate", "Assembly machine operates");
        add("subtitles.hbm_ntm_rebirth.block.assembler_strike", "Assembly machine strikes");
        add("subtitles.hbm_ntm_rebirth.block.assembler_start", "Assembly machine starts");
        add("subtitles.hbm_ntm_rebirth.block.assembler_stop", "Assembly machine stops");
        add("subtitles.hbm_ntm_rebirth.block.assembler_cut", "Assembly machine cuts");
        add("subtitles.hbm_ntm_rebirth.block.chemplant_operate", "Chemical plant operates");
        add("subtitles.hbm_ntm_rebirth.block.chemical_plant", "Chemical plant reacts");
        add("subtitles.hbm_ntm_rebirth.block.debris", "Debris tumbles");
        add("subtitles.hbm_ntm_rebirth.tool.geiger", "Geiger counter clicks");
        add("subtitles.hbm_ntm_rebirth.tool.tech_boop", "Device beeps");
        add("subtitles.hbm_ntm_rebirth.tool.tech_bleep", "Detonator bleeps");
        add("subtitles.hbm_ntm_rebirth.tool.radaway", "RadAway injector hisses");
        add("subtitles.hbm_ntm_rebirth.player.cough", "Player coughs");
        add("subtitles.hbm_ntm_rebirth.entity.ufo_blast", "Energy discharge");
        add("subtitles.hbm_ntm_rebirth.entity.chopper", "Chopper drones");
        add("subtitles.hbm_ntm_rebirth.alarm.siren", "Siren blares");
        add("subtitles.hbm_ntm_rebirth.weapon.ricochet", "Bullet ricochets");
        add("subtitles.hbm_ntm_rebirth.weapon.gbounce", "Grenade bounces");
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
        add("block.hbm_ntm_rebirth.red_cable", "Red Copper Cable");
        add("block.hbm_ntm_rebirth.red_cable_gauge", "Power Gauge");
        add("block.hbm_ntm_rebirth.cable_switch", "Red Copper Cable Switch");
        add("block.hbm_ntm_rebirth.cable_detector", "Red Copper Cable Detector");
        add("block.hbm_ntm_rebirth.cable_diode", "Red Copper Diode");
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
        add("block.hbm_ntm_rebirth.machine_battery_socket", "Battery Socket");
        add("block.hbm_ntm_rebirth.machine_radar", "Radar");
        add("block.hbm_ntm_rebirth.machine_radar_large", "Large Radar");
        add("block.hbm_ntm_rebirth.radar_screen", "Radar Screen");
        add("block.hbm_ntm_rebirth.machine_satlinker", "Satellite ID Manager");
        add("block.hbm_ntm_rebirth.sat_dock", "Cargo Landing Pad");
        add("item.hbm_ntm_rebirth.radar_linker", "Radar Linker");
        add("radar.detectMissiles", "Detect Missiles");
        add("radar.detectShells", "Detect Shells");
        add("radar.detectPlayers", "Detect Players");
        add("radar.smartMode", "Smart Mode$Redstone output ignores ascending missiles");
        add("radar.redMode", "Redstone Mode$On: Redstone output based on range$Off: Redstone output based on tier");
        add("radar.showMap", "Show Map");
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
        add("block.hbm_ntm_rebirth.machine_assembly_factory", "Assembly Factory");
        add("block.hbm_ntm_rebirth.machine_purex", "PUREX");
        add("block.hbm_ntm_rebirth.machine_silex", "Laser Isotope Separation Chamber (SILEX)");
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
        add("block.hbm_ntm_rebirth.glass_boron", "Boron Glass");
        add("container.machineAssemblyMachine", "Assembly Machine");
        add("container.machineChemicalPlant", "Chemical Plant");
        add("container.machineLiquefactor", "Liquefactor");
        add("container.hbm_ntm_rebirth.battery", "Energy Storage Block");
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
        add("item.hbm_ntm_rebirth.sat_mapper", "Surface Mapping Satellite");
        add("item.hbm_ntm_rebirth.sat_miner", "Asteroid Mining Ship");
        add("item.hbm_ntm_rebirth.sat_radar", "Radar Survey Satellite");
        add("item.hbm_ntm_rebirth.sat_resonator", "Xenium Resonator Satellite");
        add("item.hbm_ntm_rebirth.sat_scanner", "Satellite with Depth-Resource Scanning Module");
        add("satchip.frequency", "Satellite Frequency");
        add("satchip.foeq", "Smells faintly of mint and plutonium.");
        add("satchip.gerald.desc", "The tape says not to ask where he came from.");
        add("satchip.laser", "Allows targeted orbital laser strikes.");
        add("satchip.mapper", "Provides surface map data.");
        add("satchip.miner", "Returns asteroid mining cargo.");
        add("satchip.lunar_miner", "Returns lunar mining cargo.");
        add("satchip.radar", "Provides radar survey data.");
        add("satchip.resonator", "Allows coordinate-based teleportation.");
        add("satchip.scanner", "Provides depth-resource scanning data.");
        add("satchip.no_satellite", "No satellite found on this frequency.");
        add("satchip.interface.ready", "%s online on frequency %s.");
        add("satchip.coord.ready", "%s coordinate link ready on frequency %s.");
    }

    private static boolean hasExplicitNukeName(String id) {
        return switch (id) {
            case "detonator",
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
                 "centri_stick",
                 "smashing_hammer",
                 "mese_gavel",
                 "settings_tool",
                 "pellet_antimatter",
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
        return switch (id) {
            case "powder_tektite" -> true;
            case "ingot_weaponsteel", "plate_weaponsteel",
                 "ingot_dura_steel",
                 "plate_schrabidium",
                 "plate_combine_steel",
                 "plate_saturnite" -> true;
            case "powder_coal", "powder_coal_tiny" -> true;
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
                 "powder_limestone" -> true;
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
        add("chat.hbm_ntm_rebirth.tool_ability.deactivated", "[Tool ability deactivated]");
        add("chat.hbm_ntm_rebirth.tool_ability.enabled", "[Enabled");
    }

    private void addArmorModTranslations() {
        add("armorMod.all", "All");
        add("armorMod.applicableTo", "Applicable To:");
        add("armorMod.boots", "Boots");
        add("armorMod.chestplates", "Chestplates");
        add("armorMod.helmets", "Helmets");
        add("armorMod.leggings", "Leggings");
        add("armorMod.slot", "Slot:");
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
