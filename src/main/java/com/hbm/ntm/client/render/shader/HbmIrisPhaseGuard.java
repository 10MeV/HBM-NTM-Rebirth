package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import net.minecraftforge.fml.ModList;

/**
 * Reflection-only Iris/Oculus WorldRenderingPhase guard.
 */
public final class HbmIrisPhaseGuard implements AutoCloseable {
    private static final HbmIrisPhaseGuard NOOP = new HbmIrisPhaseGuard(false, null);

    private static volatile boolean initialized;
    private static volatile boolean available;
    private static Method getPipelineManager;
    private static Method getPipelineNullable;
    private static Method setPhase;
    private static Method getPhase;
    private static MethodHandle getPipelineManagerHandle;
    private static MethodHandle getPipelineNullableHandle;
    private static MethodHandle setPhaseHandle;
    private static MethodHandle getPhaseHandle;
    private static Class<?> phaseEnumClass;
    private static volatile Object phaseBlockEntities;
    private static volatile Object phaseNone;

    private final boolean active;
    private final Object previousPhase;

    private HbmIrisPhaseGuard(boolean active, Object previousPhase) {
        this.active = active;
        this.previousPhase = previousPhase;
    }

    public static HbmIrisPhaseGuard pushBlockEntities() {
        initialize();
        if (!available) {
            return NOOP;
        }
        try {
            Object pipeline = currentPipeline();
            if (pipeline == null || phaseBlockEntities == null) {
                return NOOP;
            }
            Object previous = invokeGetPhase(pipeline);
            if (previous == phaseBlockEntities) {
                return NOOP;
            }
            invokeSetPhase(pipeline, phaseBlockEntities);
            return new HbmIrisPhaseGuard(true, previous);
        } catch (Throwable ignored) {
            return NOOP;
        }
    }

    @Override
    public void close() {
        if (!active) {
            return;
        }
        try {
            Object pipeline = currentPipeline();
            if (pipeline == null) {
                return;
            }
            if (previousPhase != null) {
                invokeSetPhase(pipeline, previousPhase);
            } else if (phaseNone != null) {
                invokeSetPhase(pipeline, phaseNone);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Object currentPipeline() throws Throwable {
        Object manager = getPipelineManagerHandle == null
                ? getPipelineManager.invoke(null)
                : (Object) getPipelineManagerHandle.invokeExact();
        if (manager == null) {
            return null;
        }
        return getPipelineNullableHandle == null
                ? getPipelineNullable.invoke(manager)
                : (Object) getPipelineNullableHandle.invokeExact(manager);
    }

    private static void invokeSetPhase(Object pipeline, Object phase) throws Throwable {
        if (setPhaseHandle != null) {
            setPhaseHandle.invokeExact(pipeline, phase);
        } else if (setPhase != null) {
            setPhase.invoke(pipeline, phase);
        }
    }

    private static Object invokeGetPhase(Object pipeline) throws Throwable {
        if (getPhaseHandle != null) {
            return getPhaseHandle.invokeExact(pipeline);
        }
        return getPhase == null ? null : getPhase.invoke(pipeline);
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
            Class<?> managerClass = firstClass(
                    "net.irisshaders.iris.pipeline.PipelineManager",
                    "net.coderbot.iris.pipeline.PipelineManager");
            getPipelineNullable = managerClass.getMethod("getPipelineNullable");
            Class<?> pipelineClass = firstClass(
                    "net.irisshaders.iris.pipeline.WorldRenderingPipeline",
                    "net.coderbot.iris.pipeline.WorldRenderingPipeline");
            phaseEnumClass = firstClass(
                    "net.irisshaders.iris.pipeline.WorldRenderingPhase",
                    "net.coderbot.iris.pipeline.WorldRenderingPhase");
            setPhase = pipelineClass.getMethod("setPhase", phaseEnumClass);
            try {
                getPhase = pipelineClass.getMethod("getPhase");
            } catch (NoSuchMethodException ignored) {
                getPhase = null;
            }
            phaseBlockEntities = enumValue("BLOCK_ENTITIES");
            phaseNone = enumValue("NONE");
            bindMethodHandles();
            available = phaseBlockEntities != null && phaseNone != null;
            if (available) {
                HbmNtm.LOGGER.info("HBM Iris/Oculus phase guard initialized (methodHandles={}).",
                        setPhaseHandle != null);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            HbmNtm.LOGGER.debug("HBM Iris/Oculus phase guard unavailable: {}", exception.toString());
        }
    }

    private static void bindMethodHandles() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            getPipelineManager.setAccessible(true);
            getPipelineNullable.setAccessible(true);
            setPhase.setAccessible(true);
            getPipelineManagerHandle = lookup.unreflect(getPipelineManager)
                    .asType(MethodType.methodType(Object.class));
            getPipelineNullableHandle = lookup.unreflect(getPipelineNullable)
                    .asType(MethodType.methodType(Object.class, Object.class));
            setPhaseHandle = lookup.unreflect(setPhase)
                    .asType(MethodType.methodType(void.class, Object.class, Object.class));
            if (getPhase != null) {
                getPhase.setAccessible(true);
                getPhaseHandle = lookup.unreflect(getPhase)
                        .asType(MethodType.methodType(Object.class, Object.class));
            }
        } catch (IllegalAccessException | SecurityException exception) {
            getPipelineManagerHandle = null;
            getPipelineNullableHandle = null;
            setPhaseHandle = null;
            getPhaseHandle = null;
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

    private static Object enumValue(String name) {
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object value = Enum.valueOf((Class<Enum>) phaseEnumClass.asSubclass(Enum.class), name);
            return value;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
