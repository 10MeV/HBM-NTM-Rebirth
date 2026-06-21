package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.neutron.RBMKPanelBlockPlanner;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class RBMKPanelBlock extends BaseEntityBlock implements Toolable {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final RBMKPanelPlanner.PanelType panelType;

    public RBMKPanelBlock(BlockBehaviour.Properties properties, RBMKPanelPlanner.PanelType panelType) {
        super(properties);
        this.panelType = panelType;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public RBMKPanelPlanner.PanelType panelType() {
        return panelType;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.create(RBMKPanelBlockPlanner.miniPanelBounds(state.getValue(FACING).ordinal()));
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            RBMKPanelBlockEntity panel = resolvePanel(level, pos);
            if (panel == null) {
                return false;
            }
            if (panelType == RBMKPanelPlanner.PanelType.DISPLAY) {
                panel.rotateDisplay();
            } else {
                NetworkHooks.openScreen(serverPlayer, panel, panel.getBlockPos());
            }
        }
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ToolType tool = ToolType.getType(player.getItemInHand(hand));
        if (tool == ToolType.SCREWDRIVER) {
            return onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)
                    ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
        }
        RBMKPanelBlockEntity panel = level.isClientSide ? null : resolvePanel(level, pos);
        if (!level.isClientSide && panel != null) {
            BlockPos panelPos = panel.getBlockPos();
            BlockState panelState = panel.getBlockState();
            if (panelType == RBMKPanelPlanner.PanelType.TERMINAL && player instanceof ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, panel, panelPos);
                return InteractionResult.CONSUME;
            }
            if (panelType == RBMKPanelPlanner.PanelType.KEYPAD) {
                RBMKPanelBlockPlanner.KeypadHitPlan plan = RBMKPanelBlockPlanner.planKeypadHit(
                        panelState.getValue(FACING).ordinal(), hit.getDirection().ordinal(),
                        hit.getLocation().x - panelPos.getX(), hit.getLocation().y - panelPos.getY(),
                        hit.getLocation().z - panelPos.getZ(), player.isShiftKeyDown());
                if (plan.hitButton()) {
                    panel.clickKey(plan.keyIndex());
                    return InteractionResult.CONSUME;
                }
            } else if (panelType == RBMKPanelPlanner.PanelType.LEVER) {
                RBMKPanelBlockPlanner.LeverHitPlan plan = RBMKPanelBlockPlanner.planLeverHit(
                        panelState.getValue(FACING).ordinal(), hit.getDirection().ordinal(),
                        hit.getLocation().x - panelPos.getX(), hit.getLocation().z - panelPos.getZ(),
                        player.isShiftKeyDown());
                if (plan.hitLever()) {
                    panel.clickLever(plan.leverIndex());
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RBMKPanelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.RBMK_PANEL.get(), level.isClientSide
                ? RBMKPanelBlockEntity::clientTick : RBMKPanelBlockEntity::serverTick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    private static RBMKPanelBlockEntity resolvePanel(Level level, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(level, pos);
        return blockEntity instanceof RBMKPanelBlockEntity panel ? panel : null;
    }
}
