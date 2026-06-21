package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public final class RadGenRecipeRuntime {
    private static final Map<String, FuelSpec> FUELS = Map.of(
            "nuclear_waste_short", new FuelSpec("nuclear_waste_short", 1500, 30 * 60 * 20, "nuclear_waste_short_depleted"),
            "nuclear_waste_short_tiny", new FuelSpec("nuclear_waste_short_tiny", 150, 3 * 60 * 20, "nuclear_waste_short_depleted_tiny"),
            "nuclear_waste_long", new FuelSpec("nuclear_waste_long", 500, 2 * 60 * 60 * 20, "nuclear_waste_long_depleted"),
            "nuclear_waste_long_tiny", new FuelSpec("nuclear_waste_long_tiny", 50, 12 * 60 * 20, "nuclear_waste_long_depleted_tiny"),
            "scrap_nuclear", new FuelSpec("scrap_nuclear", 50, 5 * 60 * 20, null),
            "gem_rad", new FuelSpec("gem_rad", 25_000, 30 * 60 * 20, "minecraft:diamond"));

    private RadGenRecipeRuntime() {
    }

    public static List<FuelSpec> recipes() {
        return FUELS.values().stream()
                .filter(spec -> !spec.input().isEmpty())
                .sorted(Comparator.comparing(FuelSpec::inputName))
                .toList();
    }

    @Nullable
    public static FuelSpec fuelFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        net.minecraft.resources.ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? null : FUELS.get(key.getPath());
    }

    public record FuelSpec(String inputName, int powerPerTick, int duration, @Nullable String outputName) {
        public ItemStack input() {
            RegistryObject<Item> item = ModItems.legacyItem(inputName);
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
        }

        public ItemStack output() {
            if (outputName == null || outputName.isBlank()) {
                return ItemStack.EMPTY;
            }
            if ("minecraft:diamond".equals(outputName)) {
                return new ItemStack(Items.DIAMOND);
            }
            RegistryObject<Item> item = ModItems.legacyItem(outputName);
            return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
        }
    }
}