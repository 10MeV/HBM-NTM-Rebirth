package com.hbm.ntm.api.tile;

import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;

public interface LegacyUpgradeInfoProvider {
    String KEY_ACID = "upgrade.acid";
    String KEY_BURN = "upgrade.burn";
    String KEY_CONSUMPTION = "upgrade.consumption";
    String KEY_COOLANT_CONSUMPTION = "upgrade.coolantConsumption";
    String KEY_DELAY = "upgrade.delay";
    String KEY_SPEED = "upgrade.speed";
    String KEY_EFFICIENCY = "upgrade.efficiency";
    String KEY_PRODUCTIVITY = "upgrade.productivity";
    String KEY_FORTUNE = "upgrade.fortune";
    String KEY_RANGE = "upgrade.range";

    default boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
        Map<UpgradeType, Integer> upgrades = getValidUpgrades();
        return upgrades != null && upgrades.containsKey(type) && level <= upgrades.get(type);
    }

    default void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
    }

    Map<UpgradeType, Integer> getValidUpgrades();
}
