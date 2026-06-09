package com.hbm.ntm.block;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.explosion.ExplosionThermo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyExplosiveBarrelBlock extends Block implements ChainExplodable, ShotDetonatableBlock {
    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final Kind kind;

    public LegacyExplosiveBarrelBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 100, true));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide && shouldIgnite(level, pos)) {
            spawnPrimed(level, pos);
        }
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
            @Nullable LivingEntity igniter) {
        if (!level.isClientSide && shouldIgnite(level, pos)) {
            spawnPrimed(level, pos);
        }
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return kind.flammability() > 0;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return kind.flammability();
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return kind.encouragement();
    }

    @Override
    public boolean detonateFromShot(Level level, BlockPos pos, BlockState state, @Nullable Entity shooter) {
        if (!kind.detonatesFromShot()) {
            return false;
        }
        if (!level.isClientSide) {
            level.removeBlock(pos, false);
            explodeEntity(level, Vec3.atLowerCornerOf(pos), shooter);
        }
        return true;
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (level.isClientSide) {
            return;
        }

        BlockPos pos = BlockPos.containing(Math.floor(position.x), Math.floor(position.y), Math.floor(position.z));
        if (kind == Kind.LOX) {
            level.explode(source, position.x, position.y, position.z, 1.0F, false, Level.ExplosionInteraction.NONE);
            ExplosionThermo.freezer(level, pos.getX(), pos.getY(), pos.getZ(), 7);
            return;
        }

        level.explode(source, position.x, position.y, position.z, 2.5F, true, Level.ExplosionInteraction.BLOCK);
    }

    private boolean shouldIgnite(Level level, BlockPos pos) {
        if (kind.flammability() <= 0) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(Blocks.FIRE)) {
                return true;
            }
        }
        return false;
    }

    private void spawnPrimed(Level level, BlockPos pos) {
        level.removeBlock(pos, false);
        level.gameEvent(null, GameEvent.PRIME_FUSE, pos);
        level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 100, true));
    }

    public enum Kind {
        RED(true, 2, 15),
        PINK(true, 2, 15),
        LOX(false, 0, 0);

        private final boolean detonatesFromShot;
        private final int encouragement;
        private final int flammability;

        Kind(boolean detonatesFromShot, int encouragement, int flammability) {
            this.detonatesFromShot = detonatesFromShot;
            this.encouragement = encouragement;
            this.flammability = flammability;
        }

        public boolean detonatesFromShot() {
            return detonatesFromShot;
        }

        public int encouragement() {
            return encouragement;
        }

        public int flammability() {
            return flammability;
        }
    }
}
