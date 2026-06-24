package com.hbm.ntm.recipe;

import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.item.ICFPelletItem.FuelType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class ICFPelletRecipeRuntime {
    private ICFPelletRecipeRuntime() {
    }

    public static List<DisplayPellet> displayPellets() {
        List<DisplayPellet> recipes = new ArrayList<>();
        FuelType[] fuels = FuelType.values();
        for (int first = 0; first < fuels.length; first++) {
            for (int second = first + 1; second < fuels.length; second++) {
                recipes.add(new DisplayPellet(fuels[first], fuels[second], false));
                recipes.add(new DisplayPellet(fuels[first], fuels[second], true));
            }
        }
        return List.copyOf(recipes);
    }

    public record DisplayPellet(FuelType first, FuelType second, boolean muon) {
        public ItemStack output() {
            return ICFPelletItem.setup(first, second, muon);
        }

        public long maxDepletion() {
            return ICFPelletItem.getMaxDepletion(output());
        }

        public long fusingDifficulty() {
            return ICFPelletItem.getFusingDifficulty(output());
        }

        public double reactionMultiplier() {
            ItemStack stack = output();
            long heat = 1_000_000L;
            return ICFPelletItem.react(stack, heat) / (double) heat;
        }
    }
}
