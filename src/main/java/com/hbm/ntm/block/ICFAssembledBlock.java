package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ICFAssembledBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class ICFAssembledBlock extends Block implements EntityBlock {
    public static final BooleanProperty PORT = BooleanProperty.create("port");

    public ICFAssembledBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PORT, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ICFAssembledBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PORT);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ICFAssembledBlockEntity assembled) {
            assembled.invalidateController();
            assembled.restoreOriginalBlock();
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.ICF_BLOCK.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ICFAssembledBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (ICFAssembledBlockEntity) blockEntity);
    }
}
