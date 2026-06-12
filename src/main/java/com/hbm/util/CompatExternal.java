package com.hbm.util;

import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import com.hbm.ntm.api.recipe.RecipeRegisterListener;
import com.hbm.ntm.compat.CompatCustomWarheadRegistry;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.compat.CompatTurretTargetRegistry;
import com.hbm.ntm.explosion.CustomMissileExplosion;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidTank;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy package facade for HBM-side external compat hooks.
 */
@Deprecated(forRemoval = false)
public final class CompatExternal {
    @Nullable
    public static BlockEntity getCoreFromPos(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getCoreFromPos(level, pos);
    }

    @Nullable
    public static BlockEntity getCoreFromPos(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getCoreFromPos(level, x, y, z);
    }

    @Nullable
    public static BlockEntity getCoreFromTile(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatExternal.getCoreFromTile(blockEntity);
    }

    public static long getBufferedPowerFromTile(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatExternal.getBufferedPowerFromTile(blockEntity);
    }

    public static long getBufferedPowerFromPos(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getBufferedPowerFromPos(level, pos);
    }

    public static long getBufferedPowerFromPos(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getBufferedPowerFromPos(level, x, y, z);
    }

    public static long getMaxPowerFromTile(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatExternal.getMaxPowerFromTile(blockEntity);
    }

    public static long getMaxPowerFromPos(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getMaxPowerFromPos(level, pos);
    }

    public static long getMaxPowerFromPos(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getMaxPowerFromPos(level, x, y, z);
    }

    public static int getEnergyPriorityFromTile(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatExternal.getEnergyPriorityFromTile(blockEntity);
    }

    public static int getEnergyPriorityFromPos(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getEnergyPriorityFromPos(level, pos);
    }

    public static int getEnergyPriorityFromPos(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getEnergyPriorityFromPos(level, x, y, z);
    }

    public static ArrayList<Object[]> getFluidInfoFromTile(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatExternal.getFluidInfoFromTile(blockEntity);
    }

    public static ArrayList<Object[]> getFluidInfoFromPos(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getFluidInfoFromPos(level, pos);
    }

    public static ArrayList<Object[]> getFluidInfoFromPos(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getFluidInfoFromPos(level, x, y, z);
    }

    public static List<HbmFluidTank> getAllTanks(BlockEntity blockEntity) {
        return com.hbm.ntm.compat.CompatExternal.getAllTanks(blockEntity);
    }

    public static List<HbmFluidTank> getAllTanks(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getAllTanks(level, pos);
    }

    public static List<HbmFluidTank> getAllTanks(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getAllTanks(level, x, y, z);
    }

    public static void registerRecipeRegisterListener(RecipeRegisterListener listener) {
        com.hbm.ntm.compat.CompatExternal.registerRecipeRegisterListener(listener);
    }

    public static boolean unregisterRecipeRegisterListener(RecipeRegisterListener listener) {
        return com.hbm.ntm.compat.CompatExternal.unregisterRecipeRegisterListener(listener);
    }

    public static CompatRecipeRegistry.Diagnostics recipeDiagnostics() {
        return com.hbm.ntm.compat.CompatExternal.recipeDiagnostics();
    }

    public static CompatRecipeRegistry.RecipeFacadeCoverage recipeFacadeCoverage() {
        return com.hbm.ntm.compat.CompatExternal.recipeFacadeCoverage();
    }

    public static List<CompatRecipeRegistry.RecipeFacadeStatus> recipeFacadeStatuses() {
        return com.hbm.ntm.compat.CompatExternal.recipeFacadeStatuses();
    }

    public static void registerFluidRegisterListener(HbmFluidRegisterListener listener) {
        com.hbm.ntm.compat.CompatExternal.registerFluidRegisterListener(listener);
    }

    public static void registerFluidContainerRegisterListener(HbmFluidContainerRegisterListener listener) {
        com.hbm.ntm.compat.CompatExternal.registerFluidContainerRegisterListener(listener);
    }

    public static boolean registerFluidContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type,
            int content) {
        return com.hbm.ntm.compat.CompatExternal.registerFluidContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerFluidContainer(ResourceLocation fullContainer, ResourceLocation emptyContainer,
            FluidType type, int content) {
        return com.hbm.ntm.compat.CompatExternal.registerFluidContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerFluidContainer(String fullContainer, String emptyContainer, FluidType type,
            int content) {
        return com.hbm.ntm.compat.CompatExternal.registerFluidContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerFluidContainer(String fullNamespace, String fullPath, String emptyNamespace,
            String emptyPath, FluidType type, int content) {
        return com.hbm.ntm.compat.CompatExternal.registerFluidContainer(fullNamespace, fullPath, emptyNamespace,
                emptyPath, type, content);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, String symbol, String texture) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture, String forgeFluidId) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, String symbol, String texture, String forgeFluidId) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture, forgeFluid);
    }

    public static FluidType getFluid(String name) {
        return com.hbm.ntm.compat.CompatExternal.getFluid(name);
    }

    public static List<HbmFluidContainerRegistry.ContainerEntry> getContainers(FluidType type) {
        return com.hbm.ntm.compat.CompatExternal.getContainers(type);
    }

    public static ItemStack getFullContainer(ItemStack emptyContainer, FluidType type) {
        return com.hbm.ntm.compat.CompatExternal.getFullContainer(emptyContainer, type);
    }

    public static ItemStack getEmptyContainer(ItemStack fullContainer) {
        return com.hbm.ntm.compat.CompatExternal.getEmptyContainer(fullContainer);
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        return com.hbm.ntm.compat.CompatExternal.getFluidContent(stack, type);
    }

    public static FluidType getFluidType(ItemStack stack) {
        return com.hbm.ntm.compat.CompatExternal.getFluidType(stack);
    }

    public static HbmCompatFluidRegistry.Diagnostics fluidDiagnostics() {
        return com.hbm.ntm.compat.CompatExternal.fluidDiagnostics();
    }

    public static HbmFluidContainerRegistry.Diagnostics fluidContainerDiagnostics() {
        return com.hbm.ntm.compat.CompatExternal.fluidContainerDiagnostics();
    }

    public static void registerTurretTargetSimple(Class<? extends Entity> clazz, int type) {
        com.hbm.ntm.compat.CompatExternal.registerTurretTargetSimple(clazz, type);
    }

    public static void registerTurretTargetSimple(Class<? extends Entity> clazz,
            CompatTurretTargetRegistry.TargetType type) {
        com.hbm.ntm.compat.CompatExternal.registerTurretTargetSimple(clazz, type);
    }

    public static void registerTurretTargetBlacklist(Class<? extends Entity> clazz) {
        com.hbm.ntm.compat.CompatExternal.registerTurretTargetBlacklist(clazz);
    }

    public static void registerTurretTargetingCondition(Class<? extends Entity> clazz,
            BiFunction<Entity, Object, Integer> condition) {
        com.hbm.ntm.compat.CompatExternal.registerTurretTargetingCondition(clazz, condition);
    }

    public static CompatTurretTargetRegistry.Diagnostics turretTargetDiagnostics() {
        return com.hbm.ntm.compat.CompatExternal.turretTargetDiagnostics();
    }

    public static void setWarheadLabel(CustomMissileExplosion.WarheadType type, String label) {
        com.hbm.ntm.compat.CompatExternal.setWarheadLabel(type, label);
    }

    public static void setWarheadImpact(CustomMissileExplosion.WarheadType type,
            Consumer<CompatCustomWarheadRegistry.WarheadContext> impact) {
        com.hbm.ntm.compat.CompatExternal.setWarheadImpact(type, impact);
    }

    public static void setWarheadUpdate(CustomMissileExplosion.WarheadType type,
            Consumer<CompatCustomWarheadRegistry.WarheadContext> update) {
        com.hbm.ntm.compat.CompatExternal.setWarheadUpdate(type, update);
    }

    public static CompatCustomWarheadRegistry.Diagnostics customWarheadDiagnostics() {
        return com.hbm.ntm.compat.CompatExternal.customWarheadDiagnostics();
    }

    private CompatExternal() {
    }
}
