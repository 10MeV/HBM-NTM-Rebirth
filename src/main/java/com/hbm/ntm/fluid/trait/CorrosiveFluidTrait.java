package com.hbm.ntm.fluid.trait;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class CorrosiveFluidTrait extends FluidTrait {
    private final int rating;

    public CorrosiveFluidTrait(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public boolean isHighlyCorrosive() {
        return rating > 50;
    }

    @Override
    public void addInfo(List<Component> info) {
        if (isHighlyCorrosive()) {
            info.add(Component.literal("[Strongly Corrosive]").withStyle(ChatFormatting.GOLD));
        } else {
            info.add(Component.literal("[Corrosive]").withStyle(ChatFormatting.YELLOW));
        }
    }
}
