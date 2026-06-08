package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class CableDiodeBlock extends BaseEntityBlock implements com.hbm.ntm.energy.HbmEnergyConnectorBlock, Toolable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public CableDiodeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Direction facing = Direction.orderedByNearest(placer)[0].getOpposite();
        level.setBlock(pos, state.setValue(FACING, facing), Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableDiodeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, com.hbm.ntm.registry.ModBlockEntities.CABLE_DIODE.get(),
                CableDiodeBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean canConnectEnergy(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return side != null;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (!(level.getBlockEntity(pos) instanceof CableDiodeBlockEntity diode)) {
            return false;
        }
        if (!level.isClientSide) {
            switch (tool) {
                case SCREWDRIVER -> diode.increaseLevel();
                case HAND_DRILL -> diode.decreaseLevel();
                case DEFUSER -> diode.cyclePriority();
                default -> {
                    return false;
                }
            }
        }
        return tool == ToolType.SCREWDRIVER || tool == ToolType.HAND_DRILL || tool == ToolType.DEFUSER;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.getItemInHand(hand).isEmpty()) {
            ToolType tool = player.isShiftKeyDown() ? ToolType.HAND_DRILL : ToolType.SCREWDRIVER;
            if (onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("block.hbm_ntm_rebirth.cable_diode.desc1"));
        tooltip.add(Component.translatable("block.hbm_ntm_rebirth.cable_diode.desc2"));
        tooltip.add(Component.translatable("block.hbm_ntm_rebirth.cable_diode.desc3"));
        tooltip.add(Component.translatable("block.hbm_ntm_rebirth.cable_diode.desc4"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
