package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
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

public class MixerRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final HbmFluidStack output;
    private final Optional<HbmFluidStack> input1;
    private final Optional<HbmFluidStack> input2;
    private final Optional<HbmIngredient> solidInput;
    private final int duration;
    private final int sourceOrder;

    public MixerRecipe(ResourceLocation id, HbmFluidStack output, Optional<HbmFluidStack> input1,
            Optional<HbmFluidStack> input2, Optional<HbmIngredient> solidInput, int duration, int sourceOrder) {
        this.id = id;
        this.output = output == null ? new HbmFluidStack(HbmFluids.NONE, 0) : output;
        this.input1 = input1 == null ? Optional.empty() : input1;
        this.input2 = input2 == null ? Optional.empty() : input2;
        this.solidInput = solidInput == null ? Optional.empty() : solidInput;
        this.duration = Math.max(1, duration);
        this.sourceOrder = sourceOrder;
        if (this.output.isEmpty()) {
            throw new IllegalArgumentException("Mixer recipe must have a fluid output");
        }
    }

    public HbmFluidStack output() {
        return output;
    }

    public Optional<HbmFluidStack> input1() {
        return input1;
    }

    public Optional<HbmFluidStack> input2() {
        return input2;
    }

    public Optional<HbmIngredient> solidInput() {
        return solidInput;
    }

    public int duration() {
        return duration;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return solidInput.map(input -> input.test(container.getItem(0))).orElse(true);
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
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        solidInput.ifPresent(input -> ingredients.add(input.ingredient()));
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_MIXER.get());
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
        return ModRecipes.MIXER.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIXER.type().get();
    }

    public static class Serializer implements RecipeSerializer<MixerRecipe> {
        @Override
        public MixerRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmFluidStack output = readFluidStack(GsonHelper.getAsJsonObject(json, "output"));
            Optional<HbmFluidStack> input1 = json.has("input1")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "input1")))
                    : Optional.empty();
            Optional<HbmFluidStack> input2 = json.has("input2")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "input2")))
                    : Optional.empty();
            Optional<HbmIngredient> solidInput = json.has("solid_input")
                    ? Optional.of(HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "solid_input")))
                    : Optional.empty();
            int duration = GsonHelper.getAsInt(json, "duration", 20);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            return new MixerRecipe(id, output, input1, input2, solidInput, duration, sourceOrder);
        }

        @Nullable
        @Override
        public MixerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmFluidStack output = readFluidStack(buffer);
            Optional<HbmFluidStack> input1 = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            Optional<HbmFluidStack> input2 = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            Optional<HbmIngredient> solidInput = buffer.readBoolean()
                    ? Optional.of(HbmIngredient.fromNetwork(buffer))
                    : Optional.empty();
            int duration = buffer.readVarInt();
            int sourceOrder = buffer.readVarInt();
            return new MixerRecipe(id, output, input1, input2, solidInput, duration, sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MixerRecipe recipe) {
            writeFluidStack(buffer, recipe.output);
            buffer.writeBoolean(recipe.input1.isPresent());
            recipe.input1.ifPresent(input -> writeFluidStack(buffer, input));
            buffer.writeBoolean(recipe.input2.isPresent());
            recipe.input2.ifPresent(input -> writeFluidStack(buffer, input));
            buffer.writeBoolean(recipe.solidInput.isPresent());
            recipe.solidInput.ifPresent(input -> input.toNetwork(buffer));
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static HbmFluidStack readFluidStack(JsonObject object) {
            return HbmFluidJsonUtil.readFluidStack(object, "mixer fluid stack");
        }

        private static HbmFluidStack readFluidStack(FriendlyByteBuf buffer) {
            return new HbmFluidStack(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt(), buffer.readVarInt());
        }

        private static void writeFluidStack(FriendlyByteBuf buffer, HbmFluidStack stack) {
            buffer.writeUtf(stack.type().getName());
            buffer.writeVarInt(stack.amount());
            buffer.writeVarInt(stack.pressure());
        }
    }
}
