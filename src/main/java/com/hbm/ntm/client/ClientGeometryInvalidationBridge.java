package com.hbm.ntm.client;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class ClientGeometryInvalidationBridge {
    private ClientGeometryInvalidationBridge() {
    }

    public static void schedule(BlockPos pos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.render.HbmClientGeometryInvalidation.schedule(pos));
    }

    public static void scheduleWithNeighbors(BlockPos pos) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                com.hbm.ntm.client.render.HbmClientGeometryInvalidation.scheduleWithNeighbors(pos));
    }
}
