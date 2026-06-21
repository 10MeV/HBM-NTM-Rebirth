package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachinePartRenderMode;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjRenderContext;

final class LegacyMachinePartRenderContexts {
    private LegacyMachinePartRenderContexts() {
    }

    static ObjRenderContext apply(ObjRenderContext context, LegacyMachinePartRenderProperties properties) {
        if (properties == null) {
            return context;
        }
        ObjRenderContext resolved = context.withRenderMode(renderMode(properties.mode()));
        if (properties.hasColor()) {
            resolved = resolved.withColor(properties.color(), properties.alpha());
        } else if (properties.alpha() < 255) {
            resolved = resolved.withAlpha(properties.alpha());
        }
        if (properties.fullBright()) {
            resolved = resolved.fullBright();
        }
        return resolved;
    }

    static boolean translucent(LegacyMachinePartRenderProperties properties) {
        return properties != null && properties.translucent();
    }

    static LegacyTexturedRenderMode renderMode(LegacyMachinePartRenderMode mode) {
        return switch (mode) {
            case CUTOUT_NO_CULL -> LegacyTexturedRenderMode.CUTOUT_NO_CULL;
            case CUTOUT_CULL -> LegacyTexturedRenderMode.CUTOUT_CULL;
            case TRANSLUCENT_NO_DEPTH_WRITE -> LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE;
            case TRANSLUCENT_DEPTH_WRITE -> LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE;
            case ADDITIVE_NO_DEPTH_WRITE -> LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE;
            case ADDITIVE_DEPTH_WRITE -> LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE;
        };
    }
}
