package com.hbm.ntm.compat;

import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import java.util.List;
import java.util.Locale;
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

    public static boolean registerContainer(ResourceLocation fullContainer, ResourceLocation emptyContainer,
            FluidType type, int content) {
        return registerContainer(Compat.tryLoadItemStack(fullContainer), Compat.tryLoadItemStack(emptyContainer),
                type, content);
    }

    public static boolean registerContainer(String fullContainer, String emptyContainer, FluidType type, int content) {
        return registerContainer(resource(fullContainer), resource(emptyContainer), type, content);
    }

    public static boolean registerContainer(String fullNamespace, String fullPath, String emptyNamespace,
            String emptyPath, FluidType type, int content) {
        return registerContainer(Compat.resource(fullNamespace, fullPath), Compat.resource(emptyNamespace, emptyPath),
                type, content);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture) {
        return registerFluid(name, id, color, poison, flammability, reactivity, symbol, resource(texture));
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, String symbol, String texture) {
        return registerFluid(name, id, color, poison, flammability, reactivity, symbol(symbol), resource(texture));
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture, String forgeFluidId) {
        return registerFluid(name, id, color, poison, flammability, reactivity, symbol, resource(texture),
                resource(forgeFluidId));
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, String symbol, String texture, String forgeFluidId) {
        return registerFluid(name, id, color, poison, flammability, reactivity, symbol(symbol), texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture, forgeFluid);
    }

    public static FluidType getFluid(String name) {
        return HbmFluidJsonUtil.readFluidReference(name);
    }

    public static List<HbmFluidContainerRegistry.ContainerEntry> getContainers(FluidType type) {
        return HbmFluidContainerRegistry.getContainers(type);
    }

    public static ItemStack getFullContainer(ItemStack emptyContainer, FluidType type) {
        return HbmFluidContainerRegistry.getFullContainer(emptyContainer, type);
    }

    public static ItemStack getEmptyContainer(ItemStack fullContainer) {
        return HbmFluidContainerRegistry.getEmptyContainer(fullContainer);
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        return HbmFluidContainerRegistry.getFluidContent(stack, type);
    }

    public static FluidType getFluidType(ItemStack stack) {
        return HbmFluidContainerRegistry.getFluidType(stack);
    }

    public static HbmCompatFluidRegistry.Diagnostics diagnostics() {
        return HbmCompatFluidRegistry.diagnostics();
    }

    public static HbmFluidContainerRegistry.Diagnostics containerDiagnostics() {
        return HbmFluidContainerRegistry.diagnostics();
    }

    private static ResourceLocation resource(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return ResourceLocation.tryParse(id);
    }

    private static FluidSymbol symbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return FluidSymbol.NONE;
        }
        try {
            return FluidSymbol.valueOf(symbol.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return FluidSymbol.NONE;
        }
    }

    private CompatFluidRegistry() {
    }
}
