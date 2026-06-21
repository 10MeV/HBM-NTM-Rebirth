package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.MiniRtgBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class MiniRtgBlock extends HorizontalMachineBlock implements EntityBlock {
    private final Kind kind;

    public MiniRtgBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MiniRtgBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.MINI_RTG.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                MiniRtgBlockEntity.serverTick(tickLevel, tickPos, tickState, (MiniRtgBlockEntity) blockEntity);
    }

    public enum Kind {
        CELL(700L, 1_400L),
        POLONIUM(2_500L, 50_000L);

        private final long output;
        private final long maxPower;

        Kind(long output, long maxPower) {
            this.output = output;
            this.maxPower = maxPower;
        }

        public long output() {
            return output;
        }

        public long maxPower() {
            return maxPower;
        }
    }
}
