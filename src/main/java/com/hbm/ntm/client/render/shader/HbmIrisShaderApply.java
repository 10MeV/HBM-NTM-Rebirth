package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * Guarded apply wrapper for Iris/Oculus ExtendedShader instances.
 */
public final class HbmIrisShaderApply {
    private static final int MAX_FAILURE_REASON_LENGTH = 240;
    private static volatile String lastFailureReason = "none";

    private HbmIrisShaderApply() {
    }

    public static boolean tryApply(ShaderInstance shader) {
        if (shader == null) {
            rememberFailure("shader=null");
            return false;
        }
        try {
            shader.apply();
            lastFailureReason = "none";
            return true;
        } catch (IllegalStateException exception) {
            if (isDestroyedGlResource(exception)) {
                rememberFailure("destroyed GlResource");
                HbmIrisExtendedShaderAccess.invalidateShaderCache();
                HbmNtm.LOGGER.debug("HBM Iris/Oculus shader apply failed after destroyed GlResource; caches invalidated.");
            } else {
                rememberFailure(exception.toString());
                HbmNtm.LOGGER.debug("HBM Iris/Oculus shader apply failed: {}", exception.toString());
            }
            return false;
        } catch (Throwable throwable) {
            rememberFailure(throwable.toString());
            HbmNtm.LOGGER.debug("HBM Iris/Oculus shader apply failed: {}", throwable.toString());
            return false;
        }
    }

    public static String lastFailureReason() {
        return lastFailureReason;
    }

    public static void rememberFailure(Throwable failure) {
        rememberFailure(failure == null ? "unknown" : failure.toString());
    }

    private static void rememberFailure(String reason) {
        if (reason == null || reason.isBlank()) {
            lastFailureReason = "unknown";
            return;
        }
        String normalized = reason.replace('\r', ' ').replace('\n', ' ').strip();
        if (normalized.length() > MAX_FAILURE_REASON_LENGTH) {
            normalized = normalized.substring(0, MAX_FAILURE_REASON_LENGTH - 3) + "...";
        }
        lastFailureReason = normalized.isEmpty() ? "unknown" : normalized;
    }

    private static boolean isDestroyedGlResource(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && message.contains("destroyed GlResource");
    }
}
