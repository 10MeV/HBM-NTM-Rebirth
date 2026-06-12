package com.hbm.ntm.compat;

import com.hbm.ntm.blockentity.HbmFluidBlockEntity;
import com.hbm.ntm.api.fluid.HbmFluidContainerRegisterListener;
import com.hbm.ntm.api.fluid.HbmFluidRegisterListener;
import com.hbm.ntm.api.recipe.RecipeRegisterListener;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.explosion.CustomMissileExplosion;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.MultiblockHelper;
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
 * Stable HBM-side compat facade for external integrations and debug commands.
 */
public final class CompatExternal {
    @Nullable
    public static BlockEntity getCoreFromPos(Level level, BlockPos pos) {
        if (level == null || pos == null || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return null;
        }
        return MultiblockHelper.resolveCoreBlockEntity(level, pos);
    }

    @Nullable
    public static BlockEntity getCoreFromPos(Level level, int x, int y, int z) {
        return getCoreFromPos(level, new BlockPos(x, y, z));
    }

    @Nullable
    public static BlockEntity getCoreFromTile(BlockEntity blockEntity) {
        return blockEntity == null ? null : MultiblockHelper.resolveCoreBlockEntity(blockEntity);
    }

    public static long getBufferedPowerFromTile(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        return resolved instanceof HbmEnergyHandler handler ? handler.getPower() : 0L;
    }

    public static long getBufferedPowerFromPos(Level level, BlockPos pos) {
        return getBufferedPowerFromTile(getCoreFromPos(level, pos));
    }

    public static long getBufferedPowerFromPos(Level level, int x, int y, int z) {
        return getBufferedPowerFromPos(level, new BlockPos(x, y, z));
    }

    public static long getMaxPowerFromTile(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        return resolved instanceof HbmEnergyHandler handler ? handler.getMaxPower() : 0L;
    }

    public static long getMaxPowerFromPos(Level level, BlockPos pos) {
        return getMaxPowerFromTile(getCoreFromPos(level, pos));
    }

    public static long getMaxPowerFromPos(Level level, int x, int y, int z) {
        return getMaxPowerFromPos(level, new BlockPos(x, y, z));
    }

    /**
     * Legacy external ordinal: 0 = low, 1 = normal, 2 = high, -1 = not applicable.
     */
    public static int getEnergyPriorityFromTile(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        if (!(resolved instanceof HbmEnergyReceiver receiver)) {
            return -1;
        }
        return switch (receiver.getPriority()) {
            case LOWEST, LOW -> 0;
            case NORMAL -> 1;
            case HIGH, HIGHEST -> 2;
        };
    }

    public static int getEnergyPriorityFromPos(Level level, BlockPos pos) {
        return getEnergyPriorityFromTile(getCoreFromPos(level, pos));
    }

    public static int getEnergyPriorityFromPos(Level level, int x, int y, int z) {
        return getEnergyPriorityFromPos(level, new BlockPos(x, y, z));
    }

    /**
     * Returns legacy-style tank rows: [fluidName, fluidId, color, fill, capacity].
     */
    public static ArrayList<Object[]> getFluidInfoFromTile(BlockEntity blockEntity) {
        ArrayList<Object[]> rows = new ArrayList<>();
        for (HbmFluidTank tank : getAllTanks(blockEntity)) {
            FluidType type = tank.getTankType();
            rows.add(new Object[] {
                    type.getName(),
                    type.getId(),
                    type.getColor(),
                    tank.getFill(),
                    tank.getMaxFill()
            });
        }
        return rows;
    }

    public static ArrayList<Object[]> getFluidInfoFromPos(Level level, BlockPos pos) {
        return getFluidInfoFromTile(getCoreFromPos(level, pos));
    }

    public static ArrayList<Object[]> getFluidInfoFromPos(Level level, int x, int y, int z) {
        return getFluidInfoFromPos(level, new BlockPos(x, y, z));
    }

    public static List<HbmFluidTank> getAllTanks(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        if (!(resolved instanceof HbmFluidBlockEntity fluidBlockEntity)) {
            return List.of();
        }
        return fluidBlockEntity.getAllTanks().stream()
                .filter(tank -> tank.getTankType() != HbmFluids.NONE)
                .toList();
    }

    public static List<HbmFluidTank> getAllTanks(Level level, BlockPos pos) {
        return getAllTanks(getCoreFromPos(level, pos));
    }

    public static List<HbmFluidTank> getAllTanks(Level level, int x, int y, int z) {
        return getAllTanks(level, new BlockPos(x, y, z));
    }

    public static void registerRecipeRegisterListener(RecipeRegisterListener listener) {
        CompatRecipeRegistry.registerRecipeRegisterListener(listener);
    }

    public static boolean unregisterRecipeRegisterListener(RecipeRegisterListener listener) {
        return CompatRecipeRegistry.unregisterRecipeRegisterListener(listener);
    }

    public static CompatRecipeRegistry.Diagnostics recipeDiagnostics() {
        return CompatRecipeRegistry.diagnostics();
    }

    public static CompatRecipeRegistry.RecipeFacadeCoverage recipeFacadeCoverage() {
        return CompatRecipeRegistry.recipeFacadeCoverage();
    }

    public static List<CompatRecipeRegistry.RecipeFacadeStatus> recipeFacadeStatuses() {
        return CompatRecipeRegistry.recipeFacadeStatuses();
    }

    public static void registerFluidRegisterListener(HbmFluidRegisterListener listener) {
        HbmCompatFluidRegistry.registerFluidRegisterListener(listener);
    }

    public static void registerFluidContainerRegisterListener(HbmFluidContainerRegisterListener listener) {
        HbmCompatFluidRegistry.registerFluidContainerRegisterListener(listener);
    }

    public static boolean registerFluidContainer(ItemStack fullContainer, ItemStack emptyContainer, FluidType type,
            int content) {
        return HbmCompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerFluidContainer(ResourceLocation fullContainer, ResourceLocation emptyContainer,
            FluidType type, int content) {
        return CompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerFluidContainer(String fullContainer, String emptyContainer, FluidType type,
            int content) {
        return CompatFluidRegistry.registerContainer(fullContainer, emptyContainer, type, content);
    }

    public static boolean registerFluidContainer(String fullNamespace, String fullPath, String emptyNamespace,
            String emptyPath, FluidType type, int content) {
        return CompatFluidRegistry.registerContainer(fullNamespace, fullPath, emptyNamespace, emptyPath, type, content);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture) {
        return CompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, String symbol, String texture) {
        return CompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture, String forgeFluidId) {
        return CompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol, texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, String symbol, String texture, String forgeFluidId) {
        return CompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol, texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return HbmCompatFluidRegistry.registerFluid(name, id, color, poison, flammability, reactivity, symbol,
                texture, forgeFluid);
    }

    public static FluidType getFluid(String name) {
        return CompatFluidRegistry.getFluid(name);
    }

    public static List<HbmFluidContainerRegistry.ContainerEntry> getContainers(FluidType type) {
        return CompatFluidRegistry.getContainers(type);
    }

    public static ItemStack getFullContainer(ItemStack emptyContainer, FluidType type) {
        return CompatFluidRegistry.getFullContainer(emptyContainer, type);
    }

    public static ItemStack getEmptyContainer(ItemStack fullContainer) {
        return CompatFluidRegistry.getEmptyContainer(fullContainer);
    }

    public static int getFluidContent(ItemStack stack, FluidType type) {
        return CompatFluidRegistry.getFluidContent(stack, type);
    }

    public static FluidType getFluidType(ItemStack stack) {
        return CompatFluidRegistry.getFluidType(stack);
    }

    public static HbmCompatFluidRegistry.Diagnostics fluidDiagnostics() {
        return CompatFluidRegistry.diagnostics();
    }

    public static HbmFluidContainerRegistry.Diagnostics fluidContainerDiagnostics() {
        return CompatFluidRegistry.containerDiagnostics();
    }

    public static void registerTurretTargetSimple(Class<? extends Entity> clazz, int type) {
        CompatTurretTargetRegistry.registerSimple(clazz, type);
    }

    public static void registerTurretTargetSimple(Class<? extends Entity> clazz,
            CompatTurretTargetRegistry.TargetType type) {
        CompatTurretTargetRegistry.registerSimple(clazz, type);
    }

    public static void registerTurretTargetBlacklist(Class<? extends Entity> clazz) {
        CompatTurretTargetRegistry.registerBlacklist(clazz);
    }

    public static void registerTurretTargetingCondition(Class<? extends Entity> clazz,
            BiFunction<Entity, Object, Integer> condition) {
        CompatTurretTargetRegistry.registerCondition(clazz, condition);
    }

    public static CompatTurretTargetRegistry.Diagnostics turretTargetDiagnostics() {
        return CompatTurretTargetRegistry.diagnostics();
    }

    public static void setWarheadLabel(CustomMissileExplosion.WarheadType type, String label) {
        CompatCustomWarheadRegistry.setLabel(type, label);
    }

    public static void setWarheadImpact(CustomMissileExplosion.WarheadType type,
            Consumer<CompatCustomWarheadRegistry.WarheadContext> impact) {
        CompatCustomWarheadRegistry.setImpact(type, impact);
    }

    public static void setWarheadUpdate(CustomMissileExplosion.WarheadType type,
            Consumer<CompatCustomWarheadRegistry.WarheadContext> update) {
        CompatCustomWarheadRegistry.setUpdate(type, update);
    }

    public static CompatCustomWarheadRegistry.Diagnostics customWarheadDiagnostics() {
        return CompatCustomWarheadRegistry.diagnostics();
    }

    private CompatExternal() {
    }
}
