package com.hbm.ntm.block;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IConveyorItem;
import com.hbm.ntm.api.conveyor.IConveyorPackage;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.blockentity.CraneSplitterBlockEntity;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CraneSplitterBlock extends LegacyXrMultiblockBlock implements EntityBlock, IConveyorBelt,
        IEnterableBlock, Toolable, LegacyLookOverlayBlockProvider {
    private static final int[] LEGACY_DIMENSIONS = new int[] { 0, 0, 0, 0, 0, 1 };

    public CraneSplitterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return LEGACY_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraneSplitterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return true;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        Direction direction = getTravelDirection(level, pos);
        Vec3 snap = getClosestSnappingPosition(level, pos, itemPos);
        return ConveyorMath.travelLocation(pos, itemPos, direction, snap, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.closestSnappingPosition(pos, itemPos, getTravelDirection(level, pos));
    }

    @Override
    public boolean canItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity) {
        return getTravelDirection(level, pos) == side;
    }

    @Override
    public void onItemEnter(Level level, BlockPos pos, Direction side, IConveyorItem entity) {
        if (level.isClientSide) {
            return;
        }

        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null || !(level.getBlockEntity(core.pos()) instanceof CraneSplitterBlockEntity splitter)) {
            return;
        }

        ItemStack[] splits = splitter.splitStack(entity.getItemStack());
        spawnMovingItem(level, core.pos(), splits[0]);
        spawnMovingItem(level, core.pos().relative(sideOffset(core.state())), splits[1]);
    }

    @Override
    public boolean canPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity) {
        return false;
    }

    @Override
    public void onPackageEnter(Level level, BlockPos pos, Direction side, IConveyorPackage entity) {
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }

        if (level.isClientSide) {
            return true;
        }

        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        if (core == null || !(level.getBlockEntity(core.pos()) instanceof CraneSplitterBlockEntity splitter)) {
            return false;
        }

        int adjustment = player.isShiftKeyDown() ? -1 : 1;
        if (pos.equals(core.pos())) {
            splitter.adjustLeftRatio(adjustment);
        } else {
            splitter.adjustRightRatio(adjustment);
        }
        return true;
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        return splitterOverlay(level, viewedPos);
    }

    @Nullable
    @Override
    public LegacyLookOverlay getLookOverlay(Level level, Player player, BlockPos viewedPos, BlockState viewedState) {
        return splitterOverlay(level, viewedPos);
    }

    @Nullable
    private LegacyLookOverlay splitterOverlay(Level level, BlockPos viewedPos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, viewedPos);
        if (core == null || !(level.getBlockEntity(core.pos()) instanceof CraneSplitterBlockEntity splitter)) {
            return null;
        }
        return LegacyLookOverlay.forBlockState(core.state(), List.of(Component.literal(
                "Splitter ratio: " + splitter.getLeftRatio() + ":" + splitter.getRightRatio())));
    }

    public static Direction sideOffset(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        return facing.getCounterClockWise();
    }

    public static float legacyRenderRotationDegrees(BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }

    private Direction getTravelDirection(Level level, BlockPos pos) {
        MultiblockHelper.CoreLookup core = MultiblockHelper.findCore(level, pos);
        BlockState state = core == null ? level.getBlockState(pos) : core.state();
        return state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
    }

    private void spawnMovingItem(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        MovingItemEntity moving = new MovingItemEntity(level, stack);
        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 snap = getClosestSnappingPosition(level, pos, center);
        moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
        level.addFreshEntity(moving);
    }
}
