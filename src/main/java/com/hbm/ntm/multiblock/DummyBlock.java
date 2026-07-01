package com.hbm.ntm.multiblock;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.blockentity.MultiblockDummyBlockEntity;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IConveyorItem;
import com.hbm.ntm.api.conveyor.IConveyorPackage;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.api.multiblock.DummyPart;
import com.hbm.ntm.neutron.RBMKStructureDimensions;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class DummyBlock extends Block implements EntityBlock, DummyPart, IConveyorBelt, IEnterableBlock, Toolable,
        ICrucibleAcceptor,
        LegacyLookOverlayBlockProvider {
    private static final VoxelShape SHAPE = Shapes.block();

    public DummyBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultiblockDummyBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).is(ModItems.RADAR_LINKER.get())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return operationalCore(level, pos) == null ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        if (!level.isClientSide
                && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
            return dummy.forwardUse(serverPlayer, hand, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        validateCoreLink(level, pos, state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        validateCoreLink(level, pos, state);
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        return core != null
                && core.state().getBlock() instanceof IConveyorBelt belt
                && belt.canItemStay(level, pos, itemPos);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof IConveyorBelt belt) {
            return belt.getTravelLocation(level, pos, itemPos, speed);
        }
        return itemPos;
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof IConveyorBelt belt) {
            return belt.getClosestSnappingPosition(level, pos, itemPos);
        }
        return itemPos;
    }

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        return core != null
                && core.state().getBlock() instanceof IEnterableBlock enterable
                && enterable.canItemEnter(level, pos, side, entity);
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof IEnterableBlock enterable) {
            enterable.onItemEnter(level, pos, side, entity);
        }
    }

    @Override
    public boolean canPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        return core != null
                && core.state().getBlock() instanceof IEnterableBlock enterable
                && enterable.canPackageEnter(level, pos, side, entity);
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof IEnterableBlock enterable) {
            enterable.onPackageEnter(level, pos, side, entity);
        }
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        return core != null
                && core.state().getBlock() instanceof Toolable toolable
                && toolable.onToolUse(level, player, pos, side, hit, tool);
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acceptor = moltenAcceptor(level, pos);
        return acceptor != null && acceptor.canAcceptPartialPour(level, pos, hit, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, Vec3 hit, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acceptor = moltenAcceptor(level, pos);
        return acceptor == null ? stack : acceptor.pour(level, pos, hit, side, stack);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acceptor = moltenAcceptor(level, pos);
        return acceptor != null && acceptor.canAcceptPartialFlow(level, pos, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ICrucibleAcceptor acceptor = moltenAcceptor(level, pos);
        return acceptor == null ? stack : acceptor.flow(level, pos, side, stack);
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        MultiblockHelper.CoreLookup core = operationalCore(level, viewedPos);
        if (core != null && core.state().getBlock() instanceof LegacyLookOverlayBlockProvider provider) {
            return provider.getLookOverlay(level, viewedPos, viewedState);
        }
        return null;
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        MultiblockHelper.CoreLookup core = operationalCore(level, viewedPos);
        if (core != null && core.state().getBlock() instanceof LegacyLookOverlayBlockProvider provider) {
            return provider.getLookOverlay(level, player, viewedPos, viewedState);
        }
        return null;
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
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core != null) {
            core.state().onBlockExploded(level, core.pos(), explosion);
        } else {
            super.onBlockExploded(state, level, pos, explosion);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core == null || !core.state().isSignalSource()) {
            return 0;
        }
        return core.state().getSignal(level, core.pos(), direction);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core == null || !core.state().hasAnalogOutputSignal()) {
            return 0;
        }
        return core.state().getAnalogOutputSignal(level, core.pos());
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
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof MultiblockCoreBlock coreBlock
                && coreBlock.usesMultiblockDummySupportShapeOverride(core.state(), level, core.pos())) {
            return coreBlock.getMultiblockDummySupportShape(core.state(), level, core.pos(), pos);
        }
        return super.getBlockSupportShape(state, level, pos);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    private VoxelShape forwardedShape(BlockGetter level, BlockPos pos, CollisionContext context, boolean collision) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof MultiblockCoreBlock coreBlock) {
            if (!collision && !coreBlock.usesLocalDummyShape(core.state(), level, core.pos())) {
                return SHAPE;
            }
            if (collision && !coreBlock.usesLocalDummyCollisionShape(core.state(), level, core.pos())) {
                return SHAPE;
            }
            return collision
                    ? coreBlock.getMultiblockDummyCollisionShape(core.state(), level, core.pos(), pos, context)
                    : coreBlock.getMultiblockDummyShape(core.state(), level, core.pos(), pos, context);
        }
        return SHAPE;
    }

    @Nullable
    private ICrucibleAcceptor moltenAcceptor(Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy)
                || !dummy.getProxyMode().isProxy()
                || (!dummy.getProxyMode().moltenMetal() && !dummy.getProxyMode().allCapabilities())) {
            return null;
        }
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core == null || core.pos().equals(pos)) {
            return null;
        }
        if (core.state().getBlock() instanceof ICrucibleAcceptor acceptor) {
            return acceptor;
        }
        BlockEntity coreEntity = level.getBlockEntity(core.pos());
        return coreEntity instanceof ICrucibleAcceptor acceptor ? acceptor : null;
    }

    private BlockState particleState(BlockGetter level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = operationalCore(level, pos);
        if (core != null && core.state().getBlock() instanceof MultiblockCoreBlock coreBlock) {
            BlockState particleState = coreBlock.multiblockParticleState(core.state(), level, core.pos());
            if (particleState.getBlock() instanceof MultiblockCoreBlock || particleState.getBlock() instanceof DummyBlock) {
                return MultiblockHelper.steelParticleState();
            }
            return particleState;
        }
        return MultiblockHelper.steelParticleState();
    }

    private static void validateCoreLink(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof MultiblockDummyBlockEntity dummy) {
            MultiblockDummyBlockEntity.serverTick(level, pos, state, dummy);
        } else {
            level.removeBlock(pos, false);
        }
    }

    @Nullable
    private static MultiblockHelper.CoreLookup operationalCore(BlockGetter level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null) {
            core = RBMKStructureDimensions.findVerticalColumnCore(level, pos);
        }
        return core != null && MultiblockHelper.isOperationalCoreLayoutComplete(level, core.pos()) ? core : null;
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                manager.destroy(pos, particleState(level, pos));
                return true;
            }
        });
    }
}

