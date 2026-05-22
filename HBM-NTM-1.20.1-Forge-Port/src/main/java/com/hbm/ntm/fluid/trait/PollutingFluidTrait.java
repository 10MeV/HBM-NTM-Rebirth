package com.hbm.ntm.fluid.trait;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class PollutingFluidTrait extends FluidTrait {
    private final Map<PollutionKind, Float> releasePollution = new EnumMap<>(PollutionKind.class);
    private final Map<PollutionKind, Float> burnPollution = new EnumMap<>(PollutionKind.class);

    public PollutingFluidTrait release(PollutionKind kind, float amountPerMb) {
        releasePollution.put(kind, amountPerMb);
        return this;
    }

    public PollutingFluidTrait burn(PollutionKind kind, float amountPerMb) {
        burnPollution.put(kind, amountPerMb);
        return this;
    }

    public Map<PollutionKind, Float> getReleasePollution() {
        return Collections.unmodifiableMap(releasePollution);
    }

    public Map<PollutionKind, Float> getBurnPollution() {
        return Collections.unmodifiableMap(burnPollution);
    }

    public enum PollutionKind {
        SOOT,
        POISON,
        HEAVY_METAL,
        FALLOUT
    }
}
