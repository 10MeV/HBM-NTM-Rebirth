package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FluidPipeAnchorBlockEntity;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidDuctVariants;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.FluidPipeStyleBlockItem;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class FluidPipeBlock extends HbmFluidNodeBlock {
    public static final int LEGACY_STYLE_COUNT = HbmFluidDuctVariants.STANDARD_STYLE_COUNT;
    public static final IntegerProperty LEGACY_STYLE = IntegerProperty.create("legacy_style", 0,
            LEGACY_STYLE_COUNT - 1);
    private static final VoxelShape CORE = box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape NORTH_ARM = box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D);
    private static final VoxelShape EAST_ARM = box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    private static final VoxelShape SOUTH_ARM = box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape WEST_ARM = box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D);
    private static final VoxelShape UP_ARM = box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape DOWN_ARM = box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);
    private static final VoxelShape FULL_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape X_STRAIGHT_SHAPE = Shapes.or(WEST_ARM, CORE, EAST_ARM);
    private static final VoxelShape Y_STRAIGHT_SHAPE = Shapes.or(DOWN_ARM, CORE, UP_ARM);
    private static final VoxelShape Z_STRAIGHT_SHAPE = Shapes.or(NORTH_ARM, CORE, SOUTH_ARM);
    private static final VoxelShape ISOLATED_COLLISION_SHAPE = Shapes.or(WEST_ARM, EAST_ARM, DOWN_ARM, UP_ARM,
            NORTH_ARM, SOUTH_ARM);

    public FluidPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LEGACY_STYLE, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof IFluidIdentifierItem identifier)
                || !(level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        FluidType target = identifier.getIdentifiedFluid(level, pos, held);
        boolean toolAlt = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_ALT);
        boolean toolCtrl = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_CTRL);

        if (toolAlt && target != pipe.getFluidType()
                && identifier.setIdentifiedFluid(held, pipe.getFluidType(), true)) {
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    0.25F, 0.75F);
            return InteractionResult.CONSUME;
        }

        if (toolCtrl || player.isShiftKeyDown()) {
            FluidPipeBlockEntity.changeConnectedPipeTypes(level, pos, pipe.getFluidType(), target, 64);
        } else {
            pipe.setFluidType(target);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.is(ModBlocks.FLUID_DUCT_NEO.get())
                && level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe
                && ModItems.FLUID_DUCT.get() instanceof FluidPipeBlockItem item) {
            return FluidPipeBlockItem.createStack(item, pipe.getFluidType());
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (asItem() instanceof FluidPipeStyleBlockItem item) {
            return List.of(FluidPipeStyleBlockItem.createStack(item,
                    clampLegacyStyle(state.getValue(LEGACY_STYLE))));
        }
        return super.getDrops(state, params);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return selectedShapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShapeForState(state);
    }

    private static VoxelShape collisionShapeForState(BlockState state) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        int mask = connectionMask(north, east, south, west, up, down);

        if (mask == 0) {
            return ISOLATED_COLLISION_SHAPE;
        }
        if ((east || west) && !north && !south && !up && !down) {
            return X_STRAIGHT_SHAPE;
        }
        if ((up || down) && !north && !south && !east && !west) {
            return Y_STRAIGHT_SHAPE;
        }
        if ((north || south) && !east && !west && !up && !down) {
            return Z_STRAIGHT_SHAPE;
        }

        VoxelShape shape = CORE;
        if (north) shape = Shapes.or(shape, NORTH_ARM);
        if (east) shape = Shapes.or(shape, EAST_ARM);
        if (south) shape = Shapes.or(shape, SOUTH_ARM);
        if (west) shape = Shapes.or(shape, WEST_ARM);
        if (up) shape = Shapes.or(shape, UP_ARM);
        if (down) shape = Shapes.or(shape, DOWN_ARM);
        return shape;
    }

    private static VoxelShape selectedShapeForState(BlockState state) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        int mask = connectionMask(north, east, south, west, up, down);

        if (mask == 0) {
            return FULL_SHAPE;
        }
        if ((east || west) && !north && !south && !up && !down) {
            return X_STRAIGHT_SHAPE;
        }
        if ((up || down) && !north && !south && !east && !west) {
            return Y_STRAIGHT_SHAPE;
        }
        if ((north || south) && !east && !west && !up && !down) {
            return Z_STRAIGHT_SHAPE;
        }

        return box(west ? 0.0D : 5.0D,
                down ? 0.0D : 5.0D,
                north ? 0.0D : 5.0D,
                east ? 16.0D : 11.0D,
                up ? 16.0D : 11.0D,
                south ? 16.0D : 11.0D);
    }

    private static int connectionMask(boolean north, boolean east, boolean south, boolean west, boolean up, boolean down) {
        return (east ? 32 : 0)
                | (west ? 16 : 0)
                | (up ? 8 : 0)
                | (down ? 4 : 0)
                | (south ? 2 : 0)
                | (north ? 1 : 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEGACY_STYLE);
    }

    public static int clampLegacyStyle(int style) {
        return HbmFluidDuctVariants.clampStandardStyle(style);
    }

    public static int[] legacyCreativeStyles() {
        return HbmFluidDuctVariants.standardVisibleStyles();
    }

}

