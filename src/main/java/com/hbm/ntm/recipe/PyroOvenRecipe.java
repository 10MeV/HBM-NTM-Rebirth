package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import java.util.Optional;
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

public class PyroOvenRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Optional<HbmIngredient> inputItem;
    private final Optional<HbmFluidStack> inputFluid;
    private final Optional<HbmItemOutput> outputItem;
    private final Optional<HbmFluidStack> outputFluid;
    private final int duration;

    public PyroOvenRecipe(ResourceLocation id, Optional<HbmIngredient> inputItem, Optional<HbmFluidStack> inputFluid,
            Optional<HbmItemOutput> outputItem, Optional<HbmFluidStack> outputFluid, int duration) {
        this.id = id;
        this.inputItem = inputItem == null ? Optional.empty() : inputItem;
        this.inputFluid = inputFluid == null ? Optional.empty() : inputFluid;
        this.outputItem = outputItem == null ? Optional.empty() : outputItem;
        this.outputFluid = outputFluid == null ? Optional.empty() : outputFluid;
        this.duration = Math.max(1, duration);
    }

    public Optional<HbmIngredient> inputItem() {
        return inputItem;
    }

    public Optional<HbmFluidStack> inputFluid() {
        return inputFluid;
    }

    public Optional<HbmItemOutput> outputItem() {
        return outputItem;
    }

    public Optional<HbmFluidStack> outputFluid() {
        return outputFluid;
    }

    public int duration() {
        return duration;
    }

    @Override
    public boolean matches(Container container, Level level) {
        ItemStack stack = container.getItem(0);
        return inputItem.map(input -> input.test(stack)).orElse(stack.isEmpty());
    }

    public boolean matches(ItemStack item, FluidType fluidType) {
        if (inputItem.isPresent()) {
            if (item.isEmpty() || !inputItem.get().test(item)) {
                return false;
            }
        } else if (!item.isEmpty()) {
            return false;
        }
        return inputFluid.map(fluid -> fluid.type() == fluidType).orElse(true);
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
        return outputItem.map(HbmItemOutput::representativeStack).orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        inputItem.map(HbmIngredient::ingredient).ifPresent(ingredients::add);
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_PYROOVEN.get());
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
        return ModRecipes.PYRO_OVEN.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PYRO_OVEN.type().get();
    }

    public static class Serializer implements RecipeSerializer<PyroOvenRecipe> {
        @Override
        public PyroOvenRecipe fromJson(ResourceLocation id, JsonObject json) {
            Optional<HbmIngredient> inputItem = json.has("input_item")
                    ? Optional.of(HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input_item")))
                    : Optional.empty();
            Optional<HbmFluidStack> inputFluid = json.has("input_fluid")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "input_fluid")))
                    : Optional.empty();
            Optional<HbmItemOutput> outputItem = json.has("output_item")
                    ? Optional.of(HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output_item")))
                    : Optional.empty();
            Optional<HbmFluidStack> outputFluid = json.has("output_fluid")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "output_fluid")))
                    : Optional.empty();
            int duration = GsonHelper.getAsInt(json, "duration");
            return new PyroOvenRecipe(id, inputItem, inputFluid, outputItem, outputFluid, duration);
        }

        @Nullable
        @Override
        public PyroOvenRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Optional<HbmIngredient> inputItem = buffer.readBoolean()
                    ? Optional.of(HbmIngredient.fromNetwork(buffer))
                    : Optional.empty();
            Optional<HbmFluidStack> inputFluid = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            Optional<HbmItemOutput> outputItem = buffer.readBoolean()
                    ? Optional.of(HbmItemOutput.fromNetwork(buffer))
                    : Optional.empty();
            Optional<HbmFluidStack> outputFluid = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            return new PyroOvenRecipe(id, inputItem, inputFluid, outputItem, outputFluid, buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PyroOvenRecipe recipe) {
            buffer.writeBoolean(recipe.inputItem.isPresent());
            recipe.inputItem.ifPresent(input -> input.toNetwork(buffer));
            buffer.writeBoolean(recipe.inputFluid.isPresent());
            recipe.inputFluid.ifPresent(fluid -> writeFluidStack(buffer, fluid));
            buffer.writeBoolean(recipe.outputItem.isPresent());
            recipe.outputItem.ifPresent(output -> output.toNetwork(buffer));
            buffer.writeBoolean(recipe.outputFluid.isPresent());
            recipe.outputFluid.ifPresent(fluid -> writeFluidStack(buffer, fluid));
            buffer.writeVarInt(recipe.duration);
        }

        private static HbmFluidStack readFluidStack(JsonObject object) {
            FluidType fluid = HbmFluids.fromName(normalizeFluidName(GsonHelper.getAsString(object, "fluid")));
            int amount = GsonHelper.getAsInt(object, "amount");
            int pressure = GsonHelper.getAsInt(object, "pressure", 0);
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
