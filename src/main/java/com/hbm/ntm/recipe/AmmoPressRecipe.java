package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class AmmoPressRecipe implements Recipe<Container> {
    public static final int INPUT_SLOTS = 9;

    private final ResourceLocation id;
    private final HbmIngredient[] inputs;
    private final ItemStack output;
    private final int sourceOrder;

    public AmmoPressRecipe(ResourceLocation id, HbmIngredient[] inputs, ItemStack output, int sourceOrder) {
        if (inputs.length != INPUT_SLOTS) {
            throw new IllegalArgumentException("Ammo press recipe must have exactly 9 input slots");
        }
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("Ammo press recipe output cannot be empty");
        }
        this.id = id;
        this.inputs = Arrays.copyOf(inputs, inputs.length);
        this.output = output.copy();
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient[] inputs() {
        return Arrays.copyOf(inputs, inputs.length);
    }

    @Nullable
    public HbmIngredient input(int slot) {
        return slot >= 0 && slot < inputs.length ? inputs[slot] : null;
    }

    public ItemStack output() {
        return output.copy();
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matchesGrid(IItemHandler items) {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            HbmIngredient input = inputs[i];
            ItemStack stack = items.getStackInSlot(i);
            if (input == null) {
                if (!stack.isEmpty()) {
                    return false;
                }
            } else if (!input.test(stack)) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesSlot(int slot, ItemStack stack) {
        HbmIngredient input = input(slot);
        return input != null && input.test(stack, true);
    }

    public List<ItemStack> displayInputs(int slot) {
        HbmIngredient input = input(slot);
        return input == null ? List.of() : input.displayStacks();
    }

    @Override
    public boolean matches(Container container, net.minecraft.world.level.Level level) {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            HbmIngredient input = inputs[i];
            ItemStack stack = i < container.getContainerSize() ? container.getItem(i) : ItemStack.EMPTY;
            if (input == null) {
                if (!stack.isEmpty()) {
                    return false;
                }
            } else if (!input.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (HbmIngredient input : inputs) {
            if (input != null) {
                ingredients.add(input.ingredient());
            }
        }
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_AMMO_PRESS.get());
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
        return ModRecipes.AMMO_PRESS.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.AMMO_PRESS.type().get();
    }

    public static class Serializer implements RecipeSerializer<AmmoPressRecipe> {
        @Override
        public AmmoPressRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient[] inputs = readInputs(GsonHelper.getAsJsonArray(json, "input"));
            ItemStack output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output")).representativeStack();
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new AmmoPressRecipe(id, inputs, output, sourceOrder);
        }

        @Nullable
        @Override
        public AmmoPressRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient[] inputs = new HbmIngredient[INPUT_SLOTS];
            for (int i = 0; i < INPUT_SLOTS; i++) {
                inputs[i] = buffer.readBoolean() ? HbmIngredient.fromNetwork(buffer) : null;
            }
            ItemStack output = buffer.readItem();
            int sourceOrder = buffer.readVarInt();
            return new AmmoPressRecipe(id, inputs, output, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AmmoPressRecipe recipe) {
            for (HbmIngredient input : recipe.inputs) {
                buffer.writeBoolean(input != null);
                if (input != null) {
                    input.toNetwork(buffer);
                }
            }
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static HbmIngredient[] readInputs(JsonArray array) {
            if (array.size() != INPUT_SLOTS) {
                throw new JsonSyntaxException("Ammo press recipe input must have exactly 9 entries");
            }
            HbmIngredient[] inputs = new HbmIngredient[INPUT_SLOTS];
            for (int i = 0; i < INPUT_SLOTS; i++) {
                JsonElement element = array.get(i);
                inputs[i] = element == null || element.isJsonNull()
                        ? null
                        : HbmIngredient.fromJson(GsonHelper.convertToJsonObject(element, "ammo press input"));
            }
            return inputs;
        }
    }
}
