package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.BoilerBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class HeatBoilerBlock extends LegacyVisibleMultiblockMachineBlock {
    public HeatBoilerBlock(Properties properties, LegacyMachineDefinition definition) {
        super(properties, definition);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoilerBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!level.isClientSide && !player.isShiftKeyDown()
                && held.getItem() instanceof IFluidIdentifierItem identifier
                && level.getBlockEntity(pos) instanceof BoilerBlockEntity boiler) {
            FluidType type = identifier.getIdentifiedFluid(level, pos, held);
            if (isBoilerHeatable(type)) {
                boiler.setFeedTankType(type);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (type != ModBlockEntities.BOILER.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        BoilerBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (BoilerBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        BoilerBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (BoilerBlockEntity) blockEntity);
    }

    private static boolean isBoilerHeatable(FluidType type) {
        if (type == null) {
            return false;
        }
        HeatableFluidTrait trait = type.getTrait(HeatableFluidTrait.class);
        return trait != null && trait.getEfficiency(HeatingType.BOILER) > 0.0D;
    }
}
