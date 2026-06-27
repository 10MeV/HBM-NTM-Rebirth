package com.hbm.ntm.fluid;

import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class HbmCompatFluidRegistry {
    private static volatile int rejectedListenerRegistrations;
    private static volatile int resolvedExistingFluids;
    private static volatile int rejectedFluidRegistrations;

    public static void registerFluidRegisterListener(HbmFluidRegisterListener listener) {
        if (listener != null) {
            rejectedListenerRegistrations++;
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
        FluidType type = resolveExisting(name);
        if (type == HbmFluids.NONE && forgeFluidId != null) {
            type = resolveExistingForgeFluid(forgeFluidId);
        }
        return trackResult(type);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability, int reactivity,
            FluidSymbol symbol, ResourceLocation texture, @Nullable Fluid forgeFluid) {
        FluidType type = resolveExisting(name);
        if (type == HbmFluids.NONE && forgeFluid != null && forgeFluid != Fluids.EMPTY) {
            type = HbmFluidForgeMappings.fromForge(forgeFluid);
        }
        return trackResult(type);
    }

    private static FluidType resolveExisting(String name) {
        return HbmFluidJsonUtil.readFluidReference(name);
    }

    private static FluidType resolveExistingForgeFluid(ResourceLocation forgeFluidId) {
        Fluid forgeFluid = ForgeRegistries.FLUIDS.getValue(forgeFluidId);
        return forgeFluid == null || forgeFluid == Fluids.EMPTY ? HbmFluids.NONE
                : HbmFluidForgeMappings.fromForge(forgeFluid);
    }

    private static FluidType trackResult(FluidType type) {
        FluidType resolved = type == null ? HbmFluids.NONE : type;
        if (resolved == HbmFluids.NONE) {
            rejectedFluidRegistrations++;
        } else {
            resolvedExistingFluids++;
        }
        return resolved;
    }

    public static Diagnostics diagnostics() {
        return new Diagnostics(rejectedListenerRegistrations, 0, HbmFluids.foreignFluids().size(),
                resolvedExistingFluids, rejectedFluidRegistrations);
    }

    public record Diagnostics(int listeners, int lastInvokedListeners, int foreignFluids, int lastRegisteredFluids,
            int lastSkippedFluids) {
        public String summary() {
            return "compat fluids listenerLifecycle=disabled listenerAttempts=" + listeners
                    + " lastInvoked=" + lastInvokedListeners
                    + " foreignFluids=" + foreignFluids
                    + " existingResolved=" + lastRegisteredFluids
                    + " rejectedRegistrations=" + lastSkippedFluids;
        }
    }

    private HbmCompatFluidRegistry() {
    }
}
