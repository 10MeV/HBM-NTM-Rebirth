package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemProcessingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Machine machine;
    private final HbmIngredient input;
    private final List<HbmItemOutput> outputs;
    private final Optional<HbmFluidStack> fluidInput;
    private final int duration;
    private final float productivity;

    public ItemProcessingRecipe(ResourceLocation id, Machine machine, HbmIngredient input,
            List<HbmItemOutput> outputs, Optional<HbmFluidStack> fluidInput, int duration, float productivity) {
        this.id = id;
        this.machine = machine;
        this.input = input;
        this.outputs = List.copyOf(outputs);
        this.fluidInput = fluidInput == null ? Optional.empty() : fluidInput;
        this.duration = Math.max(0, duration);
        this.productivity = Math.max(0.0F, productivity);
    }

    public Machine machine() {
        return machine;
    }

    public HbmIngredient input() {
        return input;
    }

    public List<HbmItemOutput> outputs() {
        return outputs;
    }

    public Optional<HbmFluidStack> fluidInput() {
        return fluidInput;
    }

    public int duration() {
        return duration;
    }

    public float productivity() {
        return productivity;
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack, true);
    }

    public boolean matches(ItemStack stack, FluidType fluidType) {
        return matches(stack) && fluidInput.map(fluid -> fluid.type() == fluidType).orElse(true);
    }

    public List<ItemStack> outputStacks() {
        return outputs.stream()
                .map(HbmItemOutput::representativeStack)
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    public List<ItemStack> rollOutputStacks(RandomSource random) {
        return outputs.stream()
                .map(output -> output.collapse(random))
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return input.test(container.getItem(0));
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
        return outputs.isEmpty() ? ItemStack.EMPTY : outputs.get(0).representativeStack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return switch (machine) {
            case SHREDDER -> new ItemStack(ModBlocks.MACHINE_SHREDDER.get());
            case CENTRIFUGE -> new ItemStack(ModBlocks.MACHINE_CENTRIFUGE.get());
            case CRYSTALLIZER -> new ItemStack(ModBlocks.MACHINE_CRYSTALLIZER.get());
        };
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
        return machine.serializer();
    }

    @Override
    public RecipeType<?> getType() {
        return machine.type();
    }

    public enum Machine {
        SHREDDER(1),
        CENTRIFUGE(4),
        CRYSTALLIZER(1);

        private final int maxOutputs;

        Machine(int maxOutputs) {
            this.maxOutputs = maxOutputs;
        }

        public RecipeType<ItemProcessingRecipe> type() {
            return switch (this) {
                case SHREDDER -> ModRecipes.SHREDDER.type().get();
                case CENTRIFUGE -> ModRecipes.CENTRIFUGE.type().get();
                case CRYSTALLIZER -> ModRecipes.CRYSTALLIZER.type().get();
            };
        }

        public RecipeSerializer<ItemProcessingRecipe> serializer() {
            return switch (this) {
                case SHREDDER -> ModRecipes.SHREDDER.serializer().get();
                case CENTRIFUGE -> ModRecipes.CENTRIFUGE.serializer().get();
                case CRYSTALLIZER -> ModRecipes.CRYSTALLIZER.serializer().get();
            };
        }
    }

    public static class Serializer implements RecipeSerializer<ItemProcessingRecipe> {
        private final Machine machine;

        public Serializer(Machine machine) {
            this.machine = machine;
        }

        @Override
        public ItemProcessingRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient input = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            List<HbmItemOutput> outputs = readOutputs(json);
            if (outputs.isEmpty()) {
                throw new com.google.gson.JsonSyntaxException(machine + " recipe " + id + " has no outputs");
            }
            if (outputs.size() > machine.maxOutputs) {
                throw new com.google.gson.JsonSyntaxException(machine + " recipe " + id + " has too many outputs: "
                        + outputs.size() + " > " + machine.maxOutputs);
            }
            Optional<HbmFluidStack> fluidInput = json.has("fluid")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "fluid")))
                    : Optional.empty();
            int duration = GsonHelper.getAsInt(json, "duration", 0);
            float productivity = GsonHelper.getAsFloat(json, "productivity", 0.0F);
            return new ItemProcessingRecipe(id, machine, input, outputs, fluidInput, duration, productivity);
        }

        @Nullable
        @Override
        public ItemProcessingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient input = HbmIngredient.fromNetwork(buffer);
            List<HbmItemOutput> outputs = buffer.readList(HbmItemOutput::fromNetwork);
            Optional<HbmFluidStack> fluidInput = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            int duration = buffer.readVarInt();
            float productivity = buffer.readFloat();
            return new ItemProcessingRecipe(id, machine, input, outputs, fluidInput, duration, productivity);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ItemProcessingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeCollection(recipe.outputs, (output, itemOutput) -> itemOutput.toNetwork(output));
            buffer.writeBoolean(recipe.fluidInput.isPresent());
            recipe.fluidInput.ifPresent(fluid -> writeFluidStack(buffer, fluid));
            buffer.writeVarInt(recipe.duration);
            buffer.writeFloat(recipe.productivity);
        }

        private static List<HbmItemOutput> readOutputs(JsonObject json) {
            if (json.has("outputs")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "outputs");
                return array.asList().stream()
                        .map(element -> HbmItemOutput.fromJson(GsonHelper.convertToJsonObject(element, "item output")))
                        .toList();
            }
            return List.of(HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output")));
        }

        private HbmFluidStack readFluidStack(JsonObject object) {
            return HbmFluidJsonUtil.readFluidStack(object, machine.name().toLowerCase(java.util.Locale.ROOT)
                    + " fluid input");
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
