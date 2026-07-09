package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
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

public class ParticleAcceleratorRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input1;
    private final HbmIngredient input2;
    private final int momentum;
    private final ItemStack output1;
    private final ItemStack output2;
    private final int sourceOrder;

    public ParticleAcceleratorRecipe(ResourceLocation id, HbmIngredient input1, HbmIngredient input2, int momentum,
            ItemStack output1, @Nullable ItemStack output2, int sourceOrder) {
        if (input1 == null || input2 == null) {
            throw new IllegalArgumentException("Particle accelerator recipe requires two inputs");
        }
        if (output1 == null || output1.isEmpty()) {
            throw new IllegalArgumentException("Particle accelerator recipe requires a primary output");
        }
        this.id = id;
        this.input1 = input1;
        this.input2 = input2;
        this.momentum = Math.max(0, momentum);
        this.output1 = output1.copy();
        this.output2 = output2 == null ? ItemStack.EMPTY : output2.copy();
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient input1() {
        return input1;
    }

    public HbmIngredient input2() {
        return input2;
    }

    public int momentum() {
        return momentum;
    }

    public ItemStack output1() {
        return output1.copy();
    }

    public ItemStack output2() {
        return output2.copy();
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack first, ItemStack second) {
        return matchesOrdered(first, second) || matchesOrdered(second, first);
    }

    private boolean matchesOrdered(ItemStack first, ItemStack second) {
        return first != null && second != null && !first.isEmpty() && !second.isEmpty()
                && input1.test(first, true) && input2.test(second, true);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return output1();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return output1();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input1.ingredient());
        ingredients.add(input2.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.PA_DETECTOR.get());
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
        return ModRecipes.PARTICLE_ACCELERATOR.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PARTICLE_ACCELERATOR.type().get();
    }

    public static class Serializer implements RecipeSerializer<ParticleAcceleratorRecipe> {
        @Override
        public ParticleAcceleratorRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray inputs = GsonHelper.getAsJsonArray(json, "inputs");
            if (inputs.size() != 2) {
                throw new JsonSyntaxException("Particle accelerator recipe requires exactly two inputs");
            }
            List<HbmIngredient> inputList = new ArrayList<>(2);
            for (JsonElement element : inputs) {
                inputList.add(HbmIngredient.fromJson(GsonHelper.convertToJsonObject(element,
                        "particle accelerator input")));
            }

            JsonArray outputs = GsonHelper.getAsJsonArray(json, "outputs");
            if (outputs.isEmpty() || outputs.size() > 2) {
                throw new JsonSyntaxException("Particle accelerator recipe requires one or two outputs");
            }
            ItemStack output1 = readOutput(outputs.get(0), "particle accelerator primary output");
            ItemStack output2 = outputs.size() > 1
                    ? readOutput(outputs.get(1), "particle accelerator secondary output")
                    : ItemStack.EMPTY;
            int momentum = GsonHelper.getAsInt(json, "momentum");
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new ParticleAcceleratorRecipe(id, inputList.get(0), inputList.get(1), momentum, output1, output2,
                    sourceOrder);
        }

        @Override
        public @Nullable ParticleAcceleratorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input1 = HbmIngredient.fromNetwork(buffer);
            HbmIngredient input2 = HbmIngredient.fromNetwork(buffer);
            int momentum = buffer.readVarInt();
            ItemStack output1 = buffer.readItem();
            ItemStack output2 = buffer.readItem();
            int sourceOrder = buffer.readVarInt();
            return new ParticleAcceleratorRecipe(id, input1, input2, momentum, output1, output2, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ParticleAcceleratorRecipe recipe) {
            recipe.input1.toNetwork(buffer);
            recipe.input2.toNetwork(buffer);
            buffer.writeVarInt(recipe.momentum);
            buffer.writeItem(recipe.output1);
            buffer.writeItem(recipe.output2);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static ItemStack readOutput(JsonElement element, String name) {
            ItemStack output = HbmItemOutput.fromJson(GsonHelper.convertToJsonObject(element, name))
                    .representativeStack();
            if (output.isEmpty()) {
                throw new JsonSyntaxException("Particle accelerator output cannot be empty");
            }
            return output;
        }
    }
}
