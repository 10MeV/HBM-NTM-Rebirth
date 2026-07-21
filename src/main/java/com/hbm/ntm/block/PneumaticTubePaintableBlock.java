package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.PaintableDuctBlockEntity;
import com.hbm.ntm.blockentity.PneumaticTubePaintableBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PneumaticTubePaintableBlock extends PneumaticTubeBlock {
    public static final BooleanProperty OVERLAY = BooleanProperty.create("overlay");

    public PneumaticTubePaintableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OVERLAY, true));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PneumaticTubePaintableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PNEUMATIC_TUBE_PAINTABLE.get(),
                PneumaticTubePaintableBlockEntity::serverTick);
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
        if (tool == ToolType.HAND_DRILL && level.getBlockEntity(pos) instanceof PaintableDuctBlockEntity paintable
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
        return super.onToolUse(level, player, pos, side, hit, tool);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OVERLAY);
    }
}
