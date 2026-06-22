package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class KeyPinItem extends Item {
    private static final String TAG_PINS = "pins";

    private final boolean transferable;

    public KeyPinItem(Properties properties, boolean transferable) {
        super(properties);
        this.transferable = transferable;
    }

    public boolean canTransfer() {
        return transferable;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int pins = getPins(stack);
        if (pins != 0) {
            tooltip.add(Component.literal("Pin configuration: " + pins));
        } else {
            tooltip.add(Component.literal("Pins not set!"));
        }
        if (!transferable) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Pins can neither be changed, nor copied."));
        }
    }

    public static int getPins(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) {
            return 0;
        }
        return stack.getTag().getInt(TAG_PINS);
    }

    public static void setPins(ItemStack stack, int pins) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putInt(TAG_PINS, pins);
        }
    }

    public static boolean canTransfer(ItemStack stack) {
        return stack.getItem() instanceof KeyPinItem key && key.canTransfer();
    }
}
