package com.hbm.ntm.client;

import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.menu.TurretMenu;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class ClientMachineUpgradeTooltip {
    private ClientMachineUpgradeTooltip() {
    }

    public static boolean appendContextualInfo(ItemMachineUpgrade upgrade, List<Component> tooltip,
            boolean extendedInfo) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) {
            return false;
        }
        LegacyUpgradeInfoProvider provider = upgradeProvider(screen.getMenu());
        if (provider == null || !provider.canProvideInfo(upgrade.getUpgradeType(), upgrade.getTier(), extendedInfo)) {
            return false;
        }
        provider.provideInfo(upgrade.getUpgradeType(), upgrade.getTier(), tooltip, extendedInfo);
        return true;
    }

    private static LegacyUpgradeInfoProvider upgradeProvider(AbstractContainerMenu menu) {
        if (menu instanceof LegacyUpgradeInfoProvider provider) {
            return provider;
        }
        if (menu instanceof TurretMenu turretMenu
                && turretMenu.getUpgradeInfoProvider() instanceof LegacyUpgradeInfoProvider provider) {
            return provider;
        }
        return null;
    }
}
