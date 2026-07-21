package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RailSwitchBlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.item.LegacyTrainItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct modern carrier for {@code RailStandardSwitch} and
 * {@code RailStandardSwitchFlipped}. The deliberately mismatched flipped
 * preflight/fill footprints are source behaviour, not a modern typo.
 */
public final class LegacyRailSwitchBlock extends LegacyRailWaypointBlock implements EntityBlock {
    private static final int[] BASE_DIMENSIONS = {0, 0, 7, 7, 1, 0};
    private final Variant variant;

    public enum Variant {
        LEFT(false),
        RIGHT(true);

        private final boolean flipped;

        Variant(boolean flipped) {
            this.flipped = flipped;
        }
    }

    public LegacyRailSwitchBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
        RailDef main = addRailDef("main");
        main.addNode(-8.5D, 0.1875D, 0.5D).addNode(-7.5D, 0.1875D, 0.5D)
                .addNode(6.5D, 0.1875D, 0.5D).addNode(7.5D, 0.1875D, 0.5D)
                .addNode(8.5D, 0.1875D, 0.5D);

        RailDef side = addRailDef("side");
        double[] sideZ = variant.flipped
                ? new double[] {-3.5D, -3.5D, -3.5D, -3.5D, -3.5D, -3.5D, -3.5D, -3.5D,
                        -3.25D, -2.9375D, -2.375D, -1.4625D, -0.75D, -0.1875D, 0.175D, 0.375D, 0.5D, 0.5D}
                : new double[] {4.5D, 4.5D, 4.5D, 4.5D, 4.5D, 4.5D, 4.5D, 4.5D,
                        4.25D, 3.9375D, 3.375D, 2.4625D, 1.75D, 1.1875D, 0.875D, 0.625D, 0.5D, 0.5D};
        for (int index = 0; index < sideZ.length; index++) {
            side.addNode(-8.5D + index, 0.1875D, sideZ[index]);
        }
    }

    public boolean isFlipped() {
        return variant.flipped;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return BASE_DIMENSIONS;
    }

    @Override
    protected int getLegacyOffset() {
        return 7;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        return super.getLayout(state).withExtraOffsets(branchOffsets(state.getValue(FACING), variant.flipped));
    }

    @Override
    public boolean canPlaceDirectMultiblock(Level level, BlockPos corePos, BlockPos temporaryPos, BlockState state) {
        Direction facing = state.getValue(FACING);
        List<BlockPos> preflight = new ArrayList<>(LegacyMultiblockLayout
                .ofLegacyXrChecked(BASE_DIMENSIONS, facing).checkOffsets());
        // RailStandardSwitchFlipped inherited the left-switch preflight, yet
        // fillSpace wrote the opposite branch under BlockDummyable.safeRem.
        preflight.addAll(branchOffsets(facing, false));
        return MultiblockHelper.checkSpace(level, corePos, preflight, temporaryPos);
    }

    @Override
    protected boolean usesUncheckedLegacyDummyFill(BlockState state) {
        return variant.flipped;
    }

    @Override
    protected BlockState getLegacyDummyState(BlockState coreState, BlockPos offset) {
        return ModBlocks.RAIL_DUMMY.get().defaultBlockState()
                .setValue(RailDummyBlock.FACING, dummyFacing(coreState.getValue(FACING), offset, variant.flipped));
    }

    @Override
    protected boolean canCross(Level level, BlockPos corePos, BlockState coreState, Vec3 from, Vec3 to,
            RailDef definition) {
        if (!(level.getBlockEntity(corePos) instanceof RailSwitchBlockEntity switchEntity)) {
            return true;
        }
        Direction direction = coreState.getValue(FACING);
        if (direction == Direction.EAST && from.x < to.x) return true;
        if (direction == Direction.WEST && from.x > to.x) return true;
        if (direction == Direction.SOUTH && from.z < to.z) return true;
        if (direction == Direction.NORTH && from.z > to.z) return true;

        double boundary = 7.0D;
        if (direction == Direction.EAST && to.x < corePos.getX() + 0.5D + boundary) return true;
        if (direction == Direction.WEST && to.x > corePos.getX() + 0.5D - boundary) return true;
        if (direction == Direction.SOUTH && to.z < corePos.getZ() + 0.5D + boundary) return true;
        if (direction == Direction.NORTH && to.z > corePos.getZ() + 0.5D - boundary) return true;

        return switchEntity.isSwitched() ? "side".equals(definition.name()) : "main".equals(definition.name());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).getItem() instanceof LegacyTrainItem) {
            return InteractionResult.PASS;
        }
        BlockPos corePos = MultiblockHelper.resolveCorePos(level, pos);
        if (level.getBlockEntity(corePos) instanceof RailSwitchBlockEntity switchEntity) {
            switchEntity.toggle();
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RailSwitchBlockEntity(pos, state);
    }

    private static List<BlockPos> branchOffsets(Direction facing, boolean flipped) {
        Direction backward = facing.getOpposite();
        Direction sideways = flipped ? facing.getCounterClockWise() : facing.getClockWise();
        int lateralShift = flipped ? -1 : 0;
        List<BlockPos> offsets = new ArrayList<>();
        addLine(offsets, backward, sideways, 2, 4, 2 + lateralShift);
        addLine(offsets, backward, sideways, 4, 2, 3 + lateralShift);
        offsets.add(offset(backward, sideways, 5, 4 + lateralShift));
        for (int forward = 6; forward < 8; forward++) {
            for (int lateral = 3; lateral < 5; lateral++) {
                offsets.add(offset(backward, sideways, forward, lateral + lateralShift));
            }
        }
        offsets.add(offset(backward, sideways, 7, 5 + lateralShift));
        for (int forward = 8; forward < 15; forward++) {
            for (int lateral = 4; lateral < 6; lateral++) {
                offsets.add(offset(backward, sideways, forward, lateral + lateralShift));
            }
        }
        return offsets;
    }

    private static void addLine(List<BlockPos> offsets, Direction forward, Direction sideways,
            int startForward, int length, int sidewaysDistance) {
        for (int index = 0; index < length; index++) {
            offsets.add(offset(forward, sideways, startForward + index, sidewaysDistance));
        }
    }

    private static BlockPos offset(Direction forward, Direction sideways, int forwardDistance, int sidewaysDistance) {
        return new BlockPos(forward.getStepX() * forwardDistance + sideways.getStepX() * sidewaysDistance, 0,
                forward.getStepZ() * forwardDistance + sideways.getStepZ() * sidewaysDistance);
    }

    private static Direction dummyFacing(Direction facing, BlockPos target, boolean flipped) {
        Direction backward = facing.getOpposite();
        Direction sideways = flipped ? facing.getCounterClockWise() : facing.getClockWise();
        int shift = flipped ? -1 : 0;
        for (int d = 2; d < 6; d++) if (target.equals(offset(backward, sideways, d, 2 + shift))) return sideways;
        for (int d = 4; d < 6; d++) if (target.equals(offset(backward, sideways, d, 3 + shift))) return sideways;
        if (target.equals(offset(backward, sideways, 5, 4 + shift))) return sideways;
        for (int d = 6; d < 8; d++) for (int r = 3; r < 5; r++)
            if (target.equals(offset(backward, sideways, d, r + shift))) return backward;
        if (target.equals(offset(backward, sideways, 7, 5 + shift))) return sideways;
        for (int d = 8; d < 15; d++) for (int r = 4; r < 6; r++)
            if (target.equals(offset(backward, sideways, d, r + shift))) return backward;
        return facing;
    }
}
