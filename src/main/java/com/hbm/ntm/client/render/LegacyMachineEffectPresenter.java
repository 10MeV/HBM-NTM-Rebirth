package com.hbm.ntm.client.render;

import com.hbm.ntm.blockentity.TeslaBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyEmitterBeamRenderer;
import com.hbm.ntm.client.obj.LegacyLineRenderer;
import com.hbm.ntm.client.obj.LegacySparkRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyDangerDiamondRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.renderer.LegacyTileRenderPlans;
import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling.MachineRendererSubmissionScope;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class LegacyMachineEffectPresenter {
    private static final int BEAM_DOUBLE_STRIDE = 6;
    private static final int BEAM_INT_STRIDE = 5;
    private static final int BEAM_FLOAT_STRIDE = 2;
    private static final int SPARK_DOUBLE_STRIDE = 3;
    private static final int SPARK_INT_STRIDE = 5;
    private static final int SPARK_FLOAT_STRIDE = 2;
    private static final int UNTEXTURED_QUAD_DOUBLE_STRIDE = 12;
    private static final int UNTEXTURED_QUAD_INT_STRIDE = 8;
    private static final int SOLAR_BOILER_BEAM_MATRIX_STRIDE = 16;
    private static final int TEXTURED_QUAD_GROUP_DOUBLE_STRIDE = 20;
    private static final int TEXTURED_QUAD_GROUP_FLOAT_STRIDE = 3;
    private static final int TEXTURED_QUAD_GROUP_INT_STRIDE = 7;
    private static final int TEXTURED_OBJ_PART_INT_STRIDE = 7;
    private static final int UNTEXTURED_OBJ_PART_INT_STRIDE = 4;
    private static final int UNTEXTURED_OBJ_PART_CLIP_DOUBLE_STRIDE = 4;
    private static final int ATLAS_SPRITE_QUAD_DOUBLE_STRIDE = 20;
    private static final int ATLAS_SPRITE_QUAD_FLOAT_STRIDE = 3;
    private static final int ATLAS_SPRITE_QUAD_INT_STRIDE = 4;
    private static final Map<PresentStage, List<QueuedTask>> QUEUES = new EnumMap<>(PresentStage.class);
    private static final Map<PresentStage, List<QueuedTask>> PRESENTING = new EnumMap<>(PresentStage.class);
    private static final List<QueuedTask> TASK_POOL = new ArrayList<>();
    private static final PresentContext PRESENT_CONTEXT = new PresentContext();
    private static TextureTarget lateWorldEffectTarget;
    private static boolean lateWorldDepthCaptured;
    private static long frameGeneration;
    private static long presentCalls;
    private static long afterBlockEntitiesPresents;
    private static long afterLevelPresents;
    private static long queuedTasks;
    private static long presentedTasks;
    private static long failedTasks;
    private static long clears;
    private static long currentFramePresentCalls;
    private static long currentFramePresentedTasks;
    private static long lastFramePresentCalls;
    private static long lastFramePresentedTasks;
    private static volatile PresentStage lastPresentStage = PresentStage.MANUAL;

    private LegacyMachineEffectPresenter() {
    }

    static {
        for (PresentStage stage : PresentStage.values()) {
            QUEUES.put(stage, new ArrayList<>());
            PRESENTING.put(stage, new ArrayList<>());
        }
    }

    public static void beginFrame() {
        lastFramePresentCalls = currentFramePresentCalls;
        lastFramePresentedTasks = currentFramePresentedTasks;
        currentFramePresentCalls = 0L;
        currentFramePresentedTasks = 0L;
        lateWorldDepthCaptured = false;
        frameGeneration++;
    }

    /**
     * Saves the terrain/block-entity depth after particles but before clouds write into the main depth buffer.
     * AFTER_LEVEL effects can then remain occluded by world geometry without being punched out by clouds.
     */
    public static void captureLateWorldDepth() {
        if (QUEUES.get(PresentStage.AFTER_LEVEL).isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        ensureLateWorldEffectTarget(mainTarget);
        blit(mainTarget, lateWorldEffectTarget, GlConst.GL_DEPTH_BUFFER_BIT);
        RenderTarget restoreTarget = Minecraft.useShaderTransparency()
                ? minecraft.levelRenderer.getParticlesTarget()
                : mainTarget;
        (restoreTarget == null ? mainTarget : restoreTarget).bindWrite(false);
        lateWorldDepthCaptured = true;
    }

    public static void enqueue(PresentStage stage, Runnable task) {
        if (task == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QUEUES.get(resolvedStage).add(obtainTask().init(task,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope()));
        queuedTasks++;
    }

    public static void enqueue(PresentStage stage, PoseStack poseStack, Consumer<PoseStack> task) {
        if (poseStack == null || task == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QUEUES.get(resolvedStage).add(obtainTask().init(poseStack, task,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope()));
        queuedTasks++;
    }

    public static void enqueueTexturedQuad(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            ResourceLocation texture, LegacyTexturedRenderMode renderMode, int packedLight, int packedOverlay,
            float normalX, float normalY, float normalZ,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3,
            int color, int alpha) {
        if (poseStack == null || buffer == null || texture == null || renderMode == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QUEUES.get(resolvedStage).add(obtainTask().initTexturedQuad(poseStack, buffer, texture, renderMode,
                packedLight, packedOverlay, normalX, normalY, normalZ,
                x0, y0, z0, u0, v0, alpha,
                x1, y1, z1, u1, v1, alpha,
                x2, y2, z2, u2, v2, alpha,
                x3, y3, z3, u3, v3, alpha,
                color, HbmRenderFrameCulling.currentMachineRendererSubmissionScope()));
        queuedTasks++;
    }

    public static void enqueueDangerDiamond(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            int poison, int flammability, int reactivity, LegacyDangerDiamondRenderer.Symbol symbol) {
        if (poseStack == null || buffer == null || renderMode == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QUEUES.get(resolvedStage).add(obtainTask().initDangerDiamond(poseStack, buffer, packedLight, packedOverlay,
                renderMode, poison, flammability, reactivity, symbol,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope()));
        queuedTasks++;
    }

    public static void enqueueSolidBeamGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            boolean depthWrite, Consumer<SolidBeamGroup> beams) {
        if (poseStack == null || buffer == null || beams == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, depthWrite,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            beams.accept(task);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueSolidBeam(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            boolean depthWrite, double x, double y, double z,
            LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
            int start, int segments, float size, int layers, float thickness) {
        if (poseStack == null || buffer == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, depthWrite,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            task.add(x, y, z, wave, outerColor, innerColor, start, segments, size, layers, thickness);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueTeslaTargetBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            List<TeslaBlockEntity.TeslaTarget> targets, double sourceX, double sourceY, double sourceZ,
            int start, int color, float size, int layers, float thickness) {
        if (poseStack == null || buffer == null || targets == null || targets.isEmpty()) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, false,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            for (TeslaBlockEntity.TeslaTarget target : targets) {
                double dx = target.x() - sourceX;
                double dy = target.y() - sourceY;
                double dz = target.z() - sourceZ;
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                task.add(-dx, dy, -dz, LegacyBeamRenderer.WaveType.RANDOM, color, color,
                        start, (int) (length * 5.0D), size, layers, thickness);
            }
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueSpiralSolidBeamFan(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            boolean depthWrite, double x, double y, double z, int color, double startBase, double phaseStep,
            int count, int segments, float size, int layers, float thickness) {
        if (poseStack == null || buffer == null || count <= 0) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, depthWrite,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            for (int i = 0; i < count; i++) {
                int start = (int) ((startBase + i * phaseStep) % 360.0D);
                task.add(x, y, z, LegacyBeamRenderer.WaveType.SPIRAL, color, color,
                        start, segments, size, layers, thickness);
            }
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueOrbusBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            float beamScale, int randomStartA, int randomStartB) {
        if (poseStack == null || buffer == null || beamScale <= 0.0F) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, false,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            task.add(0.0D, 3.0D, 0.0D,
                    LegacyBeamRenderer.WaveType.SPIRAL, 0x101020, 0x101020, 0, 1, 0.0F, 6,
                    beamScale * 0.5F);
            task.add(0.0D, 3.0D, 0.0D,
                    LegacyBeamRenderer.WaveType.RANDOM, 0x202060, 0x202060, randomStartA, 6, beamScale, 2,
                    0.0625F * beamScale);
            task.add(0.0D, 3.0D, 0.0D,
                    LegacyBeamRenderer.WaveType.RANDOM, 0x202060, 0x202060, randomStartB, 6, beamScale, 2,
                    0.0625F * beamScale);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueCreativeBatteryBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int beamMask, int start) {
        if (poseStack == null || buffer == null || beamMask == 0) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, false,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            int bit = 1;
            for (int i = -1; i <= 1; i += 2) {
                for (int j = -1; j <= 1; j += 2) {
                    if ((beamMask & bit) != 0) {
                        double x = LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_XZ * i;
                        double z = LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_XZ * j;
                        addDualRandomSolidBeam(task, x, LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_Y, z,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_OUTER_COLOR,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_INNER_COLOR,
                                start,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_LONG_BEAM_SEGMENTS,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_LONG_BEAM_SIZE,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_SHORT_BEAM_SEGMENTS,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_SHORT_BEAM_SIZE,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_LAYERS,
                                LegacyTileRenderPlans.CREATIVE_BATTERY_BEAM_THICKNESS);
                    }
                    bit <<= 1;
                }
            }
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueDualRandomSolidBeam(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            boolean depthWrite, double x, double y, double z, int outerColor, int innerColor, int start,
            int longSegments, float longSize, int shortSegments, float shortSize, int layers, float thickness) {
        if (poseStack == null || buffer == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, depthWrite,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            addDualRandomSolidBeam(task, x, y, z, outerColor, innerColor, start,
                    longSegments, longSize, shortSegments, shortSize, layers, thickness);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueDfcEmitterBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int range, int randomStart) {
        if (poseStack == null || buffer == null || range <= 0) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, true,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            task.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_EMITTER_DEPTH_COLOR,
                    LegacyTileRenderPlans.DFC_EMITTER_DEPTH_COLOR, 0, 1, 0.0F, 2, 0.0625F);
            task.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.RANDOM, LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR,
                    LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR, randomStart, range * 2, 0.125F, 4, 0.0625F);
            task.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.RANDOM, LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR,
                    LegacyTileRenderPlans.DFC_EMITTER_RANDOM_COLOR, randomStart + 1, range * 2, 0.125F, 4,
                    0.0625F);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueDfcInjectorBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int range, boolean renderTank0, int tank0Color, boolean renderTank1, int tank1Color, int randomStart) {
        if (poseStack == null || buffer == null || range <= 0 || (!renderTank0 && !renderTank1)) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initLineBeamGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            if (renderTank0) {
                task.add(0.0D, 0.0D, range,
                        LegacyBeamRenderer.WaveType.RANDOM, tank0Color,
                        LegacyTileRenderPlans.DFC_INJECTOR_INNER_COLOR, randomStart, range, 0.0625F);
            }
            if (renderTank1) {
                task.add(0.0D, 0.0D, range,
                        LegacyBeamRenderer.WaveType.RANDOM, tank1Color,
                        LegacyTileRenderPlans.DFC_INJECTOR_INNER_COLOR, randomStart + 1, range, 0.0625F);
            }
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueDfcStabilizerBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int range, int fastStart, int midStart, int slowStart) {
        if (poseStack == null || buffer == null || range <= 0) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initLineBeamGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            int segments = range * 3;
            task.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_STABILIZER_OUTER_COLOR,
                    LegacyTileRenderPlans.DFC_STABILIZER_INNER_COLOR, fastStart, segments, 0.125F);
            task.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_STABILIZER_OUTER_COLOR,
                    LegacyTileRenderPlans.DFC_STABILIZER_INNER_COLOR, midStart, segments, 0.125F);
            task.add(0.0D, 0.0D, range,
                    LegacyBeamRenderer.WaveType.SPIRAL, LegacyTileRenderPlans.DFC_STABILIZER_OUTER_COLOR,
                    LegacyTileRenderPlans.DFC_STABILIZER_INNER_COLOR, slowStart, segments, 0.125F);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueFelBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int length, int color, int randomStart) {
        if (poseStack == null || buffer == null || length <= 0) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, true,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            double z = -length - 1.0D;
            task.add(0.0D, 0.0D, z,
                    LegacyBeamRenderer.WaveType.SPIRAL, color, color, 0, 1, 0.0F, 2, 0.0625F);
            task.add(0.0D, 0.0D, z,
                    LegacyBeamRenderer.WaveType.RANDOM, color, color, randomStart, length / 2 + 1, 0.0625F, 2,
                    0.0625F);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueEmitterBeamGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            int beam, float girth, int effect, int rawColor, long worldTime, float partialTick) {
        if (poseStack == null || buffer == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolidBeamGroup(poseStack, buffer, false,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            LegacyEmitterBeamRenderer.emitBeamPlans(beam, girth, effect, rawColor, worldTime, partialTick, task);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueLineBeam(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            double x, double y, double z, LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
            int start, int segments, float size) {
        if (poseStack == null || buffer == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initLineBeamGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            task.add(x, y, z, wave, outerColor, innerColor, start, segments, size);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueExposureChamberBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            boolean randomTop, boolean randomRight, boolean randomLeft, int randomColor, int loopStart,
            long currentMillis) {
        if (poseStack == null || buffer == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initLineBeamGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            if (randomTop) {
                task.add(0.0D, 3.675D, -7.5D, 0.0D, 0.0D, 5.0D,
                        LegacyBeamRenderer.WaveType.RANDOM, randomColor, 0xFFFFFF, loopStart, 15, 0.125F);
            }
            if (randomRight) {
                task.add(1.1875D, 2.5D, -7.5D, 0.0D, 0.0D, 5.0D,
                        LegacyBeamRenderer.WaveType.RANDOM, randomColor, 0xFFFFFF, loopStart, 15, 0.125F);
            }
            if (randomLeft) {
                task.add(-1.1875D, 2.5D, -7.5D, 0.0D, 0.0D, 5.0D,
                        LegacyBeamRenderer.WaveType.RANDOM, randomColor, 0xFFFFFF, loopStart, 15, 0.125F);
            }
            task.add(0.0D, 1.75D, 0.0D, 0.0D, 1.5D, 0.0D,
                    LegacyBeamRenderer.WaveType.RANDOM, 0x80D0FF, 0xFFFFFF, loopStart, 10, 0.125F);
            task.add(0.0D, 1.75D, 0.0D, 0.0D, 1.5D, 0.0D,
                    LegacyBeamRenderer.WaveType.RANDOM, 0x8080FF, 0xFFFFFF, (int) (currentMillis + 5L) / 50,
                    10, 0.125F);
            task.add(0.0D, 2.5D, 0.0D, 0.0D, 0.0D, -1.0D,
                    LegacyBeamRenderer.WaveType.SPIRAL, 0xFFFF80, 0xFFFFFF, (int) (currentMillis % 360L),
                    15, 0.125F);
            task.add(0.0D, 2.5D, 0.0D, 0.0D, 0.0D, -1.0D,
                    LegacyBeamRenderer.WaveType.SPIRAL, 0xFF8080, 0xFFFFFF, (int) (currentMillis % 360L) + 180,
                    15, 0.125F);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueLineBeamGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            Consumer<LineBeamGroup> beams) {
        if (poseStack == null || buffer == null || beams == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initLineBeamGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            beams.accept(task);
            if (task.hasBeamSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueSparkGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, Consumer<SparkGroup> sparks) {
        if (poseStack == null || buffer == null || renderMode == null || sparks == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSparkGroup(poseStack, buffer, renderMode,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            sparks.accept(task);
            if (task.hasSparkSpecs()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueUntexturedQuadGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, Consumer<UntexturedQuadGroup> quads) {
        enqueueUntexturedQuadGroup(stage, poseStack, buffer, renderMode, 255, quads);
    }

    public static void enqueueUntexturedQuadGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int renderAlpha, Consumer<UntexturedQuadGroup> quads) {
        if (poseStack == null || buffer == null || renderMode == null || quads == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initUntexturedQuadGroup(poseStack, buffer, renderMode, renderAlpha,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            quads.accept(task);
            if (task.hasUntexturedQuads()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueSolarBoilerBeams(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int renderAlpha, double halfWidth, double startY,
            int nearAlpha, int farAlpha, int[] offsets) {
        if (poseStack == null || buffer == null || renderMode == null || offsets == null || offsets.length < 3) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolarBoilerBeamGroup(poseStack, buffer, renderMode, renderAlpha,
                halfWidth, startY, nearAlpha, farAlpha,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            for (int i = 0; i + 2 < offsets.length; i += 3) {
                task.add(offsets[i], offsets[i + 1], offsets[i + 2]);
            }
            if (task.hasSolarBoilerBeams()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueSolarBoilerBeamGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, int renderAlpha, double halfWidth, double startY,
            int nearAlpha, int farAlpha, Consumer<SolarBoilerBeamGroup> beams) {
        if (poseStack == null || buffer == null || renderMode == null || beams == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initSolarBoilerBeamGroup(poseStack, buffer, renderMode, renderAlpha,
                halfWidth, startY, nearAlpha, farAlpha,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            beams.accept(task);
            if (task.hasSolarBoilerBeams()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    private static void addDualRandomSolidBeam(SolidBeamGroup beams, double x, double y, double z,
            int outerColor, int innerColor, int start, int longSegments, float longSize,
            int shortSegments, float shortSize, int layers, float thickness) {
        beams.add(x, y, z, LegacyBeamRenderer.WaveType.RANDOM,
                outerColor, innerColor, start, longSegments, longSize, layers, thickness);
        beams.add(x, y, z, LegacyBeamRenderer.WaveType.RANDOM,
                outerColor, innerColor, start, shortSegments, shortSize, layers, thickness);
    }

    public static void enqueueTexturedQuadGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            ResourceLocation texture, LegacyTexturedRenderMode renderMode, Consumer<TexturedQuadGroup> quads) {
        if (poseStack == null || buffer == null || texture == null || renderMode == null || quads == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initTexturedQuadGroup(poseStack, buffer, texture, renderMode,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            quads.accept(task);
            if (task.hasTexturedQuadGroupQuads()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueTexturedObjPartGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            Consumer<TexturedObjPartGroup> parts) {
        if (poseStack == null || buffer == null || parts == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initTexturedObjPartGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            parts.accept(task);
            if (task.hasTexturedObjParts()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueUntexturedObjPartGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            Consumer<UntexturedObjPartGroup> parts) {
        if (poseStack == null || buffer == null || parts == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initUntexturedObjPartGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            parts.accept(task);
            if (task.hasUntexturedObjParts()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueUntexturedObjPart(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
            int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
        if (poseStack == null || buffer == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initUntexturedObjPartGroup(poseStack, buffer,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            task.add(model, selection, red, green, blue, alpha, renderMode);
            if (task.hasUntexturedObjParts()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void enqueueAtlasSpriteQuadGroup(PresentStage stage, PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode, Consumer<LegacyTexturedQuadRenderer.SpritePixelQuadSink> quads) {
        if (poseStack == null || buffer == null || renderMode == null || quads == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QueuedTask task = obtainTask().initAtlasSpriteQuadGroup(poseStack, buffer, renderMode,
                HbmRenderFrameCulling.currentMachineRendererSubmissionScope());
        boolean queued = false;
        try {
            quads.accept(task);
            if (task.hasAtlasSpriteQuads()) {
                QUEUES.get(resolvedStage).add(task);
                queuedTasks++;
                queued = true;
            }
        } finally {
            if (!queued) {
                releaseTask(task);
            }
        }
    }

    public static void present(PresentStage stage) {
        PresentStage resolvedStage = stage == null ? PresentStage.MANUAL : stage;
        presentCalls++;
        currentFramePresentCalls++;
        lastPresentStage = resolvedStage;
        switch (resolvedStage) {
            case AFTER_BLOCK_ENTITIES -> afterBlockEntitiesPresents++;
            case AFTER_LEVEL -> afterLevelPresents++;
            case MANUAL -> {
            }
        }
        List<QueuedTask> queue = QUEUES.get(resolvedStage);
        if (queue.isEmpty()) {
            return;
        }
        List<QueuedTask> tasks = PRESENTING.get(resolvedStage);
        tasks.addAll(queue);
        queue.clear();
        PresentContext context = PRESENT_CONTEXT;
        context.clear();
        boolean lateWorldTargetBound = false;
        try {
            lateWorldTargetBound = resolvedStage == PresentStage.AFTER_LEVEL && beginLateWorldEffects();
            for (QueuedTask task : tasks) {
                try {
                    task.run(context);
                    presentedTasks++;
                    currentFramePresentedTasks++;
                } catch (RuntimeException exception) {
                    failedTasks++;
                    throw exception;
                }
            }
            if (resolvedStage == PresentStage.AFTER_LEVEL) {
                context.flushUntexturedBatches();
            }
        } finally {
            try {
                if (lateWorldTargetBound) {
                    finishLateWorldEffects();
                }
            } finally {
                context.clear();
                for (QueuedTask task : tasks) {
                    releaseTask(task);
                }
                tasks.clear();
            }
        }
    }

    public static void clear() {
        for (List<QueuedTask> queue : QUEUES.values()) {
            releaseTasks(queue);
            queue.clear();
        }
        for (List<QueuedTask> queue : PRESENTING.values()) {
            releaseTasks(queue);
            queue.clear();
        }
        if (lateWorldEffectTarget != null) {
            lateWorldEffectTarget.destroyBuffers();
            lateWorldEffectTarget = null;
        }
        lateWorldDepthCaptured = false;
        clears++;
    }

    private static boolean beginLateWorldEffects() {
        if (!lateWorldDepthCaptured || lateWorldEffectTarget == null) {
            return false;
        }
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        if (mainTarget.width != lateWorldEffectTarget.width || mainTarget.height != lateWorldEffectTarget.height) {
            lateWorldDepthCaptured = false;
            return false;
        }
        blit(mainTarget, lateWorldEffectTarget, GlConst.GL_COLOR_BUFFER_BIT);
        lateWorldEffectTarget.bindWrite(false);
        RenderSystem.viewport(0, 0, lateWorldEffectTarget.viewWidth, lateWorldEffectTarget.viewHeight);
        return true;
    }

    private static void finishLateWorldEffects() {
        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        blit(lateWorldEffectTarget, mainTarget, GlConst.GL_COLOR_BUFFER_BIT);
        mainTarget.bindWrite(false);
        RenderSystem.viewport(0, 0, mainTarget.viewWidth, mainTarget.viewHeight);
        lateWorldDepthCaptured = false;
    }

    private static void ensureLateWorldEffectTarget(RenderTarget mainTarget) {
        if (lateWorldEffectTarget == null) {
            lateWorldEffectTarget = new TextureTarget(mainTarget.width, mainTarget.height, true, Minecraft.ON_OSX);
        } else if (lateWorldEffectTarget.width != mainTarget.width
                || lateWorldEffectTarget.height != mainTarget.height) {
            lateWorldEffectTarget.resize(mainTarget.width, mainTarget.height, Minecraft.ON_OSX);
        }
    }

    private static void blit(RenderTarget source, RenderTarget destination, int mask) {
        GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, source.frameBufferId);
        GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, destination.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, source.width, source.height,
                0, 0, destination.width, destination.height, mask, GlConst.GL_NEAREST);
    }

    private static QueuedTask obtainTask() {
        int lastIndex = TASK_POOL.size() - 1;
        if (lastIndex < 0) {
            return new QueuedTask();
        }
        return TASK_POOL.remove(lastIndex);
    }

    private static void releaseTasks(List<QueuedTask> tasks) {
        for (QueuedTask task : tasks) {
            releaseTask(task);
        }
    }

    private static void releaseTask(QueuedTask task) {
        if (task == null) {
            return;
        }
        task.release();
        TASK_POOL.add(task);
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                frameGeneration,
                presentCalls,
                afterBlockEntitiesPresents,
                afterLevelPresents,
                queuedTasks,
                presentedTasks,
                failedTasks,
                clears,
                QUEUES.values().stream().mapToInt(List::size).sum(),
                currentFramePresentCalls,
                currentFramePresentedTasks,
                lastFramePresentCalls,
                lastFramePresentedTasks,
                lastPresentStage);
    }

    public enum PresentStage {
        AFTER_BLOCK_ENTITIES,
        AFTER_LEVEL,
        MANUAL
    }

    public interface SolidBeamGroup extends LegacyEmitterBeamRenderer.EmitterBeamPlanSink {
    }

    public interface LineBeamGroup {
        void add(double x, double y, double z,
                LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
                int start, int segments, float size);

        void add(double originX, double originY, double originZ,
                double x, double y, double z,
                LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
                int start, int segments, float size);
    }

    public interface SparkGroup {
        void add(int seed, double x, double y, double z, float yawDegrees,
                float length, int minSegments, int maxRandomSegments,
                int outerColor, int innerColor);

        void addRadians(int seed, double x, double y, double z, float yawRadians,
                float length, int minSegments, int maxRandomSegments,
                int outerColor, int innerColor);
    }

    public interface UntexturedQuadGroup {
        void add(double x0, double y0, double z0,
                double x1, double y1, double z1,
                double x2, double y2, double z2,
                double x3, double y3, double z3,
                int color, int alpha0, int alpha1, int alpha2, int alpha3);

        void add(double x0, double y0, double z0, int color0, int alpha0,
                double x1, double y1, double z1, int color1, int alpha1,
                double x2, double y2, double z2, int color2, int alpha2,
                double x3, double y3, double z3, int color3, int alpha3);
    }

    public interface SolarBoilerBeamGroup {
        void add(int dx, int dy, int dz);
    }

    public interface TexturedQuadGroup {
        void add(int packedLight, int packedOverlay,
                float normalX, float normalY, float normalZ,
                double x0, double y0, double z0, double u0, double v0, int alpha0,
                double x1, double y1, double z1, double u1, double v1, int alpha1,
                double x2, double y2, double z2, double u2, double v2, int alpha2,
                double x3, double y3, double z3, double u3, double v3, int alpha3,
                int color);
    }

    public interface TexturedObjPartGroup {
        void add(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
                ResourceLocation texture, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow,
                LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform);
    }

    public interface UntexturedObjPartGroup {
        void add(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode);

        void addClipped(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
                double clipX, double clipY, double clipZ, double clipD);
    }

    private enum BeamQueueKind {
        SOLID,
        LINE
    }

    public record Snapshot(
            long frameGeneration,
            long presentCalls,
            long afterBlockEntitiesPresents,
            long afterLevelPresents,
            long queuedTasks,
            long presentedTasks,
            long failedTasks,
            long clears,
            int queuedTasksNow,
            long currentFramePresentCalls,
            long currentFramePresentedTasks,
            long lastFramePresentCalls,
            long lastFramePresentedTasks,
            PresentStage lastPresentStage) {
    }

    private static final class PresentContext {
        private final List<TexturedQuadConsumerEntry> texturedQuadConsumers = new ArrayList<>();
        private final List<TexturedQuadConsumerEntry> texturedQuadConsumerPool = new ArrayList<>();
        private final List<UntexturedConsumerEntry> untexturedConsumers = new ArrayList<>();
        private final List<UntexturedConsumerEntry> untexturedConsumerPool = new ArrayList<>();
        private final List<LineConsumerEntry> lineConsumers = new ArrayList<>();
        private final List<LineConsumerEntry> lineConsumerPool = new ArrayList<>();
        private final List<AtlasSpriteConsumerEntry> atlasSpriteConsumers = new ArrayList<>();
        private final List<AtlasSpriteConsumerEntry> atlasSpriteConsumerPool = new ArrayList<>();

        private VertexConsumer texturedQuadConsumer(MultiBufferSource buffer, ResourceLocation texture,
                LegacyTexturedRenderMode renderMode) {
            for (TexturedQuadConsumerEntry entry : texturedQuadConsumers) {
                if (entry.matches(buffer, texture, renderMode)) {
                    return entry.consumer;
                }
            }
            TexturedQuadConsumerEntry entry = obtainTexturedQuadConsumer().init(buffer, texture, renderMode,
                    LegacyTexturedQuadRenderer.vertexAlphaConsumer(texture, buffer, renderMode));
            texturedQuadConsumers.add(entry);
            return entry.consumer;
        }

        private VertexConsumer untexturedConsumer(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                int alpha) {
            for (UntexturedConsumerEntry entry : untexturedConsumers) {
                if (entry.matches(buffer, renderMode, alpha)) {
                    return entry.consumer;
                }
            }
            UntexturedConsumerEntry entry = obtainUntexturedConsumer().init(buffer, renderMode, alpha,
                    LegacyUntexturedQuadRenderer.consumer(buffer, renderMode, alpha));
            untexturedConsumers.add(entry);
            return entry.consumer;
        }

        private VertexConsumer lineConsumer(MultiBufferSource buffer, float lineWidth,
                LegacyTexturedRenderMode renderMode, int alpha) {
            for (LineConsumerEntry entry : lineConsumers) {
                if (entry.matches(buffer, lineWidth, renderMode, alpha)) {
                    return entry.consumer;
                }
            }
            LineConsumerEntry entry = obtainLineConsumer().init(buffer, lineWidth, renderMode, alpha,
                    LegacyLineRenderer.consumer(buffer, lineWidth, renderMode, alpha));
            lineConsumers.add(entry);
            return entry.consumer;
        }

        private VertexConsumer atlasSpriteConsumer(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                int alpha) {
            for (AtlasSpriteConsumerEntry entry : atlasSpriteConsumers) {
                if (entry.matches(buffer, renderMode, alpha)) {
                    return entry.consumer;
                }
            }
            AtlasSpriteConsumerEntry entry = obtainAtlasSpriteConsumer().init(buffer, renderMode, alpha,
                    LegacyTexturedQuadRenderer.spriteAtlasConsumer(buffer, renderMode, alpha));
            atlasSpriteConsumers.add(entry);
            return entry.consumer;
        }

        private TexturedQuadConsumerEntry obtainTexturedQuadConsumer() {
            int lastIndex = texturedQuadConsumerPool.size() - 1;
            if (lastIndex < 0) {
                return new TexturedQuadConsumerEntry();
            }
            return texturedQuadConsumerPool.remove(lastIndex);
        }

        private UntexturedConsumerEntry obtainUntexturedConsumer() {
            int lastIndex = untexturedConsumerPool.size() - 1;
            if (lastIndex < 0) {
                return new UntexturedConsumerEntry();
            }
            return untexturedConsumerPool.remove(lastIndex);
        }

        private LineConsumerEntry obtainLineConsumer() {
            int lastIndex = lineConsumerPool.size() - 1;
            if (lastIndex < 0) {
                return new LineConsumerEntry();
            }
            return lineConsumerPool.remove(lastIndex);
        }

        private AtlasSpriteConsumerEntry obtainAtlasSpriteConsumer() {
            int lastIndex = atlasSpriteConsumerPool.size() - 1;
            if (lastIndex < 0) {
                return new AtlasSpriteConsumerEntry();
            }
            return atlasSpriteConsumerPool.remove(lastIndex);
        }

        private void flushUntexturedBatches() {
            float[] shaderColor = RenderSystem.getShaderColor();
            float red = shaderColor[0];
            float green = shaderColor[1];
            float blue = shaderColor[2];
            float alpha = shaderColor[3];
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            try {
                for (UntexturedConsumerEntry entry : untexturedConsumers) {
                    if (entry.buffer instanceof MultiBufferSource.BufferSource bufferSource) {
                        // Re-synchronize Blaze3D's cached blend flag with the actual GL state before the
                        // RenderType setup enables additive/alpha blending for this late batch.
                        RenderSystem.disableBlend();
                        bufferSource.endBatch(LegacyUntexturedQuadRenderer.type(entry.renderMode, entry.alpha));
                    }
                }
            } finally {
                RenderSystem.setShaderColor(red, green, blue, alpha);
            }
        }

        private void clear() {
            for (TexturedQuadConsumerEntry entry : texturedQuadConsumers) {
                entry.release();
                texturedQuadConsumerPool.add(entry);
            }
            texturedQuadConsumers.clear();
            for (UntexturedConsumerEntry entry : untexturedConsumers) {
                entry.release();
                untexturedConsumerPool.add(entry);
            }
            untexturedConsumers.clear();
            for (LineConsumerEntry entry : lineConsumers) {
                entry.release();
                lineConsumerPool.add(entry);
            }
            lineConsumers.clear();
            for (AtlasSpriteConsumerEntry entry : atlasSpriteConsumers) {
                entry.release();
                atlasSpriteConsumerPool.add(entry);
            }
            atlasSpriteConsumers.clear();
        }
    }

    private static final class TexturedQuadConsumerEntry {
        private MultiBufferSource buffer;
        private ResourceLocation texture;
        private LegacyTexturedRenderMode renderMode;
        private VertexConsumer consumer;

        private TexturedQuadConsumerEntry init(MultiBufferSource buffer, ResourceLocation texture,
                LegacyTexturedRenderMode renderMode, VertexConsumer consumer) {
            this.buffer = buffer;
            this.texture = texture;
            this.renderMode = renderMode;
            this.consumer = consumer;
            return this;
        }

        private boolean matches(MultiBufferSource buffer, ResourceLocation texture, LegacyTexturedRenderMode renderMode) {
            return this.buffer == buffer && this.renderMode == renderMode && this.texture.equals(texture);
        }

        private void release() {
            buffer = null;
            texture = null;
            renderMode = null;
            consumer = null;
        }
    }

    private static final class UntexturedConsumerEntry {
        private MultiBufferSource buffer;
        private LegacyTexturedRenderMode renderMode;
        private int alpha;
        private VertexConsumer consumer;

        private UntexturedConsumerEntry init(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                int alpha, VertexConsumer consumer) {
            this.buffer = buffer;
            this.renderMode = renderMode;
            this.alpha = alpha;
            this.consumer = consumer;
            return this;
        }

        private boolean matches(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, int alpha) {
            return this.buffer == buffer && this.renderMode == renderMode && this.alpha == alpha;
        }

        private void release() {
            buffer = null;
            renderMode = null;
            alpha = 0;
            consumer = null;
        }
    }

    private static final class LineConsumerEntry {
        private MultiBufferSource buffer;
        private float lineWidth;
        private LegacyTexturedRenderMode renderMode;
        private int alpha;
        private VertexConsumer consumer;

        private LineConsumerEntry init(MultiBufferSource buffer, float lineWidth,
                LegacyTexturedRenderMode renderMode, int alpha, VertexConsumer consumer) {
            this.buffer = buffer;
            this.lineWidth = lineWidth;
            this.renderMode = renderMode;
            this.alpha = alpha;
            this.consumer = consumer;
            return this;
        }

        private boolean matches(MultiBufferSource buffer, float lineWidth,
                LegacyTexturedRenderMode renderMode, int alpha) {
            return this.buffer == buffer && this.lineWidth == lineWidth
                    && this.renderMode == renderMode && this.alpha == alpha;
        }

        private void release() {
            buffer = null;
            lineWidth = 0.0F;
            renderMode = null;
            alpha = 0;
            consumer = null;
        }
    }

    private static final class AtlasSpriteConsumerEntry {
        private MultiBufferSource buffer;
        private LegacyTexturedRenderMode renderMode;
        private int alpha;
        private VertexConsumer consumer;

        private AtlasSpriteConsumerEntry init(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode,
                int alpha, VertexConsumer consumer) {
            this.buffer = buffer;
            this.renderMode = renderMode;
            this.alpha = alpha;
            this.consumer = consumer;
            return this;
        }

        private boolean matches(MultiBufferSource buffer, LegacyTexturedRenderMode renderMode, int alpha) {
            return this.buffer == buffer && this.renderMode == renderMode && this.alpha == alpha;
        }

        private void release() {
            buffer = null;
            renderMode = null;
            alpha = 0;
            consumer = null;
        }
    }

    private static final class QueuedTask implements Runnable, SolidBeamGroup, LineBeamGroup, UntexturedQuadGroup,
            SolarBoilerBeamGroup, SparkGroup, TexturedQuadGroup, TexturedObjPartGroup, UntexturedObjPartGroup,
            LegacyTexturedQuadRenderer.SpritePixelQuadSink {
        private final Matrix4f pose = new Matrix4f();
        private final Matrix3f normal = new Matrix3f();
        private final Matrix4f sparkPose = new Matrix4f();
        private final Matrix3f sparkNormal = new Matrix3f();
        private final Matrix4f solarBeamPose = new Matrix4f();
        private Runnable runnableTask;
        private Consumer<PoseStack> poseTask;
        private PoseStack replayPose = new PoseStack();
        private MachineRendererSubmissionScope scope;
        private PresentContext context;
        private boolean scopeApplied;
        private MultiBufferSource texturedQuadBuffer;
        private ResourceLocation texturedQuadTexture;
        private LegacyTexturedRenderMode texturedQuadRenderMode;
        private int texturedQuadPackedLight;
        private int texturedQuadPackedOverlay;
        private float texturedQuadNormalX;
        private float texturedQuadNormalY;
        private float texturedQuadNormalZ;
        private double texturedQuadX0;
        private double texturedQuadY0;
        private double texturedQuadZ0;
        private double texturedQuadU0;
        private double texturedQuadV0;
        private int texturedQuadAlpha0;
        private double texturedQuadX1;
        private double texturedQuadY1;
        private double texturedQuadZ1;
        private double texturedQuadU1;
        private double texturedQuadV1;
        private int texturedQuadAlpha1;
        private double texturedQuadX2;
        private double texturedQuadY2;
        private double texturedQuadZ2;
        private double texturedQuadU2;
        private double texturedQuadV2;
        private int texturedQuadAlpha2;
        private double texturedQuadX3;
        private double texturedQuadY3;
        private double texturedQuadZ3;
        private double texturedQuadU3;
        private double texturedQuadV3;
        private int texturedQuadAlpha3;
        private int texturedQuadColor;
        private MultiBufferSource dangerDiamondBuffer;
        private LegacyTexturedRenderMode dangerDiamondRenderMode;
        private int dangerDiamondPackedLight;
        private int dangerDiamondPackedOverlay;
        private int dangerDiamondPoison;
        private int dangerDiamondFlammability;
        private int dangerDiamondReactivity;
        private LegacyDangerDiamondRenderer.Symbol dangerDiamondSymbol;
        private BeamQueueKind beamKind;
        private MultiBufferSource beamBuffer;
        private boolean beamDepthWrite;
        private int beamCount;
        private double[] beamDoubles = new double[0];
        private int[] beamInts = new int[0];
        private float[] beamFloats = new float[0];
        private LegacyBeamRenderer.WaveType[] beamWaves = new LegacyBeamRenderer.WaveType[0];
        private MultiBufferSource sparkBuffer;
        private LegacyTexturedRenderMode sparkRenderMode;
        private int sparkCount;
        private double[] sparkDoubles = new double[0];
        private int[] sparkInts = new int[0];
        private float[] sparkFloats = new float[0];
        private MultiBufferSource untexturedQuadBuffer;
        private LegacyTexturedRenderMode untexturedQuadRenderMode;
        private int untexturedQuadRenderAlpha;
        private int untexturedQuadCount;
        private double[] untexturedQuadDoubles = new double[0];
        private int[] untexturedQuadInts = new int[0];
        private MultiBufferSource solarBeamBuffer;
        private LegacyTexturedRenderMode solarBeamRenderMode;
        private int solarBeamRenderAlpha;
        private double solarBeamHalfWidth;
        private double solarBeamStartY;
        private int solarBeamNearAlpha;
        private int solarBeamFarAlpha;
        private int solarBeamCount;
        private float[] solarBeamMatrices = new float[0];
        private double[] solarBeamDistances = new double[0];
        private MultiBufferSource texturedQuadGroupBuffer;
        private ResourceLocation texturedQuadGroupTexture;
        private LegacyTexturedRenderMode texturedQuadGroupRenderMode;
        private int texturedQuadGroupCount;
        private double[] texturedQuadGroupDoubles = new double[0];
        private float[] texturedQuadGroupFloats = new float[0];
        private int[] texturedQuadGroupInts = new int[0];
        private MultiBufferSource texturedObjPartBuffer;
        private int texturedObjPartCount;
        private LegacyWavefrontModel[] texturedObjPartModels = new LegacyWavefrontModel[0];
        private LegacyWavefrontModel.SelectionHandle[] texturedObjPartSelections =
                new LegacyWavefrontModel.SelectionHandle[0];
        private ResourceLocation[] texturedObjPartTextures = new ResourceLocation[0];
        private LegacyTexturedRenderMode[] texturedObjPartRenderModes = new LegacyTexturedRenderMode[0];
        private LegacyWavefrontModel.UvTransform[] texturedObjPartUvTransforms =
                new LegacyWavefrontModel.UvTransform[0];
        private int[] texturedObjPartInts = new int[0];
        private MultiBufferSource untexturedObjPartBuffer;
        private int untexturedObjPartCount;
        private LegacyWavefrontModel[] untexturedObjPartModels = new LegacyWavefrontModel[0];
        private LegacyWavefrontModel.SelectionHandle[] untexturedObjPartSelections =
                new LegacyWavefrontModel.SelectionHandle[0];
        private LegacyTexturedRenderMode[] untexturedObjPartRenderModes = new LegacyTexturedRenderMode[0];
        private int[] untexturedObjPartInts = new int[0];
        private double[] untexturedObjPartClipDoubles = new double[0];
        private boolean[] untexturedObjPartClipped = new boolean[0];
        private MultiBufferSource atlasSpriteQuadBuffer;
        private LegacyTexturedRenderMode atlasSpriteQuadRenderMode;
        private int atlasSpriteQuadCount;
        private double[] atlasSpriteQuadDoubles = new double[0];
        private float[] atlasSpriteQuadFloats = new float[0];
        private int[] atlasSpriteQuadInts = new int[0];
        private TextureAtlasSprite[] atlasSpriteQuadSprites = new TextureAtlasSprite[0];

        private QueuedTask init(Runnable task, MachineRendererSubmissionScope scope) {
            this.runnableTask = task;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initDangerDiamond(PoseStack sourcePose, MultiBufferSource buffer,
                int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
                int poison, int flammability, int reactivity, LegacyDangerDiamondRenderer.Symbol symbol,
                MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.beamBuffer = null;
            this.untexturedQuadBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.dangerDiamondBuffer = buffer;
            this.sparkBuffer = null;
            this.solarBeamBuffer = null;
            this.dangerDiamondPackedLight = packedLight;
            this.dangerDiamondPackedOverlay = packedOverlay;
            this.dangerDiamondRenderMode = renderMode;
            this.dangerDiamondPoison = poison;
            this.dangerDiamondFlammability = flammability;
            this.dangerDiamondReactivity = reactivity;
            this.dangerDiamondSymbol = symbol == null ? LegacyDangerDiamondRenderer.Symbol.NONE : symbol;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask init(PoseStack sourcePose, Consumer<PoseStack> task,
                MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = task;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initTexturedQuad(PoseStack sourcePose, MultiBufferSource buffer, ResourceLocation texture,
                LegacyTexturedRenderMode renderMode, int packedLight, int packedOverlay,
                float normalX, float normalY, float normalZ,
                double x0, double y0, double z0, double u0, double v0, int alpha0,
                double x1, double y1, double z1, double u1, double v1, int alpha1,
                double x2, double y2, double z2, double u2, double v2, int alpha2,
                double x3, double y3, double z3, double u3, double v3, int alpha3,
                int color, MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.texturedQuadBuffer = buffer;
            this.texturedQuadTexture = texture;
            this.texturedQuadRenderMode = renderMode;
            this.texturedQuadPackedLight = packedLight;
            this.texturedQuadPackedOverlay = packedOverlay;
            this.texturedQuadNormalX = normalX;
            this.texturedQuadNormalY = normalY;
            this.texturedQuadNormalZ = normalZ;
            this.texturedQuadX0 = x0;
            this.texturedQuadY0 = y0;
            this.texturedQuadZ0 = z0;
            this.texturedQuadU0 = u0;
            this.texturedQuadV0 = v0;
            this.texturedQuadAlpha0 = alpha0;
            this.texturedQuadX1 = x1;
            this.texturedQuadY1 = y1;
            this.texturedQuadZ1 = z1;
            this.texturedQuadU1 = u1;
            this.texturedQuadV1 = v1;
            this.texturedQuadAlpha1 = alpha1;
            this.texturedQuadX2 = x2;
            this.texturedQuadY2 = y2;
            this.texturedQuadZ2 = z2;
            this.texturedQuadU2 = u2;
            this.texturedQuadV2 = v2;
            this.texturedQuadAlpha2 = alpha2;
            this.texturedQuadX3 = x3;
            this.texturedQuadY3 = y3;
            this.texturedQuadZ3 = z3;
            this.texturedQuadU3 = u3;
            this.texturedQuadV3 = v3;
            this.texturedQuadAlpha3 = alpha3;
            this.texturedQuadColor = color;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initSolidBeamGroup(PoseStack sourcePose, MultiBufferSource buffer, boolean depthWrite,
                MachineRendererSubmissionScope scope) {
            initBeamGroup(sourcePose, buffer, BeamQueueKind.SOLID, scope);
            this.beamDepthWrite = depthWrite;
            return this;
        }

        private QueuedTask initLineBeamGroup(PoseStack sourcePose, MultiBufferSource buffer,
                MachineRendererSubmissionScope scope) {
            initBeamGroup(sourcePose, buffer, BeamQueueKind.LINE, scope);
            this.beamDepthWrite = false;
            return this;
        }

        private void initBeamGroup(PoseStack sourcePose, MultiBufferSource buffer, BeamQueueKind kind,
                MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.beamKind = kind;
            this.beamBuffer = buffer;
            this.beamCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
        }

        private QueuedTask initSparkGroup(PoseStack sourcePose, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.untexturedQuadBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.sparkBuffer = buffer;
            this.sparkRenderMode = renderMode;
            this.sparkCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initUntexturedQuadGroup(PoseStack sourcePose, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, int renderAlpha, MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.solarBeamBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.untexturedQuadBuffer = buffer;
            this.untexturedQuadRenderMode = renderMode;
            this.untexturedQuadRenderAlpha = renderAlpha;
            this.untexturedQuadCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initSolarBoilerBeamGroup(PoseStack sourcePose, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, int renderAlpha, double halfWidth, double startY,
                int nearAlpha, int farAlpha, MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.texturedObjPartBuffer = null;
            this.untexturedObjPartBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.solarBeamBuffer = buffer;
            this.solarBeamRenderMode = renderMode;
            this.solarBeamRenderAlpha = renderAlpha;
            this.solarBeamHalfWidth = halfWidth;
            this.solarBeamStartY = startY;
            this.solarBeamNearAlpha = nearAlpha;
            this.solarBeamFarAlpha = farAlpha;
            this.solarBeamCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initTexturedQuadGroup(PoseStack sourcePose, MultiBufferSource buffer,
                ResourceLocation texture, LegacyTexturedRenderMode renderMode, MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.texturedQuadGroupBuffer = buffer;
            this.texturedQuadGroupTexture = texture;
            this.texturedQuadGroupRenderMode = renderMode;
            this.texturedQuadGroupCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initTexturedObjPartGroup(PoseStack sourcePose, MultiBufferSource buffer,
                MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.untexturedObjPartBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.texturedObjPartBuffer = buffer;
            this.texturedObjPartCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initUntexturedObjPartGroup(PoseStack sourcePose, MultiBufferSource buffer,
                MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.texturedObjPartBuffer = null;
            this.atlasSpriteQuadBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.untexturedObjPartBuffer = buffer;
            this.untexturedObjPartCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private QueuedTask initAtlasSpriteQuadGroup(PoseStack sourcePose, MultiBufferSource buffer,
                LegacyTexturedRenderMode renderMode, MachineRendererSubmissionScope scope) {
            this.runnableTask = null;
            this.poseTask = null;
            this.texturedQuadBuffer = null;
            this.dangerDiamondBuffer = null;
            this.beamBuffer = null;
            this.sparkBuffer = null;
            this.untexturedQuadBuffer = null;
            this.solarBeamBuffer = null;
            this.texturedQuadGroupBuffer = null;
            this.texturedObjPartBuffer = null;
            this.untexturedObjPartBuffer = null;
            this.pose.set(sourcePose.last().pose());
            this.normal.set(sourcePose.last().normal());
            this.atlasSpriteQuadBuffer = buffer;
            this.atlasSpriteQuadRenderMode = renderMode;
            this.atlasSpriteQuadCount = 0;
            this.scope = scope;
            this.scopeApplied = false;
            return this;
        }

        private boolean hasBeamSpecs() {
            return beamCount > 0;
        }

        private boolean hasSparkSpecs() {
            return sparkCount > 0;
        }

        private boolean hasUntexturedQuads() {
            return untexturedQuadCount > 0;
        }

        private boolean hasSolarBoilerBeams() {
            return solarBeamCount > 0;
        }

        private boolean hasTexturedQuadGroupQuads() {
            return texturedQuadGroupCount > 0;
        }

        private boolean hasTexturedObjParts() {
            return texturedObjPartCount > 0;
        }

        private boolean hasUntexturedObjParts() {
            return untexturedObjPartCount > 0;
        }

        private boolean hasAtlasSpriteQuads() {
            return atlasSpriteQuadCount > 0;
        }

        @Override
        public void add(double x, double y, double z,
                LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
                int start, int segments, float size, int layers, float thickness) {
            if (segments <= 0 || layers <= 0 || x * x + y * y + z * z <= 1.0E-10D) {
                return;
            }
            addBeamSpec(0.0D, 0.0D, 0.0D, x, y, z, wave, outerColor, innerColor,
                    start, segments, layers, size, thickness);
        }

        @Override
        public void add(double x, double y, double z,
                LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
                int start, int segments, float size) {
            add(0.0D, 0.0D, 0.0D, x, y, z, wave, outerColor, innerColor, start, segments, size);
        }

        @Override
        public void add(double originX, double originY, double originZ,
                double x, double y, double z,
                LegacyBeamRenderer.WaveType wave, int outerColor, int innerColor,
                int start, int segments, float size) {
            if (segments <= 0 || x * x + y * y + z * z <= 1.0E-10D) {
                return;
            }
            addBeamSpec(originX, originY, originZ, x, y, z, wave, outerColor, innerColor,
                    start, segments, 0, size, 0.0F);
        }

        private void addBeamSpec(double originX, double originY, double originZ,
                double x, double y, double z, LegacyBeamRenderer.WaveType wave,
                int outerColor, int innerColor, int start, int segments, int layers,
                float size, float thickness) {
            ensureBeamCapacity(beamCount + 1);
            int doubleOffset = beamCount * BEAM_DOUBLE_STRIDE;
            beamDoubles[doubleOffset] = originX;
            beamDoubles[doubleOffset + 1] = originY;
            beamDoubles[doubleOffset + 2] = originZ;
            beamDoubles[doubleOffset + 3] = x;
            beamDoubles[doubleOffset + 4] = y;
            beamDoubles[doubleOffset + 5] = z;
            int intOffset = beamCount * BEAM_INT_STRIDE;
            beamInts[intOffset] = outerColor;
            beamInts[intOffset + 1] = innerColor;
            beamInts[intOffset + 2] = start;
            beamInts[intOffset + 3] = segments;
            beamInts[intOffset + 4] = layers;
            int floatOffset = beamCount * BEAM_FLOAT_STRIDE;
            beamFloats[floatOffset] = size;
            beamFloats[floatOffset + 1] = thickness;
            beamWaves[beamCount] = wave == null ? LegacyBeamRenderer.WaveType.RANDOM : wave;
            beamCount++;
        }

        private void ensureBeamCapacity(int targetCount) {
            if (beamWaves.length >= targetCount) {
                return;
            }
            int capacity = Math.max(4, beamWaves.length);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            double[] newDoubles = new double[capacity * BEAM_DOUBLE_STRIDE];
            System.arraycopy(beamDoubles, 0, newDoubles, 0, beamCount * BEAM_DOUBLE_STRIDE);
            beamDoubles = newDoubles;
            int[] newInts = new int[capacity * BEAM_INT_STRIDE];
            System.arraycopy(beamInts, 0, newInts, 0, beamCount * BEAM_INT_STRIDE);
            beamInts = newInts;
            float[] newFloats = new float[capacity * BEAM_FLOAT_STRIDE];
            System.arraycopy(beamFloats, 0, newFloats, 0, beamCount * BEAM_FLOAT_STRIDE);
            beamFloats = newFloats;
            LegacyBeamRenderer.WaveType[] newWaves = new LegacyBeamRenderer.WaveType[capacity];
            System.arraycopy(beamWaves, 0, newWaves, 0, beamCount);
            beamWaves = newWaves;
        }

        @Override
        public void add(int seed, double x, double y, double z, float yawDegrees,
                float length, int minSegments, int maxRandomSegments,
                int outerColor, int innerColor) {
            addRadians(seed, x, y, z, yawDegrees * Mth.DEG_TO_RAD, length, minSegments, maxRandomSegments,
                    outerColor, innerColor);
        }

        @Override
        public void addRadians(int seed, double x, double y, double z, float yawRadians,
                float length, int minSegments, int maxRandomSegments,
                int outerColor, int innerColor) {
            if (length <= 0.0F || minSegments < 0 || maxRandomSegments < 0) {
                return;
            }
            ensureSparkCapacity(sparkCount + 1);
            int doubleOffset = sparkCount * SPARK_DOUBLE_STRIDE;
            sparkDoubles[doubleOffset] = x;
            sparkDoubles[doubleOffset + 1] = y;
            sparkDoubles[doubleOffset + 2] = z;
            int intOffset = sparkCount * SPARK_INT_STRIDE;
            sparkInts[intOffset] = seed;
            sparkInts[intOffset + 1] = minSegments;
            sparkInts[intOffset + 2] = maxRandomSegments;
            sparkInts[intOffset + 3] = outerColor;
            sparkInts[intOffset + 4] = innerColor;
            int floatOffset = sparkCount * SPARK_FLOAT_STRIDE;
            sparkFloats[floatOffset] = yawRadians;
            sparkFloats[floatOffset + 1] = length;
            sparkCount++;
        }

        private void ensureSparkCapacity(int targetCount) {
            int currentCapacity = sparkDoubles.length / SPARK_DOUBLE_STRIDE;
            if (currentCapacity >= targetCount) {
                return;
            }
            int capacity = Math.max(4, currentCapacity);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            double[] newDoubles = new double[capacity * SPARK_DOUBLE_STRIDE];
            System.arraycopy(sparkDoubles, 0, newDoubles, 0, sparkCount * SPARK_DOUBLE_STRIDE);
            sparkDoubles = newDoubles;
            int[] newInts = new int[capacity * SPARK_INT_STRIDE];
            System.arraycopy(sparkInts, 0, newInts, 0, sparkCount * SPARK_INT_STRIDE);
            sparkInts = newInts;
            float[] newFloats = new float[capacity * SPARK_FLOAT_STRIDE];
            System.arraycopy(sparkFloats, 0, newFloats, 0, sparkCount * SPARK_FLOAT_STRIDE);
            sparkFloats = newFloats;
        }

        @Override
        public void add(double x0, double y0, double z0,
                double x1, double y1, double z1,
                double x2, double y2, double z2,
                double x3, double y3, double z3,
                int color, int alpha0, int alpha1, int alpha2, int alpha3) {
            add(x0, y0, z0, color, alpha0,
                    x1, y1, z1, color, alpha1,
                    x2, y2, z2, color, alpha2,
                    x3, y3, z3, color, alpha3);
        }

        @Override
        public void add(double x0, double y0, double z0, int color0, int alpha0,
                double x1, double y1, double z1, int color1, int alpha1,
                double x2, double y2, double z2, int color2, int alpha2,
                double x3, double y3, double z3, int color3, int alpha3) {
            ensureUntexturedQuadCapacity(untexturedQuadCount + 1);
            int doubleOffset = untexturedQuadCount * UNTEXTURED_QUAD_DOUBLE_STRIDE;
            untexturedQuadDoubles[doubleOffset] = x0;
            untexturedQuadDoubles[doubleOffset + 1] = y0;
            untexturedQuadDoubles[doubleOffset + 2] = z0;
            untexturedQuadDoubles[doubleOffset + 3] = x1;
            untexturedQuadDoubles[doubleOffset + 4] = y1;
            untexturedQuadDoubles[doubleOffset + 5] = z1;
            untexturedQuadDoubles[doubleOffset + 6] = x2;
            untexturedQuadDoubles[doubleOffset + 7] = y2;
            untexturedQuadDoubles[doubleOffset + 8] = z2;
            untexturedQuadDoubles[doubleOffset + 9] = x3;
            untexturedQuadDoubles[doubleOffset + 10] = y3;
            untexturedQuadDoubles[doubleOffset + 11] = z3;
            int intOffset = untexturedQuadCount * UNTEXTURED_QUAD_INT_STRIDE;
            untexturedQuadInts[intOffset] = color0;
            untexturedQuadInts[intOffset + 1] = LegacyUntexturedQuadRenderer.alpha(alpha0 / 255.0F);
            untexturedQuadInts[intOffset + 2] = color1;
            untexturedQuadInts[intOffset + 3] = LegacyUntexturedQuadRenderer.alpha(alpha1 / 255.0F);
            untexturedQuadInts[intOffset + 4] = color2;
            untexturedQuadInts[intOffset + 5] = LegacyUntexturedQuadRenderer.alpha(alpha2 / 255.0F);
            untexturedQuadInts[intOffset + 6] = color3;
            untexturedQuadInts[intOffset + 7] = LegacyUntexturedQuadRenderer.alpha(alpha3 / 255.0F);
            untexturedQuadCount++;
        }

        private void ensureUntexturedQuadCapacity(int targetCount) {
            int currentCapacity = untexturedQuadDoubles.length / UNTEXTURED_QUAD_DOUBLE_STRIDE;
            if (currentCapacity >= targetCount) {
                return;
            }
            int capacity = Math.max(8, currentCapacity);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            double[] newDoubles = new double[capacity * UNTEXTURED_QUAD_DOUBLE_STRIDE];
            System.arraycopy(untexturedQuadDoubles, 0, newDoubles, 0,
                    untexturedQuadCount * UNTEXTURED_QUAD_DOUBLE_STRIDE);
            untexturedQuadDoubles = newDoubles;
            int[] newInts = new int[capacity * UNTEXTURED_QUAD_INT_STRIDE];
            System.arraycopy(untexturedQuadInts, 0, newInts, 0,
                    untexturedQuadCount * UNTEXTURED_QUAD_INT_STRIDE);
            untexturedQuadInts = newInts;
        }

        @Override
        public void add(int dx, int dy, int dz) {
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance <= 0.0D || !Double.isFinite(distance)) {
                return;
            }
            ensureSolarBeamCapacity(solarBeamCount + 1);
            double pitch = -Math.asin((dy + 0.5D) / distance) + Math.PI * 0.5D;
            double yaw = -Math.atan2(dz, dx) + Math.PI;
            solarBeamPose.set(pose)
                    .translate(0.5F, 0.0F, 0.5F)
                    .translate(-dx, -dy, -dz)
                    .translate(0.0F, 1.0F, 0.0F)
                    .rotateY((float) yaw)
                    .rotateZ((float) pitch)
                    .translate(0.0F, -1.0F, 0.0F);
            solarBeamPose.get(solarBeamMatrices, solarBeamCount * SOLAR_BOILER_BEAM_MATRIX_STRIDE);
            solarBeamDistances[solarBeamCount] = distance;
            solarBeamCount++;
        }

        private void ensureSolarBeamCapacity(int targetCount) {
            if (solarBeamDistances.length >= targetCount) {
                return;
            }
            int capacity = Math.max(4, solarBeamDistances.length);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            float[] newMatrices = new float[capacity * SOLAR_BOILER_BEAM_MATRIX_STRIDE];
            System.arraycopy(solarBeamMatrices, 0, newMatrices, 0,
                    solarBeamCount * SOLAR_BOILER_BEAM_MATRIX_STRIDE);
            solarBeamMatrices = newMatrices;
            double[] newDistances = new double[capacity];
            System.arraycopy(solarBeamDistances, 0, newDistances, 0, solarBeamCount);
            solarBeamDistances = newDistances;
        }

        @Override
        public void add(int packedLight, int packedOverlay,
                float normalX, float normalY, float normalZ,
                double x0, double y0, double z0, double u0, double v0, int alpha0,
                double x1, double y1, double z1, double u1, double v1, int alpha1,
                double x2, double y2, double z2, double u2, double v2, int alpha2,
                double x3, double y3, double z3, double u3, double v3, int alpha3,
                int color) {
            if (alpha0 <= 0 && alpha1 <= 0 && alpha2 <= 0 && alpha3 <= 0) {
                return;
            }
            ensureTexturedQuadGroupCapacity(texturedQuadGroupCount + 1);
            int doubleOffset = texturedQuadGroupCount * TEXTURED_QUAD_GROUP_DOUBLE_STRIDE;
            texturedQuadGroupDoubles[doubleOffset] = x0;
            texturedQuadGroupDoubles[doubleOffset + 1] = y0;
            texturedQuadGroupDoubles[doubleOffset + 2] = z0;
            texturedQuadGroupDoubles[doubleOffset + 3] = u0;
            texturedQuadGroupDoubles[doubleOffset + 4] = v0;
            texturedQuadGroupDoubles[doubleOffset + 5] = x1;
            texturedQuadGroupDoubles[doubleOffset + 6] = y1;
            texturedQuadGroupDoubles[doubleOffset + 7] = z1;
            texturedQuadGroupDoubles[doubleOffset + 8] = u1;
            texturedQuadGroupDoubles[doubleOffset + 9] = v1;
            texturedQuadGroupDoubles[doubleOffset + 10] = x2;
            texturedQuadGroupDoubles[doubleOffset + 11] = y2;
            texturedQuadGroupDoubles[doubleOffset + 12] = z2;
            texturedQuadGroupDoubles[doubleOffset + 13] = u2;
            texturedQuadGroupDoubles[doubleOffset + 14] = v2;
            texturedQuadGroupDoubles[doubleOffset + 15] = x3;
            texturedQuadGroupDoubles[doubleOffset + 16] = y3;
            texturedQuadGroupDoubles[doubleOffset + 17] = z3;
            texturedQuadGroupDoubles[doubleOffset + 18] = u3;
            texturedQuadGroupDoubles[doubleOffset + 19] = v3;
            int floatOffset = texturedQuadGroupCount * TEXTURED_QUAD_GROUP_FLOAT_STRIDE;
            texturedQuadGroupFloats[floatOffset] = normalX;
            texturedQuadGroupFloats[floatOffset + 1] = normalY;
            texturedQuadGroupFloats[floatOffset + 2] = normalZ;
            int intOffset = texturedQuadGroupCount * TEXTURED_QUAD_GROUP_INT_STRIDE;
            texturedQuadGroupInts[intOffset] = packedLight;
            texturedQuadGroupInts[intOffset + 1] = packedOverlay;
            texturedQuadGroupInts[intOffset + 2] = color;
            texturedQuadGroupInts[intOffset + 3] = Math.max(0, Math.min(255, alpha0));
            texturedQuadGroupInts[intOffset + 4] = Math.max(0, Math.min(255, alpha1));
            texturedQuadGroupInts[intOffset + 5] = Math.max(0, Math.min(255, alpha2));
            texturedQuadGroupInts[intOffset + 6] = Math.max(0, Math.min(255, alpha3));
            texturedQuadGroupCount++;
        }

        private void ensureTexturedQuadGroupCapacity(int targetCount) {
            int currentCapacity = texturedQuadGroupDoubles.length / TEXTURED_QUAD_GROUP_DOUBLE_STRIDE;
            if (currentCapacity >= targetCount) {
                return;
            }
            int capacity = Math.max(8, currentCapacity);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            double[] newDoubles = new double[capacity * TEXTURED_QUAD_GROUP_DOUBLE_STRIDE];
            System.arraycopy(texturedQuadGroupDoubles, 0, newDoubles, 0,
                    texturedQuadGroupCount * TEXTURED_QUAD_GROUP_DOUBLE_STRIDE);
            texturedQuadGroupDoubles = newDoubles;
            float[] newFloats = new float[capacity * TEXTURED_QUAD_GROUP_FLOAT_STRIDE];
            System.arraycopy(texturedQuadGroupFloats, 0, newFloats, 0,
                    texturedQuadGroupCount * TEXTURED_QUAD_GROUP_FLOAT_STRIDE);
            texturedQuadGroupFloats = newFloats;
            int[] newInts = new int[capacity * TEXTURED_QUAD_GROUP_INT_STRIDE];
            System.arraycopy(texturedQuadGroupInts, 0, newInts, 0,
                    texturedQuadGroupCount * TEXTURED_QUAD_GROUP_INT_STRIDE);
            texturedQuadGroupInts = newInts;
        }

        @Override
        public void add(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
                ResourceLocation texture, int packedLight, int packedOverlay,
                int red, int green, int blue, int alpha, boolean legacyShadow,
                LegacyTexturedRenderMode renderMode, LegacyWavefrontModel.UvTransform uvTransform) {
            if (model == null || selection == null || texture == null || alpha <= 0) {
                return;
            }
            ensureTexturedObjPartCapacity(texturedObjPartCount + 1);
            int index = texturedObjPartCount;
            texturedObjPartModels[index] = model;
            texturedObjPartSelections[index] = selection;
            texturedObjPartTextures[index] = texture;
            texturedObjPartRenderModes[index] = renderMode == null
                    ? LegacyTexturedRenderMode.CUTOUT_NO_CULL : renderMode;
            texturedObjPartUvTransforms[index] = uvTransform == null
                    ? LegacyWavefrontModel.UvTransform.DEFAULT : uvTransform;
            int intOffset = index * TEXTURED_OBJ_PART_INT_STRIDE;
            texturedObjPartInts[intOffset] = packedLight;
            texturedObjPartInts[intOffset + 1] = packedOverlay;
            texturedObjPartInts[intOffset + 2] = Math.max(0, Math.min(255, red));
            texturedObjPartInts[intOffset + 3] = Math.max(0, Math.min(255, green));
            texturedObjPartInts[intOffset + 4] = Math.max(0, Math.min(255, blue));
            texturedObjPartInts[intOffset + 5] = Math.max(0, Math.min(255, alpha));
            texturedObjPartInts[intOffset + 6] = legacyShadow ? 1 : 0;
            texturedObjPartCount++;
        }

        private void ensureTexturedObjPartCapacity(int targetCount) {
            if (texturedObjPartModels.length >= targetCount) {
                return;
            }
            int capacity = Math.max(4, texturedObjPartModels.length);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            LegacyWavefrontModel[] newModels = new LegacyWavefrontModel[capacity];
            System.arraycopy(texturedObjPartModels, 0, newModels, 0, texturedObjPartCount);
            texturedObjPartModels = newModels;
            LegacyWavefrontModel.SelectionHandle[] newSelections =
                    new LegacyWavefrontModel.SelectionHandle[capacity];
            System.arraycopy(texturedObjPartSelections, 0, newSelections, 0, texturedObjPartCount);
            texturedObjPartSelections = newSelections;
            ResourceLocation[] newTextures = new ResourceLocation[capacity];
            System.arraycopy(texturedObjPartTextures, 0, newTextures, 0, texturedObjPartCount);
            texturedObjPartTextures = newTextures;
            LegacyTexturedRenderMode[] newRenderModes = new LegacyTexturedRenderMode[capacity];
            System.arraycopy(texturedObjPartRenderModes, 0, newRenderModes, 0, texturedObjPartCount);
            texturedObjPartRenderModes = newRenderModes;
            LegacyWavefrontModel.UvTransform[] newUvTransforms = new LegacyWavefrontModel.UvTransform[capacity];
            System.arraycopy(texturedObjPartUvTransforms, 0, newUvTransforms, 0, texturedObjPartCount);
            texturedObjPartUvTransforms = newUvTransforms;
            int[] newInts = new int[capacity * TEXTURED_OBJ_PART_INT_STRIDE];
            System.arraycopy(texturedObjPartInts, 0, newInts, 0,
                    texturedObjPartCount * TEXTURED_OBJ_PART_INT_STRIDE);
            texturedObjPartInts = newInts;
        }

        @Override
        public void add(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode) {
            addUntexturedObjPart(model, selection, red, green, blue, alpha, renderMode,
                    false, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        @Override
        public void addClipped(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection,
                int red, int green, int blue, int alpha, LegacyTexturedRenderMode renderMode,
                double clipX, double clipY, double clipZ, double clipD) {
            addUntexturedObjPart(model, selection, red, green, blue, alpha, renderMode,
                    true, clipX, clipY, clipZ, clipD);
        }

        private void addUntexturedObjPart(LegacyWavefrontModel model,
                LegacyWavefrontModel.SelectionHandle selection, int red, int green, int blue, int alpha,
                LegacyTexturedRenderMode renderMode, boolean clipped, double clipX, double clipY,
                double clipZ, double clipD) {
            if (model == null || selection == null || alpha <= 0) {
                return;
            }
            ensureUntexturedObjPartCapacity(untexturedObjPartCount + 1);
            int index = untexturedObjPartCount;
            untexturedObjPartModels[index] = model;
            untexturedObjPartSelections[index] = selection;
            untexturedObjPartRenderModes[index] = renderMode == null
                    ? LegacyTexturedRenderMode.CUTOUT_NO_CULL : renderMode;
            int intOffset = index * UNTEXTURED_OBJ_PART_INT_STRIDE;
            untexturedObjPartInts[intOffset] = Math.max(0, Math.min(255, red));
            untexturedObjPartInts[intOffset + 1] = Math.max(0, Math.min(255, green));
            untexturedObjPartInts[intOffset + 2] = Math.max(0, Math.min(255, blue));
            untexturedObjPartInts[intOffset + 3] = Math.max(0, Math.min(255, alpha));
            int clipOffset = index * UNTEXTURED_OBJ_PART_CLIP_DOUBLE_STRIDE;
            untexturedObjPartClipDoubles[clipOffset] = clipX;
            untexturedObjPartClipDoubles[clipOffset + 1] = clipY;
            untexturedObjPartClipDoubles[clipOffset + 2] = clipZ;
            untexturedObjPartClipDoubles[clipOffset + 3] = clipD;
            untexturedObjPartClipped[index] = clipped;
            untexturedObjPartCount++;
        }

        private void ensureUntexturedObjPartCapacity(int targetCount) {
            if (untexturedObjPartModels.length >= targetCount) {
                return;
            }
            int capacity = Math.max(4, untexturedObjPartModels.length);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            LegacyWavefrontModel[] newModels = new LegacyWavefrontModel[capacity];
            System.arraycopy(untexturedObjPartModels, 0, newModels, 0, untexturedObjPartCount);
            untexturedObjPartModels = newModels;
            LegacyWavefrontModel.SelectionHandle[] newSelections =
                    new LegacyWavefrontModel.SelectionHandle[capacity];
            System.arraycopy(untexturedObjPartSelections, 0, newSelections, 0, untexturedObjPartCount);
            untexturedObjPartSelections = newSelections;
            LegacyTexturedRenderMode[] newRenderModes = new LegacyTexturedRenderMode[capacity];
            System.arraycopy(untexturedObjPartRenderModes, 0, newRenderModes, 0, untexturedObjPartCount);
            untexturedObjPartRenderModes = newRenderModes;
            int[] newInts = new int[capacity * UNTEXTURED_OBJ_PART_INT_STRIDE];
            System.arraycopy(untexturedObjPartInts, 0, newInts, 0,
                    untexturedObjPartCount * UNTEXTURED_OBJ_PART_INT_STRIDE);
            untexturedObjPartInts = newInts;
            double[] newClipDoubles = new double[capacity * UNTEXTURED_OBJ_PART_CLIP_DOUBLE_STRIDE];
            System.arraycopy(untexturedObjPartClipDoubles, 0, newClipDoubles, 0,
                    untexturedObjPartCount * UNTEXTURED_OBJ_PART_CLIP_DOUBLE_STRIDE);
            untexturedObjPartClipDoubles = newClipDoubles;
            boolean[] newClipped = new boolean[capacity];
            System.arraycopy(untexturedObjPartClipped, 0, newClipped, 0, untexturedObjPartCount);
            untexturedObjPartClipped = newClipped;
        }

        @Override
        public void add(TextureAtlasSprite sprite, int packedLight, int packedOverlay,
                float normalX, float normalY, float normalZ,
                double x0, double y0, double z0, double pixelU0, double pixelV0,
                double x1, double y1, double z1, double pixelU1, double pixelV1,
                double x2, double y2, double z2, double pixelU2, double pixelV2,
                double x3, double y3, double z3, double pixelU3, double pixelV3,
                int color, int alpha) {
            if (sprite == null || alpha <= 0) {
                return;
            }
            ensureAtlasSpriteQuadCapacity(atlasSpriteQuadCount + 1);
            int doubleOffset = atlasSpriteQuadCount * ATLAS_SPRITE_QUAD_DOUBLE_STRIDE;
            atlasSpriteQuadDoubles[doubleOffset] = x0;
            atlasSpriteQuadDoubles[doubleOffset + 1] = y0;
            atlasSpriteQuadDoubles[doubleOffset + 2] = z0;
            atlasSpriteQuadDoubles[doubleOffset + 3] = pixelU0;
            atlasSpriteQuadDoubles[doubleOffset + 4] = pixelV0;
            atlasSpriteQuadDoubles[doubleOffset + 5] = x1;
            atlasSpriteQuadDoubles[doubleOffset + 6] = y1;
            atlasSpriteQuadDoubles[doubleOffset + 7] = z1;
            atlasSpriteQuadDoubles[doubleOffset + 8] = pixelU1;
            atlasSpriteQuadDoubles[doubleOffset + 9] = pixelV1;
            atlasSpriteQuadDoubles[doubleOffset + 10] = x2;
            atlasSpriteQuadDoubles[doubleOffset + 11] = y2;
            atlasSpriteQuadDoubles[doubleOffset + 12] = z2;
            atlasSpriteQuadDoubles[doubleOffset + 13] = pixelU2;
            atlasSpriteQuadDoubles[doubleOffset + 14] = pixelV2;
            atlasSpriteQuadDoubles[doubleOffset + 15] = x3;
            atlasSpriteQuadDoubles[doubleOffset + 16] = y3;
            atlasSpriteQuadDoubles[doubleOffset + 17] = z3;
            atlasSpriteQuadDoubles[doubleOffset + 18] = pixelU3;
            atlasSpriteQuadDoubles[doubleOffset + 19] = pixelV3;
            int floatOffset = atlasSpriteQuadCount * ATLAS_SPRITE_QUAD_FLOAT_STRIDE;
            atlasSpriteQuadFloats[floatOffset] = normalX;
            atlasSpriteQuadFloats[floatOffset + 1] = normalY;
            atlasSpriteQuadFloats[floatOffset + 2] = normalZ;
            int intOffset = atlasSpriteQuadCount * ATLAS_SPRITE_QUAD_INT_STRIDE;
            atlasSpriteQuadInts[intOffset] = packedLight;
            atlasSpriteQuadInts[intOffset + 1] = packedOverlay;
            atlasSpriteQuadInts[intOffset + 2] = color;
            atlasSpriteQuadInts[intOffset + 3] = Math.max(0, Math.min(255, alpha));
            atlasSpriteQuadSprites[atlasSpriteQuadCount] = sprite;
            atlasSpriteQuadCount++;
        }

        private void ensureAtlasSpriteQuadCapacity(int targetCount) {
            if (atlasSpriteQuadSprites.length >= targetCount) {
                return;
            }
            int capacity = Math.max(32, atlasSpriteQuadSprites.length);
            while (capacity < targetCount) {
                capacity <<= 1;
            }
            double[] newDoubles = new double[capacity * ATLAS_SPRITE_QUAD_DOUBLE_STRIDE];
            System.arraycopy(atlasSpriteQuadDoubles, 0, newDoubles, 0,
                    atlasSpriteQuadCount * ATLAS_SPRITE_QUAD_DOUBLE_STRIDE);
            atlasSpriteQuadDoubles = newDoubles;
            float[] newFloats = new float[capacity * ATLAS_SPRITE_QUAD_FLOAT_STRIDE];
            System.arraycopy(atlasSpriteQuadFloats, 0, newFloats, 0,
                    atlasSpriteQuadCount * ATLAS_SPRITE_QUAD_FLOAT_STRIDE);
            atlasSpriteQuadFloats = newFloats;
            int[] newInts = new int[capacity * ATLAS_SPRITE_QUAD_INT_STRIDE];
            System.arraycopy(atlasSpriteQuadInts, 0, newInts, 0,
                    atlasSpriteQuadCount * ATLAS_SPRITE_QUAD_INT_STRIDE);
            atlasSpriteQuadInts = newInts;
            TextureAtlasSprite[] newSprites = new TextureAtlasSprite[capacity];
            System.arraycopy(atlasSpriteQuadSprites, 0, newSprites, 0, atlasSpriteQuadCount);
            atlasSpriteQuadSprites = newSprites;
        }

        private void run(PresentContext context) {
            this.context = context;
            try {
                run();
            } finally {
                this.context = null;
            }
        }

        @Override
        public void run() {
            if (!scopeApplied && scope != null) {
                scopeApplied = true;
                try {
                    HbmRenderFrameCulling.runWithMachineRendererSubmissionScope(scope, this);
                } finally {
                    scopeApplied = false;
                }
                return;
            }
            if (runnableTask != null) {
                runnableTask.run();
                return;
            }
            if (poseTask != null) {
                runPoseTask();
            }
            if (texturedQuadBuffer != null) {
                runTexturedQuadTask();
            }
            if (dangerDiamondBuffer != null) {
                runDangerDiamondTask();
            }
            if (beamBuffer != null) {
                runBeamGroupTask();
            }
            if (sparkBuffer != null) {
                runSparkGroupTask();
            }
            if (untexturedQuadBuffer != null) {
                runUntexturedQuadGroupTask();
            }
            if (solarBeamBuffer != null) {
                runSolarBoilerBeamGroupTask();
            }
            if (texturedQuadGroupBuffer != null) {
                runTexturedQuadGroupTask();
            }
            if (texturedObjPartBuffer != null) {
                runTexturedObjPartGroupTask();
            }
            if (untexturedObjPartBuffer != null) {
                runUntexturedObjPartGroupTask();
            }
            if (atlasSpriteQuadBuffer != null) {
                runAtlasSpriteQuadGroupTask();
            }
        }

        private void runPoseTask() {
            PoseStack replay = beginReplayPose();
            try {
                poseTask.accept(replay);
            } finally {
                endReplayPose(replay);
            }
        }

        private PoseStack beginReplayPose() {
            PoseStack replay = replayPose;
            if (!replay.clear()) {
                replay = new PoseStack();
                replayPose = replay;
            } else {
                replay.setIdentity();
            }
            replay.mulPoseMatrix(pose);
            replay.last().normal().set(normal);
            return replay;
        }

        private void endReplayPose(PoseStack replay) {
            if (!replay.clear()) {
                replayPose = new PoseStack();
            }
        }

        private void runTexturedQuadTask() {
            VertexConsumer consumer = context.texturedQuadConsumer(texturedQuadBuffer, texturedQuadTexture,
                    texturedQuadRenderMode);
            LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, normal,
                    texturedQuadPackedLight, texturedQuadPackedOverlay,
                    texturedQuadNormalX, texturedQuadNormalY, texturedQuadNormalZ,
                    texturedQuadX0, texturedQuadY0, texturedQuadZ0, texturedQuadU0, texturedQuadV0,
                    texturedQuadAlpha0,
                    texturedQuadX1, texturedQuadY1, texturedQuadZ1, texturedQuadU1, texturedQuadV1,
                    texturedQuadAlpha1,
                    texturedQuadX2, texturedQuadY2, texturedQuadZ2, texturedQuadU2, texturedQuadV2,
                    texturedQuadAlpha2,
                    texturedQuadX3, texturedQuadY3, texturedQuadZ3, texturedQuadU3, texturedQuadV3,
                    texturedQuadAlpha3,
                    texturedQuadColor);
        }

        private void runDangerDiamondTask() {
            VertexConsumer consumer = context.texturedQuadConsumer(dangerDiamondBuffer,
                    LegacyDangerDiamondRenderer.TEXTURE, dangerDiamondRenderMode);
            LegacyDangerDiamondRenderer.renderDirect(consumer, pose, normal,
                    dangerDiamondPackedLight, dangerDiamondPackedOverlay,
                    dangerDiamondPoison, dangerDiamondFlammability, dangerDiamondReactivity, dangerDiamondSymbol);
        }

        private void runBeamGroupTask() {
            if (beamKind == BeamQueueKind.SOLID) {
                VertexConsumer consumer = context.untexturedConsumer(beamBuffer,
                        LegacyBeamRenderer.solidBeamRenderMode(beamDepthWrite), 255);
                for (int index = 0; index < beamCount; index++) {
                    int doubleOffset = index * BEAM_DOUBLE_STRIDE;
                    int intOffset = index * BEAM_INT_STRIDE;
                    int floatOffset = index * BEAM_FLOAT_STRIDE;
                    LegacyBeamRenderer.solidBeam(consumer, pose,
                            beamDoubles[doubleOffset + 3], beamDoubles[doubleOffset + 4],
                            beamDoubles[doubleOffset + 5], beamWaves[index],
                            beamInts[intOffset], beamInts[intOffset + 1],
                            beamInts[intOffset + 2], beamInts[intOffset + 3],
                            beamFloats[floatOffset], beamInts[intOffset + 4],
                            beamFloats[floatOffset + 1]);
                }
                return;
            }
            VertexConsumer consumer = context.lineConsumer(beamBuffer, LegacyLineRenderer.DEFAULT_LINE_WIDTH,
                    LegacyTexturedRenderMode.CUTOUT_NO_CULL, 255);
            for (int index = 0; index < beamCount; index++) {
                int doubleOffset = index * BEAM_DOUBLE_STRIDE;
                int intOffset = index * BEAM_INT_STRIDE;
                int floatOffset = index * BEAM_FLOAT_STRIDE;
                LegacyBeamRenderer.lineBeam(consumer, pose, normal,
                        beamDoubles[doubleOffset], beamDoubles[doubleOffset + 1], beamDoubles[doubleOffset + 2],
                        beamDoubles[doubleOffset + 3], beamDoubles[doubleOffset + 4],
                        beamDoubles[doubleOffset + 5], beamWaves[index],
                        beamInts[intOffset], beamInts[intOffset + 1],
                        beamInts[intOffset + 2], beamInts[intOffset + 3],
                        beamFloats[floatOffset]);
            }
        }

        private void runSparkGroupTask() {
            VertexConsumer outer = context.lineConsumer(sparkBuffer, LegacySparkRenderer.OUTER_LINE_WIDTH,
                    sparkRenderMode, 255);
            VertexConsumer inner = context.lineConsumer(sparkBuffer, LegacySparkRenderer.INNER_LINE_WIDTH,
                    sparkRenderMode, 255);
            for (int index = 0; index < sparkCount; index++) {
                int doubleOffset = index * SPARK_DOUBLE_STRIDE;
                int intOffset = index * SPARK_INT_STRIDE;
                int floatOffset = index * SPARK_FLOAT_STRIDE;
                sparkPose.set(pose);
                sparkNormal.set(normal);
                float yawRadians = sparkFloats[floatOffset];
                sparkPose.rotateY(yawRadians);
                sparkNormal.rotateY(yawRadians);
                LegacySparkRenderer.renderSpark(sparkPose, sparkNormal, outer, inner,
                        sparkInts[intOffset],
                        sparkDoubles[doubleOffset], sparkDoubles[doubleOffset + 1],
                        sparkDoubles[doubleOffset + 2], sparkFloats[floatOffset + 1],
                        sparkInts[intOffset + 1], sparkInts[intOffset + 2],
                        sparkInts[intOffset + 3], sparkInts[intOffset + 4]);
            }
        }

        private void runUntexturedQuadGroupTask() {
            VertexConsumer consumer = context.untexturedConsumer(untexturedQuadBuffer, untexturedQuadRenderMode,
                    untexturedQuadRenderAlpha);
            for (int index = 0; index < untexturedQuadCount; index++) {
                int doubleOffset = index * UNTEXTURED_QUAD_DOUBLE_STRIDE;
                int intOffset = index * UNTEXTURED_QUAD_INT_STRIDE;
                LegacyUntexturedQuadRenderer.vertex(consumer, pose,
                        untexturedQuadDoubles[doubleOffset],
                        untexturedQuadDoubles[doubleOffset + 1],
                        untexturedQuadDoubles[doubleOffset + 2],
                        untexturedQuadInts[intOffset], untexturedQuadInts[intOffset + 1]);
                LegacyUntexturedQuadRenderer.vertex(consumer, pose,
                        untexturedQuadDoubles[doubleOffset + 3],
                        untexturedQuadDoubles[doubleOffset + 4],
                        untexturedQuadDoubles[doubleOffset + 5],
                        untexturedQuadInts[intOffset + 2], untexturedQuadInts[intOffset + 3]);
                LegacyUntexturedQuadRenderer.vertex(consumer, pose,
                        untexturedQuadDoubles[doubleOffset + 6],
                        untexturedQuadDoubles[doubleOffset + 7],
                        untexturedQuadDoubles[doubleOffset + 8],
                        untexturedQuadInts[intOffset + 4], untexturedQuadInts[intOffset + 5]);
                LegacyUntexturedQuadRenderer.vertex(consumer, pose,
                        untexturedQuadDoubles[doubleOffset + 9],
                        untexturedQuadDoubles[doubleOffset + 10],
                        untexturedQuadDoubles[doubleOffset + 11],
                        untexturedQuadInts[intOffset + 6], untexturedQuadInts[intOffset + 7]);
            }
        }

        private void runSolarBoilerBeamGroupTask() {
            VertexConsumer consumer = context.untexturedConsumer(solarBeamBuffer, solarBeamRenderMode,
                    solarBeamRenderAlpha);
            for (int index = 0; index < solarBeamCount; index++) {
                solarBeamPose.set(solarBeamMatrices, index * SOLAR_BOILER_BEAM_MATRIX_STRIDE);
                renderSolarBoilerBeamQuads(consumer, solarBeamPose, solarBeamDistances[index]);
            }
        }

        private void renderSolarBoilerBeamQuads(VertexConsumer consumer, Matrix4f beamPose, double distance) {
            double halfWidth = solarBeamHalfWidth;
            double startY = solarBeamStartY;
            int nearAlpha = solarBeamNearAlpha;
            int farAlpha = solarBeamFarAlpha;
            LegacyUntexturedQuadRenderer.quad(consumer, beamPose,
                    halfWidth, startY, halfWidth,
                    halfWidth, startY, -halfWidth,
                    halfWidth, distance, -halfWidth,
                    halfWidth, distance, halfWidth,
                    0xFFFFFF, nearAlpha, nearAlpha, farAlpha, farAlpha);
            LegacyUntexturedQuadRenderer.quad(consumer, beamPose,
                    -halfWidth, startY, halfWidth,
                    -halfWidth, startY, -halfWidth,
                    -halfWidth, distance, -halfWidth,
                    -halfWidth, distance, halfWidth,
                    0xFFFFFF, nearAlpha, nearAlpha, farAlpha, farAlpha);
            LegacyUntexturedQuadRenderer.quad(consumer, beamPose,
                    halfWidth, startY, halfWidth,
                    -halfWidth, startY, halfWidth,
                    -halfWidth, distance, halfWidth,
                    halfWidth, distance, halfWidth,
                    0xFFFFFF, nearAlpha, nearAlpha, farAlpha, farAlpha);
            LegacyUntexturedQuadRenderer.quad(consumer, beamPose,
                    halfWidth, startY, -halfWidth,
                    -halfWidth, startY, -halfWidth,
                    -halfWidth, distance, -halfWidth,
                    halfWidth, distance, -halfWidth,
                    0xFFFFFF, nearAlpha, nearAlpha, farAlpha, farAlpha);
        }

        private void runTexturedQuadGroupTask() {
            VertexConsumer consumer = context.texturedQuadConsumer(texturedQuadGroupBuffer, texturedQuadGroupTexture,
                    texturedQuadGroupRenderMode);
            for (int index = 0; index < texturedQuadGroupCount; index++) {
                int doubleOffset = index * TEXTURED_QUAD_GROUP_DOUBLE_STRIDE;
                int floatOffset = index * TEXTURED_QUAD_GROUP_FLOAT_STRIDE;
                int intOffset = index * TEXTURED_QUAD_GROUP_INT_STRIDE;
                LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, normal,
                        texturedQuadGroupInts[intOffset], texturedQuadGroupInts[intOffset + 1],
                        texturedQuadGroupFloats[floatOffset],
                        texturedQuadGroupFloats[floatOffset + 1],
                        texturedQuadGroupFloats[floatOffset + 2],
                        texturedQuadGroupDoubles[doubleOffset],
                        texturedQuadGroupDoubles[doubleOffset + 1],
                        texturedQuadGroupDoubles[doubleOffset + 2],
                        texturedQuadGroupDoubles[doubleOffset + 3],
                        texturedQuadGroupDoubles[doubleOffset + 4],
                        texturedQuadGroupInts[intOffset + 3],
                        texturedQuadGroupDoubles[doubleOffset + 5],
                        texturedQuadGroupDoubles[doubleOffset + 6],
                        texturedQuadGroupDoubles[doubleOffset + 7],
                        texturedQuadGroupDoubles[doubleOffset + 8],
                        texturedQuadGroupDoubles[doubleOffset + 9],
                        texturedQuadGroupInts[intOffset + 4],
                        texturedQuadGroupDoubles[doubleOffset + 10],
                        texturedQuadGroupDoubles[doubleOffset + 11],
                        texturedQuadGroupDoubles[doubleOffset + 12],
                        texturedQuadGroupDoubles[doubleOffset + 13],
                        texturedQuadGroupDoubles[doubleOffset + 14],
                        texturedQuadGroupInts[intOffset + 5],
                        texturedQuadGroupDoubles[doubleOffset + 15],
                        texturedQuadGroupDoubles[doubleOffset + 16],
                        texturedQuadGroupDoubles[doubleOffset + 17],
                        texturedQuadGroupDoubles[doubleOffset + 18],
                        texturedQuadGroupDoubles[doubleOffset + 19],
                        texturedQuadGroupInts[intOffset + 6],
                        texturedQuadGroupInts[intOffset + 2]);
            }
        }

        private void runTexturedObjPartGroupTask() {
            PoseStack replay = beginReplayPose();
            try {
                for (int index = 0; index < texturedObjPartCount; index++) {
                    int intOffset = index * TEXTURED_OBJ_PART_INT_STRIDE;
                    texturedObjPartModels[index].renderOnlyInCallOrder(texturedObjPartTextures[index],
                            replay, texturedObjPartBuffer,
                            texturedObjPartInts[intOffset], texturedObjPartInts[intOffset + 1],
                            texturedObjPartInts[intOffset + 2], texturedObjPartInts[intOffset + 3],
                            texturedObjPartInts[intOffset + 4], texturedObjPartInts[intOffset + 5],
                            texturedObjPartInts[intOffset + 6] != 0,
                            texturedObjPartRenderModes[index], texturedObjPartUvTransforms[index],
                            texturedObjPartSelections[index]);
                }
            } finally {
                endReplayPose(replay);
            }
        }

        private void runUntexturedObjPartGroupTask() {
            PoseStack replay = beginReplayPose();
            try {
                for (int index = 0; index < untexturedObjPartCount; index++) {
                    int intOffset = index * UNTEXTURED_OBJ_PART_INT_STRIDE;
                    if (untexturedObjPartClipped[index]) {
                        int clipOffset = index * UNTEXTURED_OBJ_PART_CLIP_DOUBLE_STRIDE;
                        untexturedObjPartModels[index].renderOnlyUntexturedClipped(replay,
                                untexturedObjPartBuffer,
                                untexturedObjPartInts[intOffset], untexturedObjPartInts[intOffset + 1],
                                untexturedObjPartInts[intOffset + 2], untexturedObjPartInts[intOffset + 3],
                                untexturedObjPartRenderModes[index], untexturedObjPartSelections[index],
                                untexturedObjPartClipDoubles[clipOffset],
                                untexturedObjPartClipDoubles[clipOffset + 1],
                                untexturedObjPartClipDoubles[clipOffset + 2],
                                untexturedObjPartClipDoubles[clipOffset + 3]);
                        continue;
                    }
                    untexturedObjPartModels[index].renderOnlyUntextured(replay, untexturedObjPartBuffer,
                            untexturedObjPartInts[intOffset], untexturedObjPartInts[intOffset + 1],
                            untexturedObjPartInts[intOffset + 2], untexturedObjPartInts[intOffset + 3],
                            untexturedObjPartRenderModes[index], untexturedObjPartSelections[index]);
                }
            } finally {
                endReplayPose(replay);
            }
        }

        private void runAtlasSpriteQuadGroupTask() {
            VertexConsumer consumer = null;
            int consumerAlpha = -1;
            for (int index = 0; index < atlasSpriteQuadCount; index++) {
                int doubleOffset = index * ATLAS_SPRITE_QUAD_DOUBLE_STRIDE;
                int floatOffset = index * ATLAS_SPRITE_QUAD_FLOAT_STRIDE;
                int intOffset = index * ATLAS_SPRITE_QUAD_INT_STRIDE;
                int alpha = atlasSpriteQuadInts[intOffset + 3];
                if (consumer == null || consumerAlpha != alpha) {
                    consumer = context.atlasSpriteConsumer(atlasSpriteQuadBuffer, atlasSpriteQuadRenderMode, alpha);
                    consumerAlpha = alpha;
                }
                LegacyTexturedQuadRenderer.spritePixelQuadDirect(atlasSpriteQuadSprites[index], consumer, pose, normal,
                        atlasSpriteQuadInts[intOffset], atlasSpriteQuadInts[intOffset + 1],
                        atlasSpriteQuadFloats[floatOffset],
                        atlasSpriteQuadFloats[floatOffset + 1],
                        atlasSpriteQuadFloats[floatOffset + 2],
                        atlasSpriteQuadDoubles[doubleOffset],
                        atlasSpriteQuadDoubles[doubleOffset + 1],
                        atlasSpriteQuadDoubles[doubleOffset + 2],
                        atlasSpriteQuadDoubles[doubleOffset + 3],
                        atlasSpriteQuadDoubles[doubleOffset + 4],
                        atlasSpriteQuadDoubles[doubleOffset + 5],
                        atlasSpriteQuadDoubles[doubleOffset + 6],
                        atlasSpriteQuadDoubles[doubleOffset + 7],
                        atlasSpriteQuadDoubles[doubleOffset + 8],
                        atlasSpriteQuadDoubles[doubleOffset + 9],
                        atlasSpriteQuadDoubles[doubleOffset + 10],
                        atlasSpriteQuadDoubles[doubleOffset + 11],
                        atlasSpriteQuadDoubles[doubleOffset + 12],
                        atlasSpriteQuadDoubles[doubleOffset + 13],
                        atlasSpriteQuadDoubles[doubleOffset + 14],
                        atlasSpriteQuadDoubles[doubleOffset + 15],
                        atlasSpriteQuadDoubles[doubleOffset + 16],
                        atlasSpriteQuadDoubles[doubleOffset + 17],
                        atlasSpriteQuadDoubles[doubleOffset + 18],
                        atlasSpriteQuadDoubles[doubleOffset + 19],
                        atlasSpriteQuadInts[intOffset + 2], alpha);
            }
        }

        private void release() {
            runnableTask = null;
            poseTask = null;
            scope = null;
            context = null;
            scopeApplied = false;
            pose.identity();
            normal.identity();
            texturedQuadBuffer = null;
            texturedQuadTexture = null;
            texturedQuadRenderMode = null;
            texturedQuadPackedLight = 0;
            texturedQuadPackedOverlay = 0;
            texturedQuadNormalX = 0.0F;
            texturedQuadNormalY = 0.0F;
            texturedQuadNormalZ = 0.0F;
            texturedQuadColor = 0;
            dangerDiamondBuffer = null;
            dangerDiamondRenderMode = null;
            dangerDiamondPackedLight = 0;
            dangerDiamondPackedOverlay = 0;
            dangerDiamondPoison = 0;
            dangerDiamondFlammability = 0;
            dangerDiamondReactivity = 0;
            dangerDiamondSymbol = null;
            beamKind = null;
            beamBuffer = null;
            beamDepthWrite = false;
            beamCount = 0;
            sparkBuffer = null;
            sparkRenderMode = null;
            sparkCount = 0;
            untexturedQuadBuffer = null;
            untexturedQuadRenderMode = null;
            untexturedQuadRenderAlpha = 0;
            untexturedQuadCount = 0;
            solarBeamBuffer = null;
            solarBeamRenderMode = null;
            solarBeamRenderAlpha = 0;
            solarBeamHalfWidth = 0.0D;
            solarBeamStartY = 0.0D;
            solarBeamNearAlpha = 0;
            solarBeamFarAlpha = 0;
            solarBeamCount = 0;
            texturedQuadGroupBuffer = null;
            texturedQuadGroupTexture = null;
            texturedQuadGroupRenderMode = null;
            texturedQuadGroupCount = 0;
            texturedObjPartBuffer = null;
            for (int index = 0; index < texturedObjPartCount; index++) {
                texturedObjPartModels[index] = null;
                texturedObjPartSelections[index] = null;
                texturedObjPartTextures[index] = null;
                texturedObjPartRenderModes[index] = null;
                texturedObjPartUvTransforms[index] = null;
            }
            texturedObjPartCount = 0;
            untexturedObjPartBuffer = null;
            for (int index = 0; index < untexturedObjPartCount; index++) {
                untexturedObjPartModels[index] = null;
                untexturedObjPartSelections[index] = null;
                untexturedObjPartRenderModes[index] = null;
            }
            untexturedObjPartCount = 0;
            atlasSpriteQuadBuffer = null;
            atlasSpriteQuadRenderMode = null;
            for (int index = 0; index < atlasSpriteQuadCount; index++) {
                atlasSpriteQuadSprites[index] = null;
            }
            atlasSpriteQuadCount = 0;
            if (!replayPose.clear()) {
                replayPose = new PoseStack();
            }
        }
    }
}
