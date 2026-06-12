package com.hbm.ntm.artillery;

import com.hbm.ntm.pollution.PollutionType;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;

public final class LegacyArtilleryAmmoCatalog {
    public static final ArtyShell AMMO_ARTY = new ArtyShell("ammo_arty", "ammo_arty",
            "ammo_arty", "item.hbm_ntm_rebirth.ammo_arty.desc.normal");
    public static final ArtyShell AMMO_ARTY_CLASSIC = new ArtyShell("ammo_arty_classic", "ammo_arty_classic",
            "ammo_arty_classic", "item.hbm_ntm_rebirth.ammo_arty.desc.classic");
    public static final ArtyShell AMMO_ARTY_HE = new ArtyShell("ammo_arty_he", "ammo_arty_he",
            "ammo_arty_he", "item.hbm_ntm_rebirth.ammo_arty.desc.he");
    public static final ArtyShell AMMO_ARTY_PHOSPHORUS = new ArtyShell("ammo_arty_phosphorus",
            "ammo_arty_phosphorus", "ammo_arty_phosphorus", "item.hbm_ntm_rebirth.ammo_arty.desc.phosphorus");
    public static final ArtyShell AMMO_ARTY_PHOSPHORUS_MULTI = new ArtyShell("ammo_arty_phosphorus_multi",
            "ammo_arty_phosphorus_multi", "ammo_arty_phosphorus_multi",
            "item.hbm_ntm_rebirth.ammo_arty.desc.phosphorus_multi");
    public static final ArtyShell AMMO_ARTY_MINI_NUKE = new ArtyShell("ammo_arty_mini_nuke",
            "ammo_arty_mini_nuke", "ammo_arty_mini_nuke", "item.hbm_ntm_rebirth.ammo_arty.desc.mini_nuke");
    public static final ArtyShell AMMO_ARTY_MINI_NUKE_MULTI = new ArtyShell("ammo_arty_mini_nuke_multi",
            "ammo_arty_mini_nuke_multi", "ammo_arty_mini_nuke_multi",
            "item.hbm_ntm_rebirth.ammo_arty.desc.mini_nuke_multi");
    public static final ArtyShell AMMO_ARTY_NUKE = new ArtyShell("ammo_arty_nuke", "ammo_arty_nuke",
            "ammo_arty_nuke", "item.hbm_ntm_rebirth.ammo_arty.desc.nuke");
    public static final ArtyShell AMMO_ARTY_CARGO = new ArtyShell("ammo_arty_cargo", "ammo_arty_cargo",
            "ammo_arty_cargo", "item.hbm_ntm_rebirth.ammo_arty.desc.cargo");
    public static final ArtyShell AMMO_ARTY_CHLORINE = new ArtyShell("ammo_arty_chlorine",
            "ammo_arty_chlorine", "ammo_arty_chlorine", "item.hbm_ntm_rebirth.ammo_arty.desc.chlorine");
    public static final ArtyShell AMMO_ARTY_PHOSGENE = new ArtyShell("ammo_arty_phosgene",
            "ammo_arty_phosgene", "ammo_arty_phosgene", "item.hbm_ntm_rebirth.ammo_arty.desc.phosgene");
    public static final ArtyShell AMMO_ARTY_MUSTARD_GAS = new ArtyShell("ammo_arty_mustard_gas",
            "ammo_arty_mustard_gas", "ammo_arty_mustard_gas",
            "item.hbm_ntm_rebirth.ammo_arty.desc.mustard_gas");

    public static final HimarsRocket AMMO_HIMARS_STANDARD = new HimarsRocket("ammo_himars_standard",
            "ammo_himars", "himars_standard", 6, 0, "item.hbm_ntm_rebirth.ammo_himars.desc.standard");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_HE = new HimarsRocket("ammo_himars_standard_he",
            "ammo_himars", "himars_standard_he", 6, 0, "item.hbm_ntm_rebirth.ammo_himars.desc.standard_he");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_WP = new HimarsRocket("ammo_himars_standard_wp",
            "ammo_himars", "himars_standard_wp", 6, 0, "item.hbm_ntm_rebirth.ammo_himars.desc.standard_wp");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_TB = new HimarsRocket("ammo_himars_standard_tb",
            "ammo_himars", "himars_standard_tb", 6, 0, "item.hbm_ntm_rebirth.ammo_himars.desc.standard_tb");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_LAVA = new HimarsRocket("ammo_himars_standard_lava",
            "ammo_himars", "himars_standard_lava", 6, 0, "item.hbm_ntm_rebirth.ammo_himars.desc.standard_lava");
    public static final HimarsRocket AMMO_HIMARS_STANDARD_MINI_NUKE =
            new HimarsRocket("ammo_himars_standard_mini_nuke", "ammo_himars", "himars_standard_mini_nuke",
                    6, 0, "item.hbm_ntm_rebirth.ammo_himars.desc.standard_mini_nuke");
    public static final HimarsRocket AMMO_HIMARS_SINGLE = new HimarsRocket("ammo_himars_single",
            "ammo_himars", "himars_single", 1, 1, "item.hbm_ntm_rebirth.ammo_himars.desc.single");
    public static final HimarsRocket AMMO_HIMARS_SINGLE_TB = new HimarsRocket("ammo_himars_single_tb",
            "ammo_himars", "himars_single_tb", 1, 1, "item.hbm_ntm_rebirth.ammo_himars.desc.single_tb");

    private static final List<ArtyShell> ARTY_SHELLS = List.of(
            AMMO_ARTY,
            AMMO_ARTY_CLASSIC,
            AMMO_ARTY_HE,
            AMMO_ARTY_PHOSPHORUS,
            AMMO_ARTY_PHOSPHORUS_MULTI,
            AMMO_ARTY_MINI_NUKE,
            AMMO_ARTY_MINI_NUKE_MULTI,
            AMMO_ARTY_NUKE,
            AMMO_ARTY_CARGO,
            AMMO_ARTY_CHLORINE,
            AMMO_ARTY_PHOSGENE,
            AMMO_ARTY_MUSTARD_GAS);

    private static final List<HimarsRocket> HIMARS_ROCKETS = List.of(
            AMMO_HIMARS_STANDARD,
            AMMO_HIMARS_STANDARD_HE,
            AMMO_HIMARS_STANDARD_WP,
            AMMO_HIMARS_STANDARD_TB,
            AMMO_HIMARS_STANDARD_LAVA,
            AMMO_HIMARS_STANDARD_MINI_NUKE,
            AMMO_HIMARS_SINGLE,
            AMMO_HIMARS_SINGLE_TB);

    private static final Map<String, Map<PollutionType, Float>> ARTY_IMPACT_POLLUTION = Map.of(
            AMMO_ARTY_CHLORINE.legacyName(), Map.of(PollutionType.HEAVYMETAL, 5.0F),
            AMMO_ARTY_PHOSGENE.legacyName(), Map.of(
                    PollutionType.HEAVYMETAL, 10.0F,
                    PollutionType.POISON, 15.0F),
            AMMO_ARTY_MUSTARD_GAS.legacyName(), Map.of(
                    PollutionType.HEAVYMETAL, 15.0F,
                    PollutionType.POISON, 30.0F));

    public static List<ArtyShell> artyShells() {
        return ARTY_SHELLS;
    }

    public static List<HimarsRocket> himarsRockets() {
        return HIMARS_ROCKETS;
    }

    public static List<ItemStack> artyDisplayStacks() {
        return displayStacks(ARTY_SHELLS);
    }

    public static List<ItemStack> himarsDisplayStacks() {
        return displayStacks(HIMARS_ROCKETS);
    }

    private static List<ItemStack> displayStacks(List<? extends AmmoType> types) {
        return types.stream()
                .map(AmmoType::item)
                .map(ItemStack::new)
                .toList();
    }

    public static ArtyShell findArtyShell(ItemStack stack) {
        return find(stack, ARTY_SHELLS);
    }

    public static HimarsRocket findHimarsRocket(ItemStack stack) {
        return find(stack, HIMARS_ROCKETS);
    }

    public static Map<PollutionType, Float> artyImpactPollution(ArtyShell shell) {
        return shell == null ? Map.of() : ARTY_IMPACT_POLLUTION.getOrDefault(shell.legacyName(), Map.of());
    }

    public static Map<PollutionType, Float> artyImpactPollution(ItemStack stack) {
        return artyImpactPollution(findArtyShell(stack));
    }

    private static <T extends AmmoType> T find(ItemStack stack, List<T> types) {
        if (stack.isEmpty()) {
            return null;
        }
        for (T type : types) {
            if (stack.is(type.item())) {
                return type;
            }
        }
        return null;
    }

    public static RegistryObject<Item> registryObject(String legacyName) {
        return switch (legacyName) {
            case "ammo_arty" -> ModItems.AMMO_ARTY;
            case "ammo_arty_classic" -> ModItems.AMMO_ARTY_CLASSIC;
            case "ammo_arty_he" -> ModItems.AMMO_ARTY_HE;
            case "ammo_arty_phosphorus" -> ModItems.AMMO_ARTY_PHOSPHORUS;
            case "ammo_arty_phosphorus_multi" -> ModItems.AMMO_ARTY_PHOSPHORUS_MULTI;
            case "ammo_arty_mini_nuke" -> ModItems.AMMO_ARTY_MINI_NUKE;
            case "ammo_arty_mini_nuke_multi" -> ModItems.AMMO_ARTY_MINI_NUKE_MULTI;
            case "ammo_arty_nuke" -> ModItems.AMMO_ARTY_NUKE;
            case "ammo_arty_cargo" -> ModItems.AMMO_ARTY_CARGO;
            case "ammo_arty_chlorine" -> ModItems.AMMO_ARTY_CHLORINE;
            case "ammo_arty_phosgene" -> ModItems.AMMO_ARTY_PHOSGENE;
            case "ammo_arty_mustard_gas" -> ModItems.AMMO_ARTY_MUSTARD_GAS;
            case "ammo_himars_standard" -> ModItems.AMMO_HIMARS_STANDARD;
            case "ammo_himars_standard_he" -> ModItems.AMMO_HIMARS_STANDARD_HE;
            case "ammo_himars_standard_wp" -> ModItems.AMMO_HIMARS_STANDARD_WP;
            case "ammo_himars_standard_tb" -> ModItems.AMMO_HIMARS_STANDARD_TB;
            case "ammo_himars_standard_lava" -> ModItems.AMMO_HIMARS_STANDARD_LAVA;
            case "ammo_himars_standard_mini_nuke" -> ModItems.AMMO_HIMARS_STANDARD_MINI_NUKE;
            case "ammo_himars_single" -> ModItems.AMMO_HIMARS_SINGLE;
            case "ammo_himars_single_tb" -> ModItems.AMMO_HIMARS_SINGLE_TB;
            default -> null;
        };
    }

    public interface AmmoType {
        String legacyName();

        String itemTexture();

        String projectileTexture();

        String tooltipKey();

        default List<String> tooltipKeys() {
            return List.of(tooltipKey());
        }

        default Item item() {
            RegistryObject<Item> object = registryObject(legacyName());
            return object.get();
        }

        default Component tooltip() {
            return Component.translatable(tooltipKey());
        }
    }

    public record ArtyShell(String legacyName, String itemTexture, String projectileTexture,
            String tooltipKey) implements AmmoType {
    }

    public record HimarsRocket(String legacyName, String itemTexture, String projectileTexture,
            int amount, int modelType, String tooltipKey) implements AmmoType {
    }

    private LegacyArtilleryAmmoCatalog() {
    }
}
