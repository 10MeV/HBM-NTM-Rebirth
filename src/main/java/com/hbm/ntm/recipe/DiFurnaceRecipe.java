package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

public class DiFurnaceRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<HbmIngredient> inputs;
    private final HbmItemOutput output;

    public DiFurnaceRecipe(ResourceLocation id, List<HbmIngredient> inputs, HbmItemOutput output) {
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.output = output;
        if (this.inputs.size() != 2) {
            throw new IllegalArgumentException("DiFurnace recipe needs exactly two item inputs");
        }
        if (this.output == null) {
            throw new IllegalArgumentException("DiFurnace recipe needs one item output");
        }
    }

    public List<HbmIngredient> inputs() {
        return inputs;
    }

    public HbmItemOutput output() {
        return output;
    }

    public boolean matches(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        return inputs.get(0).test(first, true) && inputs.get(1).test(second, true)
                || inputs.get(0).test(second, true) && inputs.get(1).test(first, true);
    }

    public int consumedCountForSlot(int slot, ItemStack first, ItemStack second) {
        if (slot == 0) {
            return consumedCountFor(first, second);
        }
        return consumedCountFor(second, first);
    }

    private int consumedCountFor(ItemStack stack, ItemStack other) {
        if (stack.isEmpty() || other.isEmpty()) {
            return 0;
        }
        if (inputs.get(0).test(stack, true) && inputs.get(1).test(other, true)) {
            return 1;
        }
        if (inputs.get(1).test(stack, true) && inputs.get(0).test(other, true)) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return matches(container.getItem(0), container.getItem(1));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.representativeStack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (HbmIngredient input : inputs) {
            ingredients.add(input.ingredient());
        }
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_DIFURNACE_OFF.get());
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
        return ModRecipes.DIFURNACE.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DIFURNACE.type().get();
    }

    public static class Serializer implements RecipeSerializer<DiFurnaceRecipe> {
        @Override
        public DiFurnaceRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray inputArray = GsonHelper.getAsJsonArray(json, "inputs");
            List<HbmIngredient> inputs = new ArrayList<>();
            inputArray.forEach(element -> inputs.add(HbmIngredient.fromJson(
                    GsonHelper.convertToJsonObject(element, "difurnace input"))));
            HbmItemOutput output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"));
            return new DiFurnaceRecipe(id, inputs, output);
        }

        @Nullable
        @Override
        public DiFurnaceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<HbmIngredient> inputs = buffer.readList(HbmIngredient::fromNetwork);
            HbmItemOutput output = HbmItemOutput.fromNetwork(buffer);
            return new DiFurnaceRecipe(id, inputs, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DiFurnaceRecipe recipe) {
            buffer.writeCollection(recipe.inputs, (out, input) -> input.toNetwork(out));
            recipe.output.toNetwork(buffer);
        }
    }
}
