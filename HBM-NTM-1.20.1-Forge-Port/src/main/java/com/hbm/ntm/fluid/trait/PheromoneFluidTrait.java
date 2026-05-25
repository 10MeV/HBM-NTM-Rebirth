package com.hbm.ntm.fluid.trait;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PheromoneFluidTrait extends FluidTrait {
    private final int type;

    public PheromoneFluidTrait(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public void addInfo(List<Component> info) {
        if (type == 1) {
            info.add(Component.literal("[Glyphid Pheromones]").withStyle(ChatFormatting.AQUA));
        } else {
            info.add(Component.literal("[Modified Pheromones]").withStyle(ChatFormatting.BLUE));
        }
    }
}
