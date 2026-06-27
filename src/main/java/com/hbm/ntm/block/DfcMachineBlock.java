package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DfcCoreBlockEntity;
import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
import com.hbm.ntm.blockentity.DfcInjectorBlockEntity;
import com.hbm.ntm.blockentity.DfcReceiverBlockEntity;
import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class DfcMachineBlock extends HorizontalMachineBlock implements EntityBlock {
    private final Kind kind;

    public DfcMachineBlock(Properties properties, Kind kind) {
        super(properties, false);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return switch (kind) {
            case CORE -> new DfcCoreBlockEntity(pos, state);
            case EMITTER -> new DfcEmitterBlockEntity(pos, state);
            case RECEIVER -> new DfcReceiverBlockEntity(pos, state);
            case INJECTOR -> new DfcInjectorBlockEntity(pos, state);
            case STABILIZER -> new DfcStabilizerBlockEntity(pos, state);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider provider) {
            NetworkHooks.openScreen(serverPlayer, provider, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (kind == Kind.CORE && type == ModBlockEntities.DFC_CORE.get()) {
            return ticker(level, DfcCoreBlockEntity::serverTick, DfcCoreBlockEntity::clientTick);
        }
        if (kind == Kind.EMITTER && type == ModBlockEntities.DFC_EMITTER.get()) {
            return ticker(level, DfcEmitterBlockEntity::serverTick, DfcEmitterBlockEntity::clientTick);
        }
        if (kind == Kind.RECEIVER && type == ModBlockEntities.DFC_RECEIVER.get()) {
            return ticker(level, DfcReceiverBlockEntity::serverTick, DfcReceiverBlockEntity::clientTick);
        }
        if (kind == Kind.INJECTOR && type == ModBlockEntities.DFC_INJECTOR.get()) {
            return ticker(level, DfcInjectorBlockEntity::serverTick, DfcInjectorBlockEntity::clientTick);
        }
        if (kind == Kind.STABILIZER && type == ModBlockEntities.DFC_STABILIZER.get()) {
            return ticker(level, DfcStabilizerBlockEntity::serverTick, DfcStabilizerBlockEntity::clientTick);
        }
        return null;
    }

    private static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<T> ticker(Level level,
            BlockEntityTicker<E> serverTicker, BlockEntityTicker<E> clientTicker) {
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            @SuppressWarnings("unchecked")
            E cast = (E) blockEntity;
            if (level.isClientSide) {
                clientTicker.tick(tickLevel, tickPos, tickState, cast);
            } else {
                serverTicker.tick(tickLevel, tickPos, tickState, cast);
            }
        };
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            List<ItemStack> drops;
            if (blockEntity instanceof DfcCoreBlockEntity core) {
                drops = core.getDrops();
            } else if (blockEntity instanceof DfcInjectorBlockEntity injector) {
                drops = injector.getDrops();
            } else if (blockEntity instanceof DfcStabilizerBlockEntity stabilizer) {
                drops = stabilizer.getDrops();
            } else {
                drops = List.of();
            }
            for (ItemStack stack : drops) {
                Block.popResource(level, pos, stack);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public enum Kind {
        CORE,
        EMITTER,
        RECEIVER,
        INJECTOR,
        STABILIZER
    }
}
