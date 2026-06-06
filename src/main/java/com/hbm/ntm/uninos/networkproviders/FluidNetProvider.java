package com.hbm.ntm.uninos.networkproviders;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.uninos.HbmNetworkProvider;

public final class FluidNetProvider implements HbmNetworkProvider<HbmFluidNode, HbmFluidNet> {
    private final FluidType type;

    public FluidNetProvider(FluidType type) {
        this.type = type == null ? HbmFluids.NONE : type;
    }

    public FluidType getType() {
        return type;
    }

    @Override
    public HbmFluidNet provideNetwork(HbmFluidNode seedNode) {
        return new HbmFluidNet(type);
    }
}
