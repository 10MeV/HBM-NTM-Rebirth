package com.hbm.ntm.recipe;

import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class RBMKFuelDisassemblyRecipe extends CustomRecipe {
    public RBMKFuelDisassemblyRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !assemble(container, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack input = singleStack(container);
        if (input.isEmpty() || !(input.getItem() instanceof RBMKFuelRodItem rodItem)) {
            return ItemStack.EMPTY;
        }

        RBMKFuelRodState state = rodItem.getState(input);
        RBMKFuelRodRegistry.RodDisassemblyPlan plan =
                RBMKFuelRodRegistry.planDisassembly(rodItem.getLegacyRodId(), state);
        if (!plan.accepted()) {
            return ItemStack.EMPTY;
        }

        RegistryObject<Item> pellet = ModItems.legacyItem(plan.legacyPelletId());
        if (pellet == null || !pellet.isPresent()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(pellet.get(), plan.pelletCount());
        result.setDamageValue(plan.pelletMeta());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.RBMK_FUEL_DISASSEMBLY.get();
    }

    private static ItemStack singleStack(CraftingContainer container) {
        ItemStack found = ItemStack.EMPTY;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!found.isEmpty()) {
                return ItemStack.EMPTY;
            }
            found = stack;
        }
        return found;
    }
}
