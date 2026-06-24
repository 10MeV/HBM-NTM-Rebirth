package com.hbm.ntm.recipe;

import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public final class FuelPoolRecipes {
    private static final List<String> LEGACY_DEPLETED_WASTE_ITEMS = List.of(
            "waste_natural_uranium",
            "waste_uranium",
            "waste_thorium",
            "waste_mox",
            "waste_plutonium",
            "waste_u233",
            "waste_u235",
            "waste_schrabidium",
            "waste_zfb_mox",
            "waste_plate_u233",
            "waste_plate_u235",
            "waste_plate_mox",
            "waste_plate_pu239",
            "waste_plate_sa326",
            "waste_plate_ra226be",
            "waste_plate_pu238be");

    private FuelPoolRecipes() {
    }

    public static boolean isInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (isHotLegacyWaste(stack)) {
            return true;
        }
        return pwrOutput(stack.getItem()) != null;
    }

    public static boolean isInput(Level level, ItemStack stack) {
        return findRecipe(level, stack) != null || isInput(stack);
    }

    public static ItemStack cool(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (isHotLegacyWaste(stack)) {
            return DepletedFuelItem.stack(stack.getItem(), DepletedFuelItem.COLD_DAMAGE);
        }
        Item output = pwrOutput(stack.getItem());
        return output == null ? ItemStack.EMPTY : new ItemStack(output, 1);
    }

    public static ItemStack cool(Level level, ItemStack stack) {
        FuelPoolRecipe recipe = findRecipe(level, stack);
        return recipe == null ? cool(stack) : recipe.cool();
    }

    public static boolean canExtract(ItemStack stack) {
        return !isInput(stack);
    }

    public static boolean canExtract(Level level, ItemStack stack) {
        return !isInput(level, stack);
    }

    public static Map<Item, Item> recipes() {
        Map<Item, Item> recipes = new LinkedHashMap<>();
        int count = Math.min(ModItems.PWR_FUEL_HOT_ITEMS.size(), ModItems.PWR_FUEL_DEPLETED_ITEMS.size());
        for (int i = 0; i < count; i++) {
            recipes.put(ModItems.PWR_FUEL_HOT_ITEMS.get(i).get(), ModItems.PWR_FUEL_DEPLETED_ITEMS.get(i).get());
        }
        return Map.copyOf(recipes);
    }

    public static List<DisplayRecipe> displayRecipes() {
        List<DisplayRecipe> display = new ArrayList<>();
        recipes().forEach((input, output) -> display.add(new DisplayRecipe(new ItemStack(input), new ItemStack(output))));
        for (String name : LEGACY_DEPLETED_WASTE_ITEMS) {
            RegistryObject<Item> item = ModItems.legacyItem(name);
            if (item != null) {
                display.add(new DisplayRecipe(
                        DepletedFuelItem.stack(item.get(), DepletedFuelItem.HOT_DAMAGE),
                        DepletedFuelItem.stack(item.get(), DepletedFuelItem.COLD_DAMAGE)));
            }
        }
        return List.copyOf(display);
    }

    public static List<DisplayRecipe> displayRecipes(Level level) {
        if (level == null) {
            return displayRecipes();
        }
        List<DisplayRecipe> display = new ArrayList<>();
        for (FuelPoolRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.FUEL_POOL.type().get())) {
            for (ItemStack input : recipe.input().displayStacks()) {
                display.add(new DisplayRecipe(input, recipe.cool()));
            }
        }
        display.addAll(displayRecipes());
        return List.copyOf(display);
    }

    private static FuelPoolRecipe findRecipe(Level level, ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return null;
        }
        for (FuelPoolRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.FUEL_POOL.type().get())) {
            if (recipe.matches(stack)) {
                return recipe;
            }
        }
        return null;
    }

    private static boolean isHotLegacyWaste(ItemStack stack) {
        return stack.getItem() instanceof DepletedFuelItem
                && DepletedFuelItem.isHot(stack)
                && isLegacyDepletedWaste(stack.getItem());
    }

    private static boolean isLegacyDepletedWaste(Item item) {
        for (String name : LEGACY_DEPLETED_WASTE_ITEMS) {
            RegistryObject<Item> registered = ModItems.legacyItem(name);
            if (registered != null && registered.get() == item) {
                return true;
            }
        }
        return false;
    }

    private static Item pwrOutput(Item item) {
        int count = Math.min(ModItems.PWR_FUEL_HOT_ITEMS.size(), ModItems.PWR_FUEL_DEPLETED_ITEMS.size());
        for (int i = 0; i < count; i++) {
            if (ModItems.PWR_FUEL_HOT_ITEMS.get(i).get() == item) {
                return ModItems.PWR_FUEL_DEPLETED_ITEMS.get(i).get();
            }
        }
        return null;
    }

    public record DisplayRecipe(ItemStack input, ItemStack output) {
    }
}
