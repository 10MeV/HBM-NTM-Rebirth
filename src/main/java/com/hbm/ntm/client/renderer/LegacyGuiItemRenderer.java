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

        ItemPopTransform transform = popTransform(x, y, stack.getPopTime(), partialTick);
        if (transform.active()) {
            graphics.pose().pushPose();
            applyPopTransform(graphics, transform);
        }

        graphics.renderItem(stack, x, y);
        if (transform.active()) {
            graphics.pose().popPose();
        }

        graphics.renderItem(stack, x, y);
        if (renderDecorations) {
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
        }
    }

    public static ItemPopTransform popTransform(int x, int y, int popTime, float partialTick) {
        float pop = popTime - partialTick;
        if (pop <= 0.0F) {
            return ItemPopTransform.inactive();
        }
        float scale = 1.0F + pop / 5.0F;
        return new ItemPopTransform(true, pop, x + 8.0F, y + 12.0F, 1.0F / scale,
                (scale + 1.0F) / 2.0F, 1.0F);
    }

    public static void applyPopTransform(GuiGraphics graphics, ItemPopTransform transform) {
        if (graphics == null || transform == null || !transform.active()) {
            return;
        }
        graphics.pose().translate(transform.pivotX(), transform.pivotY(), 0.0F);
        graphics.pose().scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
        graphics.pose().translate(-transform.pivotX(), -transform.pivotY(), 0.0F);
    }

    public record ItemPopTransform(boolean active, float pop, float pivotX, float pivotY,
            float scaleX, float scaleY, float scaleZ) {
        public static ItemPopTransform inactive() {
            return new ItemPopTransform(false, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private LegacyGuiItemRenderer() {
    }
}
