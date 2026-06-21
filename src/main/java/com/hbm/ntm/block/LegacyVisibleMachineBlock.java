package com.hbm.ntm.block;

import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
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
public class LegacyVisibleMachineBlock extends HorizontalMachineBlock implements EntityBlock {
    private final LegacyMachineDefinition definition;

    public LegacyVisibleMachineBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, false);
        this.definition = definition;
    }

    public LegacyMachineDefinition definition() {
        return definition;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return definition.hasCollisionShapeFactory() ? definition.collisionShape(state) : Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return definition.hasCollisionShapeFactory() ? definition.collisionShape(state) : Shapes.block();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            onCoreRemoved(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                BlockState particleState = definition.particleState(state);
                if (particleState.getBlock() instanceof LegacyVisibleMultiblockMachineBlock) {
                    particleState = MultiblockHelper.steelParticleState();
                }
                manager.destroy(pos, particleState);
                return true;
            }
        });
    }
}
