package com.hbm.ntm.util;

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
import com.hbm.render.util.EnumSymbol;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;

/**
 * Legacy-name facade for stable external compatibility hooks.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class CompatExternal {
    public static final Set<Class> turretTargetPlayer =
            new RegisteringClassSet(clazz -> registerTurretTargetSimple(clazz, 0));
    public static final Set<Class> turretTargetFriendly =
            new RegisteringClassSet(clazz -> registerTurretTargetSimple(clazz, 1));
    public static final Set<Class> turretTargetHostile =
            new RegisteringClassSet(clazz -> registerTurretTargetSimple(clazz, 2));
    public static final Set<Class> turretTargetMachine =
            new RegisteringClassSet(clazz -> registerTurretTargetSimple(clazz, 3));
    public static final Set<Class> turretTargetBlacklist =
            new RegisteringClassSet(CompatExternal::registerTurretTargetBlacklist);
    public static final HashMap<Class, BiFunction<Entity, Object, Integer>> turretTargetCondition =
            new RegisteringConditionMap();

    public static BlockEntity getCoreFromPos(Level level, BlockPos pos) {
        return com.hbm.ntm.compat.CompatExternal.getCoreFromPos(level, pos);
    }

    public static BlockEntity getCoreFromPos(Level level, int x, int y, int z) {
        return com.hbm.ntm.compat.CompatExternal.getCoreFromPos(level, x, y, z);
    }

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
            int reactivity, EnumSymbol symbol, ResourceLocation texture) {
        return registerFluid(name, id, color, poison, flammability, reactivity, modern(symbol), texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, EnumSymbol symbol, String texture) {
        return registerFluid(name, id, color, poison, flammability, reactivity, modern(symbol), texture);
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
            int reactivity, EnumSymbol symbol, ResourceLocation texture, ResourceLocation forgeFluidId) {
        return registerFluid(name, id, color, poison, flammability, reactivity, modern(symbol), texture,
                forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, FluidSymbol symbol, String texture, String forgeFluidId) {
        return com.hbm.ntm.compat.CompatExternal.registerFluid(name, id, color, poison, flammability, reactivity,
                symbol, texture, forgeFluidId);
    }

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, EnumSymbol symbol, String texture, String forgeFluidId) {
        return registerFluid(name, id, color, poison, flammability, reactivity, modern(symbol), texture,
                forgeFluidId);
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

    public static FluidType registerFluid(String name, int id, int color, int poison, int flammability,
            int reactivity, EnumSymbol symbol, ResourceLocation texture, Fluid forgeFluid) {
        return registerFluid(name, id, color, poison, flammability, reactivity, modern(symbol), texture,
                forgeFluid);
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

    private static FluidSymbol modern(EnumSymbol symbol) {
        return symbol == null ? FluidSymbol.NONE : symbol.modern();
    }

    public static void registerTurretTargetSimple(Class clazz, int type) {
        if (type < 0 || type > 3) {
            return;
        }
        addWithoutCallback(typeSet(type), clazz);
        Class<? extends Entity> entityClass = asEntityClass(clazz);
        if (entityClass != null) {
            com.hbm.ntm.compat.CompatExternal.registerTurretTargetSimple(entityClass, type);
        }
    }

    public static void registerTurretTargetSimple(Class clazz, CompatTurretTargetRegistry.TargetType type) {
        if (type == null) {
            return;
        }
        registerTurretTargetSimple(clazz, switch (type) {
            case PLAYER -> 0;
            case FRIENDLY -> 1;
            case HOSTILE -> 2;
            case MACHINE -> 3;
        });
    }

    public static void registerTurretTargetBlacklist(Class clazz) {
        addWithoutCallback(turretTargetBlacklist, clazz);
        Class<? extends Entity> entityClass = asEntityClass(clazz);
        if (entityClass != null) {
            com.hbm.ntm.compat.CompatExternal.registerTurretTargetBlacklist(entityClass);
        }
    }

    public static void registerTurretTargetingCondition(Class clazz, BiFunction<Entity, Object, Integer> condition) {
        if (clazz == null || condition == null) {
            return;
        }
        turretTargetCondition.put(clazz, condition);
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

    public static CompatRecipeRegistry.Diagnostics recipeDiagnostics() {
        return CompatRecipeRegistry.diagnostics();
    }

    public static void compatExamples() {
    }

    private static Set<Class> typeSet(int type) {
        return switch (type) {
            case 0 -> turretTargetPlayer;
            case 1 -> turretTargetFriendly;
            case 2 -> turretTargetHostile;
            case 3 -> turretTargetMachine;
            default -> throw new IllegalArgumentException("Invalid turret target type: " + type);
        };
    }

    private static void addWithoutCallback(Set<Class> set, Class clazz) {
        if (set instanceof RegisteringClassSet registering) {
            registering.addDirect(clazz);
        } else if (clazz != null) {
            set.add(clazz);
        }
    }

    private static Class<? extends Entity> asEntityClass(Class clazz) {
        if (clazz == null || !Entity.class.isAssignableFrom(clazz)) {
            return null;
        }
        return clazz.asSubclass(Entity.class);
    }

    private static final class RegisteringClassSet extends AbstractSet<Class> {
        private final Set<Class> backing = new HashSet<>();
        private final Consumer<Class> callback;

        private RegisteringClassSet(Consumer<Class> callback) {
            this.callback = callback;
        }

        @Override
        public Iterator<Class> iterator() {
            return backing.iterator();
        }

        @Override
        public int size() {
            return backing.size();
        }

        @Override
        public boolean contains(Object object) {
            return backing.contains(object);
        }

        @Override
        public boolean add(Class clazz) {
            if (clazz == null || !backing.add(clazz)) {
                return false;
            }
            callback.accept(clazz);
            return true;
        }

        private void addDirect(Class clazz) {
            if (clazz != null) {
                backing.add(clazz);
            }
        }
    }

    private static final class RegisteringConditionMap
            extends HashMap<Class, BiFunction<Entity, Object, Integer>> {
        @Override
        public BiFunction<Entity, Object, Integer> put(Class clazz, BiFunction<Entity, Object, Integer> condition) {
            BiFunction<Entity, Object, Integer> previous = super.put(clazz, condition);
            Class<? extends Entity> entityClass = asEntityClass(clazz);
            if (entityClass != null && condition != null) {
                com.hbm.ntm.compat.CompatExternal.registerTurretTargetingCondition(entityClass, condition);
            }
            return previous;
        }
    }

    private CompatExternal() {
    }
}
