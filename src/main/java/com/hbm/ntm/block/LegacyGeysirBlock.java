package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import com.hbm.ntm.blockentity.LegacyGeysirBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Shared block contract; registration supplies the source-specific top/side model. */
public class LegacyGeysirBlock extends BaseEntityBlock {
    public LegacyGeysirBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWERED, false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyGeysirBlockEntity(pos, state);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == ModBlockEntities.GEYSIR.get()
                ? (tickLevel, tickPos, tickState, blockEntity) -> LegacyGeysirBlockEntity.tick(
                        tickLevel, tickPos, tickState, (LegacyGeysirBlockEntity) blockEntity)
                : null;
    }

    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.is(ModBlocks.GEYSIR_NETHER.get())) {
            level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    pos.getX() + 0.5D, pos.getY() + 1.0625D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
        }
    }
}
