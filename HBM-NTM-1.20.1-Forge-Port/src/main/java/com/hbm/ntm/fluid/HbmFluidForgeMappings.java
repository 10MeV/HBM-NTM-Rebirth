package com.hbm.ntm.fluid;

import java.util.IdentityHashMap;
import java.util.Map;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public final class HbmFluidForgeMappings {
    private static final Map<FluidType, Fluid> TO_FORGE = new IdentityHashMap<>();
    private static final Map<Fluid, FluidType> FROM_FORGE = new IdentityHashMap<>();

    static {
        register(HbmFluids.WATER, Fluids.WATER);
        register(HbmFluids.LAVA, Fluids.LAVA);
    }

    public static void bootstrap() {
        ModFluids.registerMappings();
    }

    public static void register(FluidType hbmType, Fluid forgeFluid) {
        if (hbmType == null || hbmType == HbmFluids.NONE || forgeFluid == null || forgeFluid == Fluids.EMPTY) {
            return;
        }
        TO_FORGE.put(hbmType, forgeFluid);
        FROM_FORGE.put(forgeFluid, hbmType);
    }

    public static FluidType fromForge(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return HbmFluids.NONE;
        }
        return fromForge(stack.getFluid());
    }

    public static FluidType fromForge(Fluid fluid) {
        return FROM_FORGE.getOrDefault(fluid, HbmFluids.NONE);
    }

    public static FluidStack toForge(FluidType type, int amount) {
        Fluid fluid = TO_FORGE.get(type);
        if (fluid == null || amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount);
    }

    public static boolean canExport(FluidType type) {
        return TO_FORGE.containsKey(type);
    }

    private HbmFluidForgeMappings() {
    }
}
