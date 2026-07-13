package com.hbm.ntm.compat.jei;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.itempool.HbmItemPoolIds;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

public final class SatelliteCargoRecipeCategory
        implements HbmJeiRecipeCategory<SatelliteCargoRecipeCategory.DisplayRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 86;
    private static final int TEXTURE_SIZE = 256;
    private static final int OUTPUT_VISIBLE_SLOTS = 18;
    private static final int OUTPUT_COLUMNS = 6;
    private static final ResourceLocation LEGACY_NEI_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_anvil.png");

    private final RecipeType<DisplayRecipe> type;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final ItemStack catalyst;

    SatelliteCargoRecipeCategory(RecipeType<DisplayRecipe> type, ItemLike catalyst, IGuiHelper guiHelper) {
        this.type = type;
        this.icon = guiHelper.createDrawableItemLike(catalyst);
        this.background = guiHelper.createDrawable(LEGACY_NEI_TEXTURE, 0, 0, WIDTH, HEIGHT);
        this.catalyst = new ItemStack(catalyst);
    }

    @Override
    public RecipeType<DisplayRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Satellite");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public IDrawable getRecipeBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DisplayRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(12, 24)
                .addItemStack(recipe.satellite());
        List<List<ItemStack>> outputColumns = recipe.outputColumns();
        for (int i = 0; i < outputColumns.size(); i++) {
            builder.addOutputSlot(48 + 18 * (i % OUTPUT_COLUMNS), 6 + 18 * (i / OUTPUT_COLUMNS))
                    .addItemStacks(outputColumns.get(i));
        }
        builder.addSlot(RecipeIngredientRole.CATALYST, 30, 31)
                .addItemStack(catalyst.copy());
    }

    @Override
    public void draw(DisplayRecipe recipe, IRecipeSlotsView recipeSlotsView,
            net.minecraft.client.gui.GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawBackground(guiGraphics);
        blit(guiGraphics, 11, 23, 113, 105, 18, 18);
        blit(guiGraphics, 47, 5, 5, 87, 108, 54);
        blit(guiGraphics, 29, 14, 131, 96, 18, 36);
    }

    static List<DisplayRecipe> recipes() {
        List<DisplayRecipe> recipes = new ArrayList<>();
        addRecipe(recipes, new ItemStack(ModItems.SAT_MINER.get()),
                HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SAT_MINER));
        addRecipe(recipes, new ItemStack(ModItems.SAT_LUNAR_MINER.get()),
                HbmItemPoolIds.tableFor(HbmItemPoolIds.POOL_SAT_LUNAR));
        return List.copyOf(recipes);
    }

    private static void addRecipe(List<DisplayRecipe> recipes, ItemStack satellite, ResourceLocation table) {
        List<ItemStack> outputs = lootTableStacks(table);
        if (!satellite.isEmpty() && !outputs.isEmpty()) {
            recipes.add(new DisplayRecipe(satellite, outputColumns(outputs)));
        }
    }

    private static List<List<ItemStack>> outputColumns(List<ItemStack> outputs) {
        int visible = Math.min(outputs.size(), OUTPUT_VISIBLE_SLOTS);
        List<List<ItemStack>> columns = new ArrayList<>();
        for (int i = 0; i < visible; i++) {
            List<ItemStack> cycled = new ArrayList<>();
            for (int j = 0; j * OUTPUT_VISIBLE_SLOTS + i < outputs.size(); j++) {
                cycled.add(outputs.get(j * OUTPUT_VISIBLE_SLOTS + i).copy());
            }
            columns.add(List.copyOf(cycled));
        }
        return List.copyOf(columns);
    }

    private static List<ItemStack> lootTableStacks(ResourceLocation table) {
        String resourcePath = "/data/" + table.getNamespace() + "/loot_tables/" + table.getPath() + ".json";
        try (InputStream stream = SatelliteCargoRecipeCategory.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                HbmNtm.LOGGER.warn("Missing satellite cargo JEI loot table resource {}.", resourcePath);
                return List.of();
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                return stacksFromLootTable(root, table);
            }
        } catch (IOException | IllegalStateException | JsonSyntaxException exception) {
            HbmNtm.LOGGER.warn("Could not parse satellite cargo JEI loot table {}.", table, exception);
            return List.of();
        }
    }

    private static List<ItemStack> stacksFromLootTable(JsonObject root, ResourceLocation table) {
        List<WeightedStack> weightedStacks = new ArrayList<>();
        JsonArray pools = GsonHelper.getAsJsonArray(root, "pools", new JsonArray());
        for (JsonElement poolElement : pools) {
            JsonObject pool = GsonHelper.convertToJsonObject(poolElement, "satellite cargo pool");
            JsonArray entries = GsonHelper.getAsJsonArray(pool, "entries", new JsonArray());
            for (JsonElement entryElement : entries) {
                parseEntry(entryElement, table).ifPresent(weightedStacks::add);
            }
        }
        int totalWeight = weightedStacks.stream().mapToInt(WeightedStack::weight).sum();
        if (totalWeight <= 0) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (WeightedStack weightedStack : weightedStacks) {
            ItemStack stack = weightedStack.stack().copy();
            HbmItemStackUtil.addTooltipToStack(stack,
                    ChatFormatting.RED + legacyChancePercent(weightedStack.weight(), totalWeight) + "%");
            stacks.add(stack);
        }
        return List.copyOf(stacks);
    }

    private static java.util.Optional<WeightedStack> parseEntry(JsonElement entryElement, ResourceLocation table) {
        JsonObject entry = GsonHelper.convertToJsonObject(entryElement, "satellite cargo entry");
        String type = GsonHelper.getAsString(entry, "type", "");
        if (!"minecraft:item".equals(type) && !"item".equals(type)) {
            HbmNtm.LOGGER.warn("Skipped unsupported satellite cargo JEI entry type {} in {}.", type, table);
            return java.util.Optional.empty();
        }
        ResourceLocation itemId = ResourceLocation.tryParse(GsonHelper.getAsString(entry, "name"));
        if (itemId == null) {
            HbmNtm.LOGGER.warn("Skipped malformed satellite cargo JEI item entry in {}.", table);
            return java.util.Optional.empty();
        }
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || item == Items.AIR) {
            HbmNtm.LOGGER.warn("Skipped unresolved satellite cargo JEI item {} in {}.", itemId, table);
            return java.util.Optional.empty();
        }
        ItemStack stack = new ItemStack(item);
        applyFunctions(stack, GsonHelper.getAsJsonArray(entry, "functions", new JsonArray()), table, itemId);
        int weight = Math.max(1, GsonHelper.getAsInt(entry, "weight", 1));
        return java.util.Optional.of(new WeightedStack(stack, weight));
    }

    private static void applyFunctions(ItemStack stack, JsonArray functions, ResourceLocation table,
            ResourceLocation itemId) {
        for (JsonElement functionElement : functions) {
            JsonObject function = GsonHelper.convertToJsonObject(functionElement, "satellite cargo function");
            String name = GsonHelper.getAsString(function, "function", "");
            if ("minecraft:set_count".equals(name) || "set_count".equals(name)) {
                stack.setCount(readSetCount(function.get("count"), table, itemId));
            } else if ("minecraft:set_nbt".equals(name) || "set_nbt".equals(name)) {
                applySetNbt(stack, function, table, itemId);
            } else {
                HbmNtm.LOGGER.warn("Skipped unsupported satellite cargo JEI function {} for {} in {}.",
                        name, itemId, table);
            }
        }
    }

    private static int readSetCount(JsonElement countElement, ResourceLocation table, ResourceLocation itemId) {
        if (countElement == null || countElement.isJsonNull()) {
            return 1;
        }
        if (countElement.isJsonPrimitive()) {
            return Math.max(1, Math.round(countElement.getAsFloat()));
        }
        JsonObject count = GsonHelper.convertToJsonObject(countElement, "satellite cargo count");
        String type = GsonHelper.getAsString(count, "type", "");
        if ("minecraft:uniform".equals(type) || "uniform".equals(type)) {
            return Math.max(1, Math.round(GsonHelper.getAsFloat(count, "min", 1.0F)));
        }
        HbmNtm.LOGGER.warn("Unsupported satellite cargo JEI count provider {} for {} in {}; using count 1.",
                type, itemId, table);
        return 1;
    }

    private static void applySetNbt(ItemStack stack, JsonObject function, ResourceLocation table,
            ResourceLocation itemId) {
        String nbt = GsonHelper.getAsString(function, "tag", "");
        if (nbt.isBlank()) {
            return;
        }
        try {
            CompoundTag tag = TagParser.parseTag(nbt);
            stack.setTag(tag);
        } catch (Exception exception) {
            HbmNtm.LOGGER.warn("Skipped malformed satellite cargo JEI NBT for {} in {}.", itemId, table,
                    exception);
        }
    }

    private static String legacyChancePercent(int weight, int totalWeight) {
        float chance = 100.0F * weight / totalWeight;
        return Float.toString(((int) (chance * 10.0F)) / 10.0F);
    }

    private static void blit(net.minecraft.client.gui.GuiGraphics guiGraphics, int x, int y, int u, int v,
            int width, int height) {
        guiGraphics.blit(LEGACY_NEI_TEXTURE, x, y, u, v, width, height, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static List<List<ItemStack>> copyColumns(List<List<ItemStack>> columns) {
        if (columns == null || columns.isEmpty()) {
            return List.of();
        }
        List<List<ItemStack>> copy = new ArrayList<>();
        for (List<ItemStack> column : columns) {
            List<ItemStack> stacks = new ArrayList<>();
            if (column != null) {
                for (ItemStack stack : column) {
                    stacks.add(stack == null ? ItemStack.EMPTY : stack.copy());
                }
            }
            copy.add(List.copyOf(stacks));
        }
        return List.copyOf(copy);
    }

    private record WeightedStack(ItemStack stack, int weight) {
        private WeightedStack {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            weight = Math.max(1, weight);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    public record DisplayRecipe(ItemStack satellite, List<List<ItemStack>> outputColumns) {
        public DisplayRecipe {
            satellite = satellite == null ? ItemStack.EMPTY : satellite.copy();
            outputColumns = copyColumns(outputColumns);
        }

        @Override
        public ItemStack satellite() {
            return satellite.copy();
        }

        @Override
        public List<List<ItemStack>> outputColumns() {
            return copyColumns(outputColumns);
        }
    }
}
