package com.hbm.ntm.recipe;

import com.hbm.ntm.world.saveddata.AnnihilatorSavedData;
import java.math.BigInteger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class AnnihilatorRecipeRuntime {
    private AnnihilatorRecipeRuntime() {
    }

    public static ItemStack findPayout(ServerLevel level, AnnihilatorSavedData.PoolKey key,
            AnnihilatorSavedData.IncrementResult increment, boolean alwaysPayOut) {
        if (level == null || key == null || increment == null) {
            return ItemStack.EMPTY;
        }
        BigInteger previous = previousForPayout(increment, alwaysPayOut);
        ItemStack highest = ItemStack.EMPTY;
        BigInteger highestAmount = BigInteger.ZERO;
        for (AnnihilatorRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.ANNIHILATOR.type().get())) {
            if (!recipe.key().equals(key)) {
                continue;
            }
            ItemStack payout = recipe.highestPayout(previous, increment.current());
            if (payout.isEmpty()) {
                continue;
            }
            BigInteger amount = highestTriggeredAmount(recipe, previous, increment.current());
            if (amount.compareTo(highestAmount) > 0) {
                highestAmount = amount;
                highest = payout;
            }
        }
        return highest;
    }

    private static BigInteger previousForPayout(AnnihilatorSavedData.IncrementResult increment,
            boolean alwaysPayOut) {
        return alwaysPayOut && increment.previous().signum() > 0 ? null : increment.previous();
    }

    private static BigInteger highestTriggeredAmount(AnnihilatorRecipe recipe, BigInteger previous,
            BigInteger current) {
        BigInteger highest = BigInteger.ZERO;
        for (AnnihilatorRecipe.Milestone milestone : recipe.milestones()) {
            if (previous != null && previous.compareTo(milestone.amount()) >= 0) {
                continue;
            }
            if (current.compareTo(milestone.amount()) >= 0 && milestone.amount().compareTo(highest) > 0) {
                highest = milestone.amount();
            }
        }
        return highest;
    }
}
