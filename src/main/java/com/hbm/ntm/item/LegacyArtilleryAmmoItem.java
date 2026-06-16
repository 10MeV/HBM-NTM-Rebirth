package com.hbm.ntm.item;

import com.hbm.ntm.artillery.LegacyArtilleryAmmoCatalog;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyArtilleryAmmoItem extends Item {
    private final LegacyArtilleryAmmoCatalog.AmmoType type;

    public LegacyArtilleryAmmoItem(Properties properties, LegacyArtilleryAmmoCatalog.AmmoType type) {
        super(properties);
        this.type = type;
    }

    public LegacyArtilleryAmmoCatalog.AmmoType type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (type == LegacyArtilleryAmmoCatalog.AMMO_ARTY_CARGO) {
            if (stack.hasTag() && stack.getTag().contains("cargo")) {
                ItemStack cargo = ItemStack.of(stack.getTag().getCompound("cargo"));
                if (!cargo.isEmpty()) {
                    tooltip.add(cargo.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
                    return;
                }
            }
            tooltip.add(Component.translatable(type.tooltipKey()).withStyle(ChatFormatting.RED));
            return;
        }
        for (String key : type.tooltipKeys()) {
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }
}
