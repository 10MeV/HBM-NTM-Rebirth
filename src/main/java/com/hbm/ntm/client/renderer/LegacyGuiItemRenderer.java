package com.hbm.ntm.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class LegacyGuiItemRenderer {
    public static void renderItemStack(GuiGraphics graphics, ItemStack stack, int x, int y) {
        renderItemStack(graphics, stack, x, y, Minecraft.getInstance().getPartialTick(), true);
    }

    public static void renderItemStack(GuiGraphics graphics, ItemStack stack, int x, int y, float partialTick,
            boolean renderDecorations) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        float pop = stack.getPopTime() - partialTick;
        if (pop > 0.0F) {
            graphics.pose().pushPose();
            float scale = 1.0F + pop / 5.0F;
            graphics.pose().translate(x + 8.0F, y + 12.0F, 0.0F);
            graphics.pose().scale(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
            graphics.pose().translate(-(x + 8.0F), -(y + 12.0F), 0.0F);
        }

        graphics.renderItem(stack, x, y);
        if (pop > 0.0F) {
            graphics.pose().popPose();
        }

        graphics.renderItem(stack, x, y);
        if (renderDecorations) {
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
        }
    }

    private LegacyGuiItemRenderer() {
    }
}
