package com.hbm.ntm.satellite;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SatelliteChipItem extends Item implements ISatelliteChip {
    private final LegacySatelliteType satelliteType;
    private final List<String> descriptionKeys;

    public SatelliteChipItem(Properties properties) {
        this(properties, null);
    }

    public SatelliteChipItem(Properties properties, LegacySatelliteType satelliteType, String... descriptionKeys) {
        super(properties.stacksTo(1));
        this.satelliteType = satelliteType;
        this.descriptionKeys = descriptionKeys == null
                ? List.of()
                : Arrays.stream(descriptionKeys).filter(key -> key != null && !key.isBlank()).toList();
        Satellite.registerSatelliteItem(this, satelliteType);
    }

    public LegacySatelliteType satelliteType() {
        return satelliteType;
    }

    public boolean isLaunchableSatellite() {
        return satelliteType != null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("satchip.frequency")
                .append(": " + getFrequency(stack))
                .withStyle(ChatFormatting.GRAY));
        for (String descriptionKey : descriptionKeys) {
            tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
