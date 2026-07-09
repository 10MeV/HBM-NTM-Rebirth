package com.hbm.ntm.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.item.HotItem;
import com.hbm.ntm.registry.ModItems;
import java.util.Locale;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AnvilSmithingRecipe implements Recipe<Container> {
    public static final String TAG_CYANIDE = "ntmCyanide";
    public static final String TAG_RED_PILL = "ntmRedPill";

    private final ResourceLocation id;
    private final HbmIngredient left;
    private final HbmIngredient right;
    private final HbmItemOutput output;
    private final int tier;
    private final boolean shapeless;
    private final int sourceOrder;
    private final Kind kind;
    @Nullable
    private final String moldPrefix;

    public AnvilSmithingRecipe(ResourceLocation id, HbmIngredient left, HbmIngredient right, HbmItemOutput output,
            int tier, boolean shapeless, int sourceOrder) {
        this(id, left, right, output, tier, shapeless, sourceOrder, Kind.STANDARD, null);
    }

    public AnvilSmithingRecipe(ResourceLocation id, HbmIngredient left, HbmIngredient right, HbmItemOutput output,
            int tier, boolean shapeless, int sourceOrder, Kind kind, @Nullable String moldPrefix) {
        if (left == null) {
            throw new IllegalArgumentException("Anvil smithing recipe needs a left input");
        }
        if (right == null) {
            throw new IllegalArgumentException("Anvil smithing recipe needs a right input");
        }
        if (output == null) {
            throw new IllegalArgumentException("Anvil smithing recipe needs an output");
        }
        this.id = id;
        this.left = left;
        this.right = right;
        this.output = output;
        this.tier = Math.max(0, tier);
        this.shapeless = shapeless;
        this.sourceOrder = sourceOrder;
        this.kind = kind == null ? Kind.STANDARD : kind;
        this.moldPrefix = normalizeMoldPrefix(moldPrefix);
        if (this.kind == Kind.MOLD_PREFIX && this.moldPrefix == null) {
            throw new IllegalArgumentException("Mold prefix anvil smithing recipe needs mold_prefix");
        }
    }

    public HbmIngredient left() {
        return left;
    }

    public HbmIngredient right() {
        return right;
    }

    public HbmItemOutput output() {
        return output;
    }

    public int tier() {
        return tier;
    }

    public boolean shapeless() {
        return shapeless;
    }

    public int sourceOrder() {
        return sourceOrder;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    public String moldPrefix() {
        return moldPrefix;
    }

    public boolean isTierValid(int anvilTier) {
        return tier <= anvilTier;
    }

    public Match match(ItemStack leftStack, ItemStack rightStack, int anvilTier) {
        if (!isTierValid(anvilTier) || leftStack.isEmpty() || rightStack.isEmpty()) {
            return Match.NONE;
        }
        return switch (kind) {
            case MOLD_PREFIX -> matchesMoldPrefix(leftStack, rightStack) ? new Match(true, false) : Match.NONE;
            case MOLD_EXACT -> matchesMoldExact(leftStack, rightStack) ? new Match(true, false) : Match.NONE;
            case CYANIDE -> matchesCyanide(leftStack, rightStack) ? new Match(true, false) : Match.NONE;
            case RENAME -> matchesRename(leftStack, rightStack) ? new Match(true, false) : Match.NONE;
            case STANDARD, HOT -> matchStandard(leftStack, rightStack);
        };
    }

    private Match matchStandard(ItemStack leftStack, ItemStack rightStack) {
        if (inputMatches(leftStack, left) && inputMatches(rightStack, right)) {
            return new Match(true, false);
        }
        if (shapeless && inputMatches(rightStack, left) && inputMatches(leftStack, right)) {
            return new Match(true, true);
        }
        return Match.NONE;
    }

    private boolean inputMatches(ItemStack stack, HbmIngredient ingredient) {
        if (kind == Kind.HOT && HotItem.isHotItem(stack) && !HotItem.hasUsableHeat(stack)) {
            return false;
        }
        return ingredient.test(stack);
    }

    private boolean matchesMoldPrefix(ItemStack leftStack, ItemStack rightStack) {
        return right.test(rightStack) && leftStack.getCount() == left.count()
                && hasMoldPrefixTag(leftStack, moldPrefix);
    }

    private boolean matchesMoldExact(ItemStack leftStack, ItemStack rightStack) {
        return right.test(rightStack) && leftStack.getCount() == left.count() && left.test(leftStack);
    }

    private boolean matchesCyanide(ItemStack leftStack, ItemStack rightStack) {
        return leftStack.getItem().isEdible() && right.test(rightStack)
                && (rightStack.is(ModItems.PLAN_C.get()) || rightStack.is(ModItems.PILL_RED.get()));
    }

    private boolean matchesRename(ItemStack leftStack, ItemStack rightStack) {
        return !leftStack.isEmpty() && rightStack.is(Items.NAME_TAG) && rightStack.hasCustomHoverName();
    }

    public ItemStack result() {
        return output.representativeStack();
    }

    public ItemStack result(ItemStack leftStack, ItemStack rightStack) {
        return switch (kind) {
            case HOT -> hotResult(leftStack, rightStack);
            case CYANIDE -> cyanideResult(leftStack, rightStack);
            case RENAME -> renameResult(leftStack, rightStack);
            case STANDARD, MOLD_PREFIX, MOLD_EXACT -> result();
        };
    }

    private ItemStack hotResult(ItemStack leftStack, ItemStack rightStack) {
        ItemStack result = output.representativeStack();
        if (HotItem.isHotItem(leftStack) && HotItem.isHotItem(rightStack) && HotItem.isHotItem(result)) {
            HotItem.heatUp(result, (HotItem.heatRatio(leftStack) + HotItem.heatRatio(rightStack)) / 2.0D);
        }
        return result;
    }

    private ItemStack cyanideResult(ItemStack leftStack, ItemStack rightStack) {
        ItemStack result = leftStack.copyWithCount(1);
        if (rightStack.is(ModItems.PLAN_C.get())) {
            result.getOrCreateTag().putBoolean(TAG_CYANIDE, true);
        } else if (rightStack.is(ModItems.PILL_RED.get())) {
            result.getOrCreateTag().putBoolean(TAG_RED_PILL, true);
        }
        return result;
    }

    private ItemStack renameResult(ItemStack leftStack, ItemStack rightStack) {
        ItemStack result = leftStack.copyWithCount(1);
        String name = rightStack.getHoverName().getString().replace("\\&", "\u00A7");
        result.setHoverName(Component.literal("\u00A7r" + name));
        return result;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container == null || container.getContainerSize() < 2) {
            return false;
        }
        return match(container.getItem(0), container.getItem(1), Integer.MAX_VALUE).matches();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        if (container == null || container.getContainerSize() < 2) {
            return ItemStack.EMPTY;
        }
        return result(container.getItem(0), container.getItem(1));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(left.ingredient());
        ingredients.add(right.ingredient());
        return ingredients;
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
        return ModRecipes.ANVIL_SMITHING.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ANVIL_SMITHING.type().get();
    }

    private static boolean hasMoldPrefixTag(ItemStack stack, @Nullable String prefix) {
        String directory = moldTagDirectory(prefix);
        if (directory == null) {
            return false;
        }
        if (hasLongerMoldShape(stack, directory)) {
            return false;
        }
        return stack.getTags().anyMatch(tag -> isForgeTagInDirectory(tag, directory));
    }

    private static boolean hasLongerMoldShape(ItemStack stack, String directory) {
        if ("plates".equals(directory)) {
            return stack.getTags().anyMatch(tag -> isForgeTagInDirectory(tag, "cast_plates")
                    || isForgeTagInDirectory(tag, "welded_plates"));
        }
        if ("wires".equals(directory)) {
            return stack.getTags().anyMatch(tag -> isForgeTagInDirectory(tag, "dense_wires"));
        }
        return false;
    }

    private static boolean isForgeTagInDirectory(TagKey<Item> tag, String directory) {
        ResourceLocation location = tag.location();
        String path = location.getPath();
        return "forge".equals(location.getNamespace())
                && (path.equals(directory) || path.startsWith(directory + "/"));
    }

    @Nullable
    private static String moldTagDirectory(@Nullable String prefix) {
        String normalized = normalizeMoldPrefix(prefix);
        if (normalized == null) {
            return null;
        }
        return switch (normalized) {
            case "nugget" -> "nuggets";
            case "billet" -> "billets";
            case "ingot" -> "ingots";
            case "plate" -> "plates";
            case "platetriple", "platecast" -> "cast_plates";
            case "wirefine" -> "wires";
            case "wiredense" -> "dense_wires";
            case "shell" -> "shells";
            case "pipe", "ntmpipe" -> "pipes";
            case "block" -> "storage_blocks";
            default -> normalized;
        };
    }

    @Nullable
    private static String normalizeMoldPrefix(@Nullable String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return null;
        }
        return prefix.replace("_", "").toLowerCase(Locale.ROOT);
    }

    public record Match(boolean matches, boolean mirrored) {
        public static final Match NONE = new Match(false, false);

        public int consumeLeft(AnvilSmithingRecipe recipe) {
            return switch (recipe.kind) {
                case MOLD_PREFIX, MOLD_EXACT -> 0;
                case CYANIDE, RENAME -> 1;
                case STANDARD, HOT -> mirrored ? recipe.right.count() : recipe.left.count();
            };
        }

        public int consumeRight(AnvilSmithingRecipe recipe) {
            return switch (recipe.kind) {
                case MOLD_PREFIX, MOLD_EXACT, CYANIDE -> recipe.right.count();
                case RENAME -> 0;
                case STANDARD, HOT -> mirrored ? recipe.left.count() : recipe.right.count();
            };
        }
    }

    public enum Kind {
        STANDARD("standard"),
        HOT("hot"),
        MOLD_PREFIX("mold_prefix"),
        MOLD_EXACT("mold_exact"),
        CYANIDE("cyanide"),
        RENAME("rename");

        private final String jsonName;

        Kind(String jsonName) {
            this.jsonName = jsonName;
        }

        public String jsonName() {
            return jsonName;
        }

        public static Kind fromJsonName(String name) {
            String normalized = name.toLowerCase(Locale.ROOT);
            for (Kind kind : values()) {
                if (kind.jsonName.equals(normalized)) {
                    return kind;
                }
            }
            throw new JsonSyntaxException("Unknown anvil smithing recipe kind '" + name + "'");
        }
    }

    public static class Serializer implements RecipeSerializer<AnvilSmithingRecipe> {
        @Override
        public AnvilSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
            HbmIngredient left = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "left"));
            HbmIngredient right = HbmIngredient.fromJson(GsonHelper.getAsJsonObject(json, "right"));
            HbmItemOutput output = HbmItemOutput.fromJson(GsonHelper.getAsJsonObject(json, "output"));
            int tier = GsonHelper.getAsInt(json, "tier", 1);
            boolean shapeless = GsonHelper.getAsBoolean(json, "shapeless", false);
            int sourceOrder = GsonHelper.getAsInt(json, "source_order", Integer.MAX_VALUE);
            Kind kind = Kind.fromJsonName(GsonHelper.getAsString(json, "kind", Kind.STANDARD.jsonName()));
            String moldPrefix = GsonHelper.getAsString(json, "mold_prefix", "");
            return new AnvilSmithingRecipe(id, left, right, output, tier, shapeless, sourceOrder, kind, moldPrefix);
        }

        @Nullable
        @Override
        public AnvilSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            HbmIngredient left = HbmIngredient.fromNetwork(buffer);
            HbmIngredient right = HbmIngredient.fromNetwork(buffer);
            HbmItemOutput output = HbmItemOutput.fromNetwork(buffer);
            int tier = buffer.readVarInt();
            boolean shapeless = buffer.readBoolean();
            int sourceOrder = buffer.readVarInt();
            Kind kind = buffer.readEnum(Kind.class);
            String moldPrefix = buffer.readUtf();
            return new AnvilSmithingRecipe(id, left, right, output, tier, shapeless, sourceOrder, kind, moldPrefix);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AnvilSmithingRecipe recipe) {
            recipe.left.toNetwork(buffer);
            recipe.right.toNetwork(buffer);
            recipe.output.toNetwork(buffer);
            buffer.writeVarInt(recipe.tier);
            buffer.writeBoolean(recipe.shapeless);
            buffer.writeVarInt(recipe.sourceOrder);
            buffer.writeEnum(recipe.kind);
            buffer.writeUtf(recipe.moldPrefix == null ? "" : recipe.moldPrefix);
        }
    }
}
