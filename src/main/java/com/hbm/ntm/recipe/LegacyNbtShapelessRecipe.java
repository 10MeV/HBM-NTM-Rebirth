package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.jetbrains.annotations.Nullable;

public class LegacyNbtShapelessRecipe extends ShapelessRecipe {
    public LegacyNbtShapelessRecipe(ShapelessRecipe base, ItemStack result) {
        super(base.getId(), base.getGroup(), base.category(), result, base.getIngredients());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.LEGACY_NBT_SHAPELESS.get();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return super.getResultItem(access).copy();
    }

    public static class Serializer implements RecipeSerializer<LegacyNbtShapelessRecipe> {
        @Override
        public LegacyNbtShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            return new LegacyNbtShapelessRecipe(base, result);
        }

        @Nullable
        @Override
        public LegacyNbtShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapelessRecipe base = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer);
            return base == null ? null : new LegacyNbtShapelessRecipe(base,
                    base.getResultItem(RegistryAccess.EMPTY));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LegacyNbtShapelessRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
