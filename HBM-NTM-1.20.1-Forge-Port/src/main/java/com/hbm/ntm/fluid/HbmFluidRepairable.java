package com.hbm.ntm.fluid;

import com.hbm.ntm.fluid.HbmFluidRepairMaterials.HbmRepairMaterial;
import java.util.List;

/**
 * Shared repair/extinguish surface for legacy fluid machines that used
 * IRepairable in 1.7.10.
 */
public interface HbmFluidRepairable {
    boolean isDamagedForFluidRepair();

    default List<HbmRepairMaterial> getFluidRepairMaterials() {
        return List.of();
    }

    void repairFluidMachine();

    void tryExtinguish(HbmExtinguishType type);
}
