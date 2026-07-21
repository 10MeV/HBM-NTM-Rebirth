package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy OreDictionary recipes whose output was the first item registered to a tag.
 * The recipe deliberately has no result while the output tag is empty.
 */
public final class TagOutputShapelessRecipe extends CustomRecipe {
    private final Ingredient ingredient;
    private final TagKey<Item> outputTag;
    private final int count;

    public TagOutputShapelessRecipe(ResourceLocation id, CraftingBookCategory category, Ingredient ingredient,
            TagKey<Item> outputTag, int count) {
        super(id, category);
        this.ingredient = ingredient;
        this.outputTag = outputTag;
        this.count = count;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return !resolveOutput().isEmpty() && singleIngredient(container);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        return singleIngredient(container) ? resolveOutput() : ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return resolveOutput();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.TAG_OUTPUT_SHAPELESS.get();
    }

    private boolean singleIngredient(CraftingContainer container) {
        boolean found = false;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (found || !ingredient.test(stack)) {
                return false;
            }
            found = true;
        }
        return found;
    }

    private ItemStack resolveOutput() {
        ItemStack[] entries = Ingredient.of(outputTag).getItems();
        if (entries.length == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = entries[0].copy();
        result.setCount(count);
        return result;
    }

    public static final class Serializer implements RecipeSerializer<TagOutputShapelessRecipe> {
        @Override
        public TagOutputShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ResourceLocation outputTag = new ResourceLocation(GsonHelper.getAsString(json, "output_tag"));
            int count = GsonHelper.getAsInt(json, "count");
            CraftingBookCategory category = json.has("category")
                    ? CraftingBookCategory.valueOf(GsonHelper.getAsString(json, "category").toUpperCase(java.util.Locale.ROOT))
                    : CraftingBookCategory.MISC;
            if (count < 1) {
                throw new IllegalArgumentException("Tag-output recipe " + id + " requires a positive output count");
            }
            return new TagOutputShapelessRecipe(id, category, ingredient,
                    TagKey.create(Registries.ITEM, outputTag), count);
        }

        @Nullable
        @Override
        public TagOutputShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.fromNetwork(buffer);
            TagKey<Item> outputTag = TagKey.create(Registries.ITEM, buffer.readResourceLocation());
            int count = buffer.readVarInt();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            return new TagOutputShapelessRecipe(id, category, ingredient, outputTag, count);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, TagOutputShapelessRecipe recipe) {
            recipe.ingredient.toNetwork(buffer);
            buffer.writeResourceLocation(recipe.outputTag.location());
            buffer.writeVarInt(recipe.count);
            buffer.writeEnum(recipe.category());
        }
    }
}
