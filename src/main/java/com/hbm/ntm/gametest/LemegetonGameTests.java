package com.hbm.ntm.gametest;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.LegacySignWeaponItem;
import com.hbm.ntm.recipe.LemegetonRecipeRuntime;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public final class LemegetonGameTests {
    private LemegetonGameTests() {
    }

    public static void register(RegisterGameTestsEvent event) {
        event.register(LemegetonGameTests.class);
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "itemContracts")
    public static void bookLemegetonKeepsLegacyPortableUpgradeContracts(GameTestHelper helper) {
        helper.assertTrue(ModItems.BOOK_LEMEGETON.get().getMaxStackSize() == 1,
                "book_lemegeton must retain the legacy one-item stack limit");
        helper.assertTrue(ModMenuTypes.LEMEGETON.isPresent(),
                "book_lemegeton must retain a registered portable processing menu");
        helper.assertTrue(LemegetonRecipeRuntime.recipes(helper.getLevel()).size() == 37,
                "Lemegeton must expose all 37 legacy material-upgrade recipes");
        SimpleContainer iron = new SimpleContainer(new ItemStack(Items.IRON_INGOT));
        helper.assertTrue(LemegetonRecipeRuntime.result(helper.getLevel(), iron.getItem(0)).is(ModItems.STEEL_INGOT.get()),
                "Lemegeton iron input must retain its legacy steel result");
        helper.assertTrue(LemegetonRecipeRuntime.result(helper.getLevel(), new ItemStack(Items.DIRT)).isEmpty(),
                "Lemegeton must not fabricate a result for unmatched inputs");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "itemContracts")
    public static void cbtDeviceKeepsLegacySelfBangAndRecipeContracts(GameTestHelper helper) {
        helper.assertTrue(ModItems.CBT_DEVICE.get().getMaxStackSize() == 1,
                "cbt_device must retain the legacy one-item stack limit");
        helper.assertTrue(ModEffects.BANG.isPresent(),
                "cbt_device must use the existing legacy Bang effect");
        helper.assertTrue(helper.getLevel().getRecipeManager()
                        .byKey(new ResourceLocation(HbmNtm.MOD_ID, "tools/cbt_device")).isPresent(),
                "cbt_device must retain its steel-bolt and wrench crafting recipe");
        helper.succeed();
    }

    @GameTest(templateNamespace = HbmNtm.MOD_ID, template = "empty", batch = "itemContracts")
    public static void warningSignBattleAxesKeepLegacyAlloyAndRegistrationContracts(GameTestHelper helper) {
        helper.assertTrue(ModItems.STOPSIGN.get() instanceof LegacySignWeaponItem,
                "stopsign must retain its WeaponSpecial alloy-sword runtime");
        helper.assertTrue(ModItems.SOPSIGN.get() instanceof LegacySignWeaponItem,
                "sopsign must retain its WeaponSpecial alloy-sword runtime");
        helper.assertTrue(ModItems.CHERNOBYLSIGN.get() instanceof LegacySignWeaponItem,
                "chernobylsign must retain its WeaponSpecial alloy-sword runtime");
        helper.assertTrue(ModItems.CHERNOBYLSIGN.get().getMaxDamage() == 2000,
                "chernobylsign must retain alloy's 2,000 durability");
        helper.assertTrue(ModItems.CHERNOBYLSIGN.get().getDefaultAttributeModifiers(EquipmentSlot.MAINHAND)
                        .get(Attributes.ATTACK_DAMAGE).stream().anyMatch(modifier -> modifier.getAmount() == 8.0D),
                "warning sign battle axes must retain 9 total attack damage (base 1 plus alloy sword modifier 8)");
        helper.assertTrue(helper.getLevel().getRecipeManager()
                        .byKey(new ResourceLocation(HbmNtm.MOD_ID, "tools/chernobylsign")).isEmpty(),
                "chernobylsign must not gain a crafting recipe absent from the legacy source");
        helper.succeed();
    }
}
