package com.hbm.ntm.util;

import java.lang.reflect.Method;

public final class HbmClassUtil {
    private HbmClassUtil() {
    }

    public static boolean classExists(String className) {
        return tryLoadClass(className) != null;
    }

    public static Class<?> tryLoadClass(String className) {
        if (className == null || className.isBlank()) {
            return null;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | LinkageError exception) {
            return null;
        }
    }

    public static Method findNoArgMethod(String className, String methodName) {
        Class<?> type = tryLoadClass(className);
        return type == null ? null : findNoArgMethod(type, methodName);
    }

    public static Method findNoArgMethod(Class<?> type, String methodName) {
        if (type == null || methodName == null || methodName.isBlank()) {
            return null;
        }
        try {
            Method method = type.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException | SecurityException exception) {
            return null;
        }
    }

    public static Object invokeNoArg(Method method, Object instance) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(instance);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeNoArg(Method method, Object instance, Class<T> resultType) {
        Object value = invokeNoArg(method, instance);
        return resultType != null && resultType.isInstance(value) ? (T) value : null;
    }
}
