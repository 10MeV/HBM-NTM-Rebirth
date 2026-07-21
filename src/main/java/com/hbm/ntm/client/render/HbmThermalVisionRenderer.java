package com.hbm.ntm.client.render;

import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.client.LegacySednaAimProgress;
import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.renderer.LegacyOverheadRenderer;
import com.hbm.ntm.item.FsbArmorItem;
import com.hbm.ntm.item.SednaGunItem;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

/**
 * Modern renderer for the old {@code RenderOverhead.renderThermalSight} entity boxes.
 * The shared overhead renderer owns classification, line geometry, and the 512-block culling rule.
 */
@OnlyIn(Dist.CLIENT)
public final class HbmThermalVisionRenderer {
    private static final PoseStack POSE = new PoseStack();

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.level == null || player == null || !ClientHbmPlayerProperties.shouldRenderHud()) {
            return;
        }
        if (!hasThermalSights(player)) {
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = POSE;
        poseStack.setIdentity();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose pose = poseStack.last();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!LegacyOverheadRenderer.shouldRenderThermalEntity(entity, player)) {
                continue;
            }
            int color = LegacyOverheadRenderer.thermalEntityColor(entity, player.tickCount);
            if (color != LegacyOverheadRenderer.THERMAL_SKIP_COLOR) {
                LegacyLineRenderer.boxPositionColor(builder, pose,
                        entity.getBoundingBox().minX - cameraPos.x, entity.getBoundingBox().minY - cameraPos.y,
                        entity.getBoundingBox().minZ - cameraPos.z, entity.getBoundingBox().maxX - cameraPos.x,
                        entity.getBoundingBox().maxY - cameraPos.y, entity.getBoundingBox().maxZ - cameraPos.z,
                        color, 255);
            }
        }
        Tesselator.getInstance().end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Source: 1.7.10 {@code ModEventHandlerClient}: a full FSB suit with the
     * thermal trait always enables the overlay; a held Sedna gun does so only
     * while its completed aim state is active and one of its modes has thermal
     * sights.  The modern client preserves the same four-tick interpolation in
     * {@link LegacySednaAimProgress}; {@code fullyAimed()} is the exact old
     * {@code aimingProgress == 1} predicate used by this overlay.
     */
    private static boolean hasThermalSights(Player player) {
        FsbArmorItem chestplate = FsbArmorItem.chestplate(player);
        if (chestplate != null && chestplate.fullSetTraits().thermal()) {
            return true;
        }
        if (!(player.getMainHandItem().getItem() instanceof SednaGunItem gun)
                || !LegacySednaAimProgress.fullyAimed()) {
            return false;
        }
        return gun.config().configs().stream().anyMatch(mode -> mode.thermalSights());
    }

    private HbmThermalVisionRenderer() {
    }
}
