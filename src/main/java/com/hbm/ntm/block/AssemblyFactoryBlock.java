package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.AssemblyFactoryBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("deprecation")
public class AssemblyFactoryBlock extends LegacyVisibleMultiblockMachineBlock {
    public AssemblyFactoryBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
        registerDefaultState(defaultBlockState().setValue(LegacyFrameRenderState.FRAME, false));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        // MachineAssemblyFactory#addInformation delegates to BlockDummyable's standard info tooltip.
        LegacyStandardInfoTooltip.append(tooltip, "machine_assembly_factory");
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LegacyFrameRenderState.FRAME);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyFactoryBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // Legacy MachineAssemblyFactory calls standardOpenBehavior.
        if (player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof AssemblyFactoryBlockEntity factory) {
            NetworkHooks.openScreen(serverPlayer, factory, factory.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ASSEMBLY_FACTORY.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                AssemblyFactoryBlockEntity.clientTick(tickLevel, tickPos, tickState, (AssemblyFactoryBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                AssemblyFactoryBlockEntity.serverTick(tickLevel, tickPos, tickState, (AssemblyFactoryBlockEntity) blockEntity);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof AssemblyFactoryBlockEntity factory) {
            for (ItemStack stack : factory.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }
}
