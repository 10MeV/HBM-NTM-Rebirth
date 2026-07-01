package com.hbm.ntm.block;

import com.hbm.ntm.config.HbmClientConfig;
import net.minecraft.world.level.block.RenderShape;

public final class LegacyMachineRenderShapes {
    private LegacyMachineRenderShapes() {
    }

    public static RenderShape chunkBakedStaticOrEntity() {
        return HbmClientConfig.chunkBakedMachineStatics()
                ? RenderShape.MODEL
                : RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public static boolean renderChunkBakedStaticsInBer() {
        return !HbmClientConfig.chunkBakedMachineStatics();
    }
}
