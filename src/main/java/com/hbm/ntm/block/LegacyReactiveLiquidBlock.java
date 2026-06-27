package com.hbm.ntm.block;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModParticleTypes;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("deprecation")
public class LegacyReactiveLiquidBlock extends LiquidBlock {
    private static final Vec3 LEGACY_STUCK_SPEED = new Vec3(0.25D, 0.05D, 0.25D);

    private final Kind kind;

    public LegacyReactiveLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties, Kind kind) {
        super(fluid, properties);
        this.kind = kind;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        switch (kind) {
            case ACID -> {
                entity.makeStuckInBlock(state, LEGACY_STUCK_SPEED);
                if (!level.isClientSide) {
                    EntityDamageUtil.attackEntityFromNt(entity,
                            ModDamageSources.source(level, ModDamageSources.ACID), 10_000.0F);
                }
            }
            case TOXIC, SCHRABIDIC -> {
                entity.makeStuckInBlock(state, LEGACY_STUCK_SPEED);
                if (!level.isClientSide && entity instanceof LivingEntity living) {
                    RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 1.0F);
                }
            }
            case MUD -> {
                entity.makeStuckInBlock(state, LEGACY_STUCK_SPEED);
                if (!level.isClientSide
                        && (!(entity instanceof Player player) || !ArmorUtil.checkForHazmat(player))) {
                    EntityDamageUtil.attackEntityFromNt(entity,
                            ModDamageSources.source(level, ModDamageSources.MUD_POISONING), 8.0F);
                }
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        reactToNeighbors(level, pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        reactToNeighbors(level, pos, state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide() && kind.reactsToForeignLiquid() && isForeignLiquid(state, neighborState)) {
            return ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        reactToNeighbors(level, pos, state);
        if (kind == Kind.MUD) {
            erodeNeighbors(level, pos, random);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (kind != Kind.SCHRABIDIC) {
            return;
        }
        double x = pos.getX() + 0.5D + random.nextDouble() * 2.0D - 1.0D;
        double y = pos.getY() + 0.5D + random.nextDouble() * 2.0D - 1.0D;
        double z = pos.getZ() + 0.5D + random.nextDouble() * 2.0D - 1.0D;
        level.addParticle(ModParticleTypes.SCHRAB_FOG.get(), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private void reactToNeighbors(Level level, BlockPos pos, BlockState selfState) {
        if (level.isClientSide) {
            return;
        }
        if (kind == Kind.ACID) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                if (!level.isOutsideBuildHeight(neighborPos)) {
                    BlockState neighbor = level.getBlockState(neighborPos);
                    if (neighbor.getBlock() != this && !neighbor.isAir()) {
                        level.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
            return;
        }
        if (kind == Kind.MUD) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                if (level.isOutsideBuildHeight(neighborPos)) {
                    continue;
                }
                BlockState neighbor = level.getBlockState(neighborPos);
                if (neighbor.getBlock() != this && neighbor.getBlock() instanceof LiquidBlock) {
                    level.setBlock(neighborPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            return;
        }
        if (kind.reactsToForeignLiquid()) {
            for (Direction direction : Direction.values()) {
                if (isForeignLiquid(selfState, level.getBlockState(pos.relative(direction)))) {
                    level.setBlock(pos, ModBlocks.SELLAFIELD_SLAKED.get().defaultBlockState(), Block.UPDATE_ALL);
                    return;
                }
            }
        }
    }

    private void erodeNeighbors(Level level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (!level.isOutsideBuildHeight(neighborPos)) {
                erodeNeighbor(level, neighborPos, random);
            }
        }
    }

    private void erodeNeighbor(Level level, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() == this) {
            return;
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.STONE_BRICK_STAIRS)
                || state.is(Blocks.STONE_SLAB)) {
            if (random.nextInt(20) == 0) {
                level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (state.is(Blocks.COBBLESTONE)) {
            if (random.nextInt(15) == 0) {
                level.setBlock(pos, Blocks.GRAVEL.defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (state.is(Blocks.SANDSTONE)) {
            if (random.nextInt(5) == 0) {
                level.setBlock(pos, Blocks.SAND.defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (state.is(Blocks.TERRACOTTA) || state.getBlock().getDescriptionId().contains("terracotta")) {
            if (random.nextInt(10) == 0) {
                level.setBlock(pos, Blocks.CLAY.defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (state.ignitedByLava() || state.is(Blocks.CACTUS) || state.is(Blocks.CAKE)
                || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE) || state.is(Blocks.GLASS) || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.COBWEB) || state.getExplosionResistance(level, pos, null) < 1.2F) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static boolean isForeignLiquid(BlockState selfState, BlockState neighborState) {
        return neighborState.getBlock() instanceof LiquidBlock && neighborState.getBlock() != selfState.getBlock();
    }

    public enum Kind {
        ACID(false),
        TOXIC(true),
        SCHRABIDIC(true),
        MUD(false);

        private final boolean reactsToForeignLiquid;

        Kind(boolean reactsToForeignLiquid) {
            this.reactsToForeignLiquid = reactsToForeignLiquid;
        }

        private boolean reactsToForeignLiquid() {
            return reactsToForeignLiquid;
        }
    }
}
