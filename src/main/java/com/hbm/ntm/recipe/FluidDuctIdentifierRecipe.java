package com.hbm.ntm.recipe;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class FluidDuctIdentifierRecipe extends CustomRecipe {
    public FluidDuctIdentifierRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !assemble(container, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        FluidType selected = HbmFluids.NONE;
        int identifierCount = 0;
        int normalDucts = 0;
        int typedDucts = 0;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof FluidIdentifierItem) {
                identifierCount++;
                selected = FluidIdentifierItem.getType(stack, true);
            } else if (isPlainUntypedDuct(stack)) {
                normalDucts++;
            } else if (stack.is(ModItems.FLUID_DUCT.get())) {
                typedDucts++;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (identifierCount != 1 || selected == HbmFluids.NONE || (normalDucts > 0 && typedDucts > 0)) {
            return ItemStack.EMPTY;
        }

        int ductCount = normalDucts + typedDucts;
        if (ductCount != 1 && ductCount != 8) {
            return ItemStack.EMPTY;
        }
        if (!(ModItems.FLUID_DUCT.get() instanceof FluidPipeBlockItem item)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = FluidPipeBlockItem.createStack(item, selected);
        result.setCount(ductCount);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    private static boolean isPlainUntypedDuct(ItemStack stack) {
        if (!stack.is(ModBlocks.FLUID_DUCT_NEO.get().asItem())) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag == null || tag.getInt(LegacyStateBlockItem.TAG_VARIANT) == 0;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FLUID_DUCT_IDENTIFIER.get();
    }
}
