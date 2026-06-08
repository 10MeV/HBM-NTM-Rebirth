package com.hbm.ntm.ability;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ToolAbilityConfiguration {
    public static final String TAG_ACTIVE_PRESET = "ability";
    public static final String TAG_PRESETS = "abilityPresets";
    private static final int MAX_PRESETS = 99;

    private final List<ToolPreset> presets = new ArrayList<>();
    private int currentPreset;

    public static ToolAbilityConfiguration get(ItemStackAccess stackAccess, AvailableAbilities availableAbilities) {
        ToolAbilityConfiguration configuration = new ToolAbilityConfiguration();
        CompoundTag tag = stackAccess.tag();
        if (tag == null || !tag.contains(TAG_ACTIVE_PRESET) || !tag.contains(TAG_PRESETS, Tag.TAG_LIST)) {
            configuration.reset(availableAbilities);
            return configuration;
        }

        configuration.readFromNBT(tag);
        configuration.restrictTo(availableAbilities);
        if (configuration.presets.isEmpty()) {
            configuration.reset(availableAbilities);
        }
        return configuration;
    }

    public List<ToolPreset> presets() {
        return presets;
    }

    public int currentPreset() {
        return currentPreset;
    }

    public void setCurrentPreset(int currentPreset) {
        this.currentPreset = clampPreset(currentPreset);
    }

    public void cycle(boolean reset) {
        if (presets.isEmpty()) {
            currentPreset = 0;
            return;
        }
        currentPreset = reset ? 0 : (currentPreset + 1) % presets.size();
    }

    public ToolPreset getActivePreset() {
        if (presets.isEmpty()) {
            return new ToolPreset();
        }
        currentPreset = clampPreset(currentPreset);
        return presets.get(currentPreset);
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putInt(TAG_ACTIVE_PRESET, currentPreset);

        ListTag nbtPresets = new ListTag();
        for (ToolPreset preset : presets) {
            CompoundTag nbtPreset = new CompoundTag();
            preset.writeToNBT(nbtPreset);
            nbtPresets.add(nbtPreset);
        }
        tag.put(TAG_PRESETS, nbtPresets);
    }

    public void readFromNBT(CompoundTag tag) {
        currentPreset = tag.getInt(TAG_ACTIVE_PRESET);

        presets.clear();
        ListTag nbtPresets = tag.getList(TAG_PRESETS, Tag.TAG_COMPOUND);
        int numPresets = Math.min(nbtPresets.size(), MAX_PRESETS);
        for (int i = 0; i < numPresets; i++) {
            ToolPreset preset = new ToolPreset();
            preset.readFromNBT(nbtPresets.getCompound(i));
            presets.add(preset);
        }
        currentPreset = clampPreset(currentPreset);
    }

    public void reset(AvailableAbilities availableAbilities) {
        currentPreset = 0;
        presets.clear();
        presets.add(new ToolPreset());

        availableAbilities.getToolAreaAbilities().forEach((ability, level) -> {
            if (ability != ToolAreaAbilities.NONE) {
                presets.add(new ToolPreset(ability, level, ToolHarvestAbilities.NONE, 0));
            }
        });

        availableAbilities.getToolHarvestAbilities().forEach((ability, level) -> {
            if (ability != ToolHarvestAbilities.NONE) {
                presets.add(new ToolPreset(ToolAreaAbilities.NONE, 0, ability, level));
            }
        });

        presets.sort(Comparator
                .comparing((ToolPreset preset) -> preset.harvestAbility)
                .thenComparingInt(preset -> preset.harvestAbilityLevel)
                .thenComparing(preset -> preset.areaAbility)
                .thenComparingInt(preset -> preset.areaAbilityLevel));
    }

    public void restrictTo(AvailableAbilities availableAbilities) {
        presets.forEach(preset -> preset.restrictTo(availableAbilities));
        currentPreset = clampPreset(currentPreset);
    }

    private int clampPreset(int preset) {
        if (presets.isEmpty()) {
            return 0;
        }
        return Math.max(0, Math.min(preset, presets.size() - 1));
    }

    @FunctionalInterface
    public interface ItemStackAccess {
        CompoundTag tag();
    }
}
