package com.hbm.ntm.fluid.trait;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class PoisonFluidTrait extends FluidTrait {
    private final boolean withering;
    private final int level;

    public PoisonFluidTrait(boolean withering, int level) {
        this.withering = withering;
        this.level = level;
    }

    public boolean isWithering() {
        return withering;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public void addHiddenInfo(List<Component> info) {
        info.add(Component.literal("[Toxic Fumes]").withStyle(ChatFormatting.GREEN));
    }
}
