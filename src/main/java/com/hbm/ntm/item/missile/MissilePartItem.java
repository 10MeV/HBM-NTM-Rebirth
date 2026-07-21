package com.hbm.ntm.item.missile;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class MissilePartItem extends Item {
    private final PartType type;
    private final String legacyModelKey;

    public MissilePartItem(Properties properties, PartType type, String legacyModelKey) {
        super(properties);
        this.type = type;
        this.legacyModelKey = legacyModelKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CustomMissilePartProfile profile = CustomMissilePartProfile.fromPartItem(this);
        if (profile == null || profile.type() != type) {
            tooltip.add(Component.translatable("error.generic"));
            return;
        }
        CustomMissilePartProfile.PartLore lore = CustomMissilePartProfile.loreFromPartItem(this);
        if (lore != null && lore.title() != null) {
            tooltip.add(Component.literal("\"" + lore.title() + "\"").withStyle(ChatFormatting.DARK_PURPLE));
        }

        switch (type) {
            case CHIP -> tooltip.add(descriptionLine("item.missile.part.inaccuracy",
                    gray(Float.toString(profile.inaccuracy() * 100.0F) + "%")));
            case WARHEAD -> {
                tooltip.add(descriptionLine("item.missile.part.size", gray(sizeName(profile.bottom()))));
                tooltip.add(descriptionLine("item.missile.part.type", warheadName(profile.warheadType())));
                tooltip.add(descriptionLine("item.missile.part.strength", gray(Float.toString(profile.strength()))));
                tooltip.add(descriptionLine("item.missile.part.weight", gray(Float.toString(profile.weight()) + "t")));
            }
            case FUSELAGE -> {
                tooltip.add(descriptionLine("item.missile.part.topSize", gray(sizeName(profile.top()))));
                tooltip.add(descriptionLine("item.missile.part.bottomSize", gray(sizeName(profile.bottom()))));
                tooltip.add(descriptionLine("item.missile.part.fuelType", fuelName(profile.fuelType())));
                tooltip.add(descriptionLine("item.missile.part.fuelAmount", gray(Float.toString(profile.fuel()) + "l")));
            }
            case FINS -> {
                tooltip.add(descriptionLine("item.missile.part.size", gray(sizeName(profile.top()))));
                tooltip.add(descriptionLine("item.missile.part.inaccuracy",
                        gray(Float.toString(profile.inaccuracy() * 100.0F) + "%")));
            }
            case THRUSTER -> {
                tooltip.add(descriptionLine("item.missile.part.size", gray(sizeName(profile.top()))));
                tooltip.add(descriptionLine("item.missile.part.fuelType", fuelName(profile.fuelType())));
                tooltip.add(descriptionLine("item.missile.part.fuelConsumption",
                        gray(Float.toString(profile.consumption()) + "l/tick")));
                tooltip.add(descriptionLine("item.missile.part.maxPayload", gray(Float.toString(profile.lift()) + "t")));
            }
        }

        if (type != PartType.CHIP) {
            tooltip.add(descriptionLine("item.missile.part.health", gray(Float.toString(profile.health()) + "HP")));
            appendLegacyLore(tooltip, lore);
        }
    }

    private static void appendLegacyLore(List<Component> tooltip, @Nullable CustomMissilePartProfile.PartLore lore) {
        if (lore == null) {
            return;
        }
        if (lore.rarity() != null) {
            tooltip.add(descriptionLine("item.missile.part.rarity", rarityName(lore.rarity())));
        }
        if (lore.author() != null) {
            tooltip.add(Component.literal("   ").append(Component.translatable("item.missile.part.by"))
                    .append(Component.literal(" " + lore.author())).withStyle(ChatFormatting.WHITE));
        }
        if (lore.witty() != null) {
            tooltip.add(Component.literal("   \"" + lore.witty() + "\"")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        }
    }

    private static Component descriptionLine(String labelKey, Component value) {
        return Component.translatable(labelKey).withStyle(ChatFormatting.BOLD)
                .append(Component.literal(": "))
                .append(value);
    }

    private static Component gray(String value) {
        return Component.literal(value).withStyle(ChatFormatting.GRAY);
    }

    private static Component warheadName(@Nullable com.hbm.ntm.explosion.CustomMissileExplosion.WarheadType type) {
        if (type == null) {
            return Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        }
        return switch (type) {
            case HE -> Component.translatable("item.warhead.desc.he").withStyle(ChatFormatting.YELLOW);
            case INC -> Component.translatable("item.warhead.desc.incendiary").withStyle(ChatFormatting.GOLD);
            case CLUSTER -> Component.translatable("item.warhead.desc.cluster").withStyle(ChatFormatting.GRAY);
            case BUSTER -> Component.translatable("item.warhead.desc.bunker_buster").withStyle(ChatFormatting.WHITE);
            case NUCLEAR -> Component.translatable("item.warhead.desc.nuclear").withStyle(ChatFormatting.DARK_GREEN);
            case TX -> Component.translatable("item.warhead.desc.thermonuclear").withStyle(ChatFormatting.DARK_PURPLE);
            case N2 -> Component.translatable("item.warhead.desc.n2").withStyle(ChatFormatting.RED);
            case BALEFIRE -> Component.translatable("item.warhead.desc.balefire").withStyle(ChatFormatting.GREEN);
            case SCHRAB -> Component.translatable("item.warhead.desc.schrabidium").withStyle(ChatFormatting.AQUA);
            case TAINT -> Component.translatable("item.warhead.desc.taint").withStyle(ChatFormatting.DARK_PURPLE);
            case CLOUD -> Component.translatable("item.warhead.desc.cloud").withStyle(ChatFormatting.LIGHT_PURPLE);
            case TURBINE -> Component.translatable("item.warhead.desc.turbine")
                    .withStyle(System.currentTimeMillis() % 1000L < 500L ? ChatFormatting.RED : ChatFormatting.LIGHT_PURPLE);
            case CUSTOM0, CUSTOM1, CUSTOM2, CUSTOM3, CUSTOM4, CUSTOM5, CUSTOM6, CUSTOM7, CUSTOM8, CUSTOM9 ->
                    Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        };
    }

    private static Component fuelName(@Nullable CustomMissilePartProfile.FuelType type) {
        if (type == null) {
            return Component.translatable("general.na").withStyle(ChatFormatting.BOLD);
        }
        return switch (type) {
            case KEROSENE -> Component.translatable("item.missile.fuel.kerosene_peroxide")
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
            case SOLID -> Component.translatable("item.missile.fuel.solid").withStyle(ChatFormatting.GOLD);
            case HYDROGEN -> Component.translatable("item.missile.fuel.hydrogen").withStyle(ChatFormatting.DARK_AQUA);
            case XENON -> Component.translatable("item.missile.fuel.xenon").withStyle(ChatFormatting.DARK_PURPLE);
            case BALEFIRE -> Component.translatable("item.missile.fuel.balefire").withStyle(ChatFormatting.GREEN);
        };
    }

    private static Component rarityName(CustomMissilePartProfile.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> Component.translatable("item.missile.part.rarity.common").withStyle(ChatFormatting.GRAY);
            case UNCOMMON -> Component.translatable("item.missile.part.rarity.uncommon").withStyle(ChatFormatting.YELLOW);
            case RARE -> Component.translatable("item.missile.part.rarity.rare").withStyle(ChatFormatting.AQUA);
            case EPIC -> Component.translatable("item.missile.part.rarity.epic").withStyle(ChatFormatting.LIGHT_PURPLE);
            case LEGENDARY -> Component.translatable("item.missile.part.rarity.legendary").withStyle(ChatFormatting.DARK_GREEN);
            case STRANGE -> Component.translatable("item.missile.part.rarity.strange").withStyle(ChatFormatting.DARK_AQUA);
        };
    }

    private static String sizeName(CustomMissilePartProfile.PartSize size) {
        return switch (size) {
            case SIZE_10 -> "1.0m";
            case SIZE_15 -> "1.5m";
            case SIZE_20 -> "2.0m";
            case ANY -> Component.translatable("item.missile.part.size.any").getString();
            case NONE -> Component.translatable("item.missile.part.size.none").getString();
        };
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (usesObjItemRenderer()) {
            acceptClientExtensions("com.hbm.ntm.client.renderer.MissileItemRendererBridge", consumer);
        }
    }

    private static void acceptClientExtensions(String className, Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName(className);
            bridge.getMethod("acceptMissilePart", Consumer.class).invoke(null, consumer);
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                    ? invocation.getCause()
                    : exception;
            throw new IllegalStateException("Unable to initialize missile part item client renderer", cause);
        }
    }

    public PartType type() {
        return type;
    }

    public String legacyModelKey() {
        return legacyModelKey;
    }

    public boolean usesObjItemRenderer() {
        return type != PartType.CHIP;
    }

    public enum PartType {
        CHIP,
        WARHEAD,
        FUSELAGE,
        FINS,
        THRUSTER;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
