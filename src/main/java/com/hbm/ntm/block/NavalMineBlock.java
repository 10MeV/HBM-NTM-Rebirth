package com.hbm.ntm.block;

import com.hbm.config.ServerConfig;
import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.NavalMineBlockEntity;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.BlockAllocatorWater;
import com.hbm.ntm.explosion.vnt.standard.BlockProcessorStandard;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.ExplosionEffectWeapon;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
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
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class NavalMineBlock extends BaseEntityBlock implements IBomb, Toolable {
    private static boolean safeRemoval;

    private final double triggerRange;
    private final double triggerHeight;

    public NavalMineBlock(Properties properties, double triggerRange, double triggerHeight) {
        super(properties);
        this.triggerRange = triggerRange;
        this.triggerHeight = triggerHeight;
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
        return new NavalMineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, ModBlockEntities.NAVAL_MINE.get(), NavalMineBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
        if (level.isClientSide()) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            explode(level, pos);
        } else if (!canSurvive(state, level, pos)) {
            removeOrExplodeForSupport(level, pos);
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
        if (level == null || pos == null || level.isClientSide()) {
            return BombReturnCode.UNDEFINED;
        }
        if (!level.getBlockState(pos).is(this)) {
            return BombReturnCode.UNDEFINED;
        }

        safeRemoval = true;
        try {
            level.removeBlock(pos, false);
        } finally {
            safeRemoval = false;
        }

        new ExplosionVnt(level, pos.getX() + 5.0D, pos.getY() + 5.0D, pos.getZ() + 5.0D, 25.0F)
                .setBlockAllocator(new BlockAllocatorWater(32))
                .setBlockProcessor(new BlockProcessorStandard())
                .setEntityProcessor(new EntityProcessorCrossSmooth(0.5D, ServerConfig.MINE_NAVAL_DAMAGE.get())
                        .setupPiercing(5.0F, 0.2F))
                .setPlayerProcessor(new PlayerProcessorStandard())
                .setSFX(new ExplosionEffectWeapon(10, 1.0F, 0.5F))
                .explode();

        ExplosionLarge.spawnParticlesRadial(level, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, 30);
        ExplosionLarge.spawnRubble(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 5);
        if (isWaterAbove(level, pos)) {
            ExplosionLarge.spawnFoam(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 60);
        }
        return BombReturnCode.DETONATED;
    }

    private void removeOrExplodeForSupport(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }
        if (safeRemoval) {
            level.removeBlock(pos, false);
        } else {
            explode(level, pos);
        }
    }

    private boolean isWaterAbove(BlockGetter level, BlockPos pos) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (level.getFluidState(pos.offset(xOffset, 1, zOffset)).is(FluidTags.WATER)) {
                    return true;
                }
            }
        }
        return false;
    }
}
