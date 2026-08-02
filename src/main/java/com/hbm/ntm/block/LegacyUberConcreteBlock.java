package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.ArrayList;
import java.util.List;

/** Exact state progression and debris placement contract from 1.7.10 BlockUberConcrete. */
public final class LegacyUberConcreteBlock extends Block {
    public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 15);

    public LegacyUberConcreteBlock(BlockBehaviour.Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(defaultBlockState().setValue(DAMAGE, 0));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int damage = state.getValue(DAMAGE);
        if (random.nextInt(damage + 1) > 0) return;

        if (damage < 15) {
            level.setBlock(pos, state.setValue(DAMAGE, damage + 1), Block.UPDATE_ALL);
            return;
        }

        BlockState debris = ModBlocks.legacyBlock("concrete_super_broken").get().defaultBlockState();
        if (level.getBlockState(pos.below()).isAir()) {
            level.setBlock(pos, debris, Block.UPDATE_ALL);
            return;
        }

        List<Direction> directions = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST));
        // RandomSource is not java.util.Random; this is the same Fisher-Yates shuffle the
        // legacy Collections.shuffle call used for its four horizontal candidates.
        for (int index = directions.size() - 1; index > 0; index--) {
            int swap = random.nextInt(index + 1);
            Direction value = directions.get(index);
            directions.set(index, directions.get(swap));
            directions.set(swap, value);
        }
        for (Direction direction : directions) {
            BlockPos target = pos.relative(direction);
            if (!level.getBlockState(target).isAir() || !level.getBlockState(target.below()).isAir()) continue;
            FallingBlockEntity falling = FallingBlockEntity.fall(level, target, debris);
            falling.time = 2;
            falling.dropItem = true;
            level.removeBlock(pos, false);
            return;
        }

        level.setBlock(pos, debris, Block.UPDATE_ALL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DAMAGE);
    }
}
