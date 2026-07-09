package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.BigAssTankBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BigAssTankBlock extends FluidTankBlock {
    public static final BooleanProperty TILTED = BooleanProperty.create("tilted");

    public BigAssTankBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
        registerDefaultState(defaultBlockState().setValue(TILTED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (state.hasProperty(TILTED) && state.getValue(TILTED)) {
            return super.getRenderShape(state);
        }
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigAssTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.BIG_ASS_TANK.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                BigAssTankBlockEntity.serverTick(tickLevel, tickPos, tickState, (BigAssTankBlockEntity) blockEntity);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TILTED);
    }
}
