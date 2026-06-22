package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

public final class ResearchReactorFuelRuntime {
    public static final String TAG_LIFE = "life";

    private static final Map<Item, FuelSpec> FUELS = new HashMap<>();

    static {
        fuel("plate_fuel_u233", "waste_plate_u233", 2_200_000, Function.SQUARE_ROOT, 50);
        fuel("plate_fuel_u235", "waste_plate_u235", 2_200_000, Function.SQUARE_ROOT, 40);
        fuel("plate_fuel_mox", "waste_plate_mox", 2_400_000, Function.LOGARITHM, 50);
        fuel("plate_fuel_pu239", "waste_plate_pu239", 2_000_000, Function.NEGATIVE_QUADRATIC, 50);
        fuel("plate_fuel_sa326", "waste_plate_sa326", 2_000_000, Function.LINEAR, 80);
        fuel("plate_fuel_ra226be", "waste_plate_ra226be", 1_300_000, Function.PASSIVE, 30);
        fuel("plate_fuel_pu238be", "waste_plate_pu238be", 1_000_000, Function.PASSIVE, 50);
    }

    private ResearchReactorFuelRuntime() {
    }

    public static FuelSpec fuelFor(ItemStack stack) {
        return stack.isEmpty() ? null : FUELS.get(stack.getItem());
    }

    public static boolean isFuel(ItemStack stack) {
        return fuelFor(stack) != null;
    }

    public static boolean isWaste(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return FUELS.values().stream()
                .anyMatch(spec -> ItemStack.isSameItem(stack, spec.waste()));
    }

    public static List<DisplayFuel> displayFuels() {
        return FUELS.entrySet().stream()
                .map(entry -> new DisplayFuel(new ItemStack(entry.getKey()), entry.getValue()))
                .toList();
    }

    public static int getLife(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(TAG_LIFE) : 0;
    }

    public static void setLife(ItemStack stack, int life) {
        stack.getOrCreateTag().putInt(TAG_LIFE, life);
    }

    public static int react(ItemStack stack, int flux) {
        FuelSpec spec = fuelFor(stack);
        if (spec == null) {
            return 0;
        }
        if (spec.function() != Function.PASSIVE) {
            setLife(stack, getLife(stack) + flux);
        }
        return switch (spec.function()) {
            case LOGARITHM -> (int) (Math.log10(flux + 1) * 0.5D * spec.reactivity());
            case SQUARE_ROOT -> (int) (Math.sqrt(flux) * spec.reactivity() / 10.0D);
            case NEGATIVE_QUADRATIC -> (int) Math.max((flux - flux * flux / 10000.0D) / 100.0D
                    * spec.reactivity(), 0.0D);
            case LINEAR -> (int) (flux / 100.0D * spec.reactivity());
            case PASSIVE -> {
                setLife(stack, getLife(stack) + spec.reactivity());
                yield spec.reactivity();
            }
        };
    }

    private static void fuel(String input, String waste, int lifetime, Function function, int reactivity) {
        RegistryObject<Item> inputItem = ModItems.legacyItem(input);
        RegistryObject<Item> wasteItem = ModItems.legacyItem(waste);
        if (inputItem != null && wasteItem != null) {
            FUELS.put(inputItem.get(), new FuelSpec(new ItemStack(wasteItem.get()), lifetime, function, reactivity));
        }
    }

    public enum Function {
        LOGARITHM,
        SQUARE_ROOT,
        NEGATIVE_QUADRATIC,
        LINEAR,
        PASSIVE
    }

    public record FuelSpec(ItemStack waste, int lifetime, Function function, int reactivity) {
    }

    public record DisplayFuel(ItemStack input, FuelSpec spec) {
    }
}
