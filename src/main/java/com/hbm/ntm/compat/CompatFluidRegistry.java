package com.hbm.ntm.compat;

import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/**
 * Stable compat-facing facade for addon fluid and container registration.
 */
public final class CompatFluidRegistry {
    public static void registerFluidRegisterListener(HbmFluidRegisterListener listener) {
        HbmCompatFluidRegistry.registerFluidRegisterListener(listener);
    }

    public static void registerFluidContainerRegisterListener(HbmFluidContainerRegisterListener listener) {
        HbmCompatFluidRegistry.registerFluidContainerRegisterListener(listener);
    }

    public static boolean registerContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type,
            int content) {
        return HbmCompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture, forgeFluid);
    }

    public static HbmCompatFluidRegistry.Diagnostics diagnostics() {
        return HbmCompatFluidRegistry.diagnostics();
    }

    private CompatFluidRegistry() {
    }
}
