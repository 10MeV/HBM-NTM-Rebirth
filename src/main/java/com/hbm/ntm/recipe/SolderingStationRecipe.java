package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.fluid.FluidType;
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
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SolderingStationRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<HbmIngredient> toppings;
    private final List<HbmIngredient> pcb;
    private final List<HbmIngredient> solder;
    private final Optional<HbmFluidStack> fluid;
    private final ItemStack output;
    private final int duration;
    private final long consumption;
    private final int sourceOrder;

    public SolderingStationRecipe(ResourceLocation id, List<HbmIngredient> toppings, List<HbmIngredient> pcb,
            List<HbmIngredient> solder, Optional<HbmFluidStack> fluid, ItemStack output, int duration,
            long consumption, int sourceOrder) {
        this.id = id;
        this.toppings = List.copyOf(toppings == null ? List.of() : toppings);
        this.pcb = List.copyOf(pcb == null ? List.of() : pcb);
        this.solder = List.copyOf(solder == null ? List.of() : solder);
        this.fluid = fluid == null ? Optional.empty() : fluid;
        this.output = output == null ? ItemStack.EMPTY : output.copy();
        this.duration = Math.max(1, duration);
        this.consumption = Math.max(1L, consumption);
        this.sourceOrder = sourceOrder;
        if (this.output.isEmpty()) {
            throw new IllegalArgumentException("Soldering station output cannot be empty");
        }
    }

    public List<HbmIngredient> toppings() {
        return toppings;
    }

    public List<HbmIngredient> pcb() {
        return pcb;
    }

    public List<HbmIngredient> solder() {
        return solder;
    }

    public Optional<HbmFluidStack> fluid() {
        return fluid;
    }

    public ItemStack output() {
        return output.copy();
    }

    public int duration() {
        return duration;
    }

    public long consumption() {
        return consumption;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public boolean matches(ItemStack[] toppings, ItemStack[] pcb, ItemStack solder) {
        return SolderingStationRecipeRuntime.matchesGroup(toppings, this.toppings)
                && SolderingStationRecipeRuntime.matchesGroup(pcb, this.pcb)
                && SolderingStationRecipeRuntime.matchesGroup(new ItemStack[] { solder }, this.solder);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        toppings.forEach(input -> ingredients.add(input.ingredient()));
        pcb.forEach(input -> ingredients.add(input.ingredient()));
        solder.forEach(input -> ingredients.add(input.ingredient()));
        return ingredients;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_SOLDERING_STATION.get());
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
        return ModRecipes.SOLDERING_STATION.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.SOLDERING_STATION.type().get();
    }

    public static class Serializer implements RecipeSerializer<SolderingStationRecipe> {
        @Override
        public SolderingStationRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<HbmIngredient> toppings = readIngredients(GsonHelper.getAsJsonArray(json, "toppings"),
                    "toppings");
            List<HbmIngredient> pcb = readIngredients(GsonHelper.getAsJsonArray(json, "pcb"), "pcb");
            List<HbmIngredient> solder = readIngredients(GsonHelper.getAsJsonArray(json, "solder"), "solder");
            Optional<HbmFluidStack> fluid = json.has("fluid")
                    ? Optional.of(readFluidStack(GsonHelper.getAsJsonObject(json, "fluid")))
                    : Optional.empty();
            ItemStack output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"))
                    .representativeStack();
            int duration = GsonHelper.getAsInt(json, "duration");
            long consumption = GsonHelper.getAsLong(json, "consumption");
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", 0);
            if (output.isEmpty()) {
                throw new JsonSyntaxException("Soldering station recipe " + id + " has no output");
            }
            return new SolderingStationRecipe(id, toppings, pcb, solder, fluid, output, duration, consumption,
                    sourceOrder);
        }

        @Nullable
        @Override
        public SolderingStationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<HbmIngredient> toppings = buffer.readList(HbmIngredient::fromNetwork);
            List<HbmIngredient> pcb = buffer.readList(HbmIngredient::fromNetwork);
            List<HbmIngredient> solder = buffer.readList(HbmIngredient::fromNetwork);
            Optional<HbmFluidStack> fluid = buffer.readBoolean()
                    ? Optional.of(readFluidStack(buffer))
                    : Optional.empty();
            ItemStack output = buffer.readItem();
            int duration = buffer.readVarInt();
            long consumption = buffer.readVarLong();
            int sourceOrder = buffer.readVarInt();
            return new SolderingStationRecipe(id, toppings, pcb, solder, fluid, output, duration, consumption,
                    sourceOrder);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SolderingStationRecipe recipe) {
            buffer.writeCollection(recipe.toppings, (output, input) -> input.toNetwork(output));
            buffer.writeCollection(recipe.pcb, (output, input) -> input.toNetwork(output));
            buffer.writeCollection(recipe.solder, (output, input) -> input.toNetwork(output));
            buffer.writeBoolean(recipe.fluid.isPresent());
            recipe.fluid.ifPresent(fluid -> writeFluidStack(buffer, fluid));
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.duration);
            buffer.writeVarLong(recipe.consumption);
            buffer.writeVarInt(recipe.sourceOrder);
        }

        private static List<HbmIngredient> readIngredients(JsonArray array, String name) {
            if (array.size() > 3) {
                throw new JsonSyntaxException("Too many soldering station " + name + " inputs");
            }
            return array.asList().stream()
                    .map(element -> HbmIngredient.fromJson(GsonHelper.convertToJsonObject(element, name)))
                    .toList();
        }

        private static HbmFluidStack readFluidStack(JsonObject object) {
            FluidType fluid = HbmFluids.fromName(normalizeFluidName(GsonHelper.getAsString(object, "fluid")));
            int amount = GsonHelper.getAsInt(object, "amount");
            int pressure = GsonHelper.getAsInt(object, "pressure", 0);
            if (fluid == HbmFluids.NONE || amount <= 0) {
                throw new JsonSyntaxException("Invalid soldering station fluid input");
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
