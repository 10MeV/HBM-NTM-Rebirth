package com.hbm.ntm.uninos.networkproviders.pneumatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public final class PneumaticUtil {
    public static final int PRESSURE_NONE_RANGE = 0;
    public static final int PRESSURE_LOW_RANGE = 10;
    public static final int PRESSURE_MEDIUM_RANGE = 25;
    public static final int PRESSURE_HIGH_RANGE = 100;
    public static final int PRESSURE_VERY_HIGH_RANGE = 250;
    public static final int PRESSURE_MAX_RANGE = 1_000;

    public static Optional<PneumaticItemAccess> itemAccess(Level level, BlockPos pos, @Nullable Direction side) {
        if (level == null || pos == null) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved()) {
            return Optional.empty();
        }
        Optional<IItemHandler> handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve();
        return handler.map(itemHandler -> new PneumaticItemAccess(itemHandler, pos.immutable()));
    }

    public static Optional<PneumaticItemAccess> sourceAccess(Level level, BlockPos tubePos, Direction insertionDirection) {
        if (insertionDirection == null) {
            return Optional.empty();
        }
        BlockPos sourcePos = tubePos.relative(insertionDirection);
        return itemAccess(level, sourcePos, insertionDirection.getOpposite());
    }

    public static Optional<PneumaticReceiver> receiver(Level level, BlockPos tubePos, Direction ejectionDirection, PneumaticEndpoint endpoint) {
        if (ejectionDirection == null || endpoint == null) {
            return Optional.empty();
        }
        BlockPos receiverPos = tubePos.relative(ejectionDirection);
        return itemAccess(level, receiverPos, ejectionDirection.getOpposite())
                .map(access -> new PneumaticReceiver(access.handler(), ejectionDirection, endpoint, access));
    }

    public static int rangeForPressure(int pressure) {
        return switch (pressure) {
            case 1 -> PRESSURE_LOW_RANGE;
            case 2 -> PRESSURE_MEDIUM_RANGE;
            case 3 -> PRESSURE_HIGH_RANGE;
            case 4 -> PRESSURE_VERY_HIGH_RANGE;
            case 5 -> PRESSURE_MAX_RANGE;
            default -> PRESSURE_NONE_RANGE;
        };
    }

    public static Set<Direction> allConnections() {
        return EnumSet.allOf(Direction.class);
    }

    public static int identifier(BlockPos pos) {
        return (pos.getY() + pos.getZ() * 27_644_437) * 27_644_437 + pos.getX();
    }

    private PneumaticUtil() {
    }
}
