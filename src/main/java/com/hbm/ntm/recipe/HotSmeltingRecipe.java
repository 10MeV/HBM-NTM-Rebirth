package com.hbm.ntm.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.item.HotItem;
import com.hbm.ntm.util.HbmRegistryUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.jetbrains.annotations.Nullable;

public class HotSmeltingRecipe extends SmeltingRecipe {
    private final ItemStack hotResult;
    private final double heatRatio;

    public HotSmeltingRecipe(ResourceLocation id, String group, Ingredient ingredient, ItemStack result,
            float experience, int cookingTime, double heatRatio) {
        super(id, group, CookingBookCategory.MISC, ingredient, result, experience, cookingTime);
        this.hotResult = result.copy();
        this.heatRatio = heatRatio;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return heatedResult();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return heatedResult();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.HOT_SMELTING.get();
    }

    private ItemStack heatedResult() {
        ItemStack stack = hotResult.copy();
        return HotItem.heatUp(stack, heatRatio);
    }

    public static class Serializer implements RecipeSerializer<HotSmeltingRecipe> {
        @Override
        public HotSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = readResult(json);
            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);
            double heatRatio = GsonHelper.getAsDouble(json, "heat_ratio", 1.0D);
            return new HotSmeltingRecipe(id, group, ingredient, result, experience, cookingTime, heatRatio);
        }

        @Nullable
        @Override
        public HotSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            float experience = buffer.readFloat();
            int cookingTime = buffer.readVarInt();
            double heatRatio = buffer.readDouble();
            return new HotSmeltingRecipe(id, group, ingredient, result, experience, cookingTime, heatRatio);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, HotSmeltingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            recipe.getIngredients().get(0).toNetwork(buffer);
            buffer.writeItem(recipe.hotResult);
            buffer.writeFloat(recipe.getExperience());
            buffer.writeVarInt(recipe.getCookingTime());
            buffer.writeDouble(recipe.heatRatio);
        }

        private static ItemStack readResult(JsonObject json) {
            JsonElement result = json.get("result");
            if (result == null) {
                throw new JsonSyntaxException("Missing hot smelting result");
            }
            if (result.isJsonObject()) {
                return HbmItemOutput.fromJson(result.getAsJsonObject()).representativeStack();
            }
            String itemName = GsonHelper.convertToString(result, "result");
            Item item = HbmRegistryUtil.item(new ResourceLocation(itemName))
                    .orElseThrow(() -> new JsonSyntaxException("Unknown hot smelting result item '" + itemName + "'"));
            return new ItemStack(item);
        }
    }
}
