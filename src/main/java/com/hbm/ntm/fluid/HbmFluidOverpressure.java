package com.hbm.ntm.fluid;

import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HbmFluidOverpressure {
    private HbmFluidOverpressure() {
    }

    public static boolean damageReceiver(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return false;
        }
        return damageReceiver(blockEntity.getLevel(), blockEntity.getBlockPos(), blockEntity);
    }

    public static boolean damageReceiver(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        return damageReceiver(level, pos, level.getBlockEntity(pos));
    }

    public static int damageReceivers(Collection<? extends BlockEntity> receivers) {
        if (receivers == null || receivers.isEmpty()) {
            return 0;
        }
        int damaged = 0;
        for (BlockEntity receiver : receivers) {
            if (damageReceiver(receiver)) {
                damaged++;
            }
        }
        return damaged;
    }

    public static int damagePipeNodes(Level level, Collection<HbmFluidNode> nodes) {
        if (nodes == null) {
            return 0;
        }
        return damagePipeNodes(level, nodes, Math.min(nodes.size() / 5, 100));
    }

    public static int damagePipeNodes(Level level, Collection<HbmFluidNode> nodes, int maxNodes) {
        if (level == null || level.isClientSide || nodes == null || nodes.isEmpty() || maxNodes <= 0) {
            return 0;
        }
        int damaged = 0;
        for (HbmFluidNode node : nodes) {
            if (node == null || damaged >= maxNodes) {
                continue;
            }
            boolean nodeDamaged = false;
            for (BlockPos pos : node.getPositions()) {
                if (level.getBlockEntity(pos) != null) {
                    level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
                    nodeDamaged = true;
                }
            }
            if (nodeDamaged) {
                damaged++;
            }
        }
        return damaged;
    }

    private static boolean damageReceiver(Level level, BlockPos pos, BlockEntity blockEntity) {
        if (level == null || pos == null || level.isClientSide) {
            return false;
        }
        if (blockEntity instanceof HbmFluidOverpressurable overpressurable) {
            overpressurable.explodeFromFluidOverpressure(level, pos);
            return true;
        }
        if (level.isEmptyBlock(pos)) {
            return false;
        }
        level.removeBlock(pos, false);
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5.0F,
                Level.ExplosionInteraction.NONE);
        return true;
    }
}
