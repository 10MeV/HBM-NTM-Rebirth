package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HexafluorideTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HexafluorideTankBlock extends HorizontalMachineBlock implements EntityBlock {
    private final Kind kind;

    public HexafluorideTankBlock(Properties properties, Kind kind) {
        super(properties, false);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HexafluorideTankBlockEntity(pos, state);
    }

    public enum Kind {
        UF6,
        PUF6
    }
}
