package com.hbm.ntm.ability;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ToolPreset {
    public IToolAreaAbility areaAbility = ToolAreaAbilities.NONE;
    public int areaAbilityLevel = 0;
    public IToolHarvestAbility harvestAbility = ToolHarvestAbilities.NONE;
    public int harvestAbilityLevel = 0;

    public ToolPreset() {
    }

    public ToolPreset(IToolAreaAbility areaAbility, IToolHarvestAbility harvestAbility) {
        this.areaAbility = areaAbility;
        this.harvestAbility = harvestAbility;
    }

    public ToolPreset(IToolAreaAbility areaAbility, int areaAbilityLevel,
                      IToolHarvestAbility harvestAbility, int harvestAbilityLevel) {
        this.areaAbility = areaAbility;
        this.areaAbilityLevel = areaAbilityLevel;
        this.harvestAbility = harvestAbility;
        this.harvestAbilityLevel = harvestAbilityLevel;
    }

    public MutableComponent getMessage() {
        if (isNone()) {
            return Component.translatable("chat.hbm_ntm_rebirth.tool_ability.deactivated").withStyle(ChatFormatting.GOLD);
        }

        boolean hasArea = areaAbility != ToolAreaAbilities.NONE;
        boolean hasHarvest = harvestAbility != ToolHarvestAbilities.NONE;
        MutableComponent message = Component.translatable("chat.hbm_ntm_rebirth.tool_ability.enabled").append(" ");

        if (hasArea) {
            message.append(areaAbility.getFullName(areaAbilityLevel));
        }
        if (hasArea && hasHarvest) {
            message.append(Component.literal(" + "));
        }
        if (hasHarvest) {
            message.append(harvestAbility.getFullName(harvestAbilityLevel));
        }
        return message.withStyle(ChatFormatting.YELLOW);
    }

    public boolean isNone() {
        return areaAbility == ToolAreaAbilities.NONE && harvestAbility == ToolHarvestAbilities.NONE;
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putString("area", areaAbility.getName());
        tag.putInt("areaLevel", areaAbilityLevel);
        tag.putString("harvest", harvestAbility.getName());
        tag.putInt("harvestLevel", harvestAbilityLevel);
    }

    public void readFromNBT(CompoundTag tag) {
        areaAbility = IToolAreaAbility.getByName(tag.getString("area"));
        areaAbilityLevel = clamp(tag.getInt("areaLevel"), areaAbility.levels());
        harvestAbility = IToolHarvestAbility.getByName(tag.getString("harvest"));
        harvestAbilityLevel = clamp(tag.getInt("harvestLevel"), harvestAbility.levels());
    }

    public void restrictTo(AvailableAbilities availableAbilities) {
        int maxAreaLevel = availableAbilities.maxLevel(areaAbility);
        if (maxAreaLevel == -1) {
            areaAbility = ToolAreaAbilities.NONE;
            areaAbilityLevel = 0;
        } else {
            areaAbilityLevel = Math.max(0, Math.min(areaAbilityLevel, maxAreaLevel));
        }

        if (!areaAbility.allowsHarvest(areaAbilityLevel)) {
            harvestAbility = ToolHarvestAbilities.NONE;
            harvestAbilityLevel = 0;
        }

        int maxHarvestLevel = availableAbilities.maxLevel(harvestAbility);
        if (maxHarvestLevel == -1) {
            harvestAbility = ToolHarvestAbilities.NONE;
            harvestAbilityLevel = 0;
        } else {
            harvestAbilityLevel = Math.max(0, Math.min(harvestAbilityLevel, maxHarvestLevel));
        }
    }

    private static int clamp(int level, int levels) {
        return Math.max(0, Math.min(level, Math.max(0, levels - 1)));
    }
}
