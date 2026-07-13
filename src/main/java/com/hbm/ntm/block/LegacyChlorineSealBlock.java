package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyChlorineSealBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LegacyChlorineSealBlock extends BaseEntityBlock {
    public LegacyChlorineSealBlock(Properties properties) { super(properties); }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LegacyChlorineSealBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == ModBlockEntities.CHLORINE_SEAL.get()
                ? (tickLevel, tickPos, tickState, blockEntity) -> LegacyChlorineSealBlockEntity.serverTick(tickLevel, tickPos, tickState, (LegacyChlorineSealBlockEntity) blockEntity)
                : null;
    }
}
