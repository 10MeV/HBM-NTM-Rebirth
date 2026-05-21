package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockExtents;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AssemblyMachineBlock extends HorizontalMachineBlock implements EntityBlock, MultiblockCoreBlock {
    /**
     * Legacy MachineAssemblyMachine dimensions are XR ordered as {U,D,N,S,W,E} = {2,0,1,1,1,1}.
     * The shared helper stores axis extents as {+X,-X,+Y,-Y,+Z,-Z}.
     */
    public static final MultiblockExtents EXTENTS = MultiblockExtents.ofLegacy(new int[] { 1, 1, 2, 0, 1, 1 });
    private static final VoxelShape SHAPE = Shapes.box(-1.5D, 0.0D, -1.5D, 2.5D, 2.0D, 2.5D);
    private static final ThreadLocal<Boolean> RELOCATING = ThreadLocal.withInitial(() -> false);

    public AssemblyMachineBlock(Properties properties) {
        super(properties, false);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos corePos = context.getClickedPos().relative(facing);
        return canPlaceMultiblock(context.getLevel(), corePos, context.getClickedPos())
                ? defaultBlockState().setValue(FACING, facing)
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            BlockPos corePos = pos.relative(state.getValue(FACING));
            if (!corePos.equals(pos)) {
                RELOCATING.set(true);
                level.removeBlock(pos, false);
                RELOCATING.set(false);
                level.setBlock(corePos, state, Block.UPDATE_ALL);
                MultiblockHelper.fillUp(level, corePos, EXTENTS);
            } else {
                MultiblockHelper.fillUp(level, pos, EXTENTS);
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyMachineBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ASSEMBLY_MACHINE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                AssemblyMachineBlockEntity.clientTick(tickLevel, tickPos, tickState, (AssemblyMachineBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                AssemblyMachineBlockEntity.serverTick(tickLevel, tickPos, tickState, (AssemblyMachineBlockEntity) blockEntity);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!RELOCATING.get() && !state.is(newState.getBlock()) && !level.isClientSide) {
            MultiblockHelper.removeAll(level, pos, EXTENTS);
            if (level.getBlockEntity(pos) instanceof AssemblyMachineBlockEntity assembler) {
                for (ItemStack stack : assembler.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static boolean canPlaceMultiblock(Level level, BlockPos corePos, BlockPos placedPos) {
        for (BlockPos offset : EXTENTS.offsets()) {
            BlockPos target = corePos.offset(offset);
            if (!target.equals(placedPos) && !level.getBlockState(target).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }
}
