package com.hbm.ntm.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluids;
import java.util.stream.Stream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public final class HbmFluidContainerIngredient extends AbstractIngredient {
    public static final ResourceLocation ID = new ResourceLocation(HbmNtm.MOD_ID, "fluid_container");
    public static final Serializer SERIALIZER = new Serializer();

    private final FluidType fluid;
    private final int amount;

    private HbmFluidContainerIngredient(FluidType fluid, int amount) {
        super(Stream.empty());
        if (fluid == null || fluid == HbmFluids.NONE) {
            throw new IllegalArgumentException("Fluid container ingredient requires a real fluid");
        }
        this.fluid = fluid;
        this.amount = Math.max(1, amount);
    }

    public static Ingredient of(FluidType fluid, int amount) {
        return new HbmFluidContainerIngredient(fluid, amount);
    }

    public static void register() {
        CraftingHelper.register(ID, SERIALIZER);
    }

    @Override
    public boolean test(ItemStack stack) {
        return HbmFluidContainerRegistry.getFluidContent(stack, fluid) == amount;
    }

    @Override
    public ItemStack[] getItems() {
        return HbmFluidContainerRegistry.getContainers(fluid).stream()
                .filter(entry -> entry.content() == amount)
                .map(HbmFluidContainerRegistry.ContainerEntry::copyFullContainer)
                .filter(stack -> !stack.isEmpty())
                .toArray(ItemStack[]::new);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", ID.toString());
        object.addProperty("fluid", new ResourceLocation(HbmNtm.MOD_ID, fluid.toPath()).toString());
        object.addProperty("amount", amount);
        return object;
    }

    public static final class Serializer implements IIngredientSerializer<HbmFluidContainerIngredient> {
        @Override
        public HbmFluidContainerIngredient parse(FriendlyByteBuf buffer) {
            return new HbmFluidContainerIngredient(HbmFluids.fromName(buffer.readUtf()), buffer.readVarInt());
        }

        @Override
        public HbmFluidContainerIngredient parse(JsonObject json) {
            FluidType fluid = HbmFluidJsonUtil.requireFluidReference(json.get("fluid"),
                    "fluid container ingredient");
            return new HbmFluidContainerIngredient(fluid, GsonHelper.getAsInt(json, "amount"));
        }

        @Override
        public void write(FriendlyByteBuf buffer, HbmFluidContainerIngredient ingredient) {
            buffer.writeUtf(ingredient.fluid.getName());
            buffer.writeVarInt(ingredient.amount);
        }
    }
}
