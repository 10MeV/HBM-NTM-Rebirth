package com.hbm.ntm.compat;

import com.hbm.ntm.blockentity.HbmFluidBlockEntity;
import com.hbm.ntm.energy.HbmEnergyHandler;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.multiblock.MultiblockHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Stable HBM-side compat facade for external integrations and debug commands.
 */
public final class CompatExternal {
    @Nullable
    public static BlockEntity getCoreFromPos(Level level, BlockPos pos) {
        if (level == null || pos == null || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
            return null;
        }
        return MultiblockHelper.resolveCoreBlockEntity(level, pos);
    }

    @Nullable
    public static BlockEntity getCoreFromTile(BlockEntity blockEntity) {
        return blockEntity == null ? null : MultiblockHelper.resolveCoreBlockEntity(blockEntity);
    }

    public static long getBufferedPowerFromTile(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        return resolved instanceof HbmEnergyHandler handler ? handler.getPower() : 0L;
    }

    public static long getMaxPowerFromTile(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        return resolved instanceof HbmEnergyHandler handler ? handler.getMaxPower() : 0L;
    }

    /**
     * Legacy external ordinal: 0 = low, 1 = normal, 2 = high, -1 = not applicable.
     */
    public static int getEnergyPriorityFromTile(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        if (!(resolved instanceof HbmEnergyReceiver receiver)) {
            return -1;
        }
        return switch (receiver.getPriority()) {
            case LOWEST, LOW -> 0;
            case NORMAL -> 1;
            case HIGH, HIGHEST -> 2;
        };
    }

    /**
     * Returns legacy-style tank rows: [fluidName, fluidId, color, fill, capacity].
     */
    public static ArrayList<Object[]> getFluidInfoFromTile(BlockEntity blockEntity) {
        ArrayList<Object[]> rows = new ArrayList<>();
        for (HbmFluidTank tank : getAllTanks(blockEntity)) {
            FluidType type = tank.getTankType();
            rows.add(new Object[] {
                    type.getName(),
                    type.getId(),
                    type.getColor(),
                    tank.getFill(),
                    tank.getMaxFill()
            });
        }
        return rows;
    }

    public static List<HbmFluidTank> getAllTanks(BlockEntity blockEntity) {
        BlockEntity resolved = getCoreFromTile(blockEntity);
        if (!(resolved instanceof HbmFluidBlockEntity fluidBlockEntity)) {
            return List.of();
        }
        return fluidBlockEntity.getAllTanks().stream()
                .filter(tank -> tank.getTankType() != HbmFluids.NONE)
                .toList();
    }

    private CompatExternal() {
    }
}
