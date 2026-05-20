package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LegacyComplexShapeBlock extends Block {
    private static final VoxelShape BOBBLEHEAD = box(5.5D, 0.0D, 5.5D, 10.5D, 10.0D, 10.5D);
    private static final VoxelShape SNOWGLOBE = box(4.0D, 0.0D, 4.0D, 12.0D, 5.0D, 12.0D);
    private static final VoxelShape PLUSHIE = box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);
    private static final VoxelShape DEMON_LAMP = box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape LANTERN = box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape REBAR = Shapes.or(
            box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D),
            box(0.0D, 7.0D, 7.0D, 16.0D, 9.0D, 9.0D),
            box(7.0D, 7.0D, 0.0D, 9.0D, 9.0D, 16.0D));
    private static final VoxelShape WOOD_BARRIER = box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D);
    private static final VoxelShape SANDBAGS = box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final VoxelShape shape;
    private final boolean hasCollision;

    public LegacyComplexShapeBlock(BlockBehaviour.Properties properties, VoxelShape shape) {
        this(properties, shape, true);
    }

    public LegacyComplexShapeBlock(BlockBehaviour.Properties properties, VoxelShape shape, boolean hasCollision) {
        super(properties);
        this.shape = shape;
        this.hasCollision = hasCollision;
    }

    public static LegacyComplexShapeBlock bobblehead(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, BOBBLEHEAD);
    }

    public static LegacyComplexShapeBlock snowglobe(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, SNOWGLOBE);
    }

    public static LegacyComplexShapeBlock plushie(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, PLUSHIE);
    }

    public static LegacyComplexShapeBlock demonLamp(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, DEMON_LAMP);
    }

    public static LegacyComplexShapeBlock lantern(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, LANTERN);
    }

    public static Block spotlightIncandescent(BlockBehaviour.Properties properties) {
        return new LegacyDirectionalShapeBlock(properties, LegacyDirectionalShapeBlock.Kind.SPOTLIGHT_INCANDESCENT, false);
    }

    public static Block spotlightFluoro(BlockBehaviour.Properties properties) {
        return new LegacyDirectionalShapeBlock(properties, LegacyDirectionalShapeBlock.Kind.SPOTLIGHT_FLUORO, false);
    }

    public static Block spotlightHalogen(BlockBehaviour.Properties properties) {
        return new LegacyDirectionalShapeBlock(properties, LegacyDirectionalShapeBlock.Kind.SPOTLIGHT_HALOGEN, false);
    }

    public static Block floodlight(BlockBehaviour.Properties properties) {
        return new LegacyDirectionalShapeBlock(properties, LegacyDirectionalShapeBlock.Kind.FLOODLIGHT);
    }

    public static LegacyComplexShapeBlock rebar(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, REBAR);
    }

    public static LegacyComplexShapeBlock woodBarrier(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, WOOD_BARRIER);
    }

    public static LegacyComplexShapeBlock sandbags(BlockBehaviour.Properties properties) {
        return new LegacyComplexShapeBlock(properties, SANDBAGS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasCollision ? shape : Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}
