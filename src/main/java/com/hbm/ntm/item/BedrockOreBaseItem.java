package com.hbm.ntm.item;

import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.jetbrains.annotations.Nullable;

public class BedrockOreBaseItem extends Item {
    private static final double ORE_SCALE = 0.01D;
    private static final double LEGACY_PERLIN_VALUE_SCALE = 15.0D;
    private static final PerlinSimplexNoise LEVEL_NOISE =
            legacyNoise(2114043L);
    private static final PerlinSimplexNoise[] ORE_NOISES =
            new PerlinSimplexNoise[BedrockOreType.values().length];

    public BedrockOreBaseItem(Properties properties) {
        super(properties);
    }

    public static double getOreAmount(ItemStack stack, BedrockOreType type) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0.0D;
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0D : tag.getDouble(type.suffix());
    }

    public static void setOreAmount(ItemStack stack, BedrockOreType type, double amount) {
        stack.getOrCreateTag().putDouble(type.suffix(), Math.max(0.0D, amount));
    }

    public static void setOreAmount(ItemStack stack, int x, int z, double multiplier) {
        for (BedrockOreType type : BedrockOreType.values()) {
            setOreAmount(stack, type, getOreLevel(x, z, type) * multiplier);
        }
    }

    public static double getOreLevel(int x, int z, BedrockOreType type) {
        PerlinSimplexNoise oreNoise = oreNoise(type);
        double level = LEVEL_NOISE.getValue(x * ORE_SCALE, z * ORE_SCALE, false) * LEGACY_PERLIN_VALUE_SCALE;
        double ore = oreNoise.getValue(x * ORE_SCALE, z * ORE_SCALE, false) * LEGACY_PERLIN_VALUE_SCALE;
        return Mth.clamp(Math.abs(level * ore) * 0.05D, 0.0D, 2.0D);
    }

    private static PerlinSimplexNoise oreNoise(BedrockOreType type) {
        int index = type.ordinal();
        PerlinSimplexNoise noise = ORE_NOISES[index];
        if (noise == null) {
            noise = legacyNoise(2082127L + index);
            ORE_NOISES[index] = noise;
        }
        return noise;
    }

    private static PerlinSimplexNoise legacyNoise(long seed) {
        return new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(seed)), List.of(-3, -2, -1, 0));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (BedrockOreType type : BedrockOreType.values()) {
            double amount = getOreAmount(stack, type);
            tooltip.add(Component.translatable(type.translationKey())
                    .append(Component.literal(": " + ((int) (amount * 100.0D)) / 100.0D))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
