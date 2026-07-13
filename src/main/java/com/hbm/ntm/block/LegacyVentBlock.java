package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyVentBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LegacyVentBlock extends BaseEntityBlock {
    public LegacyVentBlock(Properties properties) { super(properties.noOcclusion()); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LegacyVentBlockEntity(pos, state); }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == ModBlockEntities.LEGACY_VENT.get()
                ? (tickLevel, tickPos, tickState, blockEntity) -> LegacyVentBlockEntity.serverTick(tickLevel, tickPos, tickState, (LegacyVentBlockEntity) blockEntity)
                : null;
    }
}
