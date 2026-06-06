package com.hbm.ntm.fluid;

import com.hbm.ntm.recipe.HbmIngredient;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public final class HbmFluidRepairMaterials {
    public static final Component UNRESOLVED_DUCT_TAPE =
            Component.translatableWithFallback("repair.hbm.unresolved.ducttape", "Duct Tape");

    private HbmFluidRepairMaterials() {
    }

    public static HbmRepairMaterial item(ItemLike item, int count) {
        return new IngredientMaterial(HbmIngredient.of(item, count));
    }

    public static HbmRepairMaterial legacyOre(String legacyOreName, int count) {
        return new IngredientMaterial(HbmIngredient.legacyOre(legacyOreName, count));
    }

    public static HbmRepairMaterial optionalLegacyItem(String legacyName, int count, Component fallbackName) {
        RegistryObject<net.minecraft.world.item.Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            return new UnresolvedMaterial(legacyName, Math.max(1, count), fallbackName);
        }
        return item(item.get(), count);
    }

    public static boolean hasMaterials(Player player, List<HbmRepairMaterial> materials) {
        return checkMaterials(player, materials, false);
    }

    public static boolean consumeMaterials(Player player, List<HbmRepairMaterial> materials) {
        return checkMaterials(player, materials, true);
    }

    public static boolean tryRepair(Player player, HbmFluidRepairable repairable) {
        if (player == null || repairable == null || !repairable.isDamagedForFluidRepair()) {
            return false;
        }
        List<HbmRepairMaterial> materials = repairable.getFluidRepairMaterials();
        if (!player.getAbilities().instabuild && !consumeMaterials(player, materials)) {
            return false;
        }
        repairable.repairFluidMachine();
        return true;
    }

    private static boolean checkMaterials(Player player, List<HbmRepairMaterial> materials, boolean consume) {
        if (player == null) {
            return false;
        }
        if (materials == null || materials.isEmpty()) {
            return true;
        }
        List<ItemStack> inventory = copyInventory(player);
        for (HbmRepairMaterial material : materials) {
            if (material == null || !material.isResolved() || !consumeMaterial(inventory, material, false)) {
                return false;
            }
        }
        if (!consume) {
            return true;
        }
        for (HbmRepairMaterial material : materials) {
            consumeMaterial(player.getInventory().items, material, true);
        }
        return true;
    }

    private static List<ItemStack> copyInventory(Player player) {
        List<ItemStack> copy = new ArrayList<>(player.getInventory().items.size());
        for (ItemStack stack : player.getInventory().items) {
            copy.add(stack.copy());
        }
        return copy;
    }

    private static boolean consumeMaterial(List<ItemStack> inventory, HbmRepairMaterial material, boolean mutate) {
        int remaining = material.count();
        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty() || !material.matches(stack)) {
                continue;
            }
            int used = Math.min(remaining, stack.getCount());
            remaining -= used;
            if (mutate) {
                stack.shrink(used);
            }
        }
        return remaining <= 0;
    }

    public interface HbmRepairMaterial {
        int count();

        boolean isResolved();

        boolean matches(ItemStack stack);

        List<ItemStack> displayStacks();

        Component fallbackName();
    }

    private record IngredientMaterial(HbmIngredient ingredient) implements HbmRepairMaterial {
        @Override
        public int count() {
            return ingredient.count();
        }

        @Override
        public boolean isResolved() {
            return ingredient.hasDisplayStacks();
        }

        @Override
        public boolean matches(ItemStack stack) {
            return ingredient.test(stack, true);
        }

        @Override
        public List<ItemStack> displayStacks() {
            return ingredient.displayStacks();
        }

        @Override
        public Component fallbackName() {
            return Component.literal(ingredient.diagnosticName());
        }
    }

    private record UnresolvedMaterial(String legacyName, int count, Component fallbackName)
            implements HbmRepairMaterial {
        @Override
        public boolean isResolved() {
            return false;
        }

        @Override
        public boolean matches(ItemStack stack) {
            return false;
        }

        @Override
        public List<ItemStack> displayStacks() {
            return List.of();
        }
    }
}
