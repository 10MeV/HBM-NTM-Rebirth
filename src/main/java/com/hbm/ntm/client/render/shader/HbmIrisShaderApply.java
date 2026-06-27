package com.hbm.ntm.client.render.shader;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.ShaderInstance;

/**
 * Guarded apply wrapper for Iris/Oculus ExtendedShader instances.
 */
public final class HbmIrisShaderApply {
    private HbmIrisShaderApply() {
    }

    public static boolean tryApply(ShaderInstance shader) {
        if (shader == null) {
            return false;
        }
        try {
            shader.apply();
            return true;
        } catch (IllegalStateException exception) {
            if (isDestroyedGlResource(exception)) {
                HbmIrisExtendedShaderAccess.invalidateShaderCache();
                HbmNtm.LOGGER.debug("HBM Iris/Oculus shader apply failed after destroyed GlResource; cache invalidated.");
            } else {
                HbmNtm.LOGGER.debug("HBM Iris/Oculus shader apply failed: {}", exception.toString());
            }
            return false;
        } catch (Throwable throwable) {
            HbmNtm.LOGGER.debug("HBM Iris/Oculus shader apply failed: {}", throwable.toString());
            return false;
        }
    }

    private static boolean isDestroyedGlResource(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null && message.contains("destroyed GlResource");
    }
}
