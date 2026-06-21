package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.FluidDuctPaintableBlockEntity;
import com.hbm.ntm.blockentity.PaintableDuctBlockEntity;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidDuctPaintableBlock extends FluidPipeBlock implements Toolable {
    public static final BooleanProperty OVERLAY = BooleanProperty.create("overlay");
    private static final VoxelShape FULL_BLOCK_SHAPE = Shapes.block();

    public FluidDuctPaintableBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OVERLAY, true));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return (state == null ? defaultBlockState() : state).setValue(OVERLAY, true);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDuctPaintableBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof BlockItem blockItem
                && level.getBlockEntity(pos) instanceof PaintableDuctBlockEntity paintable
                && !paintable.hasPaintedState()
                && canPaintWith(level, pos, blockItem.getBlock(), this)) {
            if (!level.isClientSide) {
                paintable.setPaintedState(blockItem.getBlock().defaultBlockState(), legacyPaintMeta(held));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.isEmpty()
                && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(),
                        player.isShiftKeyDown() ? ToolType.DEFUSER : ToolType.SCREWDRIVER)) {
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

    public static boolean canPaintWith(BlockGetter level, BlockPos pos, Block paint, Block target) {
        if (paint == target || paint == Blocks.AIR || paint == Blocks.GRASS_BLOCK) {
            return false;
        }
        BlockState state = paint.defaultBlockState();
        return state.getRenderShape() == RenderShape.MODEL && state.isSolidRender(level, pos);
    }

    public static int legacyPaintMeta(ItemStack stack) {
        if (stack.getItem() instanceof FluidDuctVariantBlockItem duct) {
            return duct.getLegacyMetadata(stack) & 15;
        }
        if (stack.getItem() instanceof LegacyStateBlockItem stateItem) {
            return stateItem.getVariant(stack) & 15;
        }
        return stack.getDamageValue() & 15;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OVERLAY);
    }
}
