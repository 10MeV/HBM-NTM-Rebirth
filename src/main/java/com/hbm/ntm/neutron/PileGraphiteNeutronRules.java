package com.hbm.ntm.neutron;

public final class PileGraphiteNeutronRules {
    private PileGraphiteNeutronRules() {
    }

    public static PileNeutronBlockResult boronRodResult(int meta) {
        return PileGraphiteMetadata.isActive(meta) ? PileNeutronBlockResult.pass() : PileNeutronBlockResult.halt();
    }

    public static boolean detectorAllowsPassthrough(int meta) {
        return PileGraphiteMetadata.isActive(meta);
    }
}
