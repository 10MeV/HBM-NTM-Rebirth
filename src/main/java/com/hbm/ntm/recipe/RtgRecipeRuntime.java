package com.hbm.ntm.recipe;

import com.hbm.ntm.blockentity.RtgBlockEntity;
import java.util.List;

public final class RtgRecipeRuntime {
    public static List<RtgBlockEntity.FuelSpec> displayRecipes() {
        return RtgBlockEntity.acceptedFuelSpecs();
    }

    private RtgRecipeRuntime() {
    }
}
