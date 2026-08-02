package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Bomb;
import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.ExplosionNT;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Exact 1.7.10 {@code DetMiner}: instant, no-harm, all-drop mining explosion. */
public final class DetMinerBlock extends Block implements Bomb, ChainExplodable {
    public DetMinerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            Entity source = explosion.getIndirectSourceEntity();
            LivingEntity owner = source instanceof LivingEntity ? (LivingEntity) source : null;
            level.addFreshEntity(LegacyPrimedExplosiveEntity.createFixedFuse(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 0, false, owner));
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return BombReturnCode.DETONATED;
        }
        level.removeBlock(pos, false);
        explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, null);
        return BombReturnCode.DETONATED;
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            BlockPos pos = BlockPos.containing(position);
            level.removeBlock(pos, false);
            explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, source);
        }
    }

    private static void explodeAt(Level level, double x, double y, double z, @Nullable Entity source) {
        new ExplosionNT(level, source, x, y, z, 4.0F)
                .addAttrib(ExplosionNT.ExAttrib.ALLDROP, ExplosionNT.ExAttrib.NOHURT)
                .explode();
        ExplosionLarge.spawnParticles(level, x, y, z, 30);
    }
}
