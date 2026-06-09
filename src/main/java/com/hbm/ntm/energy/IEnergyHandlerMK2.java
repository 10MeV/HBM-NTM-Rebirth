package com.hbm.ntm.energy;

import com.hbm.ntm.api.tile.LoadedTile;
import com.hbm.ntm.compat.CompatEnergyControl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Legacy-name bridge for Energy MK2 providers and receivers.
 */
@Deprecated(forRemoval = false)
public interface IEnergyHandlerMK2 extends IEnergyConnectorMK2, HbmEnergyHandler, LoadedTile {
    boolean particleDebug = false;

    default Vec3 getDebugParticlePosMK2() {
        if (this instanceof BlockEntity blockEntity) {
            return Vec3.atBottomCenterOf(blockEntity.getBlockPos()).add(0.0D, 1.0D, 0.0D);
        }
        return Vec3.ZERO;
    }

    default void provideInfoForECMK2(CompoundTag data) {
        if (data != null) {
            data.putLong(CompatEnergyControl.L_ENERGY_HE, getPower());
            data.putLong(CompatEnergyControl.L_CAPACITY_HE, getMaxPower());
        }
    }

    @Override
    default boolean isLoaded() {
        return !(this instanceof BlockEntity blockEntity) || (!blockEntity.isRemoved() && blockEntity.getLevel() != null);
    }
}
