package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Supplier;

public final class ParticleAcceleratorRecipeRegistry {
    private static final List<Recipe> RECIPES = List.of(
            recipe(ModItems.PARTICLE_HYDROGEN, ModItems.PARTICLE_COPPER, 300,
                    ModItems.PARTICLE_AMAT, null),
            recipe(ModItems.PARTICLE_AMAT, ModItems.PARTICLE_AMAT, 400,
                    ModItems.PARTICLE_ASCHRAB, null),
            recipe(ModItems.PARTICLE_ASCHRAB, ModItems.PARTICLE_ASCHRAB, 10_000,
                    ModItems.PARTICLE_DARK, null),
            recipe(ModItems.PARTICLE_HYDROGEN, ModItems.PARTICLE_AMAT, 2_500,
                    ModItems.PARTICLE_MUON, null),
            recipe(ModItems.PARTICLE_HYDROGEN, ModItems.PARTICLE_LEAD, 6_500,
                    ModItems.PARTICLE_HIGGS, null),
            recipe(ModItems.PARTICLE_MUON, ModItems.PARTICLE_HIGGS, 5_000,
                    ModItems.PARTICLE_TACHYON, null),
            recipe(ModItems.PARTICLE_MUON, ModItems.PARTICLE_DARK, 12_500,
                    ModItems.PARTICLE_STRANGE, null),
            recipe(ModItems.PARTICLE_STRANGE, legacy("powder_magic"), 12_500,
                    ModItems.PARTICLE_SPARKTICLE, legacy("dust")),
            recipe(ModItems.PARTICLE_SPARKTICLE, ModItems.PARTICLE_HIGGS, 70_000,
                    ModItems.PARTICLE_DIGAMMA, null));

    private ParticleAcceleratorRecipeRegistry() {
    }

    public static List<Recipe> recipes() {
        return RECIPES;
    }

    public static Recipe getOutput(ItemStack input1, ItemStack input2) {
        for (Recipe recipe : RECIPES) {
            if (recipe.matches(input1, input2)) {
                return recipe;
            }
        }
        return null;
    }

    private static Recipe recipe(Supplier<? extends Item> input1, Supplier<? extends Item> input2, int momentum,
            Supplier<? extends Item> output1, Supplier<? extends Item> output2) {
        return new Recipe(input1, input2, momentum, output1, output2);
    }

    private static Supplier<? extends Item> legacy(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        if (item == null) {
            throw new IllegalStateException("Missing legacy PA recipe item: " + name);
        }
        return item;
    }

    public record Recipe(Supplier<? extends Item> input1, Supplier<? extends Item> input2, int momentum,
            Supplier<? extends Item> output1, Supplier<? extends Item> output2) {
        public boolean matches(ItemStack first, ItemStack second) {
            return matchesOrdered(first, second) || matchesOrdered(second, first);
        }

        private boolean matchesOrdered(ItemStack first, ItemStack second) {
            return !first.isEmpty() && !second.isEmpty()
                    && first.is(input1.get()) && second.is(input2.get());
        }

        public ItemStack output1Stack() {
            return output1 == null ? ItemStack.EMPTY : new ItemStack(output1.get());
        }

        public ItemStack output2Stack() {
            return output2 == null ? ItemStack.EMPTY : new ItemStack(output2.get());
        }
    }
}
