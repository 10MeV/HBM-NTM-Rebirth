package com.hbm.ntm.client.render;

import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.client.renderer.LegacyOverheadRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class HbmOverheadMarkers {
    private static final Map<BlockPos, Marker> QUEUED = new HashMap<>();
    private static final Map<BlockPos, Marker> ACTIVE = new HashMap<>();

    public static void queue(double x, double y, double z, int color, int expiresMillis, double maxDistance, String label) {
        long expireAt = expiresMillis > 0 ? System.currentTimeMillis() + expiresMillis : 0L;
        QUEUED.put(BlockPos.containing(x, y, z), new Marker(color, expireAt, maxDistance, label == null || label.isEmpty() ? null : label));
    }

    public static void tick() {
        mergeQueuedMarkers();
        pruneExpiredMarkers();
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        mergeQueuedMarkers();
        if (ACTIVE.isEmpty()) {
            return;
        }
        pruneExpiredMarkers();
        if (ACTIVE.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (minecraft.level == null || player == null || !ClientHbmPlayerProperties.shouldRenderHud()) {
            return;
        }
        pruneDistantMarkers(player);
        if (ACTIVE.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack markerPose = new PoseStack();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

        for (Map.Entry<BlockPos, Marker> entry : ACTIVE.entrySet()) {
            renderBox(builder, markerPose, cameraPos, entry.getKey(), entry.getValue());
        }
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        renderLabels(minecraft, camera, player);
    }

    public static void clearAll() {
        QUEUED.clear();
        ACTIVE.clear();
    }

    private static void mergeQueuedMarkers() {
        if (QUEUED.isEmpty()) {
            return;
        }
        ACTIVE.putAll(QUEUED);
        QUEUED.clear();
    }

    private static void pruneExpiredMarkers() {
        if (ACTIVE.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<BlockPos, Marker>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Marker marker = iterator.next().getValue();
            if (LegacyOverheadRenderer.expired(marker.expireAt, now)) {
                iterator.remove();
            }
        }
    }

    private static void pruneDistantMarkers(Player player) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BlockPos, Marker>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Marker> entry = iterator.next();
            BlockPos pos = entry.getKey();
            Marker marker = entry.getValue();
            if (LegacyOverheadRenderer.tooFar(player.position(), LegacyOverheadRenderer.markerCenter(pos), marker.maxDistance)) {
                iterator.remove();
            }
        }
    }

    private static void renderLabels(Minecraft minecraft, Camera camera, Player player) {
        PoseStack poseStack = new PoseStack();
        for (Map.Entry<BlockPos, Marker> entry : ACTIVE.entrySet()) {
            BlockPos pos = entry.getKey();
            Marker marker = entry.getValue();
            if (LegacyOverheadRenderer.tooFar(player.position(), LegacyOverheadRenderer.markerCenter(pos), marker.maxDistance)) {
                continue;
            }
            renderLabel(minecraft, camera, player, poseStack, pos, marker);
        }
    }

    private static void renderLabel(Minecraft minecraft, Camera camera, Player player, PoseStack poseStack, BlockPos pos, Marker marker) {
        Vec3 cameraPos = camera.getPosition();
        Vec3 toMarker = LegacyOverheadRenderer.markerCenter(pos).subtract(cameraPos);
        Vec3 labelPos = LegacyOverheadRenderer.labelPosition(toMarker);
        if (labelPos == Vec3.ZERO) {
            return;
        }

        String label = LegacyOverheadRenderer.markerLabel(marker.label, player.getLookAngle(), toMarker);
        if (label.isEmpty()) {
            return;
        }

        Font font = minecraft.font;
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        LegacyOverheadRenderer.legacyDualPassLabel(font, buffer, poseStack, camera.rotation(), labelPos, label, marker.color);
        buffer.endBatch();
    }

    private static void renderBox(BufferBuilder builder, PoseStack poseStack, Vec3 cameraPos, BlockPos pos, Marker marker) {
        LegacyOverheadRenderer.markerBoxRelative(builder, poseStack.last(), pos, cameraPos, marker.color, 255);
    }

    private record Marker(int color, long expireAt, double maxDistance, String label) {
    }

    private HbmOverheadMarkers() {
    }
}
