package com.hbm.ntm.client;

import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.armor.FsbPoweredArmor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ArmorModuleHud {
    private static final int BAR_WIDTH = 81;
    private static final int ROW_HEIGHT = 4;
    private static final int ROW_Y_FROM_BOTTOM = 61;
    private static final List<EquipmentSlot> LEGACY_ORDER = List.of(
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD);

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, Player player) {
        if (player == null) {
            return;
        }
        List<List<Bar>> rows = collectBars(player);
        if (rows.stream().allMatch(List::isEmpty)) {
            return;
        }

        int left = screenWidth / 2 - 91;
        int row = 0;
        for (List<Bar> bars : rows) {
            if (bars.isEmpty()) {
                continue;
            }
            drawRow(graphics, left, screenHeight - ROW_Y_FROM_BOTTOM - row * ROW_HEIGHT, bars);
            row++;
        }
    }

    private static List<List<Bar>> collectBars(Player player) {
        List<List<Bar>> rows = new ArrayList<>(LEGACY_ORDER.size());
        for (EquipmentSlot slot : LEGACY_ORDER) {
            ItemStack armor = player.getItemBySlot(slot);
            List<Bar> bars = new ArrayList<>();
            addBars(armor, player, bars);
            if (ArmorModHandler.hasMods(armor)) {
                for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                    addBars(mod, player, bars);
                }
            }
            rows.add(bars);
        }
        return rows;
    }

    private static void addBars(ItemStack stack, Player player, List<Bar> bars) {
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof FsbPoweredArmor powered
                && FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player)) {
            long max = powered.getMaxCharge(stack);
            if (max > 0L) {
                bars.add(new Bar(Mth.clamp((float) powered.getCharge(stack) / (float) max, 0.0F, 1.0F), 0x00FF00));
            }
        }
        if (stack.getItem() instanceof ArmorModItems.Jetpack jetpack) {
            bars.add(new Bar(jetpack.getFuelFraction(stack), jetpack.getFuelColor()));
        }
    }

    private static void drawRow(GuiGraphics graphics, int left, int top, List<Bar> bars) {
        for (int i = 0; i < bars.size(); i++) {
            int start;
            int end;
            if (i == 0) {
                start = left;
                end = start + (bars.size() == 1 ? BAR_WIDTH : 40);
            } else {
                int splitWidth = (int) Math.ceil(40.0F / (bars.size() - 1));
                start = left + 41 + splitWidth * (i - 1);
                end = i == bars.size() - 1 ? left + BAR_WIDTH : start + splitWidth;
                if (i != 1) {
                    start++;
                }
            }
            drawBar(graphics, start, end, top, bars.get(i));
        }
    }

    private static void drawBar(GuiGraphics graphics, int start, int end, int top, Bar bar) {
        if (end <= start) {
            return;
        }
        graphics.fill(start, top - 1, end, top + 2, 0xFF404040);
        int fillEnd = start + 1 + Mth.floor(Math.max(0.0F, Math.min(1.0F, bar.fraction())) * (end - start - 1));
        if (fillEnd > start + 1) {
            graphics.fill(start + 1, top, Math.min(fillEnd, end), top + 1, 0xFF000000 | bar.color());
        }
    }

    private record Bar(float fraction, int color) {
    }

    private ArmorModuleHud() {
    }
}
