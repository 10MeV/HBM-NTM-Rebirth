package com.hbm.ntm.fluid;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class HbmCompatFluidRegistry {
    private static final List<HbmFluidRegisterListener> LISTENERS = new CopyOnWriteArrayList<>();
    private static volatile int lastInvokedListeners;
    private static volatile int lastRegisteredFluids;
    private static volatile int lastSkippedFluids;

    public static void registerFluidRegisterListener(HbmFluidRegisterListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void registerFluidContainerRegisterListener(HbmFluidContainerRegisterListener listener) {
        HbmFluidContainerRegistry.registerFluidContainerRegisterListener(listener);
    }

    public static boolean registerContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type, int content) {
        return HbmFluidContainerRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability, int reactivity,
            FluidSymbol symbol, ResourceLocation texture) {
        return registerFluid(name, id, color, poison, flammability, reactivity, symbol, texture, (Fluid) null);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability, int reactivity,
            FluidSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        Fluid forgeFluid = forgeFluidId == null ? null : ForgeRegistries.FLUIDS.getValue(forgeFluidId);
        return registerFluid(name, id, color, poison, flammability, reactivity, symbol, texture, forgeFluid);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability, int reactivity,
            FluidSymbol symbol, ResourceLocation texture, @Nullable Fluid forgeFluid) {
        FluidType type = HbmFluids.registerForeign(name, id, color, poison, flammability, reactivity, symbol,
                texture == null ? null : texture.toString());
        if (type != HbmFluids.NONE) {
            lastRegisteredFluids++;
            if (forgeFluid != null && forgeFluid != Fluids.EMPTY) {
                HbmFluidForgeMappings.register(type, forgeFluid);
            }
        } else {
            lastSkippedFluids++;
        }
        return type;
    }

    static void reloadForeignFluids() {
        HbmFluids.removeForeignFluids();
        lastInvokedListeners = 0;
        lastRegisteredFluids = 0;
        lastSkippedFluids = 0;
        for (HbmFluidRegisterListener listener : LISTENERS) {
            try {
                listener.onFluidsLoad();
                lastInvokedListeners++;
            } catch (RuntimeException ex) {
                lastSkippedFluids++;
                HbmNtm.LOGGER.warn("HBM fluid register listener failed.", ex);
            }
        }
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(LISTENERS.size(), lastInvokedListeners, HbmFluids.foreignFluids().size(),
                lastRegisteredFluids, lastSkippedFluids);
    }

    public record Diagnostics(int listeners, int lastInvokedListeners, int foreignFluids, int lastRegisteredFluids,
            int lastSkippedFluids) {
        public String summary() {
            return "compat fluids listeners=" + listeners + " lastInvoked=" + lastInvokedListeners
                    + " foreignFluids=" + foreignFluids + " lastRegistered=" + lastRegisteredFluids
                    + " lastSkipped=" + lastSkippedFluids;
        }
    }

    private HbmCompatFluidRegistry() {
    }
}
