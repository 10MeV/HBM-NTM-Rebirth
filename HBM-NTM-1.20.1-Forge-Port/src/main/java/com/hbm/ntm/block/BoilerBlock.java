package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.BoilerBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BoilerBlock extends HorizontalMachineBlock implements EntityBlock {
    public BoilerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoilerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof BoilerBlockEntity boiler) {
                        BoilerBlockEntity.serverTick(tickLevel, tickPos, tickState, boiler);
                    }
                };
    }
}
