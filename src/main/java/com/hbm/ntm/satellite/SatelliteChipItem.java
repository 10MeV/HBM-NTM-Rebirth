package com.hbm.ntm.satellite;

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
        // 1.7.10 set the max stack size at each real ModItems registration,
        // rather than in ItemSatChip or either remote subclass. Preserve that
        // extension boundary by honoring the caller's properties here.
        super(properties);
        this.satelliteType = satelliteType;
        this.descriptionKeys = descriptionKeys == null
                ? List.of()
                : Arrays.stream(descriptionKeys).filter(key -> key != null && !key.isBlank()).toList();
        Satellite.registerSatelliteItem(this, satelliteType);
        com.hbm.saveddata.satellites.Satellite.registerSatellite(satelliteType, this);
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
                .append(": " + getFrequency(stack)));
        for (String descriptionKey : descriptionKeys) {
            tooltip.add(Component.translatable(descriptionKey));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
