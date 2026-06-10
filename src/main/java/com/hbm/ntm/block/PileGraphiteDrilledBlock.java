package com.hbm.ntm.block;

import com.hbm.ntm.neutron.PileGraphiteInsertionPlanner;

public class PileGraphiteDrilledBlock extends PileGraphiteDrilledBaseBlock {
    public PileGraphiteDrilledBlock(Properties properties) {
        super(properties, PileGraphiteInsertionPlanner.GraphiteBlockKind.DRILLED);
    }
}
