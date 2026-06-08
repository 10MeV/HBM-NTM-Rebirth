package com.hbm.ntm.api.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface RecipeSink {
    void accept(ResourceLocation id, JsonObject recipe);
}
