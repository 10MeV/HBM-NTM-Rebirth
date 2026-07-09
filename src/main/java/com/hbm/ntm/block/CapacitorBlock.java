package com.hbm.ntm.block;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.blockentity.CapacitorBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.BobMathUtil;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

@SuppressWarnings("deprecation")
public class CapacitorBlock extends DirectionalBlock implements EntityBlock, LegacyLookOverlayBlockProvider {
    private final long maxPower;
    private final String legacyTextureName;

    public CapacitorBlock(Properties properties, long maxPower, String legacyTextureName) {
        super(properties);
        this.maxPower = Math.max(0L, maxPower);
        this.legacyTextureName = legacyTextureName;
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.UP));
    }

    public long maxPower() {
        return maxPower;
    }

    public long legacyTooltipChargeSpeed() {
        return maxPower / 200L;
    }

    public long legacyTooltipDischargeSpeed() {
        return maxPower / 600L;
    }

    public String legacyTextureName() {
        return legacyTextureName;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CapacitorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HbmPersistentBlockState persistent) {
            persistent.readPersistentStateFromStack(stack);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return type == ModBlockEntities.CAPACITOR.get()
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        CapacitorBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (CapacitorBlockEntity) blockEntity)
                : null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (builder.getLevel() instanceof ServerLevel
                && builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof CapacitorBlockEntity capacitor) {
            return List.of(capacitor.createPersistentBlockDrop(asItem()));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof CapacitorBlockEntity capacitor) {
            capacitor.clearOutputSubscription();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        return level.getBlockEntity(viewedPos) instanceof CapacitorBlockEntity capacitor
                ? capacitor.getLookOverlay(level, viewedPos)
                : null;
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        return getLookOverlay(level, viewedPos, viewedState);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Stores up to " + BobMathUtil.getShortNumber(maxPower) + "HE")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Charge speed: " + BobMathUtil.getShortNumber(legacyTooltipChargeSpeed()) + "HE")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Discharge speed: "
                + BobMathUtil.getShortNumber(legacyTooltipDischargeSpeed()) + "HE").withStyle(ChatFormatting.GOLD));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(HbmPersistentBlockState.TAG_PERSISTENT, Tag.TAG_COMPOUND)) {
            CompoundTag persistent = tag.getCompound(HbmPersistentBlockState.TAG_PERSISTENT);
            long max = persistent.contains("maxPower") ? persistent.getLong("maxPower") : maxPower;
            tooltip.add(Component.literal(BobMathUtil.getShortNumber(persistent.getLong("power")) + "/"
                    + BobMathUtil.getShortNumber(max) + "HE").withStyle(ChatFormatting.YELLOW));
        }
    }
}
