package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class HbmZhCnLanguageProvider extends LanguageProvider {
    private final Set<String> addedKeys = new HashSet<>();

    public HbmZhCnLanguageProvider(PackOutput output, String modId, String locale) {
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
        add("itemGroup.hbm_ntm_rebirth.parts", "HBM \u96f6\u4ef6");
        add("itemGroup.hbm_ntm_rebirth.machines", "HBM \u673a\u5668");
        add("itemGroup.hbm_ntm_rebirth.consumables", "HBM \u6d88\u8017\u54c1");
        add("itemGroup.hbm_ntm_rebirth.control", "HBM \u63a7\u5236");
        add("itemGroup.hbm_ntm_rebirth.blocks", "HBM \u65b9\u5757");
        add("itemGroup.hbm_ntm_rebirth.nukes", "HBM \u6838\u5f39");
        add("itemGroup.hbm_ntm_rebirth.missiles", "HBM \u5bfc\u5f39\u4e0e\u536b\u661f");
        addAbilityTranslations();
        add("item.hbm_ntm_rebirth.ingot_uranium", "\u94c0\u952d");
        add("item.hbm_ntm_rebirth.ingot_u233", "\u94c0-233\u952d");
        add("item.hbm_ntm_rebirth.ingot_u235", "\u94c0-235\u952d");
        add("item.hbm_ntm_rebirth.ingot_u238", "\u94c0-238\u952d");
        add("item.hbm_ntm_rebirth.ingot_plutonium", "\u94b8\u952d");
        add("item.hbm_ntm_rebirth.ingot_pu238", "\u94b8-238\u952d");
        add("item.hbm_ntm_rebirth.ingot_pu239", "\u94b8-239\u952d");
        add("item.hbm_ntm_rebirth.ingot_pu240", "\u94b8-240\u952d");
        add("item.hbm_ntm_rebirth.ingot_pu241", "\u94b8-241\u952d");
        add("item.hbm_ntm_rebirth.ingot_neptunium", "\u954e\u952d");
        add("item.hbm_ntm_rebirth.ingot_polonium", "\u948b\u952d");
        add("item.hbm_ntm_rebirth.ingot_th232", "\u948d-232\u952d");
        add("item.hbm_ntm_rebirth.ingot_titanium", "\u949b\u952d");
        add("item.hbm_ntm_rebirth.ingot_tungsten", "\u94a8\u952d");
        add("item.hbm_ntm_rebirth.ingot_copper", "\u5de5\u4e1a\u7ea7\u94dc\u952d");
        add("item.hbm_ntm_rebirth.ingot_lead", "\u94c5\u952d");
        add("item.hbm_ntm_rebirth.ingot_steel", "\u94a2\u952d");
        add("item.hbm_ntm_rebirth.ingot_cobalt", "\u94b4\u952d");
        add("item.hbm_ntm_rebirth.ingot_aluminium", "\u94dd\u952d");
        add("item.hbm_ntm_rebirth.ingot_beryllium", "\u94cd\u952d");
        add("item.hbm_ntm_rebirth.ingot_schrabidium", "Sa326\u952d");
        add("item.hbm_ntm_rebirth.plate_steel", "\u94a2\u677f");
        add("item.hbm_ntm_rebirth.plate_iron", "\u94c1\u677f");
        add("item.hbm_ntm_rebirth.plate_copper", "\u94dc\u677f");
        add("item.hbm_ntm_rebirth.plate_lead", "\u94c5\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_steel", "\u94f8\u9020\u94a2\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_lead", "\u94f8\u9020\u94c5\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_copper", "\u94f8\u9020\u94dc\u677f");
        add("item.hbm_ntm_rebirth.plate_titanium", "\u949b\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_titanium", "\u94f8\u9020\u949b\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_aluminium", "\u94f8\u9020\u94dd\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_dura_steel", "\u94f8\u9020\u9ad8\u901f\u94a2\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_bismuth_bronze", "\u94f8\u9020\u94cb\u9752\u94dc\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_arsenic_bronze", "\u94f8\u9020\u7837\u9752\u94dc\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_combine_steel", "\u94f8\u9020CMB\u94a2\u677f");
        add("item.hbm_ntm_rebirth.plate_cast_ferrouranium", "\u94f8\u9020\u94c0\u94c1\u5408\u91d1\u677f");
        add("item.hbm_ntm_rebirth.plate_aluminium", "\u94dd\u677f");
        add("item.hbm_ntm_rebirth.powder_uranium", "\u94c0\u7c89");
        add("item.hbm_ntm_rebirth.powder_plutonium", "\u94b8\u7c89");
        add("item.hbm_ntm_rebirth.powder_thorium", "\u948d\u7c89");
        add("item.hbm_ntm_rebirth.powder_titanium", "\u949b\u7c89");
        add("item.hbm_ntm_rebirth.powder_tungsten", "\u94a8\u7c89");
        add("item.hbm_ntm_rebirth.powder_copper", "\u94dc\u7c89");
        add("item.hbm_ntm_rebirth.powder_iron", "\u94c1\u7c89");
        add("item.hbm_ntm_rebirth.powder_steel", "\u94a2\u7c89");
        add("item.hbm_ntm_rebirth.powder_lead", "\u94c5\u7c89");
        add("item.hbm_ntm_rebirth.coil_copper", "\u7d2b\u94dc\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.coil_tungsten", "\u52a0\u70ed\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.coil_gold", "\u91d1\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.motor", "\u9a6c\u8fbe");
        add("item.hbm_ntm_rebirth.upgrade_template", "\u673a\u5668\u5347\u7ea7\u6a21\u677f");
        add("item.hbm_ntm_rebirth.blueprints", "\u84dd\u56fe");
        add("item.hbm_ntm_rebirth.upgrade_speed_1", "\u901f\u5ea6\u5347\u7ea7 I");
        add("item.hbm_ntm_rebirth.upgrade_speed_2", "\u901f\u5ea6\u5347\u7ea7 II");
        add("item.hbm_ntm_rebirth.upgrade_speed_3", "\u901f\u5ea6\u5347\u7ea7 III");
        add("item.hbm_ntm_rebirth.upgrade_power_1", "\u8282\u80fd\u5347\u7ea7 I");
        add("item.hbm_ntm_rebirth.upgrade_power_2", "\u8282\u80fd\u5347\u7ea7 II");
        add("item.hbm_ntm_rebirth.upgrade_power_3", "\u8282\u80fd\u5347\u7ea7 III");
        add("item.hbm_ntm_rebirth.upgrade_overdrive_1", "\u8d85\u9891\u5347\u7ea7 I");
        add("item.hbm_ntm_rebirth.upgrade_overdrive_2", "\u8d85\u9891\u5347\u7ea7 II");
        add("item.hbm_ntm_rebirth.upgrade_overdrive_3", "\u8d85\u9891\u5347\u7ea7 III");
        add("item.hbm_ntm_rebirth.upgrade_screm", "\u91c7\u77ff\u6fc0\u5149\u5347\u7ea7-\u76ee \u529b \u79d1 \u5b66 \u5bb6");
        add("item.hbm_ntm_rebirth.template_folder", "\u673a\u5668\u6a21\u677f\u6587\u4ef6\u5939");
        add("item.hbm_ntm_rebirth.template_folder.desc", "\u673a\u5668\u6a21\u677f\uff1a\u7eb8\u5f20+\u67d3\u6599$\u953b\u6a21\uff1a\u7a7a\u767d\u953b\u6a21 $\u8b66\u62a5\u58f0\u8f68:\u7edd\u7f18\u4f53+\u94a2\u677f ");
        add("item.hbm_ntm_rebirth.stamp_iron_plate", "\u677f\u6750\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm_ntm_rebirth.stamp_iron_flat", "\u5e73\u677f\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm_ntm_rebirth.stamp_iron_wire", "\u7ebf\u6750\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm_ntm_rebirth.stamp_iron_circuit", "\u7535\u8def\u6a21\u5177\uff08\u94c1\uff09");
        add("item.hbm_ntm_rebirth.stamp_357", ".357\u9a6c\u683c\u5357\u953b\u6a21");
        add("item.hbm_ntm_rebirth.stamp_44", ".44\u9a6c\u683c\u5357\u953b\u6a21");
        add("item.hbm_ntm_rebirth.stamp_50", "\u5927\u53e3\u5f84\u5f39\u58f3\u953b\u6a21");
        add("item.hbm_ntm_rebirth.stamp_9", "\u5c0f\u53e3\u5f84\u5f39\u58f3\u953b\u6a21");
        add("item.hbm_ntm_rebirth.stamp_book_printing1", "\u4e66\u9875\u953b\u6a21\u4e4b\u4e00");
        add("item.hbm_ntm_rebirth.stamp_book_printing2", "\u4e66\u9875\u953b\u6a21\u4e4b\u4e8c");
        add("item.hbm_ntm_rebirth.stamp_book_printing3", "\u4e66\u9875\u953b\u6a21\u4e4b\u4e09");
        add("item.hbm_ntm_rebirth.stamp_book_printing4", "\u4e66\u9875\u953b\u6a21\u4e4b\u56db");
        add("item.hbm_ntm_rebirth.stamp_book_printing5", "\u4e66\u9875\u953b\u6a21\u4e4b\u4e94");
        add("item.hbm_ntm_rebirth.stamp_book_printing6", "\u4e66\u9875\u953b\u6a21\u4e4b\u516d");
        add("item.hbm_ntm_rebirth.stamp_book_printing7", "\u4e66\u9875\u953b\u6a21\u4e4b\u4e03");
        add("item.hbm_ntm_rebirth.stamp_book_printing8", "\u4e66\u9875\u953b\u6a21\u4e4b\u516b");
        add("item.hbm_ntm_rebirth.page_of_page1", "\u4e66\u9875\u4e4b\u4e00");
        add("item.hbm_ntm_rebirth.page_of_page2", "\u4e66\u9875\u4e4b\u4e8c");
        add("item.hbm_ntm_rebirth.page_of_page3", "\u4e66\u9875\u4e4b\u4e09");
        add("item.hbm_ntm_rebirth.page_of_page4", "\u4e66\u9875\u4e4b\u56db");
        add("item.hbm_ntm_rebirth.page_of_page5", "\u4e66\u9875\u4e4b\u4e94");
        add("item.hbm_ntm_rebirth.page_of_page6", "\u4e66\u9875\u4e4b\u516d");
        add("item.hbm_ntm_rebirth.page_of_page7", "\u4e66\u9875\u4e4b\u4e03");
        add("item.hbm_ntm_rebirth.page_of_page8", "\u4e66\u9875\u4e4b\u516b");
        add("item.hbm_ntm_rebirth.geiger_counter", "\u76d6\u9769\u8ba1\u6570\u5668");
        add("item.hbm_ntm_rebirth.dosimeter", "\u5242\u91cf\u8ba1");
        add("item.hbm_ntm_rebirth.digamma_diagnostic", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u5c04\u81ea\u68c0\u5668");
        add("item.hbm_ntm_rebirth.holotape_image_restored", "\u5168\u606f\u5361\u5e26");
        add("item.hbm_ntm_rebirth.holotape_damaged", "\u635f\u574f\u7684\u5168\u606f\u5361\u5e26");
        add("item.hbm_ntm_rebirth.pollution_detector", "\u6c61\u67d3\u68c0\u6d4b\u5668");
        add("item.hbm_ntm_rebirth.radaway", "\u6d88\u8f90\u5b81");
        add("item.hbm_ntm_rebirth.radaway_strong", "\u5f3a\u6548\u6d88\u8f90\u5b81");
        add("item.hbm_ntm_rebirth.radaway_flush", "\u51b2\u5237\u578b\u6d88\u8f90\u5b81");
        add("item.hbm_ntm_rebirth.radx", "\u9632\u8f90\u5c04\u836f");
        add("item.hbm_ntm_rebirth.radx.desc", "\u57283\u5206\u949f\u5185\u589e\u52a00.2\uff0837%\uff09\u7684\u6297\u8f90\u5c04\u80fd\u529b");
        add("item.hbm_ntm_rebirth.five_htp", "5-HTP");
        add("item.hbm_ntm_rebirth.xanax", "NAXA \u6297\u8fea\u4f3d\u9a6c\u836f\u7269");
        add("item.hbm_ntm_rebirth.xanax.desc", "\u79fb\u9664500mDRX\u8fea\u4f3d\u9a6c\u8f90\u5c04");
        add("item.hbm_ntm_rebirth.pill_iodine", "\u7898\u4e38");
        add("item.hbm_ntm_rebirth.pill_iodine.desc", "\u6d88\u9664\u8d1f\u9762buff");
        add("item.hbm_ntm_rebirth.siox", "SiOX\u6297\u764c\u836f\u7269");
        add("item.hbm_ntm_rebirth.siox.desc", "\u4f7f\u7528\u77f3\u68c9\u7684\u529b\u91cf\u9006\u8f6c\u95f4\u76ae\u7624\uff01");
        add("item.hbm_ntm_rebirth.pill_herbal", "\u8349\u836f\u818f");
        add("item.hbm_ntm_rebirth.pill_herbal.desc", "\u6709\u6548\u6cbb\u7597\u80ba\u90e8\u75be\u75c5\u548c\u8f7b\u5ea6\u8f90\u5c04\u4e2d\u6bd2$\u6709\u526f\u4f5c\u7528");
        add("item.hbm_ntm_rebirth.pill_herbal.desc.0", "\u6709\u6548\u6cbb\u7597\u80ba\u90e8\u75be\u75c5\u548c\u8f7b\u5ea6\u8f90\u5c04\u4e2d\u6bd2");
        add("item.hbm_ntm_rebirth.pill_herbal.desc.1", "\u6709\u526f\u4f5c\u7528");
        add("item.hbm_ntm_rebirth.fmn", "\u6c1f\u785d\u897f\u6cee\u7247");
        add("item.hbm_ntm_rebirth.fmn.desc", "\u79fb\u96642000mDRX\u4ee5\u4e0a\u7684\u6240\u6709\u8fea\u4f3d\u9a6c\u8f90\u5c04");
        add("item.hbm_ntm_rebirth.cap_nuka", "\u6838\u5b50\u53ef\u4e50\u74f6\u76d6");
        add("item.hbm_ntm_rebirth.cap_quantum", "\u6a31\u6843\u5473\u6838\u5b50\u53ef\u4e50\u74f6\u76d6");
        add("item.hbm_ntm_rebirth.cap_sparkle", "S~\u6838\u5b50\u53ef\u4e50\u74f6\u76d6");
        add("item.hbm_ntm_rebirth.cap_rad", "\u8f90\u5c04S~\u6838\u5b50\u53ef\u4e50\u74f6\u76d6");
        add("item.hbm_ntm_rebirth.cap_korl", "Korl\u74f6\u76d6");
        add("item.hbm_ntm_rebirth.cap_fritz", "\u5f17\u91cc\u8328\u74f6\u76d6");
        add("block.hbm_ntm_rebirth.block_cap_nuka", "\u6838\u5b50\u53ef\u4e50\u74f6\u76d6\u65b9\u5757");
        add("block.hbm_ntm_rebirth.block_cap_quantum", "\u6a31\u6843\u5473\u6838\u5b50\u53ef\u4e50\u74f6\u76d6\u65b9\u5757");
        add("block.hbm_ntm_rebirth.block_cap_sparkle", "\u6838\u5b50\u53ef\u4e50\u74f6\u76d6\u65b9\u5757");
        add("block.hbm_ntm_rebirth.block_cap_rad", "\u8f90\u5c04 S~\u6838\u5b50\u53ef\u4e50\u74f6\u76d6\u65b9\u5757");
        add("block.hbm_ntm_rebirth.block_cap_korl", "Korl\u74f6\u76d6\u65b9\u5757");
        add("block.hbm_ntm_rebirth.block_cap_fritz", "\u5f17\u91cc\u8328\u74f6\u76d6\u65b9\u5757");
        add("item.hbm_ntm_rebirth.chocolate", "\u201c\u6211\u662f-\u956d\u724c\u201d\u5de7\u514b\u529b");
        add("item.hbm_ntm_rebirth.chocolate.desc", "\u956d\u5de7\u514b\u529b\uff1f\u6211\u5f88\u786e\u5b9a\u8fd9\u662f\u51b0\u6bd2\u3002");
        add("item.hbm_ntm_rebirth.canteen_vodka", "\u4f0f\u7279\u52a0\u6c34\u58f6");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.cooldown", "\u51b7\u5374\uff1a3\u5206\u949f");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.nausea", "\u53cd\u80c3 I\uff0c\u6301\u7eed10\u79d2");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.strength", "\u529b\u91cf III\uff0c\u6301\u7eed30\u79d2");
        add("item.hbm_ntm_rebirth.canteen_vodka.desc.flavor", "\u95fb\u8d77\u6765\u50cf\u6d88\u6bd2\u5242\uff0c\u559d\u8d77\u6765\u4e5f\u50cf\u6d88\u6bd2\u5242\u3002");
        add("item.hbm_ntm_rebirth.glyphid_meat", "\u5f02\u866b\u8089");
        add("item.hbm_ntm_rebirth.glyphid_meat_grilled", "\u719f\u5f02\u866b\u8089");
        add("chem.helium3", "\u6c26-3\u63d0\u53d6");
        add("chem.meatprocessing", "\u8089\u7c7b\u5904\u7406");
        add("item.hbm_ntm_rebirth.gas_mask_filter", "\u9632\u6bd2\u9762\u5177\u8fc7\u6ee4\u5668");
        add("item.hbm_ntm_rebirth.gas_mask_filter_mono", "\u50ac\u5316\u6027\u9762\u7f69\u8fc7\u6ee4\u5668");
        add("item.hbm_ntm_rebirth.gas_mask_filter_combo", "\u9632\u6bd2\u9762\u5177\u7ec4\u5408\u5f0f\u8fc7\u6ee4\u5668");
        add("item.hbm_ntm_rebirth.gas_mask_filter_rag", "\u4e34\u65f6\u9632\u6bd2\u9762\u5177\u8fc7\u6ee4\u5668");
        add("item.hbm_ntm_rebirth.gas_mask_filter_piss", "\u5148\u8fdb\u7684\u7b80\u6613\u9632\u6bd2\u9762\u5177\u8fc7\u6ee4\u5668");
        add("item.hbm_ntm_rebirth.attachment_mask", "\u9644\u52a0\u578b\u9632\u6bd2\u9762\u5177");
        add("item.hbm_ntm_rebirth.attachment_mask_mono", "\u53ef\u63a5\u5165\u5f0f\u534a\u9762\u7f69\u9632\u6bd2\u9762\u5177");
        add("item.hbm_ntm_rebirth.goggles", "\u62a4\u76ee\u955c");
        add("item.hbm_ntm_rebirth.ashglasses", "\u9632\u7070\u70ec\u62a4\u76ee\u955c");
        add("item.hbm_ntm_rebirth.nossy_hat", "\u534e\u4e3d\u7684\u5e3d\u5b50");
        add("item.hbm_ntm_rebirth.no9", "\u77ff\u5de5\u5934\u76d4");
        add("item.hbm_ntm_rebirth.gas_mask", "\u9632\u6bd2\u9762\u5177");
        add("item.hbm_ntm_rebirth.gas_mask_m65", "M65-Z\u9632\u6bd2\u9762\u5177");
        add("item.hbm_ntm_rebirth.gas_mask_mono", "\u534a\u9762\u7f69\u9632\u6bd2\u9762\u5177");
        add("item.hbm_ntm_rebirth.gas_mask_olde", "\u76ae\u9769\u9632\u6bd2\u9762\u5177");
        add("item.hbm_ntm_rebirth.mask_rag", "\u7c97\u7cd9\u9632\u62a4\u9762\u7f69");
        add("item.hbm_ntm_rebirth.mask_piss", "\u6218\u58d5\u9762\u5177");
        add("item.hbm_ntm_rebirth.steel_helmet", "\u94a2\u5934\u76d4");
        add("item.hbm_ntm_rebirth.steel_plate", "\u94a2\u80f8\u7532");
        add("item.hbm_ntm_rebirth.steel_legs", "\u94a2\u62a4\u817f");
        add("item.hbm_ntm_rebirth.steel_boots", "\u94a2\u9774\u5b50");
        add("item.hbm_ntm_rebirth.titanium_helmet", "\u949b\u5934\u76d4");
        add("item.hbm_ntm_rebirth.titanium_plate", "\u949b\u80f8\u7532");
        add("item.hbm_ntm_rebirth.titanium_legs", "\u949b\u62a4\u817f");
        add("item.hbm_ntm_rebirth.titanium_boots", "\u949b\u9774\u5b50");
        add("item.hbm_ntm_rebirth.alloy_helmet", "\u9ad8\u7ea7\u5408\u91d1\u5934\u76d4");
        add("item.hbm_ntm_rebirth.alloy_plate", "\u9ad8\u7ea7\u5408\u91d1\u80f8\u7532");
        add("item.hbm_ntm_rebirth.alloy_legs", "\u9ad8\u7ea7\u5408\u91d1\u62a4\u817f");
        add("item.hbm_ntm_rebirth.alloy_boots", "\u9ad8\u7ea7\u5408\u91d1\u9774\u5b50");
        add("item.hbm_ntm_rebirth.cobalt_helmet", "\u94b4\u5934\u76d4");
        add("item.hbm_ntm_rebirth.cobalt_plate", "\u94b4\u80f8\u7532");
        add("item.hbm_ntm_rebirth.cobalt_legs", "\u94b4\u62a4\u817f");
        add("item.hbm_ntm_rebirth.cobalt_boots", "\u94b4\u9774\u5b50");
        add("item.hbm_ntm_rebirth.plate_paa", "PaA\u677f");
        add("item.hbm_ntm_rebirth.plate_euphemium", "Euphemium\u677f");
        add("item.hbm_ntm_rebirth.rag_damp", "\u6f6e\u6e7f\u7684\u7834\u5e03");
        add("item.hbm_ntm_rebirth.rag_piss", "\u6d78\u6e7f\u7684\u7834\u5e03");
        add("item.hbm_ntm_rebirth.rag", "\u7834\u5e03");
        add("item.hbm_ntm_rebirth.ingot_dineutronium", "\u53cc\u805a\u4e2d\u5b50\u6001\u7d20\u952d");
        add("item.hbm_ntm_rebirth.watch", "\u624b\u8868");
        add("item.hbm_ntm_rebirth.hazmat_cloth", "\u9632\u5316\u5e03\u6599");
        add("item.hbm_ntm_rebirth.hazmat_cloth_red", "\u9ad8\u7ea7\u9632\u5316\u5e03\u6599");
        add("item.hbm_ntm_rebirth.hazmat_cloth_grey", "\u94c5\u5f3a\u5316\u9632\u5316\u5e03\u6599");
        add("item.hbm_ntm_rebirth.hazmat_helmet", "\u9632\u5316\u5934\u76d4");
        add("item.hbm_ntm_rebirth.hazmat_plate", "\u9632\u5316\u80f8\u7532");
        add("item.hbm_ntm_rebirth.hazmat_legs", "\u9632\u5316\u62a4\u817f");
        add("item.hbm_ntm_rebirth.hazmat_boots", "\u9632\u5316\u9774\u5b50");
        add("item.hbm_ntm_rebirth.hazmat_helmet_red", "\u9ad8\u7ea7\u9632\u5316\u5934\u76d4");
        add("item.hbm_ntm_rebirth.hazmat_plate_red", "\u9ad8\u7ea7\u9632\u5316\u80f8\u7532");
        add("item.hbm_ntm_rebirth.hazmat_legs_red", "\u9ad8\u7ea7\u9632\u5316\u62a4\u817f");
        add("item.hbm_ntm_rebirth.hazmat_boots_red", "\u9ad8\u7ea7\u9632\u5316\u9774\u5b50");
        add("item.hbm_ntm_rebirth.hazmat_helmet_grey", "\u9ad8\u6027\u80fd\u9632\u5316\u5934\u76d4");
        add("item.hbm_ntm_rebirth.hazmat_plate_grey", "\u9ad8\u6027\u80fd\u9632\u5316\u80f8\u7532");
        add("item.hbm_ntm_rebirth.hazmat_legs_grey", "\u9ad8\u6027\u80fd\u9632\u5316\u62a4\u817f");
        add("item.hbm_ntm_rebirth.hazmat_boots_grey", "\u9ad8\u6027\u80fd\u9632\u5316\u9774\u5b50");
        add("item.hbm_ntm_rebirth.asbestos_helmet", "\u9632\u706b\u5934\u76d4");
        add("item.hbm_ntm_rebirth.asbestos_plate", "\u9632\u706b\u80f8\u7532");
        add("item.hbm_ntm_rebirth.asbestos_legs", "\u9632\u706b\u62a4\u817f");
        add("item.hbm_ntm_rebirth.asbestos_boots", "\u9632\u706b\u9774");
        add("item.hbm_ntm_rebirth.cmb_helmet", "CMB\u94a2\u5934\u76d4");
        add("item.hbm_ntm_rebirth.cmb_plate", "CMB\u94a2\u80f8\u7532");
        add("item.hbm_ntm_rebirth.cmb_legs", "CMB\u94a2\u62a4\u817f");
        add("item.hbm_ntm_rebirth.cmb_boots", "CMB\u94a2\u9774\u5b50");
        add("item.hbm_ntm_rebirth.paa_plate", "PaA\u52a0\u56fa\u62a4\u80f8");
        add("item.hbm_ntm_rebirth.paa_legs", "PaA\u52a0\u56fa\u62a4\u817f");
        add("item.hbm_ntm_rebirth.paa_boots", "PaA \u201c\u597d\u978b\u201d");
        add("item.hbm_ntm_rebirth.jackt", "\u65f6\u5c1a\u9632\u5f39\u5939\u514b");
        add("item.hbm_ntm_rebirth.jackt2", "\u65f6\u5c1a\u9632\u5f39\u5939\u514b2\uff1a\u4e1c\u4eac\u6f02\u79fb");
        add("item.hbm_ntm_rebirth.security_helmet", "\u9632\u62a4\u5934\u76d4");
        add("item.hbm_ntm_rebirth.security_plate", "\u9632\u62a4\u80f8\u7532");
        add("item.hbm_ntm_rebirth.security_legs", "\u9632\u62a4\u88e4");
        add("item.hbm_ntm_rebirth.security_boots", "\u9632\u62a4\u9774\u5b50");
        add("item.hbm_ntm_rebirth.starmetal_helmet", "\u661f\u8f89\u5934\u76d4");
        add("item.hbm_ntm_rebirth.starmetal_plate", "\u661f\u8f89\u80f8\u7532");
        add("item.hbm_ntm_rebirth.starmetal_legs", "\u661f\u8f89\u62a4\u817f");
        add("item.hbm_ntm_rebirth.starmetal_boots", "\u661f\u8f89\u9774\u5b50");
        add("item.hbm_ntm_rebirth.robes_helmet", "\u957f\u888d\u515c\u5e3d");
        add("item.hbm_ntm_rebirth.robes_plate", "\u957f\u888d");
        add("item.hbm_ntm_rebirth.robes_legs", "\u957f\u888d\u62a4\u817f");
        add("item.hbm_ntm_rebirth.robes_boots", "\u957f\u888d\u9774\u5b50");
        add("item.hbm_ntm_rebirth.zirconium_legs", "\u9506\u62a4\u817f");
        add("item.hbm_ntm_rebirth.dnt_helmet", "DNT\u5934\u76d4");
        add("item.hbm_ntm_rebirth.dnt_plate", "DNT\u80f8\u7532");
        add("item.hbm_ntm_rebirth.dnt_legs", "DNT\u62a4\u817f");
        add("item.hbm_ntm_rebirth.dnt_boots", "DNT\u9774\u5b50");
        add("item.hbm_ntm_rebirth.hazmat_paa_helmet", "PaA\u6218\u6597\u9632\u8f90\u5c04\u5957\u88c5 \u5934\u76d4");
        add("item.hbm_ntm_rebirth.hazmat_paa_plate", "PaA\u6218\u6597\u9632\u8f90\u5c04\u5957\u88c5 \u80f8\u7532");
        add("item.hbm_ntm_rebirth.hazmat_paa_legs", "PaA\u6218\u6597\u9632\u8f90\u5c04\u5957\u88c5 \u62a4\u817f");
        add("item.hbm_ntm_rebirth.hazmat_paa_boots", "PaA\u6218\u6597\u9632\u8f90\u5c04\u5957\u88c5  \u9774\u5b50");
        add("item.hbm_ntm_rebirth.liquidator_helmet", "\u6838\u5e9f\u571f\u6e05\u9053\u592b\u5957\u88c5\u5934\u76d4");
        add("item.hbm_ntm_rebirth.liquidator_plate", "\u6838\u5e9f\u571f\u6e05\u9053\u592b\u5957\u88c5\u80f8\u7532");
        add("item.hbm_ntm_rebirth.liquidator_legs", "\u6838\u5e9f\u571f\u6e05\u9053\u592b\u5957\u88c5\u62a4\u817f");
        add("item.hbm_ntm_rebirth.liquidator_boots", "\u6838\u5e9f\u571f\u6e05\u9053\u592b\u5957\u88c5\u9774\u5b50");
        add("item.hbm_ntm_rebirth.schrabidium_helmet", "Sa326\u5934\u76d4");
        add("item.hbm_ntm_rebirth.schrabidium_plate", "Sa326\u80f8\u7532");
        add("item.hbm_ntm_rebirth.schrabidium_legs", "Sa326\u62a4\u817f");
        add("item.hbm_ntm_rebirth.schrabidium_boots", "Sa326\u9774\u5b50");
        add("item.hbm_ntm_rebirth.euphemium_helmet", "Ep\u5934\u76d4");
        add("item.hbm_ntm_rebirth.euphemium_plate", "Ep\u80f8\u7532");
        add("item.hbm_ntm_rebirth.euphemium_legs", "Ep\u62a4\u817f");
        add("item.hbm_ntm_rebirth.euphemium_boots", "Ep\u9774\u5b50");
        add("item.hbm_ntm_rebirth.bismuth_helmet", "\u94cb\u5934\u76d4");
        add("item.hbm_ntm_rebirth.bismuth_plate", "\u94cb\u80f8\u7532");
        add("item.hbm_ntm_rebirth.bismuth_legs", "\u94cb\u62a4\u817f");
        add("item.hbm_ntm_rebirth.bismuth_boots", "\u94cb\u9774\u5b50");
        add("item.hbm_ntm_rebirth.t51_helmet", "T-51b\u52a8\u529b\u88c5\u7532 \u5934\u76d4");
        add("item.hbm_ntm_rebirth.t51_plate", "T-51b\u52a8\u529b\u88c5\u7532 \u80f8\u7532");
        add("item.hbm_ntm_rebirth.t51_legs", "T-51b\u52a8\u529b\u88c5\u7532 \u62a4\u817f");
        add("item.hbm_ntm_rebirth.t51_boots", "T-51b\u52a8\u529b\u88c5\u7532 \u9774\u5b50");
        add("item.hbm_ntm_rebirth.steamsuit_helmet", "\u84b8\u6c7d\u52a8\u529b\u547c\u5438\u5668\u5934\u76d4");
        add("item.hbm_ntm_rebirth.steamsuit_plate", "\u84b8\u6c7d\u52a8\u529b\u80f8\u7532");
        add("item.hbm_ntm_rebirth.steamsuit_legs", "\u84b8\u6c7d\u52a8\u529b\u62a4\u817f");
        add("item.hbm_ntm_rebirth.steamsuit_boots", "\u84b8\u6c7d\u52a8\u529b\u9774\u5b50");
        add("item.hbm_ntm_rebirth.dieselsuit_helmet", "\u67f4\u6cb9\u5957\u88c5\u5934\u76d4");
        add("item.hbm_ntm_rebirth.dieselsuit_plate", "\u67f4\u6cb9\u5957\u88c5\u80f8\u7532");
        add("item.hbm_ntm_rebirth.dieselsuit_legs", "\u67f4\u6cb9\u5957\u88c5\u62a4\u817f");
        add("item.hbm_ntm_rebirth.dieselsuit_boots", "\u67f4\u6cb9\u5957\u88c5\u9774\u5b50");
        add("item.hbm_ntm_rebirth.ajr_helmet", "AJR \u52a8\u529b\u88c5\u7532\u5934\u76d4");
        add("item.hbm_ntm_rebirth.ajr_plate", "AJR \u52a8\u529b\u88c5\u7532\u80f8\u7532");
        add("item.hbm_ntm_rebirth.ajr_legs", "AJR \u52a8\u529b\u88c5\u7532\u62a4\u817f");
        add("item.hbm_ntm_rebirth.ajr_boots", "AJR \u52a8\u529b\u88c5\u7532\u9774\u5b50");
        add("item.hbm_ntm_rebirth.ajro_helmet", "AJR \u52a8\u529b\u88c5\u7532\u5934\u76d4");
        add("item.hbm_ntm_rebirth.ajro_plate", "AJR \u52a8\u529b\u88c5\u7532\u80f8\u7532");
        add("item.hbm_ntm_rebirth.ajro_legs", "AJR \u52a8\u529b\u88c5\u7532\u62a4\u817f");
        add("item.hbm_ntm_rebirth.ajro_boots", "AJR\u52a8\u529b\u88c5\u7532\u9774\u5b50");
        add("item.hbm_ntm_rebirth.rpa_helmet", "\u52a8\u529b\u88c5\u7532 \u5934\u76d4");
        add("item.hbm_ntm_rebirth.rpa_plate", "\u52a8\u529b\u88c5\u7532 \u80f8\u7532");
        add("item.hbm_ntm_rebirth.rpa_legs", "\u52a8\u529b\u62a4\u7532 \u62a4\u817f");
        add("item.hbm_ntm_rebirth.rpa_boots", "\u52a8\u529b\u88c5\u7532 \u9774\u5b50");
        add("item.hbm_ntm_rebirth.ncrpa_helmet", "NCR\u6e38\u4fa0\u52a8\u529b\u88c5\u7532 \u5934\u76d4");
        add("item.hbm_ntm_rebirth.ncrpa_plate", "NCR\u6e38\u4fa0\u52a8\u529b\u88c5\u7532 \u80f8\u7532");
        add("item.hbm_ntm_rebirth.ncrpa_legs", "NCR\u6e38\u4fa0\u52a8\u529b\u88c5\u7532 \u62a4\u817f");
        add("item.hbm_ntm_rebirth.ncrpa_boots", "NCR\u6e38\u4fa0\u52a8\u529b\u88c5\u7532 \u9774\u5b50");
        add("item.hbm_ntm_rebirth.bj_helmet", "\u70ed\u4f20\u611f\u5668\u773c\u7f69");
        add("item.hbm_ntm_rebirth.bj_plate", "\u00a74\u6708\u795e\u00a7r\u80f8\u7532");
        add("item.hbm_ntm_rebirth.bj_plate_jetpack", "\u00a74\u6708\u795e\u00a7r\u80f8\u7532 (\u5e26\u7ffc)");
        add("item.hbm_ntm_rebirth.bj_legs", "\u00a74\u6708\u795e\u00a7r\u62a4\u817f");
        add("item.hbm_ntm_rebirth.bj_boots", "\u00a74\u6708\u795e\u00a7r\u9489\u9774");
        add("item.hbm_ntm_rebirth.envsuit_helmet", "M1TTY\u73af\u5883\u5957\u88c5\u5934\u76d4");
        add("item.hbm_ntm_rebirth.envsuit_plate", "M1TTY\u73af\u5883\u5957\u88c5\u80f8\u7532");
        add("item.hbm_ntm_rebirth.envsuit_legs", "M1TTY\u73af\u5883\u5957\u88c5\u62a4\u817f");
        add("item.hbm_ntm_rebirth.envsuit_boots", "M1TTY\u73af\u5883\u5957\u88c5\u9774\u5b50");
        add("item.hbm_ntm_rebirth.hev_helmet", "HEV Mk.IV \u5934\u76d4");
        add("item.hbm_ntm_rebirth.hev_plate", "HEV Mk.IV \u80f8\u7532");
        add("item.hbm_ntm_rebirth.hev_legs", "HEV Mk.IV \u62a4\u817f");
        add("item.hbm_ntm_rebirth.hev_boots", "HEV Mk.IV \u9774\u5b50");
        add("item.hbm_ntm_rebirth.fau_helmet", "Fau\u5934\u76d4");
        add("item.hbm_ntm_rebirth.fau_plate", "Fau\u80f8\u7532");
        add("item.hbm_ntm_rebirth.fau_legs", "Fau\u62a4\u817f");
        add("item.hbm_ntm_rebirth.fau_boots", "Fau\u9774\u5b50");
        add("item.hbm_ntm_rebirth.dns_helmet", "DNT\u53cc\u805a\u4e2d\u5b50\u6001\u7d20\u9ad8\u79d1\u6280\u5168\u73af\u5883\u4f5c\u6218\u670d\u5934\u76d4");
        add("item.hbm_ntm_rebirth.dns_plate", "DNT\u53cc\u805a\u4e2d\u5b50\u6001\u7d20\u9ad8\u79d1\u6280\u5168\u73af\u5883\u4f5c\u6218\u670d\u80f8\u7532");
        add("item.hbm_ntm_rebirth.dns_legs", "DNT\u53cc\u805a\u4e2d\u5b50\u6001\u7d20\u9ad8\u79d1\u6280\u5168\u73af\u5883\u4f5c\u6218\u670d\u62a4\u817f");
        add("item.hbm_ntm_rebirth.dns_boots", "DNT\u53cc\u805a\u4e2d\u5b50\u6001\u7d20\u9ad8\u79d1\u6280\u5168\u73af\u5883\u4f5c\u6218\u670d\u9774\u5b50");
        add("item.hbm_ntm_rebirth.taurun_helmet", "\"\u9676\u8dd1\"\u5934\u76d4");
        add("item.hbm_ntm_rebirth.taurun_plate", "\"\u9676\u8dd1\"\u80f8\u7532");
        add("item.hbm_ntm_rebirth.taurun_legs", "\"\u9676\u8dd1\"\u62a4\u817f");
        add("item.hbm_ntm_rebirth.taurun_boots", "\"\u9676\u8dd1\"\u9774\u5b50");
        add("item.hbm_ntm_rebirth.trenchmaster_helmet", "\u6218\u58d5\u738b\u8005\u7684\u5934\u76d4");
        add("item.hbm_ntm_rebirth.trenchmaster_plate", "\u6218\u58d5\u738b\u8005\u7684\u80f8\u7532");
        add("item.hbm_ntm_rebirth.trenchmaster_legs", "\u6218\u58d5\u738b\u8005\u7684\u62a4\u817f");
        add("item.hbm_ntm_rebirth.trenchmaster_boots", "\u6218\u58d5\u738b\u8005\u7684\u9774\u5b50");
        add("tooltip.hbm_ntm_rebirth.gasmask.no_filter", "\u672a\u5b89\u88c5\u8fc7\u6ee4\u5668\uff01");
        add("tooltip.hbm_ntm_rebirth.gasmask.installed_filter", "\u5df2\u5b89\u88c5\u8fc7\u6ee4\u5668\uff1a");
        add("tooltip.hbm_ntm_rebirth.armor.dt_2", "+2 DT");
        add("tooltip.hbm_ntm_rebirth.armor.dt_0_5", "+0.5 DT");
        add("tooltip.hbm_ntm_rebirth.no9.coal_breathing", "\u8ba9\u4f60\u547c\u5438\u7164\u5c18\uff0c\u9177\uff01");
        addArmorModTranslations();
        add("item.hbm_ntm_rebirth.pads_rubber", "\u6a61\u80f6\u57ab");
        add("item.hbm_ntm_rebirth.pads_slime", "\u7c98\u6db2\u57ab");
        add("item.hbm_ntm_rebirth.pads_static", "\u9759\u7535\u57ab");
        add("item.hbm_ntm_rebirth.cladding_paint", "\u94c5\u6d82\u6599");
        add("item.hbm_ntm_rebirth.cladding_rubber", "\u6a61\u80f6\u8986\u5c42");
        add("item.hbm_ntm_rebirth.cladding_lead", "\u94c5\u8986\u5c42");
        add("item.hbm_ntm_rebirth.cladding_desh", "Desh\u8986\u5c42");
        add("item.hbm_ntm_rebirth.cladding_ghiorsium", "Gh336\u8986\u5c42");
        add("item.hbm_ntm_rebirth.cladding_iron", "\u94c1\u8986\u5c42");
        add("item.hbm_ntm_rebirth.cladding_obsidian", "\u9ed1\u66dc\u77f3\u8986\u5c42");
        add("item.hbm_ntm_rebirth.insert_kevlar", "\u51ef\u592b\u62c9\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_sapi", "SAPI\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_esapi", "ESAPI\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_xsapi", "XSAPI\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_steel", "\u91cd\u578b\u94a2\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_du", "\u8d2b\u94c0\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_polonium", "\u948b\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_ghiorsium", "Gh\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_era", "\u53cd\u5e94\u88c5\u7532\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_yharonite", "\u72bd\u620e\u9f99\u7d20\u63d2\u677f");
        add("item.hbm_ntm_rebirth.insert_doxium", "\u63ba\u7837Doxium\u63d2\u677f");
        add("item.hbm_ntm_rebirth.servo_set", "\u4f3a\u670d\u88c5\u7f6e");
        add("item.hbm_ntm_rebirth.servo_set_desh", "Desh\u4f3a\u670d\u88c5\u7f6e");
        add("item.hbm_ntm_rebirth.heart_piece", "\u5fc3\u4e4b\u788e\u7247");
        add("item.hbm_ntm_rebirth.heart_container", "\u5fc3\u4e4b\u5bb9\u5668");
        add("item.hbm_ntm_rebirth.heart_booster", "\u5fc3\u4e4b\u589e\u5f3a\u5668");
        add("item.hbm_ntm_rebirth.heart_fab", "\u9ed1\u6697\u4e4b\u5fc3");
        add("item.hbm_ntm_rebirth.black_diamond", "\u9ed1\u8272\u94bb\u77f3");
        add("item.hbm_ntm_rebirth.wd40", "VT-40");
        add("item.hbm_ntm_rebirth.bottled_cloud", "\u74f6\u4e2d\u4e91");
        add("item.hbm_ntm_rebirth.jetpack_fly", "\u55b7\u6c14\u80cc\u5305");
        add("item.hbm_ntm_rebirth.jetpack_break", "\u7f13\u964d\u80cc\u5305");
        add("item.hbm_ntm_rebirth.jetpack_vector", "\u77e2\u91cf\u55b7\u6c14\u80cc\u5305");
        add("item.hbm_ntm_rebirth.jetpack_boost", "\u5f39\u8df3\u80cc\u5305");
        add("item.hbm_ntm_rebirth.wings_limp", "\u65e0\u529b\u4e4b\u7ffc");
        add("item.hbm_ntm_rebirth.wings_murk", "\u6697\u9ed1\u4e4b\u7ffc");
        add("item.hbm_ntm_rebirth.australium_iii", "MkIII\u5bff\u547d\u5ef6\u957f\u5242");
        add("item.hbm_ntm_rebirth.armor_polish", "\u4eae\u7532\u724c\u88c5\u7532\u629b\u5149\u5242");
        add("item.hbm_ntm_rebirth.bandaid", "\u90a6\u8fea\u521b\u53ef\u8d34");
        add("item.hbm_ntm_rebirth.serum", "\u8840\u6e05");
        add("item.hbm_ntm_rebirth.quartz_plutonium", "\u6df1\u6210\u77f3\u82f1");
        add("item.hbm_ntm_rebirth.morning_glory", "\u7275\u725b\u82b1");
        add("item.hbm_ntm_rebirth.lodestone", "\u78c1\u77f3");
        add("item.hbm_ntm_rebirth.horseshoe_magnet", "\u9a6c\u8e44\u5f62\u78c1\u94c1");
        add("item.hbm_ntm_rebirth.industrial_magnet", "\u5de5\u4e1a\u78c1\u94c1");
        add("item.hbm_ntm_rebirth.bathwater", "\u6709\u6bd2\u80a5\u7682\u6c34");
        add("item.hbm_ntm_rebirth.bathwater_mk2", "\u6709\u6bd2\u80a5\u7682\u6c34(\u9a6c\u9999\u5473)");
        add("item.hbm_ntm_rebirth.spider_milk", "\u4e00\u74f6\u8718\u86db\u5976");
        add("item.hbm_ntm_rebirth.ink", "\u6797 \u58a8\u6c34");
        add("item.hbm_ntm_rebirth.injector_5htp", "5-HTP\u81ea\u52a8\u836f\u7269\u6ce8\u5c04\u5668");
        add("item.hbm_ntm_rebirth.injector_knife", "8\u82f1\u5bf8\u5200\u7247\u81ea\u52a8\u836f\u7269\u6ce8\u5c04\u5668");
        add("item.hbm_ntm_rebirth.defuser_gold", "\u9ec4\u91d1\u526a\u7ebf\u94b3");
        add("item.hbm_ntm_rebirth.neutrino_lens", "\u4e2d\u5fae\u5b50\u900f\u955c");
        add("item.hbm_ntm_rebirth.night_vision", "\u591c\u89c6\u773c\u955c");
        add("item.hbm_ntm_rebirth.back_tesla", "\u80cc\u8d1f\u5f0f\u7279\u65af\u62c9\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.medal_liquidator", "\u6838\u5e9f\u571f\u6e05\u9053\u592b\u52cb\u7ae0");
        add("item.hbm_ntm_rebirth.ballistic_gauntlet", "\u51b2\u51fb\u62f3\u5957");
        add("item.hbm_ntm_rebirth.ammo_standard_stone", "\u5706\u77f3\u548c\u9ed1\u706b\u836f");
        add("item.hbm_ntm_rebirth.ammo_standard_stone_ap", "\u71e7\u77f3\u548c\u9ed1\u706b\u836f");
        add("item.hbm_ntm_rebirth.ammo_standard_stone_iron", "\u94c1\u7403\u548c\u9ed1\u706b\u836f");
        add("item.hbm_ntm_rebirth.ammo_standard_stone_shot", "\u9730\u5f39\u548c\u9ed1\u706b\u836f");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_bp", "12\u53f7\u9ed1\u706b\u836f\u9707\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_bp_magnum", "12\u53f7\u9ed1\u706b\u836f\u9a6c\u683c\u5357\u9707\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_bp_slug", "12\u53f7\u9ed1\u706b\u836f\u72ec\u5934\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12", "12\u53f7\u9e7f\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_slug", "12\u53f7\u72ec\u5934\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_flechette", "12\u53f7\u7bad\u5f62\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_magnum", "12\u53f7\u9a6c\u683c\u5357\u9e7f\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_explosive", "12\u53f7\u7206\u70b8\u72ec\u5934\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g12_phosphorus", "12\u53f7\u78f7\u70e7\u9e7f\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g10", "10\u53f7\u9e7f\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_shrapnel", "10\u53f7\u9730\u5f39(\u6613\u8df3\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_du", "10\u53f7\u8d2b\u94c0\u9e7f\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_slug", "10\u53f7\u72ec\u5934\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g10_explosive", "10\u53f7\u7206\u70b8\u9730\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_sp", ".22LR\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_fmj", ".22LR\u5b50\u5f39(\u5168\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_jhp", ".22LR\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p22_ap", ".22LR\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_sp", "9mm\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_fmj", "9mm\u5b50\u5f39(\u5168\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_jhp", "9mm\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_p9_ap", "9mm\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_sp", "5.56mm\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_fmj", "5.56mm\u5b50\u5f39(\u5168\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_jhp", "5.56mm\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r556_ap", "5.56mm\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_bp", ".44\u9a6c\u683c\u5357\u5b50\u5f39(\u9ed1\u706b\u836f\u88c5\u836f)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_sp", ".44\u9a6c\u683c\u5357\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_fmj", ".44\u9a6c\u683c\u5357\u5b50\u5f39(\u5168\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_jhp", ".44\u9a6c\u683c\u5357\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_ap", ".44\u9a6c\u683c\u5357\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m44_express", ".44\u9a6c\u683c\u5357\u5b50\u5f39(\u989d\u5916\u88c5\u836fFMJ)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_bp", ".357\u9a6c\u683c\u5357\u5b50\u5f39(\u9ed1\u706b\u836f\u88c5\u836f)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_sp", ".357\u9a6c\u683c\u5357\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_fmj", ".357\u9a6c\u683c\u5357\u5b50\u5f39(\u5168\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_jhp", ".357\u9a6c\u683c\u5357\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_ap", ".357\u9a6c\u683c\u5357\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_m357_express", ".357\u9a6c\u683c\u5357\u5b50\u5f39(\u989d\u5916\u88c5\u836fFMJ)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_sp", "7.62mm\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_fmj", "7.62mm\u5b50\u5f39(\u5168\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_jhp", "7.62mm\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_ap", "7.62mm\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_du", "7.62mm\u5b50\u5f39(\u8d2b\u94c0\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_r762_he", "7.62mm\u5b50\u5f39\uff08\u9ad8\u7206\u5f39\u836f\uff09");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_sp", ".50BMG\u5b50\u5f39(\u666e\u901a\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_fmj", ".50BMG\u5b50\u5f39(\u91d1\u5c5e\u88ab\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_jhp", ".50BMG\u5b50\u5f39(\u88ab\u7532\u7a7a\u5c16\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_ap", ".50BMG\u5b50\u5f39(\u7a7f\u7532\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_du", ".50BMG\u5b50\u5f39(\u8d2b\u94c0\u5f39)");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_he", ".50BMG\u5b50\u5f39 \uff08\u9ad8\u7206\u5f39\u836f\uff09");
        add("item.hbm_ntm_rebirth.ammo_standard_bmg50_sm", ".50BMG\u5b50\u5f39\uff08\u661f\u8f89\uff09");
        add("item.hbm_ntm_rebirth.ammo_standard_b75", ".75\u201c\u87ba\u6813\u201d");
        add("item.hbm_ntm_rebirth.ammo_standard_b75_inc", ".75\u201c\u87ba\u6813\u201d(\u71c3\u70e7)");
        add("item.hbm_ntm_rebirth.ammo_standard_b75_exp", ".75\u201c\u87ba\u6813\u201d(\u9ad8\u7206)");
        add("item.hbm_ntm_rebirth.ammo_standard_g26_flare", "26mm\u4fe1\u53f7\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g26_flare_supply", "26mm\u4fe1\u53f7\u5f39(\u652f\u63f4\u7a7a\u6295)");
        add("item.hbm_ntm_rebirth.ammo_standard_g26_flare_weapon", "26mm\u4fe1\u53f7\u5f39(\u6b66\u5668\u7a7a\u6295)");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_he", "40mm\u9ad8\u7206\u69b4\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_heat", "40mm\u805a\u80fd\u88c5\u836f\u69b4\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_demo", "40mm\u7834\u574f\u8005\u69b4\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_inc", "40mm\u71c3\u70e7\u69b4\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_g40_phosphorus", "40mm\u767d\u78f7\u69b4\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_rocket_he", "\u9ad8\u7206\u706b\u7bad\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_rocket_heat", "\u805a\u80fd\u88c5\u836f\u706b\u7bad\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_rocket_demo", "\u7834\u574f\u8005\u706b\u7bad\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_rocket_inc", "\u71c3\u70e7\u706b\u7bad\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_rocket_phosphorus", "\u767d\u78f7\u706b\u7bad\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_capacitor", "\u7535\u5bb9\u5668");
        add("item.hbm_ntm_rebirth.ammo_standard_capacitor_overcharge", "\u7535\u5bb9\u5668(\u8fc7\u8f7d)");
        add("item.hbm_ntm_rebirth.ammo_standard_capacitor_ir", "\u7535\u5bb9\u5668(\u4f4e\u6ce2\u957f)");
        add("item.hbm_ntm_rebirth.ammo_standard_coil_tungsten", "\u94a8\u5236\u7ebf\u5708\u67aa\u5b50\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_coil_ferrouranium", "\u94c0\u94c1\u5408\u91d1\u5236\u7ebf\u5708\u67aa\u5b50\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_flame_diesel", "\u67f4\u6cb9\u7f50");
        add("item.hbm_ntm_rebirth.ammo_standard_flame_gas", "\u5929\u7136\u6c14\u7f50");
        add("item.hbm_ntm_rebirth.ammo_standard_flame_napalm", "\u51dd\u56fa\u6c7d\u6cb9\u7f50");
        add("item.hbm_ntm_rebirth.ammo_standard_flame_balefire", "\u91ce\u706b\u71c3\u6599\u7f50");
        add("item.hbm_ntm_rebirth.ammo_standard_nuke_standard", "\u8ff7\u4f60\u6838\u5f39");
        add("item.hbm_ntm_rebirth.ammo_standard_nuke_demo", "\u8ff7\u4f60\u6838\u5f39(\u7834\u574f\u65b9\u5757)");
        add("item.hbm_ntm_rebirth.ammo_standard_nuke_high", "\u8ff7\u4f60\u6838\u5f39(\u9ad8\u88c5\u836f)");
        add("item.hbm_ntm_rebirth.ammo_standard_nuke_tots", "\u8ff7\u4f60\u6838\u5f39(\u5e7c\u4f53)");
        add("item.hbm_ntm_rebirth.ammo_standard_nuke_hive", "\u706b\u7bad\u5de2");
        add("item.hbm_ntm_rebirth.ammo_standard_nuke_balefire", "\u8ff7\u4f60\u91ce\u706b\u70b8\u5f39");
        add("item.hbm_ntm_rebirth.ammo_fireext_0", "\u706d\u706b\u5668 \u6c34\u7f50");
        add("item.hbm_ntm_rebirth.ammo_fireext_1", "\u706d\u706b\u5668 \u6ce1\u6cab\u7f50");
        add("item.hbm_ntm_rebirth.ammo_fireext_2", "\u706d\u706b\u5668 \u787c\u7802\u7f50");
        add("item.hbm_ntm_rebirth.ammo_secret_p35_800", ".35-800 V9");
        add("item.hbm_ntm_rebirth.ammo_secret_p35_800_bl", ".35-800 V9 (\u9ed1\u96f7\u9706)");
        add("item.hbm_ntm_rebirth.gun_pepperbox", "\u80e1\u6912\u74f6\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_maresleg", "\u9a6c\u817f\u67aa");
        add("item.hbm_ntm_rebirth.gun_maresleg_akimbo", "\u53cc\u6301\u6760\u6746\u5f0f\u9730\u5f39\u67aa");
        add("item.hbm_ntm_rebirth.gun_maresleg_broken", "\u635f\u574f\u7684\u9a6c\u817f\u67aa");
        add("item.hbm_ntm_rebirth.gun_liberator", "\u89e3\u653e\u8005");
        add("item.hbm_ntm_rebirth.gun_spas12", "SPAS-12");
        add("item.hbm_ntm_rebirth.gun_autoshotgun", "\u6495\u788e\u8005");
        add("item.hbm_ntm_rebirth.gun_autoshotgun_shredder", "\u7c89\u788e\u8005");
        add("item.hbm_ntm_rebirth.gun_autoshotgun_sexy", "\u6027\u611f\u5148\u751f");
        add("item.hbm_ntm_rebirth.gun_double_barrel", "\u65e7\u65e5\u7ecf\u5178");
        add("item.hbm_ntm_rebirth.gun_double_barrel_sacred_dragon", "\u5723\u9f99");
        add("item.hbm_ntm_rebirth.gun_autoshotgun_heretic", "\u5f02\u6559\u5f92");
        add("item.hbm_ntm_rebirth.gun_light_revolver", "\u4e2d\u6298\u5f0f\u8f6c\u8f6e\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_light_revolver_atlas", "Atlas");
        add("item.hbm_ntm_rebirth.gun_light_revolver_dani", "\u65e5\u4e0e\u591c");
        add("item.hbm_ntm_rebirth.gun_henry", "\u6760\u6746\u5f0f\u6b65\u67aa");
        add("item.hbm_ntm_rebirth.gun_henry_lincoln", "\u6797\u80af\u62c9\u6746\u67aa");
        add("item.hbm_ntm_rebirth.gun_heavy_revolver", "\u91cd\u578b\u8f6c\u8f6e\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_heavy_revolver_lilmac", "\u5c0f\u9ea6\u91d1\u5854");
        add("item.hbm_ntm_rebirth.gun_heavy_revolver_protege", "\u95e8\u5f92");
        add("item.hbm_ntm_rebirth.gun_hangman", "\u523d\u5b50\u624b");
        add("item.hbm_ntm_rebirth.gun_greasegun", "M3\u9ec4\u6cb9\u67aa");
        add("item.hbm_ntm_rebirth.gun_lag", "\u6ed1\u7a3d\u7684\u957f\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_uzi", "Uzi\u51b2\u950b\u67aa");
        add("item.hbm_ntm_rebirth.gun_uzi_akimbo", "\u53cc\u6301\u4e4c\u5179\u51b2\u950b\u67aa");
        add("item.hbm_ntm_rebirth.gun_am180", ".22\u53e3\u5f84\u51b2\u950b\u67aa");
        add("item.hbm_ntm_rebirth.gun_star_f", "\u6253\u9776\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_star_f_akimbo", "\u6253\u9776\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_g3", "\u7a81\u51fb\u6b65\u67aa");
        add("item.hbm_ntm_rebirth.gun_g3_zebra", "\u6591\u9a6c\u6b65\u67aa");
        add("item.hbm_ntm_rebirth.gun_stg77", "StG 77");
        add("item.hbm_ntm_rebirth.gun_carbine", "\u5361\u5bbe\u67aa");
        add("item.hbm_ntm_rebirth.gun_minigun", "CZ53\u673a\u67aa");
        add("item.hbm_ntm_rebirth.gun_minigun_lacunae", "\u7a7a\u767d");
        add("item.hbm_ntm_rebirth.gun_minigun_dual", "\u53cc\u8054\u8f6c\u7ba1\u673a\u67aa");
        add("item.hbm_ntm_rebirth.gun_mas36", "\u5357\u65b9\u4e4b\u661f");
        add("item.hbm_ntm_rebirth.gun_flaregun", "\u4fe1\u53f7\u67aa");
        add("item.hbm_ntm_rebirth.gun_congolake", "\u521a\u679c\u6e56");
        add("item.hbm_ntm_rebirth.gun_mk108", "Grenade Machinegun");
        add("item.hbm_ntm_rebirth.gun_amat", "\u53cd\u5668\u6750\u6b65\u67aa");
        add("item.hbm_ntm_rebirth.gun_amat_subtlety", "\u660e\u654f");
        add("item.hbm_ntm_rebirth.gun_amat_penance", "\u5fcf\u6094");
        add("item.hbm_ntm_rebirth.gun_m2", "\u52c3\u6717\u5b81M2HB\u578b\u91cd\u673a\u67aa");
        add("item.hbm_ntm_rebirth.gun_bolter", "\u7206\u5f39\u67aa");
        add("item.hbm_ntm_rebirth.gun_aberrator", "\u79bb\u6563\u8005");
        add("item.hbm_ntm_rebirth.gun_aberrator_eott", "\u66b4\u98ce\u4e4b\u773c");
        add("item.hbm_ntm_rebirth.gun_panzerschreck", "\u6218\u8f66\u5669\u68a6");
        add("item.hbm_ntm_rebirth.gun_stinger", "FIM-92\u6bd2\u523a\u5bfc\u5f39");
        add("item.hbm_ntm_rebirth.gun_quadro", "\u56db\u559c\u4e38\u5b50");
        add("item.hbm_ntm_rebirth.gun_missile_launcher", "\u5bfc\u5f39\u53d1\u5c04\u5668");
        add("item.hbm_ntm_rebirth.gun_laser_pistol", "\u6fc0\u5149\u624b\u67aa");
        add("item.hbm_ntm_rebirth.gun_laser_pistol_pew_pew", "\u6563\u5c04");
        add("item.hbm_ntm_rebirth.gun_laser_pistol_morning_glory", "\u7275\u725b\u82b1");
        add("item.hbm_ntm_rebirth.gun_lasrifle", "\u6fc0\u5149\u6b65\u67aa");
        add("item.hbm_ntm_rebirth.gun_coilgun", "\u7ebf\u5708\u67aa");
        add("item.hbm_ntm_rebirth.gun_flamer", "\u706b\u7130\u55b7\u5c04\u5668");
        add("item.hbm_ntm_rebirth.gun_flamer_topaz", "\u201c\u9ec4\u7389\u5148\u751f\u201d\u55b7\u5c04\u5668");
        add("item.hbm_ntm_rebirth.gun_flamer_daybreaker", "\u7834\u6653\u4e4b\u65f6");
        add("item.hbm_ntm_rebirth.gun_tesla_cannon", "\u7279\u65af\u62c9\u70ae");
        add("item.hbm_ntm_rebirth.gun_fatman", "M42\u6838\u5f39\u53d1\u5c04\u5668 \u201c\u80d6\u5b50\u201d");
        add("item.hbm_ntm_rebirth.gun_folly", "Digamma\u539f\u578b\u201c\u611a\u8822\u201d");
        add("item.hbm_ntm_rebirth.gun_fireext", "\u706d\u706b\u5668");
        add("tooltip.hbm_ntm_rebirth.sedna_gun.belt_ammo", "\u4ece\u7269\u54c1\u680f\u4f9b\u5f39");
        add("tooltip.hbm_ntm_rebirth.sedna_gun.infinite_ammo", "\u65e0\u9650\u80fd\u91cf");
        add("tooltip.hbm_ntm_rebirth.sedna_gun.ammo", "\u5df2\u88c5\u586b\uff1a%s/%s");
        add("tooltip.hbm_ntm_rebirth.sedna_gun.default_ammo", "\u9ed8\u8ba4\u5f39\u836f\uff1a%s x%s");
        add("item.hbm_ntm_rebirth.card_aos", "\u9ed1\u6843A");
        add("item.hbm_ntm_rebirth.card_qos", "\u9ed1\u6843\u7687\u540e");
        add("item.hbm_ntm_rebirth.protection_charm", "\u5b88\u62a4\u9879\u94fe");
        add("item.hbm_ntm_rebirth.meteor_charm", "\u9668\u77f3\u9879\u94fe");
        add("item.hbm_ntm_rebirth.gas_tester", "\u6c14\u4f53\u4f20\u611f\u5668");
        add("item.hbm_ntm_rebirth.armor_battery", "\u52a8\u529b\u88c5\u7532\u7535\u6c60\u7ec4");
        add("item.hbm_ntm_rebirth.armor_battery_mk2", "\u52a8\u529b\u88c5\u7532\u7535\u6c60\u7ec4 Mk2");
        add("item.hbm_ntm_rebirth.armor_battery_mk3", "\u52a8\u529b\u88c5\u7532\u7535\u6c60\u7ec4 Mk3");
        add("item.hbm_ntm_rebirth.scrumpy", "\u4e00\u74f6\u70c8\u6027\u82f9\u679c\u9152");
        add("item.hbm_ntm_rebirth.wild_p", "\u91ce\u751f\u73c0\u4f3d\u7d22\u65af\u5e72\u5a01\u58eb\u5fcc");
        add("item.hbm_ntm_rebirth.shackles", "\u67b7\u9501");
        add("subtitles.hbm_ntm_rebirth.tool.gasmask_screw", "\u5df2\u5b89\u88c5\u9632\u6bd2\u9762\u5177\u8fc7\u6ee4\u5668");
        add("subtitles.hbm_ntm_rebirth.tool.pin_break", "\u4fdd\u9669\u9500\u6298\u65ad");
        add("subtitles.hbm_ntm_rebirth.item.syringe", "\u6ce8\u5c04\u5668\u6fc0\u6d3b");
        add("subtitles.hbm_ntm_rebirth.item.battery", "\u88c5\u5165\u88c5\u7532\u7535\u6c60");
        add("subtitles.hbm_ntm_rebirth.item.boltgun", "\u94c6\u9489\u67aa\u5f00\u706b");
        add("subtitles.hbm_ntm_rebirth.item.gasmask_screw", "\u5df2\u5b89\u88c5\u9632\u6bd2\u9762\u5177\u8fc7\u6ee4\u5668");
        add("subtitles.hbm_ntm_rebirth.item.geiger", "\u76d6\u9769\u8ba1\u6570\u5668\u54d2\u54d2\u4f5c\u54cd");
        add("subtitles.hbm_ntm_rebirth.item.jetpack_tank", "\u88c5\u5165\u55b7\u6c14\u80cc\u5305\u6c14\u7f50");
        add("subtitles.hbm_ntm_rebirth.item.pin_break", "\u4fdd\u9669\u9500\u6298\u65ad");
        add("subtitles.hbm_ntm_rebirth.item.pin_unlock", "\u4fdd\u9669\u9500\u89e3\u9501");
        add("subtitles.hbm_ntm_rebirth.item.radaway", "\u6d88\u8f90\u5b81\u6ce8\u5c04");
        add("subtitles.hbm_ntm_rebirth.item.repair", "\u5de5\u5177\u4fee\u7406");
        add("subtitles.hbm_ntm_rebirth.item.spray", "\u55b7\u7f50\u55e4\u54cd");
        add("subtitles.hbm_ntm_rebirth.item.tech_bleep", "\u8bbe\u5907\u63d0\u793a\u97f3");
        add("subtitles.hbm_ntm_rebirth.item.tech_boop", "\u8bbe\u5907\u63d0\u793a\u97f3");
        add("subtitles.hbm_ntm_rebirth.item.unpack", "\u5305\u88f9\u6253\u5f00");
        add("subtitles.hbm_ntm_rebirth.item.upgrade_plug", "\u63d2\u5165\u5347\u7ea7\u4ef6");
        add("subtitles.hbm_ntm_rebirth.item.vice", "\u53f0\u94b3\u5939\u7d27");
        addSatelliteTranslations();
        add("itemGroup.hbm_ntm_rebirth.weapons", "NTM \u6b66\u5668");
        add("info.asbestos", "\u6211\u7684\u80ba\u5728\u707c\u70e7\u3002");
        add("info.coaldust", "\u8fd9\u91cc\u5f88\u96be\u547c\u5438\u3002");
        add("info.gasmask.no_filter", "\u4f60\u7684\u9762\u5177\u6ca1\u6709\u8fc7\u6ee4\u5668\uff01");
        add("tooltip.hbm_ntm_rebirth.protection.hold_shift", "\u6309\u4f4f <%s> \u663e\u793a\u9632\u62a4\u4fe1\u606f");
        add("item.hbm_ntm_rebirth.containment_box", "\u94c5\u886c\u76d2");
        add("item.hbm_ntm_rebirth.plastic_bag", "\u5851\u6599\u888b");
        add("item.hbm_ntm_rebirth.toolbox", "\u5de5\u5177\u7bb1");
        add("item.hbm_ntm_rebirth.toolbox.desc.swap", "\u53f3\u952e\u5de5\u5177\u7bb1\u53ef\u5c06\u70ed\u952e\u680f\u5b58\u5165/\u53d6\u51fa\u5de5\u5177\u7bb1\u3002");
        add("item.hbm_ntm_rebirth.toolbox.desc.open", "\u6f5c\u884c\u53f3\u952e\u6253\u5f00\u5de5\u5177\u7bb1\u3002");
        add("item.hbm_ntm_rebirth.screwdriver", "\u87ba\u4e1d\u5200");
        add("item.hbm_ntm_rebirth.hand_drill", "\u624b\u94bb");
        add("item.hbm_ntm_rebirth.defuser", "\u62c6\u5f39\u5668");
        add("item.hbm_ntm_rebirth.schrabidium_sword", "Sa326\u5251");
        add("item.hbm_ntm_rebirth.schrabidium_pickaxe", "Sa326\u9550");
        add("item.hbm_ntm_rebirth.schrabidium_axe", "Sa326\u65a7");
        add("item.hbm_ntm_rebirth.schrabidium_shovel", "Sa326\u94f2");
        add("item.hbm_ntm_rebirth.titanium_sword", "\u949b\u5251");
        add("item.hbm_ntm_rebirth.titanium_pickaxe", "\u949b\u9550");
        add("item.hbm_ntm_rebirth.titanium_axe", "\u949b\u65a7");
        add("item.hbm_ntm_rebirth.titanium_shovel", "\u949b\u94f2");
        add("item.hbm_ntm_rebirth.steel_sword", "\u94a2\u5251");
        add("item.hbm_ntm_rebirth.steel_pickaxe", "\u94a2\u9550");
        add("item.hbm_ntm_rebirth.steel_axe", "\u94a2\u65a7");
        add("item.hbm_ntm_rebirth.steel_shovel", "\u94a2\u94f2");
        add("item.hbm_ntm_rebirth.alloy_sword", "\u5408\u91d1\u5251");
        add("item.hbm_ntm_rebirth.alloy_pickaxe", "\u5408\u91d1\u9550");
        add("item.hbm_ntm_rebirth.alloy_axe", "\u5408\u91d1\u65a7");
        add("item.hbm_ntm_rebirth.alloy_shovel", "\u5408\u91d1\u94f2");
        add("item.hbm_ntm_rebirth.cmb_sword", "CMB\u94a2\u5251");
        add("item.hbm_ntm_rebirth.cmb_pickaxe", "CMB\u94a2\u9550");
        add("item.hbm_ntm_rebirth.cmb_axe", "CMB\u94a2\u65a7");
        add("item.hbm_ntm_rebirth.cmb_shovel", "CMB\u94a2\u94f2");
        add("item.hbm_ntm_rebirth.desh_sword", "Desh\u5251");
        add("item.hbm_ntm_rebirth.desh_pickaxe", "Desh\u9550");
        add("item.hbm_ntm_rebirth.desh_axe", "Desh\u65a7");
        add("item.hbm_ntm_rebirth.desh_shovel", "Desh\u94f2");
        add("item.hbm_ntm_rebirth.cobalt_sword", "\u94b4\u5251");
        add("item.hbm_ntm_rebirth.cobalt_pickaxe", "\u94b4\u9550");
        add("item.hbm_ntm_rebirth.cobalt_axe", "\u94b4\u65a7");
        add("item.hbm_ntm_rebirth.cobalt_shovel", "\u94b4\u94f2");
        add("item.hbm_ntm_rebirth.cobalt_decorated_sword", "\u88c5\u9970\u94b4\u5251");
        add("item.hbm_ntm_rebirth.cobalt_decorated_pickaxe", "\u88c5\u9970\u94b4\u9550");
        add("item.hbm_ntm_rebirth.cobalt_decorated_axe", "\u88c5\u9970\u94b4\u65a7");
        add("item.hbm_ntm_rebirth.cobalt_decorated_shovel", "\u88c5\u9970\u94b4\u94f2");
        add("item.hbm_ntm_rebirth.starmetal_sword", "\u661f\u91d1\u5251");
        add("item.hbm_ntm_rebirth.starmetal_pickaxe", "\u661f\u91d1\u9550");
        add("item.hbm_ntm_rebirth.starmetal_axe", "\u661f\u91d1\u65a7");
        add("item.hbm_ntm_rebirth.starmetal_shovel", "\u661f\u91d1\u94f2");
        add("item.hbm_ntm_rebirth.centri_stick", "\u79bb\u5fc3\u68d2");
        add("item.hbm_ntm_rebirth.smashing_hammer", "\u7c89\u788e\u9524");
        add("item.hbm_ntm_rebirth.elec_sword", "\u7535\u68cd");
        add("item.hbm_ntm_rebirth.elec_pickaxe", "\u51b2\u51fb\u94bb");
        add("item.hbm_ntm_rebirth.elec_axe", "\u7535\u952f");
        add("item.hbm_ntm_rebirth.elec_shovel", "\u87ba\u65cb\u94bb");
        add("item.hbm_ntm_rebirth.drax", "\u91c7\u6398\u94bb\uff08\u9057\u7559\uff09");
        add("item.hbm_ntm_rebirth.drax_mk2", "\u5f3a\u5316\u578b\u91c7\u6398\u94bb\uff08\u9057\u7559\uff09");
        add("item.hbm_ntm_rebirth.drax_mk3", "Sa326\u91c7\u6398\u94bb\uff08\u9057\u7559\uff09");
        add("item.hbm_ntm_rebirth.bismuth_pickaxe", "\u94cb\u9550");
        add("item.hbm_ntm_rebirth.bismuth_axe", "\u94cb\u65a7");
        add("item.hbm_ntm_rebirth.volcanic_pickaxe", "\u706b\u5c71\u9550");
        add("item.hbm_ntm_rebirth.volcanic_axe", "\u706b\u5c71\u65a7");
        add("item.hbm_ntm_rebirth.chlorophyte_pickaxe", "\u7eff\u85fb\u77f3\u9550");
        add("item.hbm_ntm_rebirth.chlorophyte_axe", "\u7eff\u85fb\u77f3\u65a7");
        add("item.hbm_ntm_rebirth.mese_pickaxe", "Mese\u9550");
        add("item.hbm_ntm_rebirth.mese_axe", "Mese\u65a7");
        add("item.hbm_ntm_rebirth.dnt_sword", "DNT\u5251");
        add("item.hbm_ntm_rebirth.dwarven_pickaxe", "\u77ee\u4eba\u9550");
        add("item.hbm_ntm_rebirth.mese_gavel", "Mese\u6728\u69cc");
        add("item.hbm_ntm_rebirth.chainsaw", "\u94fe\u952f");
        add("item.hbm_ntm_rebirth.settings_tool", "\u8bbe\u7f6e\u5de5\u5177");
        add("item.hbm_ntm_rebirth.settings_tool.desc1", "\u53ef\u590d\u5236\u673a\u5668\u7684\u8bbe\u7f6e\uff08\u8fc7\u6ee4\u5668\u3001\u6d41\u4f53 ID \u7b49\uff09");
        add("item.hbm_ntm_rebirth.settings_tool.desc2", "\u6f5c\u884c\u53f3\u952e\u590d\u5236\uff0c\u53f3\u952e\u7c98\u8d34");
        add("item.hbm_ntm_rebirth.settings_tool.desc3", "Ctrl \u70b9\u51fb\u7ba1\u9053\u53ef\u5c06\u8bbe\u7f6e\u7c98\u8d34\u5230\u591a\u6839\u7ba1\u9053");
        add("item.hbm_ntm_rebirth.settings_tool.none", "\u65e0");
        add("item.hbm_ntm_rebirth.settings_tool.unknown", "\u672a\u77e5");
        add("item.hbm_ntm_rebirth.settings_tool.copied", "\u5df2\u590d\u5236 %s \u7684\u8bbe\u7f6e");
        add("item.hbm_ntm_rebirth.settings_tool.copy_failed", "\u590d\u5236\u5931\u8d25");
        add("item.hbm_ntm_rebirth.settings_tool.pasted", "\u5df2\u7c98\u8d34\u8bbe\u7f6e");
        add("item.hbm_ntm_rebirth.settings_tool.paste_failed", "\u7c98\u8d34\u5931\u8d25");
        add("item.hbm_ntm_rebirth.conveyor_wand", "\u8f93\u9001\u5e26");
        add("item.hbm_ntm_rebirth.conveyor_wand.regular", "\u8f93\u9001\u5e26");
        add("item.hbm_ntm_rebirth.conveyor_wand.express", "\u5feb\u901f\u8f93\u9001\u5e26");
        add("item.hbm_ntm_rebirth.conveyor_wand.double", "\u53cc\u8f68\u9053\u8f93\u9001\u5e26");
        add("item.hbm_ntm_rebirth.conveyor_wand.triple", "\u4e09\u8f68\u9053\u8f93\u9001\u5e26");
        add("item.hbm_ntm_rebirth.conveyor_wand.desc", "\u53f3\u952e\u4e24\u70b9\u4ee5\u94fa\u8bbe\u8f93\u9001\u5e26\u8def\u7ebf");
        add("item.hbm_ntm_rebirth.conveyor_wand.vertical.desc", "\u53ef\u653e\u7f6e\u5782\u76f4\u8f93\u9001\u5e26\u548c\u6ed1\u69fd\u6765\u7ad6\u76f4\u8fd0\u8f93\u7269\u54c1");
        add("item.hbm_ntm_rebirth.conveyor_wand.selected", "\u5df2\u9009\u62e9\u7b2c\u4e00\u70b9");
        add("item.hbm_ntm_rebirth.conveyor_wand.built", "\u8f93\u9001\u5e26\u5df2\u94fa\u8bbe");
        add("item.hbm_ntm_rebirth.conveyor_wand.not_enough", "\u8f93\u9001\u5e26\u6570\u91cf\u4e0d\u8db3");
        add("item.hbm_ntm_rebirth.conveyor_wand.obstructed", "\u8f93\u9001\u5e26\u8def\u7ebf\u88ab\u963b\u6321");
        add("item.hbm_ntm_rebirth.canister_empty", "\u7a7a\u7f50");
        add("item.hbm_ntm_rebirth.canister_full", "\u7f50\u88c5\u5bb9\u5668");
        add("item.hbm_ntm_rebirth.canister_napalm", "\u51dd\u56fa\u6c7d\u6cb9\u7f50");
        add("item.hbm_ntm_rebirth.gas_empty", "\u7a7a\u6c14\u74f6");
        add("item.hbm_ntm_rebirth.gas_full", "\u6c14\u74f6");
        add("item.hbm_ntm_rebirth.fluid_tank_empty", "\u7a7a\u6d41\u4f53\u69fd");
        add("item.hbm_ntm_rebirth.fluid_tank_full", "\u6d41\u4f53\u69fd");
        add("item.hbm_ntm_rebirth.fluid_tank_lead_empty", "\u7a7a\u94c5\u5185\u58f3\u69fd");
        add("item.hbm_ntm_rebirth.fluid_tank_lead_full", "\u94c5\u5185\u58f3\u69fd");
        add("item.hbm_ntm_rebirth.fluid_barrel_empty", "\u7a7a\u6d41\u4f53\u6876");
        add("item.hbm_ntm_rebirth.fluid_barrel_full", "\u6d41\u4f53\u6876");
        add("item.hbm_ntm_rebirth.fluid_barrel_infinite", "\u65e0\u9650\u6d41\u4f53\u6876");
        add("item.hbm_ntm_rebirth.fluid_pack_empty", "\u7a7a\u6d41\u4f53\u5305");
        add("item.hbm_ntm_rebirth.fluid_pack_full", "\u6d41\u4f53\u5305");
        add("item.hbm_ntm_rebirth.fluid_icon", "\u6d41\u4f53");
        add("item.hbm_ntm_rebirth.disperser_canister_empty", "\u7a7a\u55b7\u6563\u7f50");
        add("item.hbm_ntm_rebirth.disperser_canister", "\u55b7\u6563\u7f50");
        add("item.hbm_ntm_rebirth.glyphid_gland_empty", "\u7a7a\u817a\u4f53");
        add("item.hbm_ntm_rebirth.glyphid_gland", "\u817a\u4f53");
        add("item.hbm_ntm_rebirth.inf_water", "\u65e0\u9650\u6c34");
        add("item.hbm_ntm_rebirth.inf_water_mk2", "\u65e0\u9650\u6c34 Mk2");
        add("item.hbm_ntm_rebirth.chlorine_pinwheel", "\u6c2f\u6c14\u98ce\u8f6e");
        add("item.hbm_ntm_rebirth.fluid_identifier_multi", "\u591a\u7c7b\u578b\u6d41\u4f53\u8bc6\u522b\u7801");
        add("item.hbm_ntm_rebirth.fluid_identifier_multi.info", "\u6d41\u4f53\uff1a");
        add("item.hbm_ntm_rebirth.fluid_identifier_multi.info2", "\u6b21\u8981\u7c7b\u578b\uff1a");
        add("item.hbm_ntm_rebirth.battery_potato", "\u9a6c\u94c3\u85af\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_creative", "\u65e0\u9650\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_redstone", "\u7ea2\u77f3\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_lead", "\u94c5\u9178\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_lithium", "\u9502\u79bb\u5b50\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sodium", "\u94a0\u94c1\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_schrabidium", "Sa326\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_quantum", "\u91cf\u5b50\u7535\u6c60");
        add("item.hbm_ntm_rebirth.capacitor_copper", "\u94dc\u7535\u5bb9");
        add("item.hbm_ntm_rebirth.capacitor_gold", "\u91d1\u7535\u5bb9");
        add("item.hbm_ntm_rebirth.capacitor_niobium", "\u94cc\u7535\u5bb9");
        add("item.hbm_ntm_rebirth.capacitor_tantalum", "\u94bd\u7535\u5bb9");
        add("item.hbm_ntm_rebirth.capacitor_bismuth", "\u94cb\u7535\u5bb9");
        add("item.hbm_ntm_rebirth.capacitor_spark", "Spark\u7535\u5bb9");
        add("item.hbm_ntm_rebirth.battery_sc.empty", "\u7a7a\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.waste", "\u6838\u5e9f\u6599\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.ra226", "\u956d-226\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.tc99", "\u951d-99\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.co60", "\u94b4-60\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.pu238", "\u94b8-238\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.po210", "\u948b-210\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.au198", "\u91d1-198\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.pb209", "\u94c5-209\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.battery_sc.am241", "\u9545-241\u81ea\u5145\u7535\u7535\u6c60");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_bismuth", "\u67af\u7aed\u94cb\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u71c3\u6599\u9776\u4e38");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_lead", "\u67af\u7aed\u94c5\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u71c3\u6599\u9776\u4e38");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_mercury", "\u67af\u7aed\u6c5e\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u71c3\u6599\u9776\u4e38");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_neptunium", "\u67af\u7aed\u954e\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u71c3\u6599\u9776\u4e38");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_nickel", "\u67af\u7aed\u954d\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u71c3\u6599\u9776\u4e38");
        add("item.hbm_ntm_rebirth.pellet_rtg_depleted_zirconium", "\u67af\u7aed\u9506\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u71c3\u6599\u9776\u4e38");
        add("item.hbm_ntm_rebirth.rod_empty", "\u7a7a\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_lithium", "\u9502\u68d2");
        add("item.hbm_ntm_rebirth.rod_tritium", "\u6c1a\u68d2");
        add("item.hbm_ntm_rebirth.rod_co", "\u94b4\u68d2");
        add("item.hbm_ntm_rebirth.rod_co60", "\u94b4-60\u68d2");
        add("item.hbm_ntm_rebirth.rod_th232", "\u948d-232\u68d2");
        add("item.hbm_ntm_rebirth.rod_thf", "\u948d\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_u235", "\u94c0-235\u68d2");
        add("item.hbm_ntm_rebirth.rod_np237", "\u954e-237\u68d2");
        add("item.hbm_ntm_rebirth.rod_u238", "\u94c0-238\u68d2");
        add("item.hbm_ntm_rebirth.rod_pu238", "\u949a-238\u68d2");
        add("item.hbm_ntm_rebirth.rod_pu239", "\u949a-239\u68d2");
        add("item.hbm_ntm_rebirth.rod_rgp", "\u53cd\u5e94\u5806\u7ea7\u949a\u68d2");
        add("item.hbm_ntm_rebirth.rod_waste", "\u6838\u5e9f\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_lead", "\u94c5\u68d2");
        add("item.hbm_ntm_rebirth.rod_uranium", "\u94c0\u68d2");
        add("item.hbm_ntm_rebirth.rod_ra226", "\u956d-226\u68d2");
        add("item.hbm_ntm_rebirth.rod_ac227", "\u9515-227\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_empty", "\u7a7a\u53cc\u8054\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_lithium", "\u53cc\u8054\u9502\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_tritium", "\u53cc\u8054\u6c1a\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_co", "\u53cc\u8054\u94b4\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_co60", "\u53cc\u8054\u94b4-60\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_th232", "\u53cc\u8054\u948d-232\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_thf", "\u53cc\u8054\u948d\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_u235", "\u53cc\u8054\u94c0-235\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_np237", "\u53cc\u8054\u954e-237\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_u238", "\u53cc\u8054\u94c0-238\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_pu238", "\u53cc\u8054\u949a-238\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_pu239", "\u53cc\u8054\u949a-239\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_rgp", "\u53cc\u8054\u53cd\u5e94\u5806\u7ea7\u949a\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_waste", "\u53cc\u8054\u6838\u5e9f\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_lead", "\u53cc\u8054\u94c5\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_uranium", "\u53cc\u8054\u94c0\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_ra226", "\u53cc\u8054\u956d-226\u68d2");
        add("item.hbm_ntm_rebirth.rod_dual_ac227", "\u53cc\u8054\u9515-227\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_empty", "\u7a7a\u56db\u8054\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_lithium", "\u56db\u8054\u9502\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_tritium", "\u56db\u8054\u6c1a\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_co", "\u56db\u8054\u94b4\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_co60", "\u56db\u8054\u94b4-60\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_th232", "\u56db\u8054\u948d-232\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_thf", "\u56db\u8054\u948d\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_u235", "\u56db\u8054\u94c0-235\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_np237", "\u56db\u8054\u954e-237\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_u238", "\u56db\u8054\u94c0-238\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_pu238", "\u56db\u8054\u949a-238\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_pu239", "\u56db\u8054\u949a-239\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_rgp", "\u56db\u8054\u53cd\u5e94\u5806\u7ea7\u949a\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_waste", "\u56db\u8054\u6838\u5e9f\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_lead", "\u56db\u8054\u94c5\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_uranium", "\u56db\u8054\u94c0\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_ra226", "\u56db\u8054\u956d-226\u68d2");
        add("item.hbm_ntm_rebirth.rod_quad_ac227", "\u56db\u8054\u9515-227\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_empty", "\u7a7a\u9506\u8bfa\u514b\u65af\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_natural_uranium_fuel", "\u9506\u8bfa\u514b\u65af \u5929\u7136\u94c0\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_uranium_fuel", "\u9506\u8bfa\u514b\u65af \u94c0\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_th232", "\u9506\u8bfa\u514b\u65af \u948d-232\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_thorium_fuel", "\u9506\u8bfa\u514b\u65af \u948d\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_mox_fuel", "\u9506\u8bfa\u514b\u65af  MOX\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_plutonium_fuel", "\u9506\u8bfa\u514b\u65af \u94b8\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_u233_fuel", "\u9506\u8bfa\u514b\u65af \u94c0-233\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_u235_fuel", "\u9506\u8bfa\u514b\u65af \u94c0-235\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_les_fuel", "\u9506\u8bfa\u514b\u65af  LES\u4f4e\u6d53\u5ea6Sa326\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_lithium", "\u9506\u8bfa\u514b\u65af \u9502\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_zfb_mox", "\u9506\u8bfa\u514b\u65af ZFB MOX\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_tritium", "\u9506\u8bfa\u514b\u65af \u6c1a\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_natural_uranium_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aed\u5929\u7136\u94c0\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_uranium_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aed\u94c0\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_thorium_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aed\u948d\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_mox_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aedMOX\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_plutonium_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aed\u94b8\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_u233_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aed\u94c0-233\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_u235_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aed\u94c0-235\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_les_fuel_depleted", "\u9506\u8bfa\u514b\u65af \u67af\u7aedLES\u4f4e\u6d53\u5ea6Sa326\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rod_zirnox_zfb_mox_depleted", "\u9506\u8bfa\u514b\u65af ZFB \u67af\u7aedMOX\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_meu", "MEU \u4e2d\u6d53\u7f29\u5ea6\u94c0\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_heu233", "HEU-233 \u9ad8\u6d53\u7f29\u5ea6\u94c0-233\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_heu235", "HEU-235 \u9ad8\u6d53\u7f29\u5ea6\u94c0-235\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_men", "MEN \u4e2d\u6d53\u7f29\u5ea6\u954e\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hen237", "HEN-237 \u9ad8\u6d53\u7f29\u5ea6\u954e-237\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_mox", "MOX \u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_mep", "MEP \u4e2d\u6d53\u7f29\u5ea6\u949a\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hep239", "HEP-239 \u9ad8\u6d53\u7f29\u5ea6\u949a-239\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hep241", "HEP-241 \u9ad8\u6d53\u7f29\u5ea6\u949a-241\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_mea", "MEA \u4e2d\u6d53\u7f29\u5ea6\u9545\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hea242", "HEA-242 \u9ad8\u6d53\u7f29\u5ea6\u9545-242\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hes326", "HES-326 \u9ad8\u6d53\u7f29\u5ea6Sa326\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hes327", "HES-327 \u9ad8\u6d53\u7f29\u5ea6Sa327\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_bfb_am_mix", "\u71c3\u6599\u7ea7\u9545\u538b\u6c34\u5806BFB\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_bfb_pu241", "\u949a-241\u538b\u6c34\u5806BFB\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_meu", "\u9ad8\u6e29 MEU \u4e2d\u6d53\u7f29\u5ea6\u94c0\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_heu233", "\u9ad8\u6e29 HEU-233 \u9ad8\u6d53\u7f29\u5ea6\u94c0-233\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_heu235", "\u9ad8\u6e29 HEU-235 \u9ad8\u6d53\u7f29\u5ea6\u94c0-235\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_men", "\u9ad8\u6e29 MEN \u4e2d\u6d53\u7f29\u5ea6\u954e\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hen237", "\u9ad8\u6e29 HEN-237 \u9ad8\u6d53\u7f29\u5ea6\u954e-237\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_mox", "\u9ad8\u6e29 MOX\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_mep", "\u9ad8\u6e29 MEP \u4e2d\u6d53\u7f29\u5ea6\u949a\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hep239", "\u9ad8\u6e29 HEP-239 \u9ad8\u6d53\u7f29\u5ea6\u949a-239\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hep241", "\u9ad8\u6e29 HEP-241 \u9ad8\u6d53\u7f29\u5ea6\u949a-241\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_mea", "\u9ad8\u6e29 MEA \u4e2d\u6d53\u7f29\u5ea6\u9545\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hea242", "\u9ad8\u6e29 HEA-242 \u9ad8\u6d53\u7f29\u5ea6\u9545-242\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hes326", "\u9ad8\u6e29 HES-326 \u9ad8\u6d53\u7f29\u5ea6Sa326\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_hes327", "\u9ad8\u6e29 HES-327 \u9ad8\u6d53\u7f29\u5ea6Sa327\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_bfb_am_mix", "\u9ad8\u6e29 \u71c3\u6599\u7ea7\u9545\u538b\u6c34\u5806BFB\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_hot_bfb_pu241", "\u9ad8\u6e29 \u949a-241\u538b\u6c34\u5806BFB\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_meu", "\u67af\u7aed MEU \u4e2d\u6d53\u7f29\u5ea6\u94c0\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_heu233", "\u67af\u7aed HEU-233 \u9ad8\u6d53\u7f29\u5ea6\u94c0-233\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_heu235", "\u67af\u7aed HEU-235 \u9ad8\u6d53\u7f29\u5ea6\u94c0-235\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_men", "\u67af\u7aed MEN \u4e2d\u6d53\u7f29\u5ea6\u954e\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hen237", "\u67af\u7aed HEN-237 \u9ad8\u6d53\u7f29\u5ea6\u954e-237\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_mox", "\u67af\u7aed MOX \u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_mep", "\u67af\u7aed MEP \u4e2d\u6d53\u7f29\u5ea6\u949a\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hep239", "\u67af\u7aed HEP-239 \u9ad8\u6d53\u7f29\u5ea6\u949a-239\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hep241", "\u67af\u7aed HEP-241 \u9ad8\u6d53\u7f29\u5ea6\u949a-241\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_mea", "\u67af\u7aed MEA \u4e2d\u6d53\u7f29\u5ea6\u9545\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hea242", "\u67af\u7aed HEA-242 \u9ad8\u6d53\u7f29\u5ea6\u9545-242\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hes326", "\u67af\u7aed HES-326 \u9ad8\u6d53\u7f29\u5ea6Sa326\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_hes327", "\u67af\u7aed HES-327 \u9ad8\u6d53\u7f29\u5ea6Sa327\u538b\u6c34\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_bfb_am_mix", "\u67af\u7aed\u71c3\u6599\u7ea7\u9545\u538b\u6c34\u5806BFB\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.pwr_fuel_depleted_bfb_pu241", "\u67af\u7aed\u949a-241\u538b\u6c34\u5806BFB\u68d2");
        add("item.hbm_ntm_rebirth.watz_pellet_schrabidium", "Sa326 Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_hes", "HES Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_mes", "MES Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_les", "LES Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_hen", "HEN Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_meu", "MEU Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_mep", "MEP Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_lead", "\u94c5\u5438\u6536\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_boron", "\u787c\u5438\u6536\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_du", "\u8d2b\u94c0\u5438\u6536\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_nqd", "\u6d53\u7f29\u7845\u5ca9\u91d1\u5c5e Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.watz_pellet_nqr", "\u7845\u5ca9\u91d1\u5c5e\u5316\u5408\u7269 Watz\u9776\u4e38");
        add("item.hbm_ntm_rebirth.nugget_schrabidium", "Sa326\u7c92");
        add("item.hbm_ntm_rebirth.billet_schrabidium", "Sa326\u576f\u6599");
        add("item.hbm_ntm_rebirth.ingot_schrabidate", "Sa\u9178\u94c1\u952d");
        add("item.hbm_ntm_rebirth.ingot_schraranium", "\u4f4e\u4e30\u5ea6Sa326\u952d");
        add("item.hbm_ntm_rebirth.rbmk_lid", "RBMK\u53cd\u5e94\u5806\u76d6\u677f");
        add("item.hbm_ntm_rebirth.rbmk_lid_glass", "RBMK\u53cd\u5e94\u5806\u73bb\u7483\u76d6\u677f");
        add("item.hbm_ntm_rebirth.rbmk_fuel_empty", "\u7a7a RBMK\u53cd\u5e94\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_tool", "RBMK\u63a7\u5236\u53f0\u5de5\u5177");
        add("item.hbm_ntm_rebirth.rbmk_fuel_ueu", "NU RBMK\u53cd\u5e94\u5806\u672a\u6d53\u7f29\u94c0\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_meu", "MEU RBMK\u4e2d\u6d53\u7f29\u5ea6\u94c0-235\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_heu233", "HEU-233 RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u94c0-233\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_heu235", "HEU-235 RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u94c0-235\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_uzh", "RBMK\u53cd\u5e94\u5806\u94c0\u6c22\u9506\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_thmeu", "ThMEU RBMK\u53cd\u5e94\u5806\u4e2d\u6d53\u7f29\u5ea6\u94c0-233\u5bfc\u5411\u948d\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_lep", "LEP RBMK\u53cd\u5e94\u5806\u4f4e\u6d53\u7f29\u5ea6\u949a-239\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mep", "MEP RBMK\u53cd\u5e94\u5806\u4e2d\u6d53\u7f29\u5ea6\u949a-239\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hep239", "HEP-239 RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u949a-239\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hep241", "HEP-241 RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u949a-241\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_lea", "LEA RBMK\u53cd\u5e94\u5806\u4f4e\u6d53\u7f29\u5ea6\u9545-242\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mea", "MEA RBMK\u53cd\u5e94\u5806\u4e2d\u6d53\u7f29\u5ea6\u9545-242\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hea241", "HEA-241 RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u9545-241\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hea242", "HEA-242 RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u9545-242\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_men", "MEN RBMK\u53cd\u5e94\u5806\u4e2d\u6d53\u7f29\u5ea6\u954e-237\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hen", "HEN RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u954e\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mox", "MOX RBMK\u53cd\u5e94\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_les", "LES RBMK\u53cd\u5e94\u5806\u4f4e\u6d53\u7f29\u5ea6Sa326\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_mes", "MES RBMK\u4e2d\u6d53\u7f29\u5ea6Sa326\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_hes", "HES RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6Sa326\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_leaus", "LEAus RBMK\u53cd\u5e94\u5806\u4f4e\u6d53\u7f29\u5ea6\u5965\u65af\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_heaus", "HEAus RBMK\u53cd\u5e94\u5806\u9ad8\u6d53\u7f29\u5ea6\u5965\u65af\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_po210be", "\u948b210-\u94cd RBMK\u53cd\u5e94\u5806\u4e2d\u5b50\u6e90");
        add("item.hbm_ntm_rebirth.rbmk_fuel_ra226be", "\u956d226-\u94cd RBMK\u53cd\u5e94\u5806\u4e2d\u5b50\u6e90");
        add("item.hbm_ntm_rebirth.rbmk_fuel_pu238be", "\u949a238-\u94cd RBMK\u53cd\u5e94\u5806\u4e2d\u5b50\u6e90");
        add("item.hbm_ntm_rebirth.rbmk_fuel_balefire_gold", "RBMK\u53cd\u5e94\u5806\u6fc0\u6d3b\u6001\u91d1-198\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_flashlead", "RBMK\u53cd\u5e94\u5806\u95ea\u5149\u94c5\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_balefire", "RBMK\u53cd\u5e94\u5806\u91ce\u706b\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_zfb_bismuth", "ZFB \u94cbRBMK\u53cd\u5e94\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_zfb_pu241", "ZFB \u949a-241RBMK\u53cd\u5e94\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_zfb_am_mix", "ZFB \u71c3\u6599\u7ea7\u9545RBMK\u53cd\u5e94\u5806\u71c3\u6599\u68d2");
        add("item.hbm_ntm_rebirth.rbmk_fuel_drx", "\u00A7cRBMK\u53cd\u5e94\u5806F\u8fea\u4f3d\u9a6c\u7c92\u5b50\u71c3\u6599\u68d2\u00A7r");
        add("item.hbm_ntm_rebirth.rbmk_pellet_ueu", "\u672a\u6d53\u7f29\u94c0\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_meu", "MEU \u4e2d\u6d53\u7f29\u5ea6\u94c0-235\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_heu233", "HEU-233 \u9ad8\u6d53\u7f29\u5ea6\u94c0-233\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_heu235", "HEU-235 \u9ad8\u6d53\u7f29\u5ea6\u94c0-235\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_uzh", "\u94c0\u6c22\u9506\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_thmeu", "ThMEU \u6d53\u7f29\u5ea6\u94c0-235\u5bfc\u5411\u948d\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_lep", "LEP \u4f4e\u6d53\u7f29\u5ea6\u949a-239\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mep", "MEP \u4e2d\u6d53\u7f29\u5ea6\u949a-239\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hep239", "HEP-239 \u9ad8\u6d53\u7f29\u5ea6\u949a-239\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hep241", "HEP-241 \u9ad8\u6d53\u7f29\u5ea6\u949a-241\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_lea", "LEA \u4f4e\u6d53\u7f29\u5ea6\u9545-242\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mea", "MEA \u4e2d\u6d53\u7f29\u5ea6\u9545-242\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hea241", "HEA-241 \u9ad8\u6d53\u7f29\u5ea6\u9545-241\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hea242", "HEA-242 \u9ad8\u6d53\u7f29\u5ea6\u9545-242\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_men", "MEN \u4e2d\u6d53\u7f29\u5ea6\u954e-237\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hen", "HEN \u9ad8\u6d53\u7f29\u5ea6\u954e\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mox", "MOX\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_les", "LES \u4f4e\u6d53\u7f29\u5ea6Sa-326\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_mes", "MES \u4e2d\u6d53\u7f29\u5ea6Sa-326\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_hes", "HES \u9ad8\u6d53\u7f29\u5ea6Sa-326\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_leaus", "LEAus \u4f4e\u6d53\u7f29\u5ea6\u5965\u65af\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_heaus", "HEAus \u9ad8\u6d53\u7f29\u5ea6\u5965\u65af\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_po210be", "\u948b210-\u94cd \u4e2d\u5b50\u6e90\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_ra226be", "\u956d226-\u94cd \u4e2d\u5b50\u6e90\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_pu238be", "\u949a238-\u94cd \u4e2d\u5b50\u6e90\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_balefire_gold", "\u6fc0\u6d3b\u6001\u91d1-198\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_flashlead", "\u95ea\u5149\u94c5\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_balefire", "\u91ce\u706b\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_zfb_bismuth", "ZFB \u94cb\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_zfb_pu241", "ZFB \u949a-241\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_zfb_am_mix", "ZFB \u71c3\u6599\u7ea7\u9545\u71c3\u6599\u82af\u5757");
        add("item.hbm_ntm_rebirth.rbmk_pellet_drx", "\u00A7cF\u8fea\u4f3d\u9a6c\u7c92\u5b50\u71c3\u6599\u82af\u5757\u00A7r");
        add("desc.item.battery.charge", "\u50a8\u80fd: %s / %sHE");
        add("desc.item.battery.chargePerc", "\u50a8\u80fd: %s%%");
        add("desc.item.battery.chargeRate", "\u5145\u7535\u6548\u7387: %sHE/\u523b");
        add("desc.item.battery.dischargeRate", "\u653e\u7535\u6548\u7387: %sHE/\u523b");
        add("desc.item.wasteCooling", "\u6b63\u5728\u51b7\u5374");
        add("trait.rbmk.coreTemp", "\u6838\u5fc3\u6e29\u5ea6: %s");
        add("trait.rbmk.depletion", "\u71c3\u6599\u6d88\u8017: %s");
        add("trait.rbmk.diffusion", "\u6269\u6563\u5ea6: %s");
        add("trait.rbmk.fluxFunc", "\u4e2d\u5b50\u653e\u5c04\u51fd\u6570: %s");
        add("trait.rbmk.funcType", "\u4e2d\u5b50\u653e\u5c04\u51fd\u6570\u7c7b\u578b: %s");
        add("trait.rbmk.heat", "\u6bcf\u63a5\u53d7\u4e00\u4e2a\u4e2d\u5b50\u4ea7\u751f\u7684\u70ed\u91cf: %s");
        add("trait.rbmk.melt", "\u7194\u70b9: %s");
        add("trait.rbmk.neutron.any", "\u6240\u6709\u4e2d\u5b50");
        add("trait.rbmk.neutron.fast", "\u5feb\u4e2d\u5b50");
        add("trait.rbmk.neutron.slow", "\u6162\u4e2d\u5b50");
        add("trait.rbmk.skinTemp", "\u8868\u9762\u6e29\u5ea6: %s");
        add("trait.rbmk.source", "\u81ea\u71c3");
        add("trait.rbmk.splitsInto", "\u88c2\u53d8\u540e\u91ca\u653e\u51fa: %s");
        add("trait.rbmk.splitsWith", "\u88c2\u53d8\u9700\u8981: %s");
        add("trait.rbmk.xenon", "\u6c19\u5143\u7d20\u5806\u79ef\u7a0b\u5ea6: %s");
        add("trait.rbmk.xenonBurn", "\u6c19\u5143\u7d20\u71c3\u70e7\u51fd\u6570: %s");
        add("trait.rbmk.xenonGen", "\u6c19\u5143\u7d20\u4ea7\u751f\u51fd\u6570: %s");
        add("trait.rbmx.coreTemp", "\u6838\u5fc3\u71b5: %s");
        add("trait.rbmx.depletion", "\u8870\u53d8: %s");
        add("trait.rbmx.diffusion", "\u80fd\u91cf\u6f6e\u6d8c: %s");
        add("trait.rbmx.fluxFunc", "F\u8fea\u4f3d\u9a6c\u7c92\u5b50\u653e\u5c04\u51fd\u6570: %s");
        add("trait.rbmx.funcType", "F\u8fea\u4f3d\u9a6c\u7c92\u5b50\u653e\u5c04\u51fd\u6570\u7c7b\u578b: %s");
        add("trait.rbmx.heat", "\u6ee1\u529f\u7387\u4e0b\u6bcf\u523b\u4ea7\u751f\u7684\u70ed\u91cf: %s");
        add("trait.rbmx.melt", "\u5d29\u6e83\u6df1\u5ea6: %s");
        add("trait.rbmk.neutron.any.x", "\u6240\u6709\u975e\u6b27\u51e0\u91cc\u5fb7\u5f62\u65f6\u7a7a\u5e73\u9762");
        add("trait.rbmk.neutron.fast.x", "\u692d\u5706\u975e\u6b27\u51e0\u91cc\u5fb7\u5f62\u65f6\u7a7a\u5e73\u9762");
        add("trait.rbmk.neutron.slow.x", "\u53cc\u66f2\u975e\u6b27\u51e0\u91cc\u5fb7\u5f62\u65f6\u7a7a\u5e73\u9762");
        add("trait.rbmx.skinTemp", "\u8868\u9762\u71b5: %s");
        add("trait.rbmx.source", "\u81ea\u71c3");
        add("trait.rbmx.splitsInto", "\u91ca\u653e: %s");
        add("trait.rbmx.splitsWith", "\u63a5\u53d7: %s");
        add("trait.rbmx.xenon", "\u94c5\u5143\u7d20\u5806\u79ef\u7a0b\u5ea6: %s");
        add("trait.rbmx.xenonBurn", "\u94c5\u5143\u7d20\u5bc2\u706d\u51fd\u6570: %s");
        add("trait.rbmx.xenonGen", "\u94c5\u5143\u7d20\u4ea7\u751f\u51fd\u6570: %s");
        add("effect.hbm_ntm_rebirth.radiation", "\u8f90\u5c04");
        add("effect.hbm_ntm_rebirth.radaway", "\u6d88\u8f90\u5b81");
        add("effect.hbm_ntm_rebirth.radx", "\u8f90\u7279\u5b81");
        add("effect.hbm_ntm_rebirth.taint", "\u6c61\u79fd");
        add("effect.hbm_ntm_rebirth.mutation", "\u7a81\u53d8");
        add("effect.hbm_ntm_rebirth.stability", "\u7a33\u5b9a");
        add("effect.hbm_ntm_rebirth.lead", "\u94c5\u4e2d\u6bd2");
        add("effect.hbm_ntm_rebirth.bang", "Bang");
        add("effect.hbm_ntm_rebirth.phosphorus", "\u78f7\u70e7");
        add("effect.hbm_ntm_rebirth.potionsickness", "\u836f\u6c34\u75c5");
        add("effect.hbm_ntm_rebirth.death", "\u6b7b\u4ea1");
        add("geiger.title", "\u76d6\u9769\u8ba1\u6570\u5668");
        add("geiger.title.dosimeter", "\u5242\u91cf\u8ba1");
        add("geiger.chunkRad", "\u533a\u5757\u8f90\u5c04\uff1a%s RAD/s");
        add("geiger.envRad", "\u73af\u5883\u5242\u91cf\uff1a%s RAD/s");
        add("geiger.playerRad", "\u73a9\u5bb6\u5242\u91cf\uff1a%s RAD");
        add("geiger.playerRes", "\u8f90\u5c04\u6297\u6027\uff1a%s%%\uff08%s\uff09");
        add("digamma.title", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u5c04\u81ea\u68c0\u5668");
        add("digamma.playerDigamma", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u8f90\u7167\u6c34\u5e73\uff1a%s DRX");
        add("digamma.playerHealth", "\u73a9\u5bb6\u6240\u53d7F-\u8fea\u4f3d\u9a6c\u8f90\u7167\u5f71\u54cd\uff1a%s%%");
        add("digamma.playerRes", "\u73a9\u5bb6F-\u8fea\u4f3d\u9a6c\u9632\u62a4\u6c34\u5e73\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.radiation.single", "\u8f90\u5c04\uff1a%s RAD/s");
        add("tooltip.hbm_ntm_rebirth.radiation.total", "\u6574\u7ec4\u603b\u8f90\u5c04\uff1a%s RAD/s");
        add("tooltip.hbm_ntm_rebirth.radiation.resistance", "\u8f90\u5c04\u6297\u6027\uff1a%s\uff08\u963b\u6321 %s%%\uff09");
        add("tooltip.hbm_ntm_rebirth.hazard.digamma", "Digamma\uff1a%s DRX");
        add("tooltip.hbm_ntm_rebirth.hazard.hot", "\u70ed\u91cf\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.hazard.blinding", "\u81f4\u76f2\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.hazard.asbestos", "\u77f3\u68c9\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.hazard.coal", "\u7164\u5c18\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.hazard.hydroactive", "\u6c34\u6d3b\u6027\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.hazard.explosive", "\u7206\u70b8\u6027\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.damage.set", "\u5957\u88c5\u4f24\u5bb3\u6297\u6027");
        add("tooltip.hbm_ntm_rebirth.damage.item", "\u7269\u54c1\u4f24\u5bb3\u6297\u6027");
        add("tooltip.hbm_ntm_rebirth.damage.line", "%s\uff1a%s/%s%%");
        add("tooltip.hbm_ntm_rebirth.damage.other", "\u5176\u4ed6");
        add("tooltip.hbm_ntm_rebirth.damage.category.EXPL", "\u7206\u70b8");
        add("tooltip.hbm_ntm_rebirth.damage.category.FIRE", "\u706b\u7130");
        add("tooltip.hbm_ntm_rebirth.damage.category.PHYS", "\u7269\u7406");
        add("tooltip.hbm_ntm_rebirth.damage.category.EN", "\u80fd\u91cf");
        add("tooltip.hbm_ntm_rebirth.damage.exact.drown", "\u6eba\u6c34");
        add("tooltip.hbm_ntm_rebirth.damage.exact.fall", "\u6454\u843d");
        add("tooltip.hbm_ntm_rebirth.damage.exact.laser", "\u6fc0\u5149");
        add("tooltip.hbm_ntm_rebirth.damage.exact.onfire", "\u707c\u70e7");
        add("tooltip.hbm_ntm_rebirth.damage.exact.acidplayer", "\u9178\u6db2");
        add("tooltip.hbm_ntm_rebirth.damage.exact.taublast", "Tau\u7206\u70b8");
        add("tooltip.hbm_ntm_rebirth.damage.exact.revolverbullet", "\u5b50\u5f39");
        add("tooltip.hbm_ntm_rebirth.damage.exact.chopperbullet", "\u76f4\u5347\u673a\u5b50\u5f39");
        add("tooltip.hbm_ntm_rebirth.damage.exact.cmb", "\u8054\u5408\u519b\u80fd\u91cf\u7403");
        add("tooltip.hbm_ntm_rebirth.damage.exact.nuclearblast", "\u6838\u7206");
        add("tooltip.hbm_ntm_rebirth.damage.exact.mudpoisoning", "\u6bd2\u6ce5\u4e2d\u6bd2");
        add("item.hbm_ntm_rebirth.turret_biometry", "\u751f\u7269\u8bc6\u522b\u5361");
        add("item.hbm_ntm_rebirth.turret_chip", "\u70ae\u5854\u82af\u7247");
        add("item.hbm_ntm_rebirth.turret_biometry.added", "\u5df2\u6dfb\u52a0\u73a9\u5bb6\u6570\u636e\uff01");
        add("item.hbm_ntm_rebirth.ammo_standard_tau_uranium", "Tau\u94c0\u80fd\u91cf\u5355\u5143");
        add("item.hbm_ntm_rebirth.ammo_standard_ct_hook", "\u6293\u94a9");
        add("item.hbm_ntm_rebirth.ammo_standard_ct_mortar", "\u70b8\u836f\u5305");
        add("item.hbm_ntm_rebirth.ammo_standard_ct_mortar_charge", "\u91cd\u578b\u70b8\u836f\u5305");
        add("item.hbm_ntm_rebirth.gun_tau", "\u03c4\u5b50\u70ae");
        add("item.hbm_ntm_rebirth.gun_charge_thrower", "\u70b8\u836f\u6295\u63b7\u5668");
        add("item.hbm_ntm_rebirth.gun_chemthrower", "\u5316\u5b66\u55b7\u5c04\u5668");
        add("item.hbm_ntm_rebirth.gun_n_i_4_n_i", "N I 4 N I");
        add("item.hbm_ntm_rebirth.gun_drill", "\u52a8\u529b\u94bb");
        add("item.hbm_ntm_rebirth.ammo_dgk", "20mm CIWS\u5f39");
        add("item.hbm_ntm_rebirth.ammo_shell_stock", "240mm\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_shell_explosive", "240mm\u7206\u7834\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_shell_apfsds_t", "240mm APFSDS-T\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_shell_apfsds_du", "240mm DU APFSDS\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_shell_w9", "240mm W9\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty", "16\u82f1\u5bf8\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_classic", "16\u82f1\u5bf8\u91cd\u70ae\u70ae\u5f39\uff08Factorio Special\uff09");
        add("item.hbm_ntm_rebirth.ammo_arty_he", "16\u82f1\u5bf8\u9ad8\u7206\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_phosphorus", "16\u82f1\u5bf8\u767d\u78f7\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_phosphorus_multi", "16\u82f1\u5bf8 MIRV \u767d\u78f7\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_mini_nuke", "16\u82f1\u5bf8\u6838\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_mini_nuke_multi", "16\u82f1\u5bf8 MIRV \u6838\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_nuke", "16\u82f1\u5bf8\u6838\u5f39\u5934");
        add("item.hbm_ntm_rebirth.ammo_arty_cargo", "16\u82f1\u5bf8\u8d27\u8f7d\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_chlorine", "16\u82f1\u5bf8\u6c2f\u6c14\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_phosgene", "16\u82f1\u5bf8\u5149\u6c14\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty_mustard_gas", "16\u82f1\u5bf8\u82a5\u5b50\u6c14\u91cd\u70ae\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_himars_standard", "227mm\u5236\u5bfc\u706b\u7bad\u70ae\u5f39\u8231");
        add("item.hbm_ntm_rebirth.ammo_himars_standard_he", "227mm\u9ad8\u7206\u5236\u5bfc\u706b\u7bad\u70ae\u5f39\u8231");
        add("item.hbm_ntm_rebirth.ammo_himars_standard_wp", "227mm\u767d\u78f7\u5236\u5bfc\u706b\u7bad\u70ae\u5f39\u8231");
        add("item.hbm_ntm_rebirth.ammo_himars_standard_tb", "227mm\u6e29\u538b\u5236\u5bfc\u706b\u7bad\u70ae\u5f39\u8231");
        add("item.hbm_ntm_rebirth.ammo_himars_standard_lava", "227mm\u7194\u5ca9\u5236\u5bfc\u706b\u7bad\u70ae\u5f39\u8231");
        add("item.hbm_ntm_rebirth.ammo_himars_standard_mini_nuke", "227mm\u6838\u5236\u5bfc\u706b\u7bad\u70ae\u5f39\u8231");
        add("item.hbm_ntm_rebirth.ammo_himars_single", "610mm\u5236\u5bfc\u706b\u7bad\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_himars_single_tb", "610mm\u6e29\u538b\u5236\u5bfc\u706b\u7bad\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.normal", "\u5f3a\u5ea6\uff1a10 | \u4f24\u5bb3\u500d\u7387\uff1a3x | \u4e0d\u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.classic", "\u5f3a\u5ea6\uff1a15 | \u4f24\u5bb3\u500d\u7387\uff1a5x | \u4e0d\u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.he", "\u5f3a\u5ea6\uff1a15 | \u4f24\u5bb3\u500d\u7387\uff1a3x | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.phosphorus", "\u5f3a\u5ea6\uff1a10 | \u4f24\u5bb3\u500d\u7387\uff1a3x | \u767d\u78f7\u6e85\u5c04 | \u4e0d\u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.phosphorus_multi", "\u5206\u88c2 x10");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.mini_nuke", "\u5f3a\u5ea6\uff1a20 | \u9020\u6210\u6838\u4f24\u5bb3 | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.mini_nuke_multi", "\u5206\u88c2 x5");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.nuke", "\u6838\u91cd\u70ae\u5f39\u5934");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.cargo", "\u7a7a");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.chlorine", "\u6c2f\u6c14\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.phosgene", "\u5149\u6c14\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_arty.desc.mustard_gas", "\u82a5\u5b50\u6c14\u70ae\u5f39");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.standard", "\u5f3a\u5ea6\uff1a20 | \u4f24\u5bb3\u500d\u7387\uff1a3x | \u4e0d\u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.standard_he", "\u5f3a\u5ea6\uff1a20 | \u4f24\u5bb3\u500d\u7387\uff1a3x | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.standard_wp", "\u5f3a\u5ea6\uff1a20 | \u4f24\u5bb3\u500d\u7387\uff1a3x | \u767d\u78f7\u6e85\u5c04 | \u4e0d\u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.standard_tb", "\u5f3a\u5ea6\uff1a20 | \u4f24\u5bb3\u500d\u7387\uff1a10x | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.standard_lava", "\u5f3a\u5ea6\uff1a20 | \u751f\u6210\u706b\u5c71\u7194\u5ca9 | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.standard_mini_nuke", "\u5f3a\u5ea6\uff1a20 | \u9020\u6210\u6838\u4f24\u5bb3 | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.single", "\u5f3a\u5ea6\uff1a50 | \u4f24\u5bb3\u500d\u7387\uff1a5x | \u7834\u574f\u65b9\u5757");
        add("item.hbm_ntm_rebirth.ammo_himars.desc.single_tb", "\u5f3a\u5ea6\uff1a50 | \u4f24\u5bb3\u500d\u7387\uff1a12x | \u7834\u574f\u65b9\u5757");
        add("block.hbm_ntm_rebirth.crate_iron", "\u94c1\u8d28\u50a8\u7269\u7bb1");
        add("block.hbm_ntm_rebirth.crate_steel", "\u94a2\u8d28\u50a8\u7269\u7bb1");
        add("container.crateIron", "\u94c1\u8d28\u50a8\u7269\u7bb1");
        add("container.crateSteel", "\u94a2\u8d28\u50a8\u7269\u7bb1");
        add("block.hbm_ntm_rebirth.turret_chekhov", "\u5951\u8bc3\u592b\u81ea\u52a8\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_friendly", "\u53cb\u519b\u81ea\u52a8\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_jeremy", "Jeremy 240mm\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_richard", "Richard\u706b\u7bad\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_tauon", "Tauon\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_howard", "Howard CIWS\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_sentry", "\u54e8\u6212\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_howard_damaged", "CIWS\u53cc\u8054\u5b88\u95e8\u5458\u8fd1\u9632\u7cfb\u7edf \u201c\u739b\u58eb\u6492\u62c9\u201d");
        add("block.hbm_ntm_rebirth.turret_sentry_damaged", "\u54e8\u5175\u70ae\u5854\u201c\u57c3\u5fb7\u6e29\u201d");
        add("block.hbm_ntm_rebirth.turret_maxwell", "Maxwell\u5fae\u6ce2\u70ae\u5854");
        add("block.hbm_ntm_rebirth.turret_arty", "\u91cd\u70ae\u70ae\u5854\u201c\u683c\u96f7\u683c\u201d");
        add("block.hbm_ntm_rebirth.turret_himars", "\u706b\u7bad\u70ae\u5854\u201c\u4ea8\u5229\u201d");
        add("block.hbm_ntm_rebirth.turret_fritz", "\u91cd\u578b\u706b\u7130\u55b7\u5c04\u5668\u70ae\u5854\u201c\u5f17\u91cc\u8328\u201d");
        add("container.hbm_ntm_rebirth.turret", "\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_chekhov", "\u5951\u8bc3\u592b\u81ea\u52a8\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_friendly", "\u53cb\u519b\u81ea\u52a8\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_jeremy", "Jeremy 240mm\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_richard", "Richard\u706b\u7bad\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_tauon", "Tauon\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_howard", "Howard CIWS\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_sentry", "\u54e8\u6212\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_maxwell", "Maxwell\u5fae\u6ce2\u70ae\u5854");
        add("container.hbm_ntm_rebirth.turret_arty", "\u91cd\u70ae\u70ae\u5854\u201c\u683c\u96f7\u683c\u201d");
        add("container.hbm_ntm_rebirth.turret_himars", "\u706b\u7bad\u70ae\u5854\u201c\u4ea8\u5229\u201d");
        add("container.hbm_ntm_rebirth.turret_fritz", "\u91cd\u578b\u706b\u7130\u55b7\u5c04\u5668\u70ae\u5854\u201c\u5f17\u91cc\u8328\u201d");
        add("container.hbm_ntm_rebirth.turret.players", "\u73a9\u5bb6");
        add("container.hbm_ntm_rebirth.turret.friendly", "\u53cb\u65b9");
        add("container.hbm_ntm_rebirth.turret.hostile", "\u654c\u5bf9");
        add("container.hbm_ntm_rebirth.turret.machine", "\u673a\u5668");
        add("container.hbm_ntm_rebirth.turret.on", "\u5f00");
        add("container.hbm_ntm_rebirth.turret.off", "\u5173");
        add("container.hbm_ntm_rebirth.turret.none", "\u65e0");
        add("block.hbm_ntm_rebirth.machine_press", "\u706b\u529b\u953b\u538b\u673a");
        add("block.hbm_ntm_rebirth.machine_epress", "Electric Press");
        add("subtitles.hbm_ntm_rebirth.block.press_operate", "\u706b\u529b\u953b\u538b\u673a\u8fd0\u4f5c");
        add("subtitles.hbm_ntm_rebirth.block.motor", "\u7535\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.engine", "\u5f15\u64ce\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.turbine", "\u6da1\u8f6e\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.fel", "\u81ea\u7531\u7535\u5b50\u6fc0\u5149\u5668\u55e1\u9e23");
        add("subtitles.hbm_ntm_rebirth.block.electric_hum", "\u7535\u6d41\u55e1\u9e23");
        add("subtitles.hbm_ntm_rebirth.block.fusion_reactor", "\u805a\u53d8\u53cd\u5e94\u5806\u65cb\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.boiler_groan", "\u9505\u7089\u4f4e\u9e23");
        add("subtitles.hbm_ntm_rebirth.block.centrifuge", "\u79bb\u5fc3\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.turbofan", "\u6da1\u6247\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.damage", "\u673a\u5668\u9707\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.hephaestus", "\u8d6b\u83f2\u65af\u6258\u65af\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.steam_engine", "\u84b8\u6c7d\u673a\u8fd0\u4f5c");
        add("subtitles.hbm_ntm_rebirth.block.reactor_loop", "\u53cd\u5e94\u5806\u54d2\u54d2\u4f5c\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.turbinegas", "\u71c3\u6c14\u8f6e\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.assembler_operate", "\u88c5\u914d\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.assembler_strike", "\u88c5\u914d\u673a\u51b2\u51fb");
        add("subtitles.hbm_ntm_rebirth.block.assembler_start", "\u88c5\u914d\u673a\u542f\u52a8");
        add("subtitles.hbm_ntm_rebirth.block.assembler_stop", "\u88c5\u914d\u673a\u505c\u6b62");
        add("subtitles.hbm_ntm_rebirth.block.assembler_cut", "\u88c5\u914d\u673a\u5207\u5272");
        add("subtitles.hbm_ntm_rebirth.block.chemplant_operate", "\u5316\u5de5\u5382\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.chemical_plant", "\u5316\u5de5\u5382\u53cd\u5e94");
        add("subtitles.hbm_ntm_rebirth.block.pipe_placed", "\u7ba1\u9053\u653e\u7f6e");
        add("subtitles.hbm_ntm_rebirth.block.boiler", "\u9505\u7089\u8fd0\u884c");
        add("subtitles.hbm_ntm_rebirth.block.pyro_operate", "\u70ed\u89e3\u7089\u8fd0\u884c");
        add("subtitles.hbm_ntm_rebirth.block.debris", "\u788e\u5757\u6eda\u843d");
        add("subtitles.hbm_ntm_rebirth.block.sonar_ping", "\u96f7\u8fbe\u58f0\u5450\u8109\u51b2");
        add("subtitles.hbm_ntm_rebirth.block.soyuz_ready", "\u8054\u76df\u53f7\u5c31\u7eea");
        add("subtitles.hbm_ntm_rebirth.block.bobble", "\u6447\u5934\u73a9\u5076\u4e01\u5f53");
        add("subtitles.hbm_ntm_rebirth.block.broadcast", "\u5e7f\u64ad\u5668\u64ad\u653e");
        add("subtitles.hbm_ntm_rebirth.block.lever", "\u62c9\u6746\u54d0\u5f53");
        add("subtitles.hbm_ntm_rebirth.block.cover", "\u76d6\u677f\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.block.door", "\u95e8\u4f53\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.block.crate", "\u7bb1\u5b50\u5f00\u542f");
        add("subtitles.hbm_ntm_rebirth.block.diesel", "\u67f4\u6cb9\u53d1\u52a8\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.fensu", "FEnSU\u55e1\u9e23");
        add("subtitles.hbm_ntm_rebirth.block.horn", "\u53f7\u89d2\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.hunduns_magnificent_howl", "\u6df7\u6c8c\u5486\u54ee");
        add("subtitles.hbm_ntm_rebirth.block.igenerator", "\u5de5\u4e1a\u53d1\u7535\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.lock", "\u9501\u5177\u54d2\u54d2\u4f5c\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.metal_impact", "\u91d1\u5c5e\u649e\u51fb");
        add("subtitles.hbm_ntm_rebirth.block.miner", "\u91c7\u77ff\u673a\u8fd0\u8f6c");
        add("subtitles.hbm_ntm_rebirth.block.missile_assembly", "\u5bfc\u5f39\u88c5\u914d\u673a\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.block.rbmk", "RBMK\u673a\u6784\u4f5c\u52a8");
        add("subtitles.hbm_ntm_rebirth.block.reactor_stop", "\u53cd\u5e94\u5806\u505c\u6b62");
        add("subtitles.hbm_ntm_rebirth.block.screm", "\u673a\u5668\u5c16\u53eb");
        add("subtitles.hbm_ntm_rebirth.block.shutdown", "\u505c\u673a\u8b66\u62a5\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.spark", "\u706b\u82b1\u567c\u556a\u4f5c\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.squeaky_toy", "\u73a9\u5177\u5431\u5431\u4f5c\u54cd");
        add("subtitles.hbm_ntm_rebirth.block.storage", "\u50a8\u7269\u7bb1\u5f00\u542f");
        add("subtitles.hbm_ntm_rebirth.block.vault", "\u91d1\u5e93\u95e8\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.block.warn_overspeed", "\u8d85\u901f\u8b66\u62a5\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.tool.geiger", "\u76d6\u9769\u8ba1\u6570\u5668\u54d2\u54d2\u4f5c\u54cd");
        add("subtitles.hbm_ntm_rebirth.tool.tech_boop", "\u8bbe\u5907\u63d0\u793a\u97f3");
        add("subtitles.hbm_ntm_rebirth.tool.tech_bleep", "\u8d77\u7206\u5668\u63d0\u793a\u97f3");
        add("subtitles.hbm_ntm_rebirth.tool.radaway", "\u6d88\u8f90\u5b81\u6ce8\u5c04");
        add("subtitles.hbm_ntm_rebirth.step.metal", "\u91d1\u5c5e\u811a\u6b65\u58f0");
        add("subtitles.hbm_ntm_rebirth.step.iron", "\u94c1\u7532\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.step.metal_block", "\u91d1\u5c5e\u5757\u811a\u6b65\u58f0");
        add("subtitles.hbm_ntm_rebirth.step.powered", "\u52a8\u529b\u88c5\u7532\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.player.cough", "\u73a9\u5bb6\u54b3\u55fd");
        add("subtitles.hbm_ntm_rebirth.player.vomit", "\u5455\u5410");
        add("subtitles.hbm_ntm_rebirth.player.gulp", "\u73a9\u5bb6\u541e\u54bd");
        add("subtitles.hbm_ntm_rebirth.player.groan", "\u73a9\u5bb6\u547b\u541f");
        add("subtitles.hbm_ntm_rebirth.potatos.random", "PotatOS\u8bf4\u8bdd");
        add("subtitles.hbm_ntm_rebirth.misc.null", "\u76f4\u5347\u673a\u58f0\u97f3\u91cd\u5b9a\u5411");
        add("subtitles.hbm_ntm_rebirth.music.record_lambda_core", "\u5531\u7247\u64ad\u653e");
        add("subtitles.hbm_ntm_rebirth.music.record_sector_sweep", "\u5531\u7247\u64ad\u653e");
        add("subtitles.hbm_ntm_rebirth.music.record_vortal_combat", "\u5531\u7247\u64ad\u653e");
        add("subtitles.hbm_ntm_rebirth.music.transmission", "\u4f20\u8f93\u4fe1\u53f7\u64ad\u653e");
        add("subtitles.hbm_ntm_rebirth.entity.cybercrab", "\u8d5b\u535a\u87f9\u65e0\u7ebf\u7535");
        add("subtitles.hbm_ntm_rebirth.entity.ducc", "\u9e2d\u5b50\u53eb");
        add("subtitles.hbm_ntm_rebirth.entity.megaquacc", "\u5de8\u578b\u9e2d\u53eb");
        add("subtitles.hbm_ntm_rebirth.entity.siege", "\u56f4\u653b\u8f7d\u5177\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.entity.ufo_beam", "UFO\u5149\u675f\u53d1\u5c04");
        add("subtitles.hbm_ntm_rebirth.entity.ufo_blast", "\u80fd\u91cf\u653e\u7535");
        add("subtitles.hbm_ntm_rebirth.entity.slicer", "\u5200\u5203\u5207\u5272");
        add("subtitles.hbm_ntm_rebirth.entity.chopper", "\u76f4\u5347\u673a\u8f70\u9e23");
        add("subtitles.hbm_ntm_rebirth.entity.soyuz_takeoff", "\u8054\u76df\u53f7\u706b\u7bad\u53d1\u5c04");
        add("subtitles.hbm_ntm_rebirth.entity.explosion", "\u7206\u70b8\u8f70\u9e23");
        add("subtitles.hbm_ntm_rebirth.entity.rocket_takeoff", "\u706b\u7bad\u53d1\u5c04");
        add("subtitles.hbm_ntm_rebirth.entity.bomb", "\u70b8\u5f39\u54cd\u52a8");
        add("subtitles.hbm_ntm_rebirth.entity.bomber", "\u8f70\u70b8\u673a\u98de\u884c");
        add("subtitles.hbm_ntm_rebirth.entity.plane", "\u98de\u673a\u53d7\u635f");
        add("subtitles.hbm_ntm_rebirth.entity.meteorite", "\u9668\u77f3\u5760\u843d");
        add("subtitles.hbm_ntm_rebirth.turret.fire", "\u70ae\u5854\u5f00\u706b");
        add("subtitles.hbm_ntm_rebirth.turret.reload", "\u70ae\u5854\u88c5\u586b");
        add("subtitles.hbm_ntm_rebirth.turret.lockon", "\u70ae\u5854\u9501\u5b9a");
        add("subtitles.hbm_ntm_rebirth.turret.mortar_whistle", "\u8feb\u51fb\u70ae\u5f39\u547c\u5578");
        add("subtitles.hbm_ntm_rebirth.alarm.soyuzed", "\u8054\u76df\u53f7\u8b66\u62a5\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.alarm.siren", "\u8b66\u62a5\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.alarm.train_horn", "\u706b\u8f66\u6c7d\u7b1b\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.alarm.gambit", "\u5973\u516c\u7235\u7684\u5f03\u5175\u903c\u8fd1");
        add("subtitles.hbm_ntm_rebirth.alarm.chime", "\u949f\u58f0\u54cd\u8d77");
        add("subtitles.hbm_ntm_rebirth.alarm.singer", "\u6b4c\u58f0\u54cd\u8d77");
        add("subtitles.hbm_ntm_rebirth.door.transition_seal_open", "\u8fc7\u6e21\u5bc6\u5c01\u95e8\u5f00\u542f");
        add("subtitles.hbm_ntm_rebirth.door.move", "\u95e8\u4f53\u79fb\u52a8");
        add("subtitles.hbm_ntm_rebirth.door.stop", "\u95e8\u4f53\u505c\u6b62");
        add("subtitles.hbm_ntm_rebirth.door.alarm", "\u95e8\u8b66\u62a5\u9e23\u54cd");
        add("subtitles.hbm_ntm_rebirth.door.lever", "\u62c9\u6746\u54d0\u5f53");
        add("subtitles.hbm_ntm_rebirth.weapon.muke_explosion", "\u5c0f\u578b\u6838\u7206");
        add("subtitles.hbm_ntm_rebirth.weapon.explosion", "\u7206\u70b8\u8f70\u9e23");
        add("subtitles.hbm_ntm_rebirth.weapon.chainsaw", "\u94fe\u952f\u8f70\u9e23");
        add("subtitles.hbm_ntm_rebirth.weapon.flamethrower", "\u706b\u7130\u55b7\u5c04\u5668\u5f00\u706b");
        add("subtitles.hbm_ntm_rebirth.weapon.cal_shoot", "\u6b65\u67aa\u5c04\u51fb");
        add("subtitles.hbm_ntm_rebirth.weapon.tesla_shoot", "\u7279\u65af\u62c9\u6b66\u5668\u5c04\u51fb");
        add("subtitles.hbm_ntm_rebirth.weapon.hk_shoot", "\u706b\u7bad\u7b52\u5c04\u51fb");
        add("subtitles.hbm_ntm_rebirth.weapon.fire", "\u6b66\u5668\u5f00\u706b");
        add("subtitles.hbm_ntm_rebirth.weapon.reload", "\u6b66\u5668\u6362\u5f39");
        add("subtitles.hbm_ntm_rebirth.weapon.foley", "\u6b66\u5668\u64cd\u4f5c");
        add("subtitles.hbm_ntm_rebirth.weapon.action", "\u6b66\u5668\u64cd\u4f5c");
        add("subtitles.hbm_ntm_rebirth.weapon.ricochet", "\u5b50\u5f39\u8df3\u5f39");
        add("subtitles.hbm_ntm_rebirth.weapon.gbounce", "\u69b4\u5f39\u53cd\u5f39");
        add("subtitles.hbm_ntm_rebirth.weapon.casing", "\u5f39\u58f3\u843d\u5730");
        add("item.hbm_ntm_rebirth.detonator", "\u8d77\u7206\u5668");
        add("item.hbm_ntm_rebirth.demon_core_open", "\u6253\u5f00\u7684\u6076\u9b54\u6838\u5fc3");
        add("item.hbm_ntm_rebirth.demon_core_closed", "\u5c01\u95ed\u7684\u6076\u9b54\u6838\u5fc3");
        add("item.hbm_ntm_rebirth.singularity", "\u5947\u70b9");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant", "\u53ef\u63a7\u53cd\u8c10\u632f\u5947\u70b9");
        add("item.hbm_ntm_rebirth.singularity_super_heated", "\u8d85\u70ed\u5171\u632f\u5947\u70b9");
        add("item.hbm_ntm_rebirth.singularity_spark", "Spark\u5947\u70b9");
        add("item.hbm_ntm_rebirth.black_hole", "\u5fae\u578b\u9ed1\u6d1e");
        add("item.hbm_ntm_rebirth.particle_digamma", "\u00a7c\u8fea\u4f3d\u9a6c\u7c92\u5b50\u00a7r");
        add("item.hbm_ntm_rebirth.pellet_antimatter", "\u53cd\u7269\u8d28\u56e2");
        add("item.hbm_ntm_rebirth.singularity.desc.1", "\u4f60\u53ef\u80fd\u4f1a\u95ee\uff1a");
        add("item.hbm_ntm_rebirth.singularity.desc.2", "\u201c\u8fd9\u600e\u4e48\u53ef\u80fd\uff1f\u201d");
        add("item.hbm_ntm_rebirth.singularity.desc.3", "\u201c\u6211\u4e5f\u4e0d\u77e5\u9053\uff01\u201d");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant.desc.1", "\u5728\u975e\u6b27\u7a7a\u95f4\u4e2d");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant.desc.2", "\u62b5\u6d88\u7269\u4f53\u5171\u632f\uff0c");
        add("item.hbm_ntm_rebirth.singularity_counter_resonant.desc.3", "\u4ea7\u751f\u53ef\u53d8\u5f15\u529b\u4e95\u3002");
        add("item.hbm_ntm_rebirth.singularity_super_heated.desc.1", "\u901a\u8fc7\u6bcf\u4e2a\u666e\u6717\u514b\u65f6\u95f4\u7684\u5171\u632f");
        add("item.hbm_ntm_rebirth.singularity_super_heated.desc.2", "\u6301\u7eed\u52a0\u70ed\u7269\u8d28\u3002");
        add("item.hbm_ntm_rebirth.singularity_super_heated.desc.3", "\u4e0d\u53ef\u98df\u7528\u3002");
        add("item.hbm_ntm_rebirth.singularity_spark.desc.1", "\u4e00\u4e2a\u6781\u4e0d\u7a33\u5b9a\u7684\u5947\u70b9\uff0c");
        add("item.hbm_ntm_rebirth.singularity_spark.desc.2", "\u4f1a\u8109\u51b2\u5e76\u6495\u88c2\u7a7a\u95f4\u3002");
        add("item.hbm_ntm_rebirth.singularity_spark.desc.3", "\u8bf7\u5728\u8db3\u591f\u8fdc\u5904\u64cd\u4f5c\u3002");
        add("item.hbm_ntm_rebirth.black_hole.desc.1", "\u4e2d\u5fc3\u5305\u542b\u4e00\u4e2a\u5e38\u89c4\u5947\u70b9\uff0c");
        add("item.hbm_ntm_rebirth.black_hole.desc.2", "\u8db3\u4ee5\u7ef4\u6301\u7a33\u5b9a\u3002");
        add("item.hbm_ntm_rebirth.black_hole.desc.3", "\u8fd9\u8fd8\u4e0d\u662f\u4e16\u754c\u672b\u65e5\u3002");
        add("item.hbm_ntm_rebirth.particle_digamma.desc.half_particle", "\u7c92\u5b50\u534a\u8870\u671f\uff1a1.67*10^21 \u5e74");
        add("item.hbm_ntm_rebirth.particle_digamma.desc.half_player", "\u73a9\u5bb6\u534a\u8870\u671f\uff1a%s");
        add("item.hbm_ntm_rebirth.particle_digamma.desc.digamma", "%s mDRX/s");
        add("item.hbm_ntm_rebirth.pellet_antimatter.desc.1", "\u975e\u5e38\u91cd\u7684\u53cd\u7269\u8d28\u56e2\u3002");
        add("item.hbm_ntm_rebirth.pellet_antimatter.desc.2", "\u80fd\u6e05\u9664\u9ed1\u6d1e\u3002");
        add("item.hbm_ntm_rebirth.trait.drop", "[\u6389\u843d\u89e6\u53d1]");
        add("tooltip.hbm_ntm_rebirth.detonator.set", "\u6f5c\u884c\u53f3\u952e\u8bbe\u7f6e\u4f4d\u7f6e\uff0c");
        add("tooltip.hbm_ntm_rebirth.detonator.trigger", "\u53f3\u952e\u8d77\u7206\uff01");
        add("tooltip.hbm_ntm_rebirth.detonator.no_position", "\u672a\u8bbe\u7f6e\u4f4d\u7f6e\uff01");
        add("tooltip.hbm_ntm_rebirth.detonator.linked", "\u5df2\u94fe\u63a5\u5230 %s, %s, %s");
        add("msg.hbm_ntm_rebirth.detonator.position_set", "\u4f4d\u7f6e\u5df2\u8bbe\u7f6e\uff01");
        add("msg.hbm_ntm_rebirth.detonator.no_position", "\u672a\u8bbe\u7f6e\u4f4d\u7f6e\uff01");
        add("bomb.detonated", "\u6210\u529f\u5f15\u7206\uff01");
        add("bomb.incompatible", "\u8bbe\u5907\u65e0\u6cd5\u89e6\u53d1\uff01");
        add("bomb.launched", "\u53d1\u5c04\u6210\u529f\uff01");
        add("bomb.missingComponent", "\u7ec4\u4ef6\u4e22\u5931\uff01");
        add("bomb.nobomb", "\u94fe\u63a5\u4f4d\u7f6e\u4e0d\u517c\u5bb9\u6216\u5df2\u65ad\u5f00\uff01");
        add("bomb.triggered", "\u89e6\u53d1\u6210\u529f\uff01");
        add("block.hbm_ntm_rebirth.machine_difurnace_off", "\u9ad8\u7089");
        add("block.hbm_ntm_rebirth.machine_electric_furnace_off", "\u7535\u7089");
        add("block.hbm_ntm_rebirth.machine_boiler_off", "\u9505\u7089");
        add("block.hbm_ntm_rebirth.machine_shredder", "\u7c89\u788e\u673a");
        add("block.hbm_ntm_rebirth.machine_turbine", "\u84b8\u6c7d\u8f6e\u673a");
        add("block.hbm_ntm_rebirth.machine_industrial_turbine", "\u5de5\u4e1a\u84b8\u6c7d\u8f6e\u673a");
        add("block.hbm_ntm_rebirth.decon", "\u6d88\u6c61\u5668");
        add("block.hbm_ntm_rebirth.machine_armor_table", "\u88c5\u7532\u6539\u88c5\u53f0");
        add("container.armorTable", "\u88c5\u7532\u6539\u88c5\u53f0");
        add("block.hbm_ntm_rebirth.red_cable", "\u7d2b\u94dc\u7535\u7ebf");
        add("block.hbm_ntm_rebirth.red_cable_gauge", "\u529f\u7387\u8ba1");
        add("block.hbm_ntm_rebirth.cable_switch", "\u7d2b\u94dc\u7535\u7ebf\u5f00\u5173");
        add("block.hbm_ntm_rebirth.cable_detector", "\u7d2b\u94dc\u7535\u7ebf\u68c0\u6d4b\u5668");
        add("block.hbm_ntm_rebirth.cable_diode", "\u7d2b\u94dc\u4e8c\u6781\u7ba1");
        add("block.hbm_ntm_rebirth.radio_torch_sender", "\u65e0\u7ebf\u7535\u7ea2\u77f3\u53d1\u5c04\u5668");
        add("block.hbm_ntm_rebirth.radio_torch_receiver", "\u65e0\u7ebf\u7535\u7ea2\u77f3\u63a5\u6536\u5668");
        add("block.hbm_ntm_rebirth.radio_torch_counter", "\u65e0\u7ebf\u7535\u7ea2\u77f3\u8ba1\u6570\u5668");
        add("block.hbm_ntm_rebirth.radio_torch_logic", "\u65e0\u7ebf\u7535\u7ea2\u77f3\u903b\u8f91\u63a5\u6536\u5668");
        add("block.hbm_ntm_rebirth.radio_torch_reader", "\u65e0\u7ebf\u7535\u7ea2\u77f3\u8bfb\u53d6\u5668");
        add("block.hbm_ntm_rebirth.radio_torch_controller", "\u65e0\u7ebf\u7535\u7ea2\u77f3\u63a7\u5236\u5668");
        add("block.hbm_ntm_rebirth.radio_autocal", "\u81ea\u52a8\u8ba1\u7b97\u673a");
        add("block.hbm_ntm_rebirth.radio_telex", "\u7535\u62a5\u673a");
        add("block.hbm_ntm_rebirth.rbmk_display_blank", "\u65e0\u7ebf\u7ea2\u77f3\u4fe1\u53f7\u7a7a\u767d\u9762\u677f");
        add("block.hbm_ntm_rebirth.rbmk_gauge", "RBMK\u4eea\u8868\u9762\u677f");
        add("block.hbm_ntm_rebirth.rbmk_graph", "RBMK\u56fe\u8868\u9762\u677f");
        add("block.hbm_ntm_rebirth.rbmk_indicator", "RBMK\u6307\u793a\u706f\u9762\u677f");
        add("block.hbm_ntm_rebirth.rbmk_key_pad", "RBMK\u6309\u952e\u9762\u677f");
        add("block.hbm_ntm_rebirth.rbmk_lever", "RBMK\u62c9\u6746\u9762\u677f");
        add("block.hbm_ntm_rebirth.rbmk_numitron", "RBMK\u6570\u7801\u7ba1\u9762\u677f");
        add("block.hbm_ntm_rebirth.block_graphite", "\u77f3\u58a8\u5757");
        add("block.hbm_ntm_rebirth.block_graphite_drilled", "\u94bb\u5b54\u77f3\u58a8");
        add("block.hbm_ntm_rebirth.block_graphite_fuel", "\u53cd\u5e94\u5806\u71c3\u6599");
        add("block.hbm_ntm_rebirth.block_graphite_plutonium", "\u53cd\u5e94\u5806\u71c3\u6599\uff08\u589e\u6b96\uff09");
        add("block.hbm_ntm_rebirth.block_graphite_rod", "\u53cd\u5e94\u5806\u63a7\u5236\u68d2");
        add("block.hbm_ntm_rebirth.block_graphite_source", "\u53cd\u5e94\u5806\u4e2d\u5b50\u6e90");
        add("block.hbm_ntm_rebirth.block_graphite_lithium", "\u53cd\u5e94\u5806\u9502\u71c3\u6599");
        add("block.hbm_ntm_rebirth.block_graphite_tritium", "\u53cd\u5e94\u5806\u9502\u71c3\u6599\uff08\u589e\u6b96\uff09");
        add("block.hbm_ntm_rebirth.block_graphite_detector", "\u53cd\u5e94\u5806\u4e2d\u5b50\u63a2\u6d4b\u68d2");
        add("item.hbm_ntm_rebirth.rtty_pager", "RTTY\u5bfb\u547c\u673a");
        add("container.rttyPager", "RTTY\u5bfb\u547c\u673a");
        add("container.rbmkGauge", "RBMK\u4eea\u8868\u9762\u677f");
        add("container.rbmkGraph", "RBMK\u56fe\u8868\u9762\u677f");
        add("container.rbmkIndicator", "RBMK\u6307\u793a\u706f\u9762\u677f");
        add("container.rbmkKeyPad", "RBMK\u6309\u952e\u9762\u677f");
        add("container.rbmkLever", "RBMK\u62c9\u6746\u9762\u677f");
        add("container.rbmkNumitron", "RBMK\u6570\u7801\u7ba1\u9762\u677f");
        add("block.hbm_ntm_rebirth.cable_diode.desc1", "\u9650\u5236\u541e\u5410\u91cf\u5e76\u9650\u5236\u80fd\u91cf\u6d41\u5411");
        add("block.hbm_ntm_rebirth.cable_diode.desc2", "\u4f7f\u7528\u87ba\u4e1d\u5200\u63d0\u9ad8\u541e\u5410\u91cf");
        add("block.hbm_ntm_rebirth.cable_diode.desc3", "\u4f7f\u7528\u624b\u94bb\u964d\u4f4e\u541e\u5410\u91cf");
        add("block.hbm_ntm_rebirth.cable_diode.desc4", "\u4f7f\u7528\u62c6\u5f39\u5668\u5207\u6362\u7f51\u7edc\u4f18\u5148\u7ea7");
        add("block.hbm_ntm_rebirth.fluid_duct_neo", "\u6d41\u4f53\u7ba1\u9053");
        add("item.hbm_ntm_rebirth.fluid_duct", "\u6d41\u4f53\u7ba1\u9053:");
        add("block.hbm_ntm_rebirth.fluid_duct_box", "\u901a\u7528\u6d41\u4f53\u7ba1\u9053\uff08\u65b9\u5f62\uff09");
        add("block.hbm_ntm_rebirth.fluid_duct_gauge", "\u6d41\u91cf\u8ba1\u7ba1");
        add("block.hbm_ntm_rebirth.fluid_duct_exhaust", "\u6392\u6c14\u7ba1");
        add("block.hbm_ntm_rebirth.fluid_duct_paintable", "\u53ef\u6d82\u88c5\u6d41\u4f53\u7ba1\u9053");
        add("block.hbm_ntm_rebirth.fluid_duct_paintable_block_exhaust", "\u53ef\u6d82\u88c5\u6392\u6c14\u7ba1");
        add("block.hbm_ntm_rebirth.pipe_anchor", "\u7ba1\u951a");
        add("block.hbm_ntm_rebirth.fluid_valve", "\u6d41\u4f53\u9600\u95e8");
        add("block.hbm_ntm_rebirth.fluid_switch", "\u6d41\u4f53\u5f00\u5173");
        add("block.hbm_ntm_rebirth.fluid_counter_valve", "\u6d41\u4f53\u8ba1\u6570\u9600");
        add("block.hbm_ntm_rebirth.fluid_pump", "\u6d41\u4f53\u6cf5");
        add("block.hbm_ntm_rebirth.conveyor", "\u8f93\u9001\u5e26");
        add("block.hbm_ntm_rebirth.conveyor_express", "\u5feb\u901f\u8f93\u9001\u5e26");
        add("block.hbm_ntm_rebirth.conveyor_double", "\u53cc\u8f68\u9053\u8f93\u9001\u5e26");
        add("block.hbm_ntm_rebirth.conveyor_triple", "\u4e09\u8f68\u9053\u8f93\u9001\u5e26");
        add("block.hbm_ntm_rebirth.conveyor_lift", "\u5782\u76f4\u8f93\u9001\u5e26");
        add("block.hbm_ntm_rebirth.conveyor_chute", "\u8f93\u9001\u5e26\u6ed1\u69fd");
        add("block.hbm_ntm_rebirth.machine_battery", "\u84c4\u7535\u6c60\uff08\u9057\u7559\uff09");
        add("block.hbm_ntm_rebirth.machine_battery_redd", "FEnSU");
        add("block.hbm_ntm_rebirth.machine_battery_socket", "\u7535\u6c60\u63d2\u5ea7");
        add("block.hbm_ntm_rebirth.machine_radar", "\u96f7\u8fbe");
        add("block.hbm_ntm_rebirth.machine_radar_large", "\u5927\u578b\u96f7\u8fbe");
        add("block.hbm_ntm_rebirth.radar_screen", "\u96f7\u8fbe\u5c4f\u5e55");
        add("block.hbm_ntm_rebirth.machine_satlinker", "\u536b\u661fID\u7ba1\u7406\u5668");
        add("block.hbm_ntm_rebirth.sat_dock", "\u5378\u8d27\u5e73\u53f0");
        add("block.hbm_ntm_rebirth.soyuz_capsule", "\u8d27\u7269\u7740\u9646\u8231");
        add("block.hbm_ntm_rebirth.soyuz_launcher", "\u8054\u76df\u53f7\u53d1\u5c04\u5e73\u53f0");
        add("block.hbm_ntm_rebirth.launch_pad", "\u5bfc\u5f39\u53d1\u5c04\u53f0");
        add("item.hbm_ntm_rebirth.radar_linker", "\u96f7\u8fbe\u8fde\u63a5\u5668");
        add("radar.detectMissiles", "\u63a2\u6d4b\u5bfc\u5f39");
        add("radar.detectShells", "\u63a2\u6d4b\u70ae\u5f39");
        add("radar.detectPlayers", "\u63a2\u6d4b\u73a9\u5bb6");
        add("radar.smartMode", "\u667a\u80fd\u6a21\u5f0f$\u6709\u7ea2\u77f3\u4fe1\u53f7\u65f6\u5ffd\u7565\u4e0a\u5347\u6bb5\u7684\u5bfc\u5f39");
        add("radar.redMode", "\u7ea2\u77f3\u63a7\u5236\u6a21\u5f0f$\u5f00\u542f: \u57fa\u4e8e\u5bfc\u5f39\u8ddd\u79bb\u8f93\u51fa\u7ea2\u77f3\u4fe1\u53f7$\u5173\u95ed: \u57fa\u4e8e\u5bfc\u5f39\u7ea7\u522b\u8f93\u51fa\u7ea2\u77f3\u4fe1\u53f7");
        add("radar.showMap", "\u663e\u793a\u5730\u56fe");
        add("radar.toggleGui", "\u5207\u6362GUI");
        add("radar.clearMap", "\u6e05\u9664\u5730\u56fe");
        add("radar.target.abm", "\u53cd\u5f39\u9053\u5bfc\u5f39");
        add("radar.target.custom10", "10\u53f7\u5b9a\u5236\u5bfc\u5f39");
        add("radar.target.custom1015", "10/15\u53f7\u5b9a\u5236\u5bfc\u5f39");
        add("radar.target.custom15", "15\u53f7\u5b9a\u5236\u5bfc\u5f39");
        add("radar.target.custom1520", "15/20\u53f7\u5b9a\u5236\u5bfc\u5f39");
        add("radar.target.custom20", "20\u53f7\u5b9a\u5236\u5bfc\u5f39");
        add("radar.target.doomsday", "\u672b\u65e5\u5bfc\u5f39");
        add("radar.target.shuttle", "\u4e07\u91d1\u7f57\u5bbe\u822a\u5929\u98de\u673a");
        add("radar.target.tier0", "0\u7ea7\u5bfc\u5f39");
        add("radar.target.tier1", "1\u7ea7\u5bfc\u5f39");
        add("radar.target.tier2", "2\u7ea7\u5bfc\u5f39");
        add("radar.target.tier3", "3\u7ea7\u5bfc\u5f39");
        add("radar.target.tier4", "4\u7ea7\u5bfc\u5f39");
        add("container.hbm_ntm_rebirth.radar.enabled", "\u5df2\u542f\u7528");
        add("container.hbm_ntm_rebirth.radar.disabled", "\u5df2\u7981\u7528");
        add("block.hbm_ntm_rebirth.machine_assembly_machine", "\u88c5\u914d\u673a");
        add("block.hbm_ntm_rebirth.machine_chemical_plant", "\u5316\u5de5\u5382");
        add("block.hbm_ntm_rebirth.machine_chemical_factory", "\u5316\u5b66\u5de5\u5382");
        add("block.hbm_ntm_rebirth.machine_refinery", "\u77f3\u6cb9\u7cbe\u70bc\u5382");
        add("block.hbm_ntm_rebirth.machine_catalytic_cracker", "\u50ac\u5316\u88c2\u5316\u5854");
        add("block.hbm_ntm_rebirth.machine_catalytic_reformer", "\u50ac\u5316\u91cd\u6574\u5668");
        add("block.hbm_ntm_rebirth.machine_vacuum_distill", "\u771f\u7a7a\u70bc\u6cb9\u5382");
        add("block.hbm_ntm_rebirth.machine_fraction_tower", "\u5206\u998f\u5854");
        add("block.hbm_ntm_rebirth.machine_hydrotreater", "\u52a0\u6c22\u88c5\u7f6e");
        add("block.hbm_ntm_rebirth.machine_coker", "\u7126\u5316\u88c5\u7f6e");
        add("block.hbm_ntm_rebirth.machine_pyrooven", "\u70ed\u89e3\u7089");
        add("block.hbm_ntm_rebirth.machine_solidifier", "\u5de5\u4e1a\u56fa\u5316\u673a");
        add("block.hbm_ntm_rebirth.machine_compressor", "\u538b\u7f29\u673a");
        add("block.hbm_ntm_rebirth.machine_bat9000", "\u5de8\u5c3b-9000 \u50a8\u7f50");
        add("block.hbm_ntm_rebirth.machine_bigasstank", "\u5de8\u5c3b\u50a8\u7f50");
        add("block.hbm_ntm_rebirth.machine_fluidtank", "\u50a8\u7f50");
        add("block.hbm_ntm_rebirth.steel_scaffold", "\u94a2\u811a\u624b\u67b6");
        add("block.hbm_ntm_rebirth.steel_beam", "\u94a2\u6881");
        add("block.hbm_ntm_rebirth.chain", "\u94fe\u6761");
        add("block.hbm_ntm_rebirth.barrel_plastic", "\u5b89\u5168\u6876");
        add("block.hbm_ntm_rebirth.barrel_corroded", "\u88ab\u8150\u8680\u7684\u6876");
        add("block.hbm_ntm_rebirth.barrel_steel", "\u94a2\u6876");
        add("block.hbm_ntm_rebirth.barrel_tcalloy", "\u953d-\u94a2\u5408\u91d1\u6876");
        add("block.hbm_ntm_rebirth.barrel_antimatter", "\u78c1\u7ea6\u675f\u53cd\u7269\u8d28\u5bb9\u5668");
        add("block.hbm_ntm_rebirth.machine_pumpjack", "\u77f3\u6cb9\u94bb\u673a");
        add("block.hbm_ntm_rebirth.machine_well", "\u94bb\u6cb9\u5854");
        add("block.hbm_ntm_rebirth.machine_fracking_tower", "\u6db2\u538b\u7834\u88c2\u5854");
        add("block.hbm_ntm_rebirth.machine_centrifuge", "\u79bb\u5fc3\u673a");
        add("block.hbm_ntm_rebirth.machine_gascent", "\u6c14\u4f53\u79bb\u5fc3\u673a");
        add("block.hbm_ntm_rebirth.machine_ore_slopper", "\u77ff\u77f3\u7ffb\u6599\u673a");
        add("block.hbm_ntm_rebirth.machine_sawmill", "\u65af\u7279\u6797\u952f\u6728\u673a");
        add("block.hbm_ntm_rebirth.machine_crucible", "\u5769\u57da");
        add("block.hbm_ntm_rebirth.machine_flare", "\u6c14\u4f53\u706b\u70ac");
        add("block.hbm_ntm_rebirth.chimney_brick", "\u70df\u56f1");
        add("block.hbm_ntm_rebirth.chimney_industrial", "\u5de5\u4e1a\u70df\u56f1");
        add("block.hbm_ntm_rebirth.machine_intake", "\u8fdb\u6c14\u53e3");
        add("block.hbm_ntm_rebirth.machine_drain", "\u6392\u6db2\u7ba1");
        add("block.hbm_ntm_rebirth.machine_chungus", "\u201c\u5229\u7ef4\u5766\u201d\u5de8\u578b\u6c7d\u8f6e\u673a");
        add("block.hbm_ntm_rebirth.machine_hephaestus", "\u5730\u70ed\u6362\u70ed\u5668");
        add("block.hbm_ntm_rebirth.machine_boiler", "\u5927\u578b\u9505\u7089");
        add("block.hbm_ntm_rebirth.machine_industrial_boiler", "\u5de5\u4e1a\u9505\u7089");
        add("block.hbm_ntm_rebirth.machine_combustion_engine", "\u5de5\u4e1a\u5185\u71c3\u673a");
        add("block.hbm_ntm_rebirth.pump_steam", "\u84b8\u6c7d\u52a8\u529b\u5730\u4e0b\u6c34\u6cf5");
        add("block.hbm_ntm_rebirth.pump_electric", "\u7535\u52a8\u5730\u4e0b\u6c34\u6cf5");
        add("block.hbm_ntm_rebirth.heater_heatex", "\u6362\u70ed\u52a0\u70ed\u5668");
        add("block.hbm_ntm_rebirth.heater_firebox", "\u71c3\u70e7\u5ba4");
        add("block.hbm_ntm_rebirth.heater_oven", "\u52a0\u70ed\u7089");
        add("block.hbm_ntm_rebirth.machine_ashpit", "\u7070\u5751");
        add("block.hbm_ntm_rebirth.heater_oilburner", "\u6d41\u4f53\u71c3\u70e7\u5668");
        add("block.hbm_ntm_rebirth.heater_electric", "\u7535\u52a0\u70ed\u5668");
        add("block.hbm_ntm_rebirth.machine_condenser_powered", "\u5927\u529f\u7387\u84b8\u6c7d\u51b7\u51dd\u5668");
        add("block.hbm_ntm_rebirth.machine_compressor_compact", "Compact Compressor");
        add("block.hbm_ntm_rebirth.machine_lpw2", "LPW2");
        add("block.hbm_ntm_rebirth.machine_assembly_factory", "\u5927\u578b\u88c5\u914d\u5382");
        add("block.hbm_ntm_rebirth.machine_purex", "\u94b8\u94c0\u8fd8\u539f\u63d0\u53d6\u8bbe\u5907\uff08PUREX\uff09");
        add("block.hbm_ntm_rebirth.machine_silex", "SILEX\u6fc0\u5149\u540c\u4f4d\u7d20\u5206\u79bb\u5ba4");
        add("block.hbm_ntm_rebirth.machine_crystallizer", "\u77ff\u7269\u9178\u5316\u5668");
        add("block.hbm_ntm_rebirth.machine_electrolyser", "\u7535\u89e3\u673a");
        add("block.hbm_ntm_rebirth.machine_exposure_chamber", "\u8f90\u7167\u8231");
        add("block.hbm_ntm_rebirth.machine_cyclotron", "\u56de\u65cb\u52a0\u901f\u5668");
        add("block.hbm_ntm_rebirth.machine_arc_welder", "\u7535\u5f27\u710a\u673a");
        add("block.hbm_ntm_rebirth.machine_soldering_station", "\u710a\u63a5\u53f0");
        add("block.hbm_ntm_rebirth.machine_mixer", "\u5de5\u4e1a\u6405\u62cc\u673a");
        add("block.hbm_ntm_rebirth.machine_radiolysis", "\u8f90\u5c04\u5206\u89e3\u5ba4");
        add("block.hbm_ntm_rebirth.machine_radgen", "\u653e\u5c04\u6027\u540c\u4f4d\u7d20\u53d1\u7535\u673a");
        add("block.hbm_ntm_rebirth.machine_rotary_furnace", "\u56de\u8f6c\u7089");
        add("block.hbm_ntm_rebirth.machine_steam_engine", "\u84b8\u6c7d\u673a");
        add("block.hbm_ntm_rebirth.machine_solar_boiler", "\u592a\u9633\u80fd\u9505\u7089");
        add("block.hbm_ntm_rebirth.machine_tower_small", "\u5c0f\u578b\u51b7\u5374\u5854");
        add("block.hbm_ntm_rebirth.machine_tower_large", "\u5927\u578b\u51b7\u5374\u5854");
        add("block.hbm_ntm_rebirth.machine_turbofan", "\u6da1\u6247\u53d1\u52a8\u673a");
        add("block.hbm_ntm_rebirth.machine_turbinegas", "\u71c3\u6c14\u8f6e\u673a");
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
        add("block.hbm_ntm_rebirth.glass_boron", "\u787c\u73bb\u7483");
        add("block.hbm_ntm_rebirth.glass_lead", "\u94c5\u73bb\u7483");
        add("block.hbm_ntm_rebirth.sand_boron", "\u787c\u6c99");
        add("block.hbm_ntm_rebirth.sand_lead", "\u94c5\u6c99");
        add("block.hbm_ntm_rebirth.sand_uranium", "\u94c0\u6c99");
        add("block.hbm_ntm_rebirth.sand_polonium", "\u948b\u6c99");
        add("block.hbm_ntm_rebirth.sand_quartz", "\u77f3\u82f1\u6c99");
        add("block.hbm_ntm_rebirth.moon_turf", "\u6708\u58e4");
        add("block.hbm_ntm_rebirth.reinforced_laminate", "\u5f3a\u5316\u5939\u5c42\u73bb\u7483");
        add("block.hbm_ntm_rebirth.reinforced_laminate_pane", "\u5f3a\u5316\u5939\u5c42\u9694\u677f");
        add("container.machineAssemblyMachine", "\u88c5\u914d\u673a");
        add("container.machineArcWelder", "\u7535\u5f27\u710a\u673a");
        add("container.machineChemicalPlant", "\u5316\u5de5\u5382");
        add("container.machineLiquefactor", "\u6db2\u5316\u673a");
        add("container.satLinker", "\u536b\u661fID\u7ba1\u7406");
        add("container.hbm_ntm_rebirth.sat_linker", "\u536b\u661fID\u7ba1\u7406");
        add("container.hbm_ntm_rebirth.sat_linker.copy.0", "\u7b2c\u4e00\u4e2a\u69fd\u4f4d\u4f1a\u590d\u5236\u536b\u661f/\u82af\u7247\u7684");
        add("container.hbm_ntm_rebirth.sat_linker.copy.1", "\u9891\u7387\u5e76\u7c98\u8d34\u5230\u7b2c\u4e8c\u4e2a\u69fd\u4f4d\u3002");
        add("container.hbm_ntm_rebirth.sat_linker.randomize.0", "\u7b2c\u4e09\u4e2a\u69fd\u4f4d\u4f1a\u968f\u673a\u5316");
        add("container.hbm_ntm_rebirth.sat_linker.randomize.1", "\u536b\u661f/\u82af\u7247\u7684\u9891\u7387\u3002");
        add("container.satDock", "\u5378\u8d27\u5e73\u53f0");
        add("container.hbm_ntm_rebirth.sat_dock", "\u5378\u8d27\u5e73\u53f0");
        add("container.hbm_ntm_rebirth.sat_dock.info.0", "\u9700\u8981\u5df2\u94fe\u63a5\u7684\u77ff\u4e1a\u536b\u661f\u82af\u7247\u3002");
        add("container.hbm_ntm_rebirth.sat_dock.info.1", "\u8d27\u8fd0\u98de\u8239\u4f1a\u5b9a\u671f\u964d\u843d\uff0c");
        add("container.hbm_ntm_rebirth.sat_dock.info.2", "\u5e76\u6295\u9012\u8d44\u6e90\u3002");
        add("container.hbm_ntm_rebirth.ashpit", "\u7070\u5751");
        add("container.hbm_ntm_rebirth.soyuz_capsule", "\u8d27\u7269\u7740\u9646\u8231");
        add("container.hbm_ntm_rebirth.soyuz_launcher", "\u8054\u76df\u53f7\u53d1\u5c04\u5e73\u53f0");
        add("container.hbm_ntm_rebirth.launch_pad", "\u5bfc\u5f39\u53d1\u5c04\u53f0");
        add("gui.hbm_ntm_rebirth.launch_pad.not_ready", "\u672a\u5c31\u7eea");
        add("gui.hbm_ntm_rebirth.launch_pad.loading", "\u88c5\u586b\u4e2d...");
        add("gui.hbm_ntm_rebirth.launch_pad.ready", "\u5c31\u7eea");
        add("container.hbm_ntm_rebirth.battery", "\u84c4\u7535\u6c60");
        add("container.batterySocket", "\u7535\u6c60\u63d2\u5ea7");
        add("container.hbm_ntm_rebirth.battery.red_low", "\u4f4e\u7ea2\u77f3\u6a21\u5f0f");
        add("container.hbm_ntm_rebirth.battery.red_high", "\u9ad8\u7ea2\u77f3\u6a21\u5f0f");
        add("container.hbm_ntm_rebirth.battery.mode.input", "\u8f93\u5165");
        add("container.hbm_ntm_rebirth.battery.mode.buffer", "\u7f13\u51b2");
        add("container.hbm_ntm_rebirth.battery.mode.output", "\u8f93\u51fa");
        add("container.hbm_ntm_rebirth.battery.mode.none", "\u505c\u7528");
        add("container.hbm_ntm_rebirth.battery.priority", "\u7f51\u7edc\u4f18\u5148\u7ea7");
        add("container.hbm_ntm_rebirth.battery.priority.low", "\u4f4e");
        add("container.hbm_ntm_rebirth.battery.priority.normal", "\u666e\u901a");
        add("container.hbm_ntm_rebirth.battery.priority.high", "\u9ad8");
        add("container.hbm_ntm_rebirth.battery.priority.recommended", "\u63a8\u8350\uff1a\u4f4e");
        HbmFluidLangEntries.addChinese(this::add);
        add("container.fluidtank", "\u6d41\u4f53\u50a8\u7f50");
        add("container.bat9000", "\u5de8\u5c3b-9000 \u50a8\u7f50");
        add("container.barrel", "\u6d41\u4f53\u6876");
        add("container.bigAssTank", "\u5de8\u5c3b\u50a8\u7f50");
        add("container.gasFlare", "\u6c14\u4f53\u706b\u70ac");
        add("container.fluidtank.mode", "\u6a21\u5f0f");
        add("container.fluidtank.mode.input", "\u8f93\u5165");
        add("container.fluidtank.mode.buffer", "\u7f13\u51b2");
        add("container.fluidtank.mode.output", "\u8f93\u51fa");
        add("container.fluidtank.mode.none", "\u505c\u7528");
        add("container.fluidtank.damaged", "\u635f\u574f");
        add("container.fluidtank.burning", "\u71c3\u70e7");
        add("barrel.tooltip.capacity.6000", "\u5bb9\u91cf\uff1a6,000mB");
        add("barrel.tooltip.capacity.12000", "\u5bb9\u91cf\uff1a12,000mB");
        add("barrel.tooltip.capacity.16000", "\u5bb9\u91cf\uff1a16,000mB");
        add("barrel.tooltip.capacity.24000", "\u5bb9\u91cf\uff1a24,000mB");
        add("barrel.tooltip.no_hot", "\u4e0d\u80fd\u50a8\u5b58\u9ad8\u6e29\u6d41\u4f53");
        add("barrel.tooltip.no_corrosive", "\u4e0d\u80fd\u50a8\u5b58\u8150\u8680\u6027\u6d41\u4f53");
        add("barrel.tooltip.no_antimatter", "\u4e0d\u80fd\u50a8\u5b58\u53cd\u7269\u8d28");
        add("barrel.tooltip.can_hot", "\u53ef\u50a8\u5b58\u9ad8\u6e29\u6d41\u4f53");
        add("barrel.tooltip.can_corrosive", "\u53ef\u50a8\u5b58\u8150\u8680\u6027\u6d41\u4f53");
        add("barrel.tooltip.can_high_corrosive", "\u53ef\u50a8\u5b58\u5f3a\u8150\u8680\u6027\u6d41\u4f53");
        add("barrel.tooltip.can_antimatter", "\u53ef\u50a8\u5b58\u53cd\u7269\u8d28");
        add("barrel.tooltip.no_high_corrosive_properly", "\u4e0d\u80fd\u59a5\u5584\u50a8\u5b58\u5f3a\u8150\u8680\u6027\u6d41\u4f53");
        add("barrel.tooltip.leaky", "\u4f1a\u6cc4\u6f0f");
        add("block.hbm_ntm_rebirth.gas_meltdown", "\u7194\u6bc1\u6c14\u4f53");
        add("block.hbm_ntm_rebirth.gas_monoxide", "\u4e00\u6c27\u5316\u78b3");
        add("block.hbm_ntm_rebirth.gas_asbestos", "\u77f3\u68c9\u7c89\u5c18");
        add("block.hbm_ntm_rebirth.gas_coal", "\u7164\u5c18");
        add("block.hbm_ntm_rebirth.chlorine_gas", "\u6c2f\u6c14");
        add("block.hbm_ntm_rebirth.dirt_dead", "\u6ce5\u6e23");
        add("block.hbm_ntm_rebirth.dirt_oily", "\u6cb9\u6ce5");
        add("block.hbm_ntm_rebirth.sand_dirty", "\u6cb9\u7802");
        add("block.hbm_ntm_rebirth.sand_dirty_red", "\u7ea2\u8272\u6cb9\u7802");
        add("block.hbm_ntm_rebirth.stone_cracked", "\u7834\u788e\u7684\u77f3\u5934");
        add("death.attack.monoxide", "%1$s\u6b7b\u4e8e\u4e00\u6c27\u5316\u78b3\u4e2d\u6bd2");
        add("death.attack.taint", "%1$s\u88ab\u6c61\u79fd\u541e\u566c");
        add("death.attack.electric", "%1$s\u88ab\u7535\u51fb\u81f4\u6b7b");
        add("death.attack.shrapnel", "%1$s\u88ab\u5f39\u7247\u6495\u788e");
        add("death.attack.rubble", "%1$s\u88ab\u98de\u6563\u74e6\u783e\u7838\u6b7b");
        add("death.attack.blackhole", "%1$s\u88ab\u9ed1\u6d1e\u541e\u566c");
        add("block.hbm_ntm_rebirth.rad_absorber", "\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm_ntm_rebirth.rad_absorber.1", "\u7ea2\u8272\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm_ntm_rebirth.rad_absorber.2", "\u7eff\u8272\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm_ntm_rebirth.rad_absorber.3", "\u7c89\u8272\u8f90\u5c04\u5438\u6536\u5668");
        add("block.hbm_ntm_rebirth.dummy_block", "\u865a\u62df\u65b9\u5757");
        add("block.hbm_ntm_rebirth.waste_earth", "\u5e9f\u571f");
        add("block.hbm_ntm_rebirth.waste_mycelium", "\u5e9f\u571f\u83cc\u4e1d");
        add("block.hbm_ntm_rebirth.waste_leaves", "\u5e9f\u571f\u6811\u53f6");
        add("block.hbm_ntm_rebirth.waste_log", "\u5e9f\u571f\u539f\u6728");
        add("block.hbm_ntm_rebirth.waste_planks", "\u5e9f\u571f\u6728\u677f");
        add("block.hbm_ntm_rebirth.frozen_grass", "\u51b0\u51bb\u8349");
        add("block.hbm_ntm_rebirth.frozen_dirt", "\u51b0\u51bb\u571f");
        add("block.hbm_ntm_rebirth.frozen_log", "\u51b0\u51bb\u539f\u6728");
        add("block.hbm_ntm_rebirth.frozen_planks", "\u51b0\u51bb\u6728\u677f");
        add("block.hbm_ntm_rebirth.leaves_layer", "\u843d\u53f6");
        add("block.hbm_ntm_rebirth.balefire", "\u91ce\u706b");
        add("block.hbm_ntm_rebirth.sellafield", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield.1", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield.2", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield.3", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield.4", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield.5", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield_slaked", "\u6d88\u6c89\u7684\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u7269");
        add("block.hbm_ntm_rebirth.sellafield_bedrock", "\u653e\u5c04\u6027\u7194\u878d\u51dd\u56fa\u57fa\u5ca9");
        add("block.hbm_ntm_rebirth.ore_sellafield_diamond", "\u653e\u5c04\u6027\u7194\u878d\u94bb\u77f3\u77ff");
        add("block.hbm_ntm_rebirth.ore_sellafield_emerald", "\u653e\u5c04\u6027\u7194\u878d\u7eff\u5b9d\u77f3\u77ff");
        add("block.hbm_ntm_rebirth.ore_sellafield_radgem", "\u5bcc\u542b\u5b9d\u77f3\u7684\u653e\u5c04\u6027\u7194\u878d\u7269");
        add("block.hbm_ntm_rebirth.ore_sellafield_schrabidium", "\u653e\u5c04\u6027\u7194\u878dSa326\u77ff\u77f3");
        add("block.hbm_ntm_rebirth.ore_sellafield_uranium_scorched", "\u653e\u5c04\u6027\u7194\u878d\u70e7\u7126\u94c0\u77ff");
        add("block.hbm_ntm_rebirth.waste_trinitite", "\u6838\u878d\u73bb\u7483\u77ff\u77f3");
        add("block.hbm_ntm_rebirth.waste_trinitite_red", "\u7ea2\u8272\u6838\u878d\u73bb\u7483\u77ff\u77f3");
        add("block.hbm_ntm_rebirth.glass_trinitite", "\u6838\u878d\u73bb\u7483");
        add("block.hbm_ntm_rebirth.ash_digamma", "\u7070\u70ec");
        add("block.hbm_ntm_rebirth.fire_digamma", "\u6325\u4e4b\u4e0d\u53bb\u7684\u8fea\u4f3d\u9a6c\u4e4b\u706b");
        add("block.hbm_ntm_rebirth.pribris_digamma", "\u53d1\u9ed1\u7684RBMK\u53cd\u5e94\u5806\u6b8b\u9ab8");
        add("block.hbm_ntm_rebirth.volcanic_lava_block", "\u706b\u5c71\u7194\u5ca9");
        add("block.hbm_ntm_rebirth.rad_lava_block", "\u653e\u5c04\u6027\u706b\u5c71\u7194\u5ca9");
        add("block.hbm_ntm_rebirth.volcano_core", "\u706b\u5c71\u6838\u5fc3");
        add("block.hbm_ntm_rebirth.volcano_rad_core", "\u653e\u5c04\u6027\u706b\u5c71\u6838\u5fc3");
        add("block.hbm_ntm_rebirth.mud_block", "\u6bd2\u6ce5");
        add("block.hbm_ntm_rebirth.tektite", "\u7194\u878d\u77f3");
        add("block.hbm_ntm_rebirth.ore_tektite_osmiridium", "\u6e17\u9507\u7194\u878d\u77f3");
        add("block.hbm_ntm_rebirth.crystal_virus", "\u9ed1\u6c34\u6676");
        add("block.hbm_ntm_rebirth.crystal_hardened", "\u786c\u5316\u9ed1\u6c34\u6676");
        add("block.hbm_ntm_rebirth.glyphid_spawner", "\u5f02\u866b\u8702\u5de2\u7e41\u6b96\u65b9\u5757");
        add("block.hbm_ntm_rebirth.nuke_gadget", "\u5c0f\u73a9\u610f");
        add("block.hbm_ntm_rebirth.nuke_boy", "\u5c0f\u7537\u5b69");
        add("block.hbm_ntm_rebirth.nuke_man", "\u80d6\u5b50");
        add("block.hbm_ntm_rebirth.nuke_tsar", "\u6c99\u7687\u70b8\u5f39");
        add("block.hbm_ntm_rebirth.nuke_mike", "\u5e38\u6625\u85e4\u8fc8\u514b");
        add("block.hbm_ntm_rebirth.nuke_prototype", "\u539f\u578b");
        add("block.hbm_ntm_rebirth.nuke_fleija", "F.L.E.I.J.A.");
        add("block.hbm_ntm_rebirth.nuke_solinium", "\u851a\u84dd\u6d17\u793c");
        add("block.hbm_ntm_rebirth.nuke_n2", "N2\u70b8\u5f39");
        add("block.hbm_ntm_rebirth.nuke_custom", "\u81ea\u5b9a\u4e49\u6838\u5f39");
        add("block.hbm_ntm_rebirth.nuke_fstbmb", "\u91ce\u706b\u70b8\u5f39");
        add("block.hbm_ntm_rebirth.bomb_multi", "\u591a\u529f\u80fd\u70b8\u5f39");
        add("container.nukeCustom", "\u81ea\u5b9a\u4e49\u6838\u5f39");
        add("item.hbm_ntm_rebirth.custom_tnt", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u70b8\u836f");
        add("item.hbm_ntm_rebirth.custom_nuke", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u94c0\u68d2");
        add("item.hbm_ntm_rebirth.custom_hydro", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u6c22\u68d2");
        add("item.hbm_ntm_rebirth.custom_amat", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u53cd\u7269\u8d28\u68d2");
        add("item.hbm_ntm_rebirth.custom_dirty", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u6838\u5e9f\u6599\u68d2");
        add("item.hbm_ntm_rebirth.custom_schrab", "\u81ea\u5b9a\u4e49\u6838\u5f39-Sa326\u68d2");
        add("item.hbm_ntm_rebirth.custom_fall", "\u81ea\u5b9a\u4e49\u6838\u5f39-\u6389\u843d\u5347\u7ea7");
        add("item.hbm_ntm_rebirth.custom_fall.desc", "\u4f7f\u70b8\u5f39\u5728\u6fc0\u6d3b\u65f6\u4e0b\u843d");
        add("subtitles.hbm_ntm_rebirth.weapon.fstbmb", "\u91ce\u706b\u70b8\u5f39\u4fe1\u53f7");
        add("subtitles.hbm_ntm_rebirth.weapon.nuclear_explosion", "\u6838\u7206");
        add("subtitles.hbm_ntm_rebirth.weapon.rocket_flame", "\u706b\u7bad\u706b\u7130");
        add("block.hbm_ntm_rebirth.dynamite", "\u70b8\u836f");
        add("block.hbm_ntm_rebirth.tnt_ntm", "\u8d27\u771f\u4ef7\u5b9e\u7684TNT");
        add("block.hbm_ntm_rebirth.semtex", "\u585e\u59c6\u6c40\u5851\u80f6\u70b8\u836f");
        add("block.hbm_ntm_rebirth.c4", "C-4");
        add("block.hbm_ntm_rebirth.det_cord", "\u70b8\u836f\u5f15\u4fe1");
        add("block.hbm_ntm_rebirth.det_charge", "\u70b8\u836f");
        add("block.hbm_ntm_rebirth.det_nuke", "\u6838\u70b8\u836f");
        add("block.hbm_ntm_rebirth.charge_dynamite", "\u5b9a\u65f6\u70b8\u5f39");
        add("block.hbm_ntm_rebirth.charge_miner", "\u5b9a\u65f6\u91c7\u77ff\u70b8\u836f");
        add("block.hbm_ntm_rebirth.charge_c4", "\u70b8\u836f\u5305");
        add("block.hbm_ntm_rebirth.charge_semtex", "\u585e\u59c6\u6c40\u91c7\u77ff\u70b8\u836f");
        add("tooltip.hbm_ntm_rebirth.charge.timer", "\u53f3\u952e\u66f4\u6539\u8ba1\u65f6\u5668\u3002");
        add("tooltip.hbm_ntm_rebirth.charge.arm", "\u6f5c\u884c\u53f3\u952e\u4ee5\u6b66\u88c5\u3002");
        add("tooltip.hbm_ntm_rebirth.charge.defuser", "\u53ea\u80fd\u7528\u62c6\u5f39\u5668\u89e3\u9664\u5e76\u79fb\u9664\u3002");
        add("tooltip.hbm_ntm_rebirth.charge.all_drop", "\u5c06\u6389\u843d\u6240\u6709\u65b9\u5757\u3002");
        add("tooltip.hbm_ntm_rebirth.charge.no_damage", "\u4e0d\u9020\u6210\u4f24\u5bb3\u3002");
        add("tooltip.hbm_ntm_rebirth.charge.no_drop", "\u4e0d\u6389\u843d\u65b9\u5757\u3002");
        add("tooltip.hbm_ntm_rebirth.charge.fortune", "\u65f6\u8fd0 III");
        add("block.hbm_ntm_rebirth.red_barrel", "\u7ea2\u8272\u71c3\u6cb9\u6876");
        add("block.hbm_ntm_rebirth.pink_barrel", "\u7c89\u8272\u71c3\u6cb9\u6876");
        add("block.hbm_ntm_rebirth.lox_barrel", "\u6db2\u6c27\u6876");
        add("block.hbm_ntm_rebirth.taint", "\u8150\u8d28");
        add("block.hbm_ntm_rebirth.taint_barrel", "IMP\u6b8b\u6e23\u6876");
        add("block.hbm_ntm_rebirth.yellow_barrel", "\u6838\u5e9f\u6599\u6876");
        add("block.hbm_ntm_rebirth.vitrified_barrel", "\u73bb\u7483\u5316\u6838\u5e9f\u6599\u6876");
        add("item.hbm_ntm_rebirth.powder_tektite", "\u7194\u878d\u77f3\u7c89");
        add("item.hbm_ntm_rebirth.powder_coal", "\u7164\u7c89");
        add("item.hbm_ntm_rebirth.powder_coal_tiny", "\u5c0f\u64ae\u7164\u7c89");
        add("item.hbm_ntm_rebirth.coke_coal", "\u7164\u7126\u70ad");
        add("item.hbm_ntm_rebirth.coke_lignite", "\u8910\u7164\u7126\u70ad");
        add("item.hbm_ntm_rebirth.coke_petroleum", "\u77f3\u6cb9\u7126\u70ad");
        add("item.hbm_ntm_rebirth.briquette_coal", "\u7164\u7403");
        add("item.hbm_ntm_rebirth.briquette_lignite", "\u8910\u7164\u7164\u7403");
        add("item.hbm_ntm_rebirth.briquette_wood", "\u6728\u5c51\u7403");
        add("item.hbm_ntm_rebirth.oil_tar_crude", "\u7126\u6cb9");
        add("item.hbm_ntm_rebirth.oil_tar_crack", "\u88c2\u5316\u7126\u6cb9");
        add("item.hbm_ntm_rebirth.oil_tar_coal", "\u7164\u7126\u6cb9");
        add("item.hbm_ntm_rebirth.oil_tar_wood", "\u6728\u998f\u6cb9");
        add("item.hbm_ntm_rebirth.oil_tar_wax", "\u6c2f\u5316\u77f3\u8721");
        add("item.hbm_ntm_rebirth.oil_tar_paraffin", "\u77f3\u8721");
        add("item.hbm_ntm_rebirth.powder_ash_wood", "\u6728\u7070");
        add("item.hbm_ntm_rebirth.powder_ash_coal", "\u7164\u7070");
        add("item.hbm_ntm_rebirth.powder_ash_misc", "\u7070\u5c18");
        add("item.hbm_ntm_rebirth.powder_ash_fly", "\u98de\u5c18");
        add("item.hbm_ntm_rebirth.powder_ash_soot", "\u7ec6\u70df\u7070");
        add("item.hbm_ntm_rebirth.powder_ash_fullerene", "\u5bcc\u52d2\u70ef");
        add("item.hbm_ntm_rebirth.chunk_ore_rare", "\u7a00\u571f\u77ff\u77f3\u5757");
        add("item.hbm_ntm_rebirth.chunk_ore_malachite", "\u5b54\u96c0\u77f3\u5757");
        add("item.hbm_ntm_rebirth.chunk_ore_cryolite", "\u51b0\u6676\u77f3\u5757");
        add("item.hbm_ntm_rebirth.chunk_ore_moonstone", "\u6708\u957f\u77f3");
        add("item.hbm_ntm_rebirth.plant_item_tobacco", "\u70df\u53f6");
        add("item.hbm_ntm_rebirth.plant_item_rope", "\u9ebb\u7ef3");
        add("item.hbm_ntm_rebirth.plant_item_mustardwillow", "\u82a5\u5b50\u67f3\u53f6");
        add("item.hbm_ntm_rebirth.parts_legendary_tier1", "\u4f20\u5947\u96f6\u4ef6");
        add("item.hbm_ntm_rebirth.parts_legendary_tier2", "\u4f20\u5947\u96f6\u4ef6");
        add("item.hbm_ntm_rebirth.parts_legendary_tier3", "\u4f20\u5947\u96f6\u4ef6");
        add("item.hbm_ntm_rebirth.part_generic_piston_pneumatic", "\u6c14\u52a8\u6d3b\u585e");
        add("item.hbm_ntm_rebirth.part_generic_piston_hydraulic", "\u6db2\u538b\u6d3b\u585e");
        add("item.hbm_ntm_rebirth.part_generic_piston_electric", "\u7535\u52a8\u6d3b\u585e");
        add("item.hbm_ntm_rebirth.part_generic_lde", "\u4f4e\u5bc6\u5ea6\u5143\u4ef6");
        add("item.hbm_ntm_rebirth.part_generic_hde", "\u91cd\u578b\u5143\u4ef6");
        add("item.hbm_ntm_rebirth.part_generic_glass_polarized", "\u504f\u5149\u955c\u7247");
        add("item.hbm_ntm_rebirth.item_expensive.desc", "Expensive mode item");
        add("item.hbm_ntm_rebirth.item_expensive_steel_plating", "\u94c6\u63a5\u56fa\u5b9a\u94a2\u677f");
        add("item.hbm_ntm_rebirth.item_expensive_heavy_frame", "\u91cd\u578b\u6846\u67b6");
        add("item.hbm_ntm_rebirth.item_expensive_circuit", "\u5927\u578b\u7535\u8def\u677f");
        add("item.hbm_ntm_rebirth.item_expensive_lead_plating", "\u9632\u8f90\u5c04\u9540\u5c42");
        add("item.hbm_ntm_rebirth.item_expensive_ferro_plating", "\u5f3a\u5316\u94c0\u94c1\u5408\u91d1\u677f");
        add("item.hbm_ntm_rebirth.item_expensive_computer", "\u5904\u7406\u5668\u4e3b\u673a");
        add("item.hbm_ntm_rebirth.item_expensive_bronze_tubes", "\u9752\u94dc\u7ed3\u6784\u4ef6");
        add("item.hbm_ntm_rebirth.item_expensive_plastic", "\u5851\u6599\u677f");
        add("item.hbm_ntm_rebirth.item_expensive_gold_dust", "\u8d85\u7cbe\u7ec6\u91d1\u7c89");
        add("item.hbm_ntm_rebirth.item_expensive_degenerate_matter", "\u7b80\u5e76\u6001\u7269\u8d28");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_iron", "\u94c1\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_copper", "\u94dc\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_lithium", "\u9502\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_silicon", "\u7845\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_lead", "\u94c5\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_titanium", "\u949b\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_aluminium", "\u94dd\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_sulfur", "\u786b\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_calcium", "\u9499\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_bismuth", "\u94cb\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_radium", "\u956d\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_technetium", "\u951d\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_polonium", "\u948b\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.ore_byproduct_b_uranium", "\u94c0\u6676\u4f53\u788e\u7247");
        add("item.hbm_ntm_rebirth.casing_small", "\u5c0f\u53e3\u5f84\u94dc\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.casing_large", "\u5927\u53e3\u5f84\u94dc\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.casing_small_steel", "\u5c0f\u53e3\u5f84\u94a2\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.casing_large_steel", "\u5927\u53e3\u5f84\u94a2\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.casing_shotshell", "\u9ed1\u706b\u836f\u9730\u5f39\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.casing_buckshot", "\u5851\u6599\u9730\u5f39\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.casing_buckshot_advanced", "\u9ad8\u7ea7\u9730\u5f39\u5f39\u58f3");
        add("item.hbm_ntm_rebirth.ingot_weaponsteel", "\u6b66\u5668\u7ea7\u94a2\u952d");
        add("item.hbm_ntm_rebirth.plate_weaponsteel", "\u6b66\u5668\u7ea7\u94a2\u677f");
        add("item.hbm_ntm_rebirth.mechanism_gunmetal", "\u70ae\u94dc\u673a\u6784\u4ef6");
        add("item.hbm_ntm_rebirth.mechanism_weaponsteel", "\u6b66\u5668\u7ea7\u94a2\u673a\u6784\u4ef6");
        add("item.hbm_ntm_rebirth.mechanism_saturnite", "\u571f\u661f\u77f3\u673a\u6784\u4ef6");
        add("item.hbm_ntm_rebirth.ingot_dura_steel", "\u9ad8\u901f\u94a2\u952d");
        add("item.hbm_ntm_rebirth.plate_schrabidium", "Sa326\u677f");
        add("item.hbm_ntm_rebirth.plate_combine_steel", "CMB\u94a2\u677f");
        add("item.hbm_ntm_rebirth.plate_saturnite", "\u571f\u661f\u677f");
        add("item.hbm_ntm_rebirth.fuel_additive_antiknock", "\u56db\u4e59\u57fa\u94c5\u6297\u7206\u5242");
        add("item.hbm_ntm_rebirth.fuel_additive_deicer", "\u9664\u51b0\u5242");
        add("item.hbm_ntm_rebirth.catalytic_converter", "\u50ac\u5316\u8f6c\u5316\u5668");
        add("item.hbm_ntm_rebirth.powder_lignite", "\u8910\u7164\u7c89");
        add("item.hbm_ntm_rebirth.powder_quartz", "\u77f3\u82f1\u7c89");
        add("item.hbm_ntm_rebirth.powder_boron", "\u787c\u7c89");
        add("item.hbm_ntm_rebirth.powder_lapis", "\u9752\u91d1\u77f3\u7c89");
        add("item.hbm_ntm_rebirth.powder_diamond", "\u94bb\u77f3\u7c89");
        add("item.hbm_ntm_rebirth.powder_emerald", "\u7eff\u5b9d\u77f3\u7c89");
        add("item.hbm_ntm_rebirth.powder_sawdust", "\u952f\u672b");
        add("item.hbm_ntm_rebirth.ball_resin", "\u4e73\u80f6");
        add("item.hbm_ntm_rebirth.powder_limestone", "\u77f3\u7070\u77f3\u7c89");
        add("item.hbm_ntm_rebirth.circuit_vacuum_tube", "\u771f\u7a7a\u7ba1");
        add("item.hbm_ntm_rebirth.circuit_capacitor", "\u7535\u5bb9\u5668");
        add("item.hbm_ntm_rebirth.circuit_capacitor_tantalium", "\u94bd\u7535\u5bb9\u5668");
        add("item.hbm_ntm_rebirth.circuit_pcb", "\u5370\u5237\u7535\u8def\u677f");
        add("item.hbm_ntm_rebirth.circuit_silicon", "\u538b\u5370\u7845\u6676\u5706");
        add("item.hbm_ntm_rebirth.circuit_chip", "\u5fae\u82af\u7247");
        add("item.hbm_ntm_rebirth.circuit_chip_bismoid", "\u591a\u529f\u80fd\u96c6\u6210\u7535\u8def");
        add("item.hbm_ntm_rebirth.circuit_analog", "\u6a21\u62df\u7535\u8def\u677f");
        add("item.hbm_ntm_rebirth.circuit_basic", "\u96c6\u6210\u7535\u8def\u677f");
        add("item.hbm_ntm_rebirth.circuit_advanced", "\u519b\u7528\u7ea7\u7535\u8def\u677f");
        add("item.hbm_ntm_rebirth.circuit_capacitor_board", "\u7535\u5bb9\u677f");
        add("item.hbm_ntm_rebirth.circuit_bismoid", "\u591a\u529f\u80fd\u7535\u8def\u677f");
        add("item.hbm_ntm_rebirth.circuit_controller_chassis", "\u63a7\u5236\u5355\u5143\u5916\u58f3");
        add("item.hbm_ntm_rebirth.circuit_controller", "\u63a7\u5236\u5355\u5143");
        add("item.hbm_ntm_rebirth.circuit_controller_advanced", "\u9ad8\u7ea7\u63a7\u5236\u5355\u5143");
        add("item.hbm_ntm_rebirth.circuit_quantum", "\u91cf\u5b50\u5904\u7406\u5355\u5143");
        add("item.hbm_ntm_rebirth.circuit_chip_quantum", "\u56fa\u6001\u91cf\u5b50\u5904\u7406\u5668");
        add("item.hbm_ntm_rebirth.circuit_controller_quantum", "\u91cf\u5b50\u8ba1\u7b97\u673a");
        add("item.hbm_ntm_rebirth.circuit_atomic_clock", "\u539f\u5b50\u949f");
        add("item.hbm_ntm_rebirth.circuit_numitron", "\u4e03\u6bb5\u5f0f\u767d\u70bd\u706f\u663e\u793a\u5668");
        add("item.hbm_ntm_rebirth.crt_display", "CRT \u663e\u793a\u5668");
        add("item.hbm_ntm_rebirth.sphere_steel", "\u94a2\u7403");
        add("item.hbm_ntm_rebirth.blade_titanium", "\u949b\u6da1\u8f6e\u53f6\u7247");
        add("item.hbm_ntm_rebirth.turbine_titanium", "\u949b\u6da1\u8f6e");
        add("item.hbm_ntm_rebirth.blade_tungsten", "\u94a8\u6da1\u8f6e\u53f6\u7247");
        add("item.hbm_ntm_rebirth.turbine_tungsten", "\u94a8\u6da1\u8f6e");
        add("item.hbm_ntm_rebirth.crystal_coal", "\u7164\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_iron", "\u94c1\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_gold", "\u91d1\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_redstone", "\u7ea2\u77f3\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_lapis", "\u9752\u91d1\u77f3\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_diamond", "\u94bb\u77f3\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_uranium", "\u94c0\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_thorium", "\u948d\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_plutonium", "\u94da\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_sulfur", "\u786b\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_niter", "\u785d\u77f3\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_copper", "\u94dc\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_titanium", "\u949b\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_tungsten", "\u94a8\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_aluminium", "\u94dd\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_fluorite", "\u6c1f\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_beryllium", "\u94cd\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_lead", "\u94c5\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_schraranium", "\u4f4e\u4e30\u5ea6Sa326\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_schrabidium", "Sa326\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_phosphorus", "\u78f7\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_lithium", "\u9502\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_trixite", "\u8d5b\u745e\u514b\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_cobalt", "\u94b4\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_xen", "\u4eba\u9020X\u6676\u4f53");
        add("item.hbm_ntm_rebirth.crystal_rare", "\u7a00\u571f\u6676\u4f53");
        add("item.hbm_ntm_rebirth.laser_crystal_bismuth", "\u94cb-\u9490-\u94c0-\u948d\u6fc0\u5149\u6676\u4f53");
        add("item.hbm_ntm_rebirth.ducttape", "\u80f6\u5e26");
        add("item.hbm_ntm_rebirth.ingot_meteorite", "\u9668\u77f3\u952d");
        add("item.hbm_ntm_rebirth.thruster_medium", "\u4e2d\u578b\u63a8\u8fdb\u5668");
        add("item.hbm_ntm_rebirth.thruster_small", "\u5c0f\u578b\u63a8\u8fdb\u5668");
        add("item.hbm_ntm_rebirth.powder_desh_mix", "Desh\u6df7\u5408\u7269");
        add("item.hbm_ntm_rebirth.powder_aluminium", "\u94dd\u7c89");
        add("item.hbm_ntm_rebirth.powder_cobalt_tiny", "\u5c0f\u64ae\u94b4\u7c89");
        add("item.hbm_ntm_rebirth.gem_sodalite", "\u65b9\u94a0\u77f3");
        add("item.hbm_ntm_rebirth.powder_nitan_mix", "Nitan\u6df7\u5408\u7269");
        add("item.hbm_ntm_rebirth.powder_astatine", "\u7839\u7c89");
        add("item.hbm_ntm_rebirth.powder_fire", "\u7ea2\u78f7\u7c89");
        add("item.hbm_ntm_rebirth.powder_chlorophyte", "\u7eff\u53f6\u7d20\u7c89");
        add("item.hbm_ntm_rebirth.wire_dense_copper", "\u7a20\u94dc\u7ebf");
        add("item.hbm_ntm_rebirth.wire_dense_titanium", "\u7a20\u949b\u7ebf");
        add("item.hbm_ntm_rebirth.wire_dense_neodymium", "\u7a20\u9495\u7ebf");
        add("item.hbm_ntm_rebirth.pa_coil_gold", "\u91d1\u7c92\u5b50\u52a0\u901f\u5668\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.pa_coil_niobium", "\u94cc\u949b\u7c92\u5b50\u52a0\u901f\u5668\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.pa_coil_bscco", "BSCCO\u7c92\u5b50\u52a0\u901f\u5668\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.pa_coil_chlorophyte", "\u7eff\u53f6\u7d20\u7c92\u5b50\u52a0\u901f\u5668\u7ebf\u5708");
        add("item.hbm_ntm_rebirth.arc_electrode_graphite", "\u77f3\u58a8\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_lanthanium", "\u9567\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_desh", "Desh\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_saturnite", "\u571f\u661f\u77f3\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_graphite", "\u8017\u5c3d\u7684\u77f3\u58a8\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_lanthanium", "\u8017\u5c3d\u7684\u9567\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_desh", "\u8017\u5c3d\u7684Desh\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.arc_electrode_burnt_saturnite", "\u8017\u5c3d\u7684\u571f\u661f\u77f3\u7535\u5f27\u7535\u6781");
        add("item.hbm_ntm_rebirth.drillbit_steel", "\u94a2\u94bb\u5934");
        add("item.hbm_ntm_rebirth.drillbit_steel_diamond", "\u94a2\u94bb\u5934(\u9576\u94bb)");
        add("item.hbm_ntm_rebirth.drillbit_hss", "\u9ad8\u901f\u94a2\u94bb\u5934");
        add("item.hbm_ntm_rebirth.drillbit_hss_diamond", "\u9ad8\u901f\u94a2\u94bb\u5934(\u9576\u94bb)");
        add("item.hbm_ntm_rebirth.drillbit_desh", "Desh\u94bb\u5934");
        add("item.hbm_ntm_rebirth.drillbit_desh_diamond", "Desh\u94bb\u5934(\u9576\u94bb)");
        add("item.hbm_ntm_rebirth.drillbit_tcalloy", "\u951d\u94a2\u94bb\u5934");
        add("item.hbm_ntm_rebirth.drillbit_tcalloy_diamond", "\u951d\u94a2\u94bb\u5934(\u9576\u94bb)");
        add("item.hbm_ntm_rebirth.drillbit_ferro", "\u94c0\u94c1\u5408\u91d1\u94bb\u5934");
        add("item.hbm_ntm_rebirth.drillbit_ferro_diamond", "\u94c0\u94c1\u5408\u91d1\u94bb\u5934(\u9576\u94bb)");
        add("item.hbm_ntm_rebirth.piston_set_steel", "\u94a2\u6d3b\u585e\u7ec4");
        add("item.hbm_ntm_rebirth.piston_set_dura", "\u9ad8\u901f\u94a2\u6d3b\u585e\u7ec4");
        add("item.hbm_ntm_rebirth.piston_set_desh", "Desh\u6d3b\u585e\u7ec4");
        add("item.hbm_ntm_rebirth.piston_set_starmetal", "\u661f\u8f89\u6d3b\u585e\u7ec4");
        add("item.hbm_ntm_rebirth.nugget_mercury", "\u4e00\u6ef4\u6c34\u94f6");
        add("item.hbm_ntm_rebirth.ingot_euphemium", "Euphemium\u952d");
        add("item.hbm_ntm_rebirth.nugget_euphemium", "Euphemium\u7c92");
        add("item.hbm_ntm_rebirth.powder_euphemium", "Euphemium\u7c89");
        add("item.hbm_ntm_rebirth.ingot_mercury", "\u6c34\u94f6\u952d");
        add("item.hbm_ntm_rebirth.bottle_mercury", "\u74f6\u88c5\u6c34\u94f6");
        add("item.hbm_ntm_rebirth.ingot_gh336", "Ghiorsium-336\u952d");
        add("item.hbm_ntm_rebirth.nugget_gh336", "Ghiorsium-336\u7c92");
        add("item.hbm_ntm_rebirth.billet_gh336", "Ghiorsium-336\u576f");
        add("item.hbm_ntm_rebirth.ingot_starmetal", "\u661f\u91d1\u952d");
        add("item.hbm_ntm_rebirth.ingot_chainsteel", "\u94fe\u94a2\u952d");
        add("item.hbm_ntm_rebirth.crystal_starmetal", "\u661f\u91d1\u6676\u4f53");
        add("item.hbm_ntm_rebirth.gem_volcanic", "\u706b\u5c71\u5b9d\u77f3");
        add("item.hbm_ntm_rebirth.fragment_meteorite", "\u9668\u77f3\u788e\u7247");
        add("item.hbm_ntm_rebirth.ring_starmetal", "\u661f\u91d1\u73af");
        add("item.hbm_ntm_rebirth.nugget_lead", "\u94c5\u7c92");
        add("item.hbm_ntm_rebirth.wire_dense_mingrade", "\u7a20\u7ea2\u94dc\u7ebf");
        add("item.hbm_ntm_rebirth.ball_dynamite", "\u785d\u7cd6\u70b8\u836f");
        add("item.hbm_ntm_rebirth.ball_tnt", "TNT\u70b8\u836f");
        add("item.hbm_ntm_rebirth.ball_tatb", "\u4e09\u6c28\u57fa\u4e09\u785d\u57fa\u82ef(TATB)");
        add("item.hbm_ntm_rebirth.ingot_c4", "\u4e00\u5757C-4");
        add("item.hbm_ntm_rebirth.ingot_semtex", "\u4e00\u5757Semtex");
        add("item.hbm_ntm_rebirth.ballistite", "\u6df7\u5408\u65e0\u70df\u706b\u836f");
        add("item.hbm_ntm_rebirth.cordite", "\u65e0\u70df\u7ebf\u72b6\u706b\u836f");
        add("item.hbm_ntm_rebirth.powder_polonium", "\u948b-210\u7c89");
        add("item.hbm_ntm_rebirth.powder_co60", "\u94b4-60\u7c89");
        add("item.hbm_ntm_rebirth.powder_sr90", "\u9536-90\u7c89");
        add("item.hbm_ntm_rebirth.powder_sr90_tiny", "\u5c0f\u64ae\u9536-90\u7c89");
        add("item.hbm_ntm_rebirth.powder_i131", "\u7898-131\u7c89");
        add("item.hbm_ntm_rebirth.powder_i131_tiny", "\u5c0f\u64ae\u7898-131\u7c89");
        add("item.hbm_ntm_rebirth.powder_xe135", "\u6c19-135\u7c89");
        add("item.hbm_ntm_rebirth.powder_xe135_tiny", "\u5c0f\u64ae\u6c19-135\u7c89");
        add("item.hbm_ntm_rebirth.powder_cs137", "\u94ef-137\u7c89");
        add("item.hbm_ntm_rebirth.powder_cs137_tiny", "\u5c0f\u64ae\u94ef-137\u7c89");
        add("item.hbm_ntm_rebirth.powder_au198", "\u91d1-198\u7c89");
        add("item.hbm_ntm_rebirth.powder_at209", "\u7839-209\u7c89");
        add("item.hbm_ntm_rebirth.powder_actinium", "\u9515\u7c89");
        add("item.hbm_ntm_rebirth.powder_actinium_tiny", "\u5c0f\u64ae\u9515\u7c89");
        add("item.hbm_ntm_rebirth.powder_asbestos", "\u77f3\u68c9\u7c89");
        add("item.hbm_ntm_rebirth.powder_balefire", "\u70ed\u6838\u7070\u70ec");
        add("item.hbm_ntm_rebirth.powder_caesium", "\u94ef\u7c89");
        add("item.hbm_ntm_rebirth.powder_coltan_ore", "\u7c89\u788e\u7684\u94b6\u94bd\u94c1\u77ff\u77f3");
        add("item.hbm_ntm_rebirth.powder_lithium", "\u9502\u7c89");
        add("item.hbm_ntm_rebirth.powder_lithium_tiny", "\u5c0f\u64ae\u9502\u7c89");
        add("item.hbm_ntm_rebirth.powder_neptunium", "\u954e\u7c89");
        add("item.hbm_ntm_rebirth.powder_schrabidate", "Sa\u9178\u94c1\u7c89");
        add("item.hbm_ntm_rebirth.powder_schrabidium", "Sa326\u7c89");
        add("item.hbm_ntm_rebirth.powder_sodium", "\u94a0");
        add("item.hbm_ntm_rebirth.powder_strontium", "\u9536\u7c89");
        add("item.hbm_ntm_rebirth.powder_yellowcake", "\u9ec4\u997c");
        add("item.hbm_ntm_rebirth.nugget_actinium", "\u9515-227\u7c92");
        add("item.hbm_ntm_rebirth.nugget_zirconium", "\u9506\u788e\u7247");
        add("item.hbm_ntm_rebirth.ingot_hes", "\u9ad8\u6d53\u5ea6Sa326\u71c3\u6599\u952d");
        add("item.hbm_ntm_rebirth.nugget_hes", "\u9ad8\u6d53\u5ea6Sa326\u71c3\u6599\u7c92");
        add("item.hbm_ntm_rebirth.billet_hes", "\u9ad8\u6d53\u7f29\u5ea6Sa326\u71c3\u6599\u576f\u6599");
        add("item.hbm_ntm_rebirth.ingot_les", "\u4f4e\u6d53\u5ea6Sa326\u71c3\u6599\u952d");
        add("item.hbm_ntm_rebirth.nugget_les", "\u4f4e\u6d53\u5ea6Sa326\u71c3\u6599\u7c92");
        add("item.hbm_ntm_rebirth.billet_les", "\u4f4e\u6d53\u7f29\u5ea6Sa326\u71c3\u6599\u576f\u6599");
        add("item.hbm_ntm_rebirth.dust", "\u7070\u5c18");
        add("item.hbm_ntm_rebirth.fragment_coltan", "\u94b6\u94bd\u94c1\u77ff\u788e\u7247");
        add("item.hbm_ntm_rebirth.powder_coltan", "\u7eaf\u94bd\u94c1\u77ff");
        add("item.hbm_ntm_rebirth.gem_tantalium", "\u94bd\u6676\u4f53");
        add("item.hbm_ntm_rebirth.powder_tantalium", "\u94bd\u7c89");
        add("item.hbm_ntm_rebirth.nugget_tantalium", "\u94bd\u7c92");
        add("item.hbm_ntm_rebirth.shell_aluminium", "\u5c0f\u578b\u94dd\u58f3");
        add("item.hbm_ntm_rebirth.shell_copper", "\u5c0f\u578b\u94dc\u58f3");
        add("item.hbm_ntm_rebirth.shell_aluminium.desc", "\u53ef\u63d2\u5165\u94bb\u5b54\u77f3\u58a8\u4e2d");
        add("item.hbm_ntm_rebirth.fins_big_steel", "\u5927\u578b\u94a2\u6805\u683c\u7ffc");
        add("item.hbm_ntm_rebirth.fins_flat", "\u6241\u94a2\u5916\u58f3");
        add("item.hbm_ntm_rebirth.fins_quad_titanium", "\u5c0f\u578b\u949b\u7ffc");
        add("item.hbm_ntm_rebirth.fins_small_steel", "\u5c0f\u578b\u94a2\u6805\u683c\u7ffc");
        add("item.hbm_ntm_rebirth.fins_tri_steel", "\u5927\u578b\u94a2\u7ffc");
        add("item.hbm_ntm_rebirth.reacher", "\u94a8\u957f\u81c2\u5939");
        add("item.hbm_ntm_rebirth.stick_c4", "C-4\u70b8\u836f\u68d2");
        add("item.hbm_ntm_rebirth.stick_semtex", "\u585e\u59c6\u6c40\u5851\u80f6\u70b8\u836f\u68d2");
        add("item.hbm_ntm_rebirth.stick_tnt", "TNT\u68d2");
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
                 "demon_core_open",
                 "demon_core_closed",
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
        return switch (id) {
            case "battery_potato",
                 "reacher",
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
                 "laser_crystal_bismuth",
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
        return switch (id) {
            case "ball_dynamite", "ball_tnt", "ball_tatb", "ingot_c4", "ballistite", "cordite" -> true;
            case "powder_tektite" -> true;
            case "nugget_mercury", "ingot_mercury", "bottle_mercury", "nugget_lead" -> true;
            case "ingot_euphemium", "nugget_euphemium", "powder_euphemium",
                 "plate_euphemium" -> true;
            case "ingot_gh336", "nugget_gh336", "billet_gh336" -> true;
            case "ingot_starmetal", "ingot_chainsteel", "ingot_dineutronium", "crystal_starmetal", "gem_volcanic",
                 "fragment_meteorite", "ring_starmetal" -> true;
            case "crystal_diamond", "crystal_rare", "ducttape", "ingot_meteorite", "thruster_medium", "thruster_small" -> true;
            case "plate_paa", "rag", "rag_damp", "rag_piss", "watch",
                 "hazmat_cloth", "hazmat_cloth_red", "hazmat_cloth_grey" -> true;
            case "ingot_weaponsteel", "plate_weaponsteel",
                  "mechanism_gunmetal", "mechanism_weaponsteel", "mechanism_saturnite",
                  "ingot_dura_steel",
                  "plate_schrabidium",
                  "plate_combine_steel",
                  "plate_saturnite",
                  "plate_cast_steel",
                  "plate_cast_lead",
                  "plate_cast_copper",
                  "plate_cast_titanium",
                  "plate_cast_aluminium",
                  "plate_cast_dura_steel",
                  "plate_cast_bismuth_bronze",
                  "plate_cast_arsenic_bronze",
                  "plate_cast_combine_steel",
                  "plate_cast_ferrouranium" -> true;
            case "wire_dense_copper", "wire_dense_titanium", "wire_dense_neodymium",
                 "wire_dense_mingrade" -> true;
            case "dust", "fragment_coltan", "powder_coltan", "gem_tantalium", "powder_tantalium",
                 "nugget_tantalium", "shell_aluminium", "shell_copper" -> true;
            case "powder_coal", "powder_coal_tiny", "powder_desh_mix", "powder_chlorophyte",
                 "powder_polonium", "powder_co60", "powder_sr90", "powder_sr90_tiny",
                 "powder_i131", "powder_i131_tiny", "powder_xe135", "powder_xe135_tiny",
                 "powder_cs137", "powder_cs137_tiny", "powder_au198", "powder_at209",
                 "powder_actinium", "powder_actinium_tiny", "powder_asbestos",
                 "powder_balefire", "powder_caesium", "powder_coltan_ore",
                 "powder_lithium", "powder_lithium_tiny", "powder_neptunium",
                 "powder_schrabidate", "powder_schrabidium", "powder_sodium",
                 "powder_strontium", "powder_yellowcake" -> true;
            case "nugget_actinium", "nugget_zirconium",
                 "ingot_hes", "nugget_hes", "billet_hes",
                 "ingot_les", "nugget_les", "billet_les" -> true;
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

    private void addSatelliteTranslations() {
        add("item.hbm_ntm_rebirth.designator", "\u76ee\u6807\u6307\u793a\u5668");
        add("item.hbm_ntm_rebirth.designator_range", "\u6fc0\u5149\u76ee\u6807\u6307\u793a\u5668");
        add("item.hbm_ntm_rebirth.designator_manual", "\u624b\u52a8\u76ee\u6807\u6307\u793a\u5668");
        add("item.hbm_ntm_rebirth.sat_chip", "\u536b\u661f ID \u82af\u7247");
        add("item.hbm_ntm_rebirth.sat_coord", "\u536b\u661f\u6307\u793a\u5668");
        add("item.hbm_ntm_rebirth.sat_designator", "\u536b\u661f\u6fc0\u5149\u6307\u793a\u5668");
        add("item.hbm_ntm_rebirth.sat_relay", "\u536b\u661f\u96f7\u8fbe\u4e2d\u7ee7\u5668");
        add("item.hbm_ntm_rebirth.sat_foeq", "PEAF - Mk.I FOEQ Duna \u63a2\u6d4b\u5668");
        add("item.hbm_ntm_rebirth.sat_gerald", "\u5efa\u7b51\u5b89\u5353 Gerald");
        add("item.hbm_ntm_rebirth.sat_head_laser", "\u6b7b\u5149");
        add("item.hbm_ntm_rebirth.sat_head_mapper", "\u9ad8\u589e\u76ca\u5149\u5b66\u76f8\u673a");
        add("item.hbm_ntm_rebirth.sat_head_radar", "\u96f7\u8fbe\u5929\u7ebf");
        add("item.hbm_ntm_rebirth.sat_head_resonator", "Xenium \u5171\u632f\u5668");
        add("item.hbm_ntm_rebirth.sat_head_scanner", "M700 \u52d8\u63a2\u626b\u63cf\u5668");
        add("item.hbm_ntm_rebirth.sat_interface", "\u536b\u661f\u63a7\u5236\u7ec8\u7aef");
        add("item.hbm_ntm_rebirth.sat_laser", "\u8f68\u9053\u6b7b\u5149");
        add("item.hbm_ntm_rebirth.sat_lunar_miner", "\u6708\u7403\u91c7\u77ff\u8239");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier0", "0\u7ea7");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier1", "1\u7ea7");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier2", "2\u7ea7");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier3", "3\u7ea7");
        add("tooltip.hbm_ntm_rebirth.missile.tier.tier4", "4\u7ea7");
        add("tooltip.hbm_ntm_rebirth.missile.not_launchable", "\u4e0d\u53ef\u53d1\u5c04");
        add("tooltip.hbm_ntm_rebirth.missile.fuel", "\u71c3\u6599\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.missile.fuel_capacity", "\u71c3\u6599\u5bb9\u91cf\uff1a%smB");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.solid", "\u56fa\u4f53\u71c3\u6599/\u9884\u52a0\u6ce8");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.ethanol_peroxide", "\u4e59\u9187/\u8fc7\u6c27\u5316\u7269");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.kerosene_peroxide", "\u7164\u6cb9/\u8fc7\u6c27\u5316\u7269");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.kerosene_loxy", "\u7164\u6cb9/\u6db2\u6c27");
        add("tooltip.hbm_ntm_rebirth.missile.fuel.jetfuel_loxy", "\u822a\u7a7a\u71c3\u6cb9/\u6db2\u6c27");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.chip", "\u5236\u5bfc\u82af\u7247");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.warhead", "\u6218\u6597\u90e8");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.fuselage", "\u5f39\u4f53");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.fins", "\u7a33\u5b9a\u7ffc");
        add("tooltip.hbm_ntm_rebirth.missile_part.type.thruster", "\u63a8\u8fdb\u6bb5");
        add("tooltip.hbm_ntm_rebirth.custom_missile.empty", "\u672a\u5b89\u88c5\u5bfc\u5f39\u90e8\u4ef6");
        add("tooltip.hbm_ntm_rebirth.custom_missile.chip", "\u82af\u7247\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.warhead", "\u6218\u6597\u90e8\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.fuselage", "\u5f39\u4f53\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.stability", "\u7a33\u5b9a\u7ffc\uff1a%s");
        add("tooltip.hbm_ntm_rebirth.custom_missile.thruster", "\u63a8\u8fdb\u6bb5\uff1a%s");
        ModItems.MISSILE_TAB_ITEMS.forEach(item -> addItem(item, fallbackTitle(item.getId().getPath())));
        add("item.hbm_ntm_rebirth.sat_mapper", "\u5730\u8868\u6d4b\u7ed8\u536b\u661f");
        add("item.hbm_ntm_rebirth.sat_miner", "\u5c0f\u884c\u661f\u91c7\u77ff\u8239");
        add("item.hbm_ntm_rebirth.sat_radar", "\u96f7\u8fbe\u52d8\u6d4b\u536b\u661f");
        add("item.hbm_ntm_rebirth.sat_resonator", "Xenium \u5171\u632f\u536b\u661f");
        add("item.hbm_ntm_rebirth.sat_scanner", "\u6df1\u5c42\u8d44\u6e90\u626b\u63cf\u536b\u661f");
        add("item.hbm_ntm_rebirth.missile_soyuz", "\u8054\u76df-FG\u8fd0\u8f7d\u706b\u7bad");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin", "\u6d82\u88c5");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin.0", "\u539f\u7248");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin.1", "\u6708\u7403\u822a\u5929\u4e2d\u5fc3");
        add("item.hbm_ntm_rebirth.missile_soyuz.skin.2", "\u6218\u540e");
        add("item.hbm_ntm_rebirth.missile_soyuz_lander", "\u8054\u76df\u53f7\u8f68\u9053\u8231");
        add("satchip.frequency", "\u536b\u661f\u9891\u7387");
        add("satchip.foeq", "\u7ed9\u4f60\u4e00\u4e2a\u6210\u5c31\uff0c\u4ec5\u6b64\u800c\u5df2\u3002");
        add("satchip.gerald.desc", "\u4e00\u6b21\u6027\u4f7f\u7528\u3002");
        add("satchip.gerald.desc.0", "\u4e00\u6b21\u6027\u4f7f\u7528\u3002");
        add("satchip.gerald.desc.1", "\u9700\u8981\u8054\u76df\u53f7\u8f68\u9053\u8231\u3002");
        add("satchip.gerald.desc.2", "CPU\u6bc1\u706d\u8005\uff0c\u670d\u4e3b\u4eec\u7684\u514b\u661f\u3002");
        add("satchip.laser", "\u5141\u8bb8\u53ec\u5524\u6fc0\u5149\uff0c\u51b7\u5374\u65f6\u95f410\u79d2\u3002");
        add("satchip.mapper", "\u663e\u793a\u5f53\u524d\u5df2\u52a0\u8f7d\u7684\u533a\u5757\u3002");
        add("satchip.miner", "\u5c06\u77ff\u7269\u7c89\u8fd0\u9001\u81f3\u5378\u8d27\u5e73\u53f0\u3002");
        add("satchip.lunar_miner", "\u6316\u6398\u6708\u58e4\u5e76\u5c06\u5176\u8fd0\u9001\u81f3\u5378\u8d27\u5e73\u53f0\u3002");
        add("satchip.radar", "\u663e\u793a\u6807\u6ce8\u6709\u6d3b\u52a8\u5b9e\u4f53\u7684\u5730\u56fe\u3002");
        add("satchip.resonator", "\u5141\u8bb8\u4f20\u9001\uff0c\u65e0\u51b7\u5374\u3002");
        add("satchip.scanner", "\u7ed8\u5236\u5730\u4e0b\u77ff\u7269\u7684\u4fef\u89c6\u5730\u56fe\u3002");
        add("satchip.no_satellite", "\u8be5\u9891\u7387\u4e0a\u6ca1\u6709\u536b\u661f\u3002");
        add("satchip.interface.ready", "%s \u5df2\u5728\u9891\u7387 %s \u4e0a\u7ebf\u3002");
        add("satchip.coord.ready", "%s \u5750\u6807\u94fe\u8def\u5df2\u5728\u9891\u7387 %s \u5c31\u7eea\u3002");
        add("tooltip.hbm_ntm_rebirth.designator.no_target", "\u8bf7\u9009\u62e9\u76ee\u6807\u3002");
        add("tooltip.hbm_ntm_rebirth.designator.target", "\u76ee\u6807\u5750\u6807\uff1a");
    }

    private void addAbilityTranslations() {
        add("tool.ability.recursion", "\u8fde\u9501\u91c7\u6398");
        add("tool.ability.hammer", "\u9524\u51fb");
        add("tool.ability.hammer_flat", "\u5e73\u9762\u9524\u51fb");
        add("tool.ability.explosion", "\u7206\u7834");
        add("tool.ability.silktouch", "\u7cbe\u51c6\u91c7\u96c6");
        add("tool.ability.luck", "\u65f6\u8fd0");
        add("tool.ability.smelter", "\u81ea\u52a8\u7194\u70bc");
        add("tool.ability.shredder", "\u81ea\u52a8\u7c89\u788e");
        add("tool.ability.centrifuge", "\u81ea\u52a8\u79bb\u5fc3");
        add("tool.ability.crystallizer", "\u81ea\u52a8\u7ed3\u6676");
        add("tool.ability.mercury", "\u6c5e\u89e6");
        add("weapon.ability.radiation", "\u8f90\u5c04");
        add("weapon.ability.vampire", "\u5438\u8840");
        add("weapon.ability.stun", "\u51fb\u6655");
        add("weapon.ability.phosphorus", "\u78f7\u70e7");
        add("weapon.ability.fire", "\u71c3\u70e7");
        add("weapon.ability.chainsaw", "\u94fe\u952f");
        add("weapon.ability.beheader", "\u65a9\u9996");
        add("weapon.ability.bobble", "\u6447\u5934\u5a03\u5a03");
        add("tooltip.hbm_ntm_rebirth.abilities", "\u80fd\u529b\uff1a");
        add("tooltip.hbm_ntm_rebirth.abilities.cycle", "\u53f3\u952e\u5faa\u73af\u5207\u6362\u9884\u8bbe\uff01");
        add("tooltip.hbm_ntm_rebirth.abilities.reset", "\u6f5c\u884c\u70b9\u51fb\u56de\u5230\u7b2c\u4e00\u4e2a\u9884\u8bbe\uff01");
        add("tooltip.hbm_ntm_rebirth.abilities.customize", "Alt \u70b9\u51fb\u6253\u5f00\u81ea\u5b9a\u4e49 GUI\uff01");
        add("tooltip.hbm_ntm_rebirth.weapon_modifiers", "\u6b66\u5668\u4fee\u9970\uff1a");
        add("tooltip.hbm_ntm_rebirth.depth_rock_breaker", "\u53ef\u7834\u574f\u6df1\u5c42\u5ca9\uff01");
        add("chat.hbm_ntm_rebirth.tool_ability.deactivated", "[\u5de5\u5177\u80fd\u529b\u5df2\u7981\u7528]");
        add("chat.hbm_ntm_rebirth.tool_ability.enabled", "[\u5df2\u542f\u7528");
        add("container.hbm_ntm_rebirth.tool_ability", "\u5de5\u5177\u80fd\u529b");
        add("container.hbm_ntm_rebirth.tool_ability.preset", "\u9884\u8bbe %s/%s");
        add("container.hbm_ntm_rebirth.tool_ability.area", "\u533a\u57df");
        add("container.hbm_ntm_rebirth.tool_ability.harvest", "\u91c7\u96c6");
        add("container.hbm_ntm_rebirth.tool_ability.reset", "\u91cd\u7f6e");
        add("container.hbm_ntm_rebirth.tool_ability.delete", "\u5220\u9664");
        add("container.hbm_ntm_rebirth.tool_ability.add", "\u65b0\u589e");
        add("container.hbm_ntm_rebirth.tool_ability.first", "\u9996\u4e2a");
        add("container.hbm_ntm_rebirth.tool_ability.prev", "\u4e0a\u4e00");
        add("container.hbm_ntm_rebirth.tool_ability.next", "\u4e0b\u4e00");
        add("container.hbm_ntm_rebirth.tool_ability.done", "\u5b8c\u6210");
    }

    private void addArmorModTranslations() {
        add("armorMod.all", "\u6240\u6709");
        add("armorMod.applicableTo", "\u9002\u7528\u4e8e\uff1a");
        add("armorMod.boots", "\u9774\u5b50");
        add("armorMod.chestplates", "\u80f8\u7532");
        add("armorMod.helmets", "\u5934\u76d4");
        add("armorMod.leggings", "\u62a4\u817f");
        add("armorMod.slot", "\u63d2\u69fd\uff1a");
        add("armorMod.insertHere", "\u5728\u6b64\u653e\u5165\u62a4\u7532");
        add("armorMod.type.battery", "\u7535\u6c60");
        add("armorMod.type.boots", "\u9774\u5b50");
        add("armorMod.type.chestplate", "\u80f8\u7532");
        add("armorMod.type.cladding", "\u8986\u5c42");
        add("armorMod.type.helmet", "\u5934\u76d4");
        add("armorMod.type.insert", "\u63d2\u677f");
        add("armorMod.type.leggings", "\u62a4\u817f");
        add("armorMod.type.servo", "\u4f3a\u670d\u7535\u673a");
        add("armorMod.type.special", "\u7279\u6b8a");
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
