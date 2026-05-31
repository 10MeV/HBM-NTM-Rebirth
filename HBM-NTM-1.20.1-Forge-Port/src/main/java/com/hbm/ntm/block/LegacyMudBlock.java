package com.hbm.ntm.block;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LegacyMudBlock extends Block {
    private final Random legacyRandom = new Random();

    public LegacyMudBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));
        if (entity instanceof Player player && ArmorUtil.checkForHazmat(player)) {
            return;
        }
        EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.source(level, ModDamageSources.MUD_POISONING), 8.0F);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        reactToBlocks(level, pos);
        reactToBlocks2(level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        reactToBlocks(level, pos);
    }

    private void reactToBlocks(Level level, BlockPos pos) {
        for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (neighbor.distManhattan(pos) != 1) {
                continue;
            }
            BlockState state = level.getBlockState(neighbor);
            if (state.getBlock() != this && !state.getFluidState().isEmpty()) {
                level.setBlock(neighbor, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private void reactToBlocks2(Level level, BlockPos pos) {
        for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (neighbor.distManhattan(pos) != 1) {
                continue;
            }
            erodeNeighbor(level, neighbor.immutable());
        }
    }

    private void erodeNeighbor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == this) {
            return;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.STONE_BRICK_STAIRS)
                || state.is(Blocks.STONE_SLAB)) {
            if (legacyRandom.nextInt(20) == 0) {
                level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        } else if (state.is(Blocks.COBBLESTONE)) {
            if (legacyRandom.nextInt(15) == 0) {
                level.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), 3);
            }
        } else if (state.is(Blocks.SANDSTONE)) {
            if (legacyRandom.nextInt(5) == 0) {
                level.setBlock(pos, Blocks.SAND.defaultBlockState(), 3);
            }
        } else if (state.is(Blocks.TERRACOTTA) || state.getBlock().getDescriptionId().contains("terracotta")) {
            if (legacyRandom.nextInt(10) == 0) {
                level.setBlock(pos, Blocks.CLAY.defaultBlockState(), 3);
            }
        } else if (state.ignitedByLava() || state.is(Blocks.CACTUS) || state.is(Blocks.CAKE)
                || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE) || state.is(Blocks.GLASS) || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.COBWEB) || state.getExplosionResistance(level, pos, null) < 1.2F) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
