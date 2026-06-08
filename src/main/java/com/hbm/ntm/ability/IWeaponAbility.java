package com.hbm.ntm.ability;

public interface IWeaponAbility extends IBaseAbility {
    int SORT_ORDER_BASE = 200;

    void onHit(int level, WeaponHitContext context);

    static IWeaponAbility getByName(String name) {
        return WeaponAbilities.getByName(name);
    }
}
