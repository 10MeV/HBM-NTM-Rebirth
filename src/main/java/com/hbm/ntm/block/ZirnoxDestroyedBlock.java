package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ZirnoxDestroyedBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ZirnoxDestroyedBlock extends StaticLegacyMultiblockMachineBlock implements EntityBlock {
    public ZirnoxDestroyedBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZirnoxDestroyedBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.ZIRNOX_DESTROYED.get()) {
            return null;
        }
        return level.isClientSide ? null : (tickLevel, tickPos, tickState, blockEntity) ->
                ZirnoxDestroyedBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (ZirnoxDestroyedBlockEntity) blockEntity);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!(level instanceof ServerLevel serverLevel) || state.is(oldState.getBlock())) {
            return;
        }
        RandomSource random = serverLevel.random;
        if (random.nextInt(4) == 0) {
            ZirnoxDestroyedBlockEntity.spawnFlame(serverLevel, pos, random);
        }
        schedule(serverLevel, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);

        if (aboveState.isAir()) {
            if (random.nextInt(10) == 0) {
                level.setBlock(above, ModBlocks.GAS_MELTDOWN.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (isFoamExtinguisher(aboveState) && random.nextInt(25) == 0
                && MultiblockHelper.resolveCoreBlockEntity(level, pos) instanceof ZirnoxDestroyedBlockEntity destroyed) {
            destroyed.setOnFire(false);
        }

        if (level.getBlockState(above).isAir() && random.nextInt(10) == 0) {
            level.setBlock(above, ModBlocks.GAS_MELTDOWN.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        schedule(level, pos, random);
    }

    private static void schedule(Level level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, level.getBlockState(pos).getBlock(), 100 + random.nextInt(20));
    }

    private static boolean isFoamExtinguisher(BlockState state) {
        if (state.is(ModBlocks.BLOCK_FOAM.get())) {
            return true;
        }
        RegistryObject<? extends Block> foamLayer = ModBlocks.legacyBlock("foam_layer");
        return foamLayer != null && state.is(foamLayer.get());
    }
}
