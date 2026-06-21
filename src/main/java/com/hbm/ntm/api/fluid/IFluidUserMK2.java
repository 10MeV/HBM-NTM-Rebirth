package com.hbm.ntm.api.fluid;

import com.hbm.ntm.api.tile.LoadedTile;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUser;
import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name bridge for Fluid MK2 users with tanks.
 */
@Deprecated(forRemoval = false)
public interface IFluidUserMK2 extends IFluidConnectorMK2, HbmFluidUser, LoadedTile {
    int HIGHEST_VALID_PRESSURE = HbmFluidUser.HIGHEST_VALID_PRESSURE;
    int[] DEFAULT_PRESSURE_RANGE = HbmFluidUser.DEFAULT_PRESSURE_RANGE;
    boolean particleDebug = false;

    @Override
    List<HbmFluidTank> getAllTanks();

    @Override
    default boolean isLoaded() {
        return !(this instanceof BlockEntity blockEntity) || (!blockEntity.isRemoved() && blockEntity.getLevel() != null);
    }
}
