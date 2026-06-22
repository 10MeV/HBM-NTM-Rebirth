package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CondenserBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CondenserBlock extends net.minecraft.world.level.block.Block implements EntityBlock {
    public CondenserBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CondenserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.CONDENSER.get() || level.isClientSide) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                CondenserBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (CondenserBlockEntity) blockEntity);
    }
}
