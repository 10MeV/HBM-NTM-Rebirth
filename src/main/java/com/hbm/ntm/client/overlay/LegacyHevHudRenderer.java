package com.hbm.ntm.client.overlay;

import com.hbm.ntm.armor.FsbPoweredArmor;
import com.hbm.ntm.client.ClientHbmLivingProperties;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;

public final class LegacyHevHudRenderer {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };
    private static long lastSurveyMs;
    private static float previousRadiation;
    private static float lastRadiation;

    public static boolean handlePre(RenderGuiOverlayEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (!shouldRender(player)) {
            return false;
        }

        if (event.getOverlay().id().equals(VanillaGuiOverlay.ARMOR_LEVEL.id())) {
            event.setCanceled(true);
            return true;
        }
        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())) {
            event.setCanceled(true);
            render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), player);
            return true;
        }
        return false;
    }

    private static boolean shouldRender(Player player) {
        return player != null
                && !Minecraft.getInstance().options.hideGui
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.HEV_PLATE.get())
                && FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player);
    }

    private static void render(GuiGraphics graphics, int screenWidth, int screenHeight, Player player) {
        float radiation = ClientHbmLivingProperties.getRadiation();
        float delta = lastRadiation - previousRadiation;
        long now = System.currentTimeMillis();
        if (now >= lastSurveyMs + 1000L) {
            lastSurveyMs = now;
            previousRadiation = lastRadiation;
            lastRadiation = radiation;
        }

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        graphics.pose().pushPose();
        graphics.pose().scale(2.0F, 2.0F, 2.0F);
        drawLegacyString(graphics, "+" + (int) (player.getHealth() * 5.0F),
                8 / 2, (screenHeight - 18 - 2) / 2, player.getHealth() * 5.0F > 15.0F ? 0xFFFF8000 : 0xFFFF0000);

        double chargeUnits = armorChargeUnits(player);
        drawLegacyString(graphics, "||" + (int) chargeUnits,
                70 / 2, (screenHeight - 18 - 2) / 2, chargeUnits > 15.0D ? 0xFFFF8000 : 0xFFFF0000);

        drawLegacyString(graphics, radiationBar(radiation),
                8 / 2, (screenHeight - 40) / 2, radiation < 800.0F ? 0xFFFF8000 : 0xFFFF0000);
        graphics.pose().popPose();

        if (delta > 0.0F) {
            drawLegacyString(graphics, radiationDeltaText(delta), 32, screenHeight - 55, 0xFFFF0000);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawLegacyString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(Minecraft.getInstance().font, text, x, y, color, false);
    }

    private static double armorChargeUnits(Player player) {
        double chargeUnits = 0.0D;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof FsbPoweredArmor armor) {
                long maxCharge = armor.getMaxCharge(stack);
                if (maxCharge > 0L) {
                    chargeUnits += (double) armor.getCharge(stack) / (double) maxCharge;
                }
            }
        }
        return chargeUnits * 25.0D;
    }

    private static String radiationBar(float radiation) {
        StringBuilder builder = new StringBuilder("\u2622[");
        for (int i = 0; i < 10; i++) {
            if (radiation / 100.0F > i) {
                int mid = (int) (radiation - i * 100.0F);
                if (mid < 33) {
                    builder.append("..");
                } else if (mid < 67) {
                    builder.append("|.");
                } else {
                    builder.append("||");
                }
            } else {
                builder.append(' ');
            }
        }
        return builder.append(']').toString();
    }

    private static String radiationDeltaText(float radiation) {
        if (radiation > 1000.0F) {
            return ">1000 RAD/s";
        }
        if (radiation < 1.0F) {
            return "<1 RAD/s";
        }
        return Math.round(radiation) + " RAD/s";
    }

    private LegacyHevHudRenderer() {
    }
}
