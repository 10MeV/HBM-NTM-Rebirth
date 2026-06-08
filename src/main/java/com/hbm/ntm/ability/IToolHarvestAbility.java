package com.hbm.ntm.ability;

public interface IToolHarvestAbility extends IBaseAbility {
    int SORT_ORDER_BASE = 100;

    default void preHarvestAll(int level, ToolHarvestContext context) {
    }

    default void postHarvestAll(int level, ToolHarvestContext context) {
    }

    default void onHarvestBlock(int level, ToolHarvestContext context) {
        context.harvestBlock(false);
    }

    static IToolHarvestAbility getByName(String name) {
        return ToolHarvestAbilities.getByName(name);
    }
}
