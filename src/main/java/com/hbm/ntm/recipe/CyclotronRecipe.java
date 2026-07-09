package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.NonNullList;
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

public class CyclotronRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient particle;
    private final HbmIngredient input;
    private final ItemStack output;
    private final int antimatterMb;
    private final int sourceOrder;

    public CyclotronRecipe(ResourceLocation id, HbmIngredient particle, HbmIngredient input, ItemStack output,
            int antimatterMb, int sourceOrder) {
        if (particle == null || input == null || output == null || output.isEmpty()) {
            throw new IllegalArgumentException("Cyclotron recipe requires non-empty particle, input, and output");
        }
        this.id = id;
        this.particle = particle;
        this.input = input;
        this.output = output.copy();
        this.antimatterMb = Math.max(0, antimatterMb);
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient particle() {
        return particle;
    }

    public HbmIngredient input() {
        return input;
    }

    public ItemStack output() {
        return output.copy();
    }

    public int antimatterMb() {
        return antimatterMb;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack particleStack, ItemStack inputStack) {
        return particle.test(particleStack, true) && input.test(inputStack, true);
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
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(particle.ingredient());
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_CYCLOTRON.get());
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CYCLOTRON.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CYCLOTRON.type().get();
    }

    public static class Serializer implements RecipeSerializer<CyclotronRecipe> {
        @Override
        public CyclotronRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient particle = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "particle"));
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"))
                    .representativeStack();
            if (output.isEmpty()) {
                throw new JsonSyntaxException("Cyclotron output cannot be empty");
            }
            int antimatter = GsonHelper.getAsInt(json, "antimatter", 0);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", 0);
            return new CyclotronRecipe(id, particle, input, output, antimatter, sourceOrder);
        }

        @Override
        public @Nullable CyclotronRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient particle = HbmIngredient.fromNetwork(buffer);
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            int antimatter = buffer.readVarInt();
            int sourceOrder = buffer.readVarInt();
            return new CyclotronRecipe(id, particle, input, output, antimatter, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CyclotronRecipe recipe) {
            recipe.particle.toNetwork(buffer);
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.antimatterMb);
            buffer.writeVarInt(recipe.sourceOrder);
        }
    }
}
