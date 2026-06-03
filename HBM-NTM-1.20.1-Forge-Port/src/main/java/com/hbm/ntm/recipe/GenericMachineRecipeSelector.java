package com.hbm.ntm.recipe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GenericMachineRecipeSelector {
    public static final int DEFAULT_SELECTOR_INDEX = 0;
    public static final String TAG_INDEX = "index";
    public static final String TAG_SELECTION = "selection";

    private GenericMachineRecipeSelector() {
    }

    public static CompoundTag selectionTag(@Nullable String selection) {
        return selectionTag(DEFAULT_SELECTOR_INDEX, selection);
    }

    public static CompoundTag selectionTag(int index, @Nullable String selection) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_INDEX, index);
        tag.putString(TAG_SELECTION, normalize(selection));
        return tag;
    }

    public static boolean isSelectionTag(CompoundTag tag) {
        return tag != null
                && tag.getInt(TAG_INDEX) == DEFAULT_SELECTOR_INDEX
                && tag.contains(TAG_SELECTION);
    }

    public static String readSelection(CompoundTag tag) {
        return normalize(tag == null ? null : tag.getString(TAG_SELECTION));
    }

    public static String normalize(@Nullable String selection) {
        return selection == null || selection.isBlank()
                ? GenericMachineRecipeRuntime.NULL_RECIPE
                : selection;
    }

    public static boolean isNullSelection(@Nullable String selection) {
        return GenericMachineRecipeRuntime.NULL_RECIPE.equals(normalize(selection));
    }

    public static boolean canSelect(@Nullable Level level, GenericMachineRecipe.Machine machine, @Nullable String selection) {
        String normalized = normalize(selection);
        return isNullSelection(normalized)
                || (level != null && GenericMachineRecipeRuntime.hasRecipe(level, machine, normalized));
    }

    public static List<GenericMachineRecipe> recipes(@Nullable Level level, GenericMachineRecipe.Machine machine) {
        return level == null ? List.of() : GenericMachineRecipeRuntime.recipes(level, machine);
    }
}
