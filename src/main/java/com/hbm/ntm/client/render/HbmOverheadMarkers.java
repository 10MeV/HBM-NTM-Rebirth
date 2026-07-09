package com.hbm.ntm.client.render;

import com.hbm.ntm.client.ClientHbmPlayerProperties;
import com.hbm.ntm.client.obj.LegacyLineRenderer;
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
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class HbmOverheadMarkers {
    private static final Map<BlockPos, Marker> QUEUED = new HashMap<>();
    private static final Map<BlockPos, Marker> ACTIVE = new HashMap<>();
    private static final PoseStack MARKER_POSE = new PoseStack();
    private static final PoseStack LABEL_POSE = new PoseStack();

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
        PoseStack markerPose = MARKER_POSE;
        markerPose.setIdentity();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose markerPoseEntry = markerPose.last();
        for (Map.Entry<BlockPos, Marker> entry : ACTIVE.entrySet()) {
            emitBox(builder, markerPoseEntry, cameraPos, entry.getKey(), entry.getValue());
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
            if (tooFar(player, pos, marker.maxDistance)) {
                iterator.remove();
            }
        }
    }

    private static void renderLabels(Minecraft minecraft, Camera camera, Player player) {
        PoseStack poseStack = LABEL_POSE;
        Vec3 cameraPos = camera.getPosition();
        Quaternionf cameraRotation = camera.rotation();
        Vec3 look = player.getLookAngle();
        for (Map.Entry<BlockPos, Marker> entry : ACTIVE.entrySet()) {
            BlockPos pos = entry.getKey();
            Marker marker = entry.getValue();
            poseStack.setIdentity();
            renderLabel(minecraft, cameraPos, cameraRotation, look, poseStack, pos, marker);
        }
    }

    private static void renderLabel(Minecraft minecraft, Vec3 cameraPos, Quaternionf cameraRotation, Vec3 look,
            PoseStack poseStack, BlockPos pos, Marker marker) {
        double centerX = markerCenterX(pos);
        double centerY = markerCenterY(pos);
        double centerZ = markerCenterZ(pos);
        double deltaX = centerX - cameraPos.x;
        double deltaY = centerY - cameraPos.y;
        double deltaZ = centerZ - cameraPos.z;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (distance <= 1.0E-4D) {
            return;
        }

        String label = marker.label == null ? "" : marker.label;
        double lookDelta = Math.abs(look.x - deltaX / distance)
                + Math.abs(look.y - deltaY / distance)
                + Math.abs(look.z - deltaZ / distance);
        if (lookDelta < LegacyOverheadRenderer.LOOK_APPEND_THRESHOLD) {
            label += (!label.isEmpty() ? " " : "") + ((int) distance) + "m";
        }
        if (label.isEmpty()) {
            return;
        }

        double labelScale = Math.min(distance, LegacyOverheadRenderer.LABEL_MAX_DISTANCE) / distance;
        Font font = minecraft.font;
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        LegacyOverheadRenderer.legacyDualPassLabel(font, buffer, poseStack, cameraRotation,
                deltaX * labelScale, deltaY * labelScale, deltaZ * labelScale, label, marker.color);
        buffer.endBatch();
    }

    private static void emitBox(BufferBuilder builder, PoseStack.Pose pose, Vec3 cameraPos, BlockPos pos, Marker marker) {
        LegacyLineRenderer.boxPositionColor(builder, pose,
                pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z,
                pos.getX() + 1.0D - cameraPos.x, pos.getY() + 1.0D - cameraPos.y, pos.getZ() + 1.0D - cameraPos.z,
                marker.color, 255);
    }

    private static boolean tooFar(Player player, BlockPos pos, double maxDistance) {
        if (maxDistance <= 0.0D) {
            return false;
        }
        double deltaX = player.getX() - markerCenterX(pos);
        double deltaY = player.getY() - markerCenterY(pos);
        double deltaZ = player.getZ() - markerCenterZ(pos);
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ > maxDistance * maxDistance;
    }

    private static double markerCenterX(BlockPos pos) {
        return pos.getX() + 0.5D;
    }

    private static double markerCenterY(BlockPos pos) {
        return pos.getY() + 0.5D;
    }

    private static double markerCenterZ(BlockPos pos) {
        return pos.getZ() + 0.5D;
    }

    private record Marker(int color, long expireAt, double maxDistance, String label) {
    }

    private HbmOverheadMarkers() {
    }
}
