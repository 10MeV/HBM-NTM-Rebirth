package com.hbm.items.bomb;

import com.hbm.ntm.registry.ModBlocks;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Legacy 1.7.10 package bridge for F.L.E.I.J.A. bomb parts.
 */
@Deprecated(forRemoval = false)
public class ItemFleija extends Item {
    private final boolean rare;

    public ItemFleija() {
        this(new Item.Properties(), false);
    }

    public ItemFleija(Item.Properties properties) {
        this(properties, false);
    }

    public ItemFleija(Item.Properties properties, boolean rare) {
        super(properties);
        this.rare = rare;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.bomb_part.used_in"));
        tooltip.add(ModBlocks.NUKE_FLEIJA.get().getName());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return rare ? Rarity.RARE : Rarity.COMMON;
    }
}
