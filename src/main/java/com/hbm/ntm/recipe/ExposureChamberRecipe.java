package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ExposureChamberRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient particle;
    private final HbmIngredient ingredient;
    private final ItemStack output;
    private final int sourceOrder;

    public ExposureChamberRecipe(ResourceLocation id, HbmIngredient particle, HbmIngredient ingredient,
            ItemStack output, int sourceOrder) {
        this.id = id;
        this.particle = particle;
        this.ingredient = ingredient;
        this.output = output.copy();
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient particle() {
        return particle;
    }

    public HbmIngredient ingredient() {
        return ingredient;
    }

    public ItemStack output() {
        return output.copy();
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack particleStack, ItemStack ingredientStack) {
        return particle.test(particleStack, true) && ingredient.test(ingredientStack, true);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return output();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return output();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.EXPOSURE_CHAMBER.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.EXPOSURE_CHAMBER.type().get();
    }

    public static class Serializer implements RecipeSerializer<ExposureChamberRecipe> {
        @Override
        public ExposureChamberRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient particle = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "particle"));
            HbmIngredient ingredient = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"))
                    .representativeStack();
            if (output.isEmpty()) {
                throw new JsonSyntaxException("Exposure chamber output cannot be empty");
            }
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", 0);
            return new ExposureChamberRecipe(id, particle, ingredient, output, sourceOrder);
        }

        @Override
        public @Nullable ExposureChamberRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient particle = HbmIngredient.fromNetwork(buffer);
            HbmIngredient ingredient = HbmIngredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            int sourceOrder = buffer.readVarInt();
            return new ExposureChamberRecipe(id, particle, ingredient, output, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ExposureChamberRecipe recipe) {
            recipe.particle.toNetwork(buffer);
            recipe.ingredient.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.sourceOrder);
        }
    }
}
