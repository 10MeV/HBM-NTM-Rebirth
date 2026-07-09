package com.hbm.ntm.block;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.blockentity.ConveyorPressBlockEntity;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ConveyorPressBlock extends LegacyXrMultiblockBlock
        implements EntityBlock, IConveyorBelt, Toolable, LegacyLookOverlayBlockProvider {
    private static final int[] LEGACY_DIMENSIONS = { 2, 0, 0, 0, 0, 0 };

    public ConveyorPressBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return LegacyMultiblockLayout.ofLegacyXrChecked(LEGACY_DIMENSIONS, state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConveyorPressBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof ItemPressStamp)
                || !(resolveCoreBlockEntity(level, pos) instanceof ConveyorPressBlockEntity press)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!press.installStamp(held, player.getAbilities().instabuild)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
        }
        LegacySoundPlayer.playLegacyUpgradePlug(level, pos, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER
                || !(resolveCoreBlockEntity(level, pos) instanceof ConveyorPressBlockEntity press)) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        return press.removeStamp(player, pos);
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return resolveCoreBlockEntity(level, pos.below()) instanceof ConveyorPressBlockEntity;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        Direction direction = travelDirection(level, pos);
        Vec3 snap = getClosestSnappingPosition(level, pos, itemPos);
        return ConveyorMath.travelLocation(pos, itemPos, direction, snap, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.closestSnappingPosition(pos, itemPos, travelDirection(level, pos));
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        return resolveCoreBlockEntity(level, viewedPos) instanceof ConveyorPressBlockEntity press
                ? press.getLookOverlay(level, viewedPos)
                : null;
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        return getLookOverlay(level, viewedPos, viewedState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.CONVEYOR_PRESS.get()) {
            return null;
        }
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) ->
                        ConveyorPressBlockEntity.clientTick(tickLevel, tickPos, tickState,
                                (ConveyorPressBlockEntity) blockEntity)
                : (tickLevel, tickPos, tickState, blockEntity) ->
                        ConveyorPressBlockEntity.serverTick(tickLevel, tickPos, tickState,
                                (ConveyorPressBlockEntity) blockEntity);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && resolveCoreBlockEntity(level, pos) instanceof ConveyorPressBlockEntity press) {
            ItemStack stamp = press.getStamp().copy();
            if (!stamp.isEmpty()) {
                Block.popResource(level, press.getBlockPos(), stamp);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static Direction travelDirection(Level level, BlockPos pos) {
        BlockEntity core = resolveCoreBlockEntity(level, pos.below());
        BlockState state = core == null ? level.getBlockState(pos.below()) : core.getBlockState();
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        return facing.getClockWise();
    }
}
