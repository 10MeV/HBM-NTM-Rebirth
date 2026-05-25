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
        add("item.hbm.stamp_iron_flat", "Flat Stamp (Iron)");
        add("item.hbm.stamp_iron_wire", "Wire Stamp (Iron)");
        add("item.hbm.stamp_iron_circuit", "Circuit Stamp (Iron)");
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
        add("block.hbm.machine_press", "Burner Press");
        add("subtitles.hbm.block.press_operate", "Burner Press operates");
        add("subtitles.hbm.tool.geiger", "Geiger counter clicks");
        add("subtitles.hbm.tool.tech_boop", "Device beeps");
        add("subtitles.hbm.tool.radaway", "RadAway injector hisses");
        add("subtitles.hbm.entity.ufo_blast", "Energy discharge");
        add("block.hbm.machine_difurnace_off", "Blast Furnace");
        add("block.hbm.machine_electric_furnace_off", "Electric Furnace");
        add("block.hbm.machine_boiler_off", "Old Boiler");
        add("block.hbm.machine_shredder", "Shredder");
        add("block.hbm.machine_turbine", "Steam Turbine");
        add("block.hbm.decon", "Decontaminator");
        add("block.hbm.red_cable", "Red Copper Cable");
        add("block.hbm.fluid_duct_neo", "Fluid Duct");
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
        add("block.hbm.machine_pumpjack", "Pumpjack");
        add("block.hbm.machine_centrifuge", "Centrifuge");
        add("block.hbm.machine_ore_slopper", "Ore Slopper");
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
        add("block.hbm.leaves_layer", "Fallen Leaves");
        add("block.hbm.balefire", "Balefire");
        add("block.hbm.sellafield", "Sellafite");
        add("block.hbm.sellafield.1", "Sellafite");
        add("block.hbm.sellafield.2", "Sellafite");
        add("block.hbm.sellafield.3", "Sellafite");
        add("block.hbm.sellafield.4", "Sellafite");
        add("block.hbm.sellafield.5", "Sellafite");
        add("block.hbm.sellafield_slaked", "Slaked Sellafite");
        add("block.hbm.ash_digamma", "Ash");
        add("block.hbm.fire_digamma", "Lingering Digamma");
        add("block.hbm.pribris_digamma", "Blackened RBMK Debris");
        add("block.hbm.volcanic_lava_block", "Volcanic Lava");
        add("block.hbm.rad_lava_block", "Radioactive Volcanic Lava");
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
        add("subtitles.hbm.weapon.fstbmb", "Balefire bomb signal");
        add("block.hbm.yellow_barrel", "Radioactive Barrel");
        add("block.hbm.vitrified_barrel", "Vitrified Nuclear Waste Drum");
        ModItems.EXTRA_PARTS_TAB_ITEMS.forEach(item -> addItem(item, title(item.getId().getPath())));
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitControlName(item.getId().getPath())) {
                addItem(item, title(item.getId().getPath()));
            }
        });
        ModItems.NUKE_TAB_ITEMS.forEach(item -> addItem(item, title(item.getId().getPath())));
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
                 "battery_sc.am241" -> true;
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
