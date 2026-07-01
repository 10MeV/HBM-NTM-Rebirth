package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ProcessingMachineBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class CrystallizerBlock extends ProcessingMachineBlock {
    public CrystallizerBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition, ProcessingMachineBlockEntity.Kind.CRYSTALLIZER);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }
}
