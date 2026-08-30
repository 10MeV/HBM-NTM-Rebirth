package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.blockentity.ForceFieldBlockEntity;
import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

public class ForceFieldRenderer implements BlockEntityRenderer<ForceFieldBlockEntity> {
    private static final int MAX_CACHED_SPHERE_LINE_COUNT = 128_000;
    private static final Map<SphereKey, SphereLines> SPHERE_LINES =
            new LinkedHashMap<>(16, 0.75F, true);
    private static int cachedSphereLineCount;

    public ForceFieldRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ForceFieldBlockEntity forceField, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(forceField, getViewDistance())) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(forceField, packedLight);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(forceField)) {
                ObjModelLibrary.MACHINE_RADAR_BODY_LEGACY.renderAll(ObjUtilityModels.FORCEFIELD_BASE_TEXTURE,
                        poseStack, buffer, modelLight, packedOverlay);
            }
        }

        poseStack.translate(0.0D, 0.5D, 0.0D);
        if (forceField.isOn() && forceField.getHealth() > 0 && forceField.getPower() > 0
                && forceField.getCooldown() == 0) {
            int segments = (int) (16 + forceField.getRadius() * 0.125F);
            renderSphere(poseStack, buffer, segments, segments * 2, forceField.getRadius(), forceField.getColor());
            double rotation = (System.currentTimeMillis() / 10.0D) % 360.0D;
            LegacyPoseRotations.rotateYDegrees(poseStack, (float) -rotation);
        }

        poseStack.translate(0.0D, 0.5D, 0.0D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(forceField)) {
            ObjUtilityModels.FORCEFIELD_TOP.renderAll(ObjUtilityModels.FORCEFIELD_TOP_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(ForceFieldBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(ForceFieldBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    private static void renderSphere(PoseStack poseStack, MultiBufferSource buffer, int latitudes, int segments,
            float radius, int color) {
        SphereLines lines =
                cachedSphereLines(new SphereKey(latitudes, segments, Float.floatToIntBits(radius)),
                        latitudes, segments, radius);
        VertexConsumer consumer = LegacyLineRenderer.consumer(buffer, LegacyLineRenderer.DEFAULT_LINE_WIDTH,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
        PoseStack.Pose pose = poseStack.last();
        double[] coordinates = lines.coordinates();
        for (int offset = 0; offset < coordinates.length; offset += 6) {
            LegacyLineRenderer.line(consumer, pose,
                    coordinates[offset], coordinates[offset + 1], coordinates[offset + 2],
                    coordinates[offset + 3], coordinates[offset + 4], coordinates[offset + 5],
                    color, 255);
        }
    }

    private static SphereLines cachedSphereLines(SphereKey key, int latitudes, int segments, float radius) {
        synchronized (SPHERE_LINES) {
            SphereLines cached = SPHERE_LINES.get(key);
            if (cached != null) {
                return cached;
            }
        }
        SphereLines built = buildSphereLines(latitudes, segments, radius);
        if (built.lineCount() > MAX_CACHED_SPHERE_LINE_COUNT) {
            return built;
        }
        synchronized (SPHERE_LINES) {
            SphereLines cached = SPHERE_LINES.get(key);
            if (cached != null) {
                return cached;
            }
            SPHERE_LINES.put(key, built);
            cachedSphereLineCount += built.lineCount();
            trimSphereCache();
            return built;
        }
    }

    private static void trimSphereCache() {
        while (cachedSphereLineCount > MAX_CACHED_SPHERE_LINE_COUNT && SPHERE_LINES.size() > 1) {
            Map.Entry<SphereKey, SphereLines> eldest =
                    SPHERE_LINES.entrySet().iterator().next();
            cachedSphereLineCount -= eldest.getValue().lineCount();
            SPHERE_LINES.remove(eldest.getKey());
        }
    }

    private static SphereLines buildSphereLines(int latitudes, int segments, float radius) {
        double segmentRot = Math.PI * 2.0D / segments;
        double latitudeRot = Math.PI / latitudes;
        int lineCount = segments * latitudes * 2;
        double[] coordinates = new double[lineCount * 6];
        int offset = 0;

        for (int k = 0; k < segments; k++) {
            double yaw = segmentRot * (k + 1);
            double yawCos = Math.cos(yaw);
            double yawSin = Math.sin(yaw);
            double prevX = 0.0D;
            double prevY = radius;
            double prevZ = 0.0D;
            for (int i = 0; i < latitudes; i++) {
                double pitch = latitudeRot * (i + 1);
                double localY = radius * Math.cos(pitch);
                double localZ = radius * Math.sin(pitch);
                double nextX = localZ * yawSin;
                double nextY = localY;
                double nextZ = localZ * yawCos;
                offset = addLine(coordinates, offset, prevX, prevY, prevZ, nextX, nextY, nextZ);
                prevX = nextX;
                prevY = nextY;
                prevZ = nextZ;
            }
        }

        for (int k = 0; k < latitudes; k++) {
            double ringAngle = latitudeRot * (k + 1);
            double ringX = -radius * Math.sin(ringAngle);
            double ringY = radius * Math.cos(ringAngle);
            double prevX = ringX;
            double prevY = ringY;
            double prevZ = 0.0D;
            for (int i = 0; i < segments; i++) {
                double yaw = segmentRot * (i + 1);
                double yawCos = Math.cos(yaw);
                double yawSin = Math.sin(yaw);
                double nextX = ringX * yawCos;
                double nextY = ringY;
                double nextZ = -ringX * yawSin;
                offset = addLine(coordinates, offset, prevX, prevY, prevZ, nextX, nextY, nextZ);
                prevX = nextX;
                prevY = nextY;
                prevZ = nextZ;
            }
        }
        return new SphereLines(lineCount, coordinates);
    }

    private static int addLine(double[] coordinates, int offset, double startX, double startY, double startZ,
            double endX, double endY, double endZ) {
        coordinates[offset++] = startX;
        coordinates[offset++] = startY;
        coordinates[offset++] = startZ;
        coordinates[offset++] = endX;
        coordinates[offset++] = endY;
        coordinates[offset++] = endZ;
        return offset;
    }

    private record SphereKey(int latitudes, int segments, int radiusBits) {
    }

    private record SphereLines(int lineCount, double[] coordinates) {
    }
}
