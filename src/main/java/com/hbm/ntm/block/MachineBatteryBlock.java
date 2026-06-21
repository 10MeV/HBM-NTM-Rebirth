package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.blockentity.MachineBatteryBlockEntity;
import com.hbm.ntm.energy.HbmEnergyNodespace;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.BobMathUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

@SuppressWarnings("deprecation")
public class MachineBatteryBlock extends HorizontalMachineBlock implements EntityBlock {
    public static final long DEFAULT_MAX_POWER = 1_000_000L;

    private final long maxPower;

    public MachineBatteryBlock(Properties properties) {
        this(properties, DEFAULT_MAX_POWER);
    }

    public MachineBatteryBlock(Properties properties, long maxPower) {
        super(properties);
        this.maxPower = Math.max(0L, maxPower);
    }

    public long maxPower() {
        return maxPower;
    }

    public long maxReceive() {
        return maxPower / 200L;
    }

    public long maxExtract() {
        return maxPower / 600L;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineBatteryBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MachineBatteryBlockEntity battery) {
            NetworkHooks.openScreen(serverPlayer, battery, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmPersistentBlockState persistent) {
            persistent.readPersistentStateFromStack(stack);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.MACHINE_BATTERY.get()
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                MachineBatteryBlockEntity.serverTick(tickLevel, tickPos, tickState, (MachineBatteryBlockEntity) blockEntity)
                : null;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MachineBatteryBlockEntity battery ? battery.getComparatorPower() : 0;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MachineBatteryBlockEntity battery) {
            return List.of(battery.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, neighborPos, movedByPiston);
        if (!level.isClientSide) {
            HbmEnergyNodespace.markNodeAndNeighborsChanged(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof MachineBatteryBlockEntity battery) {
                for (ItemStack stack : battery.getDrops()) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Stores up to " + BobMathUtil.getShortNumber(maxPower) + "HE")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Charge speed: " + BobMathUtil.getShortNumber(maxReceive()) + "HE")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Discharge speed: " + BobMathUtil.getShortNumber(maxExtract()) + "HE")
                .withStyle(ChatFormatting.GOLD));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            long stored = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT).getLong("power");
            tooltip.add(Component.literal(BobMathUtil.getShortNumber(stored) + "/"
                    + BobMathUtil.getShortNumber(maxPower) + "HE").withStyle(ChatFormatting.YELLOW));
        }
    }
}

