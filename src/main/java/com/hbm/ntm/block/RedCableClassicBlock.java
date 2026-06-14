package com.hbm.ntm.block;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class RedCableClassicBlock extends RedCableBlock {
    public RedCableClassicBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean usesBlockEntityRenderer(BlockState state) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
