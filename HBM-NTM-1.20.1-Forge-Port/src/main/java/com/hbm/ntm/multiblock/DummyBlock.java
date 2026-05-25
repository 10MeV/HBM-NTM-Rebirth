package com.hbm.ntm.multiblock;

import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class DummyBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.block();

    public DummyBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockDummyBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.MULTIBLOCK_DUMMY.get()) {
            return null;
        }
        return level.isClientSide
                ? null
                : (tickLevel, tickPos, tickState, blockEntity) ->
                MultiblockDummyBlockEntity.serverTick(tickLevel, tickPos, tickState, (MultiblockDummyBlockEntity) blockEntity);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && !player.isShiftKeyDown()
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
            return dummy.forwardUse(serverPlayer, hand, hit);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        return core == null ? super.getDestroyProgress(state, player, level, pos)
                : core.state().getDestroyProgress(player, level, core.pos());
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        return core == null ? ItemStack.EMPTY
                : core.state().getBlock().getCloneItemStack(level, core.pos(), core.state());
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
            dummy.setDropCoreOnRemoval(!player.getAbilities().instabuild);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && !MultiblockHelper.isClearing(level, pos)
                && level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
            dummy.destroyCore();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return forwardedShape(level, pos, context, false);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return forwardedShape(level, pos, context, true);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    private VoxelShape forwardedShape(BlockGetter level, BlockPos pos, CollisionContext context, boolean collision) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core != null && core.state().getBlock() instanceof MultiblockCoreBlock coreBlock) {
            VoxelShape shape = collision
                    ? coreBlock.getMultiblockCollisionShape(core.state(), level, core.pos(), context)
                    : coreBlock.getMultiblockShape(core.state(), level, core.pos(), context);
            return shape.move(
                    core.pos().getX() - pos.getX(),
                    core.pos().getY() - pos.getY(),
                    core.pos().getZ() - pos.getZ());
        }
        return SHAPE;
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                manager.destroy(pos, MultiblockHelper.steelParticleState());
                return true;
            }
        });
    }
}

