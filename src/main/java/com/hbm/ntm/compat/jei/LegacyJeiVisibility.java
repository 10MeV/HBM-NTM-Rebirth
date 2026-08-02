package com.hbm.ntm.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.recipe.GenericMachineRecipe;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Preserves the visibility gates applied by the legacy NEIConfig and NEIGenericRecipeHandler. */
final class LegacyJeiVisibility {
    private static final Set<String> ALWAYS_HIDDEN_ITEM_PATHS = Set.of(
            "ingot_metal",
            "ingot_metal_scrap",
            "ingot_metal_ingot",
            "ingot_metal_counter",
            "ingot_metal_key",
            "ingot_metal_beacon",
            "ingot_metal_casing",
            "ingot_metal_clockwork",
            "ingot_metal_bar",
            "ingot_metal_detector",
            "memory",
            "machine_electric_furnace_on",
            "machine_rtg_furnace_on",
            "reinforced_lamp_on",
            "statue_elb_f",
            "euphemium_kit",
            "bobmazon_hidden",
            "book_lore",
            "book_secret",
            "book_of_",
            "burnt_bark",
            "ams_core_thingy",
            "dummy_block_blast",
            "dummy_port_compact_launcher",
            "dummy_port_launch_table",
            "dummy_plate_compact_launcher",
            "dummy_plate_launch_table",
            "dummy_plate_cargo",
            "pink_log",
            "pink_planks",
            "pink_slab",
            "pink_double_slab",
            "pink_stairs",
            "spotlight_incandescent_off",
            "spotlight_fluoro_off",
            "spotlight_halogen_off",
            "spotlight_beam",
            "conveyor",
            "conveyor_chute",
            "conveyor_lift",
            "conveyor_express",
            "conveyor_double",
            "conveyor_triple");

    private static final Set<String> EXCLUDED_GENERIC_RECIPE_ITEM_PATHS = Set.of(
            "item_secret",
            "item_secret_selenium_steel",
            "meteorite_sword_seared",
            "meteorite_sword_reforged",
            "meteorite_sword_hardened",
            "meteorite_sword_alloyed",
            "meteorite_sword_machined",
            "meteorite_sword_treated",
            "meteorite_sword_etched",
            "meteorite_sword_bred",
            "meteorite_sword_irradiated",
            "meteorite_sword_fused",
            "meteorite_sword_baleful");

    private LegacyJeiVisibility() {
    }

    static List<ItemStack> hiddenRuntimeItems(Collection<ItemStack> itemStacks) {
        boolean hideSecrets = HbmClientConfig.hideSecretJeiRecipes();
        return itemStacks.stream()
                .filter(stack -> isRuntimeHidden(stack, hideSecrets))
                .toList();
    }

    static boolean isVisibleGenericRecipe(GenericMachineRecipe recipe) {
        if (!HbmClientConfig.hideSecretJeiRecipes()) {
            return true;
        }
        if (recipe.getPools().stream().anyMatch(pool -> pool.startsWith("secret."))) {
            return false;
        }
        return recipe.getItemInputs().stream()
                .flatMap(input -> input.displayStacks().stream())
                .noneMatch(LegacyJeiVisibility::isExcludedGenericRecipeItem)
                && recipe.getItemOutputEntries().stream()
                .flatMap(output -> output.displayStacks().stream())
                .noneMatch(LegacyJeiVisibility::isExcludedGenericRecipeItem);
    }

    private static boolean isRuntimeHidden(ItemStack stack, boolean hideSecrets) {
        String path = hbmItemPath(stack);
        if (path.isEmpty()) {
            return false;
        }
        if (ALWAYS_HIDDEN_ITEM_PATHS.contains(path)) {
            return true;
        }
        if (!hideSecrets) {
            return false;
        }
        return path.equals("ammo_secret")
                || path.startsWith("ammo_secret_")
                || path.equals("item_secret")
                || path.startsWith("item_secret_")
                || stack.getItem() instanceof SednaGunItem gun
                && isLegacySecretGun(gun);
    }

    private static boolean isExcludedGenericRecipeItem(ItemStack stack) {
        return EXCLUDED_GENERIC_RECIPE_ITEM_PATHS.contains(hbmItemPath(stack));
    }

    private static boolean isLegacySecretGun(SednaGunItem gun) {
        SednaGunConfig.WeaponQuality quality = gun.config().quality();
        return quality == SednaGunConfig.WeaponQuality.LEGENDARY
                || quality == SednaGunConfig.WeaponQuality.SECRET;
    }

    private static String hbmItemPath(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return HbmNtm.MOD_ID.equals(id.getNamespace()) ? id.getPath() : "";
    }
}
