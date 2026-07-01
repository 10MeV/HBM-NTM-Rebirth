package com.hbm.ntm.client.render;

import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.joml.Matrix4f;

public final class LegacyMachineEffectPresenter {
    private static final Map<PresentStage, List<Runnable>> QUEUES = new EnumMap<>(PresentStage.class);
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
        }
    }

    public static void beginFrame() {
        lastFramePresentCalls = currentFramePresentCalls;
        lastFramePresentedTasks = currentFramePresentedTasks;
        currentFramePresentCalls = 0L;
        currentFramePresentedTasks = 0L;
        frameGeneration++;
    }

    public static void enqueue(PresentStage stage, Runnable task) {
        if (task == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QUEUES.get(resolvedStage).add(HbmRenderFrameCulling.captureMachineRendererSubmissionScope(task));
        queuedTasks++;
    }

    public static void enqueue(PresentStage stage, PoseStack poseStack, Consumer<PoseStack> task) {
        if (poseStack == null || task == null) {
            return;
        }
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        enqueue(stage, () -> {
            PoseStack replay = new PoseStack();
            replay.mulPoseMatrix(pose);
            task.accept(replay);
        });
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
        List<Runnable> queue = QUEUES.get(resolvedStage);
        if (queue.isEmpty()) {
            return;
        }
        List<Runnable> tasks = new ArrayList<>(queue);
        queue.clear();
        for (Runnable task : tasks) {
            try {
                task.run();
                presentedTasks++;
                currentFramePresentedTasks++;
            } catch (RuntimeException exception) {
                failedTasks++;
                throw exception;
            }
        }
    }

    public static void clear() {
        for (List<Runnable> queue : QUEUES.values()) {
            queue.clear();
        }
        clears++;
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
}
