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
        add("item.hbm.upgrade_template", "Machine Upgrade Template");
        add("item.hbm.blueprints", "Blueprints");
        add("item.hbm.upgrade_speed_1", "Speed Upgrade I");
        add("item.hbm.upgrade_speed_2", "Speed Upgrade II");
        add("item.hbm.upgrade_speed_3", "Speed Upgrade III");
        add("item.hbm.upgrade_power_1", "Power Saving Upgrade I");
        add("item.hbm.upgrade_power_2", "Power Saving Upgrade II");
        add("item.hbm.upgrade_power_3", "Power Saving Upgrade III");
        add("item.hbm.upgrade_overdrive_1", "Overdrive Upgrade I");
        add("item.hbm.upgrade_overdrive_2", "Overdrive Upgrade II");
        add("item.hbm.upgrade_overdrive_3", "Overdrive Upgrade III");
        add("item.hbm.template_folder", "Machine Template Folder");
        add("item.hbm.template_folder.desc", "Machine Templates: Paper + Dye$Press Stamps: Flat Stamp$Siren Tracks: Insulator + Steel Plate");
        add("item.hbm.stamp_iron_plate", "Plate Stamp (Iron)");
        add("item.hbm.stamp_iron_flat", "Flat Stamp (Iron)");
        add("item.hbm.stamp_iron_wire", "Wire Stamp (Iron)");
        add("item.hbm.stamp_iron_circuit", "Circuit Stamp (Iron)");
        add("item.hbm.stamp_357", ".357 Magnum Stamp");
        add("item.hbm.stamp_44", ".44 Magnum Stamp");
        add("item.hbm.stamp_50", "Large Caliber Stamp");
        add("item.hbm.stamp_9", "Small Caliber Stamp");
        add("item.hbm.stamp_book_printing1", "Printing Press Stamp (Part 1)");
        add("item.hbm.stamp_book_printing2", "Printing Press Stamp (Part 2)");
        add("item.hbm.stamp_book_printing3", "Printing Press Stamp (Part 3)");
        add("item.hbm.stamp_book_printing4", "Printing Press Stamp (Part 4)");
        add("item.hbm.stamp_book_printing5", "Printing Press Stamp (Part 5)");
        add("item.hbm.stamp_book_printing6", "Printing Press Stamp (Part 6)");
        add("item.hbm.stamp_book_printing7", "Printing Press Stamp (Part 7)");
        add("item.hbm.stamp_book_printing8", "Printing Press Stamp (Part 8)");
        add("item.hbm.page_of_page1", "Page 1");
        add("item.hbm.page_of_page2", "Page 2");
        add("item.hbm.page_of_page3", "Page 3");
        add("item.hbm.page_of_page4", "Page 4");
        add("item.hbm.page_of_page5", "Page 5");
        add("item.hbm.page_of_page6", "Page 6");
        add("item.hbm.page_of_page7", "Page 7");
        add("item.hbm.page_of_page8", "Page 8");
        add("item.hbm.geiger_counter", "Geiger Counter");
        add("item.hbm.digamma_diagnostic", "Digamma Diagnostic");
        add("item.hbm.radaway", "RadAway");
        add("item.hbm.radaway_strong", "RadAway Strong");
        add("item.hbm.radaway_flush", "RadAway Flush");
        add("item.hbm.radx", "Rad-X");
        add("item.hbm.radx.desc", "Increases radiation resistance by 0.2 (37%) for 3 minutes");
        add("info.asbestos", "My lungs are burning.");
        add("info.coaldust", "It's hard to breathe here.");
        add("item.hbm.containment_box", "Lead-Lined Box");
        add("item.hbm.plastic_bag", "Plastic Bag");
        add("item.hbm.toolbox", "Toolbox");
        add("item.hbm.toolbox.desc.swap", "Click with the toolbox to swap hotbars in/out of the toolbox.");
        add("item.hbm.toolbox.desc.open", "Shift-click with the toolbox to open the toolbox.");
        add("item.hbm.settings_tool", "Settings Tool");
        add("item.hbm.settings_tool.desc1", "Can copy the settings (filters, fluid ID, etc) of machines");
        add("item.hbm.settings_tool.desc2", "Shift right-click to copy, right click to paste");
        add("item.hbm.settings_tool.desc3", "Ctrl click on pipes to paste settings to multiple pipes");
        add("item.hbm.settings_tool.none", " None ");
        add("item.hbm.settings_tool.unknown", "Unknown");
        add("item.hbm.settings_tool.copied", "Copied settings of %s");
        add("item.hbm.settings_tool.copy_failed", "Copy failed");
        add("item.hbm.settings_tool.pasted", "Pasted settings");
        add("item.hbm.settings_tool.paste_failed", "Paste failed");
        add("item.hbm.conveyor_wand", "Conveyor Belt");
        add("item.hbm.conveyor_wand.regular", "Conveyor Belt");
        add("item.hbm.conveyor_wand.express", "Express Conveyor Belt");
        add("item.hbm.conveyor_wand.double", "Double-Lane Conveyor Belt");
        add("item.hbm.conveyor_wand.triple", "Triple-Lane Conveyor Belt");
        add("item.hbm.conveyor_wand.desc", "Click two points to create a conveyor route");
        add("item.hbm.conveyor_wand.vertical.desc", "Can place lifts and chutes for vertical item transport");
        add("item.hbm.conveyor_wand.selected", "First point selected");
        add("item.hbm.conveyor_wand.built", "Conveyor built");
        add("item.hbm.conveyor_wand.not_enough", "Not enough conveyors");
        add("item.hbm.conveyor_wand.obstructed", "Conveyor obstructed");
        add("item.hbm.canister_empty", "Empty Canister");
        add("item.hbm.canister_full", "Canister");
        add("item.hbm.canister_napalm", "Napalm Canister");
        add("item.hbm.gas_empty", "Empty Gas Bottle");
        add("item.hbm.gas_full", "Gas Bottle");
        add("item.hbm.fluid_tank_empty", "Empty Fluid Tank");
        add("item.hbm.fluid_tank_full", "Fluid Tank");
        add("item.hbm.fluid_tank_lead_empty", "Empty Lead-Lined Tank");
        add("item.hbm.fluid_tank_lead_full", "Lead-Lined Tank");
        add("item.hbm.fluid_barrel_empty", "Empty Fluid Barrel");
        add("item.hbm.fluid_barrel_full", "Fluid Barrel");
        add("item.hbm.fluid_barrel_infinite", "Infinite Fluid Barrel");
        add("item.hbm.fluid_pack_empty", "Empty Fluid Pack");
        add("item.hbm.fluid_pack_full", "Fluid Pack");
        add("item.hbm.biomass", "Biomass");
        add("item.hbm.biomass_compressed", "Compressed Biomass");
        add("item.hbm.disperser_canister_empty", "Empty Disperser Canister");
        add("item.hbm.disperser_canister", "Disperser Canister");
        add("item.hbm.glyphid_gland_empty", "Empty Glyphid Gland");
        add("item.hbm.glyphid_gland", "Glyphid Gland");
        add("item.hbm.inf_water", "Infinite Water");
        add("item.hbm.inf_water_mk2", "Infinite Water Mk2");
        add("item.hbm.chlorine_pinwheel", "Chlorine Pinwheel");
        add("item.hbm.fluid_identifier_multi", "Multi Fluid Identifier");
        add("item.hbm.fluid_identifier_multi.info", "Fluid:");
        add("item.hbm.fluid_identifier_multi.info2", "Secondary:");
        add("item.hbm.battery_potato", "Potato Battery");
        add("item.hbm.battery_creative", "Infinite Battery");
        add("item.hbm.battery_redstone", "Redstone Battery");
        add("item.hbm.battery_lead", "Lead-Acid Battery");
        add("item.hbm.battery_lithium", "Lithium-Ion Battery");
        add("item.hbm.battery_sodium", "Sodium-Iron Battery");
        add("item.hbm.battery_schrabidium", "Schrabidium Battery");
        add("item.hbm.battery_quantum", "Quantum Battery");
        add("item.hbm.capacitor_copper", "Copper Capacitor");
        add("item.hbm.capacitor_gold", "Gold Capacitor");
        add("item.hbm.capacitor_niobium", "Niobium Capacitor");
        add("item.hbm.capacitor_tantalum", "Tantalum Capacitor");
        add("item.hbm.capacitor_bismuth", "Bismuth Capacitor");
        add("item.hbm.capacitor_spark", "Spark Capacitor");
        add("item.hbm.battery_sc.empty", "Empty Self-Charging Battery");
        add("item.hbm.battery_sc.waste", "Spent Fuel Self-Charging Battery");
        add("item.hbm.battery_sc.ra226", "Radium-226 Self-Charging Battery");
        add("item.hbm.battery_sc.tc99", "Technetium-99 Self-Charging Battery");
        add("item.hbm.battery_sc.co60", "Cobalt-60 Self-Charging Battery");
        add("item.hbm.battery_sc.pu238", "Plutonium-238 Self-Charging Battery");
        add("item.hbm.battery_sc.po210", "Polonium-210 Self-Charging Battery");
        add("item.hbm.battery_sc.au198", "Gold-198 Self-Charging Battery");
        add("item.hbm.battery_sc.pb209", "Lead-209 Self-Charging Battery");
        add("item.hbm.battery_sc.am241", "Americium-241 Self-Charging Battery");
        add("desc.item.battery.charge", "Charge: %s / %sHE");
        add("desc.item.battery.chargePerc", "Charge: %s%%");
        add("desc.item.battery.chargeRate", "Charge rate: %sHE/tick");
        add("desc.item.battery.dischargeRate", "Discharge rate: %sHE/tick");
        add("desc.item.wasteCooling", "Cooling down");
        add("effect.hbm.radiation", "Radiation");
        add("effect.hbm.radaway", "RadAway");
        add("effect.hbm.radx", "Rad-X");
        add("effect.hbm.taint", "Taint");
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
        add("tooltip.hbm.damage.set", "Armor set damage resistance");
        add("tooltip.hbm.damage.item", "Item damage resistance");
        add("tooltip.hbm.damage.line", "%s: %s/%s%%");
        add("tooltip.hbm.damage.other", "Other");
        add("tooltip.hbm.damage.category.EXPL", "Explosion");
        add("tooltip.hbm.damage.category.FIRE", "Fire");
        add("tooltip.hbm.damage.category.PHYS", "Physical");
        add("tooltip.hbm.damage.category.EN", "Energy");
        add("tooltip.hbm.damage.exact.drown", "Drowning");
        add("tooltip.hbm.damage.exact.fall", "Fall");
        add("tooltip.hbm.damage.exact.laser", "Laser");
        add("tooltip.hbm.damage.exact.onfire", "Afterburn");
        add("tooltip.hbm.damage.exact.acidplayer", "Acid");
        add("tooltip.hbm.damage.exact.taublast", "Tau blast");
        add("tooltip.hbm.damage.exact.revolverbullet", "Bullet");
        add("tooltip.hbm.damage.exact.chopperbullet", "Chopper bullet");
        add("tooltip.hbm.damage.exact.cmb", "Combine ball");
        add("tooltip.hbm.damage.exact.nuclearblast", "Nuclear blast");
        add("tooltip.hbm.damage.exact.mudpoisoning", "Mud poisoning");
        add("block.hbm.machine_press", "Burner Press");
        add("subtitles.hbm.block.press_operate", "Burner Press operates");
        add("subtitles.hbm.block.assembler_operate", "Assembly machine operates");
        add("subtitles.hbm.block.assembler_strike", "Assembly machine strikes");
        add("subtitles.hbm.block.assembler_start", "Assembly machine starts");
        add("subtitles.hbm.block.assembler_stop", "Assembly machine stops");
        add("subtitles.hbm.block.assembler_cut", "Assembly machine cuts");
        add("subtitles.hbm.block.chemplant_operate", "Chemical plant operates");
        add("subtitles.hbm.block.chemical_plant", "Chemical plant reacts");
        add("subtitles.hbm.block.debris", "Debris tumbles");
        add("subtitles.hbm.tool.geiger", "Geiger counter clicks");
        add("subtitles.hbm.tool.tech_boop", "Device beeps");
        add("subtitles.hbm.tool.tech_bleep", "Detonator bleeps");
        add("subtitles.hbm.tool.radaway", "RadAway injector hisses");
        add("subtitles.hbm.player.cough", "Player coughs");
        add("subtitles.hbm.entity.ufo_blast", "Energy discharge");
        add("subtitles.hbm.entity.chopper", "Chopper drones");
        add("subtitles.hbm.alarm.siren", "Siren blares");
        add("item.hbm.detonator", "Detonator");
        add("item.hbm.singularity", "Singularity");
        add("item.hbm.singularity_counter_resonant", "Contained Counter-Resonant Singularity");
        add("item.hbm.singularity_super_heated", "Superheated Resonating Singularity");
        add("item.hbm.singularity_spark", "Spark Singularity");
        add("item.hbm.black_hole", "Miniature Black Hole");
        add("item.hbm.particle_digamma", "The Digamma Particle");
        add("item.hbm.pellet_antimatter", "Antimatter Cluster");
        add("item.hbm.singularity.desc.1", "You may be asking:");
        add("item.hbm.singularity.desc.2", "\"How is this possible?\"");
        add("item.hbm.singularity.desc.3", "\"I have no idea!\"");
        add("item.hbm.singularity_counter_resonant.desc.1", "Nullifies resonance of objects in");
        add("item.hbm.singularity_counter_resonant.desc.2", "non-euclidean space, creating a");
        add("item.hbm.singularity_counter_resonant.desc.3", "variable gravity well.");
        add("item.hbm.singularity_super_heated.desc.1", "Continuously heats up matter by");
        add("item.hbm.singularity_super_heated.desc.2", "resonating every Planck second.");
        add("item.hbm.singularity_super_heated.desc.3", "Not edible.");
        add("item.hbm.singularity_spark.desc.1", "A violently unstable singularity");
        add("item.hbm.singularity_spark.desc.2", "that pulses and tears space open.");
        add("item.hbm.singularity_spark.desc.3", "Handle from very far away.");
        add("item.hbm.black_hole.desc.1", "Contains a regular singularity");
        add("item.hbm.black_hole.desc.2", "large enough to stay stable.");
        add("item.hbm.black_hole.desc.3", "It's not the end of the world.");
        add("item.hbm.particle_digamma.desc.half_particle", "Particle half-life: 1.67*10^21 years");
        add("item.hbm.particle_digamma.desc.half_player", "Player half-life: %s");
        add("item.hbm.particle_digamma.desc.digamma", "%s mDRX/s");
        add("item.hbm.pellet_antimatter.desc.1", "Very heavy antimatter cluster.");
        add("item.hbm.pellet_antimatter.desc.2", "Gets rid of black holes.");
        add("item.hbm.trait.drop", "[Drops when dropped]");
        add("tooltip.hbm.detonator.set", "Shift right-click to set position,");
        add("tooltip.hbm.detonator.trigger", "right-click to detonate!");
        add("tooltip.hbm.detonator.no_position", "No position set!");
        add("tooltip.hbm.detonator.linked", "Linked to %s, %s, %s");
        add("msg.hbm.detonator.position_set", "Position set!");
        add("msg.hbm.detonator.no_position", "No position set!");
        add("bomb.detonated", "Detonated successfully!");
        add("bomb.incompatible", "Device can not be triggered!");
        add("bomb.launched", "Launched successfully!");
        add("bomb.missingComponent", "Component missing!");
        add("bomb.nobomb", "Linked position incompatible or unloaded!");
        add("bomb.triggered", "Triggered successfully!");
        add("block.hbm.machine_difurnace_off", "Blast Furnace");
        add("block.hbm.machine_electric_furnace_off", "Electric Furnace");
        add("block.hbm.machine_boiler_off", "Old Boiler");
        add("block.hbm.machine_shredder", "Shredder");
        add("block.hbm.machine_turbine", "Steam Turbine");
        add("block.hbm.machine_industrial_turbine", "Industrial Steam Turbine");
        add("block.hbm.decon", "Decontaminator");
        add("block.hbm.red_cable", "Red Copper Cable");
        add("block.hbm.red_cable_gauge", "Power Gauge");
        add("block.hbm.fluid_duct_neo", "Fluid Duct");
        add("item.hbm.fluid_duct", "Fluid Duct:");
        add("block.hbm.fluid_duct_box", "Universal Fluid Duct (Boxduct)");
        add("block.hbm.fluid_duct_gauge", "Flow Gauge Pipe");
        add("block.hbm.fluid_duct_exhaust", "Exhaust Pipe");
        add("block.hbm.fluid_duct_paintable", "Paintable Fluid Duct");
        add("block.hbm.fluid_duct_paintable_block_exhaust", "Paintable Exhaust Pipe");
        add("block.hbm.pipe_anchor", "Pipe Anchor");
        add("block.hbm.fluid_valve", "Fluid Valve");
        add("block.hbm.fluid_switch", "Fluid Switch");
        add("block.hbm.fluid_counter_valve", "Fluid Counter Valve");
        add("block.hbm.fluid_pump", "Fluid Pump");
        add("block.hbm.conveyor", "Conveyor Belt");
        add("block.hbm.conveyor_express", "Express Conveyor Belt");
        add("block.hbm.conveyor_double", "Double-Lane Conveyor Belt");
        add("block.hbm.conveyor_triple", "Triple-Lane Conveyor Belt");
        add("block.hbm.conveyor_lift", "Conveyor Chain Lift");
        add("block.hbm.conveyor_chute", "Conveyor Chute");
        add("block.hbm.machine_battery", "Energy Storage Block (LEGACY)");
        add("block.hbm.machine_battery_socket", "Battery Socket");
        add("block.hbm.machine_assembly_machine", "Assembly Machine");
        add("block.hbm.machine_chemical_plant", "Chemical Plant");
        add("block.hbm.machine_liquefactor", "Industrial Liquefaction Machine");
        add("block.hbm.machine_chemical_factory", "Chemical Factory");
        add("block.hbm.machine_refinery", "Oil Refinery");
        add("block.hbm.machine_catalytic_cracker", "Catalytic Cracking Tower");
        add("block.hbm.machine_catalytic_reformer", "Catalytic Reformer");
        add("block.hbm.machine_vacuum_distill", "Vacuum Refinery");
        add("block.hbm.machine_fraction_tower", "Fractioning Tower");
        add("block.hbm.machine_hydrotreater", "Hydrotreater");
        add("block.hbm.machine_coker", "Coker Unit");
        add("block.hbm.machine_pyrooven", "Pyrolysis Oven");
        add("block.hbm.machine_solidifier", "Industrial Solidification Machine");
        add("block.hbm.machine_compressor", "Compressor");
        add("block.hbm.machine_bigasstank", "Big Industrial Tank");
        add("block.hbm.machine_fluidtank", "Fluid Tank");
        add("block.hbm.barrel_plastic", "Safe Barrel");
        add("block.hbm.barrel_corroded", "Corroded Barrel");
        add("block.hbm.barrel_iron", "Iron Barrel");
        add("block.hbm.barrel_steel", "Steel Barrel");
        add("block.hbm.barrel_tcalloy", "Technetium Steel Barrel");
        add("block.hbm.barrel_antimatter", "Magnetic Antimatter Container");
        add("block.hbm.machine_pumpjack", "Pumpjack");
        add("block.hbm.machine_well", "Oil Derrick");
        add("block.hbm.machine_fracking_tower", "Hydraulic Fracking Tower");
        add("block.hbm.machine_centrifuge", "Centrifuge");
        add("block.hbm.machine_gascent", "Gas Centrifuge");
        add("block.hbm.machine_ore_slopper", "Ore Slopper");
        add("block.hbm.machine_sawmill", "Stirling Sawmill");
        add("block.hbm.machine_crucible", "Crucible");
        add("block.hbm.machine_gasflare", "Gas Flare");
        add("block.hbm.machine_assembly_factory", "Assembly Factory");
        add("block.hbm.machine_purex", "PUREX");
        add("block.hbm.machine_silex", "Laser Isotope Separation Chamber (SILEX)");
        add("block.hbm.machine_exposure_chamber", "Exposure Chamber");
        add("block.hbm.machine_cyclotron", "Cyclotron");
        add("block.hbm.machine_arc_welder", "Arc Welder");
        add("block.hbm.machine_soldering_station", "Soldering Station");
        add("block.hbm.machine_mixer", "Industrial Mixer");
        add("block.hbm.machine_radiolysis", "Radiolysis Chamber");
        add("block.hbm.machine_radgen", "Radioisotope Generator");
        add("block.hbm.machine_rotary_furnace", "Rotary Furnace");
        add("block.hbm.machine_steam_engine", "Steam Engine");
        add("block.hbm.machine_solar_boiler", "Solar Boiler");
        add("block.hbm.machine_tower_small", "Small Cooling Tower");
        add("block.hbm.machine_tower_large", "Large Cooling Tower");
        add("block.hbm.machine_turbofan", "Turbofan");
        add("block.hbm.machine_turbinegas", "Gas Turbine");
        add("block.hbm.glass_boron", "Boron Glass");
        add("container.machineAssemblyMachine", "Assembly Machine");
        add("container.machineChemicalPlant", "Chemical Plant");
        add("container.machineLiquefactor", "Liquefactor");
        add("container.hbm.battery", "Energy Storage Block");
        add("container.batterySocket", "Battery Socket");
        add("container.hbm.battery.red_low", "Low redstone mode");
        add("container.hbm.battery.red_high", "High redstone mode");
        add("container.hbm.battery.mode.input", "Input");
        add("container.hbm.battery.mode.buffer", "Buffer");
        add("container.hbm.battery.mode.output", "Output");
        add("container.hbm.battery.mode.none", "Disabled");
        add("container.hbm.battery.priority", "Network priority");
        add("container.hbm.battery.priority.low", "Low");
        add("container.hbm.battery.priority.normal", "Normal");
        add("container.hbm.battery.priority.high", "High");
        add("container.hbm.battery.priority.recommended", "Recommended: Low");
        HbmFluidLangEntries.addEnglish(this::add);
        add("container.fluidtank", "Fluid Tank");
        add("container.barrel", "Fluid Barrel");
        add("container.bigAssTank", "Big Industrial Tank");
        add("container.gasFlare", "Gas Flare");
        add("container.fluidtank.mode", "Mode");
        add("container.fluidtank.mode.input", "Input");
        add("container.fluidtank.mode.buffer", "Buffer");
        add("container.fluidtank.mode.output", "Output");
        add("container.fluidtank.mode.none", "Disabled");
        add("container.fluidtank.damaged", "Damaged");
        add("container.fluidtank.burning", "Burning");
        add("barrel.tooltip.capacity.6000", "Capacity: 6,000mB");
        add("barrel.tooltip.capacity.8000", "Capacity: 8,000mB");
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
        add("barrel.tooltip.no_corrosive_properly", "Cannot store corrosive fluids properly");
        add("barrel.tooltip.no_high_corrosive_properly", "Cannot store highly corrosive fluids properly");
        add("barrel.tooltip.leaky", "Leaky");
        add("block.hbm.gas_meltdown", "Meltdown Gas");
        add("block.hbm.gas_monoxide", "Carbon Monoxide");
        add("block.hbm.gas_asbestos", "Asbestos Dust");
        add("block.hbm.gas_coal", "Coal Dust");
        add("block.hbm.chlorine_gas", "Chlorine Gas");
        add("death.attack.monoxide", "%1$s suffocated from carbon monoxide");
        add("death.attack.taint", "%1$s was consumed by taint");
        add("death.attack.electric", "%1$s was electrocuted");
        add("death.attack.shrapnel", "%1$s was shredded by shrapnel");
        add("death.attack.rubble", "%1$s was crushed by flying rubble");
        add("death.attack.blackhole", "%1$s was consumed by a black hole");
        add("block.hbm.rad_absorber", "Radiation Absorber");
        add("block.hbm.rad_absorber.1", "Radiation Absorber Red");
        add("block.hbm.rad_absorber.2", "Radiation Absorber Green");
        add("block.hbm.rad_absorber.3", "Radiation Absorber Pink");
        add("block.hbm.dummy_block", "Dummy Block");
        add("block.hbm.waste_earth", "Waste Earth");
        add("block.hbm.waste_mycelium", "Waste Mycelium");
        add("block.hbm.waste_leaves", "Waste Leaves");
        add("block.hbm.waste_log", "Waste Log");
        add("block.hbm.waste_planks", "Waste Planks");
        add("block.hbm.frozen_grass", "Frozen Grass");
        add("block.hbm.frozen_dirt", "Frozen Dirt");
        add("block.hbm.frozen_log", "Frozen Log");
        add("block.hbm.frozen_planks", "Frozen Planks");
        add("block.hbm.leaves_layer", "Fallen Leaves");
        add("block.hbm.balefire", "Balefire");
        add("block.hbm.sellafield", "Sellafite");
        add("block.hbm.sellafield.1", "Sellafite");
        add("block.hbm.sellafield.2", "Sellafite");
        add("block.hbm.sellafield.3", "Sellafite");
        add("block.hbm.sellafield.4", "Sellafite");
        add("block.hbm.sellafield.5", "Sellafite");
        add("block.hbm.sellafield_slaked", "Slaked Sellafite");
        add("block.hbm.sellafield_bedrock", "Bedrock Sellafite");
        add("block.hbm.ore_sellafield_diamond", "Sellafite Diamond Ore");
        add("block.hbm.ore_sellafield_emerald", "Sellafite Emerald Ore");
        add("block.hbm.ore_sellafield_radgem", "Sellafite Radioactive Gem Ore");
        add("block.hbm.ore_sellafield_schrabidium", "Sellafite Schrabidium Ore");
        add("block.hbm.ore_sellafield_uranium_scorched", "Scorched Sellafite Uranium Ore");
        add("block.hbm.waste_trinitite", "Trinitite Ore");
        add("block.hbm.waste_trinitite_red", "Red Trinitite Ore");
        add("block.hbm.glass_trinitite", "Trinity Glass");
        add("block.hbm.ash_digamma", "Ash");
        add("block.hbm.fire_digamma", "Lingering Digamma");
        add("block.hbm.pribris_digamma", "Blackened RBMK Debris");
        add("block.hbm.volcanic_lava_block", "Volcanic Lava");
        add("block.hbm.rad_lava_block", "Radioactive Volcanic Lava");
        add("block.hbm.mud_block", "Poisonous Mud");
        add("block.hbm.tektite", "Tektite");
        add("block.hbm.ore_tektite_osmiridium", "Osmiridium-Infused Tektite");
        add("block.hbm.crystal_virus", "Dark Crystal");
        add("block.hbm.crystal_hardened", "Hardened Dark Crystal");
        add("block.hbm.glyphid_spawner", "Glyphid Hive Spawner");
        add("block.hbm.nuke_gadget", "The Gadget");
        add("block.hbm.nuke_boy", "Little Boy");
        add("block.hbm.nuke_man", "Fat Man");
        add("block.hbm.nuke_tsar", "Tsar Bomba");
        add("block.hbm.nuke_mike", "Ivy Mike");
        add("block.hbm.nuke_prototype", "The Prototype");
        add("block.hbm.nuke_fleija", "F.L.E.I.J.A.");
        add("block.hbm.nuke_solinium", "The Blue Rinse");
        add("block.hbm.nuke_n2", "N2 Mine");
        add("block.hbm.nuke_custom", "Custom Nuke");
        add("block.hbm.nuke_fstbmb", "Balefire Bomb");
        add("block.hbm.bomb_multi", "Multi Purpose Bomb");
        add("container.nukeCustom", "Custom Nuke");
        add("item.hbm.custom_tnt", "Custom Nuke Explosive Charge");
        add("item.hbm.custom_nuke", "Custom Nuke Nuclear Rod");
        add("item.hbm.custom_hydro", "Custom Nuke Hydrogen Rod");
        add("item.hbm.custom_amat", "Custom Nuke Antimatter Rod");
        add("item.hbm.custom_dirty", "Custom Nuke Dirty Rod");
        add("item.hbm.custom_schrab", "Custom Nuke Schrabidium Rod");
        add("item.hbm.custom_fall", "Custom Nuke Drop Upgrade");
        add("item.hbm.custom_fall.desc", "Makes bomb drop upon activation");
        add("subtitles.hbm.weapon.fstbmb", "Balefire bomb signal");
        add("subtitles.hbm.weapon.nuclear_explosion", "Nuclear explosion");
        add("block.hbm.yellow_barrel", "Radioactive Barrel");
        add("block.hbm.vitrified_barrel", "Vitrified Nuclear Waste Drum");
        add("item.hbm.powder_tektite", "Tektite Powder");
        add("item.hbm.powder_coal", "Coal Powder");
        add("item.hbm.powder_coal_tiny", "Tiny Pile of Coal Powder");
        add("item.hbm.coke_coal", "Coal Coke");
        add("item.hbm.coke_lignite", "Lignite Coke");
        add("item.hbm.coke_petroleum", "Petroleum Coke");
        add("item.hbm.briquette_coal", "Coal Briquette");
        add("item.hbm.briquette_lignite", "Lignite Briquette");
        add("item.hbm.briquette_wood", "Sawdust Briquette");
        add("item.hbm.oil_tar_crude", "Oil Tar");
        add("item.hbm.oil_tar_crack", "Crack Oil Tar");
        add("item.hbm.oil_tar_coal", "Coal Tar");
        add("item.hbm.oil_tar_wood", "Wood Tar");
        add("item.hbm.oil_tar_wax", "Chlorinated Petroleum Wax");
        add("item.hbm.oil_tar_paraffin", "Paraffin Wax");
        add("item.hbm.powder_ash_wood", "Wood Ash");
        add("item.hbm.powder_ash_coal", "Coal Ash");
        add("item.hbm.powder_ash_misc", "Ash");
        add("item.hbm.powder_ash_fly", "Fly Ash");
        add("item.hbm.powder_ash_soot", "Fine Soot");
        add("item.hbm.powder_ash_fullerene", "Fullerene");
        add("item.hbm.chunk_ore_rare", "Rare Earth Ore Chunk");
        add("item.hbm.chunk_ore_malachite", "Malachite Chunk");
        add("item.hbm.chunk_ore_cryolite", "Cryolite Chunk");
        add("item.hbm.chunk_ore_moonstone", "Moonstone");
        add("item.hbm.plant_item_tobacco", "Tobacco");
        add("item.hbm.plant_item_rope", "Rope");
        add("item.hbm.plant_item_mustardwillow", "Mustard Willow Leaf");
        add("item.hbm.parts_legendary_tier1", "Legendary Parts");
        add("item.hbm.parts_legendary_tier2", "Legendary Parts");
        add("item.hbm.parts_legendary_tier3", "Legendary Parts");
        add("item.hbm.part_generic_piston_pneumatic", "Pneumatic Piston");
        add("item.hbm.part_generic_piston_hydraulic", "Hydraulic Piston");
        add("item.hbm.part_generic_piston_electric", "Electric Piston");
        add("item.hbm.part_generic_lde", "Low-Density Element");
        add("item.hbm.part_generic_hde", "Heavy Duty Element");
        add("item.hbm.part_generic_glass_polarized", "Polarized Lens");
        add("item.hbm.item_expensive.desc", "Expensive mode item");
        add("item.hbm.item_expensive_steel_plating", "Bolted Steel Plating");
        add("item.hbm.item_expensive_heavy_frame", "Heavy Framework");
        add("item.hbm.item_expensive_circuit", "Extensive Circuit Board");
        add("item.hbm.item_expensive_lead_plating", "Radiation Resistant Plating");
        add("item.hbm.item_expensive_ferro_plating", "Reinforced Ferrouranium Panels");
        add("item.hbm.item_expensive_computer", "Mainframe");
        add("item.hbm.item_expensive_bronze_tubes", "Bronze Structural Elements");
        add("item.hbm.item_expensive_plastic", "Plastic Panels");
        add("item.hbm.item_expensive_gold_dust", "Ultra Fine Gold Dust");
        add("item.hbm.item_expensive_degenerate_matter", "Degenerate Matter");
        add("item.hbm.ore_byproduct_b_iron", "Crystalline Iron Fragment");
        add("item.hbm.ore_byproduct_b_copper", "Crystalline Copper Fragment");
        add("item.hbm.ore_byproduct_b_lithium", "Crystalline Lithium Fragment");
        add("item.hbm.ore_byproduct_b_silicon", "Crystalline Silicon Fragment");
        add("item.hbm.ore_byproduct_b_lead", "Crystalline Lead Fragment");
        add("item.hbm.ore_byproduct_b_titanium", "Crystalline Titanium Fragment");
        add("item.hbm.ore_byproduct_b_aluminium", "Crystalline Aluminium Fragment");
        add("item.hbm.ore_byproduct_b_sulfur", "Crystalline Sulfur Fragment");
        add("item.hbm.ore_byproduct_b_calcium", "Crystalline Calcium Fragment");
        add("item.hbm.ore_byproduct_b_bismuth", "Crystalline Bismuth Fragment");
        add("item.hbm.ore_byproduct_b_radium", "Crystalline Radium Fragment");
        add("item.hbm.ore_byproduct_b_technetium", "Crystalline Technetium Fragment");
        add("item.hbm.ore_byproduct_b_polonium", "Crystalline Polonium Fragment");
        add("item.hbm.ore_byproduct_b_uranium", "Crystalline Uranium Fragment");
        add("item.hbm.casing_small", "Small Gunmetal Casing");
        add("item.hbm.casing_large", "Large Gunmetal Casing");
        add("item.hbm.casing_small_steel", "Small Weapon Steel Casing");
        add("item.hbm.casing_large_steel", "Large Weapon Steel Casing");
        add("item.hbm.casing_shotshell", "Black Powder Shotshell Casing");
        add("item.hbm.casing_buckshot", "Plastic Shotshell Casing");
        add("item.hbm.casing_buckshot_advanced", "Advanced Shotshell Casing");
        add("item.hbm.ingot_weaponsteel", "Weapon Steel Ingot");
        add("item.hbm.plate_weaponsteel", "Weapon Steel Plate");
        add("item.hbm.ingot_dura_steel", "High-Speed Steel Ingot");
        add("item.hbm.plate_schrabidium", "Schrabidium Plate");
        add("item.hbm.plate_combine_steel", "CMB Steel Plate");
        add("item.hbm.plate_saturnite", "Saturnite Plate");
        add("item.hbm.fuel_additive_antiknock", "Tetraethyllead Antiknock Agent");
        add("item.hbm.fuel_additive_deicer", "Deicer");
        add("item.hbm.catalytic_converter", "Catalytic Converter");
        add("item.hbm.powder_lignite", "Lignite Powder");
        add("item.hbm.powder_quartz", "Quartz Powder");
        add("item.hbm.powder_lapis", "Lapis Lazuli Powder");
        add("item.hbm.powder_diamond", "Diamond Powder");
        add("item.hbm.powder_emerald", "Emerald Powder");
        add("item.hbm.powder_sawdust", "Sawdust");
        add("item.hbm.ball_resin", "Latex");
        add("item.hbm.powder_limestone", "Limestone Powder");
        add("item.hbm.circuit_vacuum_tube", "Vacuum Tube");
        add("item.hbm.circuit_capacitor", "Capacitor");
        add("item.hbm.circuit_capacitor_tantalium", "Tantalum Capacitor");
        add("item.hbm.circuit_pcb", "Printed Circuit Board");
        add("item.hbm.circuit_silicon", "Printed Silicon Wafer");
        add("item.hbm.circuit_chip", "Microchip");
        add("item.hbm.circuit_chip_bismoid", "Versatile Integrated Circuit");
        add("item.hbm.circuit_analog", "Analog Circuit Board");
        add("item.hbm.circuit_basic", "Integrated Circuit Board");
        add("item.hbm.circuit_advanced", "Military Grade Circuit Board");
        add("item.hbm.circuit_capacitor_board", "Capacitor Board");
        add("item.hbm.circuit_bismoid", "Versatile Circuit Board");
        add("item.hbm.circuit_controller_chassis", "Control Unit Casing");
        add("item.hbm.circuit_controller", "Control Unit");
        add("item.hbm.circuit_controller_advanced", "Advanced Control Unit");
        add("item.hbm.circuit_quantum", "Quantum Processing Unit");
        add("item.hbm.circuit_chip_quantum", "Solid State Quantum Processor");
        add("item.hbm.circuit_controller_quantum", "Quantum Computer");
        add("item.hbm.circuit_atomic_clock", "Atomic Clock");
        add("item.hbm.circuit_numitron", "Incandescent Seven Segment Display");
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
