package com.hbm.ntm.client.render;

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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
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
        if (minecraft.level == null || player == null || minecraft.options.hideGui) {
            return;
        }
        pruneDistantMarkers(player);
        if (ACTIVE.isEmpty()) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

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
            renderBox(builder, poseStack, entry.getKey(), entry.getValue());
        }
        tesselator.end();
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        renderLabels(minecraft, camera, player, event.getPoseStack(), event.getPartialTick());
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
            if (marker.expireAt > 0L && now > marker.expireAt) {
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
            if (marker.maxDistance > 0.0D && player.distanceToSqr(Vec3.atCenterOf(pos)) > marker.maxDistance * marker.maxDistance) {
                iterator.remove();
            }
        }
    }

    private static void renderLabels(Minecraft minecraft, Camera camera, Player player, PoseStack poseStack, float partialTick) {
        for (Map.Entry<BlockPos, Marker> entry : ACTIVE.entrySet()) {
            BlockPos pos = entry.getKey();
            Marker marker = entry.getValue();
            if (marker.maxDistance > 0.0D && player.distanceToSqr(Vec3.atCenterOf(pos)) > marker.maxDistance * marker.maxDistance) {
                continue;
            }
            renderLabel(minecraft, camera, player, poseStack, partialTick, pos, marker);
        }
    }

    private static void renderLabel(Minecraft minecraft, Camera camera, Player player, PoseStack poseStack, float partialTick, BlockPos pos, Marker marker) {
        Vec3 cameraPos = camera.getPosition();
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        Vec3 toMarker = new Vec3(centerX - cameraPos.x, centerY - cameraPos.y, centerZ - cameraPos.z);
        double distance = toMarker.length();
        if (distance <= 1.0E-4D) {
            return;
        }

        Vec3 labelPos = toMarker.scale(Math.min(distance, 16.0D) / distance);
        Vec3 look = player.getLookAngle();
        Vec3 markerDirection = toMarker.normalize();
        String label = marker.label == null ? "" : marker.label;
        if (Math.abs(look.x - markerDirection.x) + Math.abs(look.y - markerDirection.y) + Math.abs(look.z - markerDirection.z) < 0.15D) {
            label += (!label.isEmpty() ? " " : "") + ((int) distance) + "m";
        }
        if (label.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(labelPos.x, labelPos.y, labelPos.z);
        poseStack.mulPose(new Quaternionf(camera.rotation()));
        float scale = -0.016666668F * 1.6F;
        poseStack.scale(scale, scale, -scale);

        Font font = minecraft.font;
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        font.drawInBatch(label, -font.width(label) * 0.5F, 0.0F, 0xFF000000 | (marker.color & 0xFFFFFF), false,
                poseStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
        buffer.endBatch();
        poseStack.popPose();
    }

    private static void renderBox(BufferBuilder builder, PoseStack poseStack, BlockPos pos, Marker marker) {
        PoseStack.Pose pose = poseStack.last();
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = minX + 1.0F;
        float maxY = minY + 1.0F;
        float maxZ = minZ + 1.0F;
        int red = marker.red();
        int green = marker.green();
        int blue = marker.blue();
        int alpha = 255;

        line(builder, pose, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        line(builder, pose, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        line(builder, pose, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        line(builder, pose, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);
        line(builder, pose, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(builder, pose, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(builder, pose, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        line(builder, pose, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
        line(builder, pose, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        line(builder, pose, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        line(builder, pose, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        line(builder, pose, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }

    private static void line(BufferBuilder builder, PoseStack.Pose pose,
            float x0, float y0, float z0, float x1, float y1, float z1,
            int red, int green, int blue, int alpha) {
        builder.vertex(pose.pose(), x0, y0, z0).color(red, green, blue, alpha).endVertex();
        builder.vertex(pose.pose(), x1, y1, z1).color(red, green, blue, alpha).endVertex();
    }

    private record Marker(int color, long expireAt, double maxDistance, String label) {
        private int red() {
            return Mth.clamp((color >> 16) & 255, 0, 255);
        }

        private int green() {
            return Mth.clamp((color >> 8) & 255, 0, 255);
        }

        private int blue() {
            return Mth.clamp(color & 255, 0, 255);
        }
    }

    private HbmOverheadMarkers() {
    }
}
