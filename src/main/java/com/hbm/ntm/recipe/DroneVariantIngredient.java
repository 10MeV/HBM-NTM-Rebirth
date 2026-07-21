package com.hbm.ntm.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.DroneItem;
import com.hbm.ntm.registry.ModItems;
import java.util.stream.Stream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

/** Datapack ingredient for the legacy drone metadata variants now carried by DroneItem NBT. */
public final class DroneVariantIngredient extends AbstractIngredient {
    public static final ResourceLocation ID = new ResourceLocation(HbmNtm.MOD_ID, "drone_variant");
    public static final Serializer SERIALIZER = new Serializer();
    private final DroneItem.DroneType variant;

    private DroneVariantIngredient(DroneItem.DroneType variant) { super(Stream.empty()); this.variant = variant; }
    public static Ingredient of(DroneItem.DroneType variant) { return new DroneVariantIngredient(variant); }
    public static void register() { CraftingHelper.register(ID, SERIALIZER); }
    @Override public boolean test(ItemStack stack) { return stack.is(ModItems.DRONE.get()) && DroneItem.typeOf(stack) == variant; }
    @Override public ItemStack[] getItems() { return new ItemStack[] { DroneItem.withType(new ItemStack(ModItems.DRONE.get()), variant) }; }
    @Override public boolean isSimple() { return false; }
    @Override public boolean isEmpty() { return false; }
    @Override public IIngredientSerializer<? extends Ingredient> getSerializer() { return SERIALIZER; }
    @Override public JsonElement toJson() { JsonObject json = new JsonObject(); json.addProperty("type", ID.toString()); json.addProperty("variant", variant.serializedName()); return json; }

    public static final class Serializer implements IIngredientSerializer<DroneVariantIngredient> {
        @Override public DroneVariantIngredient parse(FriendlyByteBuf buffer) { return new DroneVariantIngredient(DroneItem.DroneType.byName(buffer.readUtf())); }
        @Override public DroneVariantIngredient parse(JsonObject json) { return new DroneVariantIngredient(DroneItem.DroneType.byName(GsonHelper.getAsString(json, "variant"))); }
        @Override public void write(FriendlyByteBuf buffer, DroneVariantIngredient ingredient) { buffer.writeUtf(ingredient.variant.serializedName()); }
    }
}
