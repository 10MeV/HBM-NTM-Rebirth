package com.hbm.ntm.api.block;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class LegacyLookOverlayPorts {
    @Nullable
    public static LegacyLookOverlay factoryMachinePort(BlockEntity blockEntity, BlockPos viewedPos) {
        return match(blockEntity, viewedPos, factoryMachinePorts(blockEntity));
    }

    public static List<LegacyLookOverlayPort> factoryMachinePorts(BlockEntity blockEntity) {
        Direction facing = facing(blockEntity);
        Direction rot = facing.getClockWise();
        List<LegacyLookOverlayPort> ports = new ArrayList<>();
        ports.add(port(rot.getStepX() + facing.getStepX() * 3, 0,
                rot.getStepZ() + facing.getStepZ() * 3, facing, () -> List.of(
                    LegacyLookOverlayLines.fluidPort(true, HbmFluids.WATER),
                    LegacyLookOverlayLines.fluidPort(false, HbmFluids.SPENTSTEAM))));
        ports.add(port(-rot.getStepX() + facing.getStepX() * 3, 0,
                -rot.getStepZ() + facing.getStepZ() * 3, facing, () -> List.of(
                    LegacyLookOverlayLines.fluidPort(true, HbmFluids.WATER),
                    LegacyLookOverlayLines.fluidPort(false, HbmFluids.SPENTSTEAM))));
        ports.add(port(rot.getStepX() - facing.getStepX() * 3, 0,
                rot.getStepZ() - facing.getStepZ() * 3, facing.getOpposite(), () -> List.of(
                    LegacyLookOverlayLines.fluidPort(true, HbmFluids.WATER),
                    LegacyLookOverlayLines.fluidPort(false, HbmFluids.SPENTSTEAM))));
        ports.add(port(-rot.getStepX() - facing.getStepX() * 3, 0,
                -rot.getStepZ() - facing.getStepZ() * 3, facing.getOpposite(), () -> List.of(
                    LegacyLookOverlayLines.fluidPort(true, HbmFluids.WATER),
                    LegacyLookOverlayLines.fluidPort(false, HbmFluids.SPENTSTEAM))));

        ports.add(port(facing.getStepX() + rot.getStepX() * 3, 0,
                facing.getStepZ() + rot.getStepZ() * 3, rot,
                () -> List.of(LegacyLookOverlayLines.recipeField(1))));
        ports.add(port(-facing.getStepX() + rot.getStepX() * 3, 0,
                -facing.getStepZ() + rot.getStepZ() * 3, rot,
                () -> List.of(LegacyLookOverlayLines.recipeField(2))));
        ports.add(port(facing.getStepX() - rot.getStepX() * 3, 0,
                facing.getStepZ() - rot.getStepZ() * 3, rot.getOpposite(),
                () -> List.of(LegacyLookOverlayLines.recipeField(3))));
        ports.add(port(-facing.getStepX() - rot.getStepX() * 3, 0,
                -facing.getStepZ() - rot.getStepZ() * 3, rot.getOpposite(),
                () -> List.of(LegacyLookOverlayLines.recipeField(4))));
        return List.copyOf(ports);
    }

    @Nullable
    public static LegacyLookOverlay match(BlockEntity blockEntity, BlockPos viewedPos, List<LegacyLookOverlayPort> ports) {
        for (LegacyLookOverlayPort port : ports) {
            if (port.matches(blockEntity.getBlockPos(), viewedPos)) {
                return LegacyLookOverlay.forBlock(blockEntity, port.lines().get());
            }
        }
        return null;
    }

    public static boolean isFactoryCoolPort(BlockEntity blockEntity, BlockPos viewedPos) {
        Direction facing = facing(blockEntity);
        Direction rot = facing.getClockWise();
        BlockPos core = blockEntity.getBlockPos();
        return portMatches(core.offset(rot.getStepX() + facing.getStepX() * 3, 0,
                        rot.getStepZ() + facing.getStepZ() * 3), facing, viewedPos)
                || portMatches(core.offset(-rot.getStepX() + facing.getStepX() * 3, 0,
                        -rot.getStepZ() + facing.getStepZ() * 3), facing, viewedPos)
                || portMatches(core.offset(rot.getStepX() - facing.getStepX() * 3, 0,
                        rot.getStepZ() - facing.getStepZ() * 3), facing.getOpposite(), viewedPos)
                || portMatches(core.offset(-rot.getStepX() - facing.getStepX() * 3, 0,
                        -rot.getStepZ() - facing.getStepZ() * 3), facing.getOpposite(), viewedPos);
    }

    public static int factoryRecipePort(BlockEntity blockEntity, BlockPos viewedPos) {
        Direction facing = facing(blockEntity);
        Direction rot = facing.getClockWise();
        BlockPos core = blockEntity.getBlockPos();
        BlockPos[] ports = new BlockPos[] {
                core.offset(facing.getStepX() + rot.getStepX() * 3, 0,
                        facing.getStepZ() + rot.getStepZ() * 3),
                core.offset(-facing.getStepX() + rot.getStepX() * 3, 0,
                        -facing.getStepZ() + rot.getStepZ() * 3),
                core.offset(facing.getStepX() - rot.getStepX() * 3, 0,
                        facing.getStepZ() - rot.getStepZ() * 3),
                core.offset(-facing.getStepX() - rot.getStepX() * 3, 0,
                        -facing.getStepZ() - rot.getStepZ() * 3)
        };
        Direction[] directions = new Direction[] {rot, rot, rot.getOpposite(), rot.getOpposite()};
        for (int i = 0; i < ports.length; i++) {
            if (portMatches(ports[i], directions[i], viewedPos)) {
                return i + 1;
            }
        }
        return 0;
    }

    private static boolean portMatches(BlockPos portPos, Direction portDirection, BlockPos viewedPos) {
        return viewedPos.relative(portDirection).equals(portPos);
    }

    private static Direction facing(BlockEntity blockEntity) {
        return blockEntity.getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? blockEntity.getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static LegacyLookOverlayPort port(int x, int y, int z, Direction direction,
            java.util.function.Supplier<List<net.minecraft.network.chat.Component>> lines) {
        return new LegacyLookOverlayPort(new BlockPos(x, y, z), direction, lines);
    }

    private LegacyLookOverlayPorts() {
    }
}
