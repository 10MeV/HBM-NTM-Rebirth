package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name bridge for Fluid MK2 pipe node creation.
 */
@Deprecated(forRemoval = false)
public interface IFluidPipeMK2 extends IFluidConnectorMK2 {
    default FluidNode createNode(FluidType type) {
        if (this instanceof BlockEntity blockEntity) {
            return createNode(type, blockEntity.getLevel(), blockEntity.getBlockPos());
        }
        return new FluidNode(type, BlockPos.ZERO);
    }

    default FluidNode createNode(FluidType type, Level level, BlockPos pos) {
        // 1.7.10 IFluidPipeMK2#createNode declares all six DirPos endpoints
        // unconditionally. Its bridge overload must not turn current neighbour
        // visibility into a different node topology.
        return new FluidNode(type, pos == null ? BlockPos.ZERO : pos);
    }
}
