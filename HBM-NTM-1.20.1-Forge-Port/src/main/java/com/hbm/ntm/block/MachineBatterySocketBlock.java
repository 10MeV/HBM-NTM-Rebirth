package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.MachineBatterySocketBlockEntity;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class MachineBatterySocketBlock extends LegacyOffsetMultiblockBlock implements EntityBlock {
    public MachineBatterySocketBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineBatterySocketBlockEntity(pos, state);
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofOffsets(socketOffsets(state.getValue(FACING)))
                .withProxyPredicate(offset -> !offset.equals(BlockPos.ZERO));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MachineBatterySocketBlockEntity socket) {
            NetworkHooks.openScreen(serverPlayer, socket, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.MACHINE_BATTERY_SOCKET.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                MachineBatterySocketBlockEntity.clientTick(tickLevel, tickPos, tickState, (MachineBatterySocketBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                MachineBatterySocketBlockEntity.serverTick(tickLevel, tickPos, tickState, (MachineBatterySocketBlockEntity) blockEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MachineBatterySocketBlockEntity socket ? socket.getComparatorPower() : 0;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            HbmEnergyNodespace.markNodeAndNeighborsChanged(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof MachineBatterySocketBlockEntity socket) {
                ItemStack stack = socket.removeBatteryForDrop();
                if (!stack.isEmpty()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static Set<BlockPos> socketOffsets(Direction facing) {
        BlockPos behind = LegacyMultiblockLayout.behind(facing);
        BlockPos clockwise = LegacyMultiblockLayout.clockwise(facing);
        Set<BlockPos> offsets = new LinkedHashSet<>();
        offsets.add(BlockPos.ZERO);
        offsets.add(behind);
        offsets.add(clockwise);
        offsets.add(behind.offset(clockwise));
        return offsets;
    }

    private static VoxelShape shapeFor(Direction facing) {
        return LegacyMultiblockLayout.ofOffsets(socketOffsets(facing)).shape(2.0D);
    }
}
