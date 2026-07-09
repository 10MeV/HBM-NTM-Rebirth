package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.block.MassStorageBlock;
import com.hbm.ntm.blockentity.MassStorageBlockEntity;
import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

public class ContainerUpgradeCraftingRecipe extends ShapedRecipe {
    public ContainerUpgradeCraftingRecipe(ShapedRecipe base, ItemStack result) {
        super(base.getId(), base.getGroup(), base.category(), base.getRecipeWidth(), base.getRecipeHeight(),
                base.getIngredients(), result, base.showNotification());
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return super.matches(container, level)
                && isValidSourceForResult(firstContainer(container), getResultItem(level.registryAccess()));
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack source = firstContainer(container);
        ItemStack result = super.assemble(container, registryAccess);
        if (source.isEmpty() || result.isEmpty() || !isValidSourceForResult(source, result)) {
            return ItemStack.EMPTY;
        }

        CompoundTag resultTag = result.getTag();
        boolean hasTargetVariant = resultTag != null && resultTag.contains(LegacyStateBlockItem.TAG_VARIANT);
        int targetVariant = hasTargetVariant ? resultTag.getInt(LegacyStateBlockItem.TAG_VARIANT) : 0;

        CompoundTag sourceTag = source.getTag();
        result.setTag(sourceTag == null || sourceTag.isEmpty() ? null : sourceTag.copy());

        if (hasTargetVariant) {
            CompoundTag tag = result.getOrCreateTag();
            tag.putInt(LegacyStateBlockItem.TAG_VARIANT, targetVariant);
            tag.remove(MassStorageBlockEntity.LEGACY_CAPACITY_TAG);
        }

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CONTAINER_UPGRADE_CRAFTING.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return super.getResultItem(access).copy();
    }

    private static ItemStack firstContainer(CraftingContainer container) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (isStorageContainer(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isStorageContainer(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return block instanceof CrateBlock || block instanceof MassStorageBlock;
    }

    private static boolean isValidSourceForResult(ItemStack source, ItemStack result) {
        if (source.isEmpty() || result.isEmpty()) {
            return false;
        }
        if (!(result.getItem() instanceof BlockItem resultBlockItem)
                || !(source.getItem() instanceof BlockItem sourceBlockItem)) {
            return false;
        }

        if (resultBlockItem.getBlock() instanceof MassStorageBlock) {
            if (!(sourceBlockItem.getBlock() instanceof MassStorageBlock)
                    || !(source.getItem() instanceof LegacyStateBlockItem sourceItem)
                    || !(result.getItem() instanceof LegacyStateBlockItem resultItem)) {
                return false;
            }
            return sourceItem.getVariant(source) == resultItem.getVariant(result) - 1;
        }

        return sourceBlockItem.getBlock() instanceof CrateBlock;
    }

    public static class Serializer implements RecipeSerializer<ContainerUpgradeCraftingRecipe> {
        @Override
        public ContainerUpgradeCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            return new ContainerUpgradeCraftingRecipe(base, result);
        }

        @Nullable
        @Override
        public ContainerUpgradeCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer);
            return base == null ? null : new ContainerUpgradeCraftingRecipe(base,
                    base.getResultItem(RegistryAccess.EMPTY));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ContainerUpgradeCraftingRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
