package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.player.HbmPlayerProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The 1.7.10 wood roof, scaffold, and ceiling metadata block. */
@SuppressWarnings("deprecation")
public class LegacyWoodStructureBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");

    private static final VoxelShape ROOF = box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
    private static final VoxelShape SCAFFOLD = box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape CEILING = box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SCAFFOLD_SUPPORT = box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public LegacyWoodStructureBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(VARIANT, Variant.ROOF.legacyMeta())
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int variant = context.getItemInHand().getItem() instanceof LegacyStateBlockItem item
                ? item.getVariant(context.getItemInHand())
                : Variant.ROOF.legacyMeta();
        return connectedScaffoldState(defaultBlockState().setValue(VARIANT, variant), context.getLevel(),
                context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        BooleanProperty property = connectionProperty(direction);
        if (state.getValue(VARIANT) == Variant.SCAFFOLD.legacyMeta() && property != null) {
            return state.setValue(property, isScaffold(neighborState));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(VARIANT) == Variant.SCAFFOLD.legacyMeta() ? Shapes.block() : collisionShape(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return collisionShape(state);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(VARIANT) == Variant.SCAFFOLD.legacyMeta() ? SCAFFOLD_SUPPORT : Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(VARIANT) == Variant.SCAFFOLD.legacyMeta() && entity instanceof Player player) {
            HbmPlayerProperties.markOnLadder(player);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : super.getCloneItemStack(level, pos, state);
    }

    private static VoxelShape collisionShape(BlockState state) {
        return switch (Variant.byLegacyMeta(state.getValue(VARIANT))) {
            case ROOF -> ROOF;
            case SCAFFOLD -> SCAFFOLD;
            case CEILING -> CEILING;
        };
    }

    private static BlockState connectedScaffoldState(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(VARIANT) != Variant.SCAFFOLD.legacyMeta()) {
            return state;
        }
        return state
                .setValue(NORTH, isScaffold(level.getBlockState(pos.north())))
                .setValue(EAST, isScaffold(level.getBlockState(pos.east())))
                .setValue(SOUTH, isScaffold(level.getBlockState(pos.south())))
                .setValue(WEST, isScaffold(level.getBlockState(pos.west())))
                .setValue(UP, isScaffold(level.getBlockState(pos.above())));
    }

    private static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> null;
        };
    }

    private static boolean isScaffold(BlockState state) {
        return state.getBlock() instanceof LegacyWoodStructureBlock
                && state.getValue(VARIANT) == Variant.SCAFFOLD.legacyMeta();
    }

    public enum Variant {
        ROOF("roof"),
        SCAFFOLD("scaffold"),
        CEILING("ceiling");

        private static final Variant[] VALUES = values();
        private final String serializedName;

        Variant(String serializedName) {
            this.serializedName = serializedName;
        }

        public int legacyMeta() {
            return ordinal();
        }

        public String serializedName() {
            return serializedName;
        }

        public static Variant byLegacyMeta(int meta) {
            return meta >= 0 && meta < VALUES.length ? VALUES[meta] : ROOF;
        }
    }
}
