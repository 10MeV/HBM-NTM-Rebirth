package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluids;
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
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenericMachineRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Machine machine;
    private final int duration;
    private final long power;
    private final List<ItemInput> itemInputs;
    private final List<HbmFluidStack> fluidInputs;
    private final List<ItemStack> itemOutputs;
    private final List<HbmFluidStack> fluidOutputs;
    private final List<String> pools;
    @Nullable
    private final String autoSwitchGroup;

    public GenericMachineRecipe(ResourceLocation id, Machine machine, int duration, long power,
            List<ItemInput> itemInputs, List<HbmFluidStack> fluidInputs,
            List<ItemStack> itemOutputs, List<HbmFluidStack> fluidOutputs,
            List<String> pools, @Nullable String autoSwitchGroup) {
        this.id = id;
        this.machine = machine;
        this.duration = Math.max(0, duration);
        this.power = Math.max(0L, power);
        this.itemInputs = List.copyOf(itemInputs);
        this.fluidInputs = List.copyOf(fluidInputs);
        this.itemOutputs = copyStacks(itemOutputs);
        this.fluidOutputs = List.copyOf(fluidOutputs);
        this.pools = List.copyOf(pools);
        this.autoSwitchGroup = autoSwitchGroup;
    }

    public Machine getMachine() {
        return machine;
    }

    public int getDuration() {
        return duration;
    }

    public long getPower() {
        return power;
    }

    public List<ItemInput> getItemInputs() {
        return itemInputs;
    }

    public List<HbmFluidStack> getFluidInputs() {
        return fluidInputs;
    }

    public List<ItemStack> getItemOutputs() {
        return copyStacks(itemOutputs);
    }

    public List<HbmFluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    public List<String> getPools() {
        return pools;
    }

    @Nullable
    public String getAutoSwitchGroup() {
        return autoSwitchGroup;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return itemOutputs.isEmpty() ? ItemStack.EMPTY : itemOutputs.get(0).copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        itemInputs.forEach(input -> ingredients.add(input.ingredient()));
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return machine == Machine.ASSEMBLY_MACHINE
                ? new ItemStack(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get())
                : new ItemStack(ModBlocks.MACHINE_BATTERY.get());
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

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    public record ItemInput(Ingredient ingredient, int count) {
        public ItemInput {
            count = Math.max(1, count);
        }
    }

    public enum Machine {
        CHEMICAL_PLANT,
        ASSEMBLY_MACHINE;

        private RecipeType<GenericMachineRecipe> type() {
            return switch (this) {
                case CHEMICAL_PLANT -> ModRecipes.CHEMICAL_PLANT.type().get();
                case ASSEMBLY_MACHINE -> ModRecipes.ASSEMBLY_MACHINE.type().get();
            };
        }

        private RecipeSerializer<GenericMachineRecipe> serializer() {
            return switch (this) {
                case CHEMICAL_PLANT -> ModRecipes.CHEMICAL_PLANT.serializer().get();
                case ASSEMBLY_MACHINE -> ModRecipes.ASSEMBLY_MACHINE.serializer().get();
            };
        }
    }

    public static class Serializer implements RecipeSerializer<GenericMachineRecipe> {
        private final Machine machine;

        public Serializer(Machine machine) {
            this.machine = machine;
        }

        @Override
        public GenericMachineRecipe fromJson(ResourceLocation id, JsonObject json) {
            int duration = GsonHelper.getAsInt(json, "duration", 0);
            long power = GsonHelper.getAsLong(json, "power", 0L);
            List<ItemInput> itemInputs = readItemInputs(GsonHelper.getAsJsonArray(json, "input_items", new JsonArray()));
            List<HbmFluidStack> fluidInputs = readFluidStacks(GsonHelper.getAsJsonArray(json, "input_fluids", new JsonArray()));
            List<ItemStack> itemOutputs = readItemOutputs(GsonHelper.getAsJsonArray(json, "output_items", new JsonArray()));
            List<HbmFluidStack> fluidOutputs = readFluidStacks(GsonHelper.getAsJsonArray(json, "output_fluids", new JsonArray()));
            List<String> pools = readStrings(GsonHelper.getAsJsonArray(json, "pools", new JsonArray()));
            String autoSwitchGroup = GsonHelper.getAsString(json, "auto_switch_group", null);
            return new GenericMachineRecipe(id, machine, duration, power, itemInputs, fluidInputs, itemOutputs, fluidOutputs, pools, autoSwitchGroup);
        }

        @Nullable
        @Override
        public GenericMachineRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int duration = buffer.readVarInt();
            long power = buffer.readVarLong();
            List<ItemInput> itemInputs = buffer.readList(input -> new ItemInput(Ingredient.fromNetwork(input), input.readVarInt()));
            List<HbmFluidStack> fluidInputs = buffer.readList(Serializer::readFluidStack);
            List<ItemStack> itemOutputs = buffer.readList(FriendlyByteBuf::readItem);
            List<HbmFluidStack> fluidOutputs = buffer.readList(Serializer::readFluidStack);
            List<String> pools = buffer.readList(FriendlyByteBuf::readUtf);
            String autoSwitchGroup = buffer.readBoolean() ? buffer.readUtf() : null;
            return new GenericMachineRecipe(id, machine, duration, power, itemInputs, fluidInputs, itemOutputs, fluidOutputs, pools, autoSwitchGroup);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericMachineRecipe recipe) {
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarLong(recipe.power);
            buffer.writeCollection(recipe.itemInputs, (output, input) -> {
                input.ingredient().toNetwork(output);
                output.writeVarInt(input.count());
            });
            buffer.writeCollection(recipe.fluidInputs, Serializer::writeFluidStack);
            buffer.writeCollection(recipe.itemOutputs, FriendlyByteBuf::writeItem);
            buffer.writeCollection(recipe.fluidOutputs, Serializer::writeFluidStack);
            buffer.writeCollection(recipe.pools, FriendlyByteBuf::writeUtf);
            buffer.writeBoolean(recipe.autoSwitchGroup != null);
            if (recipe.autoSwitchGroup != null) {
                buffer.writeUtf(recipe.autoSwitchGroup);
            }
        }

        private static List<ItemInput> readItemInputs(JsonArray array) {
            return array.asList().stream()
                    .map(element -> {
                        JsonObject object = GsonHelper.convertToJsonObject(element, "item input");
                        Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(object, "ingredient"));
                        int count = GsonHelper.getAsInt(object, "count", 1);
                        return new ItemInput(ingredient, count);
                    })
                    .toList();
        }

        private static List<ItemStack> readItemOutputs(JsonArray array) {
            return array.asList().stream()
                    .map(element -> ShapedRecipe.itemStackFromJson(GsonHelper.convertToJsonObject(element, "item output")))
                    .toList();
        }

        private static List<HbmFluidStack> readFluidStacks(JsonArray array) {
            return array.asList().stream()
                    .map(element -> {
                        JsonObject object = GsonHelper.convertToJsonObject(element, "fluid stack");
                        String fluidName = GsonHelper.getAsString(object, "fluid");
                        FluidType type = HbmFluids.fromName(fluidName.contains(":") ? new ResourceLocation(fluidName).getPath() : fluidName);
                        int amount = GsonHelper.getAsInt(object, "amount", 0);
                        int pressure = GsonHelper.getAsInt(object, "pressure", 0);
                        return new HbmFluidStack(type, amount, pressure);
                    })
                    .toList();
        }

        private static List<String> readStrings(JsonArray array) {
            return array.asList().stream()
                    .map(element -> GsonHelper.convertToString(element, "pool"))
                    .toList();
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
