package com.hbm.ntm.api.block;

@Deprecated
public interface MiningDrill {
    DrillType getDrillTier();

    int getDrillRating();

    enum DrillType {
        PRIMITIVE,
        INDUSTRIAL,
        HITECH
    }
}
