package com.hbm.ntm.damage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DamageResistanceTooltipUtil {
    public static void addResistanceInformation(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty()) {
            return;
        }
        DamageResistanceHandler.ArmorSetInfo set = DamageResistanceHandler.setInfoForItem(stack.getItem());
        if (set != null) {
            addStats(tooltip, "tooltip.hbm_ntm_rebirth.damage.set", set.stats(), set.nonNullItems());
        }
        DamageResistanceStats item = DamageResistanceHandler.itemStats(stack.getItem());
        if (item != null) {
            addStats(tooltip, "tooltip.hbm_ntm_rebirth.damage.item", item, List.of());
        }
    }

    private static void addStats(List<Component> tooltip, String titleKey, DamageResistanceStats stats, List<Item> setItems) {
        if (stats.exactResistances().isEmpty() && stats.categoryResistances().isEmpty() && stats.otherResistance() == null) {
            return;
        }
        tooltip.add(Component.translatable(titleKey).withStyle(ChatFormatting.DARK_PURPLE));
        for (Item item : setItems) {
            tooltip.add(Component.literal("  ")
                    .append(new ItemStack(item).getHoverName())
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
        for (Map.Entry<String, DamageResistance> entry : stats.categoryResistances().entrySet()) {
            tooltip.add(line("tooltip.hbm_ntm_rebirth.damage.category." + entry.getKey(), categoryFallback(entry.getKey()), entry.getValue()));
        }
        for (Map.Entry<String, DamageResistance> entry : stats.exactResistances().entrySet()) {
            tooltip.add(line("tooltip.hbm_ntm_rebirth.damage.exact." + entry.getKey(), exactFallback(entry.getKey()), entry.getValue()));
        }
        if (stats.otherResistance() != null) {
            tooltip.add(line("tooltip.hbm_ntm_rebirth.damage.other", "Other", stats.otherResistance()));
        }
    }

    private static Component line(String labelKey, String fallback, DamageResistance resistance) {
        return Component.translatable("tooltip.hbm_ntm_rebirth.damage.line",
                Component.translatableWithFallback(labelKey, fallback),
                format(resistance.threshold()),
                (int) (resistance.resistance() * 100.0F)).withStyle(ChatFormatting.GRAY);
    }

    public static TooltipAudit tooltipAudit() {
        List<String> problems = new ArrayList<>();
        expect(problems, "cmb fallback", exactFallback("cmb").equals("Combine ball"));
        expect(problems, "subatomic fallback", exactFallback("subatomic").equals("Subatomic"));
        expect(problems, "euthanized self fallback", exactFallback("euthanizedself").equals("Euthanized self"));
        expect(problems, "category fallback", categoryFallback(DamageResistanceHandler.CATEGORY_ENERGY).equals("Energy"));
        expect(problems, "generic exact fallback", exactFallback("customdamage").equals("Customdamage"));
        return new TooltipAudit(List.copyOf(problems));
    }

    private static String categoryFallback(String key) {
        return switch (key) {
            case DamageResistanceHandler.CATEGORY_EXPLOSION -> "Explosion";
            case DamageResistanceHandler.CATEGORY_FIRE -> "Fire";
            case DamageResistanceHandler.CATEGORY_PHYSICAL -> "Physical";
            case DamageResistanceHandler.CATEGORY_ENERGY -> "Energy";
            default -> prettyKey(key);
        };
    }

    private static String exactFallback(String key) {
        return switch (DamageResistanceHandler.exactTypeKey(key)) {
            case "acid" -> "Acid";
            case "acidplayer" -> "Acid";
            case "ams" -> "AMS";
            case "amscore" -> "AMS core";
            case "asbestos" -> "Asbestos";
            case "bang" -> "Bang";
            case "blackhole" -> "Black hole";
            case "blacklung" -> "Black lung";
            case "blender" -> "Blender";
            case "boil" -> "Boiling";
            case "boxcar" -> "Boxcar";
            case "broadcast" -> "Broadcast";
            case "building" -> "Building";
            case "chopperbullet" -> "Chopper bullet";
            case "cloud" -> "Cloud";
            case "cmb" -> "Combine ball";
            case "digamma" -> "Digamma";
            case "drown" -> "Drowning";
            case "electric" -> "Electric";
            case "electricity" -> "Electricity";
            case "enervation" -> "Enervation";
            case "euthanized" -> "Euthanized";
            case "euthanizedself" -> "Euthanized self";
            case "euthanizedself2" -> "Euthanized self";
            case "exhaust" -> "Exhaust";
            case "fall" -> "Fall";
            case "flamethrower" -> "Flamethrower";
            case "ice" -> "Ice";
            case "infire" -> "Fire";
            case "laser" -> "Laser";
            case "lead" -> "Lead";
            case "lunar" -> "Lunar";
            case "meteorite" -> "Meteorite";
            case "microwave" -> "Microwave";
            case "mku" -> "MKU";
            case "monoxide" -> "Carbon monoxide";
            case "mudpoisoning" -> "Mud poisoning";
            case "nitan" -> "Nitan";
            case "nuclearblast" -> "Nuclear blast";
            case "onfire" -> "Afterburn";
            case "overdose" -> "Overdose";
            case "pc" -> "PC";
            case "plasma" -> "Plasma";
            case "radiation" -> "Radiation";
            case "revolverbullet" -> "Bullet";
            case "rubble" -> "Rubble";
            case "shrapnel" -> "Shrapnel";
            case "spikes" -> "Spikes";
            case "subatomic" -> "Subatomic";
            case "suicide" -> "Suicide";
            case "taint" -> "Taint";
            case "tau" -> "Tau";
            case "taublast" -> "Tau blast";
            case "vacuum" -> "Vacuum";
            default -> prettyKey(key);
        };
    }

    private static String prettyKey(String key) {
        if (key == null || key.isBlank()) {
            return "Unknown";
        }
        String normalized = key.replace('_', ' ').replace('-', ' ').replace('.', ' ').trim().toLowerCase(Locale.US);
        if (normalized.isEmpty()) {
            return "Unknown";
        }
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private static String format(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return Float.toString(value);
    }

    public record TooltipAudit(List<String> problems) {
        public boolean passed() {
            return problems.isEmpty();
        }
    }

    private static void expect(List<String> problems, String label, boolean ok) {
        if (!ok) {
            problems.add(label);
        }
    }

    private DamageResistanceTooltipUtil() {
    }
}
