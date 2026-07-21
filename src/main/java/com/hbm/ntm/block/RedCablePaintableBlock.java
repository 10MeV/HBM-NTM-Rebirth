package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.HbmEnergyNodeBlockEntity;
import com.hbm.ntm.blockentity.PaintableDuctBlockEntity;
import com.hbm.ntm.blockentity.RedCablePaintableBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Source: BlockCablePaintable / TileEntityCablePaintable.
 *
 * <p>The legacy block is a full cube rather than a cable-arm model.  Its
 * independent paint and port-overlay state belongs to the block entity so the
 * selected paint can retain the painted block's actual model and texture.</p>
 */
@SuppressWarnings("deprecation")
public class RedCablePaintableBlock extends HbmEnergyNodeBlock implements Toolable {
    public static final BooleanProperty OVERLAY = BooleanProperty.create("overlay");
    private static final VoxelShape FULL_BLOCK_SHAPE = Shapes.block();

    public RedCablePaintableBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OVERLAY, true));
    }

    public boolean usesBlockEntityRenderer(BlockState state) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedCablePaintableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.RED_CABLE_PAINTABLE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                HbmEnergyNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (RedCablePaintableBlockEntity) blockEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_BLOCK_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return FULL_BLOCK_SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof BlockItem blockItem
                && level.getBlockEntity(pos) instanceof PaintableDuctBlockEntity paintable
                && !paintable.hasPaintedState()
                && FluidDuctPaintableBlock.canPaintWith(level, pos, blockItem.getBlock(), this)) {
            if (!level.isClientSide) {
                paintable.setPaintedState(FluidDuctPaintableBlock.paintStateFromStack(blockItem, held),
                        FluidDuctPaintableBlock.legacyPaintMeta(held));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        ToolType tool = ToolType.getType(held);
        if (tool != null && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), tool)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool == ToolType.SCREWDRIVER
                && level.getBlockEntity(pos) instanceof PaintableDuctBlockEntity paintable
                && paintable.hasPaintedState()) {
            if (!level.isClientSide) {
                paintable.setPaintedState(null, 0);
            }
            return true;
        }
        if (tool == ToolType.DEFUSER) {
            if (!level.isClientSide && level.getBlockState(pos).is(this)) {
                level.setBlock(pos, level.getBlockState(pos).cycle(OVERLAY), Block.UPDATE_ALL);
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        LegacyStandardInfoTooltip.append(tooltip, "red_cable_paintable");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OVERLAY);
    }
}
