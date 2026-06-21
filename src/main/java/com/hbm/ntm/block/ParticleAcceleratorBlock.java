package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.blockentity.PABeamlineBlockEntity;
import com.hbm.ntm.blockentity.PABlockEntity;
import com.hbm.ntm.multiblock.LegacyMultiblockLayout;
import com.hbm.ntm.multiblock.LegacyProxyMode;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ParticleAcceleratorBlock extends LegacyXrMultiblockBlock implements EntityBlock, Toolable {
    private final Variant variant;

    public ParticleAcceleratorBlock(Properties properties, Variant variant) {
        super(properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    protected int[] getLegacyXrDimensions() {
        return variant.dimensions;
    }

    @Override
    protected int getLegacyOffset() {
        return 0;
    }

    @Override
    protected int getLegacyHeightOffset() {
        return variant.heightOffset;
    }

    @Override
    protected LegacyMultiblockLayout getLayout(BlockState state) {
        Direction facing = state.getValue(FACING);
        LegacyMultiblockLayout layout = LegacyMultiblockLayout.ofLegacyXrChecked(variant.dimensions, facing);
        return switch (variant) {
            case BEAMLINE -> layout;
            case RFC -> layout.withExtraProxyOffsets(rfcExtraOffsets(facing), proxyPowerFluid());
            case QUADRUPOLE -> layout.withExtraProxyOffsets(quadrupoleExtraOffsets(facing), proxyPowerFluid());
            case DIPOLE -> layout.withExtraProxyOffsets(dipoleExtraOffsets(), proxyPowerFluid());
            case SOURCE -> layout.withExtraProxyOffsets(sourceExtraOffsets(facing), proxyInventoryPowerFluid());
            case DETECTOR -> layout.withExtraProxyOffsets(detectorExtraOffsets(facing), proxyInventoryPowerFluid());
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return PABlockEntity.create(variant, pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide
                ? (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof PABlockEntity pa) {
                        pa.clientTick();
                    }
                }
                : (tickLevel, tickPos, tickState, blockEntity) -> {
                    if (blockEntity instanceof PABlockEntity pa) {
                        pa.serverTick();
                    }
                };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (variant == Variant.BEAMLINE) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity core = MultiblockHelper.resolveCoreBlockEntity(level, pos);
        if (core instanceof MenuProvider provider && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, provider, core.getBlockPos());
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (variant != Variant.BEAMLINE || tool != ToolType.SCREWDRIVER) {
            return false;
        }
        if (!level.isClientSide) {
            BlockEntity core = MultiblockHelper.resolveCoreBlockEntity(level, pos);
            if (core instanceof PABeamlineBlockEntity beamline) {
                beamline.toggleWindow();
            }
        }
        return true;
    }

    @Override
    protected Direction getFacingForPlacement(BlockPlaceContext context) {
        return context.getHorizontalDirection().getOpposite();
    }

    private static LegacyProxyMode proxyPowerFluid() {
        return LegacyProxyMode.passive().powerProxy().fluidProxy();
    }

    private static LegacyProxyMode proxyInventoryPowerFluid() {
        return LegacyProxyMode.passive().inventoryProxy().powerProxy().fluidProxy();
    }

    private static List<BlockPos> rfcExtraOffsets(Direction facing) {
        Direction side = clockwiseDirection(facing);
        return List.of(
                rel(side, 3).above(),
                rel(side, -3).above(),
                BlockPos.ZERO.above(),
                rel(side, 3).below(),
                rel(side, -3).below(),
                BlockPos.ZERO.below());
    }

    private static List<BlockPos> quadrupoleExtraOffsets(Direction facing) {
        return List.of(rel(facing, 1), rel(facing, -1), BlockPos.ZERO.above(), BlockPos.ZERO.below());
    }

    private static List<BlockPos> dipoleExtraOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            offsets.add(new BlockPos(direction.getStepX(), -1, direction.getStepZ()));
            offsets.add(new BlockPos(direction.getStepX(), 1, direction.getStepZ()));
        }
        return offsets;
    }

    private static List<BlockPos> sourceExtraOffsets(Direction facing) {
        Direction side = clockwiseDirection(facing);
        return List.of(
                rel(side, 4),
                rel(facing, 1),
                rel(facing, 1).offset(rel(side, 2)),
                rel(facing, 1).offset(rel(side, -2)),
                rel(facing, -1),
                rel(facing, -1).offset(rel(side, 2)),
                rel(facing, -1).offset(rel(side, -2)));
    }

    private static List<BlockPos> detectorExtraOffsets(Direction facing) {
        Direction side = clockwiseDirection(facing);
        BlockPos far = rel(side, -4);
        return List.of(
                far,
                far.above(),
                far.below(),
                far.offset(rel(facing, 1)),
                far.offset(rel(facing, -1)));
    }

    private static Direction clockwiseDirection(Direction facing) {
        BlockPos offset = LegacyMultiblockLayout.clockwise(facing);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (direction.getStepX() == offset.getX() && direction.getStepZ() == offset.getZ()) {
                return direction;
            }
        }
        return facing.getClockWise();
    }

    private static BlockPos rel(Direction direction, int distance) {
        return new BlockPos(direction.getStepX() * distance, direction.getStepY() * distance,
                direction.getStepZ() * distance);
    }

    public enum Variant {
        SOURCE(new int[] { 1, 1, 1, 1, 4, 4 }, 1),
        BEAMLINE(new int[] { 0, 0, 0, 0, 1, 1 }, 0),
        RFC(new int[] { 1, 1, 1, 1, 4, 4 }, 1),
        QUADRUPOLE(new int[] { 1, 1, 1, 1, 1, 1 }, 1),
        DIPOLE(new int[] { 1, 1, 1, 1, 1, 1 }, 1),
        DETECTOR(new int[] { 2, 2, 2, 2, 4, 4 }, 2);

        private final int[] dimensions;
        private final int heightOffset;

        Variant(int[] dimensions, int heightOffset) {
            this.dimensions = dimensions;
            this.heightOffset = heightOffset;
        }
    }
}
