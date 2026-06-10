package com.hbm.ntm.block;

import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;

public class PileGraphiteBreedingProductBlock extends PileGraphiteDrilledBaseBlock {
    public PileGraphiteBreedingProductBlock(Properties properties) {
        super(properties, PileGraphiteInsertionPlanner.GraphiteBlockKind.TRITIUM);
    }
}
