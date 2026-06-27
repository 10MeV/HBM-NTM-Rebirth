package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.LegacyWavefrontModel.RenderBackendClearReason;
import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;

/**
 * Reflection-only Iris/Oculus detector for render backend guard rails.
 */
public final class HbmShaderCompatibilityDetector {
    private static boolean initialized;
    private static Object irisApiInstance;
    private static Method irisIsShaderPackInUse;
    private static Method irisIsRenderingShadowPass;
    private static MethodHandle irisIsShaderPackInUseHandle;
    private static MethodHandle irisIsRenderingShadowPassHandle;
    private static boolean lastShaderActive;
    private static volatile boolean cachedShaderActive;
    private static volatile boolean pendingChunkInvalidation;
    private static Method irisGetPipelineManager;
    private static Method irisGetPipelineNullable;
    private static volatile boolean pipelineReflectionAvailable;
    private static volatile boolean pipelineIdentitySampled;
    private static volatile Object lastPipelineIdentity;
    private static final AtomicLong pipelineGeneration = new AtomicLong();

    private HbmShaderCompatibilityDetector() {
    }

    public static boolean isExternalShaderActive() {
        if (!RenderSystem.isOnRenderThread()) {
            return cachedShaderActive;
        }
        initialize();
        if (irisApiInstance == null || (irisIsShaderPackInUseHandle == null && irisIsShaderPackInUse == null)) {
            cachedShaderActive = false;
            return false;
        }
        try {
            boolean active = invokeBoolean(irisIsShaderPackInUseHandle, irisIsShaderPackInUse);
            cachedShaderActive = active;
            if (active != lastShaderActive) {
                lastShaderActive = active;
                pendingChunkInvalidation = true;
                LegacyWavefrontModel.clearRenderBackend(RenderBackendClearReason.SHADER_RELOAD);
                HbmNtm.LOGGER.info("HBM render shader compatibility state changed: {}", active ? "shader pack active" : "shader pack inactive");
            }
            return active;
        } catch (Throwable throwable) {
            return cachedShaderActive;
        }
    }

    public static boolean isRenderingShadowPass() {
        initialize();
        if (irisApiInstance == null || (irisIsRenderingShadowPassHandle == null && irisIsRenderingShadowPass == null)) {
            return false;
        }
        try {
            return invokeBoolean(irisIsRenderingShadowPassHandle, irisIsRenderingShadowPass);
        } catch (Throwable throwable) {
            return false;
        }
    }

    public static long tickPipelineGeneration() {
        initialize();
        if (!pipelineReflectionAvailable || !RenderSystem.isOnRenderThread()) {
            return pipelineGeneration.get();
        }
        try {
            Object pipelineManager = irisGetPipelineManager.invoke(null);
            Object pipeline = pipelineManager == null ? null : irisGetPipelineNullable.invoke(pipelineManager);
            if (!pipelineIdentitySampled) {
                lastPipelineIdentity = pipeline;
                pipelineIdentitySampled = true;
                return pipelineGeneration.get();
            }
            if (pipeline != lastPipelineIdentity) {
                lastPipelineIdentity = pipeline;
                long generation = pipelineGeneration.incrementAndGet();
                pendingChunkInvalidation = true;
                LegacyWavefrontModel.clearRenderBackend(RenderBackendClearReason.SHADER_RELOAD);
                HbmNtm.LOGGER.info("HBM render shader pipeline generation changed: {}", generation);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            HbmNtm.LOGGER.debug("Skipped Iris/Oculus pipeline generation sample: {}", exception.toString());
        }
        return pipelineGeneration.get();
    }

    public static long pipelineGeneration() {
        return pipelineGeneration.get();
    }

    public static boolean canUseIrisExtendedShader() {
        return isExternalShaderActive() && HbmIrisExtendedShaderAccess.isReflectionAvailable();
    }

    public static void processPendingChunkInvalidation() {
        if (!pendingChunkInvalidation) {
            return;
        }
        pendingChunkInvalidation = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.levelRenderer == null) {
            return;
        }
        try {
            minecraft.levelRenderer.allChanged();
        } catch (RuntimeException exception) {
            HbmNtm.LOGGER.debug("Skipped shader-state chunk invalidation: {}", exception.getMessage());
        }
    }

    public static Snapshot snapshot() {
        initialize();
        return new Snapshot(
                initialized,
                irisApiInstance != null,
                irisIsShaderPackInUseHandle != null || irisIsShaderPackInUse != null,
                cachedShaderActive,
                pipelineReflectionAvailable,
                pipelineIdentitySampled,
                pipelineGeneration.get(),
                pendingChunkInvalidation,
                HbmIrisExtendedShaderAccess.isReflectionAvailable(),
                canUseIrisExtendedShader());
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!ModList.get().isLoaded("iris") && !ModList.get().isLoaded("oculus")) {
            return;
        }
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Method getInstance = irisApiClass.getMethod("getInstance");
            irisApiInstance = getInstance.invoke(null);
            irisIsShaderPackInUse = irisApiClass.getMethod("isShaderPackInUse");
            irisIsRenderingShadowPass = irisApiClass.getMethod("isRenderingShadowPass");
            bindMethodHandles();
            bindPipelineReflection();
            HbmNtm.LOGGER.info("HBM render shader compatibility detector initialized for Iris/Oculus (methodHandles={}).",
                    irisIsShaderPackInUseHandle != null);
        } catch (ReflectiveOperationException exception) {
            HbmNtm.LOGGER.warn("HBM render shader compatibility detector could not bind Iris/Oculus API: {}", exception.toString());
            irisApiInstance = null;
            irisIsShaderPackInUse = null;
            irisIsRenderingShadowPass = null;
            irisIsShaderPackInUseHandle = null;
            irisIsRenderingShadowPassHandle = null;
            bindPipelineReflection();
        }
    }

    private static void bindMethodHandles() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            irisIsShaderPackInUse.setAccessible(true);
            irisIsRenderingShadowPass.setAccessible(true);
            irisIsShaderPackInUseHandle = lookup.unreflect(irisIsShaderPackInUse)
                    .asType(MethodType.methodType(boolean.class, Object.class));
            irisIsRenderingShadowPassHandle = lookup.unreflect(irisIsRenderingShadowPass)
                    .asType(MethodType.methodType(boolean.class, Object.class));
        } catch (IllegalAccessException | SecurityException exception) {
            HbmNtm.LOGGER.debug("HBM render shader compatibility detector will use Method.invoke: {}", exception.toString());
            irisIsShaderPackInUseHandle = null;
            irisIsRenderingShadowPassHandle = null;
        }
    }

    private static void bindPipelineReflection() {
        try {
            Class<?> irisClass = firstClass(
                    "net.irisshaders.iris.Iris",
                    "net.coderbot.iris.Iris");
            Class<?> pipelineManagerClass = firstClass(
                    "net.irisshaders.iris.pipeline.PipelineManager",
                    "net.coderbot.iris.pipeline.PipelineManager");
            irisGetPipelineManager = irisClass.getMethod("getPipelineManager");
            irisGetPipelineNullable = pipelineManagerClass.getMethod("getPipelineNullable");
            pipelineReflectionAvailable = true;
            HbmNtm.LOGGER.info("HBM render shader compatibility detector bound Iris/Oculus pipeline identity sampling.");
        } catch (ReflectiveOperationException exception) {
            irisGetPipelineManager = null;
            irisGetPipelineNullable = null;
            pipelineReflectionAvailable = false;
            HbmNtm.LOGGER.debug("HBM render shader compatibility detector could not bind Iris/Oculus pipeline reflection: {}",
                    exception.toString());
        }
    }

    private static Class<?> firstClass(String... classNames) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException exception) {
                last = exception;
            }
        }
        throw last == null ? new ClassNotFoundException() : last;
    }

    private static boolean invokeBoolean(MethodHandle handle, Method method) throws Throwable {
        if (handle != null) {
            return (boolean) handle.invokeExact((Object) irisApiInstance);
        }
        Boolean result = (Boolean) method.invoke(irisApiInstance);
        return result != null && result;
    }

    public record Snapshot(
            boolean initialized,
            boolean apiPresent,
            boolean shaderQueryPresent,
            boolean shaderActive,
            boolean pipelineReflectionAvailable,
            boolean pipelineIdentitySampled,
            long pipelineGeneration,
            boolean pendingChunkInvalidation,
            boolean irisExtendedShaderReflectionAvailable,
            boolean irisExtendedShaderUsable) {
    }
}
