package com.hbm.hazard.type;

import com.hbm.ntm.radiation.HazardType;

@Deprecated(forRemoval = false)
public class HazardTypeExplosive extends HazardTypeBase {
    public HazardTypeExplosive() {
        super(HazardType.EXPLOSIVE);
    }
}
