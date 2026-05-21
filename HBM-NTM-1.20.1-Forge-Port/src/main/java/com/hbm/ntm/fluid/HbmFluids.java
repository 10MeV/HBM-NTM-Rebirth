package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.trait.SimpleFluidTraits;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class HbmFluids {
    private static final Map<Integer, FluidType> BY_ID = new LinkedHashMap<>();
    private static final Map<String, FluidType> BY_NAME = new LinkedHashMap<>();
    private static int nextId;

    public static final FluidType NONE = register("NONE", 0xFFFFFF, 0, 0, 0, FluidSymbol.NONE);
    public static final FluidType WATER = register("WATER", 0x3333FF, 0, 0, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.LIQUID);
    public static final FluidType LAVA = register("LAVA", 0xFF3300, 4, 0, 0, FluidSymbol.NONE)
            .setTemperature(1_300)
            .addTraits(SimpleFluidTraits.LIQUID, SimpleFluidTraits.VISCOUS);
    public static final FluidType STEAM = register("STEAM", 0xE5E5E5, 3, 0, 0, FluidSymbol.NONE)
            .setTemperature(100)
            .addTraits(SimpleFluidTraits.GASEOUS, SimpleFluidTraits.UNSIPHONABLE);
    public static final FluidType HOTSTEAM = register("HOTSTEAM", 0xE7D6D6, 4, 0, 0, FluidSymbol.NONE)
            .setTemperature(300)
            .addTraits(SimpleFluidTraits.GASEOUS, SimpleFluidTraits.UNSIPHONABLE);
    public static final FluidType SUPERHOTSTEAM = register("SUPERHOTSTEAM", 0xE7B7B7, 4, 0, 0, FluidSymbol.NONE)
            .setTemperature(450)
            .addTraits(SimpleFluidTraits.GASEOUS, SimpleFluidTraits.UNSIPHONABLE);
    public static final FluidType COOLANT = register("COOLANT", 0xD8FCFF, 1, 0, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.LIQUID);
    public static final FluidType COOLANT_HOT = register("COOLANT_HOT", 0x99525E, 1, 0, 0, FluidSymbol.NONE)
            .setTemperature(400)
            .addTraits(SimpleFluidTraits.LIQUID);
    public static final FluidType OIL = register("OIL", 0x1D150D, 1, 2, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.LIQUID, SimpleFluidTraits.VISCOUS);
    public static final FluidType GAS = register("GAS", 0xFFD966, 1, 4, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.GASEOUS);
    public static final FluidType SULFURIC_ACID = register("SULFURIC_ACID", 0xB0AA64, 3, 0, 2, FluidSymbol.ACID)
            .addTraits(SimpleFluidTraits.LIQUID);
    public static final FluidType HYDROGEN = register("HYDROGEN", 0x4286F4, 3, 4, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.GASEOUS);
    public static final FluidType DEUTERIUM = register("DEUTERIUM", 0x2F6BFF, 3, 4, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.GASEOUS);
    public static final FluidType TRITIUM = register("TRITIUM", 0x0B2A86, 3, 4, 0, FluidSymbol.NONE)
            .addTraits(SimpleFluidTraits.GASEOUS);

    private static FluidType register(String name, int color, int poison, int flammability, int reactivity, FluidSymbol symbol) {
        FluidType type = new FluidType(nextId++, name, color, poison, flammability, reactivity, symbol);
        BY_ID.put(type.getId(), type);
        BY_NAME.put(normalize(name), type);
        return type;
    }

    public static void bootstrap() {
        HbmFluidForgeMappings.bootstrap();
    }

    public static FluidType fromId(int id) {
        return BY_ID.getOrDefault(id, NONE);
    }

    public static FluidType fromName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }
        return BY_NAME.getOrDefault(normalize(name), NONE);
    }

    public static Collection<FluidType> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    private static String normalize(String name) {
        return name.toUpperCase(Locale.US);
    }

    private HbmFluids() {
    }
}
