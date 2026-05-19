package com.hbm.ntm.datagen;

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
        add("item.hbm.geiger_counter", "\u76d6\u9769\u8ba1\u6570\u5668");
        add("item.hbm.digamma_diagnostic", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u5c04\u81ea\u68c0\u5668");
        add("item.hbm.radaway", "\u6d88\u8f90\u5b81");
        add("item.hbm.radaway_strong", "\u5f3a\u6548\u6d88\u8f90\u5b81");
        add("item.hbm.radaway_flush", "\u51b2\u5237\u578b\u6d88\u8f90\u5b81");
        add("effect.hbm.radiation", "\u8f90\u5c04");
        add("effect.hbm.radaway", "\u6d88\u8f90\u5b81");
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
        add("block.hbm.machine_difurnace_off", "\u9ad8\u7089");
        add("block.hbm.machine_electric_furnace_off", "\u7535\u7089");
        add("block.hbm.machine_boiler_off", "\u9505\u7089");
        add("block.hbm.machine_shredder", "\u7c89\u788e\u673a");
        add("block.hbm.decon", "\u6d88\u6c61\u5668");
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
    }
}
