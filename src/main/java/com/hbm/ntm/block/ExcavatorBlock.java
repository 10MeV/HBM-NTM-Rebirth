package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ExcavatorBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ExcavatorBlock extends LegacyVisibleMultiblockMachineBlock {
    public ExcavatorBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExcavatorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && resolveCoreBlockEntity(level, pos) instanceof ExcavatorBlockEntity excavator) {
            NetworkHooks.openScreen(serverPlayer, excavator, excavator.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onCoreRemoved(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ExcavatorBlockEntity excavator) {
            for (ItemStack stack : excavator.getDrops()) {
                Block.popResource(level, pos, stack);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof ExcavatorBlockEntity excavator) {
            List<ItemStack> drops = excavator.getDrops();
            drops.add(new ItemStack(asItem()));
            return drops;
        }
        return super.getDrops(state, builder);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.EXCAVATOR.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof ExcavatorBlockEntity excavator) {
                        ExcavatorBlockEntity.clientTick(tickLevel, tickPos, tickState, excavator);
                    }
                }
                : (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof ExcavatorBlockEntity excavator) {
                        ExcavatorBlockEntity.serverTick(tickLevel, tickPos, tickState, excavator);
                    }
                };
    }
}
