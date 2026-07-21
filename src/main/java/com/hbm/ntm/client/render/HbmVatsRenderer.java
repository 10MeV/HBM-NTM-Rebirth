package com.hbm.ntm.client.render;

import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.client.renderer.LegacyOverheadRenderer;
import com.hbm.ntm.item.FsbArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Source-backed FSB VATS target-health tags from the old RenderLivingEvent.Pre hook.
 * Text layout and the two tag passes remain owned by {@link LegacyOverheadRenderer}.
 */
public final class HbmVatsRenderer {
    private static final int SNEAKING_TAG_DISTANCE = 32;

    public static void render(LivingEntity target, PoseStack poseStack, MultiBufferSource buffer) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !ClientHbmPlayerProperties.shouldRenderHud()) {
            return;
        }

        FsbArmorItem chestplate = FsbArmorItem.chestplate(player);
        if (chestplate == null || !chestplate.fullSetTraits().vats()) {
            return;
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        if (!LegacyOverheadRenderer.shouldRenderTag(true, target == cameraEntity, target.isInvisibleTo(player),
                target.isVehicle())) {
            return;
        }

        int count = (int) Math.min(target.getMaxHealth(), 100.0F);
        if (count <= 0) {
            return;
        }
        int bars = (int) Math.ceil(target.getHealth() * count / target.getMaxHealth());
        String healthBar = healthBar(count, bars);
        int tagDistance = target.isDiscrete() ? SNEAKING_TAG_DISTANCE : LegacyOverheadRenderer.DEFAULT_TAG_DISTANCE;
        LegacyOverheadRenderer.TagDrawPlan plan = LegacyOverheadRenderer.tagDrawPlan(healthBar,
                minecraft.font.width(healthBar), target.getBbHeight(), target.isSleeping(),
                player.distanceToSqr(target), tagDistance, chestplate.fullSetTraits().thermal(),
                LegacyOverheadRenderer.DEFAULT_TAG_COLOR, LegacyOverheadRenderer.DEFAULT_TAG_SEE_THROUGH_COLOR);
        if (!plan.visible()) {
            return;
        }

        LegacyOverheadRenderer.legacyDualPassLabel(minecraft.font, buffer, poseStack,
                minecraft.getEntityRenderDispatcher().cameraOrientation(), 0.0D,
                plan.verticalOffset(), 0.0D, healthBar, plan.color(), plan.seeThroughColor(),
                plan.backgroundColor(), plan.disableDepthTest());
    }

    private static String healthBar(int count, int bars) {
        StringBuilder result = new StringBuilder(count + 4).append(ChatFormatting.RED);
        for (int index = 0; index < count; index++) {
            if (index == bars) {
                result.append(ChatFormatting.RESET);
            }
            result.append('|');
        }
        return result.toString();
    }

    private HbmVatsRenderer() {
    }
}
