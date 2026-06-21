package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;

public final class CyclotronRecipeRuntime {
    private static final List<Entry> ENTRIES = createEntries();

    public static Optional<CyclotronRecipe> find(ItemStack particle, ItemStack input) {
        if (particle.isEmpty() || input.isEmpty()) {
            return Optional.empty();
        }
        for (Entry entry : ENTRIES) {
            if (entry.matches(particle, input)) {
                return Optional.of(entry.recipe());
            }
        }
        return Optional.empty();
    }

    public static boolean isValidParticle(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (Entry entry : ENTRIES) {
            if (entry.particle().test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (Entry entry : ENTRIES) {
            if (entry.input().test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static List<DisplayRecipe> displayRecipes() {
        return ENTRIES.stream()
                .map(Entry::displayRecipe)
                .filter(recipe -> !recipe.particleInputs().isEmpty()
                        && !recipe.targetInputs().isEmpty()
                        && !recipe.output().isEmpty())
                .toList();
    }

    private static List<Entry> createEntries() {
        List<Entry> entries = new ArrayList<>();

        add(entries, "part_lithium", tag("dusts/lithium"), "powder_beryllium", 50);
        add(entries, "part_lithium", tag("dusts/beryllium"), "powder_boron", 50);
        add(entries, "part_lithium", tag("dusts/boron"), "powder_coal", 50);
        add(entries, "part_lithium", tag("dusts/quartz"), "powder_fire", 50);
        add(entries, "part_lithium", tag("dusts/phosphorus"), "sulfur", 50);
        add(entries, "part_lithium", tag("dusts/iron"), "powder_cobalt", 50);
        add(entries, "part_lithium", item("powder_strontium"), "powder_zirconium", 50);
        add(entries, "part_lithium", tag("dusts/gold"), "ingot_mercury", 50);
        add(entries, "part_lithium", tag("dusts/polonium"), "powder_astatine", 50);
        add(entries, "part_lithium", tag("dusts/lanthanium"), "powder_cerium", 50);
        add(entries, "part_lithium", tag("dusts/actinium"), "powder_thorium", 50);
        add(entries, "part_lithium", tag("dusts/uranium"), "powder_neptunium", 50);
        add(entries, "part_lithium", tag("dusts/np237"), "powder_plutonium", 50);

        add(entries, "part_beryllium", tag("dusts/lithium"), "powder_boron", 25);
        add(entries, "part_beryllium", tag("dusts/quartz"), "sulfur", 25);
        add(entries, "part_beryllium", tag("dusts/titanium"), "powder_iron", 25);
        add(entries, "part_beryllium", tag("dusts/cobalt"), "powder_copper", 25);
        add(entries, "part_beryllium", item("powder_strontium"), "powder_niobium", 25);
        add(entries, "part_beryllium", item("powder_cerium"), "powder_neodymium", 25);
        add(entries, "part_beryllium", tag("dusts/thorium"), "powder_uranium", 25);

        add(entries, "part_carbon", tag("dusts/boron"), "powder_aluminium", 10);
        add(entries, "part_carbon", tag("dusts/sulfur"), "powder_titanium", 10);
        add(entries, "part_carbon", tag("dusts/titanium"), "powder_cobalt", 10);
        add(entries, "part_carbon", item("powder_caesium"), "powder_lanthanium", 10);
        add(entries, "part_carbon", item("powder_neodymium"), "powder_gold", 10);
        add(entries, "part_carbon", item("ingot_mercury"), "powder_polonium", 10);
        add(entries, "part_carbon", tag("dusts/lead"), "powder_ra226", 10);
        add(entries, "part_carbon", item("powder_astatine"), "powder_actinium", 10);

        add(entries, "part_copper", tag("dusts/beryllium"), "powder_quartz", 15);
        add(entries, "part_copper", tag("dusts/coal"), "powder_bromine", 15);
        add(entries, "part_copper", tag("dusts/titanium"), "powder_strontium", 15);
        add(entries, "part_copper", tag("dusts/iron"), "powder_niobium", 15);
        add(entries, "part_copper", item("powder_bromine"), "powder_iodine", 15);
        add(entries, "part_copper", item("powder_strontium"), "powder_neodymium", 15);
        add(entries, "part_copper", item("powder_niobium"), "powder_caesium", 15);
        add(entries, "part_copper", item("powder_iodine"), "powder_polonium", 15);
        add(entries, "part_copper", item("powder_caesium"), "powder_actinium", 15);
        add(entries, "part_copper", tag("dusts/gold"), "powder_uranium", 15);

        add(entries, "part_plutonium", tag("dusts/phosphorus"), "powder_tennessine", 100);
        add(entries, "part_plutonium", tag("dusts/plutonium"), "powder_tennessine", 100);
        add(entries, "part_plutonium", item("powder_tennessine"), "powder_australium", 100);
        add(entries, "part_plutonium", item("pellet_charged"), "nugget_schrabidium", 1000);

        return List.copyOf(entries);
    }

    private static void add(List<Entry> entries, String particleName, RecipeInput input,
            String outputName, int antimatter) {
        RecipeInput particle = item(particleName);
        ItemStack output = stack(outputName);
        if (input != null && !output.isEmpty()) {
            entries.add(new Entry(particle, input, new CyclotronRecipe(output, antimatter)));
        }
    }

    private static RecipeInput item(String legacyName) {
        return new RecipeInput(stack -> {
            ItemStack expected = stack(legacyName);
            return !stack.isEmpty() && !expected.isEmpty() && ItemStack.isSameItemSameTags(stack, expected);
        }, () -> {
            ItemStack expected = stack(legacyName);
            return expected.isEmpty() ? List.of() : List.of(expected);
        });
    }

    private static RecipeInput tag(String forgePath) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation("forge", forgePath));
        return new RecipeInput(stack -> !stack.isEmpty() && stack.is(tag),
                () -> List.of(Ingredient.of(tag).getItems()));
    }

    private static ItemStack stack(String legacyName) {
        RegistryObject<Item> item = ModItems.legacyItem(legacyName);
        if (item == null) {
            HbmNtm.LOGGER.debug("Skipping Cyclotron recipe endpoint without modern item: {}", legacyName);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item.get());
    }

    private record RecipeInput(Predicate<ItemStack> predicate, Supplier<List<ItemStack>> displayStacks) {
        private boolean test(ItemStack stack) {
            return predicate.test(stack);
        }

        private List<ItemStack> displayStackCopies() {
            return displayStacks.get().stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
        }
    }

    private record Entry(RecipeInput particle, RecipeInput input, CyclotronRecipe recipe) {
        private boolean matches(ItemStack particleStack, ItemStack inputStack) {
            return particle.test(particleStack) && input.test(inputStack);
        }

        private DisplayRecipe displayRecipe() {
            return new DisplayRecipe(particle.displayStackCopies(), input.displayStackCopies(),
                    recipe.output(), recipe.antimatterMb());
        }
    }

    public record CyclotronRecipe(ItemStack output, int antimatterMb) {
        public CyclotronRecipe {
            output = output == null ? ItemStack.EMPTY : output.copy();
            antimatterMb = Math.max(0, antimatterMb);
        }
    }

    public record DisplayRecipe(List<ItemStack> particleInputs, List<ItemStack> targetInputs,
            ItemStack output, int antimatterMb) {
        public DisplayRecipe {
            particleInputs = particleInputs == null ? List.of() : particleInputs.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            targetInputs = targetInputs == null ? List.of() : targetInputs.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            output = output == null ? ItemStack.EMPTY : output.copy();
            antimatterMb = Math.max(0, antimatterMb);
        }
    }

    private CyclotronRecipeRuntime() {
    }
}