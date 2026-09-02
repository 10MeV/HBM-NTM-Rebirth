package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.blockentity.WatzPumpBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class WatzPumpRenderer extends LegacyVisibleMachineRenderer<WatzPumpBlockEntity> {
    public WatzPumpRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    protected int resolveModelLight(WatzPumpBlockEntity blockEntity, BlockState state,
            LegacyMachineDefinition definition, int packedLight) {
        return LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
    }
}
