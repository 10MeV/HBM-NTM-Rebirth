package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidDuctBoxBlockEntity;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidDuctBoxBlock extends FluidPipeBlock {
    public static final int LEGACY_METADATA_COUNT = 15;
    private static final int[] BOX_CREATIVE_METADATA = createSequentialMetadata();
    private static final int[] EXHAUST_CREATIVE_METADATA = {0, 3, 6, 9, 12};
    public static final IntegerProperty LEGACY_METADATA = IntegerProperty.create("legacy_metadata", 0,
            LEGACY_METADATA_COUNT - 1);

    public FluidDuctBoxBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LEGACY_METADATA, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDuctBoxBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    protected static VoxelShape shapeForState(BlockState state) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        boolean up = state.getValue(UP);
        boolean down = state.getValue(DOWN);
        DuctBounds bounds = boundsFor(state);

        if ((east || west) && !north && !south && !up && !down) {
            return box(0.0D, bounds.lowerPx(), bounds.lowerPx(), 16.0D, bounds.upperPx(), bounds.upperPx());
        }
        if ((up || down) && !north && !south && !east && !west) {
            return box(bounds.lowerPx(), 0.0D, bounds.lowerPx(), bounds.upperPx(), 16.0D, bounds.upperPx());
        }
        if ((north || south) && !east && !west && !up && !down) {
            return box(bounds.lowerPx(), bounds.lowerPx(), 0.0D, bounds.upperPx(), bounds.upperPx(), 16.0D);
        }

        boolean simpleCurve = connectionCount(north, east, south, west, up, down) == 2;
        double coreMin = simpleCurve ? bounds.lowerPx() : bounds.junctionLowerPx();
        double coreMax = simpleCurve ? bounds.upperPx() : bounds.junctionUpperPx();
        VoxelShape shape = box(coreMin, coreMin, coreMin, coreMax, coreMax, coreMax);
        if (north) {
            shape = Shapes.or(shape, box(bounds.lowerPx(), bounds.lowerPx(), 0.0D,
                    bounds.upperPx(), bounds.upperPx(), bounds.lowerPx()));
        }
        if (east) {
            shape = Shapes.or(shape, box(bounds.upperPx(), bounds.lowerPx(), bounds.lowerPx(),
                    16.0D, bounds.upperPx(), bounds.upperPx()));
        }
        if (south) {
            shape = Shapes.or(shape, box(bounds.lowerPx(), bounds.lowerPx(), bounds.upperPx(),
                    bounds.upperPx(), bounds.upperPx(), 16.0D));
        }
        if (west) {
            shape = Shapes.or(shape, box(0.0D, bounds.lowerPx(), bounds.lowerPx(),
                    bounds.lowerPx(), bounds.upperPx(), bounds.upperPx()));
        }
        if (up) {
            shape = Shapes.or(shape, box(bounds.lowerPx(), bounds.upperPx(), bounds.lowerPx(),
                    bounds.upperPx(), 16.0D, bounds.upperPx()));
        }
        if (down) {
            shape = Shapes.or(shape, box(bounds.lowerPx(), 0.0D, bounds.lowerPx(),
                    bounds.upperPx(), bounds.lowerPx(), bounds.upperPx()));
        }
        return shape;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (asItem() instanceof FluidDuctVariantBlockItem item) {
            return FluidDuctVariantBlockItem.createStack(item, state.getValue(LEGACY_METADATA));
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (asItem() instanceof FluidDuctVariantBlockItem item) {
            return List.of(FluidDuctVariantBlockItem.createStack(item, state.getValue(LEGACY_METADATA)));
        }
        return super.getDrops(state, params);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEGACY_METADATA);
    }

    public static int clampLegacyMetadata(int metadata) {
        return Math.max(0, Math.min(LEGACY_METADATA_COUNT - 1, metadata));
    }

    public static int[] boxCreativeMetadata() {
        return BOX_CREATIVE_METADATA.clone();
    }

    public static int[] exhaustCreativeMetadata() {
        return EXHAUST_CREATIVE_METADATA.clone();
    }

    public static int rectifyLegacyMaterial(int metadata) {
        return clampLegacyMetadata(metadata) % 3;
    }

    public static int legacySizeStep(int metadata) {
        return clampLegacyMetadata(metadata) / 3;
    }

    public static DuctBounds boundsFor(BlockState state) {
        int metadata = state.hasProperty(LEGACY_METADATA) ? state.getValue(LEGACY_METADATA) : 0;
        return boundsFor(metadata);
    }

    public static DuctBounds boundsFor(int metadata) {
        int step = legacySizeStep(metadata);
        double lower = 0.125D + step * 0.0625D;
        double upper = 0.875D - step * 0.0625D;
        double junctionLower = 0.0625D + step * 0.0625D;
        double junctionUpper = 0.9375D - step * 0.0625D;
        return new DuctBounds(lower, upper, junctionLower, junctionUpper);
    }

    private static int connectionCount(boolean north, boolean east, boolean south, boolean west, boolean up,
            boolean down) {
        return (north ? 1 : 0) + (east ? 1 : 0) + (south ? 1 : 0) + (west ? 1 : 0)
                + (up ? 1 : 0) + (down ? 1 : 0);
    }

    private static int[] createSequentialMetadata() {
        int[] metadata = new int[LEGACY_METADATA_COUNT];
        for (int i = 0; i < metadata.length; i++) {
            metadata[i] = i;
        }
        return metadata;
    }

    public record DuctBounds(double lower, double upper, double junctionLower, double junctionUpper) {
        double lowerPx() {
            return lower * 16.0D;
        }

        double upperPx() {
            return upper * 16.0D;
        }

        double junctionLowerPx() {
            return junctionLower * 16.0D;
        }

        double junctionUpperPx() {
            return junctionUpper * 16.0D;
        }
    }
}
