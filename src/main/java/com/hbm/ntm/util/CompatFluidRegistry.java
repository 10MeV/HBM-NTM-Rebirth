package com.hbm.ntm.util;

import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.render.util.EnumSymbol;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/**
 * Legacy-name facade for addon fluid registration.
 */
public final class CompatFluidRegistry {
    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            FluidSymbol symbol, ResourceLocation texture) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            EnumSymbol symbol, ResourceLocation texture) {
        return registerFluid(name, id, color, p, f, r, modern(symbol), texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            FluidSymbol symbol, String texture) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            EnumSymbol symbol, String texture) {
        return registerFluid(name, id, color, p, f, r, modern(symbol), texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            String symbol, String texture) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            FluidSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            EnumSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return registerFluid(name, id, color, p, f, r, modern(symbol), texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            FluidSymbol symbol, String texture, String forgeFluidId) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            EnumSymbol symbol, String texture, String forgeFluidId) {
        return registerFluid(name, id, color, p, f, r, modern(symbol), texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            String symbol, String texture, String forgeFluidId) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            FluidSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerFluid(name, id, color, p, f, r, symbol, texture,
                forgeFluid);
    }

    public static FluidType registerFluid(String name, int id, int color, int p, int f, int r,
            EnumSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return registerFluid(name, id, color, p, f, r, modern(symbol), texture, forgeFluid);
    }

    public static void registerFluidRegisterListener(HbmFluidRegisterListener listener) {
        com.hbm.ntm.compat.CompatFluidRegistry.registerFluidRegisterListener(listener);
    }

    public static void registerFluidContainerRegisterListener(HbmFluidContainerRegisterListener listener) {
        com.hbm.ntm.compat.CompatFluidRegistry.registerFluidContainerRegisterListener(listener);
    }

    public static boolean registerContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type,
            int content) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerContainer(ResourceLocation fullContainer, ResourceLocation emptyContainer,
            FluidType type, int content) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerContainer(String fullContainer, String emptyContainer, FluidType type,
            int content) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerContainer(String fullNamespace, String fullPath, String emptyNamespace,
            String emptyPath, FluidType type, int content) {
        return com.hbm.ntm.compat.CompatFluidRegistry.registerContainer(fullNamespace, fullPath, emptyNamespace,
                emptyPath, type, content);
    }

    public static FluidType getFluid(String name) {
        return com.hbm.ntm.compat.CompatFluidRegistry.getFluid(name);
    }

    public static List<HbmFluidContainerRegistry.ContainerEntry> getContainers(FluidType type) {
        return com.hbm.ntm.compat.CompatFluidRegistry.getContainers(type);
    }

    public static ItemStack getFullContainer(ItemStack emptyContainer, FluidType type) {
        return com.hbm.ntm.compat.CompatFluidRegistry.getFullContainer(emptyContainer, type);
    }

    public static ItemStack getEmptyContainer(ItemStack fullContainer) {
        return com.hbm.ntm.compat.CompatFluidRegistry.getEmptyContainer(fullContainer);
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        return com.hbm.ntm.compat.CompatFluidRegistry.getFluidContent(stack, type);
    }

    public static FluidType getFluidType(ItemStack stack) {
        return com.hbm.ntm.compat.CompatFluidRegistry.getFluidType(stack);
    }

    public static HbmCompatFluidRegistry.Diagnostics diagnostics() {
        return com.hbm.ntm.compat.CompatFluidRegistry.diagnostics();
    }

    public static HbmFluidContainerRegistry.Diagnostics containerDiagnostics() {
        return com.hbm.ntm.compat.CompatFluidRegistry.containerDiagnostics();
    }

    private static FluidSymbol modern(EnumSymbol symbol) {
        return symbol == null ? FluidSymbol.NONE : symbol.modern();
    }

    private CompatFluidRegistry() {
    }
}
