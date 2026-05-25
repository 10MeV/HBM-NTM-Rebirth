package com.hbm.ntm.fluid.trait;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class FlammableFluidTrait extends FluidTrait {
    private final long heatEnergyPerBucket;

    public FlammableFluidTrait(long heatEnergyPerBucket) {
        this.heatEnergyPerBucket = heatEnergyPerBucket;
    }

    public long getHeatEnergyPerBucket() {
        return heatEnergyPerBucket;
    }

    @Override
    public void addInfo(List<Component> info) {
        info.add(Component.literal("[Flammable]").withStyle(ChatFormatting.YELLOW));
        if (heatEnergyPerBucket > 0) {
            info.add(Component.literal("Provides ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(FluidTooltipUtil.shortNumber(heatEnergyPerBucket) + "TU ").withStyle(ChatFormatting.RED))
                    .append(Component.literal("per bucket").withStyle(ChatFormatting.YELLOW)));
        }
    }
}
