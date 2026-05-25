package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class HbmZhCnLanguageProvider extends LanguageProvider {
    public HbmZhCnLanguageProvider(PackOutput output, String modId, String locale) {
        super(output, modId, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.hbm.parts", "HBM \u96f6\u4ef6");
        add("itemGroup.hbm.machines", "HBM \u673a\u5668");
        add("itemGroup.hbm.consumables", "HBM \u6d88\u8017\u54c1");
        add("itemGroup.hbm.control", "HBM \u63a7\u5236");
        add("itemGroup.hbm.nukes", "HBM \u6838\u5f39");
        add("item.hbm.ingot_uranium", "\u94c0\u952d");
        add("item.hbm.ingot_u233", "\u94c0-233\u952d");
        add("item.hbm.ingot_u235", "\u94c0-235\u952d");
        add("item.hbm.ingot_u238", "\u94c0-238\u952d");
        add("item.hbm.ingot_plutonium", "\u94b8\u952d");
        add("item.hbm.ingot_pu238", "\u94b8-238\u952d");
        add("item.hbm.ingot_pu239", "\u94b8-239\u952d");
        add("item.hbm.ingot_pu240", "\u94b8-240\u952d");
        add("item.hbm.ingot_pu241", "\u94b8-241\u952d");
        add("item.hbm.ingot_neptunium", "\u954e\u952d");
        add("item.hbm.ingot_polonium", "\u948b\u952d");
        add("item.hbm.ingot_th232", "\u948d-232\u952d");
        add("item.hbm.ingot_titanium", "\u949b\u952d");
        add("item.hbm.ingot_tungsten", "\u94a8\u952d");
        add("item.hbm.ingot_copper", "\u5de5\u4e1a\u7ea7\u94dc\u952d");
        add("item.hbm.ingot_lead", "\u94c5\u952d");
        add("item.hbm.ingot_steel", "\u94a2\u952d");
        add("item.hbm.ingot_cobalt", "\u94b4\u952d");
        add("item.hbm.ingot_aluminium", "\u94dd\u952d");
        add("item.hbm.ingot_beryllium", "\u94cd\u952d");
        add("item.hbm.ingot_schrabidium", "Sa326\u952d");
        add("item.hbm.ingot_advanced_alloy", "\u9ad8\u7ea7\u5408\u91d1\u952d");
        add("item.hbm.plate_steel", "\u94a2\u677f");
        add("item.hbm.plate_iron", "\u94c1\u677f");
        add("item.hbm.plate_copper", "\u94dc\u677f");
        add("item.hbm.plate_lead", "\u94c5\u677f");
        add("item.hbm.plate_titanium", "\u949b\u677f");
        add("item.hbm.plate_aluminium", "\u94dd\u677f");
        add("item.hbm.powder_uranium", "\u94c0\u7c89");
        add("item.hbm.powder_plutonium", "\u94b8\u7c89");
        add("item.hbm.powder_thorium", "\u948d\u7c89");
        add("item.hbm.powder_titanium", "\u949b\u7c89");
        add("item.hbm.powder_tungsten", "\u94a8\u7c89");
        add("item.hbm.powder_copper", "\u94dc\u7c89");
        add("item.hbm.powder_iron", "\u94c1\u7c89");
        add("item.hbm.powder_steel", "\u94a2\u7c89");
        add("item.hbm.powder_lead", "\u94c5\u7c89");
        add("item.hbm.coil_copper", "\u7d2b\u94dc\u7ebf\u5708");
        add("item.hbm.coil_tungsten", "\u52a0\u70ed\u7ebf\u5708");
        add("item.hbm.coil_gold", "\u91d1\u7ebf\u5708");
        add("item.hbm.motor", "\u9a6c\u8fbe");
        add("item.hbm.stamp_iron_plate", "\u677f\u6750\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm.stamp_iron_flat", "\u5e73\u677f\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm.stamp_iron_wire", "\u7ebf\u6750\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm.stamp_iron_circuit", "\u7535\u8def\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm.geiger_counter", "\u76d6\u9769\u8ba1\u6570\u5668");
        add("item.hbm.digamma_diagnostic", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u5c04\u81ea\u68c0\u5668");
        add("item.hbm.radaway", "\u6d88\u8f90\u5b81");
        add("item.hbm.radaway_strong", "\u5f3a\u6548\u6d88\u8f90\u5b81");
        add("item.hbm.radaway_flush", "\u51b2\u5237\u578b\u6d88\u8f90\u5b81");
        add("item.hbm.radx", "\u9632\u8f90\u5c04\u836f");
        add("item.hbm.radx.desc", "\u57283\u5206\u949f\u5185\u589e\u52a00.2\uff0837%\uff09\u7684\u6297\u8f90\u5c04\u80fd\u529b");
        add("item.hbm.containment_box", "\u94c5\u886c\u76d2");
        add("item.hbm.plastic_bag", "\u5851\u6599\u888b");
        add("item.hbm.toolbox", "\u5de5\u5177\u7bb1");
        add("item.hbm.toolbox.desc.swap", "\u53f3\u952e\u5de5\u5177\u7bb1\u53ef\u5c06\u70ed\u952e\u680f\u5b58\u5165/\u53d6\u51fa\u5de5\u5177\u7bb1\u3002");
        add("item.hbm.toolbox.desc.open", "\u6f5c\u884c\u53f3\u952e\u6253\u5f00\u5de5\u5177\u7bb1\u3002");
        add("item.hbm.conveyor_wand", "\u8f93\u9001\u5e26");
        add("item.hbm.conveyor_wand.regular", "\u8f93\u9001\u5e26");
        add("item.hbm.conveyor_wand.express", "\u5feb\u901f\u8f93\u9001\u5e26");
        add("item.hbm.conveyor_wand.double", "\u53cc\u8f68\u9053\u8f93\u9001\u5e26");
        add("item.hbm.conveyor_wand.triple", "\u4e09\u8f68\u9053\u8f93\u9001\u5e26");
        add("item.hbm.conveyor_wand.desc", "\u53f3\u952e\u4e24\u70b9\u4ee5\u94fa\u8bbe\u8f93\u9001\u5e26\u8def\u7ebf");
        add("item.hbm.conveyor_wand.vertical.desc", "\u53ef\u653e\u7f6e\u5782\u76f4\u8f93\u9001\u5e26\u548c\u6ed1\u69fd\u6765\u7ad6\u76f4\u8fd0\u8f93\u7269\u54c1");
        add("item.hbm.conveyor_wand.selected", "\u5df2\u9009\u62e9\u7b2c\u4e00\u70b9");
        add("item.hbm.conveyor_wand.built", "\u8f93\u9001\u5e26\u5df2\u94fa\u8bbe");
        add("item.hbm.conveyor_wand.not_enough", "\u8f93\u9001\u5e26\u6570\u91cf\u4e0d\u8db3");
        add("item.hbm.conveyor_wand.obstructed", "\u8f93\u9001\u5e26\u8def\u7ebf\u88ab\u963b\u6321");
        add("item.hbm.canister_empty", "\u7a7a\u7f50");
        add("item.hbm.canister_full", "\u7f50\u88c5\u5bb9\u5668");
        add("item.hbm.canister_napalm", "\u51dd\u56fa\u6c7d\u6cb9\u7f50");
        add("item.hbm.gas_empty", "\u7a7a\u6c14\u74f6");
        add("item.hbm.gas_full", "\u6c14\u74f6");
        add("item.hbm.fluid_tank_empty", "\u7a7a\u6d41\u4f53\u69fd");
        add("item.hbm.fluid_tank_full", "\u6d41\u4f53\u69fd");
        add("item.hbm.fluid_tank_lead_empty", "\u7a7a\u94c5\u5185\u58f3\u69fd");
        add("item.hbm.fluid_tank_lead_full", "\u94c5\u5185\u58f3\u69fd");
        add("item.hbm.fluid_barrel_empty", "\u7a7a\u6d41\u4f53\u6876");
        add("item.hbm.fluid_barrel_full", "\u6d41\u4f53\u6876");
        add("item.hbm.fluid_barrel_infinite", "\u65e0\u9650\u6d41\u4f53\u6876");
        add("item.hbm.fluid_pack_empty", "\u7a7a\u6d41\u4f53\u5305");
        add("item.hbm.fluid_pack_full", "\u6d41\u4f53\u5305");
        add("item.hbm.disperser_canister_empty", "\u7a7a\u55b7\u6563\u7f50");
        add("item.hbm.disperser_canister", "\u55b7\u6563\u7f50");
        add("item.hbm.glyphid_gland_empty", "\u7a7a\u817a\u4f53");
        add("item.hbm.glyphid_gland", "\u817a\u4f53");
        add("item.hbm.inf_water", "\u65e0\u9650\u6c34");
        add("item.hbm.inf_water_mk2", "\u65e0\u9650\u6c34 Mk2");
        add("item.hbm.chlorine_pinwheel", "\u6c2f\u6c14\u98ce\u8f6e");
        add("item.hbm.battery_potato", "\u9a6c\u94c3\u85af\u7535\u6c60");
        add("item.hbm.battery_creative", "\u65e0\u9650\u7535\u6c60");
        add("item.hbm.battery_redstone", "\u7ea2\u77f3\u7535\u6c60");
        add("item.hbm.battery_lead", "\u94c5\u9178\u7535\u6c60");
        add("item.hbm.battery_lithium", "\u9502\u79bb\u5b50\u7535\u6c60");
        add("item.hbm.battery_sodium", "\u94a0\u94c1\u7535\u6c60");
        add("item.hbm.battery_schrabidium", "Sa326\u7535\u6c60");
        add("item.hbm.battery_quantum", "\u91cf\u5b50\u7535\u6c60");
        add("item.hbm.capacitor_copper", "\u94dc\u7535\u5bb9");
        add("item.hbm.capacitor_gold", "\u91d1\u7535\u5bb9");
        add("item.hbm.capacitor_niobium", "\u94cc\u7535\u5bb9");
        add("item.hbm.capacitor_tantalum", "\u94bd\u7535\u5bb9");
        add("item.hbm.capacitor_bismuth", "\u94cb\u7535\u5bb9");
        add("item.hbm.capacitor_spark", "Spark\u7535\u5bb9");
        add("item.hbm.battery_sc.empty", "\u7a7a\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.waste", "\u6838\u5e9f\u6599\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.ra226", "\u956d-226\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.tc99", "\u951d-99\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.co60", "\u94b4-60\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.pu238", "\u94b8-238\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.po210", "\u948b-210\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.au198", "\u91d1-198\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.pb209", "\u94c5-209\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm.battery_sc.am241", "\u9545-241\u81ea\u5145\u7535\u7535\u6c60");
        add("desc.item.battery.charge", "\u50a8\u80fd: %s / %sHE");
        add("desc.item.battery.chargePerc", "\u50a8\u80fd: %s%%");
        add("desc.item.battery.chargeRate", "\u5145\u7535\u6548\u7387: %sHE/\u523b");
        add("desc.item.battery.dischargeRate", "\u653e\u7535\u6548\u7387: %sHE/\u523b");
        add("desc.item.wasteCooling", "\u6b63\u5728\u51b7\u5374");
        add("effect.hbm.radiation", "\u8f90\u5c04");
        add("effect.hbm.radaway", "\u6d88\u8f90\u5b81");
        add("effect.hbm.radx", "\u8f90\u7279\u5b81");
        add("effect.hbm.mutation", "\u7a81\u53d8");
        add("effect.hbm.stability", "\u7a33\u5b9a");
        add("geiger.title", "\u76d6\u9769\u8ba1\u6570\u5668");
        add("geiger.chunkRad", "\u533a\u5757\u8f90\u5c04\uff1a%s RAD/s");
        add("geiger.envRad", "\u73af\u5883\u5242\u91cf\uff1a%s RAD/s");
        add("geiger.playerRad", "\u73a9\u5bb6\u5242\u91cf\uff1a%s RAD");
        add("geiger.playerRes", "\u8f90\u5c04\u6297\u6027\uff1a%s%%");
        add("digamma.title", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u5c04\u81ea\u68c0\u5668");
        add("digamma.playerDigamma", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u7167\u6c34\u5e73\uff1a%s DRX");
        add("digamma.playerHealth", "\u73a9\u5bb6\u6240\u53d7F-\u8fea\u4f3d\u9a6c\u8f90\u7167\u5f71\u54cd\uff1a%s%%");
        add("digamma.playerRes", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u9632\u62a4\u6c34\u5e73\uff1a%s");
        add("tooltip.hbm.radiation.single", "\u8f90\u5c04\uff1a%s RAD/s");
        add("tooltip.hbm.radiation.total", "\u6574\u7ec4\u603b\u8f90\u5c04\uff1a%s RAD/s");
        add("tooltip.hbm.radiation.resistance", "\u8f90\u5c04\u6297\u6027\uff1a%s\uff08\u963b\u6321 %s%%\uff09");
        add("tooltip.hbm.hazard.digamma", "Digamma\uff1a%s DRX");
        add("tooltip.hbm.hazard.hot", "\u70ed\u91cf\uff1a%s");
        add("tooltip.hbm.hazard.blinding", "\u81f4\u76f2\uff1a%s");
        add("tooltip.hbm.hazard.asbestos", "\u77f3\u68c9\uff1a%s");
        add("tooltip.hbm.hazard.coal", "\u7164\u5c18\uff1a%s");
        add("tooltip.hbm.hazard.hydroactive", "\u6c34\u6d3b\u6027\uff1a%s");
        add("tooltip.hbm.hazard.explosive", "\u7206\u70b8\u6027\uff1a%s");
        add("block.hbm.machine_press", "\u706b\u529b\u953b\u538b\u673a");
        add("subtitles.hbm.block.press_operate", "\u706b\u529b\u953b\u538b\u673a\u8fd0\u4f5c");
        add("subtitles.hbm.tool.geiger", "\u76d6\u9769\u8ba1\u6570\u5668\u54d2\u54d2\u4f5c\u54cd");
        add("subtitles.hbm.tool.tech_boop", "\u8bbe\u5907\u63d0\u793a\u97f3");
        add("subtitles.hbm.tool.radaway", "\u6d88\u8f90\u5b81\u6ce8\u5c04");
        add("subtitles.hbm.entity.ufo_blast", "\u80fd\u91cf\u653e\u7535");
        add("block.hbm.machine_difurnace_off", "\u9ad8\u7089");
        add("block.hbm.machine_electric_furnace_off", "\u7535\u7089");
        add("block.hbm.machine_boiler_off", "\u9505\u7089");
        add("block.hbm.machine_shredder", "\u7c89\u788e\u673a");
        add("block.hbm.decon", "\u6d88\u6c61\u5668");
        add("block.hbm.red_cable", "\u7d2b\u94dc\u7535\u7ebf");
        add("block.hbm.fluid_duct_neo", "\u6d41\u4f53\u7ba1\u9053");
        add("block.hbm.conveyor", "\u8f93\u9001\u5e26");
        add("block.hbm.conveyor_express", "\u5feb\u901f\u8f93\u9001\u5e26");
        add("block.hbm.conveyor_double", "\u53cc\u8f68\u9053\u8f93\u9001\u5e26");
        add("block.hbm.conveyor_triple", "\u4e09\u8f68\u9053\u8f93\u9001\u5e26");
        add("block.hbm.conveyor_lift", "\u5782\u76f4\u8f93\u9001\u5e26");
        add("block.hbm.conveyor_chute", "\u8f93\u9001\u5e26\u6ed1\u69fd");
        add("block.hbm.machine_battery", "\u84c4\u7535\u6c60\uff08\u9057\u7559\uff09");
        add("block.hbm.machine_battery_socket", "\u7535\u6c60\u63d2\u5ea7");
        add("block.hbm.machine_assembly_machine", "\u88c5\u914d\u673a");
        add("block.hbm.machine_chemical_plant", "\u5316\u5de5\u5382");
        add("block.hbm.machine_chemical_factory", "\u5316\u5b66\u5de5\u5382");
        add("block.hbm.machine_refinery", "\u77f3\u6cb9\u7cbe\u70bc\u5382");
        add("block.hbm.machine_catalytic_cracker", "\u50ac\u5316\u88c2\u5316\u5854");
        add("block.hbm.machine_catalytic_reformer", "\u50ac\u5316\u91cd\u6574\u5668");
        add("block.hbm.machine_vacuum_distill", "\u771f\u7a7a\u70bc\u6cb9\u5382");
        add("block.hbm.machine_fraction_tower", "\u5206\u998f\u5854");
        add("block.hbm.machine_hydrotreater", "\u52a0\u6c22\u88c5\u7f6e");
        add("block.hbm.machine_coker", "\u7126\u5316\u88c5\u7f6e");
        add("block.hbm.machine_pyrooven", "\u70ed\u89e3\u7089");
        add("block.hbm.machine_solidifier", "\u5de5\u4e1a\u56fa\u5316\u673a");
        add("block.hbm.machine_compressor", "\u538b\u7f29\u673a");
        add("block.hbm.machine_bigasstank", "\u5927\u578b\u5de5\u4e1a\u50a8\u7f50");
        add("block.hbm.machine_fluidtank", "\u6d41\u4f53\u50a8\u7f50");
        add("block.hbm.machine_pumpjack", "\u77f3\u6cb9\u94bb\u673a");
        add("block.hbm.machine_centrifuge", "\u79bb\u5fc3\u673a");
        add("block.hbm.machine_ore_slopper", "\u77ff\u77f3\u7ffb\u6599\u673a");
        add("block.hbm.machine_gasflare", "\u6c14\u4f53\u706b\u70ac");
        add("block.hbm.machine_assembly_factory", "\u5927\u578b\u88c5\u914d\u5382");
        add("block.hbm.machine_purex", "\u94b8\u94c0\u8fd8\u539f\u63d0\u53d6\u8bbe\u5907\uff08PUREX\uff09");
        add("block.hbm.machine_silex", "SILEX\u6fc0\u5149\u540c\u4f4d\u7d20\u5206\u79bb\u5ba4");
        add("block.hbm.machine_exposure_chamber", "\u8f90\u7167\u8231");
        add("block.hbm.machine_cyclotron", "\u56de\u65cb\u52a0\u901f\u5668");
        add("block.hbm.machine_arc_welder", "\u7535\u5f27\u710a\u673a");
        add("block.hbm.machine_soldering_station", "\u710a\u63a5\u53f0");
        add("block.hbm.machine_mixer", "\u5de5\u4e1a\u6405\u62cc\u673a");
        add("block.hbm.machine_radiolysis", "\u8f90\u5c04\u5206\u89e3\u5ba4");
        add("block.hbm.machine_radgen", "\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u53d1\u7535\u673a");
        add("block.hbm.machine_rotary_furnace", "\u56de\u8f6c\u7089");
        add("block.hbm.machine_steam_engine", "\u84b8\u6c7d\u673a");
        add("block.hbm.machine_solar_boiler", "\u592a\u9633\u80fd\u9505\u7089");
        add("block.hbm.machine_tower_small", "\u5c0f\u578b\u51b7\u5374\u5854");
        add("block.hbm.machine_tower_large", "\u5927\u578b\u51b7\u5374\u5854");
        add("block.hbm.machine_turbofan", "\u6da1\u6247\u53d1\u52a8\u673a");
        add("block.hbm.machine_turbinegas", "\u71c3\u6c14\u8f6e\u673a");
        add("block.hbm.glass_boron", "\u787c\u73bb\u7483");
        add("container.machineAssemblyMachine", "\u88c5\u914d\u673a");
        add("container.hbm.battery", "\u84c4\u7535\u6c60");
        add("container.batterySocket", "\u7535\u6c60\u63d2\u5ea7");
        add("container.hbm.battery.red_low", "\u4f4e\u7ea2\u77f3\u6a21\u5f0f");
        add("container.hbm.battery.red_high", "\u9ad8\u7ea2\u77f3\u6a21\u5f0f");
        add("container.hbm.battery.mode.input", "\u8f93\u5165");
        add("container.hbm.battery.mode.buffer", "\u7f13\u51b2");
        add("container.hbm.battery.mode.output", "\u8f93\u51fa");
        add("container.hbm.battery.mode.none", "\u505c\u7528");
        add("container.hbm.battery.priority", "\u7f51\u7edc\u4f18\u5148\u7ea7");
        add("container.hbm.battery.priority.low", "\u4f4e");
        add("container.hbm.battery.priority.normal", "\u666e\u901a");
        add("container.hbm.battery.priority.high", "\u9ad8");
        add("container.hbm.battery.priority.recommended", "\u63a8\u8350\uff1a\u4f4e");
        add("block.hbm.gas_meltdown", "\u7194\u6bc1\u6c14\u4f53");
        add("block.hbm.gas_monoxide", "\u4e00\u6c27\u5316\u78b3");
        add("block.hbm.gas_asbestos", "\u77f3\u68c9\u7c89\u5c18");
        add("block.hbm.gas_coal", "\u7164\u5c18");
        add("block.hbm.chlorine_gas", "\u6c2f\u6c14");
        add("death.attack.monoxide", "%1$s\u6b7b\u4e8e\u4e00\u6c27\u5316\u78b3\u4e2d\u6bd2");
        add("death.attack.electric", "%1$s\u88ab\u7535\u51fb\u81f4\u6b7b");
        add("death.attack.shrapnel", "%1$s\u88ab\u5f39\u7247\u6495\u788e");
        add("death.attack.rubble", "%1$s\u88ab\u98de\u6563\u74e6\u783e\u7838\u6b7b");
        add("block.hbm.rad_absorber", "\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm.rad_absorber.1", "\u7ea2\u8272\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm.rad_absorber.2", "\u7eff\u8272\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm.rad_absorber.3", "\u7c89\u8272\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm.dummy_block", "\u865a\u62df\u65b9\u5757");
        add("block.hbm.waste_earth", "\u5e9f\u571f");
        add("block.hbm.waste_mycelium", "\u5e9f\u571f\u83cc\u4e1d");
        add("block.hbm.waste_leaves", "\u5e9f\u571f\u6811\u53f6");
        add("block.hbm.waste_log", "\u5e9f\u571f\u539f\u6728");
        add("block.hbm.waste_planks", "\u5e9f\u571f\u6728\u677f");
        add("block.hbm.leaves_layer", "\u843d\u53f6");
        add("block.hbm.balefire", "\u91ce\u706b");
        add("block.hbm.sellafield", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.1", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.2", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.3", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.4", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.5", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield_slaked", "\u6d88\u6c89\u7684\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.ash_digamma", "\u7070\u70ec");
        add("block.hbm.fire_digamma", "\u6325\u4e4b\u4e0d\u53bb\u7684\u8fea\u4f3d\u9a6c\u4e4b\u706b");
        add("block.hbm.pribris_digamma", "\u53d1\u9ed1\u7684RBMK\u53cd\u5e94\u5806\u6b8b\u9ab8");
        add("block.hbm.volcanic_lava_block", "\u706b\u5c71\u7194\u5ca9");
        add("block.hbm.rad_lava_block", "\u653e\u5c04\u6027\u706b\u5c71\u7194\u5ca9");
        add("block.hbm.nuke_gadget", "\u5c0f\u73a9\u610f");
        add("block.hbm.nuke_boy", "\u5c0f\u7537\u5b69");
        add("block.hbm.nuke_man", "\u80d6\u5b50");
        add("block.hbm.nuke_tsar", "\u6c99\u7687\u70b8\u5f39");
        add("block.hbm.nuke_mike", "\u5e38\u6625\u85e4\u8fc8\u514b");
        add("block.hbm.nuke_prototype", "\u539f\u578b");
        add("block.hbm.nuke_fleija", "F.L.E.I.J.A.");
        add("block.hbm.nuke_solinium", "\u851a\u84dd\u6d17\u793c");
        add("block.hbm.nuke_n2", "N2\u70b8\u5f39");
        add("block.hbm.nuke_fstbmb", "\u91ce\u706b\u70b8\u5f39");
        add("block.hbm.bomb_multi", "\u591a\u529f\u80fd\u70b8\u5f39");
        add("subtitles.hbm.weapon.fstbmb", "\u91ce\u706b\u70b8\u5f39\u4fe1\u53f7");
        add("block.hbm.yellow_barrel", "\u6838\u5e9f\u6599\u6876");
        add("block.hbm.vitrified_barrel", "\u73bb\u7483\u5316\u6838\u5e9f\u6599\u6876");
        ModItems.EXTRA_PARTS_TAB_ITEMS.forEach(item -> addItem(item, fallbackTitle(item.getId().getPath())));
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitControlName(item.getId().getPath())) {
                addItem(item, fallbackTitle(item.getId().getPath()));
            }
        });
        ModItems.NUKE_TAB_ITEMS.forEach(item -> addItem(item, fallbackTitle(item.getId().getPath())));
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

    private static String fallbackTitle(String id) {
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
