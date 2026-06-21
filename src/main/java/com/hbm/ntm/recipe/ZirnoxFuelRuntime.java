package com.hbm.ntm.recipe;

import com.hbm.ntm.item.ZirnoxRodItem;
import com.hbm.ntm.registry.ModItems;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public final class ZirnoxFuelRuntime {
    private static final Map<Item, RegistryObject<Item>> PRODUCTS = new IdentityHashMap<>();

    static {
        map(0, "rod_zirnox_natural_uranium_fuel_depleted");
        map(1, "rod_zirnox_uranium_fuel_depleted");
        map(2, 3);
        map(3, "rod_zirnox_thorium_fuel_depleted");
        map(4, "rod_zirnox_mox_fuel_depleted");
        map(5, "rod_zirnox_plutonium_fuel_depleted");
        map(6, "rod_zirnox_u233_fuel_depleted");
        map(7, "rod_zirnox_u235_fuel_depleted");
        map(8, "rod_zirnox_les_fuel_depleted");
        map(9, "rod_zirnox_tritium");
        map(10, "rod_zirnox_zfb_mox_depleted");
    }

    private ZirnoxFuelRuntime() {
    }

    public static boolean isRod(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ZirnoxRodItem;
    }

    public static boolean isFuelRod(ItemStack stack) {
        return stack.getItem() instanceof ZirnoxRodItem rod && !rod.breeding();
    }

    public static List<DisplayRod> displayRods() {
        return PRODUCTS.entrySet().stream()
                .map(entry -> {
                    ItemStack input = new ItemStack(entry.getKey());
                    return new DisplayRod(input, product(input), heat(input), isBreeding(input));
                })
                .toList();
    }

    public static int heat(ItemStack stack) {
        return stack.getItem() instanceof ZirnoxRodItem rod ? rod.heat() : 0;
    }

    public static boolean isBreeding(ItemStack stack) {
        return stack.getItem() instanceof ZirnoxRodItem rod && rod.breeding();
    }

    public static ItemStack product(ItemStack stack) {
        RegistryObject<Item> item = PRODUCTS.get(stack.getItem());
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    private static void map(int rodIndex, String productName) {
        RegistryObject<Item> product = ModItems.legacyItem(productName);
        if (product != null && rodIndex >= 0 && rodIndex < ModItems.ZIRNOX_ROD_ITEMS.size()) {
            PRODUCTS.put(ModItems.ZIRNOX_ROD_ITEMS.get(rodIndex).get(), product);
        }
    }

    private static void map(int rodIndex, int productRodIndex) {
        if (rodIndex >= 0 && rodIndex < ModItems.ZIRNOX_ROD_ITEMS.size()
                && productRodIndex >= 0 && productRodIndex < ModItems.ZIRNOX_ROD_ITEMS.size()) {
            PRODUCTS.put(ModItems.ZIRNOX_ROD_ITEMS.get(rodIndex).get(),
                    ModItems.ZIRNOX_ROD_ITEMS.get(productRodIndex));
        }
    }

    public record DisplayRod(ItemStack input, ItemStack product, int heat, boolean breeding) {
    }
}
