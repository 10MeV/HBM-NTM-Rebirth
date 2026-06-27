package com.hbm.ntm.client.renderer;

import com.hbm.ntm.blockentity.WatzPumpBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class WatzPumpRenderer extends LegacyVisibleMachineRenderer<WatzPumpBlockEntity> {
    public WatzPumpRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }
}
