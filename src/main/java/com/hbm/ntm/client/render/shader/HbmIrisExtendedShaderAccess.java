package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.render.HbmRenderFrameLight;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.fml.ModList;

/**
 * Reflection-only bridge to Iris/Oculus ExtendedShader internals.
 *
 * <p>This is a capability and cache owner for the future companion-mesh path.
 * It intentionally does not enable shader-pack GPU rendering by itself.
 */
public final class HbmIrisExtendedShaderAccess {
    private static final String[] MAIN_SHADER_KEY_NAMES = {
            "BLOCK_ENTITY", "BLOCK_ENTITY_DIFFUSE", "ENTITIES_CUTOUT",
            "BLOCK", "TERRAIN_CUTOUT", "TERRAIN"};
    private static final String[] SHADOW_SHADER_KEY_NAMES = {
            "SHADOW_ENTITIES_CUTOUT", "SHADOW_BLOCK_ENTITY",
            "SHADOW_TERRAIN_CUTOUT", "SHADOW_TERRAIN"};

    private static final AtomicLong currentPassId = new AtomicLong();
    private static final Map<String, Object> shaderKeyCache = new HashMap<>();

    private static volatile boolean initialized;
    private static volatile boolean reflectionAvailable;
    private static volatile boolean shaderKeyCandidatesBuilt;
    private static volatile Object[] mainShaderKeys = new Object[0];
    private static volatile Object[] shadowShaderKeys = new Object[0];
    private static volatile long cachedMainPassId = -1L;
    private static volatile long cachedShadowPassId = -1L;
    private static volatile long cachedPipelineGeneration = -1L;
    private static volatile ShaderInstance cachedMainShader;
    private static volatile ShaderInstance cachedShadowShader;
    private static volatile Object lastResolvedMainKey;
    private static volatile Object lastResolvedShadowKey;

    private static Method getPipelineManager;
    private static Method getPipelineNullable;
    private static Class<?> shaderRenderingPipelineClass;
    private static Method getShaderMap;
    private static Class<?> shaderKeyClass;
    private static Method shaderMapGetShader;

    private static Object capturedRenderingStateInstance;
    private static Method setCurrentBlockEntityMethod;
    private static Method getCurrentBlockEntityMethod;
    private static MethodHandle setCurrentBlockEntityHandle;
    private static MethodHandle getCurrentBlockEntityHandle;

    private HbmIrisExtendedShaderAccess() {
    }

    public static void tickPass() {
        currentPassId.incrementAndGet();
        long generation = HbmShaderCompatibilityDetector.pipelineGeneration();
        if (generation != cachedPipelineGeneration) {
            cachedPipelineGeneration = generation;
            invalidateShaderCache();
        }
    }

    public static boolean isReflectionAvailable() {
        initialize();
        return reflectionAvailable;
    }

    public static ShaderInstance getBlockEntityShader(boolean shadowPass) {
        initialize();
        if (!reflectionAvailable) {
            return null;
        }
        long pass = currentPassId.get();
        if (shadowPass) {
            ShaderInstance cached = cachedShadowShader;
            if (cached != null && cachedShadowPassId == pass) {
                return cached;
            }
        } else {
            ShaderInstance cached = cachedMainShader;
            if (cached != null && cachedMainPassId == pass) {
                return cached;
            }
        }
        ShaderInstance shader = lookupShader(shadowPass);
        if (shadowPass) {
            cachedShadowShader = shader;
            cachedShadowPassId = pass;
        } else {
            cachedMainShader = shader;
            cachedMainPassId = pass;
        }
        return shader;
    }

    public static int setCurrentRenderedBlockEntity(int value) {
        initialize();
        if (!reflectionAvailable || capturedRenderingStateInstance == null) {
            return Integer.MIN_VALUE;
        }
        try {
            if (setCurrentBlockEntityHandle != null) {
                int previous = getCurrentBlockEntityHandle == null
                        ? 0
                        : (int) getCurrentBlockEntityHandle.invokeExact(capturedRenderingStateInstance);
                setCurrentBlockEntityHandle.invokeExact(capturedRenderingStateInstance, value);
                return previous;
            }
            if (setCurrentBlockEntityMethod == null) {
                return Integer.MIN_VALUE;
            }
            int previous = getCurrentBlockEntityMethod == null
                    ? 0
                    : (int) getCurrentBlockEntityMethod.invoke(capturedRenderingStateInstance);
            setCurrentBlockEntityMethod.invoke(capturedRenderingStateInstance, value);
            return previous;
        } catch (Throwable throwable) {
            return Integer.MIN_VALUE;
        }
    }

    public static void restoreCurrentRenderedBlockEntity(int previous) {
        if (previous == Integer.MIN_VALUE) {
            return;
        }
        initialize();
        if (!reflectionAvailable || capturedRenderingStateInstance == null) {
            return;
        }
        try {
            if (setCurrentBlockEntityHandle != null) {
                setCurrentBlockEntityHandle.invokeExact(capturedRenderingStateInstance, previous);
            } else if (setCurrentBlockEntityMethod != null) {
                setCurrentBlockEntityMethod.invoke(capturedRenderingStateInstance, previous);
            }
        } catch (Throwable ignored) {
        }
    }

    public static void invalidateShaderCache() {
        cachedMainShader = null;
        cachedShadowShader = null;
        cachedMainPassId = -1L;
        cachedShadowPassId = -1L;
        HbmRenderFrameLight.invalidateCaches();
        HbmIrisRenderBatch.invalidateCaches();
        LegacyWavefrontModel.invalidateIrisCompanionShaderAttributeCaches();
    }

    public static Snapshot snapshot() {
        initialize();
        return new Snapshot(
                initialized,
                reflectionAvailable,
                shaderKeyCandidatesBuilt,
                mainShaderKeys.length,
                shadowShaderKeys.length,
                capturedRenderingStateInstance != null && setCurrentBlockEntityMethod != null,
                setCurrentBlockEntityHandle != null,
                cachedMainShader != null,
                cachedShadowShader != null,
                currentPassId.get(),
                cachedPipelineGeneration);
    }

    private static ShaderInstance lookupShader(boolean shadowPass) {
        try {
            Object pipelineManager = getPipelineManager.invoke(null);
            if (pipelineManager == null) {
                return null;
            }
            Object pipeline = getPipelineNullable.invoke(pipelineManager);
            if (pipeline == null || !shaderRenderingPipelineClass.isInstance(pipeline)) {
                return null;
            }
            Object shaderMap = getShaderMap.invoke(pipeline);
            if (shaderMap == null) {
                return null;
            }
            ensureShaderKeyCandidatesBuilt();
            Object[] candidates = shadowPass ? shadowShaderKeys : mainShaderKeys;
            for (Object key : candidates) {
                Object instance = shaderMapGetShader.invoke(shaderMap, key);
                if (instance instanceof ShaderInstance shader) {
                    logResolvedKey(shadowPass, key);
                    return shader;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            HbmNtm.LOGGER.debug("Skipped Iris/Oculus ExtendedShader lookup: {}", exception.toString());
        }
        return null;
    }

    private static void logResolvedKey(boolean shadowPass, Object key) {
        if (shadowPass) {
            if (lastResolvedShadowKey != key) {
                lastResolvedShadowKey = key;
                HbmNtm.LOGGER.info("HBM Iris/Oculus shadow ExtendedShader resolved to {}", key);
            }
        } else if (lastResolvedMainKey != key) {
            lastResolvedMainKey = key;
            HbmNtm.LOGGER.info("HBM Iris/Oculus main ExtendedShader resolved to {}", key);
        }
    }

    private static void ensureShaderKeyCandidatesBuilt() {
        if (shaderKeyCandidatesBuilt) {
            return;
        }
        synchronized (shaderKeyCache) {
            if (shaderKeyCandidatesBuilt) {
                return;
            }
            mainShaderKeys = resolveShaderKeys(MAIN_SHADER_KEY_NAMES);
            shadowShaderKeys = resolveShaderKeys(SHADOW_SHADER_KEY_NAMES);
            shaderKeyCandidatesBuilt = true;
        }
    }

    private static Object[] resolveShaderKeys(String[] names) {
        Object[] temporary = new Object[names.length];
        int count = 0;
        for (String name : names) {
            Object key = resolveShaderKey(name);
            if (key != null) {
                temporary[count++] = key;
            }
        }
        Object[] resolved = new Object[count];
        System.arraycopy(temporary, 0, resolved, 0, count);
        return resolved;
    }

    private static Object resolveShaderKey(String name) {
        Object cached = shaderKeyCache.get(name);
        if (cached != null) {
            return cached;
        }
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object value = Enum.valueOf((Class<Enum>) shaderKeyClass.asSubclass(Enum.class), name);
            shaderKeyCache.put(name, value);
            return value;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!ModList.get().isLoaded("iris") && !ModList.get().isLoaded("oculus")) {
            return;
        }
        try {
            Class<?> irisClass = firstClass("net.irisshaders.iris.Iris", "net.coderbot.iris.Iris");
            getPipelineManager = irisClass.getMethod("getPipelineManager");
            Class<?> pipelineManagerClass = firstClass(
                    "net.irisshaders.iris.pipeline.PipelineManager",
                    "net.coderbot.iris.pipeline.PipelineManager");
            getPipelineNullable = pipelineManagerClass.getMethod("getPipelineNullable");
            shaderRenderingPipelineClass = firstClass(
                    "net.irisshaders.iris.pipeline.ShaderRenderingPipeline",
                    "net.irisshaders.iris.pipeline.programs.ShaderRenderingPipeline",
                    "net.coderbot.iris.pipeline.ShaderRenderingPipeline",
                    "net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline");
            getShaderMap = shaderRenderingPipelineClass.getMethod("getShaderMap");
            Class<?> shaderMapClass = firstClass(
                    "net.irisshaders.iris.pipeline.programs.ShaderMap",
                    "net.coderbot.iris.pipeline.newshader.ShaderMap");
            shaderKeyClass = firstClass(
                    "net.irisshaders.iris.pipeline.programs.ShaderKey",
                    "net.coderbot.iris.pipeline.newshader.ShaderKey");
            shaderMapGetShader = shaderMapClass.getMethod("getShader", shaderKeyClass);
            bindCapturedRenderingState();
            reflectionAvailable = true;
            HbmNtm.LOGGER.info("HBM Iris/Oculus ExtendedShader access initialized (pipeline={}, blockEntityReset={}).",
                    shaderRenderingPipelineClass.getName(), setCurrentBlockEntityMethod != null);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            reflectionAvailable = false;
            HbmNtm.LOGGER.warn("HBM Iris/Oculus ExtendedShader access unavailable: {}", exception.toString());
        }
    }

    private static void bindCapturedRenderingState() {
        try {
            Class<?> capturedClass = firstClass(
                    "net.irisshaders.iris.uniforms.CapturedRenderingState",
                    "net.coderbot.iris.uniforms.CapturedRenderingState");
            capturedRenderingStateInstance = capturedClass.getField("INSTANCE").get(null);
            setCurrentBlockEntityMethod = capturedClass.getMethod("setCurrentBlockEntity", int.class);
            try {
                getCurrentBlockEntityMethod = capturedClass.getMethod("getCurrentRenderedBlockEntity");
            } catch (NoSuchMethodException ignored) {
                getCurrentBlockEntityMethod = null;
            }
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                setCurrentBlockEntityMethod.setAccessible(true);
                setCurrentBlockEntityHandle = lookup.unreflect(setCurrentBlockEntityMethod)
                        .asType(MethodType.methodType(void.class, Object.class, int.class));
                if (getCurrentBlockEntityMethod != null) {
                    getCurrentBlockEntityMethod.setAccessible(true);
                    getCurrentBlockEntityHandle = lookup.unreflect(getCurrentBlockEntityMethod)
                            .asType(MethodType.methodType(int.class, Object.class));
                }
            } catch (IllegalAccessException | SecurityException exception) {
                setCurrentBlockEntityHandle = null;
                getCurrentBlockEntityHandle = null;
                HbmNtm.LOGGER.debug("HBM Iris/Oculus blockEntityId MethodHandle binding unavailable: {}",
                        exception.toString());
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            capturedRenderingStateInstance = null;
            setCurrentBlockEntityMethod = null;
            getCurrentBlockEntityMethod = null;
            setCurrentBlockEntityHandle = null;
            getCurrentBlockEntityHandle = null;
            HbmNtm.LOGGER.debug("HBM Iris/Oculus CapturedRenderingState unavailable: {}", exception.toString());
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

    public record Snapshot(
            boolean initialized,
            boolean reflectionAvailable,
            boolean shaderKeyCandidatesBuilt,
            int mainShaderKeyCandidates,
            int shadowShaderKeyCandidates,
            boolean blockEntityIdResetAvailable,
            boolean blockEntityIdMethodHandlesAvailable,
            boolean mainShaderCached,
            boolean shadowShaderCached,
            long passId,
            long pipelineGeneration) {
    }
}
