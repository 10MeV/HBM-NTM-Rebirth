package com.hbm.ntm.block;

import com.hbm.ntm.entity.logic.NukeExplosionMk3Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class FieldDisturberBlock extends Block {
    private static final int TICK_RATE = 10;
    private static final int ANTI_TELEPORT_TICKS = 100;

    public FieldDisturberBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock()) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, TICK_RATE);
        NukeExplosionMk3Entity.registerAntiTeleportEntry(level, pos.getX(), pos.getY(), pos.getZ(),
                ANTI_TELEPORT_TICKS);
    }
}
