package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.registry.ModBlocks;
import java.util.Arrays;
import java.util.Locale;
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

public class PedestalRecipe implements Recipe<Container> {
    public static final int SLOT_COUNT = 9;
    public static final int CENTER_SLOT = 4;

    private final ResourceLocation id;
    private final HbmIngredient[] input;
    private final HbmItemOutput output;
    private final ExtraCondition extra;
    private final int recipeSet;
    private final int sourceOrder;

    public PedestalRecipe(ResourceLocation id, HbmIngredient[] input, HbmItemOutput output, ExtraCondition extra,
            int recipeSet, int sourceOrder) {
        if (input == null || input.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Pedestal recipe needs exactly 9 input slots: " + id);
        }
        if (output == null) {
            throw new IllegalArgumentException("Pedestal recipe needs an output: " + id);
        }
        this.id = id;
        this.input = Arrays.copyOf(input, input.length);
        this.output = output;
        this.extra = extra == null ? ExtraCondition.NONE : extra;
        this.recipeSet = recipeSet;
        this.sourceOrder = sourceOrder;
    }

    @Nullable
    public HbmIngredient input(int slot) {
        return input[slot];
    }

    public HbmIngredient[] inputs() {
        return Arrays.copyOf(input, input.length);
    }

    public HbmItemOutput output() {
        return output;
    }

    public ExtraCondition extra() {
        return extra;
    }

    public int recipeSet() {
        return recipeSet;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public ItemStack result() {
        return output.representativeStack();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (HbmIngredient ingredient : input) {
            if (ingredient != null) {
                ingredients.add(ingredient.ingredient());
            }
        }
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.PEDESTAL.get());
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
        return ModRecipes.PEDESTAL.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PEDESTAL.type().get();
    }

    public enum ExtraCondition {
        NONE,
        FULL_MOON,
        NEW_MOON,
        SUN,
        GOOD_KARMA,
        BAD_KARMA;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static ExtraCondition fromJson(String value) {
            if (value == null || value.isBlank()) {
                return NONE;
            }
            String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            try {
                return valueOf(normalized);
            } catch (IllegalArgumentException exception) {
                throw new JsonSyntaxException("Unknown pedestal extra condition '" + value + "'", exception);
            }
        }
    }

    public static class Serializer implements RecipeSerializer<PedestalRecipe> {
        @Override
        public PedestalRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmItemOutput output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"));
            HbmIngredient[] input = readInputArray(GsonHelper.getAsJsonArray(json, "input"), id);
            ExtraCondition extra = ExtraCondition.fromJson(GsonHelper.getAsString(json, "extra", "none"));
            int recipeSet = GsonHelper.getAsInt(json, "set", 0);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new PedestalRecipe(id, input, output, extra, recipeSet, sourceOrder);
        }

        @Nullable
        @Override
        public PedestalRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient[] input = new HbmIngredient[SLOT_COUNT];
            for (int i = 0; i < input.length; i++) {
                input[i] = buffer.readBoolean() ? HbmIngredient.fromNetwork(buffer) : null;
            }
            HbmItemOutput output = HbmItemOutput.fromNetwork(buffer);
            ExtraCondition[] extras = ExtraCondition.values();
            int extraOrdinal = buffer.readVarInt();
            ExtraCondition extra = extraOrdinal >= 0 && extraOrdinal < extras.length ? extras[extraOrdinal]
                    : ExtraCondition.NONE;
            int recipeSet = buffer.readVarInt();
            int sourceOrder = buffer.readVarInt();
            return new PedestalRecipe(id, input, output, extra, recipeSet, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PedestalRecipe recipe) {
            for (HbmIngredient input : recipe.input) {
                buffer.writeBoolean(input != null);
                if (input != null) {
                    input.toNetwork(buffer);
                }
            }
            recipe.output.toNetwork(buffer);
            buffer.writeVarInt(recipe.extra.ordinal());
            buffer.writeVarInt(recipe.recipeSet);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static HbmIngredient[] readInputArray(JsonArray array, ResourceLocation id) {
            if (array.size() != SLOT_COUNT) {
                throw new JsonSyntaxException("Pedestal recipe " + id + " needs exactly 9 input slots");
            }
            HbmIngredient[] input = new HbmIngredient[SLOT_COUNT];
            for (int i = 0; i < input.length; i++) {
                JsonElement element = array.get(i);
                if (element == null || element.isJsonNull()) {
                    input[i] = null;
                    continue;
                }
                input[i] = HbmIngredient.fromJson(GsonHelper.convertToJsonObject(element,
                        "pedestal input slot " + i + " in " + id));
            }
            return input;
        }
    }
}
