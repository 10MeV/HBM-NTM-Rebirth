package com.hbm.ntm.explosion.vnt.interfaces;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;

@FunctionalInterface
public interface EntityRangeMutator {
    float mutateRange(ExplosionVnt explosion, float range);
}
