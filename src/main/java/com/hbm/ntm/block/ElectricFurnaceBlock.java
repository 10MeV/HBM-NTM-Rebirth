package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.ElectricFurnaceBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
public class ElectricFurnaceBlock extends HorizontalMachineBlock implements EntityBlock {
    private final boolean activeVariant;

    public ElectricFurnaceBlock(Properties properties, boolean activeVariant) {
        super(properties, true);
        this.activeVariant = activeVariant;
    }

    /** 1.7.10 uses two registered blocks: machine_electric_furnace_off/on. */
    public boolean isActiveVariant() {
        return activeVariant;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFurnaceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        // MachineElectricFurnace#onBlockActivated deliberately leaves a
        // sneaking click unconsumed, so tools and held-item interactions can
        // pass through just as they did in 1.7.10.
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity furnace) {
            NetworkHooks.openScreen(serverPlayer, furnace, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        // 1.7.10 MachineElectricFurnace#onBlockPlacedBy copies a renamed
        // block stack into TileEntityMachineElectricFurnace's "name" field.
        if (!level.isClientSide && stack.hasCustomHoverName()
                && level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity furnace) {
            furnace.setCustomName(stack.getHoverName().getString());
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.ELECTRIC_FURNACE.get()) {
            return null;
        }
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                ElectricFurnaceBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (ElectricFurnaceBlockEntity) blockEntity);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity furnace && furnace.isActive()) {
            BrickFurnaceBlock.frontSmokeFlame(state, level, pos, random);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // MachineElectricFurnace#updateBlockState keeps its TileEntity while replacing the
        // off block with the registered active variant (and vice versa).
        if (newState.getBlock() instanceof ElectricFurnaceBlock) {
            return;
        }
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElectricFurnaceBlockEntity furnace) {
                for (ItemStack stack : furnace.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
