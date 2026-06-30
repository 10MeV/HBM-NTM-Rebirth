package com.hbm.ntm.item;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

final class ClientItemRendererBridge {
    private static final String BRIDGE_CLASS = "com.hbm.ntm.client.renderer.ClientItemRendererBridges";

    private ClientItemRendererBridge() {
    }

    static void accept(String methodName, Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridgeClass = Class.forName(BRIDGE_CLASS);
            Method method = bridgeClass.getMethod(methodName, Consumer.class);
            method.invoke(null, consumer);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to initialize client item renderer bridge: " + methodName, ex);
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("Unable to initialize client item renderer bridge: " + methodName, cause);
        }
    }
}
