package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockExtents;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@SuppressWarnings("deprecation")
public class AssemblyMachineBlock extends LegacyXrMultiblockBlock implements EntityBlock {
    /**
     * Legacy MachineAssemblyMachine dimensions are XR ordered as {U,D,N,S,W,E} = {2,0,1,1,1,1}.
     * The shared helper stores axis extents as {+X,-X,+Y,-Y,+Z,-Z}.
     */
    private static final int[] LEGACY_XR_DIMENSIONS = new int[] { 2, 0, 1, 1, 1, 1 };
    public static final MultiblockExtents EXTENTS = MultiblockExtents.ofLegacyXr(LEGACY_XR_DIMENSIONS, Direction.SOUTH);
    private static final int LEGACY_OFFSET = 1;

    public AssemblyMachineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_XR_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return LEGACY_OFFSET;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(LEGACY_XR_DIMENSIONS, state.getValue(FACING))
                .withProxyPredicate(AssemblyMachineBlock::isLegacyProxyOffset,
                        LegacyProxyMode.combo(true, true, true));
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof AssemblyMachineBlockEntity assembler) {
            for (ItemStack stack : assembler.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    private static boolean isLegacyProxyOffset(BlockPos offset) {
        return offset.getY() == 0
                && Math.abs(offset.getX()) <= 1
                && Math.abs(offset.getZ()) <= 1
                && (offset.getX() != 0 || offset.getZ() != 0);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyMachineBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof AssemblyMachineBlockEntity assembler) {
            NetworkHooks.openScreen(serverPlayer, assembler, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ASSEMBLY_MACHINE.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                AssemblyMachineBlockEntity.clientTick(tickLevel, tickPos, tickState, (AssemblyMachineBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                AssemblyMachineBlockEntity.serverTick(tickLevel, tickPos, tickState, (AssemblyMachineBlockEntity) blockEntity);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return getMultiblockCollisionShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getMultiblockShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos corePos,
            CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getMultiblockCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
            BlockPos corePos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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

