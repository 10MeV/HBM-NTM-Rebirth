package com.hbm.ntm.client;

import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.armor.FsbPoweredArmor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ArmorModuleHud {
    private static final int BAR_WIDTH = 81;
    private static final int ROW_HEIGHT = 4;
    private static final int ROW_Y_FROM_BOTTOM = 61;
    private static final EquipmentSlot[] LEGACY_ORDER = {
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };
    private static final int MAX_BARS_PER_ROW = (ArmorModHandler.MOD_SLOTS + 1) * 2;
    private static final float[] BAR_FRACTIONS = new float[MAX_BARS_PER_ROW];
    private static final int[] BAR_COLORS = new int[MAX_BARS_PER_ROW];

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, Player player) {
        if (player == null) {
            return;
        }

        int left = screenWidth / 2 - 91;
        int row = 0;
        for (EquipmentSlot slot : LEGACY_ORDER) {
            int barCount = collectBars(player.getItemBySlot(slot), player);
            if (barCount == 0) {
                continue;
            }
            drawRow(graphics, left, screenHeight - ROW_Y_FROM_BOTTOM - row * ROW_HEIGHT, barCount);
            row++;
        }
    }

    private static int collectBars(ItemStack armor, Player player) {
        int count = addBars(armor, player, 0);
        if (ArmorModHandler.hasMods(armor)) {
            for (int slot = 0; slot < ArmorModHandler.MOD_SLOTS; slot++) {
                count = addBars(ArmorModHandler.pryMod(armor, slot), player, count);
            }
        }
        return count;
    }

    private static int addBars(ItemStack stack, Player player, int count) {
        if (stack.isEmpty()) {
            return count;
        }
        if (stack.getItem() instanceof FsbPoweredArmor powered
                && FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player)) {
            long max = powered.getMaxCharge(stack);
            if (max > 0L && count < MAX_BARS_PER_ROW) {
                BAR_FRACTIONS[count] = Mth.clamp((float) powered.getCharge(stack) / (float) max, 0.0F, 1.0F);
                BAR_COLORS[count] = 0x00FF00;
                count++;
            }
        }
        if (stack.getItem() instanceof ArmorModItems.Jetpack jetpack && count < MAX_BARS_PER_ROW) {
            BAR_FRACTIONS[count] = jetpack.getFuelFraction(stack);
            BAR_COLORS[count] = jetpack.getFuelColor();
            count++;
        }
        return count;
    }

    private static void drawRow(GuiGraphics graphics, int left, int top, int barCount) {
        for (int i = 0; i < barCount; i++) {
            int start;
            int end;
            if (i == 0) {
                start = left;
                end = start + (barCount == 1 ? BAR_WIDTH : 40);
            } else {
                int splitWidth = (int) Math.ceil(40.0F / (barCount - 1));
                start = left + 41 + splitWidth * (i - 1);
                end = i == barCount - 1 ? left + BAR_WIDTH : start + splitWidth;
                if (i != 1) {
                    start++;
                }
            }
            drawBar(graphics, start, end, top, BAR_FRACTIONS[i], BAR_COLORS[i]);
        }
    }

    private static void drawBar(GuiGraphics graphics, int start, int end, int top, float fraction, int color) {
        if (end <= start) {
            return;
        }
        graphics.fill(start, top - 1, end, top + 2, 0xFF404040);
        int fillEnd = start + 1 + Mth.floor(Math.max(0.0F, Math.min(1.0F, fraction)) * (end - start - 1));
        if (fillEnd > start + 1) {
            graphics.fill(start + 1, top, Math.min(fillEnd, end), top + 1, 0xFF000000 | color);
        }
    }

    private ArmorModuleHud() {
    }
}
