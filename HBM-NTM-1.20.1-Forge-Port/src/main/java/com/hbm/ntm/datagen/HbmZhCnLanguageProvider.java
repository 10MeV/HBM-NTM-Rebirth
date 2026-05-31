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
        add("item.hbm.fluid_identifier_multi", "\u591a\u7c7b\u578b\u6d41\u4f53\u8bc6\u522b\u7801");
        add("item.hbm.fluid_identifier_multi.info", "\u6d41\u4f53\uff1a");
        add("item.hbm.fluid_identifier_multi.info2", "\u6b21\u8981\u7c7b\u578b\uff1a");
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
        add("effect.hbm.taint", "\u6c61\u79fd");
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
        add("tooltip.hbm.damage.set", "\u5957\u88c5\u4f24\u5bb3\u6297\u6027");
        add("tooltip.hbm.damage.item", "\u7269\u54c1\u4f24\u5bb3\u6297\u6027");
        add("tooltip.hbm.damage.line", "%s\uff1a%s/%s%%");
        add("tooltip.hbm.damage.other", "\u5176\u4ed6");
        add("tooltip.hbm.damage.category.EXPL", "\u7206\u70b8");
        add("tooltip.hbm.damage.category.FIRE", "\u706b\u7130");
        add("tooltip.hbm.damage.category.PHYS", "\u7269\u7406");
        add("tooltip.hbm.damage.category.EN", "\u80fd\u91cf");
        add("tooltip.hbm.damage.exact.drown", "\u6eba\u6c34");
        add("tooltip.hbm.damage.exact.fall", "\u6454\u843d");
        add("tooltip.hbm.damage.exact.laser", "\u6fc0\u5149");
        add("tooltip.hbm.damage.exact.onfire", "\u707c\u70e7");
        add("tooltip.hbm.damage.exact.acidplayer", "\u9178\u6db2");
        add("tooltip.hbm.damage.exact.taublast", "Tau\u7206\u70b8");
        add("tooltip.hbm.damage.exact.revolverbullet", "\u5b50\u5f39");
        add("tooltip.hbm.damage.exact.chopperbullet", "\u76f4\u5347\u673a\u5b50\u5f39");
        add("tooltip.hbm.damage.exact.cmb", "\u8054\u5408\u519b\u80fd\u91cf\u7403");
        add("tooltip.hbm.damage.exact.nuclearblast", "\u6838\u7206");
        add("tooltip.hbm.damage.exact.mudpoisoning", "\u6bd2\u6ce5\u4e2d\u6bd2");
        add("block.hbm.machine_press", "\u706b\u529b\u953b\u538b\u673a");
        add("subtitles.hbm.block.press_operate", "\u706b\u529b\u953b\u538b\u673a\u8fd0\u4f5c");
        add("subtitles.hbm.block.debris", "\u788e\u5757\u6eda\u843d");
        add("subtitles.hbm.tool.geiger", "\u76d6\u9769\u8ba1\u6570\u5668\u54d2\u54d2\u4f5c\u54cd");
        add("subtitles.hbm.tool.tech_boop", "\u8bbe\u5907\u63d0\u793a\u97f3");
        add("subtitles.hbm.tool.tech_bleep", "\u8d77\u7206\u5668\u63d0\u793a\u97f3");
        add("subtitles.hbm.tool.radaway", "\u6d88\u8f90\u5b81\u6ce8\u5c04");
        add("subtitles.hbm.entity.ufo_blast", "\u80fd\u91cf\u653e\u7535");
        add("item.hbm.detonator", "\u8d77\u7206\u5668");
        add("item.hbm.singularity", "\u5947\u70b9");
        add("item.hbm.singularity_counter_resonant", "\u53ef\u63a7\u53cd\u8c10\u632f\u5947\u70b9");
        add("item.hbm.singularity_super_heated", "\u8d85\u70ed\u5171\u632f\u5947\u70b9");
        add("item.hbm.singularity_spark", "Spark\u5947\u70b9");
        add("item.hbm.black_hole", "\u5fae\u578b\u9ed1\u6d1e");
        add("item.hbm.particle_digamma", "\u00a7c\u8fea\u4f3d\u9a6c\u7c92\u5b50\u00a7r");
        add("item.hbm.pellet_antimatter", "\u53cd\u7269\u8d28\u56e2");
        add("item.hbm.singularity.desc.1", "\u4f60\u53ef\u80fd\u4f1a\u95ee\uff1a");
        add("item.hbm.singularity.desc.2", "\u201c\u8fd9\u600e\u4e48\u53ef\u80fd\uff1f\u201d");
        add("item.hbm.singularity.desc.3", "\u201c\u6211\u4e5f\u4e0d\u77e5\u9053\uff01\u201d");
        add("item.hbm.singularity_counter_resonant.desc.1", "\u5728\u975e\u6b27\u7a7a\u95f4\u4e2d");
        add("item.hbm.singularity_counter_resonant.desc.2", "\u62b5\u6d88\u7269\u4f53\u5171\u632f\uff0c");
        add("item.hbm.singularity_counter_resonant.desc.3", "\u4ea7\u751f\u53ef\u53d8\u5f15\u529b\u4e95\u3002");
        add("item.hbm.singularity_super_heated.desc.1", "\u901a\u8fc7\u6bcf\u4e2a\u666e\u6717\u514b\u65f6\u95f4\u7684\u5171\u632f");
        add("item.hbm.singularity_super_heated.desc.2", "\u6301\u7eed\u52a0\u70ed\u7269\u8d28\u3002");
        add("item.hbm.singularity_super_heated.desc.3", "\u4e0d\u53ef\u98df\u7528\u3002");
        add("item.hbm.singularity_spark.desc.1", "\u4e00\u4e2a\u6781\u4e0d\u7a33\u5b9a\u7684\u5947\u70b9\uff0c");
        add("item.hbm.singularity_spark.desc.2", "\u4f1a\u8109\u51b2\u5e76\u6495\u88c2\u7a7a\u95f4\u3002");
        add("item.hbm.singularity_spark.desc.3", "\u8bf7\u5728\u8db3\u591f\u8fdc\u5904\u64cd\u4f5c\u3002");
        add("item.hbm.black_hole.desc.1", "\u4e2d\u5fc3\u5305\u542b\u4e00\u4e2a\u5e38\u89c4\u5947\u70b9\uff0c");
        add("item.hbm.black_hole.desc.2", "\u8db3\u4ee5\u7ef4\u6301\u7a33\u5b9a\u3002");
        add("item.hbm.black_hole.desc.3", "\u8fd9\u8fd8\u4e0d\u662f\u4e16\u754c\u672b\u65e5\u3002");
        add("item.hbm.particle_digamma.desc.half_particle", "\u7c92\u5b50\u534a\u8870\u671f\uff1a1.67*10^21 \u5e74");
        add("item.hbm.particle_digamma.desc.half_player", "\u73a9\u5bb6\u534a\u8870\u671f\uff1a%s");
        add("item.hbm.particle_digamma.desc.digamma", "%s mDRX/s");
        add("item.hbm.pellet_antimatter.desc.1", "\u975e\u5e38\u91cd\u7684\u53cd\u7269\u8d28\u56e2\u3002");
        add("item.hbm.pellet_antimatter.desc.2", "\u80fd\u6e05\u9664\u9ed1\u6d1e\u3002");
        add("item.hbm.trait.drop", "[\u6389\u843d\u89e6\u53d1]");
        add("tooltip.hbm.detonator.set", "\u6f5c\u884c\u53f3\u952e\u8bbe\u7f6e\u4f4d\u7f6e\uff0c");
        add("tooltip.hbm.detonator.trigger", "\u53f3\u952e\u8d77\u7206\uff01");
        add("tooltip.hbm.detonator.no_position", "\u672a\u8bbe\u7f6e\u4f4d\u7f6e\uff01");
        add("tooltip.hbm.detonator.linked", "\u5df2\u94fe\u63a5\u5230 %s, %s, %s");
        add("msg.hbm.detonator.position_set", "\u4f4d\u7f6e\u5df2\u8bbe\u7f6e\uff01");
        add("msg.hbm.detonator.no_position", "\u672a\u8bbe\u7f6e\u4f4d\u7f6e\uff01");
        add("bomb.detonated", "\u6210\u529f\u5f15\u7206\uff01");
        add("bomb.incompatible", "\u8bbe\u5907\u65e0\u6cd5\u89e6\u53d1\uff01");
        add("bomb.launched", "\u53d1\u5c04\u6210\u529f\uff01");
        add("bomb.missingComponent", "\u7ec4\u4ef6\u4e22\u5931\uff01");
        add("bomb.nobomb", "\u94fe\u63a5\u4f4d\u7f6e\u4e0d\u517c\u5bb9\u6216\u5df2\u65ad\u5f00\uff01");
        add("bomb.triggered", "\u89e6\u53d1\u6210\u529f\uff01");
        add("block.hbm.machine_difurnace_off", "\u9ad8\u7089");
        add("block.hbm.machine_electric_furnace_off", "\u7535\u7089");
        add("block.hbm.machine_boiler_off", "\u9505\u7089");
        add("block.hbm.machine_shredder", "\u7c89\u788e\u673a");
        add("block.hbm.machine_turbine", "\u84b8\u6c7d\u8f6e\u673a");
        add("block.hbm.machine_industrial_turbine", "\u5de5\u4e1a\u84b8\u6c7d\u8f6e\u673a");
        add("block.hbm.decon", "\u6d88\u6c61\u5668");
        add("block.hbm.red_cable", "\u7d2b\u94dc\u7535\u7ebf");
        add("block.hbm.fluid_duct_neo", "\u6d41\u4f53\u7ba1\u9053");
        add("block.hbm.fluid_duct_box", "\u901a\u7528\u6d41\u4f53\u7ba1\u9053\uff08\u65b9\u5f62\uff09");
        add("block.hbm.fluid_duct_gauge", "\u6d41\u91cf\u8ba1\u7ba1");
        add("block.hbm.fluid_duct_exhaust", "\u6392\u6c14\u7ba1");
        add("block.hbm.fluid_duct_paintable", "\u53ef\u6d82\u88c5\u6d41\u4f53\u7ba1\u9053");
        add("block.hbm.fluid_duct_paintable_block_exhaust", "\u53ef\u6d82\u88c5\u6392\u6c14\u7ba1");
        add("block.hbm.pipe_anchor", "\u7ba1\u951a");
        add("block.hbm.fluid_valve", "\u6d41\u4f53\u9600\u95e8");
        add("block.hbm.fluid_switch", "\u6d41\u4f53\u5f00\u5173");
        add("block.hbm.fluid_counter_valve", "\u6d41\u4f53\u8ba1\u6570\u9600");
        add("block.hbm.fluid_pump", "\u6d41\u4f53\u6cf5");
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
        HbmFluidLangEntries.addChinese(this::add);
        add("container.fluidtank", "\u6d41\u4f53\u50a8\u7f50");
        add("container.bigAssTank", "\u5927\u578b\u5de5\u4e1a\u50a8\u7f50");
        add("container.gasFlare", "\u6c14\u4f53\u706b\u70ac");
        add("container.fluidtank.mode", "\u6a21\u5f0f");
        add("container.fluidtank.mode.input", "\u8f93\u5165");
        add("container.fluidtank.mode.buffer", "\u7f13\u51b2");
        add("container.fluidtank.mode.output", "\u8f93\u51fa");
        add("container.fluidtank.mode.none", "\u505c\u7528");
        add("container.fluidtank.damaged", "\u635f\u574f");
        add("container.fluidtank.burning", "\u71c3\u70e7");
        add("block.hbm.gas_meltdown", "\u7194\u6bc1\u6c14\u4f53");
        add("block.hbm.gas_monoxide", "\u4e00\u6c27\u5316\u78b3");
        add("block.hbm.gas_asbestos", "\u77f3\u68c9\u7c89\u5c18");
        add("block.hbm.gas_coal", "\u7164\u5c18");
        add("block.hbm.chlorine_gas", "\u6c2f\u6c14");
        add("death.attack.monoxide", "%1$s\u6b7b\u4e8e\u4e00\u6c27\u5316\u78b3\u4e2d\u6bd2");
        add("death.attack.taint", "%1$s\u88ab\u6c61\u79fd\u541e\u566c");
        add("death.attack.electric", "%1$s\u88ab\u7535\u51fb\u81f4\u6b7b");
        add("death.attack.shrapnel", "%1$s\u88ab\u5f39\u7247\u6495\u788e");
        add("death.attack.rubble", "%1$s\u88ab\u98de\u6563\u74e6\u783e\u7838\u6b7b");
        add("death.attack.blackhole", "%1$s\u88ab\u9ed1\u6d1e\u541e\u566c");
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
        add("block.hbm.frozen_grass", "\u51b0\u51bb\u8349");
        add("block.hbm.frozen_dirt", "\u51b0\u51bb\u571f");
        add("block.hbm.frozen_log", "\u51b0\u51bb\u539f\u6728");
        add("block.hbm.frozen_planks", "\u51b0\u51bb\u6728\u677f");
        add("block.hbm.leaves_layer", "\u843d\u53f6");
        add("block.hbm.balefire", "\u91ce\u706b");
        add("block.hbm.sellafield", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.1", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.2", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.3", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.4", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield.5", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield_slaked", "\u6d88\u6c89\u7684\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm.sellafield_bedrock", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u57fa\u5ca9");
        add("block.hbm.ore_sellafield_diamond", "\u653e\u5c04\u6027\u7194\u878d\u94bb\u77f3\u77ff");
        add("block.hbm.ore_sellafield_emerald", "\u653e\u5c04\u6027\u7194\u878d\u7eff\u5b9d\u77f3\u77ff");
        add("block.hbm.ore_sellafield_radgem", "\u5bcc\u542b\u5b9d\u77f3\u7684\u653e\u5c04\u6027\u7194\u878d\u7269");
        add("block.hbm.ore_sellafield_schrabidium", "\u653e\u5c04\u6027\u7194\u878dSa326\u77ff\u77f3");
        add("block.hbm.ore_sellafield_uranium_scorched", "\u653e\u5c04\u6027\u7194\u878d\u70e7\u7126\u94c0\u77ff");
        add("block.hbm.waste_trinitite", "\u6838\u878d\u73bb\u7483\u77ff\u77f3");
        add("block.hbm.waste_trinitite_red", "\u7ea2\u8272\u6838\u878d\u73bb\u7483\u77ff\u77f3");
        add("block.hbm.glass_trinitite", "\u6838\u878d\u73bb\u7483");
        add("block.hbm.ash_digamma", "\u7070\u70ec");
        add("block.hbm.fire_digamma", "\u6325\u4e4b\u4e0d\u53bb\u7684\u8fea\u4f3d\u9a6c\u4e4b\u706b");
        add("block.hbm.pribris_digamma", "\u53d1\u9ed1\u7684RBMK\u53cd\u5e94\u5806\u6b8b\u9ab8");
        add("block.hbm.volcanic_lava_block", "\u706b\u5c71\u7194\u5ca9");
        add("block.hbm.rad_lava_block", "\u653e\u5c04\u6027\u706b\u5c71\u7194\u5ca9");
        add("block.hbm.mud_block", "\u6bd2\u6ce5");
        add("block.hbm.tektite", "\u7194\u878d\u77f3");
        add("block.hbm.ore_tektite_osmiridium", "\u6e17\u9507\u7194\u878d\u77f3");
        add("block.hbm.crystal_virus", "\u9ed1\u6c34\u6676");
        add("block.hbm.crystal_hardened", "\u786c\u5316\u9ed1\u6c34\u6676");
        add("block.hbm.glyphid_spawner", "\u5f02\u866b\u8702\u5de2\u7e41\u6b96\u65b9\u5757");
        add("block.hbm.nuke_gadget", "\u5c0f\u73a9\u610f");
        add("block.hbm.nuke_boy", "\u5c0f\u7537\u5b69");
        add("block.hbm.nuke_man", "\u80d6\u5b50");
        add("block.hbm.nuke_tsar", "\u6c99\u7687\u70b8\u5f39");
        add("block.hbm.nuke_mike", "\u5e38\u6625\u85e4\u8fc8\u514b");
        add("block.hbm.nuke_prototype", "\u539f\u578b");
        add("block.hbm.nuke_fleija", "F.L.E.I.J.A.");
        add("block.hbm.nuke_solinium", "\u851a\u84dd\u6d17\u793c");
        add("block.hbm.nuke_n2", "N2\u70b8\u5f39");
        add("block.hbm.nuke_custom", "\u81ea\u5b9a\u4e49\u6838\u5f39");
        add("block.hbm.nuke_fstbmb", "\u91ce\u706b\u70b8\u5f39");
        add("block.hbm.bomb_multi", "\u591a\u529f\u80fd\u70b8\u5f39");
        add("container.nukeCustom", "\u81ea\u5b9a\u4e49\u6838\u5f39");
        add("item.hbm.custom_tnt", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u70b8\u836f");
        add("item.hbm.custom_nuke", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u94c0\u68d2");
        add("item.hbm.custom_hydro", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u6c22\u68d2");
        add("item.hbm.custom_amat", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u53cd\u7269\u8d28\u68d2");
        add("item.hbm.custom_dirty", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u6838\u5e9f\u6599\u68d2");
        add("item.hbm.custom_schrab", "\u81ea\u5b9a\u4e49\u6838\u5f39-Sa326\u68d2");
        add("item.hbm.custom_fall", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u6389\u843d\u5347\u7ea7");
        add("item.hbm.custom_fall.desc", "\u4f7f\u70b8\u5f39\u5728\u6fc0\u6d3b\u65f6\u4e0b\u843d");
        add("subtitles.hbm.weapon.fstbmb", "\u91ce\u706b\u70b8\u5f39\u4fe1\u53f7");
        add("subtitles.hbm.weapon.nuclear_explosion", "\u6838\u7206");
        add("block.hbm.yellow_barrel", "\u6838\u5e9f\u6599\u6876");
        add("block.hbm.vitrified_barrel", "\u73bb\u7483\u5316\u6838\u5e9f\u6599\u6876");
        add("item.hbm.powder_tektite", "\u7194\u878d\u77f3\u7c89");
        add("item.hbm.circuit_vacuum_tube", "\u771f\u7a7a\u7ba1");
        add("item.hbm.circuit_capacitor", "\u7535\u5bb9\u5668");
        add("item.hbm.circuit_capacitor_tantalium", "\u94bd\u7535\u5bb9\u5668");
        add("item.hbm.circuit_pcb", "\u5370\u5237\u7535\u8def\u677f");
        add("item.hbm.circuit_silicon", "\u538b\u5370\u7845\u6676\u5706");
        add("item.hbm.circuit_chip", "\u5fae\u82af\u7247");
        add("item.hbm.circuit_chip_bismoid", "\u591a\u529f\u80fd\u96c6\u6210\u7535\u8def");
        add("item.hbm.circuit_analog", "\u6a21\u62df\u7535\u8def\u677f");
        add("item.hbm.circuit_basic", "\u96c6\u6210\u7535\u8def\u677f");
        add("item.hbm.circuit_advanced", "\u519b\u7528\u7ea7\u7535\u8def\u677f");
        add("item.hbm.circuit_capacitor_board", "\u7535\u5bb9\u677f");
        add("item.hbm.circuit_bismoid", "\u591a\u529f\u80fd\u7535\u8def\u677f");
        add("item.hbm.circuit_controller_chassis", "\u63a7\u5236\u5355\u5143\u5916\u58f3");
        add("item.hbm.circuit_controller", "\u63a7\u5236\u5355\u5143");
        add("item.hbm.circuit_controller_advanced", "\u9ad8\u7ea7\u63a7\u5236\u5355\u5143");
        add("item.hbm.circuit_quantum", "\u91cf\u5b50\u5904\u7406\u5355\u5143");
        add("item.hbm.circuit_chip_quantum", "\u56fa\u6001\u91cf\u5b50\u5904\u7406\u5668");
        add("item.hbm.circuit_controller_quantum", "\u91cf\u5b50\u8ba1\u7b97\u673a");
        add("item.hbm.circuit_atomic_clock", "\u539f\u5b50\u949f");
        add("item.hbm.circuit_numitron", "\u4e03\u6bb5\u5f0f\u767d\u70bd\u706f\u663e\u793a\u5668");
        ModItems.EXTRA_PARTS_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitPartName(item.getId().getPath())) {
                addItem(item, fallbackTitle(item.getId().getPath()));
            }
        });
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitControlName(item.getId().getPath())) {
                addItem(item, fallbackTitle(item.getId().getPath()));
            }
        });
        ModItems.NUKE_TAB_ITEMS.forEach(item -> {
            if (!hasExplicitNukeName(item.getId().getPath())) {
                addItem(item, fallbackTitle(item.getId().getPath()));
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
                 "pellet_antimatter" -> true;
            default -> false;
        };
    }

    private static boolean hasExplicitPartName(String id) {
        if (id.startsWith("circuit_")) {
            return true;
        }
        return switch (id) {
            case "powder_tektite" -> true;
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
