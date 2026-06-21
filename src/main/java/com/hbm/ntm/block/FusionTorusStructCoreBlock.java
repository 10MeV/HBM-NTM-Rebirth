package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FusionTorusStructCoreBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FusionTorusStructCoreBlock extends Block implements EntityBlock {
    public FusionTorusStructCoreBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FusionTorusStructCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.FUSION_TORUS_STRUCT_CORE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                FusionTorusStructCoreBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (FusionTorusStructCoreBlockEntity) blockEntity);
    }
}
