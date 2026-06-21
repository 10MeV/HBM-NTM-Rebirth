package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

public class ItemMachineUpgrade extends Item {
    private final UpgradeType type;
    private final int tier;

    public ItemMachineUpgrade(Properties properties, UpgradeType type, int tier) {
        super(properties.stacksTo(1));
        this.type = type;
        this.tier = Math.max(0, tier);
    }

    public UpgradeType getUpgradeType() {
        return type;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Boolean provided = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.ClientMachineUpgradeTooltip.appendContextualInfo(this, tooltip,
                        flag.isAdvanced()));
        if (Boolean.TRUE.equals(provided)) {
            return;
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public enum UpgradeType {
        SPEED,
        EFFECT,
        POWER,
        OVERDRIVE,
        AFTERBURN,
        FORTUNE,
        SMELTER,
        NULLIFIER,
        SHREDDER,
        CENTRIFUGE,
        CRYSTALLIZER
    }
}
