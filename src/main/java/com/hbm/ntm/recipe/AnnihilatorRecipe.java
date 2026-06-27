package com.hbm.ntm.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmRegistryUtil;
import com.hbm.ntm.world.saveddata.AnnihilatorSavedData;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
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
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class AnnihilatorRecipe implements Recipe<Container> {
    public static final Comparator<Milestone> MILESTONE_ORDER =
            Comparator.comparing(Milestone::amount);

    private final ResourceLocation id;
    private final AnnihilatorSavedData.PoolKey key;
    private final List<Milestone> milestones;

    public AnnihilatorRecipe(ResourceLocation id, AnnihilatorSavedData.PoolKey key, List<Milestone> milestones) {
        this.id = id;
        this.key = key;
        this.milestones = milestones == null ? List.of() : milestones.stream()
                .filter(milestone -> milestone != null && milestone.amount().signum() > 0
                        && !milestone.payout().isEmpty())
                .sorted(MILESTONE_ORDER)
                .toList();
        if (this.milestones.isEmpty()) {
            throw new IllegalArgumentException("Annihilator recipe needs at least one valid milestone: " + id);
        }
    }

    public AnnihilatorSavedData.PoolKey key() {
        return key;
    }

    public List<Milestone> milestones() {
        return milestones;
    }

    public ItemStack highestPayout(@Nullable BigInteger previous, BigInteger current) {
        if (current == null) {
            return ItemStack.EMPTY;
        }
        BigInteger highestYet = BigInteger.ZERO;
        ItemStack payout = ItemStack.EMPTY;
        for (Milestone milestone : milestones) {
            if (previous != null && previous.compareTo(milestone.amount()) >= 0) {
                continue;
            }
            if (current.compareTo(highestYet) <= 0) {
                continue;
            }
            if (current.compareTo(milestone.amount()) >= 0) {
                highestYet = milestone.amount();
                payout = milestone.payout();
            }
        }
        return payout;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
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
        return milestones.get(0).payout();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModBlocks.MACHINE_ANNIHILATOR.get());
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
        return ModRecipes.ANNIHILATOR.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ANNIHILATOR.type().get();
    }

    public record Milestone(BigInteger amount, ItemStack payout) {
        public Milestone {
            if (amount == null || amount.signum() <= 0) {
                throw new IllegalArgumentException("Annihilator milestone amount must be positive");
            }
            if (payout == null || payout.isEmpty()) {
                throw new IllegalArgumentException("Annihilator milestone payout cannot be empty");
            }
            payout = payout.copy();
        }

        @Override
        public ItemStack payout() {
            return payout.copy();
        }
    }

    public static class Serializer implements RecipeSerializer<AnnihilatorRecipe> {
        @Override
        public AnnihilatorRecipe fromJson(ResourceLocation id, JsonObject json) {
            AnnihilatorSavedData.PoolKey key = readKey(GsonHelper.getAsJsonObject(json, "key"));
            JsonArray milestonesJson = GsonHelper.getAsJsonArray(json, "milestones");
            List<Milestone> milestones = new ArrayList<>();
            for (JsonElement element : milestonesJson) {
                JsonObject milestone = GsonHelper.convertToJsonObject(element, "annihilator milestone");
                BigInteger amount = readBigInteger(milestone.get("amount"), "annihilator milestone amount");
                ItemStack payout = readPayout(milestone.get("payout"));
                milestones.add(new Milestone(amount, payout));
            }
            return new AnnihilatorRecipe(id, key, milestones);
        }

        @Nullable
        @Override
        public AnnihilatorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            AnnihilatorSavedData.PoolKey key = readKey(buffer);
            List<Milestone> milestones = buffer.readList(Serializer::readMilestone);
            return new AnnihilatorRecipe(id, key, milestones);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AnnihilatorRecipe recipe) {
            writeKey(buffer, recipe.key);
            buffer.writeCollection(recipe.milestones, Serializer::writeMilestone);
        }

        private static Milestone readMilestone(FriendlyByteBuf buffer) {
            return new Milestone(new BigInteger(buffer.readUtf()), buffer.readItem());
        }

        private static void writeMilestone(FriendlyByteBuf buffer, Milestone milestone) {
            buffer.writeUtf(milestone.amount().toString());
            buffer.writeItem(milestone.payout());
        }

        private static AnnihilatorSavedData.PoolKey readKey(JsonObject key) {
            String type = GsonHelper.getAsString(key, "type");
            return switch (type) {
                case "item" -> AnnihilatorSavedData.PoolKey.item(resolveItemKey(key, "item"));
                case "comp" -> readComparableKey(key);
                case "fluid" -> AnnihilatorSavedData.PoolKey.fluid(readFluidKeyName(key));
                case "dict" -> AnnihilatorSavedData.PoolKey.oreDict(GsonHelper.getAsString(key, "dict"));
                default -> throw new JsonSyntaxException("Unsupported annihilator key type: " + type);
            };
        }

        private static AnnihilatorSavedData.PoolKey readComparableKey(JsonObject key) {
            ResourceLocation legacyId = normalizeLegacyItemId(GsonHelper.getAsString(key, "item"));
            int legacyMeta = GsonHelper.getAsInt(key, "meta", 0);
            Optional<RegistryObject<Item>> mapped = LegacyMetaItemMappings.item(legacyId, legacyMeta);
            if (mapped.isPresent()) {
                return AnnihilatorSavedData.PoolKey.itemMeta(HbmRegistryUtil.itemKey(mapped.get().get()), 0);
            }
            return AnnihilatorSavedData.PoolKey.itemMeta(resolveItemKey(legacyId), legacyMeta);
        }

        private static ItemStack readPayout(JsonElement element) {
            if (element == null || element.isJsonNull()) {
                throw new JsonSyntaxException("Missing annihilator milestone payout");
            }
            if (element.isJsonArray()) {
                return readLegacyPayout(element.getAsJsonArray());
            }
            return readModernPayout(GsonHelper.convertToJsonObject(element, "annihilator milestone payout"));
        }

        private static ItemStack readLegacyPayout(JsonArray array) {
            if (array.isEmpty()) {
                throw new JsonSyntaxException("Empty annihilator legacy payout array");
            }
            ResourceLocation legacyId = normalizeLegacyItemId(array.get(0).getAsString());
            int count = array.size() > 1 ? array.get(1).getAsInt() : 1;
            int legacyMeta = array.size() > 2 ? array.get(2).getAsInt() : 0;
            ItemStack stack = stackFromLegacyItem(legacyId, count, legacyMeta);
            if (array.size() > 3) {
                stack.setTag(parseNbt(array.get(3).getAsString(), "annihilator legacy payout"));
            }
            return stack;
        }

        private static ItemStack readModernPayout(JsonObject object) {
            Item item = resolveItem(resolveItemId(GsonHelper.getAsString(object, "item")),
                    "annihilator payout");
            int count = GsonHelper.getAsInt(object, "count", 1);
            if (count < 1) {
                throw new JsonSyntaxException("Invalid annihilator payout count: " + count);
            }
            ItemStack stack = new ItemStack(item, count);
            if (object.has("nbt")) {
                stack.setTag(parseNbt(GsonHelper.getAsString(object, "nbt"), "annihilator payout"));
            }
            return stack;
        }

        private static ItemStack stackFromLegacyItem(ResourceLocation legacyId, int count, int legacyMeta) {
            if (count < 1) {
                throw new JsonSyntaxException("Invalid annihilator payout count: " + count);
            }
            Optional<ItemStack> mapped = LegacyMetaItemMappings.stack(legacyId, legacyMeta, count);
            if (mapped.isPresent()) {
                return mapped.get();
            }
            if (legacyMeta != 0) {
                throw new JsonSyntaxException("Missing legacy annihilator payout mapping: "
                        + legacyId + " meta " + legacyMeta);
            }
            return new ItemStack(resolveItem(legacyId, "annihilator legacy payout"), count);
        }

        private static CompoundTag parseNbt(String value, String name) {
            try {
                return TagParser.parseTag(value);
            } catch (CommandSyntaxException exception) {
                throw new JsonSyntaxException("Invalid NBT in " + name + ": " + exception.getMessage(), exception);
            }
        }

        private static BigInteger readBigInteger(JsonElement element, String name) {
            if (element == null || element.isJsonNull()) {
                throw new JsonSyntaxException("Missing " + name);
            }
            try {
                return element.getAsBigInteger();
            } catch (NumberFormatException exception) {
                throw new JsonSyntaxException("Invalid " + name + ": " + element, exception);
            }
        }

        private static AnnihilatorSavedData.PoolKey readKey(FriendlyByteBuf buffer) {
            AnnihilatorSavedData.Kind kind =
                    AnnihilatorSavedData.Kind.byLegacyId(buffer.readByte());
            return switch (kind) {
                case ITEM -> AnnihilatorSavedData.PoolKey.item(buffer.readResourceLocation());
                case ITEM_META -> AnnihilatorSavedData.PoolKey.itemMeta(buffer.readResourceLocation(),
                        buffer.readVarInt());
                case FLUID -> AnnihilatorSavedData.PoolKey.fluid(buffer.readUtf());
                case ORE_DICT -> AnnihilatorSavedData.PoolKey.oreDict(buffer.readUtf());
                case UNKNOWN -> throw new IllegalArgumentException("Unknown annihilator key kind in network data");
            };
        }

        private static void writeKey(FriendlyByteBuf buffer, AnnihilatorSavedData.PoolKey key) {
            buffer.writeByte(key.kind().legacyId());
            switch (key.kind()) {
                case ITEM -> buffer.writeResourceLocation(key.item());
                case ITEM_META -> {
                    buffer.writeResourceLocation(key.item());
                    buffer.writeVarInt(key.meta());
                }
                case FLUID -> buffer.writeUtf(key.fluid());
                case ORE_DICT -> buffer.writeUtf(key.oreDict());
                case UNKNOWN -> {
                }
            }
        }

        private static ResourceLocation resolveItemKey(JsonObject object, String member) {
            return HbmRegistryUtil.itemKey(resolveItem(resolveItemId(GsonHelper.getAsString(object, member)),
                    "annihilator key"));
        }

        private static ResourceLocation resolveItemKey(ResourceLocation id) {
            return HbmRegistryUtil.itemKey(resolveItem(id, "annihilator key"));
        }

        private static Item resolveItem(ResourceLocation id, String name) {
            Optional<Item> item = HbmRegistryUtil.item(id);
            if (item.isPresent()) {
                return item.get();
            }
            if (HbmNtm.MOD_ID.equals(id.getNamespace())) {
                RegistryObject<Item> legacyItem = ModItems.legacyItem(id.getPath());
                if (legacyItem != null) {
                    return legacyItem.get();
                }
            }
            throw new JsonSyntaxException("Unknown item '" + id + "' in " + name);
        }

        private static ResourceLocation normalizeLegacyItemId(String value) {
            ResourceLocation id = resolveItemId(value);
            if ("hbm".equals(id.getNamespace())) {
                return new ResourceLocation(HbmNtm.MOD_ID, id.getPath());
            }
            return id;
        }

        private static ResourceLocation resolveItemId(String value) {
            if (value.indexOf(':') < 0) {
                return new ResourceLocation(HbmNtm.MOD_ID, value);
            }
            ResourceLocation id = ResourceLocation.tryParse(value);
            if (id == null) {
                throw new JsonSyntaxException("Invalid item id: " + value);
            }
            if ("hbm".equals(id.getNamespace())) {
                return new ResourceLocation(HbmNtm.MOD_ID, id.getPath());
            }
            return id;
        }

        private static String readFluidKeyName(JsonObject key) {
            FluidType type = HbmFluidJsonUtil.requireFluidReference(key.get("fluid"), "annihilator fluid key");
            return type.getName();
        }
    }
}
