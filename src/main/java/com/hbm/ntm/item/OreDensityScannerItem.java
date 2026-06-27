package com.hbm.ntm.item;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import com.hbm.ntm.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OreDensityScannerItem extends Item {
    private static final int INFORM_ID_BASE = 777;
    private static final int INFORM_MILLIS = 4000;

    public OreDensityScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayer player) || level.getGameTime() % 5 != 0) {
            return;
        }

        int x = player.getBlockX();
        int z = player.getBlockZ();
        double totalLevel = 0.0D;

        for (BedrockOreType type : BedrockOreType.values()) {
            double oreLevel = BedrockOreBaseItem.getOreLevel(x, z, type);
            MutableComponent message = Component.translatable(type.translationKey())
                    .append(Component.literal(": " + truncate(oreLevel) + " ("))
                    .append(Component.translatable(translateDensity(oreLevel)).withStyle(getColor(oreLevel)))
                    .append(Component.literal(")"));
            ModMessages.sendPlayerInform(player, message, INFORM_ID_BASE + type.ordinal(), INFORM_MILLIS);
            totalLevel += oreLevel;
        }

        totalLevel /= BedrockOreType.values().length;
        ModMessages.sendPlayerInform(player, boreRequirementMessage(totalLevel),
                INFORM_ID_BASE + BedrockOreType.values().length, INFORM_MILLIS);
    }

    public static String translateDensity(double density) {
        if (density <= 0.1D) return "item.hbm_ntm_rebirth.ore_density_scanner.verypoor";
        if (density <= 0.35D) return "item.hbm_ntm_rebirth.ore_density_scanner.poor";
        if (density <= 0.75D) return "item.hbm_ntm_rebirth.ore_density_scanner.low";
        if (density >= 1.9D) return "item.hbm_ntm_rebirth.ore_density_scanner.excellent";
        if (density >= 1.65D) return "item.hbm_ntm_rebirth.ore_density_scanner.veryhigh";
        if (density >= 1.25D) return "item.hbm_ntm_rebirth.ore_density_scanner.high";
        return "item.hbm_ntm_rebirth.ore_density_scanner.moderate";
    }

    public static ChatFormatting getColor(double density) {
        if (density <= 0.1D) return ChatFormatting.DARK_RED;
        if (density <= 0.35D) return ChatFormatting.RED;
        if (density <= 0.75D) return ChatFormatting.GOLD;
        if (density > 2.0D) return ChatFormatting.LIGHT_PURPLE;
        if (density >= 1.9D) return ChatFormatting.AQUA;
        if (density >= 1.65D) return ChatFormatting.BLUE;
        if (density >= 1.25D) return ChatFormatting.GREEN;
        return ChatFormatting.YELLOW;
    }

    private static Component boreRequirementMessage(double density) {
        int tier = tier(density);
        MutableComponent message = Component.literal("Tier " + tier).withStyle(ChatFormatting.YELLOW);
        FluidRequirement fluid = fluidRequirement(density);
        if (fluid != null) {
            message.append(Component.literal(" - " + fluid.amount() + "mB "))
                    .append(fluid.type().getDisplayName());
        }
        return message;
    }

    private static int tier(double density) {
        if (density > 1.5D) return 4;
        if (density > 1.0D) return 3;
        if (density > 0.75D) return 2;
        return 1;
    }

    private static FluidRequirement fluidRequirement(double density) {
        if (density > 1.5D) return new FluidRequirement(HbmFluids.SOLVENT, 2000);
        if (density > 1.0D) return new FluidRequirement(HbmFluids.SULFURIC_ACID, 1000);
        if (density > 0.75D) return new FluidRequirement(HbmFluids.WATER, 1000);
        return null;
    }

    private static double truncate(double density) {
        return ((int) (density * 100.0D)) / 100.0D;
    }

    private record FluidRequirement(FluidType type, int amount) {
    }
}
