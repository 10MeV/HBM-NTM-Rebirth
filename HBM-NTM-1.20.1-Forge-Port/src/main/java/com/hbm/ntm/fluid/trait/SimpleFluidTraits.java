package com.hbm.ntm.fluid.trait;

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
    }

    public static class GaseousAtRoomTemperature extends FluidTrait {
    }

    public static class Liquid extends FluidTrait {
    }

    public static class Viscous extends FluidTrait {
    }

    public static class Plasma extends FluidTrait {
    }

    public static class Antimatter extends FluidTrait {
    }

    public static class LeadContainer extends FluidTrait {
    }

    public static class NoId extends FluidTrait {
    }

    public static class NoContainer extends FluidTrait {
    }

    public static class Unsiphonable extends FluidTrait {
    }

    private SimpleFluidTraits() {
    }
}
