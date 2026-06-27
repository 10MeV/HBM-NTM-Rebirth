package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

public class LegacyNbtShapedRecipe extends ShapedRecipe {
    public LegacyNbtShapedRecipe(ShapedRecipe base, ItemStack result) {
        super(base.getId(), base.getGroup(), base.category(), base.getRecipeWidth(), base.getRecipeHeight(),
                base.getIngredients(), result, base.showNotification());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEGACY_NBT_SHAPED.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return super.getResultItem(access).copy();
    }

    public static class Serializer implements RecipeSerializer<LegacyNbtShapedRecipe> {
        @Override
        public LegacyNbtShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            return new LegacyNbtShapedRecipe(base, result);
        }

        @Nullable
        @Override
        public LegacyNbtShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer);
            return base == null ? null : new LegacyNbtShapedRecipe(base, base.getResultItem(RegistryAccess.EMPTY));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegacyNbtShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
