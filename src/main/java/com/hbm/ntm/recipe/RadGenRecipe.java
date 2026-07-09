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

public class RadGenRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final int powerPerTick;
    private final int duration;
    private final ItemStack output;
    private final int sourceOrder;

    public RadGenRecipe(ResourceLocation id, HbmIngredient input, int powerPerTick, int duration,
            ItemStack output, int sourceOrder) {
        if (powerPerTick <= 0) {
            throw new IllegalArgumentException("RadGen recipe power must be positive: " + id);
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("RadGen recipe duration must be positive: " + id);
        }
        this.id = id;
        this.input = input;
        this.powerPerTick = powerPerTick;
        this.duration = duration;
        this.output = output == null ? ItemStack.EMPTY : output.copy();
        this.sourceOrder = sourceOrder;
    }

    public HbmIngredient input() {
        return input;
    }

    public int powerPerTick() {
        return powerPerTick;
    }

    public int duration() {
        return duration;
    }

    public ItemStack output() {
        return output.copy();
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack, true);
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
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_RADGEN.get());
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
        return ModRecipes.RADGEN.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RADGEN.type().get();
    }

    public static class Serializer implements RecipeSerializer<RadGenRecipe> {
        @Override
        public RadGenRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            int power = GsonHelper.getAsInt(json, "power");
            int duration = GsonHelper.getAsInt(json, "duration");
            if (power <= 0) {
                throw new JsonSyntaxException("RadGen recipe power must be positive: " + id);
            }
            if (duration <= 0) {
                throw new JsonSyntaxException("RadGen recipe duration must be positive: " + id);
            }
            ItemStack output = json.has("output")
                    ? HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output")).representativeStack()
                    : ItemStack.EMPTY;
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", 0);
            return new RadGenRecipe(id, input, power, duration, output, sourceOrder);
        }

        @Override
        public @Nullable RadGenRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            int power = buffer.readVarInt();
            int duration = buffer.readVarInt();
            ItemStack output = buffer.readItem();
            int sourceOrder = buffer.readVarInt();
            return new RadGenRecipe(id, input, power, duration, output, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RadGenRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeVarInt(recipe.powerPerTick);
            buffer.writeVarInt(recipe.duration);
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.sourceOrder);
        }
    }
}
