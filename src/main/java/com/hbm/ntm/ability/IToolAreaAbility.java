package com.hbm.ntm.ability;

public interface IToolAreaAbility extends IBaseAbility {
    int SORT_ORDER_BASE = 0;

    boolean onDig(int level, ToolDigContext context);

    default boolean allowsHarvest(int level) {
        return true;
    }

    static IToolAreaAbility getByName(String name) {
        return ToolAreaAbilities.getByName(name);
    }
}
