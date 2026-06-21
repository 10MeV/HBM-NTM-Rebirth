package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class StaticLegacyMultiblockMachineBlock extends LegacyXrMultiblockBlock {
    private final LegacyMachineDefinition definition;

    public StaticLegacyMultiblockMachineBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public LegacyMachineDefinition definition() {
        return definition;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return definition.legacyXrDimensions();
    }

    @Override
    protected int getLegacyOffset() {
        return definition.legacyOffset();
    }

    @Override
    protected int getLegacyHeightOffset() {
        return definition.legacyHeightOffset();
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return definition.layout(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getMultiblockCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return definition.hasCollisionShapeFactory() ? definition.collisionShape(state) : Shapes.block();
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return definition.hasCollisionShapeFactory() ? definition.collisionShape(state) : Shapes.block();
    }

    @Override
    public boolean usesForwardedDummyCollisionShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return definition.hasCollisionShapeFactory();
    }

    @Override
    public boolean usesForwardedDummyShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return definition.hasCollisionShapeFactory();
    }

    @Override
    public boolean usesMultiblockHighlightShape(BlockState state, BlockGetter level, BlockPos corePos) {
        return definition.hasCollisionShapeFactory();
    }

    @Override
    public BlockState multiblockParticleState(BlockState state, BlockGetter level, BlockPos corePos) {
        return definition.particleState(state);
    }
}
