package com.hbm.ntm.client.render;

import com.hbm.ntm.client.render.culling.HbmRenderFrameCulling;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.joml.Matrix4f;

public final class LegacyMachineEffectPresenter {
    private static final Map<PresentStage, List<Runnable>> QUEUES = new EnumMap<>(PresentStage.class);
    private static final AtomicLong FRAME_GENERATION = new AtomicLong();
    private static final AtomicLong PRESENT_CALLS = new AtomicLong();
    private static final AtomicLong AFTER_BLOCK_ENTITIES_PRESENTS = new AtomicLong();
    private static final AtomicLong AFTER_LEVEL_PRESENTS = new AtomicLong();
    private static final AtomicLong QUEUED_TASKS = new AtomicLong();
    private static final AtomicLong PRESENTED_TASKS = new AtomicLong();
    private static final AtomicLong FAILED_TASKS = new AtomicLong();
    private static final AtomicLong CLEARS = new AtomicLong();
    private static final AtomicLong LAST_FRAME_PRESENT_CALLS = new AtomicLong();
    private static final AtomicLong LAST_FRAME_PRESENTED_TASKS = new AtomicLong();
    private static long currentFramePresentCalls;
    private static long currentFramePresentedTasks;
    private static volatile PresentStage lastPresentStage = PresentStage.MANUAL;

    private LegacyMachineEffectPresenter() {
    }

    static {
        for (PresentStage stage : PresentStage.values()) {
            QUEUES.put(stage, new ArrayList<>());
        }
    }

    public static synchronized void beginFrame() {
        LAST_FRAME_PRESENT_CALLS.set(currentFramePresentCalls);
        LAST_FRAME_PRESENTED_TASKS.set(currentFramePresentedTasks);
        currentFramePresentCalls = 0L;
        currentFramePresentedTasks = 0L;
        FRAME_GENERATION.incrementAndGet();
    }

    public static synchronized void enqueue(PresentStage stage, Runnable task) {
        if (task == null) {
            return;
        }
        PresentStage resolvedStage = stage == null ? PresentStage.AFTER_BLOCK_ENTITIES : stage;
        QUEUES.get(resolvedStage).add(HbmRenderFrameCulling.captureMachineRendererSubmissionScope(task));
        QUEUED_TASKS.incrementAndGet();
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

    public static synchronized void present(PresentStage stage) {
        PresentStage resolvedStage = stage == null ? PresentStage.MANUAL : stage;
        PRESENT_CALLS.incrementAndGet();
        currentFramePresentCalls++;
        lastPresentStage = resolvedStage;
        switch (resolvedStage) {
            case AFTER_BLOCK_ENTITIES -> AFTER_BLOCK_ENTITIES_PRESENTS.incrementAndGet();
            case AFTER_LEVEL -> AFTER_LEVEL_PRESENTS.incrementAndGet();
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
                PRESENTED_TASKS.incrementAndGet();
                currentFramePresentedTasks++;
            } catch (RuntimeException exception) {
                FAILED_TASKS.incrementAndGet();
                throw exception;
            }
        }
    }

    public static synchronized void clear() {
        for (List<Runnable> queue : QUEUES.values()) {
            queue.clear();
        }
        CLEARS.incrementAndGet();
    }

    public static Snapshot snapshot() {
        return new Snapshot(
                FRAME_GENERATION.get(),
                PRESENT_CALLS.get(),
                AFTER_BLOCK_ENTITIES_PRESENTS.get(),
                AFTER_LEVEL_PRESENTS.get(),
                QUEUED_TASKS.get(),
                PRESENTED_TASKS.get(),
                FAILED_TASKS.get(),
                CLEARS.get(),
                QUEUES.values().stream().mapToInt(List::size).sum(),
                currentFramePresentCalls,
                currentFramePresentedTasks,
                LAST_FRAME_PRESENT_CALLS.get(),
                LAST_FRAME_PRESENTED_TASKS.get(),
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
