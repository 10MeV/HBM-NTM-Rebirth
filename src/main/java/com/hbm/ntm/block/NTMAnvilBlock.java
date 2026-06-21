package com.hbm.ntm.block;

import com.hbm.ntm.menu.AnvilMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class NTMAnvilBlock extends FallingBlock {
    public static final int TIER_IRON = 1;
    public static final int TIER_STEEL = 2;
    public static final int TIER_OIL = 3;
    public static final int TIER_NUCLEAR = 4;
    public static final int TIER_RBMK = 5;
    public static final int TIER_FUSION = 6;
    public static final int TIER_PARTICLE = 7;
    public static final int TIER_GERALD = 8;

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape X_SHAPE = box(0.0D, 0.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    private static final VoxelShape Z_SHAPE = box(4.0D, 0.0D, 0.0D, 12.0D, 12.0D, 16.0D);

    private final int tier;

    public NTMAnvilBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.SOUTH));
    }

    public int tier() {
        return tier;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        if (placer != null) {
            level.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 2);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = new SimpleMenuProvider(
                    (containerId, inventory, openedPlayer) -> new AnvilMenu(containerId, inventory, tier),
                    Component.translatable("container.anvil", tier));
            NetworkHooks.openScreen(serverPlayer, provider, buffer -> buffer.writeVarInt(tier));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == net.minecraft.core.Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, net.minecraft.core.Direction side) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    public static BlockItem item(NTMAnvilBlock block, Item.Properties properties) {
        return new BlockItem(block, properties) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                    TooltipFlag flag) {
                tooltip.add(Component.literal("Tier " + block.tier() + " Anvil").withStyle(ChatFormatting.GOLD));
            }
        };
    }
}
