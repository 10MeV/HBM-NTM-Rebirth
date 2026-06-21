package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.util.HbmRegistryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class OutgasserRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmIngredient input;
    private final Optional<ItemStack> solidOutput;
    private final Optional<HbmFluidStack> fluidOutput;
    private final boolean fusionOnly;

    public OutgasserRecipe(ResourceLocation id, HbmIngredient input, Optional<ItemStack> solidOutput,
            Optional<HbmFluidStack> fluidOutput, boolean fusionOnly) {
        if (solidOutput.isEmpty() && fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Outgasser recipe must have a solid or fluid output");
        }
        this.id = id;
        this.input = input;
        this.solidOutput = solidOutput.map(ItemStack::copy);
        this.fluidOutput = fluidOutput;
        this.fusionOnly = fusionOnly;
    }

    public HbmIngredient input() {
        return input;
    }

    public Optional<ItemStack> solidOutput() {
        return solidOutput.map(ItemStack::copy);
    }

    public Optional<HbmFluidStack> fluidOutput() {
        return fluidOutput;
    }

    public boolean fusionOnly() {
        return fusionOnly;
    }

    public boolean matches(ItemStack stack) {
        return !fusionOnly && input.test(stack);
    }

    public boolean matchesFusion(ItemStack stack) {
        return fusionOnly && input.test(stack);
    }

    public boolean matchesFusionBreeder(ItemStack stack) {
        return input.test(stack, true);
    }

    public boolean matchesIgnoringCount(ItemStack stack) {
        return !fusionOnly && input.test(stack, true);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return matches(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return solidOutput().orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.RBMK_OUTGASSER.get());
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
        return ModRecipes.OUTGASSER.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.OUTGASSER.type().get();
    }

    public static class Serializer implements RecipeSerializer<OutgasserRecipe> {
        @Override
        public OutgasserRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            Optional<ItemStack> solidOutput = json.has("solid_output")
                    ? Optional.of(readItemStack(GsonHelper.getAsJsonObject(json, "solid_output"), "solid_output"))
                    : Optional.empty();
            Optional<HbmFluidStack> fluidOutput = json.has("fluid_output")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "fluid_output")))
                    : Optional.empty();
            boolean fusionOnly = GsonHelper.getAsBoolean(json, "fusion_only", false);
            if (solidOutput.isEmpty() && fluidOutput.isEmpty()) {
                throw new JsonSyntaxException("Outgasser recipe " + id + " has no outputs");
            }
            return new OutgasserRecipe(id, input, solidOutput, fluidOutput, fusionOnly);
        }

        @Nullable
        @Override
        public OutgasserRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            Optional<ItemStack> solidOutput = buffer.readBoolean()
                    ? Optional.of(buffer.readItem())
                    : Optional.empty();
            Optional<HbmFluidStack> fluidOutput = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            boolean fusionOnly = buffer.readBoolean();
            return new OutgasserRecipe(id, input, solidOutput, fluidOutput, fusionOnly);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OutgasserRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeBoolean(recipe.solidOutput.isPresent());
            recipe.solidOutput.ifPresent(buffer::writeItem);
            buffer.writeBoolean(recipe.fluidOutput.isPresent());
            recipe.fluidOutput.ifPresent(fluid -> writeFluidStack(buffer, fluid));
            buffer.writeBoolean(recipe.fusionOnly);
        }

        private static ItemStack readItemStack(JsonObject object, String name) {
            String itemName = GsonHelper.getAsString(object, "item");
            ResourceLocation itemId = new ResourceLocation(itemName);
            Item item = HbmRegistryUtil.item(itemId)
                    .orElseThrow(() -> new JsonSyntaxException("Unknown item '" + itemName + "' in " + name));
            int count = GsonHelper.getAsInt(object, "count", 1);
            if (count < 1) {
                throw new JsonSyntaxException("Invalid item count " + count + " in " + name);
            }
            return new ItemStack(item, count);
        }

        private static HbmFluidStack readFluidStack(JsonObject object) {
            FluidType fluid = HbmFluids.fromName(normalizeFluidName(GsonHelper.getAsString(object, "fluid")));
            int amount = GsonHelper.getAsInt(object, "amount");
            int pressure = GsonHelper.getAsInt(object, "pressure", 0);
            if (fluid == HbmFluids.NONE || amount <= 0) {
                throw new JsonSyntaxException("Invalid outgasser fluid output");
            }
            return new HbmFluidStack(fluid, amount, pressure);
        }

        private static HbmFluidStack readFluidStack(FriendlyByteBuf buffer) {
            return new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(), buffer.readVarInt());
        }

        private static void writeFluidStack(FriendlyByteBuf buffer, HbmFluidStack stack) {
            buffer.writeUtf(stack.type().getName());
            buffer.writeVarInt(stack.amount());
            buffer.writeVarInt(stack.pressure());
        }

        private static String normalizeFluidName(String name) {
            if (name.indexOf(':') < 0) {
                return name;
            }
            ResourceLocation id = ResourceLocation.tryParse(name);
            return id == null ? name : id.getPath();
        }
    }
}
