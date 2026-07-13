package com.hbm.ntm.block;

import com.hbm.config.ServerConfig;
import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.LandmineBlockEntity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorStandard;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectWeapon;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.handler.radiation.ChunkRadiationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class LandmineBlock extends BaseEntityBlock implements IBomb, Toolable {
    private static boolean safeRemoval;

    private final Kind kind;
    private final double triggerRange;
    private final double triggerHeight;

    public LandmineBlock(Properties properties, Kind kind, double triggerRange, double triggerHeight) {
        super(properties);
        this.kind = kind;
        this.triggerRange = triggerRange;
        this.triggerHeight = triggerHeight;
    }

    public Kind kind() {
        return kind;
    }

    public double triggerRange() {
        return triggerRange;
    }

    public double triggerHeight() {
        return triggerHeight;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LandmineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.LANDMINE.get(), LandmineBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        return switch (kind) {
            case AP, SHRAP -> box(5, 0, 5, 11, 1, 11);
            case HE -> box(4, 0, 4, 12, 2, 12);
            case FAT -> box(5, 0, 4, 11, 6, 12);
        };
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            net.minecraft.world.phys.shapes.CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP) || belowState.getBlock() instanceof FenceBlock;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) {
            if (level instanceof Level realLevel && !realLevel.isClientSide()) {
                removeOrExplodeForSupport(realLevel, pos);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide()) {
            if (level.hasNeighborSignal(pos)) {
                explode(level, pos);
            } else if (!canSurvive(state, level, pos)) {
                removeOrExplodeForSupport(level, pos);
            }
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && state.is(this) && !safeRemoval) {
            explode(level, pos);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.DEFUSER) {
            return false;
        }
        if (!level.isClientSide()) {
            safeRemoval = true;
            try {
                level.removeBlock(pos, false);
            } finally {
                safeRemoval = false;
            }
            popResource(level, pos, new ItemStack(this));
        }
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return Collections.emptyList();
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level == null || pos == null || level.isClientSide() || !level.getBlockState(pos).is(this)) {
            return BombReturnCode.UNDEFINED;
        }
        safeRemoval = true;
        try {
            level.removeBlock(pos, false);
        } finally {
            safeRemoval = false;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        switch (kind) {
            case AP -> new ExplosionVnt(level, x, y, z, 3.0F)
                    .setEntityProcessor(new EntityProcessorCrossSmooth(0.5D, ServerConfig.MINE_AP_DAMAGE.get())
                            .setupPiercing(5.0F, 0.2F))
                    .setPlayerProcessor(new PlayerProcessorStandard())
                    .setSFX(new ExplosionEffectWeapon(5, 1.0F, 0.5F))
                    .explode();
            case HE -> new ExplosionVnt(level, x, y, z, 4.0F)
                    .setBlockAllocator(new BlockAllocatorStandard())
                    .setBlockProcessor(new BlockProcessorStandard())
                    .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, ServerConfig.MINE_HE_DAMAGE.get())
                            .setupPiercing(15.0F, 0.2F))
                    .setPlayerProcessor(new PlayerProcessorStandard())
                    .setSFX(new ExplosionEffectWeapon(15, 3.5F, 1.25F))
                    .explode();
            case SHRAP -> {
                new ExplosionVnt(level, x, y, z, 3.0F)
                        .setEntityProcessor(new EntityProcessorCrossSmooth(0.5D, ServerConfig.MINE_SHRAP_DAMAGE.get()))
                        .setPlayerProcessor(new PlayerProcessorStandard())
                        .setSFX(new ExplosionEffectWeapon(5, 1.0F, 0.5F))
                        .explode();
                ExplosionLarge.spawnShrapnelShower(level, x, y, z, 0.0D, 1.0D, 0.0D, 45, 0.2D);
                ExplosionLarge.spawnShrapnels(level, x, y, z, 5);
            }
            case FAT -> {
                new ExplosionVnt(level, x, y, z, 10.0F)
                        .setBlockAllocator(new BlockAllocatorStandard(64))
                        .setBlockProcessor(new BlockProcessorStandard())
                        .setEntityProcessor(new EntityProcessorCrossSmooth(2.0D, ServerConfig.MINE_NUKE_DAMAGE.get())
                                .withRangeMod(1.5F))
                        .setPlayerProcessor(new PlayerProcessorStandard())
                        .explode();
                addLegacyFatMineRadiation(level, pos);
                ParticleUtil.spawnRbmkMush(level, x, y, z, 5.0F);
                level.playSound(null, x, y, z, ModSounds.WEAPON_MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 25.0F, 0.9F);
            }
        }
        return BombReturnCode.DETONATED;
    }

    private static void addLegacyFatMineRadiation(Level level, BlockPos pos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                int distance = Math.abs(x) + Math.abs(z);
                if (distance < 4) {
                    ChunkRadiationManager.proxy.incrementRad(level, pos.offset(x * 16, 0, z * 16),
                            75.0F / (distance + 1));
                }
            }
        }
    }

    private void removeOrExplodeForSupport(Level level, BlockPos pos) {
        if (level.getBlockState(pos).is(this)) {
            if (safeRemoval) {
                level.removeBlock(pos, false);
            } else {
                explode(level, pos);
            }
        }
    }

    public enum Kind {
        AP,
        HE,
        SHRAP,
        FAT
    }
}
