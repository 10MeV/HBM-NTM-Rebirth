package com.hbm.ntm.fluid.trait;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class SimpleFluidTraits {
    public static final Gaseous GASEOUS = new Gaseous();
    public static final GaseousAtRoomTemperature GASEOUS_AT_ROOM_TEMPERATURE = new GaseousAtRoomTemperature();
    public static final Liquid LIQUID = new Liquid();
    public static final Viscous VISCOUS = new Viscous();
    public static final Plasma PLASMA = new Plasma();
    public static final Antimatter ANTIMATTER = new Antimatter();
    public static final LeadContainer LEAD_CONTAINER = new LeadContainer();
    public static final NoId NO_ID = new NoId();
    public static final NoContainer NO_CONTAINER = new NoContainer();
    public static final Unsiphonable UNSIPHONABLE = new Unsiphonable();

    public static class Gaseous extends FluidTrait {
        @Override
        public void addHiddenInfo(List<Component> info) {
            info.add(Component.literal("[Gaseous]").withStyle(ChatFormatting.BLUE));
        }
    }

    public static class GaseousAtRoomTemperature extends FluidTrait {
        @Override
        public void addHiddenInfo(List<Component> info) {
            info.add(Component.literal("[Gaseous at Room Temperature]").withStyle(ChatFormatting.BLUE));
        }
    }

    public static class Liquid extends FluidTrait {
        @Override
        public void addHiddenInfo(List<Component> info) {
            info.add(Component.literal("[Liquid]").withStyle(ChatFormatting.BLUE));
        }
    }

    public static class Viscous extends FluidTrait {
        @Override
        public void addHiddenInfo(List<Component> info) {
            info.add(Component.literal("[Viscous]").withStyle(ChatFormatting.BLUE));
        }
    }

    public static class Plasma extends FluidTrait {
        @Override
        public void addHiddenInfo(List<Component> info) {
            info.add(Component.literal("[Plasma]").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    public static class Antimatter extends FluidTrait {
        @Override
        public void addInfo(List<Component> info) {
            info.add(Component.literal("[Antimatter]").withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static class LeadContainer extends FluidTrait {
        @Override
        public void addInfo(List<Component> info) {
            info.add(Component.literal("[Requires hazardous material tank to hold]").withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static class NoId extends FluidTrait {
    }

    public static class NoContainer extends FluidTrait {
    }

    public static class Unsiphonable extends FluidTrait {
        @Override
        public void addHiddenInfo(List<Component> info) {
            info.add(Component.literal("[Ignored by siphon]").withStyle(ChatFormatting.BLUE));
        }
    }

    private SimpleFluidTraits() {
    }
}
