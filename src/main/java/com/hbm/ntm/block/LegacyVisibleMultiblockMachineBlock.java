package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.LegacyVisibleMachineBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockCoreBlock;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class LegacyVisibleMultiblockMachineBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    private final LegacyMachineDefinition definition;

    public LegacyVisibleMultiblockMachineBlock(Properties properties, LegacyMachineDefinition definition) {
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
    protected net.minecraft.core.Direction modifyPlacementFacing(net.minecraft.core.Direction facing) {
        return definition.placementFacing(facing);
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return definition.layout(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyVisibleMachineBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
    public VoxelShape getMultiblockShape(BlockState state, BlockGetter level, BlockPos corePos, CollisionContext context) {
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
    public BlockState multiblockParticleState(BlockState state, BlockGetter level, BlockPos corePos) {
        return definition.particleState(state);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                BlockState particleState = definition.particleState(state);
                if (particleState.getBlock() instanceof MultiblockCoreBlock) {
                    particleState = MultiblockHelper.steelParticleState();
                }
                manager.destroy(pos, particleState);
                return true;
            }
        });
    }
}
